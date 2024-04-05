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
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.SerialCollection;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.SerialCollector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.github.joa.domain.GarbageCollector;

/**
 * <p>
 * UNIFIED_SERIAL_NEW
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) JDK8/11 preprocessed with {@link org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction}:
 * </p>
 * 
 * <pre>
 * [0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) DefNew: 983K-&gt;128K(1152K) Tenured: 0K-&gt;458K(768K) Metaspace: 246K-&gt;246K(1056768K) 0M-&gt;0M(1M) 1.393ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * JDK17 preprocessed with {@link org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction}:
 * </p>
 * 
 * <pre>
 * [0.060s][info][gc,start    ] GC(1) Pause Young (Allocation Failure) DefNew: 1147K(1152K)-&gt;128K(1152K) Tenured: 552K(768K)-&gt;754K(768K) Metaspace: 667K(832K)-&gt;667K(832K) 1M-&gt;0M(1M) 0.767ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedSerialNewEvent extends SerialCollector implements UnifiedLogging, BlockingEvent, YoungCollection,
        SerialCollection, YoungData, OldData, ClassData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + ")";
    /**
     * Regular expression defining the logging.
     */
    private static final String _REGEX = "" + UnifiedRegEx.DECORATOR + " Pause Young \\(" + __TRIGGER + "\\) \\DefNew: "
            + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\) Tenured: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) Metaspace: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) "
            + JdkRegEx.DURATION_MS + TimesData.REGEX_JDK9 + "[ ]*$";

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
    private long eventTime;

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
    public UnifiedSerialNewEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            eventTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 50)).intValue();
            long time = UnifiedUtil.calculateTime(matcher);
            if (!isEndstamp()) {
                timestamp = time;
            } else {
                timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
            }
            trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
            youngOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4).charAt(0)).convertTo(KILOBYTES);
            youngOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 9),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 11).charAt(0)).convertTo(KILOBYTES);
            youngSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 12),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 14).charAt(0)).convertTo(KILOBYTES);
            oldOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 15),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 17).charAt(0)).convertTo(KILOBYTES);
            oldOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 22),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 24).charAt(0)).convertTo(KILOBYTES);
            oldSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 25),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 27).charAt(0)).convertTo(KILOBYTES);
            classOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 28),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 30).charAt(0)).convertTo(KILOBYTES);
            classOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 35),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 37).charAt(0)).convertTo(KILOBYTES);
            classSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 38),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 40).charAt(0)).convertTo(KILOBYTES);
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 51) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 52)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 53)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 54)).intValue();
            }
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
    public UnifiedSerialNewEvent(String logEntry, long timestamp, int duration) {
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

    public long getDurationMicros() {
        return eventTime;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.SERIAL_NEW;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString();
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

    protected void setTrigger(GcTrigger trigger) {
        this.trigger = trigger;
    }
}
