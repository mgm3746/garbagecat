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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.HeaderEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.jdk.UnknownCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * Z_STATS
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.unified.ZCollector} output at the end of gc logging and periodically
 * based on {@link org.github.joa.JvmOptions#getzStatisticsInterval()} when diagnostics are enabled
 * (<code>-XX:+UnlockDiagnosticVMOptions</code>).
 * </p>
 * 
 * <p>
 * Periodic logging looks exactly the same as footer logging.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Non-generational:
 * </p>
 * 
 * <pre>
 * [10.485s] === Garbage Collection Statistics =======================================================================================================================
 * [10.485s]                                                              Last 10s              Last 10m              Last 10h                Total
 * [10.485s]                                                              Avg / Max             Avg / Max             Avg / Max             Avg / Max
 * [10.485s]   Collector: Garbage Collection Cycle                     41.688 / 41.688       41.688 / 41.688       41.688 / 41.688       41.688 / 41.688      ms
 * [10.485s]  Contention: Mark Segment Reset Contention                     0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [10.485s]  Contention: Mark SeqNum Reset Contention                      0 / 1                 0 / 1                 0 / 1                 0 / 1           ops/s
 * [10.485s]    Critical: Allocation Stall                                  0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [10.485s]    Critical: Allocation Stall                              0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [10.485s]    Critical: GC Locker Stall                                   0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [10.485s]    Critical: GC Locker Stall                               0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [10.485s]    Critical: Relocation Stall                                  0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [10.485s]    Critical: Relocation Stall                              0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [10.485s]      Memory: Allocation Rate                                  19 / 78               19 / 78               19 / 78               19 / 78          MB/s
 * [10.485s]      Memory: Out Of Memory                                     0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [10.485s]      Memory: Page Cache Flush                                  0 / 0                 0 / 0                 0 / 0                 0 / 0           MB/s
 * [10.485s]      Memory: Page Cache Hit L1                                 3 / 26                3 / 26                3 / 26                3 / 26          ops/s
 * [10.485s]      Memory: Page Cache Hit L2                                 0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [10.485s]      Memory: Page Cache Hit L3                                 6 / 39                6 / 39                6 / 39                6 / 39          ops/s
 * [10.485s]      Memory: Page Cache Miss                                   0 / 1                 0 / 1                 0 / 1                 0 / 1           ops/s
 * [10.485s]      Memory: Uncommit                                          0 / 0                 0 / 0                 0 / 0                 0 / 0           MB/s
 * [10.485s]      Memory: Undo Object Allocation Failed                     0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [10.485s]      Memory: Undo Object Allocation Succeeded                  0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [10.485s]      Memory: Undo Page Allocation                              0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [10.485s]       Phase: Concurrent Mark                              23.482 / 23.482       23.482 / 23.482       23.482 / 23.482       23.482 / 23.482      ms
 * [10.485s]       Phase: Concurrent Mark Continue                      0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [10.485s]       Phase: Concurrent Mark Free                          0.001 / 0.001         0.001 / 0.001         0.001 / 0.001         0.001 / 0.001       ms
 * [10.485s]       Phase: Concurrent Process Non-Strong References      2.515 / 2.515         2.515 / 2.515         2.515 / 2.515         2.515 / 2.515       ms
 * [10.485s]       Phase: Concurrent Relocate                           7.108 / 7.108         7.108 / 7.108         7.108 / 7.108         7.108 / 7.108       ms
 * [10.485s]       Phase: Concurrent Reset Relocation Set               0.001 / 0.001         0.001 / 0.001         0.001 / 0.001         0.001 / 0.001       ms
 * [10.485s]       Phase: Concurrent Select Relocation Set              6.881 / 6.881         6.881 / 6.881         6.881 / 6.881         6.881 / 6.881       ms
 * [10.485s]       Phase: Pause Mark End                                0.106 / 0.106         0.106 / 0.106         0.106 / 0.106         0.106 / 0.106       ms
 * [10.485s]       Phase: Pause Mark Start                              0.483 / 0.483         0.483 / 0.483         0.483 / 0.483         0.483 / 0.483       ms
 * [10.485s]       Phase: Pause Relocate Start                          0.013 / 0.013         0.013 / 0.013         0.013 / 0.013         0.013 / 0.013       ms
 * [10.485s]    Subphase: Concurrent Classes Purge                      0.157 / 0.157         0.157 / 0.157         0.157 / 0.157         0.157 / 0.157       ms
 * [10.485s]    Subphase: Concurrent Classes Unlink                     1.718 / 1.718         1.718 / 1.718         1.718 / 1.718         1.718 / 1.718       ms
 * [10.485s]    Subphase: Concurrent Mark                              21.264 / 21.975       21.264 / 21.975       21.264 / 21.975       21.264 / 21.975      ms
 * [10.485s]    Subphase: Concurrent Mark Try Flush                     0.022 / 0.056         0.022 / 0.056         0.022 / 0.056         0.022 / 0.056       ms
 * [10.485s]    Subphase: Concurrent Mark Try Terminate                 0.597 / 1.785         0.597 / 1.785         0.597 / 1.785         0.597 / 1.785       ms
 * [10.485s]    Subphase: Concurrent References Enqueue                 0.005 / 0.005         0.005 / 0.005         0.005 / 0.005         0.005 / 0.005       ms
 * [10.485s]    Subphase: Concurrent References Process                 0.184 / 0.184         0.184 / 0.184         0.184 / 0.184         0.184 / 0.184       ms
 * [10.485s]    Subphase: Concurrent Roots ClassLoaderDataGraph         0.164 / 0.325         0.164 / 0.325         0.164 / 0.325         0.164 / 0.325       ms
 * [10.485s]    Subphase: Concurrent Roots CodeCache                    0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [10.485s]    Subphase: Concurrent Roots JavaThreads                  0.730 / 1.083         0.730 / 1.083         0.730 / 1.083         0.730 / 1.083       ms
 * [10.485s]    Subphase: Concurrent Roots OopStorageSet                0.058 / 0.086         0.058 / 0.086         0.058 / 0.086         0.058 / 0.086       ms
 * [10.485s]    Subphase: Concurrent Weak Roots OopStorageSet           0.360 / 0.377         0.360 / 0.377         0.360 / 0.377         0.360 / 0.377       ms
 * [10.485s]    Subphase: Pause Mark Try Complete                       0.001 / 0.002         0.001 / 0.002         0.001 / 0.002         0.001 / 0.002       ms
 * [10.485s]      System: Java Threads                                     16 / 16               16 / 16               16 / 16               16 / 16          threads
 * [10.485s] =========================================================================================================================================================
 * </pre>
 * 
 * <p>
 * 2) Generational:
 * </p>
 * 
 * <pre>
 * [3.267s][info][gc,stats    ] === Garbage Collection Statistics =======================================================================================================================
 * [3.267s][info][gc,stats    ]                                                              Last 10s              Last 10m              Last 10h                Total
 * [3.267s][info][gc,stats    ]                                                              Avg / Max             Avg / Max             Avg / Max             Avg / Max
 * [3.267s][info][gc,stats    ]        Contention: Mark Segment Reset Contention                     0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [3.267s][info][gc,stats    ]        Contention: Mark SeqNum Reset Contention                      0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [3.267s][info][gc,stats    ]          Critical: Allocation Stall                                 90 / 134              90 / 134              90 / 134              90 / 134         ops/s
 * [3.267s][info][gc,stats    ]          Critical: Allocation Stall                              2.353 / 37.597        2.353 / 37.597        2.353 / 37.597        2.353 / 37.597      ms
 * [3.267s][info][gc,stats    ]          Critical: JNI Critical Stall                                0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [3.267s][info][gc,stats    ]          Critical: JNI Critical Stall                            0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [3.267s][info][gc,stats    ]          Critical: Relocation Stall                                  7 / 10                7 / 10                7 / 10                7 / 10          ops/s
 * [3.267s][info][gc,stats    ]          Critical: Relocation Stall                              0.554 / 1.508         0.554 / 1.508         0.554 / 1.508         0.554 / 1.508       ms
 * [3.267s][info][gc,stats    ]  Major Collection: Major Collection                             71.872 / 153.637      71.872 / 153.637      71.872 / 153.637      71.872 / 153.637     ms
 * [3.267s][info][gc,stats    ]            Memory: Allocation Rate                                2380 / 2420           2380 / 2420           2380 / 2420           2380 / 2420        MB/s
 * [3.267s][info][gc,stats    ]            Memory: Defragment                                        0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [3.267s][info][gc,stats    ]            Memory: Out Of Memory                                     0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [3.267s][info][gc,stats    ]            Memory: Page Cache Flush                                  1 / 4                 1 / 4                 1 / 4                 1 / 4           MB/s
 * [3.267s][info][gc,stats    ]            Memory: Page Cache Hit L1                              2595 / 3016           2595 / 3016           2595 / 3016           2595 / 3016        ops/s
 * [3.267s][info][gc,stats    ]            Memory: Page Cache Hit L2                                 0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [3.267s][info][gc,stats    ]            Memory: Page Cache Hit L3                                 6 / 17                6 / 17                6 / 17                6 / 17          ops/s
 * [3.267s][info][gc,stats    ]            Memory: Page Cache Miss                                  11 / 33               11 / 33               11 / 33               11 / 33          ops/s
 * [3.267s][info][gc,stats    ]            Memory: Uncommit                                          0 / 0                 0 / 0                 0 / 0                 0 / 0           MB/s
 * [3.267s][info][gc,stats    ]            Memory: Undo Object Allocation Failed                     0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [3.267s][info][gc,stats    ]            Memory: Undo Object Allocation Succeeded                 60 / 136              60 / 136              60 / 136              60 / 136         ops/s
 * [3.267s][info][gc,stats    ]            Memory: Undo Page Allocation                              0 / 0                 0 / 0                 0 / 0                 0 / 0           ops/s
 * [3.267s][info][gc,stats    ]  Minor Collection: Minor Collection                              3.403 / 19.703        3.403 / 19.703        3.403 / 19.703        3.403 / 19.703      ms
 * [3.267s][info][gc,stats    ]    Old Generation: Old Generation                               66.775 / 149.922      66.775 / 149.922      66.775 / 149.922      66.775 / 149.922     ms
 * [3.267s][info][gc,stats    ]         Old Pause: Pause Mark End                                0.004 / 0.009         0.004 / 0.009         0.004 / 0.009         0.004 / 0.009       ms
 * [3.267s][info][gc,stats    ]         Old Pause: Pause Relocate Start                          0.005 / 0.014         0.005 / 0.014         0.005 / 0.014         0.005 / 0.014       ms
 * [3.267s][info][gc,stats    ]         Old Phase: Concurrent Mark                              18.650 / 34.134       18.650 / 34.134       18.650 / 34.134       18.650 / 34.134      ms
 * [3.267s][info][gc,stats    ]         Old Phase: Concurrent Mark Continue                      0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [3.267s][info][gc,stats    ]         Old Phase: Concurrent Mark Free                          0.001 / 0.008         0.001 / 0.008         0.001 / 0.008         0.001 / 0.008       ms
 * [3.267s][info][gc,stats    ]         Old Phase: Concurrent Process Non-Strong                 1.105 / 1.661         1.105 / 1.661         1.105 / 1.661         1.105 / 1.661       ms
 * [3.267s][info][gc,stats    ]         Old Phase: Concurrent Relocate                           1.657 / 10.963        1.657 / 10.963        1.657 / 10.963        1.657 / 10.963      ms
 * [3.267s][info][gc,stats    ]         Old Phase: Concurrent Remap Roots                       13.400 / 14.317       13.400 / 14.317       13.400 / 14.317       13.400 / 14.317      ms
 * [3.267s][info][gc,stats    ]         Old Phase: Concurrent Reset Relocation Set               0.002 / 0.005         0.002 / 0.005         0.002 / 0.005         0.002 / 0.005       ms
 * [3.267s][info][gc,stats    ]         Old Phase: Concurrent Select Relocation Set              2.153 / 3.707         2.153 / 3.707         2.153 / 3.707         2.153 / 3.707       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Classes Purge                      0.008 / 0.031         0.008 / 0.031         0.008 / 0.031         0.008 / 0.031       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Classes Unlink                     0.723 / 1.143         0.723 / 1.143         0.723 / 1.143         0.723 / 1.143       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Mark Follow                       18.477 / 33.862       18.477 / 33.862       18.477 / 33.862       18.477 / 33.862      ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Mark Root Colored                  0.012 / 0.042         0.012 / 0.042         0.012 / 0.042         0.012 / 0.042       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Mark Root Uncolored                0.127 / 0.254         0.127 / 0.254         0.127 / 0.254         0.127 / 0.254       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Mark Roots                         0.169 / 0.284         0.169 / 0.284         0.169 / 0.284         0.169 / 0.284       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent References Enqueue                 0.000 / 0.002         0.000 / 0.002         0.000 / 0.002         0.000 / 0.002       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent References Process                 0.061 / 0.108         0.061 / 0.108         0.061 / 0.108         0.061 / 0.108       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Remap Remembered                  12.852 / 13.626       12.852 / 13.626       12.852 / 13.626       12.852 / 13.626      ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Remap Roots Colored                0.142 / 0.223         0.142 / 0.223         0.142 / 0.223         0.142 / 0.223       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Remap Roots Uncolored              0.331 / 0.557         0.331 / 0.557         0.331 / 0.557         0.331 / 0.557       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Roots ClassLoaderDataGraph         0.018 / 0.046         0.018 / 0.046         0.018 / 0.046         0.018 / 0.046       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Roots CodeCache                    0.246 / 0.454         0.246 / 0.454         0.246 / 0.454         0.246 / 0.454       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Roots JavaThreads                  0.072 / 0.253         0.072 / 0.253         0.072 / 0.253         0.072 / 0.253       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Roots OopStorageSet                0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [3.267s][info][gc,stats    ]      Old Subphase: Concurrent Weak Roots OopStorageSet           0.075 / 0.582         0.075 / 0.582         0.075 / 0.582         0.075 / 0.582       ms
 * [3.267s][info][gc,stats    ]            System: Java Threads                                     10 / 12               10 / 12               10 / 12               10 / 12          threads
 * [3.267s][info][gc,stats    ]  Young Generation: Young Generation                              4.819 / 19.395        4.819 / 19.395        4.819 / 19.395        4.819 / 19.395      ms
 * [3.267s][info][gc,stats    ]  Young Generation: Young Generation                              3.288 / 19.477        3.288 / 19.477        3.288 / 19.477        3.288 / 19.477      ms
 * [3.267s][info][gc,stats    ]  Young Generation: Young Generation (Collect Roots)              0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [3.267s][info][gc,stats    ]  Young Generation: Young Generation (Promote All)                0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [3.267s][info][gc,stats    ]       Young Pause: Pause Mark End                                0.003 / 0.014         0.003 / 0.014         0.003 / 0.014         0.003 / 0.014       ms
 * [3.267s][info][gc,stats    ]       Young Pause: Pause Mark Start                              0.004 / 0.010         0.004 / 0.010         0.004 / 0.010         0.004 / 0.010       ms
 * [3.267s][info][gc,stats    ]       Young Pause: Pause Mark Start (Major)                      0.006 / 0.011         0.006 / 0.011         0.006 / 0.011         0.006 / 0.011       ms
 * [3.267s][info][gc,stats    ]       Young Pause: Pause Relocate Start                          0.003 / 0.010         0.003 / 0.010         0.003 / 0.010         0.003 / 0.010       ms
 * [3.267s][info][gc,stats    ]       Young Phase: Concurrent Mark                               1.046 / 10.699        1.046 / 10.699        1.046 / 10.699        1.046 / 10.699      ms
 * [3.267s][info][gc,stats    ]       Young Phase: Concurrent Mark Continue                      0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [3.267s][info][gc,stats    ]       Young Phase: Concurrent Mark Free                          0.000 / 0.001         0.000 / 0.001         0.000 / 0.001         0.000 / 0.001       ms
 * [3.267s][info][gc,stats    ]       Young Phase: Concurrent Relocate                           0.105 / 5.077         0.105 / 5.077         0.105 / 5.077         0.105 / 5.077       ms
 * [3.267s][info][gc,stats    ]       Young Phase: Concurrent Reset Relocation Set               0.001 / 0.005         0.001 / 0.005         0.001 / 0.005         0.001 / 0.005       ms
 * [3.267s][info][gc,stats    ]       Young Phase: Concurrent Select Relocation Set              1.906 / 4.817         1.906 / 4.817         1.906 / 4.817         1.906 / 4.817       ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Mark Follow                        0.425 / 10.033        0.425 / 10.033        0.425 / 10.033        0.425 / 10.033      ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Mark Root Colored                  0.195 / 1.770         0.195 / 1.770         0.195 / 1.770         0.195 / 1.770       ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Mark Root Uncolored                0.410 / 0.952         0.410 / 0.952         0.410 / 0.952         0.410 / 0.952       ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Mark Roots                         0.620 / 2.252         0.620 / 2.252         0.620 / 2.252         0.620 / 2.252       ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Relocate Remset FP                 0.015 / 2.702         0.015 / 2.702         0.015 / 2.702         0.015 / 2.702       ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Roots ClassLoaderDataGraph         0.036 / 0.282         0.036 / 0.282         0.036 / 0.282         0.036 / 0.282       ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Roots CodeCache                    0.284 / 0.663         0.284 / 0.663         0.284 / 0.663         0.284 / 0.663       ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Roots JavaThreads                  0.063 / 0.410         0.063 / 0.410         0.063 / 0.410         0.063 / 0.410       ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Roots OopStorageSet                0.000 / 0.000         0.000 / 0.000         0.000 / 0.000         0.000 / 0.000       ms
 * [3.267s][info][gc,stats    ]    Young Subphase: Concurrent Weak Roots OopStorageSet           0.079 / 1.533         0.079 / 1.533         0.079 / 1.533         0.079 / 1.533       ms
 * [3.267s][info][gc,stats    ] =========================================================================================================================================================
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ZStatsEvent extends UnknownCollector implements UnifiedLogging, HeaderEvent, ThrowAwayEvent {
    /**
     * Regular expression for the header.
     */
    public static final String _REGEX_HEADER = "^(" + UnifiedRegEx.DECORATOR
            + ") ={3} Garbage Collection Statistics ={119}$";
    /**
     * Regular expression defining standard logging.
     */
    private static final String REGEX[] = {
            // Header
            _REGEX_HEADER,
            //
            "^" + UnifiedRegEx.DECORATOR + "[ ]+Last 10s[ ]+Last 10m[ ]+Last 10h[ ]+Total[ ]*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "([ ]+Avg / Max){4}[ ]*$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]+(Contention|Collector|Critical|Major Collection|Memory|Minor Collection|Old Generation|"
                    + "Old Pause|Old Phase|Old Subphase|Phase|Subphase|System|Young Generation|Young Pause|"
                    + "Young Phase|Young Subphase): .+$",
            // Footer
            "^(" + UnifiedRegEx.DECORATOR + ") ={153}$"
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
    public ZStatsEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.Z_STATS.toString();
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
