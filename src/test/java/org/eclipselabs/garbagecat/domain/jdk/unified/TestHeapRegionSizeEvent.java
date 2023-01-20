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
class TestHeapRegionSizeEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.003s][info][gc,heap] Heap region size: 1M";
        assertEquals(JdkUtil.LogEventType.HEAP_REGION_SIZE, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.HEAP_REGION_SIZE + "not identified.");
    }

    @Test
    void testLine() {
        String logLine = "[0.003s][info][gc,heap] Heap region size: 1M";
        assertTrue(HeapRegionSizeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.003s][info][gc,heap] Heap region size: 1M";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof HeapRegionSizeEvent,
                JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.HEAP_REGION_SIZE),
                JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.HEAP_REGION_SIZE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " not indentified as unified.");
    }
}
