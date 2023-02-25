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
class TestUnifiedG1InfoEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[2.726s][info][gc,start     ] GC(51) Pause Initial Mark (G1 Humongous Allocation)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_INFO, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.UNIFIED_G1_INFO + "not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "[2.726s][info][gc,start     ] GC(51) Pause Initial Mark (G1 Humongous Allocation)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + " indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "[2.726s][info][gc,start     ] GC(51) Pause Initial Mark (G1 Humongous Allocation)";
        assertTrue(UnifiedG1InfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + ".");
    }

    @Test
    void testLogLine6Spaces() {
        String logLine = "[2.751s][info][gc,start      ] GC(53) Pause Initial Mark (G1 Humongous Allocation)";
        assertTrue(UnifiedG1InfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2.726s][info][gc,start     ] GC(51) Pause Initial Mark (G1 Humongous Allocation)";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof UnifiedG1InfoEvent,
                JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_INFO),
                JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + " indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_INFO);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + " not indentified as unified.");
    }
}
