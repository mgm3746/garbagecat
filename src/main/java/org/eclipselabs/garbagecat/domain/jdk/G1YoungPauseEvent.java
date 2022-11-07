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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.OtherTime;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_YOUNG_PAUSE
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
 * 1.305: [GC pause (young) 102M-&gt;24M(512M), 0.0254200 secs]
 * </pre>
 *
 * <p>
 * 2) With -XX:+PrintGCDateStamps:
 * </p>
 *
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [GC pause (young) 102M-&gt;24M(512M), 0.0254200 secs]
 * </pre>
 *
 * <p>
 * 3) After {@link org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction}:
 * </p>
 *
 * <pre>
 *
 * 0.807: [GC pause (young), 0.00290200 secs][ 29M-&gt;2589K(59M)] [Times: user=0.01 sys=0.00, real=0.01 secs]
 * </pre>
 *
 * <p>
 * 3) After {@link org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction} with trigger:
 * </p>
 *
 * <pre>
 * 2.847: [GC pause (G1 Evacuation Pause) (young), 0.0414530 secs] [Eden: 112.0M(112.0M)-&gt;0.0B(112.0M) Survivors: 16.0M-&gt;16.0M Heap: 136.9M(30.0G)-&gt;70.9M(30.0G)]
 * </pre>
 *
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 *
 */
