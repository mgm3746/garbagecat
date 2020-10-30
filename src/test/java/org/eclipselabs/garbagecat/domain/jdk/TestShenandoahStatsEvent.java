/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahStatsEvent extends TestCase {

    public void testIdentityEventType() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_STATS + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_STATS, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahStatsEvent);
    }

    public void testNotBlocking() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        Assert.assertFalse(JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_STATS));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_STATS);
        Assert.assertFalse(JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineAllTimes() {
        String logLine = "All times are wall-clock times, except per-root-class counters, that are sum over";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineAllTimesUnified() {
        String logLine = "[0.484s][info][gc,stats     ] All times are wall-clock times, except per-root-class "
                + "counters, that are sum over";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineAllTimesWithLeadingSpaces() {
        String logLine = "  All times are wall-clock times, except per-root-class counters, that are sum over";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineAllWokers() {
        String logLine = "all workers. Dividing the <total> over the root stage time estimates parallelism.";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineAllWokersWithLeadingSpaces() {
        String logLine = "  all workers. Dividing the <total> over the root stage time estimates parallelism.";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testUpdateRegionStates() {
        String logLine = "  Update Region States              789 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testUpdateRegionStatesUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Update Region States                3 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineSTotal() {
        String logLine = "    S: <total>                    69130 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineSTotalUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     S: <total>                     1469 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testSJni() {
        String logLine = "    S: JNI Handles Roots              7 us, workers (us): ---,   7, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testSJniUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     S: JNI Handles Roots              1 us, workers (us):"
                + "   1, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineSJfr() {
        String logLine = "    S: JFR Weak Roots                 1 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---,   1, ---, ---, ---, ---, ---, ---, ---";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineSFlat() {
        String logLine = "    S: Flat Profiler Roots          129 us, workers (us): ---, ---, 129, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineWeakRoots() {
        String logLine = "  Weak Roots                         36 us, parallelism: 0.94x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineWeakRootsUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     Weak Roots                      106 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineWrTotal() {
        String logLine = "    WR: <total>                      34 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineWrJfr() {
        String logLine = "    WR: JFR Weak Roots                0 us, workers (us):   0, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineWrJni() {
        String logLine = "    WR: JNI Weak Roots               33 us, workers (us):  33, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineUpdateRegionStates() {
        String logLine = "  Update Region States              234 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineUpdateRegionStatesUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Update Region States                4 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineChooseCollectionSet() {
        String logLine = "  Choose Collection Set             440 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineChooseCollectionSetUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Choose Collection Set              41 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineRebuildFreeSet() {
        String logLine = "  Rebuild Free Set                   36 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineRebuildFreeSetUnified() {
        String logLine = "[0.484s][info][gc,stats     ]   Rebuild Free Set                    6 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineETotal() {
        String logLine = "    E: <total>                    69151 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineETotalUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     E: <total>                      876 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testEJni() {
        String logLine = "    E: JNI Handles Roots              3 us, workers (us):   3, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testEJniUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     E: JNI Handles Roots              1 us, workers (us):"
                + "   1, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineEJfr() {
        String logLine = "    E: JFR Weak Roots                 1 us, workers (us): ---, ---, ---,   1, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineEFlat() {
        String logLine = "    E: Flat Profiler Roots           22 us, workers (us):  22, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineUrTotal() {
        String logLine = "    UR: <total>                    3127 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineUrTotalUnified() {
        String logLine = "[0.484s][info][gc,stats     ]     UR: <total>                     139 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineTrashCollectionSet() {
        String logLine = "  Trash Collection Set               61 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineFinishWork() {
        String logLine = "  Finish Work                   1007819 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineUnifiedFinishWork() {
        String logLine = "[2020-10-26T14:51:41.413-0400]   Finish Work                     10950 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLinePauseDegenerateGcG() {
        String logLine = "Pause Degenerated GC (G)        1296188 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLinePauseDegenerateGcN() {
        String logLine = "Pause Degenerated GC (N)        1285099 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDegenUpdateRoots() {
        String logLine = "  Degen Update Roots               5869 us, parallelism: 17.30x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuThreadRoots() {
        String logLine = "    DU: Thread Roots              11698 us, workers (us): 404, 645, 403, 420, 421, 421, "
                + "3557, 414, 421, 421, 410, 419, 419, 414, 419, 420, 410, 419, 419, 420,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuCodeCacheRoots() {
        String logLine = "    DU: Code Cache Roots          29622 us, workers (us): 1556, 1454, 1534, 1523, 1747, "
                + "1460,   0, 1594, 1437, 1478, 1472, 1840, 1637, 1518, 1529, 1572, 1465, 1522, 1611, 1672,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuUniverseRoots() {
        String logLine = "    DU: Universe Roots                1 us, workers (us):   1, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuJniHandlesRoots() {
        String logLine = "    DU: JNI Handles Roots             4 us, workers (us): ---,   4, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuJfrWeakRoots() {
        String logLine = "    DU: JFR Weak Roots                1 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---,   1, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuJniWeakRoots() {
        String logLine = "    DU: JNI Weak Roots               45 us, workers (us): ---, ---, ---, ---, ---, ---, ---, "
                + "---,  45, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuStringTableRoots() {
        String logLine = "    DU: String Table Roots        24649 us, workers (us): 1289, 1388, 1296, 1366, 1099, "
                + "1394,  70, 1256, 1344, 1383, 1424, 991, 1209, 1336, 1308, 1283, 1374, 1398, 1276, 1164,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuSynchronizerRoots() {
        String logLine = "    DU: Synchronizer Roots        26688 us, workers (us): 1440, 1441, 1553, 1549, 1526, "
                + "1508, 1476, 1454, 1422, 1391, 1350, 1334, 1268, 1216, 1185, 1170, 1137, 1104, 1098, 1068,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuFlatProfilerRoots() {
        String logLine = "    DU: Flat Profiler Roots         147 us, workers (us): ---, 147, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuManagementRoots() {
        String logLine = "    DU: Management Roots             15 us, workers (us):  15, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuSystemDictRoots() {
        String logLine = "    DU: System Dict Roots            15 us, workers (us): ---, ---,  15, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuCldgRoots() {
        String logLine = "    DU: CLDG Roots                 8481 us, workers (us): 468, 225, 454, 451, 451, 451,  76, "
                + "457, 451, 451, 461, 452, 453, 457, 453, 451, 461, 453, 453, 451,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDuJvmtiRoots() {
        String logLine = "    DU: JVMTI Roots                 171 us, workers (us): 171, ---, ---, ---, ---, ---, ---, "
                + "---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---, ---,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineConcurrentCleanup() {
        String logLine = "[0.590s][info][gc,stats     ] Concurrent Cleanup                   38 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineMain2DigitPercent() {
        String logLine = "[0.661s][info][gc,stats     ]      10 of    71 ms ( 14.0%): main";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineUnloadClasses() {
        String logLine = "[5.608s][info][gc,stats      ]     Unload Classes                   52 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineCleanup() {
        String logLine = "[5.608s][info][gc,stats      ]     Cleanup                        1275 us, "
                + "parallelism: 1.93x";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineCuTotal() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: <total>                  2466 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineCuCodeCacheRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: Code Cache Roots          983 us, "
                + "workers (us): 488, 495,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineCuCodeCacheCleaning() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: Code Cache Cleaning       149 us, "
                + "workers (us):  79,  70,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineCuStringTableRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: String Table Roots        358 us, "
                + "workers (us): 183, 175,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineCuResolvedTableRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: Resolved Table Roots        9 us, "
                + "workers (us):   0,   9";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineCuCldgRoots() {
        String logLine = "[5.608s][info][gc,stats      ]       CU: CLDG Roots                967 us, "
                + "workers (us): 479, 489,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineCldg() {
        String logLine = "[5.608s][info][gc,stats      ]     CLDG                              0 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineDeallocateMetadata() {
        String logLine = "    Deallocate Metadata              24 us";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testLineC1CompilerThread2() {
        String logLine = "      8 of   105 ms (  7.6%): C1 CompilerThread2";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + ".",
                ShenandoahStatsEvent.match(logLine));
    }

    public void testJdk11() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset195.txt");
        GcManager gcManager1 = new GcManager();
        gcManager1.store(testFile, false);
        GcManager gcManager2 = new GcManager();
        File preprocessedFile = gcManager1.preprocess(testFile, null);
        gcManager2.store(preprocessedFile, false);
        JvmRun jvmRun1 = gcManager1.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun1.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun1.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.",
                jvmRun1.getEventTypes().contains(LogEventType.SHENANDOAH_STATS));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " collector not identified.",
                jvmRun1.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE));
        JvmRun jvmRun2 = gcManager2.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 0, jvmRun2.getEventTypes().size());
    }

    public void testJdk11Time() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset197.txt");
        GcManager gcManager1 = new GcManager();
        gcManager1.store(testFile, false);
        GcManager gcManager2 = new GcManager();
        File preprocessedFile = gcManager1.preprocess(testFile, null);
        gcManager2.store(preprocessedFile, false);
        JvmRun jvmRun1 = gcManager1.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun1.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun1.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.",
                jvmRun1.getEventTypes().contains(LogEventType.SHENANDOAH_STATS));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " collector not identified.",
                jvmRun1.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE));
        JvmRun jvmRun2 = gcManager2.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 0, jvmRun2.getEventTypes().size());
    }
}
