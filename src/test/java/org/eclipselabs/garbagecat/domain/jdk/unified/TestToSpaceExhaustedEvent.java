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
class TestToSpaceExhaustedEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[90271.764s][info][gc] GC(1437) To-space exhausted";
        assertEquals(JdkUtil.LogEventType.TO_SPACE_EXHAUSTED, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.TO_SPACE_EXHAUSTED + "not identified.");
    }

    @Test
    void testLine() {
        String logLine = "[90271.764s][info][gc] GC(1437) To-space exhausted";
        assertTrue(ToSpaceExhaustedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.TO_SPACE_EXHAUSTED.toString() + ".");
        ToSpaceExhaustedEvent event = new ToSpaceExhaustedEvent(logLine);
        assertEquals((long) 90271764, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[90271.764s][info][gc] GC(1437) To-space exhausted   ";
        assertTrue(ToSpaceExhaustedEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.TO_SPACE_EXHAUSTED.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[90271.764s][info][gc] GC(1437) To-space exhausted";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.TO_SPACE_EXHAUSTED.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[90271.764s][info][gc] GC(1437) To-space exhausted";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ToSpaceExhaustedEvent,
                JdkUtil.LogEventType.TO_SPACE_EXHAUSTED.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.TO_SPACE_EXHAUSTED),
                JdkUtil.LogEventType.TO_SPACE_EXHAUSTED.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.TO_SPACE_EXHAUSTED);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.TO_SPACE_EXHAUSTED.toString() + " not indentified as unified.");
    }
}
