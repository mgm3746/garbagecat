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
import org.eclipselabs.garbagecat.domain.LogEvent;
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
class TestZStatsEvent {

    @Test
    void testFooter() {
        LogEvent priorLogEvent = new ZStatsEvent(null);
        String logLine = "[10.485s] =================================================================================="
                + "=======================================================================";
        assertTrue(JdkUtil.parseLogLine(logLine, priorLogEvent, CollectorFamily.UNKNOWN) instanceof ZStatsEvent,
                JdkUtil.LogEventType.Z_STATS.toString() + " not parsed.");
    }

    @Test
    void testHeader() {
        String logLine = "[10.485s] === Garbage Collection Statistics ================================================"
                + "=======================================================================";
        assertEquals(JdkUtil.LogEventType.Z_STATS, JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.Z_STATS + "not identified.");
        ZStatsEvent event = new ZStatsEvent(logLine);
        assertTrue(event.isHeader(), "Header not identified.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[10.485s] === Garbage Collection Statistics ================================================"
                + "=======================================================================";
        assertEquals(JdkUtil.LogEventType.Z_STATS, JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.Z_STATS + "not identified.");
    }

    @Test
    void testMemoryAllocationRate() {
        LogEvent priorLogEvent = new ZStatsEvent(null);
        String logLine = "[10.029s][info][gc,stats    ]      Memory: Allocation Rate                                 "
                + "941 / 1154            941 / 1154            941 / 1154            941 / 1154        MB/s";
        assertTrue(JdkUtil.parseLogLine(logLine, priorLogEvent, CollectorFamily.UNKNOWN) instanceof ZStatsEvent,
                JdkUtil.LogEventType.Z_STATS.toString() + " not parsed.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[10.485s] === Garbage Collection Statistics ================================================"
                + "=======================================================================";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.Z_STATS.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[10.485s] === Garbage Collection Statistics ================================================"
                + "=======================================================================";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ZStatsEvent,
                JdkUtil.LogEventType.Z_STATS.toString() + " not parsed.");
    }

    @Test
    void testPhaseConcurrentMark() {
        LogEvent priorLogEvent = new ZStatsEvent(null);
        String logLine = "[10.029s][info][gc,stats    ]       Phase: Concurrent Mark                              "
                + "25.345 / 60.647       25.345 / 60.647       25.345 / 60.647       25.345 / 60.647      ms";
        assertTrue(JdkUtil.parseLogLine(logLine, priorLogEvent, CollectorFamily.UNKNOWN) instanceof ZStatsEvent,
                JdkUtil.LogEventType.Z_STATS.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.Z_STATS),
                JdkUtil.LogEventType.Z_STATS.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testSubphaseConcurrentMarkTryFlush() {
        LogEvent priorLogEvent = new ZStatsEvent(null);
        String logLine = "[10.029s][info][gc,stats    ]    Subphase: Concurrent Mark Try Flush                     "
                + "0.087 / 1.125         0.087 / 1.125         0.087 / 1.125         0.087 / 1.125       ms";
        assertTrue(JdkUtil.parseLogLine(logLine, priorLogEvent, CollectorFamily.UNKNOWN) instanceof ZStatsEvent,
                JdkUtil.LogEventType.Z_STATS.toString() + " not parsed.");
    }

    @Test
    void testSubphaseConcurrentMarkTryTerminate() {
        LogEvent priorLogEvent = new ZStatsEvent(null);
        String logLine = "[10.029s][info][gc,stats    ]    Subphase: Concurrent Mark Try Terminate                 "
                + "0.019 / 0.809         0.019 / 0.809         0.019 / 0.809         0.019 / 0.809       ms";
        assertTrue(JdkUtil.parseLogLine(logLine, priorLogEvent, CollectorFamily.UNKNOWN) instanceof ZStatsEvent,
                JdkUtil.LogEventType.Z_STATS.toString() + " not parsed.");
    }

    @Test
    void testTitle1() {
        LogEvent priorLogEvent = new ZStatsEvent(null);
        String logLine = "[10.029s][info][gc,stats    ]                                                              "
                + "Last 10s              Last 10m              Last 10h                Total";
        assertTrue(JdkUtil.parseLogLine(logLine, priorLogEvent, CollectorFamily.UNKNOWN) instanceof ZStatsEvent,
                JdkUtil.LogEventType.Z_STATS.toString() + " not parsed.");
    }

    @Test
    void testTitle2() {
        LogEvent priorLogEvent = new ZStatsEvent(null);
        String logLine = "[10.029s][info][gc,stats    ]                                                              "
                + "Avg / Max             Avg / Max             Avg / Max             Avg / Max";
        assertTrue(JdkUtil.parseLogLine(logLine, priorLogEvent, CollectorFamily.UNKNOWN) instanceof ZStatsEvent,
                JdkUtil.LogEventType.Z_STATS.toString() + " not parsed.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.Z_STATS);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.Z_STATS.toString() + " not indentified as unified.");
    }

    @Test
    void testZGenerational() throws IOException {
        File testFile = TestUtil.getFile("dataset273.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_STATS),
                JdkUtil.LogEventType.Z_STATS.toString() + " event not identified.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " event incorrectly identified.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " event incorrectly identified.");
    }

    @Test
    void testZNonGenerational() throws IOException {
        File testFile = TestUtil.getFile("dataset274.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.Z_STATS),
                JdkUtil.LogEventType.Z_STATS.toString() + " event not identified.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.FOOTER_STATS),
                JdkUtil.LogEventType.FOOTER_STATS.toString() + " event incorrectly identified.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_STATS),
                JdkUtil.LogEventType.SHENANDOAH_STATS.toString() + " event incorrectly identified.");
    }
}
