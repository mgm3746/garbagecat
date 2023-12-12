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
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * G1_FULL_GC_PARALLEL
 * </p>
 * 
 * <p>
 * The {@link org.eclipselabs.garbagecat.domain.jdk.G1FullGcEvent} is parallel in JDK10+. See
 * <a href="https://openjdk.java.net/jeps/307">JEP 307: Parallel Full GC for G1</a>.
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Timestamp is end of gc (e.g. no `gc,start` details):
 * </p>
 * 
 * <pre>
 * [89968.517s][info][gc] GC(1344) Pause Full (G1 Evacuation Pause) 16382M->13777M(16384M) 6796.352ms
 * </pre>
 * 
 * <p>
 * 2) Timestamp is beginning of gc (e.g. preprocessed with `gc,start` details):
 * </p>
 * 
 * <pre>
 * [2021-03-13T03:37:40.051+0530][79853119ms][gc,start] GC(8646) Pause Full (G1 Evacuation Pause) Humongous regions: 13-&gt;13 Metaspace: 214096K-&gt;214096K(739328K) 8186M-&gt;8178M(8192M) 2127.343ms User=16.40s Sys=0.09s Real=2.13s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1FullGcEvent extends G1Collector
        implements UnifiedLogging, BlockingEvent, ParallelEvent, YoungCollection, OldCollection,
        PermMetaspaceCollection, CombinedData, PermMetaspaceData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.G1_COMPACTION_PAUSE.getRegex() + "|"
            + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + "|" + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|"
            + GcTrigger.DIAGNOSTIC_COMMAND.getRegex() + "|" + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|"
            + GcTrigger.HEAP_DUMP_INITIATED_GC.getRegex() + "|" + GcTrigger.SYSTEM_GC.getRegex() + ")";

    /**
     * Regular expression defining logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Pause Full \\(" + __TRIGGER
            + "\\) (Humongous regions: \\d{1,}->\\d{1,} )?(Metaspace: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE
            + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) )?" + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + TimesData.REGEX_JDK9 + "?[ ]*$";

    private static Pattern pattern = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        Matcher matcher = pattern.matcher(logLine);
        if (matcher.find()) {
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3) == null
                    && matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1) != null) {
                // Only include G1 triggers when no "Humongous regions" so there is no overlap
                // with<code>UnifiedOldEvent</code>.
                switch (GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1))) {
                case G1_COMPACTION_PAUSE:
                case G1_EVACUATION_PAUSE:
                case G1_HUMONGOUS_ALLOCATION:
                case G1_PREVENTIVE_COLLECTION:
                    match = true;
                    break;
                default:
                    break;
                }
            } else {
                match = true;
            }
        }
        return match;
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
    private Memory permGen = Memory.ZERO;

    /**
     * Space allocated to permanent generation.
     */
    private Memory permGenAllocation = Memory.ZERO;

    /**
     * Permanent generation size at end of GC event.
     */
    private Memory permGenEnd = Memory.ZERO;

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
    public UnifiedG1FullGcEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            eventTime = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 27)).intValue();
            long time = UnifiedUtil.calculateTime(matcher);
            if (!isEndstamp()) {
                timestamp = time;
            } else {
                timestamp = time - JdkMath.convertMicrosToMillis(eventTime).longValue();
            }
            trigger = GcTrigger.getTrigger(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1));
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 4) != null) {
                permGen = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 5),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 7).charAt(0)).convertTo(KILOBYTES);
                permGenEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 12),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 14).charAt(0)).convertTo(KILOBYTES);
                permGenAllocation = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 15),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 17).charAt(0)).convertTo(KILOBYTES);
            }
            combinedBegin = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 18),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 20).charAt(0)).convertTo(KILOBYTES);
            combinedEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 21),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 23).charAt(0)).convertTo(KILOBYTES);
            combinedAllocation = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 24),
                    matcher.group(UnifiedRegEx.DECORATOR_SIZE + 26).charAt(0)).convertTo(KILOBYTES);
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 28) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 29)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 30)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 31)).intValue();
            } else {
                timeUser = TimesData.NO_DATA;
                timeReal = TimesData.NO_DATA;
            }
        }
    }

    /**
     * Alternate constructor. Create detail logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public UnifiedG1FullGcEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString();
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
