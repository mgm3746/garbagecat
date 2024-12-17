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
class TestShenandoahFinalEvacEvent {

    @Test
    void testDatestamp() {
        String logLine = "2020-03-10T08:03:46.251-0400: [Pause Final Evac, 0.009 ms]";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals(637139026251L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(9, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testDatestampUptime() {
        String logLine = "2020-03-10T08:03:46.251-0400: 17.313: [Pause Final Evac, 0.009 ms]";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals((long) 17313, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(9, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "2020-03-10T08:03:46.251-0400: 17.313: [Pause Final Evac, 0.009 ms]";
        assertEquals(JdkUtil.EventType.SHENANDOAH_FINAL_EVAC,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.SHENANDOAH_FINAL_EVAC + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "17.313: [Pause Final Evac, 0.009 ms]";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals((long) 17313, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "17.313: [Pause Final Evac, 0.009 ms]   ";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "17.313: [Pause Final Evac, 0.009 ms]";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahFinalEvacEvent,
                JdkUtil.EventType.SHENANDOAH_FINAL_EVAC.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.SHENANDOAH_FINAL_EVAC),
                JdkUtil.EventType.SHENANDOAH_FINAL_EVAC.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.SHENANDOAH_FINAL_EVAC);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.SHENANDOAH_FINAL_EVAC.toString() + " incorrectly indentified as unified.");
    }
}
