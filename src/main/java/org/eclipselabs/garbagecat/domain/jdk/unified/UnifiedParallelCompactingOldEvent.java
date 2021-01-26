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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceCollection;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.ParallelCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * UNIFIED_PARALLEL_COMPACTING_OLD
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelCompactingOldEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * Preprocessed with {@link org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction}:
 * </p>
 * 
 * <pre>
 * [0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K-&gt;496K(1536K) ParOldGen: 472K-&gt;432K(2048K) Metaspace: 701K-&gt;701K(1056768K) 0M-&gt;0M(3M) 4.336ms User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedParallelCompactingOldEvent extends ParallelCollector
        implements UnifiedLogging, BlockingEvent, OldCollection, PermMetaspaceCollection, ParallelEvent, YoungData,
        OldData, PermMetaspaceData, TriggerData, TimesData {

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
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_ERGONOMICS + ")";

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX_PREPROCESSED = "" + UnifiedRegEx.DECORATOR + " Pause Full \\(" + TRIGGER
            + "\\) PSYoungGen: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) ParOldGen: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Metaspace: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + UnifiedRegEx.DURATION + TimesData.REGEX_JDK9 + "[ ]*$";

    private static final Pattern pattern = Pattern.compile(UnifiedParallelCompactingOldEvent.REGEX_PREPROCESSED);

    /**
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedParallelCompactingOldEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                timestamp = Long.parseLong(matcher.group(13));
            } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            } else {
                if (matcher.group(15) != null) {
                    if (matcher.group(15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        timestamp = Long.parseLong(matcher.group(17));
                    } else {
                        timestamp = JdkMath.convertSecsToMillis(matcher.group(16)).longValue();
                    }
                } else {
                    // Datestamp only.
                    timestamp = UnifiedUtil.convertDatestampToMillis(matcher.group(1));
                }
            }
            trigger = matcher.group(25);
            young = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(26)), matcher.group(28).charAt(0));
            youngEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(29)), matcher.group(31).charAt(0));
            youngAvailable = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(32)), matcher.group(34).charAt(0));
            old = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(35)), matcher.group(37).charAt(0));
            oldEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(38)), matcher.group(40).charAt(0));
            oldAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(41)), matcher.group(43).charAt(0));
            permGen = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(44)), matcher.group(46).charAt(0));
            permGenEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(47)), matcher.group(49).charAt(0));
            permGenAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(50)), matcher.group(52).charAt(0));
            duration = JdkMath.convertMillisToMicros(matcher.group(62)).intValue();
            timeUser = JdkMath.convertSecsToCentis(matcher.group(64)).intValue();
            timeSys = JdkMath.convertSecsToCentis(matcher.group(65)).intValue();
            timeReal = JdkMath.convertSecsToCentis(matcher.group(66)).intValue();
        }
    }

    /**
     * Alternate constructor. Create serial logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public UnifiedParallelCompactingOldEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    protected void setTrigger(String trigger) {
        this.trigger = trigger;
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
