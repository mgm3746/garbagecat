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
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestFooterStatsEvent {

    @Test
    void testFromTheStw() {
        String logLine = "[66.558s][info][gc,stats      ]   from the STW pause times. Pacing affects the individual "
                + "threads, and so it would also be";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGrossLine1() {
        String logLine = "[16.200s][info][gc,stats    ]   \"(G)\" (gross) pauses include VM time: time to notify and "
                + "block threads, do the pre-";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGrossLine2() {
        String logLine = "[16.200s][info][gc,stats    ]         and post-safepoint housekeeping. Use "
                + "-Xlog:safepoint+stats to dissect.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testHappenedAtOutsideOfCycle() {
        String logLine = "[74.874s]         1 happened at Outside of Cycle";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testHigherDelay() {
        String logLine = "[66.558s][info][gc,stats      ]   Higher delay would prevent application outpacing the GC, "
                + "but it will hide the GC latencies";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        assertEquals(JdkUtil.LogEventType.FOOTER_STATS, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.FOOTER_STATS + "not identified.");
    }

    @Test
    void testInvisible() {
        String logLine = "[66.558s][info][gc,stats      ]   invisible to the usual profiling tools, but would add up "
                + "to end-to-end application latency.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk11Time() throws IOException {
        File testFile = TestUtil.getFile("dataset196.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " collector not identified.");
        // Some logging patterns are exactly the same as SHENANDOAH_STATS. No easy way to distinguish them.
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " collector not identified.");
    }

    @Test
    void testJdk8() throws IOException {
        File testFile = TestUtil.getFile("dataset198.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " collector not identified.");
        // Some logging patterns are exactly the same as SHENANDOAH_STATS. No easy way to distinguish them.
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.BLANK_LINE),
                JdkUtil.LogEventType.BLANK_LINE.toString() + " collector not identified.");
    }

    @Test
    void testJdk8EFlatProfilerRoots() {
        String logLine = "    E: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7118) (lvls, us =        "
                + "0,        0,        0,        0,       16)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8Enqueue() {
        String logLine = "    Enqueue                 =     0.12 s (a =       81 us) (n =  1424) (lvls, us =       "
                + "14,       20,       75,       99,      929)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8FromToCount5Sum5() {
        String logLine = "      8 ms -      16 ms:        11145       44580 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8FromToCountSumData() {
        String logLine = "      1 ms -       2 ms:         1998         999 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8FromToCountSumData2DigitSum() {
        String logLine = "     16 ms -      32 ms:          114         912 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8FromToCountSumData5DigitSum() {
        String logLine = "      8 ms -      16 ms:         4192       16768 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8GcStatistics() {
        String logLine = "GC STATISTICS:";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8InvokedImplicitly() {
        String logLine = "      0 invoked implicitly";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8ObservedPacing() {
        String logLine = "observed pacing delays may be higher than the threshold " + "when paced thread spent more";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8OsTakesLonger() {
        String logLine = "OS takes longer to unblock the thread, or JVM experiences " + "an STW pause.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8PacingDelays() {
        String logLine = "Pacing delays are measured from entering the pacing code " + "till exiting it. Therefore,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8PauseFinalEvacG() {
        String logLine = "Pause Final Evac (G)        =     0.42 s (a =      170 us) "
                + "(n =  2499) (lvls, us =       44,       82,      107,      146,     4911)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8PauseFinalEvacN() {
        String logLine = "Pause Final Evac (N)        =     0.05 s (a =       21 us) "
                + "(n =  2499) (lvls, us =       12,       15,       18,       23,      139)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8Prepare() {
        String logLine = "  Prepare                   =     0.00 s (a =        3 us) "
                + "(n =   652) (lvls, us =        1,        2,        3,        4,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8RetireGcLabs() {
        String logLine = "  Retire GCLABs             =     0.00 s (a =        1 us) "
                + "(n =  2499) (lvls, us =        0,        1,        1,        1,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8RetireTLabs() {
        String logLine = "  Retire TLABs              =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        1,        1,        2,       18))";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8SFlatProfilerRoots() {
        String logLine = "    S: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7119) (lvls, us =        "
                + "0,        0,        0,        0,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8SuccessfulConcurrentGCs() {
        String logLine = " 3151 successful concurrent GCs";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8SyncPinned() {
        String logLine = "  Sync Pinned               =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        2,        2,        2,       19)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8TimeInThePacing() {
        String logLine = "time in the pacing code. It usually happens when thread is " + "de-scheduled while paced,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8TrashCSet() {
        String logLine = "  Trash CSet                =     0.00 s (a =        1 us) "
                + "(n =  3151) (lvls, us =        0,        1,        1,        1,       21)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8URFlatProfilerRoots() {
        String logLine = "    UR: FlatProfiler Roots  =     0.00 s (a =        1 us) (n =  1117) (lvls, us =        "
                + "0,        0,        0,        0,       13)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testJdk8UThreadRoots() {
        String logLine = "    U: Thread Roots         =     0.03 s (a =       14 us) "
                + "(n =  2498) (lvls, us =        7,       10,       12,       16,      178)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof FooterStatsEvent,
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " not parsed.");
    }

    @Test
    void testRaiseMax() {
        String logLine = "[66.558s][info][gc,stats      ]   Raise max pacing delay with care.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testSystemParallelCleanup() {
        String logLine = "[57.108s][info][gc,stats     ]     Parallel Cleanup        =     0.10 s (a =       72 us) "
                + "(n =  1378) (lvls, us =       28,       45,       69,       85,      348)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testSystemPurge() {
        String logLine = "[57.108s][info][gc,stats     ]   System Purge              =     0.10 s (a =       73 us) "
                + "(n =  1378) (lvls, us =       29,       45,       69,       85,      349)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.FOOTER_STATS);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testUnifiedFromToCountSumData() {
        String logLine = "[103.684s][info][gc,stats     ]       1 ms -       2 ms:         1998         999 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedFromToCountSumData2DigitSum() {
        String logLine = "[96.867s]      16 ms -      32 ms:            6          48 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedFromToCountSumData5DigitSum() {
        String logLine = "[103.684s][info][gc,stats     ]       8 ms -      16 ms:         4192       16768 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedGcStatistics() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedInvokedImplicitly() {
        String logLine = "[103.683s][info][gc,stats     ]       0 invoked implicitly";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedObservedPacing() {
        String logLine = "[103.684s][info][gc,stats     ] observed pacing delays may be higher than the threshold "
                + "when paced thread spent more";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedObservedPacing3Spaces() {
        String logLine = "[66.558s][info][gc,stats      ]   observed pacing delays may be higher than the threshold "
                + "when paced thread spent more";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedOsTakesLonger() {
        String logLine = "[103.684s][info][gc,stats     ] OS takes longer to unblock the thread, or JVM experiences "
                + "an STW pause.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedOsTakesLonger3Spaces() {
        String logLine = "[66.558s][info][gc,stats      ]   OS takes longer to unblock the thread, or JVM experiences "
                + "an STW pause.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedPacingDelays() {
        String logLine = "[103.684s][info][gc,stats     ] Pacing delays are measured from entering the pacing code "
                + "till exiting it. Therefore,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedPacingDelays3Spaces() {
        String logLine = "[66.558s][info][gc,stats      ]   Pacing delays are measured from entering the pacing code "
                + "till exiting it. Therefore,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedPauseFinalEvacG() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (G)        =     0.42 s (a =      170 us) "
                + "(n =  2499) (lvls, us =       44,       82,      107,      146,     4911)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedPauseFinalEvacN() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (N)        =     0.05 s (a =       21 us) "
                + "(n =  2499) (lvls, us =       12,       15,       18,       23,      139)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedPrepare() {
        String logLine = "[103.683s][info][gc,stats     ]   Prepare                   =     0.00 s (a =        3 us) "
                + "(n =   652) (lvls, us =        1,        2,        3,        4,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedRetireGcLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire GCLABs             =     0.00 s (a =        1 us) "
                + "(n =  2499) (lvls, us =        0,        1,        1,        1,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedRetireTLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire TLABs              =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        1,        1,        2,       18))";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedSuccessfulConcurrentGCs() {
        String logLine = "[103.683s][info][gc,stats     ]  3151 successful concurrent GCs";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedSyncPinned() {
        String logLine = "[103.683s][info][gc,stats     ]   Sync Pinned               =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        2,        2,        2,       19)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedTimeInThePacing() {
        String logLine = "[103.684s][info][gc,stats     ] time in the pacing code. It usually happens when thread is "
                + "de-scheduled while paced,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUnifiedTrashCSet() {
        String logLine = "[103.683s][info][gc,stats     ]   Trash CSet                =     0.00 s (a =        1 us) "
                + "(n =  3151) (lvls, us =        0,        1,        1,        1,       21)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    /**
     * Test logging.
     * 
     * @throws IOException
     */
    @Test
    void testUnifiedUptimeMillis() throws IOException {
        File testFile = TestUtil.getFile("dataset165.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(0, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
    }

    @Test
    void testUnifiedUThreadRoots() {
        String logLine = "[103.683s][info][gc,stats     ]     U: Thread Roots         =     0.03 s (a =       14 us) "
                + "(n =  2498) (lvls, us =        7,       10,       12,       16,      178)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }
}
