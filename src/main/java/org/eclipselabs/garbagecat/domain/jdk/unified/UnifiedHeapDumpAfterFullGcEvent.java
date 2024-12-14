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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
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
 * [2024-12-06T05:57:30.285-0500] GC(0) Pause Full (Diagnostic Command)
 * [2024-12-06T05:57:30.294-0500] GC(0) Using 2 workers of 10 for full compaction
 * [2024-12-06T05:57:30.294-0500] GC(0) Phase 1: Mark live objects
 * [2024-12-06T05:57:30.295-0500] GC(0) Phase 1: Mark live objects 1.050ms
 * [2024-12-06T05:57:30.295-0500] GC(0) Phase 2: Prepare compaction
 * [2024-12-06T05:57:30.296-0500] GC(0) Phase 2: Prepare compaction 0.232ms
 * [2024-12-06T05:57:30.296-0500] GC(0) Phase 3: Adjust pointers
 * [2024-12-06T05:57:30.296-0500] GC(0) Phase 3: Adjust pointers 0.393ms
 * [2024-12-06T05:57:30.296-0500] GC(0) Phase 4: Compact heap
 * [2024-12-06T05:57:30.296-0500] GC(0) Phase 4: Compact heap 0.469ms
 * [2024-12-06T05:57:30.296-0500] GC(0) Phase 5: Reset Metadata
 * [2024-12-06T05:57:30.297-0500] GC(0) Phase 5: Reset Metadata 0.107ms
 * [2024-12-06T05:57:30.297-0500] GC(0) Eden regions: 1-&gt;0(2)
 * [2024-12-06T05:57:30.298-0500] GC(0) Survivor regions: 0-&gt;0(0)
 * [2024-12-06T05:57:30.298-0500] GC(0) Old regions: 1-&gt;2
 * [2024-12-06T05:57:30.298-0500] GC(0) Humongous regions: 0-&gt;0
 * [2024-12-06T05:57:30.298-0500] GC(0) Metaspace: 71K(320K)-&gt;71K(320K) NonClass: 68K(192K)-&gt;68K(192K) Class: 3K(128K)-&gt;3K(128K)
 * [2024-12-06T05:57:30.298-0500] GC(0) Heap Dump (after full gc)
 * [2024-12-06T05:57:30.303-0500] GC(0) Heap Dump (after full gc) 5.254ms
 * [2024-12-06T05:57:30.303-0500] GC(0) Pause Full (Diagnostic Command) 3M-&gt;1M(28M) 18.086ms
 * [2024-12-06T05:57:30.303-0500] GC(0) User=0.00s Sys=0.01s Real=0.02s
 * [2024-12-06T05:57:30.303-0500] Safepoint "G1CollectFull", Time since last: 22762437431 ns, Reaching safepoint: 67307 ns, Cleanup: 3200 ns, At safepoint: 18107896 ns, Total: 18178403 ns
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedHeapDumpAfterFullGcEvent implements LogEvent, UnifiedLogging {
    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Heap Dump \\(after full gc\\) "
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
    public UnifiedHeapDumpAfterFullGcEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC;
    }

    public String getLogEntry() {
        return logEntry;
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
