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

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1YoungInitialMarkEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testInitialMark() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs] "
                + "[Times: user=0.18 sys=0.02, real=0.06 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1244357, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 867328, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 613376, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 993280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 56684, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 18, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 2, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 6, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 334, event.getParallelism());
    }

    public void testNotYoungPause() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        Assert.assertFalse("Log line recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
    }

    public void testLogLineMetadataGCThresholdTrigger() {
        String logLine = "1.471: [GC pause (Metadata GC Threshold) (young) (initial-mark) 992M->22M(110G), "
                + "0.0210012 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1471, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        Assert.assertEquals("Combined begin size not parsed correctly.", 992 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 22 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 110 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 21001, event.getDuration());
    }

    public void testLogLineGCLockerInitiatedGCTriggerBeforeInitialMark() {
        String logLine = "2.443: [GC pause (GCLocker Initiated GC) (young) (initial-mark) 1061M->52M(110G), "
                + "0.0280096 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2443, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 1061 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 52 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 110 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 28009, event.getDuration());
    }

    public void testLogLineToSpaceExhaustedTriggerAfterInitialMark() {
        String logLine = "60346.050: [GC pause (young) (initial-mark) (to-space exhausted), 1.0224350 secs]"
                + "[Eden: 14.2G(14.5G)->0.0B(1224.0M) Survivors: 40.0M->104.0M Heap: 22.9G(26.0G)->19.2G(26.0G)]"
                + " [Times: user=3.03 sys=0.02, real=1.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 60346050, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED));
        Assert.assertEquals("Combined begin size not parsed correctly.", 24012390, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 20132659, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1022435, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 303, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 2, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 102, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 300, event.getParallelism());
    }

    public void testLogLineNoTriggerNoInitialMark() {
        String logLine = "44620.073: [GC pause (young), 0.2752700 secs]"
                + "[Eden: 11.3G(11.3G)->0.0B(11.3G) Survivors: 192.0M->176.0M Heap: 23.0G(26.0G)->11.7G(26.0G)]"
                + " [Times: user=1.09 sys=0.00, real=0.27 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 44620073, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Combined begin size not parsed correctly.", 23 * 1024 * 1024,
                event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 12268339, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 275270, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 109, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 27, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 404, event.getParallelism());
    }

    public void testLogLinePreprocessedNoTrigger() {
        String logLine = "27474.176: [GC pause (young) (initial-mark), 0.4234530 secs]"
                + "[Eden: 5376.0M(7680.0M)->0.0B(6944.0M) Survivors: 536.0M->568.0M "
                + "Heap: 13.8G(26.0G)->8821.4M(26.0G)] [Times: user=1.66 sys=0.02, real=0.43 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27474176, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Combined begin size not parsed correctly.", 14470349, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 9033114, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 423453, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 166, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 2, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 43, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 391, event.getParallelism());
    }

    public void testLogLinePreprocessedTriggerMetadataGcThreshold() {
        String logLine = "87.830: [GC pause (Metadata GC Threshold) (young) (initial-mark), 0.2932700 secs]"
                + "[Eden: 716.0M(1850.0M)->0.0B(1522.0M) Survivors: 96.0M->244.0M "
                + "Heap: 2260.0M(5120.0M)->1831.0M(5120.0M)] [Times: user=0.56 sys=0.04, real=0.29 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 87830, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        Assert.assertEquals("Combined begin size not parsed correctly.", 2260 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 1831 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 5120 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 293270, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 56, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 29, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 207, event.getParallelism());
    }

    public void testLogLinePreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "6896.482: [GC pause (GCLocker Initiated GC) (young) (initial-mark), 0.0525160 secs]"
                + "[Eden: 16.0M(3072.0M)->0.0B(3070.0M) Survivors: 0.0B->2048.0K "
                + "Heap: 828.8M(5120.0M)->814.8M(5120.0M)] [Times: user=0.09 sys=0.00, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6896482, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 848691, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 834355, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 5120 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 52516, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 9, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 5, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 180, event.getParallelism());
    }

    public void testLogLinePreprocessedTriggerG1HumongousAllocation() {
        String logLine = "182.037: [GC pause (G1 Humongous Allocation) (young) (initial-mark), 0.0233585 secs]"
                + "[Eden: 424.0M(1352.0M)->0.0B(1360.0M) Survivors: 80.0M->72.0M Heap: 500.9M(28.0G)->72.0M(28.0G)] "
                + "[Times: user=0.14 sys=0.01, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 182037, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION));
        Assert.assertEquals("Combined begin size not parsed correctly.", 512922, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 72 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 28 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 23358, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 14, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 750, event.getParallelism());
    }

    public void testLogLinePreprocessedTriggerSystemGc() {
        String logLine = "2020-02-26T17:18:26.505+0000: 130.241: [GC pause (System.gc()) (young) (initial-mark), "
                + "0.1009346 secs][Eden: 220.0M(241.0M)->0.0B(277.0M) Survivors: 28.0M->34.0M "
                + "Heap: 924.5M(2362.0M)->713.5M(2362.0M)] [Times: user=0.19 sys=0.00, real=0.10 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 130241, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 946688, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 730624, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 2362 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 100934, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 19, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 10, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 190, event.getParallelism());
    }

    public void testLogLinePreprocessedNoTriggerWholeNumberSizes() {
        String logLine = "449391.255: [GC pause (young) (initial-mark), 0.02147900 secs]"
                + "[Eden: 1792M(1792M)->0B(2044M) Survivors: 256M->4096K Heap: 7582M(12288M)->5537M(12288M)] "
                + "[Times: user=0.13 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 449391255, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 7582 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 5537 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 12288 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 21479, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 13, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 650, event.getParallelism());
    }

    public void testLogLinePreprocessedDatestamp() {
        String logLine = "2016-02-09T06:12:45.414-0500: 27474.176: [GC pause (young) (initial-mark), 0.4234530 secs]"
                + "[Eden: 5376.0M(7680.0M)->0.0B(6944.0M) Survivors: 536.0M->568.0M "
                + "Heap: 13.8G(26.0G)->8821.4M(26.0G)] [Times: user=1.66 sys=0.02, real=0.43 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27474176, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 14470349, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 9033114, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 423453, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 166, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 43, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 391, event.getParallelism());
    }

    public void testLogLinePreprocessedTriggerG1HumongousAllocationNoSizeData() {
        String logLine = "2017-02-20T20:17:04.874-0500: 40442.077: [GC pause (G1 Humongous Allocation) (young) "
                + "(initial-mark), 0.0142482 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 40442077, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 0, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 0, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 0, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 14248, event.getDuration());
    }

    public void testLogLinePreprocessedNoDuration() {
        String logLine = "2017-06-23T10:50:04.403-0400: 9.915: [GC pause (Metadata GC Threshold) (young) "
                + "(initial-mark)[Eden: 304.0M(1552.0M)->0.0B(1520.0M) Survivors: 0.0B->32.0M Heap: "
                + "296.0M(30.5G)->23.2M(30.5G)] [Times: user=0.12 sys=0.01, real=0.03 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 9915, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 296 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 23757, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31981568, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 10000, event.getDuration());
    }

    public void testTriggerG1EvacuationPause() {
        String logLine = "7.190: [GC pause (G1 Evacuation Pause) (young) (initial-mark) 407M->100M(8192M), "
                + "0.0720459 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 7190, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 407 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 100 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 8192 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 72045, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testTriggerG1EvacuationPauseDashDash() {
        String logLine = "424753.803: [GC pause (G1 Evacuation Pause) (young) (initial-mark)-- 8184M->8184M(8192M), "
                + "0.1294400 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 424753803, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 8184 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 8184 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 8192 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 129440, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        Assert.assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        Assert.assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testAnalysisExplicitGc() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset179.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_INITIAL_MARK));
        Assert.assertFalse(Analysis.ERROR_EXPLICIT_GC_SERIAL_G1 + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_EXPLICIT_GC_SERIAL_G1));
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_G1_YOUNG_INITIAL_MARK + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_G1_YOUNG_INITIAL_MARK));

    }
}
