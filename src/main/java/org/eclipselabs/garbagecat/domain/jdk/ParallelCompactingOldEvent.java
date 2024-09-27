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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.ClassData;
import org.eclipselabs.garbagecat.domain.ClassSpaceCollection;
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.github.joa.domain.GarbageCollector;

/**
 * <p>
 * PARALLEL_COMPACTING_OLD
 * </p>
 * 
 * <p>
 * New throughput multi-threaded collector introduced in JDK 5 update 6 and significantly enhanced in JDK 6. Enabled
 * with the <code>-XX:+UseParallelOldGC</code> JVM option.
 * </p>
 * 
 * <p>
 * If not specified, full collections are performed single-threaded with the
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} collector.
 * </p>
 * 
 * <p>
 * Performing full collections in parallel results in lower garbage collection overhead and better application
 * performance, particularly for applications with large heaps running on multiprocessor hardware.
 * </p>
 * 
 * <p>
 * Uses "ParOldGen" vs. {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} "PSOldGen".
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 2182.541: [Full GC [PSYoungGen: 1940K-&gt;0K(98560K)] [ParOldGen: 813929K-&gt;422305K(815616K)] 815869K-&gt;422305K(914176K) [PSPermGen: 81960K-&gt;81783K(164352K)], 2.4749181 secs]
 * </pre>
 * 
 * <p>
 * 2) Alternate format (Note "Full GC" vs. "Full GC (System)"):
 * </p>
 * 
 * <pre>
 * 2.417: [Full GC (System) [PSYoungGen: 1788K-&gt;0K(12736K)] [ParOldGen: 1084K-&gt;2843K(116544K)] 2872K-&gt;2843K(129280K) [PSPermGen: 8602K-&gt;8593K(131072K)], 0.1028360 secs]
 * </pre>
 * 
 * <p>
 * 3) JDK8 with "Metaspace" instead of "PSPermGen" and comma after old gen block.
 * </p>
 * 
 * <pre>
 * 1.234: [Full GC (Metadata GC Threshold) [PSYoungGen: 17779K-&gt;0K(1835008K)] [ParOldGen: 16K-&gt;16894K(4194304K)] 17795K-&gt;16894K(6029312K), [Metaspace: 19114K-&gt;19114K(1067008K)], 0.0352132 secs] [Times: user=0.09 sys=0.00, real=0.04 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class ParallelCompactingOldEvent extends ParallelCollector implements BlockingEvent, OldCollection,
        ClassSpaceCollection, ParallelEvent, YoungData, OldData, ClassData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|"
            + GcTrigger.SYSTEM_GC.getRegex() + "|" + GcTrigger.LAST_DITCH_COLLECTION.getRegex() + "|"
            + GcTrigger.ERGONOMICS.getRegex() + "|" + GcTrigger.HEAP_INSPECTION_INITIATED_GC.getRegex() + "|"
            + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|" + GcTrigger.HEAP_DUMP_INITIATED_GC.getRegex() + ")";

    /**
     * Regular expression defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[Full GC (\\(" + __TRIGGER
            + "\\) )?\\[PSYoungGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\)\\] \\[ParOldGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)\\] "
            + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)(,)? \\[(PSPermGen|Metaspace): "
            + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)\\], " + JdkRegEx.DURATION
            + "\\]" + TimesData.REGEX + "?[ ]*$";

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
    public ParallelCompactingOldEvent(String logEntry) {
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
            trigger = GcTrigger.getTrigger(matcher.group(15));
            youngOccupancyInit = kilobytes(matcher.group(17));
            youngOccupancyEnd = kilobytes(matcher.group(18));
            youngSpace = kilobytes(matcher.group(19));
            oldOccupancyInit = kilobytes(matcher.group(20));
            oldOccupancyEnd = kilobytes(matcher.group(21));
            oldSpace = kilobytes(matcher.group(22));
            // Do not need total begin/end/allocation, as these can be calculated.
            classOccupancyInit = kilobytes(matcher.group(28));
            classOccupancyEnd = kilobytes(matcher.group(29));
            classSpace = kilobytes(matcher.group(30));
            duration = JdkMath.convertSecsToMicros(matcher.group(31)).intValue();
            if (matcher.group(34) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(35)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(36)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(37)).intValue();
            }
        }
    }

    /**
     * Alternate constructor. Create parallel old detail logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public ParallelCompactingOldEvent(String logEntry, long timestamp, int duration) {
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

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.PARALLEL_OLD;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString();
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
