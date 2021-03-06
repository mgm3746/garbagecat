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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
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
public class TestShenandoahDegeneratedGcEvent {

    @Test
    public void testLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + ".",
                ShenandoahDegeneratedGcMarkEvent.match(logLine));
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 52937 - 53, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(60 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(30 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(64 * 1024), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 53697, event.getDuration());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahDegeneratedGcMarkEvent);
    }

    @Test
    public void testBlocking() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(
                JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testHydration() {
        LogEventType eventType = JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK;
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " not parsed.",
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahDegeneratedGcMarkEvent);
    }

    @Test
    public void testReportable() {
        assertTrue(
                JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_DEGENERATED_GC_MARK);
        assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) "
                + "53.697ms   ";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + ".",
                ShenandoahDegeneratedGcMarkEvent.match(logLine));
    }

    @Test
    public void testLogLineNotUnified() {
        String logLine = "2020-08-18T14:05:42.515+0000: 854868.165: [Pause Degenerated GC (Mark) "
                + "93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + ".",
                ShenandoahDegeneratedGcMarkEvent.match(logLine));
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 854868165, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(93058 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(29873 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(98304 * 1024), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 1285045, event.getDuration());
    }

    @Test
    public void testLogLineMetaspace() {
        String logLine = "[2020-10-26T14:51:41.413-0400] GC(413) Pause Degenerated GC (Mark) 90M->12M(96M) 27.501ms "
                + "Metaspace: 3963K->3963K(1056768K)";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + ".",
                ShenandoahDegeneratedGcMarkEvent.match(logLine));
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 657035501386L, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(90 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(12 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(96 * 1024), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 27501, event.getDuration());
        assertEquals("Metaspace begin size not parsed correctly.", kilobytes(3963), event.getPermOccupancyInit());
        assertEquals("Metaspace end size not parsed correctly.", kilobytes(3963), event.getPermOccupancyEnd());
        assertEquals("Metaspace allocation size not parsed correctly.", kilobytes(1056768), event.getPermSpace());
    }

    @Test
    public void testLogLineTriggerOutsideOfCycle() {
        String logLine = "[8.084s] GC(136) Pause Degenerated GC (Outside of Cycle) 90M->6M(96M) 23.018ms "
                + "Metaspace: 3847K->3847K(1056768K)";
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK.toString() + ".",
                ShenandoahDegeneratedGcMarkEvent.match(logLine));
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 8084 - 23, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(90 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(6 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(96 * 1024), event.getCombinedSpace());
        assertEquals("Duration not parsed correctly.", 23018, event.getDuration());
        assertEquals("Metaspace begin size not parsed correctly.", kilobytes(3847), event.getPermOccupancyInit());
        assertEquals("Metaspace end size not parsed correctly.", kilobytes(3847), event.getPermOccupancyEnd());
        assertEquals("Metaspace allocation size not parsed correctly.", kilobytes(1056768), event.getPermSpace());
    }
}
