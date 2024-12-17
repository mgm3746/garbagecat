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
class TestZMarkStartYoungEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.296s][info][gc,phases   ] GC(3) y: Pause Mark Start 0.010ms";
        assertEquals(JdkUtil.EventType.Z_MARK_START_YOUNG,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.Z_MARK_START_YOUNG + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.296s][info][gc,phases   ] GC(3) y: Pause Mark Start 0.010ms";
        assertTrue(ZMarkStartYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_MARK_START_YOUNG.toString() + ".");
        ZMarkStartYoungEvent event = new ZMarkStartYoungEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_MARK_START_YOUNG, event.getEventType(), "Event type incorrect.");
        assertEquals((long) (296 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(10, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.296s][info][gc,phases   ] GC(3) y: Pause Mark Start 0.010ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ZMarkStartYoungEvent,
                JdkUtil.EventType.Z_MARK_START_YOUNG.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.Z_MARK_START_YOUNG),
                JdkUtil.EventType.Z_MARK_START_YOUNG.toString() + " not indentified as reportable.");
    }

    /**
     * Test with time, uptime decorator.
     */
    @Test
    void testTimestampTimeUptime() {
        String logLine = "[2021-03-09T14:45:02.441-0300][0.296s][info][gc,phases   ] GC(3) y: Pause Mark Start 0.010ms";
        assertTrue(ZMarkStartYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_MARK_START_YOUNG.toString() + ".");
        ZMarkStartYoungEvent event = new ZMarkStartYoungEvent(logLine);
        assertEquals(JdkUtil.EventType.Z_MARK_START_YOUNG, event.getEventType(), "Event type incorrect.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.Z_MARK_START_YOUNG);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.Z_MARK_START_YOUNG.toString() + " not indentified as unified.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[0.296s][info][gc,phases   ] GC(3) y: Pause Mark Start 0.010ms   ";
        assertTrue(ZMarkStartYoungEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.Z_MARK_START_YOUNG.toString() + ".");
    }
}
