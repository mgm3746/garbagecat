/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

import java.io.File;
import java.util.Calendar;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.Jvm;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJdkUtil extends TestCase {

    /**
     * Test parsing logging with -XX:+PrintGCApplicationConcurrentTime and -XX:+PrintGCApplicationStoppedTime output.
     */
    public void testParseLoggingWithApplicationTime() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset3.txt");
        GcManager jvmManager = new GcManager();
        jvmManager.store(testFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertEquals("Should not be any unidentified log lines.", 0, jvmRun.getUnidentifiedLogLines().size());
    }

    /**
     * Test preprocessing a split <code>ParNewPromotionFailedCmsConcurrentModeFailureEvent</code>.
     */
    public void testSplitParNewPromotionFailedCmsConcurrentModeFailure() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset5.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test preprocessing <code>PrintHeapAtGcPreprocessAction</code> with underlying <code>CmsSerialOldEvent</code>.
     */
    public void testSplitPrintHeapAtGcCmsSerialOldEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset6.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
    }

    /**
     * Test preprocessing <code>PrintHeapAtGcPreprocessAction</code> with underlying
     * <code>ParNewConcurrentModeFailureEvent</code>.
     */
    public void testSplitPrintHeapAtGcParNewConcurrentModeFailureEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset7.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test preprocessing <code>PrintHeapAtGcPreprocessAction</code> with underlying
     * <code>CmsSerialOldConcurrentModeFailureEvent</code>.
     */
    public void testSplitPrintHeapAtGcCmsSerialOldConcurrentModeFailureEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset8.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString()
                        + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test preprocessing <code>PrintHeapAtGcPreprocessAction</code> with underlying
     * <code>ParNewConcurrentModeFailureEvent</code>.
     */
    public void testSplitPrintHeapAtGcParNewPromotionFailedCmsConcurrentModeFailureEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset21.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code> with underlying <code>ParallelSerialOldEvent</code>.
     */
    public void testSplitParallelSerialOldEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset9.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD));
    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code> with underlying <code>ParallelOldCompactingEvent</code>.
     */
    public void testSplitParallelOldCompactingEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset28.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING.toString() + ".", jvmRun
                        .getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING));
    }

    /**
     * Test preprocessing a split <code>CmsSerialOldEventConcurrentModeFailureEvent</code>.
     */
    public void testSplitCmsConcurrentModeFailureEventMarkLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset10.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString()
                        + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test preprocessing a split <code>CmsSerialOldConcurrentModeFailureEvent</code>.
     */
    public void testSplitCmsConcurrentModeFailureEventAbortablePrecleanLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset11.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString()
                        + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test preprocessing a split <code>ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent</code>.
     */
    public void testSplitParNewPromotionFailedCmsConcurrentModeFailurePermData() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset12.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA
                                .toString() + ".",
                jvmRun.getEventTypes().contains(
                        JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA));
    }

    /**
     * Test preprocessing a split <code>ParNewCmsConcurrentModeFailurePermDataEvent</code>.
     */
    public void testSplitParNewCmsConcurrentModeFailurePermData() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset13.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA));
    }

    /**
     * Test preprocessing <code>CmsSerialOldConcurrentModeFailureEvent</code> split over 3 lines.
     */
    public void testSplit3LinesCmsConcurrentModeFailureEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset14.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString()
                        + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test preprocessing a split <code>ParNewCmsConcurrentEvent</code> that does not include the
     * "concurrent mode failure" text.
     */
    public void testSplitParNewCmsConcurrentEventAbortablePrecleanLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset15.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW_CMS_CONCURRENT));
    }

    /**
     * Test preprocessing <code>ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent</code> split over 3 lines.
     */
    public void testSplit3LinesParNewPromotionFailedCmsConcurrentModeFailurePermDataEventMarkLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset16.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA
                                .toString() + ".",
                jvmRun.getEventTypes().contains(
                        JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA));
    }

    /**
     * Test preprocessing <code>PrintTenuringDistributionPreprocessAction</code> with underlying
     * <code>SerialEvent</code>.
     */
    public void testSplitSerialEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset17.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.SERIAL));
    }

    /**
     * Test preprocessing <code>PrintTenuringDistributionPreprocessAction</code> with underlying
     * <code>ParallelScavengeEvent</code>.
     */
    public void testSplitParallelScavengeEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset30.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SCAVENGE));
    }

    public void testConvertLogEntryTimestampsToDate() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        String logLine = "20.189: [GC 20.190: [ParNew: 86199K->8454K(91712K), 0.0375060 secs] "
                + "89399K->11655K(907328K), 0.0387074 secs]";
        String logLineConverted = "1966-08-18 19:22:04,201: [GC 1966-08-18 19:22:04,202: [ParNew: 86199K->8454K(91712K), "
                + "0.0375060 secs] 89399K->11655K(907328K), 0.0387074 secs]";
        Assert.assertEquals("Timestamps not converted to date/time correctly", logLineConverted,
                JdkUtil.convertLogEntryTimestampsToDateStamp(logLine, calendar.getTime()));
    }

    /**
     * Test preprocessing a split <code>ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent</code> with
     * -XX:+PrintTenuringDistribution logging between the initial and final lines.
     */
    public void testSplitMixedTenuringParNewPromotionFailedEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset18.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA
                                .toString() + ".",
                jvmRun.getEventTypes().contains(
                        JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA));
    }

    /**
     * Test preprocessing a combined <code>CmsConcurrentEvent</code> and <code>ApplicationConcurrentTimeEvent</code>
     * split across 2 lines.
     */
    public void testCombinedCmsConcurrentApplicationConcurrentTimeLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset19.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString()
                + ".", jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME));
    }

    /**
     * Test preprocessing a combined <code>CmsConcurrentEvent</code> and <code>ApplicationStoppedTimeEvent</code> split
     * across 2 lines.
     */
    public void testCombinedCmsConcurrentApplicationStoppedTimeLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset27.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString()
                + ".", jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME));
    }

    public void testRemoveBlankLines() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset20.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
    }

    public void testBottleneckDetectionWholeNumbers() {

        String logLine1 = "test1";
        long timestamp1 = 10000L;
        int duration1 = 500;
        BlockingEvent priorEvent = new ParallelScavengeEvent(logLine1, timestamp1, duration1);

        // 1 second between GCs with duration of .5 seconds
        String logLine2 = "test2";
        long timestamp2 = 11000L;
        int duration2 = 500;
        BlockingEvent gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);

        // Test boundary
        int throughputThreshold = 50;
        Assert.assertFalse("Event incorrectly flagged as a bottleneck.",
                JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold));

        // Test bottleneck
        duration2 = 501;
        gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);
        Assert.assertTrue("Event should have been flagged as a bottleneck.",
                JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold));

    }

    public void testBottleneckDetectionFractions() {

        String logLine1 = "test1";
        long timestamp1 = 10000L;
        int duration1 = 100;
        BlockingEvent priorEvent = new ParallelScavengeEvent(logLine1, timestamp1, duration1);

        // 123 ms between GCs with duration of 33 ms
        String logLine2 = "test2";
        long timestamp2 = 10123L;
        int duration2 = 33;
        BlockingEvent gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);

        // Test boundary
        int throughputThreshold = 41;
        Assert.assertFalse("Event incorrectly flagged as a bottleneck.",
                JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold));

        // Test boundary
        throughputThreshold = 42;
        Assert.assertTrue("Event should have been flagged as a bottleneck.",
                JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold));
    }

    public void testBottleneckDetectionParNew() {
        String previousLogLine = "56.462: [GC 56.462: [ParNew: 64768K->7168K(64768K), 0.0823950 secs] "
                + "142030K->88353K(567808K), 0.0826320 secs] [Times: user=0.10 sys=0.00, real=0.08 secs]";
        BlockingEvent priorEvent = new ParNewEvent(previousLogLine);
        String logLine = "57.026: [GC 57.026: [ParNew: 64768K->7168K(64768K), 0.1763320 secs] "
                + "145953K->98916K(567808K), 0.1765710 secs] [Times: user=0.30 sys=0.00, real=0.17 secs]";
        BlockingEvent gcEvent = new ParNewEvent(logLine);
        // Test boundary
        int throughputThreshold = 90;
        Assert.assertTrue("Event should have been flagged as a bottleneck.",
                JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold));
    }

    public void testTimeWarp() {
        String logLine1 = "test1";
        long timestamp1 = 10000L;
        int duration1 = 1000;
        BlockingEvent priorEvent = new ParallelScavengeEvent(logLine1, timestamp1, duration1);

        // 2nd event starts immediately after the first
        String logLine2 = "test2";
        long timestamp2 = 11000L;
        int duration2 = 500;
        BlockingEvent gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);

        // Test boundary
        int throughputThreshold = 100;

        Assert.assertTrue("Event should have been flagged as a bottleneck.",
                JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold));

        // Decrease timestamp by 1 ms to 2nd event start before 1st event
        // finishes
        timestamp2 = 10999L;
        gcEvent = new ParallelScavengeEvent(logLine2, timestamp2, duration2);
        try {
            Assert.assertTrue("Event should have been flagged as a bottleneck.",
                    JdkUtil.isBottleneck(gcEvent, priorEvent, throughputThreshold));
        } catch (Exception e) {
            Assert.assertTrue("Expected TimeWarpException not thrown.", e instanceof TimeWarpException);
        }
    }

    public void testGetOptionValue() {
        Assert.assertEquals("Option value not correct.", "256k", JdkUtil.getOptionValue("-Xss256k"));
        Assert.assertEquals("Option value not correct.", "2G", JdkUtil.getOptionValue("-Xmx2G"));
        Assert.assertEquals("Option value not correct.", "128M", JdkUtil.getOptionValue("-XX:MaxPermSize=128M"));
        Assert.assertNull("Option value not correct.", JdkUtil.getOptionValue(null));
    }

    /**
     * Test preprocessing <code>UnloadingClassPreprocessAction</code> with underlying
     * <code>ParallelSerialOldEvent</code>.
     */
    public void testUnloadingClassPreprocessActionParallelSerialOldEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset24.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD));
    }

    /**
     * Test preprocessing <code>UnloadingClassPreprocessAction</code> with underlying <code>TruncatedEvent</code>.
     */
    public void testUnloadingClassPreprocessActionTruncatedEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset22.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TRUNCATED.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.TRUNCATED));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    /**
     * Test to make sure a <code>ParNewPromotionFailedTruncatedEvent</code> is not mistakenly preprocessed as a
     * <code>ParNewCmsConcurrentPreprocessAction</code>.
     */
    public void testParNewPromotionFailedTruncatedEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset23.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_TRUNCATED.toString()
                        + ".", jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_TRUNCATED));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    /**
     * Test <code>DateStampPreprocessAction</code> with datestamp.
     */
    public void testDateStampPreprocessActionLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset25.txt");
        GcManager jvmManager = new GcManager();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        File preprocessedFile = jvmManager.preprocess(testFile, calendar.getTime());
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    /**
     * Test <code>DateStampPreprocessAction</code> with datestamp prefix,
     */
    public void testDateStampPreprocessActionLoggingDatestampPrefix() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset26.txt");
        GcManager jvmManager = new GcManager();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, Calendar.APRIL);
        calendar.set(Calendar.DAY_OF_MONTH, 16);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        File preprocessedFile = jvmManager.preprocess(testFile, calendar.getTime());
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".", jvmRun
                .getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    /**
     * Test preprocessing <code>ParNewConcurrentModeFailureEvent</code> split over 3 lines.
     * 
     */
    public void testSplit3LinesParNewConcurrentModeFailureEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset29.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code>.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset32.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PREPROCESSED.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PREPROCESSED));
    }
}
