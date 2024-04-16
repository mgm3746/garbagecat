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
 */
class TestShenandoahConcurrentEvent {

    @Test
    void testCleanup() {
        String logLine = "2020-03-10T08:03:29.431-0400: 0.493: [Concurrent cleanup 12501K->8434K(23296K), 0.034 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testDatestamp() {
        String logLine = "2020-03-10T08:03:29.364-0400: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals(637139009364L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testDetailsResetNoSizes() {
        String logLine = "2020-08-13T16:38:29.318+0000: 432034.969: [Concurrent reset, 26.427 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testEvacuation() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Concurrent evacuation 9712K->9862K(23296K), 0.144 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testEvacuationNoSize() {
        String logLine = "2022-10-28T10:58:59.352-0400: [Concurrent evacuation, 0.768 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "2020-03-10T08:03:29.364-0400: 0.426: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "2020-03-10T08:03:29.364-0400: 0.426: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 426, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(16434), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(16466), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(21248), event.getCombinedSpace(), "Combined space size not parsed correctly.");
    }

    @Test
    void testMarking() {
        String logLine = "2020-03-10T08:03:29.365-0400: 0.427: [Concurrent marking 16498K->17020K(21248K), 2.462 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkingProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.315-0400: 0.377: [Concurrent marking (process weakrefs) "
                + "17759K->19325K(19456K), 6.892 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkingUnloadClasses() {
        String logLine = "2024-04-09T08:22:51.006-0400: 4.218: [Concurrent marking (unload classes), 4.318 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkingUpdateRefs() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Concurrent update references 9862K->12443K(23296K), "
                + "3.463 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.315-0400: 0.377: [Concurrent marking (process weakrefs) "
                + "17759K->19325K(19456K), 6.892 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "0.426: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "0.426: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ShenandoahConcurrentEvent,
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " not parsed.");
    }

    @Test
    void testPrecleaning() {
        String logLine = "2020-03-10T08:03:29.322-0400: 0.384: [Concurrent precleaning 19325K->19357K(19456K), "
                + "0.092 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testPreprocessedWithMetaspace() {
        String logLine = "2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]"
                + ", [Metaspace: 6477K->6481K(1056768K)]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 467, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(21278), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(4701), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(37888), event.getCombinedSpace(), "Combined space size not parsed correctly.");
        assertEquals(kilobytes(6477), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(6481), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " not indentified as reportable.");
    }

    @Test
    void testTimestamp() {
        String logLine = "0.426: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 426, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_CONCURRENT);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testUpdateReferences() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Concurrent update references 9862K->12443K(23296K), "
                + "3.463 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

}
