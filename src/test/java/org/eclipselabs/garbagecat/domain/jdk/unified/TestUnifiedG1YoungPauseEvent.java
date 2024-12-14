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
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedG1YoungPauseEvent {

    /**
     * Test with time, uptime decorator.
     * 
     * @throws IOException
     */
    @Test
    void testDecoratorTimeUptime() throws IOException {
        File testFile = TestUtil.getFile("dataset200.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " event not identified.");
        UnifiedG1YoungPauseEvent event = (UnifiedG1YoungPauseEvent) jvmRun.getFirstBlockingEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (3353 - 24), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE;
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedG1YoungPauseEvent,
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) Ext Root "
                + "Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) "
                + "24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) Ext Root "
                + "Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) "
                + "24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as blocking.");
    }

    @Test
    void testJdk17() {
        String logLine = "[0.037s][info][gc,start    ] GC(0) Pause Young (Normal) (G1 Preventive Collection) Ext Root "
                + "Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 Metaspace: 331K(512K)->331K(512K) "
                + "1M->1M(4M) 0.792ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) Ext Root "
                + "Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) "
                + "24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedG1YoungPauseEvent,
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not parsed.");
    }

    @Test
    void testPreparsingOtherTime() throws IOException {
        File testFile = TestUtil.getFile("dataset261.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = (UnifiedG1YoungPauseEvent) jvmRun.getFirstBlockingEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (3792764), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testPreparsingUsingG1() throws IOException {
        File testFile = TestUtil.getFile("dataset270.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = (UnifiedG1YoungPauseEvent) jvmRun.getFirstBlockingEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (9770 - 130), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) Ext Root "
                + "Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) "
                + "24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (15086 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(3771), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3771), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(24 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(13 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(31 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(501, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testPreprocessedConcurrentStartTriggerMetaGcThreshold() {
        String logLine = "[2020-06-24T18:11:52.676-0700][58671ms][gc,start] GC(44) Pause Young (Concurrent Start) "
                + "(Metadata GC Threshold) Ext Root Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 88802K->88802K(1134592K) 733M->588M(1223M) 105.541ms User=0.18s Sys=0.00s Real=0.11s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) 58671, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.METADATA_GC_THRESHOLD, "Trigger not parsed correctly.");
        assertEquals(kilobytes(88802), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(88802), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1134592), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(733 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(588 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(1223 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(1600, event.getExtRootScanningTime(), "External root scanning time not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(105641, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(18, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(11, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(164, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedJdk11Time() {
        String logLine = "[2019-05-09T01:39:00.763+0000][gc,start] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Ext Root Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 26116K->26116K(278528K) 65M->8M(1304M) 57.263ms User=0.02s Sys=0.01s Real=0.06s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals((long) 610663140763L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(57363, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testPreprocessedJdk11TimeUptime() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5.355s][gc,start] GC(0) Pause Young (Normal) "
                + "(G1 Evacuation Pause) Ext Root Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 26116K->26116K(278528K) 65M->8M(1304M) 57.263ms User=0.02s Sys=0.01s Real=0.06s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals((long) 5355, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(57363, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testPreprocessedJdk11TimeUptimeMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms][gc,start] GC(0) Pause Young (Normal) "
                + "(G1 Evacuation Pause) Ext Root Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 26116K->26116K(278528K) 65M->8M(1304M) 57.263ms User=0.02s Sys=0.01s Real=0.06s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 5355, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(26116), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(26116), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(278528), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(65 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(8 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(1304 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(57363, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(2, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(6, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(50, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedJdk11UptimeMillis() {
        String logLine = "[325ms][gc,start] GC(0) Pause Young (Normal) (G1 Evacuation Pause) Ext Root Scanning (ms): "
                + "1.6 Other: 0.1ms Humongous regions: 13->13 Metaspace: 4300K->4300K(1056768K) 24M->3M(504M) 7.691ms "
                + "User=0.05s Sys=0.03s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals((long) 325, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(7791, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testPreprocessedMinimal() {
        String logLine = "[0.050s][info][gc       ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 1M->1M(4M) "
                + "4.854ms User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (50 - 4), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(1 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(1 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(4 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(4854, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedNoExtRootScanningNoOther() {
        String logLine = "[0.043s][info][gc,start ] GC(0) Pause Young (Normal) (G1 Evacuation Pause) Humongous "
                + "regions: 0->0 Metaspace: 477K(4864K)->477K(4864K) 1M->1M(4M) 4.478ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testPreprocessedTimeUptimemillisTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.136+0000][11728ms][gc,start] GC(3) Pause Young (Normal) "
                + "(GCLocker Initiated GC) Ext Root Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 35318K->35318K(288768K) 78M->22M(1304M) 35.722ms User=0.02s Sys=0.00s Real=0.04s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 11728, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.GCLOCKER_INITIATED_GC, "Trigger not parsed correctly.");
        assertEquals(kilobytes(35318), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(35318), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(288768), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(78 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(22 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(1304 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(35822, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(2, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(4, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(50, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedToSpaceExhausted() {
        String logLine = "[2021-03-13T03:37:40.047+0530][79853115ms][gc,start] GC(8645) Pause Young (Normal) "
                + "(GCLocker Initiated GC) To-space exhausted Other: 0.4ms Humongous regions: 18->18 "
                + "Metaspace: 214096K->214096K(739328K) 8186M->8186M(8192M) 3.471ms User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerG1EvacuationPause() {
        String logLine = "[2021-03-13T03:57:33.494+0530][81046562ms][gc,start] GC(10044) Pause Young "
                + "(Concurrent Start) (G1 Evacuation Pause) Ext Root Scanning (ms): 1.6 Other: 0.1ms "
                + "Humongous regions: 13->13 Metaspace: 214120K->214120K(739328K) 8185M->8185M(8192M) 2.859ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerMetadataGcThreshold() {
        String logLine = "[2021-09-14T11:38:33.217-0500][3.874s][info][gc,start     ] GC(0) Pause Young "
                + "(Concurrent Start) (Metadata GC Threshold) Ext Root Scanning (ms): 1.6 Other: 0.1ms Humongous "
                + "regions: 13->13 Metaspace: 20058K->20058K(1069056K) 56M->7M(8192M) 10.037ms User=0.04s "
                + "Sys=0.00s Real=0.01s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as reportable.");
    }

    /**
     * Test single line with time, uptime decorator.
     * 
     * @throws IOException
     */
    @Test
    void testSingleLineTimeUptime() throws IOException {
        File testFile = TestUtil.getFile("dataset202.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " event not identified.");
        UnifiedG1YoungPauseEvent event = (UnifiedG1YoungPauseEvent) jvmRun.getFirstBlockingEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (3353 - 24), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][gc,start] GC(4) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Humongous regions: 0->0 Metaspace: 477K(4864K)->477K(4864K) 1M->1M(4M) 4.478ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(746241357862L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampTimeUptime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3.161s][gc,start] GC(4) Pause Young (Normal) "
                + "(G1 Evacuation Pause) Humongous regions: 0->0 Metaspace: 477K(4864K)->477K(4864K) 1M->1M(4M) "
                + "4.478ms User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampTimeUptimeMillis() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms][gc,start] GC(4) Pause Young (Normal) "
                + "(G1 Evacuation Pause) Humongous regions: 0->0 Metaspace: 477K(4864K)->477K(4864K) 1M->1M(4M) "
                + "4.478ms User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampUptime() {
        String logLine = "[3.161s][gc,start] GC(4) Pause Young (Normal) (G1 Evacuation Pause) Humongous "
                + "regions: 0->0 Metaspace: 477K(4864K)->477K(4864K) 1M->1M(4M) 4.478ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampUptimeMillis() {
        String logLine = "[3161ms][gc,start] GC(4) Pause Young (Normal) (G1 Evacuation Pause) Humongous "
                + "regions: 0->0 Metaspace: 477K(4864K)->477K(4864K) 1M->1M(4M) 4.478ms "
                + "User=0.01s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    /**
     * Test single line with time, uptime decorator.
     * 
     * @throws IOException
     */
    @Test
    void testToSpaceExhausted() throws IOException {
        File testFile = TestUtil.getFile("dataset204.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " event not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_G1_EVACUATION_FAILURE.getKey()),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
        UnifiedG1YoungPauseEvent event = (UnifiedG1YoungPauseEvent) jvmRun.getFirstBlockingEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (79853115), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_YOUNG_PAUSE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as unified.");
    }

    @Test
    void testUnifiedG1YoungPauseConcurrentStartTriggerG1HumongousAllocation() throws IOException {
        File testFile = TestUtil.getFile("dataset185.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = (UnifiedG1YoungPauseEvent) jvmRun.getFirstBlockingEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (4442370), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnifiedG1YoungPauseConcurrentStartTriggerMetaGcThreshold() throws IOException {
        File testFile = TestUtil.getFile("dataset183.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = (UnifiedG1YoungPauseEvent) jvmRun.getFirstBlockingEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (58671), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnifiedG1YoungPauseDatestampMillis() throws IOException {
        File testFile = TestUtil.getFile("dataset166.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = (UnifiedG1YoungPauseEvent) jvmRun.getFirstBlockingEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (5355), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnifiedG1YoungPauseJdk9() throws IOException {
        File testFile = TestUtil.getFile("dataset158.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = (UnifiedG1YoungPauseEvent) jvmRun.getFirstBlockingEvent();
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
        assertEquals((long) (333), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnpreprocessed() {
        String logLine = "[89974.613s][info][gc] GC(1345) Pause Young (Concurrent Start) (G1 Evacuation Pause) "
                + "14593M->13853M(16384M) 92.109ms";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertTrue(event.isEndstamp(), "Event time information not identified as endstamp.");
        assertEquals((long) 89974613 - 92, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(megabytes(14593), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(13853), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(16384), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(92109, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Ext Root Scanning (ms): 1.6 Other: 0.1ms Humongous regions: 13->13 Metaspace: "
                + "3771K->3771K(1056768K) 24M->13M(31M) 0.401ms User=0.00s Sys=0.00s Real=0.00s    ";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertFalse(event.isEndstamp(), "Event time incorrectly identified as endstamp.");
    }
}
