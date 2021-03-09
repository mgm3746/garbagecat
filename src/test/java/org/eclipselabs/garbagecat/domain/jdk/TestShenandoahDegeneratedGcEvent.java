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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC_MARK;
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
class TestShenandoahDegeneratedGcEvent {

    @Test
    void testLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(ShenandoahDegeneratedGcMarkEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC_MARK + ".");
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        assertEquals((long) (52937 - 53), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(60), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(megabytes(30), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(64), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(53697, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertEquals(SHENANDOAH_DEGENERATED_GC_MARK, JdkUtil.identifyEventType(logLine),
                SHENANDOAH_DEGENERATED_GC_MARK + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof ShenandoahDegeneratedGcMarkEvent,
                SHENANDOAH_DEGENERATED_GC_MARK + " not parsed.");
    }

    @Test
    void testBlocking() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                SHENANDOAH_DEGENERATED_GC_MARK + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = SHENANDOAH_DEGENERATED_GC_MARK;
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof ShenandoahDegeneratedGcMarkEvent,
                SHENANDOAH_DEGENERATED_GC_MARK + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(SHENANDOAH_DEGENERATED_GC_MARK),
                SHENANDOAH_DEGENERATED_GC_MARK + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(SHENANDOAH_DEGENERATED_GC_MARK);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                SHENANDOAH_DEGENERATED_GC_MARK
                        + " incorrectly indentified as unified.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) "
                + "53.697ms   ";
        assertTrue(ShenandoahDegeneratedGcMarkEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC_MARK + ".");
    }

    @Test
    void testLogLineNotUnified() {
        String logLine = "2020-08-18T14:05:42.515+0000: 854868.165: [Pause Degenerated GC (Mark) "
                + "93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(ShenandoahDegeneratedGcMarkEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC_MARK + ".");
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        assertEquals((long) 854868165, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(93058), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(megabytes(29873), event.getCombinedOccupancyEnd(),
                "Combined end size not parsed correctly.");
        assertEquals(megabytes(98304), event.getCombinedSpace(),
                "Combined allocation size not parsed correctly.");
        assertEquals(1285045, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineMetaspace() {
        String logLine = "[2020-10-26T14:51:41.413-0400] GC(413) Pause Degenerated GC (Mark) 90M->12M(96M) 27.501ms "
                + "Metaspace: 3963K->3963K(1056768K)";
        assertTrue(ShenandoahDegeneratedGcMarkEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC_MARK + ".");
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        assertEquals(657057101386L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(90), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(megabytes(12), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(96), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(27501, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(kilobytes(3963), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3963), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testLogLineTriggerOutsideOfCycle() {
        String logLine = "[8.084s] GC(136) Pause Degenerated GC (Outside of Cycle) 90M->6M(96M) 23.018ms "
                + "Metaspace: 3847K->3847K(1056768K)";
        assertTrue(ShenandoahDegeneratedGcMarkEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC_MARK + ".");
        ShenandoahDegeneratedGcMarkEvent event = new ShenandoahDegeneratedGcMarkEvent(logLine);
        assertEquals((long) (8084 - 23), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(90), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(megabytes(6), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(96), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(23018, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(kilobytes(3847), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3847), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
    }
}
