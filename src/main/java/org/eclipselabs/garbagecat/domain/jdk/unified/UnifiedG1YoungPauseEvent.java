/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
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
 * 1) Timestamp is end of gc (e.g. no `gc,start` details):
 * </p>
 * 
 * <pre>
 * [18.406s][info][gc] GC(1012) Pause Young (Normal) (G1 Evacuation Pause) 38M-&gt;19M(46M) 1.815ms
 * </pre>
 * 
 * <p>
 * 2) Timestamp is beginning of gc (e.g. preprocessed with `gc,start` details):
 * </p>
 * 
 * <pre>
 * [0.369s][info][gc,start ] GC(6) Pause Young (Normal) (G1 Evacuation Pause) Ext Root Scanning (ms): 1.0 Other: 0.1ms Humongous regions: 0-&gt;0 Metaspace: 9085K-&gt;9085K(1058816K) 3M-&gt;2M(7M) 0.929ms User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 * 
 * <p>
 * 3) Preprocessed with "To-space" exhausted.
 * </p>
 * 
 * <pre>
 * [2021-03-13T03:37:40.047+0530][79853115ms] GC(8645)[gc,start] GC(8645) Pause Young (Concurrent Start) (G1 Evacuation Pause) To-space exhausted Other: 0.4ms Humongous regions: 18-&gt;18 Metaspace: 214096K-&gt;214096K(739328K) 8186M-&gt;8186M(8192M) 3.471ms User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1YoungPauseEvent extends G1Collector implements UnifiedLogging, BlockingEvent, YoungCollection,
        ParallelEvent, ClassData, CombinedData, TriggerData, TimesData, OtherTime, G1ExtRootScanningData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + "|"
            + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|" + GcTrigger.G1_PREVENTIVE_COLLECTION.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + ")";

    /**
     * Regular expression defining logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR
            + " Pause Young( \\((Normal|Concurrent Start)\\))? \\(" + __TRIGGER + "\\) "
            + UnifiedPreprocessAction.REGEX_G1_EXT_ROOT_SCANNING + "?(To\\-space exhausted )?(" + OtherTime.REGEX
            + " )?(Humongous regions: \\d{1,}->\\d{1,} )?(Metaspace: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE
            + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) )?" + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + TimesData.REGEX_JDK9 + "?[ ]*$";

    private static final Pattern PATTERN = Pattern.compile(UnifiedG1YoungPauseEvent._REGEX);

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
    public UnifiedG1YoungPauseEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            eventTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 33)).intValue();
            long time = UnifiedUtil.calculateTime(matcher);
            if (!isEndstamp()) {
                timestamp = time;
            } else {
                timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
            }
            trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3));
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4) != null) {
                extRootScanningTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5))
                        .intValue();
            } else {
                extRootScanningTime = G1ExtRootScanningData.NO_DATA;
            }
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 7) != null) {
                otherTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 8)).intValue();
            } else {
                otherTime = OtherTime.NO_DATA;
            }
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 10) != null) {
                classOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 11),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 13).charAt(0)).convertTo(KILOBYTES);
                classOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 18),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 20).charAt(0)).convertTo(KILOBYTES);
                classSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 21),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 23).charAt(0)).convertTo(KILOBYTES);
            }
            combinedOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 24),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 26).charAt(0)).convertTo(KILOBYTES);
            combinedOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 27),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 29).charAt(0)).convertTo(KILOBYTES);
            combinedSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 30),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 32).charAt(0)).convertTo(KILOBYTES);
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 34) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 35)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 36)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 37)).intValue();
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
     * @param eventTime
     *            The elapsed clock time for the GC event in microseconds.
     */
    public UnifiedG1YoungPauseEvent(String logEntry, long timestamp, int eventTime) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.eventTime = eventTime;
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

    public EventType getEventType() {
        return JdkUtil.EventType.UNIFIED_G1_YOUNG_PAUSE;
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

    /**
     * @return True if "To-space" is exhausted, false otherwise.
     */
    public boolean isToSpaceExhausted() {
        boolean isTooSpaceExhausted = false;
        isTooSpaceExhausted = logEntry.matches("^.+ " + Constants.G1_TO_SPACE_EXHAUSTED + ".*");
        return isTooSpaceExhausted;
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
