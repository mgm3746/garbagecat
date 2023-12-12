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

import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * SHENANDOAH_CONCURRENT
 * 
 * TODO: Move to general UnifiedConcurrent event? Some of these are shared w/ G1.
 * </p>
 * 
 * <p>
 * Any number of events that happen concurrently with the JVM's execution of application threads. These events are not
 * included in the GC analysis since there is no application pause time; however, they are used to determine max heap
 * and metaspace size and occupancy.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard unified logging:
 * </p>
 * 
 * <pre>
 * [0.437s][info][gc] GC(0) Concurrent reset 15M-&gt;16M(64M) 4.701ms
 * </pre>
 * 
 * <pre>
 * [0.528s][info][gc] GC(1) Concurrent marking 16M-&gt;17M(64M) 7.045ms
 * </pre>
 * 
 * <pre>
 * [0.454s][info][gc] GC(0) Concurrent marking (process weakrefs) 17M-&gt;19M(64M) 15.264ms
 * </pre>
 * 
 * <pre>
 * [0.455s][info][gc] GC(0) Concurrent precleaning 19M-&gt;19M(64M) 0.202ms
 * </pre>
 * 
 * <pre>
 * [0.465s][info][gc] GC(0) Concurrent evacuation 17M-&gt;19M(64M) 6.528ms
 * </pre>
 * 
 * <pre>
 * [0.470s][info][gc] GC(0) Concurrent update references 19M-&gt;19M(64M) 4.708ms
 * </pre>
 * 
 * <pre>
 * [0.472s][info][gc] GC(0) Concurrent cleanup 18M-&gt;15M(64M) 0.036ms
 * </pre>
 * 
 * <p>
 * 2) With <code>-Xlog:gc*:file=&lt;file&gt;:time,uptimemillis</code>.
 * </p>
 * 
 * <pre>
 * [2019-02-05T14:47:34.156-0200][3068ms] GC(0) Concurrent reset
 * </pre>
 * 
 * <p>
 * 3) JDK8:
 * </p>
 * 
 * <pre>
 * 2020-03-10T08:03:29.311-0400: 0.373: [Concurrent reset 16991K-&gt;17152K(17408K), 0.435 ms]
 * </pre>
 * 
 * <pre>
 * 2020-08-13T16:38:29.318+0000: 432034.969: [Concurrent reset, 26.427 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.365-0400: 0.427: [Concurrent marking 16498K-&gt;17020K(21248K), 2.462 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.315-0400: 0.377: [Concurrent marking (process weakrefs) 17759K-&gt;19325K(19456K), 6.892 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.322-0400: 0.384: [Concurrent precleaning 19325K-&gt;19357K(19456K), 0.092 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.427-0400: 0.489: [Concurrent evacuation 9712K-&gt;9862K(23296K), 0.144 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.427-0400: 0.489: [Concurrent update references 9862K-&gt;12443K(23296K), 3.463 ms]
 * </pre>
 * 
 * <pre>
 * 2020-03-10T08:03:29.431-0400: 0.493: [Concurrent cleanup 12501K-&gt;8434K(23296K), 0.034 ms]
 * </pre>
 * 
 * <p>
 * 4) JDK8 preprocessed with Metaspace block:
 * </p>
 * 
 * <pre>
 * 2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K-&gt;4701K(37888K), 0.048 ms], [Metaspace: 6477K-&gt;6481K(1056768K)]
 * </pre>
 * 
 * <p>
 * 5) JDK11:
 * </p>
 * 
 * <pre>
 * [5.601s][info][gc           ] GC(99) Concurrent marking (unload classes) 7.346ms
 * </pre>
 * 
 * <p>
 * 6) JDK11 preprocessed with Metaspace block:
 * </p>
 * 
 * <pre>
 * [0.266s][info][gc           ] GC(0) Concurrent cleanup 34M-&gt;20M(36M) 0.028ms Metaspace: 3692K(7168K)-&gt;3714K(7168K)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahConcurrentEvent extends ShenandoahCollector
        implements LogEvent, ParallelEvent, CombinedData, PermMetaspaceData {

    private static Pattern pattern = Pattern.compile(ShenandoahConcurrentEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DECORATOR + "|" + UnifiedRegEx.DECORATOR
            + ") [\\[]{0,1}Concurrent (class unloading|cleanup|evacuation|"
            + "marking( \\((process weakrefs|unload classes|update refs)\\)| roots)?"
            + "( \\((process weakrefs|unload classes)\\))?|precleaning|reset|uncommit|uncommit, start|"
            + "(update|weak) references|(strong|thread|update thread|weak) roots)(( " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\))?[,]{0,1} " + JdkRegEx.DURATION_MS
            + ")?[\\]]{0,1}([,]{0,1} [\\[]{0,1}Metaspace: " + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)[\\]]{0,1})?[ ]*$";

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
    public ShenandoahConcurrentEvent(String logEntry) {
        this.logEntry = logEntry;
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                int duration = 0;
                if (matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedRegEx.DECORATOR_SIZE + 20) != null) {
                    duration = JdkMath
                            .convertMillisToMicros(
                                    matcher.group(JdkUtil.DECORATOR_SIZE + UnifiedRegEx.DECORATOR_SIZE + 20))
                            .intValue();
                }
                if (matcher.group(1).matches(UnifiedRegEx.DECORATOR)) {
                    long endTimestamp;
                    if (matcher.group(JdkUtil.DECORATOR_SIZE + 3).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        endTimestamp = Long.parseLong(matcher.group(JdkUtil.DECORATOR_SIZE + 13));
                    } else if (matcher.group(JdkUtil.DECORATOR_SIZE + 3).matches(UnifiedRegEx.UPTIME)) {
                        endTimestamp = JdkMath.convertSecsToMillis(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2))
                                .longValue();
                    } else {
                        if (matcher.group(JdkUtil.DECORATOR_SIZE + 15) != null) {
                            if (matcher.group(JdkUtil.DECORATOR_SIZE + 16).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                                endTimestamp = Long.parseLong(matcher.group(JdkUtil.DECORATOR_SIZE + 18));
                            } else {
                                endTimestamp = JdkMath.convertSecsToMillis(matcher.group(JdkUtil.DECORATOR_SIZE + 17))
                                        .longValue();
                            }
                        } else {
                            // Datestamp only.
                            endTimestamp = JdkUtil.convertDatestampToMillis(matcher.group(JdkUtil.DECORATOR_SIZE + 3));
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
                if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 23) != null) {
                    combined = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 24),
                            matcher.group(UnifiedRegEx.DECORATOR_SIZE + 26).charAt(0)).convertTo(KILOBYTES);
                    combinedEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 27),
                            matcher.group(UnifiedRegEx.DECORATOR_SIZE + 29).charAt(0)).convertTo(KILOBYTES);
                    combinedAvailable = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 30),
                            matcher.group(UnifiedRegEx.DECORATOR_SIZE + 32).charAt(0)).convertTo(KILOBYTES);
                    if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 34) != null) {
                        permGen = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 35),
                                matcher.group(UnifiedRegEx.DECORATOR_SIZE + 37).charAt(0)).convertTo(KILOBYTES);
                        permGenEnd = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 42),
                                matcher.group(UnifiedRegEx.DECORATOR_SIZE + 44).charAt(0)).convertTo(KILOBYTES);
                        permGenAllocation = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 45),
                                matcher.group(UnifiedRegEx.DECORATOR_SIZE + 47).charAt(0)).convertTo(KILOBYTES);
                    }
                }

            }
        }
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

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString();
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
