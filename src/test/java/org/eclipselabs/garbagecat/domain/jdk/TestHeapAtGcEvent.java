/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestHeapAtGcEvent {

    @Test
    void testBraceLine() {
        String logLine = "}";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testHeapAfterGcInvocations() {
        String logLine = "Heap after GC invocations=15661 (full 26):";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEAP_AT_GC.toString() + ".");
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof HeapAtGcEvent,
                "JdkUtil.parseLogLine() does not return " + JdkUtil.EventType.HEAP_AT_GC.toString() + " event.");
    }

    @Test
    void testHeapAfterLowerCaseGcLine() {
        String logLine = "Heap after gc invocations=362:";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testHeapAfterPrintGCDateStampsLine() {
        String logLine = "Heap after GC invocations=1 (full 0):";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testHeapBeforeLowerCaseGcLine() {
        String logLine = "{Heap before gc invocations=1:";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testHeapBeforePrintGCDateStampsLine() {
        String logLine = "{Heap before GC invocations=0 (full 0):";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testHeapBeforeUpperCaseGcFullLine() {
        String logLine = "{Heap before GC invocations=261 (full 10):";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testNotReportable() {
        String logLine = "{Heap before gc invocations=1:";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.EventType.HEAP_AT_GC.toString() + " incorrectly indentified as reportable.");
    }
}
