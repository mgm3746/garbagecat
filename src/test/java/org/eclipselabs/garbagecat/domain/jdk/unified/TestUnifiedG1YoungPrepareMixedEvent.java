/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.junit.Test;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedG1YoungPrepareMixedEvent {

    @Test
    public void testLogLinePreprocessed() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".",
                UnifiedG1YoungPrepareMixedEvent.match(logLine));
        UnifiedG1YoungPrepareMixedEvent event = new UnifiedG1YoungPrepareMixedEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 16627 - 0, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(3801), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(3801), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(1056768), event.getPermSpace());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(24 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(13 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(31 * 1024), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 361, event.getDuration());
        assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED + "not identified.",
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedG1YoungPrepareMixedEvent);
    }

    @Test
    public void testIsBlocking() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED;
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 15108;
        int duration = 0;
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not parsed.",
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedG1YoungPrepareMixedEvent);
    }

    @Test
    public void testReportable() {
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED);
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause) "
                + "Metaspace: 3801K->3801K(1056768K) 24M->13M(31M) 0.361ms User=0.00s Sys=0.00s Real=0.00s    ";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".",
                UnifiedG1YoungPrepareMixedEvent.match(logLine));
    }

    @Test
    public void testPreprocessing() {
        File testFile = TestUtil.getFile("dataset168.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PREPARE_MIXED));
    }

}
