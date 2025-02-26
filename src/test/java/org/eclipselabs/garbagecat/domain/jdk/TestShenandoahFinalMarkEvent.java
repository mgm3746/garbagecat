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
class TestShenandoahFinalMarkEvent {

    @Test
    void testDatestamp() {
        String logLine = "2020-03-10T08:03:29.427-0400: [Pause Final Mark, 0.313 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals(637139009427L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Pause Final Mark, 0.313 ms]";
        assertEquals(JdkUtil.EventType.SHENANDOAH_FINAL_MARK,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.SHENANDOAH_FINAL_MARK + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Pause Final Mark, 0.313 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) 489, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(313, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "2020-03-10T08:03:46.283-0400: 17.345: [Pause Final Mark (update refs), 0.659 ms]";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahFinalMarkEvent,
                JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + " not parsed.");
    }

    @Test
    void testProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.491-0400: 0.553: [Pause Final Mark (process weakrefs), 0.508 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) 553, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(508, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testTimestamp() {
        String logLine = "0.489: [Pause Final Mark, 0.313 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) 489, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.SHENANDOAH_FINAL_MARK);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testUnloadClasses() {
        String logLine = "2024-04-05T16:31:32.092-0400: 9.050: [Pause Final Mark (unload classes), 1.498 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) 9050, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(1498, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUpdateRefs() {
        String logLine = "2020-03-10T08:03:46.283-0400: 17.345: [Pause Final Mark (update refs), 0.659 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) 17345, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(659, event.getDurationMicros(), "Duration not parsed correctly.");
    }
}
