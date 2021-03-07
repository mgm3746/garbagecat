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
class TestFooterStatsEvent {

    @Test
    void testLineJdk8() {
        String logLine = "GC STATISTICS:";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnified() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
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
    void testParseLogLine() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof FooterStatsEvent,
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " not parsed.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.FOOTER_STATS);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testLineJdk8UThreadRoots() {
        String logLine = "    U: Thread Roots         =     0.03 s (a =       14 us) "
                + "(n =  2498) (lvls, us =        7,       10,       12,       16,      178)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedUThreadRoots() {
        String logLine = "[103.683s][info][gc,stats     ]     U: Thread Roots         =     0.03 s (a =       14 us) "
                + "(n =  2498) (lvls, us =        7,       10,       12,       16,      178)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8RetireTLabs() {
        String logLine = "  Retire TLABs              =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        1,        1,        2,       18))";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedRetireTLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire TLABs              =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        1,        1,        2,       18))";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8SyncPinned() {
        String logLine = "  Sync Pinned               =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        2,        2,        2,       19)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedSyncPinned() {
        String logLine = "[103.683s][info][gc,stats     ]   Sync Pinned               =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        2,        2,        2,       19)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8TrashCSet() {
        String logLine = "  Trash CSet                =     0.00 s (a =        1 us) "
                + "(n =  3151) (lvls, us =        0,        1,        1,        1,       21)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedTrashCSet() {
        String logLine = "[103.683s][info][gc,stats     ]   Trash CSet                =     0.00 s (a =        1 us) "
                + "(n =  3151) (lvls, us =        0,        1,        1,        1,       21)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8PauseFinalEvacG() {
        String logLine = "Pause Final Evac (G)        =     0.42 s (a =      170 us) "
                + "(n =  2499) (lvls, us =       44,       82,      107,      146,     4911)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedPauseFinalEvacG() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (G)        =     0.42 s (a =      170 us) "
                + "(n =  2499) (lvls, us =       44,       82,      107,      146,     4911)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8PauseFinalEvacN() {
        String logLine = "Pause Final Evac (N)        =     0.05 s (a =       21 us) "
                + "(n =  2499) (lvls, us =       12,       15,       18,       23,      139)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedPauseFinalEvacN() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (N)        =     0.05 s (a =       21 us) "
                + "(n =  2499) (lvls, us =       12,       15,       18,       23,      139)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8RetireGcLabs() {
        String logLine = "  Retire GCLABs             =     0.00 s (a =        1 us) "
                + "(n =  2499) (lvls, us =        0,        1,        1,        1,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedRetireGcLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire GCLABs             =     0.00 s (a =        1 us) "
                + "(n =  2499) (lvls, us =        0,        1,        1,        1,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8Prepare() {
        String logLine = "  Prepare                   =     0.00 s (a =        3 us) "
                + "(n =   652) (lvls, us =        1,        2,        3,        4,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedPrepare() {
        String logLine = "[103.683s][info][gc,stats     ]   Prepare                   =     0.00 s (a =        3 us) "
                + "(n =   652) (lvls, us =        1,        2,        3,        4,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8SuccessfulConcurrentGCs() {
        String logLine = " 3151 successful concurrent GCs";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedSuccessfulConcurrentGCs() {
        String logLine = "[103.683s][info][gc,stats     ]  3151 successful concurrent GCs";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8InvokedImplicitly() {
        String logLine = "      0 invoked implicitly";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedInvokedImplicitly() {
        String logLine = "[103.683s][info][gc,stats     ]       0 invoked implicitly";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8FromToCountSumData() {
        String logLine = "      1 ms -       2 ms:         1998         999 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedFromToCountSumData() {
        String logLine = "[103.684s][info][gc,stats     ]       1 ms -       2 ms:         1998         999 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8FromToCountSumData5DigitSum() {
        String logLine = "      8 ms -      16 ms:         4192       16768 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedFromToCountSumData5DigitSum() {
        String logLine = "[103.684s][info][gc,stats     ]       8 ms -      16 ms:         4192       16768 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8FromToCountSumData2DigitSum() {
        String logLine = "     16 ms -      32 ms:          114         912 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedFromToCountSumData2DigitSum() {
        String logLine = "[96.867s]      16 ms -      32 ms:            6          48 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8PacingDelays() {
        String logLine = "Pacing delays are measured from entering the pacing code " + "till exiting it. Therefore,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedPacingDelays() {
        String logLine = "[103.684s][info][gc,stats     ] Pacing delays are measured from entering the pacing code "
                + "till exiting it. Therefore,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedPacingDelays3Spaces() {
        String logLine = "[66.558s][info][gc,stats      ]   Pacing delays are measured from entering the pacing code "
                + "till exiting it. Therefore,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8ObservedPacing() {
        String logLine = "observed pacing delays may be higher than the threshold " + "when paced thread spent more";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedObservedPacing() {
        String logLine = "[103.684s][info][gc,stats     ] observed pacing delays may be higher than the threshold "
                + "when paced thread spent more";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedObservedPacing3Spaces() {
        String logLine = "[66.558s][info][gc,stats      ]   observed pacing delays may be higher than the threshold "
                + "when paced thread spent more";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8TimeInThePacing() {
        String logLine = "time in the pacing code. It usually happens when thread is " + "de-scheduled while paced,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedTimeInThePacing() {
        String logLine = "[103.684s][info][gc,stats     ] time in the pacing code. It usually happens when thread is "
                + "de-scheduled while paced,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8OsTakesLonger() {
        String logLine = "OS takes longer to unblock the thread, or JVM experiences " + "an STW pause.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedOsTakesLonger() {
        String logLine = "[103.684s][info][gc,stats     ] OS takes longer to unblock the thread, or JVM experiences "
                + "an STW pause.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineUnifiedOsTakesLonger3Spaces() {
        String logLine = "[66.558s][info][gc,stats      ]   OS takes longer to unblock the thread, or JVM experiences "
                + "an STW pause.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8FromToCount5Sum5() {
        String logLine = "      8 ms -      16 ms:        11145       44580 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8SFlatProfilerRoots() {
        String logLine = "    S: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7119) (lvls, us =        "
                + "0,        0,        0,        0,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8Enqueue() {
        String logLine = "    Enqueue                 =     0.12 s (a =       81 us) (n =  1424) (lvls, us =       "
                + "14,       20,       75,       99,      929)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8EFlatProfilerRoots() {
        String logLine = "    E: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7118) (lvls, us =        "
                + "0,        0,        0,        0,       16)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineJdk8URFlatProfilerRoots() {
        String logLine = "    UR: FlatProfiler Roots  =     0.00 s (a =        1 us) (n =  1117) (lvls, us =        "
                + "0,        0,        0,        0,       13)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineSystemPurge() {
        String logLine = "[57.108s][info][gc,stats     ]   System Purge              =     0.10 s (a =       73 us) "
                + "(n =  1378) (lvls, us =       29,       45,       69,       85,      349)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineSystemParallelCleanup() {
        String logLine = "[57.108s][info][gc,stats     ]     Parallel Cleanup        =     0.10 s (a =       72 us) "
                + "(n =  1378) (lvls, us =       28,       45,       69,       85,      348)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineHigherDelay() {
        String logLine = "[66.558s][info][gc,stats      ]   Higher delay would prevent application outpacing the GC, "
                + "but it will hide the GC latencies";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineFromTheStw() {
        String logLine = "[66.558s][info][gc,stats      ]   from the STW pause times. Pacing affects the individual "
                + "threads, and so it would also be";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineInvisible() {
        String logLine = "[66.558s][info][gc,stats      ]   invisible to the usual profiling tools, but would add up "
                + "to end-to-end application latency.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineRaiseMax() {
        String logLine = "[66.558s][info][gc,stats      ]   Raise max pacing delay with care.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLineHappenedAtOutsideOfCycle() {
        String logLine = "[74.874s]         1 happened at Outside of Cycle";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    /**
     * Test logging.
     */
    @Test
    void testUnifiedUptimeMillis() {
        File testFile = TestUtil.getFile("dataset165.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(0, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
    }

    @Test
    void testJdk11Time() {
        File testFile = TestUtil.getFile("dataset196.txt");
        GcManager gcManager1 = new GcManager();
        gcManager1.store(testFile, false);
        GcManager gcManager2 = new GcManager();
        File preprocessedFile = gcManager1.preprocess(testFile, null);
        gcManager2.store(preprocessedFile, false);
        JvmRun jvmRun1 = gcManager1.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun1.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun1.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " collector not identified.");
        // Some logging patterns are exactly the same as SHENANDOAH_STATS. No easy way to distinguish them.
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.");
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.UNIFIED_BLANK_LINE),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " collector not identified.");
        JvmRun jvmRun2 = gcManager2.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(0, jvmRun2.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testJdk8() {
        File testFile = TestUtil.getFile("dataset198.txt");
        GcManager gcManager1 = new GcManager();
        gcManager1.store(testFile, false);
        GcManager gcManager2 = new GcManager();
        File preprocessedFile = gcManager1.preprocess(testFile, null);
        gcManager2.store(preprocessedFile, false);
        JvmRun jvmRun1 = gcManager1.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun1.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun1.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " collector not identified.");
        // Some logging patterns are exactly the same as SHENANDOAH_STATS. No easy way to distinguish them.
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " collector not identified.");
        assertTrue(jvmRun1.getEventTypes().contains(LogEventType.BLANK_LINE),
                JdkUtil.LogEventType.BLANK_LINE.toString() + " collector not identified.");
        JvmRun jvmRun2 = gcManager2.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(0, jvmRun2.getEventTypes().size(), "Event type count not correct.");
    }
}
