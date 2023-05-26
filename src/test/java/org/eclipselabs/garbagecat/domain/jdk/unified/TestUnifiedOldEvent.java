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
        assertEquals(JdkUtil.LogEventType.UNIFIED_OLD, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_OLD + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.231s][info][gc] GC(6) Pause Full (Ergonomics) 1M->1M(7M) 2.969ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
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
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(1 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(7 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(2969, event.getDuration(), "Duration not parsed correctly.");
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
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedOldEvent,
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
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerSystemGc() {
        String logLine = "[2020-06-24T18:13:47.695-0700][173690ms] GC(74) Pause Full (System.gc()) Metaspace: "
                + "260211K->260197K(1290240K) 887M->583M(1223M) 3460.196ms User=1.78s Sys=0.01s Real=3.46s";
        assertTrue(UnifiedOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".");
        UnifiedOldEvent event = new UnifiedOldEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_OLD.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (173690 - 3460), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.SYSTEM_GC, "Trigger not parsed correctly.");
        assertEquals(kilobytes(260211), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(260197), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1290240), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(kilobytes(887 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(583 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(1223 * 1024), event.getCombinedSpace(),
                "Combined allocation size not parsed correctly.");
        assertEquals(3460196, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(178, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(346, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(52, event.getParallelism(), "Parallelism not calculated correctly.");
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
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_YOUNG),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_EXPLICIT_GC_UNKNOWN.getKey()),
                Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.");
    }

    @Test
    void testUnifiedOldStandardLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset148.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING.getKey()),
                Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis not identified.");
    }

    @Test
    void testUnifiedSerialOldTriggerSystemGc() throws IOException {
        File testFile = TestUtil.getFile("dataset184.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + ".");
    }

    @Test
    void testUnpreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[390361.491s][info][gc] GC(1474) Pause Full (GCLocker Initiated GC) 16340M->15902M(16384M) "
                + "6628.742ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedOldEvent,
                JdkUtil.LogEventType.UNIFIED_OLD.toString() + " not parsed.");
    }
}
