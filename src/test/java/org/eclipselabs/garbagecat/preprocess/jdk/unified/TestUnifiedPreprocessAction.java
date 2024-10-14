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
package org.eclipselabs.garbagecat.preprocess.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedLogging;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedShenandoahDegeneratedGcEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedPreprocessAction {
    @Test
    void testAdaptiveSizeNoFullAfterScavenge() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] No full after scavenge average_promoted "
                + "103380024 padded_average_promoted 303467488 free in old gen 13724280784";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicy() {
        String logLine = "[2021-06-15T16:03:03.722-0400][237.884s] GC(0) AdaptiveSizePolicy::minor_collection_end: "
                + "minor gc cost: 0.000106  average: 0.000106";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAdjustingEden() {
        String logLine = "[2021-06-15T17:26:44.495-0400][5258.656s] GC(197) Adjusting eden for throughput (avg "
                + "0.989786 goal 0.990000). desired_eden_size 2474639360 eden delta 412614656";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAvgPromoted() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4) avg_promoted_avg: 103380024.000000  "
                + "avg_promoted_dev: 66695816.000000";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAvgPromotedPadded() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4) avg_promoted_padded_avg: 303467488.000000  "
                + "avg_pretenured_padded_avg: 0.000000  tenuring_thresh: 6  target_size: 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAvgSurvived() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4) avg_survived: 313898944.000000  avg_deviation:"
                + " 127148224.000000";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyAvgSurvivedPadded() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4) avg_survived_padded_avg: 695343616.000000";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyBasefootprint() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Base_footprint: 268435456 avg_young_live: "
                + "48341272 avg_old_live: 0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyCapacities() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0)     capacities are the right sizes, "
                + "returning";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyDesiredSurvivorSize() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Desired survivor size 268435456 bytes, new "
                + "threshold 7 (max threshold 15)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyEden() {
        String logLine = "[2021-06-15T19:07:24.707-0400][11298.869s] GC(716)     eden: "
                + "[0x0000000780000000..0x00000007f7e00000) 2011168768";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyEdenFromTo() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4)   Eden, from, to:";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyEdenStart() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4)     [eden_start .. eden_end): "
                + "[0x0000000780000000 .. 0x00000007e0000000) 1610612736";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyEdenToFrom() {
        String logLine = "[2021-06-15T17:26:44.495-0400][5258.656s] GC(197)   Eden, to, from:";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyFrom() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0)     from: "
                + "[0x00000007e0000000..0x00000007f0000000) 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyFromStart() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4)     [from_start .. from_end): "
                + "[0x00000007e0000000 .. 0x00000007f0000000) 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyLivespace() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Live_space: 316776736 free_space: 3221225472";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyMinorpause() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Minor_pause: 0.025024 major_pause: 0.000000 "
                + "minor_interval: 237.060257 major_interval: 0.000000pause_goal: 18446744073709552.000000";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyMinorPause() {
        String logLine = "[2021-06-15T16:03:03.722-0400][237.884s] GC(0)   minor pause: 25.024410 minor period "
                + "237035.231990";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyOldEdensize() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Old eden_size: 1610612736 desired_eden_size: "
                + "1610612736";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyOldGenCapacity() {
        String logLine = "[2021-06-15T16:03:03.722-0400][237.884s] GC(0) old_gen_capacity: 14241759232 "
                + "young_gen_capacity: 1879048192";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyPsYoungGenResizespaces() {
        String logLine = "[2021-06-15T19:07:24.707-0400][11298.869s] GC(716) PSYoungGen::resize_spaces("
                + "requested_eden_size: 2011168768, requested_survivor_size: 68157440)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyScaledEdenIncrement() {
        String logLine = "[2021-06-15T16:04:57.139-0400][351.300s] GC(5) Scaled eden increment: 1610612736 by "
                + "1.000000 down to 1610612736";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyTo() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0)       to: "
                + "[0x00000007f0000000..0x0000000800000000) 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyToStart() {
        String logLine = "[2021-06-15T16:04:45.320-0400][339.481s] GC(4)     [  to_start ..   to_end): "
                + "[0x00000007f0000000 .. 0x0000000800000000) 268435456";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAdaptiveSizePolicyYoungGenerationSize() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) Young generation size: desired eden: "
                + "1610612736 survivor: 268435456 used: 48341272 capacity: 1879048192 gen limits: "
                + "2147483648 / 2147483648";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAfterGCZOldHeap() {
        String logLine = "[66.297s][debug][gc,heap         ] GC(0) O: Heap after GC invocations=1 (full 1):";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAgeTable() {
        String logLine = "[2022-08-03T06:58:41.321+0000][gc,age      ] GC(0) Age table with threshold 15 (max "
                + "threshold 15)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAgeTableGerational() {
        String logLine = "[0.104s][info][gc,reloc    ] GC(0) Y: Age Table:";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAgeTableGerationalLowerCaseY() {
        String logLine = "[0.315s][info][gc,reloc    ] GC(3) y: Age Table:";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testArchiveRegions() {
        String logLine = "[0.038s][info][gc,heap     ] GC(0) Archive regions: 2->2";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testAttemptHeapExpansion() {
        String logLine = "[0.008s][debug][gc,ergo,heap] Attempt heap expansion (allocate archive regions). "
                + "Total size: 8388608B";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testAttemptHeapShrinking() {
        String logLine = "[2.740s][debug][gc,ergo,heap] GC(2) Attempt heap shrinking (capacity higher than max "
                + "desired capacity). Capacity: 2122317824B occupancy: 50331648B live: 30814280B "
                + "maximum_desired_capacity: 71902354B (30 %)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testAttemptingMaxCompactingCollection() {
        String logLine = "[2021-03-13T03:45:44.424+0530][80337492ms] Attempting maximally compacting collection";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testBackToBackSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset218.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testBalanceQueues() {
        String logLine = "[1234ms] GC(500)     Balance queues: 1.0m";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testBlankLine() {
        String logLine = "[2022-08-09T17:56:59.074-0400] ";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testChooseCollectionSet() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Choose "
                + "Collection Set: 0.0ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClaimedChunks() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Claimed Chunks:                Min: 0, "
                + "Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClassHistogram() {
        String logLine = "[2024-10-09T20:53:07.131+0000] GC(11648) 1000:            12            576  "
                + "io.netty.channel.DefaultChannelPipeline";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClassHistogramFooter() {
        String logLine = "[2024-10-09T20:53:09.057+0000] GC(11650) Class Histogram (before full gc) 242.529ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClassHistogramHeader() {
        String logLine = "[2024-10-09T20:53:08.815+0000] Class Histogram (before full gc)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClassHistogramHeaderDivider() {
        String logLine = "[2024-10-09T20:53:09.034+0000] GC(11650) "
                + "-------------------------------------------------------";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClassHistogramHeadings() {
        String logLine = "[2024-10-09T20:53:09.034+0000] GC(11650)  num     #instances         #bytes  class name "
                + "(module)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClassHistogramTotal() {
        String logLine = "[2024-10-09T20:53:09.057+0000] GC(11650) Total      11994288     1577290424";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClassUnloading() {
        String logLine = "[1234ms] GC(500) Class Unloading";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClearClaimedMarks() {
        String logLine = "[1234ms] GC(499)     Clear Claimed Marks: 8.5ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCleared() {
        String logLine = "[1234ms] GC(500)     Cleared: 0";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testClearLoggedCards() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Clear Logged Cards (ms):       Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 3";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCmsData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) CMS: 0K->518K(960K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" CMS: 0K->518K(960K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCmsInitialMark() {
        String logLine = "[0.053s][info][gc,start     ] GC(1) Pause Initial Mark";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCmsInitialMarkWithDuration() {
        String logLine = "[0.053s][info][gc           ] GC(1) Pause Initial Mark 0M->0M(2M) 0.278ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCmsOld() {
        String logLine = "[0.056s][info][gc,heap      ] GC(1) Old: 518K->518K(960K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCodeRootScanning() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Code Root "
                + "Scanning (ms):  Min:  0.0, Avg:  0.2, Max:  1.1, Diff:  1.1, Sum:  1.4, Workers: 8";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testCodeRootsFixup() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Code Roots "
                + "Fixup: 0.0ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcatenateDirtyCardLogs() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Concatenate Dirty Card Logs: 0.0ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentBeforeSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset217.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testConcurrentCleanup() {
        String logLine = "[2022-08-09T17:56:59.058-0400] GC(0) Concurrent cleanup";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentClearClaimedMarks() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Clear Claimed Marks";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentClearClaimedMarksWithDuration() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Clear Claimed Marks 0.080ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentCycle() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Cycle";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMark() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Mark (79857.381s)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkAbort() {
        String logLine = "[2021-03-13T03:37:46.439+0530][79859507ms] GC(8652) Concurrent Mark Abort";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkDoubleTime() {
        String logLine = "[2022-06-06T08:27:45.926-0500] GC(98825) Concurrent Mark (846234.699s, 846235.254s) "
                + "555.386ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkFromRoots() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857381ms] GC(8652) Concurrent Mark From Roots";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkResetForOverflow() {
        String logLine = "[2022-05-12T14:50:49.174-0500][410877.325s][info][gc,marking    ] GC(566) Concurrent Mark "
                + "reset for overflow";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkRootsZGenerational() {
        String logLine = "[65.489s][debug][gc,phases,start ] GC(0) Y: Concurrent Mark Roots";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkRootsZGenerationalWithDuration() {
        String logLine = "[65.490s][debug][gc,phases       ] GC(0) Y: Concurrent Mark Roots 1.615ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkSpaceAtEnd() {
        String logLine = "[0.053s][info][gc           ] GC(1) Concurrent Mark ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentPreclean() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Preclean";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentRebuildRememberedSetsAndScrubRegions() {
        String logLine = "[2023-11-16T06:43:27.109-0500] GC(5) Concurrent Rebuild Remembered Sets and Scrub Regions";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentRebuildRememberedSetsAndScrubRegionsWithDuration() {
        String logLine = "[2023-11-16T06:43:27.111-0500] GC(5) Concurrent Rebuild Remembered Sets and Scrub Regions "
                + "2.155ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentRefinement() {
        String logLine = "[0.834s][debug][gc,refine,stats] GC(0) Concurrent refinement: 0.00ms, refined: 0, "
                + "precleaned: 0, dirtied: 0";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentRefinementTimes() {
        String logLine = "[0.838s][debug][gc,ergo,refine ] GC(0) Concurrent refinement times: Logged Cards Scan time "
                + "goal: 20.00ms Logged Cards Scan time: -nanms HCC time: 0.00ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentReset() {
        String logLine = "[0.055s][info][gc           ] GC(1) Concurrent Reset";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentScanRootRegions() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Scan Root Regions";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentScanRootRegionsWithDuration() {
        String logLine = "[2022-06-06T08:27:45.371-0500] GC(98825) Concurrent Scan Root Regions 47.873ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentSweep() {
        String logLine = "[0.055s][info][gc           ] GC(1) Concurrent Sweep";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentUndoCycle() {
        String logLine = "[2023-01-11T16:09:59.190+0000][19084.729s] GC(300) Concurrent Undo Cycle";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentUndoCycleWithDuration() {
        String logLine = "[2023-01-11T16:09:59.244+0000][19084.784s] GC(300) Concurrent Undo Cycle 54.191ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCopiedBytes() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)         Copied Bytes                   Min: 455944, "
                + "Avg: 697517.5, Max: 2108432, Diff: 1652488, Sum: 9067728, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDeactivatedWorker() {
        String logLine = "[1234ms] Deactivated worker 0, off threshold: 515, current: 515, processed: 18";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDead5Spaces() {
        String logLine = "[2024-02-01T11:09:51.569+0000][0.843s][debug][gc,phases      ] GC(1)     Dead"
                + "                           Min: 0, Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDead9Spaces() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)         Dead                           Min: 0, "
                + "Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDebugLoggingG1Cycle() throws IOException {
        File testFile = TestUtil.getFile("dataset283.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(5, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_CLEANUP),
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " collector not identified.");
    }

    @Test
    void testDebugLoggingG1YoungPause() throws IOException {
        File testFile = TestUtil.getFile("dataset282.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_HEAP),
                JdkUtil.LogEventType.UNIFIED_HEAP.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " collector not identified.");
    }

    @Test
    void testDefNewData() {
        String logLine = "[0.112s][info][gc,heap        ] GC(3) DefNew: 1016K->128K(1152K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" DefNew: 1016K->128K(1152K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDefNewDataJdk17() {
        String logLine = "[0.036s][info][gc,heap     ] GC(0) DefNew: 1022K(1152K)->127K(1152K) "
                + "Eden: 1022K(1024K)->0K(1024K) From: 0K(128K)->127K(128K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" DefNew: 1022K(1152K)->127K(1152K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDictionary() {
        String logLine = "[1234ms] GC(500) Dictionary";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDidNotExpandTheHeap() {
        String logLine = "[2.929s][debug][gc,ergo,heap   ] GC(4) Did not expand the heap (heap shrinking operation "
                + "failed) Humongous regions: 0->0 Metaspace: 23110K(23424K)->23110K(23424K) 23M->23M(48M) 30.469ms "
                + "User=0.03s Sys=0.00s Real=0.03s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDidNotShrinkTheHeap() {
        String logLine = "[2.881s][debug][gc,ergo,heap   ] GC(4) Did not shrink the heap (heap shrinking operation "
                + "failed) Humongous regions: 0->0 Metaspace: 24342K(24768K)->24342K(24768K) 16M->16M(40M) 29.267ms "
                + "User=0.06s Sys=0.00s Real=0.03s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDirtyCards() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Dirty Cards:                   Min: 0, "
                + "Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDiscovered() {
        String logLine = "[1234ms] GC(500)     Discovered: 18407";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDoNotContinueMixedGcs() {
        String logLine = "[1234ms] GC(502) do not continue mixed GCs (reclaimable percentage not over threshold). "
                + "candidate old regions: 226 reclaimable: 1287906400 (5.00) threshold: 5";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDoNotRequestConcurrentCycleInitiation() {
        String logLine = "[1729.296s][debug][gc,ergo,ihop      ] GC(97) Do not request concurrent cycle initiation "
                + "(still doing mixed collections) occupancy: 6610223104B allocation request: 0B threshold: 0B (0.00) "
                + "source: end of GC";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testEagerlyReclaimHumongousObjects() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Eagerly Reclaim Humongous Objects (ms): "
                + "Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testEagerReclaim() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Eager Reclaim (ms):            skipped";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testElasticSearchDefaultLoggingPattern() throws IOException {
        File testFile = TestUtil.getFile("dataset253.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testEnteringSafepoint() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ] Entering safepoint region: "
                + "CollectForMetadataAllocation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        Set<String> context = new HashSet<String>();
        UnifiedPreprocessAction preprocessAction = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertNull(preprocessAction.getLogEntry());
    }

    @Test
    void testEnteringSafepointCleanup() {
        String logLine = "[2021-09-14T11:38:31.797-0500][2.454s][info][safepoint    ] Entering safepoint region: "
                + "Cleanup";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testEnteringSafepointHalt() {
        String logLine = "[0.031s] Entering safepoint region: Halt";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testEnteringSafepointMarkActiveNMethods() {
        String logLine = "[2022-12-15T16:09:39.476+0300][335136.892s] Entering safepoint region: MarkActiveNMethods";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testEvacuateOptionalCollectionSet() {
        String logLine = "[2023-01-11T17:46:39.590+0000][24885.130s] GC(453)   Evacuate Optional Collection Set: "
                + "25.6ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testExpandTheHeap() {
        String logLine = "[3.039s][debug][gc,ergo,heap   ] GC(6) Expand the heap. requested expansion amount: "
                + "12582912B expansion amount: 16777216B";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testExtRootScanning() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Ext Root "
                + "Scanning (ms):   Min:  0.9, Avg:  1.0, Max:  1.0, Diff:  0.1, Sum:  7.8, Workers: 8";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Ext Root Scanning (ms): 1.0", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testExtRootScanningDoubleDigit() {
        String logLine = "[1234ms] GC(469)     Ext Root Scanning (ms):   Min:  0.1, Avg:  1.4, Max: 23.4, Diff: 23.2, "
                + "Sum: 32.1, Workers: 23";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Ext Root Scanning (ms): 23.4", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testExtRootScanningTrippleDigit() {
        String logLine = "[1234ms] GC(499)     Ext Root Scanning (ms):   Min: 27.9, Avg: 28.3, Max: 28.6, Diff:  0.7, "
                + "Sum: 651.2, Workers: 23";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Ext Root Scanning (ms): 28.6", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testExtRootScanningWithManySpacesAfter() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Ext Root Scanning (ms):        Min:  0.0, "
                + "Avg:  0.2, Max:  1.5, Diff:  1.5, Sum:  2.4, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Ext Root Scanning (ms): 1.5", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalizeConcurrentMarkCleanup() {
        String logLine = "[1234ms] GC(500) Finalize Concurrent Mark Cleanup";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalizeMarking() {
        String logLine = "[1234ms] GC(500) Finalize Marking";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalRef() {
        String logLine = "[1234ms] GC(500)     FinalRef (ms):            Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, "
                + "Sum:  0.0, Workers: 19";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalReference() {
        String logLine = "[1234ms] GC(500)   FinalReference:";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinishAddingOldRegionsToCset() {
        String logLine = "[1234ms] GC(502) Finish adding old regions to CSet (reclaimable percentage not over "
                + "threshold). old 129 regions, max 308 regions, reclaimable: 1287906400B (5.00%) threshold: 5";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFlushTaskCaches() {
        String logLine = "[1234ms] GC(500) Flush Task Caches";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFoundPollingPageLoopExecutionAtPc() {
        String logLine = "[27.197s] ... found polling page loop exception at pc = 0x00007f95fc518271, "
                + "stub =0x00007f95fc455c00";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFreeCollectionSet() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Free Collection Set (ms):      Min:  0.0, "
                + "Avg:  0.0, Max:  0.1, Diff:  0.1, Sum:  0.2, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1ActivatedWorker() {
        String logLine = "[2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,refine         ] Activated worker 0, "
                + "on threshold: 19, current: 80";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1AttemptingFullCompaction() {
        String logLine = "[2023-08-22T02:49:17.609-0400][185.733s] Attempting full compaction";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1AttemptingMaximumFullCompactionClearingSoftReferences() {
        String logLine = "[2023-08-22T02:49:12.471-0400][180.595s] Attempting maximum full compaction clearing soft "
                + "references";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1BasicInformation() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,ihop           ] GC(9) Basic information "
                + "(value update), threshold: 7701161574B (70.00), target occupancy: 11001659392B, current occupancy: "
                + "115332592B, recent";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Cleanup() {
        String logLine = "[0.117s][info][gc            ] GC(2) Pause Cleanup 1M->1M(5M) 0.024ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1CollectForAllocation() throws IOException {
        File testFile = TestUtil.getFile("dataset290.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " event not identified.");
    }

    @Test
    void testG1ConcurrentRefinement() {
        String logLine = "[2.555s][debug][gc,refine,stats   ] GC(2) Concurrent refinement: 0.00ms, refined: 0, "
                + "precleaned: 0, dirtied: 0";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1DatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Humongous regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1DeadHumongousRegion() {
        String logLine = "[1234ms] GC(472) Dead humongous region 369 object size 6411784 start 0x00000002b8800000 with "
                + "remset 0 code roots 0 is marked 0 reclaim candidate 1 type array 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1Eden() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Eden regions: 1->0(1)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Eden5Digits() {
        String logLine = "[2023-01-15T00:03:39.675+0200][info][gc,heap     ] GC(21) Eden regions: 103888->0(47034)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1EdenDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Eden regions: 65->0(56)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1EdenDatestampMillisRegions4Digits() {
        String logLine = "[2020-09-11T05:33:44.563+0000][1732868ms] GC(42) Eden regions: 307->0(1659)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1EdenTotal6Digits() {
        String logLine = "[2023-01-15T00:01:29.656+0200][info][gc,heap     ] GC(20) Eden regions: 2216->0(103888)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Evacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Evacuate Collection Set: 1.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1EvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Evacuate Collection Set: 56.4ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FinishChoosingCSet() {
        String logLine = "[2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,ergo,cset      ] GC(9) Finish choosing "
                + "CSet. old: 0 regions, predicted old region time: 0.00ms, time remaining: 0.43";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcBeginTriggerDiagnosticCommand() {
        String logLine = "[2022-05-12T14:53:58.573-0500][411066.724s][info][gc,start      ] GC(567) Pause Full "
                + "(Diagnostic Command)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcMiddleTriggerDiagnosticCommand() {
        String logLine = "[2023-08-22T02:49:11.116-0400][179.240s] GC(73) Pause Full (G1 Compaction Pause) "
                + "2679M->2149M(3072M) 657.941ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcParallelConcurrentIntermingled() throws IOException {
        File testFile = TestUtil.getFile("dataset257.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testG1FullGcTriggerDiagnosticCommandDetails() {
        String logLine = "[2022-05-12T14:54:09.413-0500][411077.565s][info][gc             ] GC(567) Pause Full "
                + "(Diagnostic Command) 41808M->35651M(49152M) 10840.271ms";
        String nextLogLine = "[2022-05-12T14:54:09.413-0500][411077.565s][info][gc,cpu         ] GC(567) "
                + "User=84.75s Sys=0.00s Real=10.85s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(UnifiedPreprocessAction.TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 41808M->35651M(49152M) 10840.271ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1FullGcTriggerG1CompactionPause() {
        String logLine = "[2023-08-22T02:49:10.458-0400][178.582s] GC(73) Pause Full (G1 Compaction Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcTriggerG1EvacuationPause() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcTriggerG1HumongousAllocation() {
        String logLine = "[2021-10-29T21:02:24.624+0000][info][gc,start       ] GC(23863) Pause Full "
                + "(G1 Humongous Allocation)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(
                "[2021-10-29T21:02:24.624+0000][info][gc,start       ] GC(23863) Pause Full (G1 Humongous Allocation)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1FullGcTriggerG1HumongousAllocationDetails() {
        String logLine = "[2021-10-29T21:02:33.467+0000][info][gc             ] GC(23863) Pause Full "
                + "(G1 Humongous Allocation) 16339M->14486M(16384M) 8842.979ms";
        String nextLogLine = "[2021-10-29T21:02:33.467+0000][info][gc,cpu         ] GC(23863) "
                + "User=52.67s Sys=0.01s Real=8.84s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(UnifiedPreprocessAction.TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 16339M->14486M(16384M) 8842.979ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1FullGcTriggerGcLockerInitiatedGc() {
        String logLine = "[2021-03-13T03:45:44.425+0530][80337493ms] GC(9216) Pause Full (GCLocker Initiated GC)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcTriggerGcLockerInitiatedGcDetails() {
        String logLine = "[2021-03-13T03:45:46.526+0530][80339594ms] GC(9216) Pause Full (GCLocker Initiated GC) "
                + "8184M->8180M(8192M) 2101.341ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullTriggerGcLockerInitiatedGc() throws IOException {
        File testFile = TestUtil.getFile("dataset206.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
    }

    @Test
    void testG1FullTriggerMetadataGcThreshold() throws IOException {
        File testFile = TestUtil.getFile("dataset210.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
    }

    @Test
    void testG1GcTerminationStats() {
        String logLine = "[2022-10-09T13:16:49.277+0000][3792.765s][debug][gc,task,stats     ] GC(9) GC Termination "
                + "Stats";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1GcTerminationStatsEntryThrDigit1() {
        String logLine = "[2024-02-21T01:24:03.951+0900][24074288ms] GC(470)   8    122.87     15.46  12.58      "
                + "5.97   4.86       84       0       0       0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1GcTerminationStatsEntryThrDigit2() {
        String logLine = "[2024-02-21T01:24:03.951+0900][24074288ms] GC(470)  10    122.89     15.42  12.55      "
                + "5.97   4.86       78       0       0       0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1HeapBeforeGc() {
        String logLine = "[2022-10-09T13:16:39.707+0000][3783.195s][debug][gc,heap ] GC(9) Heap before GC "
                + "invocations=9 (full 0): garbage-first heap total 10743808K, used 1819374K [0x0000000570400000, "
                + "0x0000000800000000)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Humongous() {
        String logLine = "[2021-09-22T10:57:21.297-0500][5259442ms] GC(5172) Humongous regions: 13->13";
        String nextLogLine = "[2021-09-22T10:57:21.297-0500][5259442ms] GC(5172) Metaspace: 82409K->82409K(1126400K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Humongous regions: 13->13", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1Humongous4Digit() {
        String logLine = "[2023-01-15T00:03:39.675+0200][info][gc,heap     ] GC(21) Humongous regions: 9739->9739";
        String nextLogLine = "[2023-01-15T00:03:39.675+0200][info][gc,metaspace] GC(21) Metaspace: "
                + "243951K(262400K)->243951K(262400K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Humongous regions: 9739->9739", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1HumongousRegister() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Humongous "
                + "Register: 0.1ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1InitiateConcurrentCycle() {
        String logLine = "[0.833s][debug][gc,ergo       ] GC(0) Initiate concurrent cycle (concurrent cycle initiation "
                + "requested)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1LiveHumongousRegion() {
        String logLine = "[1234ms] GC(526) Live humongous region 103 object size 6392280 start 0x0000000233800000  "
                + "with remset 0 code roots 0 is marked 0 reclaim candidate 0 type array 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1MarkClosedArchiveRegions() {
        String logLine = "[0.004s][info][gc,cds       ] Mark closed archive regions in map: [0x00000000fff00000, "
                + "0x00000000fff69ff8]";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1MarkingStats() {
        String logLine = "[1234ms] Marking Stats, task = 0, calls = 62";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1MarkingStatsElapsedTime() {
        String logLine = "[1234ms]   Elapsed time = 535.19ms, Termination time = 3.04ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1MarkingStatsMarkStatsCache() {
        String logLine = "[1234ms]   Mark Stats Cache: hits 3531515 misses 404 ratio 99.989";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1MarkingStatsStepTimes() {
        String logLine = "[1234ms]   Step Times (cum): num = 2275, avg = 1.19ms, sd = 3.18ms max = 10.92ms, "
                + "total = 2704.19ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1MarkOpenArchiveRegions() {
        String logLine = "[0.004s][info][gc,cds       ] Mark open archive regions in map: [0x00000000ffe00000, "
                + "0x00000000ffe46ff8]";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1MixedPauseInfo() {
        String logLine = "[16.630s][info][gc            ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "15M->12M(31M) 1.202ms";
        String nextLogLine = "[16.630s][info][gc           ] GC(0) User=0.18s Sys=0.00s Real=0.11s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 15M->12M(31M) 1.202ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1MmuTargetViolated() {
        String logLine = "[2020-06-24T18:11:22.155-0700][28150ms] GC(24) MMU target violated: 201.0ms "
                + "(200.0ms/201.0ms)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1MutatorRefinement() {
        String logLine = "[2.555s][debug][gc,refine,stats   ] GC(2) Mutator refinement: 0.00ms, refined: 0, "
                + "precleaned: 0, dirtied: 0";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1Old() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Old regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Old2Digits() {
        String logLine = "[10.989s][info][gc,heap      ] GC(684) Old regions: 15->15";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Old7Spaces() {
        String logLine = "[17.728s][info][gc,heap       ] GC(1098) Old regions: 21->21";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1OldDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Old regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1OldGenerationAllocationInTheLastMutatorPeriod() {
        String logLine = "[123456ms] Old generation allocation in the last mutator period, old gen allocated: 12345B, "
                + "humongous allocated: 0B,old gen growth: 1234B.";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1Other() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Other: 0.2ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Other: 0.2ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1Other5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Other: 0.1ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Other: 0.1ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1OtherDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Other: 0.3ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Other: 0.3ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1PantomRef() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases,ref     ] GC(9)         "
                + "PhantomRef (ms):          Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PauseCleanup() {
        String logLine = "[16.081s][info][gc,start      ] GC(969) Pause Cleanup";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PauseYoungBeginning() {
        String logLine = "[2021-09-14T11:38:33.217-0500][3.874s][info][gc,start     ] GC(0) Pause Young "
                + "(Concurrent Start) (Metadata GC Threshold)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PauseYoungInfo() {
        String logLine = "[0.337s][info][gc           ] GC(0) Pause Young (G1 Evacuation Pause) 25M->4M(254M) 3.523ms";
        String nextLogLine = "[0.337s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 25M->4M(254M) 3.523ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1PostEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Post Evacuate Collection Set: 0.2ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PostEvacuate5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Post Evacuate Collection Set: 0.1ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PostEvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Post Evacuate Collection Set: 0.5ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PreEvacuate() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Pre Evacuate Collection Set: 0.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PreEvacuate5Spaces() {
        String logLine = "[16.071s][info][gc,phases     ] GC(971)   Pre Evacuate Collection Set: 0.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1PreEvacuateDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Pre Evacuate Collection Set: 0.0ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Preparsing() throws IOException {
        File testFile = TestUtil.getFile("dataset246.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(5, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_CLEANUP),
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testG1PreparsingHugeHeap() throws IOException {
        File testFile = TestUtil.getFile("dataset266.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " event not identified.");
    }

    @Test
    void testG1RequestConcurrentCycleInitiation() {
        String logLine = "[0.833s][debug][gc,ergo       ] Request concurrent cycle initiation (requested by GC cause). "
                + "GC cause: Metadata GC Threshold";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1ScannedCards() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)       Scanned "
                + "Cards:            Min: 194, Avg: 509.4, Max: 1890, Diff: 1696, Sum: 4075, Workers: 8";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1ServiceThread() {
        String logLine = "[0.034s][debug][gc,task       ] G1 Service Thread (Remembered Set Sampling Task) (register)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testG1StringTable() {
        String logLine = "[16.053s][info][gc,stringtable] GC(969) Cleaned string and symbol table, strings: "
                + "5786 processed, 4 removed, symbols: 38663 processed, 11 removed";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1StringTableLargeNumbers() {
        String logLine = "[2021-05-25T08:46:20.163-0400][1191010566ms] GC(14942) Cleaned string and symbol table, "
                + "strings: 756258 processed, 688568 removed, symbols: 349887 processed, 34 removed";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1StringTableRemoved6Digits() {
        String logLine = "[2020-09-11T08:23:37.353+0000][11925659ms] GC(194) Cleaned string and symbol table, "
                + "strings: 368294 processed, 9531 removed, symbols: 1054532 processed, 18545 removed";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Survivor() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Survivor regions: 0->1(1)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1SurvivorDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Survivor regions: 0->9(9)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1TlabTotals() {
        String logLine = "[2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,tlab           ] GC(9) TLAB totals: "
                + "thrds: 223  refills: 9596 max: 462 slow allocs: 767 max 41 waste:  1.5% gc: 16198888B max: 1568112B "
                + "slow: 6511096B max: 409312B fast: 0B max: 0B";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1tMutatorAllocstionStats() {
        String logLine = "[2022-10-09T13:16:49.276+0000][3792.764s][debug][gc,alloc,region   ] GC(9) Mutator "
                + "Allocation stats, regions: 418, wasted size: 4224B ( 0.0%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UpdatedRefinementZones() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,ergo,refine    ] GC(9) Updated "
                + "Refinement Zones: green: 16, yellow: 48, red: 80";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForClassUnloading() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent class unloading";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForEvacuation() {
        String logLine = "[0.100s][info][gc,task      ] GC(0) Using 2 workers of 4 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForEvacuation2Digits() {
        String logLine = "[0.333s][info][gc,task      ] GC(0) Using 10 workers of 10 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForEvacuation3Digit() {
        String logLine = "[2023-01-14T23:33:54.519+0200][info][gc,task  ] GC(0) Using 143 workers of 143 for"
                + " evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForEvacuation7Spaces() {
        String logLine = "[16.070s][info][gc,task       ] GC(971) Using 2 workers of 4 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForEvacuationDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Using 1 workers of 1 for evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForFullCompaction() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8651) Using 8 workers of 8 for full "
                + "compaction";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1UsingWorkersForMarking() {
        String logLine = "[16.121s][info][gc,task       ] GC(974) Using 1 workers of 1 for marking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1YoungPauseConcurrentStartTriggerG1EvacuationPause() {
        String logLine = "[16.601s][info][gc           ] GC(1032) Pause Young (Concurrent Start) "
                + "(G1 Evacuation Pause) 38M->20M(46M) 0.772ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1YoungPauseNormal() {
        String logLine = "[0.101s][info][gc           ] GC(0) Pause Young (Normal) "
                + "(G1 Evacuation Pause) 0M->0M(2M) 1.371ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1YoungPauseNormalDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "65M->8M(1304M) 57.263ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1YoungPauseNormalTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "78M->22M(1304M) 35.722ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1YoungPauseWithSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset260.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testG1YoungPlab() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,plab           ] GC(9) Young PLAB "
                + "allocation: allocated: 28620256B, wasted: 14568B, unused: 2989832B, used: 25615856B, undo waste: "
                + "0B,";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testGenerateDirtyCardsRate() {
        String logLine = "[4.987s][debug][gc,refine,stats   ] GC(4) Generate dirty cards rate: 0.00 cards/ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testHeapAfterGCZYoungMajor() {
        String logLine = "[66.259s][debug][gc,heap         ] GC(0) Y: Heap after GC invocations=1 (full 1):";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testHeapAfterGCZYoungMinor() {
        String logLine = "[4964.468s][debug][gc,heap         ] GC(11) y: Heap after GC invocations=12 (full 10):";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testHeapBeforeGCZOld() {
        String logLine = "[66.259s][debug][gc,heap         ] GC(0) O: Heap before GC invocations=1 (full 1):";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testHeapBeforeGCZYoungMajor() {
        String logLine = "[65.488s][debug][gc,heap         ] GC(0) Y: Heap before GC invocations=0 (full 0):";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testHeapBeforeGCZYoungMinor() {
        String logLine = "[4960.974s][debug][gc,heap         ] GC(11) y: Heap before GC invocations=11 (full 10):";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testHeapExpansion() {
        String logLine = "[0.772s][debug][gc,ergo,heap] GC(0) Heap expansion: short term pause time ratio 0.00% long "
                + "term pause time ratio 0.00% threshold 2.53% pause time ratio 7.69% fully expanded false resize by "
                + "0B Other: 0.5ms Humongous regions: 0->0 Metaspace: 10930K(11200K)->10930K(11200K) 103M->19M(2024M) "
                + "3.940ms User=0.04s Sys=0.01s Real=0.00s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testHeapExpansionTriggers() {
        String logLine = "[0.772s][trace][gc,ergo,heap] GC(0) Heap expansion triggers: pauses since start: 0 num prev "
                + "pauses for heuristics: 10 ratio over threshold count: 0";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testhenandoahFinalMarkAdaptiveCSetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Adaptive CSet Selection. Target Free: 130M, "
                + "Actual Free: 1084M, Max CSet: 54M, Min Garbage: 0M";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testHotCardCache() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Hot Card Cache (ms):           Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testHumongousRegion() {
        String logLine = "[77.301s][debug][gc,humongous      ] GC(19) Humongous region 1 (object size 179719864 @ "
                + "0x0000000381000000) remset 0 code roots 0 marked 0 reclaim candidate 1 type array 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testHumongousTotal() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)         Humongous Total                Min: 0, "
                + "Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testJdk17G1() throws IOException {
        File testFile = TestUtil.getFile("dataset235.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
    }

    @Test
    void testJniWeak3Spaces() {
        String logLine = "[0.843s][debug][gc,phases      ] GC(1)   JNI Weak                       Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testJniWeak7Spaces() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       JNI Weak                       Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testJvmtiTagWeakOopStorage3Spaces() {
        String logLine = "[0.843s][debug][gc,phases      ] GC(1)   JVMTI Tag Weak OopStorage      Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testJvmtiTagWeakOopStorage7Spaces() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       JVMTI Tag Weak OopStorage      Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLabWaste() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)         LAB Waste                      Min: 200, "
                + "Avg: 1113.8, Max: 3040, Diff: 2840, Sum: 14480, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogBuffers() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Log Buffers (ms):              Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMarkStatsCache() {
        String logLine = "[1234ms] Mark stats cache hits 25288251 misses 4149 ratio 99.984";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMergeHeapRoots() {
        String logLine = "[0.038s][info][gc,phases   ] GC(0)   Merge Heap Roots: 0.1ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testMergeOptionalHeapRoots() {
        String logLine = "[2023-01-11T17:46:35.751+0000][24881.291s] GC(452)   Merge Optional Heap Roots: 0.3ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testMergePerThreadState() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Merge Per-Thread State (ms):   Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMergeSparsed() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Merged Sparse:                 Min: 0, "
                + "Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspace2Spaces() {
        String logLine = "[16.072s][info][gc,metaspace  ] GC(971) Metaspace: 10793K->10793K(1058816K)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testMetaspaceData() {
        String logLine = "[0.032s][info][gc,metaspace ] GC(0) Metaspace: 120K->120K(1056768K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Metaspace: 120K->120K(1056768K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceDataJdk17() {
        String logLine = "[0.061s][info][gc,metaspace] GC(1) Metaspace: 667K(832K)->667K(832K) NonClass: "
                + "617K(704K)->617K(704K) Class: 49K(128K)->49K(128K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Metaspace: 667K(832K)->667K(832K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Metaspace: 26116K->26116K(278528K)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testMetaspaceNoInfo() {
        String logLine = "[2022-08-03T06:58:41.321+0000][gc,metaspace] GC(0) Metaspace: 19460K(19840K)->19460K(19840K) "
                + "NonClass: 17082K(17280K)->17082K(17280K) Class: 2378K(2560K)->2378K(2560K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Metaspace: 19460K(19840K)->19460K(19840K)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceTimePidTags() {
        String logLine = "[2022-08-04T11:38:08.058+0000][1908][gc,metaspace] GC(0) Metaspace: 21086K(21504K)->"
                + "21086K(21504K) NonClass: 18491K(18752K)->18491K(18752K) Class: 2594K(2752K)->2594K(2752K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Metaspace: 21086K(21504K)->21086K(21504K)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testMinimalJdk21G1() throws IOException {
        File testFile = TestUtil.getFile("dataset275.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testMmuTargetViolated() {
        String logLine = "[2021-09-14T11:41:18.173-0500][168.830s][info][gc,mmu        ] GC(26) MMU target violated: "
                + "201.0ms (200.0ms/201.0ms)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testNoRememberedSets() {
        String logLine = "[0.844s][debug][gc,phases         ] GC(1) No Remembered Sets to update after rebuild";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testNotify() {
        String logLine = "[1234ms] GC(500)   Notify Soft/WeakReferences: 0.4ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testObjectSynchronizerWeak3Spaces() {
        String logLine = "[0.843s][debug][gc,phases      ] GC(1)   ObjectSynchronizer Weak        Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testObjectSynchronizerWeak7Spaces() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       ObjectSynchronizer Weak        Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testOldCandidateCollectionSetEmpty() {
        String logLine = "[1759.746s][debug][gc,ergo,cset      ] GC(101) Old candidate collection set empty.";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testParallelCompactingOldAdjustRoots() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Adjust Roots 0.666ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldAdjustRootsStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Adjust Roots";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldCompactionPhase() {
        String logLine = "[0.087s][info][gc,phases      ] GC(3) Compaction Phase 2.540ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldCompactionPhaseStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Compaction Phase";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldMarkingPhase() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Marking Phase 1.032ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldMarkingPhaseStart() {
        String logLine = "[0.083s][info][gc,phases,start] GC(3) Marking Phase";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldPostCompact() {
        String logLine = "[0.087s][info][gc,phases      ] GC(3) Post Compact 0.012ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldPostStart() {
        String logLine = "[0.087s][info][gc,phases,start] GC(3) Post Compact";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldSummaryPhase() {
        String logLine = "[0.084s][info][gc,phases      ] GC(3) Summary Phase 0.005ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldTriggerMetadataGcClearSoftReferencesBegin() {
        String logLine = "[2022-02-08T07:33:13.183+0000][7731431ms] GC(112) Pause Full (Metadata GC Clear Soft "
                + "References)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelCompactingOldTriggerMetadataGcClearSoftReferencesMiddle() {
        String priorLine = "[2022-02-08T07:33:13.853+0000][7732101ms] GC(112) Metaspace: 243927K->243728K(481280K)";
        String logLine = "[2022-02-08T07:33:13.853+0000][7732101ms] GC(112) Pause Full (Metadata GC Clear Soft "
                + "References) 141M->141M(2147M) 670.712ms";
        String nextLogLine = "[2022-02-08T07:33:13.854+0000][7732102ms] GC(112) User=1.05s Sys=0.01s Real=0.67s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(UnifiedPreprocessAction.TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC);
        context.add(PreprocessAction.NEWLINE);
        context.add(UnifiedPreprocessAction.TOKEN);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(priorLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 141M->141M(2147M) 670.712ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testParallelCompactingOlSummaryPhaseStart() {
        String logLine = "[0.084s][info][gc,phases,start] GC(3) Summary Phase";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelPrintAdaptiveSizePolicy() throws IOException {
        File testFile = TestUtil.getFile("dataset212.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    void testParallelPrintAdaptiveSizePolicyDoScavenge() throws IOException {
        File testFile = TestUtil.getFile("dataset213.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    void testParallelScavengeTriggerMetadataGcClearSoftReferencesBegin() {
        String logLine = "[2022-02-08T07:33:13.178+0000][7731426ms] GC(111) Pause Young (Metadata GC Clear Soft "
                + "References)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testParallelScavengeTriggerMetadataGcClearSoftReferencesMiddle() {
        String priorLogLine = "[2022-02-08T07:33:13.182+0000][7731430ms] GC(111) Metaspace: 243927K->243927K(481280K)";
        String logLine = "[2022-02-08T07:33:13.183+0000][7731431ms] GC(111) Pause Young (Metadata GC Clear Soft "
                + "References) 141M->141M(2147M) 4.151ms";
        String nextLogLine = "[2022-02-08T07:33:13.183+0000][7731431ms] GC(111) User=0.00s Sys=0.00s Real=0.01s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(PreprocessAction.NEWLINE);
        context.add(UnifiedPreprocessAction.TOKEN);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(priorLogLine, logLine, nextLogLine,
                entangledLogLines, context);
        assertEquals(" 141M->141M(2147M) 4.151ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testParallelYoungParallelSerialMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset229.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD),
                JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testParMark() {
        String logLine = "[2.178s][debug][gc,phases,start] GC(1) Par Mark";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testParNewData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) ParNew: 974K->128K(1152K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" ParNew: 974K->128K(1152K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testParOldGenData() {
        String logLine = "[0.030s][info][gc,heap      ] GC(0) ParOldGen: 0K->8K(512K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" ParOldGen: 0K->8K(512K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseFullBeginning() {
        String logLine = "[2021-09-14T06:51:15.478-0500][3.530s][info][gc,start     ] GC(1) Pause Full (Metadata GC "
                + "Threshold)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPauseYoungAllocationFailureStart() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPauseYoungInfo() {
        String logLine = "[0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M->1M(2M) 0.700ms";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(PreprocessAction.NEWLINE);
        context.add(UnifiedPreprocessAction.TOKEN);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" 1M->1M(2M) 0.700ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfo11SpacesAfterGc() {
        String logLine = "[0.032s][info][gc           ] GC(0) Pause Young (Allocation Failure) 0M->0M(1M) 1.195ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPauseYoungInfoConcurrentStartTriggerG1EvacuationPause() {
        String logLine = "[0.058s][info][gc           ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 1M->1M(4M) "
                + "5.780ms";
        String nextLogLine = "[0.058s][info][gc,cpu       ] GC(0) User=0.01s Sys=0.00s Real=0.01s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 1M->1M(4M) 5.780ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoConcurrentStartTriggerG1HumongousAllocationFull() {
        String logLine = "[390354.671s][info][gc] GC(1471) Pause Young (Concurrent Start) (G1 Humongous Allocation) "
                + "16113M->15932M(16384M) 36.022ms";
        String nextLogLine = "[390354.671s][info][gc] GC(1472) Concurrent Cycle";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoConcurrentStartTriggerG1HumongousAllocationMiddle() {
        String logLine = "[2020-06-24T19:24:56.395-0700][4442390ms] GC(126) Pause Young (Concurrent Start) "
                + "(G1 Humongous Allocation) 882M->842M(1223M) 19.777ms";
        String nextLogLine = "[2020-06-24T19:24:56.395-0700][4442390ms] GC(126) User=0.04s Sys=0.00s Real=0.02s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 882M->842M(1223M) 19.777ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoConcurrentStartTriggerGcLockerInitiatedGcMiddle() {
        String logLine = "[2023-02-12T01:16:20.227+0200][info][gc             ] GC(3296) Pause Young "
                + "(Concurrent Start) (GCLocker Initiated GC) 614121M->614121M(614400M) 52.062ms";
        String nextLogLine = "[2023-02-12T01:16:20.228+0200][info][gc,cpu         ] GC(3296) User=3.24s Sys=0.03s "
                + "Real=0.05s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 614121M->614121M(614400M) 52.062ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoConcurrentStartTriggerMetaGcThreshold() {
        String logLine = "[2020-06-24T18:11:52.781-0700][58776ms] GC(44) Pause Young (Concurrent Start) "
                + "(Metadata GC Threshold) 733M->588M(1223M) 105.541ms";
        String nextLogLine = "[2020-06-24T18:11:52.781-0700][58776ms] GC(44) User=0.18s Sys=0.00s Real=0.11s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 733M->588M(1223M) 105.541ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoNormalTriggerG1PreventiveCollection() {
        String logLine = "[0.038s][info][gc          ] GC(0) Pause Young (Normal) (G1 Preventive Collection) "
                + "1M->1M(4M) 0.792ms";
        String nextLogLine = "[0.038s][info][gc,cpu      ] GC(0) User=0.00s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 1M->1M(4M) 0.792ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoNormalTriggerGcLockerInitiatedGcFull() {
        String logLine = "[390354.862s][info][gc] GC(1473) Pause Young (Normal) (GCLocker Initiated GC) "
                + "16340M->16340M(16384M) 47.840ms";
        String nextLogLine = "[390361.491s][info][gc] GC(1474) Pause Full (GCLocker Initiated GC) "
                + "16340M->15902M(16384M) 6628.742ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoNormalTriggerGcLockerWithDatestamp() {
        String logLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "78M->22M(1304M) 35.722ms";
        String nextLogLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) User=0.02s Sys=0.00s Real=0.04s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 78M->22M(1304M) 35.722ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoNormalWithDatestamp() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "65M->8M(1304M) 57.263ms";
        String nextLogLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) User=0.02s Sys=0.01s Real=0.06s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 65M->8M(1304M) 57.263ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoStandAlone() {
        String logLine = "[1.507s][info][gc] GC(77) Pause Young (Allocation Failure) 24M->4M(25M) 0.509ms";
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.NEWLINE);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoTriggerHeapDumpInitiatedGc() {
        String logLine = "[2021-11-01T20:48:05.107+0000][240210706ms] GC(950) Pause Young (Heap Dump Initiated GC) "
                + "678M->166M(1678M) 9.184ms";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(PreprocessAction.NEWLINE);
        context.add(UnifiedPreprocessAction.TOKEN);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" 678M->166M(1678M) 9.184ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungMetadataGcThresholdStart() {
        String logLine = "[2.159s][info ][gc,start     ] GC(0) Pause Young (Metadata GC Threshold)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPauseYoungNormalTriggerG1EvacuationPause() {
        String logLine = "[2022-08-03T06:58:41.321+0000][gc          ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)"
                + " 615M->23M(12288M) 7,870ms";
        String nextLogLine = "[2022-08-03T06:58:41.321+0000][gc,cpu      ] GC(0) User=0,04s Sys=0,00s Real=0,01s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 615M->23M(12288M) 7,870ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungStartTriggerHeapDumpInitiatedGc() {
        String logLine = "[2021-11-01T20:48:05.098+0000][240210697ms] GC(950) Pause Young (Heap Dump Initiated GC)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPerformingGcAfterExcitingCriticalSection() {
        String logLine = "[1234ms] Performing GC after exiting critical section. Thread \"default task-12\" 0 locked.";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPhantomRef() {
        String logLine = "[1234ms] GC(500)     PhantomRef (ms):          Min:  0.1, Avg:  0.1, Max:  0.1, Diff:  0.0, "
                + "Sum:  0.1, Workers: 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPhantomReference() {
        String logLine = "[1234ms] GC(500)   PhantomReference:";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPhaseAdjust() {
        String logLine = "[4.065s][info][gc,phases      ] GC(2264) Phase 3: Adjust pointers 2.453ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseAdjustStart() {
        String logLine = "[4.063s][info][gc,phases,start] GC(2264) Phase 3: Adjust pointers";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseCompute() {
        String logLine = "[4.063s][info][gc,phases      ] GC(2264) Phase 2: Compute new object addresses 1.165ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseComputeStart() {
        String logLine = "[4.062s][info][gc,phases,start] GC(2264) Phase 2: Compute new object addresses";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseMark() {
        String logLine = "[4.062s][info][gc,phases      ] GC(2264) Phase 1: Mark live objects 4.352ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseMarkStart() {
        String logLine = "[4.057s][info][gc,phases,start] GC(2264) Phase 1: Mark live objects";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseMove() {
        String logLine = "[4.067s][info][gc,phases      ] GC(2264) Phase 4: Move objects 1.248ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPhaseMoveStart() {
        String logLine = "[4.065s][info][gc,phases,start] GC(2264) Phase 4: Move objects";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPostEvacuateCleanup() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Post Evacuate Cleanup 1: 0.0ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrecleanFinalReferences() {
        String logLine = "[1234ms] GC(500) Preclean FinalReferences";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrecleanPhantomReferences() {
        String logLine = "[1234ms] GC(500) Preclean PhantomReferences";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrecleanSoftReferences() {
        String logLine = "[1234ms] GC(500) Preclean SoftReferences";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrecleanWeakReferences() {
        String logLine = "[1234ms] GC(500) Preclean WeakReferences";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPreCompact() {
        String logLine = "[2.178s][debug][gc,phases,start] GC(1) Pre Compact";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPredictedBaseTime() {
        String logLine = "[0.007s][trace][gc,ergo,heap] Predicted base time: total 10.000000 lb_cards 0 rs_length 0 "
                + "effective_scanned_cards 0 card_merge_time 0.000000 card_scan_time 0.000000 constant_other_time "
                + "10.000000 survivor_evac_time 0.000000";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrepareHeapRoots() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Prepare Heap Roots: 0.0ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrepareMergeHeapRoots() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Prepare Merge Heap Roots: 0.1ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrepareTlabs() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)     Prepare "
                + "TLABs: 0.1ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPreprocessingCms() throws IOException {
        File testFile = TestUtil.getFile("dataset178.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CMS_INITIAL_MARK),
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PAR_NEW),
                JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingG1Cleanup() throws IOException {
        File testFile = TestUtil.getFile("dataset157.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_CLEANUP),
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingG1FullAndConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset205.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingG1Remark() throws IOException {
        File testFile = TestUtil.getFile("dataset156.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
    }

    /**
     * Verify that preprocessing logging that does not need preprocessing does not change logging.
     * 
     * @throws IOException
     */
    @Test
    void testPreprocessingG1Unecessarily() throws IOException {
        File testFile = TestUtil.getFile("dataset186.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingG1YoungPauseNormalCollection() throws IOException {
        File testFile = TestUtil.getFile("dataset155.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingG1YoungPauseNormalTriggerGcLockerWithDatestamps() throws IOException {
        File testFile = TestUtil.getFile("dataset170.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingParallelCompactingOld() throws IOException {
        File testFile = TestUtil.getFile("dataset176.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingParallelScavengeParallelCompactingOld() throws IOException {
        File testFile = TestUtil.getFile("dataset175.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingParallelScavengeSerialOld() throws IOException {
        File testFile = TestUtil.getFile("dataset173.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingParNew() throws IOException {
        File testFile = TestUtil.getFile("dataset177.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PAR_NEW),
                JdkUtil.LogEventType.UNIFIED_PAR_NEW.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingSerialNew() throws IOException {
        File testFile = TestUtil.getFile("dataset171.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_NEW),

                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingSerialNewAllocationFailureTriggersSerialOldDetails() throws IOException {
        File testFile = TestUtil.getFile("dataset172.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD),
                JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
        UnifiedSerialOldEvent event = (UnifiedSerialOldEvent) jvmRun.getFirstGcEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (73), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testPreprocessingSerialOldTriggerErgonomics() throws IOException {
        File testFile = TestUtil.getFile("dataset174.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD),
                JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
    }

    @Test
    void testProcessedBuffers() {
        String logLine = "[2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases         ] GC(9)       Processed "
                + "Buffers:        Min: 1, Avg: 26.9, Max: 111, Diff: 110, Sum: 215, Workers: 8";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPromotionFailed() {
        String logLine = "[2021-10-30T02:03:26.792+0000][404347ms] Promotion failed";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Promotion failed", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testProtectionDomainCacheTable() {
        String logLine = "[1234ms] GC(500) ProtectionDomainCacheTable";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPruned() {
        String logLine = "[140.081s][debug][gc,ergo,cset      ] GC(27) Pruned 19 regions out of 21, leaving 109682752 "
                + "bytes waste (allowed 966367641)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPsAdaptiveSizePolicy() {
        String logLine = "[2021-06-15T16:03:03.723-0400][237.884s] GC(0) PSAdaptiveSizePolicy::check_gc_overhead_limit:"
                + " promo_limit: 14241759232 max_eden_size: 1610612736 total_free_limit: 15852371968 max_old_gen_size: "
                + "14241759232 max_eden_size: 1610612736 mem_free_limit: 317047439";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPsOldGenData() {
        String logLine = "[0.032s][info][gc,heap      ] GC(0) PSOldGen: 0K->8K(512K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" PSOldGen: 0K->8K(512K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPsYoungGenData() {
        String logLine = "[0.032s][info][gc,heap      ] GC(0) PSYoungGen: 512K->464K(1024K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" PSYoungGen: 512K->464K(1024K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPurgeCodeRoots() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Purge Code Roots (ms):         Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPurgeMetaspace() {
        String logLine = "[1234ms] GC(500) Purge Metaspace";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRebuildFreeList() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Rebuild Free List: 0.0ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRecalculateUsedMemory() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Recalculate Used Memory (ms):  Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testReclaim() {
        String logLine = "[1234ms] GC(500) Reclaim Empty Regions";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testReclaimed() {
        String logLine = "[1234ms] GC(500) Reclaimed 3 empty regions";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testReclaimEmptyRegions() {
        String logLine = "[1234ms] GC(500) Reclaim Empty Regions";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testReconsiderSoftReferences() {
        String logLine = "[25231951ms] GC(500)   Reconsider SoftReferences: 1.2ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRedirtiedCards() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)         Redirtied Cards:               Min: 0, "
                + "Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRedirtyLoggedCards() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Redirty Logged Cards (ms):     Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testReferenceProcessing() {
        String logLine = "[1234ms] GC(500) Reference Processing";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testReferenceProcessor() {
        String logLine = "[0.837s][debug][gc,ref         ] GC(0) ReferenceProcessor::execute queues: 1, "
                + "RefProcThreadModel::Single, marks_oops_alive: false";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRegionRegister() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Region Register: 0.1ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRemark() {
        String logLine = "[16.051s][info][gc,start     ] GC(969) Pause Remark";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testRemark6Spaces() {
        String logLine = "[16.175s][info][gc,start      ] GC(974) Pause Remark";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testRemarkWithDuration() {
        String logLine = "[0.055s][info][gc           ] GC(1) Pause Remark 0M->0M(2M) 0.332ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testRememberedSet() {
        String logLine = "[1234ms] GC(500) Remembered Set Tracking update regions total 3072, selected 419";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRememberedSets() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Remembered Sets (ms):          Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRememberedSetWithTags() {
        String logLine = "[0.843s][debug][gc,remset,tracking] GC(1) Remembered Set Tracking update regions total 1152, "
                + "selected 0";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testReportObjectCount() {
        String logLine = "[1234ms] GC(500) Report Object Count";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRequestMixedGcs() {
        String logLine = "[1234ms] GC(500) request mixed gcs (candidate old regions available). candidate old "
                + "regions: 355 reclaimable: 2360770192 (9.16) threshold: 5";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRequestYoungOnlyGcs() {
        String logLine = "[7.447s][debug][gc,ergo           ] GC(7) request young-only gcs (candidate old regions not "
                + "available)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testResetHotCardCache() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Reset Hot Card Cache (ms):     Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testResolvedMethodTableWeak3Spaces() {
        String logLine = "[0.843s][debug][gc,phases      ] GC(1)   ResolvedMethodTable Weak       Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testResolvedMethodTableWeak7Spaces() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       ResolvedMethodTable Weak       Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testRunningG1() {
        String logLine = "[2022-10-09T13:16:49.287+0000][3792.776s][debug][gc,ergo           ] GC(9) Running G1 Clear "
                + "Card Table Task using 4 workers for 4 units of work for 446 regions.";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset215.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testSafepointApplicationTime() {
        String logLine = "[0.030s][info][safepoint    ] Application time: 0.0012757 seconds";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointCGCOperation() {
        String logLine = "[0.116s][info][safepoint    ] Entering safepoint region: CGC_Operation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointEnteringEnableBiasedLocking() {
        String logLine = "[0.029s][info][safepoint    ] Entering safepoint region: EnableBiasedLocking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointEnteringG1CollectFull() {
        String logLine = "[2021-09-22T10:31:58.206-0500][3736351ms] Entering safepoint region: G1CollectFull";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointEnteringShenandoahInitMark() {
        String logLine = "[2021-10-27T13:03:16.666-0400] Entering safepoint region: ShenandoahInitMark";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointEnteringShenandoahInitUpdateRefs() {
        String logLine = "[2021-10-27T13:03:16.666-0400] Entering safepoint region: ShenandoahInitUpdateRefs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointG1CollectFull() throws IOException {
        File testFile = TestUtil.getFile("dataset220.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testSafepointG1PauseYoung() throws IOException {
        File testFile = TestUtil.getFile("dataset216.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testSafepointHandshakeFallBack() throws IOException {
        File testFile = TestUtil.getFile("dataset226.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testSafepointIcBufferFull() {
        String logLine = "[2021-09-14T11:40:51.508-0500][142.164s][info][safepoint     ] Entering safepoint region: "
                + "ICBufferFull";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointJdk17u8TimeUptime() {
        String logLine = "[2023-08-18T11:40:51.508-0500][1.708s][info][safepoint     ] Safepoint "
                + "\"G1CollectForAllocation\", Time since last: 11990384 ns, Reaching safepoint: 2496 ns, Cleanup: "
                + "11042 ns, At safepoint: 623787 ns, " + "Total: 637325 ns";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedPreprocessAction.JDK17U8);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2023-08-18T11:40:51.508-0500][1.708s][info][safepoint     ] JDK17U8 Safepoint "
                + "\"G1CollectForAllocation\", Time since last: 11990384 ns, Reaching safepoint: 2496 ns, Cleanup: "
                + "11042 ns, At safepoint: 623787 ns, Total: 637325 ns", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testSafepointJdk17u9Time() {
        String logLine = "[2023-12-20T14:15:55.393-0500] Safepoint \"ICBufferFull\", Time since last: 256916975 ns, "
                + "Reaching safepoint: 2351 ns, Cleanup: 84109 ns, At safepoint: 1590 ns, Total: 88050 ns";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedPreprocessAction.JDK17U8);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(
                "[2023-12-20T14:15:55.393-0500] JDK17U8 Safepoint \"ICBufferFull\", Time since last: 256916975 ns, "
                        + "Reaching safepoint: 2351 ns, Cleanup: 84109 ns, At safepoint: 1590 ns, Total: 88050 ns",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSafepointLeaving() {
        String logLine = "[0.029s][info][safepoint    ] Leaving safepoint region";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointParallel() throws IOException {
        File testFile = TestUtil.getFile("dataset219.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testSafepointSynchronizationInitiatedUsingFutexWaitBarrier() {
        String logLine = "[3.459s] Safepoint synchronization initiated using futex wait barrier. (14 threads)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSafepointTime() {
        String logLine = "[2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time for which "
                + "application threads were stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 seconds";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSafepointTimeDecoratorSpaces() {
        String logLine = "[932126.909s][info   ][safepoint     ] Total time for which application threads were "
                + "stopped: 0.0732215 seconds, Stopping threads took: 0.0004592 seconds";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testScanHeapRoots() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)     Scan Heap Roots (ms):          Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testScannedBlocks() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Scanned Blocks:                Min: 0, "
                + "Avg:  0.0, Max: 0, Diff: 0, Sum: 0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testScavenge() {
        String logLine = "[2.159s][debug][gc,phases,start] GC(0) Scavenge";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSerialNewStart() {
        String logLine = "[0.118s][info][gc,start       ] GC(4) Pause Young (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldInfo() {
        String logLine = "[0.076s][info][gc             ] GC(2) Pause Full (Allocation Failure) 0M->0M(2M) 1.699ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldInfoTriggerG1EvacuationPause() {
        String logLine = "[2021-03-13T03:37:42.178+0530][79855246ms] GC(8646) Pause Full (G1 Evacuation Pause) "
                + "8186M->8178M(8192M) 2127.343ms";
        String nextLogLine = "[2021-03-13T03:37:42.179+0530][79855247ms] GC(8646) User=16.40s Sys=0.09s Real=2.13s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(UnifiedPreprocessAction.TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 8186M->8178M(8192M) 2127.343ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSerialOldInfoTriggerSystemGc() {
        String logLine = "[2020-06-24T18:13:51.155-0700][177150ms] GC(74) Pause Full (System.gc()) 887M->583M(1223M) "
                + "3460.196ms";
        String nextLogLine = "[2020-06-24T18:13:51.155-0700][177150ms] GC(74) User=1.78s Sys=0.01s Real=3.46s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(UnifiedPreprocessAction.TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 887M->583M(1223M) 3460.196ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSerialOldStart() {
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldStart7SpacesAfterStart() {
        String logLine = "[0.119s][info][gc,start       ] GC(5) Pause Full (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldStartTriggerHeapDumpInitiatedGc() {
        String logLine = "[2021-11-01T20:48:05.108+0000][240210707ms] GC(951) Pause Full (Heap Dump Initiated GC)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldStartTriggerMetadataGcThreshold() {
        String logLine = "[2021-05-06T21:03:33.749+0000][22637ms] GC(11) Pause Full (Metadata GC Threshold) "
                + "58M->53M(236M) 521.443ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldStartTriggerSystemGc() {
        String logLine = "[2021-09-22T10:57:20.259-0500][5258404ms] GC(5172) Pause Full (System.gc())";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSerialOldTriggerErgonomics() {
        String logLine = "[2021-10-27T13:03:09.055-0400] GC(3) Pause Full (Ergonomics)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2021-10-27T13:03:09.055-0400][gc,start] GC(3) Pause Full (Ergonomics)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testSerialOldTriggerErgonomicsDetails() {
        String logLine = "[0.092s][info][gc             ] GC(3) Pause Full (Ergonomics) 0M->0M(3M) 1.849ms";
        String nextLogLine = "[0.092s][info][gc,cpu         ] GC(3) User=0.01s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(UnifiedPreprocessAction.TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 0M->0M(3M) 1.849ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSerialOldTriggerHeapDumpInitiatedGcDetails() {
        String logLine = "[2021-11-01T20:48:05.297+0000][240210896ms] GC(951) Pause Full (Heap Dump Initiated GC) "
                + "166M->160M(1678M) 189.216ms";
        String nextLogLine = "[2021-11-01T20:48:05.297+0000][240210896ms] GC(951) User=0.80s Sys=0.02s Real=0.19s";
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        context.add(UnifiedPreprocessAction.TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 166M->160M(1678M) 189.216ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSettingNeedsGc() {
        String logLine = "[1234ms] Setting _needs_gc. Thread \"VM Thread\" 1 locked";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahBadProgressForExernalFragmentationNegativePercent() {
        String logLine = "[2023-08-25T02:17:13.552-0400][308.957s] GC(37) Bad progress for external fragmentation: "
                + "-41.7%, need 1.0%";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahBadProgressForInternalFragmentation() {
        String logLine = "[2023-08-25T02:17:13.552-0400][308.957s] GC(37) Bad progress for internal fragmentation: "
                + "0.1%, need 1.0%";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahCancellingGcAllocationFailure() {
        String logLine = "[52.872s][info][gc           ] Cancelling GC: Allocation Failure";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahCleanup() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent cleanup";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahConcurrent() {
        String logLine = "[2022-08-09T17:56:59.059-0400] GC(0) Concurrent cleanup 28M->27M(32M) 0.103ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahConcurrentMarkingNoTime() {
        String logLine = "[2023-02-22T12:31:34.605+0000][2243][gc,start     ] GC(0) Concurrent marking "
                + "(process weakrefs) (unload classes)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahConcurrentMarkingUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Concurrent marking (unload classes)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahConcurrentMarkingWithTime() {
        String logLine = "[2023-02-22T12:31:34.629+0000][2243][gc           ] GC(0) Concurrent marking "
                + "(process weakrefs) (unload classes) 24.734ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahCycle1() throws IOException {
        File testFile = TestUtil.getFile("dataset286.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(10, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_TRIGGER),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_STATS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_STATS.toString() + " event not identified.");
        assertEquals(kilobytes(3795), jvmRun.getMaxClassSpaceOccupancyNonBlocking(),
                "Metaspace max occupancy not parsed correctly.");
        assertEquals(kilobytes(3968), jvmRun.getMaxClassSpaceNonBlocking(),
                "Metaspace max allocation not parsed correctly.");
        assertEquals(megabytes(28), jvmRun.getMaxHeapOccupancyNonBlocking(),
                "Heap max occupancy not parsed correctly.");
        assertEquals(megabytes(37), jvmRun.getMaxHeapSpaceNonBlocking(), "Heap max allocation not parsed correctly.");
    }

    @Test
    void testShenandoahCycle2() throws IOException {
        File testFile = TestUtil.getFile("dataset287.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(11, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_TRIGGER),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_STATS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_STATS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " event not identified.");
        assertEquals(kilobytes(4135), jvmRun.getMaxClassSpaceOccupancyNonBlocking(),
                "Metaspace max occupancy not parsed correctly.");
        assertEquals(kilobytes(4288), jvmRun.getMaxClassSpaceNonBlocking(),
                "Metaspace max allocation not parsed correctly.");
        assertEquals(megabytes(83), jvmRun.getMaxHeapOccupancyNonBlocking(),
                "Heap max occupancy not parsed correctly.");
        assertEquals(megabytes(92), jvmRun.getMaxHeapSpaceNonBlocking(), "Heap max allocation not parsed correctly.");
    }

    @Test
    void testShenandoahDecoratorPacerForMark() {
        String logLine = "[41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 8.5x";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahDegeneratedGc() throws IOException {
        File testFile = TestUtil.getFile("dataset285.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(5, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_DEGENERATED_GC),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_DEGENERATED_GC.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_TRIGGER),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_STATS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_STATS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " event not identified.");
        UnifiedShenandoahDegeneratedGcEvent event = (UnifiedShenandoahDegeneratedGcEvent) jvmRun.getFirstGcEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals(766256160635L - 13L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(90), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(15), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(96), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(13088, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(kilobytes(4162), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(4153), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(4352), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testShenandoahDegeneratedGcEvacuation() {
        String logLine = "[2024-04-12T13:14:26.315-0400] GC(97) Pause Degenerated GC (Evacuation)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2024-04-12T13:14:26.315-0400][gc,start] GC(97) Pause Degenerated GC (Evacuation)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahDegeneratedGcMark() {
        String logLine = "[52.883s][info][gc,start     ] GC(1632) Pause Degenerated GC (Mark)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahDegeneratedGcOutsideOfCycle() {
        String logLine = "[8.061s][gc,start] GC(136) Pause Degenerated GC (Outside of Cycle)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahDegeneratedGcTriggerOutsideOfCycleData() {
        String logLine = "[2024-04-12T17:56:00.635-0400] GC(74) Pause Degenerated GC (Outside of Cycle) 90M->15M(96M) "
                + "13.088ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahDegeneratedGcUpdateRefs() {
        String logLine = "[2024-04-12T17:56:01.722-0400] GC(90) Pause Degenerated GC (Update Refs)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2024-04-12T17:56:01.722-0400][gc,start] GC(90) Pause Degenerated GC (Update Refs)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahDiscoveredReferences() {
        String logLine = "[0.212s][info][gc,ref      ] GC(1) Discovered  references: Soft: 0, Weak: 108, Final: 0, "
                + "Phantom: 6";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahEncounteredReferences() {
        String logLine = "[0.212s][info][gc,ref      ] GC(1) Encountered references: Soft: 3110, Weak: 230, Final: 2, "
                + "Phantom: 8";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahEnqueuedReferences() {
        String logLine = "[0.212s][info][gc,ref      ] GC(1) Enqueued    references: Soft: 0, Weak: 0, Final: 0, "
                + "Phantom: 0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahEvacuationReserve() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Evacuation Reserve: 3M (13 regions), "
                + "Max regular: 256K";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahEvacuationReserveNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Evacuation Reserve: 3M (13 regions), Max regular: 256K";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahEvacuationReserveUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Evacuation Reserve: 65M (131 regions), "
                + "Max regular: 512K";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFailedToAllocate() {
        String logLine = "[52.872s][info][gc           ] Failed to allocate 256K";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFailedToAllocateShared() {
        String logLine = "[2024-04-12T13:14:26.313-0400] Failed to allocate Shared, 1496B";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFailedToAllocateTlab() {
        String logLine = "[2024-04-12T17:56:00.620-0400] Failed to allocate TLAB, 165K";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFinalMarkAdaptiveCSetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Adaptive CSet Selection. Target Free: 130M, "
                + "Actual Free: 1084M, Max CSet: 54M, Min Garbage: 0M";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFinalMarkCollectableGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Collectable Garbage: 179M (61% of total), "
                + "23M CSet, 407 CSet regions";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFinalMarkMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset227.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahFinalMarkPacerRateInfinity() {
        String logLine = "[2.919s] GC(67) Pacer for Evacuation. Used CSet: 79104K, Free: 0B, Non-Taxable: 0B, Alloc "
                + "Tax Rate: infx";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFinalMarkPacerUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pacer for Evacuation. Used CSet: 203M, Free: "
                + "1023M, Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFinalMarkStartUnified() {
        String logLine = "[41.911s][info][gc,start     ] GC(1500) Pause Final Mark (update refs) (process weakrefs)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFinalUpdateMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset228.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahFreeExternalFrag3Digit() {
        String logLine = "[42.421s][info][gc,ergo      ] Free: 8M (72 regions), Max regular: 256K, Max humongous: 0K, "
                + "External frag: 100%, Internal frag: 51%";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFreeHeadroomUnified() {
        String logLine = "[41.917s][info][gc,ergo      ] Free headroom: 11M (free) - 3M (spike) - 0M (penalties) = 8M";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFreeHeadroomUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Free headroom: 132M (free) - 65M (spike) - 0M "
                + "(penalties) = 67M";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFreeNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Free: 18M (109 regions), Max regular: 256K, Max humongous: "
                + "1280K, External frag: 94%, Internal frag: 33%";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFreeUnified() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Free: 18M (109 regions), Max regular: 256K, "
                + "Max humongous: 1280K, External frag: 94%, Internal frag: 33%";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Free: 1022M (2045 regions), Max regular: 512K, "
                + "Max humongous: 929280K, External frag: 12%, Internal frag: 0%";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahGoodProgressForExernalFragmentation() {
        String logLine = "[2023-08-25T02:17:17.382-0400][312.787s] GC(38) Good progress for external fragmentation: "
                + "33.9%, need 1.0%";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahGoodProgressFreeSpaceDegeneratedGcUnified() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for free space: 31426K, need 655K";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahGoodProgressUsedSpaceDegeneratedGcUnified() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for used space: 31488K, need 256K";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahImmediateGarbage() {
        String logLine = "[0.330s][info][gc,ergo      ] GC(0) Immediate Garbage: 4258K (31% of total), 17 regions";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMarkMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset225.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahInitMarkProcessWeakRefs() {
        String logLine = "[2021-10-27T13:03:16.626-0400] GC(0) Pause Init Mark (process weakrefs)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2021-10-27T13:03:16.626-0400][gc,start] GC(0) Pause Init Mark (process weakrefs)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMarkStart() {
        String logLine = "[69.704s][info][gc,start     ] GC(2583) Pause Init Mark";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMarkStartProcessWeakrefsUptimeMillisUnified() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Pause Init Mark (process weakrefs)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2019-02-05T14:47:34.175-0200][3087ms][gc,start] GC(0) Pause Init Mark (process weakrefs)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMarkStartUpdateRefs() {
        String logLine = "[41.918s][info][gc,start     ] GC(1501) Pause Init Mark (update refs)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMarkStartUpdateRefsProcessWeakrefs() {
        String logLine = "[41.893s][info][gc,start     ] GC(1500) Pause Init Mark (update refs) (process weakrefs)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMarkUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Pause Init Mark (unload classes)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitUpdate() {
        String logLine = "[2021-10-27T13:03:16.666-0400] GC(2) Pause Init Update Refs";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2021-10-27T13:03:16.666-0400][gc,start] GC(2) Pause Init Update Refs", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitUpdateMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset224.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahInitUpdatePacerUpdateMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pacer for Update Refs. Used: 242M, Free: 1020M, "
                + "Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitUpdateStartUnified() {
        String logLine = "[69.612s][info][gc,start     ] GC(2582) Pause Init Update Refs";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitUpdateStartUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2019-02-05T14:47:34.229-0200][3141ms][gc,start] GC(0) Pause Init Update Refs",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahJdk17Datestamp() throws IOException {
        File testFile = TestUtil.getFile("dataset256.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(9, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " even not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " even not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_STATS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_STATS.toString() + " even not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_TRIGGER),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " even not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " event not identified.");
    }

    @Test
    void testShenandoahJdk17Uptime() throws IOException {
        File testFile = TestUtil.getFile("dataset236.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(10, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " even not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " even not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " even not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_STATS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_STATS.toString() + " even not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_TRIGGER),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " even not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " event not identified.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " event incorrectly identified.");
    }

    @Test
    void testShenandoahMarkingRoots() {
        String logLine = "[0.188s][info][gc,start    ] GC(0) Concurrent marking roots";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahMaxHeapData() throws IOException {
        File testFile = TestUtil.getFile("dataset167.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(6, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_TRIGGER),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " collector not identified.");
        assertEquals(kilobytes(19 * 1024), jvmRun.getMaxHeapOccupancyNonBlocking(),
                "Max heap occupancy for a non blocking event not parsed correctly.");
        assertEquals(kilobytes(33 * 1024), jvmRun.getMaxHeapSpaceNonBlocking(),
                "Max heap space for a non blocking event not parsed correctly.");
    }

    @Test
    void testShenandoahMetaspaceDataJdk11() {
        String logLine = "[0.258s] Metaspace: 3477K->3501K(1056768K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Metaspace: 3477K->3501K(1056768K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahMetaspaceJdk17() {
        String logLine = "[0.196s][info][gc,metaspace] Metaspace: 3118K(3328K)->3130K(3328K) NonClass: "
                + "2860K(2944K)->2872K(2944K) Class: 258K(384K)->258K(384K)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Metaspace: 3118K(3328K)->3130K(3328K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahMetaspaceJdk17NoTags() {
        String logLine = "[2022-08-09T17:56:59.074-0400] Metaspace: 3369K(3520K)->3419K(3648K) NonClass: "
                + "3091K(3136K)->3133K(3264K) Class: 278K(384K)->285K(384K)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Metaspace: 3369K(3520K)->3419K(3648K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPacerForIdle() {
        String logLine = "[41.912s][info][gc,ergo      ] Pacer for Idle. Initial: 1M, Alloc Tax Rate: 1.0x";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPacerForMarkPercent2Digit() {
        String logLine = "[42.019s][info][gc,ergo      ] GC(1505) Pacer for Mark. Expected Live: 22M, Free: 7M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 11.5x";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPacerForMarkPercentInf() {
        String logLine = "[52.875s][info][gc,ergo      ] GC(1631) Pacer for Mark. Expected Live: 19163K, Free: 0B, "
                + "Non-Taxable: 0B, Alloc Tax Rate: infx";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPacerForMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pacer for Mark. Expected Live: 130M, "
                + "Free: 911M, Non-Taxable: 91M, Alloc Tax Rate: 0.5x";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPacerForPrecleaning() {
        String logLine = "[2020-06-26T15:30:31.311-0400] GC(0) Pacer for Precleaning. Non-Taxable: 98304K";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPacerForReset() {
        String logLine = "[2020-06-26T15:30:31.303-0400] GC(0) Pacer for Reset. Non-Taxable: 98304K";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPacerForUpdateRefs() {
        String logLine = "[2021-10-27T13:03:16.666-0400] GC(2) Pacer for Update Refs. Used: 32278K, Free: 60967K, "
                + "Non-Taxable: 6096K, Alloc Tax Rate: 1.1x";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseFinalMark() {
        String logLine = "[2021-10-27T13:03:16.630-0400] GC(0) Pause Final Mark (process weakrefs) 0.674ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseFinalMarkNoTime() {
        String logLine = "[2021-10-27T13:03:16.629-0400] GC(0) Pause Final Mark (process weakrefs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2021-10-27T13:03:16.629-0400][gc,start] GC(0) Pause Final Mark (process weakrefs)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseFinalMarkProcessWeakrefsUnloadClasses() {
        String logLine = "[2023-02-22T12:31:34.630+0000][2243][gc,start     ] GC(0) Pause Final Mark "
                + "(process weakrefs) (unload classes)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseFinalMarkProcessWeakrefsUnloadClassesWithTime() {
        String logLine = "[2023-02-22T12:31:34.641+0000][2243][gc            ] GC(0) Pause Final Mark "
                + "(process weakrefs) (unload classes) 10.861ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseFinalRoots() {
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2023-08-25T02:15:57.862-0400][233.267s][gc,start] GC(4) Pause Final Roots", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseFinalRootsWithTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots 0.019ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseFinalUpdate() {
        String logLine = "[2021-10-27T13:03:16.634-0400] GC(0) Pause Final Update Refs 0.084ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseFinalUpdateNoTime() {
        String logLine = "[2021-10-27T13:03:16.634-0400] GC(0) Pause Final Update Refs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2021-10-27T13:03:16.634-0400][gc,start] GC(0) Pause Final Update Refs", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseFull() {
        String logLine = "[2023-08-25T02:16:58.619-0400][294.024s] GC(22) Pause Full";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2023-08-25T02:16:58.619-0400][294.024s][gc,start] GC(22) Pause Full", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseInitMark() {
        String logLine = "[2021-10-27T13:03:16.646-0400] GC(1) Pause Init Mark";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals("[2021-10-27T13:03:16.646-0400][gc,start] GC(1) Pause Init Mark", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseInitMarkProcessWeakrefs() {
        String logLine = "[2021-10-27T13:03:16.627-0400] GC(0) Pause Init Mark (process weakrefs) 0.575ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseInitMarkProcessWeakrefsUnloadClasses() {
        String logLine = "[2023-02-22T12:31:34.604+0000][2243][gc,start     ] GC(0) Pause Init Mark (process weakrefs) "
                + "(unload classes)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseInitMarkProcessWeakrefsUnloadClassesWithTime() {
        String logLine = "[2023-02-22T12:31:34.605+0000][2243][gc           ] GC(0) Pause Init Mark (process weakrefs) "
                + "(unload classes) 0.537ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        context.add(UnifiedLogging.Tag.GC_START.toString());
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" 0.537ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseInitUpdateRefs() throws IOException {
        File testFile = TestUtil.getFile("dataset254.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPauseInitUpdateRefsWithTime() {
        String logLine = "[2021-10-27T13:03:16.666-0400] GC(2) Pause Init Update Refs 0.012ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPreprocessingFinalEvac() throws IOException {
        File testFile = TestUtil.getFile("dataset162.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingFinalMark() throws IOException {
        File testFile = TestUtil.getFile("dataset161.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingFinalUpdate() throws IOException {
        File testFile = TestUtil.getFile("dataset164.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingInitialMark() throws IOException {
        File testFile = TestUtil.getFile("dataset160.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingInitUpdate() throws IOException {
        File testFile = TestUtil.getFile("dataset163.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahReferenceProcessing() {
        String logLine = "[2024-04-09T08:23:52.028-0400] Reference processing: parallel discovery, parallel processing";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUncommittedUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Uncommitted 140M. Heap: 1303M reserved, 1163M "
                + "committed, 874M used";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUncommitUptimeMillisNoGcEventNumber() {
        String logLine = "[2019-02-05T14:52:31.132-0200][300044ms] Concurrent uncommit";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] GC(0) Concurrent reset";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingShenandoah() {
        String logLine = "[0.003s][info][gc] Using Shenandoah";
        assertFalse(UnifiedPreprocessAction.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentEvacuation() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent evacuation";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentMarking() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent marking";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentMarkingRoots() {
        String logLine = "[0.188s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent marking roots";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentPreclean() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 1 of 4 workers for concurrent preclean";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentReferenceUpdate() {
        String logLine = "[69.612s][info][gc,task      ] GC(2582) Using 2 of 4 workers for concurrent reference "
                + "update";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentReset() {
        String logLine = "[41.892s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent reset";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentResetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for concurrent reset";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentStrongRoot() {
        String logLine = "[0.192s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent strong root";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentThreadRoots() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for Concurrent thread roots";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentWeakReferences() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent weak references";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentWeakRoot() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent weak root";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForDegeneratedGc() {
        String logLine = "[52.883s][info][gc,task      ] GC(1632) Using 2 of 2 workers for stw degenerated gc";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForFinalMarking() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for final marking";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForFinalReferenceUpdate() {
        String logLine = "[69.644s][info][gc,task      ] GC(2582) Using 2 of 4 workers for final reference update";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForInitMarking() {
        String logLine = "[2021-10-27T13:03:16.626-0400] GC(0) Using 4 of 6 workers for init marking";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForMarking() {
        String logLine = "[16.601s][info][gc,task      ] GC(1033) Using 1 workers of 1 for marking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShrinkTheHeap() {
        String logLine = "[2.740s][debug][gc,ergo,heap] GC(2) Shrink the heap. requested shrinking amount: 2050415470B "
                + "aligned shrinking amount: 2046820352B attempted shrinking amount: 2046820352B";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSkippedPhaseNoSpace() {
        String logLine = "[2022-10-09T13:16:49.288+0000][3792.776s][debug][gc,ref            ] GC(9) Skipped phase1 "
                + "of Reference Processing due to unavailable references";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSkippedPhaseSpace() {
        String logLine = "[0.837s][debug][gc,ref         ] GC(0) Skipped phase 1 of Reference Processing: no "
                + "references";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSkippingRememberedSetRebuild() {
        String logLine = "[0.844s][debug][gc,marking        ] GC(1) Skipping Remembered Set Rebuild. No regions "
                + "selected for rebuild";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSoft() {
        String logLine = "[0.134s] GC(0) Soft: 3088 encountered, 0 discovered, 0 enqueued";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSoftMaxCapacity() {
        String logLine = "[0.134s] GC(0) Soft Max Capacity: 96M(100%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testSoftRef() {
        String logLine = "[1234ms] GC(500)     SoftRef (ms)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSoftReference() {
        String logLine = "[1234ms] GC(500)   SoftReference:";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testStartAddingOldRegions() {
        String logLine = "[1759.746s][debug][gc,ergo,cset      ] GC(101) Start adding old regions to collection set. "
                + "Min 14 regions, max 116 regions, time remaining 179.04ms, optional threshold 35.81ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testStartG1MixedPause() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseConcurrentStartG1EvacuationPause() {
        String logLine = "[16.600s][info][gc,start     ] GC(1032) Pause Young (Concurrent Start) "
                + "(G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseConcurrentStartG1HumongousAllocation() {
        String logLine = "[2021-12-20T10:29:00.098-0500] GC(0) Pause Young (Concurrent Start) "
                + "(G1 Humongous Allocation)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseConcurrentStartMetadataGcThreshold() {
        String logLine = "[0.833s][info ][gc,start      ] GC(0) Pause Young (Concurrent Start) (Metadata GC "
                + "Threshold)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNoQualifier() {
        String logLine = "[0.333s][info][gc,start     ] GC(0) Pause Young (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormal() {
        String logLine = "[0.099s][info][gc,start     ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormal6Spaces() {
        String logLine = "[16.070s][info][gc,start      ] GC(971) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormalDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormalMillis8Digits() {
        String logLine = "[2019-05-09T04:31:19.449+0000][10344041ms] GC(9) Pause Young (Normal) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormalTriggerG1PreventiveCollection() {
        String logLine = "[0.037s][info][gc,start    ] GC(0) Pause Young (Normal) (G1 Preventive Collection)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseNormalTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.136+0000][11728ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPrepareMixed() {
        String logLine = "[15.108s][info][gc,start      ] GC(1194) Pause Young (Prepare Mixed) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStringDedupTableWeak3Spaces() {
        String logLine = "[0.843s][debug][gc,phases      ] GC(1)   StringDedup Table Weak         Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.1, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testStringDedupTableWeak7Spaces() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       StringDedup Table Weak         Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testStringTableWeak3Spaces() {
        String logLine = "[0.843s][debug][gc,phases      ] GC(1)   StringTable Weak               Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.1, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testStringTableWeak7Spaces() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       StringTable Weak               Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.3, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testTenuredData() {
        String logLine = "[32.636s][info][gc,heap        ] GC(9239) Tenured: 24193K->24195K(25240K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Tenured: 24193K->24195K(25240K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    void testTenuredDataJdk17() {
        String logLine = "[0.036s][info][gc,heap     ] GC(0) Tenured: 0K(768K)->552K(768K)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" Tenured: 0K(768K)->552K(768K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testTimesData() {
        String logLine = "[0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(" User=0.00s Sys=0.00s Real=0.00s", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testTimesData8Spaces() {
        String logLine = "[16.053s][info][gc,cpu        ] GC(969) User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testTimesDataDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) User=0.02s Sys=0.01s Real=0.06s";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testTotal() {
        String logLine = "[1234ms] GC(500)     Total (ms):               Min:  0.1, Avg:  0.1, Max:  0.2, Diff:  0.1, "
                + "Sum:  2.5, Workers: 19";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testTotalRefinement() {
        String logLine = "[0.834s][debug][gc,refine,stats] GC(0) Total refinement: 0.00ms, refined: 0, precleaned: 0, "
                + "dirtied: 0";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUnifiedG1FullGcTriggerG1HumongousAllocation() throws IOException {
        File testFile = TestUtil.getFile("dataset232.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedOldSingleLine() throws IOException {
        File testFile = TestUtil.getFile("dataset268.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_MIXED_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedRemarkMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset230.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedShenandoahStatsEventUpdateRegionStates() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Update Region States               15 us";
        // This is a <code>UnifiedShenandoahStatsEvent</code>, a <code>ThrowAwayEvent</code> which should be removed
        // during preparsing.
        assertFalse(UnifiedPreprocessAction.match(logLine),
                "Log line incorrectly recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testUnifiedShenandoahStatsEventWeakReferences() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Weak References                   163 us";
        // This is a <code>UnifiedShenandoahStatsEvent</code>, a <code>ThrowAwayEvent</code> which should be removed
        // during preparsing.
        assertFalse(UnifiedPreprocessAction.match(logLine),
                "Log line incorrectly recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testUnifiedShenandoahStatsEventWeakRoots() {
        String logLine = "[2024-04-11T20:17:37.413-0400]     Weak Roots                       57 us";
        // This is a <code>UnifiedShenandoahStatsEvent</code>, a <code>ThrowAwayEvent</code> which should be removed
        // during preparsing.
        assertFalse(UnifiedPreprocessAction.match(logLine),
                "Log line incorrectly recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testUnifiedYoungPreparsing() throws IOException {
        File testFile = TestUtil.getFile("dataset289.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
    }

    @Test
    void testUpdateDerivedPointers() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Update Derived Pointers (ms):  Min:  0.0, "
                + "vg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 1";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUpdateRememberedSetTracking() {
        String logLine = "[1234ms] GC(500) Update Remembered Set Tracking Before Rebuild";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUsed() {
        String logLine = "[0.134s] GC(0)      Used:       10M (10%)          12M (12%)          12M (12%)           "
                + "6M (6%)           14M (15%)           6M (6%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testUsingParallel() {
        String logLine = "[0.003s][info][gc] Using Parallel";
        assertFalse(UnifiedPreprocessAction.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testUsingWorkersForFullCompaction() {
        String logLine = "[2020-06-24T18:13:47.695-0700][173690ms] GC(74) Using 2 workers of 2 for full compaction";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testVisitedCards() {
        String logLine = "[0.834s][debug][gc,remset      ] GC(0) Visited cards 0 Total dirty 0 (0.00%) Total old "
                + "458752 (0.00%)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testVmThread() {
        String logLine = "[2.178s][debug][gc,task,time   ] GC(0) VM-Thread 2158865378 2176983952 2178104454";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testVmWeak3Spaces() {
        String logLine = "[0.843s][debug][gc,phases      ] GC(1)   VM Weak                        Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testVmWeak7Spaces() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       VM Weak                        Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testWeak() {
        String logLine = "[0.134s] GC(0) Weak: 225 encountered, 203 discovered, 43 enqueued";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testWeakJfrOldObjectSamples3Spaces() {
        String logLine = "[0.843s][debug][gc,phases      ] GC(1)   Weak JFR Old Object Samples    Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testWeakJfrOldObjectSamples7Spaces() {
        String logLine = "[0.838s][debug][gc,phases      ] GC(0)       Weak JFR Old Object Samples    Min:  0.0, "
                + "Avg:  0.0, Max:  0.0, Diff:  0.0, Sum:  0.0, Workers: 13";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testWeakProcessing() {
        String logLine = "[1234ms] GC(500) Weak Processing";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testWeakRef() {
        String logLine = "[1234ms] GC(500)     WeakRef (ms):             Min:  0.0, Avg:  0.0, Max:  0.0, Diff:  0.0, "
                + "Sum:  0.0, Workers: 19";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testWeakReference() {
        String logLine = "[1234ms] GC(500)   WeakReference:";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testYoungDesiredLength() {
        String logLine = "[0.007s][trace][gc,ergo,heap] Young desired length 12 survivor length 0 allocated young "
                + "length 0 absolute min young length 12 absolute max young length 150 desired eden length by mmu 0 "
                + "desired eden length by pause 12";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testYoungListLength() {
        String logLine = "[0.007s][trace][gc,ergo,heap] Young list length update: pending cards 0 rs_length 0 old "
                + "target 0 desired: 12 target: 12 max: 12";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testYoungSingleLine() {
        String logLine = "[1.507s][info][gc] GC(77) Pause Young (Allocation Failure) 24M->4M(25M) 0.509ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testYoungTargetLength() {
        String logLine = "[0.007s][trace][gc,ergo,heap] Young target length: No need to use reserve receiving "
                + "additional eden 12";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZAdjustingWorkersForOldGeneration() {
        String logLine = "[3.394s][info][gc,task     ] Adjusting Workers for Old Generation: 6 -> 5";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZAdjustingWorkersForYoungGeneration() {
        String logLine = "[3.394s][info][gc,task     ] Adjusting Workers for Young Generation: 2 -> 4";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZAllocated() {
        String logLine = "[0.134s] GC(0) Allocated:         -                 2M (2%)            2M (2%)            "
                + "1M (2%)             -                  -";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZAllocationStallC1() {
        String logLine = "[3.394s] Allocation Stall (C1 CompilerThread0) 24.753ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZAllocationStallCommonCleaner() {
        String logLine = "[2023-11-16T08:36:06.351-0500] Allocation Stall (Common-Cleaner) 1.534ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZAllocationStallMain() {
        String logLine = "[0.274s] Allocation Stall (main) 12.040ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZAllocationStallReferenceHandler() {
        String logLine = "[2023-11-16T08:36:06.392-0500] Allocation Stall (Reference Handler) 1.544ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZAllocationStallsGenerational() {
        String logLine = "[0.119s][info][gc,alloc    ] GC(0) O: Allocation Stalls:          0                0"
                + "                0                0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZCapacity() {
        String logLine = "[0.134s] GC(0)  Capacity:       32M (33%)          32M (33%)          32M (33%)          "
                + "32M (33%)          32M (33%)          32M (33%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZClearingAllSoftReferences() {
        String logLine = "[0.296s] GC(3) Clearing All SoftReferences";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZFinal() {
        String logLine = "[0.134s] GC(0) Final: 2 encountered, 0 discovered, 0 enqueued";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZFinalReferencesGenerational() {
        String logLine = "[0.228s][info][gc,ref      ] GC(2) O: Final References:               2            0"
                + "            0 ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZForwardingUsage() {
        String logLine = "[0.134s] GC(0) Forwarding Usage: 0M";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZFree() {
        String logLine = "[0.134s] GC(0)      Free:       86M (90%)          84M (88%)          84M (88%)          "
                + "90M (94%)          90M (94%)          82M (85%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGarbage() {
        String logLine = "[0.134s] GC(0)   Garbage:         -                 6M (7%)            6M (7%)            "
                + "0M (1%)             -                  -";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGarbageCollectionAllocationRate() {
        String logLine = "[0.424s] GC(7) Garbage Collection (Allocation Rate)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGarbageCollectionAllocationStall() {
        String logLine = "[0.262s] GC(2) Garbage Collection (Allocation Stall)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGarbageCollectionMetadataGcThreshold() {
        String logLine = "[2023-12-02T00:22:33.236+0700][2.783s] GC(0) Garbage Collection (Metadata GC Threshold)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGarbageEdenGenerational() {
        String logLine = "[0.137s][info][gc,reloc    ] GC(1) Y: Eden               9M (10%)           6M (7%)"
                + "           6 / 1              0 / 0              2 / 0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGarbageHeaderGenerational() {
        String logLine = "[0.137s][info][gc,reloc    ] GC(1) Y:                    Live             Garbage"
                + "             Small              Medium             Large";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGarbageHeaderGenerationalLowerCaseY() {
        String logLine = "[0.315s][info][gc,reloc    ] GC(3) y:                    Live             Garbage"
                + "             Small              Medium             Large";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGarbageSurvivor1DigitGenerational() {
        String logLine = "[0.137s][info][gc,reloc    ] GC(1) Y: Survivor 1         1M (2%)            2M (3%)"
                + "           2 / 2              0 / 0              0 / 0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGenerationalClassLoaderData() {
        String logLine = "[66.290s][debug][gc,phases,start ] GC(0) O: ClassLoaderData";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalConcurrentClassesUnlink() {
        String logLine = "[66.290s][debug][gc,phases,start ] GC(0) O: Concurrent Classes Unlink";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalConcurrentMarkOld() {
        String logLine = "[0.213s][info][gc,phases   ] GC(2) O: Concurrent Mark 0.295ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalConcurrentMarkYoung() {
        String logLine = "[0.100s][info][gc,phases   ] GC(0) Y: Concurrent Mark 2.536ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalConcurrentReferencesProcess() {
        String logLine = "[66.260s][debug][gc,phases,start ] GC(0) O: Concurrent References Process";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalConcurrentReferencesProcessWithDuration() {
        String logLine = "[66.260s][debug][gc,phases       ] GC(0) O: Concurrent References Process 0.022ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalPauseMarkEndOld() {
        String logLine = "[66.260s][debug][gc,phases,start ] GC(0) O: Pause Mark End";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalPauseMarkEndYoungMajor() {
        String logLine = "[66.076s][debug][gc,phases,start ] GC(0) Y: Pause Mark End";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalPauseMarkEndYoungMinor() {
        String logLine = "[2998.647s][debug][gc,phases,start ] GC(8) y: Pause Mark End";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalPauseMarkStartMajor() {
        String logLine = "[65.488s][debug][gc,phases,start ] GC(0) Y: Pause Mark Start (Major)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalPauseMarkStartMinor() {
        String logLine = "[4960.975s][debug][gc,phases,start ] GC(11) y: Pause Mark Start";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalPauseMarkStartWithDuration() {
        String logLine = "[65.489s][info ][gc,phases       ] GC(0) Y: Pause Mark Start (Major) 0.028ms";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalPauseRelocateStartOld() {
        String logLine = "[78.704s][debug][gc,phases,start ] GC(2) O: Pause Relocate Start";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalPauseRelocateStartYoung() {
        String logLine = "[66.101s][debug][gc,phases,start ] GC(0) Y: Pause Relocate Start";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalTriggerCleanups() {
        String logLine = "[66.290s][debug][gc,phases,start ] GC(0) O: Trigger cleanups";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZGenerationalUsingTenuringThreshold() {
        String logLine = "[0.102s][info][gc,reloc    ] GC(0) Y: Using tenuring threshold: 1 (Computed)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZGenerationalYoungGeneration() {
        String logLine = "[0.098s][info][gc,phases   ] GC(0) Y: Young Generation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZHeapStatisticsCompactedGenerational() {
        String logLine = "[0.104s][info][gc,heap     ] GC(0) Y: Compacted:         -                  -"
                + "                  -                 1M (2%)     ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZHeapStatisticsGenerational() {
        String logLine = "[0.104s][info][gc,heap     ] GC(0) Y: Heap Statistics:";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZHeapStatisticsPromotedGenerational() {
        String logLine = "[0.104s][info][gc,heap     ] GC(0) Y:  Promoted:         -                  -"
                + "                 0M (0%)            0M (0%)     ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZLargePages() {
        String logLine = "[0.134s] GC(0) Large Pages: 0 / 0M, Empty: 0M, Relocated: 0M, In-Place: 0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZLargePagesInfo() {
        String logLine = "[0.132s][info][gc,reloc    ] GC(0) Large Pages: 0 / 0M, Empty: 0M, Relocated: 0M, "
                + "In-Place: 0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZLive() {
        String logLine = "[0.134s] GC(0)      Live:         -                 3M (4%)            3M (4%)            "
                + "3M (4%)             -                  -";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZLoad() {
        String logLine = "[0.275s] GC(2) Load: 0.53/0.41/0.33";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZLoadGenerationalOld() {
        String logLine = "[0.119s][info][gc,load     ] GC(0) O: Load: 0.96 (8%) / 0.58 (5%) / 0.74 (6%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZLoadGenerationalYoung() {
        String logLine = "[0.104s][info][gc,load     ] GC(0) Y: Load: 0.96 (8%) / 0.58 (5%) / 0.74 (6%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZLoadGenerationalYoungLowerCaseY() {
        String logLine = "[0.315s][info][gc,load     ] GC(3) y: Load: 0.96 (8%) / 0.58 (5%) / 0.74 (6%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZLoadInfo() {
        String logLine = "[0.132s][info][gc,load     ] GC(0) Load: 0.42/0.38/0.32";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMajorCollectionCodeCacheGCThreshold() {
        String logLine = "[518.231s][info ][gc              ] GC(4) Major Collection (CodeCache GC Threshold)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZMajorCollectionCodeCacheGCThresholdWithDetails() {
        String logLine = "[524.199s][info ][gc              ] GC(4) Major Collection (CodeCache GC Threshold) "
                + "6762M(24%)->4956M(18%) 5.969s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZMajorCollectionProactive() {
        String logLine = "[3107.964s][info ][gc              ] GC(9) Major Collection (Proactive)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZMajorCollectionWarmup() {
        String logLine = "[2021-12-01T10:04:06.358-0500] GC(0) Major Collection (Warmup)";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testZMark() {
        String logLine = "[0.275s] GC(2) Mark: 1 stripe(s), 1 proactive flush(es), 1 terminate flush(es), "
                + "0 completion(s), 0 continuation(s)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMarkingHeader() {
        String logLine = "[0.275s] GC(2)                Mark Start          Mark End        Relocate Start      "
                + "Relocate End           High               Low         ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMarkingHeaderGenerationalOld() {
        String logLine = "[0.228s][info][gc,alloc    ] GC(2) O:                         Mark Start        "
                + "Mark End      Relocate Start    Relocate End   ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMarkingHeaderGenerationalYoung() {
        String logLine = "[0.104s][info][gc,alloc    ] GC(0) Y:                         Mark Start        "
                + "Mark End      Relocate Start    Relocate End   ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMarkingHeaderGenerationalYoungLowerCaseY() {
        String logLine = "[0.315s][info][gc,alloc    ] GC(3) y:                         Mark Start        Mark End"
                + "      Relocate Start    Relocate End";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMarkingHeaderInfo() {
        String logLine = "[0.132s][info][gc,heap     ] GC(0)                Mark Start          Mark End        "
                + "Relocate Start      Relocate End           High               Low";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMarkStackUsage() {
        String logLine = "[0.275s] GC(2) Mark Stack Usage: 32M";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMaxCapacity() {
        String logLine = "[0.132s][info][gc,heap     ] GC(0) Max Capacity: 96M(100%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMediumPages() {
        String logLine = "[2023-12-02T00:22:33.278+0700][2.825s] GC(0) Medium Pages: 0 / 0M, Empty: 0M, Relocated: "
                + "0M, In-Place: 0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMetaspace() {
        String logLine = "[0.275s] GC(2) Metaspace: 3M used, 3M committed, 1032M reserved";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMetaspaceGenerationalOld() {
        String logLine = "[0.228s][info][gc,metaspace] GC(2) O: Metaspace: 2M used, 3M committed, 1088M reserved";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMetaspaceGenerationalYoung() {
        String logLine = "[0.104s][info][gc,metaspace] GC(0) Y: Metaspace: 0M used, 0M committed, 1088M reserved";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMetaspaceGenerationalYoungLowerCaseY() {
        String logLine = "[0.315s][info][gc,metaspace] GC(3) y: Metaspace: 3M used, 4M committed, 1088M reserved";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMinCapacity() {
        String logLine = "[0.132s][info][gc,heap     ] GC(0) Min Capacity: 32M(33%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMinorCollectionAllocationRateGenerational() {
        String logLine = "[0.296s][info][gc          ] GC(3) Minor Collection (Allocation Rate)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMinorCollectionHighUsageGenerational() {
        String logLine = "[1.962s][info][gc          ] GC(382) Minor Collection (High Usage)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZMmu() {
        String logLine = "[0.275s] GC(2) MMU: 2ms/99.6%, 5ms/99.8%, 10ms/99.9%, 20ms/99.9%, 50ms/99.9%, 100ms/100.0%";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZNMethods() {
        String logLine = "[0.275s] GC(2) NMethods: 756 registered, 0 unregistered";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZNMethodsInfo() {
        String logLine = "[0.132s][info][gc,nmethod  ] GC(0) NMethods: 490 registered, 0 unregistered";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZOldGenerationGenerational() {
        String logLine = "[0.212s][info][gc,phases   ] GC(2) O: Old Generation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZOldUsingWorkersGenerational() {
        String logLine = "[0.215s][info][gc,task     ] GC(2) O: Using 1 Workers for Old Generation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZPagesHeaderGenerationalOld() {
        String logLine = "[0.365s][info][gc,reloc    ] GC(4) O:                        Candidates     Selected     "
                + "In-Place         Size        Empty    Relocated ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZPagesHeaderGenerationalYoung() {
        String logLine = "[0.104s][info][gc,reloc    ] GC(0) Y:                        Candidates     Selected     "
                + "In-Place         Size        Empty    Relocated ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZPagesHeaderGenerationalYoungLowerCaseY() {
        String logLine = "[0.315s][info][gc,reloc    ] GC(3) y:                        Candidates     Selected     "
                + "In-Place         Size        Empty    Relocated";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZPhantom() {
        String logLine = "[0.134s] GC(0) Phantom: 25 encountered, 22 discovered, 20 enqueued";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZPhantomReferencesGenerational() {
        String logLine = "[0.228s][info][gc,ref      ] GC(2) O: Phantom References:            29            0"
                + "            0 ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZPreparsing() throws IOException {
        File testFile = TestUtil.getFile("dataset242.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(5, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_MARK_START),
                JdkUtil.LogEventType.Z_MARK_START.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_MARK_END),
                JdkUtil.LogEventType.Z_MARK_END.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_RELOCATE_START),
                JdkUtil.LogEventType.Z_RELOCATE_START.toString() + " collector not identified.");
    }

    @Test
    void testZPreparsingGenerationalOldCollection() throws IOException {
        File testFile = TestUtil.getFile("dataset271.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(7, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_MARK_START_YOUNG_AND_OLD),
                JdkUtil.LogEventType.Z_MARK_START_YOUNG_AND_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_MARK_END_YOUNG),
                JdkUtil.LogEventType.Z_MARK_END_YOUNG.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_MARK_END_OLD),
                JdkUtil.LogEventType.Z_MARK_END_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_RELOCATE_START_YOUNG),
                JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_RELOCATE_START_OLD),
                JdkUtil.LogEventType.Z_RELOCATE_START_OLD.toString() + " collector not identified.");
    }

    @Test
    void testZPreparsingGenerationalYoungCollection() throws IOException {
        File testFile = TestUtil.getFile("dataset272.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(5, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_MARK_START_YOUNG),
                JdkUtil.LogEventType.Z_MARK_START_YOUNG.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_MARK_END_YOUNG),
                JdkUtil.LogEventType.Z_MARK_END_YOUNG.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_RELOCATE_START_YOUNG),
                JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + " collector not identified.");
    }

    @Test
    void testZReclaimed() {
        String logLine = "[0.134s] GC(0) Reclaimed:         -                  -                 0M (0%)            "
                + "5M (6%)             -                  -";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZReferencesHeaderGenerational() {
        String logLine = "[0.228s][info][gc,ref      ] GC(2) O:                       Encountered   Discovered     "
                + "Enqueued ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZRelocationStallC1() {
        String logLine = "[3.394s] Relocation Stall (C1 CompilerThread0) 0.334ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZRelocationStallC2() {
        String logLine = "[0.407s] Relocation Stall (C2 CompilerThread0) 0.702ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZRelocationStallReferenceHandler() {
        String logLine = "[0.407s] Relocation Stall (Reference Handler) 0.702ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZRelocationStallZWorkerOld0() {
        String logLine = "[2023-11-16T09:18:12.565-0500] GC(468) O: Relocation Stall (ZWorkerOld#0) 0.009ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZRelocationStallZWorkerYoung0() {
        String logLine = "[0.750s][info][gc          ] GC(89) y: Relocation Stall (ZWorkerYoung#0) 0.721ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZSmallPages() {
        String logLine = "[0.134s] GC(0) Small Pages: 5 / 10M, Empty: 0M, Relocated: 3M, In-Place: 0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZSmallPagesGenerational() {
        String logLine = "[0.137s][info][gc,reloc    ] GC(1) Y: Small Pages:                    8            3"
                + "            0          16M           0M           1M";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZSoftReferencesGenerational() {
        String logLine = "[0.228s][info][gc,ref      ] GC(2) O: Soft References:             3240            0"
                + "            0 ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZStoppingGenerational() {
        String logLine = "[3.267s][info][gc,exit     ] Stopping ZGC";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZUsingWorkers() {
        String logLine = "[2021-12-01T10:04:06.358-0500] GC(0) Using 1 workers";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZUsingWorkersGenerationalOld() {
        String logLine = "[0.098s][info][gc,task     ] GC(0) Using 1 Workers for Old Generation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZUsingWorkersGenerationalYoung() {
        String logLine = "[0.098s][info][gc,task     ] GC(0) Using 1 Workers for Young Generation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZWarmup() {
        String logLine = "[2021-12-01T10:04:06.358-0500] GC(0) Garbage Collection (Warmup)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZWarmupWithDetails() {
        String logLine = "[0.134s] GC(0) Garbage Collection (Warmup) 10M(10%)->6M(6%)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZWeakReferencesGenerational() {
        String logLine = "[0.228s][info][gc,ref      ] GC(2) O: Weak References:              291            0"
                + "            0 ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZYoungAndOldWarmupWithDetailsGenerational() {
        String logLine = "[0.119s][info][gc          ] GC(0) Major Collection (Warmup) 10M(10%)->14M(15%) 0.022s";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testZYoungGenerationGenerational() {
        String logLine = "[0.296s][info][gc,phases   ] GC(3) y: Young Generation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }
}
