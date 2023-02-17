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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestParallelScavengeEvent {

    @Test
    void testDatestamp() {
        String logLine = "2017-02-01T17:09:50.155+0000: [GC (Heap Dump Initiated GC) "
                + "[PSYoungGen: 335699K->33192K(397312K)] 1220565K->918194K(1287680K), 0.0243428 secs] "
                + "[Times: user=0.07 sys=0.01, real=0.03 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals(539266190155L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testDoubleDash() {
        String logLine = "2017-02-01T15:56:24.437+0000: 1025076.327: [GC (Allocation Failure) "
                + "--[PSYoungGen: 385537K->385537K(397824K)] 1271095K->1275901K(1288192K), 0.1674611 secs] "
                + "[Times: user=0.24 sys=0.00, real=0.17 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 1025076327, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.ALLOCATION_FAILURE,
                "Trigger not recognized as " + GcTrigger.Type.ALLOCATION_FAILURE.toString() + ".");
        assertEquals(kilobytes(385537), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(385537), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(397824), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(1271095 - 385537), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(1275901 - 385537), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(1288192 - 397824), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(167461, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(24, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(17, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(142, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testHeapDumpInitiatedGcTrigger() {
        String logLine = "2017-02-01T17:09:50.155+0000: 1029482.045: [GC (Heap Dump Initiated GC) "
                + "[PSYoungGen: 335699K->33192K(397312K)] 1220565K->918194K(1287680K), 0.0243428 secs] "
                + "[Times: user=0.07 sys=0.01, real=0.03 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 1029482045, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.HEAP_DUMP_INITIATED_GC,
                "Trigger not recognized as " + GcTrigger.Type.HEAP_DUMP_INITIATED_GC.toString() + ".");
        assertEquals(kilobytes(335699), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(33192), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(397312), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(1220565 - 335699), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(918194 - 33192), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(1287680 - 397312), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(24342, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(7, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(3, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(267, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testHeapInspectionInitiatedGcTrigger() {
        String logLine = "285196.842: [GC (Heap Inspection Initiated GC) [PSYoungGen: 1475708K->47669K(1514496K)] "
                + "4407360K->2982516K(6233088K), 0.2635940 secs] [Times: user=0.86 sys=0.00, real=0.27 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 285196842, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.HEAP_INSPECTION_INITIATED_GC,
                "Trigger not recognized as " + GcTrigger.Type.HEAP_INSPECTION_INITIATED_GC.toString() + ".");
        assertEquals(kilobytes(1475708), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(47669), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1514496), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(4407360 - 1475708), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(2982516 - 47669), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(6233088 - 1514496), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(263594, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(86, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(27, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(319, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] "
                + "160183K->133159K(585088K), 0.0225213 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " not indentified as blocking.");
    }

    @Test
    void testJDK8LogLineWithAllocationFailureTrigger() {
        String logLine = "7.682: [GC (Allocation Failure) [PSYoungGen: 1048576K->131690K(1223168K)] "
                + "1118082K->201204K(4019712K), 0.0657426 secs] [Times: user=0.13 sys=0.00, real=0.07 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 7682, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.ALLOCATION_FAILURE, "Trigger not parsed correctly.");
        assertEquals(kilobytes(1048576), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(131690), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1223168), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(1118082 - 1048576), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(201204 - 131690), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(4019712 - 1223168), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(65742, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(13, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(7, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(186, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testJDK8LogLineWithGcLockerInitiateGcTrigger() {
        String logLine = "4.172: [GC (GCLocker Initiated GC) [PSYoungGen: 649034K->114285K(1223168K)] "
                + "673650K->138909K(4019712K), 0.0711412 secs] [Times: user=0.24 sys=0.01, real=0.08 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 4172, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.GCLOCKER_INITIATED_GC, "Trigger not parsed correctly.");
        assertEquals(kilobytes(649034), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(114285), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1223168), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(673650 - 649034), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(138909 - 114285), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(4019712 - 1223168), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(71141, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(24, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(8, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(313, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testJDK8LogLineWithMetatdataGcThreshholdTrigger() {
        String logLine = "1.219: [GC (Metadata GC Threshold) [PSYoungGen: 1226834K->17779K(1835008K)] "
                + "1226834K->17795K(6029312K), 0.0144911 secs] [Times: user=0.04 sys=0.00, real=0.01 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 1219, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.METADATA_GC_THRESHOLD, "Trigger not parsed correctly.");
        assertEquals(kilobytes(1226834), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(17779), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1835008), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(1226834 - 1226834), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(17795 - 17779), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(6029312 - 1835008), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(14491, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(4, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(1, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(400, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLastDitchCollectionTrigger() {
        String logLine = "372405.495: [GC (Last ditch collection) [PSYoungGen: 0K->0K(1569280K)] "
                + "773083K->773083K(6287872K), 0.2217060 secs] [Times: user=0.76 sys=0.00, real=0.22 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 372405495, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.LAST_DITCH_COLLECTION, "Trigger not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1569280), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(773083 - 0), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(773083 - 0), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(6287872 - 1569280), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(221706, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(76, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(22, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(346, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLine() {
        String logLine = "19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] "
                + "160183K->133159K(585088K), 0.0225213 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 19810091, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(27808), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(632), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(28032), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(132375), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(132527), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(557056), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(22521, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] "
                + "160183K->133159K(585088K), 0.0225213 secs]     ";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
    }

    @Test
    void testSizeWithNineTensPlacesLogLine() {
        String logLine = "1006.751: [GC [PSYoungGen: 61139904K->20643840K(67413056K)] "
                + "119561147K->80396669K(129092672K), 3.8993460 secs] [Times: user=66.40 sys=3.73, real=3.89 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 1006751, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(61139904), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(20643840), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(67413056), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(119561147 - 61139904), event.getOldOccupancyInit(),
                "Old begin size not parsed correctly.");
        assertEquals(kilobytes(80396669 - 20643840), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(129092672 - 67413056), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(3899346, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(6640, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(373, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(389, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(1803, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testStressedJvmLogLine() {
        String logLine = "14112.691: [GC-- [PSYoungGen: 313864K->313864K(326656K)] "
                + "879670K->1012935K(1025728K), 0.9561947 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 14112691, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(313864), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(313864), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(326656), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(565806), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(699071), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(699072), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(956194, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testSystemGcTrigger() {
        String logLine = "180069.616: [GC (System.gc()) [PSYoungGen: 553672K->22188K(1472512K)] "
                + "2900456K->2372732K(6191104K), 0.1668270 secs] [Times: user=0.58 sys=0.00, real=0.17 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 180069616, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.Type.SYSTEM_GC,
                "Trigger not recognized as " + GcTrigger.Type.SYSTEM_GC.toString() + ".");
        assertEquals(kilobytes(553672), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(22188), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1472512), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(2900456 - 553672), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(2372732 - 22188), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(6191104 - 1472512), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(166827, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(58, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(17, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(342, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTimestamp() {
        String logLine = "1029482.045: [GC (Heap Dump Initiated GC) "
                + "[PSYoungGen: 335699K->33192K(397312K)] 1220565K->918194K(1287680K), 0.0243428 secs] "
                + "[Times: user=0.07 sys=0.01, real=0.03 secs]";
        assertTrue(ParallelScavengeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals((long) 1029482045, event.getTimestamp(), "Time stamp not parsed correctly.");
    }
}
