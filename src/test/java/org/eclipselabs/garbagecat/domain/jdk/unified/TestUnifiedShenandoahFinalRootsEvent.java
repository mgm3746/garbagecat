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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedShenandoahFinalRootsEvent {

    @Test
    void testBlocking() {
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots 0.019ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS;
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots 0.019ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedShenandoahFinalRootsEvent,
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[2023-08-25T02:15:57.862-0400][233.267s] GC(4) Pause Final Roots 0.019ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms] GC(4) Pause Final Roots 0.019ms";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null,
                        CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahFinalRootsEvent,
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestampUptime() {
        String logLine = "[3.161s] GC(4) Pause Final Roots 0.019ms";
        assertTrue(UnifiedShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + ".");
        UnifiedShenandoahFinalRootsEvent event = new UnifiedShenandoahFinalRootsEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testTimestampUptimeMillis() {
        String logLine = "[3161ms] GC(4) Pause Final Roots 0.019ms";
        assertTrue(UnifiedShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + ".");
        UnifiedShenandoahFinalRootsEvent event = new UnifiedShenandoahFinalRootsEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + " not indentified as unified.");
    }

    @Test
    void testUnifiedTime() {
        String logLine = "[2023-08-25T02:15:57.862-0400] GC(4) Pause Final Roots 0.019ms";
        assertTrue(UnifiedShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + ".");
        UnifiedShenandoahFinalRootsEvent event = new UnifiedShenandoahFinalRootsEvent(logLine);
        assertEquals(746241357862L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUnifiedTimeUptime() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3.161s] GC(4) Pause Final Roots 0.019ms";
        assertTrue(UnifiedShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + ".");
        UnifiedShenandoahFinalRootsEvent event = new UnifiedShenandoahFinalRootsEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUnifiedTimeUptimeMillis() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms] GC(4) Pause Final Roots 0.019ms";
        assertTrue(UnifiedShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + ".");
        UnifiedShenandoahFinalRootsEvent event = new UnifiedShenandoahFinalRootsEvent(logLine);
        assertEquals(3161, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(19, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[2023-08-25T02:15:57.862-0400][3161ms] GC(4) Pause Final Roots 0.019ms   ";
        assertTrue(UnifiedShenandoahFinalRootsEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_ROOTS.toString() + ".");
    }
}
