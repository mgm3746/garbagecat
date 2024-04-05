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
import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * G1_CLEANUP
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.G1CleanupEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Logging without details:
 * </p>
 * 
 * <pre>
 * [15.101s][info][gc] GC(1099) Pause Cleanup 30M-&gt;30M(44M) 0.058ms
 * </pre>
 * 
 * <p>
 * 2) Preprocessed from logging with details:
 * </p>
 * 
 * <pre>
 * [16.082s][info][gc            ] GC(969) Pause Cleanup 28M-&gt;28M(46M) 0.064ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1CleanupEvent extends G1Collector
        implements UnifiedLogging, BlockingEvent, ParallelEvent, CombinedData, TimesData {
    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Pause Cleanup " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + "[ ]*$";
    /**
     * Regular expression defining preprocessed logging.
     */
    private static final String _REGEX_PREPROCESSED = "^" + UnifiedRegEx.DECORATOR + " Pause Cleanup " + JdkRegEx.SIZE
            + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + TimesData.REGEX_JDK9
            + "[ ]*$";

    private static final Pattern REGEX_PATTERN = Pattern.compile(_REGEX);
    private static final Pattern REGEX_PREPROCESSED_PATTERN = Pattern.compile(_REGEX_PREPROCESSED);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return REGEX_PATTERN.matcher(logLine).matches() || REGEX_PREPROCESSED_PATTERN.matcher(logLine).matches();
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
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedG1CleanupEvent(String logEntry) {
        this.logEntry = logEntry;

        Matcher matcher;

        if ((matcher = REGEX_PATTERN.matcher(logEntry)).matches()) {
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
                combinedOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3).charAt(0)).convertTo(KILOBYTES);
                combinedOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 6).charAt(0)).convertTo(KILOBYTES);
                combinedSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 7),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 9).charAt(0)).convertTo(KILOBYTES);
                duration = JdkMath.roundMillis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 10)).intValue();
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                timeUser = TimesData.NO_DATA;
                timeReal = TimesData.NO_DATA;
            }
        } else if ((matcher = REGEX_PREPROCESSED_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                // Preparsed logging has a true timestamp in gc+start, but not gc. is logging (it outputs the beginning
                // logging before the safepoint).
                if (matcher.group(2).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    timestamp = Long.parseLong(matcher.group(13));
                } else if (matcher.group(2).matches(UnifiedRegEx.UPTIME)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                } else {
                    if (matcher.group(15) != null) {
                        if (matcher.group(15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                            timestamp = Long.parseLong(matcher.group(17));
                        } else {
                            timestamp = JdkMath.convertSecsToMillis(matcher.group(16)).longValue();
                        }
                    } else {
                        // Datestamp only.
                        timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                    }
                }
                combinedOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3).charAt(0)).convertTo(KILOBYTES);
                combinedOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 6).charAt(0)).convertTo(KILOBYTES);
                combinedSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 7),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 9).charAt(0)).convertTo(KILOBYTES);
                duration = JdkMath.roundMillis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 10)).intValue();
                if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 11) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 12)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 13)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 14)).intValue();
                } else {
                    timeUser = TimesData.NO_DATA;
                    timeReal = TimesData.NO_DATA;
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
    public UnifiedG1CleanupEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString();
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

    public boolean isEndstamp() {
        boolean isEndStamp = false;
        return isEndStamp;
    }
}
