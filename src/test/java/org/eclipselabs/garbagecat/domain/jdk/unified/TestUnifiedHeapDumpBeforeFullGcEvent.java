/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
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
class TestUnifiedHeapDumpBeforeFullGcEvent {

    @Test
    void testBeforeGc() {
        String logLine = "[2024-12-06T10:15:56.126-0500] GC(0) Heap Dump (before full gc) 7.667ms";
        assertEquals(JdkUtil.EventType.UNIFIED_HEAP_DUMP_BEFORE_FULL_GC,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_HEAP_DUMP_BEFORE_FULL_GC + " not identified.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2024-12-06T10:15:56.126-0500] GC(0) Heap Dump (before full gc) 7.667ms";
        assertEquals(JdkUtil.EventType.UNIFIED_HEAP_DUMP_BEFORE_FULL_GC,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_HEAP_DUMP_BEFORE_FULL_GC + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2024-12-06T10:15:56.126-0500] GC(0) Heap Dump (before full gc) 7.667ms";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null,
                        CollectorFamily.UNKNOWN) instanceof UnifiedHeapDumpBeforeFullGcEvent,
                JdkUtil.EventType.UNIFIED_HEAP_DUMP_BEFORE_FULL_GC.toString() + " not parsed.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<>();
        eventTypes.add(EventType.UNIFIED_HEAP_DUMP_BEFORE_FULL_GC);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_HEAP_DUMP_BEFORE_FULL_GC.toString() + " incorrectly indentified as unified.");
    }
}
