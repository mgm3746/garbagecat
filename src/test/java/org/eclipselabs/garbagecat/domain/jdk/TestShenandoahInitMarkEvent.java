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
public class TestShenandoahInitMarkEvent {

    @Test
    public void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:29.365-0400: 0.427: [Pause Init Mark, 0.419 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 427, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 419, event.getDuration());
    }

    @Test
    public void testLogLineUnified() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 521 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 453, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahInitMarkEvent);
    }

    @Test
    public void testBlocking() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_INIT_MARK;
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahInitMarkEvent);
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_INIT_MARK);
        assertFalse(JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.521s][info][gc] GC(1) Pause Init Mark 0.453ms   ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
    }

    @Test
    public void testLogLineJdk8ProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.489-0400: 0.551: [Pause Init Mark (process weakrefs), 0.314 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 551, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 314, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedProcessWeakrefs() {
        String logLine = "[0.456s][info][gc] GC(0) Pause Init Mark (process weakrefs) 0.868ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 456 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 868, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedUpdateRefs() {
        String logLine = "[10.453s][info][gc] GC(279) Pause Init Mark (update refs) 0.244ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 10453 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 244, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedUpdateRefsProcessWeakrefs() {
        String logLine = "[11.006s][info][gc] GC(300) Pause Init Mark (update refs) (process weakrefs) 0.266ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 11006 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 266, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedDetailed() {
        String logLine = "[41.893s][info][gc           ] GC(1500) Pause Init Mark (update refs) "
                + "(process weakrefs) 0.295ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 41893 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 295, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.178-0200][3090ms] GC(0) Pause Init Mark (process weakrefs) 2.904ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 3090 - 2, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 2904, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedUnloadClasses() {
        String logLine = "[5.593s][info][gc           ] GC(99) Pause Init Mark (unload classes) 0.088ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + ".",
                ShenandoahInitMarkEvent.match(logLine));
        ShenandoahInitMarkEvent event = new ShenandoahInitMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 5593 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 88, event.getDuration());
    }
}
