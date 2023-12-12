/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * Z_RELOCATE_START
 * </p>
 * 
 * <p>
 * The third phase of the Z garbage collector. Region compaction.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * [0.132s] GC(0) Pause Relocate Start 0.004ms
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ZRelocateStartEvent extends ZCollector implements UnifiedLogging, BlockingEvent {

    private static final Pattern PATTERN = Pattern.compile(ZRelocateStartEvent.REGEX);
    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR + " Pause Relocate Start " + JdkRegEx.DURATION_MS
            + "[ ]*$";

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
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long eventTime;
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
    public ZRelocateStartEvent(String logEntry) {
        this.logEntry = logEntry;
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                eventTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1)).intValue();
                long time = UnifiedUtil.calculateTime(matcher);
                if (!isEndstamp()) {
                    timestamp = time;
                } else {
                    timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
                }
            }
        }
    }

    /**
     * Alternate constructor. Create detail logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public ZRelocateStartEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.eventTime = duration;
    }

    public long getDurationMicros() {
        return eventTime;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.Z_RELOCATE_START.toString();
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEndstamp() {
        return true;
    }
}
