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
import org.eclipselabs.garbagecat.domain.ClassData;
import org.eclipselabs.garbagecat.domain.ClassSpaceCollection;
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.SerialCollection;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.github.joa.domain.GarbageCollector;

/**
 * <p>
 * SERIAL_OLD
 * </p>
 * 
 * <p>
 * Enabled with the <code>-XX:+UseSerialGC</code> JVM option. Uses a mark-sweep-compact algorithm.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 187.159: [Full GC 187.160: [Tenured: 97171K-&gt;102832K(815616K), 0.6977443 secs] 152213K-&gt;102832K(907328K), [Perm : 49152K-&gt;49154K(49158K)], 0.6929258 secs]
 * </pre>
 * 
 * <p>
 * 2) JDK 1.6 with trigger:
 * </p>
 * 
 * <pre>
 * 2.457: [Full GC (System) 2.457: [Tenured: 1092K-&gt;2866K(116544K), 0.0489980 secs] 11012K-&gt;2866K(129664K), [Perm : 8602K-&gt;8602K(131072K)], 0.0490880 secs]
 * </pre>
 * 
 * <p>
 * 3) Combined {@link org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent} and
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent} with permanent generation data.
 * 
 * <p>
 * It looks like this is a result of the young generation guarantee. The young generation fills up to where it exceeds
 * the old generation free space, so a full collection is triggered to free up old space.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * 3727.365: [GC 3727.365: [DefNew: 400314K-&gt;400314K(400384K), 0.0000550 secs]3727.365: [Tenured: 837793K-&gt;597490K(889536K), 44.7498530 secs] 1238107K-&gt;597490K(1289920K), [Perm : 54745K-&gt;54745K(54784K)], 44.7501880 secs] [Times: user=5.32 sys=0.33, real=44.75 secs]
 * </pre>
 * 
 * <p>
 * With Metaspace and Datestamps.
 * </p>
 * 
 * <pre>
 * 2.447: [Full GC (Metadata GC Threshold) 2.447: [Tenured: 0K-&gt;12062K(524288K), 0.1248607 secs] 62508K-&gt;12062K(760256K), [Metaspace: 20526K-&gt;20526K(1069056K)], 0.1249442 secs] [Times: user=0.18 sys=0.08, real=0.13 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class SerialOldEvent extends SerialCollector implements BlockingEvent, ClassData, ClassSpaceCollection,
        OldCollection, OldData, SerialCollection, TimesData, TriggerData, YoungCollection, YoungData {

    /**
     * Regular expression for SERIAL_NEW block in some events.
     */
    public static final String __SERIAL_NEW_BLOCK = JdkRegEx.DECORATOR + " \\[DefNew( \\(("
            + GcTrigger.PROMOTION_FAILED.getRegex() + ")\\) )?: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]";

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.LAST_DITCH_COLLECTION.getRegex() + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|"
            + GcTrigger.SYSTEM_GC.getRegex() + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[(Full )?GC( \\(" + __TRIGGER + "\\))?([ ]{0,1}"
            + __SERIAL_NEW_BLOCK + ")?( )?" + JdkRegEx.DECORATOR + " \\[Tenured: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), \\[(Perm |Metaspace): " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\], " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX
            + "?[ ]*$";

    private static final Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static boolean match(String logLine) {
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
     * Default constructor
     */
    public SerialOldEvent() {
    }

    /**
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public SerialOldEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
            } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            } else {
                // Datestamp only.
                timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
            }
            // Use last trigger
            if (matcher.group(33) != null) {
                trigger = GcTrigger.getTrigger(matcher.group(33));
            } else if (matcher.group(16) != null) {
                trigger = GcTrigger.getTrigger(matcher.group(16));
            } else {
                trigger = GcTrigger.NONE;
            }
            oldOccupancyInit = memory(matcher.group(60), matcher.group(62).charAt(0)).convertTo(KILOBYTES);
            oldOccupancyEnd = memory(matcher.group(63), matcher.group(65).charAt(0)).convertTo(KILOBYTES);
            oldSpace = memory(matcher.group(66), matcher.group(68).charAt(0)).convertTo(KILOBYTES);
            youngOccupancyInit = memory(matcher.group(72), matcher.group(74).charAt(0)).convertTo(KILOBYTES)
                    .minus(getOldOccupancyInit());
            youngOccupancyEnd = memory(matcher.group(75), matcher.group(77).charAt(0)).convertTo(KILOBYTES)
                    .minus(getOldOccupancyEnd());
            youngSpace = memory(matcher.group(78), matcher.group(80).charAt(0)).convertTo(KILOBYTES)
                    .minus(getOldSpace());
            // Do not need total begin/end/allocation, as these can be calculated.
            classOccupancyInit = memory(matcher.group(82), matcher.group(84).charAt(0)).convertTo(KILOBYTES);
            classOccupancyEnd = memory(matcher.group(85), matcher.group(87).charAt(0)).convertTo(KILOBYTES);
            classSpace = memory(matcher.group(88), matcher.group(90).charAt(0)).convertTo(KILOBYTES);
            duration = JdkMath.convertSecsToMicros(matcher.group(91)).intValue();
            if (matcher.group(94) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(95)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(96)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(97)).intValue();
            }
        }
    }

    /**
     * Alternate constructor. Create serial old detail logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public SerialOldEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.EventType.SERIAL_OLD;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.SERIAL_OLD;
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

    protected void setClassSpace(Memory classSpace) {
        this.classOccupancyInit = classSpace;
    }

    protected void setClassSpaceAllocation(Memory classSpaceAllocation) {
        this.classSpace = classSpaceAllocation;
    }

    protected void setClassSpaceEnd(Memory classSpaceEnd) {
        this.classOccupancyEnd = classSpaceEnd;
    }

    protected void setDuration(int duration) {
        this.duration = duration;
    }

    protected void setLogEntry(String logEntry) {
        this.logEntry = logEntry;
    }

    protected void setOldOccupancyEnd(Memory oldEnd) {
        this.oldOccupancyEnd = oldEnd;
    }

    protected void setOldOccupancyInit(Memory old) {
        this.oldOccupancyInit = old;
    }

    protected void setOldSpace(Memory oldAllocation) {
        this.oldSpace = oldAllocation;
    }

    protected void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    protected void setTrigger(GcTrigger trigger) {
        this.trigger = trigger;
    }

    protected void setYoungOccupancyEnd(Memory youngEnd) {
        this.youngOccupancyEnd = youngEnd;
    }

    protected void setYoungOccupancyInit(Memory young) {
        this.youngOccupancyInit = young;
    }

    protected void setYoungSpace(Memory youngAvailable) {
        this.youngSpace = youngAvailable;
    }
}
