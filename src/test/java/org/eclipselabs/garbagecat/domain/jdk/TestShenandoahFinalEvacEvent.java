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
class TestShenandoahFinalEvacEvent {

    @Test
    void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:46.251-0400: 17.313: [Pause Final Evac, 0.009 ms]";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals((long) 17313, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(9, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Timestamp() {
        String logLine = "17.313: [Pause Final Evac, 0.009 ms]";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals((long) 17313, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Datestamp() {
        String logLine = "2020-03-10T08:03:46.251-0400: [Pause Final Evac, 0.009 ms]";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals(637139026251L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(9, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineUnified() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals((long) (10486 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(2, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof ShenandoahFinalEvacEvent,
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not parsed.");
    }

    @Test
    void testBlocking() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC;
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahFinalEvacEvent,
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_FINAL_EVAC);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[10.486s][info][gc] GC(280) Pause Final Evac 0.002ms    ";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
    }

    @Test
    void testLogLineUnifiedDetailed() {
        String logLine = "[41.912s][info][gc           ] GC(1500) Pause Final Evac 0.022ms";
        assertTrue(ShenandoahFinalEvacEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_FINAL_EVAC.toString() + ".");
        ShenandoahFinalEvacEvent event = new ShenandoahFinalEvacEvent(logLine);
        assertEquals((long) (41912 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(22, event.getDuration(), "Duration not parsed correctly.");
    }
}
