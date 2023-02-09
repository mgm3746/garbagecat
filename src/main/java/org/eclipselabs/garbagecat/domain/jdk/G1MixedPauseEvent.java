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
import org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_MIXED_PAUSE
 * </p>
 * 
 * <p>
 * G1 mixed generation collection. Performed at the same time as a young collection, so it is a collection of the young
 * space and the low liveness regions of the old space.
 * </p>
 * 
 * <p>
 * The <code>-XX:InitiatingHeapOccupancyPercent</code> defines the heap occupancy threshold to start a concurrent GC
 * cycle (G1 marking) Default is 45%. Lower it to start marking earlier to avoid marking not finishing before heap fills
 * up (analogous to CMS concurrent mode failure). A value of 0 results in constant GC cycles.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 1.305: [GC pause (mixed) 102M-&gt;24M(512M), 0.0254200 secs]
 * </pre>
 * 
 * <p>
 * 2) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [GC pause (mixed) 102M-&gt;24M(512M), 0.0254200 secs]
 * </pre>
 * 
 * <p>
 * 3) After {@link org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction} with trigger:
 * </p>
 * 
 * <pre>
 * 2017-06-22T12:25:26.515+0530: 66155.261: [GC pause (G1 Humongous Allocation) (mixed) (to-space exhausted), 0.2466797 secs][Ext Root Scanning (ms): 1.8][Other: 23.0 ms][Eden: 32.0M(204.0M)-&gt;0.0B(204.0M) Survivors: 0.0B-&gt;0.0B Heap: 3816.0M(4096.0M)-&gt;3734.5M(4096.0M)] [Times: user=0.39 sys=0.03, real=0.25 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1MixedPauseEvent extends G1Collector implements BlockingEvent, ParallelEvent, CombinedData, TriggerData,
        TimesData, OtherTime, G1ExtRootScanningData {

    /**
     * Regular expression standard format.
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR + " \\[GC pause( \\(" + G1MixedPauseEvent.TRIGGER
            + "\\))? \\(mixed\\)(--)? " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
            + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX);

    /**
     * Regular expression preprocessed.
     */
    private static final String REGEX_PREPROCESSED = "^" + JdkRegEx.DECORATOR + " \\[GC pause( \\("
            + G1MixedPauseEvent.TRIGGER + "\\))? \\(mixed\\)( \\(" + G1MixedPauseEvent.TRIGGER + "\\))?, "
            + JdkRegEx.DURATION + "\\]" + G1PreprocessAction.REGEX_EXT_ROOT_SCANNING + OtherTime.REGEX + "\\[Eden: "
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\) Survivors: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern REGEX_PREPROCESSED_PATTERN = Pattern.compile(REGEX_PREPROCESSED);

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|"
            + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + "|" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + ")";

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
    public G1MixedPauseEvent(String logEntry) {
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
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
                // use last trigger
                if (matcher.group(17) != null) {
                    trigger = matcher.group(17);
                } else if (matcher.group(15) != null) {
                    trigger = matcher.group(15);
                }
                eventTime = JdkMath.convertSecsToMicros(matcher.group(18)).intValue();
                if (matcher.group(21) != null) {
                    extRootScanningTime = JdkMath.convertMillisToMicros(matcher.group(22)).intValue();
                } else {
                    extRootScanningTime = G1ExtRootScanningData.NO_DATA;
                }
                if (matcher.group(23) != null) {
                    otherTime = JdkMath.convertMillisToMicros(matcher.group(24)).intValue();
                } else {
                    otherTime = OtherTime.NO_DATA;
                }
                combined = JdkMath.convertSizeToKilobytes(matcher.group(43), matcher.group(45).charAt(0));
                combinedEnd = JdkMath.convertSizeToKilobytes(matcher.group(49), matcher.group(51).charAt(0));
                combinedAvailable = JdkMath.convertSizeToKilobytes(matcher.group(52), matcher.group(54).charAt(0));
                if (matcher.group(55) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(56)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(57)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(58)).intValue();
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
    public G1MixedPauseEvent(String logEntry, long timestamp, int duration) {
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

    public long getExtRootScanningTime() {
        return extRootScanningTime;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.G1_MIXED_PAUSE.toString();
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
