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
class TestUnifiedSerialNewEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.EventType.UNIFIED_SERIAL_NEW,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_SERIAL_NEW + "not identified.");
    }

    @Test
    void testJdk17() {
        String logLine = "[0.035s][info][gc,start    ] GC(0) Pause Young (Allocation Failure) DefNew: "
                + "1022K(1152K)->127K(1152K) Tenured: 0K(768K)->552K(768K) Metaspace: 155K(256K)->155K(256K) "
                + "0M->0M(1M) 0.937ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_NEW.toString() + ".");
        UnifiedSerialNewEvent event = new UnifiedSerialNewEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_SERIAL_NEW, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 35, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLine7SpacesAfterStart() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure) DefNew: "
                + "1016K->128K(1152K) Tenured: 929K->1044K(1552K) Metaspace: 1222K->1222K(1056768K) 1M->1M(2M) "
                + "0.700ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_NEW.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s   ";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_NEW.toString() + ".");
    }

    /**
     * Test with uptime decorator.
     */
    @Test
    void testMillis() {
        String logLine = "[3ms][gc,start] GC(6) Pause Young (Allocation Failure) DefNew: 1016K->128K(1152K) "
                + "Tenured: 929K->1044K(1552K) Metaspace: 1222K->1222K(1056768K) 1M->1M(2M) 0.700ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_NEW.toString() + ".");
        UnifiedSerialNewEvent event = new UnifiedSerialNewEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_SERIAL_NEW, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 3, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedSerialNewEvent,
                JdkUtil.EventType.UNIFIED_SERIAL_NEW.toString() + " not parsed.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) "
                + "DefNew: 983K->128K(1152K) Tenured: 0K->458K(768K) Metaspace: 246K->246K(1056768K) 0M->0M(1M) "
                + "1.393ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedSerialNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SERIAL_NEW.toString() + ".");
        UnifiedSerialNewEvent event = new UnifiedSerialNewEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_SERIAL_NEW, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 41, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(983), event.getYoungOccupancyInit(), "Young initial occupancy not parsed correctly.");
        assertEquals(kilobytes(128), event.getYoungOccupancyEnd(), "Young end occupancy not parsed correctly.");
        assertEquals(kilobytes(1152), event.getYoungSpace(), "Young space size not parsed correctly.");
        assertEquals(kilobytes(0), event.getOldOccupancyInit(), "Old initial occupancy not parsed correctly.");
        assertEquals(kilobytes(458), event.getOldOccupancyEnd(), "Old end occupancy not parsed correctly.");
        assertEquals(kilobytes(768), event.getOldSpace(), "Old space size not parsed correctly.");
        assertEquals(kilobytes(246), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(246), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(1393, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_SERIAL_NEW);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_SERIAL_NEW.toString() + " not indentified as unified.");
    }
}
