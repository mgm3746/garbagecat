/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * FOOTER_STATS
 * </p>
 * 
 * <p>
 * Stats information printed at the end of gc logging with unified detailed logging
 * (<code>-Xlog:gc*:file=&lt;file&gt;</code>).
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) JDK8 standard format:
 * </p>
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
 * <p>
 * 2) Unified standard format:
 * </p>
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
 * <p>
 * 3) Unified with <code>-Xlog:gc*:file=&lt;file&gt;:time,uptimemillis</code>:
 * </p>
 * 
 * <pre>
 * [2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:
 * [2019-02-05T15:10:08.998-0200][1357910ms]   "(G)" (gross) pauses include VM time: time to notify and block threads, do the pre-
 * [2019-02-05T15:10:08.998-0200][1357910ms]         and post-safepoint housekeeping. Use -XX:+PrintSafepointStatistics to dissect.
 * [2019-02-05T15:10:08.998-0200][1357910ms]   "(N)" (net) pauses are the times spent in the actual GC code.
 * [2019-02-05T15:10:08.998-0200][1357910ms]   "a" is average time for each phase, look at levels to see if average makes sense.
 * [2019-02-05T15:10:08.998-0200][1357910ms]   "lvls" are quantiles: 0% (minimum), 25%, 50% (median), 75%, 100% (maximum).
 * [2019-02-05T15:10:08.998-0200][1357910ms] 
 * [2019-02-05T15:10:08.998-0200][1357910ms] Total Pauses (G)            =     0.46 s (a =     3009 us) (n =   152) (lvls, us =      189,     1367,     2598,     4141,    14005)
 * [2019-02-05T15:10:08.998-0200][1357910ms] Total Pauses (N)            =     0.36 s (a =     2386 us) (n =   152) (lvls, us =       57,      916,     1973,     3203,    11509)
 * [2019-02-05T15:10:08.998-0200][1357910ms] Pause Init Mark (G)         =     0.29 s (a =     4199 us) (n =    68) (lvls, us =     1406,     2715,     3516,     4863,    13992)
 * [2019-02-05T15:10:08.998-0200][1357910ms] Pause Init Mark (N)         =     0.24 s (a =     3564 us) (n =    68) (lvls, us =     1172,     2266,     2949,     4160,    11502)
 * [2019-02-05T15:10:08.998-0200][1357910ms]   Accumulate Stats          =     0.00 s (a =       19 us) (n =    68) (lvls, us =        5,       12,       17,       23,       54)
 * [2019-02-05T15:10:08.998-0200][1357910ms]   Make Parsable             =     0.00 s (a =       19 us) (n =    68) (lvls, us =        5,       10,       14,       19,      158)
 * [2019-02-05T15:10:08.998-0200][1357910ms]   Clear Liveness            =     0.01 s (a =      190 us) (n =    68) (lvls, us =       73,      121,      166,      219,      738)
 * [2019-02-05T15:10:08.999-0200][1357911ms]   Scan Roots                =     0.21 s (a =     3059 us) (n =    68) (lvls, us =     1016,     1855,     2539,     3555,    10222)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: Thread Roots         =     0.01 s (a =      188 us) (n =    68) (lvls, us =       65,      102,      131,      170,     1230)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: String Table Roots   =     0.05 s (a =      705 us) (n =    68) (lvls, us =      242,      426,      576,      832,     1948)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: Universe Roots       =     0.00 s (a =        3 us) (n =    68) (lvls, us =        1,        2,        3,        4,       11)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: JNI Roots            =     0.00 s (a =        3 us) (n =    68) (lvls, us =        1,        2,        2,        3,       29)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: JNI Weak Roots       =     0.02 s (a =      232 us) (n =    68) (lvls, us =        0,      125,      219,      307,      684)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: Synchronizer Roots   =     0.00 s (a =        0 us) (n =    68) (lvls, us =        0,        0,        0,        0,        1)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: Management Roots     =     0.00 s (a =        3 us) (n =    68) (lvls, us =        1,        2,        2,        4,        7)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: System Dict Roots    =     0.00 s (a =       25 us) (n =    68) (lvls, us =        8,       14,       20,       28,      128)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: CLDG Roots           =     0.05 s (a =      792 us) (n =    68) (lvls, us =      299,      494,      641,      865,     2989)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     S: JVMTI Roots          =     0.00 s (a =        1 us) (n =    68) (lvls, us =        0,        1,        1,        1,        2)
 * [2019-02-05T15:10:08.999-0200][1357911ms]   Resize TLABs              =     0.00 s (a =       16 us) (n =    68) (lvls, us =        4,        9,       13,       17,      138)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Pause Final Mark (G)        =     0.14 s (a =     2125 us) (n =    68) (lvls, us =      420,     1016,     1465,     2617,     7942)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Pause Final Mark (N)        =     0.11 s (a =     1606 us) (n =    68) (lvls, us =      260,      613,     1191,     2109,     7431)
 * [2019-02-05T15:10:08.999-0200][1357911ms]   Finish Queues             =     0.05 s (a =      777 us) (n =    68) (lvls, us =       47,      154,      418,      926,     4835)
 * [2019-02-05T15:10:08.999-0200][1357911ms]   Weak References           =     0.01 s (a =      846 us) (n =    14) (lvls, us =      244,      410,      602,     1113,     1964)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     Process                 =     0.01 s (a =      840 us) (n =    14) (lvls, us =      240,      404,      598,     1113,     1953)
 * [2019-02-05T15:10:08.999-0200][1357911ms]   Complete Liveness         =     0.01 s (a =       96 us) (n =    68) (lvls, us =       24,       50,       69,      119,      431)
 * [2019-02-05T15:10:08.999-0200][1357911ms]   Prepare Evacuation        =     0.02 s (a =      240 us) (n =    68) (lvls, us =       91,      141,      197,      295,      881)
 * [2019-02-05T15:10:08.999-0200][1357911ms]   Initial Evacuation        =     0.01 s (a =      876 us) (n =     8) (lvls, us =      371,      418,      564,      895,     2090)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     E: Thread Roots         =     0.00 s (a =      350 us) (n =     8) (lvls, us =       41,       45,       88,      363,     1565)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     E: Code Cache Roots     =     0.00 s (a =      255 us) (n =     8) (lvls, us =       88,       93,      238,      357,      423)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Pause Init  Update Refs (G) =     0.01 s (a =      821 us) (n =     8) (lvls, us =      189,      193,      285,      803,     3564)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Pause Init  Update Refs (N) =     0.00 s (a =      115 us) (n =     8) (lvls, us =       56,       72,      111,      125,      210)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Pause Final Update Refs (G) =     0.02 s (a =     2569 us) (n =     8) (lvls, us =      771,      953,     1562,     2578,     5644)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Pause Final Update Refs (N) =     0.01 s (a =     1257 us) (n =     8) (lvls, us =      650,      803,     1133,     1328,     2061)
 * [2019-02-05T15:10:08.999-0200][1357911ms]   Update Roots              =     0.01 s (a =     1061 us) (n =     8) (lvls, us =      545,      691,      977,     1172,     1642)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: Thread Roots        =     0.00 s (a =       94 us) (n =     8) (lvls, us =       37,       50,       81,      123,      172)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: String Table Roots  =     0.00 s (a =      170 us) (n =     8) (lvls, us =      109,      109,      121,      197,      275)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: Universe Roots      =     0.00 s (a =        1 us) (n =     8) (lvls, us =        1,        1,        1,        1,        1)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: JNI Roots           =     0.00 s (a =        1 us) (n =     8) (lvls, us =        1,        1,        1,        1,        2)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: JNI Weak Roots      =     0.00 s (a =       79 us) (n =     8) (lvls, us =       52,       55,       58,       94,      141)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: Synchronizer Roots  =     0.00 s (a =        0 us) (n =     8) (lvls, us =        0,        0,        0,        0,        0)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: Management Roots    =     0.00 s (a =        2 us) (n =     8) (lvls, us =        1,        1,        1,        1,        5)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: System Dict Roots   =     0.00 s (a =        9 us) (n =     8) (lvls, us =        7,        7,        7,        9,       14)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: CLDG Roots          =     0.00 s (a =      277 us) (n =     8) (lvls, us =      141,      154,      264,      334,      492)
 * [2019-02-05T15:10:08.999-0200][1357911ms]     UR: JVMTI Roots         =     0.00 s (a =        1 us) (n =     8) (lvls, us =        0,        0,        0,        1,        1)
 * [2019-02-05T15:10:08.999-0200][1357911ms]   Recycle                   =     0.00 s (a =       78 us) (n =     8) (lvls, us =       52,       55,       64,       66,      178)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Concurrent Reset            =     0.12 s (a =     1790 us) (n =    68) (lvls, us =      258,      977,     1465,     1895,    18874)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Concurrent Marking          =     2.92 s (a =    42992 us) (n =    68) (lvls, us =    20703,    34766,    39062,    50195,    84271)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Concurrent Precleaning      =     0.03 s (a =     1941 us) (n =    14) (lvls, us =      607,      625,     1113,     1445,     8060)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Concurrent Evacuation       =     0.06 s (a =     7618 us) (n =     8) (lvls, us =       93,      119,      516,     5176,    27570)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Concurrent Update Refs      =     0.18 s (a =    22825 us) (n =     8) (lvls, us =    14062,    14258,    18164,    25391,    42718)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Concurrent Cleanup          =     0.05 s (a =      614 us) (n =    76) (lvls, us =      215,      371,      471,      699,     2993)
 * [2019-02-05T15:10:08.999-0200][1357911ms] Concurrent Uncommit         =     0.17 s (a =    34042 us) (n =     5) (lvls, us =     1445,     1445,     5391,     7812,    82719)
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:08.999-0200][1357911ms] Under allocation pressure, concurrent cycles may cancel, and either continue cycle
 * [2019-02-05T15:10:08.999-0200][1357911ms] under stop-the-world pause or result in stop-the-world Full GC. Increase heap size,
 * [2019-02-05T15:10:08.999-0200][1357911ms] tune GC heuristics, set more aggressive pacing delay, or lower allocation rate
 * [2019-02-05T15:10:08.999-0200][1357911ms] to avoid Degenerated and Full GC cycles.
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:08.999-0200][1357911ms]    68 successful concurrent GCs
 * [2019-02-05T15:10:08.999-0200][1357911ms]       0 invoked explicitly
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:08.999-0200][1357911ms]     0 Degenerated GCs
 * [2019-02-05T15:10:08.999-0200][1357911ms]       0 caused by allocation failure
 * [2019-02-05T15:10:08.999-0200][1357911ms]       0 upgraded to Full GC
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:08.999-0200][1357911ms]     0 Full GCs
 * [2019-02-05T15:10:08.999-0200][1357911ms]       0 invoked explicitly
 * [2019-02-05T15:10:08.999-0200][1357911ms]       0 caused by allocation failure
 * [2019-02-05T15:10:08.999-0200][1357911ms]       0 upgraded from Degenerated GC
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:08.999-0200][1357911ms] ALLOCATION PACING:
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:08.999-0200][1357911ms] Max pacing delay is set for 10 ms.
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:08.999-0200][1357911ms] Higher delay would prevent application outpacing the GC, but it will hide the GC latencies
 * [2019-02-05T15:10:08.999-0200][1357911ms] from the STW pause times. Pacing affects the individual threads, and so it would also be
 * [2019-02-05T15:10:08.999-0200][1357911ms] invisible to the usual profiling tools, but would add up to end-to-end application latency.
 * [2019-02-05T15:10:08.999-0200][1357911ms] Raise max pacing delay with care.
 * [2019-02-05T15:10:08.999-0200][1357911ms] 
 * [2019-02-05T15:10:09.000-0200][1357912ms] Actual pacing delays histogram:
 * [2019-02-05T15:10:09.000-0200][1357912ms] 
 * [2019-02-05T15:10:09.000-0200][1357912ms]       From -         To         Count         Sum
 * [2019-02-05T15:10:09.000-0200][1357912ms]       1 ms -       2 ms:           15           7 ms
 * [2019-02-05T15:10:09.000-0200][1357912ms]       2 ms -       4 ms:           11          11 ms
 * [2019-02-05T15:10:09.000-0200][1357912ms]       4 ms -       8 ms:           20          40 ms
 * [2019-02-05T15:10:09.000-0200][1357912ms]       8 ms -      16 ms:          172         688 ms
 * [2019-02-05T15:10:09.000-0200][1357912ms]      16 ms -      32 ms:           14         112 ms
 * [2019-02-05T15:10:09.000-0200][1357912ms]                   Total:          232         858 ms
 * [2019-02-05T15:10:09.000-0200][1357912ms] 
 * [2019-02-05T15:10:09.000-0200][1357912ms] 
 * [2019-02-05T15:10:09.000-0200][1357912ms] 
 * [2019-02-05T15:10:09.000-0200][1357912ms]   Allocation tracing is disabled, use -XX:+ShenandoahAllocationTrace to enable.
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class FooterStatsEvent implements ThrowAwayEvent {

    /**
     * Regular expression defining standard logging.
     */
    private static final String REGEX[] = {
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?GC STATISTICS:$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?  \"\\(G\\)\" \\(gross\\) pauses include VM time: time to notify and block threads, do the "
                    + "pre-$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?        and post-safepoint housekeeping. Use -XX:\\+PrintSafepointStatistics to dissect.$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?  \"\\(N\\)\" \\(net\\) pauses are the times spent in the actual GC code.$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?  \"a\" is average time for each phase, look at levels to see if average makes sense.$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?  \"lvls\" are quantiles: 0% \\(minimum\\), 25%, 50% \\(median\\), 75%, 100% "
                    + "\\(maximum\\).$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Total Pauses \\([G|N]\\)[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?Pause (Init[ ]{0,1}|Final) (Mark|Update Refs|Evac) \\([G|N]\\)[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?  Accumulate Stats[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?  Make Parsable[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?  (Clear|Complete) Liveness[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?  ((Scan|(Degen )?Update) Roots|Finish Work)[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?    (DU|E|S|U|UR): (CLDG|Code Cache|FlatProfiler|JNI|JNI Weak|Management|String Table|"
                    + "Synchronizer|System Dict|Thread|Universe|JVMTI) Roots[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?  (Resize|Retire|Sync|Trash) (CSet|GCLABs|Pinned|TLABs)[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?  Finish Queues[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?  (Weak References|System Purge)[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?    (Process|Enqueue|Parallel Cleanup)[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?  (Initial|Prepare)( Evacuation)?[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?  Recycle[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?Concurrent (Reset|Marking|Precleaning|Evacuation|Update Refs|Cleanup|Uncommit)[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?Under allocation pressure, concurrent cycles may cancel, and either continue cycle$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?under stop-the-world pause or result in stop-the-world Full GC. Increase heap size,$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?tune GC heuristics, set more aggressive pacing delay, or lower allocation rate$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?to avoid Degenerated and Full GC cycles.$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{1,7}\\d{1,7} (successful concurrent|Degenerated|Full|upgraded to Full) GC(s)?$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?      \\d{1,7} invoked (ex|im)plicitly$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?      \\d{1,7} caused by allocation failure$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?        \\d{1,7} happened at (Mark|Outside of Cycle)$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?      \\d{1,7} upgraded from Degenerated GC$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?ALLOCATION PACING:$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Max pacing delay is set for 10 ms.$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{0,3}Higher delay would prevent application outpacing the GC, but it will hide the GC "
                    + "latencies$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{0,3}from the STW pause times. Pacing affects the individual threads, and so it would also "
                    + "be$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{0,3}invisible to the usual profiling tools, but would add up to end-to-end application "
                    + "latency.$",
            //
            "^(" + UnifiedRegEx.DECORATOR + ")?[ ]{0,3}Raise max pacing delay with care.$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Actual pacing delays histogram:$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?      From -         To         Count         Sum$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{5,7}\\d{1,2} ms -[ ]{6,7}\\d{1,2} ms:[ ]{8,12}\\d{1,5}[ ]{7,11}\\d{1,5} ms$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?                  Total:.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + " )?  Allocation tracing is disabled, use -XX:\\+ShenandoahAllocationTrace to enable.$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{0,3}Pacing delays are measured from entering the pacing code till exiting it. Therefore,$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{0,3}observed pacing delays may be higher than the threshold when paced thread spent more$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{0,3}time in the pacing code. It usually happens when thread is de-scheduled while paced,$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{0,3}OS takes longer to unblock the thread, or JVM experiences an STW pause.$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Pause Degenerated GC \\((G|N)\\)[ ]{1,}=.*$",
            //
            "^(" + UnifiedRegEx.DECORATOR
                    + ")?[ ]{3,4}(Cleanup|CLDG|Deallocate Metadata|Enqueue|Parallel Cleanup|Process|Unload Classes|"
                    + "Weak Roots)[ ]{1,}=.*$"
            //
    };

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

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.FOOTER_STATS.toString();
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
        boolean match = false;
        for (int i = 0; i < REGEX.length; i++) {
            if (logLine.matches(REGEX[i])) {
                match = true;
                break;
            }
        }
        return match;
    }
}
