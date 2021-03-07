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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedParallelScavengeEvent {

    @Test
    void testPreprocessed() {
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
        UnifiedParallelScavengeEvent event = new UnifiedParallelScavengeEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString(),event.getName(),"Event name incorrect.");
        assertEquals((long) 31,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(512),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(464),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(1024),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(0),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(8),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(512),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(120),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(120),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1056768),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(1195,event.getDuration(),"Duration not parsed correctly.");
        assertEquals(1,event.getTimeUser(),"User time not parsed correctly.");
        assertEquals(0,event.getTimeReal(),"Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE,event.getParallelism(),"Parallelism not calculated correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE,JdkUtil.identifyEventType(logLine),JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedParallelScavengeEvent, JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not parsed.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE;
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(JdkUtil
		.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedParallelScavengeEvent, JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE), JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_PARALLEL_SCAVENGE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes), JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s    ";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testLogLine7SpacesAfterStart() {
        String logLine = "[15.030s][info][gc,start       ] GC(1199) Pause Young (Allocation Failure) PSYoungGen: "
                + "20544K->64K(20992K) PSOldGen: 15496K->15504K(17920K) Metaspace: 3779K->3779K(1056768K) "
                + "35M->15M(38M) 0.402ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testPreprocessedParallelCompactingOld() {
        String logLine = "[0.029s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->432K(1024K) ParOldGen: 0K->8K(512K) Metaspace: 121K->121K(1056768K) 0M->0M(1M) 0.762ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
        UnifiedParallelScavengeEvent event = new UnifiedParallelScavengeEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_PARALLEL_SCAVENGE.toString(),event.getName(),"Event name incorrect.");
        assertEquals((long) 29,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(512),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(432),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(1024),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(0),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(8),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(512),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(121),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(121),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1056768),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(762,event.getDuration(),"Duration not parsed correctly.");
        assertEquals(0,event.getTimeUser(),"User time not parsed correctly.");
        assertEquals(0,event.getTimeReal(),"Real time not parsed correctly.");
        assertEquals(100,event.getParallelism(),"Parallelism not calculated correctly.");
    }
}
