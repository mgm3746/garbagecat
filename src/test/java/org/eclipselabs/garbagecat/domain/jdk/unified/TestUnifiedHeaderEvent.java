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
class TestUnifiedHeaderEvent {

    @Test
    void testLogLine() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        UnifiedHeaderEvent event = new UnifiedHeaderEvent(logLine);
        assertEquals((long) 13, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedHeaderEvent,
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " not parsed.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_HEADER);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " not indentified as unified.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)   ";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testTimeUptime() {
        String logLine = "[2021-03-09T14:45:02.441-0300][12.082s] Version: 17.0.1+12-LTS (release)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testCpus() {
        String logLine = "[0.013s][info][gc,init] CPUs: 12 total, 12 available";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testMemory() {
        String logLine = "[0.013s][info][gc,init] Memory: 31907M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testLargePageSupport() {
        String logLine = "[0.013s][info][gc,init] Large Page Support: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testNumaSupport() {
        String logLine = "[0.013s][info][gc,init] NUMA Support: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testCompressedOops() {
        String logLine = "[0.013s][info][gc,init] Compressed Oops: Enabled (32-bit)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapMinCapacity() {
        String logLine = "[0.013s][info][gc,init] Heap Min Capacity: 2M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapInitialCapacity() {
        String logLine = "[0.013s][info][gc,init] Heap Initial Capacity: 2M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapMaxCapacity() {
        String logLine = "[0.013s][info][gc,init] Heap Max Capacity: 64M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testPreTouch() {
        String logLine = "[0.013s][info][gc,init] Pre-touch: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testCdsArchivesMappedAt() {
        String logLine = "[0.013s][info][gc,metaspace] CDS archive(s) mapped at: "
                + "[0x0000000800000000-0x0000000800be2000-0x0000000800be2000), size 12460032, SharedBaseAddress: "
                + "0x0000000800000000, ArchiveRelocationMode: 0.";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testCompressedClassSpaceMappedAt() {
        String logLine = "[0.013s][info][gc,metaspace] Compressed class space mapped at: "
                + "0x0000000800c00000-0x0000000840c00000, reserved size: 1073741824";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testNarrowKlassBase() {
        String logLine = "[0.013s][info][gc,metaspace] Narrow klass base: 0x0000000800000000, Narrow klass shift: 0, "
                + "Narrow klass range: 0x100000000";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }
}
