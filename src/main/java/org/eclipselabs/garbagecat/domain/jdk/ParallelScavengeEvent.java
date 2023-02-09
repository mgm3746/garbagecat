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
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
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
 * 2) With 2 dashes after the GC indicates a <code>JdkRegEx.Promotion.TRIGGER_PROMOTION_FAILED</code>. See <a href=
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

    private static final Pattern pattern = Pattern.compile(ParallelScavengeEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR + " \\[GC(--)? (\\(" + ParallelScavengeEvent.TRIGGER
            + "\\) )?(--)?\\[PSYoungGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\)\\] " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), "
            + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|"
            + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|" + JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_SYSTEM_GC + "|" + JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC + ")";

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
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ParallelScavengeEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
            } else if (matcher.group(1) != null) {
                if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
            }
            trigger = matcher.group(16);
            young = kilobytes((matcher.group(19)));
            youngEnd = kilobytes((matcher.group(20)));
            youngAvailable = kilobytes((matcher.group(21)));
            old = kilobytes(matcher.group(22)).minus(young);
            oldEnd = kilobytes(matcher.group(23)).minus(youngEnd);
            oldAllocation = kilobytes(matcher.group(24)).minus(youngAvailable);
            duration = JdkMath.convertSecsToMicros(matcher.group(25)).intValue();
            if (matcher.group(28) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(29)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(30)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(31)).intValue();
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
        return JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString();
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

    public String getTrigger() {
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
}
