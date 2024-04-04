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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * SHENANDOAH_FINAL_MARK
 * </p>
 * 
 * <p>
 * Finishes the concurrent marking by draining all pending marking/update queues and re-scanning the root set. It also
 * initializes evacuation by figuring out the regions to be evacuated (collection set), pre-evacuating some roots, and
 * generally prepares runtime for the next phase. Part of this work can be done concurrently during Concurrent
 * precleaning phase. This is the second pause in the cycle, and the most dominant time consumers here are draining the
 * queues and scanning the root set[1].
 * 
 * [1]<a href="https://wiki.openjdk.java.net/display/shenandoah/Main">Shenandoah GC</a>
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) JDK8 standard format:
 * </p>
 * 
 * <pre>
 * 2020-03-10T08:03:29.427-0400: 0.489: [Pause Final Mark, 0.313 ms]
 * </pre>
 * 
 * <p>
 * 2) JDK8 process weakrefs:
 * </p>
 * 
 * <pre>
 * 2020-03-10T08:03:45.915-0400: 16.977: [Pause Final Mark (process weakrefs), 1.040 ms]
 * </pre>
 * 
 * <p>
 * 3) JDK8 update refs:
 * </p>
 * 
 * <pre>
 * 2020-03-10T08:07:16.371-0400: 227.433: [Pause Final Mark (update refs), 0.589 ms]
 * </pre>
 * 
 * <p>
 * 4) Unified tandard format:
 * </p>
 * 
 * <pre>
 * [0.531s][info][gc] GC(1) Pause Final Mark 1.004ms
 * </pre>
 * 
 * <p>
 * 5) Unified process weakrefs:
 * </p>
 * 
 * <pre>
 * [0.820s][info][gc] GC(5) Pause Final Mark (process weakrefs) 0.231ms
 * </pre>
 * 
 * <p>
 * 6) Unified update refs:
 * </p>
 * 
 * <pre>
 * [10.459s][info][gc] GC(279) Pause Final Mark (update refs) 0.253ms
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahFinalMarkEvent extends ShenandoahCollector implements BlockingEvent, ParallelEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^(" + JdkRegEx.DECORATOR + "|" + UnifiedRegEx.DECORATOR
            + ") [\\[]{0,1}Pause Final Mark( \\((process weakrefs|update refs|unload classes)\\))?"
            + "( \\((process weakrefs|unload classes)\\))?[,]{0,1} " + JdkRegEx.DURATION_MS + "[\\]]{0,1}[ ]*$";

    private static final Pattern PATTERN = Pattern.compile(_REGEX);

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
    private long duration;

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
    public ShenandoahFinalMarkEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            duration = JdkMath
                    .convertMillisToMicros(matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedRegEx.DECORATOR_SIZE + 6))
                    .intValue();
            if (matcher.group(1).matches(UnifiedRegEx.DECORATOR)) {
                long endTimestamp;
                if (matcher.group(JdkUtil.DECORATOR_SIZE + 3).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    endTimestamp = Long.parseLong(matcher.group(JdkUtil.DECORATOR_SIZE + 13));
                } else if (matcher.group(JdkUtil.DECORATOR_SIZE + 3).matches(UnifiedRegEx.UPTIME)) {
                    endTimestamp = JdkMath.convertSecsToMillis(matcher.group(JdkUtil.DECORATOR_SIZE + 13)).longValue();
                } else {
                    if (matcher.group(JdkUtil.DECORATOR_SIZE + 15) != null) {
                        if (matcher.group(JdkUtil.DECORATOR_SIZE + 16).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                            endTimestamp = Long.parseLong(matcher.group(JdkUtil.DECORATOR_SIZE + 18));
                        } else {
                            endTimestamp = JdkMath.convertSecsToMillis(matcher.group(JdkUtil.DECORATOR_SIZE + 17))
                                    .longValue();
                        }
                    } else {
                        // Datestamp only.
                        endTimestamp = JdkUtil.convertDatestampToMillis(matcher.group(JdkUtil.DECORATOR_SIZE + 3));
                    }
                }
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
            } else {
                // JDK8
                if (matcher.group(14) != null && matcher.group(14).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(14)).longValue();
                } else if (matcher.group(2).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(2)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
            }
        }
    }

    /**
     * Alternate constructor. Create event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public ShenandoahFinalMarkEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public long getDurationMicros() {
        return duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
