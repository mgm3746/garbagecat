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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType.SHENANDOAH_DEGENERATED_GC;
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
    void testBlocking() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                SHENANDOAH_DEGENERATED_GC + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = SHENANDOAH_DEGENERATED_GC;
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        long timestamp = 521;
        int duration = 0;
        assertTrue(JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                duration) instanceof ShenandoahDegeneratedGcEvent, SHENANDOAH_DEGENERATED_GC + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertEquals(SHENANDOAH_DEGENERATED_GC, JdkUtil.identifyEventType(logLine, null),
                SHENANDOAH_DEGENERATED_GC + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals((long) (52937 - 53), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(60), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(megabytes(30), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(64), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(53697, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8() {
        String logLine = "2020-08-18T14:05:42.515+0000: 854868.165: [Pause Degenerated GC (Mark) "
                + "93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals((long) 854868165, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(93058), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(megabytes(29873), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(98304), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(1285045, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Datestamp() {
        String logLine = "2020-08-18T14:05:42.515+0000: [Pause Degenerated GC (Mark) "
                + "93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals(651056742515L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(93058), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(megabytes(29873), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(98304), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(1285045, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Timestamp() {
        String logLine = "854868.165: [Pause Degenerated GC (Mark) 93058M->29873M(98304M), 1285.045 ms]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals((long) 854868165, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineMetaspace() {
        String logLine = "[2020-10-26T14:51:41.413-0400] GC(413) Pause Degenerated GC (Mark) 90M->12M(96M) 27.501ms "
                + "Metaspace: 3963K->3963K(1056768K)";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals(JdkUtil.convertDatestampToMillis("2020-10-26T14:51:41.413-0400") - 27, event.getTimestamp(),
                "Time stamp not parsed correctly.");
        assertEquals(megabytes(90), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(megabytes(12), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(96), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(27501, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(kilobytes(3963), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3963), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testLogLineTriggerOutsideOfCycle() {
        String logLine = "[8.084s] GC(136) Pause Degenerated GC (Outside of Cycle) 90M->6M(96M) 23.018ms "
                + "Metaspace: 3847K->3847K(1056768K)";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals((long) (8084 - 23), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(90), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(megabytes(6), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(96), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(23018, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(kilobytes(3847), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3847), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) "
                + "53.697ms   ";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ShenandoahDegeneratedGcEvent,
                SHENANDOAH_DEGENERATED_GC + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(SHENANDOAH_DEGENERATED_GC),
                SHENANDOAH_DEGENERATED_GC + " not indentified as reportable.");
    }

    @Test
    void testreprocessedEvacuationMetaspace() {
        String logLine = "2021-03-23T20:19:44.992+0000: 2871.667: [Pause Degenerated GC (Evacuation) "
                + "1605M->1053M(1690M), 496.640 ms], [Metaspace: 256569K->256569K(1292288K)]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
    }

    @Test
    void testreprocessedMarkMetaspace() {
        String logLine = "2021-03-23T20:57:24.270+0000: 120817.553: [Pause Degenerated GC (Mark) 1578M->1127M(1690M), "
                + "1346.267 ms], [Metaspace: 282194K->282194K(1314816K)]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
        ShenandoahDegeneratedGcEvent event = new ShenandoahDegeneratedGcEvent(logLine);
        assertEquals((long) 120817553, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(1578), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(megabytes(1127), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(1690), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(1346267, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(kilobytes(282194), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(282194), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1314816), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testreprocessedUpdateRefsMetaspace() {
        String logLine = "2021-03-23T20:57:30.279+0000: 120823.562: [Pause Degenerated GC (Update Refs) "
                + "1584M->1466M(1690M), 138.146 ms], [Metaspace: 282194K->282194K(1314816K)]";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
    }

    @Test
    void testreprocessedUpdateRefsMetaspaceJdk11() {
        String logLine = "[2023-02-22T06:35:41.594+0000][2003][gc            ] GC(329) Pause Degenerated GC "
                + "(Update Refs) 5855M->1809M(6144M) 22.221ms Metaspace: 125660K(138564K)->125660K(138564K)";
        assertTrue(ShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + SHENANDOAH_DEGENERATED_GC + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(SHENANDOAH_DEGENERATED_GC);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                SHENANDOAH_DEGENERATED_GC + " incorrectly indentified as unified.");
    }
}
