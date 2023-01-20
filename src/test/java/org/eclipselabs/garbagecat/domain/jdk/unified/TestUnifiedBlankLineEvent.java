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
class TestUnifiedBlankLineEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[69.946s][info][gc,stats     ]";
        assertEquals(JdkUtil.LogEventType.UNIFIED_BLANK_LINE, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE + "not identified.");
    }

    @Test
    void testLineTimeUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]";
        assertTrue(UnifiedBlankLineEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".");
    }

    @Test
    void testLineUnifiedFooterHeap() {
        String logLine = "[69.946s][info][gc,heap,exit ]";
        assertTrue(UnifiedBlankLineEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".");
    }

    @Test
    void testLineUnifiedFooterStats() {
        String logLine = "[69.946s][info][gc,stats     ]";
        assertTrue(UnifiedBlankLineEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".");
    }

    @Test
    void testLineUptimeMillis() {
        String logLine = "[1357910ms]";
        assertTrue(UnifiedBlankLineEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]   ";
        assertTrue(UnifiedBlankLineEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".");
    }

    @Test
    void testReportable() {
        String logLine = "[69.946s][info][gc,stats     ]";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_BLANK_LINE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " not indentified as unified.");
    }
}
