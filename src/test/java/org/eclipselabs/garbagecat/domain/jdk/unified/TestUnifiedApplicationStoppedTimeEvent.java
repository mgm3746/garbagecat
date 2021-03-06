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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedApplicationStoppedTimeEvent {

    @Test
    public void testLogLine() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + ".",
                UnifiedApplicationStoppedTimeEvent.match(logLine));
        UnifiedApplicationStoppedTimeEvent event = new UnifiedApplicationStoppedTimeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 31, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 64, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds";
        assertEquals(JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME + "not identified.",
                JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds";
        assertTrue(JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedApplicationStoppedTimeEvent);
    }

    @Test
    public void testNotBlocking() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds";
        assertFalse(
                JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString()
                        + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_APPLICATION_STOPPED_TIME);
        assertTrue(JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWithSpacesAtEnd() {
        String logLine = "[0.031s][info][safepoint    ] Total time for which application threads were stopped: "
                + "0.0000643 seconds, Stopping threads took: 0.0000148 seconds   ";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + ".",
                UnifiedApplicationStoppedTimeEvent.match(logLine));
    }
}
