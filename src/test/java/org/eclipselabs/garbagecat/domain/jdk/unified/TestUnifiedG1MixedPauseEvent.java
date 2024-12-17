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
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
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
class TestUnifiedG1MixedPauseEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE + "not identified.");
    }

    @Test
    void testLogLinePreprocessed() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 16629, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(3801), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(3801), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(kilobytes(15 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(12 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(31 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(1302, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedExtRootScanning() {
        String logLine = "[1234ms][gc,start] GC(502) Pause Young (Mixed) (G1 Evacuation Pause) Ext Root Scanning (ms):"
                + " 28.1 Other: 1.1ms Humongous regions: 16->16 Metaspace: 424441K(898652K)->424441K(898652K) "
                + "4711M->2664M(24576M) 117.183ms User=2.56s Sys=0.00s Real=0.11s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 1234, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(424441), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(424441), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(898652), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(kilobytes(4711 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(2664 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(24576 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(1100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(117183 + 1100, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(256, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(11, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(2328, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedJdk17() {
        String logLine = "[2022-08-05T05:08:55.096+0000][1908][gc,start    ] GC(1362) Pause Young (Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 147162K(149824K)->147162K(149824K) 8331M->4808M(32768M) 25,917ms User=0,17s "
                + "Sys=0,00s Real=0,03s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testLogLinePreprocessedNoOther() {
        String logLine = "[0.147s][info][gc,start    ] GC(13) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Humongous regions: 0->0 Metaspace: 1240K(1344K)->1240K(1344K) 11M->10M(50M) 1.753ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testLogLinePreprocessedTriggerG1HumongousAllocation() {
        String logLine = "[2022-01-26T10:02:45.142+0530][297108898ms] GC(8626) Pause Young (Mixed) "
                + "(G1 Humongous Allocation) Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 443023K->443023K(1468416K) 8176M->717M(8192M) 9.643ms User=0.03s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testLogLinePreprocessedTriggerG1PreventiveCollection() {
        String logLine = "[4.529s][info][gc,start       ] GC(24) Pause Young (Mixed) (G1 Preventive Collection) "
                + "Other: 0.1ms Humongous regions: 0->0 Metaspace: 43948K(44672K)->43948K(44672K) 38M->30M(72M) "
                + "3.310ms User=0.04s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testLogLinePreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[2021-10-14T17:52:08.374+0400][info][gc,start      ] GC(2131) Pause Young (Mixed) (GCLocker "
                + "Initiated GC) Other: 0.1ms Humongous regions: 13->13 Metaspace: 365476K->365476K(1384448K) "
                + "3827M->3109M(12288M) 23.481ms User=0.20s Sys=0.02s Real=0.02s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s     ";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedG1MixedPauseEvent,
                JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not parsed.");
    }

    @Test
    void testPreprocessing() throws IOException {
        File testFile = TestUtil.getFile("dataset169.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = (UnifiedG1MixedPauseEvent) jvmRun.getFirstBlockingEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (16629), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE),
                JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestampTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][gc,start] GC(4) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(746241357862L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampTimeUptime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3.161s][gc,start] GC(4) Pause Young (Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) "
                + "15M->12M(31M) 1.202ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampTimeUptimeMillis() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms][gc,start] GC(4) Pause Young (Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) "
                + "15M->12M(31M) 1.202ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampUptime() {
        String logLine = "[3.161s][gc,start] GC(4) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampUptimeMillis() {
        String logLine = "[3161ms][gc,start] GC(4) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_G1_MIXED_PAUSE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not indentified as unified.");
    }

    @Test
    void testUnpreprocessedTriggerG1EvacuationPause() {
        String logLine = "[89961.720s][info][gc] GC(1343) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "16218M->16382M(16384M) 408.985ms";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 89961720 - 408, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(megabytes(16218), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(16382), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(16384), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(408985, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUnpreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[217230.988s][info][gc] GC(141) Pause Young (Mixed) (GCLocker Initiated GC) "
                + "4137M->2444M(16384M) 103.899ms";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }
}
