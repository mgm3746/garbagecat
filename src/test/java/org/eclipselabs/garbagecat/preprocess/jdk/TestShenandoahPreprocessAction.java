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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestShenandoahPreprocessAction {

    @Test
    void testLogLineInitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "2020-03-11T07:00:00.999-0400: 0.496: [Pause Init Mark (process weakrefs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineDegeneratedGcMarkStart() {
        String logLine = "2021-03-23T20:57:22.923+0000: 120816.207: [Pause Degenerated GC (Mark), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineDegeneratedGcEvacuationStart() {
        String logLine = "2021-03-23T20:19:44.496+0000: 2871.170: [Pause Degenerated GC (Evacuation), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineDegeneratedGcUpdateRefsStart() {
        String logLine = "2021-03-23T20:57:30.141+0000: 120823.424: [Pause Degenerated GC (Update Refs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFullGcStart() {
        String logLine = "2021-03-23T20:57:42.349+0000: 120835.633: [Pause Full, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineGoodProgressFreeSpaceDegeneratedGc() {
        String logLine = "    Good progress for free space: 495M, need 17305K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineBadProgressFreeSpaceDegeneratedGc() {
        String logLine = "    Bad progress for free space: 11750K, need 17305K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkStart() {
        String logLine = "2020-03-11T07:00:01.015-0400: 0.512: [Pause Final Mark (process weakrefs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalEvacStart() {
        String logLine = "2020-03-11T07:00:50.985-0400: 50.482: [Pause Final Evac, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineInitUpdateStart() {
        String logLine = "2020-03-11T07:00:04.771-0400: 4.268: [Pause Init Update Refs, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalUpdateStart() {
        String logLine = "2020-03-11T07:00:04.856-0400: 4.353: [Pause Final Update Refs, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFailedToAllocateShared() {
        String logLine = "    Failed to allocate Shared, 45072B";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFailedToAllocateSharedNoLeadingSpaces() {
        String logLine = "Failed to allocate Shared, 48280B";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUnifiedUsingParallel() {
        String logLine = "[0.003s][info][gc] Using Parallel";
        assertFalse(ShenandoahPreprocessAction.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineResetStart() {
        String logLine = "2020-03-11T07:00:00.997-0400: 0.494: [Concurrent reset, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineCleanupStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent cleanup, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePrecleaningStart() {
        String logLine = "2020-03-11T07:00:01.014-0400: 0.512: [Concurrent precleaning, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineEvacuationStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent evacuation, start]";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineConcurrentMarkingPartial() {
        String logLine = "2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineConcurrentUpdateReferencesPartial() {
        String logLine = "19.373: [Concurrent update references";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineConcurrentEvacuationPartial() {
        String logLine = "2021-10-27T19:37:39.139-0400: [Concurrent evacuation";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineUpdateReferencesStart() {
        String logLine = "2020-03-11T07:00:01.023-0400: 0.520: [Concurrent update references, start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineConcurrentMarkingProcessWeakrefs() {
        String logLine = "2020-03-11T07:00:01.007-0400: 0.505: [Concurrent marking (process weakrefs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineConcurrentMarkingUpdateRefs() {
        String logLine = "2020-03-11T07:00:51.479-0400: 50.976: [Concurrent marking (update refs), start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineConcurrentMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "2020-03-11T07:02:09.720-0400: 129.217: [Concurrent marking (update refs) (process weakrefs), "
                + "start]";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFailedToAllocateTlabNoDecorator() {
        String logLine = "    Failed to allocate TLAB, 4096K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineCancellingGcAllocationFailureNoDecorator() {
        String logLine = "    Cancelling GC: Allocation Failure";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineCancellingGcStoppingVmNoDecorator() {
        String logLine = "    Cancelling GC: Stopping VM";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineCancellingGcUpgradeToFullGc() {
        String logLine = "    Cancelling GC: Upgrade To Full GC";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineBeginConcurrentMarking() {
        String logLine = "2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginConcurrentUpdateReferences() {
        String logLine = "19.373: [Concurrent update references";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndDuration() {
        String logLine = ", 2714.003 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineConcurrentCleanup() {
        String logLine = "2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndMetaspace() {
        String logLine = ", [Metaspace: 6477K->6481K(1056768K)]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineGoodProgressUsedSpaceDegeneratedGc() {
        String logLine = "   Good progress for used space: 486M, need 512K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUsingWorkersConcurrentReset() {
        String logLine = "    Using 1 of 2 workers for concurrent reset";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFreeHeadroom() {
        String logLine = "Free headroom: 16207K (free) - 3276K (spike) - 0B (penalties) = 12930K";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFree() {
        String logLine = "Free: 48924K (192 regions), Max regular: 256K, Max humongous: 42496K, External frag: 14%, "
                + "Internal frag: 0%";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineInitUpdatePacer() {
        String logLine = "    Pacer for Update Refs. Used: 15015K, Free: 48702K, Non-Taxable: 4870K, "
                + "Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePacerForMark() {
        String logLine = "    Pacer for Mark. Expected Live: 6553K, Free: 44512K, Non-Taxable: 4451K, "
                + "Alloc Tax Rate: 0.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePacerForMark3DigitRate() {
        String logLine = "    Pacer for Mark. Expected Live: 1115M, Free: 12463K, Non-Taxable: 1246K, "
                + "Alloc Tax Rate: 112.0x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineInitUpdatePacer2DigitRate() {
        String logLine = "    Pacer for Update Refs. Used: 1544M, Free: 129M, Non-Taxable: 13303K, "
                + "Alloc Tax Rate: 14.5x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineInitUpdatePacerInfiniteRate() {
        String logLine = "    Pacer for Update Refs. Used: 1615M, Free: 0B, Non-Taxable: 0B, Alloc Tax Rate: infx";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUsingWorkersStwDegeneratedGc() {
        String logLine = "    Using 3 of 3 workers for stw degenerated gc";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkCollectableGarbageWithImmediateBlock() {
        String logLine = "    Collectable Garbage: 30279K (99%), Immediate: 16640K (54%), CSet: 13639K (44%)";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkPacer() {
        String logLine = "    Pacer for Evacuation. Used CSet: 7668K, Free: 49107K, Non-Taxable: 4910K, "
                + "Alloc Tax Rate: 1.1x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkPacer2DigitRate() {
        String logLine = "    Pacer for Evacuation. Used CSet: 1030M, Free: 146M, Non-Taxable: 15012K, "
                + "Alloc Tax Rate: 17.2x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkPacer4DigitRate() {
        String logLine = "    Pacer for Evacuation. Used CSet: 656M, Free: 853K, Non-Taxable: 87437B, "
                + "Alloc Tax Rate: 1923.2x";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePacerForPrecleaningNoDecorator() {
        String logLine = "    Pacer for Precleaning. Non-Taxable: 128M";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUsingWorkersInitMark() {
        String logLine = "    Using 2 of 2 workers for init marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineUsingWorkersFullGc() {
        String logLine = "    Using 3 of 3 workers for full gc";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineFinalMarkUsingWorkers() {
        String logLine = "    Using 2 of 2 workers for final marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLinePacerForResetNoDecorator() {
        String logLine = "    Pacer for Reset. Non-Taxable: 128M";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineCancellingGcAllocationFailureNoLeadingSpaces() {
        String logLine = "Cancelling GC: Allocation Failure";
        assertTrue(ShenandoahPreprocessAction.match(logLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testLogLineinalMarkAdaptiveCSet() {
        String logLine = "    Adaptive CSet Selection. Target Free: 6553K, Actual Free: 52224K, Max CSet: 2730K, Min "
                + "Garbage: 0B";
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testPreprocessingConcurrentMarkingCancellingGc() throws IOException {
        File testFile = TestUtil.getFile("dataset194.txt");
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
    void testPreprocessingConcurrentEvacuationCancellingGc() throws IOException {
        File testFile = TestUtil.getFile("dataset231.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
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
}
