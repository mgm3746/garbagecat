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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestParallelSerialOldEvent {

    @Test
    void test() {
        String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] "
                + "[PSOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) "
                + "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]";
        assertTrue(ParallelSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.PARALLEL_SERIAL_OLD.toString() + ".");
        ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
        assertEquals((long) 3600, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(5424), event.getYoungOccupancyInit(), "Young initial occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end occupancy not parsed correctly.");
        assertEquals(kilobytes(38208), event.getYoungSpace(), "Young space size not parsed correctly.");
        assertEquals(kilobytes(488), event.getOldOccupancyInit(), "Old initial occupancy not parsed correctly.");
        assertEquals(kilobytes(5786), event.getOldOccupancyEnd(), "Old end occupancy not parsed correctly.");
        assertEquals(kilobytes(87424), event.getOldSpace(), "Old space size not parsed correctly.");
        assertEquals(kilobytes(13092), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(13094), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(131072), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(69936, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testDatestamp() {
        String logLine = "2018-12-06T19:04:46.807-0500: [Full GC (Ergonomics) [PSYoungGen: 508K->385K(1536K)] "
                + "[PSOldGen: 408K->501K(2048K)] 916K->887K(3584K), "
                + "[Metaspace: 3680K->3680K(1056768K)], 0.0030057 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]";
        assertTrue(ParallelSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.PARALLEL_SERIAL_OLD.toString() + ".");
        ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
        assertEquals(597438286807L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testJdk16() {
        String logLine = "4.165: [Full GC (System) [PSYoungGen: 1784K->0K(12736K)] "
                + "[PSOldGen: 1081K->2855K(116544K)] 2865K->2855K(129280K) "
                + "[PSPermGen: 8600K->8600K(131072K)], 0.0427680 secs]";
        assertTrue(ParallelSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.PARALLEL_SERIAL_OLD.toString() + ".");
        ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
        assertEquals((long) 4165, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.SYSTEM_GC,
                "Trigger not recognized as " + GcTrigger.SYSTEM_GC.toString() + ".");
        assertEquals(kilobytes(1784), event.getYoungOccupancyInit(), "Young initial occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end occupancy not parsed correctly.");
        assertEquals(kilobytes(12736), event.getYoungSpace(), "Young space size not parsed correctly.");
        assertEquals(kilobytes(1081), event.getOldOccupancyInit(), "Old initial occupancy not parsed correctly.");
        assertEquals(kilobytes(2855), event.getOldOccupancyEnd(), "Old end occupancy not parsed correctly.");
        assertEquals(kilobytes(116544), event.getOldSpace(), "Old space size not parsed correctly.");
        assertEquals(kilobytes(8600), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(8600), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(131072), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(42768, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testTimestamp() {
        String logLine = "0.122: [Full GC (Ergonomics) [PSYoungGen: 508K->385K(1536K)] "
                + "[PSOldGen: 408K->501K(2048K)] 916K->887K(3584K), "
                + "[Metaspace: 3680K->3680K(1056768K)], 0.0030057 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]";
        assertTrue(ParallelSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.PARALLEL_SERIAL_OLD.toString() + ".");
        ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
        assertEquals((long) 122, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTriggerErgonomicsWithMetaspace() {
        String logLine = "2018-12-06T19:04:46.807-0500: 0.122: [Full GC (Ergonomics) [PSYoungGen: 508K->385K(1536K)] "
                + "[PSOldGen: 408K->501K(2048K)] 916K->887K(3584K), "
                + "[Metaspace: 3680K->3680K(1056768K)], 0.0030057 secs] [Times: user=0.01 sys=0.00, real=0.00 secs]";
        assertTrue(ParallelSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.PARALLEL_SERIAL_OLD.toString() + ".");
        ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
        assertEquals((long) 122, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.ERGONOMICS,
                "Trigger not recognized as " + GcTrigger.ERGONOMICS.toString() + ".");
        assertEquals(kilobytes(508), event.getYoungOccupancyInit(), "Young initial occupancy not parsed correctly.");
        assertEquals(kilobytes(385), event.getYoungOccupancyEnd(), "Young end occupancy not parsed correctly.");
        assertEquals(kilobytes(1536), event.getYoungSpace(), "Young space size not parsed correctly.");
        assertEquals(kilobytes(408), event.getOldOccupancyInit(), "Old initial occupancy not parsed correctly.");
        assertEquals(kilobytes(501), event.getOldOccupancyEnd(), "Old end occupancy not parsed correctly.");
        assertEquals(kilobytes(2048), event.getOldSpace(), "Old space size not parsed correctly.");
        assertEquals(kilobytes(3680), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(3680), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(3005, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(1, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(Integer.MAX_VALUE, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testWhiteSpaceAtEnd() {
        String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] "
                + "[PSOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) "
                + "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]  ";
        assertTrue(ParallelSerialOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.PARALLEL_SERIAL_OLD.toString() + ".");
    }
}
