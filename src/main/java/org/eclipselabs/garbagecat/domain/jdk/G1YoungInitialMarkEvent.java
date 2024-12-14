/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * G1_YOUNG_INITIAL_MARK
 * </p>
 * 
 * <p>
 * G1 collector young generation initial marking triggered by
 * {@link org.eclipselabs.garbagecat.domain.jdk.G1YoungPauseEvent} when it is determined space reclamation is needed or
 * by a humongous
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 1.305: [GC pause (young) (initial-mark) 102M-&gt;24M(512M), 0.0254200 secs]
 * </pre>
 * 
 * <p>
 * 2) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [GC pause (young) (initial-mark) 102M-&gt;24M(512M), 0.0254200 secs]
 * </pre>
 * 
 * <p>
 * 3) Preprocessed:
 * </p>
 * 
 * <pre>
 * 2020-02-26T17:18:26.505+0000: 130.241: [GC pause (System.gc()) (young) (initial-mark), 0.1009346 secs][Ext Root Scanning (ms): 1.8][Other: 7.5 ms][Eden: 220.0M(241.0M)-&gt;0.0B(277.0M) Survivors: 28.0M-&gt;34.0M Heap: 924.5M(2362.0M)-&gt;713.5M(2362.0M)] [Times: user=0.19 sys=0.00, real=0.10 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1YoungInitialMarkEvent extends G1Collector implements BlockingEvent, CombinedData, TriggerData,
        ParallelEvent, TimesData, OtherTime, G1ExtRootScanningData {

    /**
     * Regular expressions defining the logging.
     * 
     * 1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[GC pause (\\(("
            + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|" + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|"
            + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|" + GcTrigger.G1_EVACUATION_PAUSE.getRegex()
            + ")\\) )?\\(young\\) \\(initial-mark\\)(--)? " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    /**
     * Regular expression preprocessed.
     * 
     * 27474.176: [GC pause (young) (initial-mark), 0.4234530 secs][Eden: 5376.0M(7680.0M)->0.0B(6944.0M) Survivors:
     * 536.0M->568.0M Heap: 13.8G(26.0G)->8821.4M(26.0G)] [Times: user=1.66 sys=0.02, real=0.43 secs]
     * 
     * 2017-02-20T20:17:04.874-0500: 40442.077: [GC pause (G1 Humongous Allocation) (young) (initial-mark), 0.0142482
     * secs]
     */
    private static final String _REGEX_PREPROCESSED = "^" + JdkRegEx.DECORATOR + " \\[GC pause (\\(("
            + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|" + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|"
            + GcTrigger.SYSTEM_GC.getRegex() + ")\\) )?\\(young\\)( \\(initial-mark\\))?( \\(("
            + GcTrigger.TO_SPACE_EXHAUSTED.getRegex() + ")\\))?(, " + JdkRegEx.DURATION + "\\])?"
            + G1PreprocessAction.REGEX_EXT_ROOT_SCANNING + "?(" + OtherTime.REGEX + ")?(\\[Eden: " + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Survivors: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\]" + TimesData.REGEX + "?)?[ ]*$";

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
     * Combined generation occupancy at end of GC event.
     */
    private Memory combinedOccupancyEnd = Memory.ZERO;

    /**
     * Combined generation occupancy at beginning of GC event.
     */
    private Memory combinedOccupancyInit = Memory.ZERO;

    /**
     * Available space in multiple generation.
     */
    private Memory combinedSpace = Memory.ZERO;

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
    private GcTrigger trigger;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public G1YoungInitialMarkEvent(String logEntry) {
        this.logEntry = logEntry;

        Matcher matcher;

        if ((matcher = REGEX_PATTERN.matcher(logEntry)).matches()) {
            // standard format
            matcher.reset();
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
                trigger = GcTrigger.getTrigger(matcher.group(15));
                combinedOccupancyInit = memory(matcher.group(17), matcher.group(19).charAt(0)).convertTo(KILOBYTES);
                combinedOccupancyEnd = memory(matcher.group(20), matcher.group(22).charAt(0)).convertTo(KILOBYTES);
                combinedSpace = memory(matcher.group(23), matcher.group(25).charAt(0)).convertTo(KILOBYTES);
                eventTime = JdkMath.convertSecsToMicros(matcher.group(26)).intValue();
                if (matcher.group(29) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(30)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(31)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(32)).intValue();
                }
            }
        } else if ((matcher = REGEX_PREPROCESSED_PATTERN.matcher(logEntry)).matches()) {
            // preprocessed format
            matcher.reset();
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
                if (matcher.group(15) != null) {
                    trigger = GcTrigger.getTrigger(matcher.group(15));
                } else if (matcher.group(19) != null) {
                    trigger = GcTrigger.getTrigger(matcher.group(19));
                } else {
                    trigger = GcTrigger.NONE;
                }
                if (matcher.group(24) != null) {
                    extRootScanningTime = JdkMath.convertMillisToMicros(matcher.group(25)).intValue();
                } else {
                    extRootScanningTime = G1ExtRootScanningData.NO_DATA;
                }
                if (matcher.group(26) != null) {
                    otherTime = JdkMath.convertMillisToMicros(matcher.group(27)).intValue();
                } else {
                    otherTime = OtherTime.NO_DATA;
                }
                if (matcher.group(20) != null) {
                    eventTime = JdkMath.convertSecsToMicros(matcher.group(21)).intValue();
                } else {
                    if (matcher.group(59) != null) {
                        // Use Times block duration
                        eventTime = JdkMath.convertSecsToMicros(matcher.group(61)).intValue();
                    }
                }
                if (matcher.group(28) != null) {
                    combinedOccupancyInit = JdkMath.convertSizeToKilobytes(matcher.group(47),
                            matcher.group(49).charAt(0));
                    combinedOccupancyEnd = JdkMath.convertSizeToKilobytes(matcher.group(53),
                            matcher.group(55).charAt(0));
                    combinedSpace = JdkMath.convertSizeToKilobytes(matcher.group(56), matcher.group(58).charAt(0));
                }
                if (matcher.group(59) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(60)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(61)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(62)).intValue();
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
    public G1YoungInitialMarkEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.eventTime = duration;
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
        return eventTime + otherTime;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.G1_YOUNG_INITIAL_MARK;
    }

    public long getExtRootScanningTime() {
        return extRootScanningTime;
    }

    public String getLogEntry() {
        return logEntry;
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

    public GcTrigger getTrigger() {
        return trigger;
    }
}
