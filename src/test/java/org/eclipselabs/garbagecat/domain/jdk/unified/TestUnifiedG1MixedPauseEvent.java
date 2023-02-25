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
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedG1MixedPauseEvent {

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE;
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 15108;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedG1MixedPauseEvent,
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLinePreprocessed() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 16629, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_EVACUATION_PAUSE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(3801), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(3801), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(kilobytes(15 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(12 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(31 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(100, event.getOtherTime(), "Other time not parsed correctly.");
        assertEquals(1302, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedJdk17() {
        String logLine = "[2022-08-05T05:08:55.096+0000][1908][gc,start    ] GC(1362) Pause Young (Mixed) "
                + "(G1 Evacuation Pause) Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 147162K(149824K)->147162K(149824K) 8331M->4808M(32768M) 25,917ms User=0,17s "
                + "Sys=0,00s Real=0,03s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testLogLinePreprocessedTriggerG1HumongousAllocation() {
        String logLine = "[2022-01-26T10:02:45.142+0530][297108898ms] GC(8626) Pause Young (Mixed) "
                + "(G1 Humongous Allocation) Other: 0.1ms Humongous regions: 13->13 "
                + "Metaspace: 443023K->443023K(1468416K) 8176M->717M(8192M) 9.643ms User=0.03s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testLogLinePreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[2021-10-14T17:52:08.374+0400][info][gc,start      ] GC(2131) Pause Young (Mixed) (GCLocker "
                + "Initiated GC) Other: 0.1ms Humongous regions: 13->13 Metaspace: 365476K->365476K(1384448K) "
                + "3827M->3109M(12288M) 23.481ms User=0.20s Sys=0.02s Real=0.02s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s     ";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedG1MixedPauseEvent,
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not parsed.");
    }

    @Test
    void testPreprocessing() throws IOException {
        File testFile = TestUtil.getFile("dataset169.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not indentified as reportable.");
    }

    /**
     * Test with time, uptime decorator.
     */
    @Test
    void testTimeUptime() {
        String logLine = "[2021-03-09T14:45:02.441-0300][12.082s] GC(6) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Other: 0.1ms Humongous regions: 13->13 Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString(), event.getName(), "Event name incorrect.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_MIXED_PAUSE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not indentified as unified.");
    }
}
