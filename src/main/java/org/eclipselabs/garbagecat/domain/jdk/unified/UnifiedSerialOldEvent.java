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
import org.eclipselabs.garbagecat.domain.ClassSpaceCollection;
import org.eclipselabs.garbagecat.domain.OldCollection;
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
 * UNIFIED_SERIAL_OLD
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) JDK8/11 preprocessed with {@link org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction}:
 * </p>
 * 
 * <pre>
 * [0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure) 0M-&gt;0M(2M) 1.699ms DefNew: 1152K-&gt;0K(1152K) Tenured: 458K-&gt;929K(960K) Metaspace: 697K-&gt;697K(1056768K) 1M-&gt;0M(2M) 3.061ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <pre>
 * [0.091s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K-&gt;436K(1536K) PSOldGen: 460K-&gt;511K(2048K) Metaspace: 701K-&gt;701K(1056768K) 0M-&gt;0M(3M) 1.849ms User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * JDK17 preprocessed with {@link org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction}:
 * </p>
 * 
 * <pre>
 * [0.091s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K-&gt;436K(1536K) PSOldGen: 460K-&gt;511K(2048K) Metaspace: 701K-&gt;701K(1056768K) 0M-&gt;0M(3M) 1.849ms User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedSerialOldEvent extends SerialCollector implements UnifiedLogging, BlockingEvent, YoungCollection,
        OldCollection, ClassSpaceCollection, SerialCollection, YoungData, OldData, ClassData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.ERGONOMICS.getRegex() + "|" + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + ")";

    /**
     * Regular expression defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR
            + "( Pause Young \\(Allocation Failure\\))? Pause Full \\(" + __TRIGGER + "\\)( " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + ")? (DefNew|PSYoungGen): "
            + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\) (Tenured|PSOldGen): " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\("
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
    public UnifiedSerialOldEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            eventTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 64)).intValue();
            long time = UnifiedUtil.calculateTime(matcher);
            if (!isEndstamp()) {
                timestamp = time;
            } else {
                timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
            }
            trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2));
            youngOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 15),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 17).charAt(0)).convertTo(KILOBYTES);
            youngOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 22),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 24).charAt(0)).convertTo(KILOBYTES);
            youngSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 25),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 27).charAt(0)).convertTo(KILOBYTES);
            oldOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 29),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 31).charAt(0)).convertTo(KILOBYTES);
            oldOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 36),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 38).charAt(0)).convertTo(KILOBYTES);
            oldSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 39),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 41).charAt(0)).convertTo(KILOBYTES);
            classOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 42),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 44).charAt(0)).convertTo(KILOBYTES);
            classOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 49),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 51).charAt(0)).convertTo(KILOBYTES);
            classSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 52),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 54).charAt(0)).convertTo(KILOBYTES);
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 65) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 66)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 67)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 68)).intValue();
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
    public UnifiedSerialOldEvent(String logEntry, long timestamp, int duration) {
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
        return GarbageCollector.SERIAL_OLD;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString();
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
