/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * CMS_SERIAL_OLD
 * </p>
 * 
 * <p>
 * The concurrent low pause collector does not compact. When fragmentation becomes an issue a
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent} compacts the heap. Made a separate event for tracking
 * purposes.
 * </p>
 * 
 * <p>
 * It also happens for undetermined reasons, possibly the JVM requires a certain amount of heap or combination of
 * resources that is not being met, and consequently the concurrent low pause collector is not used despite being
 * specified with the <code>-XX:+UseConcMarkSweepGC</code> JVM option.
 * </p>
 * 
 * <h3>Example Logging</h3>
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
        OldCollection, PermMetaspaceCollection, YoungData, OldData, PermMetaspaceData, TriggerData, SerialCollection {

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the GC event in micriseconds (rounded).
     */
    private int duration;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Young generation size at beginning of GC event.
     */
    private Memory young = Memory.ZERO;

    /**
     * Young generation size at end of GC event.
     */
    private Memory youngEnd = Memory.ZERO;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngAvailable = Memory.ZERO;

    /**
     * Old generation size at beginning of GC event.
     */
    private Memory old = Memory.ZERO;

    /**
     * Old generation size at end of GC event.
     */
    private Memory oldEnd = Memory.ZERO;

    /**
     * Space allocated to old generation.
     */
    private Memory oldAllocation = Memory.ZERO;

    /**
     * Permanent generation size at beginning of GC event.
     */
    private Memory permGen = Memory.ZERO;

    /**
     * Permanent generation size at end of GC event.
     */
    private Memory permGenEnd = Memory.ZERO;

    /**
     * Space allocated to permanent generation.
     */
    private Memory permGenAllocation = Memory.ZERO;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Trigger(s) after "Full GC".
     */
    private static final String TRIGGER_FULL_GC = "(" + JdkRegEx.TRIGGER_SYSTEM_GC + "|"
            + JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|"
            + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|" + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|"
            + JdkRegEx.TRIGGER_JVM_TI_FORCED_GAREBAGE_COLLECTION + "|" + JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + ")";

    /**
     * Trigger(s) after "GC".
     */
    private static final String TRIGGER_GC = "(" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + ")";

    /**
     * Trigger(s) after "CMS".
     */
    private static final String TRIGGER_CMS = "(" + JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE + "|"
            + JdkRegEx.TRIGGER_CONCURRENT_MODE_INTERRUPTED + ")";

    /**
     * Trigger(s) after "ParNew".
     */
    private static final String TRIGGER_PAR_NEW = "(" + JdkRegEx.TRIGGER_PROMOTION_FAILED + ")";

    /**
     * Regular expression for CMS_REMARK block in some events.
     */
    private static final String REMARK_BLOCK = "\\[YG occupancy: " + JdkRegEx.SIZE_K + " \\(" + JdkRegEx.SIZE_K
            + "\\)\\]" + JdkRegEx.TIMESTAMP + ": \\[Rescan \\(parallel\\) , " + JdkRegEx.DURATION + "\\]"
            + JdkRegEx.TIMESTAMP + ": \\[weak refs processing, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
            + ": \\[class unloading, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
            + ": \\[scrub symbol & string tables, " + JdkRegEx.DURATION + "\\]";

    /**
     * Regular expression defining the logging beginning with "Full GC".
     */
    private static final String REGEX_FULL_GC = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[Full GC( \\(" + TRIGGER_FULL_GC + "\\))?[ ]{0,1}(" + ClassHistogramEvent.REGEX_PREPROCESSED + ")?("
            + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": "
            + "\\[CMS(bailing out to foreground collection)?( \\(" + TRIGGER_CMS + "\\))?( \\(" + TRIGGER_CMS + "\\))?("
            + REMARK_BLOCK + ")?: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), "
            + JdkRegEx.DURATION + "\\](" + ClassHistogramEvent.REGEX_PREPROCESSED + ")? " + JdkRegEx.SIZE_K + "->"
            + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), " + "\\[(CMS Perm |Metaspace): " + JdkRegEx.SIZE_K
            + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)\\]" + JdkRegEx.ICMS_DC_BLOCK + "?, "
            + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    /**
     * Regular expression defining the logging beginning with "GC".
     */
    private static final String REGEX_GC = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[GC( \\("
            + TRIGGER_GC + "\\))?[ ]{0,1}(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[ParNew("
            + JdkRegEx.PRINT_PROMOTION_FAILURE + ")?( \\(" + TRIGGER_PAR_NEW + "\\))?: " + JdkRegEx.SIZE_K + "->"
            + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), " + JdkRegEx.DURATION + "\\]("
            + ClassHistogramEvent.REGEX_PREPROCESSED + ")?(((" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[(CMS|Tenured))?(Java HotSpot\\(TM\\) Server VM warning: )?"
            + "(bailing out to foreground collection)?( \\(" + TRIGGER_CMS + "\\))?(: " + JdkRegEx.SIZE_K + "->"
            + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), " + JdkRegEx.DURATION + "\\])?)?("
            + ClassHistogramEvent.REGEX_PREPROCESSED + ")?( " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\)(, \\[(CMS Perm |Perm |Metaspace): " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K
            + "\\(" + JdkRegEx.SIZE_K + "\\)\\])?" + JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION + "\\])?"
            + TimesData.REGEX + "?[ ]*$";

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
                this.timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                // If multiple triggers, use last one.
                if (matcher.group(52) != null) {
                    this.trigger = matcher.group(52);
                } else if (matcher.group(50) != null) {
                    this.trigger = matcher.group(50);
                } else if (matcher.group(16) != null || matcher.group(89) != null) {
                    this.trigger = JdkRegEx.TRIGGER_CLASS_HISTOGRAM;
                } else if (matcher.group(14) != null) {
                    this.trigger = matcher.group(14);
                }
                this.old = kilobytes(matcher.group(72));
                this.oldEnd = kilobytes(matcher.group(73));
                this.oldAllocation = kilobytes(matcher.group(74));
                this.young = kilobytes(matcher.group(98)).minus(this.old);
                this.youngEnd = kilobytes(matcher.group(99)).minus(this.oldEnd);
                this.youngAvailable = kilobytes(matcher.group(100)).minus(this.oldAllocation);
                this.permGen = kilobytes(matcher.group(102));
                this.permGenEnd = kilobytes(matcher.group(103));
                this.permGenAllocation = kilobytes(matcher.group(104));
                if (matcher.group(105) != null) {
                    super.setIncrementalMode(true);
                }
                this.duration = JdkMath.convertSecsToMicros(matcher.group(106)).intValue();
            }
        } else if (logEntry.matches(REGEX_GC)) {
            Pattern pattern = Pattern.compile(REGEX_GC);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                this.timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                // If multiple triggers, use last one.
                if (matcher.group(75) != null) {
                    this.trigger = matcher.group(75);
                } else if (matcher.group(30) != null) {
                    this.trigger = matcher.group(30);
                } else if (matcher.group(14) != null) {
                    this.trigger = matcher.group(14);
                } else {
                    // assume promotion failure
                    this.trigger = JdkRegEx.TRIGGER_PROMOTION_FAILED;
                }
                this.young = kilobytes(matcher.group(31));
                // No data to determine young end size.
                this.youngEnd = Memory.ZERO;
                this.youngAvailable = kilobytes(matcher.group(33));

                // use young block duration for truncated events
                if (matcher.group(113) == null) {
                    this.duration = JdkMath.convertSecsToMicros(matcher.group(34)).intValue();
                }

                // old block after young
                if (matcher.group(76) != null) {
                    this.old = kilobytes(matcher.group(77));
                    this.oldEnd = kilobytes(matcher.group(78));
                    this.oldAllocation = kilobytes(matcher.group(79));
                    if (matcher.group(105) != null) {
                        this.youngEnd = kilobytes(matcher.group(105)).minus(this.oldEnd);
                    }
                } else {
                    if (matcher.group(103) != null) {
                        this.old = kilobytes(matcher.group(104)).minus(this.young);
                        // No data to determine old end size.
                        this.oldEnd = Memory.ZERO;
                        this.oldAllocation = kilobytes(matcher.group(106)).minus(this.youngAvailable);
                    }
                }
                // perm/metaspace data
                if (matcher.group(107) != null) {
                    this.permGen = kilobytes(matcher.group(109));
                    this.permGenEnd = kilobytes(matcher.group(110));
                    this.permGenAllocation = kilobytes(matcher.group(111));
                }
                if (matcher.group(112) != null) {
                    super.setIncrementalMode(true);
                }
                if (matcher.group(113) != null) {
                    this.duration = JdkMath.convertSecsToMicros(matcher.group(113)).intValue();
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

    public String getLogEntry() {
        return logEntry;
    }

    protected void setLogEntry(String logEntry) {
        this.logEntry = logEntry;
    }

    public int getDuration() {
        return duration;
    }

    protected void setDuration(int duration) {
        this.duration = duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    protected void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Memory getYoungOccupancyInit() {
        return young;
    }

    protected void setYoungOccupancyInit(Memory young) {
        this.young = young;
    }

    public Memory getYoungOccupancyEnd() {
        return youngEnd;
    }

    protected void setYoungOccupancyEnd(Memory youngEnd) {
        this.youngEnd = youngEnd;
    }

    public Memory getYoungSpace() {
        return youngAvailable;
    }

    protected void setYoungSpace(Memory youngAvailable) {
        this.youngAvailable = youngAvailable;
    }

    public Memory getOldOccupancyInit() {
        return old;
    }

    protected void setOldOccupancyInit(Memory old) {
        this.old = old;
    }

    public Memory getOldOccupancyEnd() {
        return oldEnd;
    }

    protected void setOldOccupancyEnd(Memory oldEnd) {
        this.oldEnd = oldEnd;
    }

    public Memory getOldSpace() {
        return oldAllocation;
    }

    protected void setOldSpace(Memory oldAllocation) {
        this.oldAllocation = oldAllocation;
    }

    public Memory getPermOccupancyInit() {
        return permGen;
    }

    protected void setPermOccupancyInit(Memory permGen) {
        this.permGen = permGen;
    }

    public Memory getPermOccupancyEnd() {
        return permGenEnd;
    }

    protected void setPermOccupancyEnd(Memory permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    public Memory getPermSpace() {
        return permGenAllocation;
    }

    protected void setPermSpace(Memory permGenAllocation) {
        this.permGenAllocation = permGenAllocation;
    }

    public String getName() {
        return JdkUtil.LogEventType.CMS_SERIAL_OLD.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    protected void setTrigger(String trigger) {
        this.trigger = trigger;
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static boolean match(String logLine) {
        return logLine.matches(REGEX_FULL_GC) || logLine.matches(REGEX_GC);
    }
}
