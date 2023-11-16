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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_HEADER
 * </p>
 * 
 * <p>
 * Initial log lines with environment information.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Serial:
 * </p>
 * 
 * <pre>
 * [0.013s][info][gc,init] CPUs: 12 total, 12 available
 * [0.013s][info][gc,init] Memory: 31907M
 * [0.013s][info][gc,init] Large Page Support: Disabled
 * [0.013s][info][gc,init] NUMA Support: Disabled
 * [0.013s][info][gc,init] Compressed Oops: Enabled (32-bit)
 * [0.013s][info][gc,init] Heap Min Capacity: 2M
 * [0.013s][info][gc,init] Heap Initial Capacity: 2M
 * [0.013s][info][gc,init] Heap Max Capacity: 64M
 * [0.013s][info][gc,init] Pre-touch: Disabled
 * [0.013s][info][gc,metaspace] CDS archive(s) mapped at: [0x0000000800000000-0x0000000800be2000-0x0000000800be2000), size 12460032, SharedBaseAddress: 0x0000000800000000, ArchiveRelocationMode: 0.
 * [0.013s][info][gc,metaspace] Compressed class space mapped at: 0x0000000800c00000-0x0000000840c00000, reserved size: 1073741824
 * [0.013s][info][gc,metaspace] Narrow klass base: 0x0000000800000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * </pre>
 * 
 * <p>
 * 2) Parallel:
 * </p>
 * 
 * <pre>
 * [0.013s][info][gc,init] CPUs: 12 total, 12 available
 * [0.013s][info][gc,init] Memory: 31907M
 * [0.013s][info][gc,init] Large Page Support: Disabled
 * [0.013s][info][gc,init] NUMA Support: Disabled
 * [0.013s][info][gc,init] Compressed Oops: Enabled (32-bit)
 * [0.013s][info][gc,init] Alignments: Space 512K, Generation 512K, Heap 2M
 * [0.013s][info][gc,init] Heap Min Capacity: 2M
 * [0.013s][info][gc,init] Heap Initial Capacity: 2M
 * [0.013s][info][gc,init] Heap Max Capacity: 64M
 * [0.013s][info][gc,init] Pre-touch: Disabled
 * [0.013s][info][gc,init] Parallel Workers: 10
 * [0.013s][info][gc,metaspace] CDS archive(s) mapped at: [0x0000000800000000-0x0000000800be2000-0x0000000800be2000), size 12460032, SharedBaseAddress: 0x0000000800000000, ArchiveRelocationMode: 0.
 * [0.013s][info][gc,metaspace] Compressed class space mapped at: 0x0000000800c00000-0x0000000840c00000, reserved size: 1073741824
 * [0.013s][info][gc,metaspace] Narrow klass base: 0x0000000800000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * </pre>
 * 
 * <p>
 * 3) G1:
 * </p>
 * 
 * <pre>
 * [0.014s][info][gc,init] CPUs: 12 total, 12 available
 * [0.014s][info][gc,init] Memory: 31907M
 * [0.014s][info][gc,init] Large Page Support: Disabled
 * [0.014s][info][gc,init] NUMA Support: Disabled
 * [0.014s][info][gc,init] Compressed Oops: Enabled (32-bit)
 * [0.014s][info][gc,init] Heap Region Size: 1M
 * [0.014s][info][gc,init] Heap Min Capacity: 2M
 * [0.014s][info][gc,init] Heap Initial Capacity: 2M
 * [0.014s][info][gc,init] Heap Max Capacity: 96M
 * [0.014s][info][gc,init] Pre-touch: Disabled
 * [0.014s][info][gc,init] Parallel Workers: 10
 * [0.014s][info][gc,init] Concurrent Workers: 3
 * [0.014s][info][gc,init] Concurrent Refinement Workers: 10
 * [0.014s][info][gc,init] Periodic GC: Disabled
 * [0.014s][info][gc,metaspace] CDS archive(s) mapped at: [0x0000000800000000-0x0000000800be2000-0x0000000800be2000), size 12460032, SharedBaseAddress: 0x0000000800000000, ArchiveRelocationMode: 0.
 * [0.014s][info][gc,metaspace] Compressed class space mapped at: 0x0000000800c00000-0x0000000840c00000, reserved size: 1073741824
 * [0.014s][info][gc,metaspace] Narrow klass base: 0x0000000800000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * </pre>
 * 
 * <p>
 * 4) Shenandoah:
 * </p>
 * 
 * <pre>
 * [0.014s][info][gc,ergo] Pacer for Idle. Initial: 1966K, Alloc Tax Rate: 1.0x
 * [0.014s][info][gc,init] CPUs: 12 total, 12 available
 * [0.014s][info][gc,init] Memory: 31907M
 * [0.014s][info][gc,init] Large Page Support: Disabled
 * [0.014s][info][gc,init] NUMA Support: Disabled
 * [0.014s][info][gc,init] Compressed Oops: Enabled (32-bit)
 * [0.014s][info][gc,init] Heap Min Capacity: 32M
 * [0.014s][info][gc,init] Heap Initial Capacity: 32M
 * [0.014s][info][gc,init] Heap Max Capacity: 96M
 * [0.014s][info][gc,init] Pre-touch: Disabled
 * [0.014s][info][gc,init] Mode: Snapshot-At-The-Beginning (SATB)
 * [0.014s][info][gc,init] Heuristics: Adaptive
 * [0.014s][info][gc,init] Heap Region Count: 384
 * [0.014s][info][gc,init] Heap Region Size: 256K
 * [0.014s][info][gc,init] TLAB Size Max: 256K
 * [0.014s][info][gc,init] Humongous Object Threshold: 256K
 * [0.014s][info][gc,init] Parallel Workers: 6
 * [0.014s][info][gc,init] Concurrent Workers: 3
 * </pre>
 * 
 * <pre>
 * [2023-02-22T12:31:30.322+0000][2243][gc] Min heap equals to max heap, disabling ShenandoahUncommit
 * [2023-02-22T12:31:30.329+0000][2243][gc,init] Regions: 3072 x 2048K
 * [2023-02-22T12:31:30.329+0000][2243][gc,init] Humongous object threshold: 2048K
 * [2023-02-22T12:31:30.329+0000][2243][gc,init] Max TLAB size: 2048K
 * [2023-02-22T12:31:30.330+0000][2243][gc,init] GC threads: 2 parallel, 1 concurrent
 * [2023-02-22T12:31:30.330+0000][2243][gc     ] Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent
 * [2023-02-22T12:31:30.330+0000][2243][gc     ] Heuristics ergonomically sets -XX:+ShenandoahImplicitGCInvokesConcurrent
 * [2023-02-22T12:31:30.330+0000][2243][gc,init] Shenandoah GC mode: Snapshot-At-The-Beginning (SATB)
 * [2023-02-22T12:31:30.330+0000][2243][gc,init] Shenandoah heuristics: Adaptive
 * [2023-02-22T12:31:32.306+0000][2243][gc,ergo] Pacer for Idle. Initial: 122M, Alloc Tax Rate: 1.0x
 * [2023-02-22T12:31:32.306+0000][2243][gc,init] Initialize Shenandoah heap: 6144M initial, 6144M min, 6144M max
 * [2023-02-22T12:31:32.306+0000][2243][gc,init] Safepointing mechanism: global-page poll
 * </pre>
 * 
 * <p>
 * 5) Z:
 * </p>
 * 
 * <pre>
 * [0.014s][info][gc,init] Initializing The Z Garbage Collector
 * [0.014s][info][gc,init] NUMA Support: Disabled
 * [0.014s][info][gc,init] CPUs: 12 total, 12 available
 * [0.014s][info][gc,init] Memory: 31907M 
 * [0.014s][info][gc,init] Large Page Support: Disabled
 * [0.014s][info][gc,init] GC Workers: 1 (dynamic)
 * [0.014s][info][gc,init] Address Space Type: Contiguous/Unrestricted/Complete
 * [0.014s][info][gc,init] Address Space Size: 1536M x 3 = 4608M
 * [0.014s][info][gc,init] Heap Backing File: /memfd:java_heap
 * [0.014s][info][gc,init] Heap Backing Filesystem: tmpfs (0x1021994)
 * [0.015s][info][gc,init] Min Capacity: 32M
 * [0.015s][info][gc,init] Initial Capacity: 32M 
 * [0.015s][info][gc,init] Max Capacity: 96M
 * [0.015s][info][gc,init] Medium Page Size: N/A
 * [0.015s][info][gc,init] Pre-touch: Disabled
 * [0.015s][info][gc,init] Available space on backing filesystem: N/A
 * [0.015s][info][gc,init] Uncommit: Enabled
 * [0.015s][info][gc,init] Uncommit Delay: 300s
 * [0.018s][info][gc,init] Runtime Workers: 1
 * [0.018s][info][gc,metaspace] CDS archive(s) mapped at: [0x0000000800000000-0x0000000800bb6000-0x0000000800bb6000), size 12279808, SharedBaseAddress: 0x0000000800000000, ArchiveRelocationMode: 0.
 * [0.018s][info][gc,metaspace] Compressed class space mapped at: 0x0000000800c00000-0x0000000840c00000, reserved size: 1073741824
 * [0.018s][info][gc,metaspace] Narrow klass base: 0x0000000800000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedHeaderEvent implements LogEvent, UnifiedLogging {

    private static Pattern pattern = Pattern.compile(UnifiedHeaderEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR
            + " (Address Space (Size|Type)|Alignments|Available space on backing filesystem|(Initial|Max|Min) Capacity|"
            + "CardTable entry size|(CDS archive\\(s\\)|Compressed class space) mapped at|Compressed Oops|"
            + "Concurrent( Refinement)? Workers|CPUs|GC threads|GC Workers|Heap (Initial|Max|Min) Capacity|"
            + "Heap Backing Filesystem|Heap Backing File|Heap Region (Count|Size)|Heuristics|"
            + "Humongous [oO]bject [tT]hreshold|Initialize Shenandoah heap|Initializing The Z Garbage Collector|"
            + "Large Page Support|Max TLAB size|Medium Page Size|Memory|Mode|Narrow klass base|"
            + "Min heap equals to max heap, disabling ShenandoahUncommit|NUMA Support|Pacer for Idle|Parallel Workers|"
            + "Periodic GC|Pre-touch|Regions|Runtime Workers|Safepointing mechanism|Shenandoah GC mode|"
            + "Shenandoah heuristics|TLAB Size Max|Uncommit( Delay)?|Using legacy single-generation mode)(:)?.*$";

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static boolean match(String logLine) {
        return pattern.matcher(logLine).matches();
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

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
    public UnifiedHeaderEvent(String logEntry) {
        this.logEntry = logEntry;

        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
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
            }
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_HEADER.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
