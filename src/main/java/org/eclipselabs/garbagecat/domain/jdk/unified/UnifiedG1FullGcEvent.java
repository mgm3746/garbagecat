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
import static org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil.DECORATOR_SIZE;

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
import org.eclipselabs.garbagecat.util.jdk.GcTrigger.Type;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

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
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * [2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause) Humongous regions: 13-&gt;13 Metaspace: 214096K-&gt;214096K(739328K) 8186M-&gt;8178M(8192M) 2127.343ms User=16.40s Sys=0.09s Real=2.13s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedG1FullGcEvent extends G1Collector
        implements UnifiedLogging, BlockingEvent, ParallelEvent, YoungCollection, OldCollection,
        PermMetaspaceCollection, CombinedData, PermMetaspaceData, TriggerData, TimesData {

    /**
     * Regular expression defining preprocessed logging.
     */
    private static final String REGEX_PREPROCESSED = "^" + UnifiedRegEx.DECORATOR + " Pause Full \\("
            + UnifiedG1FullGcEvent.TRIGGER + "\\) Humongous regions: \\d{1,}->\\d{1,} Metaspace: " + JdkRegEx.SIZE
            + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + TimesData.REGEX_JDK9 + "[ ]*$";

    private static final Pattern REGEX_PREPROCESSED_PATTERN = Pattern.compile(REGEX_PREPROCESSED);

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + GcTrigger.G1_EVACUATION_PAUSE + "|" + GcTrigger.G1_HUMONGOUS_ALLOCATION
            + "|" + GcTrigger.DIAGNOSTIC_COMMAND + "|" + GcTrigger.GCLOCKER_INITIATED_GC + "|"
            + GcTrigger.HEAP_DUMP_INITIATED_GC + "|" + GcTrigger.SYSTEM_GC + ")";

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return REGEX_PREPROCESSED_PATTERN.matcher(logLine).matches();
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
    private long duration;

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
    private String trigger;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedG1FullGcEvent(String logEntry) {
        this.logEntry = logEntry;

        Matcher matcher = REGEX_PREPROCESSED_PATTERN.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(1).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                timestamp = Long.parseLong(matcher.group(12));
            } else if (matcher.group(1).matches(UnifiedRegEx.UPTIME)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(11)).longValue();
            } else {
                if (matcher.group(14) != null) {
                    if (matcher.group(14).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        timestamp = Long.parseLong(matcher.group(16));
                    } else {
                        timestamp = JdkMath.convertSecsToMillis(matcher.group(15)).longValue();
                    }
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
            }
            trigger = matcher.group(DECORATOR_SIZE + 1);
            permGen = memory(matcher.group(DECORATOR_SIZE + 3), matcher.group(DECORATOR_SIZE + 5).charAt(0))
                    .convertTo(KILOBYTES);
            permGenEnd = memory(matcher.group(DECORATOR_SIZE + 10), matcher.group(DECORATOR_SIZE + 12).charAt(0))
                    .convertTo(KILOBYTES);
            permGenAllocation = memory(matcher.group(DECORATOR_SIZE + 13), matcher.group(DECORATOR_SIZE + 15).charAt(0))
                    .convertTo(KILOBYTES);
            combinedBegin = memory(matcher.group(DECORATOR_SIZE + 16), matcher.group(DECORATOR_SIZE + 18).charAt(0))
                    .convertTo(KILOBYTES);
            combinedEnd = memory(matcher.group(DECORATOR_SIZE + 19), matcher.group(DECORATOR_SIZE + 21).charAt(0))
                    .convertTo(KILOBYTES);
            combinedAllocation = memory(matcher.group(DECORATOR_SIZE + 22),
                    matcher.group(DECORATOR_SIZE + 24).charAt(0)).convertTo(KILOBYTES);
            duration = JdkMath.convertMillisToMicros(matcher.group(DECORATOR_SIZE + 25)).intValue();
            if (matcher.group(DECORATOR_SIZE + 26) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 27)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 28)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(DECORATOR_SIZE + 29)).intValue();
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
        this.duration = duration;
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

    public long getDuration() {
        return duration;
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

    public Type getTrigger() {
        return GcTrigger.getTrigger(trigger);
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
