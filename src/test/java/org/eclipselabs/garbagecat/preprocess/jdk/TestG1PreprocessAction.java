/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestG1PreprocessAction {

    @Test
    void testLogLineG1EvacuationPause() {
        String logLine = "2.192: [GC pause (G1 Evacuation Pause) (young)";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRootRegionScanWaiting() {
        String logLine = "   [Root Region Scan Waiting: 112.3 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineParallelTime() {
        String logLine = "   [Parallel Time: 12.6 ms, GC Workers: 6]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGcWorker() {
        String logLine = "      [GC Worker (ms):  387,2  387,4  386,2  385,9  386,1  386,2  386,9  386,4  386,4  "
                + "386,8  386,1  385,2  386,1";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGcWorkerStart() {
        String logLine = "      [GC Worker Start Time (ms):  807.5  807.8  807.8  810.1]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGcWorkerStartJdk8() {
        String logLine = "      [GC Worker Start (ms): Min: 2191.9, Avg: 2191.9, Max: 2191.9, Diff: 0.1]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGcWorkerStartWithCommas() {
        String logLine = "      [GC Worker Start (ms): Min: 6349,9, Avg: 6353,8, Max: 6355,9, Diff: 6,0]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineExtRootScanning() {
        String logLine = "      [Ext Root Scanning (ms): Min: 2.7, Avg: 3.0, Max: 3.5, Diff: 0.8, Sum: 18.1]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineUpdateRs() {
        String logLine = "      [Update RS (ms): Min: 0.0, Avg: 0.0, Max: 0.1, Diff: 0.1, Sum: 0.1]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineProcessedBuffers() {
        String logLine = "         [Processed Buffers : 2 1 0 0";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineProcessedBuffersJdk8() {
        String logLine = "         [Processed Buffers: Min: 0, Avg: 8.0, Max: 39, Diff: 39, Sum: 48]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineScanRs() {
        String logLine = "      [Scan RS (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineObjectCopy() {
        String logLine = "      [Object Copy (ms): Min: 9.0, Avg: 9.4, Max: 9.8, Diff: 0.8, Sum: 56.7]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineTermination() {
        String logLine = "      [Termination (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.1]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGcWorkerOther() {
        String logLine = "      [GC Worker Other (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.2]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGcWorkerTotal() {
        String logLine = "      [GC Worker Total (ms): Min: 12.5, Avg: 12.5, Max: 12.6, Diff: 0.1, Sum: 75.3]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGcWorkerEnd() {
        String logLine = "      [GC Worker End Time (ms):  810.1  810.2  810.1  810.1]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGcWorkerEndJdk8() {
        String logLine = "      [GC Worker End (ms): Min: 2204.4, Avg: 2204.4, Max: 2204.4, Diff: 0.0]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineCodeRootFixup() {
        String logLine = "   [Code Root Fixup: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineCodeRootPurge() {
        String logLine = "   [Code Root Purge: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineCodeRootMigration() {
        String logLine = "   [Code Root Migration: 0.8 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineCodeRootScanning() {
        String logLine = "      [Code Root Scanning (ms): Min: 0.0, Avg: 0.2, Max: 0.4, Diff: 0.4, Sum: 0.8]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineCodeRootMarking() {
        String logLine = "      [Code Root Marking (ms): Min: 0.1, Avg: 1.8, Max: 3.7, Diff: 3.7, Sum: 7.2]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineClearCt() {
        String logLine = "   [Clear CT: 0.1 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineCompleteCsetMarking() {
        String logLine = "   [Complete CSet Marking:   0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineOther() {
        String logLine = "      [Other:   0.9 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineOtherJdk8() {
        String logLine = "   [Other: 8.2 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineChooseCSet() {
        String logLine = "      [Choose CSet: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRefProc() {
        String logLine = "      [Ref Proc: 7.9 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRefEnq() {
        String logLine = "      [Ref Enq: 0.1 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineFreeCSet() {
        String logLine = "      [Free CSet: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineSum() {
        String logLine = "          Sum: 4, Avg: 1, Min: 1, Max: 1]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineMarkStackScanning() {
        String logLine = "      [Mark Stack Scanning (ms):  0.0  0.0  0.0  0.0";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineTerminationAttempts() {
        String logLine = "         [Termination Attempts : 1 1 1 1";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineTerminationAttemptsNoSpaceBeforeColon() {
        String logLine = "         [Termination Attempts: Min: 274, Avg: 618.2, Max: 918, Diff: 644, Sum: 11127]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineAvg() {
        String logLine = "       Avg:   1.1, Min:   0.0, Max:   1.5]   0.0, Min:   0.0, Max:   0.0]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogEvacuationFailure() {
        String logLine = "      [Evacuation Failure: 2381.8 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRetainMiddleJdk8() {
        String logLine = "   [Eden: 128.0M(128.0M)->0.0B(112.0M) Survivors: 0.0B->16.0M "
                + "Heap: 128.0M(30.0G)->24.9M(30.0G)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRetainMiddleYoung() {
        String logLine = "   [ 29M->2589K(59M)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRetainMiddleDuration() {
        String logLine = ", 0.0209631 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRetainMiddleDurationWithToSpaceExhaustedTrigger() {
        String logLine = " (to-space exhausted), 0.3857580 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRetainMiddleDurationWithToSpaceOverflowTrigger() {
        String logLine = " (to-space overflow), 0.77121400 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGCLockerInitiatedGC() {
        String logLine = "5.293: [GC pause (GCLocker Initiated GC) (young)";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineToSpaceExhausted() {
        String logLine = "27997.968: [GC pause (young) (to-space exhausted), 0.1208740 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineFullGC() {
        String logLine = "105.151: [Full GC (System.gc()) 5820M->1381M(30G), 5.5390169 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoungInitialMark() {
        String logLine = "2970.268: [GC pause (G1 Evacuation Pause) (young) (initial-mark)";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineSATBFiltering() {
        String logLine = "      [SATB Filtering (ms): Min: 0.0, Avg: 0.1, Max: 0.4, Diff: 0.4, Sum: 0.4]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogRemark() {
        String logLine = "2971.469: [GC remark 2972.470: [GC ref-proc, 0.1656600 secs], 0.2274544 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogMixed() {
        String logLine = "2973.338: [GC pause (G1 Evacuation Pause) (mixed)";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogMixedNoTrigger() {
        String logLine = "3082.652: [GC pause (mixed), 0.0762060 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogCleanup() {
        String logLine = "2972.698: [GC cleanup 13G->12G(30G), 0.0358748 secs]";
        String nextLogLine = " [Times: user=0.33 sys=0.04, real=0.17 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineConcurrent() {
        String logLine = "27744.494: [GC concurrent-mark-start]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoungPauseWithToSpaceExhaustedTrigger() {
        String logLine = "27997.968: [GC pause (young) (to-space exhausted), 0.1208740 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoungPauseDoubleTriggerToSpaceExhausted() {
        String logLine = "6049.175: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 3.1713585 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineFullGcNoTrigger() {
        String logLine = "27999.141: [Full GC 18G->4153M(26G), 10.1760410 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineMixedYoungPauseWithConcurrentRootRegionScanEnd() {
        String logLine = "4969.943: [GC pause (young)4970.158: [GC concurrent-root-region-scan-end, 0.5703200 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineMixedYoungPauseWithConcurrentWithDatestamps() {
        String logLine = "2016-02-09T06:17:15.619-0500: 27744.381: [GC pause (young)"
                + "2016-02-09T06:17:15.732-0500: 27744.494: [GC concurrent-root-region-scan-end, 0.3550210 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineMixedYoungPauseWithConcurrentCleanupEnd() {
        String logLine = "6554.823: [GC pause (young)6554.824: [GC concurrent-cleanup-end, 0.0029080 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1ErgonomicsWithDatestamp() {
        String logLine = "2016-02-11T17:26:43.599-0500: 12042.669: [G1Ergonomics (CSet Construction) start choosing "
                + "CSet, _pending_cards: 250438, predicted base time: 229.38 ms, remaining time: 270.62 ms, target "
                + "pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoungPauseWithG1Ergonomics() {
        String logLine = "72945.823: [GC pause (young) 72945.823: [G1Ergonomics (CSet Construction) start choosing "
                + "CSet, _pending_cards: 497394, predicted base time: 66.16 ms, remaining time: 433.84 ms, target "
                + "pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoungPauseWithG1ErgonomicsAndDateStamps() {
        String logLine = "2016-02-16T01:02:06.283-0500: 16023.627: [GC pause (young)2016-02-16T01:02:06.338-0500:  "
                + "16023.683: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 36870, predicted "
                + "base time: 143.96 ms, remaining time: 856.04 ms, target pause time: 1000.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoungPauseWithTriggerWithG1ErgonomicsDoubleTimestampAndDateStamps() {
        String logLine = "2017-03-21T15:05:53.717+1100: 425001.630: [GC pause (G1 Evacuation Pause) (young)"
                + "2017-03-21T15:05:53.717+1100: 425001.630:  425001.630: [G1Ergonomics (CSet Construction) start "
                + "choosing CSet, _pending_cards: 3, predicted base time: 45.72 ms, remaining time: 304.28 ms, target "
                + "pause time: 350.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoungPauseTriggerHumongousAllocationWithG1Ergonomics() {
        String priorLogLine = "";
        String logLine = "2020-02-16T23:24:09.668+0000: 880272.698: [GC pause (G1 Humongous Allocation) (young) "
                + "880272.699: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 241090, "
                + "predicted base time: 129.61 ms, remaining time: 70.39 ms, target pause time: 200.00 ms]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2020-02-16T23:24:09.668+0000: 880272.698: [GC pause (G1 Humongous Allocation) (young)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineYoungInitialMarkWithDatestamp() {
        String logLine = "2017-01-20T23:18:29.561-0500: 1513296.434: [GC pause (young) (initial-mark), "
                + "0.0225230 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoungInitialMarkWithG1Ergonomics() {
        String logLine = "2016-02-11T15:22:23.213-0500: 4582.283: [GC pause (young) (initial-mark) 4582.283: "
                + "[G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: 6084, predicted base time: "
                + "41.16 ms, remaining time: 458.84 ms, target pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoungInitialMarkTriggerG1HumongousAllocationWithG1Ergonomics() {
        String logLine = "2017-02-02T01:55:56.661-0500: 860.367: [GC pause (G1 Humongous Allocation) (young) "
                + "(initial-mark) 860.367: [G1Ergonomics (CSet Construction) start choosing CSet, _pending_cards: "
                + "3305091, predicted base time: 457.90 ms, remaining time: 42.10 ms, target pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineMixedWithG1Ergonomics() {
        String logLine = "2016-02-11T16:06:59.987-0500: 7259.058: [GC pause (mixed) 7259.058: [G1Ergonomics (CSet "
                + "Construction) start choosing CSet, _pending_cards: 273214, predicted base time: 74.01 ms, "
                + "remaining time: 425.99 ms, target pause time: 500.00 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineMixedHumongousAllocationToSpaceExhausted() {
        String logLine = "2017-06-22T12:25:26.515+0530: 66155.261: [GC pause (G1 Humongous Allocation) (mixed) "
                + "(to-space exhausted), 0.2466797 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineConcurrentCleanupEndWithDatestamp() {
        String logLine = "2016-02-11T18:15:35.431-0500: 14974.501: [GC concurrent-cleanup-end, 0.0033880 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineSingleConcurrentMarkStartBlock() {
        String logLine = "[GC concurrent-mark-start]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineGcConcurrentRootRegionScanEndMissingTimestamp() {
        String logLine = "[GC concurrent-root-region-scan-end, 0.6380480 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineBeginningYoungConcurrent() {
        String logLine = "2016-02-16T01:05:36.945-0500: 16233.809: [GC pause (young)2016-02-16T01:05:37.046-0500: "
                + "16233.910: [GC concurrent-root-region-scan-end, 0.5802520 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineTimesBlock() {
        String logLine = " [Times: user=0.33 sys=0.04, real=0.17 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineTimesBlockWithSpaceAtEnd() {
        String logLine = " [Times: user=0.33 sys=0.04, real=0.17 secs] ";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1Cleanup() {
        String logLine = "1.515: [GC cleanup 165M->165M(110G), 0.0028925 secs]";
        String nextLogLine = "2.443: [GC pause (GCLocker Initiated GC) (young) (initial-mark) 1061M->52M(110G), "
                + "0.0280096 secs]";
        assertFalse(G1PreprocessAction.match(logLine, null, nextLogLine),
                "Log line recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineStringDedupFixup() {
        String logLine = "   [String Dedup Fixup: 1.6 ms, GC Workers: 18]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineStringDedupFixupQueueFixup() {
        String logLine = "      [Queue Fixup (ms): Min: 0.0, Avg: 0.0, Max: 0.0, Diff: 0.0, Sum: 0.0]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineStringDedupFixupTableFixup() {
        String logLine = "      [Table Fixup (ms): Min: 0.0, Avg: 0.1, Max: 1.3, Diff: 1.3, Sum: 1.3]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRedirtyCards() {
        String logLine = "      [Redirty Cards: 0.6 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineHumongousRegister() {
        String logLine = "      [Humongous Register: 0.1 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineHumongousReclaim() {
        String logLine = "      [Humongous Reclaim: 0.0 ms]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRemarkWithFinalizeMarkingAndUnloading() {
        String logLine = "5.745: [GC remark 5.746: [Finalize Marking, 0.0068506 secs] 5.752: "
                + "[GC ref-proc, 0.0014064 secs] 5.754: [Unloading, 0.0057674 secs], 0.0157938 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineLastExec() {
        String logLine = "   [Last Exec: 0.0118158 secs, Idle: 0.9330710 secs, Blocked: 0/0.0000000 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineInspected() {
        String logLine = "      [Inspected:           10116]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineSkipped() {
        String logLine = "         [Skipped:              0(  0.0%)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineHashed() {
        String logLine = "         [Hashed:            3088( 30.5%)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineKnown() {
        String logLine = "         [Known:             3404( 33.6%)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineNew() {
        String logLine = "         [New:               6712( 66.4%)    526.1K]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineDuplicated() {
        String logLine = "      [Deduplicated:         3304( 49.2%)    197.2K( 37.5%)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineYoung() {
        String logLine = "         [Young:             3101( 93.9%)    173.8K( 88.1%)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineOld() {
        String logLine = "         [Old:                203(  6.1%)     23.4K( 11.9%)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineTotalExec() {
        String logLine = "   [Total Exec: 2/0.0281081 secs, Idle: 2/9.1631547 secs, Blocked: 2/0.0266213 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineTable() {
        String logLine = "   [Table]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineMemoryUsage() {
        String logLine = "      [Memory Usage: 745.2K]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineSize() {
        String logLine = "      [Size: 16384, Min: 1024, Max: 16777216]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineEntries() {
        String logLine = "      [Entries: 26334, Load: 160.7%, Cached: 0, Added: 26334, Removed: 0]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineResizeCount() {
        String logLine = "      [Resize Count: 4, Shrink Threshold: 10922(66.7%), Grow Threshold: 32768(200.0%)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineRehashCount() {
        String logLine = "      [Rehash Count: 0, Rehash Threshold: 120, Hash Seed: 0x0]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineAgeThreshold() {
        String logLine = "      [Age Threshold: 3]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineQueue() {
        String logLine = "   [Queue]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineDropped() {
        String logLine = "      [Dropped: 0]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineSpaceDetailsWithPerm() {
        String logLine = "   [Eden: 143.0M(1624.0M)->0.0B(1843.0M) Survivors: 219.0M->0.0B "
                + "Heap: 999.5M(3072.0M)->691.1M(3072.0M)], [Perm: 175031K->175031K(175104K)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineSpaceNoDetailsWithoutPerm() {
        String logLine = "   [Eden: 4096M(4096M)->0B(3528M) Survivors: 0B->568M Heap: 4096M(16384M)->567M(16384M)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1FullCombinedConcurrentRootRegionScanStart() {
        String logLine = "88.123: [Full GC (Metadata GC Threshold) 88.123: [GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineFullCombinedConcurrentRootRegionScanEndWithDuration() {
        String logLine = "93.315: [Full GC (Metadata GC Threshold) 93.315: "
                + "[GC concurrent-root-region-scan-end, 0.0003872 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineEndOfFullCollection() {
        String logLine = " 1831M->1213M(5120M), 5.1353878 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineSpaceDetailsWithMetaspace() {
        String logLine = "   [Eden: 0.0B(1522.0M)->0.0B(2758.0M) Survivors: 244.0M->0.0B "
                + "Heap: 1831.0M(5120.0M)->1213.5M(5120.0M)], [Metaspace: 396834K->324903K(1511424K)]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1YoungInitialMarkTriggerMetaGcThreshold() {
        String logLine = "87.830: [GC pause (Metadata GC Threshold) (young) (initial-mark), 0.2932700 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineMiddleG1FullWithSizeInformation() {
        String logLine = " 1831M->1213M(5120M), 5.1353878 secs]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        G1PreprocessAction action = new G1PreprocessAction(null, logLine, null, null, context);
        String preprocessedLine = "1831M->1213M(5120M), 5.1353878 secs]";
        assertEquals(preprocessedLine, action.getLogEntry(), "Preprocessing failed.");
    }

    @Test
    void testLogLineG1FullTriggerLastDitchCollection2SpacesAfterTrigger() {
        String logLine = "98.150: [Full GC (Last ditch collection)  1196M->1118M(5120M), 4.4628626 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1FullTriggerJvmTiForcedGarbageCollection() {
        String logLine = "102.621: [Full GC (JvmtiEnv ForceGarbageCollection)  1124M->1118M(5120M), 3.8954775 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1FullTriggerMetaDataGcThresholdMixedConcurrentRootRegionScanEnd() {
        String logLine = "290.944: [Full GC (Metadata GC Threshold) 290.944: "
                + "[GC concurrent-root-region-scan-end, 0.0003793 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1FullCombinedConcurrentRootRegionScanStartMissingTimestamp() {
        String logLine = "298.027: [Full GC (Metadata GC Threshold) [GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1FullTriggerMetaDataGcThreshold() {
        String logLine = "4708.816: [Full GC (Metadata GC Threshold)  801M->801M(5120M), 3.5048336 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1YoungInitialMarkTriggerGcLockerInitiatedGc() {
        String logLine = "6896.482: [GC pause (GCLocker Initiated GC) (young) (initial-mark), 0.0525160 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1YoungConcurrentTriggerG1HumongousAllocation() {
        String logLine = "2017-06-22T13:55:45.753+0530: 71574.499: [GC pause (G1 Humongous Allocation) (young)"
                + "2017-06-22T13:55:45.771+0530: 71574.517: [GC concurrent-root-region-scan-end, 0.0181265 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1ConcurrentWithDatestamp() {
        String logLine = "2016-02-09T06:17:15.377-0500: 27744.139: [GC concurrent-root-region-scan-start]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineFullGcPrintClassHistogram() {
        String priorLogLine = "";
        String logLine = "49689.217: [Full GC49689.217: [Class Histogram (before full gc):";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("49689.217: [Full GC49689.217: [Class Histogram (before full gc):", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLinePrintClassHistogramSpaceAtEnd() {
        String priorLogLine = "";
        String logLine = "49709.036: [Class Histogram (after full gc): ";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("49709.036: [Class Histogram (after full gc):", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLinePrintClassHistogramDatestamp() {
        String priorLogLine = "   [Eden: 448.0M(7936.0M)->0.0B(7936.0M) Survivors: 0.0B->0.0B Heap: "
                + "8185.5M(31.0G)->7616.3M(31.0G)], [Metaspace: 668658K->668658K(1169408K)]";
        String logLine = "2021-10-07T10:05:58.708+0100: 69326.814: [Class Histogram (after full gc): ";
        String nextLogLine = " num     #instances         #bytes  class name";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2021-10-07T10:05:58.708+0100: 69326.814: [Class Histogram (after full gc):", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineYoungPause() {
        String priorLogLine = "";
        String logLine = "785,047: [GC pause (young), 0,73936800 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineYoungPauseMixedConcurrentMarkEnd() {
        String priorLogLine = "";
        String logLine = "188935.313: [GC pause (G1 Evacuation Pause) (young)"
                + "188935.321: [GC concurrent-mark-end, 0.4777427 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("188935.313: [GC pause (G1 Evacuation Pause) (young)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineYoungPauseMixedConcurrentRootRegiaonScanEnd() {
        String priorLogLine = "";
        String logLine = "2021-06-15T13:51:22.274-0600: 39666.928: [GC pause (G1 Evacuation Pause) (young)"
                + "2021-06-15T13:51:22.274-0600: 39666.928: [GC concurrent-root-region-scan-end, 0.0005374 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2021-06-15T13:51:22.274-0600: 39666.928: [GC pause (G1 Evacuation Pause) (young)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineYoungPauseMixedConcurrentRootRegionScanStart() {
        String priorLogLine = "";
        String logLine = "537.122: [GC pause (G1 Evacuation Pause) (young)"
                + "537.123: [GC concurrent-root-region-scan-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("537.122: [GC pause (G1 Evacuation Pause) (young)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineConcurrentMixedYoungPauseToSpaceExhaustedEnd() {
        String priorLogLine = "";
        String logLine = "537.142: [GC concurrent-root-region-scan-end, 0.0189841 secs] (to-space exhausted), "
                + "0.3314995 secs][Eden: 0.0B(151.0M)->0.0B(153.0M) Survivors: 2048.0K->0.0B Heap: "
                + "3038.7M(3072.0M)->3038.7M(3072.0M)] [Times: user=0.20 sys=0.00, real=0.33 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(
                " (to-space exhausted), 0.3314995 secs][Eden: 0.0B(151.0M)->0.0B(153.0M) "
                        + "Survivors: 2048.0K->0.0B Heap: 3038.7M(3072.0M)->3038.7M(3072.0M)] "
                        + "[Times: user=0.20 sys=0.00, real=0.33 secs]" + Constants.LINE_SEPARATOR
                        + "537.142: [GC concurrent-root-region-scan-end, 0.0189841 secs]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineConcurrentMixedYoungPauseEndNoToSpaceExhausted() {
        String priorLogLine = "";
        String logLine = "2132.962: [GC concurrent-root-region-scan-end, 0.0001111 secs], 0.1083307 secs]"
                + "[Eden: 0.0B(153.0M)->0.0B(153.0M) Survivors: 0.0B->0.0B Heap: 3035.6M(3072.0M)->3035.6M(3072.0M)] "
                + "[Times: user=0.09 sys=0.00, real=0.11 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(
                ", 0.1083307 secs]" + "[Eden: 0.0B(153.0M)->0.0B(153.0M) Survivors: 0.0B->0.0B "
                        + "Heap: 3035.6M(3072.0M)->3035.6M(3072.0M)] [Times: user=0.09 sys=0.00, real=0.11 secs]"
                        + Constants.LINE_SEPARATOR + "2132.962: [GC concurrent-root-region-scan-end, 0.0001111 secs]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineG1RemarkDatestamps() {
        String logLine = "2016-03-14T16:06:13.991-0700: 5.745: [GC remark 2016-03-14T16:06:13.991-0700: 5.746: "
                + "[Finalize Marking, 0.0068506 secs] 2016-03-14T16:06:13.998-0700: 5.752: [GC ref-proc, "
                + "0.0014064 secs] 2016-03-14T16:06:14.000-0700: 5.754: [Unloading, 0.0057674 secs], 0.0157938 secs]";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1FullDatestamp() {
        String logLine = "2016-10-31T14:09:15.030-0700: 49689.217: [Full GC"
                + "2016-10-31T14:09:15.030-0700: 49689.217: [Class Histogram (before full gc):";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineFullMixedConcurrentNoSpaceAfterTrigger() {
        String logLine = "2017-02-27T02:55:32.523+0300: 35911.404: [Full GC (Allocation Failure)"
                + "2017-02-27T02:55:32.524+0300: 35911.405: [GC concurrent-root-region-scan-end, 0.0127300 secs]";
        String nextLogLine = "2017-02-27T02:55:32.524+0300: 35911.405: [GC concurrent-mark-start]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2017-02-27T02:55:32.523+0300: 35911.404: [Full GC (Allocation Failure)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineFullMixedConcurrentMiddle() {
        String logLine = "35420.674: [Full GC (Allocation Failure) 35420.734: "
                + "[GC concurrent-mark-start]3035M->3030M(3072M), 21.7552521 secs]"
                + "[Eden: 0.0B(153.0M)->0.0B(153.0M) Survivors: 0.0B->0.0B Heap: 3035.5M(3072.0M)->3030.4M(3072.0M)], "
                + "[Metaspace: 93308K->93308K(352256K)] [Times: user=16.39 sys=0.04, real=21.75 secs]";
        String nextLogLine = "2132.960: [GC pause (G1 Evacuation Pause) (young)2132.962: "
                + "[GC concurrent-root-region-scan-start]";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(
                "35420.674: [Full GC (Allocation Failure) 3035M->3030M(3072M), 21.7552521 secs]"
                        + "[Eden: 0.0B(153.0M)->0.0B(153.0M) Survivors: 0.0B->0.0B"
                        + " Heap: 3035.5M(3072.0M)->3030.4M(3072.0M)], [Metaspace: 93308K->93308K(352256K)] "
                        + "[Times: user=16.39 sys=0.04, real=21.75 secs]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineConcurrentWithDatestamp() {
        String logLine = "2017-02-27T02:55:32.524+0300: 35911.405: [GC concurrent-mark-start]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2017-02-27T02:55:32.524+0300: 35911.405: [GC concurrent-mark-start]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleInitialMark() {
        String logLine = " (initial-mark), 0.12895600 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMixedYoungPauseWithConcurrentRootRegionScanEndWithDatestamps() {
        String logLine = "2017-06-01T03:09:18.078-0400: 3978.886: [GC pause (GCLocker Initiated GC) (young)"
                + "2017-06-01T03:09:18.081-0400: 3978.888: [GC concurrent-root-region-scan-end, 0.0059070 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2017-06-01T03:09:18.078-0400: 3978.886: [GC pause (GCLocker Initiated GC) (young)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineG1YoungPauseMixedG1SummarizeRSetStatsBeforeRsSummary() {
        String logLine = "0.449: [GC pause (G1 Evacuation Pause) (young)Before GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("0.449: [GC pause (G1 Evacuation Pause) (young)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineG1YoungInitialMarkMixedG1SummarizeRSetStatsBeforeRsSummary() {
        String logLine = "1.738: [GC pause (Metadata GC Threshold) (young) (initial-mark)Before GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("1.738: [GC pause (Metadata GC Threshold) (young) (initial-mark)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningG1FullMixedG1SummarizeRSetStatsBeforeRsSummary() {
        String logLine = "73.164: [Full GC (System.gc()) Before GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("73.164: [Full GC (System.gc())", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleG1FullMixedG1SummarizeRSetStatsAfterRsSummary() {
        String logLine = " 390M->119M(512M)After GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("390M->119M(512M)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineG1MixedPauseMixedG1SummarizeRSetStatsBeforeRsSummary() {
        String logLine = "2017-06-28T18:24:40.453-0400: 12289.351: [GC pause (G1 Evacuation Pause) (mixed)"
                + "Before GC RS summary";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        G1PreprocessAction event = new G1PreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2017-06-28T18:24:40.453-0400: 12289.351: [GC pause (G1 Evacuation Pause) (mixed)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRecentRefinementStats() {
        String logLine = " Recent concurrent refinement statistics";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsProcessedCards() {
        String logLine = "  Processed 0 cards";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCompletedBuffersHeading() {
        String logLine = "  Of 2736 completed buffers:";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCompletedBuffersHeadingDigits3() {
        String logLine = "  Of 170 completed buffers:";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRsThreads() {
        String logLine = "         2736 ( 94.3%) by concurrent RS threads.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRsThreadsPercent100() {
        String logLine = "          170 (100.0%) by concurrent RS threads.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsMutatorThreads() {
        String logLine = "            0 (  0.0%) by mutator threads.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCoarsenings() {
        String logLine = "  Did 0 coarsenings.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCoarseningsDigits3() {
        String logLine = "  Did 239 coarsenings.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRsThreadTimesHeading() {
        String logLine = "  Concurrent RS threads times (s)";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRsThreadTimesOutput() {
        String logLine = "          0.00     0.00     0.00     0.00     0.00     0.00     0.00     0.00";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsSamplingThreadTimesHeading() {
        String logLine = "  Concurrent sampling threads times (s)";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsSamplingThreadTimesOutput() {
        String logLine = "          0.00";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsSamplingThreadTimesOutputDigits2() {
        String logLine = "         13.33";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRSetHeading() {
        String logLine = " Current rem set statistics";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRSetTotalKMaxB() {
        String logLine = "  Total per region rem sets sizes = 1513K. Max = 6336B.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRSetTotalMMaxK() {
        String logLine = "  Total per region rem sets sizes = 38M. Max = 25K.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRSetTotalKMaxK() {
        String logLine = "  Total per region rem sets sizes = 1606K. Max = 14K.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRSetYoung() {
        String logLine = "          78K (  5.2%) by 25 Young regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRSetHumongous() {
        String logLine = "           0B (  0.0%) by 0 Humonguous regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRSetFree() {
        String logLine = "        1434K ( 94.8%) by 487 Free regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRSetFreeMDigits4() {
        String logLine = "          36M ( 94.9%) by 3709 Free regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRSetOld() {
        String logLine = "           0B (  0.0%) by 0 Old regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsStaticStructures() {
        String logLine = "   Static structures = 64K, free_lists = 0B.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsOccupiedCards() {
        String logLine = "    0 occupied cards represented.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsOccupiedCardsDigits9() {
        String logLine = "    122457800 occupied cards represented.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsOccupiedCardsYoung() {
        String logLine = "            0 (  0.0%) entries by 25 Young regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsOccupiedCardsHumongous() {
        String logLine = "            0 (  0.0%) entries by 0 Humonguous regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsOccupiedCardsFree() {
        String logLine = "            0 (  0.0%) entries by 487 Free regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsOccupiedCardsOld() {
        String logLine = "            0 (  0.0%) entries by 0 Old regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsOccupiedCardsOldDigits9() {
        String logLine = "     122327000 (100.0%) entries by 1171 Old regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRegionLargestRset() {
        String logLine = "    Region with largest rem set = 511:(E)[0x00000000dff00000,0x00000000e0000000,"
                + "0x00000000e0000000], size = 6336B, occupied = 0B.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsHeapTotal() {
        String logLine = "  Total heap region code root sets sizes = 13K.  Max = 3336B.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCodeRoots() {
        String logLine = "    205 code roots represented.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCodeRootsDigits5() {
        String logLine = "    12153 code roots represented.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCodeRootsYoung() {
        String logLine = "            7 (  2.8%) elements by 4 Young regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCodeRootsHumongous() {
        String logLine = "            0 (  0.0%) elements by 0 Humonguous regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCodeRootsFree() {
        String logLine = "            0 (  0.0%) elements by 493 Free regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsCodeRootsOld() {
        String logLine = "          242 ( 97.2%) elements by 15 Old regions";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsRegionLargestCodeRoots() {
        String logLine = "    Region with largest amount of code roots = 511:(E)[0x00000000dff00000,"
                + "0x00000000e0000000,0x00000000e0000000], size = 3336B, num_elems = 136.";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineG1SummarizeRSetStatsAfterRsSummaryHeading() {
        String logLine = "After GC RS summary";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    @Test
    void testLogLineFullGcClassHistogram() {
        String logLine = "2021-10-07T10:05:34.135+0100: 69302.241: [Full GC (Heap Dump Initiated GC) "
                + "2021-10-07T10:05:34.135+0100: 69302.241: [Class Histogram (before full gc):";
        assertTrue(G1PreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.G1.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseLogging() {
        File testFile = TestUtil.getFile("dataset32.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertEquals((long) 0, jvmRun.getInvertedParallelismCount(), "Inverted parallelism event count not correct.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE with G1_EVACUATION_PAUSE trigger.
     * 
     */
    @Test
    void testG1PreprocessActionG1EvacuationPauseLogging() {
        File testFile = TestUtil.getFile("dataset34.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE with GCLOCKER_INITIATED_GC trigger.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithGCLockerInitiatedGCLogging() {
        File testFile = TestUtil.getFile("dataset35.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertEquals((long) 1, jvmRun.getInvertedParallelismCount(), "Inverted parallelism event count not correct.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PARALLELISM_INVERTED),
                Analysis.WARN_PARALLELISM_INVERTED + " analysis not identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_FULL_GC.
     * 
     */
    @Test
    void testG1PreprocessActionG1FullGCLogging() {
        File testFile = TestUtil.getFile("dataset36.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_EXPLICIT_GC_SERIAL_G1),
                Analysis.ERROR_EXPLICIT_GC_SERIAL_G1 + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_G1),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis incorrectly identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_INITIAL_MARK.
     * 
     */
    @Test
    void testG1PreprocessActionYoungInitialMarkLogging() {
        File testFile = TestUtil.getFile("dataset37.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_INITIAL_MARK.
     * 
     */
    @Test
    void testG1PreprocessActionG1InitialMarkWithCodeRootLogging() {
        File testFile = TestUtil.getFile("dataset43.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_REMARK.
     * 
     */
    @Test
    void testG1PreprocessActionRemarkLogging() {
        File testFile = TestUtil.getFile("dataset38.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_REMARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_MIXED_PAUSE with G1_EVACUATION_PAUSE trigger.
     * 
     */
    @Test
    void testG1PreprocessActionMixedPauseLogging() {
        File testFile = TestUtil.getFile("dataset39.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_MIXED_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_CLEANUP.
     * 
     */
    @Test
    void testG1PreprocessActionCleanupLogging() {
        File testFile = TestUtil.getFile("dataset40.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CLEANUP),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT.
     * 
     */
    @Test
    void testG1PreprocessActionConcurrentLogging() {
        File testFile = TestUtil.getFile("dataset44.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE with TO_SPACE_EXHAUSTED trigger.
     * 
     */
    @Test
    void testG1PreprocessActionToSpaceExhaustedLogging() {
        File testFile = TestUtil.getFile("dataset45.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_G1_EVACUATION_FAILURE),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_MIXED_PAUSE with no trigger.
     * 
     */
    @Test
    void testG1PreprocessActionMixedPauseNoTriggerLogging() {
        File testFile = TestUtil.getFile("dataset46.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_MIXED_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT.
     * 
     */
    @Test
    void testG1PreprocessActionYoungConcurrentLogging() {
        File testFile = TestUtil.getFile("dataset47.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging() {
        File testFile = TestUtil.getFile("dataset48.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_INITIAL_MARK with ergonomics.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungInitialMarkWithG1ErgonomicsLogging() {
        File testFile = TestUtil.getFile("dataset49.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_PAUSE with TRIGGER_TO_SPACE_EXHAUSTED with ergonomics.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseTriggerToSpaceExhaustedWithG1ErgonomicsLogging() {
        File testFile = TestUtil.getFile("dataset50.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_G1_EVACUATION_FAILURE),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging2() {
        File testFile = TestUtil.getFile("dataset51.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging3() {
        File testFile = TestUtil.getFile("dataset52.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for G1_YOUNG_INITIAL_MARK with ergonomics.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungInitialMarkWithTriggerAndG1ErgonomicsLogging() {
        File testFile = TestUtil.getFile("dataset53.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_G1_EVACUATION_FAILURE),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging4() {
        File testFile = TestUtil.getFile("dataset54.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging5() {
        File testFile = TestUtil.getFile("dataset55.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    @Test
    void testG1PreprocessActionG1YoungPauseWithG1ErgonomicsLogging6() {
        File testFile = TestUtil.getFile("dataset57.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_INITIAL_MARK),
                JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_CONCURRENT),
                JdkUtil.LogEventType.G1_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    /**
     * Test to ensure it does not falsely erroneously preprocess.
     * 
     */
    @Test
    void testG1CleanupG1InitialMark() {
        File testFile = TestUtil.getFile("dataset62.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_CLEANUP),
                JdkUtil.LogEventType.G1_CLEANUP.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_INITIAL_MARK),
                JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + " collector not identified.");
    }

    /**
     * Test for G1_REMARK with JDK8 details.
     * 
     */
    @Test
    void testRemarkWithFinalizeMarkingAndUnloading() {
        File testFile = TestUtil.getFile("dataset63.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_REMARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".");
    }

    /**
     * Test for G1_CONCURRENT string deduplication.
     * 
     */
    @Test
    void testConcurrentStringDeduplicatonLogging() {
        File testFile = TestUtil.getFile("dataset64.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test for G1_FULL across 3 lines with details.
     * 
     */
    @Test
    void testG1Full3Lines() {
        File testFile = TestUtil.getFile("dataset65.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED),
                Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_CAUSE_MISSING),
                Analysis.WARN_PRINT_GC_CAUSE_MISSING + " analysis incorrectly identified.");
    }

    /**
     * Test preprocessing G1_FULL triggered by TRIGGER_LAST_DITCH_COLLECTION.
     * 
     */
    @Test
    void testG1FullLastDitchCollectionTrigger() {
        File testFile = TestUtil.getFile("dataset74.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_METASPACE_ALLOCATION_FAILURE),
                JdkUtil.TriggerType.LAST_DITCH_COLLECTION.toString() + " trigger not identified.");
    }

    /**
     * Test preprocessing G1_FULL triggered by TRIGGER_JVMTI_FORCED_GARBAGE_COLLECTION.
     * 
     */
    @Test
    void testG1FullJvmTiForcedGarbageCollectionTrigger() {
        File testFile = TestUtil.getFile("dataset75.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_JVMTI),
                JdkUtil.TriggerType.JVMTI_FORCED_GARBAGE_COLLECTION.toString() + " trigger not identified.");
    }

    /**
     * Test preprocessing G1 concurrent missing timestamp.
     * 
     */
    @Test
    void testG1Concurrent() {
        File testFile = TestUtil.getFile("dataset76.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test preprocessing G1_FULL.
     * 
     */
    @Test
    void testG1Full() {
        File testFile = TestUtil.getFile("dataset79.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
    }

    /**
     * Test preprocessing G1_FULL with CLASS_HISTOGRAM.
     * 
     */
    @Test
    void testG1FullWithPrintClassHistogram() {
        File testFile = TestUtil.getFile("dataset93.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CLASS_HISTOGRAM),
                Analysis.WARN_CLASS_HISTOGRAM + " analysis not identified.");
        // G1_FULL is caused by CLASS_HISTOGRAM
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_G1),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis incorrectly identified.");
    }

    /**
     * Test preprocessing G1_YOUNG_PAUSE with no size details (whole number units).
     * 
     */
    @Test
    void testG1YoungPauseNoSizeDetails() {
        File testFile = TestUtil.getFile("dataset97.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    /**
     * Test preprocessing G1_YOUNG_PAUSE with double trigger and Evacuation Failure details.
     * 
     */
    @Test
    void testG1YoungPauseEvacuationFailure() {
        File testFile = TestUtil.getFile("dataset100.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_G1_EVACUATION_FAILURE),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
    }

    @Test
    void testFullGcMixedConcurrent() {
        File testFile = TestUtil.getFile("dataset116.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_G1),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis not identified.");
    }

    @Test
    void testG1YoungInitialMark() {
        File testFile = TestUtil.getFile("dataset127.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_INITIAL_MARK),
                JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + " collector not identified.");
    }

    @Test
    void testFullMixedConcurrent() {
        File testFile = TestUtil.getFile("dataset134.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_SERIAL),
                JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_CONCURRENT),
                JdkUtil.LogEventType.G1_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_G1),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis not identified.");
    }

    @Test
    void testSummarizeRSetStatsPreprocessing() {
        File testFile = TestUtil.getFile("dataset139.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT),
                Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT + " analysis not identified.");
    }

    @Test
    void testPreprocessingWithCommas() {
        File testFile = TestUtil.getFile("dataset143.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingYoungMixedConcurrent() {
        File testFile = TestUtil.getFile("dataset144.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_CONCURRENT),
                JdkUtil.LogEventType.G1_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingFullMixedConcurrent() {
        File testFile = TestUtil.getFile("dataset145.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_SERIAL),
                JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_CONCURRENT),
                JdkUtil.LogEventType.G1_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingYoungMixedErgonomics() {
        File testFile = TestUtil.getFile("dataset180.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testRemarkPrintReferenceGc() {
        File testFile = TestUtil.getFile("dataset214.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_REMARK),
                JdkUtil.LogEventType.G1_REMARK.toString() + " collector not identified.");
    }

    @Test
    void testTriggerHeapDumpInitiatedGcClassHistogram() {
        File testFile = TestUtil.getFile("dataset221.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        // assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
        // JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_SERIAL),
                JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + " collector not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_G1),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis incorrectly identified.");
    }
}
