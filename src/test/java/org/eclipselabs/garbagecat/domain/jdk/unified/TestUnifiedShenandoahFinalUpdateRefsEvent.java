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
class TestUnifiedShenandoahFinalUpdateRefsEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.478s][info][gc] GC(0) Pause Final Update Refs 0.232ms";
        assertEquals(JdkUtil.EventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.478s][info][gc] GC(0) Pause Final Update Refs 0.232ms";
        assertTrue(UnifiedShenandoahFinalUpdateRefsEvent.match(logLine), "Log line not recognized as "
                + JdkUtil.EventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + ".");
        UnifiedShenandoahFinalUpdateRefsEvent event = new UnifiedShenandoahFinalUpdateRefsEvent(logLine);
        assertEquals(478 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(232, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.478s][info][gc] GC(0) Pause Final Update Refs 0.232ms";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null,
                        CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahFinalUpdateRefsEvent,
                JdkUtil.EventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + " not parsed.");
    }

    @Test
    void testTimestamp() {
        String logLine = "[0.478s][info][gc] GC(0) Pause Final Update Refs 0.232ms";
        assertTrue(UnifiedShenandoahFinalUpdateRefsEvent.match(logLine), "Log line not recognized as "
                + JdkUtil.EventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + ".");
        UnifiedShenandoahFinalUpdateRefsEvent event = new UnifiedShenandoahFinalUpdateRefsEvent(logLine);
        assertEquals(478 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + " not indentified as unified.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "[0.478s][info][gc] GC(0) Pause Final Update Refs 0.232ms  ";
        assertTrue(UnifiedShenandoahFinalUpdateRefsEvent.match(logLine), "Log line not recognized as "
                + JdkUtil.EventType.UNIFIED_SHENANDOAH_FINAL_UPDATE_REFS.toString() + ".");
    }
}
