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
class TestUnifiedSerialOldEvent {

    @Test
    void test7SpacesAfterStart() {
        String logLine = "[0.119s][info][gc,start       ] GC(5) Pause Full (Allocation Failure) DefNew: "
                + "1142K->110K(1152K) Tenured: 1044K->1934K(1936K) Metaspace: 1295K->1295K(1056768K) 2M->1M(4M) "
                + "3.178ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + ".");
    }

    @Test
    void testHydration() {
        EventType eventType = JdkUtil.EventType.UNIFIED_SERIAL_OLD;
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure) DefNew: "
                + "1152K->0K(1152K) Tenured: 458K->929K(960K) Metaspace: 697K->697K(1056768K) 1M->0M(2M) 3.061ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        long timestamp = 27091;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof UnifiedSerialOldEvent,
                JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure) DefNew: "
                + "1152K->0K(1152K) Tenured: 458K->929K(960K) Metaspace: 697K->697K(1056768K) 1M->0M(2M) 3.061ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.EventType.UNIFIED_SERIAL_OLD,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_SERIAL_OLD + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure) DefNew: "
                + "1152K->0K(1152K) Tenured: 458K->929K(960K) Metaspace: 697K->697K(1056768K) 1M->0M(2M) 3.061ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + " not indentified as blocking.");
    }

    @Test
    void testJdk17() {
        String logLine = "[0.071s][info][gc,start    ] GC(3) Pause Full (Allocation Failure) DefNew: "
                + "1125K(1152K)->0K(1152K) Tenured: 754K(768K)->1500K(2504K) Metaspace: 1003K(1088K)->1003K(1088K) "
                + "1M->1M(3M) 1.064ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure) DefNew: "
                + "1152K->0K(1152K) Tenured: 458K->929K(960K) Metaspace: 697K->697K(1056768K) 1M->0M(2M) 3.061ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedSerialOldEvent,
                JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + " not parsed.");
    }

    @Test
    void testPreprocessedTriggerErgonomics() {
        String logLine = "[0.091s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K->436K(1536K) "
                + "PSOldGen: 460K->511K(2048K) Metaspace: 701K->701K(1056768K) 0M->0M(3M) 1.849ms "
                + "User=0.01s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + ".");
        UnifiedSerialOldEvent event = new UnifiedSerialOldEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_SERIAL_OLD, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 91, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.ERGONOMICS, "Trigger not parsed correctly.");
        assertEquals(kilobytes(502), event.getYoungOccupancyInit(), "Young initial occupancy not parsed correctly.");
        assertEquals(kilobytes(436), event.getYoungOccupancyEnd(), "Young end occupancy not parsed correctly.");
        assertEquals(kilobytes(1536), event.getYoungSpace(), "Young space size not parsed correctly.");
        assertEquals(kilobytes(460), event.getOldOccupancyInit(), "Old initial occupancy not parsed correctly.");
        assertEquals(kilobytes(511), event.getOldOccupancyEnd(), "Old end occupancy not parsed correctly.");
        assertEquals(kilobytes(2048), event.getOldSpace(), "Old space size not parsed correctly.");
        assertEquals(kilobytes(701), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(701), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(1849, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testPreprocessedYoungCollectionTriggersFullGc() {
        String logLine = "[0.073s][info][gc,start     ] GC(1) Pause Young (Allocation Failure) Pause Full "
                + "(Allocation Failure) DefNew: 1152K->0K(1152K) Tenured: 458K->929K(960K) Metaspace: "
                + "697K->697K(1056768K) 1M->0M(2M) 3.061ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + ".");
        UnifiedSerialOldEvent event = new UnifiedSerialOldEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_SERIAL_OLD, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 73, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(1152), event.getYoungOccupancyInit(), "Young initial occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end occupancy not parsed correctly.");
        assertEquals(kilobytes(1152), event.getYoungSpace(), "Young space size not parsed correctly.");
        assertEquals(kilobytes(458), event.getOldOccupancyInit(), "Old initial occupancy not parsed correctly.");
        assertEquals(kilobytes(929), event.getOldOccupancyEnd(), "Old end occupancy not parsed correctly.");
        assertEquals(kilobytes(960), event.getOldSpace(), "Old space size not parsed correctly.");
        assertEquals(kilobytes(697), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(697), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(3061, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_SERIAL_OLD),
                JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_SERIAL_OLD);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + " not indentified as unified.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure) DefNew: "
                + "1152K->0K(1152K) Tenured: 458K->929K(960K) Metaspace: 697K->697K(1056768K) 1M->0M(2M) 3.061ms "
                + "User=0.00s Sys=0.00s Real=0.00s    ";
        assertTrue(UnifiedSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_OLD.toString() + ".");
    }
}
