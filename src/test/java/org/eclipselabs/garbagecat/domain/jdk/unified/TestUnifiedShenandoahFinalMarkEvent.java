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
class TestUnifiedShenandoahFinalMarkEvent {

    @Test
    void testBlocking() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK;
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        long timestamp = 531 - 1;
        int duration = 1;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedShenandoahFinalMarkEvent,
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertTrue(UnifiedShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + ".");
        UnifiedShenandoahFinalMarkEvent event = new UnifiedShenandoahFinalMarkEvent(logLine);
        assertEquals(531 - 1, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(1004, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahFinalMarkEvent,
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " not parsed.");
    }

    @Test
    void testProcessWeakrefs() {
        String logLine = "[0.820s][info][gc] GC(5) Pause Final Mark (process weakrefs) 0.231ms";
        assertTrue(UnifiedShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + ".");
        UnifiedShenandoahFinalMarkEvent event = new UnifiedShenandoahFinalMarkEvent(logLine);
        assertEquals(820 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(231, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + " not indentified as unified.");
    }

    @Test
    void testUpdateRefs() {
        String logLine = "[10.459s][info][gc] GC(279) Pause Final Mark (update refs) 0.253ms";
        assertTrue(UnifiedShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_MARK.toString() + ".");
        UnifiedShenandoahFinalMarkEvent event = new UnifiedShenandoahFinalMarkEvent(logLine);
        assertEquals(10459 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(253, event.getDurationMicros(), "Duration not parsed correctly.");
    }
}
