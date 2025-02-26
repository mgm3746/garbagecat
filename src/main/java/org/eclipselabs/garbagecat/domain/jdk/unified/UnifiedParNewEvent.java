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
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.CmsCollector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.github.joa.domain.GarbageCollector;

/**
 * <p>
 * UNIFIED_PAR_NEW
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * Preprocessed with {@link org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction}:
 * </p>
 * 
 * <pre>
 * [0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: 974K-&gt;128K(1152K) CMS: 0K-&gt;518K(960K) Metaspace: 250K-&gt;250K(1056768K) 0M-&gt;0M(2M) 3.544ms User=0.01s Sys=0.01s Real=0.01s
 * </pre>
 * 
 * <pre>
 * [2022-10-25T08:41:22.776-0400] GC(0) Pause Young (Allocation Failure) ParNew: 935K-&gt;128K(1152K) CMS: 0K-&gt;486K(960K) Metaspace: 244K(4480K)-&gt;244K(4480K) 0M-&gt;0M(2M) 1.944ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedParNewEvent extends CmsCollector implements UnifiedLogging, BlockingEvent, YoungCollection,
        ParallelEvent, YoungData, OldData, ClassData, TriggerData, TimesData {
    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + ")";
    /**
     * Regular expression defining the logging.
     */
    private static final String _REGEX_PREPROCESSED = "" + UnifiedRegEx.DECORATOR + " Pause Young \\(" + __TRIGGER
            + "\\) ParNew: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) CMS: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Metaspace: " + JdkRegEx.SIZE + "(\\("
            + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + TimesData.REGEX_JDK9 + "[ ]*$";
    private static final Pattern REGEX_PREPROCESSED_PATTERN = Pattern.compile(_REGEX_PREPROCESSED);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return REGEX_PREPROCESSED_PATTERN.matcher(logLine).matches();
    }

    /**
     * Permanent generation or metaspace occupancy at end of GC event.
     */
    private Memory classOccupancyEnd;

    /**
     * Permanent generation or metaspace occupancy at beginning of GC event.
     */
    private Memory classOccupancyInit;

    /**
     * Space allocated to permanent generation or metaspace.
     */
    private Memory classSpace;
    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long duration;

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Old generation occupancy at end of GC event.
     */
    private Memory oldOccupancyEnd;

    /**
     * Old generation occupancy at beginning of GC event.
     */
    private Memory oldOccupancyInit;

    /**
     * Space allocated to old generation.
     */
    private Memory oldSpace;

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
     * Young generation occupancy at end of GC event.
     */
    private Memory youngOccupancyEnd;

    /**
     * Young generation occupancy at beginning of GC event.
     */
    private Memory youngOccupancyInit;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngSpace;

    /**
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedParNewEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = REGEX_PREPROCESSED_PATTERN.matcher(logEntry);
        if (matcher.find()) {
            // Preparsed logging has a true timestamp (it outputs the beginning logging before the safepoint).
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
            trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
            youngOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4).charAt(0)).convertTo(KILOBYTES);
            youngOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 7).charAt(0)).convertTo(KILOBYTES);
            youngSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 8),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 10).charAt(0)).convertTo(KILOBYTES);
            oldOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 11),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 13).charAt(0)).convertTo(KILOBYTES);
            oldOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 14),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 16).charAt(0)).convertTo(KILOBYTES);
            oldSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 17),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 19).charAt(0)).convertTo(KILOBYTES);
            classOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 20),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 22).charAt(0)).convertTo(KILOBYTES);
            classOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 27),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 29).charAt(0)).convertTo(KILOBYTES);
            classSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 30),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 32).charAt(0)).convertTo(KILOBYTES);
            duration = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 42)).intValue();
            timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 44)).intValue();
            timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 45)).intValue();
            timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 46)).intValue();
        }
    }

    /**
     * Alternate constructor. Create serial logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public UnifiedParNewEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
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

    public long getDurationMicros() {
        return duration;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.UNIFIED_PAR_NEW;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.CMS;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public Memory getOldOccupancyEnd() {
        return oldOccupancyEnd;
    }

    public Memory getOldOccupancyInit() {
        return oldOccupancyInit;
    }

    public Memory getOldSpace() {
        return oldSpace;
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

    public Memory getYoungOccupancyEnd() {
        return youngOccupancyEnd;
    }

    public Memory getYoungOccupancyInit() {
        return youngOccupancyInit;
    }

    public Memory getYoungSpace() {
        return youngSpace;
    }

    public boolean isEndstamp() {
        boolean isEndStamp = false;
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

    protected void setTrigger(GcTrigger trigger) {
        this.trigger = trigger;
    }
}
