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
public class TestShenandoahFinalUpdateEvent {

    @Test
    public void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:47.442-0400: 18.504: [Pause Final Update Refs, 0.206 ms]";
        assertTrue(ShenandoahFinalUpdateEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".");
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals((long) 18504,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(206,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineUnified() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        assertTrue(ShenandoahFinalUpdateEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".");
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals((long) (1030 - 0),event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(97,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE,JdkUtil.identifyEventType(logLine),JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE + "not identified.");
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof ShenandoahFinalUpdateEvent, JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not parsed.");
    }

    @Test
    public void testBlocking() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not indentified as blocking.");
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE;
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(JdkUtil
		.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahFinalUpdateEvent, JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not parsed.");
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE), JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " not indentified as reportable.");
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_UPDATE);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes), JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " incorrectly indentified as unified.");
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[1.030s][info][gc] GC(10) Pause Final Update Refs 0.097ms    ";
        assertTrue(ShenandoahFinalUpdateEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".");
    }

    @Test
    public void testLogLineUnifiedDetailed() {
        String logLine = "[69.644s][info][gc           ] GC(2582) Pause Final Update Refs 0.302ms";
        assertTrue(ShenandoahFinalUpdateEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".");
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals((long) (69644 - 0),event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(302,event.getDuration(),"Duration not parsed correctly.");
    }

    @Test
    public void testLogLineUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.249-0200][3161ms] GC(0) Pause Final Update Refs 0.998ms";
        assertTrue(ShenandoahFinalUpdateEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + ".");
        ShenandoahFinalUpdateEvent event = new ShenandoahFinalUpdateEvent(logLine);
        assertEquals((long) (3161 - 0),event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(998,event.getDuration(),"Duration not parsed correctly.");
    }
}
