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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestShenandoahInitUpdateEvent {

    @Test
    void testBlocking() {
        String logLine = "17.346: [Pause Init Update Refs, 0.017 ms]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE;
        String logLine = "17.346: [Pause Init Update Refs, 0.017 ms]";
        long timestamp = 17346;
        int duration = 17;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahInitUpdateEvent,
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "17.346: [Pause Init Update Refs, 0.017 ms]";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "2020-03-10T08:03:46.284-0400: 17.346: [Pause Init Update Refs, 0.017 ms]";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals((long) 17346, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(17, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineDatestamp() {
        String logLine = "2020-03-10T08:03:46.284-0400: [Pause Init Update Refs, 0.017 ms]";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals(637139026284L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(17, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTimestamp() {
        String logLine = "17.346: [Pause Init Update Refs, 0.017 ms]";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals((long) 17346, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "17.346: [Pause Init Update Refs, 0.017 ms]   ";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "17.346: [Pause Init Update Refs, 0.017 ms]";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahInitUpdateEvent,
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_INIT_UPDATE);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " incorrectly indentified as unified.");
    }
}
