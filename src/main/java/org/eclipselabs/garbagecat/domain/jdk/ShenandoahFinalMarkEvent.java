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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

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
 * <h3>Example Logging</h3>
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
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private int duration;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DECORATOR + "|" + UnifiedRegEx.DECORATOR
            + ") [\\[]{0,1}Pause Final Mark( \\((update refs|unload classes)\\))?"
            + "( \\(process weakrefs\\))?[,]{0,1} " + UnifiedRegEx.DURATION + "[\\]]{0,1}[ ]*$";

    private static final Pattern pattern = Pattern.compile(REGEX);

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ShenandoahFinalMarkEvent(String logEntry) {
        this.logEntry = logEntry;
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                duration = JdkMath.convertMillisToMicros(matcher.group(40)).intValue();
                if (matcher.group(1).matches(UnifiedRegEx.DECORATOR)) {
                    long endTimestamp;
                    if (matcher.group(13).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        endTimestamp = Long.parseLong(matcher.group(29));
                    } else if (matcher.group(13).matches(UnifiedRegEx.UPTIME)) {
                        endTimestamp = JdkMath.convertSecsToMillis(matcher.group(24)).longValue();
                    } else {
                        if (matcher.group(27) != null) {
                            if (matcher.group(27).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                                endTimestamp = Long.parseLong(matcher.group(29));
                            } else {
                                endTimestamp = JdkMath.convertSecsToMillis(matcher.group(28)).longValue();
                            }
                        } else {
                            // Datestamp only.
                            endTimestamp = UnifiedUtil.convertDatestampToMillis(matcher.group(13));
                        }
                    }
                    timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                } else {
                    // JDK8
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
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

    public String getLogEntry() {
        return logEntry;
    }

    public int getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString();
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
