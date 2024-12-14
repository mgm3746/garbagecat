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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedParallelScavengeEvent {

    @Test
    void testHydration() {
        EventType eventType = JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE;
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedParallelScavengeEvent,
                JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        assertEquals(JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not indentified as blocking.");
    }

    @Test
    void testJdk17() {
        String logLine = "[0.026s][info][gc,start    ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K(1024K)->448K(1024K) ParOldGen: 0K(512K)->8K(512K) Metaspace: 88K(192K)->88K(192K) 0M->0M(1M) "
                + "0.656ms User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testLogLine7SpacesAfterStart() {
        String logLine = "[15.030s][info][gc,start       ] GC(1199) Pause Young (Allocation Failure) PSYoungGen: "
                + "20544K->64K(20992K) PSOldGen: 15496K->15504K(17920K) Metaspace: 3779K->3779K(1056768K) "
                + "35M->15M(38M) 0.402ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s    ";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedParallelScavengeEvent,
                JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not parsed.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "[0.031s][myhost][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->464K(1024K) PSOldGen: 0K->8K(512K) Metaspace: 120K->120K(1056768K) 0M->0M(1M) 1.195ms "
                + "User=0.01s Sys=0.01s Real=0.00s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
        UnifiedParallelScavengeEvent event = new UnifiedParallelScavengeEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 31, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(512), event.getYoungOccupancyInit(), "Young initial occupancy not parsed correctly.");
        assertEquals(kilobytes(464), event.getYoungOccupancyEnd(), "Young end occupancy not parsed correctly.");
        assertEquals(kilobytes(1024), event.getYoungSpace(), "Young space size not parsed correctly.");
        assertEquals(kilobytes(0), event.getOldOccupancyInit(), "Old initial occupancy not parsed correctly.");
        assertEquals(kilobytes(8), event.getOldOccupancyEnd(), "Old end occupancy not parsed correctly.");
        assertEquals(kilobytes(512), event.getOldSpace(), "Old space size not parsed correctly.");
        assertEquals(kilobytes(120), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(120), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(1195, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedParallelCompactingOld() {
        String logLine = "[0.029s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: "
                + "512K->432K(1024K) ParOldGen: 0K->8K(512K) Metaspace: 121K->121K(1056768K) 0M->0M(1M) 0.762ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
        UnifiedParallelScavengeEvent event = new UnifiedParallelScavengeEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 29, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(512), event.getYoungOccupancyInit(), "Young initial occupancy not parsed correctly.");
        assertEquals(kilobytes(432), event.getYoungOccupancyEnd(), "Young end occupancy not parsed correctly.");
        assertEquals(kilobytes(1024), event.getYoungSpace(), "Young space size not parsed correctly.");
        assertEquals(kilobytes(0), event.getOldOccupancyInit(), "Old initial occupancy not parsed correctly.");
        assertEquals(kilobytes(8), event.getOldOccupancyEnd(), "Old end occupancy not parsed correctly.");
        assertEquals(kilobytes(512), event.getOldSpace(), "Old space size not parsed correctly.");
        assertEquals(kilobytes(121), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(121), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(762, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedPromotionFailed() {
        String logLine = "[2021-10-30T02:03:26.100+0000][403655ms] GC(22) Pause Young (Allocation Failure) "
                + "Promotion failed PSYoungGen: 1246735K->1246735K(1264128K) ParOldGen: 2927696K->3125241K(3125248K) "
                + "Metaspace: 589156K->589156K(1687552K) 4076M->4269M(4286M) 692.086ms User=1.83s Sys=0.21s Real=0.69s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "[2021-09-14T06:53:30.699-0500][138.751s][info][gc,start       ] GC(6) Pause Young (GCLocker "
                + "Initiated GC) PSYoungGen: 2097664K->213686K(2446848K) ParOldGen: 31515K->31523K(5592576K) "
                + "Metaspace: 70500K->70500K(1116160K) 2079M->239M(7851M) 119.933ms User=0.61s Sys=0.21s Real=0.12s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerHeapDumpInitiatedGc() {
        String logLine = "[2021-11-01T20:48:05.098+0000][240210697ms] GC(950) Pause Young (Heap Dump Initiated GC) "
                + "PSYoungGen: 542130K->17888K(1538048K) ParOldGen: 152353K->152353K(180224K) "
                + "Metaspace: 217673K->217673K(1275904K) 678M->166M(1678M) 9.184ms User=0.04s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerMetadataGcClearSoftReferences() {
        String logLine = "[2022-02-08T07:33:13.178+0000][7731426ms] GC(111) Pause Young (Metadata GC Clear Soft "
                + "References) PSYoungGen: 0K->0K(731136K) ParOldGen: 145284K->145284K(1467904K) Metaspace: "
                + "243927K->243927K(481280K) 141M->141M(2147M) 4.151ms User=0.00s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testPreprocessedTriggerMetadataGcThreshold() {
        String logLine = "[2021-05-06T21:03:33.183+0000][22071ms] GC(10) Pause Young (Metadata GC Threshold) "
                + "PSYoungGen: 30682K->2160K(66560K) ParOldGen: 46817K->57994K(175104K) "
                + "Metaspace: 88760K->88760K(337920K) 75M->58M(236M) 44.313ms User=0.07s Sys=0.02s Real=0.04s";
        assertTrue(UnifiedParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE),
                JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_PARALLEL_SCAVENGE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_PARALLEL_SCAVENGE.toString() + " not indentified as unified.");
    }
}
