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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
class TestUnifiedHeapDumpAfterFullGcEvent {

    @Test
    void testBeforeGc() {
        String logLine = "[2024-12-06T05:57:30.303-0500] GC(0) Heap Dump (after full gc) 5.254ms";
        assertEquals(JdkUtil.EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC + " not identified.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2024-12-06T05:57:30.303-0500] GC(0) Heap Dump (after full gc) 5.254ms";
        assertEquals(JdkUtil.EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2024-12-06T05:57:30.303-0500] GC(0) Heap Dump (after full gc) 5.254ms";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedHeapDumpAfterFullGcEvent,
                JdkUtil.EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC),
                JdkUtil.EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC.toString()
                        + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<>();
        eventTypes.add(EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_HEAP_DUMP_AFTER_FULL_GC.toString() + " incorrectly indentified as unified.");
    }
}
