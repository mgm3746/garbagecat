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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestParNewEvent {

    /**
     * Test identifying <code>ParNewEvent</code> running in incremental mode.
     * 
     * @throws IOException
     */
    @Test
    void testCmsIncrementalModeAnalysis() throws IOException {
        File testFile = TestUtil.getFile("dataset68.txt");
        String jvmOptions = "Xss128k -XX:+CMSIncrementalMode -XX:CMSInitiatingOccupancyFraction=70 -Xms2048M";
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_CMS_INCREMENTAL_MODE),
                Analysis.WARN_CMS_INCREMENTAL_MODE + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT),
                Analysis.WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT + " analysis not identified.");
    }

    @Test
    void testIsBlocking() {
        String logLine = "20.189: [GC 20.190: [ParNew: 86199K->8454K(91712K), 0.0375060 secs] "
                + "89399K->11655K(907328K), 0.0387074 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.PAR_NEW.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "20.189: [GC 20.190: [ParNew: 86199K->8454K(91712K), 0.0375060 secs] "
                + "89399K->11655K(907328K), 0.0387074 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 20189, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(86199), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(8454), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(91712), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(3200), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(3201), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(815616), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(38707, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineAfterPreprocessing() {
        String logLine = "13.086: [GC13.086: [ParNew: 272640K->33532K(306688K), 0.0381419 secs] "
                + "272640K->33532K(1014528K), 0.0383306 secs] [Times: user=0.11 sys=0.02, real=0.04 secs]";
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals(13086L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineCmsScavengeBeforeRemark() {
        String logLine = "7236.341: [GC[YG occupancy: 1388745 K (4128768 K)]7236.341: [GC7236.341: [ParNew: "
                + "1388745K->458752K(4128768K), 0.5246295 secs] 2977822K->2161212K(13172736K), 0.5248785 secs] "
                + "[Times: user=0.92 sys=0.03, real=0.51 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 7236341, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(1388745), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(458752), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(4128768), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((2977822 - 1388745)), event.getOldOccupancyInit(),
                "Old begin size not parsed correctly.");
        assertEquals(kilobytes((2161212 - 458752)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((13172736 - 4128768)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(524878, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(92, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(3, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(51, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(187, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineDatestampTimestamp() {
        String logLine = "2010-04-16T12:11:18.979+0200: 84.335: [GC 2010-04-16T12:11:18.979+0200: 84.336: "
                + "[ParNew: 273152K->858K(341376K), 0.0030008 secs] 273152K->858K(980352K), 0.0031183 secs] "
                + "[Times: user=0.00 sys=0.00, real=0.00 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 84335, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(273152), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(858), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(341376), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((273152 - 273152)), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes((858 - 858)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((980352 - 341376)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(3118, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(0, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(100, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineGcLockerTrigger() {
        String logLine = "2.480: [GC (GCLocker Initiated GC) 2.480: [ParNew: 1228800K->30695K(1382400K), "
                + "0.0395910 secs] 1228800K->30695K(8235008K), 0.0397980 secs] "
                + "[Times: user=0.23 sys=0.01, real=0.04 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 2480, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC), "Trigger not parsed correctly.");
        assertEquals(kilobytes(1228800), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(30695), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(1382400), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((1228800 - 1228800)), event.getOldOccupancyInit(),
                "Old begin size not parsed correctly.");
        assertEquals(kilobytes((30695 - 30695)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((8235008 - 1382400)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(39798, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(23, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(4, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(600, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineHugeTimestamp() {
        String logLine = "4687597.901: [GC 4687597.901: [ParNew: 342376K->16369K(368640K), "
                + "0.0865160 secs] 1561683K->1235676K(2056192K), 0.0869060 secs]";
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals(4687597901L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineJdk8NoSpaceAfterTrigger() {
        String logLine = "1.948: [GC (Allocation Failure)1.948: [ParNew: 136576K->17023K(153600K), 0.0303800 secs] "
                + "136576K->19515K(494976K), 0.0305360 secs] [Times: user=0.10 sys=0.01, real=0.03 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 1948, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(136576), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(17023), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(153600), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((136576 - 136576)), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes((19515 - 17023)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((494976 - 153600)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(30536, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(10, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(3, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(367, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineJdk8WithTrigger() {
        String logLine = "6.703: [GC (Allocation Failure) 6.703: [ParNew: 886080K->11485K(996800K), 0.0193349 secs] "
                + "886080K->11485K(1986432K), 0.0198375 secs] [Times: user=0.09 sys=0.01, real=0.02 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 6703, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE), "Trigger not parsed correctly.");
        assertEquals(kilobytes(886080), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(11485), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(996800), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((886080 - 886080)), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes((11485 - 11485)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((1986432 - 996800)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(19837, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(9, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(2, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(500, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineNoSpaceAfterTrigger() {
        String logLine = "78.251: [GC (CMS Final Remark)[YG occupancy: 2619547 K (8388608 K)]"
                + "78.251: [GC (CMS Final Remark)78.251: [ParNew: 2619547K->569438K(8388608K), 0.3405110 secs] "
                + "6555444K->5043068K(22020096K) icms_dc=100 , 0.3406250 secs] "
                + "[Times: user=2.12 sys=0.01, real=0.34 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 78251, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK), "Trigger not parsed correctly.");
        assertEquals(kilobytes(2619547), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(569438), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(8388608), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((6555444 - 2619547)), event.getOldOccupancyInit(),
                "Old begin size not parsed correctly.");
        assertEquals(kilobytes((5043068 - 569438)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((22020096 - 8388608)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(340625, event.getDuration(), "Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(212, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(34, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(627, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLinePromotionFailed() {
        String logLine = "393747.603: [GC393747.603: [ParNew (promotion failed): 476295K->476295K(4128768K), "
                + "0.5193071 secs] 7385012K->7555732K(13172736K), 0.5196411 secs] "
                + "[Times: user=0.92 sys=0.00, real=0.55 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 393747603, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(476295), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(476295), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(4128768), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((7385012 - 476295)), event.getOldOccupancyInit(),
                "Old begin size not parsed correctly.");
        assertEquals(kilobytes((7555732 - 476295)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((13172736 - 4128768)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(519641, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(92, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(55, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(168, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineSystemGcTrigger() {
        String logLine = "27880.710: [GC (System.gc()) 27880.710: [ParNew: 925502K->58125K(996800K), 0.0133005 secs] "
                + "5606646K->4742781K(8277888K), 0.0138294 secs] [Times: user=0.14 sys=0.00, real=0.02 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 27880710, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC), "Trigger not parsed correctly.");
        assertEquals(kilobytes(925502), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(58125), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(996800), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((5606646 - 925502)), event.getOldOccupancyInit(),
                "Old begin size not parsed correctly.");
        assertEquals(kilobytes((4742781 - 58125)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((8277888 - 996800)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(13829, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(14, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(2, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(700, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineTriggerCmsFinalRemarkJdk8() {
        String logLine = "4.506: [GC (CMS Final Remark) [YG occupancy: 100369 K (153344 K)]"
                + "4.506: [GC (CMS Final Remark) 4.506: [ParNew: 100369K->10116K(153344K), 0.0724021 secs] "
                + "100369K->16685K(4177280K), 0.0724907 secs] [Times: user=0.13 sys=0.01, real=0.07 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 4506, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK), "Trigger not parsed correctly.");
        assertEquals(kilobytes(100369), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(10116), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(153344), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((100369 - 100369)), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes((16685 - 10116)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((4177280 - 153344)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(72490, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(13, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(7, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(200, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineTriggerCmsFinalRemarkJdk8WithTimeStamps() {
        String logLine = "2017-01-07T22:02:15.504+0300: 66.504: [GC (CMS Final Remark)[YG occupancy: 4266790 K "
                + "(8388608 K)]2017-01-07T22:02:15.504+0300: 66.504: [GC (CMS Final Remark)"
                + "2017-01-07T22:02:15.504+0300: 66.504: [ParNew: 4266790K->922990K(8388608K), 0.6540990 secs] "
                + "6417140K->3472610K(22020096K) icms_dc=35 , 0.6542370 secs] "
                + "[Times: user=1.89 sys=0.01, real=0.66 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 66504, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK), "Trigger not parsed correctly.");
        assertEquals(kilobytes(4266790), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(922990), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(8388608), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((6417140 - 4266790)), event.getOldOccupancyInit(),
                "Old begin size not parsed correctly.");
        assertEquals(kilobytes((3472610 - 922990)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((22020096 - 8388608)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(654237, event.getDuration(), "Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(189, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(66, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(288, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineTriggerPromotionFailed() {
        String logLine = "58427.547: [GC (CMS Final Remark)[YG occupancy: 5117539 K (8388608 K)]"
                + "58427.548: [GC (CMS Final Remark)58427.548: [ParNew (promotion failed): "
                + "5117539K->5001473K(8388608K), 27.6557600 secs] 17958061K->18622281K(22020096K) icms_dc=57 , "
                + "27.6560550 secs] [Times: user=49.10 sys=6.01, real=27.65 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 58427547, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals(kilobytes(5117539), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(5001473), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(8388608), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((17958061 - 5117539)), event.getOldOccupancyInit(),
                "Old begin size not parsed correctly.");
        assertEquals(kilobytes((18622281 - 5001473)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((22020096 - 8388608)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(27656055, event.getDuration(), "Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(4910, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(601, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(2765, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(200, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineTriggerScavengeBeforeRemarkNoGcDetailsPreprocessed() {
        String logLine = "2017-04-03T03:12:02.133-0500: 30.385: [GC (CMS Final Remark) 2017-04-03T03:12:02.134-0500: "
                + "30.385: [GC (CMS Final Remark)  890910K->620060K(7992832K), 0.1223879 secs] 620060K(7992832K), "
                + "0.2328529 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 30385, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK), "Trigger not parsed correctly.");
        assertEquals(kilobytes(890910), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(620060), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(7992832), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((620060 - 620060)), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes((620060 - 620060)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((7992832 - 7992832)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(232852, event.getDuration(), "Duration not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "68331.885: [GC 68331.885: [ParNew: 149120K->18211K(149120K), "
                + "0.0458577 secs] 4057776K->3931241K(8367360K), 0.0461448 secs] "
                + "[Times: user=0.34 sys=0.01, real=0.05 secs]    ";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
    }

    @Test
    void testLogLineWithIcmsDcData() {
        String logLine = "42514.965: [GC 42514.966: [ParNew: 54564K->1006K(59008K), 0.0221640 secs] "
                + "417639K->364081K(1828480K) icms_dc=0 , 0.0225090 secs] "
                + "[Times: user=0.05 sys=0.00, real=0.02 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 42514965, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(54564), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(1006), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(59008), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes((417639 - 54564)), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes((364081 - 1006)), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes((1828480 - 59008)), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(22509, event.getDuration(), "Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(5, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(2, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(250, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineWithTimesData() {
        String logLine = "68331.885: [GC 68331.885: [ParNew: 149120K->18211K(149120K), "
                + "0.0458577 secs] 4057776K->3931241K(8367360K), 0.0461448 secs] "
                + "[Times: user=0.34 sys=0.01, real=0.05 secs]";
        assertTrue(ParNewEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals((long) 68331885, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(149120), event.getYoungOccupancyInit(), "Young begin size not parsed correctly.");
        assertEquals(kilobytes(18211), event.getYoungOccupancyEnd(), "Young end size not parsed correctly.");
        assertEquals(kilobytes(149120), event.getYoungSpace(), "Young available size not parsed correctly.");
        assertEquals(kilobytes(3908656), event.getOldOccupancyInit(), "Old begin size not parsed correctly.");
        assertEquals(kilobytes(3913030), event.getOldOccupancyEnd(), "Old end size not parsed correctly.");
        assertEquals(kilobytes(8218240), event.getOldSpace(), "Old allocation size not parsed correctly.");
        assertEquals(46144, event.getDuration(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertEquals(34, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(5, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(700, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    /**
     * Test datestamp only logging with passing in JVM start datetime.
     * 
     * @throws IOException
     */
    @Test
    void testParNewDatestampNoTimestampJvmStartDate() throws IOException {
        File testFile = TestUtil.getFile("dataset113.txt");
        Date jvmStartDate = GcUtil.parseDateStamp("2017-02-28T11:26:24.135+0100");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, jvmStartDate, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        assertFalse(jvmRun.hasAnalysis(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED),
                Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED + " analysis incorrectly identified.");
    }

    /**
     * Test datestamp only logging without passing in JVM start datetime.
     * 
     * @throws IOException
     */
    @Test
    void testParNewDatestampNoTimestampNoJvmStartDate() throws IOException {
        File testFile = TestUtil.getFile("dataset113.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        // Don't report datestamp only lines unidentified
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE),
                Analysis.ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE + " analysis incorrectly identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST),
                Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST + " analysis incorrectly identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT),
                Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT + " analysis incorrectly identified.");
    }

    /**
     * Test preprocessing a split <code>ParNewCmsConcurrentEvent</code> that does not include the "concurrent mode
     * failure" text.
     * 
     * @throws IOException
     */
    @Test
    void testSplitParNewCmsConcurrentEventAbortablePrecleanLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset15.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }
}
