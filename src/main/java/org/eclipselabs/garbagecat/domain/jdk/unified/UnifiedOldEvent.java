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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

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
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
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
 * <h2>Example Logging</h2>
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
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.ERGONOMICS.getRegex() + "|" + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|"
            + GcTrigger.LAST_DITCH_COLLECTION.getRegex() + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|"
            + GcTrigger.SYSTEM_GC.getRegex() + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Pause Full \\(" + __TRIGGER
            + "\\)( Metaspace: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\))? "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS
            + TimesData.REGEX_JDK9 + "?[ ]*$";

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
     * Combined young + old generation allocation.
     */
    private Memory combinedAllocation;

    /**
     * Combined young + old generation size at beginning of GC event.
     */
    private Memory combinedBegin;

    /**
     * Combined young + old generation size at end of GC event.
     */
    private Memory combinedEnd;

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long eventTime;

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Permanent generation size at beginning of GC event.
     */
    private Memory permGen;

    /**
     * Space allocated to permanent generation.
     */
    private Memory permGenAllocation;
    /**
     * Permanent generation size at end of GC event.
     */
    private Memory permGenEnd;
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
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedOldEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            eventTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 22)).intValue();
            long time = UnifiedUtil.calculateTime(matcher);
            if (!isEndstamp()) {
                timestamp = time;
            } else {
                timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
            }
            trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3) != null) {
                permGen = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 6).charAt(0)).convertTo(KILOBYTES);
                permGenEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 7),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 9).charAt(0)).convertTo(KILOBYTES);
                permGenAllocation = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 10),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 12).charAt(0)).convertTo(KILOBYTES);
            }
            combinedBegin = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 13),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 15).charAt(0)).convertTo(KILOBYTES);
            combinedEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 16),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 18).charAt(0)).convertTo(KILOBYTES);
            combinedAllocation = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 19),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 21).charAt(0)).convertTo(KILOBYTES);
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 23) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 24)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 25)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 26)).intValue();
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
        this.eventTime = duration;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combinedBegin;
    }

    public Memory getCombinedSpace() {
        return combinedAllocation;
    }

    public long getDurationMicros() {
        return eventTime;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_OLD.toString();
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

    public Memory getPermOccupancyEnd() {
        return permGenEnd;
    }

    public Memory getPermOccupancyInit() {
        return permGen;
    }

    public Memory getPermSpace() {
        return permGenAllocation;
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
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

    public boolean isEndstamp() {
        // default assumes gc,start not logged (e.g. not preprocessed)
        boolean isEndStamp = true;
        isEndStamp = !logEntry.matches(UnifiedRegEx.TAG_GC_START);
        return isEndStamp;
    }

    protected void setPermOccupancyEnd(Memory permGenEnd) {
        this.permGenEnd = permGenEnd;
    }

    protected void setPermOccupancyInit(Memory permGen) {
        this.permGen = permGen;
    }

    protected void setPermSpace(Memory permGenAllocation) {
        this.permGenAllocation = permGenAllocation;
    }
}
