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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestG1ConcurrentEvent {

    @Test
    void testCleanupEnd() {
        String logLine = "50.685: [GC concurrent-cleanup-end, 0.0001080 secs]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 50685, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testCleanupStart() {
        String logLine = "50.685: [GC concurrent-cleanup-start]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 50685, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testDatestamp() {
        String logLine = "2016-02-09T06:22:10.399-0500: [GC concurrent-root-region-scan-start]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals(508314130399L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testDatestampTimestamp() {
        String logLine = "2016-02-09T06:22:10.399-0500: 28039.161: [GC concurrent-root-region-scan-start]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 28039161, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineCleanupEndWithDatestamp() {
        String logLine = "2016-02-11T18:15:35.431-0500: 14974.501: [GC concurrent-cleanup-end, 0.0033880 secs]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 14974501, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineConcurrentMarkResetForOverflow() {
        String logLine = "1048.227: [GC concurrent-mark-reset-for-overflow]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 1048227, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineDoubleTimestamp() {
        String logLine = "23743.632: 23743.632: [GC concurrent-root-region-scan-start]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 23743632, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineStringDeduplication() {
        String logLine = "8.556: [GC concurrent-string-deduplication, 906.5K->410.2K(496.3K), avg 54.8%, "
                + "0.0162924 secs]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 8556, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineWithDatestamp() {
        String logLine = "2016-02-09T06:17:15.377-0500: 27744.139: [GC concurrent-root-region-scan-start]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 27744139, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineWithoutTrailingSecs() {
        String logLine = "449391.280: [GC concurrent-root-region-scan-end, 0.0033660]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 449391280, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineWithTrailingSecNotSecs() {
        String logLine = "449391.442: [GC concurrent-mark-end, 0.1620950 sec]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 449391442, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testMarkEnd() {
        String logLine = "50.655: [GC concurrent-mark-end, 0.5186330 secs]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 50655, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testMarkStart() {
        String logLine = "50.136: [GC concurrent-mark-start]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 50136, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testPreprocessed() {
        String logLine = "27744.494: [GC concurrent-mark-start], 0.3349320 secs] 10854M->9765M(26624M) "
                + "[Times: user=0.98 sys=0.00, real=0.33 secs]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 27744494, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testRootRegionScanEnd() {
        String logLine = "50.136: [GC concurrent-root-region-scan-end, 0.0346620 secs]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 50136, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testRootRegionScanStart() {
        String logLine = "50.101: [GC concurrent-root-region-scan-start]";
        assertTrue(G1ConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.G1_CONCURRENT.toString() + ".");
        G1ConcurrentEvent event = new G1ConcurrentEvent(logLine);
        assertEquals((long) 50101, event.getTimestamp(), "Time stamp not parsed correctly.");
    }
}
