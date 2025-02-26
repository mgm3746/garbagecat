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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
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
 * PARALLEL_SCAVENGE
 * </p>
 * 
 * <p>
 * Young generation collector used when <code>-XX:+UseParallelGC</code> or <code>-XX:+UseParallelOldGC</code> JVM
 * options specified.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 19810.091: [GC [PSYoungGen: 27808K-&gt;632K(28032K)] 160183K-&gt;133159K(585088K), 0.0225213 secs]
 * </pre>
 * 
 * <p>
 * 2) With 2 dashes after the GC indicates a <code>JdkRegEx.Promotion.PROMOTION_FAILED</code>. See <a href=
 * "http://hg.openjdk.java.net/jdk8u/jdk8u/hotspot/file/de8045923ad2/src/share/vm/gc_implementation/parallelScavenge/psScavenge.cpp">psScavenge.cpp</a>.
 * This seems to happen when the JVM is stressed out doing continuous full GCs.
 * </p>
 * 
 * <pre>
 * 14112.691: [GC-- [PSYoungGen: 313864K-&gt;313864K(326656K)] 879670K-&gt;1012935K(1025728K), 0.9561947 secs]
 * </pre>
 * 
 * <p>
 * 3) JDK8 with trigger.
 * </p>
 * 
 * <pre>
 * 1.219: [GC (Metadata GC Threshold) [PSYoungGen: 1226834K-&gt;17779K(1835008K)] 1226834K-&gt;17795K(6029312K), 0.0144911 secs] [Times: user=0.04 sys=0.00, real=0.01 secs]
 * </pre>
 * 
 * <p>
 * 4) With 2 dashes before PSYoungGen block. Undetermined what causes this.
 * </p>
 * 
 * <pre>
 * 2017-02-01T15:56:24.437+0000: 1025076.327: [GC (Allocation Failure) --[PSYoungGen: 385537K-&gt;385537K(397824K)] 1271095K-&gt;1275901K(1288192K), 0.1674611 secs] [Times: user=0.24 sys=0.00, real=0.17 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class ParallelScavengeEvent extends ParallelCollector
        implements BlockingEvent, YoungCollection, ParallelEvent, YoungData, OldData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.LAST_DITCH_COLLECTION.getRegex() + "|" + GcTrigger.HEAP_INSPECTION_INITIATED_GC.getRegex() + "|"
            + GcTrigger.SYSTEM_GC.getRegex() + "|" + GcTrigger.HEAP_DUMP_INITIATED_GC.getRegex() + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[GC(--)? (\\(" + __TRIGGER
            + "\\) )?(--)?\\[PSYoungGen: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]"
            + TimesData.REGEX + "?[ ]*$";

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
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ParallelScavengeEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
            } else if (matcher.group(1) != null) {
                if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
            }
            trigger = GcTrigger.getTrigger(matcher.group(16));
            youngOccupancyInit = memory(matcher.group(19), matcher.group(21).charAt(0)).convertTo(KILOBYTES);
            youngOccupancyEnd = memory(matcher.group(22), matcher.group(24).charAt(0)).convertTo(KILOBYTES);
            youngSpace = memory(matcher.group(25), matcher.group(27).charAt(0)).convertTo(KILOBYTES);
            oldOccupancyInit = memory(matcher.group(28), matcher.group(30).charAt(0)).convertTo(KILOBYTES)
                    .minus(youngOccupancyInit);
            oldOccupancyEnd = memory(matcher.group(31), matcher.group(33).charAt(0)).convertTo(KILOBYTES)
                    .minus(youngOccupancyEnd);
            oldSpace = memory(matcher.group(34), matcher.group(36).charAt(0)).convertTo(KILOBYTES).minus(youngSpace);
            duration = JdkMath.convertSecsToMicros(matcher.group(37)).intValue();
            if (matcher.group(40) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(41)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(42)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(43)).intValue();
            }
        }
    }

    /**
     * Alternate constructor. Create parallel scavenge logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public ParallelScavengeEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public long getDurationMicros() {
        return duration;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.PARALLEL_SCAVENGE;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.PARALLEL_SCAVENGE;
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
}
