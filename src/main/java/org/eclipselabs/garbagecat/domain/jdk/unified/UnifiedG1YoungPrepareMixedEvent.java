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
import org.eclipselabs.garbagecat.domain.ClassData;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.OtherTime;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
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
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * UNIFIED_G1_YOUNG_PREPARE_MIXED
 * </p>
 * 
 * <p>
 * Final young-only phase when it has been determined that space-reclamation will follow with a
 * {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1MixedPauseEvent}.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Timestamp is end of gc (e.g. no `gc,start` details):
 * </p>
 * 
 * <pre>
 * [217224.994s][info][gc] GC(137) Pause Young (Prepare Mixed) (G1 Evacuation Pause) 13840M->7940M(16384M) 44.565ms"
 * </pre>
 * 
 * <p>
 * 2) Timestamp is beginning of gc (e.g. preprocessed with `gc,start` details):
 * </p>
 * 
 * <pre>
 * [16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) Other: 0.1ms Humongous regions: 0-&gt;0 Metaspace: 3801K-&gt;3801K(1056768K) 24M-&gt;13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1YoungPrepareMixedEvent extends G1Collector
        implements UnifiedLogging, BlockingEvent, YoungCollection, ParallelEvent, ClassData, CombinedData, TriggerData,
        TimesData, OtherTime, G1ExtRootScanningData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + "|"
            + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|" + GcTrigger.G1_PREVENTIVE_COLLECTION.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + ")";

    /**
     * Regular expression defining logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Pause Young \\(Prepare Mixed\\) \\("
            + __TRIGGER + "\\) " + UnifiedPreprocessAction.REGEX_G1_EXT_ROOT_SCANNING + "?(" + OtherTime.REGEX
            + " )?(Humongous regions: \\d{1,}->\\d{1,} )?(Metaspace: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE
            + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) )?" + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + TimesData.REGEX_JDK9 + "?[ ]*$";

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
     * Permanent generation or metaspace occupancy at end of GC event.
     */
    private Memory classOccupancyEnd = Memory.ZERO;

    /**
     * Permanent generation or metaspace occupancy at beginning of GC event.
     */
    private Memory classOccupancyInit = Memory.ZERO;

    /**
     * Space allocated to permanent generation or metaspace.
     */
    private Memory classSpace = Memory.ZERO;

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
    public UnifiedG1YoungPrepareMixedEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            eventTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 30)).intValue();
            long time = UnifiedUtil.calculateTime(matcher);
            if (!isEndstamp()) {
                timestamp = time;
            } else {
                timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
            }
            trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4) != null) {
                extRootScanningTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5))
                        .intValue();
            } else {
                extRootScanningTime = G1ExtRootScanningData.NO_DATA;
            }
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5) != null) {
                otherTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5)).intValue();
            } else {
                otherTime = OtherTime.NO_DATA;
            }
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 7) != null) {
                classOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 8),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 10).charAt(0)).convertTo(KILOBYTES);
                classOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 15),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 17).charAt(0)).convertTo(KILOBYTES);
                classSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 18),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 20).charAt(0)).convertTo(KILOBYTES);
            }
            combinedOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 21),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 23).charAt(0)).convertTo(KILOBYTES);
            combinedOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 24),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 26).charAt(0)).convertTo(KILOBYTES);
            combinedSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 27),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 29).charAt(0)).convertTo(KILOBYTES);
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 31) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 32)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 33)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 34)).intValue();
            } else {
                timeUser = TimesData.NO_DATA;
                timeReal = TimesData.NO_DATA;
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
    public UnifiedG1YoungPrepareMixedEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.eventTime = duration;
    }

    public Memory getClassOccupancyEnd() {
        return classOccupancyEnd;
    }

    public Memory getClassOccupancyInit() {
        return classOccupancyInit;
    }

    public Memory getClassSpace() {
        return classSpace;
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

    public long getExtRootScanningTime() {
        return extRootScanningTime;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString();
    }

    @Override
    public long getOtherTime() {
        return otherTime;
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
        // default assumes gc,start not logged (e.g. not preprocessed)
        boolean isEndStamp = true;
        isEndStamp = !logEntry.matches(UnifiedRegEx.TAG_GC_START);
        return isEndStamp;
    }

    protected void setClassSpace(Memory classSpace) {
        this.classOccupancyInit = classSpace;
    }

    protected void setClassSpaceAllocation(Memory classSpaceAllocation) {
        this.classSpace = classSpaceAllocation;
    }

    protected void setClassSpaceEnd(Memory classSpaceEnd) {
        this.classOccupancyEnd = classSpaceEnd;
    }
}
