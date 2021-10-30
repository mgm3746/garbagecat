/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
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
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
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
class TestUnifiedG1YoungPrepareMixedEvent {

    @Test
    void testLogLinePreprocessed() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString(), event.getName(),
                "Event name incorrect.");
        assertEquals((long) (16627 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(3801), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(3801), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(kilobytes(24 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(13 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(31 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(361, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedG1YoungPrepareMixedEvent,
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not parsed.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED;
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 15108;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedG1YoungPrepareMixedEvent,
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s    ";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
    }

    /**
     * Test with time, uptime decorator.
     */
    @Test
    void testTimeUptime() {
        String logLine = "[2021-03-09T14:45:02.441-0300][12.082s] GC(6) Pause Young (Prepare Mixed) "
                + "(G1 Evacuation Pause) Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testPreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[2021-10-14T00:22:54.796+0400][info][gc,start      ] GC(891) Pause Young (Prepare Mixed) "
                + "(GCLocker Initiated GC) Metaspace: 360792K->360792K(1380352K) 10311M->3024M(12288M) 33.928ms "
                + "User=0.25s Sys=0.04s Real=0.03s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testPreprocessedTriggerG1HumongousAllocation() {
        String logLine = "[2021-10-29T20:56:08.426+0000][info][gc,start      ] GC(734) Pause Young (Prepare Mixed) "
                + "(G1 Humongous Allocation) Metaspace: 66401K->66401K(151552K) 15678M->1575M(16384M) 24.193ms "
                + "User=0.12s Sys=0.00s Real=0.03s";
        assertTrue(UnifiedG1YoungPrepareMixedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testPreprocessing() {
        File testFile = TestUtil.getFile("dataset168.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".");
    }
}
