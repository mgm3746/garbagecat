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
package org.eclipselabs.garbagecat.domain;

import java.io.File;
import java.util.Calendar;

import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJvmRun extends TestCase {
    
    public void testSummaryStatsParallel() {

        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset1.txt");
        GcManager jvmManager = new GcManager();
        jvmManager.store(testFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);

        Assert.assertEquals("Max heap space not calculated correctly.", 1034624, jvmRun.getMaxHeapSpace());
        Assert.assertEquals("Max heap occupancy not calculated correctly.", 1013058, jvmRun.getMaxHeapOccupancy());
        Assert.assertEquals("Max pause not calculated correctly.", 2782, jvmRun.getMaxGcPause());        
        Assert.assertEquals("Max perm gen space not calculated correctly.", 159936, jvmRun.getMaxPermSpace());
        Assert.assertEquals("Max perm gen occupancy not calculated correctly.", 76972, jvmRun.getMaxPermOccupancy());
        Assert.assertEquals("Total GC duration not calculated correctly.", 5614, jvmRun.getTotalGcPause());
        Assert.assertEquals("GC Event count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SCAVENGE));
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD));
        Assert.assertTrue(Analysis.KEY_APPLICATION_STOPPED_TIME_MISSING + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_APPLICATION_STOPPED_TIME_MISSING));
        Assert.assertTrue(Analysis.KEY_THROUGHPUT_SERIAL_GC + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_THROUGHPUT_SERIAL_GC));
    }

    public void testSummaryStatsParNew() {

        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset2.txt");
        GcManager jvmManager = new GcManager();
        jvmManager.store(testFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);

        Assert.assertEquals("Max heap space not calculated correctly.", 1048256, jvmRun.getMaxHeapSpace());
        Assert.assertEquals("Max heap occupancy not calculated correctly.", 424192, jvmRun.getMaxHeapOccupancy());
        Assert.assertEquals("Max pause not calculated correctly.", 1070, jvmRun.getMaxGcPause());
        Assert.assertEquals("Max perm gen space not calculated correctly.", 99804, jvmRun.getMaxPermSpace());
        Assert.assertEquals("Max perm gen occupancy not calculated correctly.", 60155, jvmRun.getMaxPermOccupancy());
        Assert.assertEquals("Total GC duration not calculated correctly.", 1282, jvmRun.getTotalGcPause());
        Assert.assertEquals("GC Event count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PAR_NEW.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        Assert.assertTrue(JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(Analysis.KEY_CMS_SERIAL_GC + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_CMS_SERIAL_GC));
    }
    
    public void testLastTimestampNoEvents() {
        GcManager jvmManager = new GcManager();
        jvmManager.store(null);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Last timestamp not correct.", 0, jvmRun.getLastTimestamp());
    }

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
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString() + ".",
                jvmRun.getEventTypes()
                        .contains(JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE));
        Assert.assertTrue(Analysis.KEY_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED));
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
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
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
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString() + ".",
                jvmRun.getEventTypes()
                        .contains(JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE));
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
     * Test preprocessing <code>GcTimeLimitExceededEvent</code> with underlying <code>ParallelOldCompactingEvent</code>
     * .
     */
    public void testSplitParallelOldCompactingEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset28.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING));
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
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                jvmRun.getEventTypes()
                        .contains(JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA));
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
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA));
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
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                jvmRun.getEventTypes()
                        .contains(JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA));
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
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.SERIAL));
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
        Assert.assertTrue("Log line not recognized as "
                + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
                jvmRun.getEventTypes()
                        .contains(JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA));
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
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME));
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
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME));
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
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TRUNCATED.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.TRUNCATED));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
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
                        + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_TRUNCATED));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    /**
     * Test <code>DateStampPreprocessAction</code>.
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
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    /**
     * Test <code>DateStampPrefixPreprocessAction</code>.
     */
    public void testDateStampPrefixPreprocessAction() {
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
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    /**
     * Test <code>DateStampPrefixPreprocessAction</code> with multiple datestamps.
     */
    public void testDateStampPrefixPreprocessActionMultiple() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset33.txt");
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
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
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
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_YOUNG_PAUSE.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungPauseLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset32.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
    }

    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_YOUNG_PAUSE with G1_EVACUATION_PAUSE trigger.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1EvacuationPauseLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset34.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
    }

    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_YOUNG_PAUSE with GCLOCKER_INITIATED_GC trigger.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungPauseWithGCLockerInitiatedGCLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset35.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_YOUNG_PAUSE with TO_SPACE_EXHAUSTED trigger.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionToSpaceExhaustedLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset45.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
    }

    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_FULL_GC.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1FullGCLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset36.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC));
        Assert.assertTrue(JdkUtil.TriggerType.SYSTEM_GC.toString() + " trigger not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_SERIAL));
        Assert.assertTrue(Analysis.KEY_EXPLICIT_GC_SERIAL + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_SERIAL));
        Assert.assertTrue(Analysis.KEY_EXPLICIT_GC_UNECESSARY_CMS_G1 + " analysis incorrectly identified.", !jvmRun.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_UNECESSARY_CMS_G1));
        Assert.assertTrue(Analysis.KEY_EXPLICIT_GC_UNNECESSARY + " analysis incorrectly identified.", !jvmRun.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_UNNECESSARY));
    }

    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_YOUNG_INITIAL_MARK.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionYoungInitialMarkLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset37.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK));
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_YOUNG_INITIAL_MARK.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1InitialMarkWithCodeRootLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset43.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK));
    }

    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_REMARK.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionRemarkLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset38.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_REMARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_REMARK));
    }

    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_MIXED_PAUSE with G1_EVACUATION_PAUSE trigger.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionMixedPauseLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset39.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_MIXED_PAUSE));
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_MIXED_PAUSE with no trigger.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionMixedPauseNoTriggerLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset46.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_MIXED_PAUSE));
    }

    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_CLEANUP.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionCleanupLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset40.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CLEANUP));
    }
    
    public void testSummaryStatsStoppedTime() {

        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset41.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Max heap space not calculated correctly.", 31457280, jvmRun.getMaxHeapSpace());
        Assert.assertEquals("Max heap occupancy not calculated correctly.", 140288, jvmRun.getMaxHeapOccupancy());
        Assert.assertEquals("Max GC pause not calculated correctly.", 41, jvmRun.getMaxGcPause());
        Assert.assertEquals("Max perm gen space not calculated correctly.", 0, jvmRun.getMaxPermSpace());
        Assert.assertEquals("Max perm gen occupancy not calculated correctly.", 0, jvmRun.getMaxPermOccupancy());
        Assert.assertEquals("Total GC pause time not calculated correctly.", 61, jvmRun.getTotalGcPause());
        Assert.assertEquals("GC last timestamp not calculated correctly.", 2847, jvmRun.getLastTimestamp());
        Assert.assertEquals("GC throughput not calculated correctly.", 98, jvmRun.getGcThroughput());
        Assert.assertEquals("GC Event count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE));
        Assert.assertTrue(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + " not identified.", jvmRun.getEventTypes().contains(LogEventType.APPLICATION_STOPPED_TIME));        
        Assert.assertEquals("Stopped Time event count not correct.", 6, jvmRun.getStoppedTimeEventCount());
        Assert.assertEquals("Max stopped time not calculated correctly.", 1000, jvmRun.getMaxStoppedTime());
        Assert.assertEquals("Total stopped time not calculated correctly.", 1064, jvmRun.getTotalStoppedTime());
        Assert.assertEquals("Stopped time throughput not calculated correctly.", 63, jvmRun.getStoppedTimeThroughput());
        Assert.assertEquals("GC/Stopped ratio not calculated correctly.", 6, jvmRun.getGcStoppedRatio());
        Assert.assertTrue(Analysis.KEY_GC_STOPPED_RATIO + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_GC_STOPPED_RATIO));
    }  
    
    public void testHeaderLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset42.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " information not identified.", jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_MEMORY.toString() + " information not identified.", jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_VERSION.toString() + " information not identified.", jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION));
        Assert.assertTrue(Analysis.KEY_THREAD_STACK_SIZE_NOT_SET + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_THREAD_STACK_SIZE_NOT_SET));
    }  

    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionConcurrentLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset44.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionYoungConcurrentLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset47.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));    
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungPauseWithG1ErgonomicsLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset48.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT)); 
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_YOUNG_INITIAL_MARK with ergonomics.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungInitialMarkWithG1ErgonomicsLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset49.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK));
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_YOUNG_INITIAL_MARK with ergonomics.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungInitialMarkWithTriggerAndG1ErgonomicsLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset53.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK));
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for G1_YOUNG_PAUSE with TRIGGER_TO_SPACE_EXHAUSTED with ergonomics.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungPauseTriggerToSpaceExhaustedWithG1ErgonomicsLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset50.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
    }
        
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungPauseWithG1ErgonomicsLogging2() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset51.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT)); 
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungPauseWithG1ErgonomicsLogging3() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset52.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT)); 
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungPauseWithG1ErgonomicsLogging4() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset54.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT)); 
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungPauseWithG1ErgonomicsLogging5() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset55.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_YOUNG_PAUSE));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_CONCURRENT)); 
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    public void testExplicitGcAnalsysisParallelSerialOld() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset56.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SCAVENGE));
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD));
        Assert.assertTrue(Analysis.KEY_EXPLICIT_GC_UNNECESSARY + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_UNNECESSARY));
    }
    
    /**
     * Test <code>G1PrintGcDetailsPreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    public void testG1PrintGcDetailsPreprocessActionG1YoungPauseWithG1ErgonomicsLogging6() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset57.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.", jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_INITIAL_MARK));
        Assert.assertTrue(JdkUtil.LogEventType.G1_CONCURRENT.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.G1_CONCURRENT));
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE));        
    }
    
    /**
     * Test <code>CmsPreprocessAction</code> for mixed PAR_NEW and CMS_CONCURRENT.
     * 
     */
    public void testCmsPreprocessActionParNewWithConcurrent() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset58.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.", jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PAR_NEW.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        Assert.assertTrue(JdkUtil.LogEventType.CMS_CONCURRENT.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));        
    }
    
    /**
     * Test JVM Header parsing.
     * 
     */
    public void testHeaders() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset59.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.", jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " not identified.", jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS));               
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_MEMORY.toString() + " not identified.", jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_VERSION.toString() + " not identified.", jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION));
        Assert.assertTrue(Analysis.KEY_EXPLICIT_GC_DISABLED + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_DISABLED));
    }
    
    /**
     * Test analysis perm gen or metaspace size not set.
     * 
     */
    public void testAnalysisPermMetaspaceNotSet() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset60.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.KEY_PERM_METASPACE_NOT_SET + " analysis not identified.", jvmRun.getAnalysisKeys().contains(Analysis.KEY_PERM_METASPACE_NOT_SET));
    }
    
    /**
     * Test passing JVM options on the command line.
     * 
     */
    public void testJvmOptionsPassedInOnCommandLine() {
        String options = "MGM was here!";
        GcManager jvmManager = new GcManager();       
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(options, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);   
        jvmRun.doAnalysis();
        Assert.assertTrue("JVM options passed in are missing or have changed.", jvmRun.getJvm().getOptions().equals(options));
    }
}
