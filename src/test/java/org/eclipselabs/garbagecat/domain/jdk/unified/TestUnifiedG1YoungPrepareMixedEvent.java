/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
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
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedG1YoungPrepareMixedEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedG1YoungPrepareMixedEvent,
                JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not parsed.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (16627 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(3801), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(3801), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(kilobytes(24 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(13 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(31 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(461, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedExtRootScanning() {
        String logLine = "[1234ms][gc,start] GC(501) Pause Young (Prepare Mixed) (G1 Evacuation Pause) Ext Root "
                + "Scanning (ms): 27.3 Other: 1.1ms Humongous regions: 16->16 "
                + "Metaspace: 424009K(898652K)->424009K(898652K) 18221M->3823M(24576M) 153.101ms "
                + "User=3.35s Sys=0.01s Real=0.15s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (1234 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(424009), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(424009), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(898652), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(kilobytes(18221 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(3823 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(24576 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(27300, event.getExtRootScanningTime(), "External root scanning time not parsed correctly.");
        assertEquals(1100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(153101 + 1100, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(335, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(15, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(2240, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedJdk17() {
        String logLine = "[2022-08-05T05:08:51.394+0000][1908][gc,start    ] GC(1360) Pause Young (Prepare Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 147162K(149824K)->147162K(149824K) 24336M->9999M(32768M) 26,821ms "
                + "User=0,18s Sys=0,00s Real=0,03s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, event.getEventType(), "Event type incorrect.");
    }

    @Test
    void testPreprocessedMinimalJdk21() {
        String logLine = "[0.113s][info][gc       ] GC(6) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "4M->5M(30M) 1.419ms User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (113 - 1), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(kilobytes(4 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(5 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(1419, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedNoOther() {
        String logLine = "[0.112s][info][gc,start    ] GC(12) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Humongous regions: 0->0 Metaspace: 654K(832K)->654K(832K) 10M->10M(50M) 1.550ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, event.getEventType(), "Event type incorrect.");
    }

    @Test
    void testPreprocessedTriggerG1HumongousAllocation() {
        String logLine = "[2021-10-29T20:56:08.426+0000][info][gc,start      ] GC(734) Pause Young (Prepare Mixed) "
                + "(G1 Humongous Allocation) Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 66401K->66401K(151552K) 15678M->1575M(16384M) 24.193ms User=0.12s Sys=0.00s Real=0.03s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, event.getEventType(), "Event type incorrect.");
    }

    @Test
    void testPreprocessedTriggerG1HumongousAllocationTooSpaceExhausted() {
        String logLine = "[14.232s][info][gc,start] GC(17368) Pause Young (Prepare Mixed) (G1 Humongous Allocation) "
                + "To-space exhausted Other: 1.3ms Humongous regions: 312->70 Metaspace: 401628K->401628K(1421312K) "
                + "11481M->8717M(12000M) 1125.930ms User=6.31s Sys=0.13s Real=1.13s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, event.getEventType(), "Event type incorrect.");
    }

    @Test
    void testPreprocessedTriggerG1PreventiveCollection() {
        String logLine = "[2022-08-22T16:07:11.203+0000][248.117s][gc,start] GC(26) Pause Young (Prepare Mixed) "
                + "(G1 Preventive Collection) Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 52236K(52736K)->52236K(52736K) 269M->81M(300M) 14.821ms User=0.02s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, event.getEventType(), "Event type incorrect.");
    }

    @Test
    void testPreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[2021-10-14T00:22:54.796+0400][info][gc,start      ] GC(891) Pause Young (Prepare Mixed) "
                + "(GCLocker Initiated GC) Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 360792K->360792K(1380352K) 10311M->3024M(12288M) 33.928ms User=0.25s "
                + "Sys=0.04s Real=0.03s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, event.getEventType(), "Event type incorrect.");
    }

    @Test
    void testPreprocessing() throws IOException {
        File testFile = TestUtil.getFile("dataset168.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = (UnifiedG1YoungPrepareMixedEvent) jvmRun.getFirstBlockingEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (16627), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED),
                JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestampTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][gc,start] GC(4) Pause Young (Prepare Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) "
                + "24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(746241357862L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampTimeUptime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3.161s][gc,start] GC(4) Pause Young (Prepare Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) "
                + "24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampTimeUptimeMillis() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms][gc,start] GC(4) Pause Young (Prepare Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) "
                + "24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampUptime() {
        String logLine = "[3.161s][gc,start] GC(4) Pause Young (Prepare Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) "
                + "24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampUptimeMillis() {
        String logLine = "[3161ms][gc,start] GC(4) Pause Young (Prepare Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) "
                + "24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not indentified as unified.");
    }

    @Test
    void testUnpreprocessedTriggerG1EvacuationPause() {
        String logLine = "[217224.994s][info][gc] GC(137) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "13840M->7940M(16384M) 44.565ms";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms "
                + "User=0.00s Sys=0.00s Real=0.00s    ";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
    }
}
