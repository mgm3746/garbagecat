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
import static org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType.SHENANDOAH_FULL_GC;
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
class TestShenandoahFullGcEvent {

    @Test
    void testBlocking() {
        String logLine = "[2020-02-14T15:21:55.207-0500][052ms] [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                SHENANDOAH_FULL_GC + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = SHENANDOAH_FULL_GC;
        String logLine = "2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        long timestamp = 120839710;
        int duration = 4077;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp, duration) instanceof ShenandoahFullGcEvent,
                SHENANDOAH_FULL_GC + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertEquals(SHENANDOAH_FULL_GC, JdkUtil.identifyEventType(logLine, null),
                SHENANDOAH_FULL_GC + "not identified.");
    }

    @Test
    void testLogLineJdk8() {
        String logLine = "2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals((long) (120839710), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(1589), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(megabytes(1002), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(1690), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(kilobytes(282195), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(281648), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1314816), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(4077274, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Datestamp() {
        String logLine = "2021-03-23T20:57:46.427+0000: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals(669830266427L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineJdk8Timestamp() {
        String logLine = "120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals((long) (120839710), event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]    ";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ShenandoahFullGcEvent,
                SHENANDOAH_FULL_GC + " not parsed.");
    }

    @Test
    void testParseLogLineUnifiedTime() {
        String logLine = "[2020-02-14T15:21:55.207-0500] [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals(635008911130L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(4077274, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLineUnifiedTimeUptime() {
        String logLine = "[2020-02-14T15:21:55.207-0500][100.052s] [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals((long) (100052 - 4077), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(4077274, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLineUnifiedTimeUptimemillis() {
        String logLine = "[2020-02-14T15:21:55.207-0500][100052ms] [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals((long) (100052 - 4077), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(4077274, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLineUnifiedUptime() {
        String logLine = "[100.052s] [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals((long) (100052 - 4077), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(4077274, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testParseLogLineUnifiedUptimemillis() {
        String logLine = "[100052ms] [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals((long) (100052 - 4077), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(4077274, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(SHENANDOAH_FULL_GC), SHENANDOAH_FULL_GC + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(SHENANDOAH_FULL_GC);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                SHENANDOAH_FULL_GC + " incorrectly indentified as unified.");
    }
}
