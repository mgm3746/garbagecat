/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
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
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Serial:
 * </p>
 * 
 * <pre>
 * [0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)
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
 * [0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)
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
 * [0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)
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
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedHeaderEvent extends G1Collector implements UnifiedLogging {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR
            + " (Alignments|(CDS archive\\(s\\)|Compressed class space) mapped at|Compressed Oops|"
            + "Concurrent( Refinement)? Workers|CPUs|Heap (Initial|Max|Min) Capacity|Heap Region Size|"
            + "Large Page Support|Memory|Narrow klass base|NUMA Support|Parallel Workers|Periodic GC|Pre-touch|Version)"
            + ":.+$";

    private static Pattern pattern = Pattern.compile(REGEX);

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
}
