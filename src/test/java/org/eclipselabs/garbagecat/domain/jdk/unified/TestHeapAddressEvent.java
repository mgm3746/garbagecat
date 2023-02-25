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
class TestHeapAddressEvent {

    @Test
    void testCompressedOops() {
        String logLine = "[0.019s][info][gc,heap,coops] Heap address: 0x00000006c2800000, size: 4056 MB, "
                + "Compressed Oops mode: Zero based, Oop shift amount: 3";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof HeapAddressEvent,
                JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.004s][info][gc,heap,coops] Heap address: 0x00000000fc000000, size: 64 MB, "
                + "Compressed Oops mode: 32-bit";
        assertEquals(JdkUtil.LogEventType.HEAP_ADDRESS, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.HEAP_ADDRESS + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.004s][info][gc,heap,coops] Heap address: 0x00000000fc000000, size: 64 MB, "
                + "Compressed Oops mode: 32-bit";
        assertTrue(HeapAddressEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_ADDRESS.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.004s][info][gc,heap,coops] Heap address: 0x00000000fc000000, size: 64 MB, "
                + "Compressed Oops mode: 32-bit";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.004s][info][gc,heap,coops] Heap address: 0x00000000fc000000, size: 64 MB, "
                + "Compressed Oops mode: 32-bit";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof HeapAddressEvent,
                JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.HEAP_ADDRESS),
                JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testTimeUptimemillis() {
        String logLine = "[2019-02-05T14:47:31.092-0200][4ms] Heap address: 0x00000000ae900000, size: 1303 MB, "
                + "Compressed Oops mode: 32-bit";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof HeapAddressEvent,
                JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " not parsed.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.HEAP_ADDRESS);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " not indentified as unified.");
    }
}
