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
public class TestShenandoahFinalUpdateEvent {

    @Test
    public void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:47.442-0400: 18.504: [Pause Final Update Refs, 0.206 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 18504, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 206, event.getDuration());
    }

    @Test
    public void testLogLineUnified() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 1030 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 97, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahFinalUpdateEvent);
    }

    @Test
    public void testBlocking() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE;
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not parsed.", JdkUtil
                .hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahFinalUpdateEvent);
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_UPDATE);
        assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms    ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
    }

    @Test
    public void testLogLineUnifiedDetailed() {
        String logLine = "[69.644s][info][gc           ] GC(2582) Pause Final Update Refs 0.302ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 69644 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 302, event.getDuration());
    }

    @Test
    public void testLogLineUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.249-0200][3161ms] GC(0) Pause Final Update Refs 0.998ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".",
                ShenandoahFinalUpdateEvent.match(logLine));
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 3161 - 0, event.getTimestamp());
        assertEquals("Duration not parsed correctly.", 998, event.getDuration());
    }
}
