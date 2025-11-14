/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedShenandoahStatsEvent {

    @Test
    void test3SpacesAccumulateStats() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Accumulate Stats                    4 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test3SpacesCleanup() {
        String logLine = "[2024-04-12T13:14:26.319-0400]   Cleanup                            36 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test3SpacesCmParallelMark() {
        String logLine = "[2025-10-30T12:34:06.464-0400]   CM: Parallel Mark               12046 us, workers (us): "
                + "4017, 4016, 4013, ---, ---, ---,";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test3SpacesCmTotal() {
        String logLine = "[2025-10-30T12:34:06.464-0400]   CM: <total>                     12046 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test3SpacesDegenStwMark() {
        String logLine = "[2024-04-12T17:56:00.636-0400]   Degen STW Mark                   7622 us, parallelism: "
                + "3.45x";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test3SpacesDegenUpdateRoots() {
        String logLine = "[2024-04-12T13:14:26.319-0400]   Degen Update Roots                670 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test3SpacesEvacuation() {
        String logLine = "[2024-04-12T13:14:26.319-0400]   Evacuation                        720 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test3SpacesFinishQueues() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Finish Queues                      47 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test3SpacesFlushSatb() {
        String logLine = "[2025-10-30T12:34:06.464-0400]   Flush SATB                         93 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test3SpacesInitialEvacuation() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Initial Evacuation                801 us, parallelism: "
                + "1.60x:";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test5SpacesCleanup() {
        String logLine = "[2024-04-12T13:14:31.849-0400]     Cleanup                        2050 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test5SpacesDsmTotal() {
        String logLine = "[2024-04-12T17:56:00.636-0400]     DSM: <total>                  26324 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test5SpacesDuTotal() {
        String logLine = "[2024-04-12T13:14:26.319-0400]     DU: <total>                    1670 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test5SpacesETotal() {
        String logLine = "[2024-04-11T20:17:37.413-0400]     E: <total>                     1284 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void test7SpacesCuTotal() {
        String logLine = "[2024-04-12T13:14:31.849-0400]       CU: <total>                  3981 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAllWorkers() {
        String logLine = "[2024-04-12T17:56:00.636-0400] all workers. Dividing the <total> over the root stage time "
                + "estimates parallelism.";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentMarking() {
        String logLine = "[2024-04-11T20:17:37.413-0400] Concurrent Marking                 9893 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentMarkRoots() {
        String logLine = "[2024-04-09T08:26:09.925-0400] Concurrent Mark Roots               363 us, parallelism: "
                + "1.16x";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentPrecleaning() {
        String logLine = "[2024-04-11T20:17:37.413-0400] Concurrent Precleaning             1009 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentReset() {
        String logLine = "[2024-04-11T20:17:37.413-0400] Concurrent Reset                   1827 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentResetAfterCollect() {
        String logLine = "[2025-10-30T12:34:06.464-0400] Concurrent Reset After Collect       58 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentUpdateRefsPrepare() {
        String logLine = "[2025-10-30T12:34:06.464-0400] Concurrent Update Refs Prepare       70 us, workers (us): "
                + "---, ---, ---, ---, ---, ---,";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2024-04-09T08:26:09.935-0400] All times are wall-clock times, except per-root-class "
                + "counters, that are sum over";
        assertEquals(JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS + "not identified.");
    }

    @Test
    void testJdk21() throws IOException {
        File testFile = TestUtil.getFile("dataset284.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(0, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testMakeParsable() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Make Parsable                       5 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2024-04-09T08:26:09.935-0400] All times are wall-clock times, except per-root-class "
                + "counters, that are sum over";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahStatsEvent,
                JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + " not parsed.");
    }

    @Test
    void testPauseDegeneratedGcG() {
        String logLine = "[2024-04-12T13:14:26.319-0400] Pause Degenerated GC (G)           3712 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPauseInitMarkG() {
        String logLine = "[2024-04-11T20:17:37.413-0400] Pause Init Mark (G)                1907 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPauseInitMarkN() {
        String logLine = "[2024-04-11T20:17:37.413-0400] Pause Init Mark (N)                1574 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPauseInitUpdateRefsG() {
        String logLine = "[2024-04-12T13:14:27.251-0400] Pause Init  Update Refs (G)        4388 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testProcess() {
        String logLine = "[2024-04-11T20:17:37.413-0400]     Process                         157 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testResizeTlabs() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Resize TLABs                        3 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testRetireGclabs() {
        String logLine = "[2024-04-12T13:14:27.251-0400]   Retire GCLABs                       4 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testRetireTlabs() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Retire TLABs                        4 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testScanRoots() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Scan Roots                       1223 us, parallelism: "
                + "1.70x";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSJvmtiRoots() {
        String logLine = "[2024-04-11T20:17:37.413-0400]     S: JVMTI Roots                    1 us, workers (us): "
                + "---, ---, ---,   1, ---, --- ";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSTotal() {
        String logLine = "[2024-04-11T20:17:37.413-0400]     S: <total>                     2080 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSystemPurge3() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   System Purge                       57 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSystemPurge5() {
        String logLine = "[2024-04-12T17:56:00.636-0400]     System Purge                   1366 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testThreeSpacePropagateGcState() {
        String logLine = "[2025-10-30T12:34:06.464-0400]   Propagate GC State                  0 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_SHENANDOAH_STATS);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + " not indentified as unified.");
    }

    @Test
    void testUnloadClasses5() {
        String logLine = "[2024-04-12T13:14:31.849-0400]     Unload Classes                   74 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUnloadClasses7() {
        String logLine = "[2024-04-12T17:56:00.636-0400]       Unload Classes               1092 us, parallelism: "
                + "2.49x";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUpdateReferences() {
        String logLine = "[2024-04-12T13:14:26.319-0400]   Update References                2069 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUpdateRegionStates() {
        UnifiedShenandoahStatsEvent priorLogEvent = new UnifiedShenandoahStatsEvent(null);
        String logLine = "[2024-04-11T20:17:37.413-0400]   Update Region States                5 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
        assertEquals(JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS + "not identified.");
    }

    @Test
    void testUpdateRoots() {
        String logLine = "[2024-04-12T13:14:27.251-0400]   Update Roots                      124 us, parallelism: "
                + "1.03x";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUrThreadRoots() {
        String logLine = "[2024-04-11T20:17:37.521-0400]     UR: Thread Roots                 34 us, workers (us):  "
                + "18,  16, ---, ---, ---, ---,";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUrTotal() {
        String logLine = "[2024-04-11T20:17:37.413-0400]     UR: <total>                     186 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWeakReferences3() {
        String logLine = "[2024-04-11T20:17:37.413-0400]   Weak References                   163 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWeakReferences7() {
        String logLine = "[2024-04-12T17:56:00.636-0400]       Weak References               197 us, parallelism: "
                + "0.84x";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWeakRoots() {
        String logLine = "[2024-04-12T13:14:31.849-0400]     Weak Roots                       56 us";
        assertTrue(UnifiedShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_STATS.toString() + ".");
    }
}
