/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestParallelCompactingOldEvent {

    @Test
    void testLogLine() {
        String logLine = "2182.541: [Full GC [PSYoungGen: 1940K->0K(98560K)] "
                + "[ParOldGen: 813929K->422305K(815616K)] 815869K->422305K(914176K) "
                + "[PSPermGen: 81960K->81783K(164352K)], 2.4749181 secs]";
        assertTrue(ParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals((long) 2182541, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1940), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(98560), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(813929), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(422305), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(815616), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(81960), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(81783), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(164352), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(2474918, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhiteSpaceAtEnd() {
        String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] "
                + "[ParOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) "
                + "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]  ";
        assertTrue(ParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
    }

    @Test
    void testLogLineJdk16() {
        String logLine = "2.417: [Full GC (System) [PSYoungGen: 1788K->0K(12736K)] "
                + "[ParOldGen: 1084K->2843K(116544K)] 2872K->2843K(129280K) "
                + "[PSPermGen: 8602K->8593K(131072K)], 0.1028360 secs]";
        assertTrue(ParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals((long) 2417, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC),
                "Trigger not recognized as " + JdkUtil.TriggerType.SYSTEM_GC.toString() + ".");
        assertEquals(kilobytes(1788), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(12736), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(1084), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(2843), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(116544), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(8602), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(8593), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(131072), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(102836, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineJdk8() {
        String logLine = "1.234: [Full GC (Metadata GC Threshold) [PSYoungGen: 17779K->0K(1835008K)] "
                + "[ParOldGen: 16K->16894K(4194304K)] 17795K->16894K(6029312K), [Metaspace: 19114K->19114K(1067008K)], "
                + "0.0352132 secs] [Times: user=0.09 sys=0.00, real=0.04 secs]";
        assertTrue(ParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals((long) 1234, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD),
                "Trigger not recognized as " + JdkUtil.TriggerType.METADATA_GC_THRESHOLD.toString() + ".");
        assertEquals(kilobytes(17779), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1835008), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(16), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(16894), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(4194304), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(19114), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(19114), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1067008), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(35213, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(9, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(4, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(225, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineLastDitchCollectionTrigger() {
        String logLine = "372405.718: [Full GC (Last ditch collection) [PSYoungGen: 0K->0K(1569280K)] "
                + "[ParOldGen: 773083K->773083K(4718592K)] 773083K->773083K(6287872K), "
                + "[Metaspace: 4177368K->4177368K(4194304K)], 1.9708670 secs] "
                + "[Times: user=4.41 sys=0.01, real=1.97 secs]";
        assertTrue(ParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals((long) 372405718, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION),
                "Trigger not recognized as " + JdkUtil.TriggerType.LAST_DITCH_COLLECTION.toString() + ".");
        assertEquals(kilobytes(0), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1569280), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(773083), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(773083), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(4718592), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(4177368), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(4177368), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(4194304), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(1970867, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(441, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(197, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(225, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "2182.541: [Full GC [PSYoungGen: 1940K->0K(98560K)] "
                + "[ParOldGen: 813929K->422305K(815616K)] 815869K->422305K(914176K) "
                + "[PSPermGen: 81960K->81783K(164352K)], 2.4749181 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLineErgonomicsTrigger() {
        String logLine = "21415.385: [Full GC (Ergonomics) [PSYoungGen: 105768K->0K(547840K)] "
                + "[ParOldGen: 1390311K->861344K(1398272K)] 1496080K->861344K(1946112K), "
                + "[Metaspace: 136339K->135256K(1177600K)], 3.4522057 secs] "
                + "[Times: user=11.58 sys=0.64, real=3.45 secs]";
        assertTrue(ParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals((long) 21415385, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ERGONOMICS),
                "Trigger not recognized as " + JdkUtil.TriggerType.ERGONOMICS.toString() + ".");
        assertEquals(kilobytes(105768), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(547840), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(1390311), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(861344), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(1398272), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(136339), event.getPermOccupancyInit(), "Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(135256), event.getPermOccupancyEnd(), "Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1177600), event.getPermSpace(), "Perm gen allocation size not parsed correctly.");
        assertEquals(3452205, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(1158, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(64, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(345, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(355, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testHeapInspectionInitiatedGcTrigger() {
        String logLine = "285197.105: [Full GC (Heap Inspection Initiated GC) [PSYoungGen: 47669K->0K(1514496K)] "
                + "[ParOldGen: 2934846K->851463K(4718592K)] 2982516K->851463K(6233088K), "
                + "[Metaspace: 3959933K->3959881K(3977216K)], 2.4308400 secs] "
                + "[Times: user=6.95 sys=0.03, real=2.43 secs]";
        assertTrue(ParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals((long) 285197105, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC),
                "Trigger not recognized as " + JdkUtil.TriggerType.HEAP_INSPECTION_INITIATED_GC.toString() + ".");
        assertEquals(kilobytes(47669), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1514496), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(2934846), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(851463), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(4718592), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(3959933), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3959881), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(3977216), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(2430840, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(695, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(3, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(243, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(288, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testAllocationFailureTrigger() {
        String logLine = "3203650.654: [Full GC (Allocation Failure) [PSYoungGen: 393482K->393073K(532992K)] "
                + "[ParOldGen: 1398224K->1398199K(1398272K)] 1791707K->1791273K(1931264K), "
                + "[Metaspace: 170955K->170731K(1220608K)], 3.5730395 secs] "
                + "[Times: user=26.24 sys=0.09, real=3.57 secs]";
        assertTrue(ParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals(Long.parseLong("3203650654"), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE),
                "Trigger not recognized as " + JdkUtil.TriggerType.ALLOCATION_FAILURE.toString() + ".");
        assertEquals(kilobytes(393482), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(393073), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(532992), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(1398224), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(1398199), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(1398272), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(170955), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(170731), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1220608), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(3573039, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(2624, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(9, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(357, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(738, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testHeapDumpInitiatedGcTrigger() {
        String logLine = "2017-02-01T17:09:50.180+0000: 1029482.070: [Full GC (Heap Dump Initiated GC) "
                + "[PSYoungGen: 33192K->0K(397312K)] [ParOldGen: 885002K->812903K(890368K)] "
                + "918194K->812903K(1287680K), [Metaspace: 142181K->141753K(1185792K)], 2.3728899 secs] "
                + "[Times: user=7.55 sys=0.60, real=2.37 secs]";
        assertTrue(ParallelCompactingOldEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals(Long.parseLong("1029482070"), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC),
                "Trigger not recognized as " + JdkUtil.TriggerType.HEAP_DUMP_INITIATED_GC.toString() + ".");
        assertEquals(kilobytes(33192), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(0), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(397312), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(885002), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(812903), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(890368), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(kilobytes(142181), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(141753), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1185792), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
        assertEquals(2372889, event.getDuration(), "Duration not parsed correctly.");
        assertEquals(755, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(60, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(237, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(344, event.getParallelism(), "Parallelism not calculated correctly.");
    }
}