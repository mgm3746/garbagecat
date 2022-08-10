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
package org.eclipselabs.garbagecat.preprocess.jdk.unified;

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
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
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
    void testAgeTable() {
        String logLine = "[2022-08-03T06:58:41.321+0000][gc,age      ] GC(0) Age table with threshold 15 (max "
                + "threshold 15)";
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testBlankLine() {
        String logLine = "[2022-08-09T17:56:59.074-0400]";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testCmsData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) CMS: 0K->518K(960K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" CMS: 0K->518K(960K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCmsInitialMark() {
        String logLine = "[0.053s][info][gc,start     ] GC(1) Pause Initial Mark";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCmsInitialMarkWithDuration() {
        String logLine = "[0.053s][info][gc           ] GC(1) Pause Initial Mark 0M->0M(2M) 0.278ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testCmsOld() {
        String logLine = "[0.056s][info][gc,heap      ] GC(1) Old: 518K->518K(960K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testConcurrentCleanup() {
        String logLine = "[2022-08-09T17:56:59.058-0400] GC(0) Concurrent cleanup";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentCleanupConcurrentReset() throws IOException {
        File testFile = TestUtil.getFile("dataset255.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testConcurrentClearClaimedMarks() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Clear Claimed Marks";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(null, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentClearClaimedMarksWithDuration() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Clear Claimed Marks 0.080ms";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentCycle() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Cycle";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(null, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMark() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Mark (79857.381s)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkAbort() {
        String logLine = "[2021-03-13T03:37:46.439+0530][79859507ms] GC(8652) Concurrent Mark Abort";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(null, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkDoubleTime() {
        String logLine = "[2022-06-06T08:27:45.926-0500] GC(98825) Concurrent Mark (846234.699s, 846235.254s) "
                + "555.386ms";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkFromRoots() {
        String logLine = "[2021-03-13T03:37:44.312+0530][79857381ms] GC(8652) Concurrent Mark From Roots";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(null, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkResetForOverflow() {
        String logLine = "[2022-05-12T14:50:49.174-0500][410877.325s][info][gc,marking    ] GC(566) Concurrent Mark "
                + "reset for overflow";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(null, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkSpaceAtEnd() {
        String logLine = "[0.053s][info][gc           ] GC(1) Concurrent Mark ";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testConcurrentPreclean() {
        String logLine = "[0.054s][info][gc           ] GC(1) Concurrent Preclean";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
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
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentSweep() {
        String logLine = "[0.055s][info][gc           ] GC(1) Concurrent Sweep";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testDefNewData() {
        String logLine = "[0.112s][info][gc,heap        ] GC(3) DefNew: 1016K->128K(1152K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" DefNew: 1016K->128K(1152K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDefNewDataJdk17() {
        String logLine = "[0.036s][info][gc,heap     ] GC(0) DefNew: 1022K(1152K)->127K(1152K) "
                + "Eden: 1022K(1024K)->0K(1024K) From: 0K(128K)->127K(128K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" DefNew: 1022K(1152K)->127K(1152K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testElasticSearchDefaultLoggingPattern() throws IOException {
        File testFile = TestUtil.getFile("dataset253.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
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
        assertEquals(logLine, preprocessAction.getLogEntry());
    }

    @Test
    void testEnteringSafepointCleanup() {
        String logLine = "[2021-09-14T11:38:31.797-0500][2.454s][info][safepoint    ] Entering safepoint region: "
                + "Cleanup";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testEnteringSafepointExit() {
        String logLine = "[2021-09-22T10:59:49.112-0500][5455002ms] Entering safepoint region: Exit";
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
    void testG1Cleanup() {
        String logLine = "[0.117s][info][gc            ] GC(2) Pause Cleanup 1M->1M(5M) 0.024ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Eden() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Eden regions: 1->0(1)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Eden3Digits() {
        String logLine = "[0.335s][info][gc,heap      ] GC(0) Eden regions: 24->0(149)";
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
    void testG1FullGcTriggerDiagnosticCommand() {
        String logLine = "[2022-05-12T14:53:58.573-0500][411066.724s][info][gc,start      ] GC(567) Pause Full "
                + "(Diagnostic Command)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1FullGcTriggerDiagnosticCommandDetails() {
        String logLine = "[2022-05-12T14:54:09.413-0500][411077.565s][info][gc             ] GC(567) Pause Full "
                + "(Diagnostic Command) 41808M->35651M(49152M) 10840.271ms";
        String nextLogLine = "[2022-05-12T14:54:09.413-0500][411077.565s][info][gc,cpu         ] GC(567) "
                + "User=84.75s Sys=0.00s Real=10.85s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 41808M->35651M(49152M) 10840.271ms", event.getLogEntry(), "Log line not parsed correctly.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
    }

    @Test
    void testG1FullTriggerMetadataGcThreshold() throws IOException {
        File testFile = TestUtil.getFile("dataset210.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
    }

    @Test
    void testG1Humongous() {
        String logLine = "[0.101s][info][gc,heap      ] GC(0) Humongous regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1HumongousDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.821+0000][5413ms] GC(0) Humongous regions: 0->0";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1MarkClosedArchiveRegions() {
        String logLine = "[0.004s][info][gc,cds       ] Mark closed archive regions in map: [0x00000000fff00000, "
                + "0x00000000fff69ff8]";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
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
    void testG1Other() {
        String logLine = "[0.101s][info][gc,phases    ] GC(0)   Other: 0.2ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1Other5Spaces() {
        String logLine = "[16.072s][info][gc,phases     ] GC(971)   Other: 0.1ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testG1OtherDatestampMillis() {
        String logLine = "[2019-05-09T01:39:00.820+0000][5412ms] GC(0)   Other: 0.3ms";
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
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
    void testG1YoungPauseConcurrentStart() {
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
    void testJdk17G1() throws IOException {
        File testFile = TestUtil.getFile("dataset235.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testJdk17Shenandoah() throws IOException {
        File testFile = TestUtil.getFile("dataset236.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
        assertEquals(7, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testMergeHeapRoots() {
        String logLine = "[0.038s][info][gc,phases   ] GC(0)   Merge Heap Roots: 0.1ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
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
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Metaspace: 120K->120K(1056768K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceDataJdk11Shenandoah() {
        String logLine = "[0.258s] Metaspace: 3477K->3501K(1056768K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Metaspace: 3477K->3501K(1056768K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceDataJdk17() {
        String logLine = "[0.061s][info][gc,metaspace] GC(1) Metaspace: 667K(832K)->667K(832K) NonClass: "
                + "617K(704K)->617K(704K) Class: 49K(128K)->49K(128K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
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
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Metaspace: 19460K(19840K)->19460K(19840K)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceTimePidTags() {
        String logLine = "[2022-08-04T11:38:08.058+0000][1908][gc,metaspace] GC(0) Metaspace: 21086K(21504K)->"
                + "21086K(21504K) NonClass: 18491K(18752K)->18491K(18752K) Class: 2594K(2752K)->2594K(2752K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Metaspace: 21086K(21504K)->21086K(21504K)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testMmuTargetViolated() {
        String logLine = "[2021-09-14T11:41:18.173-0500][168.830s][info][gc,mmu        ] GC(26) MMU target violated: "
                + "201.0ms (200.0ms/201.0ms)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
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
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD),
                JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testParNewData() {
        String logLine = "[0.053s][info][gc,heap      ] GC(0) ParNew: 974K->128K(1152K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" ParNew: 974K->128K(1152K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testParOldGenData() {
        String logLine = "[0.030s][info][gc,heap      ] GC(0) ParOldGen: 0K->8K(512K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
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
    void testPauseYoungInfo() {
        String logLine = "[0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M->1M(2M) 0.700ms";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        context.add(UnifiedPreprocessAction.TOKEN);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 1M->1M(2M) 0.700ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfo11SpacesAfterGc() {
        String logLine = "[0.032s][info][gc           ] GC(0) Pause Young (Allocation Failure) 0M->0M(1M) 1.195ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPauseYoungInfoConcurrentStartTriggerG1HumongousAllocation() {
        String logLine = "[2020-06-24T19:24:56.395-0700][4442390ms] GC(126) Pause Young (Concurrent Start) "
                + "(G1 Humongous Allocation) 882M->842M(1223M) 19.777ms";
        String nextLogLine = "[2020-06-24T19:24:56.395-0700][4442390ms] GC(126) User=0.04s Sys=0.00s Real=0.02s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 882M->842M(1223M) 19.777ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoConcurrentStartTriggerMetaGcThreshold() {
        String logLine = "[2020-06-24T18:11:52.781-0700][58776ms] GC(44) Pause Young (Concurrent Start) "
                + "(Metadata GC Threshold) 733M->588M(1223M) 105.541ms";
        String nextLogLine = "[2020-06-24T18:11:52.781-0700][58776ms] GC(44) User=0.18s Sys=0.00s Real=0.11s";
        Set<String> context = new HashSet<String>();
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
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 1M->1M(4M) 0.792ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoNormalTriggerGcLockerWithDatestamp() {
        String logLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "78M->22M(1304M) 35.722ms";
        String nextLogLine = "[2019-05-09T01:39:07.172+0000][11764ms] GC(3) User=0.02s Sys=0.00s Real=0.04s";
        Set<String> context = new HashSet<String>();
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
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(Constants.LINE_SEPARATOR + logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungInfoTriggerHeapDumpInitiatedGc() {
        String logLine = "[2021-11-01T20:48:05.107+0000][240210706ms] GC(950) Pause Young (Heap Dump Initiated GC) "
                + "678M->166M(1678M) 9.184ms";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        context.add(UnifiedPreprocessAction.TOKEN);
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 678M->166M(1678M) 9.184ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungNormalTriggerG1EvacuationPause() {
        String logLine = "[2022-08-03T06:58:41.321+0000][gc          ] GC(0) Pause Young (Normal) (G1 Evacuation Pause)"
                + " 615M->23M(12288M) 7,870ms";
        String nextLogLine = "[2022-08-03T06:58:41.321+0000][gc,cpu      ] GC(0) User=0,04s Sys=0,00s Real=0,01s";
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 615M->23M(12288M) 7,870ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseYoungStart() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testPauseYoungStartTriggerHeapDumpInitiatedGc() {
        String logLine = "[2021-11-01T20:48:05.098+0000][240210697ms] GC(950) Pause Young (Heap Dump Initiated GC)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
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
    void testPreprocessingCms() throws IOException {
        File testFile = TestUtil.getFile("dataset178.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        // assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");

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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_NEW),

                JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingSerialOld() throws IOException {
        File testFile = TestUtil.getFile("dataset172.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD),
                JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingSerialOldTriggerErgonomics() throws IOException {
        File testFile = TestUtil.getFile("dataset174.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SERIAL_OLD),
                JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + " collector not identified.");
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
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" PSOldGen: 0K->8K(512K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPsYoungGenData() {
        String logLine = "[0.032s][info][gc,heap      ] GC(0) PSYoungGen: 512K->464K(1024K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" PSYoungGen: 512K->464K(1024K)", event.getLogEntry(), "Log line not parsed correctly.");
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
    void testSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset215.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_OLD),
                JdkUtil.LogEventType.UNIFIED_OLD.toString() + " collector not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
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
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testSerialOldTriggerErgonomicsDetails() {
        String logLine = "[0.092s][info][gc             ] GC(3) Pause Full (Ergonomics) 0M->0M(3M) 1.849ms";
        String nextLogLine = "[0.092s][info][gc,cpu         ] GC(3) User=0.01s Sys=0.00s Real=0.00s";
        Set<String> context = new HashSet<String>();
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
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 166M->160M(1678M) 189.216ms", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahCancellingGcAllocationFailure() {
        String logLine = "[52.872s][info][gc           ] Cancelling GC: Allocation Failure";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahConcurrent() {
        String logLine = "[2022-08-09T17:56:59.059-0400] GC(0) Concurrent cleanup 28M->27M(32M) 0.103ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahConcurrentMarkingUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Concurrent marking (unload classes)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahDecoratorPacerForMark() {
        String logLine = "[41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 8.5x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahDegeneratedGc() {
        String logLine = "[52.883s][info][gc,start     ] GC(1632) Pause Degenerated GC (Mark)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahDegeneratedGcOutsideOfCycle() {
        String logLine = "[8.061s] GC(136) Pause Degenerated GC (Outside of Cycle)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahEvacuationReserve() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Evacuation Reserve: 3M (13 regions), "
                + "Max regular: 256K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahEvacuationReserveNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Evacuation Reserve: 3M (13 regions), Max regular: 256K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahEvacuationReserveUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Evacuation Reserve: 65M (131 regions), "
                + "Max regular: 512K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFailedToAllocate() {
        String logLine = "[52.872s][info][gc           ] Failed to allocate 256K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFinalEvacStart() {
        String logLine = "[41.912s][info][gc,start     ] GC(1500) Pause Final Evac";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFinalMarkAdaptiveCSetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Adaptive CSet Selection. Target Free: 130M, "
                + "Actual Free: 1084M, Max CSet: 54M, Min Garbage: 0M";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFinalMarkCollectableGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Collectable Garbage: 179M (61% of total), "
                + "23M CSet, 407 CSet regions";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFinalMarkMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset227.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahFinalMarkPacerUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pacer for Evacuation. Used CSet: 203M, Free: "
                + "1023M, Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFinalMarkStart() {
        String logLine = "[41.911s][info][gc,start     ] GC(1500) Pause Final Mark (update refs) (process weakrefs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFinalUpdateMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset228.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahFinalUpdateStart() {
        String logLine = "[69.644s][info][gc,start     ] GC(2582) Pause Final Update Refs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFree() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Free: 18M (109 regions), Max regular: 256K, "
                + "Max humongous: 1280K, External frag: 94%, Internal frag: 33%";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFreeExternalFrag3Digit() {
        String logLine = "[42.421s][info][gc,ergo      ] Free: 8M (72 regions), Max regular: 256K, Max humongous: 0K, "
                + "External frag: 100%, Internal frag: 51%";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFreeHeadroom() {
        String logLine = "[41.917s][info][gc,ergo      ] Free headroom: 11M (free) - 3M (spike) - 0M (penalties) = 8M";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFreeHeadroomUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Free headroom: 132M (free) - 65M (spike) - 0M "
                + "(penalties) = 67M";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFreeNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Free: 18M (109 regions), Max regular: 256K, Max humongous: "
                + "1280K, External frag: 94%, Internal frag: 33%";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Free: 1022M (2045 regions), Max regular: 512K, "
                + "Max humongous: 929280K, External frag: 12%, Internal frag: 0%";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahGoodProgressFreeSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for free space: 31426K, need 655K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahGoodProgressUsedSpaceDegeneratedGc() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for used space: 31488K, need 256K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahImmediateGarbage() {
        String logLine = "[0.330s][info][gc,ergo      ] GC(0) Immediate Garbage: 4258K (31% of total), 17 regions";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMark() {
        String logLine = "[2021-10-27T13:03:16.646-0400] GC(1) Pause Init Mark";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahInitMarkProcessWeakRefs() {
        String logLine = "[2021-10-27T13:03:16.626-0400] GC(0) Pause Init Mark (process weakrefs)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitMarkStart() {
        String logLine = "[69.704s][info][gc,start     ] GC(2583) Pause Init Mark";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahInitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Pause Init Mark (process weakrefs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahInitMarkStartUpdateRefs() {
        String logLine = "[41.918s][info][gc,start     ] GC(1501) Pause Init Mark (update refs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahInitMarkStartUpdateRefsProcessWeakrefs() {
        String logLine = "[41.893s][info][gc,start     ] GC(1500) Pause Init Mark (update refs) (process weakrefs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahInitMarkUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Pause Init Mark (unload classes)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahInitUpdate() {
        String logLine = "[2021-10-27T13:03:16.666-0400] GC(2) Pause Init Update Refs";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahInitUpdateMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset224.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahInitUpdatePacerUpdateMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pacer for Update Refs. Used: 242M, Free: 1020M, "
                + "Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahInitUpdateStart() {
        String logLine = "[69.612s][info][gc,start     ] GC(2582) Pause Init Update Refs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahInitUpdateStartUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahJdk17() throws IOException {
        File testFile = TestUtil.getFile("dataset256.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(7, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
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
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
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
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPacerForIdle() {
        String logLine = "[41.912s][info][gc,ergo      ] Pacer for Idle. Initial: 1M, Alloc Tax Rate: 1.0x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahPacerForMarkPercentInf() {
        String logLine = "[52.875s][info][gc,ergo      ] GC(1631) Pacer for Mark. Expected Live: 19163K, Free: 0B, "
                + "Non-Taxable: 0B, Alloc Tax Rate: infx";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahPacerForMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pacer for Mark. Expected Live: 130M, "
                + "Free: 911M, Non-Taxable: 91M, Alloc Tax Rate: 0.5x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahPacerForMaxkPercent2Digit() {
        String logLine = "[42.019s][info][gc,ergo      ] GC(1505) Pacer for Mark. Expected Live: 22M, Free: 7M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 11.5x";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahPacerForPrecleaning() {
        String logLine = "[2020-06-26T15:30:31.311-0400] GC(0) Pacer for Precleaning. Non-Taxable: 98304K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahPacerForReset() {
        String logLine = "[2020-06-26T15:30:31.303-0400] GC(0) Pacer for Reset. Non-Taxable: 98304K";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
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
    void testShenandoahPauseFinalMarkNoDuration() {
        String logLine = "[2021-10-27T13:03:16.629-0400] GC(0) Pause Final Mark (process weakrefs)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
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
    void testShenandoahPauseFinalUpdateNoDuration() {
        String logLine = "[2021-10-27T13:03:16.634-0400] GC(0) Pause Final Update Refs";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, null, entangledLogLines, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahPauseInitMark() {
        String logLine = "[2021-10-27T13:03:16.627-0400] GC(0) Pause Init Mark (process weakrefs) 0.575ms";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahPauseInitUpdateRefs() throws IOException {
        File testFile = TestUtil.getFile("dataset254.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingFinalEvac() throws IOException {
        File testFile = TestUtil.getFile("dataset162.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_EVAC),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingFinalMark() throws IOException {
        File testFile = TestUtil.getFile("dataset161.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingFinalUpdate() throws IOException {
        File testFile = TestUtil.getFile("dataset164.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingInitialMark() throws IOException {
        File testFile = TestUtil.getFile("dataset160.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahPreprocessingInitUpdate() throws IOException {
        File testFile = TestUtil.getFile("dataset163.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        // assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
    }

    @Test
    void testShenandoahUncommittedUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Uncommitted 140M. Heap: 1303M reserved, 1163M "
                + "committed, 874M used";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentEvacuation() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent evacuation";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentMarking() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent marking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentMarkingRoots() {
        String logLine = "[0.188s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent marking roots";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentPreclean() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 1 of 4 workers for concurrent preclean";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentReferenceUpdate() {
        String logLine = "[69.612s][info][gc,task      ] GC(2582) Using 2 of 4 workers for concurrent reference "
                + "update";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentReset() {
        String logLine = "[41.892s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent reset";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentResetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for concurrent reset";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentStrongRoot() {
        String logLine = "[0.192s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent strong root";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentThreadRoots() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for Concurrent thread roots";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentWeakReferences() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent weak references";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForConcurrentWeakRoot() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent weak root";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForDegeneratedGc() {
        String logLine = "[52.883s][info][gc,task      ] GC(1632) Using 2 of 2 workers for stw degenerated gc";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForFinalMarking() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for final marking";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForFinalReferenceUpdate() {
        String logLine = "[69.644s][info][gc,task      ] GC(2582) Using 2 of 4 workers for final reference update";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testShenandoahUsingWorkersForInitMarking() {
        String logLine = "[2021-10-27T13:03:16.626-0400] GC(0) Using 4 of 6 workers for init marking";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
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
    void testStartG1MixedPause() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause)";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testStartG1YoungPauseConcurrentStart() {
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
    void testTenuredData() {
        String logLine = "[32.636s][info][gc,heap        ] GC(9239) Tenured: 24193K->24195K(25240K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Tenured: 24193K->24195K(25240K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    void testTenuredDataJdk17() {
        String logLine = "[0.036s][info][gc,heap     ] GC(0) Tenured: 0K(768K)->552K(768K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Tenured: 0K(768K)->552K(768K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testTimesData() {
        String logLine = "[0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
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
    void testToSpaceExhausted() {
        String logLine = "[2021-03-13T03:37:40.051+0530][79853119ms] GC(8645) To-space exhausted";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testUnifiedG1FullGcTriggerG1HumongousAllocation() throws IOException {
        File testFile = TestUtil.getFile("dataset232.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_PARALLEL),
                JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedRemarkMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset230.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
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
    void testWeak() {
        String logLine = "[0.134s] GC(0) Weak: 225 encountered, 203 discovered, 43 enqueued";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
    }

    @Test
    void testYoungSingleLine() {
        String logLine = "[1.507s][info][gc] GC(77) Pause Young (Allocation Failure) 24M->4M(25M) 0.509ms";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.UNIFIED.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        UnifiedPreprocessAction event = new UnifiedPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(Constants.LINE_SEPARATOR + logLine, event.getLogEntry(), "Log line not parsed correctly.");
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
    void testZAllocationStallMain() {
        String logLine = "[0.274s] Allocation Stall (main) 12.040ms";
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
    void testZLoadInfo() {
        String logLine = "[0.132s][info][gc,load     ] GC(0) Load: 0.42/0.38/0.32";
        assertTrue(UnifiedPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".");
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
    void testZMetaspace() {
        String logLine = "[0.275s] GC(2) Metaspace: 3M used, 3M committed, 1032M reserved";
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
    void testZPhantom() {
        String logLine = "[0.134s] GC(0) Phantom: 25 encountered, 22 discovered, 20 enqueued";
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
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
    void testZReclaimed() {
        String logLine = "[0.134s] GC(0) Reclaimed:         -                  -                 0M (0%)            "
                + "5M (6%)             -                  -";
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
    void testZSmallPages() {
        String logLine = "[0.134s] GC(0) Small Pages: 5 / 10M, Empty: 0M, Relocated: 3M, In-Place: 0";
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

}
