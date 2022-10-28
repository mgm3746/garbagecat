/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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
import static org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil.DECORATOR_SIZE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_G1_YOUNG_PAUSE
 * </p>
 *
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.G1YoungPauseEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <p>
 * G1 young generation collection. Live objects from Eden and Survivor regions are copied to new regions, either to a
 * survivor region or promoted to the old space.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * [18.406s][info][gc] GC(1012) Pause Young (Normal) (G1 Evacuation Pause) 38M-&gt;19M(46M) 1.815ms
 * </pre>
 * 
 * <p>
 * 2) Preprocessed from logging with details:
 * </p>
 * 
 * <pre>
 * [0.101s][info][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 0M-&gt;0M(2M) 1.371ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * 3) Preprocessed from logging with details with datestamp and milliseconds:
 * </p>
 * 
 * <pre>
 * [2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 65M-&gt;8M(1304M) 57.263ms User=0.02s Sys=0.01s Real=0.06s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1YoungPauseEvent extends G1Collector implements UnifiedLogging, BlockingEvent, YoungCollection,
        ParallelEvent, PermMetaspaceData, CombinedData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|"
            + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + "|" + JdkRegEx.TRIGGER_G1_PREVENTIVE_COLLECTION + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + ")";

    /**
     * Regular expression defining standard logging (no details).
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR
            + " Pause Young \\((Normal|Concurrent Start)\\) \\(" + TRIGGER + "\\) " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + UnifiedRegEx.DURATION + "[ ]*$";

    private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX);

    /**
     * Regular expression defining preprocessed logging.
     * 
     * [0.333s][info][gc,start ] GC(0) Pause Young (G1 Evacuation Pause) Humongous regions: 13->13 Metaspace:
     * 6591K-&gt;6591K(1056768K) 25M-&gt;4M(254M) 3.523ms User=0.00s Sys=0.00s Real=0.00s
     *
     * [0.037s][info][gc,start ] GC(0) Pause Young (Normal) (G1 Preventive Collection) Humongous regions: 13->13
     * Metaspace: 331K(512K)-&gt;331K(512K) 1M->1M(4M) 0.792ms User=0.00s Sys=0.00s Real=0.00s
     */
    private static final String REGEX_PREPROCESSED = "^" + UnifiedRegEx.DECORATOR
            + " Pause Young( \\((Normal|Concurrent Start)\\))? \\(" + TRIGGER
            + "\\) Humongous regions: \\d{1,}->\\d{1,} Metaspace: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + UnifiedRegEx.DURATION + TimesData.REGEX_JDK9 + "[ ]*$";

    private static final Pattern REGEX_PREPROCESSED_PATTERN = Pattern.compile(REGEX_PREPROCESSED);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long duration;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Combined young + old generation size at beginning of GC event.
     */
    private Memory combinedBegin;

    /**
     * Combined young + old generation size at end of GC event.
     */
    private Memory combinedEnd;

    /**
     * Combined young + old generation allocation.
     */
    private Memory combinedAllocation;

    /**
     * Permanent generation size at beginning of GC event.
     */
    private Memory permGen;

    /**
     * Permanent generation size at end of GC event.
     */
    private Memory permGenEnd;

    /**
     * Space allocated to permanent generation.
     */
    private Memory permGenAllocation;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * The time of all user (non-kernel) threads added together in centiseconds.
     */
    private int timeUser;

    /**
     * The time of all system (kernel) threads added together in centiseconds.
     */
    private int timeSys;

    /**
     * The wall (clock) time in centiseconds.
     */
    private int timeReal;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedG1YoungPauseEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher;
        if ((matcher = REGEX_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                long endTimestamp;
                if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    endTimestamp = Long.parseLong(matcher.group(12));
                } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                    endTimestamp = JdkMath.convertSecsToMillis(matcher.group(11)).longValue();
                } else {
                    if (matcher.group(14) != null) {
                        if (matcher.group(14).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                            endTimestamp = Long.parseLong(matcher.group(16));
                        } else {
                            endTimestamp = JdkMath.convertSecsToMillis(matcher.group(15)).longValue();
                        }
                    } else {
                        // Datestamp only.
                        endTimestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                    }
                }
                trigger = matcher.group(DECORATOR_SIZE + 2);
                combinedBegin = memory(matcher.group(DECORATOR_SIZE + 3), matcher.group(DECORATOR_SIZE + 5).charAt(0))
                        .convertTo(KILOBYTES);
                combinedEnd = memory(matcher.group(DECORATOR_SIZE + 6), matcher.group(DECORATOR_SIZE + 8).charAt(0))
                        .convertTo(KILOBYTES);
                combinedAllocation = memory(matcher.group(DECORATOR_SIZE + 9),
                        matcher.group(DECORATOR_SIZE + 11).charAt(0)).convertTo(KILOBYTES);
                duration = JdkMath.convertMillisToMicros(matcher.group(DECORATOR_SIZE + 12)).intValue();
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                timeUser = TimesData.NO_DATA;
                timeReal = TimesData.NO_DATA;
            }
        } else if ((matcher = REGEX_PREPROCESSED_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    timestamp = Long.parseLong(matcher.group(12));
                } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(11)).longValue();
                } else {
                    if (matcher.group(14) != null) {
                        if (matcher.group(14).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                            timestamp = Long.parseLong(matcher.group(16));
                        } else {
                            timestamp = JdkMath.convertSecsToMillis(matcher.group(15)).longValue();
                        }
                    } else {
                        // Datestamp only.
                        timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                    }
                }
                trigger = matcher.group(DECORATOR_SIZE + 3);
                permGen = memory(matcher.group(DECORATOR_SIZE + 4), matcher.group(DECORATOR_SIZE + 6).charAt(0))
                        .convertTo(KILOBYTES);
                permGenEnd = memory(matcher.group(DECORATOR_SIZE + 11), matcher.group(DECORATOR_SIZE + 13).charAt(0))
                        .convertTo(KILOBYTES);
                permGenAllocation = memory(matcher.group(DECORATOR_SIZE + 14),
                        matcher.group(DECORATOR_SIZE + 16).charAt(0)).convertTo(KILOBYTES);
                combinedBegin = memory(matcher.group(DECORATOR_SIZE + 17), matcher.group(DECORATOR_SIZE + 19).charAt(0))
                        .convertTo(KILOBYTES);
                combinedEnd = memory(matcher.group(DECORATOR_SIZE + 20), matcher.group(DECORATOR_SIZE + 22).charAt(0))
                        .convertTo(KILOBYTES);
                combinedAllocation = memory(matcher.group(DECORATOR_SIZE + 23),
                        matcher.group(DECORATOR_SIZE + 25).charAt(0)).convertTo(KILOBYTES);
                duration = JdkMath.convertMillisToMicros(matcher.group(DECORATOR_SIZE + 26)).intValue();
                if (matcher.group(DECORATOR_SIZE + 27) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 28)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 29)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 30)).intValue();
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
    public UnifiedG1YoungPauseEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Memory getCombinedOccupancyInit() {
        return combinedBegin;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public Memory getCombinedSpace() {
        return combinedAllocation;
    }

    public Memory getPermOccupancyInit() {
        return permGen;
    }

    protected void setPermOccupancyInit(Memory permGen) {
        this.permGen = permGen;
    }

    public Memory getPermOccupancyEnd() {
        return permGenEnd;
    }

    protected void setPermOccupancyEnd(Memory permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    public Memory getPermSpace() {
        return permGenAllocation;
    }

    protected void setPermSpace(Memory permGenAllocation) {
        this.permGenAllocation = permGenAllocation;
    }

    public String getTrigger() {
        return trigger;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString();
    }

    public int getTimeUser() {
        return timeUser;
    }

    public int getTimeSys() {
        return timeSys;
    }

    public int getTimeReal() {
        return timeReal;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

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
}
