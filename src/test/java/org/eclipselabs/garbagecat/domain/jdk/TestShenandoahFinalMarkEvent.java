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
class TestShenandoahFinalMarkEvent {

    @Test
    void testBlocking() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK;
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        long timestamp = 456;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahFinalMarkEvent,
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK + "not identified.");
    }

    @Test
    void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Pause Final Mark, 0.313 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) 489, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(313, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Datestamp() {
        String logLine = "2020-03-10T08:03:29.427-0400: [Pause Final Mark, 0.313 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals(637139009427L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineJdk8ProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.491-0400: 0.553: [Pause Final Mark (process weakrefs), 0.508 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) 553, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(508, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Timestamp() {
        String logLine = "0.489: [Pause Final Mark, 0.313 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) 489, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineJdk8UpdateRefs() {
        String logLine = "2020-03-10T08:03:46.283-0400: 17.345: [Pause Final Mark (update refs), 0.659 ms]";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) 17345, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(659, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedDetailed() {
        String logLine = "[41.911s][info][gc           ] GC(1500) Pause Final Mark (update refs) (process weakrefs) "
                + "0.429ms";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) (41911 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(429, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedProcessWeakrefs() {
        String logLine = "[0.472s][info][gc] GC(0) Pause Final Mark (process weakrefs) 1.772ms";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) (472 - 1), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(1772, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedUnified() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) (531 - 1), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(1004, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedUpdateRefs() {
        String logLine = "[10.459s][info][gc] GC(279) Pause Final Mark (update refs) 0.253ms";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) (10459 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(253, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedUpdateRefsProcessWeakrefs() {
        String logLine = "[11.012s][info][gc] GC(300) Pause Final Mark (update refs) (process weakrefs) 0.200ms";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) (11012 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(200, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pause Final Mark (process weakrefs) 2.517ms";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) (3114 - 2), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(2517, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnifiedWhitespaceAtEnd() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms   ";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
    }

    @Test
    void testLogLineUnloadClasses() {
        String logLine = "[5.602s][info][gc            ] GC(99) Pause Final Mark (unload classes) 1.561ms";
        assertTrue(ShenandoahFinalMarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".");
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals((long) (5602 - 1), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(1561, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ShenandoahFinalMarkEvent,
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_MARK);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " inocrrectly indentified as unified.");
    }
}
