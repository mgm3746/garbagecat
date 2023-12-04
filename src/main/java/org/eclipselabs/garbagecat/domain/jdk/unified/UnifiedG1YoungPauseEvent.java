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
import static org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil.DECORATOR_SIZE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.OtherTime;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.domain.jdk.G1ExtRootScanningData;
import org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
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
 * <p>
 * There are 2 types:
 * </p>
 * <ul>
 * <li>Normal: standard young generation cleanup.</li>
 * <li>Concurrent Start: young generation with preparation for a concurrent mark.</li>
 * </ul>
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
 * 2) Preprocessed:
 * </p>
 * 
 * <pre>
 * [0.369s][info][gc,start ] GC(6) Pause Young (Normal) (G1 Evacuation Pause) Ext Root Scanning (ms): 1.0 Other: 0.1ms Humongous regions: 0-&gt;0 Metaspace: 9085K-&gt;9085K(1058816K) 3M-&gt;2M(7M) 0.929ms User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1YoungPauseEvent extends G1Collector implements UnifiedLogging, BlockingEvent, YoungCollection,
        ParallelEvent, PermMetaspaceData, CombinedData, TriggerData, TimesData, OtherTime, G1ExtRootScanningData {

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String _TRIGGER = "(" + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + "|"
            + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|" + GcTrigger.G1_PREVENTIVE_COLLECTION.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + ")";

    /**
     * Regular expression defining standard logging (no details). Include all triggers, as there is no overlap with
     * <code>UnifiedYoungEvent</code>.
     * 
     * [89974.613s][info][gc] GC(1345) Pause Young (Concurrent Start) (G1 Evacuation Pause) 14593M->13853M(16384M)
     * 92.109ms
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR
            + " Pause Young \\((Normal|Concurrent Start)\\) \\(" + _TRIGGER + "\\) " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + "[ ]*$";

    private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX);

    /**
     * Regular expression defining preprocessed logging.
     * 
     * [0.369s][info][gc,start ] GC(6) Pause Young (Normal) (G1 Evacuation Pause) Ext Root Scanning (ms): 1.0 Other:
     * 0.1ms Humongous regions: 0->0 Metaspace: 9085K->9085K(1058816K) 3M->2M(7M) 0.929ms User=0.01s Sys=0.00s
     * Real=0.01s
     */
    private static final String REGEX_PREPROCESSED = "^" + UnifiedRegEx.DECORATOR
            + " Pause Young( \\((Normal|Concurrent Start)\\))? \\(" + _TRIGGER + "\\) "
            + UnifiedPreprocessAction.REGEX_G1_EXT_ROOT_SCANNING + "?" + OtherTime.REGEX
            + "?Humongous regions: \\d{1,}->\\d{1,} Metaspace: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + TimesData.REGEX_JDK9 + "[ ]*$";

    private static final Pattern REGEX_PREPROCESSED_PATTERN = Pattern.compile(REGEX_PREPROCESSED);

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
     * Combined young + old generation allocation.
     */
    private Memory combinedAllocation;

    /**
     * Combined young + old generation size at beginning of GC event.
     */
    private Memory combinedBegin;

    /**
     * Combined young + old generation size at end of GC event.
     */
    private Memory combinedEnd;
    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long eventTime;

    /**
     * The elapsed clock time for external root scanning in microseconds (rounded).
     */
    private long extRootScanningTime;

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Time spent outside of garbage collection in microseconds (rounded).
     */
    private long otherTime;

    /**
     * Permanent generation size at beginning of GC event.
     */
    private Memory permGen;

    /**
     * Space allocated to permanent generation.
     */
    private Memory permGenAllocation;

    /**
     * Permanent generation size at end of GC event.
     */
    private Memory permGenEnd;

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
                trigger = GcTrigger.getTrigger(matcher.group(DECORATOR_SIZE + 2));
                combinedBegin = memory(matcher.group(DECORATOR_SIZE + 3), matcher.group(DECORATOR_SIZE + 5).charAt(0))
                        .convertTo(KILOBYTES);
                combinedEnd = memory(matcher.group(DECORATOR_SIZE + 6), matcher.group(DECORATOR_SIZE + 8).charAt(0))
                        .convertTo(KILOBYTES);
                combinedAllocation = memory(matcher.group(DECORATOR_SIZE + 9),
                        matcher.group(DECORATOR_SIZE + 11).charAt(0)).convertTo(KILOBYTES);
                eventTime = JdkMath.convertMillisToMicros(matcher.group(DECORATOR_SIZE + 12)).intValue();
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(eventTime).longValue();
                timeUser = TimesData.NO_DATA;
                timeReal = TimesData.NO_DATA;
            }
        } else if ((matcher = REGEX_PREPROCESSED_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                // Preparsed logging has a true timestamp (it outputs the beginning logging before the safepoint).
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
                trigger = GcTrigger.getTrigger(matcher.group(DECORATOR_SIZE + 3));
                if (matcher.group(DECORATOR_SIZE + 4) != null) {
                    extRootScanningTime = JdkMath.convertMillisToMicros(matcher.group(DECORATOR_SIZE + 5)).intValue();
                } else {
                    extRootScanningTime = G1ExtRootScanningData.NO_DATA;
                }
                if (matcher.group(DECORATOR_SIZE + 6) != null) {
                    otherTime = JdkMath.convertMillisToMicros(matcher.group(DECORATOR_SIZE + 7)).intValue();
                } else {
                    otherTime = OtherTime.NO_DATA;
                }
                permGen = memory(matcher.group(DECORATOR_SIZE + 8), matcher.group(DECORATOR_SIZE + 10).charAt(0))
                        .convertTo(KILOBYTES);
                permGenEnd = memory(matcher.group(DECORATOR_SIZE + 15), matcher.group(DECORATOR_SIZE + 17).charAt(0))
                        .convertTo(KILOBYTES);
                permGenAllocation = memory(matcher.group(DECORATOR_SIZE + 18),
                        matcher.group(DECORATOR_SIZE + 20).charAt(0)).convertTo(KILOBYTES);
                combinedBegin = memory(matcher.group(DECORATOR_SIZE + 21), matcher.group(DECORATOR_SIZE + 23).charAt(0))
                        .convertTo(KILOBYTES);
                combinedEnd = memory(matcher.group(DECORATOR_SIZE + 24), matcher.group(DECORATOR_SIZE + 26).charAt(0))
                        .convertTo(KILOBYTES);
                combinedAllocation = memory(matcher.group(DECORATOR_SIZE + 27),
                        matcher.group(DECORATOR_SIZE + 29).charAt(0)).convertTo(KILOBYTES);
                eventTime = JdkMath.convertMillisToMicros(matcher.group(DECORATOR_SIZE + 30)).intValue();
                if (matcher.group(DECORATOR_SIZE + 31) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 32)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 33)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 34)).intValue();
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
        this.eventTime = duration;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combinedBegin;
    }

    public Memory getCombinedSpace() {
        return combinedAllocation;
    }

    public long getDuration() {
        return eventTime + otherTime;
    }

    public long getExtRootScanningTime() {
        return extRootScanningTime;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString();
    }

    @Override
    public long getOtherTime() {
        return otherTime;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

    public Memory getPermOccupancyEnd() {
        return permGenEnd;
    }

    public Memory getPermOccupancyInit() {
        return permGen;
    }

    public Memory getPermSpace() {
        return permGenAllocation;
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

    protected void setPermOccupancyEnd(Memory permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    protected void setPermOccupancyInit(Memory permGen) {
        this.permGen = permGen;
    }

    protected void setPermSpace(Memory permGenAllocation) {
        this.permGenAllocation = permGenAllocation;
    }
}
