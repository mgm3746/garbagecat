/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.io.File;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestCmsRemarkEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.CMS_REMARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 253103, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 85701, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertFalse("Class unloading not parsed correctly.", event.isClassUnloading());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]  ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
    }

    public void testLogLineWithTimesData() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs] "
                + "[Times: user=0.15 sys=0.01, real=0.09 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 253103, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 85701, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertFalse("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 15, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 9, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 167, event.getParallelism());
    }

    public void testLogLineJdk8WithTriggerAndDatestamps() {
        String logLine = "13.749: [GC (CMS Final Remark)[YG occupancy: 149636 K (153600 K)]13.749: "
                + "[Rescan (parallel) , 0.0216980 secs]13.771: [weak refs processing, 0.0005180 secs]13.772: "
                + "[scrub string table, 0.0015820 secs] [1 CMS-remark: 217008K(341376K)] "
                + "366644K(494976K), 0.0239510 secs] [Times: user=0.18 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 13749, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        Assert.assertEquals("Duration not parsed correctly.", 23951, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertFalse("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 18, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 900, event.getParallelism());
    }

    public void testLogLineDatestamp() {
        String logLine = "2016-10-27T19:06:06.651-0400: 6.458: [GC[YG occupancy: 480317 K (5505024 K)]6.458: "
                + "[Rescan (parallel) , 0.0103480 secs]6.469: [weak refs processing, 0.0000110 secs]6.469: "
                + "[scrub string table, 0.0001750 secs] [1 CMS-remark: 0K(37748736K)] 480317K(43253760K), "
                + "0.0106300 secs] [Times: user=0.23 sys=0.01, real=0.01 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6458, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 10630, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertFalse("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 23, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 2300, event.getParallelism());
    }

    public void testLogLineAllDatestamps() {
        String logLine = "2017-03-04T05:36:05.691-0500: 214.303: [GC[YG occupancy: 1674105 K (2752512 K)]"
                + "2017-03-04T05:36:05.691-0500: 214.303: [Rescan (parallel) , 0.2958890 secs]"
                + "2017-03-04T05:36:05.987-0500: 214.599: [weak refs processing, 0.0046990 secs]"
                + "2017-03-04T05:36:05.992-0500: 214.604: [scrub string table, 0.0023080 secs] "
                + "[1 CMS-remark: 6775345K(7340032K)] 8449451K(10092544K), 0.3035200 secs] "
                + "[Times: user=0.98 sys=0.01, real=0.31 secs] ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 214303, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 303520, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertFalse("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 98, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 31, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 317, event.getParallelism());
    }

    public void testLogLineSpaceBeforeYgBlockAndNoSpaceBeforeCmsRemarkBlock() {
        String logLine = "61.013: [GC (CMS Final Remark) [YG occupancy: 237181 K (471872 K)]61.014: [Rescan (parallel)"
                + " , 0.0335675 secs]61.047: [weak refs processing, 0.0011687 secs][1 CMS-remark: 1137616K(1572864K)] "
                + "1374798K(2044736K), 0.0351204 secs] [Times: user=0.12 sys=0.00, real=0.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 61013, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 35120, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertFalse("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 12, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 4, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 300, event.getParallelism());
    }

    public void testLogLineClassUnloading() {
        String logLine = "76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: "
                + "[Rescan (parallel) , 0.0574180 secs]76694.785: [weak refs processing, 0.0170540 secs]76694.802: "
                + "[class unloading, 0.0363010 secs]76694.838: [scrub symbol & string tables, 0.0276600 secs] "
                + "[1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 76694727, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 144688, event.getDuration());
        Assert.assertTrue("Class unloading not parsed correctly.", event.isClassUnloading());
    }

    public void testLogLineClassUnloadingWhitespaceAtEnd() {
        String logLine = "76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: "
                + "[Rescan (parallel) , 0.0574180 secs]76694.785: [weak refs processing, 0.0170540 secs]76694.802: "
                + "[class unloading, 0.0363010 secs]76694.838: [scrub symbol & string tables, 0.0276600 secs] "
                + "[1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs]     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
    }

    public void testLogLineClassUnloadingWithTimesData() {
        String logLine = "76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: "
                + "[Rescan (parallel) , 0.0574180 secs]76694.785: [weak refs processing, 0.0170540 secs]76694.802: "
                + "[class unloading, 0.0363010 secs]76694.838: [scrub symbol & string tables, 0.0276600 secs] "
                + "[1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs] "
                + "[Times: user=0.13 sys=0.00, real=0.07 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 76694727, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 144688, event.getDuration());
        Assert.assertTrue("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 13, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 7, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 186, event.getParallelism());
    }

    public void testLogLineClassUnloadingJdk7() {
        String logLine = "75.500: [GC[YG occupancy: 163958 K (306688 K)]75.500: [Rescan (parallel) , 0.0491823 secs]"
                + "75.549: [weak refs processing, 0.0088472 secs]75.558: [class unloading, 0.0049468 secs]75.563: "
                + "[scrub symbol table, 0.0034342 secs]75.566: [scrub string table, 0.0005542 secs] [1 CMS-remark: "
                + "378031K(707840K)] 541989K(1014528K), 0.0687411 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 75500, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 68741, event.getDuration());
        Assert.assertTrue("Class unloading not parsed correctly.", event.isClassUnloading());
    }

    public void testLogLineClassUnloadingJdk7WithTimesData() {
        String logLine = "75.500: [GC[YG occupancy: 163958 K (306688 K)]75.500: [Rescan (parallel) , 0.0491823 secs]"
                + "75.549: [weak refs processing, 0.0088472 secs]75.558: [class unloading, 0.0049468 secs]75.563: "
                + "[scrub symbol table, 0.0034342 secs]75.566: [scrub string table, 0.0005542 secs] [1 CMS-remark: "
                + "378031K(707840K)] 541989K(1014528K), 0.0687411 secs] [Times: user=0.13 sys=0.00, real=0.07 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
    }

    public void testLogLineClassUnloadingJdk8WithTrigger() {
        String logLine = "13.758: [GC (CMS Final Remark) [YG occupancy: 235489 K (996800 K)]13.758: "
                + "[Rescan (parallel) , 0.0268664 secs]13.785: [weak refs processing, 0.0000365 secs]13.785: "
                + "[class unloading, 0.0058936 secs]13.791: [scrub symbol table, 0.0081277 secs]13.799: "
                + "[scrub string table, 0.0007018 secs][1 CMS-remark: 0K(989632K)] 235489K(1986432K), 0.0430349 secs] "
                + "[Times: user=0.36 sys=0.00, real=0.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 13758, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        Assert.assertEquals("Duration not parsed correctly.", 43034, event.getDuration());
        Assert.assertTrue("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 36, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 4, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 900, event.getParallelism());
    }

    public void testLogLineClassUnloadingJdk7NonParallelRescan() {
        String logLine = "7.294: [GC[YG occupancy: 42599 K (76672 K)]7.294: [Rescan (non-parallel) 7.294: "
                + "[grey object rescan, 0.0049340 secs]7.299: [root rescan, 0.0230250 secs], 0.0280700 secs]7.322: "
                + "[weak refs processing, 0.0001950 secs]7.322: [class unloading, 0.0034660 secs]7.326: "
                + "[scrub symbol table, 0.0047330 secs]7.330: [scrub string table, 0.0006570 secs] "
                + "[1 CMS-remark: 7720K(1249088K)] 50319K(1325760K), 0.0375310 secs] "
                + "[Times: user=0.03 sys=0.01, real=0.03 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 7294, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 37531, event.getDuration());
        Assert.assertTrue("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 3, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 3, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testLogLineJdk8NoGcYgBlockClassUnloading() {
        String logLine = "4.578: [Rescan (parallel) , 0.0185521 secs]4.597: [weak refs processing, 0.0008993 secs]"
                + "4.598: [class unloading, 0.0046742 secs]4.603: [scrub symbol table, 0.0044444 secs]"
                + "4.607: [scrub string table, 0.0005670 secs][1 CMS-remark: 6569K(4023936K)] 16685K(4177280K), "
                + "0.1025102 secs] [Times: user=0.17 sys=0.01, real=0.10 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4578, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 102510, event.getDuration());
        Assert.assertTrue("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 17, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 10, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 170, event.getParallelism());
    }

    public void testLogLineJdk8NoGcYgBlockNoClassUnloading() {
        String logLine = "4237.354: [Rescan (parallel) , 0.1378986 secs]4237.492: [weak refs processing, "
                + "0.1842394 secs] [1 CMS-remark: 4271964K(8388608K)] 4271964K(12582848K), 0.4124068 secs] "
                + "[Times: user=2.82 sys=0.04, real=0.41 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4237354, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 412406, event.getDuration());
        Assert.assertFalse("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 282, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 41, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 688, event.getParallelism());
    }

    public void testLogLineClassUnloadingNoSpaceAfterTrigger() {
        String logLine = "38.695: [GC (CMS Final Remark)[YG occupancy: 4251867 K (8388608 K)]"
                + "38.695: [Rescan (parallel) , 0.5678440 secs]39.263: [weak refs processing, 0.0000540 secs]"
                + "39.263: [class unloading, 0.0065460 secs]39.270: [scrub symbol table, 0.0118150 secs]"
                + "39.282: [scrub string table, 0.0020090 secs] "
                + "[1 CMS-remark: 0K(13631488K)] 4251867K(22020096K), 0.5893800 secs] "
                + "[Times: user=3.92 sys=0.04, real=0.59 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 38695, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        Assert.assertEquals("Duration not parsed correctly.", 589380, event.getDuration());
        Assert.assertTrue("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 392, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 59, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 665, event.getParallelism());
    }

    public void testLogLineClassUnloadingDatestamp() {
        String logLine = "2016-10-10T18:43:51.337-0700: 3.674: [GC (CMS Final Remark) [YG occupancy: 87907 K "
                + "(153344 K)]2016-10-10T18:43:51.337-0700: 3.674: [Rescan (parallel) , 0.0590379 secs]"
                + "2016-10-10T18:43:51.396-0700: 3.733: [weak refs processing, 0.0000785 secs]"
                + "2016-10-10T18:43:51.396-0700: 3.733: [class unloading, 0.0102437 secs]"
                + "2016-10-10T18:43:51.407-0700: 3.744: [scrub symbol table, 0.0208682 secs]"
                + "2016-10-10T18:43:51.428-0700: 3.765: [scrub string table, 0.0013969 secs]"
                + "[1 CMS-remark: 6993K(8218240K)] 94901K(8371584K), 0.0935737 secs] "
                + "[Times: user=0.26 sys=0.01, real=0.09 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3674, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        Assert.assertEquals("Duration not parsed correctly.", 93573, event.getDuration());
        Assert.assertTrue("Class unloading not parsed correctly.", event.isClassUnloading());
        Assert.assertEquals("User time not parsed correctly.", 26, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 9, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 289, event.getParallelism());
    }

    public void testLogLineTruncated() {
        String logLine = "2017-09-15T09:53:41.262+0200: 19763.069: [GC (CMS Final Remark) "
                + "[YG occupancy: 425526 K (613440 K)]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 19763069, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        Assert.assertEquals("Duration not parsed correctly.", 0, event.getDuration());
        Assert.assertFalse("Class unloading not parsed correctly.", event.isClassUnloading());
    }

    public void testTruncatedPreprocessing() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset142.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 5, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_MEMORY.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_VERSION.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION));
        Assert.assertTrue(JdkUtil.LogEventType.PAR_NEW.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        Assert.assertTrue(JdkUtil.LogEventType.CMS_REMARK.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_REMARK));
    }
}
