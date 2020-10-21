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
public class TestFooterStatsEvent extends TestCase {

    public void testLineJdk8() {
        String logLine = "GC STATISTICS:";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnified() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        Assert.assertEquals(JdkUtil.LogEventType.FOOTER_STATS + "not identified.", JdkUtil.LogEventType.FOOTER_STATS,
                JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        Assert.assertTrue(JdkUtil.LogEventType.FOOTER_STATS.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof FooterStatsEvent);
    }

    public void testNotBlocking() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] GC STATISTICS:";
        Assert.assertFalse(JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.FOOTER_STATS));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.FOOTER_STATS);
        Assert.assertFalse(JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineJdk8UThreadRoots() {
        String logLine = "    U: Thread Roots         =     0.03 s (a =       14 us) "
                + "(n =  2498) (lvls, us =        7,       10,       12,       16,      178)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedUThreadRoots() {
        String logLine = "[103.683s][info][gc,stats     ]     U: Thread Roots         =     0.03 s (a =       14 us) "
                + "(n =  2498) (lvls, us =        7,       10,       12,       16,      178)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8RetireTLabs() {
        String logLine = "  Retire TLABs              =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        1,        1,        2,       18))";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedRetireTLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire TLABs              =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        1,        1,        2,       18))";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8SyncPinned() {
        String logLine = "  Sync Pinned               =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        2,        2,        2,       19)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedSyncPinned() {
        String logLine = "[103.683s][info][gc,stats     ]   Sync Pinned               =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        2,        2,        2,       19)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8TrashCSet() {
        String logLine = "  Trash CSet                =     0.00 s (a =        1 us) "
                + "(n =  3151) (lvls, us =        0,        1,        1,        1,       21)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedTrashCSet() {
        String logLine = "[103.683s][info][gc,stats     ]   Trash CSet                =     0.00 s (a =        1 us) "
                + "(n =  3151) (lvls, us =        0,        1,        1,        1,       21)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8PauseFinalEvacG() {
        String logLine = "Pause Final Evac (G)        =     0.42 s (a =      170 us) "
                + "(n =  2499) (lvls, us =       44,       82,      107,      146,     4911)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedPauseFinalEvacG() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (G)        =     0.42 s (a =      170 us) "
                + "(n =  2499) (lvls, us =       44,       82,      107,      146,     4911)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8PauseFinalEvacN() {
        String logLine = "Pause Final Evac (N)        =     0.05 s (a =       21 us) "
                + "(n =  2499) (lvls, us =       12,       15,       18,       23,      139)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedPauseFinalEvacN() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (N)        =     0.05 s (a =       21 us) "
                + "(n =  2499) (lvls, us =       12,       15,       18,       23,      139)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8RetireGcLabs() {
        String logLine = "  Retire GCLABs             =     0.00 s (a =        1 us) "
                + "(n =  2499) (lvls, us =        0,        1,        1,        1,       20)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedRetireGcLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire GCLABs             =     0.00 s (a =        1 us) "
                + "(n =  2499) (lvls, us =        0,        1,        1,        1,       20)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8Prepare() {
        String logLine = "  Prepare                   =     0.00 s (a =        3 us) "
                + "(n =   652) (lvls, us =        1,        2,        3,        4,       20)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedPrepare() {
        String logLine = "[103.683s][info][gc,stats     ]   Prepare                   =     0.00 s (a =        3 us) "
                + "(n =   652) (lvls, us =        1,        2,        3,        4,       20)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8SuccessfulConcurrentGCs() {
        String logLine = " 3151 successful concurrent GCs";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedSuccessfulConcurrentGCs() {
        String logLine = "[103.683s][info][gc,stats     ]  3151 successful concurrent GCs";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8InvokedImplicitly() {
        String logLine = "      0 invoked implicitly";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedInvokedImplicitly() {
        String logLine = "[103.683s][info][gc,stats     ]       0 invoked implicitly";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8FromToCountSumData() {
        String logLine = "      1 ms -       2 ms:         1998         999 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedFromToCountSumData() {
        String logLine = "[103.684s][info][gc,stats     ]       1 ms -       2 ms:         1998         999 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8FromToCountSumData5DigitSum() {
        String logLine = "      8 ms -      16 ms:         4192       16768 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedFromToCountSumData5DigitSum() {
        String logLine = "[103.684s][info][gc,stats     ]       8 ms -      16 ms:         4192       16768 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8FromToCountSumData2DigitSum() {
        String logLine = "     16 ms -      32 ms:          114         912 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedFromToCountSumData2DigitSum() {
        String logLine = "[96.867s]      16 ms -      32 ms:            6          48 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8PacingDelays() {
        String logLine = "Pacing delays are measured from entering the pacing code " + "till exiting it. Therefore,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedPacingDelays() {
        String logLine = "[103.684s][info][gc,stats     ] Pacing delays are measured from entering the pacing code "
                + "till exiting it. Therefore,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8ObservedPacing() {
        String logLine = "observed pacing delays may be higher than the threshold " + "when paced thread spent more";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedObservedPacing() {
        String logLine = "[103.684s][info][gc,stats     ] observed pacing delays may be higher than the threshold "
                + "when paced thread spent more";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8TimeInThePacing() {
        String logLine = "time in the pacing code. It usually happens when thread is " + "de-scheduled while paced,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedTimeInThePacing() {
        String logLine = "[103.684s][info][gc,stats     ] time in the pacing code. It usually happens when thread is "
                + "de-scheduled while paced,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8OsTakesLonger() {
        String logLine = "OS takes longer to unblock the thread, or JVM experiences " + "an STW pause.";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineUnifiedOsTakesLonger() {
        String logLine = "[103.684s][info][gc,stats     ] OS takes longer to unblock the thread, or JVM experiences "
                + "an STW pause.";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8FromToCount5Sum5() {
        String logLine = "      8 ms -      16 ms:        11145       44580 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8SFlatProfilerRoots() {
        String logLine = "    S: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7119) (lvls, us =        "
                + "0,        0,        0,        0,       20)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8Enqueue() {
        String logLine = "    Enqueue                 =     0.12 s (a =       81 us) (n =  1424) (lvls, us =       "
                + "14,       20,       75,       99,      929)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8EFlatProfilerRoots() {
        String logLine = "    E: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7118) (lvls, us =        "
                + "0,        0,        0,        0,       16)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineJdk8URFlatProfilerRoots() {
        String logLine = "    UR: FlatProfiler Roots  =     0.00 s (a =        1 us) (n =  1117) (lvls, us =        "
                + "0,        0,        0,        0,       13)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineSystemPurge() {
        String logLine = "[57.108s][info][gc,stats     ]   System Purge              =     0.10 s (a =       73 us) "
                + "(n =  1378) (lvls, us =       29,       45,       69,       85,      349)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineSystemParallelCleanup() {
        String logLine = "[57.108s][info][gc,stats     ]     Parallel Cleanup        =     0.10 s (a =       72 us) "
                + "(n =  1378) (lvls, us =       28,       45,       69,       85,      348)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    /**
     * Test logging.
     */
    public void testUnifiedUptimeMillis() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset165.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 0, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
    }
}
