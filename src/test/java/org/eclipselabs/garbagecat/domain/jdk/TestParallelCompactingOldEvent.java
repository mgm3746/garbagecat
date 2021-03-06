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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParallelCompactingOldEvent {

    @Test
    public void testLogLine() {
        String logLine = "2182.541: [Full GC [PSYoungGen: 1940K->0K(98560K)] "
                + "[ParOldGen: 813929K->422305K(815616K)] 815869K->422305K(914176K) "
                + "[PSPermGen: 81960K->81783K(164352K)], 2.4749181 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                ParallelCompactingOldEvent.match(logLine));
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 2182541, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(1940), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(98560), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(813929), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(422305), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(815616), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(81960), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(81783), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(164352), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 2474918, event.getDuration());
    }

    @Test
    public void testLogLineWhiteSpaceAtEnd() {
        String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] "
                + "[ParOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) "
                + "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]  ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                ParallelCompactingOldEvent.match(logLine));
    }

    @Test
    public void testLogLineJdk16() {
        String logLine = "2.417: [Full GC (System) [PSYoungGen: 1788K->0K(12736K)] "
                + "[ParOldGen: 1084K->2843K(116544K)] 2872K->2843K(129280K) "
                + "[PSPermGen: 8602K->8593K(131072K)], 0.1028360 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                ParallelCompactingOldEvent.match(logLine));
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 2417, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.SYSTEM_GC.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(1788), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(12736), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(1084), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(2843), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(116544), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(8602), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(8593), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(131072), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 102836, event.getDuration());
    }

    @Test
    public void testLogLineJdk8() {
        String logLine = "1.234: [Full GC (Metadata GC Threshold) [PSYoungGen: 17779K->0K(1835008K)] "
                + "[ParOldGen: 16K->16894K(4194304K)] 17795K->16894K(6029312K), [Metaspace: 19114K->19114K(1067008K)], "
                + "0.0352132 secs] [Times: user=0.09 sys=0.00, real=0.04 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                ParallelCompactingOldEvent.match(logLine));
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 1234, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.METADATA_GC_THRESHOLD.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        assertEquals("Young begin size not parsed correctly.", kilobytes(17779), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1835008), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(16), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(16894), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(4194304), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(19114), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(19114), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(1067008), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 35213, event.getDuration());
        assertEquals("User time not parsed correctly.", 9, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 4, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 225, event.getParallelism());
    }

    @Test
    public void testLogLineLastDitchCollectionTrigger() {
        String logLine = "372405.718: [Full GC (Last ditch collection) [PSYoungGen: 0K->0K(1569280K)] "
                + "[ParOldGen: 773083K->773083K(4718592K)] 773083K->773083K(6287872K), "
                + "[Metaspace: 4177368K->4177368K(4194304K)], 1.9708670 secs] "
                + "[Times: user=4.41 sys=0.01, real=1.97 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                ParallelCompactingOldEvent.match(logLine));
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 372405718, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.LAST_DITCH_COLLECTION.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION));
        assertEquals("Young begin size not parsed correctly.", kilobytes(0), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1569280), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(773083), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(773083), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(4718592), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(4177368), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(4177368), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(4194304), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 1970867, event.getDuration());
        assertEquals("User time not parsed correctly.", 441, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 197, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 225, event.getParallelism());
    }

    @Test
    public void testIsBlocking() {
        String logLine = "2182.541: [Full GC [PSYoungGen: 1940K->0K(98560K)] "
                + "[ParOldGen: 813929K->422305K(815616K)] 815869K->422305K(914176K) "
                + "[PSPermGen: 81960K->81783K(164352K)], 2.4749181 secs]";
        assertTrue(JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLineErgonomicsTrigger() {
        String logLine = "21415.385: [Full GC (Ergonomics) [PSYoungGen: 105768K->0K(547840K)] "
                + "[ParOldGen: 1390311K->861344K(1398272K)] 1496080K->861344K(1946112K), "
                + "[Metaspace: 136339K->135256K(1177600K)], 3.4522057 secs] "
                + "[Times: user=11.58 sys=0.64, real=3.45 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                ParallelCompactingOldEvent.match(logLine));
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 21415385, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.ERGONOMICS.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ERGONOMICS));
        assertEquals("Young begin size not parsed correctly.", kilobytes(105768), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(547840), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(1390311), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(861344), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(1398272), event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", kilobytes(136339), event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", kilobytes(135256), event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", kilobytes(1177600), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 3452205, event.getDuration());
        assertEquals("User time not parsed correctly.", 1158, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 64, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 345, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 355, event.getParallelism());
    }

    @Test
    public void testHeapInspectionInitiatedGcTrigger() {
        String logLine = "285197.105: [Full GC (Heap Inspection Initiated GC) [PSYoungGen: 47669K->0K(1514496K)] "
                + "[ParOldGen: 2934846K->851463K(4718592K)] 2982516K->851463K(6233088K), "
                + "[Metaspace: 3959933K->3959881K(3977216K)], 2.4308400 secs] "
                + "[Times: user=6.95 sys=0.03, real=2.43 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                ParallelCompactingOldEvent.match(logLine));
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 285197105, event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.HEAP_INSPECTION_INITIATED_GC.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(47669), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1514496), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(2934846), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(851463), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(4718592), event.getOldSpace());
        assertEquals("Metaspace begin size not parsed correctly.", kilobytes(3959933), event.getPermOccupancyInit());
        assertEquals("Metaspace end size not parsed correctly.", kilobytes(3959881), event.getPermOccupancyEnd());
        assertEquals("Metaspace allocation size not parsed correctly.", kilobytes(3977216), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 2430840, event.getDuration());
        assertEquals("User time not parsed correctly.", 695, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 3, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 243, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 288, event.getParallelism());
    }

    @Test
    public void testAllocationFailureTrigger() {
        String logLine = "3203650.654: [Full GC (Allocation Failure) [PSYoungGen: 393482K->393073K(532992K)] "
                + "[ParOldGen: 1398224K->1398199K(1398272K)] 1791707K->1791273K(1931264K), "
                + "[Metaspace: 170955K->170731K(1220608K)], 3.5730395 secs] "
                + "[Times: user=26.24 sys=0.09, real=3.57 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                ParallelCompactingOldEvent.match(logLine));
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", Long.parseLong("3203650654"), event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.ALLOCATION_FAILURE.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Young begin size not parsed correctly.", kilobytes(393482), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(393073), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(532992), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(1398224), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(1398199), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(1398272), event.getOldSpace());
        assertEquals("Metaspace begin size not parsed correctly.", kilobytes(170955), event.getPermOccupancyInit());
        assertEquals("Metaspace end size not parsed correctly.", kilobytes(170731), event.getPermOccupancyEnd());
        assertEquals("Metaspace allocation size not parsed correctly.", kilobytes(1220608), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 3573039, event.getDuration());
        assertEquals("User time not parsed correctly.", 2624, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 9, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 357, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 738, event.getParallelism());
    }

    @Test
    public void testHeapDumpInitiatedGcTrigger() {
        String logLine = "2017-02-01T17:09:50.180+0000: 1029482.070: [Full GC (Heap Dump Initiated GC) "
                + "[PSYoungGen: 33192K->0K(397312K)] [ParOldGen: 885002K->812903K(890368K)] "
                + "918194K->812903K(1287680K), [Metaspace: 142181K->141753K(1185792K)], 2.3728899 secs] "
                + "[Times: user=7.55 sys=0.60, real=2.37 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                ParallelCompactingOldEvent.match(logLine));
        ParallelCompactingOldEvent event = new ParallelCompactingOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", Long.parseLong("1029482070"), event.getTimestamp());
        assertTrue("Trigger not recognized as " + JdkUtil.TriggerType.HEAP_DUMP_INITIATED_GC.toString() + ".",
                event.getTrigger().matches(JdkRegEx.TRIGGER_HEAP_DUMP_INITIATED_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(33192), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(0), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(397312), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(885002), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(812903), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(890368), event.getOldSpace());
        assertEquals("Metaspace begin size not parsed correctly.", kilobytes(142181), event.getPermOccupancyInit());
        assertEquals("Metaspace end size not parsed correctly.", kilobytes(141753), event.getPermOccupancyEnd());
        assertEquals("Metaspace allocation size not parsed correctly.", kilobytes(1185792), event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 2372889, event.getDuration());
        assertEquals("User time not parsed correctly.", 755, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 60, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 237, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 344, event.getParallelism());
    }
}