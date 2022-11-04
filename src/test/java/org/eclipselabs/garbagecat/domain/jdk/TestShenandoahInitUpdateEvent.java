/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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
class TestShenandoahInitUpdateEvent {

    @Test
    void testBlocking() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE;
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahInitUpdateEvent,
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE + "not identified.");
    }

    @Test
    void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:46.284-0400: 17.346: [Pause Init Update Refs, 0.017 ms]";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals((long) 17346, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(17, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Datestamp() {
        String logLine = "2020-03-10T08:03:46.284-0400: [Pause Init Update Refs, 0.017 ms]";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals(637139026284L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(17, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Timestamp() {
        String logLine = "17.346: [Pause Init Update Refs, 0.017 ms]";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals((long) 17346, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineUnified() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals((long) (4766 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(4, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedDetailed() {
        String logLine = "[69.612s][info][gc           ] GC(2582) Pause Init Update Refs 0.036ms";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals((long) (69612 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(36, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedTime() {
        String logLine = "[2019-02-05T14:47:34.229-0200] GC(0) Pause Init Update Refs 0.092ms";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals(JdkUtil.convertDatestampToMillis("2019-02-05T14:47:34.229-0200") - 0, event.getTimestamp(),
                "Time stamp not parsed correctly.");
        assertEquals(92, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedTimeUptime() {
        String logLine = "[2019-02-05T14:47:34.229-0200][4.766s] GC(0) Pause Init Update Refs 0.092ms";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals((long) (4766 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(92, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedTimeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.229-0200][3141ms] GC(0) Pause Init Update Refs 0.092ms";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
        ShenandoahInitUpdateEvent event = new ShenandoahInitUpdateEvent(logLine);
        assertEquals((long) (3141 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(92, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms    ";
        assertTrue(ShenandoahInitUpdateEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[4.766s][info][gc] GC(97) Pause Init Update Refs 0.004ms";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof ShenandoahInitUpdateEvent,
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
