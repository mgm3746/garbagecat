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
public class TestHeapRegionSizeEvent {

    @Test
    public void testLine() {
        String logLine = "[0.003s][info][gc,heap] Heap region size: 1M";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + ".",
                HeapRegionSizeEvent.match(logLine));
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.003s][info][gc,heap] Heap region size: 1M";
        assertEquals(JdkUtil.LogEventType.HEAP_REGION_SIZE + "not identified.", JdkUtil.LogEventType.HEAP_REGION_SIZE,
                JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.003s][info][gc,heap] Heap region size: 1M";
        assertTrue(JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof HeapRegionSizeEvent);
    }

    @Test
    public void testNotBlocking() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertFalse(JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertFalse(JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.HEAP_REGION_SIZE));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.HEAP_REGION_SIZE);
        assertTrue(JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUptimeMillis() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Regions: 2606 x 512K";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + ".",
                HeapRegionSizeEvent.match(logLine));
    }
}
