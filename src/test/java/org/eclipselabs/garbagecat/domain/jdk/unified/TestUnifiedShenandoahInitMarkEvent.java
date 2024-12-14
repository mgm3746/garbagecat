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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedShenandoahInitMarkEvent {

    @Test
    void testBlocking() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        EventType eventType = JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK;
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        long timestamp = 521 - 0;
        int duration = 453;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedShenandoahInitMarkEvent,
                JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertEquals(JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK + "not identified.");
    }

    @Test
    void testIsUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_SHENANDOAH_INIT_MARK);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " not indentified as unified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertTrue(UnifiedShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + ".");
        UnifiedShenandoahInitMarkEvent event = new UnifiedShenandoahInitMarkEvent(logLine);
        assertEquals(521 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(453, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahInitMarkEvent,
                JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " not parsed.");
    }

    @Test
    void testProcessWeakrefs() {
        String logLine = "[0.456s][info][gc] GC(0) Pause Init Mark (process weakrefs) 0.868ms";
        assertTrue(UnifiedShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + ".");
        UnifiedShenandoahInitMarkEvent event = new UnifiedShenandoahInitMarkEvent(logLine);
        assertEquals(456 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(868, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + " not indentified as reportable.");
    }

    @Test
    void testUpdateRefs() {
        String logLine = "[10.453s][info][gc] GC(279) Pause Init Mark (update refs) 0.244ms";
        assertTrue(UnifiedShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + ".");
        UnifiedShenandoahInitMarkEvent event = new UnifiedShenandoahInitMarkEvent(logLine);
        assertEquals(10453 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(244, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms   ";
        assertTrue(UnifiedShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_INIT_MARK.toString() + ".");
    }
}
