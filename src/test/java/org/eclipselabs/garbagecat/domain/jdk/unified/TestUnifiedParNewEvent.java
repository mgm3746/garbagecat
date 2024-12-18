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
class TestUnifiedParNewEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertEquals(JdkUtil.EventType.UNIFIED_PAR_NEW,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_PAR_NEW + "not identified.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s    ";
        assertTrue(UnifiedParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PAR_NEW.toString() + ".");
    }

    @Test
    void testMetaspaceWithBeginSize() {
        String logLine = "[2022-10-25T08:41:22.776-0400] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "935K->128K(1152K) CMS: 0K->486K(960K) Metaspace: 244K(4480K)->244K(4480K) 0M->0M(2M) 1.944ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.EventType.UNIFIED_PAR_NEW,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_PAR_NEW + "not identified.");
    }

    @Test
    void testParallelismUserZero() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.00s Sys=0.00s Real=0.01s";
        assertTrue(UnifiedParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PAR_NEW.toString() + ".");
        UnifiedParNewEvent event = new UnifiedParNewEvent(logLine);
        assertEquals(0, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedParNewEvent,
                JdkUtil.EventType.UNIFIED_PAR_NEW.toString() + " not parsed.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "[0.049s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) ParNew: "
                + "974K->128K(1152K) CMS: 0K->518K(960K) Metaspace: 250K->250K(1056768K) 0M->0M(2M) 3.544ms "
                + "User=0.01s Sys=0.01s Real=0.01s";
        assertTrue(UnifiedParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_PAR_NEW.toString() + ".");
        UnifiedParNewEvent event = new UnifiedParNewEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_PAR_NEW, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 49, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(974), event.getYoungOccupancyInit(), "Young initial occupancy not parsed correctly.");
        assertEquals(kilobytes(128), event.getYoungOccupancyEnd(), "Young end occupancy not parsed correctly.");
        assertEquals(kilobytes(1152), event.getYoungSpace(), "Young space size not parsed correctly.");
        assertEquals(kilobytes(0), event.getOldOccupancyInit(), "Old initial occupancy not parsed correctly.");
        assertEquals(kilobytes(518), event.getOldOccupancyEnd(), "Old end occupancy not parsed correctly.");
        assertEquals(kilobytes(960), event.getOldSpace(), "Old space size not parsed correctly.");
        assertEquals(kilobytes(250), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(250), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(3544, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(1, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(200, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTriggerGCLocaterInitiateGC() {
        String logLine = "[2022-10-23T10:40:35.421+0200] GC(2) Pause Young (GCLocker Initiated GC) ParNew: "
                + "596352K->18048K(596352K) CMS: 69520K->113365K(1482752K) Metaspace: 94138K->94138K(604160K) "
                + "650M->128M(2030M) 102,315ms User=0,18s Sys=0,02s Real=0,10s";
        assertEquals(JdkUtil.EventType.UNIFIED_PAR_NEW,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_PAR_NEW + "not identified.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_PAR_NEW);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_PAR_NEW.toString() + " not indentified as unified.");
    }
}
