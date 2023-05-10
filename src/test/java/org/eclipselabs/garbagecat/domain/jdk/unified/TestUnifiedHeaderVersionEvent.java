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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
class TestUnifiedHeaderVersionEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER_VERSION, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_HEADER_VERSION + "not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.GC_INFO + "not identified.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)   ";
        assertTrue(UnifiedHeaderVersionEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER_VERSION.toString() + ".");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.GC_INFO + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertTrue(UnifiedHeaderVersionEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER_VERSION.toString() + ".");
        UnifiedHeaderVersionEvent event = new UnifiedHeaderVersionEvent(logLine);
        assertEquals((long) 13, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(17, event.getJdkVersionMajor(), "JDK major version not correct.");
        assertEquals(1, event.getJdkVersionMinor(), "JDK minor version not correct.");
        assertEquals("17.0.1+12-LTS", event.getJdkReleaseString(), "JDK release string not correct.");
        assertNotEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.UNIFIED_HEADER_VERSION.toString() + " incorrectly indentified as blocking.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.GC_INFO + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedHeaderVersionEvent,
                JdkUtil.LogEventType.UNIFIED_HEADER_VERSION.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_HEADER_VERSION),
                JdkUtil.LogEventType.UNIFIED_HEADER_VERSION.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testSingleDigitAfterPlusSign() {
        String logLine = "[2023-05-10T09:00:58.258-0400] Version: 17.0.7+7-LTS (release)";
        assertTrue(UnifiedHeaderVersionEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER_VERSION.toString() + ".");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.GC_INFO + "not identified.");
    }

    @Test
    void testTimeUptime() {
        String logLine = "[2021-03-09T14:45:02.441-0300][12.082s] Version: 17.0.1+12-LTS (release)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER_VERSION, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_HEADER_VERSION + "not identified.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_HEADER_VERSION);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_HEADER_VERSION.toString() + " not indentified as unified.");
    }

}
