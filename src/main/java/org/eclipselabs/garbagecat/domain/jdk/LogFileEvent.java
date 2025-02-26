/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * LOG_FILE
 * </p>
 * 
 * <p>
 * GC log file information.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * 2016-09-29 07:13:12 GC log file created /path/to/gc.log
 * 
 * 2016-03-24 10:28:33 GC log file has reached the maximum size. Saved as /path/to/gc.log.0
 * 
 * 2021-10-09 00:01:02 GC log rotation request has been received. Saved as /path/to/gc.log.2021-10-08_21-57-44.0
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class LogFileEvent implements LogEvent {

    /**
     * Regular expressions defining the logging JDK8 and prior.
     */
    private static final String _REGEX = "^(" + JdkRegEx.DATETIME
            + ") GC log (file created|file has reached the maximum size|rotation request has been received).+$";

    public static final Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return PATTERN.matcher(logLine).matches();
    }

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
    public LogFileEvent(String logEntry) {
        this.logEntry = logEntry;
        // no reason to calculate
        timestamp = 0L;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.LOG_FILE;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return True if the event is when the log file is created, false otherwise.
     */
    public boolean isCreated() {
        return logEntry.matches("^" + JdkRegEx.DATETIME + " GC log file created.+$");
    }

}
