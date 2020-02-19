/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestFooterStatsEvent extends TestCase {

    public void testLine() {
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
        Assert.assertTrue(JdkUtil.LogEventType.FOOTER_STATS.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineUThreadRoots() {
        String logLine = "[103.683s][info][gc,stats     ]     U: Thread Roots         =     0.03 s (a =       14 us) "
                + "(n =  2498) (lvls, us =        7,       10,       12,       16,      178)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineRetireTLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire TLABs              =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        1,        1,        2,       18))";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineSyncPinned() {
        String logLine = "[103.683s][info][gc,stats     ]   Sync Pinned               =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        2,        2,        2,       19)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineTrashCSet() {
        String logLine = "[103.683s][info][gc,stats     ]   Trash CSet                =     0.00 s (a =        1 us) "
                + "(n =  3151) (lvls, us =        0,        1,        1,        1,       21)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLinePauseFinalEvacG() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (G)        =     0.42 s (a =      170 us) "
                + "(n =  2499) (lvls, us =       44,       82,      107,      146,     4911)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLinePauseFinalEvacN() {
        String logLine = "[103.683s][info][gc,stats     ] Pause Final Evac (N)        =     0.05 s (a =       21 us) "
                + "(n =  2499) (lvls, us =       12,       15,       18,       23,      139)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineRetireGcLabs() {
        String logLine = "[103.683s][info][gc,stats     ]   Retire GCLABs             =     0.00 s (a =        1 us) "
                + "(n =  2499) (lvls, us =        0,        1,        1,        1,       20)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLinePrepare() {
        String logLine = "[103.683s][info][gc,stats     ]   Prepare                   =     0.00 s (a =        3 us) "
                + "(n =   652) (lvls, us =        1,        2,        3,        4,       20)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineSuccessfulConcurrentGCs() {
        String logLine = "[103.683s][info][gc,stats     ]  3151 successful concurrent GCs";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineInvokedImplicitly() {
        String logLine = "[103.683s][info][gc,stats     ]       0 invoked implicitly";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineFromToCountSumData() {
        String logLine = "[103.684s][info][gc,stats     ]       1 ms -       2 ms:         1998         999 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineFromToCountSumData5DigitSum() {
        String logLine = "[103.684s][info][gc,stats     ]       8 ms -      16 ms:         4192       16768 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineFromToCountSumData2DigitSum() {
        String logLine = "[96.867s]      16 ms -      32 ms:            6          48 ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLinePacingDelays() {
        String logLine = "[103.684s][info][gc,stats     ] Pacing delays are measured from entering the pacing code "
                + "till exiting it. Therefore,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineObservedPacing() {
        String logLine = "[103.684s][info][gc,stats     ] observed pacing delays may be higher than the threshold "
                + "when paced thread spent more";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineTimeInThePacing() {
        String logLine = "[103.684s][info][gc,stats     ] time in the pacing code. It usually happens when thread is "
                + "de-scheduled while paced,";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    public void testLineOsTakesLonger() {
        String logLine = "[103.684s][info][gc,stats     ] OS takes longer to unblock the thread, or JVM experiences "
                + "an STW pause.";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".",
                FooterStatsEvent.match(logLine));
    }

    /**
     * Test logging.
     */
    public void testUptimeMillis() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset165.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 0, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
    }
}
