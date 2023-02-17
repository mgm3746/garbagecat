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
import org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger.Type;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_YOUNG_PAUSE
 * </p>
 *
 * <p>
 * G1 young generation collection. Live objects from eden and survivor regions are copied to a survivor region or
 * promoted to an old space region.
 * </p>
 * 
 * <p>
 * This event triggers a {@link org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent} if any of the following
 * calculations determines space reclamation is needed:
 * </p>
 * 
 * <ul>
 * <li>The old generation occupancy as a percent of the total heap size reaches (&gt;=) Initiating Heap Occupancy
 * Percent (IHOP). IHOP is initially set to <code>InitiatingHeapOccupancyPercent</code> (default 45) and adaptive based
 * on ergonomics. If adaptive IHOP is disabled with <code>-XX:-G1UseAdaptiveIHOP</code>, IHOP is fixed at
 * <code>InitiatingHeapOccupancyPercent</code>.</li>
 * <li>The percent of free space reaches (&lt;=) <code>G1ReservePercent</code> (default 10) if adaptive IHOP is
 * enabled.</li>
 * </ul>
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
public class G1YoungPauseEvent extends G1Collector implements BlockingEvent, YoungCollection, ParallelEvent,
        CombinedData, TriggerData, TimesData, OtherTime, G1ExtRootScanningData {
    /**
     * Regular expression standard format.
     *
     * 1.234: [GC pause (young) 102M-&gt;24M(512M), 0.0254200 secs]
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR + " \\[GC pause (\\((" + GcTrigger.G1_EVACUATION_PAUSE
            + "|" + GcTrigger.G1_HUMONGOUS_ALLOCATION + "|" + GcTrigger.GCLOCKER_INITIATED_GC
            + ")\\) )?\\(young\\)(--)?[ ]{0,1}" + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
            + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

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
     * 2017-05-25T12:24:06.040+0000: 206.156: [GC pause (young) (to-space overflow), 0.77121400 secs][Ext Root Scanning
     * (ms): 1.8][Other: 544.6 ms][Eden: 1270M(1270M)->0B(723M) Survivors: 124M->175M Heap: 2468M(3072M)->1695M(3072M)]
     * [Times: user=1.51 sys=0.14, real=0.77 secs]
     */
    private static final String REGEX_PREPROCESSED_DETAILS = "^" + JdkRegEx.DECORATOR + " \\[GC pause (\\(("
            + GcTrigger.G1_EVACUATION_PAUSE + "|" + GcTrigger.GCLOCKER_INITIATED_GC + "|"
            + GcTrigger.G1_HUMONGOUS_ALLOCATION + ")\\) )?\\(young\\)( \\((" + GcTrigger.TO_SPACE_EXHAUSTED + "|"
            + GcTrigger.TO_SPACE_OVERFLOW + ")\\))?, " + JdkRegEx.DURATION + "\\]"
            + G1PreprocessAction.REGEX_EXT_ROOT_SCANNING + "?" + OtherTime.REGEX + "?\\[Eden: " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Survivors: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)\\]" + TimesData.REGEX + "?[ ]*$";

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
            + GcTrigger.G1_EVACUATION_PAUSE + ")\\) )?\\(young\\)\\[Eden: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Survivors: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + " Heap: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\]" + TimesData.REGEX + "[ ]*$";

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
                eventTime = JdkMath.convertSecsToMicros(matcher.group(18)).intValue();
                if (matcher.group(21) != null) {
                    extRootScanningTime = JdkMath.convertMillisToMicros(matcher.group(22)).intValue();
                } else {
                    extRootScanningTime = G1ExtRootScanningData.NO_DATA;
                }
                if (matcher.group(24) != null) {
                    otherTime = JdkMath.convertMillisToMicros(matcher.group(24)).intValue();
                } else {
                    otherTime = OtherTime.NO_DATA;
                }
                eden = JdkMath.convertSizeToKilobytes(matcher.group(25), matcher.group(27).charAt(0));
                edenEnd = JdkMath.convertSizeToKilobytes(matcher.group(31), matcher.group(33).charAt(0));
                combined = JdkMath.convertSizeToKilobytes(matcher.group(43), matcher.group(45).charAt(0));
                combinedEnd = JdkMath.convertSizeToKilobytes(matcher.group(49), matcher.group(51).charAt(0));
                combinedAvailable = JdkMath.convertSizeToKilobytes(matcher.group(52), matcher.group(54).charAt(0));
                if (matcher.group(55) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(56)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(57)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(58)).intValue();
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

    public long getExtRootScanningTime() {
        return extRootScanningTime;
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

    public Type getTrigger() {
        return GcTrigger.getTrigger(trigger);
    }
}