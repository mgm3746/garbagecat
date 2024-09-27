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
class TestUnifiedOldEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_OLD,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_OLD + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms";
        assertTrue(UnifiedOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".");
        UnifiedOldEvent event = new UnifiedOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (231 - 2), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.ERGONOMICS, "Trigger not parsed correctly.");
        assertEquals(kilobytes(1 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(1 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(7 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(2969, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms     ";
        assertTrue(UnifiedOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedOldEvent,
                JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not parsed.");
    }

    @Test
    void testPreprocessedTriggerGcLockerInitiatedGc() throws IOException {
        File testFile = TestUtil.getFile("dataset269.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_G1_EVACUATION_FAILURE.getKey()),
                Analysis.ERROR_G1_EVACUATION_FAILURE + " analysis not identified.");
        UnifiedOldEvent event = (UnifiedOldEvent) jvmRun.getLastGcEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (390361491 - 6628), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testPreprocessedTriggerSystemGc() {
        String logLine = "[2020-06-24T18:13:47.695-0700][173690ms][gc,start] GC(74) Pause Full (System.gc()) "
                + "Metaspace: 260211K->260197K(1290240K) 887M->583M(1223M) 3460.196ms User=1.78s Sys=0.01s Real=3.46s";
        assertTrue(UnifiedOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".");
        UnifiedOldEvent event = new UnifiedOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (173690), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.SYSTEM_GC, "Trigger not parsed correctly.");
        assertEquals(kilobytes(260211), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(260197), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1290240), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(887 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(583 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(1223 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(3460196, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(178, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(346, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(52, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessingNewAllocationFailureTriggersOld() throws IOException {
        File testFile = TestUtil.getFile("dataset277.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_YOUNG),
                JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_OLD),
                JdkUtil.LogEventType.UNIFIED_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_SAFEPOINT),
                JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + " collector not identified.");
        UnifiedOldEvent event = (UnifiedOldEvent) jvmRun.getLastGcEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (89 - 1), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_OLD),
                JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_OLD);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not indentified as unified.");
    }

    @Test
    void testUnifiedOldExplictGc() throws IOException {
        File testFile = TestUtil.getFile("dataset153.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_SERIAL_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_EXPLICIT_GC_UNKNOWN.getKey()),
                Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.");
        UnifiedOldEvent event = (UnifiedOldEvent) jvmRun.getLastGcEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (7187 - 31), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnifiedOldStandardLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset148.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_COMPACTING_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING.getKey()),
                Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis not identified.");
        UnifiedOldEvent event = (UnifiedOldEvent) jvmRun.getLastGcEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (139 - 5), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnpreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[390361.491s][info][gc] GC(1474) Pause Full (GCLocker Initiated GC) 16340M->15902M(16384M) "
                + "6628.742ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedOldEvent,
                JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not parsed.");
    }
}
