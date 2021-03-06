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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedG1InfoEvent {

    @Test
    public void testLogLine() {
        String logLine = "[2.726s][info][gc,start     ] GC(51) Pause Initial Mark (G1 Humongous Allocation)";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + ".",
                UnifiedG1InfoEvent.match(logLine));
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[2.726s][info][gc,start     ] GC(51) Pause Initial Mark (G1 Humongous Allocation)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_G1_INFO + "not identified.",
                JdkUtil.LogEventType.UNIFIED_G1_INFO, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[2.726s][info][gc,start     ] GC(51) Pause Initial Mark (G1 Humongous Allocation)";
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedG1InfoEvent);
    }

    @Test
    public void testIsBlocking() {
        String logLine = "[2.726s][info][gc,start     ] GC(51) Pause Initial Mark (G1 Humongous Allocation)";
        assertFalse(JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + " indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertFalse(JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + " indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_G1_INFO));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_G1_INFO);
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLine6Spaces() {
        String logLine = "[2.751s][info][gc,start      ] GC(53) Pause Initial Mark (G1 Humongous Allocation)";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_G1_INFO.toString() + ".",
                UnifiedG1InfoEvent.match(logLine));
    }
}
