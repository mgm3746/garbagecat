/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.ClassData;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.SafepointEvent;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsIncrementalModeCollector;
import org.eclipselabs.garbagecat.domain.jdk.unified.SafepointEventSummary;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction.PreprocessEvent;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;
import org.github.joa.domain.JvmContext;

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
                    + "duration bigint, log_entry varchar(500))" };

    private static final String[] TABLES_DELETE_SQL = { "delete from safepoint_event " };

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
    private List<Analysis> analysis = new ArrayList<Analysis>();

    /**
     * GC events that are blocking.
     */
    private List<BlockingEvent> blockingEvents = new ArrayList<>();

    /**
     * List of all event types associate with the JVM run.
     */
    List<LogEventType> eventTypes = new ArrayList<>();

    /**
     * Maximum external root scanning time (microseconds).
     */
    private long extRootScanningTimeMax;

    /**
     * Total external root scanning time (microseconds).
     */
    private long extRootScanningTimeTotal;

    /**
     * The first log event.
     */
    private LogEvent firstLogEvent;

    /**
     * List of all GC triggers associate with the JVM run.
     */
    List<GcTrigger> gcTriggers = new ArrayList<>();

    /**
     * Number of <code>ParallelCollection</code> with "inverted" parallelism.
     */
    private long invertedParallelismCount;

    /**
     * Number of <code>SerialCollection</code> with "inverted" serialism.
     */
    private long invertedSerialismCount;

    /**
     * The JVM context.
     */
    private JvmContext jvmContext = new JvmContext(null);

    /**
     * Whether or not the logging ends with <code>UnknownEvent</code>s (e.g. it's truncated).
     */
    private boolean logEndingUnidentified = false;

    /**
     * The date and time the log file was created.
     */
    private Date logFileDate;

    /**
     * Used for tracking max perm space or metaspace outside of <code>BlockingEvent</code>s.
     */
    private int maxClassSpaceNonBlocking;

    /**
     * Used for tracking max perm space or metaspace occupancy outside of <code>BlockingEvent</code>s.
     */
    private int maxClassSpaceOccupancyNonBlocking;

    /**
     * Used for tracking max heap occupancy outside of <code>BlockingEvent</code>s.
     */
    private int maxHeapOccupancyNonBlocking;

    /**
     * Used for tracking max heap space outside of <code>BlockingEvent</code>s.
     */
    private int maxHeapNonBlocking;

    /**
     * Used for tracking max heap space outside of <code>BlockingEvent</code>s.
     */
    private int maxHeapAfterGcNonBlocking;

    /**
     * Used for tracking max heap space outside of <code>BlockingEvent</code>s.
     */
    private int maxClassSpaceAfterGcNonBlocking;

    /**
     * JVM memory information.
     */
    private String memory;

    /**
     * Maximum "Other" time (microseconds).
     */
    private long otherTimeMax;

    /**
     * Total "Other" time (microseconds).
     */
    private long otherTimeTotal;

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
     * List of all preparsing events associate with the JVM run.
     */
    List<PreprocessEvent> preprocessEvents = new ArrayList<>();

    /**
     * Number of <code>SerialCollection</code> events.
     */
    private long serialCount;

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
     * Number of<code>ParallelCollection</code> or <code>Serial Collection</code> where sys exceeds user time.
     */
    private long sysGtUserCount;

    /**
     * Logging lines that do not match any known GC events.
     */
    private List<String> unidentifiedLogLines = new ArrayList<>();

    /**
     * Safepoint events.
     */
    private List<UnifiedSafepointEvent> unifiedSafepointEvents = new ArrayList<>();

    /**
     * Convenience field for vm_info.
     */
    private String vmInfo;

    /**
     * <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    private LogEvent worstInvertedParallelismEvent;

    /**
     * <code>SerialCollection</code> event with the lowest "inverted" serialism.
     */
    private LogEvent worstInvertedSerialismEvent;

    /**
     * <code>ParallelCollection</code> or <code>Serial Collection</code> event with the greatest sys - user.
     */
    private LogEvent worstSysGtUserEvent;

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
        // TODO: Remove below when hsqldb dependency removed
        Statement statement = null;
        try {
            statement = connection.createStatement();
            for (int i = 0; i < TABLES_DELETE_SQL.length; i++) {
                statement.executeUpdate(TABLES_DELETE_SQL[i]);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error deleting rows from tables.");
        } finally {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closing Statement.");
            }
        }
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

    /**
     * Retrieve all <code>CmsIncrementalModeCollector</code> events.
     * 
     * @return <code>List</code> of <code>CmsIncrementalModeCollector</code> events.
     */
    public synchronized List<BlockingEvent> getCmsIncrementalModeCollectorEvents() {
        return this.blockingEvents.stream().filter(e -> e instanceof CmsIncrementalModeCollector)
                .map(JvmDao::toBlockingEvent).collect(toList());
    }

    /**
     * The maximum <code>BlockingEvent</code> pause time.
     * 
     * @return maximum pause duration (microseconds).
     */
    public synchronized long getDurationMax() {
        return longs(this.blockingEvents, BlockingEvent::getDurationMicros).mapToLong(Long::valueOf).max().orElse(0);
    }

    /**
     * The total <code>BlockEvent</code> pause time.
     * 
     * @return total pause duration (microseconds).
     */
    public synchronized long getDurationTotal() {
        return longs(this.blockingEvents, BlockingEvent::getDurationMicros).collect(summingLong(Long::valueOf));
    }

    public List<LogEventType> getEventTypes() {
        return eventTypes;
    }

    public long getExtRootScanningTimeMax() {
        return extRootScanningTimeMax;
    }

    public long getExtRootScanningTimeTotal() {
        return extRootScanningTimeTotal;
    }

    /**
     * The first blocking event.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The first blocking event.
     */
    public synchronized BlockingEvent getFirstGcEvent() {
        return this.blockingEvents.isEmpty() ? null : this.blockingEvents.get(0);
    }

    public LogEvent getFirstLogEvent() {
        return firstLogEvent;
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

    public List<GcTrigger> getGcTriggers() {
        return gcTriggers;
    }

    /**
     * @return The number of "inverted" parallelism events.
     */
    public long getInvertedParallelismCount() {
        return invertedParallelismCount;
    }

    /**
     * @return The number of "inverted" serialism events.
     */
    public long getInvertedSerialismCount() {
        return invertedSerialismCount;
    }

    public JvmContext getJvmContext() {
        return jvmContext;
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

    public Date getLogFileDate() {
        return logFileDate;
    }

    /**
     * The maximum perm/metaspace size during the JVM run.
     * 
     * @return maximum perm/metaspace footprint (kilobytes).
     */
    public synchronized int getMaxClassSpace() {
        return (int) kilobytes(ClassData.class, ClassData::getClassSpace).max().orElse(0);
    }

    /**
     * The maximum perm/metaspace after GC during the JVM run.
     * 
     * @return maximum perm/metaspac after GC (kilobytes).
     */
    public synchronized int getMaxClassSpaceAfterGc() {
        return (int) kilobytes(ClassData.class, ClassData::getClassOccupancyEnd).max().orElse(0);
    }

    public int getMaxClassSpaceAfterGcNonBlocking() {
        return maxClassSpaceAfterGcNonBlocking;
    }

    /**
     * @return The maximum perm space or metaspace in non <code>BlockingEvent</code>s.
     */
    public int getMaxClassSpaceNonBlocking() {
        return maxClassSpaceNonBlocking;
    }

    /**
     * The maximum perm/metaspace occupancy during the JVM run.
     * 
     * @return maximum perm/metaspace occupancy (kilobytes).
     */
    public synchronized int getMaxClassSpaceOccupancy() {
        return (int) kilobytes(ClassData.class, ClassData::getClassOccupancyInit).max().orElse(0);
    }

    /**
     * @return The maximum perm occupancy in non <code>BlockingEvent</code>s.
     */
    public int getMaxClassSpaceOccupancyNonBlocking() {
        return maxClassSpaceOccupancyNonBlocking;
    }

    /**
     * The maximum heap space size during the JVM run.
     * 
     * @return maximum heap size (kilobytes).
     */
    public synchronized int getMaxHeap() {
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

    public int getMaxHeapAfterGcNonBlocking() {
        return maxHeapAfterGcNonBlocking;
    }

    /**
     * @return The maximum heap space in non <code>BlockingEvent</code>s.
     */
    public int getMaxHeapNonBlocking() {
        return maxHeapNonBlocking;
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
     * The maximum old space size during the JVM run.
     * 
     * @return maximum old space size (kilobytes).
     */
    public synchronized int getMaxOldSpace() {
        return (int) kilobytes(OldData.class, OldData::getOldSpace).max().orElse(0);
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

    public long getOtherTimeMax() {
        return otherTimeMax;
    }

    public long getOtherTimeTotal() {
        return otherTimeTotal;
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

    public List<PreprocessEvent> getPreprocessEvents() {
        return preprocessEvents;
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
                pst.setLong(DURATION_INDEX, event.getDurationMicros());
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
                SafepointEventSummary summary = new SafepointEventSummary(trigger, rs.getLong(2), rs.getLong(3),
                        rs.getLong(4));
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
     * @return The number of <code>SerialCollection</code> events.
     */
    public long getSerialCount() {
        return serialCount;
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
        return longs(this.stoppedTimeEvents, ApplicationStoppedTimeEvent::getDurationMicros).mapToLong(Long::valueOf)
                .max().orElse(0);
    }

    /**
     * The total stopped time event pause time.
     * 
     * @return total pause duration (microseconds).
     */
    public synchronized long getStoppedTimeTotal() {
        return longs(this.stoppedTimeEvents, ApplicationStoppedTimeEvent::getDurationMicros)
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

    /**
     * @return The number of sys &gt; user time events.
     */
    public long getSysGtUserCount() {
        return sysGtUserCount;
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
     * @return maximum pause duration (nanoseconds).
     */
    public synchronized long getUnifiedSafepointTimeMax() {
        return longs(this.unifiedSafepointEvents, UnifiedSafepointEvent::getDurationNanos).mapToLong(Long::valueOf)
                .max().orElse(0);
    }

    /**
     * The total unified safepoint event pause time.
     * 
     * @return total pause duration (nanoseconds).
     */
    public synchronized long getUnifiedSafepointTimeTotal() {
        return longs(this.unifiedSafepointEvents, UnifiedSafepointEvent::getDurationNanos)
                .collect(summingLong(Long::valueOf));
    }

    public String getVmInfo() {
        return vmInfo;
    }

    /**
     * @return The <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    public LogEvent getWorstInvertedParallelismEvent() {
        return worstInvertedParallelismEvent;
    }

    /**
     * @return The <code>SerialCollection</code> event with the lowest "inverted" serialism.
     */
    public LogEvent getWorstInvertedSerialismEvent() {
        return worstInvertedSerialismEvent;
    }

    /**
     * @return The <code>ParallelCollection</code> or <code>SerialCollection</code> event with the greatest sys - user.
     */
    public LogEvent getWorstSysGtUserEvent() {
        return worstSysGtUserEvent;
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

    public boolean isLogEndingUnidentified() {
        return logEndingUnidentified;
    }

    private <T> LongStream kilobytes(Class<T> clazz, Function<T, Memory> func) {
        return this.blockingEvents.stream() //
                .filter(clazz::isInstance) //
                .map(clazz::cast).map(func) //
                .filter(Objects::nonNull) //
                .mapToLong(m -> m.getValue(KILOBYTES));
    }

    public void setExtRootScanningTimeMax(long extRootScanningTimeMax) {
        this.extRootScanningTimeMax = extRootScanningTimeMax;
    }

    public void setExtRootScanningTimeTotal(long extRootScanningTimeTotal) {
        this.extRootScanningTimeTotal = extRootScanningTimeTotal;
    }

    public void setFirstLogEvent(LogEvent firstLogEvent) {
        this.firstLogEvent = firstLogEvent;
    }

    /**
     * @param invertedParallelismCount
     *            The number of "low" parallelism events.
     */
    public void setInvertedParallelismCount(long invertedParallelismCount) {
        this.invertedParallelismCount = invertedParallelismCount;
    }

    /**
     * @param invertedSerialismCount
     *            The number of "low" serialism events.
     */
    public void setInvertedSerialismCount(long invertedSerialismCount) {
        this.invertedSerialismCount = invertedSerialismCount;
    }

    public void setLogEndingUnidentified(boolean logEndingUnidentified) {
        this.logEndingUnidentified = logEndingUnidentified;
    }

    public void setLogFileDate(Date logFileDate) {
        this.logFileDate = logFileDate;
    }

    public void setMaxClassSpaceAfterGcNonBlocking(int maxClassSpaceAfterGcNonBlocking) {
        this.maxClassSpaceAfterGcNonBlocking = maxClassSpaceAfterGcNonBlocking;
    }

    /**
     * @param maxClassSpaceNonBlocking
     *            The maximum perm space or metaspace in non <code>BlockingEvent</code>s.
     */
    public void setMaxClassSpaceNonBlocking(int maxClassSpaceNonBlocking) {
        this.maxClassSpaceNonBlocking = maxClassSpaceNonBlocking;
    }

    /**
     * @param maxClassSpaceOccupancyNonBlocking
     *            The maximum perm space or metaspace occupancy in non <code>BlockingEvent</code>s.
     */
    public void setMaxClassSpaceOccupancyNonBlocking(int maxClassSpaceOccupancyNonBlocking) {
        this.maxClassSpaceOccupancyNonBlocking = maxClassSpaceOccupancyNonBlocking;
    }

    public void setMaxHeapAfterGcNonBlocking(int maxHeapAfterGcNonBlocking) {
        this.maxHeapAfterGcNonBlocking = maxHeapAfterGcNonBlocking;
    }

    /**
     * @param maxHeapNonBlocking
     *            The maximum heap space in non <code>BlockingEvent</code>s.
     */
    public void setMaxHeapNonBlocking(int maxHeapNonBlocking) {
        this.maxHeapNonBlocking = maxHeapNonBlocking;
    }

    /**
     * @param maxHeapOccupancyNonBlocking
     *            The maximum heap occupancy in non <code>BlockingEvent</code>s.
     */
    public void setMaxHeapOccupancyNonBlocking(int maxHeapOccupancyNonBlocking) {
        this.maxHeapOccupancyNonBlocking = maxHeapOccupancyNonBlocking;
    }

    /**
     * @param memory
     *            The JVM memory information to set.
     */
    public void setMemory(String memory) {
        this.memory = memory;
    }

    public void setOtherTimeMax(long otherTimeMax) {
        this.otherTimeMax = otherTimeMax;
    }

    public void setOtherTimeTotal(long otherTimeTotal) {
        this.otherTimeTotal = otherTimeTotal;
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
     * @param serialCount
     *            The number of <code>SerialCollection</code> events.
     */
    public void setSerialCount(long serialCount) {
        this.serialCount = serialCount;
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
     * @param sysGtUserCount
     *            The number of events with sys &gt; user time.
     */
    public void setSysGtUserCount(long sysGtUserCount) {
        this.sysGtUserCount = sysGtUserCount;
    }

    public void setVmInfo(String vmInfo) {
        this.vmInfo = vmInfo;
    }

    /**
     * @param worstInvertedParallelismEvent
     *            The <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    public void setWorstInvertedParallelismEvent(LogEvent worstInvertedParallelismEvent) {
        this.worstInvertedParallelismEvent = worstInvertedParallelismEvent;
    }

    /**
     * @param worstInvertedSerialismEvent
     *            The <code>SerialCollection</code> event with the lowest "inverted" serialism.
     */
    public void setWorstInvertedSerialismEvent(LogEvent worstInvertedSerialismEvent) {
        this.worstInvertedSerialismEvent = worstInvertedSerialismEvent;
    }

    /**
     * @param worstSysGtUserEvent
     *            <code>ParallelCollection</code> or <code>Serial Collection</code> event with the greatest sys - user.
     */
    public void setWorstSysGtUserEvent(LogEvent worstSysGtUserEvent) {
        this.worstSysGtUserEvent = worstSysGtUserEvent;
    }
}
