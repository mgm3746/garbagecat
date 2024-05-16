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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.junit.jupiter.api.Test;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestG1YoungPauseEvent {

    @Test
    void testDatestamp() {
        String logLine = "2018-01-22T12:43:33.359-0700: [GC pause (G1 Evacuation Pause) (young) "
                + "511M->103M(10G), 0.1343977 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertEquals(569947413359L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLineKilobytes() {
        String logLine = "0.308: [GC pause (young) 8192K->2028K(59M), 0.0078140 secs] "
                + "[Times: user=0.01 sys=0.00, real=0.02 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertEquals((long) 308, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(8192), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(2028), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(60416), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(7814, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(2, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(50, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedDatestamp() {
        String logLine = "2016-12-21T14:28:11.672-0500: [GC pause (G1 Evacuation Pause) (young), "
                + "0.0124023 secs][Ext Root Scanning (ms): 1.2][Other: 544.6 ms][Eden: 75.0M(75.0M)->0.0B(66.0M) "
                + "Survivors: 0.0B->9216.0K Heap: 75.0M(1500.0M)->8749.6K(1500.0M)] [Times: user=0.03 sys=0.00, "
                + "real=0.02 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertEquals(535645691672L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedDatestampTimestamp() {
        String logLine = "2016-12-21T14:28:11.672-0500: 0.823: [GC pause (G1 Evacuation Pause) (young), "
                + "0.0124023 secs][Ext Root Scanning (ms): 1.2][Other: 544.6 ms][Eden: 75.0M(75.0M)->0.0B(66.0M) "
                + "Survivors: 0.0B->9216.0K Heap: 75.0M(1500.0M)->8749.6K(1500.0M)] [Times: user=0.03 sys=0.00, "
                + "real=0.02 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals((long) 823, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(75 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(8750), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(1500 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(1200, event.getExtRootScanningTime(), "Ext root scanning time not parsed correctly.");
        assertEquals(544600, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(557002, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(3, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(2, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(150, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedDoubleTrigger() {
        String logLine = "6049.175: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 3.1713585 secs][Ext "
                + "Root Scanning (ms): 1.2][Other: 544.6 ms][Eden: 27.1G(50.7G)->0.0B(50.7G) Survivors: 112.0M->0.0B "
                + "Heap: 27.9G(28.0G)->16.1G(28.0G)] [Times: user=17.73 sys=0.00, real=3.18 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.TO_SPACE_EXHAUSTED, "Trigger not parsed correctly.");
        assertEquals((long) 6049175, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(29255270), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(16882074), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(28 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(1200, event.getExtRootScanningTime(), "Ext root scanning time not parsed correctly.");
        assertEquals(544600, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(3715958, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1773, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(318, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(558, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedG1Details() {
        String logLine = "2.847: [GC pause (G1 Evacuation Pause) (young), 0.0414530 secs][Ext Root Scanning (ms): 1.2]"
                + "[Other: 0.9 ms][Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M "
                + "Heap: 136.9M(30.0G)->70.9M(30.0G)]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals((long) 2847, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(140186), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(72602), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(31457280), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(1200, event.getExtRootScanningTime(), "Ext root scanning time not parsed correctly.");
        assertEquals(900, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(42353, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedG1DetailsTriggerAfterYoungToSpaceExhausted() {
        String logLine = "27997.968: [GC pause (young) (to-space exhausted), 0.1208740 secs][Ext Root Scanning (ms): "
                + "1.2][Other: 0.9 ms][Eden: 1280.0M(1280.0M)->0.0B(1288.0M) Survivors: 48.0M->40.0M "
                + "Heap: 18.9G(26.0G)->17.8G(26.0G)] [Times: user=0.41 sys=0.02, real=0.12 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.TO_SPACE_EXHAUSTED, "Trigger not parsed correctly.");
        assertEquals((long) 27997968, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(19818086), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(18664653), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(26 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(121774, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(41, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(2, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(12, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(359, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedG1DetailsTriggerGcLockerInitiatedGc() {
        String logLine = "5.293: [GC pause (GCLocker Initiated GC) (young), 0.0176868 secs][Ext Root Scanning (ms): "
                + "1.2][Other: 0.9 ms][Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M "
                + "Heap: 415.0M(30.0G)->313.0M(30.0G)] [Times: user=0.01 sys=0.00, real=0.02 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.GCLOCKER_INITIATED_GC, "Trigger not parsed correctly.");
        assertEquals((long) 5293, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(415 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(313 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(30 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(1200, event.getExtRootScanningTime(), "Ext root scanning time not parsed correctly.");
        assertEquals(900, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(18586, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(2, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(50, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedG1Sizes() {
        String logLine = "0.807: [GC pause (young), 0.00290200 secs][ 29M->2589K(59M)]"
                + " [Times: user=0.01 sys=0.00, real=0.01 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.NONE, "Trigger not parsed correctly.");
        assertEquals((long) 807, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(29 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(2589), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(59 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(2902, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(1, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedNoDuration() {
        String logLine = "2017-04-05T09:09:00.416-0500: 201626.141: [GC pause (G1 Evacuation Pause) (young)"
                + "[Eden: 3808.0M(3808.0M)->0.0B(3760.0M) Survivors: 40.0M->64.0M "
                + "Heap: 7253.9M(8192.0M)->3472.3M(8192.0M)] [Times: user=0.22 sys=0.00, real=0.11 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals((long) 201626141, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(7427994), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(3555635), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(8192 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(110000, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(22, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(11, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(200, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedNoSizeDetails() {
        String logLine = "785,047: [GC pause (young), 0,73936800 secs][Ext Root Scanning (ms): 1.2][Other: 544.6 ms]"
                + "[Eden: 4096M(4096M)->0B(3528M) Survivors: 0B->568M Heap: 4096M(16384M)->567M(16384M)] [Times: "
                + "user=4,42 sys=0,38, real=0,74 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.NONE, "Trigger not parsed correctly.");
        assertEquals((long) 785047, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(4096 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(567 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(16384 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(1200, event.getExtRootScanningTime(), "Ext root scanning time not parsed correctly.");
        assertEquals(544600, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(1283968, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedNoSpaceAfterYoung() {
        String logLine = "2018-12-07T11:26:56.282-0500: 0.314: [GC pause (G1 Evacuation Pause) "
                + "(young)3589K->2581K(6144K), 0.0063282 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertEquals((long) 314, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(3589), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(2581), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(6144), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(6328, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedNoTrigger() {
        String logLine = "44620.073: [GC pause (young), 0.2752700 secs][Ext Root Scanning (ms): 1.2][Other: 544.6 ms]"
                + "[Eden: 11.3G(11.3G)->0.0B(11.3G) Survivors: 192.0M->176.0M Heap: 23.0G(26.0G)->11.7G(26.0G)]"
                + " [Times: user=1.09 sys=0.00, real=0.27 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.NONE, "Trigger not parsed correctly.");
        assertEquals((long) 44620073, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(23 * 1024 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(12268339), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(26 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(1200, event.getExtRootScanningTime(), "Ext root scanning time not parsed correctly.");
        assertEquals(544600, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(819870, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(109, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(27, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(404, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedToSpaceOverflow() {
        String logLine = "2017-05-25T12:24:06.040+0000: 206.156: [GC pause (young) (to-space overflow), "
                + "0.77121400 secs][Ext Root Scanning (ms): 1.2][Other: 0.9 ms][Eden: 1270M(1270M)->0B(723M) "
                + "Survivors: 124M->175M Heap: 2468M(3072M)->1695M(3072M)] [Times: user=1.51 sys=0.14, "
                + "real=0.77 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.TO_SPACE_OVERFLOW, "Trigger not parsed correctly.");
        assertEquals((long) 206156, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(2468 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(1695 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(3072 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(1200, event.getExtRootScanningTime(), "Ext root scanning time not parsed correctly.");
        assertEquals(900, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(772114, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(151, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(14, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(77, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(215, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedTriggerHumongousAllocatioin() {
        String logLine = "2020-02-16T23:24:09.668+0000: 880272.698: [GC pause (G1 Humongous Allocation) (young) "
                + "(to-space exhausted), 0.6167306 secs][Ext Root Scanning (ms): 1.2][Other: 0.9 ms]"
                + "[Eden: 545.0M(1691.0M)->0.0B(1691.0M) Survivors: 0.0B->1024.0K "
                + "Heap: 3038.2M(3072.0M)->2748.7M(3072.0M)] [Times: user=1.08 sys=0.01, real=0.62 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.TO_SPACE_EXHAUSTED, "Trigger not parsed correctly.");
        assertEquals((long) 880272698, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(2814669), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(3072 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(1200, event.getExtRootScanningTime(), "Ext root scanning time not parsed correctly.");
        assertEquals(900, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(617630, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(108, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(62, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(176, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedWithCommas() {
        String logLine = "2018-09-20T14:57:22.095+0300: 6,350: [GC pause (young), 0,1275790 secs][Ext Root Scanning "
                + "(ms): 1.2][Other: 0.9 ms][Eden: 306,0M(306,0M)->0,0B(266,0M) Survivors: 0,0B->40,0M "
                + "Heap: 306,0M(6144,0M)->57,7M(6144,0M)] [Times: user=0,25 sys=0,05, real=0,12 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertEquals((long) 6350, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(306 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(59085), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(6144 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(1200, event.getExtRootScanningTime(), "Ext root scanning time not parsed correctly.");
        assertEquals(900, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(128479, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(25, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(5, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(12, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(250, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testNotInitialMark() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]";
        assertFalse(G1YoungPauseEvent.match(logLine),
                "Log line recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    /**
     * Test preprocessing resulting in no space after (young).
     * 
     * @throws IOException
     * 
     */
    @Test
    void testPreprocessingNoSpaceAfterYoung() throws IOException {
        File testFile = TestUtil.getFile("dataset146.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_CONCURRENT),
                JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.G1_CONCURRENT.toString() + " collector not identified.");
    }

    /**
     * Test preprocessing TO_SPACE_OVERFLOW.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testPreprocessingTriggerToSpaceOverflow() throws IOException {
        File testFile = TestUtil.getFile("dataset128.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_G1_EVACUATION_FAILURE.getKey()),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
    }

    @Test
    void testTriggerG1EvacuationPauseDashDash() {
        String logLine = "424751.601: [GC pause (G1 Evacuation Pause) (young)-- 8172M->8168M(8192M), 0.4589730 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertEquals((long) 424751601, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(8172 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(8168 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(8192 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(458973, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTriggerG1EvacuationPauseDatestampTimestamp() {
        String logLine = "2018-01-22T12:43:33.359-0700: 17.629: [GC pause (G1 Evacuation Pause) (young) "
                + "511M->103M(10G), 0.1343977 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertEquals((long) 17629, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(511 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(103 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(10 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(134397, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTriggerG1HumongousAllocation() {
        String logLine = "16565.143: [GC pause (G1 Humongous Allocation) (young) 5829M->1966M(6144M), 0.1543333 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testTriggerGcLockerInitiatedGc() {
        String logLine = "9.466: [GC pause (GCLocker Initiated GC) (young) 523M->198M(8192M), 0.0500110 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        assertEquals((long) 9466, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.GCLOCKER_INITIATED_GC, "Trigger not parsed correctly.");
        assertEquals(kilobytes(523 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(198 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(8192 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(50011, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(TimesData.NO_DATA, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testYoungPause() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        assertTrue(G1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".");
    }
}
