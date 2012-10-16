/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.hsql;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermData;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
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
     * SQL statements to create table(s).
     * 
     * Notes:
     * 
     * 1) combined_space is given its own column, even though in most cases it can be computed from young_space +
     * old_space, because some logging events log combined new + old sizes.
     * 
     */
    private static final String[] TABLES_CREATE_SQL = { "create table blocking_event (id integer identity, "
            + "time_stamp bigint, event_name varchar(64), duration integer, young_space integer, "
            + "old_space integer, combined_space integer, perm_space integer, young_occupancy_init integer, "
            + "old_occupancy_init integer, combined_occupancy_init integer, perm_occupancy_init integer, "
            + "log_entry varchar(500))" };

    private static final String[] TABLES_DELETE_SQL = { "delete from blocking_event " };

    /**
     * The database connection.
     */
    private static Connection connection;

    /**
     * List of all event types associate with JVM run.
     */
    List<LogEventType> eventTypes;

    /**
     * Logging lines that do not match any known GC events.
     */
    private List<String> unidentifiedLogLines;

    /**
     * The number of inserts to batch before persisting to database.
     */
    private static int batchSize = 100;

    /**
     * Batch database inserts for improved performance.
     */
    private List<BlockingEvent> batch;

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
        unidentifiedLogLines = new ArrayList<String>();
        batch = new ArrayList<BlockingEvent>();
    }

    public List<String> getUnidentifiedLogLines() {
        return unidentifiedLogLines;
    }

    public void setUnidentifiedLogLines(List<String> unidentifiedLogLines) {
        this.unidentifiedLogLines = unidentifiedLogLines;
    }

    public List<LogEventType> getEventTypes() {
        return eventTypes;
    }

    public void addBlockingEvent(BlockingEvent event) {
        if (batch.size() == batchSize) {
            processBatch();
        }
        batch.add(event);
    }

    public synchronized void processBatch() {
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

            for (int i = 0; i < batch.size(); i++) {
                BlockingEvent event = batch.get(i);
                pst.setLong(TIME_STAMP_INDEX, event.getTimestamp());
                pst.setString(EVENT_NAME_INDEX, event.getName());
                pst.setInt(DURATION_INDEX, event.getDuration());
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
                if (event instanceof PermData) {
                    pst.setInt(PERM_SPACE_INDEX, ((PermData) event).getPermSpace());
                    pst.setInt(PERM_OCCUPANCY_INIT_INDEX, ((PermData) event).getPermOccupancyInit());
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
            batch.clear();
            try {
                pst.close();
            } catch (SQLException e) {
                System.err.println(e.getMessage());
                throw new RuntimeException("Error closingPreparedStatement.");
            }
        }
    }

    /**
     * The maximum blocking event pause time.
     * 
     * @return maximum pause duration (milliseconds).
     */
    public synchronized int getMaxPause() {
        int maxPause = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select max(duration) from blocking_event");
            if (rs.next()) {
                maxPause = rs.getInt(1);
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
    public synchronized int getTotalPause() {
        int totalPause = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select sum(duration) from blocking_event");
            if (rs.next()) {
                totalPause = rs.getInt(1);
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
     * The first blocking event timestamp.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The time of the first blocking event, in milliseconds after JVM startup.
     */
    public synchronized long getFirstTimestamp() {
        long firstTimestamp = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select time_stamp from blocking_event where id = "
                    + "(select min(id) from blocking_event)");
            if (rs.next()) {
                firstTimestamp = rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining first timestamp.");
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
        return firstTimestamp;
    }

    /**
     * Retrieve the last blocking event timestamp.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The time of the last blocking event, in milliseconds after JVM startup.
     */
    public synchronized long getLastTimestamp() {
        long lastTimestamp = 0;
        // Retrieve last timestamp from batch or database.
        if (batch.size() > 0) {
            BlockingEvent event = batch.get(batch.size() - 1);
            lastTimestamp = event.getTimestamp();
        } else {
            lastTimestamp = getDbLastTimeStamp();
        }
        return lastTimestamp;
    }

    /**
     * Retrieve the last blocking event duration.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The duration of the last blocking event (milliseconds).
     */
    public synchronized int getLastDuration() {
        int lastDuration = 0;
        // Retrieve last duration from batch or database.
        if (batch.size() > 0) {
            BlockingEvent event = batch.get(batch.size() - 1);
            lastDuration = event.getDuration();
        } else {
            lastDuration = getDbLastDuration();
        }
        return lastDuration;
    }

    /**
     * Retrieve the last blocking event timestamp from the database.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The time of the last blocking event in database, in milliseconds after JVM startup.
     */

    private synchronized long getDbLastTimeStamp() {
        long lastTimestamp = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select time_stamp from blocking_event where id = "
                    + "(select max(id) from blocking_event)");
            if (rs.next()) {
                lastTimestamp = rs.getLong(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining last timestamp.");
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
        return lastTimestamp;
    }

    /**
     * Retrieve the last blocking event duration from the database.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The duration of the last blocking event in database (milliseconds).
     */

    private synchronized int getDbLastDuration() {
        int duration = 0;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = connection.createStatement();
            rs = statement.executeQuery("select duration from blocking_event where id = "
                    + "(select max(id) from blocking_event)");
            if (rs.next()) {
                duration = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            throw new RuntimeException("Error determining last duration.");
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
        return duration;
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
                    + " order by time_stamp asc");
            rs = statement.executeQuery(sql.toString());
            while (rs.next()) {
                LogEventType eventType = JdkUtil.determineEventType(rs.getString(2));
                BlockingEvent event = JdkUtil.hydrateBlockingEvent(eventType, rs.getString(4), rs.getLong(1), rs
                        .getInt(3));
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
                BlockingEvent event = JdkUtil.hydrateBlockingEvent(eventType, rs.getString(3), rs.getLong(1), rs
                        .getInt(2));
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
     * The maximum perm gen space size during the JVM run.
     * 
     * @return maximum perm gen space footprint (kilobytes).
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
     * The maximum perm gen occupancy during the JVM run.
     * 
     * @return maximum perm gen occupancy (kilobytes).
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
     * Calculate the throughput for the JVM session. Throughput is the percent of time not spent doing GC. 0 means all
     * time was spent doing GC. 100 means no time was spent doing GC.
     * 
     * @return Throughput as a percent.
     */
    public long getThroughput() {
        long firstTimestamp = getFirstTimestamp();
        long lastTimestamp = getLastTimestamp();
        int lastDuration = getLastDuration();
        int totalPause = getTotalPause();

        long timeTotal;

        if (lastTimestamp > firstTimestamp && firstTimestamp > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000) {
            // Partial log. Use the timestamp of the first GC event, not 0, in order to determine
            // throughput more accurately.
            timeTotal = lastTimestamp + new Long(lastDuration).longValue() - firstTimestamp;
        } else {
            // Complete log or a log with only 1 event.
            timeTotal = lastTimestamp + new Long(lastDuration).longValue();
        }
        long timeNotGc = timeTotal - new Long(totalPause).longValue();
        BigDecimal throughput = new BigDecimal(timeNotGc);
        throughput = throughput.divide(new BigDecimal(timeTotal), 2, RoundingMode.HALF_EVEN);
        throughput = throughput.movePointRight(2);
        return throughput.intValue();
    }
}
