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
package org.eclipselabs.garbagecat.domain.jdk;

import java.io.File;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1YoungPauseEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testYoungPause() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
    }

    public void testNotInitialMark() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]";
        Assert.assertFalse("Log line recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
    }

    public void testLogLineKilobytes() {
        String logLine = "0.308: [GC pause (young) 8192K->2028K(59M), 0.0078140 secs] "
                + "[Times: user=0.01 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 308, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 8192, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2028, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 60416, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 7814, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 50, event.getParallelism());
    }

    public void testTriggerGcLockerInitiatedGc() {
        String logLine = "9.466: [GC pause (GCLocker Initiated GC) (young) 523M->198M(8192M), 0.0500110 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 9466, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 523 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 198 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 8192 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 50011, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testDatestameTriggerG1EvacuationPause() {
        String logLine = "2018-01-22T12:43:33.359-0700: 17.629: [GC pause (G1 Evacuation Pause) (young) "
                + "511M->103M(10G), 0.1343977 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 17629, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 511 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 103 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 10 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 134397, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testTriggerG1EvacuationPauseDashDash() {
        String logLine = "424751.601: [GC pause (G1 Evacuation Pause) (young)-- 8172M->8168M(8192M), 0.4589730 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 424751601, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 8172 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 8168 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 8192 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 458973, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testLogLinePreprocessedG1Details() {
        String logLine = "2.847: [GC pause (G1 Evacuation Pause) (young), 0.0414530 secs]"
                + "[Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 136.9M(30.0G)->70.9M(30.0G)]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Time stamp not parsed correctly.", 2847, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 140186, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 72602, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 41453, event.getDuration());
    }

    public void testLogLinePreprocessedG1Sizes() {
        String logLine = "0.807: [GC pause (young), 0.00290200 secs][ 29M->2589K(59M)]"
                + " [Times: user=0.01 sys=0.00, real=0.01 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Time stamp not parsed correctly.", 807, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 29 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2589, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 59 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 2902, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testLogLinePreprocessedG1DetailsTriggerGcLockerInitiatedGc() {
        String logLine = "5.293: [GC pause (GCLocker Initiated GC) (young), 0.0176868 secs]"
                + "[Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 415.0M(30.0G)->313.0M(30.0G)]"
                + " [Times: user=0.01 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Time stamp not parsed correctly.", 5293, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 415 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 313 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 30 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 17686, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 1, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 50, event.getParallelism());
    }

    public void testLogLinePreprocessedG1DetailsTriggerAfterYoungToSpaceExhausted() {
        String logLine = "27997.968: [GC pause (young) (to-space exhausted), 0.1208740 secs]"
                + "[Eden: 1280.0M(1280.0M)->0.0B(1288.0M) Survivors: 48.0M->40.0M Heap: 18.9G(26.0G)->17.8G(26.0G)]"
                + " [Times: user=0.41 sys=0.02, real=0.12 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED));
        Assert.assertEquals("Time stamp not parsed correctly.", 27997968, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 19818086, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 18664653, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 120874, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 41, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 12, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 342, event.getParallelism());
    }

    public void testLogLinePreprocessedNoTrigger() {
        String logLine = "44620.073: [GC pause (young), 0.2752700 secs]"
                + "[Eden: 11.3G(11.3G)->0.0B(11.3G) Survivors: 192.0M->176.0M Heap: 23.0G(26.0G)->11.7G(26.0G)]"
                + " [Times: user=1.09 sys=0.00, real=0.27 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Time stamp not parsed correctly.", 44620073, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 23 * 1024 * 1024,
                event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 12268339, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 275270, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 109, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 27, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 404, event.getParallelism());
    }

    public void testLogLinePreprocessedNoSizeDetails() {
        String logLine = "785,047: [GC pause (young), 0,73936800 secs][Eden: 4096M(4096M)->0B(3528M) "
                + "Survivors: 0B->568M Heap: 4096M(16384M)->567M(16384M)] [Times: user=4,42 sys=0,38, real=0,74 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Time stamp not parsed correctly.", 785047, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 4096 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 567 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 16384 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 739368, event.getDuration());
    }

    public void testLogLinePreprocessedDoubleTrigger() {
        String logLine = "6049.175: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 3.1713585 secs]"
                + "[Eden: 27.1G(50.7G)->0.0B(50.7G) Survivors: 112.0M->0.0B Heap: 27.9G(28.0G)->16.1G(28.0G)] "
                + "[Times: user=17.73 sys=0.00, real=3.18 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED));
        Assert.assertEquals("Time stamp not parsed correctly.", 6049175, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 29255270, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 16882074, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 28 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3171358, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 1773, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 318, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 558, event.getParallelism());
    }

    public void testLogLinePreprocessedDatestamp() {
        String logLine = "2016-12-21T14:28:11.672-0500: 0.823: [GC pause (G1 Evacuation Pause) (young), "
                + "0.0124023 secs][Eden: 75.0M(75.0M)->0.0B(66.0M) Survivors: 0.0B->9216.0K "
                + "Heap: 75.0M(1500.0M)->8749.6K(1500.0M)] [Times: user=0.03 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Time stamp not parsed correctly.", 823, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 75 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 8750, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 1500 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 12402, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 3, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 150, event.getParallelism());
    }

    public void testLogLinePreprocessedNoDuration() {
        String logLine = "2017-04-05T09:09:00.416-0500: 201626.141: [GC pause (G1 Evacuation Pause) (young)"
                + "[Eden: 3808.0M(3808.0M)->0.0B(3760.0M) Survivors: 40.0M->64.0M "
                + "Heap: 7253.9M(8192.0M)->3472.3M(8192.0M)] [Times: user=0.22 sys=0.00, real=0.11 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Time stamp not parsed correctly.", 201626141, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 7427994, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 3555635, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 8192 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 110000, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 22, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 11, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 200, event.getParallelism());
    }

    public void testLogLinePreprocessedToSpaceOverflow() {
        String logLine = "2017-05-25T12:24:06.040+0000: 206.156: [GC pause (young) (to-space overflow), "
                + "0.77121400 secs][Eden: 1270M(1270M)->0B(723M) Survivors: 124M->175M Heap: "
                + "2468M(3072M)->1695M(3072M)] [Times: user=1.51 sys=0.14, real=0.77 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_OVERFLOW));
        Assert.assertEquals("Time stamp not parsed correctly.", 206156, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 2468 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 1695 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 3072 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 771214, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 151, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 77, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 197, event.getParallelism());
    }

    public void testLogLinePreprocessedWithCommas() {
        String logLine = "2018-09-20T14:57:22.095+0300: 6,350: [GC pause (young), 0,1275790 secs]"
                + "[Eden: 306,0M(306,0M)->0,0B(266,0M) Survivors: 0,0B->40,0M Heap: 306,0M(6144,0M)->57,7M(6144,0M)] "
                + "[Times: user=0,25 sys=0,05, real=0,12 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6350, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 306 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 59085, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 6144 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 127579, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 25, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 12, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 209, event.getParallelism());
    }

    public void testLogLinePreprocessedNoSpaceAfterYoung() {
        String logLine = "2018-12-07T11:26:56.282-0500: 0.314: [GC pause (G1 Evacuation Pause) "
                + "(young)3589K->2581K(6144K), 0.0063282 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 314, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 3589, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2581, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 6144, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 6328, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    /**
     * Test preprocessing TRIGGER_TO_SPACE_OVERFLOW.
     * 
     */
    public void testPreprocessingTriggerToSpaceOverflow() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset128.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE));
        Assert.assertTrue(Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_G1_EVACUATION_FAILURE));
    }

    /**
     * Test preprocessing resulting in no space after (young).
     * 
     */
    public void testPreprocessingNoSpaceAfterYoung() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset146.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.G1_CONCURRENT));
        Assert.assertTrue(JdkUtil.LogEventType.G1_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE));
    }
}
