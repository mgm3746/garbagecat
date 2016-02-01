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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestG1PrintGcDetailsPreprocessAction extends TestCase {

    public void testLogLineG1EvacuationPause() {
        String logLine = "2.192: [GC pause (G1 Evacuation Pause) (young)";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineParallelTime() {
        String logLine = "   [Parallel Time: 12.6 ms, GC Workers: 6]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineGcWorkerStart() {
        String logLine = "      [GC Worker Start Time (ms):  807.5  807.8  807.8  810.1]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineGcWorkerStartJdk8() {
        String logLine = "      [GC Worker Start (ms): Min: 2191.9, Avg: 2191.9, Max: 2191.9, Diff: 0.1]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineExtRootScanning() {
        String logLine = "      [Ext Root Scanning (ms): Min: 2.7, Avg: 3.0, Max: 3.5, Diff: 0.8, Sum: 18.1]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineUpdateRs() {
        String logLine = "      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.1]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineProcessedBuffers() {
        String logLine = "         [Processed Buffers : 2 1 0 0";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineProcessedBuffersJdk8() {
        String logLine = "         [Processed Buffers: Min: 0, Avg: 8.0, Max: 39, Diff: 39, Sum: 48]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineScanRs() {
        String logLine = "      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineObjectCopy() {
        String logLine = "      [Object Copy (ms): Min: 9.0, Avg: 9.4, Max: 9.8, Diff: 0.8, Sum: 56.7]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineTermination() {
        String logLine = "      [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineGcWorkerOther() {
        String logLine = "      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.2]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineGcWorkerTotal() {
        String logLine = "      [GC Worker Total (ms): Min: 12.5, Avg: 12.5, Max: 12.6, Diff: 0.1, Sum: 75.3]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineGcWorkerEnd() {
        String logLine = "      [GC Worker End Time (ms):  810.1  810.2  810.1  810.1]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineGcWorkerEndJdk8() {
        String logLine = "      [GC Worker End (ms): Min: 2204.4, Avg: 2204.4, Max: 2204.4, Diff: 0.0]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineCodeRootFixup() {
        String logLine = "   [Code Root Fixup: 0.0 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineClearCt() {
        String logLine = "   [Clear CT: 0.1 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineOther() {
        String logLine = "      [Other:   0.9 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineOtherJdk8() {
        String logLine = "   [Other: 8.2 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineChooseCSet() {
        String logLine = "      [Choose CSet: 0.0 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineRefProc() {
        String logLine = "      [Ref Proc: 7.9 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineRefEnq() {
        String logLine = "      [Ref Enq: 0.1 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineFreeCSet() {
        String logLine = "      [Free CSet: 0.0 ms]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineSum() {
        String logLine = "          Sum: 4, Avg: 1, Min: 1, Max: 1]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineMarkStackScanning() {
        String logLine = "      [Mark Stack Scanning (ms):  0.0  0.0  0.0  0.0";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineTerminationAttempts() {
        String logLine = "         [Termination Attempts : 1 1 1 1";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineAvg() {
        String logLine = "       Avg:   1.1, Min:   0.0, Max:   1.5]   0.0, Min:   0.0, Max:   0.0]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineRetainMiddleJdk8() {
        String logLine = "   [Eden: 128.0M(128.0M)->0.0B(112.0M) Survivors: 0.0B->16.0M "
                + "Heap: 128.0M(30.0G)->24.9M(30.0G)]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }
    
    public void testLogLineRetainMiddleYoung() {
        String logLine = "   [ 29M->2589K(59M)]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineRetainMiddleDuration() {
        String logLine = ", 0.0209631 secs]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testLogLineGCLockerInitiatedGC() {
        String logLine = "5.293: [GC pause (GCLocker Initiated GC) (young)";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }
    
    public void testLogLineFullGC() {
        String logLine = "105.151: [Full GC (System.gc()) 5820M->1381M(30G), 5.5390169 secs]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    } 
    
    public void testLogLineYoungInitialMark() {
        String logLine = "2970.268: [GC pause (G1 Evacuation Pause) (young) (initial-mark)";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }
    
    public void testLogLineSATBFiltering() {
        String logLine = "      [SATB Filtering (ms): Min: 0.0, Avg: 0.1, Max: 0.4, Diff: 0.4, Sum: 0.4]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }
    
    public void testLogRemark() {
        String logLine = "2971.469: [GC remark 2972.470: [GC ref-proc, 0.1656600 secs], 0.2274544 secs]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }
    
    public void testLogMixed() {
        String logLine = "2973.338: [GC pause (G1 Evacuation Pause) (mixed)";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }
    
    public void testLogCleanup() {
        String logLine = "2972.698: [GC cleanup 13G->12G(30G), 0.0358748 secs]";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString() + ".",
                G1PrintGcDetailsPreprocessAction.match(logLine));
    }
}
