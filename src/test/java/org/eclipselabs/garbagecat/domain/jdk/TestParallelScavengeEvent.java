/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.junit.Test;

import static org.eclipselabs.garbagecat.Memory.kilobytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParallelScavengeEvent {

    @Test
    public void testIsBlocking() {
        String logLine = "19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] "
                + "160183K->133159K(585088K), 0.0225213 secs]";
        assertTrue(JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLine() {
        String logLine = "19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] "
                + "160183K->133159K(585088K), 0.0225213 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 19810091, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(27808), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(632), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(28032), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(132375), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(132527), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(557056), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 22521, event.getDuration());
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "19810.091: [GC [PSYoungGen: 27808K->632K(28032K)] "
                + "160183K->133159K(585088K), 0.0225213 secs]     ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
    }

    @Test
    public void testStressedJvmLogLine() {
        String logLine = "14112.691: [GC-- [PSYoungGen: 313864K->313864K(326656K)] "
                + "879670K->1012935K(1025728K), 0.9561947 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 14112691, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(313864), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(313864), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(326656), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(565806), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(699071), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(699072), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 956194, event.getDuration());
    }

    @Test
    public void testSizeWithNineTensPlacesLogLine() {
        String logLine = "1006.751: [GC [PSYoungGen: 61139904K->20643840K(67413056K)] "
                + "119561147K->80396669K(129092672K), 3.8993460 secs] [Times: user=66.40 sys=3.73, real=3.89 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 1006751, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(61139904), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(20643840), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(67413056), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(119561147 - 61139904), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(80396669 - 20643840), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(129092672 - 67413056), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 3899346, event.getDuration());
        assertEquals("User time not parsed correctly.", 6640, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 373, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 389, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 1803, event.getParallelism());
    }

    @Test
    public void testJDK8LogLineWithMetatdataGcThreshholdTrigger() {
        String logLine = "1.219: [GC (Metadata GC Threshold) [PSYoungGen: 1226834K->17779K(1835008K)] "
                + "1226834K->17795K(6029312K), 0.0144911 secs] [Times: user=0.04 sys=0.00, real=0.01 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 1219, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        assertEquals("Young begin size not parsed correctly.", kilobytes(1226834), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(17779), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1835008), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(1226834 - 1226834), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(17795 - 17779), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(6029312 - 1835008), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 14491, event.getDuration());
        assertEquals("User time not parsed correctly.", 4, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 400, event.getParallelism());
    }

    @Test
    public void testJDK8LogLineWithGcLockerInitiateGcTrigger() {
        String logLine = "4.172: [GC (GCLocker Initiated GC) [PSYoungGen: 649034K->114285K(1223168K)] "
                + "673650K->138909K(4019712K), 0.0711412 secs] [Times: user=0.24 sys=0.01, real=0.08 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 4172, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(649034), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(114285), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1223168), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(673650 - 649034), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(138909 - 114285), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(4019712 - 1223168), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 71141, event.getDuration());
        assertEquals("User time not parsed correctly.", 24, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 8, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 313, event.getParallelism());
    }

    @Test
    public void testJDK8LogLineWithAllocationFailureTrigger() {
        String logLine = "7.682: [GC (Allocation Failure) [PSYoungGen: 1048576K->131690K(1223168K)] "
                + "1118082K->201204K(4019712K), 0.0657426 secs] [Times: user=0.13 sys=0.00, real=0.07 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 7682, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Young begin size not parsed correctly.", kilobytes(1048576), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(131690), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1223168), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(1118082 - 1048576), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(201204 - 131690), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(4019712 - 1223168), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 65742, event.getDuration());
        assertEquals("User time not parsed correctly.", 13, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 7, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 186, event.getParallelism());
    }

    @Test
    public void testLastDitchCollectionTrigger() {
        String logLine = "372405.495: [GC (Last ditch collection) [PSYoungGen: 0K->0K(1569280K)] "
                + "773083K->773083K(6287872K), 0.2217060 secs] [Times: user=0.76 sys=0.00, real=0.22 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 372405495, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION));
        assertEquals("Young begin size not parsed correctly.", kilobytes(0), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1569280), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(773083 - 0), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(773083 - 0), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(6287872 - 1569280), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 221706, event.getDuration());
        assertEquals("User time not parsed correctly.", 76, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 22, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 346, event.getParallelism());
    }

    @Test
    public void testHeapInspectionInitiatedGcTrigger() {
        String logLine = "285196.842: [GC (Heap Inspection Initiated GC) [PSYoungGen: 1475708K->47669K(1514496K)] "
                + "4407360K->2982516K(6233088K), 0.2635940 secs] [Times: user=0.86 sys=0.00, real=0.27 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 285196842, event.getTimestamp());
        assertTrue(
                "Trigger not recognized as " + JdkUtil.TriggerType.HEAP_INSPECTION_INITIATED_GC.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(1475708), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(47669), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1514496), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(4407360 - 1475708), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(2982516 - 47669), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(6233088 - 1514496), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 263594, event.getDuration());
        assertEquals("User time not parsed correctly.", 86, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 27, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 319, event.getParallelism());
    }

    @Test
    public void testSystemGcTrigger() {
        String logLine = "180069.616: [GC (System.gc()) [PSYoungGen: 553672K->22188K(1472512K)] "
                + "2900456K->2372732K(6191104K), 0.1668270 secs] [Times: user=0.58 sys=0.00, real=0.17 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 180069616, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.SYSTEM_GC.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(553672), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(22188), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1472512), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(2900456 - 553672), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(2372732 - 22188), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(6191104 - 1472512), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 166827, event.getDuration());
        assertEquals("User time not parsed correctly.", 58, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 17, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 342, event.getParallelism());
    }

    @Test
    public void testHeapDumpInitiatedGcTrigger() {
        String logLine = "2017-02-01T17:09:50.155+0000: 1029482.045: [GC (Heap Dump Initiated GC) "
                + "[PSYoungGen: 335699K->33192K(397312K)] 1220565K->918194K(1287680K), 0.0243428 secs] "
                + "[Times: user=0.07 sys=0.01, real=0.03 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 1029482045, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.HEAP_DUMP_INITIATED_GC.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(335699), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(33192), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(397312), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(1220565 - 335699), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(918194 - 33192), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(1287680 - 397312), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 24342, event.getDuration());
        assertEquals("User time not parsed correctly.", 7, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 3, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 267, event.getParallelism());
    }

    @Test
    public void testDoubleDash() {
        String logLine = "2017-02-01T15:56:24.437+0000: 1025076.327: [GC (Allocation Failure) "
                + "--[PSYoungGen: 385537K->385537K(397824K)] 1271095K->1275901K(1288192K), 0.1674611 secs] "
                + "[Times: user=0.24 sys=0.00, real=0.17 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                ParallelScavengeEvent.match(logLine));
        ParallelScavengeEvent event = new ParallelScavengeEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 1025076327, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.ALLOCATION_FAILURE.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Young begin size not parsed correctly.", kilobytes(385537), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(385537), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(397824), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(1271095 - 385537), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(1275901 - 385537), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(1288192 - 397824), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 167461, event.getDuration());
        assertEquals("User time not parsed correctly.", 24, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 17, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 142, event.getParallelism());
    }
}
