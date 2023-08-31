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
package org.eclipselabs.garbagecat.domain.jdk;

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
class TestShenandoahFinalRootsEvent {

    @Test
    void testBlocking() {
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots 0.019ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS;
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots 0.019ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahFinalRootsEvent,
                JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots 0.019ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS + "not identified.");
    }

    @Test
    void testLogLineUnifiedTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400] GC(4) Pause Final Roots 0.019ms";
        assertTrue(ShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + ".");
        ShenandoahFinalRootsEvent event = new ShenandoahFinalRootsEvent(logLine);
        assertEquals(746241357862L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedTimeUptime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3.161s] GC(4) Pause Final Roots 0.019ms";
        assertTrue(ShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + ".");
        ShenandoahFinalRootsEvent event = new ShenandoahFinalRootsEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedTimeUptimeMillis() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms] GC(4) Pause Final Roots 0.019ms";
        assertTrue(ShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + ".");
        ShenandoahFinalRootsEvent event = new ShenandoahFinalRootsEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUptime() {
        String logLine = "[3.161s] GC(4) Pause Final Roots 0.019ms";
        assertTrue(ShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + ".");
        ShenandoahFinalRootsEvent event = new ShenandoahFinalRootsEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUptimeMillis() {
        String logLine = "[3161ms] GC(4) Pause Final Roots 0.019ms";
        assertTrue(ShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + ".");
        ShenandoahFinalRootsEvent event = new ShenandoahFinalRootsEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms] GC(4) Pause Final Roots 0.019ms   ";
        assertTrue(ShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms] GC(4) Pause Final Roots 0.019ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ShenandoahFinalRootsEvent,
                JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_ROOTS);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_ROOTS.toString() + " incorrectly indentified as unified.");
    }
}
