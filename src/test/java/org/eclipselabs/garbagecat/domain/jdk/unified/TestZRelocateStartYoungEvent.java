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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestZRelocateStartYoungEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.103s][info][gc,phases   ] GC(0) Y: Pause Relocate Start 0.006ms";
        assertEquals(JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.103s][info][gc,phases   ] GC(0) Y: Pause Relocate Start 0.006ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.103s][info][gc,phases   ] GC(0) Y: Pause Relocate Start 0.006ms";
        assertTrue(ZRelocateStartYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + ".");
        ZRelocateStartYoungEvent event = new ZRelocateStartYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (103 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(6, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLowerCaseY() {
        String logLine = "[0.310s][info][gc,phases   ] GC(3) y: Pause Relocate Start 0.008ms";
        assertTrue(ZRelocateStartYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + ".");
        ZRelocateStartYoungEvent event = new ZRelocateStartYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (310 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(8, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.103s][info][gc,phases   ] GC(0) Y: Pause Relocate Start 0.006ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ZRelocateStartYoungEvent,
                JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + " not parsed.");
    }

    @Test
    void testPreprocessed() throws IOException {
        File testFile = TestUtil.getFile("dataset278.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(7, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.Z_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.Z_MARK_END_YOUNG),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_MARK_END_YOUNG.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_SAFEPOINT),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SAFEPOINT.toString() + ".");
        ZRelocateStartYoungEvent event = (ZRelocateStartYoungEvent) jvmRun.getLastBlockingEvent();
        assertTrue(event.isEndstamp(), "Event time not identified as endstamp.");
        assertEquals((long) (755666465303L - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG),
                JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + " not indentified as reportable.");
    }

    /**
     * Test with time, uptime decorator.
     */
    @Test
    void testTimestampTimeUptime() {
        String logLine = "[2021-03-09T14:45:02.441-0300][0.103s][info][gc,phases   ] GC(0) Y: Pause Relocate Start "
                + "0.006ms";
        assertTrue(ZRelocateStartYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + ".");
        ZRelocateStartYoungEvent event = new ZRelocateStartYoungEvent(logLine);
        assertEquals(JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString(), event.getName(), "Event name incorrect.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.Z_RELOCATE_START_YOUNG);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + " not indentified as unified.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[0.103s][info][gc,phases   ] GC(0) Y: Pause Relocate Start 0.006ms    ";
        assertTrue(ZRelocateStartYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.Z_RELOCATE_START_YOUNG.toString() + ".");
    }
}
