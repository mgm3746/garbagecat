/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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
import static org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestShenandoahDegeneratedGcEvent {

    @Test
    void testBlocking() {
        String logLine = "854868.165: [Pause Degenerated GC (Mark) 93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                SHENANDOAH_DEGENERATED_GC + " not indentified as blocking.");
    }

    @Test
    void testDatestamp() {
        String logLine = "2020-08-18T14:05:42.515+0000: [Pause Degenerated GC (Mark) "
                + "93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals(651056742515L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(93058), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(29873), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(98304), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(1285045, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = SHENANDOAH_DEGENERATED_GC;
        String logLine = "854868.165: [Pause Degenerated GC (Mark) 93058M->29873M(98304M), 1285.045 ms]";
        long timestamp = 854868165;
        int duration = 1285045;
        assertTrue(JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                duration) instanceof ShenandoahDegeneratedGcEvent, SHENANDOAH_DEGENERATED_GC + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "854868.165: [Pause Degenerated GC (Mark) 93058M->29873M(98304M), 1285.045 ms]";
        assertEquals(SHENANDOAH_DEGENERATED_GC, JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                SHENANDOAH_DEGENERATED_GC + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "2020-08-18T14:05:42.515+0000: 854868.165: [Pause Degenerated GC (Mark) "
                + "93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals((long) 854868165, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(93058), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(29873), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(98304), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(1285045, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "854868.165: [Pause Degenerated GC (Mark) 93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "854868.165: [Pause Degenerated GC (Mark) 93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahDegeneratedGcEvent,
                SHENANDOAH_DEGENERATED_GC + " not parsed.");
    }

    @Test
    void testPreprocessedEvacuationMetaspace() {
        String logLine = "2021-03-23T20:19:44.992+0000: 2871.667: [Pause Degenerated GC (Evacuation) "
                + "1605M->1053M(1690M), 496.640 ms], [Metaspace: 256569K->256569K(1292288K)]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
    }

    @Test
    void testPreprocessedMarkMetaspace() {
        String logLine = "2021-03-23T20:57:24.270+0000: 120817.553: [Pause Degenerated GC (Mark) 1578M->1127M(1690M), "
                + "1346.267 ms], [Metaspace: 282194K->282194K(1314816K)]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals((long) 120817553, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(1578), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1127), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(1690), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(1346267, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(kilobytes(282194), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(282194), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1314816), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testPreprocessedUpdateRefsMetaspace() {
        String logLine = "2021-03-23T20:57:30.279+0000: 120823.562: [Pause Degenerated GC (Update Refs) "
                + "1584M->1466M(1690M), 138.146 ms], [Metaspace: 282194K->282194K(1314816K)]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(SHENANDOAH_DEGENERATED_GC),
                SHENANDOAH_DEGENERATED_GC + " not indentified as reportable.");
    }

    @Test
    void testTimestamp() {
        String logLine = "854868.165: [Pause Degenerated GC (Mark) 93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals((long) 854868165, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(SHENANDOAH_DEGENERATED_GC);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                SHENANDOAH_DEGENERATED_GC + " incorrectly indentified as unified.");
    }
}
