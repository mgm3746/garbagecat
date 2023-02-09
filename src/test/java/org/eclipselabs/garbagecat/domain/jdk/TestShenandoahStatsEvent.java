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
package org.eclipselabs.garbagecat.domain.jdk;

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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
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
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAdjustPointers() {
        String logLine = "  Adjust Pointers                994706 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAllTimes() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAllTimesUnified() {
        String logLine = "[0.484s][info][gc,stats     ] All times are wall-clock times, except per-root-class "
                + "counters, that are sum over";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAllTimesWithLeadingSpaces() {
        String logLine = "  All times are wall-clock times, except per-root-class counters, that are sum over";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAllWokers() {
        String logLine = "all workers. Dividing the <total> over the root stage time estimates parallelism.";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testAllWokersWithLeadingSpaces() {
        String logLine = "  all workers. Dividing the <total> over the root stage time estimates parallelism.";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testC1CompilerThread2() {
        String logLine = "      8 of   105 ms (  7.6%): C1 CompilerThread2";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testC2CompilerThread2() {
        String logLine = "[2.160s][info][gc,stats     ]       3 of    41 ms (  6.7%): C2 CompilerThread0";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCalculateAddresses() {
        String logLine = "  Calculate Addresses            823007 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testChooseCollectionSet() {
        String logLine = "  Choose Collection Set             440 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testChooseCollectionSetUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Choose Collection Set              41 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCldg() {
        String logLine = "[5.608s][info][gc,stats      ]     CLDG                              0 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCleanup() {
        String logLine = "[5.608s][info][gc,stats      ]     Cleanup                        1275 us, "
                + "parallelism: 1.93x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCmrThreadRoots() {
        String logLine = "[0.196s][info][gc,stats    ]   CMR: Thread Roots                 478 us, workers (us): 102, "
                + "376, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCmrTotal() {
        String logLine = "[0.196s][info][gc,stats    ]   CMR: <total>                      653 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCmrVmStrongRoots() {
        String logLine = "[0.196s][info][gc,stats    ]   CMR: VM Strong Roots               55 us, workers (us):  19,  "
                + "36, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCodeRoots() {
        String logLine = "[0.196s][info][gc,stats    ]     Code Roots                      298 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentClassUnloading() {
        String logLine = "[0.196s][info][gc,stats    ] Concurrent Class Unloading          357 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentCleanup() {
        String logLine = "[0.590s][info][gc,stats     ] Concurrent Cleanup                   38 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentMarkRoots() {
        String logLine = "[0.196s][info][gc,stats    ] Concurrent Mark Roots               448 us, parallelism: 1.46x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentStrongRoots() {
        String logLine = "[0.333s][info][gc,stats    ] Concurrent Strong Roots             111 us, parallelism: 0.68x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentThreadRoots() {
        String logLine = "[0.196s][info][gc,stats    ] Concurrent Thread Roots             455 us, parallelism: 1.05x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentUpdateTreadRoots() {
        String logLine = "[0.363s][info][gc,stats    ] Concurrent Update Thread Roots      378 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentWeakReferences() {
        String logLine = "[0.196s][info][gc,stats    ] Concurrent Weak References           95 us, parallelism: 0.56x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testConcurrentWeakRoots() {
        String logLine = "[0.196s][info][gc,stats    ] Concurrent Weak Roots               270 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCopyObjects() {
        String logLine = "  Copy Objects                   545833 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCsrTotal() {
        String logLine = "[0.196s][info][gc,stats    ]   CSR: <total>                      223 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCtrTotal() {
        String logLine = "[0.196s][info][gc,stats    ]   CTR: <total>                      476 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCuCldgRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: CLDG Roots                967 us, "
                + "workers (us): 479, 489,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCuCodeCacheCleaning() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: Code Cache Cleaning       149 us, "
                + "workers (us):  79,  70,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCuCodeCacheRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: Code Cache Roots          983 us, "
                + "workers (us): 488, 495,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCuResolvedTableRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: Resolved Table Roots        9 us, "
                + "workers (us):   0,   9";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCuStringTableRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: String Table Roots        358 us, "
                + "workers (us): 183, 175,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCuTotal() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: <total>                  2466 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCwrfTotal() {
        String logLine = "[0.196s][info][gc,stats    ]   CWRF: <total>                      53 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCwrfWeakReferences() {
        String logLine = "[0.196s][info][gc,stats    ]   CWRF: Weak References              53 us, workers (us):  53, "
                + "  0, ---, ---, ---, ---";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testCwrTotal() {
        String logLine = "[0.196s][info][gc,stats    ]     CWR: <total>                    422 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDcuTotal() {
        String logLine = "[2022-10-27T22:37:03.153-0400]         DCU: <total>               2985 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDcuUnlinkClds() {
        String logLine = "[2022-10-27T22:37:03.153-0400]         DCU: Unlink CLDs           1458 us, workers (us):"
                + " 445, 506, 506,   0, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDcuUnloadCodeCaches() {
        String logLine = "[2022-10-27T22:37:03.153-0400]         DCU: Unload Code Caches     1527 us, workers (us):"
                + " 547, 485, 496,   0, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDeallocateMetadata() {
        String logLine = "    Deallocate Metadata              24 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDefaultAccept() {
        String logLine = "     11 of 76651 ms (  0.0%): default Accept";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDefaultIO() {
        String logLine = "     74 of 76651 ms (  0.1%): default I/O-6";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDegenStwMark() {
        String logLine = "[2022-10-27T22:37:03.153-0400]   Degen STW Mark                   3914 us, parallelism: "
                + "3.94x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDegenUpdateRoots() {
        String logLine = "  Degen Update Roots               5869 us, parallelism: 17.30x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDsmCldgRoots() {
        String logLine = "[2022-10-27T22:37:03.153-0400]     DSM: CLDG Roots                  44 us, workers (us):"
                + "   1,  42,   1,   1, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDsmParallelMark() {
        String logLine = "[2022-10-27T22:37:03.153-0400]     DSM: Parallel Mark            13266 us, workers (us):"
                + " 3473, 3763, 2256, 3774, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDsmThreadRoots() {
        String logLine = "[2022-10-27T22:37:03.153-0400]     DSM: Thread Roots              2118 us, workers (us):"
                + " 87,  52, 1612,  67, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDsmTotal() {
        String logLine = "[2022-10-27T22:37:03.153-0400]     DSM: <total>                  15432 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDsmVmStrongRoots() {
        String logLine = "[2022-10-27T22:37:03.153-0400]     DSM: VM Strong Roots              4 us, workers (us):"
                + "   0,   0,   0,   3, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuCldgRoots() {
        String logLine = "    DU: CLDG Roots                 8481 us, workers (us): 468, 225, 454, 451, 451, 451,  76, "
                + "457, 451, 451, 461, 452, 453, 457, 453, 451, 461, 453, 453, 451,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuCodeCacheRoots() {
        String logLine = "    DU: Code Cache Roots          29622 us, workers (us): 1556, 1454, 1534, 1523, 1747, "
                + "1460,   0, 1594, 1437, 1478, 1472, 1840, 1637, 1518, 1529, 1572, 1465, 1522, 1611, 1672,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuFlatProfilerRoots() {
        String logLine = "    DU: Flat Profiler Roots         147 us, workers (us): ---, 147, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuJfrWeakRoots() {
        String logLine = "    DU: JFR Weak Roots                1 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---,   1, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuJniHandlesRoots() {
        String logLine = "    DU: JNI Handles Roots             4 us, workers (us): ---,   4, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuJniWeakRoots() {
        String logLine = "    DU: JNI Weak Roots               45 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---,  45, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuJvmtiRoots() {
        String logLine = "    DU: JVMTI Roots                 171 us, workers (us): 171, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuManagementRoots() {
        String logLine = "    DU: Management Roots             15 us, workers (us):  15, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuStringTableRoots() {
        String logLine = "    DU: String Table Roots        24649 us, workers (us): 1289, 1388, 1296, 1366, 1099, "
                + "1394,  70, 1256, 1344, 1383, 1424, 991, 1209, 1336, 1308, 1283, 1374, 1398, 1276, 1164,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuSynchronizerRoots() {
        String logLine = "    DU: Synchronizer Roots        26688 us, workers (us): 1440, 1441, 1553, 1549, 1526, "
                + "1508, 1476, 1454, 1422, 1391, 1350, 1334, 1268, 1216, 1185, 1170, 1137, 1104, 1098, 1068,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuSystemDictRoots() {
        String logLine = "    DU: System Dict Roots            15 us, workers (us): ---, ---,  15, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuThreadRoots() {
        String logLine = "    DU: Thread Roots              11698 us, workers (us): 404, 645, 403, 420, 421, 421, "
                + "3557, 414, 421, 421, 410, 419, 419, 414, 419, 420, 410, 419, 419, 420,";

        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDuUniverseRoots() {
        String logLine = "    DU: Universe Roots                1 us, workers (us):   1, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDwrTotal() {
        String logLine = "[2022-10-27T22:37:03.153-0400]         DWR: <total>                 88 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testDwrVmWeakRoots() {
        String logLine = "[2022-10-27T22:37:03.153-0400]         DWR: VM Weak Roots           88 us, workers (us):"
                + "  31,  30,  26,   1, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testEFlat() {
        String logLine = "    E: Flat Profiler Roots           22 us, workers (us):  22, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testEJfr() {
        String logLine = "    E: JFR Weak Roots                 1 us, workers (us): ---, ---, ---,   1, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
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
    void testETotal() {
        String logLine = "    E: <total>                    69151 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testETotalUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     E: <total>                      876 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testEvacuation() {
        String logLine = "[2022-10-27T22:37:03.153-0400]   Evacuation                        348 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testExceptionCaches() {
        String logLine = "[0.196s][info][gc,stats    ]     Exception Caches                  0 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFaJfrWeakRoots() {
        String logLine = "    FA: JFR Weak Roots                1 us, workers (us):   1, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFaThreadRoots() {
        String logLine = "    FA: Thread Roots              14090 us, workers (us): 6396, 5528, 2166,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFaTotal() {
        String logLine = "    FA: <total>                   93431 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFinishMark() {
        String logLine = "[0.196s][info][gc,stats    ]   Finish Mark                        43 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFinishQueues() {
        String logLine = "  Finish Queues                    3023 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFinishWork() {
        String logLine = "  Finish Work                   1007819 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFlatProfilerRoots() {
        String logLine = "    FS: Flat Profiler Roots         115 us, workers (us): ---, 115, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFsThreadRoots() {
        String logLine = "    FS: Thread Roots              14587 us, workers (us): 7245, 120, 7222,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFsTotal() {
        String logLine = "    FS: <total>                   33402 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFuJfrWeakRoots() {
        String logLine = "      FU: JFR Weak Roots              1 us, workers (us): ---, ---,   1,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFuThreadRoots() {
        String logLine = "      FU: Thread Roots            26649 us, workers (us): 9394, 8215, 9040,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testFuTotal() {
        String logLine = "      FU: <total>                 53311 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testHumongousObjects() {
        String logLine = "    Humongous Objects                86 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_STATS, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.SHENANDOAH_STATS + "not identified.");
    }

    @Test
    void testJdk11() throws IOException {
        File testFile = TestUtil.getFile("dataset195.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " collector not identified.");
    }

    @Test
    void testJdk11Time() throws IOException {
        File testFile = TestUtil.getFile("dataset197.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " collector not identified.");
    }

    @Test
    void testJniHandlesRoots() {
        String logLine = "    FS: JNI Handles Roots            22 us, workers (us): ---,  22, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testMain2DigitPercent() {
        String logLine = "[0.661s][info][gc,stats     ]      10 of    71 ms ( 14.0%): main";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testManageGcLabs() {
        String logLine = "[0.196s][info][gc,stats    ]   Manage GCLABs                       2 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testManageGcTlabs() {
        String logLine = "[2022-10-27T22:37:03.153-0400]   Manage GC/TLABs                     3 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testMark() {
        String logLine = "  Mark                          1559756 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof ShenandoahStatsEvent,
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " not parsed.");
    }

    @Test
    void testPauseDegenerateGcG() {
        String logLine = "Pause Degenerated GC (G)        1296188 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPauseDegenerateGcN() {
        String logLine = "Pause Degenerated GC (N)        1285099 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPauseFullGcG() {
        String logLine = "Pause Full GC (G)               4085272 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPauseFullGcN() {
        String logLine = "Pause Full GC (N)               4077354 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPreHeapDump() {
        String logLine = "  Pre Heap Dump                       0 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testProcess() {
        String logLine = "      Process                     24118 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testPurgeUnlinked() {
        String logLine = "[0.196s][info][gc,stats    ]   Purge Unlinked                     16 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testRebuildFreeSet() {
        String logLine = "  Rebuild Free Set                   36 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testRebuildFreeSetUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Rebuild Free Set                    6 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testRebuildRegionSets() {
        String logLine = "    Rebuild Region Sets             221 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testRegularObjects() {
        String logLine = "    Regular Objects              697921 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testRendezvous() {
        String logLine = "[0.196s][info][gc,stats    ]   Rendezvous                         19 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testResetCompleteBitmap() {
        String logLine = "    Reset Complete Bitmap          3901 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testRoots() {
        String logLine = "[0.196s][info][gc,stats    ]   Roots                             235 us, parallelism: 1.79x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSFlat() {
        String logLine = "    S: Flat Profiler Roots          129 us, workers (us): ---, ---, 129, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSJfr() {
        String logLine = "    S: JFR Weak Roots                 1 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---,   1, ---, ---, ---, ---, ---, ---, ---";
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
    void testSTotal() {
        String logLine = "    S: <total>                    69130 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSTotalUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     S: <total>                     1469 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testSystemDictionary() {
        String logLine = "[0.196s][info][gc,stats    ]     System Dictionary                 5 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testTrashCollectionSet() {
        String logLine = "  Trash Collection Set               61 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_STATS);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testUnifiedFinishWork() {
        String logLine = "[2020-10-26T14:51:41.413-0400]   Finish Work                     10950 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUnlinkStale() {
        String logLine = "[0.196s][info][gc,stats    ]   Unlink Stale                      305 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUnloadClasses() {
        String logLine = "[5.608s][info][gc,stats      ]     Unload Classes                   52 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUpdateReferences() {
        String logLine = "[2022-10-27T22:37:03.153-0400]   Update References                2520 us";
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
    void testUpdateRoots() {
        String logLine = "  Update Roots                     4231 us, parallelism: 2.26x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUpdateRoots4Spaces() {
        String logLine = "    Update Roots                  21973 us, parallelism: 2.46x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUrTotal() {
        String logLine = "    UR: <total>                    3127 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testUrTotalUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     UR: <total>                     139 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWeakClassLinks() {
        String logLine = "[0.196s][info][gc,stats    ]     Weak Class Links                  0 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWeakReferences() {
        String logLine = "  Weak References                  2251 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWeakRoots() {
        String logLine = "  Weak Roots                         36 us, parallelism: 0.94x";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWeakRootsUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     Weak Roots                      106 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWrJfr() {
        String logLine = "    WR: JFR Weak Roots                0 us, workers (us):   0, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWrJni() {
        String logLine = "    WR: JNI Weak Roots               33 us, workers (us):  33, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWrpTotal() {
        String logLine = "[2022-10-27T22:37:03.153-0400]         WRP: <total>                 93 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWrpWeakReferences() {
        String logLine = "[2022-10-27T22:37:03.153-0400]         WRP: Weak References         93 us, workers (us):"
                + "  81,  12,   0,   0, ---, ---,";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }

    @Test
    void testWrTotal() {
        String logLine = "    WR: <total>                      34 us";
        assertTrue(ShenandoahStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".");
    }
}
