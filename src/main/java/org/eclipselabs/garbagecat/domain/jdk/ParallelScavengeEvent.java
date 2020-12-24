/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

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
 * <h3>Example Logging</h3>
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

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private int duration;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Young generation size (kilobytes) at beginning of GC event.
     */
    private int young;

    /**
     * Young generation size (kilobytes) at end of GC event.
     */
    private int youngEnd;

    /**
     * Available space in young generation (kilobytes). Equals young generation allocation minus one survivor space.
     */
    private int youngAvailable;

    /**
     * Old generation size (kilobytes) at beginning of GC event.
     */
    private int old;

    /**
     * Old generation size (kilobytes) at end of GC event.
     */
    private int oldEnd;

    /**
     * Space allocated to old generation (kilobytes).
     */
    private int oldAllocation;

    /**
     * The trigger for the GC event.
     */
    private String trigger;
    /**
     * The time of all user (non-kernel) threads added together in centiseconds.
     */
    private int timeUser;

    /**
     * The time of all system (kernel) threads added together in centiseconds.
     */
    private int timeSys;

    /**
     * The wall (clock) time in centiseconds.
     */
    private int timeReal;

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|"
            + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|" + JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_SYSTEM_GC + "|" + JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[GC(--)? (\\("
            + TRIGGER + "\\) )?(--)?\\[PSYoungGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\)\\] " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), "
            + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern pattern = Pattern.compile(ParallelScavengeEvent.REGEX);

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
            timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            trigger = matcher.group(15);
            young = Integer.parseInt(matcher.group(18));
            youngEnd = Integer.parseInt(matcher.group(19));
            youngAvailable = Integer.parseInt(matcher.group(20));
            int totalBegin = Integer.parseInt(matcher.group(21));
            old = totalBegin - young;
            int totalEnd = Integer.parseInt(matcher.group(22));
            oldEnd = totalEnd - youngEnd;
            int totalAllocation = Integer.parseInt(matcher.group(23));
            oldAllocation = totalAllocation - youngAvailable;
            duration = JdkMath.convertSecsToMicros(matcher.group(24)).intValue();
            if (matcher.group(27) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(28)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(29)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(30)).intValue();
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

    public String getLogEntry() {
        return logEntry;
    }

    public int getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getYoungOccupancyInit() {
        return young;
    }

    public int getYoungOccupancyEnd() {
        return youngEnd;
    }

    public int getYoungSpace() {
        return youngAvailable;
    }

    public int getOldOccupancyInit() {
        return old;
    }

    public int getOldOccupancyEnd() {
        return oldEnd;
    }

    public int getOldSpace() {
        return oldAllocation;
    }

    public String getName() {
        return JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    public int getTimeUser() {
        return timeUser;
    }

    public int getTimeSys() {
        return timeSys;
    }

    public int getTimeReal() {
        return timeReal;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

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
}
