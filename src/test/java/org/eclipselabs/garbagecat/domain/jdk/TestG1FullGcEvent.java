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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.junit.jupiter.api.Test;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestG1FullGcEvent {

    @Test
    void testHeapInspectionInitiatedGc() throws IOException {
        File testFile = TestUtil.getFile("dataset188.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_SERIAL),
                JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + " event not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_G1.getKey()),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis incorrectly identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "1302.524: [Full GC (System.gc()) 653M->586M(979M), 1.6364900 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLinePreprocessedClassHistogram() {
        String logLine = "49689.217: [Full GC49689.217: [Class Histogram (before full gc):, 8.8690440 secs]"
                + " 11G->2270M(12G), 19.8185620 secs][Eden: 0.0B(612.0M)->0.0B(7372.0M) Survivors: 0.0B->0.0B "
                + "Heap: 11.1G(12.0G)->2270.1M(12.0G)], [Perm: 730823K->730823K(2097152K)]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertEquals(GcTrigger.CLASS_HISTOGRAM, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 49689217, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(11639194), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(2324582), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(12 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(kilobytes(730823), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(730823), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(2097152), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(19818562, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedDatestamp() {
        String logLine = "2017-02-27T02:55:32.523+0300: [Full GC (Allocation Failure) 21G->20G(22G), "
                + "40.6782890 secs][Eden: 0.0B(1040.0M)->0.0B(1120.0M) Survivors: 80.0M->0.0B "
                + "Heap: 22.0G(22.0G)->20.6G(22.0G)], [Perm: 1252884K->1252884K(2097152K)] "
                + "[Times: user=56.34 sys=1.78, real=40.67 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertEquals(541450532523L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedDetailsNoTriggerPerm() {
        String logLine = "178.892: [Full GC 999M->691M(3072M), 3.4262061 secs]"
                + "[Eden: 143.0M(1624.0M)->0.0B(1843.0M) Survivors: 219.0M->0.0B "
                + "Heap: 999.5M(3072.0M)->691.1M(3072.0M)], [Perm: 175031K->175031K(175104K)]"
                + " [Times: user=4.43 sys=0.05, real=3.44 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.NONE, "Trigger not parsed correctly.");
        assertEquals((long) 178892, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1023488), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(707686), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(3072 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(kilobytes(175031), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(175031), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(175104), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(3426206, event.getDurationMicros(), "Duration not parsed correctly.");
        assertEquals(443, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(5, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(344, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(131, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePreprocessedDetailsPermNoSpaceAfterTriggerWithDatestamp() {
        String logLine = "2017-02-27T02:55:32.523+0300: 35911.404: [Full GC (Allocation Failure) 21G->20G(22G), "
                + "40.6782890 secs][Eden: 0.0B(1040.0M)->0.0B(1120.0M) Survivors: 80.0M->0.0B "
                + "Heap: 22.0G(22.0G)->20.6G(22.0G)], [Perm: 1252884K->1252884K(2097152K)] "
                + "[Times: user=56.34 sys=1.78, real=40.67 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals((long) 35911404, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(22 * 1024 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(21600666), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(22 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(kilobytes(1252884), event.getClassOccupancyInit(),
                "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(1252884), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(2097152), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(40678289, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedDetailsTriggerAllocationFailure() {
        String logLine = "56965.451: [Full GC (Allocation Failure)  28G->387M(28G), 1.1821630 secs]"
                + "[Eden: 0.0B(45.7G)->0.0B(34.4G) Survivors: 0.0B->0.0B Heap: 28.0G(28.0G)->387.6M(28.0G)], "
                + "[Metaspace: 65867K->65277K(1112064K)] [Times: user=1.43 sys=0.00, real=1.18 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertEquals(GcTrigger.ALLOCATION_FAILURE, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 56965451, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(28 * 1024 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(396902), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(28 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(kilobytes(65867), event.getClassOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(65277), event.getClassOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1112064), event.getClassSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(1182163, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedDetailsTriggerJvmTi() {
        String logLine = "102.621: [Full GC (JvmtiEnv ForceGarbageCollection)  1124M->1118M(5120M), 3.8954775 secs]"
                + "[Eden: 6144.0K(3072.0M)->0.0B(3072.0M) Survivors: 0.0B->0.0B "
                + "Heap: 1124.8M(5120.0M)->1118.9M(5120.0M)], [Metaspace: 323874K->323874K(1511424K)]"
                + " [Times: user=5.87 sys=0.01, real=3.89 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertEquals(GcTrigger.JVMTI_FORCED_GARBAGE_COLLECTION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 102621, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1151795), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(1145754), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(5120 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(kilobytes(323874), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(323874), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1511424), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(3895477, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedDetailsTriggerLastDitchCollection2SpacesAfterTrigger() {
        String logLine = "98.150: [Full GC (Last ditch collection)  1196M->1118M(5120M), 4.4628626 secs]"
                + "[Eden: 0.0B(3072.0M)->0.0B(3072.0M) Survivors: 0.0B->0.0B "
                + "Heap: 1196.3M(5120.0M)->1118.8M(5120.0M)], [Metaspace: 324984K->323866K(1511424K)] "
                + "[Times: user=6.37 sys=0.00, real=4.46 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertEquals(GcTrigger.LAST_DITCH_COLLECTION, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 98150, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1225011), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(1145651), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(5120 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(kilobytes(324984), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(323866), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1511424), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(4462862, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedDetailsTriggerMetadatGcThresholdMetaspace() {
        String logLine = "188.123: [Full GC (Metadata GC Threshold) 1831M->1213M(5120M), 5.1353878 secs]"
                + "[Eden: 0.0B(1522.0M)->0.0B(2758.0M) Survivors: 244.0M->0.0B "
                + "Heap: 1831.0M(5120.0M)->1213.5M(5120.0M)], [Metaspace: 396834K->324903K(1511424K)]"
                + " [Times: user=7.15 sys=0.04, real=5.14 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertEquals(GcTrigger.METADATA_GC_THRESHOLD, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 188123, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1831 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(1242624), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(5120 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(kilobytes(396834), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(324903), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(1511424), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(5135387, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedDetailsTriggerToSpaceExhausted() {
        String logLine = "105.151: [Full GC (System.gc()) 5820M->1381M(30G), 5.5390169 secs]"
                + "[Eden: 80.0M(112.0M)->0.0B(128.0M) Survivors: 16.0M->0.0B Heap: 5820.3M(30.0G)->1381.9M(30.0G)]"
                + " [Times: user=5.76 sys=1.00, real=5.53 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.SYSTEM_GC, "Trigger not parsed correctly.");
        assertEquals((long) 105151, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(5959987), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(1415066), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(30 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(5539016, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedNoDetailsNoTrigger() {
        String logLine = "2017-05-25T13:00:52.772+0000: 2412.888: [Full GC 1630M->1281M(3072M), 4.1555250 secs] "
                + "[Times: user=7.02 sys=0.01, real=4.16 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.NONE, "Trigger not parsed correctly.");
        assertEquals((long) 2412888, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1630 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(1281 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(3072 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(4155525, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedNoDetailsTriggerMetadataGcThreshold() {
        String logLine = "19544.442: [Full GC (Metadata GC Threshold) 2167M->1007M(6144M), 3.5952929 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
    }

    @Test
    void testLogLinePreprocessedTriggerHeapDumpInitiatedGc() {
        String logLine = "2020-07-14T14:51:39.493-0500: 5590.760: [Full GC (Heap Dump Initiated GC)  "
                + "277M->16M(1024M), 0.1206075 secs][Eden: 259.0M(614.0M)->0.0B(614.0M) Survivors: 0.0B->0.0B "
                + "Heap: 277.7M(1024.0M)->16.7M(1024.0M)], [Metaspace: 41053K->41053K(1085440K)] "
                + "[Times: user=0.14 sys=0.00, real=0.12 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertEquals(GcTrigger.HEAP_DUMP_INITIATED_GC, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 5590760, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(284365), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(17101), event.getCombinedOccupancyEnd(), "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(1024 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(120607, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedTriggerHeapDumpInitiatedGcClassHistogram() {
        String logLine = "2021-10-07T10:05:34.135+0100: 69302.241: [Full GC (Heap Dump Initiated GC) "
                + "2021-10-07T10:05:34.135+0100: 69302.241: [Class Histogram (before full gc):, 4.7148918 secs]"
                + " 8185M->7616M(31G), 24.5727654 secs][Eden: 448.0M(7936.0M)->0.0B(7936.0M) Survivors: 0.0B->0.0B "
                + "Heap: 8185.5M(31.0G)->7616.3M(31.0G)], [Metaspace: 668658K->668658K(1169408K)]"
                + "2021-10-07T10:05:58.708+0100: 69326.814: [Class Histogram (after full gc):, 4.5682980 secs] "
                + "[Times: user=33.60 sys=0.00, real=29.14 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertEquals(GcTrigger.HEAP_DUMP_INITIATED_GC, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 69302241, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(8381952), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(7799091), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(31 * 1024 * 1024), event.getCombinedSpace(),
                "Combined available size not parsed correctly.");
        assertEquals(24572765, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedTriggerHeapInspection() {
        String logLine = "2020-06-26T00:00:06.152+0200: 21424.319: [Full GC (Heap Inspection Initiated GC)  "
                + "3198M->827M(4096M), 4.1354492 secs][Eden: 1404.0M(1794.0M)->0.0B(2456.0M) Survivors: 102.0M->0.0B "
                + "Heap: 3198.1M(4096.0M)->827.6M(4096.0M)], [Metaspace: 319076K->318118K(1343488K)] "
                + "[Times: user=5.01 sys=0.00, real=4.14 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertEquals(GcTrigger.HEAP_INSPECTION_INITIATED_GC, event.getTrigger(), "Trigger not parsed correctly.");
        assertEquals((long) 21424319, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(3274854), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(847462), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(4096 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(4135449, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineTriggerSystemGC() {
        String logLine = "1302.524: [Full GC (System.gc()) 653M->586M(979M), 1.6364900 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.SYSTEM_GC, "Trigger not parsed correctly.");
        assertEquals((long) 1302524, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(653 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(586 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(979 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(1636490, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testNotVerboseGcOld() {
        String logLine = "424753.957: [Full GC (Allocation Failure)  8184M->6998M(8192M), 24.1990452 secs]";
        assertFalse(G1YoungPauseEvent.match(logLine),
                "Log line recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".");
    }

    @Test
    void testTriggerAllocationFailure() {
        String logLine = "424753.957: [Full GC (Allocation Failure)  8184M->6998M(8192M), 24.1990452 secs]";
        assertTrue(G1FullGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + ".");
        G1FullGcEvent event = new G1FullGcEvent(logLine);
        assertTrue(event.getTrigger() == GcTrigger.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals((long) 424753957, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(8184 * 1024), event.getCombinedOccupancyInit(),
                "Combined initial occupancy not parsed correctly.");
        assertEquals(kilobytes(6998 * 1024), event.getCombinedOccupancyEnd(),
                "Combined end occupancy not parsed correctly.");
        assertEquals(kilobytes(8192 * 1024), event.getCombinedSpace(), "Combined available size not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyInit(), "Class initial occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassOccupancyEnd(), "Class end occupancy not parsed correctly.");
        assertEquals(kilobytes(0), event.getClassSpace(), "Class space size not parsed correctly.");
        assertEquals(24199045, event.getDurationMicros(), "Duration not parsed correctly.");
    }

    @Test
    void testTriggerHeapDumpInitiatedGc() throws IOException {
        File testFile = TestUtil.getFile("dataset189.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_FULL_GC_SERIAL),
                JdkUtil.LogEventType.G1_FULL_GC_SERIAL.toString() + " event not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_G1.getKey()),
                Analysis.ERROR_SERIAL_GC_G1 + " analysis incorrectly identified.");
    }
}
