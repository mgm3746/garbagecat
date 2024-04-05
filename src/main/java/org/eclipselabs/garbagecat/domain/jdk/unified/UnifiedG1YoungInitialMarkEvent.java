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

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_G1_YOUNG_INITIAL_MARK
 * </p>
 *
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <p>
 * G1 young generation collection. Live objects from Eden and Survivor regions are copied to new regions, either to a
 * survivor region or promoted to the old space.
 * </p>
 * 
 * <h2>Example Logging</h2>
 *
 * <pre>
 * [2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) 562M-&gt;5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1YoungInitialMarkEvent extends G1Collector
        implements UnifiedLogging, BlockingEvent, YoungCollection, ParallelEvent, CombinedData, TriggerData, TimesData {
    /**
     * Trigger(s) regular expression.
     */
    static final String __TRIGGER = "(" + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + ")";
    /**
     * Regular expression defining standard logging (no details).
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Pause Initial Mark \\(" + __TRIGGER + "\\) "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS
            + TimesData.REGEX_JDK9 + "[ ]*$";

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
     * Combined young + old generation size at end of GC event.
     */
    private Memory combinedOccupancyEnd;

    /**
     * Combined young + old generation size at beginning of GC event.
     */
    private Memory combinedOccupancyInit;

    /**
     * Combined young + old generation allocation.
     */
    private Memory combinedSpace;

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long duration;

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;
    /**
     * The wall (clock) time in centiseconds.
     */
    private int timeReal = TimesData.NO_DATA;
    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The time of all system (kernel) threads added together in centiseconds.
     */
    private int timeSys = TimesData.NO_DATA;

    /**
     * The time of all user (non-kernel) threads added together in centiseconds.
     */
    private int timeUser = TimesData.NO_DATA;

    /**
     * The trigger for the GC event.
     */
    private GcTrigger trigger;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedG1YoungInitialMarkEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher;
        if ((matcher = PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                long endTimestamp;
                if (matcher.group(2).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    endTimestamp = Long.parseLong(matcher.group(JdkUtil.DECORATOR_SIZE));
                } else if (matcher.group(2).matches(UnifiedRegEx.UPTIME)) {
                    endTimestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                } else {
                    if (matcher.group(JdkUtil.DECORATOR_SIZE + 1) != null) {
                        if (matcher.group(JdkUtil.DECORATOR_SIZE + 2).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                            endTimestamp = Long.parseLong(matcher.group(JdkUtil.DECORATOR_SIZE + 4));
                        } else {
                            endTimestamp = JdkMath.convertSecsToMillis(matcher.group(JdkUtil.DECORATOR_SIZE + 3))
                                    .longValue();
                        }
                    } else {
                        // Datestamp only.
                        endTimestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                    }
                }
                trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
                combinedOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4).charAt(0)).convertTo(KILOBYTES);
                combinedOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 7).charAt(0)).convertTo(KILOBYTES);
                combinedSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 8),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 10).charAt(0)).convertTo(KILOBYTES);
                duration = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 11)).intValue();
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 13)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 14)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 15)).intValue();
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
    public UnifiedG1YoungInitialMarkEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedOccupancyEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combinedOccupancyInit;
    }

    public Memory getCombinedSpace() {
        return combinedSpace;
    }

    public long getDurationMicros() {
        return duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString();
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public int getTimeReal() {
        return timeReal;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTimeSys() {
        return timeSys;
    }

    public int getTimeUser() {
        return timeUser;
    }

    public GcTrigger getTrigger() {
        return trigger;
    }

    public boolean isEndstamp() {
        boolean isEndStamp = false;
        return isEndStamp;
    }
}
