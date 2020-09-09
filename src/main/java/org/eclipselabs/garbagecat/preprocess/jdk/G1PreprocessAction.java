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
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1 preprocessing.
 * </p>
 *
 * <p>
 * Remove G1 collector verbose logging when <code>-XX:+UseG1GC</code> used in combination with
 * <code>-XX:+PrintGCDetails</code>. It is currently not being used for analysis. Other general G1 preprocessing.
 * </p>
 *
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Young collection:
 * </p>
 *
 * <pre>
 * 0.304: [GC pause (young), 0.00376500 secs]
 *    [Parallel Time:   3.6 ms]
 *       [GC Worker Start Time (ms):  304.3  304.4  305.7  305.7]
 *       [Update RS (ms):  0.0  0.0  0.0  0.0
 *        Avg:   0.0, Min:   0.0, Max:   0.0]
 *          [Processed Buffers : 0 0 4 2
 *           Sum: 6, Avg: 1, Min: 0, Max: 4]
 *       [Ext Root Scanning (ms):  1.6  2.3  0.6  0.6
 *        Avg:   1.3, Min:   0.6, Max:   2.3]
 *       [Mark Stack Scanning (ms):  0.0  0.0  0.0  0.0
 *        Avg:   0.0, Min:   0.0, Max:   0.0]
 *       [Scan RS (ms):  0.0  0.0  0.0  0.0
 *        Avg:   0.0, Min:   0.0, Max:   0.0]
 *       [Object Copy (ms):  1.9  1.3  1.2  1.2
 *        Avg:   1.4, Min:   1.2, Max:   1.9]
 *       [Termination (ms):  0.1  0.0  0.5  0.5
 *        Avg:   0.2, Min:   0.0, Max:   0.5]
 *          [Termination Attempts : 1 1 1 1
 *           Sum: 4, Avg: 1, Min: 1, Max: 1]
 *       [GC Worker End Time (ms):  308.0  308.0  308.0  308.0]
 *       [Other:   0.7 ms]
 *    [Clear CT:   0.0 ms]
 *    [Other:   0.1 ms]
 *       [Choose CSet:   0.0 ms]
 *    [ 8192K-&gt;2112K(59M)]
 *  [Times: user=0.01 sys=0.00, real=0.01 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 0.304: [GC pause (young), 0.00376500 secs] 8192K-&gt;2112K(59M) [Times: user=0.01 sys=0.00, real=0.01 secs]
 * </pre>
 * 
 * <p>
 * 2) JDK8 G1 Evacuation Pause:
 * </p>
 *
 * <pre>
 * 2.192: [GC pause (G1 Evacuation Pause) (young)
 * Desired survivor size 8388608 bytes, new threshold 15 (max 15)
 * , 0.0209631 secs]
 *    [Parallel Time: 12.6 ms, GC Workers: 6]
 *       [GC Worker Start (ms): Min: 2191.9, Avg: 2191.9, Max: 2191.9, Diff: 0.1]
 *       [Ext Root Scanning (ms): Min: 2.7, Avg: 3.0, Max: 3.5, Diff: 0.8, Sum: 18.1]
 *       [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.1]
 *          [Processed Buffers: Min: 0, Avg: 8.0, Max: 39, Diff: 39, Sum: 48]
 *       [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
 *       [Object Copy (ms): Min: 9.0, Avg: 9.4, Max: 9.8, Diff: 0.8, Sum: 56.7]
 *       [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
 *       [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.2]
 *       [GC Worker Total (ms): Min: 12.5, Avg: 12.5, Max: 12.6, Diff: 0.1, Sum: 75.3]
 *       [GC Worker End (ms): Min: 2204.4, Avg: 2204.4, Max: 2204.4, Diff: 0.0]
 *    [Code Root Fixup: 0.0 ms]
 *    [Clear CT: 0.1 ms]
 *    [Other: 8.2 ms]
 *       [Choose CSet: 0.0 ms]
 *       [Ref Proc: 7.9 ms]
 *       [Ref Enq: 0.1 ms]
 *       [Free CSet: 0.0 ms]
 *    [Eden: 128.0M(128.0M)-&gt;0.0B(112.0M) Survivors: 0.0B-&gt;16.0M Heap: 128.0M(30.0G)-&gt;24.9M(30.0G)]
 *  [Times: user=0.09 sys=0.02, real=0.03 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2.192: [GC pause (G1 Evacuation Pause) (young), 0.0209631 secs][Eden: 128.0M(128.0M)-&gt;0.0B(112.0M) Survivors: 0.0B-&gt;16.0M Heap: 128.0M(30.0G)-&gt;24.9M(30.0G)] [Times: user=0.09 sys=0.02, real=0.03 secs]
 * </pre>
 * 
 * <p>
 * 3) JDK8 GCLocker Initiated GC:
 * </p>
 *
 * <pre>
 * 5.293: [GC pause (GCLocker Initiated GC) (young)
 * Desired survivor size 8388608 bytes, new threshold 15 (max 15)
 * - age   1:    3074480 bytes,    3074480 total
 * , 0.0176868 secs]
 *    [Parallel Time: 9.3 ms, GC Workers: 6]
 *       [GC Worker Start (ms): Min: 5292.9, Avg: 5293.0, Max: 5293.0, Diff: 0.0]
 *       [Ext Root Scanning (ms): Min: 3.1, Avg: 3.9, Max: 4.4, Diff: 1.2, Sum: 23.2]
 *       [Update RS (ms): Min: 0.2, Avg: 0.6, Max: 1.0, Diff: 0.8, Sum: 3.8]
 *          [Processed Buffers: Min: 1, Avg: 3.2, Max: 6, Diff: 5, Sum: 19]
 *       [Scan RS (ms): Min: 0.1, Avg: 0.2, Max: 0.4, Diff: 0.3, Sum: 1.5]
 *       [Object Copy (ms): Min: 4.2, Avg: 4.5, Max: 4.8, Diff: 0.6, Sum: 26.9]
 *       [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
 *       [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]
 *       [GC Worker Total (ms): Min: 9.2, Avg: 9.2, Max: 9.3, Diff: 0.0, Sum: 55.5]
 *       [GC Worker End (ms): Min: 5302.2, Avg: 5302.2, Max: 5302.2, Diff: 0.0]
 *    [Code Root Fixup: 0.0 ms]
 *    [Clear CT: 0.1 ms]
 *    [Other: 8.3 ms]
 *       [Choose CSet: 0.0 ms]
 *       [Ref Proc: 8.0 ms]
 *       [Ref Enq: 0.1 ms]
 *       [Free CSet: 0.0 ms]
 *    [Eden: 112.0M(112.0M)-&gt;0.0B(112.0M) Survivors: 16.0M-&gt;16.0M Heap: 415.0M(30.0G)-&gt;313.0M(30.0G)]
 *  [Times: user=0.01 sys=0.00, real=0.02 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 5.293: [GC pause (GCLocker Initiated GC) (young), 0.0176868 secs][Eden: 112.0M(112.0M)-&gt;0.0B(112.0M) Survivors: 16.0M-&gt;16.0M Heap: 415.0M(30.0G)-&gt;313.0M(30.0G)] [Times: user=0.01 sys=0.00, real=0.02 secs]
 * </pre>
 * 
 * <p>
 * 4) JDK8 Remark:
 * </p>
 *
 * <pre>
 * 2971.469: [GC remark 2972.470: [GC ref-proc, 0.1656600 secs]
 *  [Times: user=0.22 sys=0.00, real=0.22 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2971.469: [GC remark, 0.2274544 secs] [Times: user=0.22 sys=0.00, real=0.22 secs]
 * </pre>
 * 
 * *
 * <p>
 * 5) JDK8 Mixed Pause:
 * </p>
 *
 * <pre>
 * 2973.338: [GC pause (G1 Evacuation Pause) (mixed)
 * Desired survivor size 8388608 bytes, new threshold 15 (max 15)
 * - age   1:    1228792 bytes,    1228792 total
 * - age   2:    3465472 bytes,    4694264 total
 * - age   3:      61528 bytes,    4755792 total
 * - age   4:    1792320 bytes,    6548112 total
 * - age   5:    1095352 bytes,    7643464 total
 * , 0.0457502 secs]
 *    [Parallel Time: 41.1 ms, GC Workers: 6]
 *       [GC Worker Start (ms): Min: 2973338.2, Avg: 2973338.2, Max: 2973338.3, Diff: 0.0]
 *       [Ext Root Scanning (ms): Min: 21.6, Avg: 25.5, Max: 29.1, Diff: 7.5, Sum: 153.0]
 *       [Update RS (ms): Min: 0.0, Avg: 2.7, Max: 6.5, Diff: 6.5, Sum: 15.9]
 *          [Processed Buffers: Min: 0, Avg: 17.5, Max: 39, Diff: 39, Sum: 105]
 *       [Scan RS (ms): Min: 1.3, Avg: 5.1, Max: 6.2, Diff: 4.8, Sum: 30.6]
 *       [Object Copy (ms): Min: 6.8, Avg: 7.8, Max: 11.3, Diff: 4.5, Sum: 46.6]
 *       [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]
 *       [GC Worker Other (ms): Min: 0.0, Avg: 0.1, Max: 0.1, Diff: 0.1, Sum: 0.3]
 *       [GC Worker Total (ms): Min: 41.1, Avg: 41.1, Max: 41.1, Diff: 0.0, Sum: 246.5]
 *       [GC Worker End (ms): Min: 2973379.3, Avg: 2973379.3, Max: 2973379.3, Diff: 0.0]
 *    [Code Root Fixup: 0.0 ms]
 *    [Clear CT: 1.9 ms]
 *    [Other: 2.7 ms]
 *       [Choose CSet: 0.2 ms]
 *       [Ref Proc: 0.2 ms]
 *       [Ref Enq: 0.0 ms]
 *       [Free CSet: 1.9 ms]
 *    [Eden: 112.0M(112.0M)-&gt;0.0B(112.0M) Survivors: 16.0M-&gt;16.0M Heap: 12.9G(30.0G)-&gt;11.3G(30.0G)]
 *  [Times: user=0.19 sys=0.00, real=0.05 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2973.338: [GC pause (G1 Evacuation Pause) (mixed), 0.0457502 secs][Eden: 112.0M(112.0M)-&gt;0.0B(112.0M) Survivors: 16.0M-&gt;16.0M Heap: 12.9G(30.0G)-&gt;11.3G(30.0G)] [Times: user=0.19 sys=0.00, real=0.05 secs]
 * </pre>
 * 
 * <p>
 * 6) JDK8 GC Cleanup:
 * </p>
 *
 * <pre>
 * 2972.698: [GC cleanup 13G-&gt;12G(30G), 0.0358748 secs]
 *  [Times: user=0.19 sys=0.00, real=0.03 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2972.698: [GC cleanup 13G-&gt;12G(30G), 0.0358748 secs] [Times: user=0.19 sys=0.00, real=0.03 secs]
 * </pre>
 * 
 * <p>
 * 7) JDK8 Full GC:
 * </p>
 *
 * <pre>
 * 2016-02-09T06:21:30.379-0500: 27999.141: [Full GC 18G-&gt;4153M(26G), 10.1760410 secs]
 *    [Eden: 0.0B(1328.0M)-&gt;0.0B(15.6G) Survivors: 0.0B-&gt;0.0B Heap: 18.9G(26.0G)-&gt;4153.8M(26.0G)]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 2016-02-09T06:21:30.379-0500: 27999.141: [Full GC 18G-&gt;4153M(26G), 10.1760410 secs] [Eden: 0.0B(1328.0M)-&gt;0.0B(15.6G) Survivors: 0.0B-&gt;0.0B Heap: 18.9G(26.0G)-&gt;4153.8M(26.0G)]
 * </pre>
 * 
 * <p>
 * 7) Mixed up G1_YOUNG_PAUSE and G1_CONCURRENT
 * </p>
 *
 * <pre>
 * 4969.943: [GC pause (young)4970.158: [GC concurrent-root-region-scan-end, 0.5703200 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 4970.158: [GC concurrent-root-region-scan-end, 0.5703200 secs]
 * 4969.943: [GC pause (young)
 * </pre>
 * 
 * <p>
 * 8) With G1Ergonomics
 * </p>
 *
 * <pre>
 * 72945.823: [GC pause (young) 72945.823: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 497394, predicted base time: 66.16 ms, remaining time: 433.84 ms, target pause time: 500.00 ms]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 72945.823: [GC pause (young) 72945.823:
 * </pre>
 *
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class G1PreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning G1_YOUNG_PAUSE collection. Trigger can be before and/or after
     * "(young)".
     * 
     * 0.807: [GC pause (young), 0.00290200 secs]
     * 
     * 6049.175: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 3.1713585 secs]
     * 
     * 2017-03-21T15:05:53.717+1100: 425001.630: [GC pause (G1 Evacuation Pause) (young)2017-03-21T15:05:53.717+1100:
     * 425001.630: 425001.630: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 3, predicted base
     * time: 45.72 ms, remaining time: 304.28 ms, target pause time: 350.00 ms]
     * 
     * 0.449: [GC pause (G1 Evacuation Pause) (young)Before GC RS summary
     */
    private static final String REGEX_RETAIN_BEGINNING_YOUNG_PAUSE = "^((" + JdkRegEx.DATESTAMP + ": )?"
            + JdkRegEx.TIMESTAMP + ": \\[GC pause( \\((" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION
            + ")\\))? \\(young\\)( \\((" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + ")\\))?(, "
            + JdkRegEx.DURATION + "\\])?)((" + JdkRegEx.DATESTAMP + ": )?(" + JdkRegEx.TIMESTAMP + ": )?( )?"
            + JdkRegEx.TIMESTAMP + ": \\[G1Ergonomics.+)?(Before GC RS summary)?[ ]*$";

    /**
     * Regular expression for retained beginning G1_YOUNG_INITIAL_MARK collection.
     * 
     * 2017-01-20T23:18:29.561-0500: 1513296.434: [GC pause (young) (initial-mark), 0.0225230 secs]
     */
    private static final String REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK = "^((" + JdkRegEx.DATESTAMP + ": )?"
            + JdkRegEx.TIMESTAMP + ": \\[GC pause( \\((" + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + "|"
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION
            + ")\\))? \\(young\\) \\(initial-mark\\)(, " + JdkRegEx.DURATION + "\\])?)( " + JdkRegEx.TIMESTAMP
            + ": \\[G1Ergonomics.+)?(Before GC RS summary)?[ ]*$";

    /**
     * Regular expression for retained beginning G1_FULL_GC collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_FULL_GC = "^((" + JdkRegEx.DATESTAMP + ": )?"
            + JdkRegEx.TIMESTAMP + ": \\[Full GC (\\((" + JdkRegEx.TRIGGER_SYSTEM_GC + "|"
            + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|" + JdkRegEx.TRIGGER_JVM_TI_FORCED_GAREBAGE_COLLECTION + "|"
            + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + ")\\))?[ ]{0,2}(" + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\])?)( Before GC RS summary)?[ ]*$";

    /**
     * Regular expression for retained beginning G1_FULL_GC with PRINT_CLASS_HISTOGRAM collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_FULL_GC_CLASS_HISTOGRAM = "^((" + JdkRegEx.DATESTAMP + ": )?"
            + JdkRegEx.TIMESTAMP + ": \\[Full GC(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[Class Histogram \\(before full gc\\):)[ ]*$";

    /**
     * Regular expression for retained beginning PRINT_CLASS_HISTOGRAM collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_CLASS_HISTOGRAM = "^(" + JdkRegEx.TIMESTAMP
            + ": \\[Class Histogram \\(after full gc\\):)[ ]*$";

    /**
     * Regular expression for retained beginning G1_CONCURRENT collection.
     * 
     * 2018-12-06T21:56:32.691-0500: 18.9732018-12-06T21:56:32.691-0500: : 18.973[GC concurrent-root-region-scan-start]
     */
    private static final String REGEX_RETAIN_BEGINNING_CONCURRENT = "^(: )?(" + JdkRegEx.DATESTAMP + ": )?(("
            + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + "(" + JdkRegEx.DATESTAMP + ": )?: )("
            + JdkRegEx.TIMESTAMP + "[:]{0,1}[ ]{0,1})?(" + JdkRegEx.DATESTAMP
            + ")?(\\[GC concurrent-((root-region-scan|mark|cleanup)-(start|end|abort))(, " + JdkRegEx.DURATION
            + ")?\\])[ ]*$";

    /**
     * Regular expression for retained beginning G1_REMARK collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_REMARK = "^((" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[GC remark) (" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": (\\[Finalize Marking, "
            + JdkRegEx.DURATION + "\\] (" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": )?\\[GC ref-proc, "
            + JdkRegEx.DURATION + "\\]( (" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[Unloading, "
            + JdkRegEx.DURATION + "\\])?(, " + JdkRegEx.DURATION + "\\])[ ]*$";

    /**
     * Regular expression for retained beginning G1_MIXED collection.
     * 
     * 2017-06-22T12:25:26.515+0530: 66155.261: [GC pause (G1 Humongous Allocation) (mixed) (to-space exhausted),
     * 0.2466797 secs]
     */
    private static final String REGEX_RETAIN_BEGINNING_MIXED = "^((" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[GC pause( \\((" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|"
            + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + ")\\))? \\(mixed\\)( \\(("
            + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + ")\\))?(, " + JdkRegEx.DURATION + "\\])?)( " + JdkRegEx.TIMESTAMP
            + ": \\[G1Ergonomics.+)?(Before GC RS summary)?[ ]*$";

    /**
     * Regular expression for retained beginning G1_CLEANUP collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_CLEANUP = "^(" + JdkRegEx.TIMESTAMP + ": \\[GC cleanup "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\])[ ]*$";

    /**
     * Regular expression for retained beginning G1_YOUNG_PAUSE mixed with G1_CONCURRENT collection.
     * 
     * 2017-06-01T03:09:18.078-0400: 3978.886: [GC pause (GCLocker Initiated GC) (young)2017-06-01T03:09:18.081-0400:
     * 3978.888: [GC concurrent-root-region-scan-end, 0.0059070 secs]
     * 
     * 2017-06-22T13:55:45.753+0530: 71574.499: [GC pause (G1 Humongous Allocation) (young)2017-06-22T13:55:45.771+0530:
     * 71574.517: [GC concurrent-root-region-scan-end, 0.0181265 secs]
     * 
     * 537.122: [GC pause (G1 Evacuation Pause) (young)537.123: [GC concurrent-root-region-scan-start]
     */
    private static final String REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT = "^((" + JdkRegEx.DATESTAMP + ": )?"
            + JdkRegEx.TIMESTAMP + ": \\[GC pause( \\((" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION
            + ")\\))? \\(young\\))((" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[GC concurrent-(root-region-scan|cleanup|mark)-(start|end)(, " + JdkRegEx.DURATION + ")?\\])[ ]*$";

    /**
     * Regular expression for retained beginning G1_FULL_GC mixed with G1_CONCURRENT collection.
     * 
     * Sometimes the G1_CONCURRENT timestamp is missing. When that happens, use the G1_FULL timestamp.
     * 
     * 298.027: [Full GC (Metadata GC Threshold) [GC concurrent-root-region-scan-start]
     * 
     * 2017-02-27T02:55:32.523+`0300: 35911.404: [Full GC (Allocation Failure)2017-02-27T02:55:32.524+0300: 35911.405:
     * [GC concurrent-root-region-scan-end, 0.0127300 secs]
     * 
     * 2017-06-22T16:03:36.126+0530: 2017-06-22T16:03:36.126+0530: 79244.87279244.872: : [Full GC (Metadata GC
     * Threshold) [GC concurrent-root-region-scan-start]
     * 
     * 2017-06-22T16:35:58.032+0530: 81186.777: 2017-06-22T16:35:58.032+0530: [Full GC (Metadata GC Threshold)
     * 81186.777: [GC concurrent-root-region-scan-start]
     * 
     * 35420.674: [Full GC (Allocation Failure) 35420.734: [GC concurrent-mark-start]3035M->3030M(3072M), 21.7552521
     * secs][Eden: 0.0B(153.0M)->0.0B(153.0M) Survivors: 0.0B->0.0B Heap: 3035.5M(3072.0M)->3030.4M(3072.0M)],
     * [Metaspace: 93308K->93308K(352256K)] [Times: user=16.39 sys=0.04, real=21.75 secs]
     * 
     */
    private static final String REGEX_RETAIN_BEGINNING_FULL_CONCURRENT = "^(" + JdkRegEx.DATESTAMP + ": )?("
            + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + "?(: )?" + JdkRegEx.TIMESTAMP + "?(:)?( )?(: )?("
            + JdkRegEx.DATESTAMP + ": )?(\\[Full GC \\((" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|"
            + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + ")\\)[ ]{0,1})((" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + "?(:)?( )?(\\[GC concurrent-(root-region-scan|mark)-(start|end)(, " + JdkRegEx.DURATION + ")?\\]))("
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION
            + "\\]\\[Eden: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\) Survivors: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\], \\[Metaspace: " + JdkRegEx.SIZE
            + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\]" + TimesData.REGEX + ")?[ ]*$";

    /**
     * Regular expression for retained middle G1_YOUNG_PAUSE collection.
     * 
     * [ 29M->2589K(59M)]
     */
    private static final String REGEX_RETAIN_MIDDLE_YOUNG_PAUSE = "^   (\\[ " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\)\\])[ ]*$";

    /**
     * Regular expression for retained middle G1_YOUNG_INTIAL_MARK collection.
     * 
     * (initial-mark), 0.12895600 secs]
     */
    private static final String REGEX_RETAIN_MIDDLE_YOUNG_INITIAL_MARK = "^( \\(initial-mark\\), " + JdkRegEx.DURATION
            + "\\])[ ]*$";

    /**
     * Regular expression for retained middle G1_FULL_GC collection.
     * 
     * , 5.1353878 secs]
     * 
     * Prepended with size information:
     * 
     * 1831M->1213M(5120M), 5.1353878 secs]
     * 
     * 390M->119M(512M)After GC RS summary
     */
    private static final String REGEX_RETAIN_MIDDLE_FULL = "^ (" + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)(, " + JdkRegEx.DURATION + "\\])?)(After GC RS summary)?[ ]*$";

    /**
     * Regular expression for retained middle.
     * 
     * [Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 136.9M(30.0G)->70.9M(30.0G)]
     * 
     * [Eden: 4096M(4096M)->0B(3528M) Survivors: 0B->568M Heap: 4096M(16384M)->567M(16384M)]
     * 
     * [Eden: 306,0M(306,0M)->0,0B(266,0M) Survivors: 0,0B->40,0M Heap: 306,0M(6144,0M)->57,7M(6144,0M)]
     * 
     */
    private static final String REGEX_RETAIN_MIDDLE = "^   (\\[Eden: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Survivors: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + " Heap: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\](, \\[(Perm|Metaspace): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\])?)[ ]*$";

    /**
     * Regular expression for retained middle duration.
     * 
     * , 0.0414530 secs]
     * 
     * (to-space exhausted), 0.3857580 secs]
     * 
     * (to-space overflow), 0.77121400 secs]
     */
    private static final String REGEX_RETAIN_MIDDLE_DURATION = "^(( \\((" + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + "|"
            + JdkRegEx.TRIGGER_TO_SPACE_OVERFLOW + ")\\))?, " + JdkRegEx.DURATION + "\\])[ ]*$";

    /**
     * Regular expression for retained end.
     * 
     * [Times: user=0.01 sys=0.00, real=0.01 secs]
     */
    private static final String REGEX_RETAIN_END = "^(" + TimesData.REGEX + ")( )?[ ]*$";

    /**
     * Regular expression for retained end concurrent mixed with young pause.
     * 
     * 537.142: [GC concurrent-root-region-scan-end, 0.0189841 secs] (to-space exhausted), 0.3314995 secs][Eden:
     * 0.0B(151.0M)->0.0B(153.0M) Survivors: 2048.0K->0.0B Heap: 3038.7M(3072.0M)->3038.7M(3072.0M)] [Times: user=0.20
     * sys=0.00, real=0.33 secs]
     */
    private static final String REGEX_RETAIN_END_CONCURRENT_YOUNG = "^(" + JdkRegEx.TIMESTAMP
            + ": \\[GC concurrent-root-region-scan-end, " + JdkRegEx.DURATION + "\\])(( \\("
            + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + "\\))?, " + JdkRegEx.DURATION + "\\]\\[Eden: " + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Survivors: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\]" + TimesData.REGEX + ")( )?[ ]*$";

    /**
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {

            "^   \\[Root Region Scan Waiting:.+$",
            //
            "^   \\[Parallel Time:.+$",
            // Use complete pattern to identify mixed logging (e.g. stopped time at end)
            "^      \\[GC Worker Start \\(ms\\): Min: \\d{1,10}[\\.,]\\d, Avg: \\d{1,10}[\\.,]\\d, "
                    + "Max: \\d{1,10}[\\.,]\\d, Diff: \\d{1,10}[\\.,]\\d\\]$",
            // JDK8 does not have "Time"
            "^      \\[GC Worker Start( Time)? \\(ms\\):(  \\d{1,10}(\\.|,)\\d)+(\\])?$",
            //
            "^      \\[GC Worker( (End|Other|Total))?( Time)? \\(ms\\):.+$",
            //
            "^      \\[Ext Root Scanning \\(ms\\):.+$",
            //
            "^      \\[SATB Filtering \\(ms\\):.+",
            //
            "^      \\[Update RS \\(ms\\):.+$",
            // Earlier JDKs appear to have a superfluous space
            "^         \\[Processed Buffers( )?:.+$",
            //
            "^      \\[Scan RS \\(ms\\):.+$",
            //
            "^      \\[Object Copy \\(ms\\):.+$",
            //
            "^      \\[Termination \\(ms\\):.+$",
            //
            "^      \\[Code Root Scanning \\(ms\\):.+$",
            //
            "^   \\[Code Root Fixup:.+$",
            //
            "^   \\[Code Root Purge:.+$",
            //
            "^   \\[Code Root Migration:.+$",
            //
            "^      \\[Code Root Marking \\(ms\\):.+$",
            //
            "^   \\[Complete CSet Marking:.+$",
            //
            "^   \\[Clear CT:.+$",
            // Other
            // JDK8 has 3 leading spaces
            "^   (   )?\\[Other:.+$",
            //
            "^      \\[Redirty Cards:.+$",
            //
            "^      \\[Humongous Register:.+$",
            //
            "^      \\[Humongous Reclaim:.+$",
            //
            "^      \\[Choose CSet:.+$",
            //
            "^      \\[Evacuation Failure:.+$",
            //
            "^      \\[Ref Proc:.+$",
            //
            "^      \\[Ref Enq:.+$",
            //
            "^      \\[Free CSet:.+$",
            //
            "^          Sum:.+$",
            //
            "^      \\[Mark Stack Scanning \\(ms\\):.+$",
            //
            "^         \\[Termination Attempts( )?:.+$",
            // Partial concurrent event. Maybe a logging bug?
            "^\\[GC concurrent.+$",
            //
            "^       Avg:.+$",
            // Ergonomics.
            "^(" + JdkRegEx.DATESTAMP + "(:)?[ ]{1,2})?(" + JdkRegEx.TIMESTAMP + ":  )?( )?" + JdkRegEx.TIMESTAMP
                    + ": \\[G1Ergonomics.+$",
            // -XX:+PrintStringDeduplicationStatistics
            "^   \\[String Dedup Fixup:.+$",
            //
            "^      \\[Queue Fixup \\(ms\\):.+$",
            //
            "^      \\[Table Fixup \\(ms\\):.+$",
            //
            "^   \\[Last Exec:.+$",
            //
            "^      \\[Inspected:.+$",
            //
            "^         \\[Skipped:.+$",
            //
            "^         \\[Hashed:.+$",
            //
            "^         \\[Known:.+$",
            //
            "^         \\[New:.+$",
            //
            "^      \\[Deduplicated:.+$",
            //
            "^         \\[Young:.+$",
            //
            "^         \\[Old:.+$",
            //
            "^   \\[Total Exec:.+$",
            //
            "^   \\[Table\\]$",
            //
            "^      \\[Memory Usage:.+$",
            //
            "^      \\[Size:.+$",
            //
            "^      \\[Entries:.+$",
            //
            "^      \\[Resize Count:.+$",
            //
            "^      \\[Rehash Count:.+$",
            //
            "^      \\[Age Threshold:.+$",
            //
            "^   \\[Queue\\]$",
            //
            "^      \\[Dropped:.+$",
            // Summarized remembered set processing info: -XX:+G1SummarizeRSetStats -XX:G1SummarizeRSetStatsPeriod=1
            "^ Recent concurrent refinement statistics$",
            //
            "^  Processed.+$",
            //
            "^  Of \\d{1,4} completed buffers:$",
            //
            "^[ ]{9,12}\\d{1,4} \\([ ]{0,2}\\d{1,3}.\\d%\\) by (concurrent RS|mutator) threads\\.$",
            //
            "^  Did \\d{1,3} coarsenings\\.$",
            //
            "^  Concurrent (RS|sampling) threads times \\(s\\)$",
            //
            "[ ]{9,10}\\d{1,2}\\.\\d{2}(     \\d{1,2}\\.\\d{2}     \\d{1,2}\\.\\d{2}     \\d{1,2}\\.\\d{2}     "
                    + "\\d{1,2}\\.\\d{2}     \\d{1,2}\\.\\d{2}     \\d{1,2}\\.\\d{2}     \\d{1,2}\\.\\d{2})?",
            //
            "^ (Current rem set|Recent concurrent refinement) statistics$",
            //
            "^  Total per region rem sets sizes.+$",
            //
            "^[ ]{5,12}\\d{1,9}(B|K|M)? \\([ ]{0,2}\\d{1,3}\\.\\d%\\)( (entries|elements))? by \\d{1,4} "
                    + "(Young|Humonguous|Free|Old) regions$",
            //
            "^   Static structures.+$",
            //
            "^    \\d{1,9} occupied cards represented\\.$",
            //
            "^    Region with largest (rem set|amount of code roots).+$",
            //
            "^    \\d{1,5} code roots represented.$",
            //
            "^  Total heap region code root sets sizes.+$",
            //
            "^After GC RS summary$",
            //
    };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     * 
     * For example, it is used with the <code>G1PreprocessAction</code> to identify concurrent events intermingled with
     * non-concurrent events to store them in the intermingled log lines list for output after the non-concurrent event.
     */
    public static final String TOKEN = "G1_PREPROCESS_ACTION_TOKEN";

    /**
     * Create event from log entry.
     *
     * @param priorLogEntry
     *            The prior log line.
     * @param logEntry
     *            The log line.
     * @param nextLogEntry
     *            The next log line.
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @param context
     *            Information to make preprocessing decisions.
     */
    public G1PreprocessAction(String priorLogEntry, String logEntry, String nextLogEntry,
            List<String> entangledLogLines, Set<String> context) {

        // Beginning logging
        if (logEntry.matches(REGEX_RETAIN_BEGINNING_FULL_GC)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_FULL_GC);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_FULL_GC_CLASS_HISTOGRAM)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_FULL_GC_CLASS_HISTOGRAM);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_CLASS_HISTOGRAM)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_CLASS_HISTOGRAM);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_CLEANUP)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_CLEANUP);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT)) {
            // Handle concurrent mixed with young collections. See datasets 47-48 and 51-52, 54.
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                entangledLogLines.add(matcher.group(16));
            }
            // Output beginning of young line
            this.logEntry = matcher.group(1);
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_FULL_CONCURRENT)) {
            // Handle concurrent mixed with full collections. See dataset 74.
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_FULL_CONCURRENT);
            Matcher matcher = pattern.matcher(logEntry);
            int indexG1FullDatestamp = 12;
            int indexG1FullTimestamp = 23;
            int indexFullBlock = 40;
            int indexConcurrentLine = 42;
            int indexConcurrentDatestamp = 43;
            int indexConcurrentTimestamp = 54;
            int indexConcurrentBlock = 57;
            int indexG1DetailsBlock = 64;
            if (matcher.matches()) {
                if (matcher.group(indexConcurrentTimestamp) != null) {
                    entangledLogLines.add(matcher.group(indexConcurrentLine));
                } else {
                    // G1_CONCURRENT timestamp missing. Use G1_FULL timestamp.
                    if (matcher.group(indexG1FullDatestamp) != null) {
                        entangledLogLines.add(matcher.group(indexG1FullDatestamp) + matcher.group(indexG1FullTimestamp)
                                + ": " + matcher.group(indexConcurrentBlock));
                    } else {
                        if (matcher.group(1) != null) {
                            entangledLogLines.add(matcher.group(1) + matcher.group(indexG1FullTimestamp) + ": "
                                    + matcher.group(indexConcurrentBlock));
                        } else {
                            entangledLogLines.add(
                                    matcher.group(indexG1FullTimestamp) + ": " + matcher.group(indexConcurrentBlock));
                        }
                    }
                }
            }
            // Output beginning of G1_FULL line
            if (matcher.group(indexG1FullTimestamp) != null) {
                if (matcher.group(indexG1FullDatestamp) != null) {
                    this.logEntry = matcher.group(indexG1FullDatestamp) + matcher.group(indexG1FullTimestamp) + ": "
                            + matcher.group(indexFullBlock);
                } else {
                    if (matcher.group(1) != null) {
                        this.logEntry = matcher.group(1) + matcher.group(indexG1FullTimestamp) + ": "
                                + matcher.group(indexFullBlock);
                    } else {
                        if (matcher.group(indexG1DetailsBlock) != null) {
                            this.logEntry = matcher.group(indexG1FullTimestamp) + ": " + matcher.group(indexFullBlock)
                                    + matcher.group(indexG1DetailsBlock);
                        } else {
                            this.logEntry = matcher.group(indexG1FullTimestamp) + ": " + matcher.group(indexFullBlock);
                        }
                    }
                }
            } else {
                // G1_FULL timestamp missing. Use G1_CONCURRENT timestamp.
                if (matcher.group(indexConcurrentDatestamp) != null) {
                    this.logEntry = matcher.group(indexConcurrentDatestamp) + matcher.group(indexConcurrentTimestamp)
                            + ": " + matcher.group(indexFullBlock);
                } else {
                    this.logEntry = matcher.group(indexConcurrentTimestamp) + ": " + matcher.group(indexFullBlock);
                }
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_CONCURRENT)) {
            // Strip out any leading colon
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_CONCURRENT);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                // Handle concurrent mixed with young collections. See datasets 47-48 and 51-52, 54.
                if (!context.contains(TOKEN)) {
                    // Output now
                    if (matcher.group(2) != null && matcher.group(13) != null) {
                        this.logEntry = matcher.group(2) + matcher.group(25) + ": " + matcher.group(50);
                    } else {
                        if (matcher.group(13) != null) {
                            this.logEntry = matcher.group(25) + ": " + matcher.group(50);
                        } else if (matcher.group(2) != null) {
                            this.logEntry = matcher.group(25) + ": " + matcher.group(50);
                        }
                    }
                } else {
                    // Output later
                    if (matcher.group(2) != null && matcher.group(13) != null) {
                        entangledLogLines.add(matcher.group(2) + matcher.group(25) + ": " + matcher.group(50));
                    } else {
                        if (matcher.group(13) != null) {
                            entangledLogLines.add(matcher.group(25) + ": " + matcher.group(50));
                        } else if (matcher.group(2) != null) {
                            entangledLogLines.add(matcher.group(25) + ": " + matcher.group(50));
                        }
                    }
                }
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_YOUNG_PAUSE)) {
            // Strip out G1Ergonomics
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_YOUNG_PAUSE);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_REMARK)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_REMARK);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + matcher.group(61);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_MIXED)) {
            // Strip out G1Ergonomics
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_MIXED);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK)) {
            // Strip out G1Ergonomics
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_YOUNG_PAUSE)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_YOUNG_PAUSE);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_YOUNG_INITIAL_MARK)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_YOUNG_INITIAL_MARK);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_FULL)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_FULL);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_DURATION)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_DURATION);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (logEntry.matches(REGEX_RETAIN_END)) {
            // End of logging event
            Pattern pattern = Pattern.compile(REGEX_RETAIN_END);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            clearEntangledLines(entangledLogLines);
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.remove(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_END_CONCURRENT_YOUNG)) {
            // End of logging event
            Pattern pattern = Pattern.compile(REGEX_RETAIN_END_CONCURRENT_YOUNG);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                entangledLogLines.add(matcher.group(1));
                this.logEntry = matcher.group(6);
            }
            clearEntangledLines(entangledLogLines);
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.remove(TOKEN);
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.G1.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @param priorLogLine
     *            The last log entry processed.
     * @param nextLogLine
     *            The next log entry processed.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine, String priorLogLine, String nextLogLine) {
        boolean match = false;
        if (logLine.matches(REGEX_RETAIN_BEGINNING_YOUNG_PAUSE)
                || logLine.matches(REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK)
                || logLine.matches(REGEX_RETAIN_BEGINNING_FULL_GC)
                || logLine.matches(REGEX_RETAIN_BEGINNING_FULL_GC_CLASS_HISTOGRAM)
                || logLine.matches(REGEX_RETAIN_BEGINNING_CLASS_HISTOGRAM)
                || logLine.matches(REGEX_RETAIN_BEGINNING_REMARK) || logLine.matches(REGEX_RETAIN_BEGINNING_MIXED)
                || (logLine.matches(REGEX_RETAIN_BEGINNING_CLEANUP) && nextLogLine.matches(REGEX_RETAIN_END))
                || logLine.matches(REGEX_RETAIN_BEGINNING_CONCURRENT)
                || logLine.matches(REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT)
                || logLine.matches(REGEX_RETAIN_BEGINNING_FULL_CONCURRENT)
                || logLine.matches(REGEX_RETAIN_MIDDLE_YOUNG_PAUSE)
                || logLine.matches(REGEX_RETAIN_MIDDLE_YOUNG_INITIAL_MARK) || logLine.matches(REGEX_RETAIN_MIDDLE_FULL)
                || logLine.matches(REGEX_RETAIN_MIDDLE) || logLine.matches(REGEX_RETAIN_MIDDLE_DURATION)
                || logLine.matches(REGEX_RETAIN_END) || logLine.matches(REGEX_RETAIN_END_CONCURRENT_YOUNG)) {
            match = true;
        } else {
            // TODO: Get rid of this and make them throwaway events?
            for (int i = 0; i < REGEX_THROWAWAY.length; i++) {
                if (logLine.matches(REGEX_THROWAWAY[i])) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

    /**
     * Convenience method to write out any saved log lines.
     * 
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @return
     */
    private final void clearEntangledLines(List<String> entangledLogLines) {
        if (entangledLogLines != null && entangledLogLines.size() > 0) {
            // Output any entangled log lines
            Iterator<String> iterator = entangledLogLines.iterator();
            while (iterator.hasNext()) {
                String logLine = iterator.next();
                this.logEntry = this.logEntry + Constants.LINE_SEPARATOR + logLine;
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
    }
}