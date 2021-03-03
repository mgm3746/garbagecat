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
public class TestHeapAddressEvent {

    @Test
    public void testLogLine() {
        String logLine = "[0.004s][info][gc,heap,coops] Heap address: 0x00000000fc000000, size: 64 MB, "
                + "Compressed Oops mode: 32-bit";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_ADDRESS.toString() + ".",
                HeapAddressEvent.match(logLine));
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.004s][info][gc,heap,coops] Heap address: 0x00000000fc000000, size: 64 MB, "
                + "Compressed Oops mode: 32-bit";
        assertEquals(JdkUtil.LogEventType.HEAP_ADDRESS + "not identified.", JdkUtil.LogEventType.HEAP_ADDRESS,
                JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.004s][info][gc,heap,coops] Heap address: 0x00000000fc000000, size: 64 MB, "
                + "Compressed Oops mode: 32-bit";
        assertTrue(JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof HeapAddressEvent);
    }

    @Test
    public void testNotBlocking() {
        String logLine = "[0.004s][info][gc,heap,coops] Heap address: 0x00000000fc000000, size: 64 MB, "
                + "Compressed Oops mode: 32-bit";
        assertFalse(JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertFalse(JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.HEAP_ADDRESS));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.HEAP_ADDRESS);
        assertTrue(JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testCompressedOops() {
        String logLine = "[0.019s][info][gc,heap,coops] Heap address: 0x00000006c2800000, size: 4056 MB, "
                + "Compressed Oops mode: Zero based, Oop shift amount: 3";
        assertTrue(JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof HeapAddressEvent);
    }

    @Test
    public void testTimeUptimemillis() {
        String logLine = "[2019-02-05T14:47:31.092-0200][4ms] Heap address: 0x00000000ae900000, size: 1303 MB, "
                + "Compressed Oops mode: 32-bit";
        assertTrue(JdkUtil.LogEventType.HEAP_ADDRESS.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof HeapAddressEvent);
    }
}
