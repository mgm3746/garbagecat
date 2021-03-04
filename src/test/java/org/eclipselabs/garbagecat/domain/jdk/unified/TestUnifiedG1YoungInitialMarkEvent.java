/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.junit.Test;

import static org.eclipselabs.garbagecat.Memory.kilobytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedG1YoungInitialMarkEvent {

    @Test
    public void testLogLine() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".",
                UnifiedG1YoungInitialMarkEvent.match(logLine));
        UnifiedG1YoungInitialMarkEvent event = new UnifiedG1YoungInitialMarkEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 2752 - 1, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_HUMONGOUS_ALLOCATION));
        assertEquals("Combined begin size not parsed correctly.", kilobytes(562 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(5 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(1250 * 1024), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 1212, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK + "not identified.",
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedG1YoungInitialMarkEvent);
    }

    @Test
    public void testIsBlocking() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK);
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[2.752s][info][gc            ] GC(53) Pause Initial Mark (G1 Humongous Allocation) "
                + "562M->5M(1250M) 1.212ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".",
                UnifiedG1YoungInitialMarkEvent.match(logLine));
    }

    @Test
    public void testLogLine11Spaces() {
        String logLine = "[2.727s][info][gc           ] GC(51) Pause Initial Mark (G1 Humongous Allocation) "
                + "1162M->5M(1250M) 1.336ms User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_YOUNG_INITIAL_MARK.toString() + ".",
                UnifiedG1YoungInitialMarkEvent.match(logLine));
    }
}
