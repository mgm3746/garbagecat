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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermMetaspaceCollection;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
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
public class SerialOldEvent extends SerialCollector implements BlockingEvent, YoungCollection, OldCollection,
        PermMetaspaceCollection, YoungData, OldData, PermMetaspaceData, TriggerData, SerialCollection, TimesData {

    /**
     * Regular expression for SERIAL_NEW block in some events.
     */
    public static final String __SERIAL_NEW_BLOCK = JdkRegEx.DECORATOR + " \\[DefNew( \\(("
            + GcTrigger.PROMOTION_FAILED.getRegex() + ")\\) )?: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\), " + JdkRegEx.DURATION + "\\]";

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String __TRIGGER = "(" + GcTrigger.SYSTEM_GC.getRegex() + "|"
            + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|" + GcTrigger.ALLOCATION_FAILURE.getRegex() + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[(Full )?GC( \\(" + __TRIGGER + "\\))?([ ]{0,1}"
            + __SERIAL_NEW_BLOCK + ")?( )?" + JdkRegEx.DECORATOR + " \\[Tenured: " + JdkRegEx.SIZE_K + "->"
            + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE_K + "->"
            + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), \\[(Perm |Metaspace): " + JdkRegEx.SIZE_K + "->"
            + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)\\], " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX
            + "?[ ]*$";

    private static Pattern pattern = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static boolean match(String logLine) {
        return pattern.matcher(logLine).matches();
    }

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long duration;

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Old generation size at beginning of GC event.
     */
    private Memory old;

    /**
     * Space allocated to old generation.
     */
    private Memory oldAllocation;

    /**
     * Old generation size at end of GC event.
     */
    private Memory oldEnd;

    /**
     * Permanent generation size at beginning of GC event.
     */
    private Memory permGen;

    /**
     * Space allocated to permanent generation.
     */
    private Memory permGenAllocation;

    /**
     * Permanent generation size at end of GC event.
     */
    private Memory permGenEnd;

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
     * Young generation size at beginning of GC event.
     */
    private Memory young;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngAvailable;

    /**
     * Young generation size at end of GC event.
     */
    private Memory youngEnd;

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
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
            } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            } else {
                // Datestamp only.
                timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
            }
            // Use last trigger
            if (matcher.group(33) != null) {
                trigger = GcTrigger.getTrigger(matcher.group(33));
            } else if (matcher.group(16) != null) {
                trigger = GcTrigger.getTrigger(matcher.group(16));
            } else {
                trigger = GcTrigger.NONE;
            }
            old = kilobytes(matcher.group(54));
            oldEnd = kilobytes(matcher.group(55));
            oldAllocation = kilobytes(matcher.group(56));
            young = kilobytes(matcher.group(60)).minus(getOldOccupancyInit());
            youngEnd = kilobytes(matcher.group(61)).minus(getOldOccupancyEnd());
            youngAvailable = kilobytes(matcher.group(62)).minus(getOldSpace());
            // Do not need total begin/end/allocation, as these can be calculated.
            permGen = kilobytes(matcher.group(64));
            permGenEnd = kilobytes(matcher.group(65));
            permGenAllocation = kilobytes(matcher.group(66));
            duration = JdkMath.convertSecsToMicros(matcher.group(67)).intValue();
            if (matcher.group(70) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(71)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(72)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(73)).intValue();
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

    public long getDuration() {
        return duration;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.SERIAL_OLD;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.SERIAL_OLD.toString();
    }

    public Memory getOldOccupancyEnd() {
        return oldEnd;
    }

    public Memory getOldOccupancyInit() {
        return old;
    }

    public Memory getOldSpace() {
        return oldAllocation;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

    public Memory getPermOccupancyEnd() {
        return permGenEnd;
    }

    public Memory getPermOccupancyInit() {
        return permGen;
    }

    public Memory getPermSpace() {
        return permGenAllocation;
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
        return youngEnd;
    }

    public Memory getYoungOccupancyInit() {
        return young;
    }

    public Memory getYoungSpace() {
        return youngAvailable;
    }

    protected void setDuration(int duration) {
        this.duration = duration;
    }

    protected void setLogEntry(String logEntry) {
        this.logEntry = logEntry;
    }

    protected void setOldOccupancyEnd(Memory oldEnd) {
        this.oldEnd = oldEnd;
    }

    protected void setOldOccupancyInit(Memory old) {
        this.old = old;
    }

    protected void setOldSpace(Memory oldAllocation) {
        this.oldAllocation = oldAllocation;
    }

    protected void setPermOccupancyEnd(Memory permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    protected void setPermOccupancyInit(Memory permGen) {
        this.permGen = permGen;
    }

    protected void setPermSpace(Memory permGenAllocation) {
        this.permGenAllocation = permGenAllocation;
    }

    protected void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    protected void setTrigger(GcTrigger trigger) {
        this.trigger = trigger;
    }

    protected void setYoungOccupancyEnd(Memory youngEnd) {
        this.youngEnd = youngEnd;
    }

    protected void setYoungOccupancyInit(Memory young) {
        this.young = young;
    }

    protected void setYoungSpace(Memory youngAvailable) {
        this.youngAvailable = youngAvailable;
    }
}
