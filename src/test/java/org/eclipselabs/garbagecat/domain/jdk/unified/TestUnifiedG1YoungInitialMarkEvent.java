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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedG1YoungInitialMarkEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".");
        UnifiedG1YoungInitialMarkEvent event = new UnifiedG1YoungInitialMarkEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString(), event.getName(),
                "Event name incorrect.");
        assertEquals((long) (2752 - 1), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.G1_HUMONGOUS_ALLOCATION, "Trigger not parsed correctly.");
        assertEquals(kilobytes(562 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(5 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(1250 * 1024), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(1212, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLine11Spaces() {
        String logLine = "[2.727s][info][gc           ] GC(51) Pause Initial Mark (G1 Humongous Allocation) "
                + "1162M->5M(1250M) 1.336ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedG1YoungInitialMarkEvent,
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not indentified as reportable.");
    }

    /**
     * Test with time, uptime decorator.
     */
    @Test
    void testTimestampTimeUptime() {
        String logLine = "[2021-03-09T14:45:02.441-0300][12.082s] GC(6) Pause Initial Mark (G1 Humongous Allocation) "
                + "1162M->5M(1250M) 1.336ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(UnifiedG1YoungInitialMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".");
        UnifiedG1YoungInitialMarkEvent event = new UnifiedG1YoungInitialMarkEvent(logLine);
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString(), event.getName(),
                "Event name incorrect.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not indentified as unified.");
    }
}
