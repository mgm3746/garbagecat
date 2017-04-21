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
import java.util.Date;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParNewEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "20.189: [GC 20.190: [ParNew: 86199K->8454K(91712K), 0.0375060 secs] "
                + "89399K->11655K(907328K), 0.0387074 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.PAR_NEW.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "20.189: [GC 20.190: [ParNew: 86199K->8454K(91712K), 0.0375060 secs] "
                + "89399K->11655K(907328K), 0.0387074 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 20189, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 86199, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 8454, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 91712, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 3200, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 3201, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 815616, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 38, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineWithTimesData() {
        String logLine = "68331.885: [GC 68331.885: [ParNew: 149120K->18211K(149120K), "
                + "0.0458577 secs] 4057776K->3931241K(8367360K), 0.0461448 secs] "
                + "[Times: user=0.34 sys=0.01, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 68331885, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 149120, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 18211, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 149120, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 3908656, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 3913030, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 8218240, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 46, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 34, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 5, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 7, event.getParallelism());
    }

    public void testLogLineWithIcmsDcData() {
        String logLine = "42514.965: [GC 42514.966: [ParNew: 54564K->1006K(59008K), 0.0221640 secs] "
                + "417639K->364081K(1828480K) icms_dc=0 , 0.0225090 secs] "
                + "[Times: user=0.05 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 42514965, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 54564, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 1006, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 59008, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (417639 - 54564), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (364081 - 1006), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (1828480 - 59008), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 22, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 5, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 3, event.getParallelism());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "68331.885: [GC 68331.885: [ParNew: 149120K->18211K(149120K), "
                + "0.0458577 secs] 4057776K->3931241K(8367360K), 0.0461448 secs] "
                + "[Times: user=0.34 sys=0.01, real=0.05 secs]    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
    }

    public void testLogLineHugeTimestamp() {
        String logLine = "4687597.901: [GC 4687597.901: [ParNew: 342376K->16369K(368640K), "
                + "0.0865160 secs] 1561683K->1235676K(2056192K), 0.0869060 secs]";
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4687597901L, event.getTimestamp());
    }

    public void testLogLineAfterPreprocessing() {
        String logLine = "13.086: [GC13.086: [ParNew: 272640K->33532K(306688K), 0.0381419 secs] "
                + "272640K->33532K(1014528K), 0.0383306 secs] " + "[Times: user=0.11 sys=0.02, real=0.04 secs]";
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 13086L, event.getTimestamp());
    }

    public void testLogLineJdk8WithTrigger() {
        String logLine = "6.703: [GC (Allocation Failure) 6.703: [ParNew: 886080K->11485K(996800K), 0.0193349 secs] "
                + "886080K->11485K(1986432K), 0.0198375 secs] [Times: user=0.09 sys=0.01, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6703, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Young begin size not parsed correctly.", 886080, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 11485, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 996800, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (886080 - 886080), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (11485 - 11485), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (1986432 - 996800), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 19, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 9, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 5, event.getParallelism());
    }

    public void testLogLineJdk8NoSpaceAfterTrigger() {
        String logLine = "1.948: [GC (Allocation Failure)1.948: [ParNew: 136576K->17023K(153600K), 0.0303800 secs] "
                + "136576K->19515K(494976K), 0.0305360 secs] [Times: user=0.10 sys=0.01, real=0.03 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1948, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Young begin size not parsed correctly.", 136576, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 17023, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 153600, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (136576 - 136576), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (19515 - 17023), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (494976 - 153600), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 30, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 10, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 3, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 4, event.getParallelism());
    }

    public void testLogLineGcLockerTrigger() {
        String logLine = "2.480: [GC (GCLocker Initiated GC) 2.480: [ParNew: 1228800K->30695K(1382400K), "
                + "0.0395910 secs] 1228800K->30695K(8235008K), 0.0397980 secs] "
                + "[Times: user=0.23 sys=0.01, real=0.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2480, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Young begin size not parsed correctly.", 1228800, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 30695, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1382400, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (1228800 - 1228800), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (30695 - 30695), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (8235008 - 1382400), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 39, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 23, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 4, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 6, event.getParallelism());
    }

    public void testLogLineCmsScavengeBeforeRemark() {
        String logLine = "7236.341: [GC[YG occupancy: 1388745 K (4128768 K)]7236.341: [GC7236.341: [ParNew: "
                + "1388745K->458752K(4128768K), 0.5246295 secs] 2977822K->2161212K(13172736K), 0.5248785 secs] "
                + "[Times: user=0.92 sys=0.03, real=0.51 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 7236341, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1388745, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 458752, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 4128768, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (2977822 - 1388745), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (2161212 - 458752), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (13172736 - 4128768), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 524, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 92, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 51, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 2, event.getParallelism());
    }

    public void testLogLineSystemGcTrigger() {
        String logLine = "27880.710: [GC (System.gc()) 27880.710: [ParNew: 925502K->58125K(996800K), 0.0133005 secs] "
                + "5606646K->4742781K(8277888K), 0.0138294 secs] [Times: user=0.14 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27880710, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        Assert.assertEquals("Young begin size not parsed correctly.", 925502, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 58125, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 996800, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (5606646 - 925502), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (4742781 - 58125), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (8277888 - 996800), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 13, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 14, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 7, event.getParallelism());
    }

    public void testLogLinePromotionFailed() {
        String logLine = "393747.603: [GC393747.603: [ParNew (promotion failed): 476295K->476295K(4128768K), "
                + "0.5193071 secs] 7385012K->7555732K(13172736K), 0.5196411 secs] "
                + "[Times: user=0.92 sys=0.00, real=0.55 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 393747603, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 476295, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 476295, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 4128768, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (7385012 - 476295), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (7555732 - 476295), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (13172736 - 4128768), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 519, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 92, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 55, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 2, event.getParallelism());
    }

    public void testLogLineWithDatestamp() {
        String logLine = "2010-04-16T12:11:18.979+0200: 84.335: [GC 84.336: [ParNew: 273152K->858K(341376K), "
                + "0.0030008 secs] 273152K->858K(980352K), 0.0031183 secs] "
                + "[Times: user=0.00 sys=0.00, real=0.00 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 84335, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 273152, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 858, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 341376, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (273152 - 273152), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (858 - 858), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (980352 - 341376), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", -1, event.getParallelism());
    }

    public void testLogLineWithDatestampNoTimestamp() {
        String logLine = "2017-02-27T07:23:39.571+0100: [GC [ParNew: 2304000K->35161K(2688000K), 0.0759285 secs] "
                + "2304000K->35161K(9856000K), 0.0760907 secs] [Times: user=0.21 sys=0.05, real=0.08 secs]";
        // Datestamp only is handled by preparsing.
        Assert.assertFalse("Log line incorrectly recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
    }

    public void testLogLineWithDoubleDatestamp() {
        String logLine = "2013-12-09T16:18:17.813+0000: 13.086: [GC2013-12-09T16:18:17.813+0000: 13.086: [ParNew: "
                + "272640K->33532K(306688K), 0.0381419 secs] 272640K->33532K(1014528K), 0.0383306 secs] "
                + "[Times: user=0.11 sys=0.02, real=0.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 13086, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 272640, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 33532, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 306688, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (272640 - 272640), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (33532 - 33532), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (1014528 - 306688), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 38, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
        Assert.assertEquals("User time not parsed correctly.", 11, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 4, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 3, event.getParallelism());
    }

    /**
     * Test preprocessing a split <code>ParNewCmsConcurrentEvent</code> that does not include the
     * "concurrent mode failure" text.
     */
    public void testSplitParNewCmsConcurrentEventAbortablePrecleanLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset15.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
    }

    /**
     * Test identifying <code>ParNewEvent</code> running in incremental mode.
     */
    public void testCmsIncrementalModeAnalysis() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset68.txt");
        String jvmOptions = "Xss128k -XX:+CMSIncrementalMode -XX:CMSInitiatingOccupancyFraction=70 -Xms2048M";
        Jvm jvm = new Jvm(jvmOptions, null);
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_CMS_INCREMENTAL_MODE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INCREMENTAL_MODE));
        Assert.assertTrue(Analysis.WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT));
    }

    /**
     * Test datestamp only logging without passing in JVM start datetime.
     */
    public void testParNewDatestampNoTimestampNoJvmStartDate() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset113.txt");
        Jvm jvm = new Jvm(null, null);
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.ERROR_DATESTAMP_NO_TIMESTAMP + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_DATESTAMP_NO_TIMESTAMP));
        // Don't report datestamp only lines unidentified
        Assert.assertFalse(Analysis.ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE));
        Assert.assertFalse(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST));
        Assert.assertFalse(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT));
    }

    /**
     * Test datestamp only logging with passing in JVM start datetime.
     */
    public void testParNewDatestampNoTimestampJvmStartDate() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset113.txt");
        Date jvmStartDate = GcUtil.parseStartDateTime("2017-02-28 11:26:24,135");
        Jvm jvm = new Jvm(null, jvmStartDate);
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, jvmStartDate);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        Assert.assertFalse(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED));
    }
}
