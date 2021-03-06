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
public class TestUnifiedBlankLineEvent {

    @Test
    public void testIdentityEventType() {
        String logLine = "[69.946s][info][gc,stats     ]";
        assertEquals(JdkUtil.LogEventType.UNIFIED_BLANK_LINE + "not identified.",
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testReportable() {
        String logLine = "[69.946s][info][gc,stats     ]";
        assertFalse(JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_BLANK_LINE);
        assertTrue(JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLineUnifiedFooterStats() {
        String logLine = "[69.946s][info][gc,stats     ]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }

    @Test
    public void testLineUnifiedFooterHeap() {
        String logLine = "[69.946s][info][gc,heap,exit ]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }

    @Test
    public void testLineTimeUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }

    @Test
    public void testLineUptimeMillis() {
        String logLine = "[1357910ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]   ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }
}
