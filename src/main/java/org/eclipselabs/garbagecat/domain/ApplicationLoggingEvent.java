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
package org.eclipselabs.garbagecat.domain;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * APPLICATION_LOGGING
 * </p>
 * 
 * <p>
 * Remove application logging.
 * </p>
 * 
 * <p>
 * It is recommended to log garbage collection logging to a dedicated file with the <code>-Xverboselog:</code> option
 * and avoid mixing application and garbage collection logging.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * 1) Lines beginning with HH:MM:SS timestamp:
 * 
 * <pre>
 * 00:02:04,915 INFO [STDOUT]
 * </pre>
 * 
 * 2) Lines beginning with YYYY-MM-DD HH:MM:SS date + timestamp:
 * 
 * <pre>
 * 2010-03-25 17:00:20,769 WARN
 * </pre>
 * 
 * 3) Exceptions:
 * 
 * <pre>
 * java.sql.SQLException: pingDatabase failed status=-1
 * </pre>
 * 
 * 4) Stack trace:
 * 
 * <pre>
 * at oracle.jdbc.driver.T4CConnection.logon(T4CConnection.java:292)
 * ... 56 more
 * </pre>
 * 
 * 5) Oracle errors:
 * 
 * <pre>
 * ORA-12514, TNS:listener does not currently know of service requested in connect descriptor
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ApplicationLoggingEvent implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX[] = {
            // HH:MM:SS datestamp
            "^(\\d{4}-\\d{2}-\\d{2} )?\\d{2}:\\d{2}:\\d{2},\\d{3} (DEBUG|ERROR|FATAL|INFO|TRACE|WARN) .*$",
            // Exceptions, Errors
            "^(java|com|org).*(Exception|Error).*$",
            // Oracle exceptions
            "^ORA-\\d{1,6}.*$",
            // stack trace
            "^\\tat (java|com|org|oracle).*$",
            // stack trace caused by
            "^Caused by: (java|com|org|oracle).*$",
            // stack trace ellipsis
            "\\t\\.\\.\\. \\d{1,3} more$",
            // JBoss divider
            "^=========================================================================$",
            // JBoss bootstrap environment
            "^  JBoss Bootstrap Environment$",
            // JBOSS_HOME
            "^  JBOSS_HOME:.*$",
            // JBoss JAVA
            "^  JAVA:.*$",
            // JBoss CLASS_PATH
            "^  CLASSPATH:.*$",
            // JBoss JAVA_OPTS warning
            "^JAVA_OPTS already set in environment; overriding default settings with values: -d64$"
            //
    };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ApplicationLoggingEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.APPLICATION_LOGGING.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean isMatch = false;
        for (int i = 0; i < REGEX.length; i++) {
            if (logLine.matches(REGEX[i])) {
                isMatch = true;
                break;
            }
        }
        return isMatch;
    }

}
