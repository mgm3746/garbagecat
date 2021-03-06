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
 * PARALLEL_SERIAL_OLD
 * </p>
 * 
 * <p>
 * Enabled with <code>-XX:+UseParallelGC</code> JVM option. This is really a
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent}; however, the logging is different. Treat as a separate
 * event for now.
 * </p>
 * 
 * <p>
 * Uses "PSOldGen" vs. {@link org.eclipselabs.garbagecat.domain.jdk.ParallelCompactingOldEvent} "ParOldGen".
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 3.600: [Full GC [PSYoungGen: 5424K-&gt;0K(38208K)] [PSOldGen: 488K-&gt;5786K(87424K)] 5912K-&gt;5786K(125632K) [PSPermGen: 13092K-&gt;13094K(131072K)], 0.0699360 secs]
 * </pre>
 * 
 * <p>
 * 2) With trigger (Note "Full GC" vs. "Full GC (System)"):
 * </p>
 * 
 * <pre>
 * 4.165: [Full GC (System) [PSYoungGen: 1784K-&gt;0K(12736K)] [PSOldGen: 1081K-&gt;2855K(116544K)] 2865K-&gt;2855K(129280K) [PSPermGen: 8600K-&gt;8600K(131072K)], 0.0427680 secs]
 * </pre>
 * 
 * <p>
 * 3) JDK8 with comman before Metaspace block:
 * </p>
 * 
 * <pre>
 * 2018-12-06T19:04:46.807-0500: 0.122: [Full GC (Ergonomics) [PSYoungGen: 508K-&gt;385K(1536K)] [PSOldGen: 408K-&gt;501K(2048K)] 916K-&gt;887K(3584K), [Metaspace: 3680K-&gt;3680K(1056768K)], 0.0030057 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]
 * </pre>
 * 
 * TODO: Expand or extend {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent}.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class ParallelSerialOldEvent extends ParallelCollector implements BlockingEvent, YoungCollection, OldCollection,
        PermMetaspaceCollection, YoungData, OldData, PermMetaspaceData, TriggerData, SerialCollection {

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
     * Young generation size at beginning of GC event.
     */
    private Memory young;

    /**
     * Young generation size at end of GC event.
     */
    private Memory youngEnd;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngAvailable;

    /**
     * Old generation size at beginning of GC event.
     */
    private Memory old;

    /**
     * Old generation size at end of GC event.
     */
    private Memory oldEnd;

    /**
     * Space allocated to old generation.
     */
    private Memory oldAllocation;

    /**
     * Permanent generation size at beginning of GC event.
     */
    private Memory permGen;

    /**
     * Permanent generation size at end of GC event.
     */
    private Memory permGenEnd;

    /**
     * Space allocated to permanent generation.
     */
    private Memory permGenAllocation;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_SYSTEM_GC + "|" + JdkRegEx.TRIGGER_ERGONOMICS + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[Full GC (\\("
            + TRIGGER + "\\) )?\\[PSYoungGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\)\\] \\[PSOldGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)\\] "
            + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\)[,]{0,1} \\[(PSPermGen|Metaspace): " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\)\\], " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static Pattern pattern = Pattern.compile(ParallelSerialOldEvent.REGEX);

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ParallelSerialOldEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            this.timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();

            if (matcher.group(14) != null) {
                this.trigger = matcher.group(14);
            }
            this.young = kilobytes(matcher.group(16));
            this.youngEnd = kilobytes(matcher.group(17));
            this.youngAvailable = kilobytes(matcher.group(18));

            this.old = kilobytes(matcher.group(19));
            this.oldEnd = kilobytes(matcher.group(20));
            this.oldAllocation = kilobytes(matcher.group(21));

            this.permGen = kilobytes(matcher.group(26));
            this.permGenEnd = kilobytes(matcher.group(27));
            this.permGenAllocation = kilobytes(matcher.group(28));

            this.duration = JdkMath.convertSecsToMicros(matcher.group(29)).intValue();
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
    public ParallelSerialOldEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString();
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
    public static final boolean match(String logLine) {
        return pattern.matcher(logLine).matches();
    }
}
