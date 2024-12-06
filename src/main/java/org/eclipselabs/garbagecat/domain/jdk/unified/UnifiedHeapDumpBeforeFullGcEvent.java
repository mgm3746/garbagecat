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

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_HEAP_DUMP
 * </p>
 * 
 * <p>
 * A heap dump as a result of -XX:+HeapDumpAfterFullGC or -XX:+HeapDumpAfterFullGC. The heap dump time is included in
 * the full gc, so it is handled like a concurrent event.
 * </p>
 * 
 * <pre>
 * [2024-12-06T10:15:44.116-0500] CardTable entry size: 512
 * [2024-12-06T10:15:44.116-0500] Using G1
 * [2024-12-06T10:15:44.118-0500] Version: 21.0.5+10-LTS (release)
 * [2024-12-06T10:15:44.118-0500] CPUs: 12 total, 12 available
 * [2024-12-06T10:15:44.118-0500] Memory: 31900M
 * [2024-12-06T10:15:44.118-0500] Large Page Support: Disabled
 * [2024-12-06T10:15:44.118-0500] NUMA Support: Disabled
 * [2024-12-06T10:15:44.118-0500] Compressed Oops: Enabled (Zero based)
 * [2024-12-06T10:15:44.118-0500] Heap Region Size: 4M
 * [2024-12-06T10:15:44.118-0500] Heap Min Capacity: 8M
 * [2024-12-06T10:15:44.118-0500] Heap Initial Capacity: 500M
 * [2024-12-06T10:15:44.118-0500] Heap Max Capacity: 7976M
 * [2024-12-06T10:15:44.118-0500] Pre-touch: Disabled
 * [2024-12-06T10:15:44.118-0500] Parallel Workers: 10
 * [2024-12-06T10:15:44.118-0500] Concurrent Workers: 3
 * [2024-12-06T10:15:44.118-0500] Concurrent Refinement Workers: 10
 * [2024-12-06T10:15:44.118-0500] Periodic GC: Disabled
 * [2024-12-06T10:15:44.129-0500] CDS archive(s) mapped at: [0x00007fbba3000000-0x00007fbba3c99000-0x00007fbba3c99000), size 13209600, SharedBaseAddress: 0x00007fbba3000000, ArchiveRelocationMode: 1.
 * [2024-12-06T10:15:44.129-0500] Compressed class space mapped at: 0x00007fbba4000000-0x00007fbbe4000000, reserved size: 1073741824
 * [2024-12-06T10:15:44.129-0500] Narrow klass base: 0x00007fbba3000000, Narrow klass shift: 0, Narrow klass range: 0x100000000
 * [2024-12-06T10:15:56.118-0500] GC(0) Pause Full (Diagnostic Command)
 * [2024-12-06T10:15:56.118-0500] GC(0) Heap Dump (before full gc)
 * [2024-12-06T10:15:56.126-0500] GC(0) Heap Dump (before full gc) 7.667ms
 * [2024-12-06T10:15:56.126-0500] GC(0) Using 2 workers of 10 for full compaction
 * [2024-12-06T10:15:56.126-0500] GC(0) Phase 1: Mark live objects
 * [2024-12-06T10:15:56.127-0500] GC(0) Phase 1: Mark live objects 0.764ms
 * [2024-12-06T10:15:56.127-0500] GC(0) Phase 2: Prepare compaction
 * [2024-12-06T10:15:56.127-0500] GC(0) Phase 2: Prepare compaction 0.232ms
 * [2024-12-06T10:15:56.127-0500] GC(0) Phase 3: Adjust pointers
 * [2024-12-06T10:15:56.127-0500] GC(0) Phase 3: Adjust pointers 0.453ms
 * [2024-12-06T10:15:56.127-0500] GC(0) Phase 4: Compact heap
 * [2024-12-06T10:15:56.128-0500] GC(0) Phase 4: Compact heap 0.473ms
 * [2024-12-06T10:15:56.128-0500] GC(0) Phase 5: Reset Metadata
 * [2024-12-06T10:15:56.128-0500] GC(0) Phase 5: Reset Metadata 0.081ms
 * [2024-12-06T10:15:56.129-0500] GC(0) Eden regions: 1->0(2)
 * [2024-12-06T10:15:56.129-0500] GC(0) Survivor regions: 0->0(0)
 * [2024-12-06T10:15:56.129-0500] GC(0) Old regions: 1->2
 * [2024-12-06T10:15:56.129-0500] GC(0) Humongous regions: 0->0
 * [2024-12-06T10:15:56.129-0500] GC(0) Metaspace: 71K(320K)->71K(320K) NonClass: 68K(192K)->68K(192K) Class: 3K(128K)->3K(128K)
 * [2024-12-06T10:15:56.129-0500] GC(0) Pause Full (Diagnostic Command) 3M->1M(28M) 11.020ms
 * [2024-12-06T10:15:56.129-0500] GC(0) User=0.00s Sys=0.00s Real=0.02s
 * [2024-12-06T10:15:56.129-0500] Safepoint "G1CollectFull", Time since last: 11984558938 ns, Reaching safepoint: 65962 ns, Cleanup: 3459 ns, At safepoint: 11047330 ns, Total: 11116751 ns
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedHeapDumpBeforeFullGcEvent implements LogEvent, UnifiedLogging {
    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Heap Dump \\(before full gc\\) "
            + JdkRegEx.DURATION_MS + "[ ]*$";

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
    public UnifiedHeapDumpBeforeFullGcEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_HEAP_DUMP_BEFORE_FULL_GC.toString();
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
}
