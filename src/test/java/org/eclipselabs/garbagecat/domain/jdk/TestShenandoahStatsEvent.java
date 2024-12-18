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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
 * 
 */
class TestShenandoahStatsEvent {

    @Test
    void test4DigitPercent() {
        String logLine = "  97643 of  5906 ms (1653.2%): <total>";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAdjustPointers() {
        String logLine = "  Adjust Pointers                994706 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAllTimes() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAllWokers() {
        String logLine = "all workers. Dividing the <total> over the root stage time estimates parallelism.";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
        ShenandoahStatsEvent priorLogEvent = new ShenandoahStatsEvent(null);
        assertEquals(JdkUtil.EventType.SHENANDOAH_STATS,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.SHENANDOAH_STATS + "not identified.");
    }

    @Test
    void testC1CompilerThread2() {
        String logLine = "      8 of   105 ms (  7.6%): C1 CompilerThread2";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCalculateAddresses() {
        String logLine = "  Calculate Addresses            823007 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testChooseCollectionSet() {
        String logLine = "  Choose Collection Set             440 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentReset() {
        ShenandoahStatsEvent priorLogEvent = new ShenandoahStatsEvent(null);
        String logLine = "Concurrent Reset                  22380 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
        assertEquals(JdkUtil.EventType.SHENANDOAH_STATS,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.SHENANDOAH_STATS + "not identified.");

    }

    @Test
    void testCopyObjects() {
        String logLine = "  Copy Objects                   545833 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDefaultAccept() {
        String logLine = "     11 of 76651 ms (  0.0%): default Accept";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDefaultIO() {
        String logLine = "     74 of 76651 ms (  0.1%): default I/O-6";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDegenUpdateRoots() {
        String logLine = "  Degen Update Roots               5869 us, parallelism: 17.30x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuCldgRoots() {
        String logLine = "    DU: CLDG Roots                 8481 us, workers (us): 468, 225, 454, 451, 451, 451,  76, "
                + "457, 451, 451, 461, 452, 453, 457, 453, 451, 461, 453, 453, 451,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuCodeCacheRoots() {
        String logLine = "    DU: Code Cache Roots          29622 us, workers (us): 1556, 1454, 1534, 1523, 1747, "
                + "1460,   0, 1594, 1437, 1478, 1472, 1840, 1637, 1518, 1529, 1572, 1465, 1522, 1611, 1672,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuFlatProfilerRoots() {
        String logLine = "    DU: Flat Profiler Roots         147 us, workers (us): ---, 147, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuJfrWeakRoots() {
        String logLine = "    DU: JFR Weak Roots                1 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---,   1, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuJniHandlesRoots() {
        String logLine = "    DU: JNI Handles Roots             4 us, workers (us): ---,   4, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuJniWeakRoots() {
        String logLine = "    DU: JNI Weak Roots               45 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---,  45, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuJvmtiRoots() {
        String logLine = "    DU: JVMTI Roots                 171 us, workers (us): 171, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuManagementRoots() {
        String logLine = "    DU: Management Roots             15 us, workers (us):  15, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuStringTableRoots() {
        String logLine = "    DU: String Table Roots        24649 us, workers (us): 1289, 1388, 1296, 1366, 1099, "
                + "1394,  70, 1256, 1344, 1383, 1424, 991, 1209, 1336, 1308, 1283, 1374, 1398, 1276, 1164,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuSynchronizerRoots() {
        String logLine = "    DU: Synchronizer Roots        26688 us, workers (us): 1440, 1441, 1553, 1549, 1526, "
                + "1508, 1476, 1454, 1422, 1391, 1350, 1334, 1268, 1216, 1185, 1170, 1137, 1104, 1098, 1068,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuSystemDictRoots() {
        String logLine = "    DU: System Dict Roots            15 us, workers (us): ---, ---,  15, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuThreadRoots() {
        String logLine = "    DU: Thread Roots              11698 us, workers (us): 404, 645, 403, 420, 421, 421, "
                + "3557, 414, 421, 421, 410, 419, 419, 414, 419, 420, 410, 419, 419, 420,";

        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuUniverseRoots() {
        String logLine = "    DU: Universe Roots                1 us, workers (us):   1, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testEFlat() {
        String logLine = "    E: Flat Profiler Roots           22 us, workers (us):  22, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testEJfr() {
        String logLine = "    E: JFR Weak Roots                 1 us, workers (us): ---, ---, ---,   1, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testEJni() {
        String logLine = "    E: JNI Handles Roots              3 us, workers (us):   3, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testETotal() {
        String logLine = "    E: <total>                    69151 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFaJfrWeakRoots() {
        String logLine = "    FA: JFR Weak Roots                1 us, workers (us):   1, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFaThreadRoots() {
        String logLine = "    FA: Thread Roots              14090 us, workers (us): 6396, 5528, 2166,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFaTotal() {
        String logLine = "    FA: <total>                   93431 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFinishQueues() {
        String logLine = "  Finish Queues                    3023 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFinishWork() {
        String logLine = "  Finish Work                   1007819 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFlatProfilerRoots() {
        String logLine = "    FS: Flat Profiler Roots         115 us, workers (us): ---, 115, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFsThreadRoots() {
        String logLine = "    FS: Thread Roots              14587 us, workers (us): 7245, 120, 7222,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFsTotal() {
        String logLine = "    FS: <total>                   33402 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertEquals(JdkUtil.EventType.SHENANDOAH_STATS,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.SHENANDOAH_STATS + "not identified.");
    }

    @Test
    void testJniHandlesRoots() {
        String logLine = "    FS: JNI Handles Roots            22 us, workers (us): ---,  22, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testMark() {
        String logLine = "  Mark                          1559756 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahStatsEvent,
                JdkUtil.EventType.SHENANDOAH_STATS.toString() + " not parsed.");
    }

    @Test
    void testPauseDegenerateGcG() {
        String logLine = "Pause Degenerated GC (G)        1296188 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPauseDegenerateGcN() {
        String logLine = "Pause Degenerated GC (N)        1285099 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPauseFullGcG() {
        String logLine = "Pause Full GC (G)               4085272 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPauseFullGcN() {
        String logLine = "Pause Full GC (N)               4077354 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPreHeapDump() {
        String logLine = "  Pre Heap Dump                       0 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testRebuildFreeSet() {
        String logLine = "  Rebuild Free Set                   36 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSFlat() {
        String logLine = "    S: Flat Profiler Roots          129 us, workers (us): ---, ---, 129, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSJfr() {
        String logLine = "    S: JFR Weak Roots                 1 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---,   1, ---, ---, ---, ---, ---, ---, ---";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSJni() {
        String logLine = "    S: JNI Handles Roots              7 us, workers (us): ---,   7, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSTotal() {
        String logLine = "    S: <total>                    69130 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSystemPurge() {
        String logLine = "  System Purge                     1044 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testTrashCollectionSet() {
        String logLine = "  Trash Collection Set               61 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.SHENANDOAH_STATS);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testUnloadClasses() {
        String logLine = "    Unload Classes                    6 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUpdateRegionStates() {
        String logLine = "  Update Region States              789 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUpdateRoots() {
        String logLine = "  Update Roots                     4231 us, parallelism: 2.26x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUrTotal() {
        String logLine = "    UR: <total>                    3127 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWeakReferences() {
        String logLine = "  Weak References                  2251 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWeakRoots() {
        String logLine = "  Weak Roots                         36 us, parallelism: 0.94x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWrJfr() {
        String logLine = "    WR: JFR Weak Roots                0 us, workers (us):   0, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWrJni() {
        String logLine = "    WR: JNI Weak Roots               33 us, workers (us):  33, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWrTotal() {
        String logLine = "    WR: <total>                      34 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_STATS.toString() + ".");
    }
}
