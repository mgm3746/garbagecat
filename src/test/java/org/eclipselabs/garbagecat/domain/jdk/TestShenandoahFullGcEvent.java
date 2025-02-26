/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
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
import static org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType.SHENANDOAH_FULL_GC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestShenandoahFullGcEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertEquals(SHENANDOAH_FULL_GC, JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                SHENANDOAH_FULL_GC + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "2021-03-23T20:57:46.427+0000: 120839.710: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals((long) (120839710), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(1589), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1002), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(1690), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(kilobytes(282195), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(281648), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1314816), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(4077274, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineDatestamp() {
        String logLine = "2021-03-23T20:57:46.427+0000: [Pause Full 1589M->1002M(1690M), 4077.274 ms], "
                + "[Metaspace: 282195K->281648K(1314816K)]";
        assertTrue(ShenandoahFullGcEvent.match(logLine), "Log line not recognized as " + SHENANDOAH_FULL_GC + ".");
        ShenandoahFullGcEvent event = new ShenandoahFullGcEvent(logLine);
        assertEquals(669830266427L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineTimestamp() {
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
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahFullGcEvent,
                SHENANDOAH_FULL_GC + " not parsed.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(SHENANDOAH_FULL_GC);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                SHENANDOAH_FULL_GC + " incorrectly indentified as unified.");
    }
}
