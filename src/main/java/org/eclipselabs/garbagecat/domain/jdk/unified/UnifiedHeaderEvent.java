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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
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
 * [0.003s][info][gc] Using Serial
 * [0.003s][info][gc,init] Version: 17.0.9+9-LTS (release)
 * [0.003s][info][gc,init] CPUs: 12 total, 12 available
 * [0.003s][info][gc,init] Memory: 31888M
 * [0.003s][info][gc,init] Large Page Support: Disabled
 * [0.003s][info][gc,init] NUMA Support: Disabled
 * [0.003s][info][gc,init] Compressed Oops: Enabled (32-bit)
 * [0.003s][info][gc,init] Heap Min Capacity: 2M
 * [0.003s][info][gc,init] Heap Initial Capacity: 2M
 * [0.003s][info][gc,init] Heap Max Capacity: 64M
 * [0.003s][info][gc,init] Pre-touch: Disabled
 * [0.010s][info][gc,metaspace] CDS archive(s) mapped at: [0x00007fe80b000000-0x00007fe80bbc8000-0x00007fe80bbc8000), size 12353536, SharedBaseAddress: 0x00007fe80b000000, ArchiveRelocationMode: 1.
 * [0.010s][info][gc,metaspace] Compressed class space mapped at: 0x00007fe80c000000-0x00007fe84c000000, reserved size: 1073741824
 * [0.010s][info][gc,metaspace] Narrow klass base: 0x00007fe80b000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * </pre>
 * 
 * <p>
 * 2) Parallel:
 * </p>
 * 
 * <pre>
 * [0.004s][info][gc] Using Parallel
 * [0.005s][info][gc,init] Version: 17.0.9+9-LTS (release)
 * [0.005s][info][gc,init] CPUs: 12 total, 12 available
 * [0.005s][info][gc,init] Memory: 31888M
 * [0.005s][info][gc,init] Large Page Support: Disabled
 * [0.005s][info][gc,init] NUMA Support: Disabled
 * [0.005s][info][gc,init] Compressed Oops: Enabled (32-bit)
 * [0.005s][info][gc,init] Alignments: Space 512K, Generation 512K, Heap 2M
 * [0.005s][info][gc,init] Heap Min Capacity: 2M
 * [0.005s][info][gc,init] Heap Initial Capacity: 2M
 * [0.005s][info][gc,init] Heap Max Capacity: 64M
 * [0.005s][info][gc,init] Pre-touch: Disabled
 * [0.005s][info][gc,init] Parallel Workers: 10
 * [0.014s][info][gc,metaspace] CDS archive(s) mapped at: [0x00007f1d1f000000-0x00007f1d1fbc8000-0x00007f1d1fbc8000), size 12353536, SharedBaseAddress: 0x00007f1d1f000000, ArchiveRelocationMode: 1.
 * [0.014s][info][gc,metaspace] Compressed class space mapped at: 0x00007f1d20000000-0x00007f1d60000000, reserved size: 1073741824
 * [0.014s][info][gc,metaspace] Narrow klass base: 0x00007f1d1f000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * </pre>
 * 
 * <p>
 * 3) G1:
 * </p>
 * 
 * <pre>
 * [0.002s][info][gc] Using G1
 * [0.002s][info][gc,init] Version: 17.0.9+9-LTS (release)
 * [0.002s][info][gc,init] CPUs: 12 total, 12 available
 * [0.002s][info][gc,init] Memory: 31888M
 * [0.002s][info][gc,init] Large Page Support: Disabled
 * [0.002s][info][gc,init] NUMA Support: Disabled
 * [0.002s][info][gc,init] Compressed Oops: Enabled (32-bit)
 * [0.002s][info][gc,init] Heap Region Size: 1M
 * [0.002s][info][gc,init] Heap Min Capacity: 2M
 * [0.002s][info][gc,init] Heap Initial Capacity: 2M
 * [0.002s][info][gc,init] Heap Max Capacity: 96M
 * [0.002s][info][gc,init] Pre-touch: Disabled
 * [0.002s][info][gc,init] Parallel Workers: 10
 * [0.002s][info][gc,init] Concurrent Workers: 3
 * [0.002s][info][gc,init] Concurrent Refinement Workers: 10
 * [0.002s][info][gc,init] Periodic GC: Disabled
 * [0.007s][info][gc,metaspace] CDS archive(s) mapped at: [0x00007f4dcb000000-0x00007f4dcbbc8000-0x00007f4dcbbc8000), size 12353536, SharedBaseAddress: 0x00007f4dcb000000, ArchiveRelocationMode: 1.
 * [0.007s][info][gc,metaspace] Compressed class space mapped at: 0x00007f4dcc000000-0x00007f4e0c000000, reserved size: 1073741824
 * [0.007s][info][gc,metaspace] Narrow klass base: 0x00007f4dcb000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * </pre>
 * 
 * <p>
 * 4) Shenandoah:
 * </p>
 * 
 * <pre>
 * [0.002s][info][gc] Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent
 * [0.002s][info][gc] Heuristics ergonomically sets -XX:+ShenandoahImplicitGCInvokesConcurrent
 * [0.002s][info][gc] Using Shenandoah
 * [0.002s][info][gc,ergo] Pacer for Idle. Initial: 1966K, Alloc Tax Rate: 1.0x
 * [0.002s][info][gc,init] Version: 17.0.9+9-LTS (release)
 * [0.002s][info][gc,init] CPUs: 12 total, 12 available
 * [0.002s][info][gc,init] Memory: 31888M
 * [0.002s][info][gc,init] Large Page Support: Disabled
 * [0.002s][info][gc,init] NUMA Support: Disabled
 * [0.002s][info][gc,init] Compressed Oops: Enabled (32-bit)
 * [0.002s][info][gc,init] Heap Min Capacity: 32M
 * [0.002s][info][gc,init] Heap Initial Capacity: 32M
 * [0.002s][info][gc,init] Heap Max Capacity: 96M
 * [0.002s][info][gc,init] Pre-touch: Disabled
 * [0.002s][info][gc,init] Mode: Snapshot-At-The-Beginning (SATB)
 * [0.002s][info][gc,init] Heuristics: Adaptive
 * [0.002s][info][gc,init] Heap Region Count: 384
 * [0.002s][info][gc,init] Heap Region Size: 256K
 * [0.002s][info][gc,init] TLAB Size Max: 256K
 * [0.002s][info][gc,init] Humongous Object Threshold: 256K
 * [0.002s][info][gc,init] Parallel Workers: 6
 * [0.002s][info][gc,init] Concurrent Workers: 3
 * [0.006s][info][gc,metaspace] CDS archive(s) mapped at: [0x00007f293f000000-0x00007f293fbc8000-0x00007f293fbc8000), size 12353536, SharedBaseAddress: 0x00007f293f000000, ArchiveRelocationMode: 1.
 * [0.006s][info][gc,metaspace] Compressed class space mapped at: 0x00007f2940000000-0x00007f2980000000, reserved size: 1073741824
 * [0.006s][info][gc,metaspace] Narrow klass base: 0x00007f293f000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * </pre>
 * 
 * <p>
 * 5) Z:
 * </p>
 * 
 * <pre>
 * [0.002s][info][gc,init] Initializing The Z Garbage Collector
 * [0.002s][info][gc,init] Version: 17.0.9+9-LTS (release)
 * [0.002s][info][gc,init] NUMA Support: Disabled
 * [0.002s][info][gc,init] CPUs: 12 total, 12 available
 * [0.002s][info][gc,init] Memory: 31888M
 * [0.002s][info][gc,init] Large Page Support: Disabled
 * [0.002s][info][gc,init] GC Workers: 1 (dynamic)
 * [0.003s][info][gc,init] Address Space Type: Contiguous/Unrestricted/Complete
 * [0.003s][info][gc,init] Address Space Size: 1536M x 3 = 4608M
 * [0.003s][info][gc,init] Heap Backing File: /memfd:java_heap
 * [0.003s][info][gc,init] Heap Backing Filesystem: tmpfs (0x1021994)
 * [0.003s][info][gc,init] Min Capacity: 32M
 * [0.003s][info][gc,init] Initial Capacity: 32M
 * [0.003s][info][gc,init] Max Capacity: 96M
 * [0.003s][info][gc,init] Medium Page Size: N/A
 * [0.003s][info][gc,init] Pre-touch: Disabled
 * [0.003s][info][gc,init] Available space on backing filesystem: N/A
 * [0.003s][info][gc,init] Uncommit: Enabled
 * [0.003s][info][gc,init] Uncommit Delay: 300s
 * [0.007s][info][gc,init] Runtime Workers: 1
 * [0.007s][info][gc     ] Using The Z Garbage Collector
 * [0.012s][info][gc,metaspace] CDS archive(s) mapped at: [0x00007f35ef000000-0x00007f35efba0000-0x00007f35efba0000), size 12189696, SharedBaseAddress: 0x00007f35ef000000, ArchiveRelocationMode: 1.
 * [0.012s][info][gc,metaspace] Compressed class space mapped at: 0x00007f35f0000000-0x00007f3630000000, reserved size: 1073741824
 * [0.012s][info][gc,metaspace] Narrow klass base: 0x00007f35ef000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedHeaderEvent implements LogEvent, UnifiedLogging {

    /**
     * Regular expression for garbage collector information.
     */
    private static final String __REGEX_GARBAGE_COLLECTOR = "Using (Concurrent Mark Sweep|G1|Parallel|Serial|"
            + "Shenandoah|The Z Garbage Collector)";

    /**
     * Regular expression for JDK version information.
     */
    private static final String __REGEX_VERSION = "Version: " + UnifiedRegEx.RELEASE_STRING + " \\(release\\)";

    /**
     * Regular expression for memory information.
     */
    private static final String __REGEX_MEMORY = "Memory: (" + JdkRegEx.SIZE + ")";

    /**
     * Regular expression for jvm_args information.
     */
    private static final String __REGEX_JVM_ARGS = "jvm_args:[ ]{0,1}(.*)";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " (" + __REGEX_GARBAGE_COLLECTOR + "|"
            + __REGEX_VERSION + "|" + __REGEX_JVM_ARGS + "|" + __REGEX_MEMORY
            + "| - (commit_granule_bytes|commit_granule_words|enlarge_chunks_in_place|use_allocation_guard|"
            + "virtual_space_node_default_size)|Activate regions|Address Space (Size|Type)|Alignments|"
            + "Available space on backing filesystem|(Initial|Max|Min) Capacity|CardTable entry size|"
            + "CDS archive\\(s\\) (mapped at|not mapped)|Compressed class space mapped at|Compressed Oops|"
            + "ConcGCThreads|Concurrent( Refinement)? Workers|CPUs|Consider \\-XX:\\+ClassUnloadingWithConcurrentMark|"
            + "Expand the heap\\.|GC threads|GC Workers|Heap (Initial|Max|Min) Capacity|Heap address|"
            + "Heap Backing Filesystem|Heap Backing File|Heap [Rr]egion (Count|[Ss]ize)|Heuristics|"
            + "Heuristics ergonomically sets |Humongous [oO]bject [tT]hreshold|Initialize mark stack|"
            + "Initial Refinement Zones|Initialize Shenandoah heap|Initializing The Z Garbage Collector|"
            + "java_class_path \\(initial\\)|java_command|Large Page Support|Launcher Type|"
            + "Mark (closed|open) archive regions in map|Max TLAB size|Medium Page Size|Mode|"
            + "Min heap equals to max heap, disabling ShenandoahUncommit|Minimum heap|Narrow klass base|NUMA Nodes|"
            + "NUMA Support|Pacer for Idle|ParallelGCThreads|Parallel Workers|Periodic GC|Pre-touch|"
            + "Reference processing|Regions|Runtime Workers|Safepointing mechanism|Shenandoah GC mode|"
            + "Shenandoah heuristics|String Deduplication|Target occupancy update|TLAB Size Max|Uncommit( Delay)?|"
            + "Using legacy single-generation mode|VM Arguments)(:)?.*$";

    private static Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static boolean match(String logLine) {
        return PATTERN.matcher(logLine).matches();
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
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(2).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                timestamp = Long.parseLong(matcher.group(13));
            } else if (matcher.group(2).matches(UnifiedRegEx.UPTIME)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            } else {
                if (matcher.group(15) != null) {
                    if (matcher.group(15).matches(UnifiedRegEx.UPTIMEMILLIS)) {
                        timestamp = Long.parseLong(matcher.group(17));
                    } else {
                        timestamp = JdkMath.convertSecsToMillis(matcher.group(16)).longValue();
                    }
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
            }
        }
    }

    /**
     * @return The <code>CollectorFamily</code> based on the garbage collector header.
     */
    public CollectorFamily getCollectorFamily() {
        CollectorFamily collectorFamily = CollectorFamily.UNKNOWN;
        String using = null;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.matches()) {
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2) != null) {
                using = matcher.group(UnifiedRegEx.DECORATOR_SIZE + 2);
            }
        }
        if (using != null) {
            if (using.equals("Concurrent Mark Sweep")) {
                collectorFamily = CollectorFamily.CMS;
            } else if (using.equals("G1")) {
                collectorFamily = CollectorFamily.G1;
            } else if (using.equals("Parallel")) {
                collectorFamily = CollectorFamily.PARALLEL;
            } else if (using.equals("Serial")) {
                collectorFamily = CollectorFamily.SERIAL;
            } else if (using.equals("Shenandoah")) {
                collectorFamily = CollectorFamily.SHENANDOAH;
            } else if (using.equals("The Z Garbage Collector")) {
                collectorFamily = CollectorFamily.Z;
            }
        }
        return collectorFamily;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.UNIFIED_HEADER;
    }

    /**
     * The Java release string. For example:
     * 
     * <pre>
     * 1.8.0_332-b09-1
     * 11.0.15+9-LTS-1
     * 17.0.3+6-LTS-2
     * </pre>
     * 
     * @return The Java release string.
     */
    public String getJdkReleaseString() {
        String jdkReleaseString = null;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            jdkReleaseString = matcher.group(UnifiedRegEx.DECORATOR_SIZE + 3);
        }
        return jdkReleaseString;
    }

    /**
     * @return The JDK major version, or <code>org.github.joa.domain.JvmContext.UNKNOWN</code> if it cannot be
     *         determined. Not available in unified logging (JDK11+).
     */
    public int getJdkVersionMajor() {
        int jdkVersionMajor = org.github.joa.domain.JvmContext.UNKNOWN;
        if (logEntry != null) {
            Matcher matcher = PATTERN.matcher(logEntry);
            if (matcher.find()) {
                int index = UnifiedRegEx.DECORATOR_SIZE + 4;
                if (matcher.group(index) != null) {
                    if (matcher.group(index).equals("1.6.0")) {
                        jdkVersionMajor = 6;
                    } else if (matcher.group(index).equals("1.7.0")) {
                        jdkVersionMajor = 7;
                    } else if (matcher.group(index).equals("1.8.0")) {
                        jdkVersionMajor = 8;
                    } else {
                        jdkVersionMajor = Integer.parseInt(matcher.group(index));
                    }
                }
            }
        }
        return jdkVersionMajor;
    }

    /**
     * @return The JDK minor version (update), or <code>org.github.joa.domain.JvmContext.UNKNOWN</code> if it cannot be
     *         determined.
     */
    public int getJdkVersionMinor() {
        int jdkVersionMinor = org.github.joa.domain.JvmContext.UNKNOWN;
        if (logEntry != null) {
            Matcher matcher = PATTERN.matcher(logEntry);
            if (matcher.find()) {
                int index = UnifiedRegEx.DECORATOR_SIZE + 6;
                if (matcher.group(index) != null) {
                    jdkVersionMinor = Integer.parseInt(matcher.group(index));
                }
            }
        }
        return jdkVersionMinor;
    }

    /**
     * @return The jvm_args string.
     */
    public String getJvmArgs() {
        String jvmArgs = null;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            jvmArgs = matcher.group(UnifiedRegEx.DECORATOR_SIZE + 8);
        }
        return jvmArgs;
    }

    public String getLogEntry() {
        return logEntry;
    }

    /**
     * @return The memory for the given memory string.
     */
    public Memory getMemory() {
        Memory memory = null;
        if (isMemory()) {
            Matcher matcher = PATTERN.matcher(logEntry);
            if (matcher.find()) {
                memory = memory(matcher.group(UnifiedRegEx.DECORATOR_SIZE + 10),
                        matcher.group(UnifiedRegEx.DECORATOR_SIZE + 12).charAt(0));
            }
        }
        return memory;
    }

    /**
     * The memory string. For example:
     * 
     * <pre>
     * Memory: 31888M
     * </pre>
     * 
     * @return The memory string.
     */
    public String getMemoryString() {
        String memory = null;
        if (isMemory()) {
            Matcher matcher = PATTERN.matcher(logEntry);
            if (matcher.find()) {
                memory = matcher.group(UnifiedRegEx.DECORATOR_SIZE + 9);
            }
        }
        return memory;
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isEndstamp() {
        return false;
    }

    /**
     * @return true if the header contains garbage collector information, false otherwise.
     */
    public boolean isGarbageCollector() {
        boolean isGarbageCollector = false;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.matches()) {
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1) != null) {
                isGarbageCollector = matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1).matches(__REGEX_GARBAGE_COLLECTOR);
            }
        }
        return isGarbageCollector;
    }

    /**
     * @return true if the header contains jvm_args information, false otherwise.
     */
    public boolean isJvmArgs() {
        boolean isJvmArgs = false;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.matches()) {
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1) != null) {
                isJvmArgs = matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1).matches(__REGEX_JVM_ARGS);
            }
        }
        return isJvmArgs;
    }

    /**
     * @return true if the header contains memory information, false otherwise.
     */
    public boolean isMemory() {
        boolean isMemory = false;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.matches()) {
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1) != null) {
                isMemory = matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1).matches(__REGEX_MEMORY);
            }
        }
        return isMemory;
    }

    /**
     * @return true if the header contains JDK version information, false otherwise.
     */
    public boolean isVersion() {
        boolean isVersion = false;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.matches()) {
            if (matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1) != null) {
                isVersion = matcher.group(UnifiedRegEx.DECORATOR_SIZE + 1).matches(__REGEX_VERSION);
            }
        }
        return isVersion;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
