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
package org.eclipselabs.garbagecat.domain.jdk;

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
 * <h3>Example Logging</h3>
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
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1FullGCEvent extends G1Collector implements BlockingEvent, YoungCollection, OldCollection,
        PermMetaspaceCollection, CombinedData, PermMetaspaceData, TriggerData, SerialCollection {

    /**
     * Regular expression standard format.
     */
    private static final String REGEX = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[Full GC (\\(("
            + JdkRegEx.TRIGGER_SYSTEM_GC + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + ")\\))?[ ]{0,2}" + JdkRegEx.SIZE
            + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX
            + "?[ ]*$";
    /**
     * Regular expression preprocessed with G1 details.
     */
    private static final String REGEX_PREPROCESSED = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[Full GC[ ]{0,1}(\\((" + JdkRegEx.TRIGGER_SYSTEM_GC + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD
            + "|" + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|" + JdkRegEx.TRIGGER_JVM_TI_FORCED_GAREBAGE_COLLECTION
            + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|" + JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC + ")\\)[ ]{0,2}|" + ClassHistogramEvent.REGEX_PREPROCESSED + ")?"
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION
            + "\\]\\[Eden: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\) Survivors: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\](, \\[(Perm|Metaspace): "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\])?" + TimesData.REGEX + "?[ ]*$";

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
     * Combined size (kilobytes) at beginning of GC event.
     */
    private int combined;

    /**
     * Combined size (kilobytes) at end of GC event.
     */
    private int combinedEnd;

    /**
     * Combined available space (kilobytes).
     */
    private int combinedAvailable;

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
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public G1FullGCEvent(String logEntry) {
        this.logEntry = logEntry;
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                if (matcher.group(14) != null) {
                    trigger = matcher.group(14);
                }
                combined = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(16)), matcher.group(18).charAt(0));
                combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(19)), matcher.group(21).charAt(0));
                combinedAvailable = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(22)),
                        matcher.group(24).charAt(0));
                duration = JdkMath.convertSecsToMicros(matcher.group(25)).intValue();
            }
        } else if (logEntry.matches(REGEX_PREPROCESSED)) {
            Pattern pattern = Pattern.compile(REGEX_PREPROCESSED);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                if (matcher.group(14) != null) {
                    trigger = matcher.group(14);
                } else if (matcher.group(13) != null) {
                    trigger = JdkRegEx.TRIGGER_CLASS_HISTOGRAM;
                }
                combined = JdkMath.convertSizeToKilobytes(matcher.group(65), matcher.group(67).charAt(0));
                combinedEnd = JdkMath.convertSizeToKilobytes(matcher.group(71), matcher.group(73).charAt(0));
                combinedAvailable = JdkMath.convertSizeToKilobytes(matcher.group(74), matcher.group(76).charAt(0));
                duration = JdkMath.convertSecsToMicros(matcher.group(44)).intValue();
                if (matcher.group(77) != null) {
                    permGen = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(79)), matcher.group(81).charAt(0));
                    permGenEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(82)),
                            matcher.group(84).charAt(0));
                    permGenAllocation = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(85)),
                            matcher.group(87).charAt(0));
                }
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
    public G1FullGCEvent(String logEntry, long timestamp, int duration) {
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

    public int getCombinedOccupancyInit() {
        return combined;
    }

    public int getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public int getCombinedSpace() {
        return combinedAvailable;
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
        return JdkUtil.LogEventType.G1_FULL_GC.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX) || logLine.matches(REGEX_PREPROCESSED);
    }
}
