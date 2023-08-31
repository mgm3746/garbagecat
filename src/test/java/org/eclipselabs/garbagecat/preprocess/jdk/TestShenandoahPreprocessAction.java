/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
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
class TestShenandoahPreprocessAction {
    @Test
    void testBadProgressForExernalFragmentationNegativePercent() {
        String logLine = "[2023-08-25T02:17:13.552-0400][308.957s] GC(37) Bad progress for external fragmentation: "
                + "-41.7%, need 1.0%";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testBadProgressForInternalFragmentation() {
        String logLine = "[2023-08-25T02:17:13.552-0400][308.957s] GC(37) Bad progress for internal fragmentation: "
                + "0.1%, need 1.0%";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testBadProgressFreeSpaceDegeneratedGc() {
        String logLine = "    Bad progress for free space: 11750K, need 17305K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testBeginConcurrentMarking() {
        String logLine = "2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testBeginConcurrentUpdateReferences() {
        String logLine = "19.373: [Concurrent update references";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCancellingGcAllocationFailure() {
        String logLine = "[52.872s][info][gc           ] Cancelling GC: Allocation Failure";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testCancellingGcAllocationFailureNoDecorator() {
        String logLine = "    Cancelling GC: Allocation Failure";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testCancellingGcAllocationFailureNoLeadingSpaces() {
        String logLine = "Cancelling GC: Allocation Failure";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testCancellingGcStoppingVmNoDecorator() {
        String logLine = "    Cancelling GC: Stopping VM";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testCancellingGcUpgradeToFullGc() {
        String logLine = "    Cancelling GC: Upgrade To Full GC";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testCleanupStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent cleanup, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testConcurrentCleanup() {
        String logLine = "2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentCleanupConcurrentReset() throws IOException {
        File testFile = TestUtil.getFile("dataset255.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testConcurrentEvacuationPartial() {
        String logLine = "2021-10-27T19:37:39.139-0400: [Concurrent evacuation";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkingNoTime() {
        String logLine = "[2023-02-22T12:31:34.605+0000][2243][gc,start     ] GC(0) Concurrent marking "
                + "(process weakrefs) (unload classes)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkingPartial() {
        String logLine = "2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkingProcessWeakrefs() {
        String logLine = "2020-03-11T07:00:01.007-0400: 0.505: [Concurrent marking (process weakrefs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testConcurrentMarkingUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Concurrent marking (unload classes)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testConcurrentMarkingUpdateRefs() {
        String logLine = "2020-03-11T07:00:51.479-0400: 50.976: [Concurrent marking (update refs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testConcurrentMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "2020-03-11T07:02:09.720-0400: 129.217: [Concurrent marking (update refs) (process weakrefs), "
                + "start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testConcurrentMarkingWithTime() {
        String logLine = "[2023-02-22T12:31:34.629+0000][2243][gc           ] GC(0) Concurrent marking "
                + "(process weakrefs) (unload classes) 24.734ms";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentUpdateReferencesPartial() {
        String logLine = "19.373: [Concurrent update references";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDecoratorPacerForMark() {
        String logLine = "[41.893s][info][gc,ergo      ] GC(1500) Pacer for Mark. Expected Live: 22M, Free: 9M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 8.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testDegeneratedGc() {
        String logLine = "[52.883s][info][gc,start     ] GC(1632) Pause Degenerated GC (Mark)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testDegeneratedGcEvacuationStart() {
        String logLine = "2021-03-23T20:19:44.496+0000: 2871.170: [Pause Degenerated GC (Evacuation), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testDegeneratedGcMarkStart() {
        String logLine = "2021-03-23T20:57:22.923+0000: 120816.207: [Pause Degenerated GC (Mark), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testDegeneratedGcOutsideOfCycle() {
        String logLine = "[8.061s] GC(136) Pause Degenerated GC (Outside of Cycle)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testDegeneratedGcUpdateRefsStart() {
        String logLine = "2021-03-23T20:57:30.141+0000: 120823.424: [Pause Degenerated GC (Update Refs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testEndDuration() {
        String logLine = ", 2714.003 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testEndMetaspace() {
        String logLine = ", [Metaspace: 6477K->6481K(1056768K)]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        // Random metaspace lines are thrown away
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testEvacuationReserve() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Evacuation Reserve: 3M (13 regions), "
                + "Max regular: 256K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testEvacuationReserveNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Evacuation Reserve: 3M (13 regions), Max regular: 256K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testEvacuationReserveUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Evacuation Reserve: 65M (131 regions), "
                + "Max regular: 512K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testEvacuationStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent evacuation, start]";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFailedToAllocate() {
        String logLine = "[52.872s][info][gc           ] Failed to allocate 256K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFailedToAllocateShared() {
        String logLine = "    Failed to allocate Shared, 45072B";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFailedToAllocateSharedNoLeadingSpaces() {
        String logLine = "Failed to allocate Shared, 48280B";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFailedToAllocateTlabNoDecorator() {
        String logLine = "    Failed to allocate TLAB, 4096K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalEvacStart() {
        String logLine = "2020-03-11T07:00:50.985-0400: 50.482: [Pause Final Evac, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkAdaptiveCSetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Adaptive CSet Selection. Target Free: 130M, "
                + "Actual Free: 1084M, Max CSet: 54M, Min Garbage: 0M";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkCollectableGarbageCSetRegions4Digit() {
        String logLine = "    Collectable Garbage: 5964M (95% of total), 102031K CSet, 1516 CSet regions";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkCollectableGarbageUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.201-0200][3113ms] GC(0) Collectable Garbage: 179M (61% of total), "
                + "23M CSet, 407 CSet regions";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkCollectableGarbageWithImmediateBlock() {
        String logLine = "    Collectable Garbage: 30279K (99%), Immediate: 16640K (54%), CSet: 13639K (44%)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset227.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testFinalMarkPacer() {
        String logLine = "    Pacer for Evacuation. Used CSet: 7668K, Free: 49107K, Non-Taxable: 4910K, "
                + "Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkPacerRate2Digit() {
        String logLine = "    Pacer for Evacuation. Used CSet: 1030M, Free: 146M, Non-Taxable: 15012K, "
                + "Alloc Tax Rate: 17.2x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkPacerRate4Digit() {
        String logLine = "    Pacer for Evacuation. Used CSet: 656M, Free: 853K, Non-Taxable: 87437B, "
                + "Alloc Tax Rate: 1923.2x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkPacerUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pacer for Evacuation. Used CSet: 203M, Free: "
                + "1023M, Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkStart() {
        String logLine = "2020-03-11T07:00:01.015-0400: 0.512: [Pause Final Mark (process weakrefs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkStartUnified() {
        String logLine = "[41.911s][info][gc,start     ] GC(1500) Pause Final Mark (update refs) (process weakrefs)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkUsingWorkers() {
        String logLine = "    Using 2 of 2 workers for final marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalUpdateStart() {
        String logLine = "2020-03-11T07:00:04.856-0400: 4.353: [Pause Final Update Refs, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFree() {
        String logLine = "Free: 48924K (192 regions), Max regular: 256K, Max humongous: 42496K, External frag: 14%, "
                + "Internal frag: 0%";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFreeExternalFrag3Digit() {
        String logLine = "[42.421s][info][gc,ergo      ] Free: 8M (72 regions), Max regular: 256K, Max humongous: 0K, "
                + "External frag: 100%, Internal frag: 51%";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFreeHeadroom() {
        String logLine = "Free headroom: 16207K (free) - 3276K (spike) - 0B (penalties) = 12930K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFreeHeadroomUnified() {
        String logLine = "[41.917s][info][gc,ergo      ] Free headroom: 11M (free) - 3M (spike) - 0M (penalties) = 8M";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFreeHeadroomUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Free headroom: 132M (free) - 65M (spike) - 0M "
                + "(penalties) = 67M";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFreeNoGcEventNumber() {
        String logLine = "[41.912s][info][gc,ergo      ] Free: 18M (109 regions), Max regular: 256K, Max humongous: "
                + "1280K, External frag: 94%, Internal frag: 33%";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFreeUnified() {
        String logLine = "[41.911s][info][gc,ergo      ] GC(1500) Free: 18M (109 regions), Max regular: 256K, "
                + "Max humongous: 1280K, External frag: 94%, Internal frag: 33%";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.203-0200][3115ms] GC(0) Free: 1022M (2045 regions), Max regular: 512K, "
                + "Max humongous: 929280K, External frag: 12%, Internal frag: 0%";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFullGc() {
        String logLine = "2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFullGcStart() {
        String logLine = "2021-03-23T20:57:42.349+0000: 120835.633: [Pause Full, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testGoodProgressForExernalFragmentation() {
        String logLine = "[2023-08-25T02:17:17.382-0400][312.787s] GC(38) Good progress for external fragmentation: "
                + "33.9%, need 1.0%";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testGoodProgressFreeSpaceDegeneratedGc() {
        String logLine = "    Good progress for free space: 495M, need 17305K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testGoodProgressFreeSpaceDegeneratedGcUnified() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for free space: 31426K, need 655K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testGoodProgressUsedSpaceDegeneratedGc() {
        String logLine = "   Good progress for used space: 486M, need 512K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testGoodProgressUsedSpaceDegeneratedGcUnified() {
        String logLine = "[52.937s][info][gc,ergo      ] GC(1632) Good progress for used space: 31488K, need 256K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testImmediateGarbage() {
        String logLine = "[0.330s][info][gc,ergo      ] GC(0) Immediate Garbage: 4258K (31% of total), 17 regions";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testinalMarkAdaptiveCSet() {
        String logLine = "    Adaptive CSet Selection. Target Free: 6553K, Actual Free: 52224K, Max CSet: 2730K, Min "
                + "Garbage: 0B";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitMarkMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset225.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testInitMarkProcessWeakRefs() {
        String logLine = "[2021-10-27T13:03:16.626-0400] GC(0) Pause Init Mark (process weakrefs)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testInitMarkStart() {
        String logLine = "[69.704s][info][gc,start     ] GC(2583) Pause Init Mark";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "2020-03-11T07:00:00.999-0400: 0.496: [Pause Init Mark (process weakrefs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitMarkStartProcessWeakrefsUptimeMillisUnified() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Pause Init Mark (process weakrefs)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitMarkStartUpdateRefs() {
        String logLine = "[41.918s][info][gc,start     ] GC(1501) Pause Init Mark (update refs)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitMarkStartUpdateRefsProcessWeakrefs() {
        String logLine = "[41.893s][info][gc,start     ] GC(1500) Pause Init Mark (update refs) (process weakrefs)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitMarkUnloadClasses() {
        String logLine = "[5.593s][info][gc,start     ] GC(99) Pause Init Mark (unload classes)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitUpdate() {
        String logLine = "[2021-10-27T13:03:16.666-0400] GC(2) Pause Init Update Refs";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testInitUpdateMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset224.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testInitUpdatePacer() {
        String logLine = "    Pacer for Update Refs. Used: 15015K, Free: 48702K, Non-Taxable: 4870K, "
                + "Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitUpdatePacer2DigitRate() {
        String logLine = "    Pacer for Update Refs. Used: 1544M, Free: 129M, Non-Taxable: 13303K, "
                + "Alloc Tax Rate: 14.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitUpdatePacerInfiniteRate() {
        String logLine = "    Pacer for Update Refs. Used: 1615M, Free: 0B, Non-Taxable: 0B, Alloc Tax Rate: infx";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitUpdatePacerUpdateMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pacer for Update Refs. Used: 242M, Free: 1020M, "
                + "Non-Taxable: 102M, Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitUpdateStart() {
        String logLine = "2020-03-11T07:00:04.771-0400: 4.268: [Pause Init Update Refs, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitUpdateStartUnified() {
        String logLine = "[69.612s][info][gc,start     ] GC(2582) Pause Init Update Refs";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testInitUpdateStartUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testJdk17Datestamp() throws IOException {
        File testFile = TestUtil.getFile("dataset256.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(12, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testJdk17Uptime() throws IOException {
        File testFile = TestUtil.getFile("dataset236.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
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
        assertEquals(12, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testMetaspaceDataJdk11() {
        String logLine = "[0.258s] Metaspace: 3477K->3501K(1056768K)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" Metaspace: 3477K->3501K(1056768K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceJdk17() {
        String logLine = "[0.196s][info][gc,metaspace] Metaspace: 3118K(3328K)->3130K(3328K) NonClass: "
                + "2860K(2944K)->2872K(2944K) Class: 258K(384K)->258K(384K)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMetaspaceJdk17NoTags() {
        String logLine = "[2022-08-09T17:56:59.074-0400] Metaspace: 3369K(3520K)->3419K(3648K) NonClass: "
                + "3091K(3136K)->3133K(3264K) Class: 278K(384K)->285K(384K)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPacerForIdle() {
        String logLine = "[41.912s][info][gc,ergo      ] Pacer for Idle. Initial: 1M, Alloc Tax Rate: 1.0x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForMark() {
        String logLine = "    Pacer for Mark. Expected Live: 6553K, Free: 44512K, Non-Taxable: 4451K, "
                + "Alloc Tax Rate: 0.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForMark3DigitRate() {
        String logLine = "    Pacer for Mark. Expected Live: 1115M, Free: 12463K, Non-Taxable: 1246K, "
                + "Alloc Tax Rate: 112.0x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForMarkPercent2Digit() {
        String logLine = "[42.019s][info][gc,ergo      ] GC(1505) Pacer for Mark. Expected Live: 22M, Free: 7M, "
                + "Non-Taxable: 0M, Alloc Tax Rate: 11.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForMarkPercentInf() {
        String logLine = "[52.875s][info][gc,ergo      ] GC(1631) Pacer for Mark. Expected Live: 19163K, Free: 0B, "
                + "Non-Taxable: 0B, Alloc Tax Rate: infx";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForMarkUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pacer for Mark. Expected Live: 130M, "
                + "Free: 911M, Non-Taxable: 91M, Alloc Tax Rate: 0.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForPrecleaning() {
        String logLine = "[2020-06-26T15:30:31.311-0400] GC(0) Pacer for Precleaning. Non-Taxable: 98304K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForPrecleaningNoDecorator() {
        String logLine = "    Pacer for Precleaning. Non-Taxable: 128M";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForReset() {
        String logLine = "[2020-06-26T15:30:31.303-0400] GC(0) Pacer for Reset. Non-Taxable: 98304K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForResetNoDecorator() {
        String logLine = "    Pacer for Reset. Non-Taxable: 128M";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPacerForUpdateRefs() {
        String logLine = "[2021-10-27T13:03:16.666-0400] GC(2) Pacer for Update Refs. Used: 32278K, Free: 60967K, "
                + "Non-Taxable: 6096K, Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPauseFinalMark() {
        String logLine = "[2021-10-27T13:03:16.630-0400] GC(0) Pause Final Mark (process weakrefs) 0.674ms";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseFinalMarkNoTime() {
        String logLine = "[2021-10-27T13:03:16.629-0400] GC(0) Pause Final Mark (process weakrefs)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseFinalMarkProcessWeakrefsUnloadClasses() {
        String logLine = "[2023-02-22T12:31:34.630+0000][2243][gc,start     ] GC(0) Pause Final Mark "
                + "(process weakrefs) (unload classes)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseFinalMarkProcessWeakrefsUnloadClassesWithTime() {
        String logLine = "[2023-02-22T12:31:34.641+0000][2243][gc            ] GC(0) Pause Final Mark "
                + "(process weakrefs) (unload classes) 10.861ms";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseFinalRoots() {
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseFinalUpdateNoTime() {
        String logLine = "[2021-10-27T13:03:16.634-0400] GC(0) Pause Final Update Refs";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseFull() {
        String logLine = "[2023-08-25T02:16:58.619-0400][294.024s] GC(22) Pause Full";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseInitMark() {
        String logLine = "[2021-10-27T13:03:16.646-0400] GC(1) Pause Init Mark";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseInitMarkProcessWeakrefsUnloadClasses() {
        String logLine = "[2023-02-22T12:31:34.604+0000][2243][gc,start     ] GC(0) Pause Init Mark (process weakrefs) "
                + "(unload classes)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseInitMarkProcessWeakrefsUnloadClassesWithTime() {
        String logLine = "[2023-02-22T12:31:34.605+0000][2243][gc           ] GC(0) Pause Init Mark (process weakrefs) "
                + "(unload classes) 0.537ms";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseInitUpdateRefs() throws IOException {
        File testFile = TestUtil.getFile("dataset254.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
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
    void testPauseInitUpdateRefsWithTime() {
        String logLine = "[2021-10-27T13:03:16.666-0400] GC(2) Pause Init Update Refs 0.012ms";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseFinalRootsWithTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots 0.019ms";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrecleaningStart() {
        String logLine = "2020-03-11T07:00:01.014-0400: 0.512: [Concurrent precleaning, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testPreprocessingConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset190.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingConcurrentEvacuationCancellingGc() throws IOException {
        File testFile = TestUtil.getFile("dataset231.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.APPLICATION_STOPPED_TIME),
                JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingConcurrentMarkingCancellingGc() throws IOException {
        File testFile = TestUtil.getFile("dataset194.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingConcurrentWithMetaspace() throws IOException {
        File testFile = TestUtil.getFile("dataset191.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingFinalEvac() throws IOException {
        File testFile = TestUtil.getFile("dataset162.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_EVAC),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingFinalMark() throws IOException {
        File testFile = TestUtil.getFile("dataset161.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingFinalUpdate() throws IOException {
        File testFile = TestUtil.getFile("dataset164.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingInitialMark() throws IOException {
        File testFile = TestUtil.getFile("dataset160.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingInitUpdate() throws IOException {
        File testFile = TestUtil.getFile("dataset163.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        // assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingMetaspaceThrowaway() throws IOException {
        File testFile = TestUtil.getFile("dataset259.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(10, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_DEGENERATED_GC),
                JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.APPLICATION_STOPPED_TIME),
                JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
    }

    @Test
    void testResetStart() {
        String logLine = "2020-03-11T07:00:00.997-0400: 0.494: [Concurrent reset, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUncommittedUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Uncommitted 140M. Heap: 1303M reserved, 1163M "
                + "committed, 874M used";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUnifiedConcurrent() {
        String logLine = "[2022-08-09T17:56:59.059-0400] GC(0) Concurrent cleanup 28M->27M(32M) 0.103ms";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUnifiedFinalMarkPacerRateInfinity() {
        String logLine = "[2.919s] GC(67) Pacer for Evacuation. Used CSet: 79104K, Free: 0B, Non-Taxable: 0B, Alloc "
                + "Tax Rate: infx";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUnifiedFinalUpdateMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset228.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
    }

    @Test
    void testUnifiedPauseFinalUpdate() {
        String logLine = "[2021-10-27T13:03:16.634-0400] GC(0) Pause Final Update Refs 0.084ms";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUnifiedPauseInitMark() {
        String logLine = "[2021-10-27T13:03:16.627-0400] GC(0) Pause Init Mark (process weakrefs) 0.575ms";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUnifiedUsingParallel() {
        String logLine = "[0.003s][info][gc] Using Parallel";
        assertFalse(ShenandoahPreprocessAction.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUpdateReferencesStart() {
        String logLine = "2020-03-11T07:00:01.023-0400: 0.520: [Concurrent update references, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersConcurrentReset() {
        String logLine = "    Using 1 of 2 workers for concurrent reset";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForConcurrentEvacuation() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent evacuation";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForConcurrentMarking() {
        String logLine = "[41.893s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForConcurrentMarkingRoots() {
        String logLine = "[0.188s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent marking roots";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUsingWorkersForConcurrentPreclean() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 1 of 4 workers for concurrent preclean";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForConcurrentReferenceUpdate() {
        String logLine = "[69.612s][info][gc,task      ] GC(2582) Using 2 of 4 workers for concurrent reference "
                + "update";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForConcurrentReset() {
        String logLine = "[41.892s][info][gc,task      ] GC(1500) Using 2 of 4 workers for concurrent reset";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForConcurrentResetUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.175-0200][3087ms] GC(0) Using 4 of 4 workers for concurrent reset";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForConcurrentStrongRoot() {
        String logLine = "[0.192s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent strong root";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForConcurrentThreadRoots() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for Concurrent thread roots";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUsingWorkersForConcurrentWeakReferences() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent weak references";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForConcurrentWeakRoot() {
        String logLine = "[0.191s][info][gc,task     ] GC(0) Using 2 of 6 workers for concurrent weak root";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForDegeneratedGc() {
        String logLine = "[52.883s][info][gc,task      ] GC(1632) Using 2 of 2 workers for stw degenerated gc";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForFinalMarking() {
        String logLine = "[41.911s][info][gc,task      ] GC(1500) Using 2 of 4 workers for final marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForFinalReferenceUpdate() {
        String logLine = "[69.644s][info][gc,task      ] GC(2582) Using 2 of 4 workers for final reference update";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersForInitMarking() {
        String logLine = "[2021-10-27T13:03:16.626-0400] GC(0) Using 4 of 6 workers for init marking";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUsingWorkersFullGc() {
        String logLine = "    Using 3 of 3 workers for full gc";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersInitMark() {
        String logLine = "    Using 2 of 2 workers for init marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testUsingWorkersStwDegeneratedGc() {
        String logLine = "    Using 3 of 3 workers for stw degenerated gc";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }
}
