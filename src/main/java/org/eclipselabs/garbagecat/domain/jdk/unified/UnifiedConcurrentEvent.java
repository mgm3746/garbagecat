/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.domain.ClassData;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.jdk.UnknownCollector;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_CONCURRENT
 * </p>
 * 
 * <p>
 * Any number of events that happen concurrently with the JVM's execution of application threads. These events are not
 * included in the GC analysis since there is no application pause time.
 * </p>
 * 
 * <h2>CMS</h2>
 * 
 * <pre>
 * [0.082s][info][gc] GC(1) Concurrent Mark
 * </pre>
 * 
 * <pre>
 * [0.083s][info][gc] GC(1) Concurrent Mark 1.428ms
 * </pre>
 * 
 * <pre>
 * [0.054s][info][gc           ] GC(1) Concurrent Mark 1.260ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <pre>
 * [0.083s][info][gc] GC(1) Concurrent Preclean
 * </pre>
 * 
 * <pre>
 * [0.083s][info][gc] GC(1) Concurrent Preclean 0.032ms
 * </pre>
 * 
 * <pre>
 * [0.054s][info][gc           ] GC(1) Concurrent Preclean 0.033ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <pre>
 * [0.084s][info][gc] GC(1) Concurrent Sweep
 * </pre>
 * 
 * <pre>
 * [0.085s][info][gc] GC(1) Concurrent Sweep 0.364ms
 * </pre>
 * 
 * <pre>
 * [0.055s][info][gc           ] GC(1) Concurrent Sweep 0.298ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <pre>
 * [0.085s][info][gc] GC(1) Concurrent Reset
 * </pre>
 * 
 * <pre>
 * [0.086s][info][gc] GC(1) Concurrent Reset 0.841ms
 * </pre>
 * 
 * <pre>
 * [0.056s][info][gc           ] GC(1) Concurrent Reset 0.693ms User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * G1:
 * </p>
 * 
 * <pre>
 * [36.400s][info][gc] GC(1330) Concurrent Cycle
 * </pre>
 * 
 * <pre>
 * [36.606s][info][gc] GC(1335) Concurrent Cycle 90.487ms
 * </pre>
 * 
 * <p>
 * Detailed logging:
 * </p>
 * 
 * <pre>
 * [16.601s][info][gc           ] GC(1033) Concurrent Cycle
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks 0.019ms
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions 0.283ms
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Mark (16.601s)
 * </pre>
 * 
 * <pre>
 * [16.050s][info][gc,marking   ] GC(969) Concurrent Mark (16.017s, 16.050s) 33.614ms
 * </pre>
 * 
 * <pre>
 * [16.601s][info][gc,marking   ] GC(1033) Concurrent Mark From Roots
 * </pre>
 * 
 * <pre>
 * [16.053s][info][gc,marking    ] GC(969) Concurrent Rebuild Remembered Sets
 * </pre>
 * 
 * <pre>
 * [16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark
 * </pre>
 * 
 * <pre>
 * [16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark 0.428ms
 * </pre>
 * 
 * <pre>
 * [2021-10-08T16:04:26.204-0400][8.937s] Concurrent String Deduplication (8.937s)
 * [2021-10-08T16:04:26.249-0400][8.983s] Concurrent String Deduplication 3428.0K-&gt;2498.6K(929.4K) avg 27.1% (8.937s, 8.983s) 45.667ms
 * </pre>
 * 
 * <pre>
 * [0.062s][info][gc          ] GC(2) Concurrent Mark Cycle
 * </pre>
 * 
 * <p>
 * Shenandoah:
 * </p>
 * 
 * <p>
 * 1) Preprocessed with combined and metaspace data:
 * </p>
 * 
 * <pre>
 * [0.256s][info][gc           ] GC(0) Concurrent cleanup 32M-&gt;18M(36M) 0.036ms Metaspace: 3867K(7168K)-&gt;3872K(7168K)
 * </pre>
 * 
 * <p>
 * ZGC:
 * </p>
 * 
 * <pre>
 * [0.129s] GC(0) Concurrent Mark Free 0.000ms
 * </pre>
 * 
 * <pre>
 * [0.130s] GC(0) Concurrent Process Non-Strong References 0.685ms
 * </pre>
 * 
 * <pre>
 * [0.131s] GC(0) Concurrent Select Relocation Set 1.444ms
 * </pre>
 * 
 * <pre>
 * [0.134s] GC(0) Concurrent Relocate 2.550ms
 * </pre>
 * 
 * <pre>
 * [2023-01-11T16:09:59.244+0000][19084.784s] GC(300) Concurrent Undo Cycle 54.191ms
 * </pre>
 * 
 * <pre>
 * [0.100s][info][gc,phases   ] GC(0) Y: Concurrent Reset Relocation Set 0.000ms
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedConcurrentEvent extends UnknownCollector
        implements UnifiedLogging, ParallelEvent, CombinedData, ClassData {
    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR
            + "( [OYy]:)? (ClassLoaderData|Concurrent (class unloading|Classes Purge|Classes Unlink|cleanup|"
            + "Cleanup for Next Mark|Clear Claimed Marks|Create Live Data|Cycle|evacuation|Mark|Mark Abort|"
            + "Mark Continue|Mark Cycle|Mark Follow|Mark Free|Mark From Roots|Mark Roots|marking|"
            + "marking \\(process weakrefs\\)|marking \\(process weakrefs\\) \\(unload classes\\)|"
            + "marking roots|marking \\(unload classes\\)|marking \\(update refs\\)||"
            + "marking \\(update refs\\) \\(process weakrefs\\)|Preclean|Preclean SoftReferences|precleaning|"
            + "Process Non-Strong|Process Non-Strong References|Rebuild Remembered Sets|"
            + "Rebuild Remembered Sets and Scrub Regions|References Enqueue|References Process|Relocate|"
            + "Relocate Remset FP|Remap Roots|[Rr]eset|Reset Relocation Set|Scan Root Regions|"
            + "Select Relocation Set|String Deduplication.*|strong roots|Sweep|thread roots|uncommit|"
            + "Undo Cycle|update references|update thread roots|weak references|weak roots)|" + "Trigger cleanups)( \\("
            + JdkRegEx.TIMESTAMP + "s(, " + JdkRegEx.TIMESTAMP + "s)?\\))?( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\))?( " + JdkRegEx.DURATION_MS + ")?" + TimesData.REGEX_JDK9 + "?( Metaspace: "
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\))?[ ]*$";

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
     * Combined size at end of GC event.
     */
    private Memory combinedOccupancyEnd;

    /**
     * Combined size at beginning of GC event.
     */
    private Memory combinedOccupancyInit;

    /**
     * Combined available space.
     */
    private Memory combinedSpace;

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long duration;

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp = 0L;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedConcurrentEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 19) != null) {
                duration = JdkMath.convertMillisToMicros(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 19)).intValue();
            }
            long endTimestamp;
            if (matcher.group(2).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                endTimestamp = Long.parseLong(matcher.group(13));
            } else if (matcher.group(2).matches(UnifiedRegEx.UPTIME)) {
                endTimestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            } else {
                if (matcher.group(14) != null) {
                    if (matcher.group(15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        endTimestamp = Long.parseLong(matcher.group(17));
                    } else {
                        endTimestamp = JdkMath.convertSecsToMillis(matcher.group(16)).longValue();
                    }
                } else {
                    // Datestamp only.
                    endTimestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
            }
            timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 8) != null) {
                combinedOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 9),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 11).charAt(0)).convertTo(KILOBYTES);
                combinedOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 12),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 14).charAt(0)).convertTo(KILOBYTES);
                combinedSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 15),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 17).charAt(0)).convertTo(KILOBYTES);
            }
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 24) != null) {
                classOccupancyInit = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 25),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 27).charAt(0)).convertTo(KILOBYTES);
                classOccupancyEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 31),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 33).charAt(0)).convertTo(KILOBYTES);
                classSpace = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 34),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 36).charAt(0)).convertTo(KILOBYTES);
            }
        }
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

    public Memory getCombinedOccupancyEnd() {
        return combinedOccupancyEnd;
    }

    public Memory getCombinedOccupancyInit() {
        return combinedOccupancyInit;
    }

    public Memory getCombinedSpace() {
        return combinedSpace;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString();
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEndstamp() {
        boolean isEndStamp = false;
        return isEndStamp;
    }
}