public class G1YoungPauseEvent extends G1Collector
        implements BlockingEvent, YoungCollection, ParallelEvent, CombinedData, TriggerData, TimesData, OtherTime {

    /**
     * Regular expression standard format.
     *
     * 1.234: [GC pause (young) 102M-&gt;24M(512M), 0.0254200 secs]
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR + " \\[GC pause (\\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|" + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + ")\\) )?\\(young\\)(--)?[ ]{0,1}" + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX);

    /**
     * Regular expression preprocessed, no details.
     *
     * 0.807: [GC pause (young), 0.00290200 secs][ 29M-&gt;2589K(59M)] [Times: user=0.01 sys=0.00, real=0.01 secs]
     *
     */
    private static final String REGEX_PREPROCESSED = "^" + JdkRegEx.DECORATOR + " \\[GC pause \\(young\\), "
            + JdkRegEx.DURATION + "\\]" + OtherTime.REGEX + "?\\[ " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)\\]" + TimesData.REGEX + "?[ ]*$";

    /**
     * Regular expression preprocessed with G1 details.
     *
     * 2017-05-25T12:24:06.040+0000: 206.156: [GC pause (young) (to-space overflow), 0.77121400 secs][Other: 544.6
     * ms][Eden: 1270M(1270M)->0B(723M) Survivors: 124M->175M Heap: 2468M(3072M)->1695M(3072M)] [Times: user=1.51
     * sys=0.14, real=0.77 secs]
     */
    private static final String REGEX_PREPROCESSED_DETAILS = "^" + JdkRegEx.DECORATOR + " \\[GC pause (\\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + ")\\) )?\\(young\\)( \\(("
            + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + "|" + JdkRegEx.TRIGGER_TO_SPACE_OVERFLOW + ")\\))?, "
            + JdkRegEx.DURATION + "\\]" + OtherTime.REGEX + "?\\[Eden: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Survivors: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + " Heap: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern REGEX_PREPROCESSED_DETAILS_PATTERN = Pattern.compile(REGEX_PREPROCESSED_DETAILS);

    /**
     * Regular expression preprocessed with G1 details with no duration. Get duration from times block.
     *
     * Trigger before (young):
     *
     * 2017-04-05T09:09:00.416-0500: 201626.141: [GC pause (G1 Evacuation Pause) (young)[Eden:
     * 3808.0M(3808.0M)->0.0B(3760.0M) Survivors: 40.0M->64.0M Heap: 7253.9M(8192.0M)->3472.3M(8192.0M)] [Times:
     * user=0.22 sys=0.00, real=0.11 secs]
     */
    private static final String REGEX_PREPROCESSED_NO_DURATION = "^" + JdkRegEx.DECORATOR + " \\[GC pause (\\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + ")\\) )?\\(young\\)\\[Eden: " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Survivors: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)\\]" + TimesData.REGEX + "[ ]*$";

    private static final Pattern REGEX_PREPROCESSED_NO_DURATION_PATTERN = Pattern
            .compile(REGEX_PREPROCESSED_NO_DURATION);

    private static final Pattern REGEX_PREPROCESSED_PATTERN = Pattern.compile(REGEX_PREPROCESSED);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     *
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return REGEX_PATTERN.matcher(logLine).matches() || REGEX_PREPROCESSED_DETAILS_PATTERN.matcher(logLine).matches()
                || REGEX_PREPROCESSED_PATTERN.matcher(logLine).matches()
                || REGEX_PREPROCESSED_NO_DURATION_PATTERN.matcher(logLine).matches();
    }

    /**
     * Combined generation size at beginning of GC event.
     */
    private Memory combined;

    /**
     * Available space in multiple generation.
     */
    private Memory combinedAvailable;

    /**
     * Combined generation size at end of GC event.
     */
    private Memory combinedEnd;

    /**
     * Combined generation size at beginning of GC event.
     */
    private Memory eden;

    /**
     * Combined generation size at end of GC event.
     * 
     */
    private Memory edenEnd;

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long eventTime;

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Time spent outside of garbage collection in microseconds (rounded).
     */
    private long otherTime;

    /**
     * The wall (clock) time in centiseconds.
     */
    private int timeReal;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The time of all system (kernel) threads added together in centiseconds.
     */
    private int timeSys;

    /**
     * The time of all user (non-kernel) threads added together in centiseconds.
     */
    private int timeUser;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Create event from log entry.
     *
     * @param logEntry
     *            The log entry for the event.
     */
    public G1YoungPauseEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher;
        if ((matcher = REGEX_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
                trigger = matcher.group(15);
                combined = memory(matcher.group(17), matcher.group(19).charAt(0)).convertTo(KILOBYTES);
                combinedEnd = memory(matcher.group(20), matcher.group(22).charAt(0)).convertTo(KILOBYTES);
                combinedAvailable = memory(matcher.group(23), matcher.group(25).charAt(0)).convertTo(KILOBYTES);
                eventTime = JdkMath.convertSecsToMicros(matcher.group(26)).intValue();
                if (matcher.group(29) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(30)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(31)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(32)).intValue();
                }
            }
        } else if ((matcher = REGEX_PREPROCESSED_DETAILS_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
                if (matcher.group(17) != null) {
                    // trigger after (young):
                    trigger = matcher.group(17);
                } else {
                    // trigger before (young):
                    trigger = matcher.group(15);
                }
                if (matcher.group(22) != null) {
                    otherTime = JdkMath.convertMillisToMicros(matcher.group(22)).intValue();
                } else {
                    otherTime = TimesData.NO_DATA;
                }
                eventTime = JdkMath.convertSecsToMicros(matcher.group(18)).intValue();
                eden = JdkMath.convertSizeToKilobytes(matcher.group(23), matcher.group(25).charAt(0));
                edenEnd = JdkMath.convertSizeToKilobytes(matcher.group(29), matcher.group(31).charAt(0));
                combined = JdkMath.convertSizeToKilobytes(matcher.group(41), matcher.group(43).charAt(0));
                combinedEnd = JdkMath.convertSizeToKilobytes(matcher.group(47), matcher.group(49).charAt(0));
                combinedAvailable = JdkMath.convertSizeToKilobytes(matcher.group(50), matcher.group(52).charAt(0));
                if (matcher.group(53) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(54)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(55)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(56)).intValue();
                }
            }
        } else if ((matcher = REGEX_PREPROCESSED_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
                eventTime = JdkMath.convertSecsToMicros(matcher.group(14)).intValue();
                combined = memory(matcher.group(19), matcher.group(21).charAt(0)).convertTo(KILOBYTES);
                combinedEnd = memory(matcher.group(22), matcher.group(24).charAt(0)).convertTo(KILOBYTES);
                combinedAvailable = memory(matcher.group(25), matcher.group(27).charAt(0)).convertTo(KILOBYTES);
                if (matcher.group(28) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(29)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(30)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(31)).intValue();
                }
            }
        } else if ((matcher = REGEX_PREPROCESSED_NO_DURATION_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
                if (matcher.group(15) != null) {
                    // trigger before (young):
                    trigger = matcher.group(15);
                }
                // Get duration from times block
                eventTime = JdkMath.convertSecsToMicros(matcher.group(49)).intValue();
                combined = JdkMath.convertSizeToKilobytes(matcher.group(34), matcher.group(36).charAt(0));
                combinedEnd = JdkMath.convertSizeToKilobytes(matcher.group(40), matcher.group(42).charAt(0));
                eden = JdkMath.convertSizeToKilobytes(matcher.group(16), matcher.group(18).charAt(0));
                edenEnd = JdkMath.convertSizeToKilobytes(matcher.group(22), matcher.group(24).charAt(0));
                combinedAvailable = JdkMath.convertSizeToKilobytes(matcher.group(43), matcher.group(45).charAt(0));
                timeUser = JdkMath.convertSecsToCentis(matcher.group(47)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(48)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(49)).intValue();
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
    public G1YoungPauseEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.eventTime = duration;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combined;
    }

    public Memory getCombinedSpace() {
        return combinedAvailable;
    }

    public long getDuration() {
        return eventTime + otherTime;
    }

    public Memory getEdenOccupancyEnd() {
        return edenEnd;
    }

    public Memory getEdenOccupancyInit() {
        return eden;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString();
    }

    @Override
    public long getOtherTime() {
        return otherTime;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
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

    public String getTrigger() {
        return trigger;
    }
}