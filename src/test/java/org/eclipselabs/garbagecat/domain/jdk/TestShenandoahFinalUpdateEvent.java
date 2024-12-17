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
class TestShenandoahFinalUpdateEvent {

    @Test
    void testDatestamp() {
        String logLine = "2020-03-10T08:03:47.442-0400: [Pause Final Update Refs, 0.206 ms]";
        assertTrue(ShenandoahFinalUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE.toString() + ".");
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals(637139027442L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(206, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "18.504: [Pause Final Update Refs, 0.206 ms]";
        assertEquals(JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "2020-03-10T08:03:47.442-0400: 18.504: [Pause Final Update Refs, 0.206 ms]";
        assertTrue(ShenandoahFinalUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE.toString() + ".");
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals((long) 18504, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(206, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "18.504: [Pause Final Update Refs, 0.206 ms]";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahFinalUpdateEvent,
                JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE),
                JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestamp() {
        String logLine = "18.504: [Pause Final Update Refs, 0.206 ms]";
        assertTrue(ShenandoahFinalUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE.toString() + ".");
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals((long) 18504, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.SHENANDOAH_FINAL_UPDATE);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "18.504: [Pause Final Update Refs, 0.206 ms]";
        assertTrue(ShenandoahFinalUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_UPDATE.toString() + ".");
    }
}
