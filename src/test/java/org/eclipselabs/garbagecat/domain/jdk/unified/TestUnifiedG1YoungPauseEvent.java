/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedG1YoungPauseEvent {

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
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not indentified as blocking.");
    }

    @Test
    void testJdk17() {
        String logLine = "[0.037s][info][gc,start    ] GC(0) Pause Young (Normal) (G1 Preventive Collection) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 331K(512K)->331K(512K) 1M->1M(4M) 0.792ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedG1YoungPauseEvent,
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " not parsed.");
    }

    @Test
    void testPreparsingOtherTime() throws IOException {
        File testFile = TestUtil.getFile("dataset261.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        assertEquals(1, jvmRun.getDurationGtRealCount(), "Duration > real time count not correct.");
        LogEvent worstEvent = jvmRun.getWorstDurationGtRealTimeEvent();
        // 969
        int duration = JdkMath.convertMicrosToCentis(((BlockingEvent) worstEvent).getDuration()).intValue();
        assertEquals(958, duration, "Duration not correct.");
        int real = ((TimesData) worstEvent).getTimeReal();
        assertEquals(1, real, "Real time not correct.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) 0.401ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (15086 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(3771), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3771), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(24 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(13 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(31 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(501, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testPreprocessedConcurrentStartTriggerMetaGcThreshold() {
        String logLine = "[2020-06-24T18:11:52.676-0700][58671ms] GC(44) Pause Young (Concurrent Start) "
                + "(Metadata GC Threshold) Other: 0.1ms Humongous regions: 13->13 Metaspace: 88802K->88802K(1134592K) "
                + "733M->588M(1223M) 105.541ms User=0.18s Sys=0.00s Real=0.11s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 58671, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD), "Trigger not parsed correctly.");
        assertEquals(kilobytes(88802), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(88802), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1134592), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(733 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(588 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(1223 * 1024), event.getCombinedSpace(),
                "Combined allocation size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(105641, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(18, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(11, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(164, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedJdk11Time() {
        String logLine = "[2019-05-09T01:39:00.763+0000] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 26116K->26116K(278528K) 65M->8M(1304M) 57.263ms "
                + "User=0.02s Sys=0.01s Real=0.06s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals((long) 610663140763L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(57363, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testPreprocessedJdk11TimeUptime() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5.355s] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 26116K->26116K(278528K) 65M->8M(1304M) 57.263ms "
                + "User=0.02s Sys=0.01s Real=0.06s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals((long) 5355, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(57363, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testPreprocessedJdk11TimeUptimeMillis() {
        String logLine = "[2019-05-09T01:39:00.763+0000][5355ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 26116K->26116K(278528K) 65M->8M(1304M) 57.263ms "
                + "User=0.02s Sys=0.01s Real=0.06s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 5355, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(26116), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(26116), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(278528), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(65 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(8 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(1304 * 1024), event.getCombinedSpace(),
                "Combined allocation size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(57363, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(2, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(6, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(50, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedJdk11UptimeMillis() {
        String logLine = "[325ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) Other: 0.1ms Humongous regions: "
                + "13->13 Metaspace: 4300K->4300K(1056768K) 24M->3M(504M) 7.691ms User=0.05s Sys=0.03s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals((long) 325, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(7791, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testPreprocessedTimeUptimemillisTriggerGcLocker() {
        String logLine = "[2019-05-09T01:39:07.136+0000][11728ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 35318K->35318K(288768K) 78M->22M(1304M) 35.722ms "
                + "User=0.02s Sys=0.00s Real=0.04s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        UnifiedG1YoungPauseEvent event = new UnifiedG1YoungPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 11728, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC), "Trigger not parsed correctly.");
        assertEquals(kilobytes(35318), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(35318), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(288768), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(78 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(22 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(1304 * 1024), event.getCombinedSpace(),
                "Combined allocation size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(35822, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(2, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(4, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(50, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedTriggerG1EvacuationPause() {
        String logLine = "[2021-03-13T03:57:33.494+0530][81046562ms] GC(10044) Pause Young (Concurrent Start) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 Metaspace: 214120K->214120K(739328K) "
                + "8185M->8185M(8192M) 2.859ms User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerMetadataGcThreshold() {
        String logLine = "[2021-09-14T11:38:33.217-0500][3.874s][info][gc,start     ] GC(0) Pause Young "
                + "(Concurrent Start) (Metadata GC Threshold) Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 20058K->20058K(1069056K) 56M->7M(8192M) 10.037ms User=0.04s Sys=0.00s Real=0.01s";
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
    }

    /**
     * Test with time, uptime decorator.
     * 
     * @throws IOException
     */
    @Test
    void testTimeUptime() throws IOException {
        File testFile = TestUtil.getFile("dataset200.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.USING_G1),
                JdkUtil.LogEventType.USING_G1.toString() + " collector not identified.");
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
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
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
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testUnifiedG1YoungPauseConcurrentStartTriggerMetaGcThreshold() throws IOException {
        File testFile = TestUtil.getFile("dataset183.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testUnifiedG1YoungPauseDatestampMillis() throws IOException {
        File testFile = TestUtil.getFile("dataset166.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testUnifiedG1YoungPauseJdk9() throws IOException {
        File testFile = TestUtil.getFile("dataset158.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[15.086s][info][gc,start     ] GC(1192) Pause Young (Normal) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3771K->3771K(1056768K) 24M->13M(31M) "
                + "0.401ms User=0.00s Sys=0.00s Real=0.00s    ";
        assertTrue(UnifiedG1YoungPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
    }
}
