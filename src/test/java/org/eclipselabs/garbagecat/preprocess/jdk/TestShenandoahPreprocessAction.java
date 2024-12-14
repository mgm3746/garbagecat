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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestShenandoahPreprocessAction {

    @Test
    void testBadProgressFreeSpaceDegeneratedGc() {
        String logLine = "    Bad progress for free space: 11750K, need 17305K";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testBeginConcurrentMarking() {
        String logLine = "2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testBeginConcurrentUpdateReferences() {
        String logLine = "19.373: [Concurrent update references";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context,
                null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCancellingGcAllocationFailureNoDecorator() {
        String logLine = "    Cancelling GC: Allocation Failure";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCancellingGcStoppingVmNoDecorator() {
        String logLine = "    Cancelling GC: Stopping VM";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testCancellingGcUpgradeToFullGc() {
        String logLine = "    Cancelling GC: Upgrade To Full GC";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentCleanup() {
        String logLine = "2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context,
                null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentCleanupConcurrentReset() throws IOException {
        File testFile = TestUtil.getFile("dataset255.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_CONCURRENT),
                JdkUtil.EventType.SHENANDOAH_CONCURRENT.toString() + " event not identified.");
    }

    @Test
    void testConcurrentCleanupStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent cleanup, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentEvacuationPartial() {
        String logLine = "2021-10-27T19:37:39.139-0400: [Concurrent evacuation";
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkingPartial() {
        String logLine = "2020-08-18T14:05:39.789+0000: 854865.439: [Concurrent marking";
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkingProcessWeakrefs() {
        String logLine = "2020-03-11T07:00:01.007-0400: 0.505: [Concurrent marking (process weakrefs), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkingUnloadClassesStart() {
        String logLine = "2024-04-12T13:21:24.037-0400: 4.907: [Concurrent marking (unload classes), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkingUpdateRefs() {
        String logLine = "2020-03-11T07:00:51.479-0400: 50.976: [Concurrent marking (update refs), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "2020-03-11T07:02:09.720-0400: 129.217: [Concurrent marking (update refs) (process weakrefs), "
                + "start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testConcurrentUpdateReferencesPartial() {
        String logLine = "19.373: [Concurrent update references";
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        Set<String> context = new HashSet<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, null, null, context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDegeneratedGcEvacuationStart() {
        String logLine = "2021-03-23T20:19:44.496+0000: 2871.170: [Pause Degenerated GC (Evacuation), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDegeneratedGcMarkStart() {
        String logLine = "2021-03-23T20:57:22.923+0000: 120816.207: [Pause Degenerated GC (Mark), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testDegeneratedGcUpdateRefsStart() {
        String logLine = "2021-03-23T20:57:30.141+0000: 120823.424: [Pause Degenerated GC (Update Refs), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testEndDuration() {
        String logLine = ", 2714.003 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context,
                null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testEndMetaspace() {
        String logLine = ", [Metaspace: 6477K->6481K(1056768K)]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context,
                null);
        // Random metaspace lines are thrown away
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testEvacuationStart() {
        String logLine = "2020-03-11T07:00:01.020-0400: 0.517: [Concurrent evacuation, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFailedToAllocateShared() {
        String logLine = "    Failed to allocate Shared, 45072B";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFailedToAllocateTlabNoDecorator() {
        String logLine = "    Failed to allocate TLAB, 4096K";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalEvacStart() {
        String logLine = "2020-03-11T07:00:50.985-0400: 50.482: [Pause Final Evac, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalMark() {
        String logLine = "2024-04-05T16:31:23.569-0400: 0.526: [Pause Final Mark (process weakrefs), 0.498 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalMarkAdaptiveCSet() {
        String logLine = "    Adaptive CSet Selection. Target Free: 6553K, Actual Free: 52224K, Max CSet: 2730K, Min "
                + "Garbage: 0B";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
        ;
    }

    @Test
    void testFinalMarkCollectableGarbageCSetRegions4Digit() {
        String logLine = "    Collectable Garbage: 5964M (95% of total), 102031K CSet, 1516 CSet regions";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalMarkCollectableGarbageWithImmediateBlock() {
        String logLine = "    Collectable Garbage: 30279K (99%), Immediate: 16640K (54%), CSet: 13639K (44%)";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalMarkPacer() {
        String logLine = "    Pacer for Evacuation. Used CSet: 7668K, Free: 49107K, Non-Taxable: 4910K, "
                + "Alloc Tax Rate: 1.1x";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalMarkPacerRate2Digit() {
        String logLine = "    Pacer for Evacuation. Used CSet: 1030M, Free: 146M, Non-Taxable: 15012K, "
                + "Alloc Tax Rate: 17.2x";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalMarkPacerRate4Digit() {
        String logLine = "    Pacer for Evacuation. Used CSet: 656M, Free: 853K, Non-Taxable: 87437B, "
                + "Alloc Tax Rate: 1923.2x";
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
    }

    @Test
    void testFinalMarkStart() {
        String logLine = "2020-03-11T07:00:01.015-0400: 0.512: [Pause Final Mark (process weakrefs), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalMarkUsingWorkers() {
        String logLine = "    Using 2 of 2 workers for final marking";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFinalUpdateStart() {
        String logLine = "2020-03-11T07:00:04.856-0400: 4.353: [Pause Final Update Refs, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFree() {
        String logLine = "Free: 48924K (192 regions), Max regular: 256K, Max humongous: 42496K, External frag: 14%, "
                + "Internal frag: 0%";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFreeHeadroom() {
        String logLine = "Free headroom: 16207K (free) - 3276K (spike) - 0B (penalties) = 12930K";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFullGc() {
        String logLine = "2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.SHENANDOAH.toString() + ".");
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, null, context,
                null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testFullGcStart() {
        String logLine = "2021-03-23T20:57:42.349+0000: 120835.633: [Pause Full, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testGoodProgressFreeSpaceDegeneratedGc() {
        String logLine = "    Good progress for free space: 495M, need 17305K";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testGoodProgressUsedSpaceDegeneratedGc() {
        String logLine = "    Good progress for used space: 486M, need 512K";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testInitMarkStartProcessWeakrefsUptimeMillis() {
        String logLine = "2020-03-11T07:00:00.999-0400: 0.496: [Pause Init Mark (process weakrefs), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testInitUpdatePacer() {
        String logLine = "    Pacer for Update Refs. Used: 15015K, Free: 48702K, Non-Taxable: 4870K, "
                + "Alloc Tax Rate: 1.1x";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testInitUpdatePacer2DigitRate() {
        String logLine = "    Pacer for Update Refs. Used: 1544M, Free: 129M, Non-Taxable: 13303K, "
                + "Alloc Tax Rate: 14.5x";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testInitUpdatePacerInfiniteRate() {
        String logLine = "    Pacer for Update Refs. Used: 1615M, Free: 0B, Non-Taxable: 0B, Alloc Tax Rate: infx";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testInitUpdateRefs() {
        String logLine = "2024-04-05T16:31:23.570-0400: 0.527: [Pause Init Update Refs, 0.011 ms]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testInitUpdateStart() {
        String logLine = "2020-03-11T07:00:04.771-0400: 4.268: [Pause Init Update Refs, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testMarkingStart() {
        String logLine = "2022-10-28T10:58:59.284-0400: [Concurrent marking, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPacerForMark() {
        String logLine = "    Pacer for Mark. Expected Live: 6553K, Free: 44512K, Non-Taxable: 4451K, "
                + "Alloc Tax Rate: 0.5x";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPacerForMark3DigitRate() {
        String logLine = "    Pacer for Mark. Expected Live: 1115M, Free: 12463K, Non-Taxable: 1246K, "
                + "Alloc Tax Rate: 112.0x";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPacerForPrecleaningNoDecorator() {
        String logLine = "    Pacer for Precleaning. Non-Taxable: 128M";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPacerForResetNoDecorator() {
        String logLine = "    Pacer for Reset. Non-Taxable: 128M";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseFinalMarkUnloadClassesStart() {
        String logLine = "2024-04-12T13:21:24.042-0400: 4.911: [Pause Final Mark (unload classes), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPauseInitMarkUnloadClassesStart() {
        String logLine = "2024-04-12T13:21:24.037-0400: 4.906: [Pause Init Mark (unload classes), start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPrecleaningStart() {
        String logLine = "2020-03-11T07:00:01.014-0400: 0.512: [Concurrent precleaning, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testPreprocessingConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset190.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_CONCURRENT),
                JdkUtil.EventType.SHENANDOAH_CONCURRENT.toString() + " event not identified.");
    }

    @Test
    void testPreprocessingConcurrentEvacuationCancellingGc() throws IOException {
        File testFile = TestUtil.getFile("dataset231.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_CONCURRENT),
                JdkUtil.EventType.SHENANDOAH_CONCURRENT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.APPLICATION_STOPPED_TIME),
                JdkUtil.EventType.APPLICATION_STOPPED_TIME.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + " event not identified.");
    }

    @Test
    void testPreprocessingConcurrentMarkingCancellingGc() throws IOException {
        File testFile = TestUtil.getFile("dataset194.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_CONCURRENT),
                JdkUtil.EventType.SHENANDOAH_CONCURRENT.toString() + " event not identified.");
    }

    @Test
    void testPreprocessingConcurrentWithMetaspace() throws IOException {
        File testFile = TestUtil.getFile("dataset191.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_CONCURRENT),
                JdkUtil.EventType.SHENANDOAH_CONCURRENT.toString() + " event not identified.");
    }

    @Test
    void testPreprocessingMetaspaceThrowaway() throws IOException {
        File testFile = TestUtil.getFile("dataset259.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(6, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_CONCURRENT),
                JdkUtil.EventType.SHENANDOAH_CONCURRENT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_DEGENERATED_GC),
                JdkUtil.EventType.SHENANDOAH_DEGENERATED_GC.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.APPLICATION_STOPPED_TIME),
                JdkUtil.EventType.APPLICATION_STOPPED_TIME.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_INIT_MARK),
                JdkUtil.EventType.SHENANDOAH_INIT_MARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.EventType.SHENANDOAH_INIT_UPDATE.toString() + " event not identified.");
    }

    @Test
    void testResetStart() {
        String logLine = "2020-03-11T07:00:00.997-0400: 0.494: [Concurrent reset, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUncommitStart() {
        String logLine = "2021-03-12T06:36:18.692+0000: 58175.759: [Concurrent uncommit, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUpdateReferencesStart() {
        String logLine = "2020-03-11T07:00:01.023-0400: 0.520: [Concurrent update references, start]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUsingWorkersConcurrentReset() {
        String logLine = "    Using 1 of 2 workers for concurrent reset";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUsingWorkersFullGc() {
        String logLine = "    Using 3 of 3 workers for full gc";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUsingWorkersInitMark() {
        String logLine = "    Using 2 of 2 workers for init marking";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testUsingWorkersStwDegeneratedGc() {
        String logLine = "    Using 3 of 3 workers for stw degenerated gc";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        assertTrue(ShenandoahPreprocessAction.match(logLine, null),
                "Log line not recognized as " + PreprocessActionType.SHENANDOAH.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        ShenandoahPreprocessAction event = new ShenandoahPreprocessAction(null, logLine, nextLogLine, entangledLogLines,
                context, null);
        assertNull(event.getLogEntry(), "Log line not parsed correctly.");
    }
}
