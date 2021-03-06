/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahFinalMarkEvent {

    @Test
    public void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Pause Final Mark, 0.313 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 489, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 313, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedUnified() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 531 - 1, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 1004, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahFinalMarkEvent);
    }

    @Test
    public void testBlocking() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK;
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms";
        long timestamp = 456;
        int duration = 0;
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahFinalMarkEvent);
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_MARK);
        assertFalse(JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " inocrrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineUnifiedWhitespaceAtEnd() {
        String logLine = "[0.531s][info][gc] GC(1) Pause Final Mark 1.004ms   ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
    }

    @Test
    public void testLogLineJdk8ProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.491-0400: 0.553: [Pause Final Mark (process weakrefs), 0.508 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 553, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 508, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedProcessWeakrefs() {
        String logLine = "[0.472s][info][gc] GC(0) Pause Final Mark (process weakrefs) 1.772ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 472 - 1, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 1772, event.getDuration());
    }

    @Test
    public void testLogLineJdk8UpdateRefs() {
        String logLine = "2020-03-10T08:03:46.283-0400: 17.345: [Pause Final Mark (update refs), 0.659 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 17345, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 659, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedUpdateRefs() {
        String logLine = "[10.459s][info][gc] GC(279) Pause Final Mark (update refs) 0.253ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 10459 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 253, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedUpdateRefsProcessWeakrefs() {
        String logLine = "[11.012s][info][gc] GC(300) Pause Final Mark (update refs) (process weakrefs) 0.200ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 11012 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 200, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedDetailed() {
        String logLine = "[41.911s][info][gc           ] GC(1500) Pause Final Mark (update refs) (process weakrefs) "
                + "0.429ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 41911 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 429, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.202-0200][3114ms] GC(0) Pause Final Mark (process weakrefs) 2.517ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 3114 - 2, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 2517, event.getDuration());
    }

    @Test
    public void testLogLineUnloadClasses() {
        String logLine = "[5.602s][info][gc            ] GC(99) Pause Final Mark (unload classes) 1.561ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + ".",
                ShenandoahFinalMarkEvent.match(logLine));
        ShenandoahFinalMarkEvent event = new ShenandoahFinalMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 5602 - 1, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 1561, event.getDuration());
    }
}
