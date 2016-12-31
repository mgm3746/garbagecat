/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermCollection;
import org.eclipselabs.garbagecat.domain.PermData;
import org.eclipselabs.garbagecat.domain.SerialCollection;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
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
 * 3) After {@link org.eclipselabs.garbagecat.preprocess.jdk.DateStampPrefixPreprocessAction} with no space after Full
 * GC:
 * </p>
 * 
 * <pre>
 * raw:
 * 2013-12-09T16:43:09.366+0000: 1504.625: [Full GC2013-12-09T16:43:09.366+0000: 1504.625: [CMS: 1172695K-&gt;840574K(1549164K), 3.7572507 secs] 1301420K-&gt;840574K(1855852K), [CMS Perm : 226817K-&gt;226813K(376168K)], 3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]
 * 
 * </pre>
 * 
 * <pre>
 * preprocessed:
 * 1504.625: [Full GC1504.625: [CMS: 1172695K-&gt;840574K(1549164K), 3.7572507 secs] 1301420K-&gt;840574K(1855852K), [CMS Perm : 226817K-&gt;226813K(376168K)], 3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]
 * </pre>
 * 
 * <p>
 * 4) After {@link org.eclipselabs.garbagecat.preprocess.jdk.DateStampPrefixPreprocessAction} with trigger after "CMS":
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
public class CmsSerialOldEvent extends CmsCollector implements BlockingEvent, YoungCollection, OldCollection,
        PermCollection, YoungData, OldData, PermData, TriggerData, SerialCollection {

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the GC event in milliseconds (rounded).
     */
    private int duration;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
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
     * Permanent generation size (kilobytes) at beginning of GC event.
     */
    private int permGen;

    /**
     * Permanent generation size (kilobytes) at end of GC event.
     */
    private int permGenEnd;

    /**
     * Space allocated to permanent generation (kilobytes).
     */
    private int permGenAllocation;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_SYSTEM_GC + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE
            + "|" + JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC + "|" + JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE + "|"
            + JdkRegEx.TRIGGER_CONCURRENT_MODE_INTERRUPTED + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|" + JdkRegEx.TRIGGER_JVM_TI_FORCED_GAREBAGE_COLLECTION + "|"
            + JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC + "|" + JdkRegEx.TRIGGER_PROMOTION_FAILED + ")";

    /**
     * Regular expression for CMS_REMARK block in some events.
     */
    private static final String REMARK_BLOCK = "\\[YG occupancy: " + JdkRegEx.SIZE + " \\(" + JdkRegEx.SIZE + "\\)\\]"
            + JdkRegEx.TIMESTAMP + ": \\[Rescan \\(parallel\\) , " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
            + ": \\[weak refs processing, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP + ": \\[class unloading, "
            + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP + ": \\[scrub symbol & string tables, " + JdkRegEx.DURATION
            + "\\]";

    /**
     * Regular expression for young data block.
     */
    private static final String YOUNG_BLOCK = JdkRegEx.TIMESTAMP + ": \\[ParNew( \\(" + TRIGGER + "\\))?: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]";

    /**
     * Regular expression for old data block.
     */
    private static final String OLD_BLOCK = "(" + ClassHistogramEvent.REGEX_PREPROCESSED + ")?" + JdkRegEx.TIMESTAMP
            + ": \\[(CMS|Tenured)(bailing out to foreground collection)?( \\(" + TRIGGER + "\\))?( \\(" + TRIGGER
            + "\\))?(" + REMARK_BLOCK + ")?(: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
            + JdkRegEx.DURATION + "\\])?";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[(Full )?GC( )?(\\(" + TRIGGER + "\\) )?("
            + YOUNG_BLOCK + "|" + OLD_BLOCK + ")(" + OLD_BLOCK + ")?((" + ClassHistogramEvent.REGEX_PREPROCESSED + ")? "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)(, \\[(CMS Perm |Metaspace|Perm ): "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\])?" + JdkRegEx.ICMS_DC_BLOCK + "?, "
            + JdkRegEx.DURATION + "\\])?" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    private static Pattern pattern = Pattern.compile(CmsSerialOldEvent.REGEX);

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public CmsSerialOldEvent(String logEntry) {

        this.setLogEntry(logEntry);
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            this.timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();

            // trigger
            if (matcher.group(5) != null) {
                this.trigger = matcher.group(5);
            } else if (matcher.group(49) != null || matcher.group(82) != null) {
                this.trigger = JdkRegEx.TRIGGER_CLASS_HISTOGRAM;
            }

            // young or old block
            if (matcher.group(7) != null) {
                if (matcher.group(7).matches(YOUNG_BLOCK)) {
                    // assume promotion failure
                    this.trigger = JdkRegEx.TRIGGER_PROMOTION_FAILED;

                    this.young = Integer.parseInt(matcher.group(12));
                    // No data to determine old end size.
                    this.youngAvailable = Integer.parseInt(matcher.group(14));

                    // use young block duration for truncated events
                    if (matcher.group(98) == null) {
                        this.duration = JdkMath.convertSecsToMillis(matcher.group(15)).intValue();
                    }

                    // old block after young
                    if (matcher.group(76) != null) {
                        // update trigger
                        if (matcher.group(60) != null) {
                            this.trigger = matcher.group(60);
                        }
                        this.old = Integer.parseInt(matcher.group(77));
                        this.oldEnd = Integer.parseInt(matcher.group(78));
                        this.oldAllocation = Integer.parseInt(matcher.group(79));
                        if (matcher.group(81) != null) {
                            this.youngEnd = Integer.parseInt(matcher.group(90)) - this.oldEnd;
                        }
                    } else {
                        if (matcher.group(81) != null) {
                            this.old = Integer.parseInt(matcher.group(89)) - this.young;
                            // No data to determine old end size.
                            this.oldEnd = 0;
                            this.oldAllocation = Integer.parseInt(matcher.group(91)) - this.youngAvailable;
                        }
                    }
                } else {
                    if (matcher.group(27) != null) {
                        this.trigger = matcher.group(27);
                    }
                    if (matcher.group(44) != null) {
                        this.old = Integer.parseInt(matcher.group(44));
                    }
                    if (matcher.group(45) != null) {
                        this.oldEnd = Integer.parseInt(matcher.group(45));
                    }
                    if (matcher.group(46) != null) {
                        this.oldAllocation = Integer.parseInt(matcher.group(46));
                    }
                    if (matcher.group(81) != null) {
                        this.young = Integer.parseInt(matcher.group(89)) - this.old;
                        this.youngEnd = Integer.parseInt(matcher.group(90)) - this.oldEnd;
                        this.youngAvailable = Integer.parseInt(matcher.group(91)) - this.oldAllocation;
                    }
                }
            }

            // perm/metaspace data
            if (matcher.group(92) != null) {
                this.permGen = Integer.parseInt(matcher.group(94));
                this.permGenEnd = Integer.parseInt(matcher.group(95));
                this.permGenAllocation = Integer.parseInt(matcher.group(96));
            }

            if (matcher.group(98) != null) {
                this.duration = JdkMath.convertSecsToMillis(matcher.group(98)).intValue();
            }
        }

    }

    /**
     * Alternate constructor. Create CMS logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event happened in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in milliseconds.
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

    public int getYoungOccupancyInit() {
        return young;
    }

    protected void setYoungOccupancyInit(int young) {
        this.young = young;
    }

    public int getYoungOccupancyEnd() {
        return youngEnd;
    }

    protected void setYoungOccupancyEnd(int youngEnd) {
        this.youngEnd = youngEnd;
    }

    public int getYoungSpace() {
        return youngAvailable;
    }

    protected void setYoungSpace(int youngAvailable) {
        this.youngAvailable = youngAvailable;
    }

    public int getOldOccupancyInit() {
        return old;
    }

    protected void setOldOccupancyInit(int old) {
        this.old = old;
    }

    public int getOldOccupancyEnd() {
        return oldEnd;
    }

    protected void setOldOccupancyEnd(int oldEnd) {
        this.oldEnd = oldEnd;
    }

    public int getOldSpace() {
        return oldAllocation;
    }

    protected void setOldSpace(int oldAllocation) {
        this.oldAllocation = oldAllocation;
    }

    public int getPermOccupancyInit() {
        return permGen;
    }

    protected void setPermOccupancyInit(int permGen) {
        this.permGen = permGen;
    }

    public int getPermOccupancyEnd() {
        return permGenEnd;
    }

    protected void setPermOccupancyEnd(int permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    public int getPermSpace() {
        return permGenAllocation;
    }

    protected void setPermSpace(int permGenAllocation) {
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
        return pattern.matcher(logLine).matches();
    }
}
