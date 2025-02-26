/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestZConcurrentEvent {

    @Test
    void testGarbageCollectionTriggerAllocationStallNoTime() {
        String logLine = "[2024-11-25T17:18:20.331-0500] GC(3) Garbage Collection (Allocation Stall) "
                + "96M(100%)->24M(25%)";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
    }

    @Test
    void testGarbageCollectionTriggerWarmupNoTime() {
        String logLine = "[1.234s] GC(0) Garbage Collection (Warmup) 20M(21%)->16M(17%)";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (1234 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(20), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(16), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(94), event.getCombinedSpace(), "Combined space size not parsed correctly.");
    }

    @Test
    void testMajorCollectionTriggerMetadataGcThreshold() {
        String logLine = "[18.668s][info ][gc                      ] GC(0) Major Collection (Metadata GC Threshold) "
                + "460M(2%)->892M(4%) 0.478s";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (18668 - 478), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(460), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(892), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(22300), event.getCombinedSpace(), "Combined space size not parsed correctly.");
    }

    @Test
    void testMajorCollectionTriggerProactive() {
        String logLine = "[2024-11-19T04:02:29.854+0800][1508.445s][info ][gc                      ] GC(131) Major "
                + "Collection (Proactive) 15120M(64%)->12048M(51%) 17.390s";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (1508445 - 17390), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(15120), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(12048), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(23624), event.getCombinedSpace(), "Combined space size not parsed correctly.");
    }

    @Test
    void testMetaspaceDatestamp() {
        String logLine = "[2024-11-19T04:02:29.854+0800][info ][gc,metaspace            ] GC(131) O: "
                + "Metaspace: 55M used, 58M committed, 1088M reserved";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals(785257349854L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testMetaspaceGenerationalOld() {
        String logLine = "[0.228s][info][gc,metaspace] GC(2) O: Metaspace: 2M used, 3M committed, 1088M reserved";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 228, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(megabytes(2), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(megabytes(3), event.getClassSpace(), "Class space size not parsed correctly.");
    }

    @Test
    void testMetaspaceGenerationalYoung() {
        String logLine = "[0.104s][info][gc,metaspace] GC(0) Y: Metaspace: 0M used, 0M committed, 1088M reserved";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 104, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(megabytes(0), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(megabytes(0), event.getClassSpace(), "Class space size not parsed correctly.");
    }

    @Test
    void testMetaspaceGenerationalYoungLowerCaseY() {
        String logLine = "[0.315s][info][gc,metaspace] GC(3) y: Metaspace: 3M used, 4M committed, 1088M reserved";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 315, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(megabytes(3), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(megabytes(4), event.getClassSpace(), "Class space size not parsed correctly.");
    }

    @Test
    void testMetaspaceIdentityEventType() {
        String logLine = "[2024-11-19T04:02:29.854+0800][1508.444s][info ][gc,metaspace            ] GC(131) O: "
                + "Metaspace: 55M used, 58M committed, 1088M reserved";
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.Z_CONCURRENT + "not identified.");
    }

    @Test
    void testMetaspaceNonGenerational() {
        String logLine = "[0.275s] GC(2) Metaspace: 3M used, 3M committed, 1032M reserved";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 275, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(Memory.ZERO, event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(megabytes(3), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(megabytes(3), event.getClassSpace(), "Class space size not parsed correctly.");
    }

    @Test
    void testMetaspaceTimestamp() {
        String logLine = "[1508.444s][info ][gc,metaspace            ] GC(131) O: "
                + "Metaspace: 55M used, 58M committed, 1088M reserved";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) 1508444, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testMinorCollectionTriggerAllocationRate() {
        String logLine = "[0.316s][info][gc          ] GC(3) Minor Collection (Allocation Rate) 60M(62%)->62M(65%) "
                + "0.020s";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (316 - 20), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(60), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(62), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(95), event.getCombinedSpace(), "Combined space size not parsed correctly.");
    }

    @Test
    void testMinorCollectionTriggerHighUsage() {
        String logLine = "[196.626s][info ][gc                      ] GC(122) Minor Collection (High Usage) "
                + "21366M(91%)->11576M(49%) 2.737s";
        assertTrue(ZConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_CONCURRENT.toString() + ".");
        ZConcurrentEvent event = new ZConcurrentEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_CONCURRENT, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (196626 - 2737), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(21366), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(11576), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(23624), event.getCombinedSpace(), "Combined space size not parsed correctly.");
    }
}
