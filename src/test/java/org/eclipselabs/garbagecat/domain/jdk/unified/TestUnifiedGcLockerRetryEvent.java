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
class TestUnifiedGcLockerRetryEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[2023-02-12T07:16:14.167+0200][warning][gc,alloc       ] ForkJoinPool-123-worker: Retried "
                + "waiting for GCLocker too often allocating 1235 words";
        assertEquals(JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY + "not identified.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[2023-02-12T07:16:14.167+0200][warning][gc,alloc       ] ForkJoinPool-123-worker: Retried "
                + "waiting for GCLocker too often allocating 1235 words  ";
        assertTrue(UnifiedGcLockerRetryEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2023-02-12T07:16:14.167+0200][warning][gc,alloc       ] ForkJoinPool-123-worker: Retried "
                + "waiting for GCLocker too often allocating 1235 words";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedGcLockerRetryEvent,
                JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY),
                JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestampTime() {
        String logLine = "[2023-02-12T07:16:14.167+0200][warning][gc,alloc       ] ForkJoinPool-123-worker: Retried "
                + "waiting for GCLocker too often allocating 1235 words";
        assertTrue(UnifiedGcLockerRetryEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY.toString() + ".");
        UnifiedGcLockerRetryEvent event = new UnifiedGcLockerRetryEvent(logLine);
        assertEquals(729476174167L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampTimeUptime() {
        String logLine = "[2023-02-12T07:16:14.167+0200][0.005s][warning][gc,alloc       ] ForkJoinPool-123-worker: "
                + "Retried waiting for GCLocker too often allocating 1235 words";
        assertTrue(UnifiedGcLockerRetryEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY.toString() + ".");
        UnifiedGcLockerRetryEvent event = new UnifiedGcLockerRetryEvent(logLine);
        assertEquals((long) 5, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testTimestampUptime() {
        String logLine = "[0.005s][warning][gc,alloc       ] ForkJoinPool-123-worker: Retried "
                + "waiting for GCLocker too often allocating 1235 words";
        assertTrue(UnifiedGcLockerRetryEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY.toString() + ".");
        UnifiedGcLockerRetryEvent event = new UnifiedGcLockerRetryEvent(logLine);
        assertEquals((long) 5, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_GC_LOCKER_RETRY);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_GC_LOCKER_RETRY.toString() + " incorrectly indentified as unified.");
    }
}
