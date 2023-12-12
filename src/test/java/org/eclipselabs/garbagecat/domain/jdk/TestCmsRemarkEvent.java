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
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestCmsRemarkEvent {

    @Test
    void testIsBlocking() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.CMS_REMARK.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 253103, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(85701, event.getDurationMicros(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertFalse(event.isClassUnloading(), "Class unloading not parsed correctly.");
    }

    @Test
    void testLogLineAllDatestamps() {
        String logLine = "2017-03-04T05:36:05.691-0500: 214.303: [GC[YG occupancy: 1674105 K (2752512 K)]"
                + "2017-03-04T05:36:05.691-0500: 214.303: [Rescan (parallel) , 0.2958890 secs]"
                + "2017-03-04T05:36:05.987-0500: 214.599: [weak refs processing, 0.0046990 secs]"
                + "2017-03-04T05:36:05.992-0500: 214.604: [scrub string table, 0.0023080 secs] "
                + "[1 CMS-remark: 6775345K(7340032K)] 8449451K(10092544K), 0.3035200 secs] "
                + "[Times: user=0.98 sys=0.01, real=0.31 secs] ";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 214303, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(303520, event.getDurationMicros(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertFalse(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(98, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(31, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(320, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineClassUnloading() {
        String logLine = "76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: "
                + "[Rescan (parallel) , 0.0574180 secs]76694.785: [weak refs processing, 0.0170540 secs]76694.802: "
                + "[class unloading, 0.0363010 secs]76694.838: [scrub symbol & string tables, 0.0276600 secs] "
                + "[1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 76694727, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(144688, event.getDurationMicros(), "Duration not parsed correctly.");
        assertTrue(event.isClassUnloading(), "Class unloading not parsed correctly.");
    }

    @Test
    void testLogLineClassUnloadingDatestampTimestamp() {
        String logLine = "2016-10-10T18:43:51.337-0700: 3.674: [GC (CMS Final Remark) [YG occupancy: 87907 K "
                + "(153344 K)]2016-10-10T18:43:51.337-0700: 3.674: [Rescan (parallel) , 0.0590379 secs]"
                + "2016-10-10T18:43:51.396-0700: 3.733: [weak refs processing, 0.0000785 secs]"
                + "2016-10-10T18:43:51.396-0700: 3.733: [class unloading, 0.0102437 secs]"
                + "2016-10-10T18:43:51.407-0700: 3.744: [scrub symbol table, 0.0208682 secs]"
                + "2016-10-10T18:43:51.428-0700: 3.765: [scrub string table, 0.0013969 secs]"
                + "[1 CMS-remark: 6993K(8218240K)] 94901K(8371584K), 0.0935737 secs] "
                + "[Times: user=0.26 sys=0.01, real=0.09 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 3674, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.CMS_FINAL_REMARK, "Trigger not parsed correctly.");
        assertEquals(93573, event.getDurationMicros(), "Duration not parsed correctly.");
        assertTrue(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(26, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(9, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(300, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineClassUnloadingJdk7() {
        String logLine = "75.500: [GC[YG occupancy: 163958 K (306688 K)]75.500: [Rescan (parallel) , 0.0491823 secs]"
                + "75.549: [weak refs processing, 0.0088472 secs]75.558: [class unloading, 0.0049468 secs]75.563: "
                + "[scrub symbol table, 0.0034342 secs]75.566: [scrub string table, 0.0005542 secs] [1 CMS-remark: "
                + "378031K(707840K)] 541989K(1014528K), 0.0687411 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 75500, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(68741, event.getDurationMicros(), "Duration not parsed correctly.");
        assertTrue(event.isClassUnloading(), "Class unloading not parsed correctly.");
    }

    @Test
    void testLogLineClassUnloadingJdk7NonParallelRescan() {
        String logLine = "7.294: [GC[YG occupancy: 42599 K (76672 K)]7.294: [Rescan (non-parallel) 7.294: "
                + "[grey object rescan, 0.0049340 secs]7.299: [root rescan, 0.0230250 secs], 0.0280700 secs]7.322: "
                + "[weak refs processing, 0.0001950 secs]7.322: [class unloading, 0.0034660 secs]7.326: "
                + "[scrub symbol table, 0.0047330 secs]7.330: [scrub string table, 0.0006570 secs] "
                + "[1 CMS-remark: 7720K(1249088K)] 50319K(1325760K), 0.0375310 secs] "
                + "[Times: user=0.03 sys=0.01, real=0.03 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 7294, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(37531, event.getDurationMicros(), "Duration not parsed correctly.");
        assertTrue(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(3, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(3, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(134, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineClassUnloadingJdk7WithTimesData() {
        String logLine = "75.500: [GC[YG occupancy: 163958 K (306688 K)]75.500: [Rescan (parallel) , 0.0491823 secs]"
                + "75.549: [weak refs processing, 0.0088472 secs]75.558: [class unloading, 0.0049468 secs]75.563: "
                + "[scrub symbol table, 0.0034342 secs]75.566: [scrub string table, 0.0005542 secs] [1 CMS-remark: "
                + "378031K(707840K)] 541989K(1014528K), 0.0687411 secs] [Times: user=0.13 sys=0.00, real=0.07 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
    }

    @Test
    void testLogLineClassUnloadingJdk8WithTrigger() {
        String logLine = "13.758: [GC (CMS Final Remark) [YG occupancy: 235489 K (996800 K)]13.758: "
                + "[Rescan (parallel) , 0.0268664 secs]13.785: [weak refs processing, 0.0000365 secs]13.785: "
                + "[class unloading, 0.0058936 secs]13.791: [scrub symbol table, 0.0081277 secs]13.799: "
                + "[scrub string table, 0.0007018 secs][1 CMS-remark: 0K(989632K)] 235489K(1986432K), 0.0430349 secs] "
                + "[Times: user=0.36 sys=0.00, real=0.04 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 13758, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.CMS_FINAL_REMARK, "Trigger not parsed correctly.");
        assertEquals(43034, event.getDurationMicros(), "Duration not parsed correctly.");
        assertTrue(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(36, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(4, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(900, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineClassUnloadingNoSpaceAfterTrigger() {
        String logLine = "38.695: [GC (CMS Final Remark)[YG occupancy: 4251867 K (8388608 K)]"
                + "38.695: [Rescan (parallel) , 0.5678440 secs]39.263: [weak refs processing, 0.0000540 secs]"
                + "39.263: [class unloading, 0.0065460 secs]39.270: [scrub symbol table, 0.0118150 secs]"
                + "39.282: [scrub string table, 0.0020090 secs] "
                + "[1 CMS-remark: 0K(13631488K)] 4251867K(22020096K), 0.5893800 secs] "
                + "[Times: user=3.92 sys=0.04, real=0.59 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 38695, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.CMS_FINAL_REMARK, "Trigger not parsed correctly.");
        assertEquals(589380, event.getDurationMicros(), "Duration not parsed correctly.");
        assertTrue(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(392, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(4, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(59, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(672, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineClassUnloadingWhitespaceAtEnd() {
        String logLine = "76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: "
                + "[Rescan (parallel) , 0.0574180 secs]76694.785: [weak refs processing, 0.0170540 secs]76694.802: "
                + "[class unloading, 0.0363010 secs]76694.838: [scrub symbol & string tables, 0.0276600 secs] "
                + "[1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs]     ";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
    }

    @Test
    void testLogLineClassUnloadingWithTimesData() {
        String logLine = "76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: "
                + "[Rescan (parallel) , 0.0574180 secs]76694.785: [weak refs processing, 0.0170540 secs]76694.802: "
                + "[class unloading, 0.0363010 secs]76694.838: [scrub symbol & string tables, 0.0276600 secs] "
                + "[1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs] "
                + "[Times: user=0.13 sys=0.00, real=0.07 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 76694727, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(144688, event.getDurationMicros(), "Duration not parsed correctly.");
        assertTrue(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(13, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(7, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(186, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineDatestamp() {
        String logLine = "2016-10-27T19:06:06.651-0400: [GC[YG occupancy: 480317 K (5505024 K)]6.458: "
                + "[Rescan (parallel) , 0.0103480 secs]6.469: [weak refs processing, 0.0000110 secs]6.469: "
                + "[scrub string table, 0.0001750 secs] [1 CMS-remark: 0K(37748736K)] 480317K(43253760K), "
                + "0.0106300 secs] [Times: user=0.23 sys=0.01, real=0.01 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals(530906766651L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testLogLineDatestampTimestamp() {
        String logLine = "2016-10-27T19:06:06.651-0400: 6.458: [GC[YG occupancy: 480317 K (5505024 K)]6.458: "
                + "[Rescan (parallel) , 0.0103480 secs]6.469: [weak refs processing, 0.0000110 secs]6.469: "
                + "[scrub string table, 0.0001750 secs] [1 CMS-remark: 0K(37748736K)] 480317K(43253760K), "
                + "0.0106300 secs] [Times: user=0.23 sys=0.01, real=0.01 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 6458, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(10630, event.getDurationMicros(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertFalse(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(23, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(1, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(2400, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineDetails() {
        String logLine = "2021-11-27T09:12:25.283-0500: 0.078: [GC (CMS Final Remark) [YG occupancy: 188 K (1152 K)]"
                + "2021-11-27T09:12:25.283-0500: 0.078: [Rescan (non-parallel) "
                + "2021-11-27T09:12:25.283-0500: 0.078: [grey object rescan, 0.0000023 secs]"
                + "2021-11-27T09:12:25.283-0500: 0.078: [root rescan, 0.0002962 secs]"
                + "2021-11-27T09:12:25.284-0500: 0.078: [visit unhandled CLDs, 0.0000026 secs]"
                + "2021-11-27T09:12:25.284-0500: 0.078: [dirty klass scan, 0.0000123 secs], 0.0003211 secs]"
                + "2021-11-27T09:12:25.284-0500: 0.078: [weak refs processing, 0.0000145 secs]"
                + "2021-11-27T09:12:25.284-0500: 0.078: [class unloading, 0.0001938 secs]"
                + "2021-11-27T09:12:25.284-0500: 0.079: [scrub symbol table, 0.0004021 secs]"
                + "2021-11-27T09:12:25.284-0500: 0.079: [scrub string table, 0.0001095 secs]"
                + "[1 CMS-remark: 509K(768K)] 697K(1920K), 0.0010805 secs] "
                + "[Times: user=0.00 sys=0.00, real=0.01 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 78, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.CMS_FINAL_REMARK, "Trigger not parsed correctly.");
        assertEquals(1080, event.getDurationMicros(), "Duration not parsed correctly.");
        assertTrue(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(0, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(1, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(0, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineJdk8NoGcYgBlockClassUnloading() {
        String logLine = "4.578: [Rescan (parallel) , 0.0185521 secs]4.597: [weak refs processing, 0.0008993 secs]"
                + "4.598: [class unloading, 0.0046742 secs]4.603: [scrub symbol table, 0.0044444 secs]"
                + "4.607: [scrub string table, 0.0005670 secs][1 CMS-remark: 6569K(4023936K)] 16685K(4177280K), "
                + "0.1025102 secs] [Times: user=0.17 sys=0.01, real=0.10 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 4578, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(102510, event.getDurationMicros(), "Duration not parsed correctly.");
        assertTrue(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(17, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(10, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(180, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineJdk8NoGcYgBlockNoClassUnloading() {
        String logLine = "4237.354: [Rescan (parallel) , 0.1378986 secs]4237.492: [weak refs processing, "
                + "0.1842394 secs] [1 CMS-remark: 4271964K(8388608K)] 4271964K(12582848K), 0.4124068 secs] "
                + "[Times: user=2.82 sys=0.04, real=0.41 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 4237354, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(412406, event.getDurationMicros(), "Duration not parsed correctly.");
        assertFalse(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(282, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(4, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(41, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(698, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineJdk8WithTriggerAndDatestamps() {
        String logLine = "13.749: [GC (CMS Final Remark)[YG occupancy: 149636 K (153600 K)]13.749: "
                + "[Rescan (parallel) , 0.0216980 secs]13.771: [weak refs processing, 0.0005180 secs]13.772: "
                + "[scrub string table, 0.0015820 secs] [1 CMS-remark: 217008K(341376K)] "
                + "366644K(494976K), 0.0239510 secs] [Times: user=0.18 sys=0.00, real=0.02 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 13749, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.CMS_FINAL_REMARK, "Trigger not parsed correctly.");
        assertEquals(23951, event.getDurationMicros(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertFalse(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(18, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(2, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(900, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineSpaceBeforeYgBlockAndNoSpaceBeforeCmsRemarkBlock() {
        String logLine = "61.013: [GC (CMS Final Remark) [YG occupancy: 237181 K (471872 K)]61.014: [Rescan (parallel)"
                + " , 0.0335675 secs]61.047: [weak refs processing, 0.0011687 secs][1 CMS-remark: 1137616K(1572864K)] "
                + "1374798K(2044736K), 0.0351204 secs] [Times: user=0.12 sys=0.00, real=0.04 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 61013, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(35120, event.getDurationMicros(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertFalse(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(12, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(0, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(4, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(300, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testLogLineTruncated() {
        String logLine = "2017-09-15T09:53:41.262+0200: 19763.069: [GC (CMS Final Remark) "
                + "[YG occupancy: 425526 K (613440 K)]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 19763069, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.CMS_FINAL_REMARK, "Trigger not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
        assertFalse(event.isClassUnloading(), "Class unloading not parsed correctly.");
    }

    @Test
    void testLogLineTruncatedDatestamp() {
        String logLine = "2017-09-15T09:53:41.262+0200: [GC (CMS Final Remark) [YG occupancy: 425526 K (613440 K)]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals(558759221262L, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertTrue(event.getTrigger() == GcTrigger.CMS_FINAL_REMARK, "Trigger not parsed correctly.");
        assertEquals(0, event.getDurationMicros(), "Duration not parsed correctly.");
        assertFalse(event.isClassUnloading(), "Class unloading not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]  ";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
    }

    @Test
    void testLogLineWithTimesData() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs] "
                + "[Times: user=0.15 sys=0.01, real=0.09 secs]";
        assertTrue(CmsRemarkEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        assertEquals((long) 253103, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(85701, event.getDurationMicros(), "Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
        assertFalse(event.isClassUnloading(), "Class unloading not parsed correctly.");
        assertEquals(15, event.getTimeUser(), "User time not parsed correctly.");
        assertEquals(1, event.getTimeSys(), "Sys time not parsed correctly.");
        assertEquals(9, event.getTimeReal(), "Real time not parsed correctly.");
        assertEquals(178, event.getParallelism(), "Parallelism not calculated correctly.");
    }

    @Test
    void testTruncatedPreprocessing() throws IOException {
        File testFile = TestUtil.getFile("dataset142.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(7, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS),
                JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY),
                JdkUtil.LogEventType.HEADER_MEMORY.toString() + " not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.HEADER_VM_INFO),
                JdkUtil.LogEventType.HEADER_VM_INFO.toString() + " not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                JdkUtil.LogEventType.PAR_NEW.toString() + " not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_REMARK),
                JdkUtil.LogEventType.CMS_REMARK.toString() + " not identified.");
    }
}
