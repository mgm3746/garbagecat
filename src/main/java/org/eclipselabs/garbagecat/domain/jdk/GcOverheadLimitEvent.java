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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * GC_OVERHEAD_LIMIT
 * </p>
 * 
 * <p>
 * Garbage collection overhead limit being reached. This happens when 98% of the total time is spent in garbage
 * collection and less than 2% of the heap is recovered. This feature is a throttle to prevent applications from running
 * for an extended period of time while making little or no progress because the heap is too small. If desired, this
 * feature can be disabled with the <code>-XX:-UseGCOverheadLimit</code> option.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) With "would exceed":
 * </p>
 * 
 * <pre>
 * GC time would exceed GCTimeLimit of 98%
 * </pre>
 * 
 * <p>
 * 2) With "is exceeding":
 * </p>
 * 
 * <pre>
 * GC time is exceeding GCTimeLimit of 98%
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class GcOverheadLimitEvent implements LogEvent {

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^GC time (would exceed|is exceeding) GCTimeLimit of 98%$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

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
    public GcOverheadLimitEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString();
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
        return PATTERN.matcher(logLine).matches();
    }
}
