/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.hsql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

/**
 * <p>
 * Manage storing and retrieving JVM data in an HSQL database.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JvmDao {

    /**
     * SQL statement(s) to create table(s).
     * 
     * Notes:
     * 
     * 1) combined_space is given its own column, even though in most cases it can be computed from young_space +
     * old_space, because some logging events log combined new + old sizes.
     * 
     */
    private static final String[] TABLES_CREATE_SQL = {
            "create table blocking_event (id integer identity, "
                    + "time_stamp bigint, event_name varchar(64), duration integer, young_space integer, "
                    + "old_space integer, combined_space integer, perm_space integer, young_occupancy_init integer, "
                    + "old_occupancy_init integer, combined_occupancy_init integer, perm_occupancy_init integer, "
                    + "log_entry varchar(500))",
            "create table application_stopped_time (id integer identity, "
                    + "time_stamp bigint, event_name varchar(64), duration bigint, log_entry varchar(500))" };

    /**
     * SQL statement(s) to delete table(s).
     */
    private static final String[] TABLES_DELETE_SQL = { "delete from blocking_event ",
            "delete from application_stopped_time " };

    /**
     * The database connection.
     */
    private static Connection connection;

    /**
     * List of all event types associate with JVM run.
     */
    List<LogEventType> eventTypes;

    /**
     * Analysis property keys.
     */
    private List<Analysis> analysis;

    /**
     * Collector families for JVM run.
     */
    List<CollectorFamily> collectorFamilies;

    /**
     * Logging lines that do not match any known GC events.
     */
    private List<String> unidentifiedLogLines;

    /**
     * The number of inserts to batch before persisting to database.
     */
    private static int batchSize = 100;

    /**
     * Batch blocking database inserts for improved performance.
     */
    private List<BlockingEvent> blockingBatch;

    /**
     * Batch stopped time database inserts for improved performance.
     */
    private List<ApplicationStoppedTimeEvent> stoppedTimeBatch;

    /**
     * The JVM options for the JVM run.
     */
    private String options;

    /**
     * JVM version.
     */
    private String version;

    /**
     * JVM memory information.
     */
    private String memory;

    /**
     * Physical memory (bytes).
     */
    private long physicalMemory;

    /**
     * Physical memory free (bytes).
     */
    private long physicalMemoryFree;

    /**
     * Swap size (bytes).
     */
    private long swap;

    /**
     * Swap free (bytes).
     */
    private long swapFree;

    /**
     * Number of <code>ParallelCollection</code> events.
     */
    private long parallelCount;

    /**
     * Number of <code>ParallelCollection</code> with "inverted" parallelism.
     */
    private long invertedParallelismCount;

    /**
     * <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    private LogEvent worstInvertedParallelismEvent;

    /**
     * Used for tracking max heap space outside of <code>BlockingEvent</code>s.
     */
    private int maxHeapSpaceNonBlocking;

    /**
     * Used for tracking max heap occupancy outside of <code>BlockingEvent</code>s.
     */
    private int maxHeapOccupancyNonBlocking;

    /**
     * Used for tracking max perm space outside of <code>BlockingEvent</code>s.
     */
    private int maxPermSpaceNonBlocking;

    /**
     * Used for tracking max perm occupancy outside of <code>BlockingEvent</code>s.
     */
    private int maxPermOccupancyNonBlocking;

    public JvmDao() {
        try {
            // Load database driver.
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Failed to load HSQLDB JDBC driver.");
        }

        try {
            // Connect to database.

            // Database server for development
            // connection = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/xdb", "sa",
            // "");

            // In-process standalone mode for deployment.
            connection = DriverManager.getConnection("jdbc:hsqldb:mem:gcdb", "sa", "");
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

        eventTypes = new ArrayList<LogEventType>();
        collectorFamilies = new ArrayList<CollectorFamily>();
        analysis = new ArrayList<Analysis>();
        unidentifiedLogLines = new ArrayList<String>();
        blockingBatch = new ArrayList<BlockingEvent>();
        stoppedTimeBatch = new ArrayList<ApplicationStoppedTimeEvent>();
        // prevent false positives of Analysis.INFO_SWAP_DISABLED
        swap = -1;
    }

    public List<String> getUnidentifiedLogLines() {
        return unidentifiedLogLines;
    }

    public List<LogEventType> getEventTypes() {
        return eventTypes;
    }

    public List<Analysis> getAnalysis() {
        return analysis;
    }

    public void addAnalysis(Analysis analysis) {
        if (!this.analysis.contains(analysis)) {
            this.analysis.add(analysis);
        }
    }

    public List<CollectorFamily> getCollectorFamilies() {
        return collectorFamilies;
    }

    public void addBlockingEvent(BlockingEvent event) {
        if (blockingBatch.size() == batchSize) {
            processBlockingBatch();
        }
        blockingBatch.add(event);
    }

    public void addStoppedTimeEvent(ApplicationStoppedTimeEvent event) {
        if (stoppedTimeBatch.size() == batchSize) {
            processStoppedTimeBatch();
        }
        stoppedTimeBatch.add(event);
    }

    /**
     * @return The JVM options.
     */
    public String getOptions() {
        return options;
    }

    /**
     * @param options
     *            The JVM options to set.
     */
    public void setOptions(String options) {
        this.options = options;
    }

    /**
     * @return The JVM version information.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            The JVM version information to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return The JVM memory information.
     */
    public String getMemory() {
        return memory;
    }

    /**
     * @param memory
     *            The JVM memory information to set.
     */
    public void setMemory(String memory) {
        this.memory = memory;
    }

    /**
     * @return The JVM environment physical memory (bytes).
     */
    public long getPhysicalMemory() {
        return physicalMemory;
    }

    /**
     * @param physicalMemory
     *            The JVM physical memory to set.
     */
    public void setPhysicalMemory(long physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    /**
     * @return The JVM environment physical free memory (bytes).
     */
    public long getPhysicalMemoryFree() {
        return physicalMemoryFree;
    }

    /**
     * @param physicalMemoryFree
     *            The JVM physical free memory to set.
     */
    public void setPhysicalMemoryFree(long physicalMemoryFree) {
        this.physicalMemoryFree = physicalMemoryFree;
    }

    /**
     * @return The JVM environment swap size (bytes).
     */
    public long getSwap() {
        return swap;
    }

    /**
     * @param swap
     *            The JVM swap to set.
     */
    public void setSwap(long swap) {
        this.swap = swap;
    }

    /**
     * @return The JVM environment swap free (bytes).
     */
    public long getSwapFree() {
        return swapFree;
    }

    /**
     * @param swapFree
     *            The JVM swap free to set.
     */
    public void setSwapFree(long swapFree) {
        this.swapFree = swapFree;
    }

    /**
     * @return The number of <code>ParallelCollection</code> events.
     */
    public long getParallelCount() {
        return parallelCount;
    }

    /**
     * @param parallelCount
     *            The number of <code>ParallelCollection</code> events.
     */
    public void setParallelCount(long parallelCount) {
        this.parallelCount = parallelCount;
    }

    /**
     * @return The number of "inverted" parallelism events.
     */
    public long getInvertedParallelismCount() {
        return invertedParallelismCount;
    }

    /**
     * @param invertedParallelismCount
     *            The number of "low" parallelism events.
     */
    public void setInvertedParallelismCount(long invertedParallelismCount) {
        this.invertedParallelismCount = invertedParallelismCount;
    }

    /**
     * @return The <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    public LogEvent getWorstInvertedParallelismEvent() {
        return worstInvertedParallelismEvent;
    }

    /**
     * @param worstInvertedParallelismEvent
     *            The <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    public void setWorstInvertedParallelismEvent(LogEvent worstInvertedParallelismEvent) {
        this.worstInvertedParallelismEvent = worstInvertedParallelismEvent;
    }

    /**
     * @return The maximum heap space in non <code>BlockingEvent</code>s.
     */
    public int getMaxHeapSpaceNonBlocking() {
        return maxHeapSpaceNonBlocking;
    }

    /**
     * @param maxHeapSpaceNonBlocking
     *            The maximum heap space in non <code>BlockingEvent</code>s.
     */
    public void setMaxHeapSpaceNonBlocking(int maxHeapSpaceNonBlocking) {
        this.maxHeapSpaceNonBlocking = maxHeapSpaceNonBlocking;
    }

    /**
     * @return The maximum heap occupancy in non <code>BlockingEvent</code>s.
     */
    public int getMaxHeapOccupancyNonBlocking() {
        return maxHeapOccupancyNonBlocking;
    }

    /**
     * @param maxHeapOccupancyNonBlocking
     *            The maximum heap occupancy in non <code>BlockingEvent</code>s.
     */
    public void setMaxHeapOccupancyNonBlocking(int maxHeapOccupancyNonBlocking) {
        this.maxHeapOccupancyNonBlocking = maxHeapOccupancyNonBlocking;
    }

    /**
     * @return The maximum perm space in non <code>BlockingEvent</code>s.
     */
    public int getMaxPermSpaceNonBlocking() {
        return maxPermSpaceNonBlocking;
    }

    /**
     * @param maxPermSpaceNonBlocking
     *            The maximum perm space in non <code>BlockingEvent</code>s.
     */
    public void setMaxPermSpaceNonBlocking(int maxPermSpaceNonBlocking) {
        this.maxPermSpaceNonBlocking = maxPermSpaceNonBlocking;
    }

    /**
     * @return The maximum perm occupancy in non <code>BlockingEvent</code>s.
     */
    public int getMaxPermOccupancyNonBlocking() {
        return maxPermOccupancyNonBlocking;
    }

    /**
     * @param maxPermOccupancyNonBlocking
     *            The maximum perm occupancy in non <code>BlockingEvent</code>s.
     */
    public void setMaxPermOccupancyNonBlocking(int maxPermOccupancyNonBlocking) {
        this.maxPermOccupancyNonBlocking = maxPermOccupancyNonBlocking;
    }

    /**
     * Add blocking events to database.
     */
    public synchronized void processBlockingBatch() {

        PreparedStatement pst = null;
        try {
            String sqlInsertBlockingEvent = "insert into blocking_event (time_stamp, event_name, "
                    + "duration, young_space, old_space, combined_space, perm_space, young_occupancy_init, "
                    + "old_occupancy_init, combined_occupancy_init, perm_occupancy_init, log_entry) "
                    + "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)";

            final int TIME_STAMP_INDEX = 1;
            final int EVENT_NAME_INDEX = 2;
            final int DURATION_INDEX = 3;
            final int YOUNG_SPACE_INDEX = 4;
            final int OLD_SPACE_INDEX = 5;
            final int COMBINED_SPACE_INDEX = 6;
            final int PERM_SPACE_INDEX = 7;
            final int YOUNG_OCCUPANCY_INIT_INDEX = 8;
            final int OLD_OCCUPANCY_INIT_INDEX = 9;
            final int COMBINED_OCCUPANCY_INIT_INDEX = 10;
            final int PERM_OCCUPANCY_INIT_INDEX = 11;
            final int LOG_ENTRY_INDEX = 12;

            pst = connection.prepareStatement(sqlInsertBlockingEvent);

            for (int i = 0; i < blockingBatch.size(); i++) {
                BlockingEvent event = blockingBatch.get(i);
                pst.setLong(TIME_STAMP_INDEX, event.getTimestamp());
                pst.setString(EVENT_NAME_INDEX, event.getName());
                pst.setLong(DURATION_INDEX, event.getDuration());
                if (event instanceof YoungData) {
                    pst.setInt(YOUNG_SPACE_INDEX, ((YoungData) event).getYoungSpace());
                    pst.setInt(YOUNG_OCCUPANCY_INIT_INDEX, ((YoungData) event).getYoungOccupancyInit());
                } else {
                    pst.setInt(YOUNG_SPACE_INDEX, 0);
                    pst.setInt(YOUNG_OCCUPANCY_INIT_INDEX, 0);
                }
                if (event instanceof OldData) {
                    pst.setInt(OLD_SPACE_INDEX, ((OldData) event).getOldSpace());
                    pst.setInt(OLD_OCCUPANCY_INIT_INDEX, ((OldData) event).getOldOccupancyInit());
                } else {
                    pst.setInt(OLD_SPACE_INDEX, 0);
                    pst.setInt(OLD_OCCUPANCY_INIT_INDEX, 0);
                }
                if (event instanceof CombinedData) {
                    pst.setInt(COMBINED_SPACE_INDEX, ((CombinedData) event).getCombinedSpace());
                    pst.setInt(COMBINED_OCCUPANCY_INIT_INDEX, ((CombinedData) event).getCombinedOccupancyInit());
                } else {
                    pst.setInt(COMBINED_SPACE_INDEX, 0);
                    pst.setInt(COMBINED_OCCUPANCY_INIT_INDEX, 0);
                }
                if (event instanceof PermMetaspaceData) {
                    pst.setInt(PERM_SPACE_INDEX, ((PermMetaspaceData) event).getPermSpace());
                    pst.setInt(PERM_OCCUPANCY_INIT_INDEX, ((PermMetaspaceData) event).getPermOccupancyInit());
                } else {
                    pst.setInt(PERM_SPACE_INDEX, 0);
                    pst.setInt(PERM_OCCUPANCY_INIT_INDEX, 0);
                }
                pst.setString(LOG_ENTRY_INDEX, event.getLogEntry());
                pst.addBatch();
            }
            pst.executeBatch();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error inserting blocking event.");
        } finally {
            blockingBatch.clear();
            try {
                pst.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closingPreparedStatement.");
            }
        }
    }

    /**
     * Add stopped time events to database.
     */
    public synchronized void processStoppedTimeBatch() {

        PreparedStatement pst = null;
        try {
            String sqlInsertStoppedEvent = "insert into application_stopped_time (time_stamp, event_name, "
                    + "duration, log_entry) " + "values(?, ?, ?, ?)";

            final int TIME_STAMP_INDEX = 1;
            final int EVENT_NAME_INDEX = 2;
            final int DURATION_INDEX = 3;
            final int LOG_ENTRY_INDEX = 4;

            pst = connection.prepareStatement(sqlInsertStoppedEvent);

            for (int i = 0; i < stoppedTimeBatch.size(); i++) {
                ApplicationStoppedTimeEvent event = stoppedTimeBatch.get(i);
                pst.setLong(TIME_STAMP_INDEX, event.getTimestamp());
                pst.setString(EVENT_NAME_INDEX, event.getName());
                pst.setInt(DURATION_INDEX, event.getDuration());
                pst.setString(LOG_ENTRY_INDEX, event.getLogEntry());
                pst.addBatch();
            }
            pst.executeBatch();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error inserting stopped time event.");
        } finally {
            stoppedTimeBatch.clear();
            try {
                pst.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closingPreparedStatement.");
            }
        }
    }

    /**
     * The maximum GC blocking event pause time.
     * 
     * @return maximum pause duration (milliseconds).
     */
    public synchronized int getMaxGcPause() {
        int maxPause = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select max(duration) from blocking_event");
            if (rs.next()) {
                maxPause = JdkMath.convertMicrosToMillis(rs.getInt(1)).intValue();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determine maximum pause time.");
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
        return maxPause;
    }

    /**
     * The total blocking event pause time.
     * 
     * @return total pause duration (milliseconds).
     */
    public synchronized long getTotalGcPause() {
        long totalPause = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select sum(duration) from blocking_event");
            if (rs.next()) {
                totalPause = JdkMath.convertMicrosToMillis(rs.getLong(1)).longValue();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining total pause time.");
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
        return totalPause;
    }

    /**
     * The first blocking event.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The first blocking event.
     */
    public synchronized BlockingEvent getFirstGcEvent() {
        BlockingEvent event = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(
                    "select log_entry from blocking_event where id = " + "(select min(id) from blocking_event)");
            if (rs.next()) {
                event = (BlockingEvent) JdkUtil.parseLogLine(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining first blocking event.");
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
        return event;
    }

    /**
     * Retrieve the last blocking event.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The last blocking event.
     */
    public synchronized BlockingEvent getLastGcEvent() {
        BlockingEvent event = null;
        // Retrieve last event from batch or database.
        if (blockingBatch.size() > 0) {
            event = blockingBatch.get(blockingBatch.size() - 1);
        } else {
            event = queryGcLastEvent();
        }
        return event;
    }

    /**
     * Retrieve the last blocking event.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The last blocking event in database.
     */

    private synchronized BlockingEvent queryGcLastEvent() {
        BlockingEvent event = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery(
                    "select log_entry from blocking_event where id = " + "(select max(id) from blocking_event)");
            if (rs.next()) {
                event = (BlockingEvent) JdkUtil.parseLogLine(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining last blocking event.");
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
        return event;
    }

    /**
     * Delete table(s). Useful when running in server mode during development.
     */
    public synchronized void cleanup() {
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

    /**
     * Retrieve all <code>BlockingEvent</code>s.
     * 
     * @return <code>List</code> of events.
     */
    public synchronized List<BlockingEvent> getBlockingEvents() {
        List<BlockingEvent> events = new ArrayList<BlockingEvent>();
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            StringBuffer sql = new StringBuffer();
            sql.append("select time_stamp, event_name, duration, log_entry from blocking_event"
                    + " order by time_stamp asc, id asc");
            rs = statement.executeQuery(sql.toString());
            while (rs.next()) {
                LogEventType eventType = JdkUtil.determineEventType(rs.getString(2));
                BlockingEvent event = JdkUtil.hydrateBlockingEvent(eventType, rs.getString(4), rs.getLong(1),
                        rs.getInt(3));
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error retrieving blocking events.");
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
        return events;
    }

    /**
     * Retrieve all <code>BlockingEvent</code>s of the specified type.
     * 
     * @param eventType
     *            The event type to retrieve.
     * @return <code>List</code> of events.
     */
    public synchronized List<BlockingEvent> getBlockingEvents(LogEventType eventType) {
        List<BlockingEvent> events = new ArrayList<BlockingEvent>();
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            StringBuffer sql = new StringBuffer();
            sql.append("select time_stamp, duration, log_entry from blocking_event where event_name='");
            sql.append(eventType);
            sql.append("' order by time_stamp asc");
            rs = statement.executeQuery(sql.toString());
            while (rs.next()) {
                BlockingEvent event = JdkUtil.hydrateBlockingEvent(eventType, rs.getString(3), rs.getLong(1),
                        rs.getInt(2));
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error retrieving blocking events.");
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
        return events;
    }

    /**
     * The total number of blocking events.
     * 
     * @return total number of blocking events.
     */
    public synchronized int getBlockingEventCount() {
        int count = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select count(id) from blocking_event");
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining blocking event count.");
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
        return count;
    }

    /**
     * The maximum young space size during the JVM run.
     * 
     * @return maximum young space size (kilobytes).
     */
    public synchronized int getMaxYoungSpace() {
        int space = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select max(young_space) from blocking_event");
            if (rs.next()) {
                space = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining max young space.");
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
        return space;
    }

    /**
     * The maximum old space size during the JVM run.
     * 
     * @return maximum old space size (kilobytes).
     */
    public synchronized int getMaxOldSpace() {
        int space = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select max(old_space) from blocking_event");
            if (rs.next()) {
                space = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining max old space.");
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
        return space;
    }

    /**
     * The maximum heap space size during the JVM run.
     * 
     * @return maximum heap size (kilobytes).
     */
    public synchronized int getMaxHeapSpace() {
        int space = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement
                    .executeQuery("select max(young_space + old_space " + "+ combined_space) from blocking_event");
            if (rs.next()) {
                space = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining max heap space.");
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
        return space;
    }

    /**
     * The maximum heap occupancy during the JVM run.
     * 
     * @return maximum heap occupancy (kilobytes).
     */
    public synchronized int getMaxHeapOccupancy() {
        int occupancy = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select max(young_occupancy_init + old_occupancy_init "
                    + "+ combined_occupancy_init) from blocking_event");
            if (rs.next()) {
                occupancy = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining max heap occupancy.");
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
        return occupancy;
    }

    /**
     * The maximum perm/metaspace size during the JVM run.
     * 
     * @return maximum perm/metaspace footprint (kilobytes).
     */
    public synchronized int getMaxPermSpace() {
        int space = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select max(perm_space) from blocking_event");
            if (rs.next()) {
                space = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining max perm space.");
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
        return space;
    }

    /**
     * The maximum perm/metaspace occupancy during the JVM run.
     * 
     * @return maximum perm/metaspac occupancy (kilobytes).
     */
    public synchronized int getMaxPermOccupancy() {
        int occupancy = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select max(perm_occupancy_init) from blocking_event");
            if (rs.next()) {
                occupancy = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining max perm gen occupancy.");
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
        return occupancy;
    }

    /**
     * The first stopped event.
     * 
     * @return The time first stopped event.
     */
    public synchronized ApplicationStoppedTimeEvent getFirstStoppedEvent() {
        ApplicationStoppedTimeEvent event = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select log_entry from application_stopped_time where id = "
                    + "(select min(id) from application_stopped_time)");
            if (rs.next()) {
                event = (ApplicationStoppedTimeEvent) JdkUtil.parseLogLine(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining first stopped event.");
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
        return event;
    }

    /**
     * Retrieve the last stopped event.
     * 
     * @return The last stopped event.
     */
    public synchronized ApplicationStoppedTimeEvent getLastStoppedEvent() {
        ApplicationStoppedTimeEvent event;
        // Retrieve last event from batch or database.
        if (stoppedTimeBatch.size() > 0) {
            event = stoppedTimeBatch.get(stoppedTimeBatch.size() - 1);
        } else {
            event = queryStoppedLastEvent();
        }
        return event;
    }

    /**
     * Retrieve the last stopped event from the database.
     * 
     * @return The last stopped event in database.
     */

    private synchronized ApplicationStoppedTimeEvent queryStoppedLastEvent() {
        ApplicationStoppedTimeEvent event = null;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select log_entry from application_stopped_time where id = "
                    + "(select max(id) from application_stopped_time)");
            if (rs.next()) {
                event = (ApplicationStoppedTimeEvent) JdkUtil.parseLogLine(rs.getString(1));
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining last stopped event.");
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
        return event;
    }

    /**
     * The maximum stopped time event pause time.
     * 
     * @return maximum pause duration (milliseconds).
     */
    public synchronized int getMaxStoppedTime() {
        int maxStoppedTime = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select max(duration) from application_stopped_time");
            if (rs.next()) {
                long micros = rs.getInt(1);
                maxStoppedTime = JdkMath.convertMicrosToMillis(micros).intValue();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determine maximum stopped time.");
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
        return maxStoppedTime;
    }

    /**
     * The total stopped time event pause time.
     * 
     * @return total pause duration (milliseconds).
     */
    public synchronized int getTotalStoppedTime() {
        int totalStoppedTime = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select sum(duration) from application_stopped_time");
            if (rs.next()) {
                long micros = rs.getLong(1);
                totalStoppedTime = JdkMath.convertMicrosToMillis(micros).intValue();
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining total stopped time.");
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
        return totalStoppedTime;
    }

    /**
     * The total number of stopped time events.
     * 
     * @return total number of stopped time events.
     */
    public synchronized int getStoppedTimeEventCount() {
        int count = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select count(id) from application_stopped_time");
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining stopped time event count.");
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
        return count;
    }
}
