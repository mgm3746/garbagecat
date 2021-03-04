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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Date;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.Jvm;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParNewEvent {

    @Test
    public void testIsBlocking() {
        String logLine = "20.189: [GC 20.190: [ParNew: 86199K->8454K(91712K), 0.0375060 secs] "
                + "89399K->11655K(907328K), 0.0387074 secs]";
        assertTrue(JdkUtil.LogEventType.PAR_NEW.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLine() {
        String logLine = "20.189: [GC 20.190: [ParNew: 86199K->8454K(91712K), 0.0375060 secs] "
                + "89399K->11655K(907328K), 0.0387074 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 20189, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(86199), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(8454), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(91712), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(3200), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(3201), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(815616), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 38707, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    @Test
    public void testLogLineWithTimesData() {
        String logLine = "68331.885: [GC 68331.885: [ParNew: 149120K->18211K(149120K), "
                + "0.0458577 secs] 4057776K->3931241K(8367360K), 0.0461448 secs] "
                + "[Times: user=0.34 sys=0.01, real=0.05 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 68331885, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(149120), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(18211), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(149120), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes(3908656), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes(3913030), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes(8218240), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 46144, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 34, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 5, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 700, event.getParallelism());
    }

    @Test
    public void testLogLineWithIcmsDcData() {
        String logLine = "42514.965: [GC 42514.966: [ParNew: 54564K->1006K(59008K), 0.0221640 secs] "
                + "417639K->364081K(1828480K) icms_dc=0 , 0.0225090 secs] "
                + "[Times: user=0.05 sys=0.00, real=0.02 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 42514965, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(54564), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(1006), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(59008), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((417639 - 54564)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((364081 - 1006)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((1828480 - 59008)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 22509, event.getDuration());
        assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 5, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 250, event.getParallelism());
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "68331.885: [GC 68331.885: [ParNew: 149120K->18211K(149120K), "
                + "0.0458577 secs] 4057776K->3931241K(8367360K), 0.0461448 secs] "
                + "[Times: user=0.34 sys=0.01, real=0.05 secs]    ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
    }

    @Test
    public void testLogLineHugeTimestamp() {
        String logLine = "4687597.901: [GC 4687597.901: [ParNew: 342376K->16369K(368640K), "
                + "0.0865160 secs] 1561683K->1235676K(2056192K), 0.0869060 secs]";
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 4687597901L, event.getTimestamp());
    }

    @Test
    public void testLogLineAfterPreprocessing() {
        String logLine = "13.086: [GC13.086: [ParNew: 272640K->33532K(306688K), 0.0381419 secs] "
                + "272640K->33532K(1014528K), 0.0383306 secs] " + "[Times: user=0.11 sys=0.02, real=0.04 secs]";
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 13086L, event.getTimestamp());
    }

    @Test
    public void testLogLineJdk8WithTrigger() {
        String logLine = "6.703: [GC (Allocation Failure) 6.703: [ParNew: 886080K->11485K(996800K), 0.0193349 secs] "
                + "886080K->11485K(1986432K), 0.0198375 secs] [Times: user=0.09 sys=0.01, real=0.02 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 6703, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Young begin size not parsed correctly.", kilobytes(886080), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(11485), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(996800), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((886080 - 886080)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((11485 - 11485)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((1986432 - 996800)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 19837, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 9, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 500, event.getParallelism());
    }

    @Test
    public void testLogLineJdk8NoSpaceAfterTrigger() {
        String logLine = "1.948: [GC (Allocation Failure)1.948: [ParNew: 136576K->17023K(153600K), 0.0303800 secs] "
                + "136576K->19515K(494976K), 0.0305360 secs] [Times: user=0.10 sys=0.01, real=0.03 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 1948, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Young begin size not parsed correctly.", kilobytes(136576), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(17023), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(153600), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((136576 - 136576)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((19515 - 17023)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((494976 - 153600)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 30536, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 10, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 3, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 367, event.getParallelism());
    }

    @Test
    public void testLogLineGcLockerTrigger() {
        String logLine = "2.480: [GC (GCLocker Initiated GC) 2.480: [ParNew: 1228800K->30695K(1382400K), "
                + "0.0395910 secs] 1228800K->30695K(8235008K), 0.0397980 secs] "
                + "[Times: user=0.23 sys=0.01, real=0.04 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 2480, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(1228800), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(30695), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(1382400), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((1228800 - 1228800)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((30695 - 30695)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((8235008 - 1382400)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 39798, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 23, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 4, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 600, event.getParallelism());
    }

    @Test
    public void testLogLineCmsScavengeBeforeRemark() {
        String logLine = "7236.341: [GC[YG occupancy: 1388745 K (4128768 K)]7236.341: [GC7236.341: [ParNew: "
                + "1388745K->458752K(4128768K), 0.5246295 secs] 2977822K->2161212K(13172736K), 0.5248785 secs] "
                + "[Times: user=0.92 sys=0.03, real=0.51 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 7236341, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(1388745), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(458752), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(4128768), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((2977822 - 1388745)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((2161212 - 458752)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((13172736 - 4128768)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 524878, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 92, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 3, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 51, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 187, event.getParallelism());
    }

    @Test
    public void testLogLineSystemGcTrigger() {
        String logLine = "27880.710: [GC (System.gc()) 27880.710: [ParNew: 925502K->58125K(996800K), 0.0133005 secs] "
                + "5606646K->4742781K(8277888K), 0.0138294 secs] [Times: user=0.14 sys=0.00, real=0.02 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 27880710, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        assertEquals("Young begin size not parsed correctly.", kilobytes(925502), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(58125), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(996800), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((5606646 - 925502)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((4742781 - 58125)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((8277888 - 996800)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 13829, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 14, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 700, event.getParallelism());
    }

    @Test
    public void testLogLinePromotionFailed() {
        String logLine = "393747.603: [GC393747.603: [ParNew (promotion failed): 476295K->476295K(4128768K), "
                + "0.5193071 secs] 7385012K->7555732K(13172736K), 0.5196411 secs] "
                + "[Times: user=0.92 sys=0.00, real=0.55 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 393747603, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(476295), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(476295), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(4128768), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((7385012 - 476295)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((7555732 - 476295)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((13172736 - 4128768)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 519641, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 92, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 55, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 168, event.getParallelism());
    }

    @Test
    public void testLogLineWithDatestamp() {
        String logLine = "2010-04-16T12:11:18.979+0200: 84.335: [GC 84.336: [ParNew: 273152K->858K(341376K), "
                + "0.0030008 secs] 273152K->858K(980352K), 0.0031183 secs] "
                + "[Times: user=0.00 sys=0.00, real=0.00 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 84335, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(273152), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(858), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(341376), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((273152 - 273152)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((858 - 858)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((980352 - 341376)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 3118, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 0, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    @Test
    public void testLogLineWithDatestampNoTimestamp() {
        String logLine = "2017-02-27T07:23:39.571+0100: [GC [ParNew: 2304000K->35161K(2688000K), 0.0759285 secs] "
                + "2304000K->35161K(9856000K), 0.0760907 secs] [Times: user=0.21 sys=0.05, real=0.08 secs]";
        // Datestamp only is handled by preparsing.
        assertFalse("Log line incorrectly recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
    }

    @Test
    public void testLogLineWithDoubleDatestamp() {
        String logLine = "2013-12-09T16:18:17.813+0000: 13.086: [GC2013-12-09T16:18:17.813+0000: 13.086: [ParNew: "
                + "272640K->33532K(306688K), 0.0381419 secs] 272640K->33532K(1014528K), 0.0383306 secs] "
                + "[Times: user=0.11 sys=0.02, real=0.04 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 13086, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", kilobytes(272640), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(33532), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(306688), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((272640 - 272640)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((33532 - 33532)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((1014528 - 306688)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 38330, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 11, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 2, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 4, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 325, event.getParallelism());
    }

    @Test
    public void testLogLineTriggerCmsFinalRemarkJdk8() {
        String logLine = "4.506: [GC (CMS Final Remark) [YG occupancy: 100369 K (153344 K)]"
                + "4.506: [GC (CMS Final Remark) 4.506: [ParNew: 100369K->10116K(153344K), 0.0724021 secs] "
                + "100369K->16685K(4177280K), 0.0724907 secs] [Times: user=0.13 sys=0.01, real=0.07 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 4506, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        assertEquals("Young begin size not parsed correctly.", kilobytes(100369), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(10116), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(153344), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((100369 - 100369)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((16685 - 10116)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((4177280 - 153344)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 72490, event.getDuration());
        assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 13, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 7, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 200, event.getParallelism());
    }

    @Test
    public void testLogLineTriggerCmsFinalRemarkJdk8WithTimeStamps() {
        String logLine = "2017-01-07T22:02:15.504+0300: 66.504: [GC (CMS Final Remark)[YG occupancy: 4266790 K "
                + "(8388608 K)]2017-01-07T22:02:15.504+0300: 66.504: [GC (CMS Final Remark)"
                + "2017-01-07T22:02:15.504+0300: 66.504: [ParNew: 4266790K->922990K(8388608K), 0.6540990 secs] "
                + "6417140K->3472610K(22020096K) icms_dc=35 , 0.6542370 secs] "
                + "[Times: user=1.89 sys=0.01, real=0.66 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 66504, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        assertEquals("Young begin size not parsed correctly.", kilobytes(4266790), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(922990), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(8388608), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((6417140 - 4266790)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((3472610 - 922990)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((22020096 - 8388608)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 654237, event.getDuration());
        assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 189, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 66, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 288, event.getParallelism());
    }

    @Test
    public void testLogLineNoSpaceAfterTrigger() {
        String logLine = "78.251: [GC (CMS Final Remark)[YG occupancy: 2619547 K (8388608 K)]"
                + "78.251: [GC (CMS Final Remark)78.251: [ParNew: 2619547K->569438K(8388608K), 0.3405110 secs] "
                + "6555444K->5043068K(22020096K) icms_dc=100 , 0.3406250 secs] "
                + "[Times: user=2.12 sys=0.01, real=0.34 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 78251, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        assertEquals("Young begin size not parsed correctly.", kilobytes(2619547), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(569438), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(8388608), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((6555444 - 2619547)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((5043068 - 569438)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((22020096 - 8388608)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 340625, event.getDuration());
        assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 212, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 1, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 34, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 627, event.getParallelism());
    }

    @Test
    public void testLogLineTriggerPromotionFailed() {
        String logLine = "58427.547: [GC (CMS Final Remark)[YG occupancy: 5117539 K (8388608 K)]"
                + "58427.548: [GC (CMS Final Remark)58427.548: [ParNew (promotion failed): "
                + "5117539K->5001473K(8388608K), 27.6557600 secs] 17958061K->18622281K(22020096K) icms_dc=57 , "
                + "27.6560550 secs] [Times: user=49.10 sys=6.01, real=27.65 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 58427547, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        assertEquals("Young begin size not parsed correctly.", kilobytes(5117539), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(5001473), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(8388608), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((17958061 - 5117539)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((18622281 - 5001473)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((22020096 - 8388608)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 27656055, event.getDuration());
        assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        assertEquals("User time not parsed correctly.", 4910, event.getTimeUser());
        assertEquals("Sys time not parsed correctly.", 601, event.getTimeSys());
        assertEquals("Real time not parsed correctly.", 2765, event.getTimeReal());
        assertEquals("Parallelism not calculated correctly.", 200, event.getParallelism());
    }

    @Test
    public void testLogLineTriggerScavengeBeforeRemarkNoGcDetailsPreprocessed() {
        String logLine = "2017-04-03T03:12:02.133-0500: 30.385: [GC (CMS Final Remark) 2017-04-03T03:12:02.134-0500: "
                + "30.385: [GC (CMS Final Remark)  890910K->620060K(7992832K), 0.1223879 secs] 620060K(7992832K), "
                + "0.2328529 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 30385, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        assertEquals("Young begin size not parsed correctly.", kilobytes(890910), event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", kilobytes(620060), event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", kilobytes(7992832), event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", kilobytes((620060 - 620060)), event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", kilobytes((620060 - 620060)), event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", kilobytes((7992832 - 7992832)), event.getOldSpace());
        assertEquals("Duration not parsed correctly.", 232852, event.getDuration());
    }

    /**
     * Test preprocessing a split <code>ParNewCmsConcurrentEvent</code> that does not include the "concurrent mode
     * failure" text.
     */
    @Test
    public void testSplitParNewCmsConcurrentEventAbortablePrecleanLogging() {
        File testFile = TestUtil.getFile("dataset15.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
    }

    /**
     * Test identifying <code>ParNewEvent</code> running in incremental mode.
     */
    @Test
    public void testCmsIncrementalModeAnalysis() {
        File testFile = TestUtil.getFile("dataset68.txt");
        String jvmOptions = "Xss128k -XX:+CMSIncrementalMode -XX:CMSInitiatingOccupancyFraction=70 -Xms2048M";
        Jvm jvm = new Jvm(jvmOptions, null);
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_CMS_INCREMENTAL_MODE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INCREMENTAL_MODE));
        assertTrue(Analysis.WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT));
    }

    /**
     * Test datestamp only logging without passing in JVM start datetime.
     */
    @Test
    public void testParNewDatestampNoTimestampNoJvmStartDate() {
        File testFile = TestUtil.getFile("dataset113.txt");
        Jvm jvm = new Jvm(null, null);
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.ERROR_DATESTAMP_NO_TIMESTAMP + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_DATESTAMP_NO_TIMESTAMP));
        // Don't report datestamp only lines unidentified
        assertFalse(Analysis.ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE));
        assertFalse(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST));
        assertFalse(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT));
    }

    /**
     * Test datestamp only logging with passing in JVM start datetime.
     */
    @Test
    public void testParNewDatestampNoTimestampJvmStartDate() {
        File testFile = TestUtil.getFile("dataset113.txt");
        Date jvmStartDate = GcUtil.parseStartDateTime("2017-02-28 11:26:24,135");
        Jvm jvm = new Jvm(null, jvmStartDate);
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, jvmStartDate);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        assertFalse(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED));
    }
}
