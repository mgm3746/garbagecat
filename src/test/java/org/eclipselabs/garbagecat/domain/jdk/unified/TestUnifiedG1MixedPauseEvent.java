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
class TestUnifiedG1MixedPauseEvent {

    @Test
    void testLogLinePreprocessed() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
        UnifiedG1MixedPauseEvent event = new UnifiedG1MixedPauseEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 16629, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(3801), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(3801), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(kilobytes(15 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(12 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(31 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(1202, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedG1MixedPauseEvent,
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not parsed.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE;
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 15108;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedG1MixedPauseEvent,
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_MIXED_PAUSE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 15M->12M(31M) 1.202ms User=0.00s Sys=0.00s Real=0.00s     ";
        ;
        assertTrue(UnifiedG1MixedPauseEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }

    @Test
    void testPreprocessing() {
        File testFile = TestUtil.getFile("dataset169.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_MIXED_PAUSE.toString() + ".");
    }
}
