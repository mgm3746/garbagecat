/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_PRINT_GC_DETAILS
 * </p>
 *
 * <p>
 * Remove G1 collector verbose logging when <code>-XX:+UseG1GC</code> used in combination with
 * <code>-XX:+PrintGCDetails</code>. It is currently not being used for analysis.
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
 *    [ 8192K->2112K(59M)]
 *  [Times: user=0.01 sys=0.00, real=0.01 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 0.304: [GC pause (young), 0.00376500 secs] 8192K->2112K(59M) [Times: user=0.01 sys=0.00, real=0.01 secs]
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
 *    [Eden: 128.0M(128.0M)->0.0B(112.0M) Survivors: 0.0B->16.0M Heap: 128.0M(30.0G)->24.9M(30.0G)]
 *  [Times: user=0.09 sys=0.02, real=0.03 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2.192: [GC pause (G1 Evacuation Pause) (young), 0.0209631 secs] 128.0M->24.9M(30.0G) [Times: user=0.09 sys=0.02, real=0.03 secs]
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
 *    [Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 415.0M(30.0G)->313.0M(30.0G)]
 *  [Times: user=0.01 sys=0.00, real=0.02 secs]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 5.293: [GC pause (GCLocker Initiated GC) (young), 0.0176868 secs] 415M->313M(30720M) [Times: user=0.01 sys=0.00, real=0.02 secs]
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
 *    [Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 12.9G(30.0G)->11.3G(30.0G)]
 *  [Times: user=0.19 sys=0.00, real=0.05 secs]
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2971.469: [GC remark, 0.2274544 secs] [Times: user=0.22 sys=0.00, real=0.22 secs]
 * </pre>
 * 
 * 6) JDK8 GC Cleanup:
 * </p>
 *
 * <pre>
 * 2972.698: [GC cleanup 13G->12G(30G), 0.0358748 secs]
 *  [Times: user=0.19 sys=0.00, real=0.03 secs]
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * 2972.698: [GC cleanup 13G->12G(30G), 0.0358748 secs] [Times: user=0.19 sys=0.00, real=0.03 secs]
 * </pre>
 * 
 * <p>
 * 7) JDK8 Full GC:
 * </p>
 *
 * <pre>
 * 2016-02-09T06:21:30.379-0500: 27999.141: [Full GC 18G->4153M(26G), 10.1760410 secs]
 *    [Eden: 0.0B(1328.0M)->0.0B(15.6G) Survivors: 0.0B->0.0B Heap: 18.9G(26.0G)->4153.8M(26.0G)]
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
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
 * 8) With G1Ergonomic
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
public class G1PrintGcDetailsPreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning G1_YOUNG_PAUSE collection. Trigger can be before or after "(young)".
     */
    private static final String REGEX_RETAIN_BEGINNING_YOUNG_PAUSE = "^(" + JdkRegEx.TIMESTAMP + ": \\[GC pause( \\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|"
            + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + ")\\))? \\(young\\)( \\((" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE
            + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED + ")\\))?(, "
            + JdkRegEx.DURATION + "\\])?)(( )?" + JdkRegEx.TIMESTAMP + ": \\[G1Ergonomics.+)?$";

    /**
     * Regular expression for retained beginning G1_YOUNG_INITIAL_MARK collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK = "^(" + JdkRegEx.TIMESTAMP
            + ": \\[GC pause( \\((" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + ")\\))? \\(young\\) \\(initial-mark\\)(, "
            + JdkRegEx.DURATION + "\\])?)( " + JdkRegEx.TIMESTAMP + ": \\[G1Ergonomics.+)?$";

    /**
     * Regular expression for retained beginning G1_FULL_GC collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_FULL_GC = "^" + JdkRegEx.TIMESTAMP + ": \\[Full GC (\\("
            + JdkRegEx.TRIGGER_SYSTEM_GC + "\\) )?" + JdkRegEx.SIZE_G1 + "->" + JdkRegEx.SIZE_G1 + "\\("
            + JdkRegEx.SIZE_G1 + "\\), " + JdkRegEx.DURATION + "\\]$";

    /**
     * Regular expression for retained beginning G1_CONCURRENT collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_CONCURRENT = "^(: )?((" + JdkRegEx.TIMESTAMP
            + ": \\[GC concurrent-((root-region-scan|mark|cleanup)-(start|end|abort))(, " + JdkRegEx.DURATION
            + ")?\\]))$";

    /**
     * Regular expression for retained beginning G1_REMARK collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_REMARK = "^(" + JdkRegEx.TIMESTAMP + ": \\[GC remark) "
            + JdkRegEx.TIMESTAMP + ": \\[GC ref-proc, " + JdkRegEx.DURATION + "\\](, " + JdkRegEx.DURATION + "\\])$";

    /**
     * Regular expression for retained beginning G1_MIXED collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_MIXED = "^(" + JdkRegEx.TIMESTAMP + ": \\[GC pause( \\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + ")\\))? \\(mixed\\)(, " + JdkRegEx.DURATION + "\\])?)( "
            + JdkRegEx.TIMESTAMP + ": \\[G1Ergonomics.+)?$";

    /**
     * Regular expression for retained beginning G1_CLEANUP collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_CLEANUP = "^" + JdkRegEx.TIMESTAMP + ": \\[GC cleanup "
            + JdkRegEx.SIZE_G1 + "->" + JdkRegEx.SIZE_G1 + "\\(" + JdkRegEx.SIZE_G1 + "\\), " + JdkRegEx.DURATION
            + "\\]$";

    /**
     * Regular expression for retained beginning G1_YOUNG_PAUSE mixed with G1_CONCURRENT collection.
     */
    private static final String REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT = "^(" + JdkRegEx.TIMESTAMP
            + ": \\[GC pause \\(young\\))(" + JdkRegEx.TIMESTAMP + ": \\[GC concurrent-(root-region-scan|cleanup)-end, "
            + JdkRegEx.DURATION + "\\])$";

    /**
     * Regular expression for retained middle G1_YOUNG_PAUSE collection.
     */
    private static final String REGEX_RETAIN_MIDDLE_YOUNG_PAUSE = "^   \\[( " + JdkRegEx.SIZE_G1 + "->"
            + JdkRegEx.SIZE_G1 + "\\(" + JdkRegEx.SIZE_G1 + "\\))\\]$";

    /**
     * Regular expression for retained middle JDK8.
     */
    private static final String REGEX_RETAIN_MIDDLE_JDK8 = "^   \\[Eden: " + JdkRegEx.SIZE_G1_DETAILS + "\\("
            + JdkRegEx.SIZE_G1_DETAILS + "\\)->" + JdkRegEx.SIZE_G1_DETAILS + "\\(" + JdkRegEx.SIZE_G1_DETAILS
            + "\\) Survivors: " + JdkRegEx.SIZE_G1_DETAILS + "->" + JdkRegEx.SIZE_G1_DETAILS + " Heap: "
            + JdkRegEx.SIZE_G1_DETAILS + "\\(" + JdkRegEx.SIZE_G1_DETAILS + "\\)->" + JdkRegEx.SIZE_G1_DETAILS + "\\("
            + JdkRegEx.SIZE_G1_DETAILS + "\\)\\]$";

    /**
     * Regular expression for retained middle duration.
     */
    private static final String REGEX_RETAIN_MIDDLE_DURATION = "^( \\((" + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED
            + ")\\))?, " + JdkRegEx.DURATION + "\\]$";

    /**
     * Regular expression for retained end.
     */
    private static final String REGEX_RETAIN_END = "^" + JdkRegEx.TIMES_BLOCK + "( )?$";

    /**
     * Regular expressions for lines thrown away.
     */
    private static final String[] REGEX_THROWAWAY = {

            "^   \\[Root Region Scan Waiting:.+$",
            //
            "^   \\[Parallel Time:.+$",
            // JDK8 does not have "Time"
            "^      \\[GC Worker Start( Time)? \\(ms\\):.+$",
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
            "^      \\[GC Worker Other \\(ms\\):.+$",
            //
            "^      \\[GC Worker Total \\(ms\\):.+$",
            // JDK8 does not have "Time"
            "^      \\[GC Worker End( Time)? \\(ms\\):.+$",
            //
            "^      \\[Code Root Scanning \\(ms\\):.+$",
            //
            "^   \\[Code Root Fixup:.+$",
            //
            "^   \\[Code Root Migration:.+$",
            //
            "^      \\[Code Root Marking \\(ms\\):.+$",
            //
            "^   \\[Clear CT:.+$",
            // JDK8 has 3 leading spaces
            "^   (   )?\\[Other:.+$",
            //
            "^      \\[Choose CSet:.+$",
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
            "^         \\[Termination Attempts :.+$",
            // Partial concurrent event. Maybe a logging bug?
            "^\\[GC concurrent.+$",
            //
            "^       Avg:.+$",
            // Ergonomics. 
            "^(:)?( )?(" + JdkRegEx.TIMESTAMP + ":  )?" + JdkRegEx.TIMESTAMP + ": \\[G1Ergonomics.+$" };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;
    
    /**
     * Log entry in the entangle log list to indicate the GC details block started with a non-concurrent event.
     * Inspection of logging has shown that concurrent events can become intermingled with the GC details logging. When
     * this happens, we save the concurrent event(s) in the intermingled log lines list and output them after the
     * non-concurrent event.
     */
    private static final String BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT = "BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT";

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
     */
    public G1PrintGcDetailsPreprocessAction(String priorLogEntry, String logEntry, String nextLogEntr,
            List<String> entangledLogLines) {
        
        // Beginning logging
        if (logEntry.matches(REGEX_RETAIN_BEGINNING_FULL_GC) || logEntry.matches(REGEX_RETAIN_BEGINNING_CLEANUP)) {
            this.logEntry = logEntry;
            if(!entangledLogLines.contains(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT)) {
                entangledLogLines.add(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT);
            }
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT)) {
            // Handle concurrent mixed with young collections. See datasets 47-48 and 51-52, 54.
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                entangledLogLines.add(matcher.group(3) + System.getProperty("line.separator"));
            }
            // Output beginning of young line
            this.logEntry = matcher.group(1);
            if(!entangledLogLines.contains(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT)) {
                entangledLogLines.add(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT);
            }
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_CONCURRENT)) {
            // Strip out any leading colon
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_CONCURRENT);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                logEntry = matcher.group(2) + System.getProperty("line.separator");
            }
            
            // Handle concurrent mixed with young collections. See datasets 47-48 and 51-52, 54.
            if (entangledLogLines.contains(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT)) {
                // save concurrent event to output at the end of the non-concurrent event
                entangledLogLines.add(logEntry);
            } else {
                this.logEntry = logEntry;
            }
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_YOUNG_PAUSE)) {            
            // Strip out G1Ergonomics
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_YOUNG_PAUSE);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                logEntry = matcher.group(1);
                this.logEntry = logEntry;
            }
            if(!entangledLogLines.contains(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT)) {
                entangledLogLines.add(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT);
            }
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_REMARK)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_REMARK);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1) + matcher.group(5);
            }
            if(!entangledLogLines.contains(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT)) {
                entangledLogLines.add(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT);
            }
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_MIXED)) {
            // Strip out G1Ergonomics
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_MIXED);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            if(!entangledLogLines.contains(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT)) {
                entangledLogLines.add(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT);
            }
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK)) {
            // Strip out G1Ergonomics
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            if(!entangledLogLines.contains(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT)) {
                entangledLogLines.add(BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT);
            }
            // Middle logging
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_YOUNG_PAUSE)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_YOUNG_PAUSE);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_JDK8)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_JDK8);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                // For now put logging in standard G1 form (K and M). If standard logging one day has B, G, or
                // decimals, we would want to remove this from preprocessing and expand the normal handling to account
                // for decimals, bytes, and/or gigabytes.
                this.logEntry = " ";
                this.logEntry = this.logEntry
                        + JdkUtil.convertSizeG1DetailsToSizeG1(matcher.group(13), matcher.group(14).charAt(0));
                this.logEntry = this.logEntry + "->";
                this.logEntry = this.logEntry
                        + JdkUtil.convertSizeG1DetailsToSizeG1(matcher.group(17), matcher.group(18).charAt(0));
                this.logEntry = this.logEntry + "(";
                this.logEntry = this.logEntry
                        + JdkUtil.convertSizeG1DetailsToSizeG1(matcher.group(19), matcher.group(20).charAt(0));
                this.logEntry = this.logEntry + ")";
            }
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_DURATION)) {
            this.logEntry = logEntry;
            // End logging
        } else if (logEntry.matches(REGEX_RETAIN_END)) {
            this.logEntry = logEntry + System.getProperty("line.separator");
            clearEntangledLines(entangledLogLines);
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString();
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     *
     * @param logLine
     *            The log line to test.
     * @param priorLogLine
     *            The last log entry processed.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        if (logLine.matches(REGEX_RETAIN_BEGINNING_YOUNG_PAUSE)
                || logLine.matches(REGEX_RETAIN_BEGINNING_YOUNG_INITIAL_MARK)
                || logLine.matches(REGEX_RETAIN_BEGINNING_FULL_GC) || logLine.matches(REGEX_RETAIN_BEGINNING_REMARK)
                || logLine.matches(REGEX_RETAIN_BEGINNING_MIXED) || logLine.matches(REGEX_RETAIN_BEGINNING_CLEANUP)
                || logLine.matches(REGEX_RETAIN_BEGINNING_CONCURRENT)
                || logLine.matches(REGEX_RETAIN_BEGINNING_YOUNG_CONCURRENT)
                || logLine.matches(REGEX_RETAIN_MIDDLE_YOUNG_PAUSE) || logLine.matches(REGEX_RETAIN_MIDDLE_JDK8)
                || logLine.matches(REGEX_RETAIN_MIDDLE_DURATION) || logLine.matches(REGEX_RETAIN_END)) {
            match = true;
        } else {
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
        if (entangledLogLines.size() > 0) {
            // Output any entangled log lines
            Iterator<String> iterator = entangledLogLines.iterator();
            while (iterator.hasNext()) {
                String logLine = iterator.next();
                if (logLine != G1PrintGcDetailsPreprocessAction.BLOCK_STARTED_WITH_NON_CONCURRENT_EVENT) {
                    this.logEntry = this.logEntry + logLine;
                }
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
    }
}