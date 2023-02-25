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
class TestUsingZEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.018s][info][gc     ] Using The Z Garbage Collector";
        assertEquals(JdkUtil.LogEventType.USING_Z, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.USING_Z + "not identified.");
    }

    @Test
    void testLine() {
        String logLine = "[0.018s][info][gc     ] Using The Z Garbage Collector";
        assertTrue(UsingZEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_Z.toString() + ".");
        UsingZEvent event = new UsingZEvent(logLine);
        assertEquals((long) 18, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.018s][info][gc     ] Using The Z Garbage Collector    ";
        assertTrue(UsingZEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_Z.toString() + ".");
    }

    @Test
    void testLineWithTimeUptimemillis() {
        String logLine = "[2021-11-25T14:47:31.092-0200][18ms][info][gc     ] Using The Z Garbage Collector";
        assertTrue(UsingZEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_Z.toString() + ".");
        UsingZEvent event = new UsingZEvent(logLine);
        assertEquals((long) 18, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.018s][info][gc     ] Using The Z Garbage Collector";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.USING_Z.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.018s][info][gc     ] Using The Z Garbage Collector";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UsingZEvent,
                JdkUtil.LogEventType.USING_Z.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.USING_Z),
                JdkUtil.LogEventType.USING_Z.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_Z);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.USING_Z.toString() + " not indentified as unified.");
    }
}
