/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahFinalEvacEvent {

    @Test
    public void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:46.251-0400: 17.313: [Pause Final Evac, 0.009 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".",
                ShenandoahFinalEvacEvent.match(logLine));
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 17313, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 9, event.getDuration());
    }

    @Test
    public void testLogLineUnified() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".",
                ShenandoahFinalEvacEvent.match(logLine));
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 10486 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 2, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahFinalEvacEvent);
    }

    @Test
    public void testBlocking() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC;
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahFinalEvacEvent);
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_EVAC);
        assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms    ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".",
                ShenandoahFinalEvacEvent.match(logLine));
    }

    @Test
    public void testLogLineUnifiedDetailed() {
        String logLine = "[41.912s][info][gc           ] GC(1500) Pause Final Evac 0.022ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".",
                ShenandoahFinalEvacEvent.match(logLine));
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 41912 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 22, event.getDuration());
    }
}
