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
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceCollection;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.jdk.UnknownCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * UNIFIED_OLD
 * </p>
 * 
 * <p>
 * Either {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} or
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelCompactingOldEvent}.
 * </p>
 * 
 * <p>
 * Both {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} and
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelCompactingOldEvent} use the same logging pattern in JDK9+ when
 * logging without details (<code>-Xlog:gc:file=&lt;file&gt;</code>).
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Normal:
 * </p>
 * 
 * <pre>
 * [0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M-&gt;1M(7M) 2.969ms
 * </pre>
 * 
 * <p>
 * 2) Preprocessed:
 * </p>
 * 
 * <pre>
 * [2020-06-24T18:13:47.695-0700][173690ms] GC(74) Pause Full (System.gc()) Metaspace: 260211K-&gt;260197K(1290240K) 887M-&gt;583M(1223M) 3460.196ms User=1.78s Sys=0.01s Real=3.46s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedOldEvent extends UnknownCollector implements UnifiedLogging, BlockingEvent, OldCollection,
        PermMetaspaceCollection, PermMetaspaceData, CombinedData, TriggerData, ParallelEvent, TimesData {

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
     * Combined young + old generation size (kilobytes) at beginning of GC event.
     */
    private int combinedBegin;

    /**
     * Combined young + old generation size (kilobytes) at end of GC event.
     */
    private int combinedEnd;

    /**
     * Combined young + old generation allocation (kilobytes).
     */
    private int combinedAllocation;

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
            + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|"
            + JdkRegEx.TRIGGER_ERGONOMICS + "|" + JdkRegEx.TRIGGER_SYSTEM_GC + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR + " Pause Full \\(" + TRIGGER + "\\)( Metaspace: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\))? " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + UnifiedRegEx.DURATION + TimesData.REGEX_JDK9 + "?[ ]*$";

    private static final Pattern pattern = Pattern.compile(REGEX);

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedOldEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            long endTimestamp;
            if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                endTimestamp = Long.parseLong(matcher.group(13));
            } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                endTimestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            } else {
                if (matcher.group(15) != null) {
                    if (matcher.group(15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        endTimestamp = Long.parseLong(matcher.group(17));
                    } else {
                        endTimestamp = JdkMath.convertSecsToMillis(matcher.group(16)).longValue();
                    }
                } else {
                    // Datestamp only.
                    endTimestamp = UnifiedUtil.convertDatestampToMillis(matcher.group(1));
                }
            }
            trigger = matcher.group(25);
            if (matcher.group(27) != null) {
                permGen = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(28)), matcher.group(30).charAt(0));
                permGenEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(31)), matcher.group(33).charAt(0));
                permGenAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(34)),
                        matcher.group(36).charAt(0));
            }
            combinedBegin = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(37)), matcher.group(39).charAt(0));
            combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(40)), matcher.group(42).charAt(0));
            combinedAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(43)),
                    matcher.group(45).charAt(0));
            duration = JdkMath.convertMillisToMicros(matcher.group(46)).intValue();
            timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
            if (matcher.group(47) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(48)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(49)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(50)).intValue();
            }
        }
    }

    /**
     * Alternate constructor. Create logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public UnifiedOldEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_OLD.toString();
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

    public int getCombinedOccupancyInit() {
        return combinedBegin;
    }

    public int getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public int getCombinedSpace() {
        return combinedAllocation;
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
