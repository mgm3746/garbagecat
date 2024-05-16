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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType.UNIFIED_SHENANDOAH_DEGENERATED_GC;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class TestUnifiedShenandoahDegeneratedGcEvent {

    @Test
    void testBlocking() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                UNIFIED_SHENANDOAH_DEGENERATED_GC + " not indentified as blocking.");
    }

    @Test
    void testHydration() {
        LogEventType eventType = UNIFIED_SHENANDOAH_DEGENERATED_GC;
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        long timestamp = 52937 - 53;
        int duration = 53;
        assertTrue(
                JdkUtil.hydrateBlockingEvent(eventType, logLine, timestamp,
                        duration) instanceof UnifiedShenandoahDegeneratedGcEvent,
                UNIFIED_SHENANDOAH_DEGENERATED_GC + " not parsed.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertEquals(UNIFIED_SHENANDOAH_DEGENERATED_GC,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                UNIFIED_SHENANDOAH_DEGENERATED_GC + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(UnifiedShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + UNIFIED_SHENANDOAH_DEGENERATED_GC + ".");
        UnifiedShenandoahDegeneratedGcEvent event = new UnifiedShenandoahDegeneratedGcEvent(logLine);
        assertEquals(52937 - 53, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(60), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(30), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(64), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(53697, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms  ";
        assertTrue(UnifiedShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + UNIFIED_SHENANDOAH_DEGENERATED_GC + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[52.937s][info][gc           ] GC(1632) Pause Degenerated GC (Mark) 60M->30M(64M) 53.697ms";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null,
                        CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahDegeneratedGcEvent,
                UNIFIED_SHENANDOAH_DEGENERATED_GC + " not parsed.");
    }

    @Test
    void testPreprocessedUpdateRefskMetaspace() {
        String logLine = "[2023-02-22T06:35:41.594+0000][2003][gc            ] GC(329) Pause Degenerated GC "
                + "(Update Refs) 5855M->1809M(6144M) 22.221ms Metaspace: 125660K(138564K)->125660K(138564K)";
        assertTrue(UnifiedShenandoahDegeneratedGcEvent.match(logLine),
                "Log line not recognized as " + UNIFIED_SHENANDOAH_DEGENERATED_GC + ".");
        UnifiedShenandoahDegeneratedGcEvent event = new UnifiedShenandoahDegeneratedGcEvent(logLine);
        assertEquals(730344941594L - 22, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(5855), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(megabytes(1809), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(megabytes(6144), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(22221, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(kilobytes(125660), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(125660), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(138564), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(UNIFIED_SHENANDOAH_DEGENERATED_GC),
                UNIFIED_SHENANDOAH_DEGENERATED_GC + " not indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(UNIFIED_SHENANDOAH_DEGENERATED_GC);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                UNIFIED_SHENANDOAH_DEGENERATED_GC + " not indentified as unified.");
    }
}
