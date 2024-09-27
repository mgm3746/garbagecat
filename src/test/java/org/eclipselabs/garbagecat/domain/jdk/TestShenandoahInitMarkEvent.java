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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestShenandoahInitMarkEvent {

    @Test
    void testBlocking() {
        String logLine = "0.427: [Pause Init Mark, 0.419 ms]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testDatestamp() {
        String logLine = "2020-03-10T08:03:29.365-0400: [Pause Init Mark, 0.419 ms]";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals(637139009365L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(419, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_INIT_MARK;
        String logLine = "0.427: [Pause Init Mark, 0.419 ms]";
        long timestamp = 427;
        int duration = 419;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahInitMarkEvent,
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "0.427: [Pause Init Mark, 0.419 ms]";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK + "not identified.");
    }

    @Test
    void testIsUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_INIT_MARK);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testLogLine() {
        String logLine = "2020-03-10T08:03:29.365-0400: 0.427: [Pause Init Mark, 0.419 ms]";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) 427, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(419, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "0.427: [Pause Init Mark, 0.419 ms]";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahInitMarkEvent,
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not parsed.");
    }

    @Test
    void testProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.489-0400: 0.551: [Pause Init Mark (process weakrefs), 0.314 ms]";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) 551, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(314, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestamp() {
        String logLine = "0.427: [Pause Init Mark, 0.419 ms]";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) 427, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnloadClasses() {
        String logLine = "2024-04-05T16:31:32.088-0400: 9.045: [Pause Init Mark (unload classes), 0.241 ms]";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) 9045, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(241, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testWhitespaceAtEnd() {
        String logLine = "0.427: [Pause Init Mark, 0.419 ms]    ";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
    }
}
