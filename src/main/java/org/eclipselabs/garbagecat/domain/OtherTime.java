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
package org.eclipselabs.garbagecat.domain;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;

/**
 * <p>
 * Time spent outside of garbage collection (e.g. an I/O delay preventing a timely GC log write from one line to the
 * next). Total safepoint time and GC time are measured with two different clocks. The
 * <code>UnifiedSafepointEvent</code> duration (wall clock) is the sum of GC time and this "Other" time.
 * </p>
 * 
 * <p>
 * Enabled with <code>-XX:+PrintGCDetails</code> in JDK8 and "gc+phases=info" in JDK9+ unified logging.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 *
 * <p>
 * Notes:
 * </p>
 * 
 * <ul>
 * <li>The <code>UnifiedSafepointEvent</code> duration is the time between the first and last GC(9) events: 3792.777 -
 * 3783.195 = 9.582 seconds.</li>
 * <li>The GC duration is the time between the start and end of the <code>UnifiedG1YoungPauseEvent</code>s: 3792.777 -
 * 3792.764 = .013 seconds.</li>
 * <li>Other time is 9569.7ms.</li>
 * <li>9.5697 + .013 = 9.582 seconds.</li>
 * </ul>
 * 
 * <pre>
 * [2022-10-09T13:16:39.707+0000][3783.195s][debug][gc,heap           ] GC(9) Heap before GC invocations=9 (full 0): garbage-first heap   total 10743808K, used 1819374K [0x0000000570400000, 0x0000000800000000)
 * [2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,heap           ] GC(9)   region size 4096K, 425 young (1740800K), 7 survivors (28672K)
 * [2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,heap           ] GC(9)  Metaspace       used 91225K, capacity 98503K, committed 98816K, reserved 339968K
 * [2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,heap           ] GC(9)   class space    used 10833K, capacity 13361K, committed 13440K, reserved 253952K
 * [2022-10-09T13:16:49.276+0000][3792.764s][info ][gc,start          ] GC(9) Pause Young (Normal) (G1 Evacuation Pause)
 * [2022-10-09T13:16:49.276+0000][3792.764s][info ][gc,task           ] GC(9) Using 8 workers of 8 for evacuation
 * [2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,tlab           ] GC(9) TLAB totals: thrds: 223  refills: 9596 max: 462 slow allocs: 767 max 41 waste:  1.5% gc: 16198888B max: 1568112B slow: 6511096B max: 409312B fast: 0B max: 0B
 * [2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,alloc,region   ] GC(9) Mutator Allocation stats, regions: 418, wasted size: 4224B ( 0.0%)
 * [2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,age            ] GC(9) Desired survivor size 113246208 bytes, new threshold 15 (max threshold 15)
 * [2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,ergo,cset      ] GC(9) Finish choosing CSet. old: 0 regions, predicted old region time: 0.00ms, time remaining: 0.43
 * [2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,refine         ] Activated worker 0, on threshold: 19, current: 80
 * [2022-10-09T13:16:49.277+0000][3792.765s][debug][gc,task,stats     ] GC(9) GC Termination Stats
 * [2022-10-09T13:16:49.277+0000][3792.765s][debug][gc,task,stats     ] GC(9)      elapsed  --strong roots-- -------termination------- ------waste (KiB)------
 * [2022-10-09T13:16:49.277+0000][3792.765s][debug][gc,task,stats     ] GC(9) thr     ms        ms      %        ms      %    attempts  total   alloc    undo
 * [2022-10-09T13:16:49.277+0000][3792.765s][debug][gc,task,stats     ] GC(9) --- --------- --------- ------ --------- ------ -------- ------- ------- -------
 * [2022-10-09T13:16:49.287+0000][3792.775s][debug][gc,task,stats     ] GC(9)   5     10.35      6.24  60.30      0.00   0.00        1       0       0       0
 * [2022-10-09T13:16:49.287+0000][3792.775s][debug][gc,task,stats     ] GC(9)   7     10.40      5.65  54.30      0.00   0.02        1       1       1       0
 * [2022-10-09T13:16:49.287+0000][3792.775s][debug][gc,task,stats     ] GC(9)   4     10.46      8.23  78.69      0.00   0.01        1       8       8       0
 * [2022-10-09T13:16:49.287+0000][3792.775s][debug][gc,task,stats     ] GC(9)   2     10.50      6.03  57.40      0.00   0.01        1       0       0       0
 * [2022-10-09T13:16:49.287+0000][3792.775s][debug][gc,task,stats     ] GC(9)   1     10.54      6.53  62.00      0.00   0.01        1       0       0       0
 * [2022-10-09T13:16:49.287+0000][3792.775s][debug][gc,task,stats     ] GC(9)   3     10.57      7.14  67.57      0.00   0.01        1       1       1       0
 * [2022-10-09T13:16:49.287+0000][3792.775s][debug][gc,task,stats     ] GC(9)   6     10.58      7.25  68.51      0.00   0.02        1       0       0       0
 * [2022-10-09T13:16:49.287+0000][3792.775s][debug][gc,task,stats     ] GC(9)   0     10.65      5.69  53.40      0.00   0.01        1       0       0       0
 * [2022-10-09T13:16:49.287+0000][3792.776s][debug][gc,ergo           ] GC(9) Running G1 Clear Card Table Task using 4 workers for 4 units of work for 446 regions.
 * [2022-10-09T13:16:49.288+0000][3792.776s][debug][gc,ref            ] GC(9) Skipped phase1 of Reference Processing due to unavailable references
 * [2022-10-09T13:16:49.288+0000][3792.776s][debug][gc,ergo           ] GC(9) Running G1 Free Collection Set using 8 workers for collection set length 425
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,plab           ] GC(9) Young PLAB allocation: allocated: 28620256B, wasted: 14568B, unused: 2989832B, used: 25615856B, undo waste: 0B,
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,plab           ] GC(9) Young other allocation: region end waste: 0B, regions filled: 7, direct allocated: 262160B, failure used: 0B, failure wasted: 0B
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,plab           ] GC(9) Young sizing: calculated: 5123168B, actual: 5081248B
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,plab           ] GC(9) Old PLAB allocation: allocated: 0B, wasted: 0B, unused: 0B, used: 0B, undo waste: 0B,
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,plab           ] GC(9) Old other allocation: region end waste: 0B, regions filled: 1, direct allocated: 0B, failure used: 0B, failure wasted: 0B
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,plab           ] GC(9) Old sizing: calculated: 0B, actual: 2064B
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,ihop           ] GC(9) Basic information (value update), threshold: 7701161574B (70.00), target occupancy: 11001659392B, current occupancy: 115332592B, recent allocation size: 0B, recent allocation duration: 773718.23ms, recent old gen allocation rate: 0.00B/s, recent marking phase length: 0.00ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,ihop           ] GC(9) Adaptive IHOP information (value update), threshold: 7701161574B (82.35), internal target occupancy: 9351410483B, occupancy: 115332592B, additional buffer size: 2462056448B, predicted old gen allocation rate: 18218.99B/s, predicted marking phase length: 0.00ms, prediction active: false
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,ergo,refine    ] GC(9) Updated Refinement Zones: green: 16, yellow: 48, red: 80
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,phases         ] GC(9)   Pre Evacuate Collection Set: 0.1ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Prepare TLABs: 0.1ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Choose Collection Set: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Humongous Register: 0.1ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,phases         ] GC(9)   Evacuate Collection Set: 10.7ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Ext Root Scanning (ms):   Min:  0.9, Avg:  1.0, Max:  1.0, Diff:  0.1, Sum:  7.8, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Update RS (ms):           Min:  0.4, Avg:  0.7, Max:  1.6, Diff:  1.2, Sum:  5.5, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)       Processed Buffers:        Min: 1, Avg: 26.9, Max: 111, Diff: 110, Sum: 215, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)       Scanned Cards:            Min: 194, Avg: 509.4, Max: 1890, Diff: 1696, Sum: 4075, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)       Skipped Cards:            Min: 0, Avg:  1.8, Max: 8, Diff: 8, Sum: 14, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Scan RS (ms):             Min:  0.0, Avg:  0.0, Max:  0.1, Diff:  0.1, Sum:  0.3, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)       Scanned Cards:            Min: 0, Avg:  2.6, Max: 21, Diff: 21, Sum: 21, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)       Claimed Cards:            Min: 0, Avg:  4.4, Max: 32, Diff: 32, Sum: 35, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)       Skipped Cards:            Min: 0, Avg:  2.1, Max: 17, Diff: 17, Sum: 17, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Code Root Scanning (ms):  Min:  0.0, Avg:  0.2, Max:  1.1, Diff:  1.1, Sum:  1.4, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     AOT Root Scanning (ms):   skipped
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Object Copy (ms):         Min:  7.5, Avg:  8.5, Max:  8.9, Diff:  1.5, Sum: 67.7, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Termination (ms):         Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)       Termination Attempts:     Min: 1, Avg:  1.0, Max: 1, Diff: 0, Sum: 8, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     GC Worker Other (ms):     Min:  0.1, Avg:  0.2, Max:  0.3, Diff:  0.3, Sum:  1.7, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     GC Worker Total (ms):     Min: 10.4, Avg: 10.5, Max: 10.7, Diff:  0.3, Sum: 84.3, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,phases         ] GC(9)   Post Evacuate Collection Set: 1.2ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Code Roots Fixup: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Clear Card Table: 0.3ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Reference Processing: 0.2ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)       Reconsider SoftReferences: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         SoftRef (ms):             skipped
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)       Notify Soft/WeakReferences: 0.1ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         SoftRef (ms):             Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         WeakRef (ms):             Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         FinalRef (ms):            Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Total (ms):               Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)       Notify and keep alive finalizable: 0.1ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Balance queues: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         FinalRef (ms):            Min:  0.0, Avg:  0.0, Max:  0.1, Diff:  0.0, Sum:  0.3, Workers: 8
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)       Notify PhantomReferences: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         PhantomRef (ms):          Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)       SoftReference:
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Discovered: 0
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Cleared: 0
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)       WeakReference:
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Discovered: 5
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Cleared: 5
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)       FinalReference:
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Discovered: 38
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Cleared: 0
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)       PhantomReference:
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Discovered: 2
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         Cleared: 2
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Weak Processing: 0.1ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Merge Per-Thread State: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Code Roots Purge: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Redirty Cards: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     DerivedPointerTable Update: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Free Collection Set: 0.5ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Humongous Reclaim: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Start New Collection Set: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Resize TLABs: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Expand Heap After Collection: 0.0ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,phases         ] GC(9)   Other: 9569.7ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,heap           ] GC(9) Eden regions: 418-&gt;0(580)
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,heap           ] GC(9) Survivor regions: 7-&gt;7(54)
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,heap           ] GC(9) Old regions: 22-&gt;22
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,heap           ] GC(9) Humongous regions: 0-&gt;0
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,metaspace      ] GC(9) Metaspace: 91225K-&gt;91225K(339968K)
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,heap           ] GC(9) Heap after GC invocations=10 (full 0): garbage-first heap   total 10743808K, used 112629K [0x0000000570400000, 0x0000000800000000)
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,heap           ] GC(9)   region size 4096K, 7 young (28672K), 7 survivors (28672K)
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,heap           ] GC(9)  Metaspace       used 91225K, capacity 98503K, committed 98816K, reserved 339968K
 * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,heap           ] GC(9)   class space    used 10833K, capacity 13361K, committed 13440K, reserved 253952K
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc                ] GC(9) Pause Young (Normal) (G1 Evacuation Pause) 1780M-&gt;109M(10492M) 13.288ms
 * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,cpu            ] GC(9) User=0.07s Sys=0.01s Real=0.01s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface OtherTime {

    /**
     * Use for logging events that do not include other time data.
     */
    public static final int NO_DATA = 0;

    /**
     * Regular expression for times data block.
     * 
     * JDK8:
     * 
     * [Other: 0.9 ms]
     * 
     * JDK9+ (with preceding decoractor):
     * 
     * Other: 9569.7ms
     * 
     */
    public static final String REGEX = "[\\[]{0,1}Other:[ ]{1,3}" + JdkRegEx.DURATION_MS + "[\\]]{0,1}";

    /**
     * @return The "Other" time in microseconds (rounded).
     */
    long getOtherTime();
}
