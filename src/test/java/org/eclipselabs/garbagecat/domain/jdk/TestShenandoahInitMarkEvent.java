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
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_INIT_MARK;
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahInitMarkEvent,
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK + "not identified.");
    }

    @Test
    void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:29.365-0400: 0.427: [Pause Init Mark, 0.419 ms]";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) 427, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(419, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Datestamp() {
        String logLine = "2020-03-10T08:03:29.365-0400: [Pause Init Mark, 0.419 ms]";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals(637139009365L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(419, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8ProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.489-0400: 0.551: [Pause Init Mark (process weakrefs), 0.314 ms]";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) 551, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(314, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Timestamp() {
        String logLine = "0.427: [Pause Init Mark, 0.419 ms]";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) 427, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineUnified() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) (521 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(453, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedDetailed() {
        String logLine = "[41.893s][info][gc           ] GC(1500) Pause Init Mark (update refs) "
                + "(process weakrefs) 0.295ms";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) (41893 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(295, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedProcessWeakrefs() {
        String logLine = "[0.456s][info][gc] GC(0) Pause Init Mark (process weakrefs) 0.868ms";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) (456 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(868, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedUnloadClasses() {
        String logLine = "[5.593s][info][gc           ] GC(99) Pause Init Mark (unload classes) 0.088ms";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) (5593 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(88, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedUpdateRefs() {
        String logLine = "[10.453s][info][gc] GC(279) Pause Init Mark (update refs) 0.244ms";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) (10453 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(244, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedUpdateRefsProcessWeakrefs() {
        String logLine = "[11.006s][info][gc] GC(300) Pause Init Mark (update refs) (process weakrefs) 0.266ms";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) (11006 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(266, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pause Init Mark (process weakrefs) 2.904ms";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals((long) (3090 - 2), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(2904, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms   ";
        assertTrue(ShenandoahInitMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof ShenandoahInitMarkEvent,
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_INIT_MARK);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " incorrectly indentified as unified.");
    }
}
