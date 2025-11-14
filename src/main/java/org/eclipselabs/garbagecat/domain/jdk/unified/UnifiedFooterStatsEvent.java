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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_FOOTER_STATS
 * </p>
 * 
 * <p>
 * Stats information printed at the end of gc logging with unified detailed logging
 * (<code>-Xlog:gc*:file=&lt;file&gt;</code>).
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * [69.946s][info][gc,stats     ] 
 * [69.946s][info][gc,stats     ] GC STATISTICS:
 * [69.946s][info][gc,stats     ]   "(G)" (gross) pauses include VM time: time to notify and block threads, do the pre-
 * [69.946s][info][gc,stats     ]         and post-safepoint housekeeping. Use -XX:+PrintSafepointStatistics to dissect.
 * [69.946s][info][gc,stats     ]   "(N)" (net) pauses are the times spent in the actual GC code.
 * [69.946s][info][gc,stats     ]   "a" is average time for each phase, look at levels to see if average makes sense.
 * [69.946s][info][gc,stats     ]   "lvls" are quantiles: 0% (minimum), 25%, 50% (median), 75%, 100% (maximum).
 * [69.946s][info][gc,stats     ] 
 * [69.946s][info][gc,stats     ] Total Pauses (G)            =     4.93 s (a =      597 us) (n =  8262) (lvls, us =       54,      191,      402,      570,    20803)
 * [69.946s][info][gc,stats     ] Total Pauses (N)            =     2.59 s (a =      314 us) (n =  8262) (lvls, us =       17,       45,      264,      359,    11100)
 * [69.946s][info][gc,stats     ] Pause Init Mark (G)         =     1.73 s (a =      670 us) (n =  2585) (lvls, us =      240,      395,      467,      641,     8696)
 * [69.946s][info][gc,stats     ] Pause Init Mark (N)         =     0.96 s (a =      371 us) (n =  2585) (lvls, us =      156,      268,      318,      402,     2847)
 * [69.946s][info][gc,stats     ]   Accumulate Stats          =     0.01 s (a =        2 us) (n =  2585) (lvls, us =        1,        2,        2,        2,       21)
 * [69.946s][info][gc,stats     ]   Make Parsable             =     0.01 s (a =        2 us) (n =  2585) (lvls, us =        1,        2,        2,        2,       19)
 * [69.947s][info][gc,stats     ]   Clear Liveness            =     0.02 s (a =        8 us) (n =  2585) (lvls, us =        3,        6,        7,        9,       42)
 * [69.947s][info][gc,stats     ]   Scan Roots                =     0.75 s (a =      288 us) (n =  2585) (lvls, us =      104,      205,      246,      303,     2658)
 * [69.947s][info][gc,stats     ]     S: Thread Roots         =     0.05 s (a =       18 us) (n =  2585) (lvls, us =        7,       13,       16,       19,      361)
 * [69.947s][info][gc,stats     ]     S: String Table Roots   =     0.28 s (a =      109 us) (n =  2585) (lvls, us =       46,       91,      109,      125,      369)
 * [69.947s][info][gc,stats     ]     S: Universe Roots       =     0.00 s (a =        2 us) (n =  2585) (lvls, us =        1,        1,        2,        2,       18)
 * [69.947s][info][gc,stats     ]     S: JNI Roots            =     0.00 s (a =        1 us) (n =  2585) (lvls, us =        0,        1,        1,        1,       40)
 * [69.947s][info][gc,stats     ]     S: JNI Weak Roots       =     0.04 s (a =       15 us) (n =  2585) (lvls, us =        0,        8,       15,       21,      121)
 * [69.947s][info][gc,stats     ]     S: Synchronizer Roots   =     0.00 s (a =        0 us) (n =  2585) (lvls, us =        0,        0,        0,        0,        8)
 * [69.947s][info][gc,stats     ]     S: Management Roots     =     0.00 s (a =        1 us) (n =  2585) (lvls, us =        0,        1,        1,        1,        6)
 * [69.947s][info][gc,stats     ]     S: System Dict Roots    =     0.01 s (a =        4 us) (n =  2585) (lvls, us =        2,        4,        4,        5,       66)
 * [69.947s][info][gc,stats     ]     S: CLDG Roots           =     0.17 s (a =       64 us) (n =  2585) (lvls, us =       21,       55,       65,       75,      165)
 * [69.947s][info][gc,stats     ]     S: JVMTI Roots          =     0.00 s (a =        1 us) (n =  2585) (lvls, us =        0,        0,        0,        1,       17)
 * [69.947s][info][gc,stats     ]   Resize TLABs              =     0.00 s (a =        1 us) (n =  2585) (lvls, us =        0,        1,        1,        1,       18)
 * [69.947s][info][gc,stats     ] Pause Final Mark (G)        =     2.11 s (a =      815 us) (n =  2585) (lvls, us =      215,      385,      473,      803,    19215)
 * [69.947s][info][gc,stats     ] Pause Final Mark (N)        =     1.42 s (a =      550 us) (n =  2585) (lvls, us =      145,      275,      340,      490,    11088)
 * [69.947s][info][gc,stats     ]   Update Roots              =     0.24 s (a =      116 us) (n =  2078) (lvls, us =       54,       79,       95,      119,     2199)
 * [69.947s][info][gc,stats     ]     U: Thread Roots         =     0.03 s (a =       16 us) (n =  2078) (lvls, us =        8,       12,       14,       17,      190)
 * [69.947s][info][gc,stats     ]     U: String Table Roots   =     0.05 s (a =       25 us) (n =  2078) (lvls, us =       16,       21,       25,       28,      134)
 * [69.947s][info][gc,stats     ]     U: Universe Roots       =     0.00 s (a =        1 us) (n =  2078) (lvls, us =        0,        1,        1,        1,       54)
 * [69.947s][info][gc,stats     ]     U: JNI Roots            =     0.00 s (a =        1 us) (n =  2078) (lvls, us =        0,        1,        1,        1,       17)
 * [69.947s][info][gc,stats     ]     U: JNI Weak Roots       =     0.01 s (a =        5 us) (n =  2078) (lvls, us =        2,        4,        5,        5,       34)
 * [69.947s][info][gc,stats     ]     U: Synchronizer Roots   =     0.00 s (a =        0 us) (n =  2078) (lvls, us =        0,        0,        0,        0,        5)
 * [69.947s][info][gc,stats     ]     U: Management Roots     =     0.00 s (a =        1 us) (n =  2078) (lvls, us =        1,        1,        1,        1,       17)
 * [69.947s][info][gc,stats     ]     U: System Dict Roots    =     0.01 s (a =        3 us) (n =  2078) (lvls, us =        2,        3,        3,        3,       19)
 * [69.947s][info][gc,stats     ]     U: CLDG Roots           =     0.03 s (a =       12 us) (n =  2078) (lvls, us =        7,       10,       12,       13,       45)
 * [69.947s][info][gc,stats     ]     U: JVMTI Roots          =     0.00 s (a =        1 us) (n =  2078) (lvls, us =        0,        0,        0,        1,        8)
 * [69.947s][info][gc,stats     ]   Finish Queues             =     0.60 s (a =      233 us) (n =  2585) (lvls, us =       15,       28,       40,       85,    10140)
 * [69.947s][info][gc,stats     ]   Weak References           =     0.02 s (a =       38 us) (n =   517) (lvls, us =       17,       28,       32,       40,      566)
 * [69.947s][info][gc,stats     ]     Process                 =     0.02 s (a =       34 us) (n =   517) (lvls, us =       14,       25,       29,       36,      560)
 * [69.947s][info][gc,stats     ]   Complete Liveness         =     0.01 s (a =        5 us) (n =  2585) (lvls, us =        3,        4,        5,        6,       36)
 * [69.947s][info][gc,stats     ]   Prepare Evacuation        =     0.19 s (a =       74 us) (n =  2585) (lvls, us =       37,       57,       65,       84,      307)
 * [69.947s][info][gc,stats     ]   Initial Evacuation        =     0.19 s (a =       72 us) (n =  2585) (lvls, us =       29,       47,       57,       76,     3142)
 * [69.947s][info][gc,stats     ]     E: Thread Roots         =     0.03 s (a =       11 us) (n =  2585) (lvls, us =        4,        8,       10,       13,      204)
 * [69.947s][info][gc,stats     ]     E: Code Cache Roots     =     0.05 s (a =       20 us) (n =  2585) (lvls, us =        8,       15,       19,       22,      113)
 * [69.947s][info][gc,stats     ] Pause Final Evac (G)        =     0.56 s (a =      272 us) (n =  2078) (lvls, us =       54,       94,      119,      166,    20802)
 * [69.947s][info][gc,stats     ] Pause Final Evac (N)        =     0.07 s (a =       34 us) (n =  2078) (lvls, us =       17,       24,       31,       39,      901)
 * [69.947s][info][gc,stats     ] Pause Init  Update Refs (G) =     0.19 s (a =      378 us) (n =   507) (lvls, us =       68,      105,      135,      186,    17083)
 * [69.947s][info][gc,stats     ] Pause Init  Update Refs (N) =     0.02 s (a =       45 us) (n =   507) (lvls, us =       23,       34,       41,       52,      140)
 * [69.947s][info][gc,stats     ] Pause Final Update Refs (G) =     0.33 s (a =      650 us) (n =   507) (lvls, us =      168,      279,      359,      600,     9705)
 * [69.947s][info][gc,stats     ] Pause Final Update Refs (N) =     0.11 s (a =      224 us) (n =   507) (lvls, us =       95,      146,      174,      227,     2427)
 * [69.947s][info][gc,stats     ]   Update Roots              =     0.08 s (a =      150 us) (n =   507) (lvls, us =       57,       85,      104,      148,     2339)
 * [69.947s][info][gc,stats     ]     UR: Thread Roots        =     0.01 s (a =       19 us) (n =   507) (lvls, us =        7,       13,       15,       18,      346)
 * [69.947s][info][gc,stats     ]     UR: String Table Roots  =     0.01 s (a =       26 us) (n =   507) (lvls, us =       17,       21,       25,       28,       79)
 * [69.947s][info][gc,stats     ]     UR: Universe Roots      =     0.00 s (a =        1 us) (n =   507) (lvls, us =        1,        1,        1,        1,        2)
 * [69.947s][info][gc,stats     ]     UR: JNI Roots           =     0.00 s (a =        1 us) (n =   507) (lvls, us =        0,        1,        1,        1,        2)
 * [69.947s][info][gc,stats     ]     UR: JNI Weak Roots      =     0.00 s (a =        5 us) (n =   507) (lvls, us =        2,        4,        5,        6,       21)
 * [69.947s][info][gc,stats     ]     UR: Synchronizer Roots  =     0.00 s (a =        0 us) (n =   507) (lvls, us =        0,        0,        0,        0,        0)
 * [69.947s][info][gc,stats     ]     UR: Management Roots    =     0.00 s (a =        1 us) (n =   507) (lvls, us =        1,        1,        1,        1,        3)
 * [69.947s][info][gc,stats     ]     UR: System Dict Roots   =     0.00 s (a =        3 us) (n =   507) (lvls, us =        2,        3,        3,        4,       19)
 * [69.947s][info][gc,stats     ]     UR: CLDG Roots          =     0.01 s (a =       13 us) (n =   507) (lvls, us =        7,       10,       12,       14,       31)
 * [69.947s][info][gc,stats     ]     UR: JVMTI Roots         =     0.00 s (a =        1 us) (n =   507) (lvls, us =        0,        0,        0,        1,        2)
 * [69.947s][info][gc,stats     ]   Recycle                   =     0.01 s (a =       11 us) (n =   507) (lvls, us =        6,        9,       10,       13,       40)
 * [69.947s][info][gc,stats     ] Concurrent Reset            =     0.64 s (a =      247 us) (n =  2585) (lvls, us =       92,      131,      158,      213,     6384)
 * [69.947s][info][gc,stats     ] Concurrent Marking          =    35.61 s (a =    13775 us) (n =  2585) (lvls, us =     2070,    10156,    13672,    17188,    48290)
 * [69.947s][info][gc,stats     ] Concurrent Precleaning      =     0.16 s (a =      305 us) (n =   517) (lvls, us =       37,       95,      182,      240,    14159)
 * [69.947s][info][gc,stats     ] Concurrent Evacuation       =     0.46 s (a =      177 us) (n =  2585) (lvls, us =       46,       85,      109,      148,     5772)
 * [69.948s][info][gc,stats     ] Concurrent Update Refs      =     2.97 s (a =     5851 us) (n =   507) (lvls, us =     1875,     3711,     5137,     7148,    31206)
 * [69.948s][info][gc,stats     ] Concurrent Cleanup          =     0.32 s (a =       63 us) (n =  5170) (lvls, us =       30,       50,       60,       71,      231)
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ] Under allocation pressure, concurrent cycles may cancel, and either continue cycle
 * [69.948s][info][gc,stats     ] under stop-the-world pause or result in stop-the-world Full GC. Increase heap size,
 * [69.948s][info][gc,stats     ] tune GC heuristics, set more aggressive pacing delay, or lower allocation rate
 * [69.948s][info][gc,stats     ] to avoid Degenerated and Full GC cycles.
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ]  2585 successful concurrent GCs
 * [69.948s][info][gc,stats     ]       0 invoked explicitly
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ]     0 Degenerated GCs
 * [69.948s][info][gc,stats     ]       0 caused by allocation failure
 * [69.948s][info][gc,stats     ]       0 upgraded to Full GC
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ]     0 Full GCs
 * [69.948s][info][gc,stats     ]       0 invoked explicitly
 * [69.948s][info][gc,stats     ]       0 caused by allocation failure
 * [69.948s][info][gc,stats     ]       0 upgraded from Degenerated GC
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ] ALLOCATION PACING:
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ] Max pacing delay is set for 10 ms.
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ] Higher delay would prevent application outpacing the GC, but it will hide the GC latencies
 * [69.948s][info][gc,stats     ] from the STW pause times. Pacing affects the individual threads, and so it would also be
 * [69.948s][info][gc,stats     ] invisible to the usual profiling tools, but would add up to end-to-end application latency.
 * [69.948s][info][gc,stats     ] Raise max pacing delay with care.
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ] Actual pacing delays histogram:
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ]       From -         To         Count         Sum
 * [69.948s][info][gc,stats     ]       1 ms -       2 ms:         2185        1092 ms
 * [69.948s][info][gc,stats     ]       2 ms -       4 ms:          572         572 ms
 * [69.948s][info][gc,stats     ]       4 ms -       8 ms:          859        1718 ms
 * [69.948s][info][gc,stats     ]       8 ms -      16 ms:         2390        9560 ms
 * [69.948s][info][gc,stats     ]      16 ms -      32 ms:           68         544 ms
 * [69.948s][info][gc,stats     ]      32 ms -      64 ms:            1          16 ms
 * [69.948s][info][gc,stats     ]                   Total:         6075       13502 ms
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ] 
 * [69.948s][info][gc,stats     ]   Allocation tracing is disabled, use -XX:+ShenandoahAllocationTrace to enable.
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedFooterStatsEvent implements UnifiedLogging, HeaderEvent, ThrowAwayEvent {

    /**
     * Regular expression for percent with 2 decimal places.
     */
    public static final String REGEX_PERCENT = "\\d{1,3}\\.\\d{2}%";

    /**
     * Regular expression for the header.
     */
    public static final String _REGEX_HEADER = "^" + UnifiedRegEx.DECORATOR + " GC STATISTICS:$";
    /**
     * Regular expression defining standard logging.
     */
    private static final String REGEX[] = {
            //
            _REGEX_HEADER,
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "   \"\\(G\\)\" \\(gross\\) pauses include VM time: time to notify and block threads, do the "
                    + "pre-$",
            // F
            "^" + UnifiedRegEx.DECORATOR
                    + "         and post-safepoint housekeeping. Use (-XX:\\+PrintSafepointStatistics|"
                    + "-Xlog:safepoint\\+stats) to dissect\\.$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "   \"\\(N\\)\" \\(net\\) pauses are the times spent in the actual GC code\\.$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "   \"a\" is average time for each phase, look at levels to see if average makes sense\\.$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   \"lvls\" are quantiles: .+$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "   All times are wall-clock times, except per-root-class counters, that are sum over$",
            "^" + UnifiedRegEx.DECORATOR
            //
                    + "   all workers\\. Dividing the <total> over the root stage time estimates parallelism\\.$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Total Pauses \\([G|N]\\)[ ]{1,}=.*$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Pause (Init[ ]{0,1}|Final) (Mark|Update Refs|Evac) \\([G|N]\\)[ ]{1,}=.*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   Accumulate Stats[ ]{1,}=.*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   Make Parsable[ ]{1,}=.*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   (Clear|Complete) Liveness[ ]{1,}=.*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   ((Scan|(Degen )?Update) Roots|Finish Work)[ ]{1,}=.*$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " [ ]{1,}(CMR|CSR|CTR|CU|CWR|CWRF|DCU|DSM|DU|DWR|E|S|U|UR|WR|WRP): (<total>|CLDG Roots|"
                    + "Code Cache Cleaning |Code Cache Roots|FlatProfiler Roots|JFR Weak Roots|JNI Handles Roots|"
                    + "JNI Roots|JNI Weak Roots|JVMTI Roots|Management Roots|Parallel Mark|Resolved Table Roots|"
                    + "String Table Roots|Synchronizer Roots|System Dict Roots|Thread Roots|Universe Roots|Unlink CLDs|"
                    + "Unload Code Caches|VM Strong Roots|VM Weak Roots|Weak References)[ ]{1,}=.*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   (Resize|Retire|Sync|Trash) (CSet|GCLABs|Pinned|TLABs)[ ]{1,}=.*$",
            // 1 space
            "^" + UnifiedRegEx.DECORATOR
                    + " (Concurrent (Class Unloading|Cleanup|Evacuation|Mark Roots|Marking|Precleaning|Reset|"
                    + "Reset After Collect|Strong Roots|Thread Roots|Uncommit|Update Refs|Update Refs Prepare|"
                    + "Update Thread Roots|Weak References|Weak Roots)|Pacing)[ ]{1,}=.*",
            // 3 spaces
            "^" + UnifiedRegEx.DECORATOR
                    + "   (Choose Collection Set|Cleanup|CM: (<total>|Parallel Mark)|Degen STW Mark|Evacuation|"
                    + "Finish Mark|Finish Queues|Flush SATB|Initial Evacuation|Manage GCLABs|Manage GC\\/TLABs|"
                    + "Prepare|Prepare Evacuation|Propagate GC State|Purge Unlinked|Rebuild Free Set|Recycle|"
                    + "Rendezvous|Roots|System Purge|Trash Collection Set|Update References|Update Region States|"
                    + "Unlink Stale|Weak References|Weak Roots)[ ]{1,}=.*$",
            // 5 spaces
            "^" + UnifiedRegEx.DECORATOR
                    + "     (CLDG|Cleanup|Code Roots|Deallocate Metadata|Process|Enqueue|Exception Caches|"
                    + "Parallel Cleanup|System Dictionary|System Purge|Unload Classes|Weak Class Links|Weak Roots)"
                    + "[ ]{1,}=.*$",
            // 7 spaces
            "^" + UnifiedRegEx.DECORATOR + "       (CLDG|Unload Classes|Weak References|Weak Roots)[ ]{1,}=.*$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " Under allocation pressure, concurrent cycles may cancel, and either continue cycle$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " under stop-the-world pause or result in stop-the-world Full GC. Increase heap size,$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " tune GC heuristics, set more aggressive pacing delay, or lower allocation rate$",
            //
            "^" + UnifiedRegEx.DECORATOR + " to avoid Degenerated and Full GC cycles.( Abbreviated cycles are those "
                    + "which found)?$",
            // Line2 for long form above^^^
            "^" + UnifiedRegEx.DECORATOR + " enough regions with no live objects to skip evacuation.$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{1,7}\\d{1,7} (Completed|Degenerated|Full|[sS]uccessful [cC]oncurrent|upgraded to Full) "
                    + "GC(s)?( \\(" + REGEX_PERCENT + "\\))?$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{1,8}\\d{1,8} (abbreviated|caused by allocation failure|caused by Concurrent GC|"
                    + "invoked explicitly|invoked implicitly|upgraded from Degenerated GC)( \\(" + REGEX_PERCENT
                    + "\\))?$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "         \\d{1,7} happened at (Evacuation|Mark|Outside of Cycle|Update References|Update Refs)$",
            //
            "^" + UnifiedRegEx.DECORATOR + "       \\d{1,7} upgraded from Degenerated GC$",
            //
            "^" + UnifiedRegEx.DECORATOR + " ALLOCATION PACING:$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Max pacing delay is set for 10 ms.$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{0,3}Higher delay would prevent application outpacing the GC, but it will hide the GC "
                    + "latencies$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{0,3}from the STW pause times. Pacing affects the individual threads, and so it would also "
                    + "be$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{0,3}invisible to the usual profiling tools, but would add up to end-to-end application "
                    + "latency.$",
            //
            "^" + UnifiedRegEx.DECORATOR + "[ ]{0,3}Raise max pacing delay with care.$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Actual pacing delays histogram:$",
            //
            "^" + UnifiedRegEx.DECORATOR + "       From -         To         Count         Sum$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{5,7}\\d{1,2} ms -[ ]{6,7}\\d{1,2} ms:[ ]{8,12}\\d{1,5}[ ]{7,11}\\d{1,5} ms$",
            //
            "^" + UnifiedRegEx.DECORATOR + "                   Total:.*$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "   Allocation tracing is disabled, use -XX:\\+ShenandoahAllocationTrace to enable.$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{0,3}Pacing delays are measured from entering the pacing code till exiting it. Therefore,$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{0,3}observed pacing delays may be higher than the threshold when paced thread spent more$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{0,3}time in the pacing code. It usually happens when thread is de-scheduled while paced,$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{0,3}OS takes longer to unblock the thread, or JVM experiences an STW pause.$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Pause Degenerated GC \\((G|N)\\)[ ]{1,}=.*$"
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
    public UnifiedFooterStatsEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.UNIFIED_FOOTER_STATS;
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

    @Override
    public boolean isHeader() {
        boolean isHeader = false;
        if (this.logEntry != null) {
            isHeader = logEntry.matches(_REGEX_HEADER);
        }
        return isHeader;
    }
}