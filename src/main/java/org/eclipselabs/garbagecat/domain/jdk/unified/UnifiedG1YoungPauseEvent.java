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
package org.eclipselabs.garbagecat.domain.jdk.unified;

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
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

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
 * <h3>Example Logging</h3>
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
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + ")";

    /**
     * Regular expression defining standard logging (no details).
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR
            + " Pause Young \\((Normal|Concurrent Start)\\) \\(" + TRIGGER + "\\) " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + UnifiedRegEx.DURATION + "[ ]*$";

    /**
     * Regular expression defining preprocessed logging.
     * 
     * [0.333s][info][gc,start ] GC(0) Pause Young (G1 Evacuation Pause) Metaspace: 6591K-&gt;6591K(1056768K)
     * 25M-&gt;4M(254M) 3.523ms User=0.00s Sys=0.00s Real=0.00s
     */
    private static final String REGEX_PREPROCESSED = "^" + UnifiedRegEx.DECORATOR
            + " Pause Young( \\((Normal|Concurrent Start)\\))? \\(" + TRIGGER + "\\) Metaspace: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + UnifiedRegEx.DURATION + TimesData.REGEX_JDK9 + "[ ]*$";

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
     * Combined young + old generation size (kilobytes) at beginning of GC event.
     */
    private int combinedBegin;

    /**
     * Combined young + old generation size (kilobytes) at end of GC event.
     */
    private int combinedEnd;

    /**
     * Combined young + old generation allocation (kilobytes).
     */
    private int combinedAllocation;

    /**
     * Permanent generation size (kilobytes) at beginning of GC event.
     */
    private int permGen;

    /**
     * Permanent generation size (kilobytes) at end of GC event.
     */
    private int permGenEnd;

    /**
     * Space allocated to permanent generation (kilobytes).
     */
    private int permGenAllocation;

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
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                long endTimestamp;
                if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    endTimestamp = Long.parseLong(matcher.group(13));
                } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                    endTimestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                } else {
                    if (matcher.group(15) != null) {
                        if (matcher.group(15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                            endTimestamp = Long.parseLong(matcher.group(17));
                        } else {
                            endTimestamp = JdkMath.convertSecsToMillis(matcher.group(16)).longValue();
                        }
                    } else {
                        // Datestamp only.
                        endTimestamp = UnifiedUtil.convertDatestampToMillis(matcher.group(1));
                    }
                }
                trigger = matcher.group(26);
                combinedBegin = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(27)), matcher.group(29).charAt(0));
                combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(30)), matcher.group(32).charAt(0));
                combinedAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(33)),
                        matcher.group(35).charAt(0));
                duration = JdkMath.convertMillisToMicros(matcher.group(36)).intValue();
                timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                timeUser = TimesData.NO_DATA;
                timeReal = TimesData.NO_DATA;
            }
        } else if (logEntry.matches(REGEX_PREPROCESSED)) {
            Pattern pattern = Pattern.compile(REGEX_PREPROCESSED);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                    timestamp = Long.parseLong(matcher.group(13));
                } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
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
                        timestamp = UnifiedUtil.convertDatestampToMillis(matcher.group(1));
                    }
                }
                trigger = matcher.group(27);
                permGen = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(28)), matcher.group(30).charAt(0));
                permGenEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(31)), matcher.group(33).charAt(0));
                permGenAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(34)),
                        matcher.group(36).charAt(0));
                combinedBegin = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(37)), matcher.group(39).charAt(0));
                combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(40)), matcher.group(42).charAt(0));
                combinedAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(43)),
                        matcher.group(45).charAt(0));
                duration = JdkMath.convertMillisToMicros(matcher.group(46)).intValue();
                if (matcher.group(47) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(48)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(49)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(50)).intValue();
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

    public int getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getCombinedOccupancyInit() {
        return combinedBegin;
    }

    public int getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public int getCombinedSpace() {
        return combinedAllocation;
    }

    public int getPermOccupancyInit() {
        return permGen;
    }

    protected void setPermOccupancyInit(int permGen) {
        this.permGen = permGen;
    }

    public int getPermOccupancyEnd() {
        return permGenEnd;
    }

    protected void setPermOccupancyEnd(int permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    public int getPermSpace() {
        return permGenAllocation;
    }

    protected void setPermSpace(int permGenAllocation) {
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
        return logLine.matches(REGEX) || logLine.matches(REGEX_PREPROCESSED);
    }
}
