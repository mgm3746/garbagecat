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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
class TestUnifiedApplicationStoppedTimeEvent {

    @Test
    void testLogLine() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds";
        assertTrue(UnifiedApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + ".");
        UnifiedApplicationStoppedTimeEvent event = new UnifiedApplicationStoppedTimeEvent(logLine);
        assertEquals((long) 31,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(64,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds";
        assertEquals(JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME,JdkUtil.identifyEventType(logLine),JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedApplicationStoppedTimeEvent, JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + " not parsed.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString()
		+ " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME), JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_APPLICATION_STOPPED_TIME);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes), JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLineWithSpacesAtEnd() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds   ";
        assertTrue(UnifiedApplicationStoppedTimeEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + ".");
    }
}
