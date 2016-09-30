/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * LOG_ROTATION
 * </p>
 * 
 * <p>
 * Log rotation.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 2016-03-24 10:28:33 GC log file has reached the maximum size. Saved as /path/to/gc.log.0
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class LogRotation implements LogEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} GC log file has reached the maximum "
            + "size.+$";
    private static Pattern pattern = Pattern.compile(LogRotation.REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public LogRotation(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.LOG_ROTATION.toString();
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
        return pattern.matcher(logLine).matches();
    }
}
