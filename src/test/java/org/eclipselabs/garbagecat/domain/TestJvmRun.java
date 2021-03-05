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
package org.eclipselabs.garbagecat.domain;

import static org.eclipselabs.garbagecat.Memory.kilobytes;
import static org.eclipselabs.garbagecat.Memory.Unit.BYTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.eclipselabs.garbagecat.Memory;
import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.Test;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJvmRun {

    /**
     * Test passing JVM options on the command line.
     * 
     */
    @Test
    public void testJvmOptionsPassedInOnCommandLine() {
        String options = "MGM was here!";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(options, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue("JVM options passed in are missing or have changed.",
                jvmRun.getJvm().getOptions().equals(options));
    }

    /**
     * Test if -XX:+PrintReferenceGC enabled by inspecting logging events.
     */
    @Test
    public void testPrintReferenceGCByLogging() {
        String jvmOptions = null;
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.REFERENCE_GC);
        jvmRun.setEventTypes(eventTypes);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED));
    }

    /**
     * Test if -XX:+PrintReferenceGC enabled by inspecting jvm options.
     */
    @Test
    public void testPrintReferenceGCByOptions() {
        String jvmOptions = "-Xss128k -XX:+PrintReferenceGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED));
    }

    /**
     * Test if -XX:+PrintStringDeduplicationStatistics enabled by inspecting jvm options.
     */
    @Test
    public void testPrintStringDeduplicationStatistics() {
        String jvmOptions = "-Xss128k -XX:+PrintStringDeduplicationStatistics -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_STRING_DEDUP_STATS_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_STRING_DEDUP_STATS_ENABLED));
    }

    /**
     * Test if PrintGCDetails disabled with -XX:-PrintGCDetails.
     */
    @Test
    public void testPrintGCDetailsDisabled() {
        String jvmOptions = "-Xss128k -XX:-PrintGCDetails -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_GC_DETAILS_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_DISABLED));
        assertFalse(Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING));
    }

    /**
     * Test if PAR_NEW collector disabled with -XX:-UseParNewGC.
     */
    @Test
    public void testUseParNewGcDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseParNewGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_CMS_PAR_NEW_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_PAR_NEW_DISABLED));
    }

    /**
     * Test percent swap free at threshold.
     */
    @Test
    public void testPercentSwapFreeAtThreshold() {
        String jvmOptions = null;
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setSwap(new Memory(1000, BYTES));
        jvmRun.getJvm().setSwapFree(new Memory(946, BYTES));
        jvmRun.doAnalysis();
        assertEquals("Percent swap free not correct.", 95, jvmRun.getJvm().getPercentSwapFree());
        assertFalse(Analysis.INFO_SWAPPING + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_SWAPPING));
    }

    /**
     * Test percent swap free below threshold.
     */
    @Test
    public void testPercentSwapFreeBelowThreshold() {
        String jvmOptions = null;
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setSwap(new Memory(1000, BYTES));
        jvmRun.getJvm().setSwapFree(new Memory(945, BYTES));
        jvmRun.doAnalysis();
        assertEquals("Percent swap free not correct.", 94, jvmRun.getJvm().getPercentSwapFree());
        assertTrue(Analysis.INFO_SWAPPING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_SWAPPING));
    }

    /**
     * Test physical memory equals heap + perm/metaspace.
     */
    @Test
    public void testPhysicalMemoryEqualJvmAllocation() {
        String jvmOptions = "-Xmx1024M -XX:MaxPermSize=128M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setPhysicalMemory(new Memory(1207959552, BYTES));
        jvmRun.doAnalysis();
        assertFalse(Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    @Test
    public void testPhysicalMemoryLessThanJvmAllocation() {
        String jvmOptions = "-Xmx1024M -XX:MaxPermSize=128M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setPhysicalMemory(new Memory(1207959551, BYTES));
        jvmRun.doAnalysis();
        assertTrue(Analysis.ERROR_PHYSICAL_MEMORY + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

    @Test
    public void testLastTimestampNoEvents() {
        GcManager gcManager = new GcManager();
        gcManager.store(null, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertNull("Last GC event not correct.", jvmRun.getLastGcEvent());
    }

    @Test
    public void testSummaryStatsParallel() {
        File testFile = TestUtil.getFile("dataset1.txt");
        GcManager gcManager = new GcManager();
        gcManager.store(testFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Max young space not calculated correctly.", kilobytes(248192), jvmRun.getMaxYoungSpace());
        assertEquals("Max old space not calculated correctly.", kilobytes(786432), jvmRun.getMaxOldSpace());
        assertEquals("NewRatio not calculated correctly.", 3, jvmRun.getNewRatio());
        assertEquals("Max heap space not calculated correctly.", kilobytes(1034624), jvmRun.getMaxHeapSpace());
        assertEquals("Max heap occupancy not calculated correctly.", kilobytes(1013058), jvmRun.getMaxHeapOccupancy());
        assertEquals("Max pause not calculated correctly.", 2782, jvmRun.getMaxGcPause());
        assertEquals("Max perm gen space not calculated correctly.", 159936, jvmRun.getMaxPermSpace());
        assertEquals("Max perm gen occupancy not calculated correctly.", 76972, jvmRun.getMaxPermOccupancy());
        assertEquals("Total GC duration not calculated correctly.", 5615, jvmRun.getTotalGcPause());
        assertEquals("GC Event count not correct.", 2, jvmRun.getEventTypes().size());
        assertTrue(JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SCAVENGE));
        assertTrue(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD));
        assertTrue(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING));
        assertTrue(Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL));
    }

    @Test
    public void testSummaryStatsParNew() {
        File testFile = TestUtil.getFile("dataset2.txt");
        GcManager gcManager = new GcManager();
        gcManager.store(testFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Max young space not calculated correctly.", kilobytes(348864), jvmRun.getMaxYoungSpace());
        assertEquals("Max old space not calculated correctly.", kilobytes(699392), jvmRun.getMaxOldSpace());
        assertEquals("NewRatio not calculated correctly.", 2, jvmRun.getNewRatio());
        assertEquals("Max heap space not calculated correctly.", kilobytes(1048256), jvmRun.getMaxHeapSpace());
        assertEquals("Max heap occupancy not calculated correctly.", kilobytes(424192), jvmRun.getMaxHeapOccupancy());
        assertEquals("Max pause not calculated correctly.", 1070, jvmRun.getMaxGcPause());
        assertEquals("Max perm gen space not calculated correctly.", 99804, jvmRun.getMaxPermSpace());
        assertEquals("Max perm gen occupancy not calculated correctly.", 60155, jvmRun.getMaxPermOccupancy());
        assertEquals("Total GC duration not calculated correctly.", 1283, jvmRun.getTotalGcPause());
        assertEquals("GC Event count not correct.", 2, jvmRun.getEventTypes().size());
        assertTrue(JdkUtil.LogEventType.PAR_NEW.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        assertTrue(JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    /**
     * Test parsing logging with -XX:+PrintGCApplicationConcurrentTime and -XX:+PrintGCApplicationStoppedTime output.
     */
    @Test
    public void testParseLoggingWithApplicationTime() {
        File testFile = TestUtil.getFile("dataset3.txt");
        GcManager gcManager = new GcManager();
        gcManager.store(testFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Max young space not calculated correctly.", kilobytes(1100288), jvmRun.getMaxYoungSpace());
        assertEquals("Max old space not calculated correctly.", kilobytes(1100288), jvmRun.getMaxOldSpace());
        assertEquals("NewRatio not calculated correctly.", 1, jvmRun.getNewRatio());
        assertEquals("Event count not correct.", 3, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertEquals("Should not be any unidentified log lines.", 0, jvmRun.getUnidentifiedLogLines().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME));
        assertTrue(Analysis.INFO_NEW_RATIO_INVERTED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_NEW_RATIO_INVERTED));
    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code> with underlying <code>ParallelCompactingOldEvent</code>
     * .
     */
    @Test
    public void testSplitParallelOldCompactingEventLogging() {
        File testFile = TestUtil.getFile("dataset28.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD));
        assertTrue(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED));
        assertTrue(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED));
    }

    /**
     * Test preprocessing a combined <code>CmsConcurrentEvent</code> and <code>ApplicationConcurrentTimeEvent</code>
     * split across 2 lines.
     */
    @Test
    public void testCombinedCmsConcurrentApplicationConcurrentTimeLogging() {
        File testFile = TestUtil.getFile("dataset19.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
    }

    /**
     * Test preprocessing a combined <code>CmsConcurrentEvent</code> and <code>ApplicationStoppedTimeEvent</code> split
     * across 2 lines.
     */
    @Test
    public void testCombinedCmsConcurrentApplicationStoppedTimeLogging() {
        File testFile = TestUtil.getFile("dataset27.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME));
    }

    @Test
    public void testRemoveBlankLines() {
        File testFile = TestUtil.getFile("dataset20.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        assertTrue(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME));

    }

    /**
     * Test <code>DateStampPreprocessAction</code>.
     */
    @Test
    public void testDateStampPreprocessActionLogging() {
        File testFile = TestUtil.getFile("dataset25.txt");
        GcManager gcManager = new GcManager();
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        File preprocessedFile = gcManager.preprocess(testFile, calendar.getTime());
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    @Test
    public void testSummaryStatsStoppedTime() {
        File testFile = TestUtil.getFile("dataset41.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE));
        assertTrue(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.APPLICATION_STOPPED_TIME));
        assertEquals("GC Event count not correct.", 2, jvmRun.getEventTypes().size());
        assertEquals("GC pause total not correct.", 62, jvmRun.getTotalGcPause());
        assertEquals("GC first timestamp not correct.", 2192, jvmRun.getFirstGcEvent().getTimestamp());
        assertEquals("GC last timestamp not correct.", 2847, jvmRun.getLastGcEvent().getTimestamp());
        assertEquals("GC last duration not correct.", 41453, jvmRun.getLastGcEvent().getDuration());
        assertEquals("Stopped Time event count not correct.", 6, jvmRun.getStoppedTimeEventCount());
        assertEquals("Stopped time total not correct.", 1064, jvmRun.getTotalStoppedTime());
        assertEquals("Stopped first timestamp not correct.", 964, jvmRun.getFirstStoppedEvent().getTimestamp());
        assertEquals("Stopped last timestamp not correct.", 3884, jvmRun.getLastStoppedEvent().getTimestamp());
        assertEquals("Stopped last duration not correct.", 1000688, jvmRun.getLastStoppedEvent().getDuration());
        assertEquals("JVM first event timestamp not correct.", 964, jvmRun.getFirstEvent().getTimestamp());
        assertEquals("JVM last event timestamp not correct.", 3884, jvmRun.getLastEvent().getTimestamp());
        assertEquals("JVM run duration not correct.", 4884, jvmRun.getJvmRunDuration());
        assertEquals("GC throughput not correct.", 99, jvmRun.getGcThroughput());
        assertEquals("Stopped time throughput not correct.", 78, jvmRun.getStoppedTimeThroughput());
        assertTrue(Analysis.WARN_GC_STOPPED_RATIO + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_GC_STOPPED_RATIO));
    }

    @Test
    public void testSummaryStatsUnifiedStoppedTime() {
        File testFile = TestUtil.getFile("dataset182.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE));
        assertTrue(JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT));
        assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK));
        assertTrue(JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_CLEANUP));
        assertTrue(JdkUtil.LogEventType.UNIFIED_APPLICATION_STOPPED_TIME.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_APPLICATION_STOPPED_TIME));
        assertEquals("GC Event count not correct.", 5, jvmRun.getEventTypes().size());
        assertEquals("GC pause total not correct.", 24, jvmRun.getTotalGcPause());
        assertEquals("GC first timestamp not correct.", 53, jvmRun.getFirstGcEvent().getTimestamp());
        assertEquals("GC last timestamp not correct.", 167, jvmRun.getLastGcEvent().getTimestamp());
        assertEquals("GC last duration not correct.", 362, jvmRun.getLastGcEvent().getDuration());
        assertEquals("Stopped Time event count not correct.", 12, jvmRun.getStoppedTimeEventCount());
        assertEquals("Stopped time total not correct.", 25, jvmRun.getTotalStoppedTime());
        assertEquals("Stopped first timestamp not correct.", 29, jvmRun.getFirstStoppedEvent().getTimestamp());
        assertEquals("Stopped last timestamp not correct.", 167, jvmRun.getLastStoppedEvent().getTimestamp());
        assertEquals("Stopped last duration not correct.", 418, jvmRun.getLastStoppedEvent().getDuration());
        assertEquals("JVM first event timestamp not correct.", 29, jvmRun.getFirstEvent().getTimestamp());
        assertEquals("JVM last event timestamp not correct.", 167, jvmRun.getLastEvent().getTimestamp());
        assertEquals("JVM run duration not correct.", 167, jvmRun.getJvmRunDuration());
        assertEquals("GC throughput not correct.", 86, jvmRun.getGcThroughput());
        assertEquals("Stopped time throughput not correct.", 85, jvmRun.getStoppedTimeThroughput());
        assertFalse(Analysis.WARN_GC_STOPPED_RATIO + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_GC_STOPPED_RATIO));
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    @Test
    public void testExplicitGcAnalsysisParallelSerialOld() {
        File testFile = TestUtil.getFile("dataset56.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertTrue(JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SCAVENGE));
        assertTrue(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD));
        assertTrue(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL));
        assertTrue(Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL));
    }

    /**
     * Test JVM Header parsing.
     * 
     */
    @Test
    public void testHeaders() {
        File testFile = TestUtil.getFile("dataset59.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertEquals("Event type count not correct.", 3, jvmRun.getEventTypes().size());
        assertTrue(JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS));
        assertTrue(JdkUtil.LogEventType.HEADER_MEMORY.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY));
        assertTrue(JdkUtil.LogEventType.HEADER_VERSION.toString() + " not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION));
        assertTrue(Analysis.WARN_EXPLICIT_GC_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_DISABLED));
    }

    /**
     * Test <code>PrintTenuringDistributionPreprocessAction</code> with no space after "GC".
     * 
     */
    @Test
    public void testPrintTenuringDistributionPreprocessActionNoSpaceAfterGc() {
        File testFile = TestUtil.getFile("dataset66.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
    }

    /**
     * Test application stopped time w/o timestamps.
     */
    @Test
    public void testApplicationStoppedTimeNoTimestamps() {
        File testFile = TestUtil.getFile("dataset96.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("GC pause total not correct.", 2097, jvmRun.getTotalGcPause());
        assertEquals("GC first timestamp not correct.", 16517, jvmRun.getFirstGcEvent().getTimestamp());
        assertEquals("GC last timestamp not correct.", 31432, jvmRun.getLastGcEvent().getTimestamp());
        assertEquals("GC last duration not correct.", 271019, jvmRun.getLastGcEvent().getDuration());
        assertEquals("Stopped time total not correct.", 1830, jvmRun.getTotalStoppedTime());
        assertEquals("Stopped first timestamp not correct.", 0, jvmRun.getFirstStoppedEvent().getTimestamp());
        assertEquals("Stopped last timestamp not correct.", 0, jvmRun.getLastStoppedEvent().getTimestamp());
        assertEquals("Stopped last duration not correct.", 50, jvmRun.getLastStoppedEvent().getDuration());
        assertEquals("JVM first event timestamp not correct.", 16517, jvmRun.getFirstEvent().getTimestamp());
        assertEquals("JVM last event timestamp not correct.", 31432, jvmRun.getLastEvent().getTimestamp());
        assertEquals("JVM run duration not correct.", 31703, jvmRun.getJvmRunDuration());
        assertEquals("GC throughput not correct.", 93, jvmRun.getGcThroughput());
        assertEquals("Stopped time throughput not correct.", 94, jvmRun.getStoppedTimeThroughput());
        assertEquals("Inverted parallelism event count not correct.", 0, jvmRun.getInvertedParallelismCount());
    }

    /**
     * Test summary stats for a partial log file (1st timestamp > Constants.FIRST_TIMESTAMP_THRESHOLD). Same data as
     * dataset41.txt with 1000 seconds added to each timestamp.
     */
    @Test
    public void testSummaryStatsPartialLog() {
        File testFile = TestUtil.getFile("dataset98.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("GC event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertEquals("GC blocking event count not correct.", 2, jvmRun.getBlockingEventCount());
        assertEquals("GC pause total not correct.", 62, jvmRun.getTotalGcPause());
        assertEquals("GC first timestamp not correct.", 1002192, jvmRun.getFirstGcEvent().getTimestamp());
        assertEquals("GC last timestamp not correct.", 1002847, jvmRun.getLastGcEvent().getTimestamp());
        assertEquals("GC last duration not correct.", 41453, jvmRun.getLastGcEvent().getDuration());
        assertEquals("Stopped Time event count not correct.", 6, jvmRun.getStoppedTimeEventCount());
        assertEquals("Stopped time total not correct.", 1064, jvmRun.getTotalStoppedTime());
        assertEquals("Stopped first timestamp not correct.", 1000964,
                jvmRun.getFirstStoppedEvent().getTimestamp());
        assertEquals("Stopped last timestamp not correct.", 1003884,
                jvmRun.getLastStoppedEvent().getTimestamp());
        assertEquals("Stopped last duration not correct.", 1000688, jvmRun.getLastStoppedEvent().getDuration());
        assertEquals("JVM first event timestamp not correct.", 1000964, jvmRun.getFirstEvent().getTimestamp());
        assertEquals("JVM last event timestamp not correct.", 1003884, jvmRun.getLastEvent().getTimestamp());
        assertEquals("JVM run duration not correct.", 3920, jvmRun.getJvmRunDuration());
        assertEquals("GC throughput not correct.", 98, jvmRun.getGcThroughput());
        assertEquals("Stopped time throughput not correct.", 73, jvmRun.getStoppedTimeThroughput());
        assertTrue(Analysis.WARN_GC_STOPPED_RATIO + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_GC_STOPPED_RATIO));
    }

    /**
     * Test summary stats with batching.
     */
    @Test
    public void testStoppedTime() {
        File testFile = TestUtil.getFile("dataset103.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("GC event type count not correct.", 3, jvmRun.getEventTypes().size());
        assertEquals("GC blocking event count not correct.", 160, jvmRun.getBlockingEventCount());
        assertEquals("GC pause total not correct.", 2568199, jvmRun.getTotalGcPause());
        assertEquals("GC first timestamp not correct.", 4364, jvmRun.getFirstGcEvent().getTimestamp());
        assertEquals("GC last timestamp not correct.", 2801954, jvmRun.getLastGcEvent().getTimestamp());
        assertEquals("GC last duration not correct.", 25963804, jvmRun.getLastGcEvent().getDuration());
        assertEquals("Stopped Time event count not correct.", 151, jvmRun.getStoppedTimeEventCount());
        assertEquals("Stopped time total not correct.", 2721420, jvmRun.getTotalStoppedTime());
        assertEquals("Stopped first timestamp not correct.", 0, jvmRun.getFirstStoppedEvent().getTimestamp());
        assertEquals("Stopped last timestamp not correct.", 0, jvmRun.getLastStoppedEvent().getTimestamp());
        assertEquals("Stopped last duration not correct.", 36651675, jvmRun.getLastStoppedEvent().getDuration());
        assertEquals("JVM first event timestamp not correct.", 4364, jvmRun.getFirstEvent().getTimestamp());
        assertEquals("JVM last timestamp not correct.", 2801954, jvmRun.getLastEvent().getTimestamp());
        assertEquals("JVM run duration not correct.", 2827917, jvmRun.getJvmRunDuration());
        assertEquals("GC throughput not correct.", 9, jvmRun.getGcThroughput());
        assertEquals("Stopped time throughput not correct.", 4, jvmRun.getStoppedTimeThroughput());
        assertFalse(Analysis.WARN_GC_STOPPED_RATIO + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_GC_STOPPED_RATIO));
        assertEquals("Inverted parallelism event count not correct.", 0, jvmRun.getInvertedParallelismCount());
    }

    /**
     * Test no gc logging events, only stopped time events.
     */
    @Test
    public void testStoppedTimeWithoutGcEvents() {
        File testFile = TestUtil.getFile("dataset108.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        gcManager.store(testFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Stopped time throughput not correct.", 0, jvmRun.getStoppedTimeThroughput());
    }

    /**
     * Test identifying <code>ParNewEvent</code> running in incremental mode.
     */
    @Test
    public void testPrintGcApplicationConcurrentTimeAnalysis() {
        File testFile = TestUtil.getFile("dataset104.txt");
        Jvm jvm = new Jvm(null, null);
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME));
    }

}
