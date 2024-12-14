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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedG1CleanupEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertEquals(JdkUtil.EventType.UNIFIED_G1_CLEANUP,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_G1_CLEANUP + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_CLEANUP, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (15101 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(44 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessed() {
        String logLine = "[16.082s][info][gc            ] GC(969) Pause Cleanup 28M->28M(46M) 0.064ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_CLEANUP, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (16082 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(28 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(28 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(46 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedUptimeMillis() {
        String logLine = "[2021-05-25T08:46:20.294-0400][1191010697ms] GC(14942) Pause Cleanup 233M->233M(512M) "
                + "0.496ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(JdkUtil.EventType.UNIFIED_G1_CLEANUP, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (1191010697 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(233 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(233 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(512 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms     ";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedG1CleanupEvent,
                JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_G1_CLEANUP),
                JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestampUptime() {
        String logLine = "[3.161s] GC(4) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(44 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testTimestampUptimeMillis() {
        String logLine = "[3161ms] GC(4) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(44 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_G1_CLEANUP);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + " not indentified as unified.");
    }

    @Test
    void testUnifiedTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400] GC(4) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(746241357862L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(44 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUnifiedTimeUptime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3.161s] GC(4) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(44 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUnifiedTimeUptimeMillis() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms] GC(4) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(44 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
    }
}
