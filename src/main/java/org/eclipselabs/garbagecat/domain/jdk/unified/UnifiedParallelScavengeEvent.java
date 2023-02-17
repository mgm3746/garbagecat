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
import static org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil.DECORATOR_SIZE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.ParallelCollector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger.Type;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.github.joa.domain.GarbageCollector;

/**
 * <p>
 * UNIFIED_PARALLEL_SCAVENGE
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * Preprocessed with {@link org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction}:
 * </p>
 * 
 * <p>
 * 1) When in combination with {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialOldEvent}:
 * </p>
 * 
 * <pre>
 * [0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: 512K-&gt;464K(1024K) PSOldGen: 0K-&gt;8K(512K) Metaspace: 120K-&gt;120K(1056768K) 0M-&gt;0M(1M) 1.195ms User=0.01s Sys=0.01s Real=0.00s
 * </pre>
 * 
 * <p>
 * 2) When in combination with {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelCompactingOldEvent}:
 * </p>
 * 
 * <pre>
 * [0.029s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: 512K-&gt;432K(1024K) ParOldGen: 0K-&gt;8K(512K) Metaspace: 121K-&gt;121K(1056768K) 0M-&gt;0M(1M) 0.762ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 *
 * <p>
 * 3) JDK17:
 * </p>
 * 
 * <pre>
 * [0.026s][info][gc,start    ] GC(0) Pause Young (Allocation Failure) PSYoungGen: 512K(1024K)-&gt;448K(1024K) ParOldGen: 0K(512K)-&gt;8K(512K) Metaspace: 88K(192K)-&gt;88K(192K) 0M-&gt;0M(1M) 0.656ms User=0.01s Sys=0.00s Real=0.00s[0.029s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: 512K-&gt;432K(1024K) ParOldGen: 0K-&gt;8K(512K) Metaspace: 121K-&gt;121K(1056768K) 0M-&gt;0M(1M) 0.762ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedParallelScavengeEvent extends ParallelCollector implements UnifiedLogging, BlockingEvent,
        YoungCollection, ParallelEvent, YoungData, OldData, PermMetaspaceData, TriggerData, TimesData {

    private static final Pattern pattern = Pattern.compile(UnifiedParallelScavengeEvent.REGEX_PREPROCESSED);

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX_PREPROCESSED = "" + UnifiedRegEx.DECORATOR + " Pause Young \\("
            + UnifiedParallelScavengeEvent.TRIGGER + "\\)( Promotion failed)? PSYoungGen: " + JdkRegEx.SIZE + "(\\("
            + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) (PS|Par)OldGen: " + JdkRegEx.SIZE
            + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Metaspace: "
            + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS
            + TimesData.REGEX_JDK9 + "[ ]*$";

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE + "|" + GcTrigger.GCLOCKER_INITIATED_GC
            + "|" + GcTrigger.HEAP_DUMP_INITIATED_GC + "|" + GcTrigger.METADATE_GC_CLEAR_SOFT_REFERENCES + "|"
            + GcTrigger.METADATA_GC_THRESHOLD + ")";

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
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
    private String trigger;

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
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedParallelScavengeEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                timestamp = Long.parseLong(matcher.group(12));
            } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(11)).longValue();
            } else {
                if (matcher.group(14) != null) {
                    if (matcher.group(14).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        timestamp = Long.parseLong(matcher.group(16));
                    } else {
                        timestamp = JdkMath.convertSecsToMillis(matcher.group(15)).longValue();
                    }
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
            }
            trigger = matcher.group(DECORATOR_SIZE + 1);
            young = memory(matcher.group(DECORATOR_SIZE + 3), matcher.group(DECORATOR_SIZE + 5).charAt(0))
                    .convertTo(KILOBYTES);
            youngEnd = memory(matcher.group(DECORATOR_SIZE + 10), matcher.group(DECORATOR_SIZE + 12).charAt(0))
                    .convertTo(KILOBYTES);
            youngAvailable = memory(matcher.group(DECORATOR_SIZE + 13), matcher.group(DECORATOR_SIZE + 15).charAt(0))
                    .convertTo(KILOBYTES);
            old = memory(matcher.group(DECORATOR_SIZE + 17), matcher.group(DECORATOR_SIZE + 19).charAt(0))
                    .convertTo(KILOBYTES);
            oldEnd = memory(matcher.group(DECORATOR_SIZE + 24), matcher.group(DECORATOR_SIZE + 26).charAt(0))
                    .convertTo(KILOBYTES);
            oldAllocation = memory(matcher.group(DECORATOR_SIZE + 27), matcher.group(DECORATOR_SIZE + 29).charAt(0))
                    .convertTo(KILOBYTES);
            permGen = memory(matcher.group(DECORATOR_SIZE + 30), matcher.group(DECORATOR_SIZE + 32).charAt(0))
                    .convertTo(KILOBYTES);
            permGenEnd = memory(matcher.group(DECORATOR_SIZE + 37), matcher.group(DECORATOR_SIZE + 39).charAt(0))
                    .convertTo(KILOBYTES);
            permGenAllocation = memory(matcher.group(DECORATOR_SIZE + 40), matcher.group(DECORATOR_SIZE + 42).charAt(0))
                    .convertTo(KILOBYTES);
            duration = JdkMath.convertMillisToMicros(matcher.group(DECORATOR_SIZE + 52)).intValue();
            timeUser = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 54)).intValue();
            timeSys = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 55)).intValue();
            timeReal = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 56)).intValue();
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
    public UnifiedParallelScavengeEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.PARALLEL_SCAVENGE;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString();
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

    public Type getTrigger() {
        return GcTrigger.getTrigger(trigger);
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

    protected void setPermOccupancyEnd(Memory permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    protected void setPermOccupancyInit(Memory permGen) {
        this.permGen = permGen;
    }

    protected void setPermSpace(Memory permGenAllocation) {
        this.permGenAllocation = permGenAllocation;
    }

    protected void setTrigger(String trigger) {
        this.trigger = trigger;
    }
}
