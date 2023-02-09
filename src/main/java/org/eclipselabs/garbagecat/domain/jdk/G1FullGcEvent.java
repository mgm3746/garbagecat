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

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.PermMetaspaceCollection;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.SerialCollection;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_FULL_GC
 * </p>
 * 
 * <p>
 * G1 collector Full GC event. A serial (single-threaded) collector, which means it will take a very long time to
 * collect a large heap. If the G1 collector is running optimally, there will not be any G1 Full GC collections. G1 Full
 * GCs happen when the PermGen/Metaspace fills up, when the old space fills up with humongous objects (allocated
 * directly in the old space), or when there are more allocations than the G1 can concurrently collect.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 5060.152: [Full GC (System.gc()) 2270M-&gt;2038M(3398M), 5.8360430 secs]
 * </pre>
 * 
 * <p>
 * 2) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [Full GC (System.gc()) 2270M-&gt;2038M(3398M), 5.8360430 secs]
 * </pre>
 * 
 * <p>
 * 3) After {@link org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction}:
 * </p>
 * 
 * <pre>
 * 2.847: [GC pause (G1 Evacuation Pause) (young), 0.0414530 secs] [Eden: 112.0M(112.0M)-&gt;0.0B(112.0M) Survivors: 16.0M-&gt;16.0M Heap: 136.9M(30.0G)-&gt;70.9M(30.0G)]
 * </pre>
 *
 * <p>
 * 4) After {@link org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction} with
 * {@link org.eclipselabs.garbagecat.domain.jdk.ClassHistogramEvent} output.
 * </p>
 *
 * <pre>
 * 2016-10-31T14:09:15.030-0700: 49689.217: [Full GC2016-10-31T14:09:15.030-0700: 49689.217: [Class Histogram (before full gc):, 8.8690440 secs]11G-&gt;2270M(12G), 19.8185620 secs][Eden: 0.0B(612.0M)-&gt;0.0B(7372.0M) Survivors: 0.0B-&gt;0.0B Heap: 11.1G(12.0G)-&gt;2270.1M(12.0G)], [Perm: 730823K-&gt;730823K(2097152K)]2016-10-31T14:09:34.848-0700: 49709.036: [Class Histogram (after full gc):, 2.4232900 secs] [Times: user=29.91 sys=0.08, real=22.24 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1FullGcEvent extends G1Collector implements BlockingEvent, YoungCollection, OldCollection,
        PermMetaspaceCollection, CombinedData, PermMetaspaceData, TriggerData, SerialCollection, TimesData {

    /**
     * Regular expression standard format.
     */
    private static final String REGEX = "^" + JdkRegEx.DECORATOR + " \\[Full GC (\\(("
            + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_SYSTEM_GC + ")\\))?[ ]{0,2}" + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern REGEX_PATTERN = Pattern.compile(REGEX);

    /**
     * Regular expression preprocessed with G1 details.
     */
    private static final String REGEX_PREPROCESSED = "^" + JdkRegEx.DECORATOR + " \\[Full GC[ ]{0,1}(\\(("
            + JdkRegEx.TRIGGER_SYSTEM_GC + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|" + JdkRegEx.TRIGGER_JVM_TI_FORCED_GAREBAGE_COLLECTION + "|"
            + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|" + JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC + ")\\)[ ]{0,2})?(" + ClassHistogramEvent.REGEX_PREPROCESSED
            + ")? " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION
            + "\\]\\[Eden: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\) Survivors: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\](, \\[(Perm|Metaspace): "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\])?("
            + ClassHistogramEvent.REGEX_PREPROCESSED + ")?" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern REGEX_PREPROCESSED_PATTERN = Pattern.compile(REGEX_PREPROCESSED);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return REGEX_PATTERN.matcher(logLine).matches() || REGEX_PREPROCESSED_PATTERN.matcher(logLine).matches();
    }

    /**
     * Combined size at beginning of GC event.
     */
    private Memory combined = Memory.ZERO;

    /**
     * Combined available space.
     */
    private Memory combinedAvailable = Memory.ZERO;

    /**
     * Combined size at end of GC event.
     */
    private Memory combinedEnd = Memory.ZERO;

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
    private String trigger;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public G1FullGcEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher;
        if ((matcher = REGEX_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
                if (matcher.group(15) != null) {
                    trigger = matcher.group(15);
                }
                combined = memory(matcher.group(17), matcher.group(19).charAt(0)).convertTo(KILOBYTES);
                combinedEnd = memory(matcher.group(20), matcher.group(22).charAt(0)).convertTo(KILOBYTES);
                combinedAvailable = memory(matcher.group(23), matcher.group(25).charAt(0)).convertTo(KILOBYTES);
                duration = JdkMath.convertSecsToMicros(matcher.group(26)).intValue();
                if (matcher.group(29) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(30)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(31)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(32)).intValue();
                }
            }
        } else if ((matcher = REGEX_PREPROCESSED_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
                if (matcher.group(15) != null) {
                    trigger = matcher.group(15);
                } else if (matcher.group(17) != null
                        && matcher.group(17).matches(ClassHistogramEvent.REGEX_PREPROCESSED)) {
                    trigger = JdkRegEx.TRIGGER_CLASS_HISTOGRAM;
                }
            }
            combined = JdkMath.convertSizeToKilobytes(matcher.group(67), matcher.group(69).charAt(0));
            combinedEnd = JdkMath.convertSizeToKilobytes(matcher.group(73), matcher.group(75).charAt(0));
            combinedAvailable = JdkMath.convertSizeToKilobytes(matcher.group(76), matcher.group(78).charAt(0));
            duration = JdkMath.convertSecsToMicros(matcher.group(46)).intValue();
            if (matcher.group(79) != null) {
                permGen = memory(matcher.group(81), matcher.group(83).charAt(0)).convertTo(KILOBYTES);
                permGenEnd = memory(matcher.group(84), matcher.group(86).charAt(0)).convertTo(KILOBYTES);
                permGenAllocation = memory(matcher.group(87), matcher.group(89).charAt(0)).convertTo(KILOBYTES);
            }
            if (matcher.group(110) != null) {
                timeUser = JdkMath.convertSecsToCentis(matcher.group(111)).intValue();
                timeSys = JdkMath.convertSecsToCentis(matcher.group(112)).intValue();
                timeReal = JdkMath.convertSecsToCentis(matcher.group(113)).intValue();
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
    public G1FullGcEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public Memory getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combined;
    }

    public Memory getCombinedSpace() {
        return combinedAvailable;
    }

    public long getDuration() {
        return duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString();
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

    public String getTrigger() {
        return trigger;
    }

    protected void setDuration(int duration) {
        this.duration = duration;
    }

    protected void setLogEntry(String logEntry) {
        this.logEntry = logEntry;
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

    protected void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
