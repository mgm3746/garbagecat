/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.junit.jupiter.api.Test;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestG1CleanupEvent {

    @Test
    void testCleanup() {
        String logLine = "18.650: [GC cleanup 297M->236M(512M), 0.0014690 secs]";
        assertTrue(G1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".");
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        assertEquals((long) 18650, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(304128), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(241664), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(524288), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(1469, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testCleanupWhiteSpacesAtEnd() {
        String logLine = "18.650: [GC cleanup 297M->236M(512M), 0.0014690 secs]   ";
        assertTrue(G1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".");
    }

    @Test
    void testIsBlocking() {
        String logLine = "2972.698: [GC cleanup 13G->12G(30G), 0.0358748 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.G1_CLEANUP.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLineDatestamp() {
        String logLine = "2021-10-26T09:58:12.091-0400: [GC cleanup 3014K->3014K(6144K), 0.0001830 secs]";
        assertTrue(G1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".");
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        assertEquals(688553892091L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(3014), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(3014), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(6144), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(183, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineDetailsDatestamp() {
        String logLine = "2020-04-29T22:05:39.708+0200: [GC cleanup 1745.419: [G1Ergonomics "
                + "(Concurrent Cycles) finish cleanup, occupancy: 22498457048 bytes, capacity: 32212254720 bytes, "
                + "known garbage: 9291782792 bytes (28.85 %)]21456M->20543M(30720M), 0.0155840 secs] "
                + "[Times: user=0.07 sys=0.06, real=0.01 secs]";
        assertTrue(G1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".");
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        assertEquals(641487939708L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineGigabytes() {
        String logLine = "2972.698: [GC cleanup 13G->12G(30G), 0.0358748 secs]";
        assertTrue(G1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".");
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        assertEquals((long) 2972698, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(13631488), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(12582912), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(31457280), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(35874, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineMissingSizes() {
        String logLine = "2017-05-09T00:46:14.766+1000: 288368.997: [GC cleanup, 0.0000910 secs] "
                + "[Times: user=0.00 sys=0.00, real=0.00 secs]";
        assertTrue(G1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".");
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        assertEquals((long) 288368997, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(0), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(91, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineMixedErgonomics() {
        String logLine = "2020-04-29T22:05:39.708+0200: 1745.417: [GC cleanup 1745.419: [G1Ergonomics "
                + "(Concurrent Cycles) finish cleanup, occupancy: 22498457048 bytes, capacity: 32212254720 bytes, "
                + "known garbage: 9291782792 bytes (28.85 %)]21456M->20543M(30720M), 0.0155840 secs] "
                + "[Times: user=0.07 sys=0.06, real=0.01 secs]";
        assertTrue(G1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".");
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        assertEquals((long) 1745417, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(21456 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(20543 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(30720 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(15584, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(7, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(6, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(1, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(1300, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineWithTimesData() {
        String logLine = "2016-11-08T09:36:22.388-0800: 35290.131: [GC cleanup 5252M->3592M(12G), 0.0154490 secs] "
                + "[Times: user=0.19 sys=0.00, real=0.01 secs]";
        assertTrue(G1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".");
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        assertEquals((long) 35290131, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(5252 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(3592 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(12 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(15449, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(19, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(1, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(1900, event.getParallelism(), "Parallelism not calculated correctly.");
    }
}
