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
class TestUnifiedFooterStatsEvent {

    @Test
    void test3SpacesCmParallelMark() {
        String logLine = "[2025-10-30T12:34:08.119-0400]   CM: Parallel Mark            =    0.601 s (a =    11553 us) "
                + "(n =    52) (lvls, us =     8750,    10547,    11133,    12305,    14829)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void test3SpacesCmTotal() {
        String logLine = "[2025-10-30T12:34:08.119-0400]   CM: <total>                  =    0.601 s (a =    11553 us) "
                + "(n =    52) (lvls, us =     8750,    10547,    11133,    12305,    14829)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void test3SpacesFlushSatb() {
        String logLine = "[2025-10-30T12:34:08.119-0400]   Flush SATB                   =    0.004 s (a =       81 us) "
                + "(n =    52) (lvls, us =       26,       60,       85,       90,      201)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void test3SpacesPropogateGcState() {
        String logLine = "[2025-10-30T12:34:08.119-0400]   Propagate GC State           =    0.000 s (a =        0 us) "
                + "(n =    52) (lvls, us =        0,        0,        0,        0,        1)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testAbbreviatedPercent() {
        String logLine = "[2025-10-30T12:34:08.119-0400]       0 abbreviated (0.00%)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testCausedByCondurrentGc() {
        String logLine = "[2025-10-30T12:34:08.119-0400]      52 caused by Concurrent GC (100.00%)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testConcurrentMarkRoots() {
        String logLine = "[2022-10-27T22:37:06.695-0400] Concurrent Mark Roots          =    0.041 s (a =      435 us) "
                + "(n =    94) (lvls, us =      141,      320,      398,      457,     1418)\" (id=153)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testConcurrentResetAfterCollect() {
        String logLine = "[2025-10-30T12:34:08.119-0400] Concurrent Reset After Collect =    0.005 s (a =      102 us) "
                + "(n =    52) (lvls, us =       41,       99,      102,      111,      131)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testConcurrentUpdateRefsPrepare() {
        String logLine = "[2025-10-30T12:34:08.119-0400] Concurrent Update Refs Prepare =    0.003 s (a =       59 us) "
                + "(n =    52) (lvls, us =       19,       33,       54,       94,      114)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testDegeneratedGcsHappenedAtEvacuation() {
        String logLine = "[2024-04-12T13:14:30.227-0400]         1 happened at Evacuation";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testFinishQueues() {
        String logLine = "[2020-10-26T14:52:27.770-0400]   Finish Queues                =    0.116 s (a =       83 us)"
                + " (n =  1398) (lvls, us =       11,       20,       51,       65,    10887)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testFromTheStw() {
        String logLine = "[66.558s][info][gc,stats      ]   from the STW pause times. Pacing affects the individual "
                + "threads, and so it would also be";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testFromToCountSumData() {
        String logLine = "[103.684s][info][gc,stats     ]       1 ms -       2 ms:         1998         999 ms";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testFromToCountSumData2DigitSum() {
        String logLine = "[96.867s]      16 ms -      32 ms:            6          48 ms";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testFromToCountSumData5DigitSum() {
        String logLine = "[103.684s][info][gc,stats     ]       8 ms -      16 ms:         4192       16768 ms";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGcsCompleted() {
        String logLine = "[2025-10-30T12:34:08.119-0400]    52 Completed GCs";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGcsDegenerated() {
        String logLine = "[2025-10-30T12:34:08.119-0400]     0 Degenerated GCs (0.00%)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGcsFull() {
        String logLine = "[2025-10-30T12:34:08.119-0400]     0 Full GCs (0.00%)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGcsSuccessfulConcurrent() {
        String logLine = "[2025-10-30T12:34:08.119-0400]    52 Successful Concurrent GCs (100.00%)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGcStatistics() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGrossLine1() {
        String logLine = "[16.200s][info][gc,stats    ]   \"(G)\" (gross) pauses include VM time: time to notify and "
                + "block threads, do the pre-";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGrossLine2() {
        String logLine = "[16.200s][info][gc,stats    ]         and post-safepoint housekeeping. Use "
                + "-Xlog:safepoint+stats to dissect.";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testHappenedAtOutsideOfCycle() {
        String logLine = "[74.874s]         1 happened at Outside of Cycle";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testHappenedAtUpdateReferences() {
        String logLine = "[5.106s][info][gc,stats    ]         1 happened at Update References";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testHigherDelay() {
        String logLine = "[66.558s][info][gc,stats      ]   Higher delay would prevent application outpacing the GC, "
                + "but it will hide the GC latencies";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        assertEquals(JdkUtil.EventType.UNIFIED_FOOTER_STATS,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_FOOTER_STATS + "not identified.");
    }

    @Test
    void testInvisible() {
        String logLine = "[66.558s][info][gc,stats      ]   invisible to the usual profiling tools, but would add up "
                + "to end-to-end application latency.";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testInvokedExplicitlyPercent() {
        String logLine = "[2025-10-30T12:34:08.119-0400]       0 invoked explicitly (0.00%)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testInvokedImplicitly() {
        String logLine = "[103.683s][info][gc,stats     ]       0 invoked implicitly";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk11Time() throws IOException {
        File testFile = TestUtil.getFile("dataset196.txt");
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
    void testJdk17Shenendoah() throws IOException {
        File testFile = TestUtil.getFile("dataset258.txt");
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
    void testObservedPacing() {
        String logLine = "[103.684s][info][gc,stats     ] observed pacing delays may be higher than the threshold "
                + "when paced thread spent more";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testObservedPacing3Spaces() {
        String logLine = "[66.558s][info][gc,stats      ]   observed pacing delays may be higher than the threshold "
                + "when paced thread spent more";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testOsTakesLonger() {
        String logLine = "[103.684s][info][gc,stats     ] OS takes longer to unblock the thread, or JVM experiences "
                + "an STW pause.";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testOsTakesLonger3Spaces() {
        String logLine = "[66.558s][info][gc,stats      ]   OS takes longer to unblock the thread, or JVM experiences "
                + "an STW pause.";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testPacingDelays() {
        String logLine = "[103.684s][info][gc,stats     ] Pacing delays are measured from entering the pacing code "
                + "till exiting it. Therefore,";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testPacingDelays3Spaces() {
        String logLine = "[66.558s][info][gc,stats      ]   Pacing delays are measured from entering the pacing code "
                + "till exiting it. Therefore,";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedFooterStatsEvent,
                JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + " not parsed.");
    }

    @Test
    void testPauseFinalEvacG() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (G)        =     0.42 s (a =      170 us) "
                + "(n =  2499) (lvls, us =       44,       82,      107,      146,     4911)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testPauseFinalEvacN() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (N)        =     0.05 s (a =       21 us) "
                + "(n =  2499) (lvls, us =       12,       15,       18,       23,      139)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testPrepare() {
        String logLine = "[103.683s][info][gc,stats     ]   Prepare                   =     0.00 s (a =        3 us) "
                + "(n =   652) (lvls, us =        1,        2,        3,        4,       20)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testRaiseMax() {
        String logLine = "[66.558s][info][gc,stats      ]   Raise max pacing delay with care.";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testRetireGcLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire GCLABs             =     0.00 s (a =        1 us) "
                + "(n =  2499) (lvls, us =        0,        1,        1,        1,       20)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testRetireTLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire TLABs              =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        1,        1,        2,       18))";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testSTotal() {
        String logLine = "[2020-10-26T14:52:27.770-0400]     S: <total>                 =    0.136 s (a =       98 us) "
                + "(n =  1398) (lvls, us =       62,       84,       92,      104,     1170)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testSuccessfulConcurrentGCs() {
        String logLine = "[103.683s][info][gc,stats     ]  3151 successful concurrent GCs";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testSyncPinned() {
        String logLine = "[103.683s][info][gc,stats     ]   Sync Pinned               =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        2,        2,        2,       19)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testSystemParallelCleanup() {
        String logLine = "[57.108s][info][gc,stats     ]     Parallel Cleanup        =     0.10 s (a =       72 us) "
                + "(n =  1378) (lvls, us =       28,       45,       69,       85,      348)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testSystemPurge() {
        String logLine = "[57.108s][info][gc,stats     ]   System Purge              =     0.10 s (a =       73 us) "
                + "(n =  1378) (lvls, us =       29,       45,       69,       85,      349)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testTimeInThePacing() {
        String logLine = "[103.684s][info][gc,stats     ] time in the pacing code. It usually happens when thread is "
                + "de-scheduled while paced,";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testToAvoidDegeneratedAndFullGcCyclesAbreviated() {
        String logLine = "[2025-10-30T12:34:08.119-0400] to avoid Degenerated and Full GC cycles. Abbreviated cycles "
                + "are those which found";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testToAvoidDegeneratedAndFullGcCyclesAbreviatedLine2() {
        String logLine = "[2025-10-30T12:34:08.119-0400] enough regions with no live objects to skip evacuation.";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testTrashCSet() {
        String logLine = "[103.683s][info][gc,stats     ]   Trash CSet                =     0.00 s (a =        1 us) "
                + "(n =  3151) (lvls, us =        0,        1,        1,        1,       21)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_FOOTER_STATS);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + " not indentified as unified.");
    }

    @Test
    void testUpdateRegionStates() {
        String logLine = "[2020-10-26T14:52:27.770-0400]   Update Region States         =    0.003 s (a =        2 us) "
                + "(n =  1398) (lvls, us =        1,        2,        2,        2,       18)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUpgradedFromDegeneratedGcPercent() {
        String logLine = "[2025-10-30T12:34:08.119-0400]       0 upgraded from Degenerated GC (0.00%)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }

    /**
     * Test logging.
     * 
     * @throws IOException
     */
    @Test
    void testUptimeMillis() throws IOException {
        File testFile = TestUtil.getFile("dataset165.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(0, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testUThreadRoots() {
        String logLine = "[103.683s][info][gc,stats     ]     U: Thread Roots         =     0.03 s (a =       14 us) "
                + "(n =  2498) (lvls, us =        7,       10,       12,       16,      178)";
        assertTrue(UnifiedFooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_FOOTER_STATS.toString() + ".");
    }
}
