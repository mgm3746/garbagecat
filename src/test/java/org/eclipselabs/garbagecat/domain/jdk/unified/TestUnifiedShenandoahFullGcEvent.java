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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType.UNIFIED_SHENANDOAH_FULL_GC;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class TestUnifiedShenandoahFullGcEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[10.478s][info][gc] GC(0) Pause Full 1589M->1002M(1690M), 4077.274 ms";
        assertEquals(UNIFIED_SHENANDOAH_FULL_GC, JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                UNIFIED_SHENANDOAH_FULL_GC + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[10.478s][info][gc] GC(0) Pause Full 1589M->1002M(1690M), 4077.274 ms";
        assertTrue(UnifiedShenandoahFullGcEvent.match(logLine),
                "Log line not recognized as " + UNIFIED_SHENANDOAH_FULL_GC + ".");
        UnifiedShenandoahFullGcEvent event = new UnifiedShenandoahFullGcEvent(logLine);
        assertEquals(10478 - 4077, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(1589), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1002), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(1690), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(4077274, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[10.478s][info][gc] GC(0) Pause Full 1589M->1002M(1690M), 4077.274 ms  ";
        assertTrue(UnifiedShenandoahFullGcEvent.match(logLine),
                "Log line not recognized as " + UNIFIED_SHENANDOAH_FULL_GC + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[10.478s][info][gc] GC(0) Pause Full 1589M->1002M(1690M), 4077.274 ms";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahFullGcEvent,
                UNIFIED_SHENANDOAH_FULL_GC + " not parsed.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "[10.478s][info][gc] GC(0) Pause Full 1589M->1002M(1690M), 4077.274 ms "
                + "Metaspace: 125660K(138564K)->125660K(138564K)";
        assertTrue(UnifiedShenandoahFullGcEvent.match(logLine),
                "Log line not recognized as " + UNIFIED_SHENANDOAH_FULL_GC + ".");
        UnifiedShenandoahFullGcEvent event = new UnifiedShenandoahFullGcEvent(logLine);
        assertEquals(10478 - 4077, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(1589), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1002), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(1690), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(kilobytes(125660), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(125660), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(138564), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(4077274, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(UNIFIED_SHENANDOAH_FULL_GC);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                UNIFIED_SHENANDOAH_FULL_GC + " incorrectly not as unified.");
    }
}
