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
class TestUnifiedShenandoahFinalEvacEvent {

    @Test
    void testBlocking() {
        String logLine = "[10.444s][info][gc] GC(278) Pause Final Evac 0.003ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC;
        String logLine = "[10.444s][info][gc] GC(278) Pause Final Evac 0.003ms";
        long timestamp = 1044 - 0;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedShenandoahFinalEvacEvent,
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[10.444s][info][gc] GC(278) Pause Final Evac 0.003ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[10.444s][info][gc] GC(278) Pause Final Evac 0.003ms";
        assertTrue(UnifiedShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC.toString() + ".");
        UnifiedShenandoahFinalEvacEvent event = new UnifiedShenandoahFinalEvacEvent(logLine);
        assertEquals(10444 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[10.444s][info][gc] GC(278) Pause Final Evac 0.003ms   ";
        assertTrue(UnifiedShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[10.444s][info][gc] GC(278) Pause Final Evac 0.003ms";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahFinalEvacEvent,
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_FINAL_EVAC.toString() + " not indentified as unified.");
    }
}
