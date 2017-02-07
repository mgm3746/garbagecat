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
package org.eclipselabs.garbagecat.domain;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJvmRun extends TestCase {

    /**
     * Test passing JVM options on the command line.
     * 
     */
    public void testJvmOptionsPassedInOnCommandLine() {
        String options = "MGM was here!";
        GcManager jvmManager = new GcManager();
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(options, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue("JVM options passed in are missing or have changed.",
                jvmRun.getJvm().getOptions().equals(options));
    }

    /**
     * Test if -XX:+PrintReferenceGC enabled by inspecting logging events.
     */
    public void testPrintReferenceGCByLogging() {
        String jvmOptions = null;
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.REFERENCE_GC);
        jvmRun.setEventTypes(eventTypes);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED));
    }

    /**
     * Test if -XX:+PrintReferenceGC enabled by inspecting jvm options.
     */
    public void testPrintReferenceGCByOptions() {
        String jvmOptions = "-Xss128k -XX:+PrintReferenceGC -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED));
    }

    /**
     * Test if -XX:+PrintStringDeduplicationStatistics enabled by inspecting jvm options.
     */
    public void testPrintStringDeduplicationStatistics() {
        String jvmOptions = "-Xss128k -XX:+PrintStringDeduplicationStatistics -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_STRING_DEDUP_STATS_ENABLED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_STRING_DEDUP_STATS_ENABLED));
    }

    /**
     * Test if PrintGCDetails disabled with -XX:-PrintGCDetails.
     */
    public void testPrintGCDetailsDisabled() {
        String jvmOptions = "-Xss128k -XX:-PrintGCDetails -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_GC_DETAILS_DISABLED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_GC_DETAILS_DISABLED));
        Assert.assertFalse(Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis incorrectly identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING));
    }

    /**
     * Test if PAR_NEW collector disabled with -XX:-UseParNewGC.
     */
    public void testUseParNewGcDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseParNewGC -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_CMS_PAR_NEW_DISABLED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_CMS_PAR_NEW_DISABLED));
    }

    /**
     * Test percent swap free at threshold.
     */
    public void testPercentSwapFreeAtThreshold() {
        String jvmOptions = null;
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setSwap(1000);
        jvmRun.getJvm().setSwapFree(946);
        jvmRun.doAnalysis();
        Assert.assertEquals("Percent swap free not correct.", 95, jvmRun.getJvm().getPercentSwapFree());
        Assert.assertFalse(Analysis.INFO_SWAPPING + " analysis incorrectly identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.INFO_SWAPPING));
    }

    /**
     * Test percent swap free below threshold.
     */
    public void testPercentSwapFreeBelowThreshold() {
        String jvmOptions = null;
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setSwap(1000);
        jvmRun.getJvm().setSwapFree(945);
        jvmRun.doAnalysis();
        Assert.assertEquals("Percent swap free not correct.", 94, jvmRun.getJvm().getPercentSwapFree());
        Assert.assertTrue(Analysis.INFO_SWAPPING + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.INFO_SWAPPING));
    }

    /**
     * Test physical memory equals heap + perm/metaspace.
     */
    public void testPhysicalMemoryEqualJvmAllocation() {
        String jvmOptions = "-Xmx1024M -XX:MaxPermSize=128M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setPhysicalMemory(1207959552);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    public void testPhysicalMemoryLessThanJvmAllocation() {
        String jvmOptions = "-Xmx1024M -XX:MaxPermSize=128M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setPhysicalMemory(1207959551);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.ERROR_PHYSICAL_MEMORY + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

    public void testSummaryStatsParallel() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset1.txt");
        GcManager jvmManager = new GcManager();
        jvmManager.store(testFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);

        Assert.assertEquals("Max heap space not calculated correctly.", 1034624, jvmRun.getMaxHeapSpace());
        Assert.assertEquals("Max heap occupancy not calculated correctly.", 1013058, jvmRun.getMaxHeapOccupancy());
        Assert.assertEquals("Max pause not calculated correctly.", 2782, jvmRun.getMaxGcPause());
        Assert.assertEquals("Max perm gen space not calculated correctly.", 159936, jvmRun.getMaxPermSpace());
        Assert.assertEquals("Max perm gen occupancy not calculated correctly.", 76972, jvmRun.getMaxPermOccupancy());
        Assert.assertEquals("Total GC duration not calculated correctly.", 5614, jvmRun.getTotalGcPause());
        Assert.assertEquals("GC Event count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SCAVENGE));
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD));
        Assert.assertTrue(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_PARALLEL));
    }

    public void testSummaryStatsParNew() {

        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset2.txt");
        GcManager jvmManager = new GcManager();
        jvmManager.store(testFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);

        Assert.assertEquals("Max heap space not calculated correctly.", 1048256, jvmRun.getMaxHeapSpace());
        Assert.assertEquals("Max heap occupancy not calculated correctly.", 424192, jvmRun.getMaxHeapOccupancy());
        Assert.assertEquals("Max pause not calculated correctly.", 1070, jvmRun.getMaxGcPause());
        Assert.assertEquals("Max perm gen space not calculated correctly.", 99804, jvmRun.getMaxPermSpace());
        Assert.assertEquals("Max perm gen occupancy not calculated correctly.", 60155, jvmRun.getMaxPermOccupancy());
        Assert.assertEquals("Total GC duration not calculated correctly.", 1282, jvmRun.getTotalGcPause());
        Assert.assertEquals("GC Event count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PAR_NEW.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        Assert.assertTrue(JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    public void testLastTimestampNoEvents() {
        GcManager jvmManager = new GcManager();
        jvmManager.store(null, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertNull("Last GC event not correct.", jvmRun.getLastGcEvent());
    }

    /**
     * Test parsing logging with -XX:+PrintGCApplicationConcurrentTime and -XX:+PrintGCApplicationStoppedTime output.
     */
    public void testParseLoggingWithApplicationTime() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset3.txt");
        GcManager jvmManager = new GcManager();
        jvmManager.store(testFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Should not be any unidentified log lines.", 0, jvmRun.getUnidentifiedLogLines().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME));
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
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING));
        Assert.assertTrue(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED));
        Assert.assertTrue(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED));
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
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
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
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME));
    }

    public void testRemoveBlankLines() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset20.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        Assert.assertTrue(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME));

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
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    public void testSummaryStatsStoppedTime() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset41.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE));
        Assert.assertTrue(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.APPLICATION_STOPPED_TIME));
        Assert.assertEquals("GC Event count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertEquals("GC pause total not correct.", 61, jvmRun.getTotalGcPause());
        Assert.assertEquals("GC first timestamp not correct.", 2192, jvmRun.getFirstGcEvent().getTimestamp());
        Assert.assertEquals("GC last timestamp not correct.", 2847, jvmRun.getLastGcEvent().getTimestamp());
        Assert.assertEquals("GC last duration not correct.", 41, jvmRun.getLastGcEvent().getDuration());
        Assert.assertEquals("Stopped Time event count not correct.", 6, jvmRun.getStoppedTimeEventCount());
        Assert.assertEquals("Stopped time total not correct.", 1064, jvmRun.getTotalStoppedTime());
        Assert.assertEquals("Stopped first timestamp not correct.", 964, jvmRun.getFirstStoppedEvent().getTimestamp());
        Assert.assertEquals("Stopped last timestamp not correct.", 3884, jvmRun.getLastStoppedEvent().getTimestamp());
        Assert.assertEquals("Stopped last duration not correct.", 1000688, jvmRun.getLastStoppedEvent().getDuration());
        Assert.assertEquals("JVM first event timestamp not correct.", 964, jvmRun.getFirstEvent().getTimestamp());
        Assert.assertEquals("JVM last event timestamp not correct.", 3884, jvmRun.getLastEvent().getTimestamp());
        Assert.assertEquals("JVM run duration not correct.", 4884, jvmRun.getJvmRunDuration());
        Assert.assertEquals("GC throughput not correct.", 99, jvmRun.getGcThroughput());
        Assert.assertEquals("Stopped time throughput not correct.", 78, jvmRun.getStoppedTimeThroughput());
        Assert.assertTrue(Analysis.WARN_GC_STOPPED_RATIO + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_GC_STOPPED_RATIO));
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    public void testExplicitGcAnalsysisParallelSerialOld() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset56.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SCAVENGE));
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD));
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_PARALLEL));
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
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_MEMORY.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_VERSION.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION));
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_DISABLED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_EXPLICIT_GC_DISABLED));
    }

    /**
     * Test <code>PrintTenuringDistributionPreprocessAction</code> with no space after "GC".
     * 
     */
    public void testPrintTenuringDistributionPreprocessActionNoSpaceAfterGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset66.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    /**
     * Test application stopped time w/o timestamps.
     */
    public void testApplicationStoppedTimeNoTimestamps() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset96.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("GC pause total not correct.", 2096, jvmRun.getTotalGcPause());
        Assert.assertEquals("GC first timestamp not correct.", 16517, jvmRun.getFirstGcEvent().getTimestamp());
        Assert.assertEquals("GC last timestamp not correct.", 31432, jvmRun.getLastGcEvent().getTimestamp());
        Assert.assertEquals("GC last duration not correct.", 271, jvmRun.getLastGcEvent().getDuration());
        Assert.assertEquals("Stopped time total not correct.", 1830, jvmRun.getTotalStoppedTime());
        Assert.assertEquals("Stopped first timestamp not correct.", 0, jvmRun.getFirstStoppedEvent().getTimestamp());
        Assert.assertEquals("Stopped last timestamp not correct.", 0, jvmRun.getLastStoppedEvent().getTimestamp());
        Assert.assertEquals("Stopped last duration not correct.", 50, jvmRun.getLastStoppedEvent().getDuration());
        Assert.assertEquals("JVM first event timestamp not correct.", 16517, jvmRun.getFirstEvent().getTimestamp());
        Assert.assertEquals("JVM last event timestamp not correct.", 31432, jvmRun.getLastEvent().getTimestamp());
        Assert.assertEquals("JVM run duration not correct.", 31703, jvmRun.getJvmRunDuration());
        Assert.assertEquals("GC throughput not correct.", 93, jvmRun.getGcThroughput());
        Assert.assertEquals("Stopped time throughput not correct.", 94, jvmRun.getStoppedTimeThroughput());
    }

    /**
     * Test summary stats for a partial log file (1st timestamp > Constants.FIRST_TIMESTAMP_THRESHOLD). Same data as
     * dataset41.txt with 1000 seconds added to each timestamp.
     */
    public void testSummaryStatsPartialLog() {

        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset98.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("GC event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertEquals("GC blocking event count not correct.", 2, jvmRun.getBlockingEventCount());
        Assert.assertEquals("GC pause total not correct.", 61, jvmRun.getTotalGcPause());
        Assert.assertEquals("GC first timestamp not correct.", 1002192, jvmRun.getFirstGcEvent().getTimestamp());
        Assert.assertEquals("GC last timestamp not correct.", 1002847, jvmRun.getLastGcEvent().getTimestamp());
        Assert.assertEquals("GC last duration not correct.", 41, jvmRun.getLastGcEvent().getDuration());
        Assert.assertEquals("Stopped Time event count not correct.", 6, jvmRun.getStoppedTimeEventCount());
        Assert.assertEquals("Stopped time total not correct.", 1064, jvmRun.getTotalStoppedTime());
        Assert.assertEquals("Stopped first timestamp not correct.", 1000964,
                jvmRun.getFirstStoppedEvent().getTimestamp());
        Assert.assertEquals("Stopped last timestamp not correct.", 1003884,
                jvmRun.getLastStoppedEvent().getTimestamp());
        Assert.assertEquals("Stopped last duration not correct.", 1000688, jvmRun.getLastStoppedEvent().getDuration());
        Assert.assertEquals("JVM first event timestamp not correct.", 1000964, jvmRun.getFirstEvent().getTimestamp());
        Assert.assertEquals("JVM last event timestamp not correct.", 1003884, jvmRun.getLastEvent().getTimestamp());
        Assert.assertEquals("JVM run duration not correct.", 3920, jvmRun.getJvmRunDuration());
        Assert.assertEquals("GC throughput not correct.", 98, jvmRun.getGcThroughput());
        Assert.assertEquals("Stopped time throughput not correct.", 73, jvmRun.getStoppedTimeThroughput());
        Assert.assertTrue(Analysis.WARN_GC_STOPPED_RATIO + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_GC_STOPPED_RATIO));
    }

    /**
     * Test summary stats with batching.
     */
    public void testStopedTime() {

        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset103.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("GC event type count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertEquals("GC blocking event count not correct.", 160, jvmRun.getBlockingEventCount());
        Assert.assertEquals("GC pause total not correct.", 2568121, jvmRun.getTotalGcPause());
        Assert.assertEquals("GC first timestamp not correct.", 4364, jvmRun.getFirstGcEvent().getTimestamp());
        Assert.assertEquals("GC last timestamp not correct.", 2801954, jvmRun.getLastGcEvent().getTimestamp());
        Assert.assertEquals("GC last duration not correct.", 25963, jvmRun.getLastGcEvent().getDuration());
        Assert.assertEquals("Stopped Time event count not correct.", 151, jvmRun.getStoppedTimeEventCount());
        Assert.assertEquals("Stopped time total not correct.", 2721420, jvmRun.getTotalStoppedTime());
        Assert.assertEquals("Stopped first timestamp not correct.", 0, jvmRun.getFirstStoppedEvent().getTimestamp());
        Assert.assertEquals("Stopped last timestamp not correct.", 0, jvmRun.getLastStoppedEvent().getTimestamp());
        Assert.assertEquals("Stopped last duration not correct.", 36651675, jvmRun.getLastStoppedEvent().getDuration());
        Assert.assertEquals("JVM first event timestamp not correct.", 4364, jvmRun.getFirstEvent().getTimestamp());
        Assert.assertEquals("JVM last timestamp not correct.", 2801954, jvmRun.getLastEvent().getTimestamp());
        Assert.assertEquals("JVM run duration not correct.", 2827917, jvmRun.getJvmRunDuration());
        Assert.assertEquals("GC throughput not correct.", 9, jvmRun.getGcThroughput());
        Assert.assertEquals("Stopped time throughput not correct.", 4, jvmRun.getStoppedTimeThroughput());
        Assert.assertFalse(Analysis.WARN_GC_STOPPED_RATIO + " analysis incorrectly identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_GC_STOPPED_RATIO));
    }

    /**
     * Test no gc logging events, only stopped time events.
     */
    public void testStoppedTimeWithoutGcEvents() {

        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset108.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        jvmManager.store(testFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Stopped time throughput not correct.", 0, jvmRun.getStoppedTimeThroughput());
    }

    /**
     * Test identifying <code>ParNewEvent</code> running in incremental mode.
     */
    public void testPrintGcApplicationConcurrentTimeAnalysis() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset104.txt");
        Jvm jvm = new Jvm(null, null);
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME));
    }
}
