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
            + JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC + ")";

    /**
     * Regular expression for CMS_REMARK block in some events.
     */
    private static final String REMARK_BLOCK = "\\[YG occupancy: " + JdkRegEx.SIZE + " \\(" + JdkRegEx.SIZE + "\\)\\]"
            + JdkRegEx.TIMESTAMP + ": \\[Rescan \\(parallel\\) , " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
            + ": \\[weak refs processing, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP + ": \\[class unloading, "
            + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP + ": \\[scrub symbol & string tables, " + JdkRegEx.DURATION
            + "\\]";

    /**
     * Regular expression for CMS block in some events.
     */
    private static final String CMS_BLOCK = JdkRegEx.TIMESTAMP + ": \\[CMS(bailing out to foreground collection)?( \\("
            + TRIGGER + "\\))?( \\(" + TRIGGER + "\\))?(" + REMARK_BLOCK + ")?: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[Full GC( )?(\\(" + TRIGGER + "\\) )?("
            + ClassHistogramEvent.REGEX_PREPROCESSED + ")?(" + CMS_BLOCK + ")?("
            + ClassHistogramEvent.REGEX_PREPROCESSED + ")? " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\), \\[(CMS Perm |Metaspace): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)\\]" + JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION + "\\]"
            + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

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
            if (matcher.group(17) != null) {
                // if > 1 triggers, use the last one
                this.trigger = matcher.group(17);
            } else if (matcher.group(6) != null || matcher.group(37) != null) {
                this.trigger = JdkRegEx.TRIGGER_CLASS_HISTOGRAM;
            } else if (matcher.group(4) != null) {
                this.trigger = matcher.group(4);
            }

            int totalBegin = Integer.parseInt(matcher.group(44));
            int totalEnd = Integer.parseInt(matcher.group(45));
            int totalAllocation = Integer.parseInt(matcher.group(46));

            // Only CMS block has old data
            if (matcher.group(13) != null) {
                this.old = Integer.parseInt(matcher.group(33));
                this.oldEnd = Integer.parseInt(matcher.group(34));
                this.oldAllocation = Integer.parseInt(matcher.group(35));
                this.young = totalBegin - this.old;
                this.youngEnd = totalEnd - this.oldEnd;
                this.youngAvailable = totalAllocation - this.oldAllocation;
            }

            this.permGen = Integer.parseInt(matcher.group(48));
            this.permGenEnd = Integer.parseInt(matcher.group(49));
            this.permGenAllocation = Integer.parseInt(matcher.group(50));
            this.duration = JdkMath.convertSecsToMillis(matcher.group(52)).intValue();
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
