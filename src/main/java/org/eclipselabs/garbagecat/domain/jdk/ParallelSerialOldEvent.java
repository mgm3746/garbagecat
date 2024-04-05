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
import org.github.joa.domain.GarbageCollector;

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
 * <h2>Example Logging</h2>
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
        ClassSpaceCollection, YoungData, OldData, ClassData, TriggerData, SerialCollection, TimesData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.SYSTEM_GC.getRegex() + "|" + GcTrigger.ERGONOMICS.getRegex()
            + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[Full GC (\\(" + __TRIGGER
            + "\\) )?\\[PSYoungGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\)\\] \\[PSOldGen: " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)\\] "
            + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K
            + "\\)[,]{0,1} \\[(PSPermGen|Metaspace): " + JdkRegEx.SIZE_K + "->" + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\)\\], " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

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
     * /** The wall (clock) time in centiseconds.
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
    public ParallelSerialOldEvent(String logEntry) {
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
            if (matcher.group(15) != null) {
                this.trigger = GcTrigger.getTrigger(matcher.group(15));
            }
            this.youngOccupancyInit = kilobytes(matcher.group(17));
            this.youngOccupancyEnd = kilobytes(matcher.group(18));
            this.youngSpace = kilobytes(matcher.group(19));
            this.oldOccupancyInit = kilobytes(matcher.group(20));
            this.oldOccupancyEnd = kilobytes(matcher.group(21));
            this.oldSpace = kilobytes(matcher.group(22));
            this.classOccupancyInit = kilobytes(matcher.group(27));
            this.classOccupancyEnd = kilobytes(matcher.group(28));
            this.classSpace = kilobytes(matcher.group(29));
            this.duration = JdkMath.convertSecsToMicros(matcher.group(30)).intValue();
            if (matcher.group(33) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(34)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(35)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(36)).intValue();
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
    public ParallelSerialOldEvent(String logEntry, long timestamp, int duration) {
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
        return GarbageCollector.PARALLEL_SERIAL_OLD;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString();
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
