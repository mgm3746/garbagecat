/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.dao;

import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toList;
import static org.eclipselabs.garbagecat.util.Memory.ZERO;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.SafepointEvent;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.SafepointEventSummary;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;

/**
 * <p>
 * Manage storing and retrieving JVM data from streams.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JvmDao {

    private static final Comparator<BlockingEvent> COMPARE_BY_TIMESTAMP = comparing(BlockingEvent::getTimestamp);

    /**
     * The database connection.
     */
    private static Connection connection;

    private static boolean created;

    /**
     * SQL statement(s) to create table.
     */
    private static final String[] TABLES_CREATE_SQL = {
            "create table safepoint_event (id integer identity, time_stamp bigint, trigger_type varchar(64), "
                    + "duration integer, log_entry varchar(500))" };

    private static Memory add(Memory m1, Memory m2) {
        return m1 == null ? nullSafe(m2) : m1.plus(nullSafe(m2));
    }

    private static <T> Stream<Integer> ints(List<T> list, Function<T, Integer> function) {
        return list.stream().map(function).filter(Objects::nonNull);
    }

    private static <T> Stream<Long> longs(List<T> list, Function<T, Long> function) {
        return list.stream().map(function).filter(Objects::nonNull);
    }

    private static Memory nullSafe(Memory memory) {
        return memory == null ? ZERO : memory;
    }

    private static BlockingEvent toBlockingEvent(BlockingEvent e) {
        return e;
    }

    private static SafepointEvent toSafepointEvent(SafepointEvent e) {
        return e;
    }

    /**
     * Analysis property keys.
     */
    private List<Analysis> analysis = new ArrayList<>();

    /**
     * GC events that are blocking.
     */
    private List<BlockingEvent> blockingEvents = new ArrayList<>();

    /**
     * Collector families for JVM run.
     */
    List<CollectorFamily> collectorFamilies = new ArrayList<>();

    /**
     * List of all event types associate with JVM run.
     */
    List<LogEventType> eventTypes = new ArrayList<>();

    /**
     * Number of <code>ParallelCollection</code> with "inverted" parallelism.
     */
    private long invertedParallelismCount;

    /**
     * Used for tracking max heap occupancy outside of <code>BlockingEvent</code>s.
     */
    private int maxHeapOccupancyNonBlocking;

    /**
     * Used for tracking max heap space outside of <code>BlockingEvent</code>s.
     */
    private int maxHeapSpaceNonBlocking;

    /**
     * Used for tracking max perm occupancy outside of <code>BlockingEvent</code>s.
     */
    private int maxPermOccupancyNonBlocking;

    /**
     * Used for tracking max perm space outside of <code>BlockingEvent</code>s.
     */
    private int maxPermSpaceNonBlocking;

    /**
     * JVM memory information.
     */
    private String memory;

    /**
     * The JVM options for the JVM run.
     */
    private String options;

    /**
     * Number of <code>ParallelCollection</code> events.
     */
    private long parallelCount;

    /**
     * Physical memory (bytes).
     */
    private long physicalMemory;

    /**
     * Physical memory free (bytes).
     */
    private long physicalMemoryFree;

    /**
     * Stopped time events.
     */
    private List<ApplicationStoppedTimeEvent> stoppedTimeEvents = new ArrayList<>();

    /**
     * Swap size (bytes).
     * 
     * Initial value is negative to prevent false positives of Analysis.INFO_SWAP_DISABLED.
     */
    private long swap = -1;

    /**
     * Swap free (bytes).
     */
    private long swapFree;

    /**
     * Logging lines that do not match any known GC events.
     */
    private List<String> unidentifiedLogLines = new ArrayList<>();

    /**
     * Safepoint events.
     */
    private List<UnifiedSafepointEvent> unifiedSafepointEvents = new ArrayList<>();

    /**
     * JVM version.
     */
    private String version;

    /**
     * <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    private LogEvent worstInvertedParallelismEvent;

    public JvmDao() {
        if (created) {
            cleanup();
        } else {
            created = true;
        }
        try {
            // Load database driver.
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Failed to load HSQLDB JDBC driver.");
        }

        try {
            // Connect to database.
            connection = DriverManager.getConnection("jdbc:hsqldb:mem:vmdb", "sa", "");
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error accessing database.");
        }

        // Create tables
        Statement statement = null;
        try {
            statement = connection.createStatement();
            for (int i = 0; i < TABLES_CREATE_SQL.length; i++) {
                statement.executeUpdate(TABLES_CREATE_SQL[i]);
            }
        } catch (SQLException e) {
            if (e.getMessage().startsWith("Table already exists")) {
                cleanup();
            } else {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error creating tables.");
            }
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing Statement.");
            }
        }
    }

    public void addAnalysis(Analysis analysis) {
        if (!this.analysis.contains(analysis)) {
            this.analysis.add(analysis);
        }
    }

    public void addBlockingEvent(BlockingEvent event) {
        blockingEvents.add(insertPosition(event), event);
    }

    public void addSafepointEvent(UnifiedSafepointEvent event) {
        unifiedSafepointEvents.add(event);
    }

    public void addStoppedTimeEvent(ApplicationStoppedTimeEvent event) {
        stoppedTimeEvents.add(event);
    }

    /**
     * Cleanup operations.
     */
    public synchronized void cleanup() {
        this.blockingEvents.clear();
        JvmDao.created = false;
    }

    public List<Analysis> getAnalysis() {
        return analysis;
    }

    /**
     * The total number of blocking events.
     * 
     * @return total number of blocking events.
     */
    public synchronized int getBlockingEventCount() {
        return this.blockingEvents.size();
    }

    /**
     * Retrieve all <code>BlockingEvent</code>s.
     * 
     * @return <code>List</code> of events.
     */
    public synchronized List<BlockingEvent> getBlockingEvents() {
        return this.blockingEvents.stream().map(JvmDao::toBlockingEvent).collect(toList());
    }

    /**
     * Retrieve all <code>BlockingEvent</code>s of the specified type.
     * 
     * @param eventType
     *            The event type to retrieve.
     * @return <code>List</code> of events.
     */
    public synchronized List<BlockingEvent> getBlockingEvents(LogEventType eventType) {
        return this.blockingEvents.stream().filter(e -> e.getName().equals(eventType.toString()))
                .map(JvmDao::toBlockingEvent).collect(toList());
    }

    public List<CollectorFamily> getCollectorFamilies() {
        return collectorFamilies;
    }

    public List<LogEventType> getEventTypes() {
        return eventTypes;
    }

    /**
     * The first blocking event.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The first blocking event.
     */
    public synchronized BlockingEvent getFirstGcEvent() {
        // TODO JdkUtil#parseLogLine no longer needed?
        return this.blockingEvents.isEmpty() ? null : this.blockingEvents.get(0);
    }

    /**
     * The first {@link org.eclipselabs.garbagecat.domain.SafepointEvent}.
     * 
     * @return The first <code>SafepointEvent</code>.
     */
    public synchronized SafepointEvent getFirstSafepointEvent() {
        SafepointEvent firstSafepointEvent = null;
        if (!unifiedSafepointEvents.isEmpty()) {
            firstSafepointEvent = getFirstUnifiedSafepointEvent();
        } else if (!stoppedTimeEvents.isEmpty()) {
            firstSafepointEvent = getFirstStoppedEvent();
        }
        return firstSafepointEvent;
    }

    /**
     * The first {@link org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent}.
     * 
     * @return The first stopped event.
     */
    private synchronized ApplicationStoppedTimeEvent getFirstStoppedEvent() {
        return stoppedTimeEvents.isEmpty() ? null : stoppedTimeEvents.get(0);
    }

    /**
     * The first {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent}.
     * 
     * @return The first unified safepoint event.
     */
    private synchronized UnifiedSafepointEvent getFirstUnifiedSafepointEvent() {
        return unifiedSafepointEvents.isEmpty() ? null : unifiedSafepointEvents.get(0);
    }

    /**
     * The total blocking event pause time.
     * 
     * @return total pause duration (microseconds).
     */
    public synchronized long getGcPauseTotal() {
        return longs(this.blockingEvents, BlockingEvent::getDuration).collect(summingLong(Long::valueOf));
    }

    /**
     * @return The number of "inverted" parallelism events.
     */
    public long getInvertedParallelismCount() {
        return invertedParallelismCount;
    }

    /**
     * Retrieve the last blocking event.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The last blocking event.
     */
    public synchronized BlockingEvent getLastGcEvent() {
        return this.blockingEvents.isEmpty() ? null : this.blockingEvents.get(blockingEvents.size() - 1);
    }

    /**
     * The first {@link org.eclipselabs.garbagecat.domain.SafepointEvent}.
     * 
     * @return The last safepoint event.
     */
    public synchronized SafepointEvent getLastSafepointEvent() {
        SafepointEvent lastSafepointEvent = null;
        if (!unifiedSafepointEvents.isEmpty()) {
            lastSafepointEvent = getLastUnifiedSafepointEvent();
        } else if (!stoppedTimeEvents.isEmpty()) {
            lastSafepointEvent = getLastStoppedEvent();
        }
        return lastSafepointEvent;
    }

    /**
     * Retrieve the last stopped event.
     * 
     * @return The last stopped event.
     */
    private synchronized ApplicationStoppedTimeEvent getLastStoppedEvent() {
        return stoppedTimeEvents.isEmpty() ? null : stoppedTimeEvents.get(stoppedTimeEvents.size() - 1);
    }

    /**
     * Retrieve the last safepoint event.
     * 
     * @return The last safepoint event.
     */
    private synchronized UnifiedSafepointEvent getLastUnifiedSafepointEvent() {
        return unifiedSafepointEvents.isEmpty() ? null : unifiedSafepointEvents.get(unifiedSafepointEvents.size() - 1);
    }

    /**
     * The maximum GC blocking event pause time.
     * 
     * @return maximum pause duration (microseconds).
     */
    public synchronized long getMaxGcPause() {
        return longs(this.blockingEvents, BlockingEvent::getDuration).mapToLong(Long::valueOf).max().orElse(0);
    }

    /**
     * The maximum heap after GC during the JVM run.
     * 
     * @return maximum heap after GC (kilobytes).
     */
    public synchronized int getMaxHeapAfterGc() {
        int oldMaxHeapAfterGc = (int) kilobytes(OldData.class,
                t -> add(t.getYoungOccupancyEnd(), t.getOldOccupancyEnd())).max().orElse(0);
        int combinedMaxHeapAfterGc = (int) kilobytes(CombinedData.class, CombinedData::getCombinedOccupancyEnd).max()
                .orElse(0);
        return Math.max(oldMaxHeapAfterGc, combinedMaxHeapAfterGc);
    }

    /**
     * The maximum heap occupancy during the JVM run.
     * 
     * @return maximum heap occupancy (kilobytes).
     */
    public synchronized int getMaxHeapOccupancy() {
        return (int) this.blockingEvents.stream() //
                .map(e -> {
                    if (e instanceof OldData) {
                        OldData old = (OldData) e;
                        return add(old.getYoungOccupancyInit(), old.getOldOccupancyInit());
                    } else if (e instanceof CombinedData) {
                        return ((CombinedData) e).getCombinedOccupancyInit();
                    } else {
                        return ZERO;
                    }
                }) //
                .filter(Objects::nonNull) //
                .mapToLong(m -> m.getValue(KILOBYTES)).max().orElse(0);
    }

    /**
     * @return The maximum heap occupancy in non <code>BlockingEvent</code>s.
     */
    public int getMaxHeapOccupancyNonBlocking() {
        return maxHeapOccupancyNonBlocking;
    }

    /**
     * The maximum heap space size during the JVM run.
     * 
     * @return maximum heap size (kilobytes).
     */
    public synchronized int getMaxHeapSpace() {
        return (int) this.blockingEvents.stream() //
                .map(e -> {
                    if (e instanceof OldData) {
                        OldData old = (OldData) e;
                        return add(old.getYoungSpace(), old.getOldSpace());
                    } else if (e instanceof CombinedData) {
                        return ((CombinedData) e).getCombinedSpace();
                    } else {
                        return ZERO;
                    }
                }) //
                .filter(Objects::nonNull) //
                .mapToLong(m -> m.getValue(KILOBYTES)).max().orElse(0);
    }

    /**
     * @return The maximum heap space in non <code>BlockingEvent</code>s.
     */
    public int getMaxHeapSpaceNonBlocking() {
        return maxHeapSpaceNonBlocking;
    }

    /**
     * The maximum old space size during the JVM run.
     * 
     * @return maximum old space size (kilobytes).
     */
    public synchronized int getMaxOldSpace() {
        return (int) kilobytes(OldData.class, OldData::getOldSpace).max().orElse(0);
    }

    /**
     * The maximum perm/metaspace after GC during the JVM run.
     * 
     * @return maximum perm/metaspac after GC (kilobytes).
     */
    public synchronized int getMaxPermAfterGc() {
        return (int) kilobytes(PermMetaspaceData.class, PermMetaspaceData::getPermOccupancyEnd).max().orElse(0);
    }

    /**
     * The maximum perm/metaspace occupancy during the JVM run.
     * 
     * @return maximum perm/metaspac occupancy (kilobytes).
     */
    public synchronized int getMaxPermOccupancy() {
        return (int) kilobytes(PermMetaspaceData.class, PermMetaspaceData::getPermOccupancyInit).max().orElse(0);
    }

    /**
     * @return The maximum perm occupancy in non <code>BlockingEvent</code>s.
     */
    public int getMaxPermOccupancyNonBlocking() {
        return maxPermOccupancyNonBlocking;
    }

    /**
     * The maximum perm/metaspace size during the JVM run.
     * 
     * @return maximum perm/metaspace footprint (kilobytes).
     */
    public synchronized int getMaxPermSpace() {
        return (int) kilobytes(PermMetaspaceData.class, PermMetaspaceData::getPermSpace).max().orElse(0);
    }

    /**
     * @return The maximum perm space in non <code>BlockingEvent</code>s.
     */
    public int getMaxPermSpaceNonBlocking() {
        return maxPermSpaceNonBlocking;
    }

    /**
     * The maximum young space size during the JVM run.
     * 
     * @return maximum young space size (kilobytes).
     */
    public synchronized int getMaxYoungSpace() {
        return (int) kilobytes(YoungData.class, YoungData::getYoungSpace).max().orElse(0);
    }

    /**
     * @return The JVM memory information.
     */
    public String getMemory() {
        return memory;
    }

    /**
     * @return The JVM options.
     */
    public String getOptions() {
        return options;
    }

    /**
     * @return The number of <code>ParallelCollection</code> events.
     */
    public long getParallelCount() {
        return parallelCount;
    }

    /**
     * @return The JVM environment physical memory (bytes).
     */
    public long getPhysicalMemory() {
        return physicalMemory;
    }

    /**
     * @return The JVM environment physical free memory (bytes).
     */
    public long getPhysicalMemoryFree() {
        return physicalMemoryFree;
    }

    /**
     * Retrieve all <code>SafepointEvent</code>s.
     * 
     * @return <code>List</code> of events.
     */
    public synchronized List<SafepointEvent> getSafepointEvents() {
        if (!this.stoppedTimeEvents.isEmpty()) {
            return this.stoppedTimeEvents.stream().map(JvmDao::toSafepointEvent).collect(toList());
        } else {
            return this.unifiedSafepointEvents.stream().map(JvmDao::toSafepointEvent).collect(toList());
        }
    }

    /**
     * Generate <code>SafepointEventSummary</code>s.
     * 
     * @return <code>List</code> of <code>SafepointEventSummary</code>s.
     */
    public synchronized List<SafepointEventSummary> getSafepointEventSummaries() {
        List<SafepointEventSummary> safepointEventSummaries = new ArrayList<SafepointEventSummary>();

        PreparedStatement pst = null;
        try {
            String sqlInsertSafepointEvent = "insert into safepoint_event (time_stamp, trigger_type, duration, "
                    + "log_entry) values (?, ?, ?, ?)";

            final int TIME_STAMP_INDEX = 1;
            final int TRIGGER_TYPE_INDEX = 2;
            final int DURATION_INDEX = 3;
            final int LOG_ENTRY_INDEX = 4;

            pst = connection.prepareStatement(sqlInsertSafepointEvent);

            for (int i = 0; i < unifiedSafepointEvents.size(); i++) {
                UnifiedSafepointEvent event = unifiedSafepointEvents.get(i);
                pst.setLong(TIME_STAMP_INDEX, event.getTimestamp());
                // Use trigger for event name
                pst.setString(TRIGGER_TYPE_INDEX, event.getTrigger().toString());
                pst.setLong(DURATION_INDEX, event.getDuration());
                pst.setString(LOG_ENTRY_INDEX, event.getLogEntry());
                pst.addBatch();
            }
            pst.executeBatch();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error inserting safepoint event.");
        } finally {
            try {
                pst.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closingPreparedStatement.");
            }
        }

        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            StringBuffer sql = new StringBuffer();
            sql.append("select trigger_type, count(id), sum(duration), max(duration) from safepoint_event group by "
                    + "trigger_type order by sum(duration) desc");
            rs = statement.executeQuery(sql.toString());
            while (rs.next()) {
                Trigger trigger = UnifiedSafepoint.identifyTrigger(rs.getString(1));
                SafepointEventSummary summary = new SafepointEventSummary(trigger, rs.getLong(2),
                        JdkMath.convertMicrosToMillis(rs.getLong(3)).longValue(),
                        JdkMath.convertMicrosToMillis(rs.getInt(4)).intValue());
                safepointEventSummaries.add(summary);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error retrieving safepoint event summaries.");
        } finally {
            try {
                rs.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing ResultSet.");
            }
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing Statement.");
            }
        }
        return safepointEventSummaries;
    }

    /**
     * The total number of stopped time events.
     * 
     * @return total number of stopped time events.
     */
    public synchronized int getStoppedTimeEventCount() {
        return this.stoppedTimeEvents.size();
    }

    /**
     * The maximum stopped time event pause time.
     * 
     * @return maximum pause duration (microseconds).
     */
    public synchronized long getStoppedTimeMax() {
        return longs(this.stoppedTimeEvents, ApplicationStoppedTimeEvent::getDuration).mapToLong(Long::valueOf).max()
                .orElse(0);
    }

    /**
     * The total stopped time event pause time.
     * 
     * @return total pause duration (microseconds).
     */
    public synchronized long getStoppedTimeTotal() {
        return longs(this.stoppedTimeEvents, ApplicationStoppedTimeEvent::getDuration)
                .collect(summingLong(Long::valueOf));
    }

    /**
     * @return The JVM environment swap size (bytes).
     */
    public long getSwap() {
        return swap;
    }

    /**
     * @return The JVM environment swap free (bytes).
     */
    public long getSwapFree() {
        return swapFree;
    }

    public List<String> getUnidentifiedLogLines() {
        return unidentifiedLogLines;
    }

    /**
     * The total number of unifed safepoint events.
     * 
     * @return total number of unified safepoint time events.
     */
    public synchronized int getUnifiedSafepointEventCount() {
        return this.unifiedSafepointEvents.size();
    }

    /**
     * The maximum unified safepoint event pause time.
     * 
     * @return maximum pause duration (microseconds).
     */
    public synchronized long getUnifiedSafepointTimeMax() {
        return longs(this.unifiedSafepointEvents, UnifiedSafepointEvent::getDuration).mapToLong(Long::valueOf).max()
                .orElse(0);
    }

    /**
     * The total unified safepoint event pause time.
     * 
     * @return total pause duration (microseconds).
     */
    public synchronized long getUnifiedSafepointTimeTotal() {
        return longs(this.unifiedSafepointEvents, UnifiedSafepointEvent::getDuration)
                .collect(summingLong(Long::valueOf));
    }

    /**
     * @return The JVM version information.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return The <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    public LogEvent getWorstInvertedParallelismEvent() {
        return worstInvertedParallelismEvent;
    }

    private int insertPosition(BlockingEvent event) {
        int size = blockingEvents.size();
        if (size > 0 && COMPARE_BY_TIMESTAMP.compare(blockingEvents.get(size - 1), event) <= 0) {
            return size;
        }
        // here we could raise an Exception: Add param boolean reorderingAllowed to method
        // if (!reorderingAllowed) throw new TimeWarpException("bad order")
        return -binarySearch(blockingEvents, event, COMPARE_BY_TIMESTAMP) - 1;
    }

    private <T> LongStream kilobytes(Class<T> clazz, Function<T, Memory> func) {
        return this.blockingEvents.stream() //
                .filter(clazz::isInstance) //
                .map(clazz::cast).map(func) //
                .filter(Objects::nonNull) //
                .mapToLong(m -> m.getValue(KILOBYTES));
    }

    /**
     * @param invertedParallelismCount
     *            The number of "low" parallelism events.
     */
    public void setInvertedParallelismCount(long invertedParallelismCount) {
        this.invertedParallelismCount = invertedParallelismCount;
    }

    /**
     * @param maxHeapOccupancyNonBlocking
     *            The maximum heap occupancy in non <code>BlockingEvent</code>s.
     */
    public void setMaxHeapOccupancyNonBlocking(int maxHeapOccupancyNonBlocking) {
        this.maxHeapOccupancyNonBlocking = maxHeapOccupancyNonBlocking;
    }

    /**
     * @param maxHeapSpaceNonBlocking
     *            The maximum heap space in non <code>BlockingEvent</code>s.
     */
    public void setMaxHeapSpaceNonBlocking(int maxHeapSpaceNonBlocking) {
        this.maxHeapSpaceNonBlocking = maxHeapSpaceNonBlocking;
    }

    /**
     * @param maxPermOccupancyNonBlocking
     *            The maximum perm occupancy in non <code>BlockingEvent</code>s.
     */
    public void setMaxPermOccupancyNonBlocking(int maxPermOccupancyNonBlocking) {
        this.maxPermOccupancyNonBlocking = maxPermOccupancyNonBlocking;
    }

    /**
     * @param maxPermSpaceNonBlocking
     *            The maximum perm space in non <code>BlockingEvent</code>s.
     */
    public void setMaxPermSpaceNonBlocking(int maxPermSpaceNonBlocking) {
        this.maxPermSpaceNonBlocking = maxPermSpaceNonBlocking;
    }

    /**
     * @param memory
     *            The JVM memory information to set.
     */
    public void setMemory(String memory) {
        this.memory = memory;
    }

    /**
     * @param options
     *            The JVM options to set.
     */
    public void setOptions(String options) {
        this.options = options;
    }

    /**
     * @param parallelCount
     *            The number of <code>ParallelCollection</code> events.
     */
    public void setParallelCount(long parallelCount) {
        this.parallelCount = parallelCount;
    }

    /**
     * @param physicalMemory
     *            The JVM physical memory to set.
     */
    public void setPhysicalMemory(long physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    /**
     * @param physicalMemoryFree
     *            The JVM physical free memory to set.
     */
    public void setPhysicalMemoryFree(long physicalMemoryFree) {
        this.physicalMemoryFree = physicalMemoryFree;
    }

    /**
     * @param swap
     *            The JVM swap to set.
     */
    public void setSwap(long swap) {
        this.swap = swap;
    }

    /**
     * @param swapFree
     *            The JVM swap free to set.
     */
    public void setSwapFree(long swapFree) {
        this.swapFree = swapFree;
    }

    /**
     * @param version
     *            The JVM version information to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @param worstInvertedParallelismEvent
     *            The <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    public void setWorstInvertedParallelismEvent(LogEvent worstInvertedParallelismEvent) {
        this.worstInvertedParallelismEvent = worstInvertedParallelismEvent;
    }
}
