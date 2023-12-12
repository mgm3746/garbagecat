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
class TestUnifiedYoungEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[9.602s][info][gc] GC(569) Pause Young (Allocation Failure) 32M->12M(38M) 1.812ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_YOUNG, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_YOUNG + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[9.602s][info][gc] GC(569) Pause Young (Allocation Failure) 32M->12M(38M) 1.812ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " not indentified as blocking.");
    }

    @Test
    void testJdk17() {
        String logLine = "[0.070s][info][gc,start    ] GC(2) Pause Young (Allocation Failure) 1M->1M(2M) 0.663ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
        UnifiedYoungEvent event = new UnifiedYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertTrue(event.getTrigger() == GcTrigger.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals((long) (70 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(1 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(2 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(663, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLine() {
        String logLine = "[9.602s][info][gc] GC(569) Pause Young (Allocation Failure) 32M->12M(38M) 1.812ms";
        assertTrue(UnifiedYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
        UnifiedYoungEvent event = new UnifiedYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertTrue(event.getTrigger() == GcTrigger.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals((long) (9602 - 1), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(32 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(12 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(38 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(1812, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[1.102s][info][gc] GC(48) Pause Young (Allocation Failure) 23M->3M(25M) 0.409ms     ";
        assertTrue(UnifiedYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
    }

    @Test
    void testNoData() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure)";
        assertEquals(JdkUtil.LogEventType.UNKNOWN, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNKNOWN + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[9.602s][info][gc] GC(569) Pause Young (Allocation Failure) 32M->12M(38M) 1.812ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedYoungEvent,
                JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_YOUNG),
                JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " not indentified as reportable.");
    }

    @Test
    void testTriggerExplicitGc() {
        String logLine = "[7.487s][info][gc] GC(497) Pause Young (System.gc()) 16M->10M(36M) 0.940ms";
        assertTrue(UnifiedYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
        UnifiedYoungEvent event = new UnifiedYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (7487 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.SYSTEM_GC, "Trigger not parsed correctly.");
        assertEquals(kilobytes(16 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(10 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(36 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(940, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testTriggerGcLockerInitiatedGc() {
        String logLine = "[18.084s][info][gc] GC(9) Pause Young (GCLocker Initiated GC) 1668M->411M(5727M) 86.334ms";
        assertTrue(UnifiedYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
    }

    @Test
    void testTriggerMetadataGcThreshold() {
        String logLine = "[1.705s][info][gc] GC(0) Pause Young (Metadata GC Threshold) 337M->7M(5888M) 7.886ms";
        assertTrue(UnifiedYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_YOUNG);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " not indentified as unified.");
    }

    @Test
    void testUnifiedYoungExplictGc() throws IOException {
        File testFile = TestUtil.getFile("dataset154.txt");
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
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_YOUNG),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_EXPLICIT_GC_UNKNOWN.getKey()),
                Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.");
        UnifiedYoungEvent event = (UnifiedYoungEvent) jvmRun.getLastGcEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (7487 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnifiedYoungMixedSafepoint() throws IOException {
        File testFile = TestUtil.getFile("dataset276.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_SAFEPOINT),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_YOUNG),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".");
        UnifiedYoungEvent event = (UnifiedYoungEvent) jvmRun.getLastGcEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (50 - 1), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnifiedYoungStandardLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset149.txt");
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
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_YOUNG),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING.getKey()),
                Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis not identified.");
        UnifiedYoungEvent event = (UnifiedYoungEvent) jvmRun.getLastGcEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (1507 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
    }
}
