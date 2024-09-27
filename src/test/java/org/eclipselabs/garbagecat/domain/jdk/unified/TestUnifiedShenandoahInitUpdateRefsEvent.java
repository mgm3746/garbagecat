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
class TestUnifiedShenandoahInitUpdateRefsEvent {

    @Test
    void testBlocking() {
        String logLine = "[5.312s][info][gc] GC(110) Pause Init Update Refs 0.005ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS;
        String logLine = "[5.312s][info][gc] GC(110) Pause Init Update Refs 0.005ms";
        long timestamp = 5312 - 0;
        int duration = 5;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedShenandoahInitUpdateRefsEvent,
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[5.312s][info][gc] GC(110) Pause Init Update Refs 0.005ms";
        assertEquals(JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[5.312s][info][gc] GC(110) Pause Init Update Refs 0.005ms";
        assertTrue(UnifiedShenandoahInitUpdateRefsEvent.match(logLine), "Log line not recognized as "
                + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + ".");
        UnifiedShenandoahInitUpdateRefsEvent event = new UnifiedShenandoahInitUpdateRefsEvent(logLine);
        assertEquals(5312 - 0, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(5, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[5.312s][info][gc] GC(110) Pause Init Update Refs 0.005ms  ";
        assertTrue(UnifiedShenandoahInitUpdateRefsEvent.match(logLine), "Log line not recognized as "
                + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[5.312s][info][gc] GC(110) Pause Init Update Refs 0.005ms";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null,
                        CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahInitUpdateRefsEvent,
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString()
                        + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_SHENANDOAH_INIT_UPDATE_REFS.toString() + " not indentified as unified.");
    }
}
