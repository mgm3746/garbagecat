/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.ArrayList;
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
 * <h2>Example Logging</h2>
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
 * :
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
     * Regular expression for retained beginning G1_CLEANUP collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_CLEANUP = "^(" + JdkRegEx.DECORATOR + " \\[GC cleanup "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\])[ ]*$";

    private static final Pattern REGEX_RETAIN_BEGINNING_CLEANUP_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_CLEANUP);
    /**
     * Regular expressions for retained beginning G1_CONCURRENT collection.
     *
     * 1) DECORATOR:
     * 
     * 2018-12-06T21:56:32.691-0500: 18.973: [GC concurrent-root-region-scan-start]
     * 
     * 2018-12-06T21:56:32.691-0500: [GC concurrent-root-region-scan-start]
     * 
     * 27744.494: [GC concurrent-mark-start]
     *
     * 2) DATESTAMP: DATESTAMP:
     * 
     * 2021-10-26T09:58:12.120-0400: 2021-10-26T09:58:12.120-0400[GC concurrent-root-region-scan-start]
     * 
     * 2021-10-27T10:13:37.450-0400: 2021-10-27T10:13:37.450-0400: [GC concurrent-root-region-scan-start]
     * 
     * 2021-10-27T10:50:59.400-04002021-10-27T10:50:59.400-0400: : [GC concurrent-root-region-scan-start]
     *
     * 3) TIMESTAMP: TIMESTAMP:
     *
     * 0.218: 0.218[GC concurrent-root-region-scan-start]
     * 
     * 0.2270.227: : [GC concurrent-root-region-scan-start]
     * 
     * 4) DATESTAMP: DATESTAMP: TIMESTAMP:
     *
     * 2021-10-27T08:03:11.757-0400: 2021-10-27T08:03:11.757-0400: 0.174: [GC concurrent-root-region-scan-start]
     * 
     * 2021-10-27T12:32:11.621-0400: 2021-10-27T12:32:11.621-04000.210: : [GC concurrent-root-region-scan-start]
     *
     * 5) DATESTAMP: DATESTAMP: TIMESTAMP: TIMESTAMP
     *
     * 2021-10-26T18:15:06.169-0400: 2021-10-26T18:15:06.169-0400: 0.156: 0.156: [GC concurrent-root-region-scan-start]
     *
     * 2021-10-27T08:03:11.806-04002021-10-27T08:03:11.806-0400: : 0.2230.223: : [GC concurrent-root-region-scan-start]
     */
    private static final String REGEX_RETAIN_BEGINNING_CONCURRENT = "^((" + JdkRegEx.DECORATOR + ")|("
            + JdkRegEx.DATESTAMP + "(: )?" + JdkRegEx.DATESTAMP + "(:)?( :)?)|(" + JdkRegEx.TIMESTAMP + "(: )?"
            + JdkRegEx.TIMESTAMP + ")(: :)?|(" + JdkRegEx.DATESTAMP + ": " + JdkRegEx.DATESTAMP + "(: )?"
            + JdkRegEx.TIMESTAMP + ":( :)?)|(" + JdkRegEx.DATESTAMP + "(: )?" + JdkRegEx.DATESTAMP + ": (: )?"
            + JdkRegEx.TIMESTAMP + "(: )?" + JdkRegEx.TIMESTAMP
            + ":( :)?))[ ]{0,1}(\\[GC concurrent-((root-region-scan|mark|cleanup)-(start|end|abort))(, "
            + JdkRegEx.DURATION + ")?\\])[ ]*$";

    private static final Pattern REGEX_RETAIN_BEGINNING_CONCURRENT_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_CONCURRENT);
    /**
     * Regular expression for retained beginning G1_FULL_GC mixed with G1_CONCURRENT collection.
     * 
     * Sometimes the G1_CONCURRENT timestamp is missing. When that happens, use the G1_FULL timestamp.
     * 
     * 298.027: [Full GC (Metadata GC Threshold) [GC concurrent-root-region-scan-start]
     * 
     * 2017-02-27T02:55:32.523+`0300: 35911.404: [Full GC (Allocation Failure)2017-02-27T02:55:32.524+0300: 35911.405:
     * [GC concurrent-root-region-scan-end, 0.0127300 secs]
     */
    private static final String REGEX_RETAIN_BEGINNING_FULL_CONCURRENT = "^(" + JdkRegEx.DECORATOR
            + ")?( \\[Full GC \\((" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE
            + ")\\)[ ]{0,1})(: )?((" + JdkRegEx.DECORATOR
            + ")?( \\[GC concurrent-(root-region-scan|mark)-(start|end)(, " + JdkRegEx.DURATION + ")?\\]))("
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION
            + "\\]\\[Eden: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\) Survivors: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\], \\[Metaspace: " + JdkRegEx.SIZE
            + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\]" + TimesData.REGEX + ")?[ ]*$";

    private static final Pattern REGEX_RETAIN_BEGINNING_FULL_CONCURRENT_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_FULL_CONCURRENT);

    /**
     * Regular expression for retained beginning G1_FULL_GC collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_FULL_GC = "^(" + JdkRegEx.DECORATOR + " \\[Full GC (\\(("
            + JdkRegEx.TRIGGER_SYSTEM_GC + "|" + JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION + "|"
            + JdkRegEx.TRIGGER_JVM_TI_FORCED_GAREBAGE_COLLECTION + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD
            + ")\\))?[ ]{0,2}(" + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
            + JdkRegEx.DURATION + "\\])?)( Before GC RS summary)?[ ]*$";

    /**
     * Regular expression for retained beginning G1_FULL_GC collection with PRINT_CLASS_HISTOGRAM.
     */
    private static final String REGEX_RETAIN_BEGINNING_FULL_GC_CLASS_HISTOGRAM = "^(" + JdkRegEx.DECORATOR
            + " \\[Full GC( \\(" + JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC + "\\) )?" + JdkRegEx.DECORATOR
            + " \\[Class Histogram \\(before full gc\\):)[ ]*$";

    private static final Pattern REGEX_RETAIN_BEGINNING_FULL_GC_CLASS_HISTOGRAM_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_FULL_GC_CLASS_HISTOGRAM);

    private static final Pattern REGEX_RETAIN_BEGINNING_FULL_GC_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_FULL_GC);

    /**
     * Regular expression for retained beginning G1_MIXED collection.
     * 
     * 2017-06-22T12:25:26.515+0530: 66155.261: [GC pause (G1 Humongous Allocation) (mixed) (to-space exhausted),
     * 0.2466797 secs]
     * 
     * 2021-12-15T12:19:59.319-0300: 5612.998: [GC pause (GCLocker Initiated GC) (mixed) 5612.999: [G1Ergonomics (CSet
     * Construction) start choosing CSet, _pending_cards: 434455, predicted base time: 121.64 ms, remaining time: 378.36
     * ms, target pause time: 500.00 ms]
     */
    private static final String REGEX_RETAIN_BEGINNING_MIXED = "^(" + JdkRegEx.DECORATOR + " \\[GC pause( \\(("
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|"
            + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + ")\\))? \\(mixed\\)( \\(("
            + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + ")\\))?(, " + JdkRegEx.DURATION + "\\])?)( " + JdkRegEx.DECORATOR
            + " \\[G1Ergonomics.+)?(Before GC RS summary)?[ ]*$";

    private static final Pattern REGEX_RETAIN_BEGINNING_MIXED_PATTERN = Pattern.compile(REGEX_RETAIN_BEGINNING_MIXED);

    /**
     * Regular expression for retained beginning G1_REMARK collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_REMARK = "^(" + JdkRegEx.DECORATOR + " \\[GC remark) "
            + JdkRegEx.DECORATOR + " (\\[Finalize Marking, " + JdkRegEx.DURATION + "\\] " + JdkRegEx.DECORATOR
            + " )?\\[GC ref-proc, " + JdkRegEx.DURATION + "\\]( " + JdkRegEx.DECORATOR + " \\[Unloading, "
            + JdkRegEx.DURATION + "\\])?(, " + JdkRegEx.DURATION + "\\])[ ]*$";

    private static final Pattern REGEX_RETAIN_BEGINNING_REMARK_PATTERN = Pattern.compile(REGEX_RETAIN_BEGINNING_REMARK);

    /**
     * Regular expression for retained beginning G1_YOUNG_PAUSE mixed with G1_CONCURRENT collection.
     * 
     * 2017-06-01T03:09:18.078-0400: 3978.886: [GC pause (GCLocker Initiated GC) (young)2017-06-01T03:09:18.081-0400:
     * 3978.888: [GC concurrent-root-region-scan-end, 0.0059070 secs]
     * 
     * 2017-06-22T13:55:45.753+0530: 71574.499: [GC pause (G1 Humongous Allocation) (young)2017-06-22T13:55:45.771+0530:
     * 71574.517: [GC concurrent-root-region-scan-end, 0.0181265 secs]
     * 
     * 2021-06-15T13:51:22.274-0600: 39666.928: [GC pause (G1 Evacuation Pause) (young)2021-06-15T13:51:22.274-0600:
     * 39666.928: [GC concurrent-root-region-scan-end, 0.0005374 secs]
     * 
     * 537.122: [GC pause (G1 Evacuation Pause) (young)537.123: [GC concurrent-root-region-scan-start]
     */
    private static final String REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT = "^(" + JdkRegEx.DECORATOR
            + " \\[GC pause( \\((" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC
            + "|" + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + ")\\))? \\(young\\))(" + JdkRegEx.DECORATOR
            + " \\[GC concurrent-(root-region-scan|cleanup|mark)-(start|end)(, " + JdkRegEx.DURATION + ")?\\])[ ]*$";

    private static final Pattern REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT);

    /**
     * Regular expression for retained beginning G1_YOUNG_INITIAL_MARK collection.
     * 
     * 2017-01-20T23:18:29.561-0500: 1513296.434: [GC pause (young) (initial-mark), 0.0225230 secs]
     */
    private static final String REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK = "^(" + JdkRegEx.DECORATOR
            + " \\[GC pause( \\((" + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + "|" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE
            + "|" + JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + ")\\))? \\(young\\) \\(initial-mark\\)(, " + JdkRegEx.DURATION
            + "\\])?)( " + JdkRegEx.DECORATOR + " \\[G1Ergonomics.+)?(Before GC RS summary)?[ ]*$";

    private static final Pattern REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK);

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
    private static final String REGEX_RETAIN_BEGINNING_YOUNG_PAUSE = "^(" + JdkRegEx.DECORATOR + " \\[GC pause( \\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION + ")\\))? \\(young\\)( \\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + ")\\))?(, " + JdkRegEx.DURATION + "\\])?)((" + JdkRegEx.DECORATOR
            + " )?( )?" + JdkRegEx.DECORATOR + " \\[G1Ergonomics.+)?(Before GC RS summary)?[ ]*$";

    private static final Pattern REGEX_RETAIN_BEGINNING_YOUNG_PAUSE_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_YOUNG_PAUSE);

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
    private static final String REGEX_RETAIN_END_CONCURRENT_YOUNG = "^(" + JdkRegEx.DECORATOR
            + " \\[GC concurrent-root-region-scan-end, " + JdkRegEx.DURATION + "\\])(( \\("
            + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + "\\))?, " + JdkRegEx.DURATION + "\\]\\[Eden: " + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Survivors: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + " Heap: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\]" + TimesData.REGEX + ")( )?[ ]*$";

    private static final Pattern REGEX_RETAIN_END_CONCURRENT_YOUNG_PATTERN = Pattern
            .compile(REGEX_RETAIN_END_CONCURRENT_YOUNG);

    private static final Pattern REGEX_RETAIN_END_PATTERN = Pattern.compile(REGEX_RETAIN_END);

    /**
     * Regular expression for retained middle collection @link org.eclipselabs.garbagecat.domain.jdk.G1FullGcEvent}
     * and @link org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent}.
     * 
     * , 5.1353878 secs]
     * 
     * Prepended with size information:
     * 
     * 1831M->1213M(5120M), 5.1353878 secs]
     * 
     * 390M->119M(512M)After GC RS summary
     */
    private static final String REGEX_RETAIN_MIDDLE = "^( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)(, " + JdkRegEx.DURATION + "\\])?)(After GC RS summary)?[ ]*$";

    /**
     * Regular expression for retained middle PRINT_CLASS_HISTOGRAM.
     * 
     * 2021-10-07T10:05:58.708+0100: 69326.814: [Class Histogram (after full gc):
     */
    private static final String REGEX_RETAIN_MIDDLE_CLASS_HISTOGRAM = "^(" + JdkRegEx.DECORATOR
            + " \\[Class Histogram \\(after full gc\\):)[ ]*$";

    private static final Pattern REGEX_RETAIN_MIDDLE_CLASS_HISTOGRAM_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_CLASS_HISTOGRAM);

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

    private static final Pattern REGEX_RETAIN_MIDDLE_DURATION_PATTERN = Pattern.compile(REGEX_RETAIN_MIDDLE_DURATION);

    /**
     * Regular expression for retained middle.
     * 
     * [Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 136.9M(30.0G)->70.9M(30.0G)]
     * 
     * [Eden: 4096M(4096M)->0B(3528M) Survivors: 0B->568M Heap: 4096M(16384M)->567M(16384M)]
     * 
     * [Eden: 306,0M(306,0M)->0,0B(266,0M) Survivors: 0,0B->40,0M Heap: 306,0M(6144,0M)->57,7M(6144,0M)]
     * 
     * [Eden: 448.0M(7936.0M)->0.0B(7936.0M) Survivors: 0.0B->0.0B Heap: 8185.5M(31.0G)->7616.3M(31.0G)], [Metaspace:
     * 668658K->668658K(1169408K)]
     */
    private static final String REGEX_RETAIN_MIDDLE_EDEN = "^   (\\[Eden: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) Survivors: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + " Heap: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\](, \\[(Perm|Metaspace): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\)\\])?)[ ]*$";

    private static final Pattern REGEX_RETAIN_MIDDLE_EDEN_PATTERN = Pattern.compile(REGEX_RETAIN_MIDDLE_EDEN);

    private static final Pattern REGEX_RETAIN_MIDDLE_PATTERN = Pattern.compile(REGEX_RETAIN_MIDDLE);

    /**
     * Regular expression for retained middle G1_YOUNG_INTIAL_MARK collection.
     * 
     * (initial-mark), 0.12895600 secs]
     */
    private static final String REGEX_RETAIN_MIDDLE_YOUNG_INITIAL_MARK = "^( \\(initial-mark\\), " + JdkRegEx.DURATION
            + "\\])[ ]*$";

    private static final Pattern REGEX_RETAIN_MIDDLE_YOUNG_INITIAL_MARK_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_YOUNG_INITIAL_MARK);

    /**
     * Regular expression for retained middle G1_YOUNG_PAUSE collection.
     * 
     * [ 29M->2589K(59M)]
     */
    private static final String REGEX_RETAIN_MIDDLE_YOUNG_PAUSE = "^   (\\[ " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\)\\])[ ]*$";

    private static final Pattern REGEX_RETAIN_MIDDLE_YOUNG_PAUSE_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_YOUNG_PAUSE);

    /**
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {

            "^   \\[Root Region Scan Waiting:.+$",
            //
            "^   \\[Parallel Time:.+$",
            // Use complete pattern to identify mixed logging (e.g. stopped time at end)
            "^      \\[GC Worker Start \\(ms\\): Min: \\d{1,}[\\.,]\\d, Avg: \\d{1,}[\\.,]\\d, Max: \\d{1,}[\\.,]\\d, "
                    + "Diff: \\d{1,}[\\.,]\\d\\]$",
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
            "^[ ]{0,1}" + JdkRegEx.DECORATOR + " \\[G1Ergonomics.+$",
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

    private static final List<Pattern> THROWAWAY_PATTERN_LIST = new ArrayList<>(REGEX_THROWAWAY.length);

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     * 
     * For example, it is used with the <code>G1PreprocessAction</code> to identify concurrent events intermingled with
     * non-concurrent events to store them in the intermingled log lines list for output after the non-concurrent event.
     */
    public static final String TOKEN = "G1_PREPROCESS_ACTION_TOKEN";

    static {
        for (String regex : REGEX_THROWAWAY) {
            THROWAWAY_PATTERN_LIST.add(Pattern.compile(regex));
        }
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
        if (REGEX_RETAIN_BEGINNING_YOUNG_PAUSE_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_FULL_GC_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_FULL_GC_CLASS_HISTOGRAM_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_CLASS_HISTOGRAM_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_REMARK_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_MIXED_PATTERN.matcher(logLine).matches()
                || (REGEX_RETAIN_BEGINNING_CLEANUP_PATTERN.matcher(logLine).matches()
                        && REGEX_RETAIN_END_PATTERN.matcher(nextLogLine).matches())
                || REGEX_RETAIN_BEGINNING_CONCURRENT_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_FULL_CONCURRENT_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_YOUNG_PAUSE_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_YOUNG_INITIAL_MARK_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_EDEN_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_DURATION_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_END_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_END_CONCURRENT_YOUNG_PATTERN.matcher(logLine).matches()) {
            return true;
        }
        // TODO: Get rid of this and make them throwaway events?
        for (Pattern pattern : THROWAWAY_PATTERN_LIST) {
            if (pattern.matcher(logLine).matches()) {
                return true;
            }
        }
        return false;
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

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

        Matcher matcher;

        // Beginning logging
        if ((matcher = REGEX_RETAIN_BEGINNING_FULL_GC_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_FULL_GC_CLASS_HISTOGRAM_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_CLEANUP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT_PATTERN.matcher(logEntry)).matches()) {
            // Handle concurrent mixed with young collections. See datasets 47-48 and 51-52, 54.
            matcher.reset();
            if (matcher.matches()) {
                entangledLogLines.add(matcher.group(17));
            }
            // Output beginning of young line
            this.logEntry = matcher.group(1);
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_FULL_CONCURRENT_PATTERN.matcher(logEntry)).matches()) {
            // Handle concurrent mixed with full collections. See dataset 74.
            matcher.reset();
            int indexG1FullDecorator = 1;
            int indexFullBlock = 15;
            int indexConcurrentLine = 16;
            int indexConcurrentDecorator = 17;
            int indexConcurrentBlock = 33;
            int indexG1DetailsBlock = 40;
            if (matcher.matches()) {
                if (matcher.group(indexConcurrentDecorator) != null) {
                    entangledLogLines.add(matcher.group(indexConcurrentLine));
                } else {
                    // G1_CONCURRENT timestamp missing. Use G1_FULL timestamp.
                    entangledLogLines.add(matcher.group(indexG1FullDecorator) + matcher.group(indexConcurrentBlock));
                }
            }
            // Output beginning of G1_FULL line
            if (matcher.group(indexG1FullDecorator) != null) {
                if (matcher.group(indexG1DetailsBlock) != null) {
                    this.logEntry = matcher.group(indexG1FullDecorator) + matcher.group(indexFullBlock)
                            + matcher.group(indexG1DetailsBlock);
                } else {
                    this.logEntry = matcher.group(indexG1FullDecorator) + matcher.group(indexFullBlock);
                }
            } else {
                // G1_FULL timestamp missing. Use G1_CONCURRENT timestamp.
                this.logEntry = matcher.group(indexConcurrentDecorator) + " " + matcher.group(indexFullBlock);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_CONCURRENT_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                // Handle concurrent mixed with young collections. See datasets 47-48 and 51-52, 54.
                String decorator;
                if (matcher.group(3) != null) {
                    decorator = matcher.group(2);
                } else if (matcher.group(16) != null) {
                    decorator = matcher.group(17) + ":";
                } else if (matcher.group(38) != null) {
                    decorator = matcher.group(39) + ":";
                } else if (matcher.group(43) != null) {
                    decorator = matcher.group(44) + ": " + matcher.group(63) + ":";
                } else {
                    decorator = matcher.group(66) + ": " + matcher.group(86) + ":";
                }
                if (!context.contains(TOKEN)) {
                    // Output now
                    this.logEntry = decorator + " " + matcher.group(90);
                } else {
                    // Output later
                    entangledLogLines.add(decorator + " " + matcher.group(90));
                }
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_YOUNG_PAUSE_PATTERN.matcher(logEntry)).matches()) {
            // Strip out G1Ergonomics
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_REMARK_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + matcher.group(65);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_MIXED_PATTERN.matcher(logEntry)).matches()) {
            // Strip out G1Ergonomics
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK_PATTERN.matcher(logEntry)).matches()) {
            // Strip out G1Ergonomics
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_YOUNG_PAUSE_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_YOUNG_INITIAL_MARK_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_EDEN_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_CLASS_HISTOGRAM_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
        } else if ((matcher = REGEX_RETAIN_MIDDLE_DURATION_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_RETAIN_END_PATTERN.matcher(logEntry)).matches()) {
            // End of logging event
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            clearEntangledLines(entangledLogLines);
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.remove(TOKEN);
        } else if ((matcher = REGEX_RETAIN_END_CONCURRENT_YOUNG_PATTERN.matcher(logEntry)).matches()) {
            // End of logging event
            matcher.reset();
            if (matcher.matches()) {
                entangledLogLines.add(matcher.group(1));
                this.logEntry = matcher.group(18);
            }
            clearEntangledLines(entangledLogLines);
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.remove(TOKEN);
        }
    }

    /**
     * Convenience method to write out any saved log lines.
     * 
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @return
     */
    private final void clearEntangledLines(List<String> entangledLogLines) {
        if (entangledLogLines != null && !entangledLogLines.isEmpty()) {
            // Output any entangled log lines
            for (String logLine : entangledLogLines) {
                this.logEntry = this.logEntry + Constants.LINE_SEPARATOR + logLine;
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.G1.toString();
    }
}
