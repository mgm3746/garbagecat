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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.HeaderEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * FOOTER_STATS
 * </p>
 * 
 * <p>
 * Stats information printed at the end of gc logging.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 *GC STATISTICS:
 *   "(G)" (gross) pauses include VM time: time to notify and block threads, do the pre-
 *         and post-safepoint housekeeping. Use -XX:+PrintSafepointStatistics to dissect.
 *   "(N)" (net) pauses are the times spent in the actual GC code.
 *   "a" is average time for each phase, look at levels to see if average makes sense.
 *   "lvls" are quantiles: 0% (minimum), 25%, 50% (median), 75%, 100% (maximum).
 * 
 * Total Pauses (G)            =    11.13 s (a =      495 us) (n = 22472) (lvls, us =       43,      117,      445,      676,    61882)
 * Total Pauses (N)            =     7.81 s (a =      348 us) (n = 22472) (lvls, us =       10,       17,      340,      504,    44201)
 * Pause Init Mark (G)         =     3.58 s (a =      503 us) (n =  7119) (lvls, us =      285,      408,      461,      520,    15931)
 * Pause Init Mark (N)         =     2.66 s (a =      373 us) (n =  7119) (lvls, us =      223,      320,      357,      402,     5994)
 *   Accumulate Stats          =     0.02 s (a =        3 us) (n =  7119) (lvls, us =        2,        2,        2,        3,       27)
 *   Make Parsable             =     0.01 s (a =        1 us) (n =  7119) (lvls, us =        0,        0,        1,        1,       22)
 *   Clear Liveness            =     0.05 s (a =        7 us) (n =  7119) (lvls, us =        3,        5,        6,        7,       38)
 *   Scan Roots                =     2.40 s (a =      338 us) (n =  7119) (lvls, us =      195,      289,      322,      363,     5914)
 *     S: Thread Roots         =     0.08 s (a =       11 us) (n =  7119) (lvls, us =        5,        9,       10,       12,      393)
 *     S: String Table Roots   =     1.33 s (a =      186 us) (n =  7119) (lvls, us =       83,      166,      189,      203,      670)
 *     S: Universe Roots       =     0.01 s (a =        1 us) (n =  7119) (lvls, us =        1,        1,        1,        1,       21)
 *     S: JNI Roots            =     0.01 s (a =        1 us) (n =  7119) (lvls, us =        0,        1,        1,        1,       22)
 *     S: JNI Weak Roots       =     0.00 s (a =        0 us) (n =  7119) (lvls, us =        0,        0,        0,        0,       17)
 *     S: Synchronizer Roots   =     0.00 s (a =        0 us) (n =  7119) (lvls, us =        0,        0,        0,        0,       16)
 *     S: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7119) (lvls, us =        0,        0,        0,        0,       20)
 *     S: Management Roots     =     0.00 s (a =        1 us) (n =  7119) (lvls, us =        0,        0,        0,        1,       18)
 *     S: System Dict Roots    =     0.03 s (a =        4 us) (n =  7119) (lvls, us =        2,        3,        4,        4,       40)
 *     S: CLDG Roots           =     0.50 s (a =       71 us) (n =  7119) (lvls, us =       34,       57,       67,       82,      256)
 *     S: JVMTI Roots          =     0.00 s (a =        0 us) (n =  7119) (lvls, us =        0,        0,        0,        0,       17)
 *   Resize TLABs              =     0.00 s (a =        1 us) (n =  7119) (lvls, us =        0,        1,        1,        1,       42)
 * Pause Final Mark (G)        =     6.32 s (a =      888 us) (n =  7118) (lvls, us =      322,      666,      801,      953,    61879)
 * Pause Final Mark (N)        =     4.74 s (a =      665 us) (n =  7118) (lvls, us =      234,      506,      615,      721,    44200)
 *   Update Roots              =     1.50 s (a =      250 us) (n =  6000) (lvls, us =      158,      201,      242,      273,     9842)
 *     U: Thread Roots         =     0.07 s (a =       11 us) (n =  6000) (lvls, us =        6,        9,        9,       11,      188)
 *     U: String Table Roots   =     0.75 s (a =      125 us) (n =  6000) (lvls, us =       58,      115,      127,      131,      218)
 *     U: Universe Roots       =     0.00 s (a =        1 us) (n =  6000) (lvls, us =        0,        1,        1,        1,       21)
 *     U: JNI Roots            =     0.00 s (a =        1 us) (n =  6000) (lvls, us =        0,        1,        1,        1,       21)
 *     U: JNI Weak Roots       =     0.00 s (a =        0 us) (n =  6000) (lvls, us =        0,        0,        0,        0,       33)
 *     U: Synchronizer Roots   =     0.00 s (a =        0 us) (n =  6000) (lvls, us =        0,        0,        0,        0,       14)
 *     U: FlatProfiler Roots   =     0.00 s (a =        1 us) (n =  6000) (lvls, us =        0,        0,        0,        0,       32)
 *     U: Management Roots     =     0.01 s (a =        1 us) (n =  6000) (lvls, us =        0,        1,        1,        1,       21)
 *     U: System Dict Roots    =     0.03 s (a =        4 us) (n =  6000) (lvls, us =        3,        3,        4,        5,      265)
 *     U: CLDG Roots           =     0.26 s (a =       43 us) (n =  6000) (lvls, us =       25,       36,       40,       50,       83)
 *     U: JVMTI Roots          =     0.00 s (a =        0 us) (n =  6000) (lvls, us =        0,        0,        0,        0,       34)
 *   Finish Queues             =     0.93 s (a =      131 us) (n =  7118) (lvls, us =       18,       38,       72,      129,    43729)
 *   Weak References           =     0.12 s (a =       87 us) (n =  1424) (lvls, us =       20,       25,       80,      104,      936)
 *     Process                 =     0.01 s (a =        4 us) (n =  1424) (lvls, us =        2,        3,        3,        3,      246)
 *     Enqueue                 =     0.12 s (a =       81 us) (n =  1424) (lvls, us =       14,       20,       75,       99,      929)
 *   Complete Liveness         =     0.03 s (a =        4 us) (n =  7118) (lvls, us =        3,        4,        4,        4,       40)
 *   Retire TLABs              =     0.01 s (a =        1 us) (n =  7118) (lvls, us =        0,        1,        1,        1,       32)
 *   Sync Pinned               =     0.01 s (a =        1 us) (n =  7118) (lvls, us =        1,        1,        1,        1,       32)
 *   Trash CSet                =     0.01 s (a =        1 us) (n =  7118) (lvls, us =        0,        1,        1,        1,       35)
 *   Prepare Evacuation        =     0.23 s (a =       33 us) (n =  7118) (lvls, us =       10,       26,       29,       41,      101)
 *   Initial Evacuation        =     1.69 s (a =      237 us) (n =  7118) (lvls, us =      154,      197,      225,      260,     2808)
 *     E: Thread Roots         =     0.06 s (a =        8 us) (n =  7118) (lvls, us =        3,        6,        7,        9,      205)
 *     E: Code Cache Roots     =     0.14 s (a =       20 us) (n =  7118) (lvls, us =        4,       14,       17,       22,      100)
 *     E: String Table Roots   =     0.83 s (a =      116 us) (n =  7118) (lvls, us =       61,      107,      115,      121,      514)
 *     E: Universe Roots       =     0.00 s (a =        1 us) (n =  7118) (lvls, us =        0,        1,        1,        1,       32)
 *     E: JNI Roots            =     0.00 s (a =        1 us) (n =  7118) (lvls, us =        0,        0,        1,        1,       32)
 *     E: JNI Weak Roots       =     0.00 s (a =        0 us) (n =  7118) (lvls, us =        0,        0,        0,        0,       16)
 *     E: Synchronizer Roots   =     0.00 s (a =        0 us) (n =  7118) (lvls, us =        0,        0,        0,        0,       17)
 *     E: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7118) (lvls, us =        0,        0,        0,        0,       16)
 *     E: Management Roots     =     0.00 s (a =        1 us) (n =  7118) (lvls, us =        0,        0,        0,        1,       31)
 *     E: System Dict Roots    =     0.03 s (a =        4 us) (n =  7118) (lvls, us =        2,        3,        4,        4,       42)
 *     E: CLDG Roots           =     0.21 s (a =       30 us) (n =  7118) (lvls, us =        8,       23,       28,       36,      199)
 *     E: JVMTI Roots          =     0.00 s (a =        0 us) (n =  7118) (lvls, us =        0,        0,        0,        0,       31)
 * Pause Final Evac (G)        =     0.59 s (a =       99 us) (n =  6001) (lvls, us =       43,       63,       81,      109,     9180)
 * Pause Final Evac (N)        =     0.09 s (a =       15 us) (n =  6001) (lvls, us =       10,       12,       13,       16,       66)
 * Pause Init  Update Refs (G) =     0.15 s (a =      138 us) (n =  1117) (lvls, us =       44,       64,       85,      113,    10456)
 * Pause Init  Update Refs (N) =     0.02 s (a =       19 us) (n =  1117) (lvls, us =       12,       15,       16,       20,       70)
 *   Prepare                   =     0.00 s (a =        3 us) (n =  1117) (lvls, us =        1,        2,        2,        2,       21)
 * Pause Final Update Refs (G) =     0.48 s (a =      425 us) (n =  1117) (lvls, us =      227,      287,      328,      381,    18768)
 * Pause Final Update Refs (N) =     0.30 s (a =      265 us) (n =  1117) (lvls, us =      170,      205,      229,      266,    11360)
 *   Update Roots              =     0.26 s (a =      232 us) (n =  1117) (lvls, us =      146,      178,      197,      232,    11288)
 *     UR: Thread Roots        =     0.01 s (a =       11 us) (n =  1117) (lvls, us =        5,        8,        9,       10,      307)
 *     UR: String Table Roots  =     0.13 s (a =      115 us) (n =  1117) (lvls, us =       57,      104,      115,      121,      172)
 *     UR: Universe Roots      =     0.00 s (a =        1 us) (n =  1117) (lvls, us =        0,        0,        0,        1,        4)
 *     UR: JNI Roots           =     0.00 s (a =        1 us) (n =  1117) (lvls, us =        0,        1,        1,        1,       20)
 *     UR: JNI Weak Roots      =     0.00 s (a =        0 us) (n =  1117) (lvls, us =        0,        0,        0,        0,        1)
 *     UR: Synchronizer Roots  =     0.00 s (a =        0 us) (n =  1117) (lvls, us =        0,        0,        0,        0,        8)
 *     UR: FlatProfiler Roots  =     0.00 s (a =        1 us) (n =  1117) (lvls, us =        0,        0,        0,        0,       13)
 *     UR: Management Roots    =     0.00 s (a =        1 us) (n =  1117) (lvls, us =        0,        1,        1,        1,       20)
 *     UR: System Dict Roots   =     0.00 s (a =        4 us) (n =  1117) (lvls, us =        3,        3,        3,        4,       24)
 *     UR: CLDG Roots          =     0.04 s (a =       32 us) (n =  1117) (lvls, us =       14,       26,       29,       34,       95)
 *     UR: JVMTI Roots         =     0.00 s (a =        0 us) (n =  1117) (lvls, us =        0,        0,        0,        0,        1)
 *   Sync Pinned               =     0.00 s (a =        3 us) (n =  1117) (lvls, us =        1,        2,        2,        3,       22)
 *   Trash CSet                =     0.00 s (a =        1 us) (n =  1117) (lvls, us =        1,        1,        1,        1,       18)
 * Concurrent Reset            =     1.41 s (a =      198 us) (n =  7119) (lvls, us =       88,      125,      146,      188,    77277)
 * Concurrent Marking          =   147.68 s (a =    20748 us) (n =  7118) (lvls, us =     1777,    16211,    22461,    25586,    89676)
 * Concurrent Precleaning      =     0.17 s (a =      116 us) (n =  1424) (lvls, us =       34,       56,       97,      131,     9348)
 * Concurrent Evacuation       =     1.00 s (a =      141 us) (n =  7118) (lvls, us =       24,       59,       91,      133,    16873)
 * Concurrent Update Refs      =     6.16 s (a =     5515 us) (n =  1117) (lvls, us =     1445,     3418,     5234,     6836,    20421)
 * Concurrent Cleanup          =     0.33 s (a =       41 us) (n =  8235) (lvls, us =       25,       33,       38,       45,      192)
 * 
 * 
 * Under allocation pressure, concurrent cycles may cancel, and either continue cycle
 * under stop-the-world pause or result in stop-the-world Full GC. Increase heap size,
 * tune GC heuristics, set more aggressive pacing delay, or lower allocation rate
 * to avoid Degenerated and Full GC cycles.
 * 
 *  7118 successful concurrent GCs
 *       0 invoked explicitly
 *       0 invoked implicitly
 * 
 *     0 Degenerated GCs
 *       0 caused by allocation failure
 *       0 upgraded to Full GC
 * 
 *     0 Full GCs
 *       0 invoked explicitly
 *       0 invoked implicitly
 *       0 caused by allocation failure
 *       0 upgraded from Degenerated GC
 * 
 * 
 * ALLOCATION PACING:
 * 
 * Max pacing delay is set for 10 ms.
 * 
 * Higher delay would prevent application outpacing the GC, but it will hide the GC latencies
 * from the STW pause times. Pacing affects the individual threads, and so it would also be
 * invisible to the usual profiling tools, but would add up to end-to-end application latency.
 * Raise max pacing delay with care.
 * 
 * Actual pacing delays histogram:
 * 
 *       From -         To         Count         Sum
 *       1 ms -       2 ms:         2674        1337 ms
 *       2 ms -       4 ms:         2047        2047 ms
 *       4 ms -       8 ms:         1969        3938 ms
 *       8 ms -      16 ms:        11145       44580 ms
 *      16 ms -      32 ms:          114         912 ms
 *      32 ms -      64 ms:            7         112 ms
 *                   Total:        17956       52926 ms
 * 
 * Pacing delays are measured from entering the pacing code till exiting it. Therefore,
 * observed pacing delays may be higher than the threshold when paced thread spent more
 * time in the pacing code. It usually happens when thread is de-scheduled while paced,
 * OS takes longer to unblock the thread, or JVM experiences an STW pause.
 * 
 * 
 * 
 *   Allocation tracing is disabled, use -XX:+ShenandoahAllocationTrace to enable.
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class FooterStatsEvent implements HeaderEvent, ThrowAwayEvent {

    /**
     * Regular expression for the header.
     */
    public static final String _REGEX_HEADER = "^GC STATISTICS:$";
    /**
     * Regular expression defining standard logging.
     */
    private static final String REGEX[] = {
            //
            _REGEX_HEADER,
            //
            "^  \"\\(G\\)\" \\(gross\\) pauses include VM time: time to notify and block threads, do the " + "pre-$",
            //
            "^        and post-safepoint housekeeping. Use (-XX:\\+PrintSafepointStatistics|"
                    + "-Xlog:safepoint\\+stats) to dissect\\.$",
            //
            "^  \"\\(N\\)\" \\(net\\) pauses are the times spent in the actual GC code\\.$",
            //
            "^  \"a\" is average time for each phase, look at levels to see if average makes sense\\.$",
            //
            "^  \"lvls\" are quantiles: .+$",
            //
            "^  All times are wall-clock times, except per-root-class counters, that are sum over$",
            //
            "^  all workers\\. Dividing the <total> over the root stage time estimates parallelism\\.$",
            //
            "^Total Pauses \\([G|N]\\)[ ]{1,}=.*$",
            //
            "^Pause (Init[ ]{0,1}|Final) (Mark|Update Refs|Evac) \\([G|N]\\)[ ]{1,}=.*$",
            //
            "^  Accumulate Stats[ ]{1,}=.*$",
            //
            "^  Make Parsable[ ]{1,}=.*$",
            //
            "^  (Clear|Complete) Liveness[ ]{1,}=.*$",
            //
            "^  ((Scan|(Degen )?Update) Roots|Finish Work)[ ]{1,}=.*$",
            //
            "^[ ]{1,}(CMR|CSR|CTR|CU|CWR|CWRF|DCU|DSM|DU|DWR|E|S|U|UR|WR|WRP): (<total>|CLDG Roots|"
                    + "Code Cache Cleaning |Code Cache Roots|FlatProfiler Roots|JFR Weak Roots|JNI Handles Roots|"
                    + "JNI Roots|JNI Weak Roots|JVMTI Roots|Management Roots|Parallel Mark|Resolved Table Roots|"
                    + "String Table Roots|Synchronizer Roots|System Dict Roots|Thread Roots|Universe Roots|Unlink CLDs|"
                    + "Unload Code Caches|VM Strong Roots|VM Weak Roots|Weak References)[ ]{1,}=.*$",
            //
            "^  (Resize|Retire|Sync|Trash) (CSet|GCLABs|Pinned|TLABs)[ ]{1,}=.*$",
            // 0 spaces
            "^Pacing[ ]{1,}=.*",
            // 2 spaces
            "^  (Choose Collection Set|Evacuation|Finish Mark|Finish Queues|Initial Evacuation|Manage GCLABs|"
                    + "Manage GC\\/TLABs|Prepare|Prepare Evacuation|Purge Unlinked|Rebuild Free Set|Recycle|Rendezvous|"
                    + "Roots|System Purge|Trash Collection Set|"
                    + "Update References|Update Region States|Unlink Stale|Weak References|Weak Roots)[ ]{1,}=.*$",
            // 4 spaces: acronyms
            "^    (E|S): .+$",
            // 4 spaces: words
            "^    (CLDG|Cleanup|Code Roots|Deallocate Metadata|Process|Enqueue|Exception Caches|Parallel Cleanup|"
                    + "System Dictionary|System Purge|Unload Classes|Weak Class Links|Weak Roots)" + "[ ]{1,}=.*$",
            // 6 spaces
            "^      Weak References[ ]{1,}=.*$",
            //
            "^Concurrent (Reset|Marking|Precleaning|Evacuation|Update Refs|Cleanup|Uncommit)[ ]{1,}=.*$",
            //
            "^Under allocation pressure, concurrent cycles may cancel, and either continue cycle$",
            //
            "^under stop-the-world pause or result in stop-the-world Full GC. Increase heap size,$",
            //
            "^tune GC heuristics, set more aggressive pacing delay, or lower allocation rate$",
            //
            "^to avoid Degenerated and Full GC cycles.$",
            //
            "^[ ]{1,7}\\d{1,7} (successful concurrent|Degenerated|Full|upgraded to Full) GC(s)?$",
            //
            "^      \\d{1,7} invoked (ex|im)plicitly$",
            //
            "^      \\d{1,7} caused by allocation failure$",
            //
            "^        \\d{1,7} happened at (Mark|Outside of Cycle|Update References|Update Refs)$",
            //
            "^      \\d{1,7} upgraded from Degenerated GC$",
            //
            "^ALLOCATION PACING:$",
            //
            "^Max pacing delay is set for 10 ms.$",
            //
            "^[ ]{0,3}Higher delay would prevent application outpacing the GC, but it will hide the GC " + "latencies$",
            //
            "^[ ]{0,3}from the STW pause times. Pacing affects the individual threads, and so it would also " + "be$",
            //
            "^[ ]{0,3}invisible to the usual profiling tools, but would add up to end-to-end application "
                    + "latency.$",
            //
            "^[ ]{0,3}Raise max pacing delay with care.$",
            //
            "^Actual pacing delays histogram:$",
            //
            "^      From -         To         Count         Sum$",
            //
            "^[ ]{5,7}\\d{1,2} ms -[ ]{6,7}\\d{1,2} ms:[ ]{8,12}\\d{1,5}[ ]{7,11}\\d{1,5} ms$",
            //
            "^                  Total:.*$",
            //
            "^  Allocation tracing is disabled, use -XX:\\+ShenandoahAllocationTrace to enable.$",
            //
            "^[ ]{0,3}Pacing delays are measured from entering the pacing code till exiting it. Therefore,$",
            //
            "^[ ]{0,3}observed pacing delays may be higher than the threshold when paced thread spent more$",
            //
            "^[ ]{0,3}time in the pacing code. It usually happens when thread is de-scheduled while paced,$",
            //
            "^[ ]{0,3}OS takes longer to unblock the thread, or JVM experiences an STW pause.$",
            //
            "^Pause Degenerated GC \\((G|N)\\)[ ]{1,}=.*$"
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
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public FooterStatsEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.FOOTER_STATS;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getTimestamp() {
        return timestamp;
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
