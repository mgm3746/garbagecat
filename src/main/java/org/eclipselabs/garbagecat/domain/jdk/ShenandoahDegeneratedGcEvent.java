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
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

/**
 * <p>
 * SHENANDOAH_DEGENERATED_GC
 * </p>
 * 
 * <p>
 * When allocation failure occurs, degenerated GC continues the in-progress "concurrent" cycle under stop-the-world.[1].
 * 
 * [1]<a href="https://wiki.openjdk.java.net/display/shenandoah/Main">Shenandoah GC</a>
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Non-Unified Mark:
 * </p>
 * 
 * <pre>
 * 2020-08-18T14:05:42.515+0000: 854868.165: [Pause Degenerated GC (Mark) 93058M-&gt;29873M(98304M), 1285.045 ms]
 * </pre>
 * 
 * <p>
 * 2) Unified Mark:
 * </p>
 * 
 * <pre>
 * [52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M-&gt;30M(64M) 53.697ms
 * </pre>
 * 
 * <p>
 * 3) Preprocessed Mark:
 * </p>
 * 
 * <pre>
 * 2021-03-23T20:57:33.301+0000: 120826.585: [Pause Degenerated GC (Mark) 1572M-&gt;1136M(1690M), 1649.410 ms], [Metaspace: 282194K-&gt;282194K(1314816K)]
 * </pre>
 * 
 * <p>
 * 4) Preprocessed Update Refs JDK11:
 * </p>
 * 
 * <pre>
 * [2023-02-22T06:35:41.594+0000][2003][gc            ] GC(329) Pause Degenerated GC (Update Refs) 5855M-&gt;1809M(6144M) 22.221ms Metaspace: 125660K(138564K)-&gt;125660K(138564K)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahDegeneratedGcEvent extends ShenandoahCollector
        implements BlockingEvent, ParallelEvent, CombinedData, PermMetaspaceData {

    private static final Pattern pattern = Pattern.compile(ShenandoahDegeneratedGcEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DECORATOR + "|" + UnifiedRegEx.DECORATOR
            + ") [\\[]{0,1}Pause Degenerated GC \\((Evacuation|Mark|Outside of Cycle|Update Refs)\\) " + JdkRegEx.SIZE
            + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)[,]{0,1} " + JdkRegEx.DURATION_MS
            + "[]]{0,1}([,]{0,1} [\\[]{0,1}" + "Metaspace: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)[]]{0,1})?[ ]*$";

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

    /**
     * Combined size at beginning of GC event.
     */
    private Memory combined;

    /**
     * Combined available space.
     */
    private Memory combinedAvailable;

    /**
     * Combined size at end of GC event.
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
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ShenandoahDegeneratedGcEvent(String logEntry) {
        this.logEntry = logEntry;
        if (logEntry.matches(REGEX)) {
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                duration = JdkMath
                        .convertMillisToMicros(matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 12))
                        .intValue();
                if (matcher.group(1).matches(UnifiedRegEx.DECORATOR)) {
                    long endTimestamp;
                    if (matcher.group(15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        endTimestamp = Long.parseLong(matcher.group(26));
                    } else if (matcher.group(15).matches(UnifiedRegEx.UPTIME)) {
                        endTimestamp = JdkMath.convertSecsToMillis(matcher.group(JdkUtil.DECORATOR_SIZE + 12))
                                .longValue();
                    } else {
                        if (matcher.group(JdkUtil.DECORATOR_SIZE + 14) != null) {
                            if (matcher.group(JdkUtil.DECORATOR_SIZE + 15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                                endTimestamp = Long.parseLong(matcher.group(JdkUtil.DECORATOR_SIZE + 17));
                            } else {
                                endTimestamp = JdkMath.convertSecsToMillis(matcher.group(JdkUtil.DECORATOR_SIZE + 16))
                                        .longValue();
                            }
                        } else {
                            // Datestamp only.
                            endTimestamp = JdkUtil.convertDatestampToMillis(matcher.group(15));
                        }
                    }
                    timestamp = endTimestamp - JdkMath.convertMicrosToMillis(duration).longValue();
                } else {
                    // JDK8
                    if (matcher.group(14) != null && matcher.group(14).matches(JdkRegEx.TIMESTAMP)) {
                        timestamp = JdkMath.convertSecsToMillis(matcher.group(14)).longValue();
                    } else if (matcher.group(2).matches(JdkRegEx.TIMESTAMP)) {
                        timestamp = JdkMath.convertSecsToMillis(matcher.group(2)).longValue();
                    } else {
                        // Datestamp only.
                        timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                    }
                }
                combined = memory(matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 3),
                        matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 5).charAt(0))
                                .convertTo(KILOBYTES);
                combinedEnd = memory(matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 6),
                        matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 8).charAt(0))
                                .convertTo(KILOBYTES);
                combinedAvailable = memory(matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 9),
                        matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 11).charAt(0))
                                .convertTo(KILOBYTES);
                if (matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 13) != null) {
                    permGen = memory(matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 14),
                            matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 16).charAt(0))
                                    .convertTo(KILOBYTES);
                    permGenEnd = memory(matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 21),
                            matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 23).charAt(0))
                                    .convertTo(KILOBYTES);
                    permGenAllocation = memory(matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 24),
                            matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedUtil.DECORATOR_SIZE + 26).charAt(0))
                                    .convertTo(KILOBYTES);
                }
            }
        }
    }

    /**
     * Alternate constructor. Create event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public ShenandoahDegeneratedGcEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC.toString();
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

    public long getTimestamp() {
        return timestamp;
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
