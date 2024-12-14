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
 * CMS_SERIAL_OLD
 * </p>
 * 
 * <p>
 * When a concurrent collection of the old or permanent generation does not finish before the old or permanent
 * generation becomes full, the {@link GcTrigger#CONCURRENT_MODE_FAILURE} initiates a full GC using a slow
 * (single-threaded) serial collector in an attempt to free space.
 * 
 * 
 * <p>
 * The concurrent low pause collector does not compact. When fragmentation becomes an issue a
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent} (default) or CMS collection running in foreground mode
 * (-XX:+UseCMSCompactAtFullCollection) compacts the heap.
 * </p>
 * 
 * <p>
 * It also seems to happen for undetermined reasons, possibly the JVM requires a certain amount of heap or combination
 * of resources that is not being met, and consequently the concurrent low pause collector is not used despite being
 * specified with the <code>-XX:+UseConcMarkSweepGC</code> JVM option.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 5.980: [Full GC 5.980: [CMS: 5589K-&gt;5796K(122880K), 0.0889610 secs] 11695K-&gt;5796K(131072K), [CMS Perm : 13140K-&gt;13124K(131072K)], 0.0891270 secs]
 * </pre>
 * 
 * <p>
 * 2) JDK 1.6 format with trigger after "Full GC":
 * </p>
 * 
 * <pre>
 * 2.928: [Full GC (System) 2.929: [CMS: 0K-&gt;6501K(8218240K), 0.2525532 secs] 66502K-&gt;6501K(8367360K), [CMS Perm : 16640K-&gt;16623K(524288K)], 0.2527331 secs]
 * </pre>
 * 
 * <p>
 * 3) No space after Full GC: GC:
 * </p>
 * 
 * <pre>
 * 2013-12-09T16:43:09.366+0000: 1504.625: [Full GC2013-12-09T16:43:09.366+0000: 1504.625: [CMS: 1172695K-&gt;840574K(1549164K), 3.7572507 secs] 1301420K-&gt;840574K(1855852K), [CMS Perm : 226817K-&gt;226813K(376168K)], 3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]
 * </pre>
 * 
 * <p>
 * 4) With trigger after "CMS":
 * </p>
 * 
 * <pre>
 * raw:
 * 2013-12-09T16:43:09.366+0000: 1504.625: [Full GC2013-12-09T16:43:09.366+0000: 1504.625: [CMS: 1172695K-&gt;840574K(1549164K), 3.7572507 secs] 1301420K-&gt;840574K(1855852K), [CMS Perm : 226817K-&gt;226813K(376168K)], 3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]
 * </pre>
 * 
 * *
 * <p>
 * 5) ParNew promotion failed:
 * </p>
 * 
 * <pre>
 * 144501.626: [GC 144501.627: [ParNew (promotion failed): 680066K-&gt;680066K(707840K), 3.7067346 secs] 1971073K-&gt;1981370K(2018560K), 3.7084059 secs]
 * </pre>
 * 
 * <p>
 * 6) ParNew promotion failed in incremental mode (<code>-XX:+CMSIncrementalMode</code>):
 * </p>
 * 
 * <pre>
 * 159275.552: [GC 159275.552: [ParNew (promotion failed): 2007040K-&gt;2007040K(2007040K), 4.3393411 secs] 5167424K-&gt;5187429K(12394496K) icms_dc=7 , 4.3398519 secs] [Times: user=4.96 sys=1.91, real=4.34 secs]
 * </pre>
 * 
 * <p>
 * 7) ParNew promotion failed truncated:
 * </p>
 * 
 * <pre>
 * 5881.424: [GC 5881.424: [ParNew (promotion failed): 153272K-&gt;152257K(153344K), 0.2143850 secs]5881.639: [CMS
 * </pre>
 * 
 * <p>
 * 8) ParNew promotion failed with CMS block:
 * </p>
 * 
 * <pre>
 * 1181.943: [GC 1181.943: [ParNew (promotion failed): 145542K-&gt;142287K(149120K), 0.1316193 secs]1182.075: [CMS: 6656483K-&gt;548489K(8218240K), 9.1244297 secs] 6797120K-&gt;548489K(8367360K), 9.2564476 secs]
 * </pre>
 * 
 * <p>
 * 9) JDK 1.7 with perm data
 * </p>
 * 
 * <pre>
 * 6.102: [GC6.102: [ParNew: 19648K-&gt;2176K(19648K), 0.0184470 secs]6.121: [Tenured: 44849K-&gt;25946K(44864K), 0.2586250 secs] 60100K-&gt;25946K(64512K), [Perm : 43759K-&gt;43759K(262144K)], 0.2773070 secs] [Times: user=0.16 sys=0.01, real=0.28 secs]
 * </pre>
 * 
 * <p>
 * 10) JDK 1.8
 * </p>
 * 
 * <pre>
 * 1817.644: [GC (Allocation Failure) 1817.646: [ParNew: 1382383K-&gt;1382383K(1382400K), 0.0000530 secs]1817.646: [CMS: 2658303K-&gt;2658303K(2658304K), 8.7951430 secs] 4040686K-&gt;2873414K(4040704K), [Metaspace: 72200K-&gt;72200K(1118208K)], 8.7986750 secs] [Times: user=8.79 sys=0.01, real=8.80 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 */
public class CmsSerialOldEvent extends CmsIncrementalModeCollector implements BlockingEvent, YoungCollection,
        OldCollection, ClassSpaceCollection, YoungData, OldData, ClassData, TriggerData, SerialCollection, TimesData {

    /**
     * Regular expression for CMS_REMARK block in some events.
     */
    private static final String _REMARK_BLOCK = "\\[YG occupancy: " + JdkRegEx.SIZE + " \\(" + JdkRegEx.SIZE + "\\)\\]"
            + JdkRegEx.DECORATOR + " \\[Rescan \\(parallel\\) , " + JdkRegEx.DURATION + "\\]" + JdkRegEx.DECORATOR
            + " \\[weak refs processing, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.DECORATOR + " \\[class unloading, "
            + JdkRegEx.DURATION + "\\]" + JdkRegEx.DECORATOR + " \\[scrub symbol & string tables, " + JdkRegEx.DURATION
            + "\\]";

    /**
     * Trigger(s) after "CMS".
     */
    private static final String _TRIGGER_CMS = "(" + GcTrigger.CONCURRENT_MODE_FAILURE.getRegex() + "|"
            + GcTrigger.CONCURRENT_MODE_INTERRUPTED.getRegex() + ")";

    /**
     * Trigger(s) after "Full GC".
     */
    private static final String _TRIGGER_FULL_GC = "(" + GcTrigger.SYSTEM_GC.getRegex() + "|"
            + GcTrigger.HEAP_INSPECTION_INITIATED_GC.getRegex() + "|" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|" + GcTrigger.LAST_DITCH_COLLECTION.getRegex() + "|"
            + GcTrigger.JVMTI_FORCED_GARBAGE_COLLECTION.getRegex() + "|" + GcTrigger.HEAP_DUMP_INITIATED_GC.getRegex()
            + "|" + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + ")";

    /**
     * Trigger(s) after "GC".
     */
    private static final String _TRIGGER_GC = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + ")";

    /**
     * Trigger(s) after "ParNew".
     */
    private static final String _TRIGGER_PAR_NEW = "(" + GcTrigger.PROMOTION_FAILED.getRegex() + ")";

    /**
     * Regular expression defining the logging beginning with "Full GC".
     */
    private static final String REGEX_FULL_GC = "^" + JdkRegEx.DECORATOR + " \\[Full GC( \\(" + _TRIGGER_FULL_GC
            + "\\))?[ ]{0,1}(" + ClassHistogramEvent._REGEX_PREPROCESSED + ")?" + JdkRegEx.DECORATOR + " "
            + "\\[CMS(bailing out to foreground collection)?( \\(" + _TRIGGER_CMS + "\\))?( \\(" + _TRIGGER_CMS
            + "\\))?(" + _REMARK_BLOCK + ")?: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
            + JdkRegEx.DURATION + "\\](" + ClassHistogramEvent._REGEX_PREPROCESSED + ")? " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + "\\[(CMS Perm |Metaspace): " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\]" + JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION
            + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern REGEX_FULL_GC_PATTERN = Pattern.compile(REGEX_FULL_GC);

    /**
     * Regular expression defining the logging beginning with "GC".
     */
    private static final String REGEX_GC = "^" + JdkRegEx.DECORATOR + " \\[GC( \\(" + _TRIGGER_GC + "\\))?[ ]{0,1}"
            + JdkRegEx.DECORATOR + " \\[ParNew(" + JdkRegEx.PRINT_PROMOTION_FAILURE + ")?( \\(" + _TRIGGER_PAR_NEW
            + "\\))?: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION
            + "\\](" + ClassHistogramEvent._REGEX_PREPROCESSED + ")?((" + JdkRegEx.DECORATOR
            + " \\[(CMS|Tenured))?(Java HotSpot\\(TM\\) Server VM warning: )?"
            + "(bailing out to foreground collection)?( \\(" + _TRIGGER_CMS + "\\))?(: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\])?)?("
            + ClassHistogramEvent._REGEX_PREPROCESSED + ")?( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)(, \\[(CMS Perm |Perm |Metaspace): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)\\])?" + JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION + "\\])?"
            + TimesData.REGEX + "?[ ]*$";

    private static final Pattern REGEX_GC_PATTERN = Pattern.compile(REGEX_GC);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static boolean match(String logLine) {
        return REGEX_FULL_GC_PATTERN.matcher(logLine).matches() || REGEX_GC_PATTERN.matcher(logLine).matches();
    }

    /**
     * Permanent generation or metaspace occupancy at end of GC event.
     */
    private Memory classOccupancyEnd = Memory.ZERO;

    /**
     * Permanent generation or metaspace occupancy at beginning of GC event.
     */
    private Memory classOccupancyInit = Memory.ZERO;

    /**
     * Space allocated to permanent generation or metaspace.
     */
    private Memory classSpace = Memory.ZERO;

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
    private Memory oldOccupancyEnd = Memory.ZERO;

    /**
     * Old generation occupancy at beginning of GC event.
     */
    private Memory oldOccupancyInit = Memory.ZERO;

    /**
     * Space allocated to old generation.
     */
    private Memory oldSpace = Memory.ZERO;

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
    private Memory youngOccupancyEnd = Memory.ZERO;

    /**
     * Young generation occupancy at beginning of GC event.
     */
    private Memory youngOccupancyInit = Memory.ZERO;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngSpace = Memory.ZERO;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public CmsSerialOldEvent(String logEntry) {

        this.setLogEntry(logEntry);
        if (logEntry.matches(REGEX_FULL_GC)) {
            Pattern pattern = Pattern.compile(REGEX_FULL_GC);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
                // If multiple triggers, use last one.
                if (matcher.group(54) != null) {
                    trigger = GcTrigger.getTrigger(matcher.group(54));
                } else if (matcher.group(52) != null) {
                    trigger = GcTrigger.getTrigger(matcher.group(52));
                } else if (matcher.group(17) != null) {
                    trigger = GcTrigger.CLASS_HISTOGRAM;
                } else if (matcher.group(15) != null) {
                    trigger = GcTrigger.getTrigger(matcher.group(15));
                }
                oldOccupancyInit = memory(matcher.group(126), matcher.group(128).charAt(0)).convertTo(KILOBYTES);
                oldOccupancyEnd = memory(matcher.group(129), matcher.group(131).charAt(0)).convertTo(KILOBYTES);
                oldSpace = memory(matcher.group(132), matcher.group(134).charAt(0)).convertTo(KILOBYTES);
                youngOccupancyInit = memory(matcher.group(158), matcher.group(160).charAt(0)).convertTo(KILOBYTES)
                        .minus(oldOccupancyInit);
                youngOccupancyEnd = memory(matcher.group(161), matcher.group(163).charAt(0)).convertTo(KILOBYTES)
                        .minus(oldOccupancyEnd);
                youngSpace = memory(matcher.group(164), matcher.group(166).charAt(0)).convertTo(KILOBYTES)
                        .minus(oldSpace);
                classOccupancyInit = memory(matcher.group(168), matcher.group(170).charAt(0)).convertTo(KILOBYTES);
                classOccupancyEnd = memory(matcher.group(171), matcher.group(173).charAt(0)).convertTo(KILOBYTES);
                classSpace = memory(matcher.group(174), matcher.group(176).charAt(0)).convertTo(KILOBYTES);
                if (matcher.group(177) != null) {
                    super.setIncrementalMode(true);
                }
                duration = JdkMath.convertSecsToMicros(matcher.group(178)).intValue();
                if (matcher.group(181) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(182)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(183)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(184)).intValue();
                }
            }
        } else if (logEntry.matches(REGEX_GC)) {
            Pattern pattern = Pattern.compile(REGEX_GC);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
                // If multiple triggers, use last one.
                if (matcher.group(84) != null) {
                    trigger = GcTrigger.getTrigger(matcher.group(84));
                } else if (matcher.group(32) != null) {
                    trigger = GcTrigger.getTrigger(matcher.group(32));
                } else if (matcher.group(15) != null) {
                    trigger = GcTrigger.getTrigger(matcher.group(15));
                } else {
                    // assume promotion failure
                    trigger = GcTrigger.PROMOTION_FAILED;
                }
                youngOccupancyInit = memory(matcher.group(33), matcher.group(35).charAt(0)).convertTo(KILOBYTES);
                // No data to determine young end size.
                youngOccupancyEnd = Memory.ZERO;
                youngSpace = memory(matcher.group(39), matcher.group(41).charAt(0)).convertTo(KILOBYTES);

                // use young block duration for truncated events
                if (matcher.group(116) == null) {
                    duration = JdkMath.convertSecsToMicros(matcher.group(42)).intValue();
                }

                // old block after young
                if (matcher.group(85) != null) {
                    oldOccupancyInit = memory(matcher.group(86), matcher.group(88).charAt(0)).convertTo(KILOBYTES);
                    oldOccupancyEnd = memory(matcher.group(89), matcher.group(91).charAt(0)).convertTo(KILOBYTES);
                    oldSpace = memory(matcher.group(92), matcher.group(94).charAt(0)).convertTo(KILOBYTES);
                    if (matcher.group(118) != null) {
                        youngOccupancyEnd = memory(matcher.group(122), matcher.group(124).charAt(0))
                                .convertTo(KILOBYTES).minus(oldOccupancyEnd);
                    }
                } else {
                    if (matcher.group(118) != null) {
                        oldOccupancyInit = memory(matcher.group(119), matcher.group(121).charAt(0)).convertTo(KILOBYTES)
                                .minus(youngOccupancyInit);
                        // No data to determine old end size.
                        oldOccupancyEnd = Memory.ZERO;
                        oldSpace = memory(matcher.group(125), matcher.group(127).charAt(0)).convertTo(KILOBYTES)
                                .minus(youngSpace);
                    }
                }
                // perm/metaspace data
                if (matcher.group(128) != null) {
                    classOccupancyInit = memory(matcher.group(130), matcher.group(132).charAt(0)).convertTo(KILOBYTES);
                    classOccupancyEnd = memory(matcher.group(133), matcher.group(135).charAt(0)).convertTo(KILOBYTES);
                    classSpace = memory(matcher.group(136), matcher.group(138).charAt(0)).convertTo(KILOBYTES);
                }
                if (matcher.group(139) != null) {
                    super.setIncrementalMode(true);
                }
                if (matcher.group(140) != null) {
                    this.duration = JdkMath.convertSecsToMicros(matcher.group(140)).intValue();
                }
                if (matcher.group(143) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(144)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(145)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(146)).intValue();
                }
            }
        }
    }

    /**
     * Alternate constructor. Create CMS logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public CmsSerialOldEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.EventType.CMS_SERIAL_OLD;
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
