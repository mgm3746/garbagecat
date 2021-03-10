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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestShenandoahStatsEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_STATS, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.SHENANDOAH_STATS + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof ShenandoahStatsEvent,
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " not parsed.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_STATS);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testLineAllTimes() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineAllTimesUnified() {
        String logLine = "[0.484s][info][gc,stats     ] All times are wall-clock times, except per-root-class "
                + "counters, that are sum over";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineAllTimesWithLeadingSpaces() {
        String logLine = "  All times are wall-clock times, except per-root-class counters, that are sum over";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineAllWokers() {
        String logLine = "all workers. Dividing the <total> over the root stage time estimates parallelism.";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineAllWokersWithLeadingSpaces() {
        String logLine = "  all workers. Dividing the <total> over the root stage time estimates parallelism.";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUpdateRegionStates() {
        String logLine = "  Update Region States              789 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUpdateRegionStatesUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Update Region States                3 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineSTotal() {
        String logLine = "    S: <total>                    69130 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineSTotalUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     S: <total>                     1469 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSJni() {
        String logLine = "    S: JNI Handles Roots              7 us, workers (us): ---,   7, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSJniUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     S: JNI Handles Roots              1 us, workers (us):"
                + "   1, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineSJfr() {
        String logLine = "    S: JFR Weak Roots                 1 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---,   1, ---, ---, ---, ---, ---, ---, ---";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineSFlat() {
        String logLine = "    S: Flat Profiler Roots          129 us, workers (us): ---, ---, 129, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineWeakRoots() {
        String logLine = "  Weak Roots                         36 us, parallelism: 0.94x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineWeakRootsUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     Weak Roots                      106 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineWrTotal() {
        String logLine = "    WR: <total>                      34 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineWrJfr() {
        String logLine = "    WR: JFR Weak Roots                0 us, workers (us):   0, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineWrJni() {
        String logLine = "    WR: JNI Weak Roots               33 us, workers (us):  33, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineUpdateRegionStates() {
        String logLine = "  Update Region States              234 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineUpdateRegionStatesUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Update Region States                4 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineChooseCollectionSet() {
        String logLine = "  Choose Collection Set             440 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineChooseCollectionSetUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Choose Collection Set              41 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineRebuildFreeSet() {
        String logLine = "  Rebuild Free Set                   36 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineRebuildFreeSetUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Rebuild Free Set                    6 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineETotal() {
        String logLine = "    E: <total>                    69151 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineETotalUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     E: <total>                      876 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testEJni() {
        String logLine = "    E: JNI Handles Roots              3 us, workers (us):   3, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testEJniUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     E: JNI Handles Roots              1 us, workers (us):"
                + "   1, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineEJfr() {
        String logLine = "    E: JFR Weak Roots                 1 us, workers (us): ---, ---, ---,   1, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineEFlat() {
        String logLine = "    E: Flat Profiler Roots           22 us, workers (us):  22, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineUrTotal() {
        String logLine = "    UR: <total>                    3127 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineUrTotalUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     UR: <total>                     139 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineTrashCollectionSet() {
        String logLine = "  Trash Collection Set               61 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineFinishWork() {
        String logLine = "  Finish Work                   1007819 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedFinishWork() {
        String logLine = "[2020-10-26T14:51:41.413-0400]   Finish Work                     10950 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLinePauseDegenerateGcG() {
        String logLine = "Pause Degenerated GC (G)        1296188 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLinePauseDegenerateGcN() {
        String logLine = "Pause Degenerated GC (N)        1285099 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDegenUpdateRoots() {
        String logLine = "  Degen Update Roots               5869 us, parallelism: 17.30x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuThreadRoots() {
        String logLine = "    DU: Thread Roots              11698 us, workers (us): 404, 645, 403, 420, 421, 421, "
                + "3557, 414, 421, 421, 410, 419, 419, 414, 419, 420, 410, 419, 419, 420,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuCodeCacheRoots() {
        String logLine = "    DU: Code Cache Roots          29622 us, workers (us): 1556, 1454, 1534, 1523, 1747, "
                + "1460,   0, 1594, 1437, 1478, 1472, 1840, 1637, 1518, 1529, 1572, 1465, 1522, 1611, 1672,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuUniverseRoots() {
        String logLine = "    DU: Universe Roots                1 us, workers (us):   1, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuJniHandlesRoots() {
        String logLine = "    DU: JNI Handles Roots             4 us, workers (us): ---,   4, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuJfrWeakRoots() {
        String logLine = "    DU: JFR Weak Roots                1 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---,   1, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuJniWeakRoots() {
        String logLine = "    DU: JNI Weak Roots               45 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---,  45, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuStringTableRoots() {
        String logLine = "    DU: String Table Roots        24649 us, workers (us): 1289, 1388, 1296, 1366, 1099, "
                + "1394,  70, 1256, 1344, 1383, 1424, 991, 1209, 1336, 1308, 1283, 1374, 1398, 1276, 1164,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuSynchronizerRoots() {
        String logLine = "    DU: Synchronizer Roots        26688 us, workers (us): 1440, 1441, 1553, 1549, 1526, "
                + "1508, 1476, 1454, 1422, 1391, 1350, 1334, 1268, 1216, 1185, 1170, 1137, 1104, 1098, 1068,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuFlatProfilerRoots() {
        String logLine = "    DU: Flat Profiler Roots         147 us, workers (us): ---, 147, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuManagementRoots() {
        String logLine = "    DU: Management Roots             15 us, workers (us):  15, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuSystemDictRoots() {
        String logLine = "    DU: System Dict Roots            15 us, workers (us): ---, ---,  15, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuCldgRoots() {
        String logLine = "    DU: CLDG Roots                 8481 us, workers (us): 468, 225, 454, 451, 451, 451,  76, "
                + "457, 451, 451, 461, 452, 453, 457, 453, 451, 461, 453, 453, 451,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDuJvmtiRoots() {
        String logLine = "    DU: JVMTI Roots                 171 us, workers (us): 171, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineConcurrentCleanup() {
        String logLine = "[0.590s][info][gc,stats     ] Concurrent Cleanup                   38 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineMain2DigitPercent() {
        String logLine = "[0.661s][info][gc,stats     ]      10 of    71 ms ( 14.0%): main";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineUnloadClasses() {
        String logLine = "[5.608s][info][gc,stats      ]     Unload Classes                   52 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineCleanup() {
        String logLine = "[5.608s][info][gc,stats      ]     Cleanup                        1275 us, "
                + "parallelism: 1.93x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineCuTotal() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: <total>                  2466 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineCuCodeCacheRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: Code Cache Roots          983 us, "
                + "workers (us): 488, 495,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineCuCodeCacheCleaning() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: Code Cache Cleaning       149 us, "
                + "workers (us):  79,  70,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineCuStringTableRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: String Table Roots        358 us, "
                + "workers (us): 183, 175,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineCuResolvedTableRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: Resolved Table Roots        9 us, "
                + "workers (us):   0,   9";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineCuCldgRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: CLDG Roots                967 us, "
                + "workers (us): 479, 489,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineCldg() {
        String logLine = "[5.608s][info][gc,stats      ]     CLDG                              0 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineDeallocateMetadata() {
        String logLine = "    Deallocate Metadata              24 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testLineC1CompilerThread2() {
        String logLine = "      8 of   105 ms (  7.6%): C1 CompilerThread2";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }
    
    @Test
    void testLineC2CompilerThread2() {
        String logLine = "[2.160s][info][gc,stats     ]       3 of    41 ms (  6.7%): C2 CompilerThread0";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testJdk11() {
        File testFile = TestUtil.getFile("dataset195.txt");
        GcManager gcManager1 = new GcManager();
        gcManager1.store(testFile, false);
        GcManager gcManager2 = new GcManager();
        File preprocessedFile = gcManager1.preprocess(testFile, null);
        gcManager2.store(preprocessedFile, false);
        JvmRun jvmRun1 = gcManager1.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun1.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun1.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.");
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " collector not identified.");
        JvmRun jvmRun2 = gcManager2.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(0, jvmRun2.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testJdk11Time() {
        File testFile = TestUtil.getFile("dataset197.txt");
        GcManager gcManager1 = new GcManager();
        gcManager1.store(testFile, false);
        GcManager gcManager2 = new GcManager();
        File preprocessedFile = gcManager1.preprocess(testFile, null);
        gcManager2.store(preprocessedFile, false);
        JvmRun jvmRun1 = gcManager1.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun1.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun1.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.");
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " collector not identified.");
        JvmRun jvmRun2 = gcManager2.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(0, jvmRun2.getEventTypes().size(), "Event type count not correct.");
    }
}
