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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedG1CleanupEvent {

    @Test
    void testLogLine() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (15101 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(30 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(44 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(0, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedG1CleanupEvent,
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " not parsed.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " not indentified as blocking.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP),
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_CLEANUP);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms     ";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + ".");
    }

    @Test
    void testLogLinePreprocessed() {
        String logLine = "[16.082s][info][gc            ] GC(969) Pause Cleanup 28M->28M(46M) 0.064ms "
                + "User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (16082 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(28 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(28 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(46 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(0, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedUptimeMillis() {
        String logLine = "[2021-05-25T08:46:20.294-0400][1191010697ms] GC(14942) Pause Cleanup 233M->233M(512M) "
                + "0.496ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1CleanupEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + ".");
        UnifiedG1CleanupEvent event = new UnifiedG1CleanupEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (1191010697 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(233 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(233 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(512 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(0, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }
}
