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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestUnifiedConcurrentEvent {

    @Test
    void testClassLoaderData() {
        String logLine = "[66.290s][debug][gc,phases       ] GC(0) O: ClassLoaderData 0.036ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCleanupForNextMark() {
        String logLine = "[16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCleanupForNextMarkWithDuration() {
        String logLine = "[16.082s][info][gc,marking    ] GC(969) Concurrent Cleanup for Next Mark 0.428ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testClearClaimedMarks() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testClearClaimedMarksWithDuration() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Clear Claimed Marks 0.019ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentClassesPurge() {
        String logLine = "[66.292s][debug][gc,phases       ] GC(0) O: Concurrent Classes Purge 0.411ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentClassesUnlink() {
        String logLine = "[66.291s][debug][gc,phases       ] GC(0) O: Concurrent Classes Unlink 1.631ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMark() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkAbort() {
        String logLine = "[2020-06-24T18:13:51.156-0700][177151ms] GC(73) Concurrent Mark Abort";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkContinueZGenerationalYoung() {
        String logLine = "[2023-11-16T09:18:11.215-0500] GC(157) y: Concurrent Mark Continue 0.073ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkCycle() {
        String logLine = "[0.062s][info][gc          ] GC(2) Concurrent Mark Cycle";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkFollowZGenerationalOld() {
        String logLine = "[66.076s][debug][gc,phases       ] GC(0) Y: Concurrent Mark Follow 585.589ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkFree() {
        String logLine = "[0.129s] GC(0) Concurrent Mark Free 0.000ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkRootsZGenerationalOld() {
        String logLine = "[65.490s][debug][gc,phases       ] GC(0) Y: Concurrent Mark Roots 1.615ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkWithDuration() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Mark 1.428ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkWithTimesData() {
        String logLine = "[0.054s][info][gc           ] GC(1) Concurrent Mark 1.260ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentMarkZGenerationalYoung() {
        String logLine = "[0.305s][info][gc,phases   ] GC(3) y: Concurrent Mark 8.889ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentPrecleanWithDuration() {
        String logLine = "[0.083s][info][gc] GC(1) Concurrent Preclean 0.032ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentPrecleanWithTimesData() {
        String logLine = "[0.054s][info][gc           ] GC(1) Concurrent Preclean 0.033ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentProcessNonStrong() {
        String logLine = "[0.213s][info][gc,phases   ] GC(2) O: Concurrent Process Non-Strong 0.658ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentProcessNoStrongReferences() {
        String logLine = "[0.130s] GC(0) Concurrent Process Non-Strong References 0.685ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentRebuildRememberedSetsAndScrubRegionsWithDuration() {
        String logLine = "[2023-11-16T06:43:27.111-0500] GC(5) Concurrent Rebuild Remembered Sets and Scrub Regions "
                + "2.155ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentReferencesEnqueue() {
        String logLine = "[66.292s][debug][gc,phases       ] GC(0) O: Concurrent References Enqueue 0.004ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentReferencesProcess() {
        String logLine = "[66.260s][debug][gc,phases       ] GC(0) O: Concurrent References Process 0.022ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentRelocate() {
        String logLine = "[0.134s] GC(0) Concurrent Relocate 2.550ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentRelocateRemsetFpZGenerationalOld() {
        String logLine = "[66.259s][debug][gc,phases       ] GC(0) Y: Concurrent Relocate Remset FP 0.072ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentRemapRoots() {
        String logLine = "[0.228s][info][gc,phases   ] GC(2) O: Concurrent Remap Roots 12.855ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentReset() {
        String logLine = "[0.085s][info][gc] GC(1) Concurrent Reset";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentResetRelocationSet() {
        String logLine = "[0.100s][info][gc,phases   ] GC(0) Y: Concurrent Reset Relocation Set 0.000ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentResetWithDuration() {
        String logLine = "[0.086s][info][gc] GC(1) Concurrent Reset 0.841ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentResetWithTimesData() {
        String logLine = "[0.056s][info][gc           ] GC(1) Concurrent Reset 0.693ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentSelectRelocationSet() {
        String logLine = "[0.131s] GC(0) Concurrent Select Relocation Set 1.444ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentStringDeduplication() {
        String logLine = "[2021-10-08T16:04:26.204-0400][8.937s] Concurrent String Deduplication (8.937s)";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentStringDeduplicationDetails() {
        String logLine = "[2021-10-08T16:04:26.249-0400][8.983s] Concurrent String Deduplication "
                + "3428.0K->2498.6K(929.4K) avg 27.1% (8.937s, 8.983s) 45.667ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentSweep() {
        String logLine = "[0.084s][info][gc] GC(1) Concurrent Sweep";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentSweepWithDuration() {
        String logLine = "[0.085s][info][gc] GC(1) Concurrent Sweep 0.364ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentSweepWithTimesData() {
        String logLine = "[0.055s][info][gc           ] GC(1) Concurrent Sweep 0.298ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testConcurrentUndoCycle() {
        String logLine = "[2023-01-11T16:09:59.244+0000][19084.784s] GC(300) Concurrent Undo Cycle 54.191ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCreateLiveData() {
        String logLine = "[2.730s][info][gc,marking    ] GC(52) Concurrent Create Live Data";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCreateLiveDataWithDuration() {
        String logLine = "[2.731s][info][gc,marking    ] GC(52) Concurrent Create Live Data 0.483ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCycleDetailed() {
        String logLine = "[16.601s][info][gc           ] GC(1033) Concurrent Cycle";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCycleDetailed12Spaces() {
        String logLine = "[16.121s][info][gc            ] GC(974) Concurrent Cycle";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testCycleDetailedWithDuration() {
        String logLine = "[16.082s][info][gc            ] GC(969) Concurrent Cycle 65.746ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        assertEquals(JdkUtil.EventType.UNIFIED_CONCURRENT,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_CONCURRENT + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[14.859s][info][gc] GC(1083) Concurrent Cycle";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testLogLineCycleWithDuration() {
        String logLine = "[14.904s][info][gc] GC(1083) Concurrent Cycle 45.374ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark    ";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkDoubleTimestampWithDuration() {
        String logLine = "[16.050s][info][gc,marking   ] GC(969) Concurrent Mark (16.017s, 16.050s) 33.614ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkTimestamp() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Mark (16.601s)";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.082s][info][gc] GC(1) Concurrent Mark";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedConcurrentEvent,
                JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + " not parsed.");
    }

    @Test
    void testPrecleanWithDuration() {
        String logLine = "[16.050s][info][gc,marking   ] GC(969) Concurrent Preclean 0.115ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testProcessNonStrongReferencesWithDuration() {
        String logLine = "[10.029s][info][gc,phases   ] GC(162) Concurrent Process Non-Strong References 1.902ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testRebuildRememberedSets() {
        String logLine = "[16.053s][info][gc,marking    ] GC(969) Concurrent Rebuild Remembered Sets";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testResetRelocationSetWithDuration() {
        String logLine = "[10.029s][info][gc,phases   ] GC(162) Concurrent Reset Relocation Set 0.003ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testScanRootRegions() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testScanRootRegionsWithDuration() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Scan Root Regions 0.283ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahClassUnloading() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent class unloading";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahClassUnloadingWithDuration() {
        String logLine = "[0.192s][info][gc          ] GC(0) Concurrent class unloading 0.343ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahCleanupWithSizeAndDuration() {
        String logLine = "[0.472s][info][gc] GC(0) Concurrent cleanup 18M->15M(64M) 0.036ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
        UnifiedConcurrentEvent event = new UnifiedConcurrentEvent(logLine);
        assertEquals(472, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(18), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(15), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(64), event.getCombinedSpace(), "Combined space size not parsed correctly.");
    }

    @Test
    void testShenandoahDetailsReset() {
        String logLine = "[41.893s][info][gc           ] GC(1500) Concurrent reset 50M->50M(64M) 0.126ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahEvacuation() {
        String logLine = "[0.465s][info][gc] GC(0) Concurrent evacuation 17M->19M(64M) 6.528ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahMarking() {
        String logLine = "[0.528s][info][gc] GC(1) Concurrent marking 16M->17M(64M) 7.045ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahMarkingProcessWeakrefs() {
        String logLine = "[0.454s][info][gc] GC(0) Concurrent marking (process weakrefs) 17M->19M(64M) 15.264ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahMarkingProcessWeakrefsUnloadClasses() {
        String logLine = "[2023-02-22T12:31:34.629+0000][2243][gc           ] GC(0) Concurrent marking "
                + "(process weakrefs) (unload classes) 24.734ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahMarkingRootsWithDuration() {
        String logLine = "[0.188s][info][gc          ] GC(0) Concurrent marking roots 0.435ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahMarkingUpdateRefs() {
        String logLine = "[10.458s][info][gc] GC(279) Concurrent marking (update refs) 47M->48M(64M) 5.559ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "[11.012s][info][gc] GC(300) Concurrent marking (update refs) (process weakrefs) "
                + "49M->49M(64M) 5.416ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahPrecleaning() {
        String logLine = "[0.455s][info][gc] GC(0) Concurrent precleaning 19M->19M(64M) 0.202ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahPreprocessedCombinedMetaspace() {
        String logLine = "[0.256s][info][gc           ] GC(0) Concurrent cleanup 32M->18M(36M) 0.036ms Metaspace: "
                + "3867K(7168K)->3872K(7168K)";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
        UnifiedConcurrentEvent event = new UnifiedConcurrentEvent(logLine);
        assertEquals(256, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(32), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(18), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(36), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(kilobytes(3867), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3872), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(7168), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testShenandoahReset() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahResetNoSizes() {
        String logLine = "[41.892s][info][gc,start     ] GC(1500) Concurrent reset";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahStrongRoots() {
        String logLine = "[0.192s][info][gc,start    ] GC(0) Concurrent strong roots";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahStrongRootsWithDuration() {
        String logLine = "[0.192s][info][gc          ] GC(0) Concurrent strong roots 0.302ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahThreadRoots() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent thread roots";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahThreadRootsWithDuration() {
        String logLine = "[0.191s][info][gc          ] GC(0) Concurrent thread roots 0.442ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahUncommitUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Concurrent uncommit 874M->874M(1303M) 5.654ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahUnloadClasses() {
        String logLine = "[5.601s][info][gc           ] GC(99) Concurrent marking (unload classes) 7.346ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahUpdateReferences() {
        String logLine = "[0.470s][info][gc] GC(0) Concurrent update references 19M->19M(64M) 4.708ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahUpdateThreadRootsWithDuration() {
        String logLine = "[0.195s][info][gc          ] GC(0) Concurrent update thread roots 0.359ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahWeakReferences() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent weak references";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahWeakRoots() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent weak roots";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testShenandoahWeakRootsWithDuration() {
        String logLine = "[0.191s][info][gc          ] GC(0) Concurrent weak roots 0.262ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testTriggerCleanups() {
        String logLine = "[66.290s][debug][gc,phases       ] GC(0) O: Trigger cleanups 0.003ms";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_CONCURRENT);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + " not indentified as unified.");
    }

    @Test
    void testUsingWorkersForMarkFromRoots() {
        String logLine = "[16.601s][info][gc,marking   ] GC(1033) Concurrent Mark From Roots";
        assertTrue(UnifiedConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + ".");
    }
}
