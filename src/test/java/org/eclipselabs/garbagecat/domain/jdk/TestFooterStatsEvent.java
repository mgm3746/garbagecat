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
class TestFooterStatsEvent {

    @Test
    void testAllTimesAreWallClockTimes() {
        FooterStatsEvent priorLogEvent = new FooterStatsEvent(null);
        String logLine = "  All times are wall-clock times, except per-root-class counters, that are sum over";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
        assertEquals(JdkUtil.LogEventType.FOOTER_STATS, JdkUtil.identifyEventType(logLine, priorLogEvent),
                JdkUtil.LogEventType.FOOTER_STATS + "not identified.");
    }

    @Test
    void testEFlatProfilerRoots() {
        String logLine = "    E: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7118) (lvls, us =        "
                + "0,        0,        0,        0,       16)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testEnqueue() {
        String logLine = "    Enqueue                 =     0.12 s (a =       81 us) (n =  1424) (lvls, us =       "
                + "14,       20,       75,       99,      929)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testFromToCount5Sum5() {
        String logLine = "      8 ms -      16 ms:        11145       44580 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testFromToCountSumData() {
        String logLine = "      1 ms -       2 ms:         1998         999 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testFromToCountSumData2DigitSum() {
        String logLine = "     16 ms -      32 ms:          114         912 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testFromToCountSumData5DigitSum() {
        String logLine = "      8 ms -      16 ms:         4192       16768 ms";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testGcStatistics() {
        String logLine = "GC STATISTICS:";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testHappenedAtUpdateRefs() {
        String logLine = "        1 happened at Update Refs";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "GC STATISTICS:";
        assertEquals(JdkUtil.LogEventType.FOOTER_STATS, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.FOOTER_STATS + "not identified.");
    }

    @Test
    void testInvokedImplicitly() {
        String logLine = "      0 invoked implicitly";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testLogLines() throws IOException {
        File testFile = TestUtil.getFile("dataset198.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.BLANK_LINE),
                JdkUtil.LogEventType.BLANK_LINE.toString() + " collector not identified.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "GC STATISTICS:";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testObservedPacing() {
        String logLine = "observed pacing delays may be higher than the threshold " + "when paced thread spent more";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testOsTakesLonger() {
        String logLine = "OS takes longer to unblock the thread, or JVM experiences " + "an STW pause.";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testPacingDelays() {
        String logLine = "Pacing delays are measured from entering the pacing code " + "till exiting it. Therefore,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "GC STATISTICS:";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof FooterStatsEvent,
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " not parsed.");
    }

    @Test
    void testPauseFinalEvacG() {
        String logLine = "Pause Final Evac (G)        =     0.42 s (a =      170 us) "
                + "(n =  2499) (lvls, us =       44,       82,      107,      146,     4911)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testPauseFinalEvacN() {
        String logLine = "Pause Final Evac (N)        =     0.05 s (a =       21 us) "
                + "(n =  2499) (lvls, us =       12,       15,       18,       23,      139)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testPrepare() {
        String logLine = "  Prepare                   =     0.00 s (a =        3 us) "
                + "(n =   652) (lvls, us =        1,        2,        3,        4,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testRetireGcLabs() {
        String logLine = "  Retire GCLABs             =     0.00 s (a =        1 us) "
                + "(n =  2499) (lvls, us =        0,        1,        1,        1,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testRetireTLabs() {
        String logLine = "  Retire TLABs              =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        1,        1,        2,       18))";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testSFlatProfilerRoots() {
        String logLine = "    S: FlatProfiler Roots   =     0.00 s (a =        0 us) (n =  7119) (lvls, us =        "
                + "0,        0,        0,        0,       20)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testSuccessfulConcurrentGCs() {
        String logLine = " 3151 successful concurrent GCs";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testSyncPinned() {
        String logLine = "  Sync Pinned               =     0.01 s (a =        2 us) "
                + "(n =  3151) (lvls, us =        1,        2,        2,        2,       19)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testTimeInThePacing() {
        String logLine = "time in the pacing code. It usually happens when thread is " + "de-scheduled while paced,";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testTrashCSet() {
        String logLine = "  Trash CSet                =     0.00 s (a =        1 us) "
                + "(n =  3151) (lvls, us =        0,        1,        1,        1,       21)";
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
    void testURFlatProfilerRoots() {
        String logLine = "    UR: FlatProfiler Roots  =     0.00 s (a =        1 us) (n =  1117) (lvls, us =        "
                + "0,        0,        0,        0,       13)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

    @Test
    void testUThreadRoots() {
        String logLine = "    U: Thread Roots         =     0.03 s (a =       14 us) "
                + "(n =  2498) (lvls, us =        7,       10,       12,       16,      178)";
        assertTrue(FooterStatsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.FOOTER_STATS.toString() + ".");
    }

}
