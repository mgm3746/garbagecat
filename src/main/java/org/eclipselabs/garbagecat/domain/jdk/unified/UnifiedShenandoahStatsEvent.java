/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.HeaderEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_SHENANDOAH_STATS
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * [2024-04-09T08:26:09.935-0400] All times are wall-clock times, except per-root-class counters, that are sum over
 * [2024-04-09T08:26:09.935-0400] all workers. Dividing the &lt;total&gt; over the root stage time estimates parallelism.
 * [2024-04-09T08:26:09.935-0400]
 * [2024-04-09T08:26:09.935-0400] Concurrent Reset                    104 us
 * [2024-04-09T08:26:09.935-0400] Pause Init Mark (G)                  59 us
 * [2024-04-09T08:26:09.935-0400] Pause Init Mark (N)                  21 us
 * [2024-04-09T08:26:09.935-0400]   Update Region States                1 us
 * [2024-04-09T08:26:09.935-0400] Concurrent Mark Roots               317 us, parallelism: 1.22x
 * [2024-04-09T08:26:09.935-0400]   CMR: &lt;total&gt;                      387 us
 * [2024-04-09T08:26:09.936-0400]   CMR: Thread Roots                 355 us, workers (us): 246, 109, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400]   CMR: VM Strong Roots                3 us, workers (us):   1,   1, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400]   CMR: CLDG Roots                    30 us, workers (us): ---,  30, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400] Concurrent Marking                 3899 us
 * [2024-04-09T08:26:09.936-0400] Pause Final Mark (G)                173 us
 * [2024-04-09T08:26:09.936-0400] Pause Final Mark (N)                123 us
 * [2024-04-09T08:26:09.936-0400]   Finish Mark                        75 us
 * [2024-04-09T08:26:09.936-0400]   Update Region States                2 us
 * [2024-04-09T08:26:09.936-0400]   Choose Collection Set              20 us
 * [2024-04-09T08:26:09.936-0400]   Rebuild Free Set                    2 us
 * [2024-04-09T08:26:09.936-0400] Concurrent Thread Roots             269 us, parallelism: 1.08x
 * [2024-04-09T08:26:09.936-0400]   CTR: &lt;total&gt;                      292 us
 * [2024-04-09T08:26:09.936-0400]   CTR: Thread Roots                 292 us, workers (us): 210,  82, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400] Concurrent Weak References           68 us, parallelism: 0.11x
 * [2024-04-09T08:26:09.936-0400]   CWRF: &lt;total&gt;                       8 us
 * [2024-04-09T08:26:09.936-0400]   CWRF: Weak References               8 us, workers (us):   1,   7, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400] Concurrent Weak Roots               289 us
 * [2024-04-09T08:26:09.936-0400]   Roots                             234 us, parallelism: 1.38x
 * [2024-04-09T08:26:09.936-0400]     CWR: &lt;total&gt;                    322 us
 * [2024-04-09T08:26:09.936-0400]     CWR: Code Cache Roots           184 us, workers (us):  72, 112, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400]     CWR: VM Weak Roots              137 us, workers (us):  77,  59, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400]     CWR: CLDG Roots                   1 us, workers (us): ---,   1, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400]   Rendezvous                         41 us
 * [2024-04-09T08:26:09.936-0400] Concurrent Cleanup                   25 us
 * [2024-04-09T08:26:09.936-0400] Concurrent Class Unloading          451 us
 * [2024-04-09T08:26:09.936-0400]   Unlink Stale                      391 us
 * [2024-04-09T08:26:09.936-0400]     System Dictionary                 2 us
 * [2024-04-09T08:26:09.936-0400]     Weak Class Links                  0 us
 * [2024-04-09T08:26:09.936-0400]     Code Roots                      389 us
 * [2024-04-09T08:26:09.936-0400]   Rendezvous                         43 us
 * [2024-04-09T08:26:09.936-0400]   Purge Unlinked                      2 us
 * [2024-04-09T08:26:09.936-0400]     Code Roots                        0 us
 * [2024-04-09T08:26:09.936-0400]     CLDG                              2 us
 * [2024-04-09T08:26:09.936-0400]     Exception Caches                  0 us
 * [2024-04-09T08:26:09.936-0400] Concurrent Strong Roots              80 us, parallelism: 0.26x
 * [2024-04-09T08:26:09.936-0400]   CSR: &lt;total&gt;                      20 us
 * [2024-04-09T08:26:09.936-0400]   CSR: VM Strong Roots                2 us, workers (us):   1,   1, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400]   CSR: CLDG Roots                    18 us, workers (us):  18, ---, ---, ---, ---, ---,
 * [2024-04-09T08:26:09.936-0400] Concurrent Evacuation               169 us
 * [2024-04-09T08:26:09.936-0400] Pause Init Update Refs (G)           58 us
 * [2024-04-09T08:26:09.936-0400] Pause Init Update Refs (N)           17 us
 * [2024-04-09T08:26:09.936-0400]   Manage GCLABs                       1 us
 * [2024-04-09T08:26:09.936-0400] Concurrent Update Refs             2343 us
 * [2024-04-09T08:26:09.936-0400] Concurrent Update Thread Roots      266 us
 * [2024-04-09T08:26:09.936-0400] Pause Final Update Refs (G)         136 us
 * [2024-04-09T08:26:09.936-0400] Pause Final Update Refs (N)          20 us
 * [2024-04-09T08:26:09.936-0400]   Update Region States                2 us
 * [2024-04-09T08:26:09.936-0400]   Trash Collection Set                1 us
 * [2024-04-09T08:26:09.936-0400]   Rebuild Free Set                    2 us
 * [2024-04-09T08:26:09.936-0400] Concurrent Cleanup                   32 us
 * [2024-04-09T08:26:09.936-0400] Pacing                             6427 us
 * [2024-04-09T08:26:09.936-0400]
 * [2024-04-09T08:26:09.936-0400] Allocation pacing accrued:
 * [2024-04-09T08:26:09.936-0400]       6 of    10 ms ( 63.5%): main
 * [2024-04-09T08:26:09.936-0400]       6 of    10 ms ( 63.5%): &lt;total&gt;
 * [2024-04-09T08:26:09.936-0400]       1 of    10 ms (  5.3%): &lt;average total&gt;
 * [2024-04-09T08:26:09.936-0400]       6 of    10 ms ( 63.5%): &lt;average non-zero&gt;
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedShenandoahStatsEvent extends ShenandoahCollector
        implements UnifiedLogging, HeaderEvent, ThrowAwayEvent {
    /**
     * Regular expression for the header.
     */
    public static final String _REGEX_HEADER = "^" + UnifiedRegEx.DECORATOR
            + " All times are wall-clock times, except per-root-class counters, that are sum over$";
    /**
     * Regular expression defining standard logging.
     */
    private static final String REGEX[] = {
            //
            _REGEX_HEADER,
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " all workers\\. Dividing the <total> over the root stage time estimates parallelism\\.$",
            // Main headings (1 space)
            "^" + UnifiedRegEx.DECORATOR
                    + " (Concurrent (Cleanup|Class Unloading|Evacuation|Marking|Mark Roots|Precleaning|Reset|"
                    + "Strong Roots|Thread Roots|Update Refs|Update Thread Roots|Weak References|Weak Roots)|Pacing|"
                    + "Pause Degenerated GC \\([GN]\\)|Pause Final Mark \\([GN]\\)|Pause Final Update Refs \\([GN]\\)|"
                    + "Pause Init Mark \\([GN]\\)|Pause Init[ ]{1,2}Update Refs \\([GN]\\))[ ]{1,}\\d{1,} us.*$",
            // Indented 3 spaces
            "^" + UnifiedRegEx.DECORATOR
                    + "   (Accumulate Stats|Choose Collection Set|Cleanup|CMR: (<total>|(CLDG|Thread|VM Strong) Roots)|"
                    + "CSR: (<total>|(CLDG|VM Strong) Roots)|CTR: (<total>|Thread Roots)|"
                    + "CWRF: (<total>|Weak References)|Degen (STW Mark|Update Roots)|Evacuation|Finish (Mark|Queues)|"
                    + "Initial Evacuation|Make Parsable|Manage (GC\\/TLABs|GCLABs)|Purge Unlinked|Rebuild Free Set|"
                    + "Rendezvous|Resize TLABs|Retire (GCLABs|TLABs)|Roots|Scan Roots|System Purge|"
                    + "Trash Collection Set|Update Region States|Unlink Stale|Update (References|Region States|Roots)|"
                    + "Weak References)[ ]{1,}\\d{1,} us.*$",
            // Indented 5 spaces
            "^" + UnifiedRegEx.DECORATOR + "     (CLDG|Cleanup|CMR: (<total>|(CLDG|Code Cache|VM Strong) Roots)|"
                    + "CWR: (<total>|(CLDG|Code Cache|Thread|VM Strong|VM Weak) Roots)|Code Roots|Exception Caches|"
                    + "DSM: (<total>|Parallel Mark|(CLDG|Thread|VM Strong) Roots)|"
                    + "DU: (<total>|(CLDG|Code Cache|Thread|VM Strong|VM Weak) Roots)|E: (<total>|(CLDG|Code Cache|"
                    + "JNI Handles|JVMTI|Management|Synchronizer|System Dict|Thread|Universe) Roots)|" + "Process|"
                    + "S: (<total>|(CLDG|JNI Handles|JVMTI|Management|Synchronizer|System Dict|Thread|Universe) Roots)|"
                    + "System (Dictionary|Purge)|Unload Classes|UR: (<total>|Thread Roots)|Weak Class Links|"
                    + "Weak Roots) .*$",
            // Indented 7 spaces
            "^" + UnifiedRegEx.DECORATOR + "       (CLDG|"
                    + "CU: (<total>|Code Cache (Cleaning|Roots)|(CLDG|String Table|Resolved Table) Roots)|"
                    + "Unload Classes|Weak (References|Roots)) .*$",
            // Indented 9 spaces
            "^" + UnifiedRegEx.DECORATOR + "         (DCU: (<total>|Unlink CLDs|Unload Code Caches)|"
                    + "DWR: (<total>|VM Weak Roots)|WRP: (<total>|Weak References)) .*$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Allocation pacing accrued:$",
            //
            "^" + UnifiedRegEx.DECORATOR + "[ ]{0,}\\d{1,} of[ ]{0,}\\d{1,} ms \\([ ]{0,}\\d{1,}\\.\\d%\\):.+$"
            //
    };
    private static final List<Pattern> REGEX_PATTERN_LIST = new ArrayList<>(REGEX.length);
    static {
        for (String regex : REGEX) {
            REGEX_PATTERN_LIST.add(Pattern.compile(regex));
        }
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        for (int i = 0; i < REGEX_PATTERN_LIST.size(); i++) {
            Pattern pattern = REGEX_PATTERN_LIST.get(i);
            if (pattern.matcher(logLine).matches()) {
                match = true;
                break;
            }
        }
        return match;
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedShenandoahStatsEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS;
    }

    public String getLogEntry() {
        return logEntry;
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return 0;
    }

    @Override
    public boolean isEndstamp() {
        return false;
    }

    @Override
    public boolean isHeader() {
        boolean isHeader = false;
        if (this.logEntry != null) {
            isHeader = logEntry.matches(_REGEX_HEADER);
        }
        return isHeader;
    }
}
