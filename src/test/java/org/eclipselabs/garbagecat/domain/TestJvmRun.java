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
package org.eclipselabs.garbagecat.domain;

import static org.eclipselabs.garbagecat.TestUtil.parseDate;
import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.eclipselabs.garbagecat.util.Memory.Unit.BYTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestJvmRun {

    /**
     * Test passing JVM options on the command line.
     * 
     */
    @Test
    void testJvmOptionsPassedInOnCommandLine() {
        String options = "MGM was here!";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(options, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getJvm().getOptions().equals(options), "JVM options passed in are missing or have changed.");
    }

    /**
     * Test if -XX:+PrintReferenceGC enabled by inspecting logging events.
     */
    @Test
    void testPrintReferenceGCByLogging() {
        String jvmOptions = null;
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.REFERENCE_GC);
        jvmRun.setEventTypes(eventTypes);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED),
                Analysis.WARN_PRINT_REFERENCE_GC_ENABLED + " analysis not identified.");
    }

    /**
     * Test if -XX:+PrintReferenceGC enabled by inspecting jvm options.
     */
    @Test
    void testPrintReferenceGCByOptions() {
        String jvmOptions = "-Xss128k -XX:+PrintReferenceGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED),
                Analysis.WARN_PRINT_REFERENCE_GC_ENABLED + " analysis not identified.");
    }

    /**
     * Test if -XX:+PrintStringDeduplicationStatistics enabled by inspecting jvm options.
     */
    @Test
    void testPrintStringDeduplicationStatistics() {
        String jvmOptions = "-Xss128k -XX:+PrintStringDeduplicationStatistics -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_STRING_DEDUP_STATS_ENABLED),
                Analysis.WARN_PRINT_STRING_DEDUP_STATS_ENABLED + " analysis not identified.");
    }

    /**
     * Test if PrintGCDetails disabled with -XX:-PrintGCDetails.
     */
    @Test
    void testPrintGCDetailsDisabled() {
        String jvmOptions = "-Xss128k -XX:-PrintGCDetails -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_DISABLED),
                Analysis.WARN_PRINT_GC_DETAILS_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING),
                Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis incorrectly identified.");
    }

    /**
     * Test if PAR_NEW collector disabled with -XX:-UseParNewGC.
     */
    @Test
    void testUseParNewGcDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseParNewGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_PAR_NEW_DISABLED),
                Analysis.WARN_CMS_PAR_NEW_DISABLED + " analysis not identified.");
    }

    /**
     * Test percent swap free at threshold.
     */
    @Test
    void testPercentSwapFreeAtThreshold() {
        String jvmOptions = null;
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setSwap(new Memory(1000, BYTES));
        jvmRun.getJvm().setSwapFree(new Memory(946, BYTES));
        jvmRun.doAnalysis();
        assertEquals((long) 95, jvmRun.getJvm().getPercentSwapFree(), "Percent swap free not correct.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_SWAPPING),
                Analysis.INFO_SWAPPING + " analysis incorrectly identified.");
    }

    /**
     * Test percent swap free below threshold.
     */
    @Test
    void testPercentSwapFreeBelowThreshold() {
        String jvmOptions = null;
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setSwap(new Memory(1000, BYTES));
        jvmRun.getJvm().setSwapFree(new Memory(945, BYTES));
        jvmRun.doAnalysis();
        assertEquals((long) 94, jvmRun.getJvm().getPercentSwapFree(), "Percent swap free not correct.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_SWAPPING),
                Analysis.INFO_SWAPPING + " analysis not identified.");
    }

    /**
     * Test physical memory equals heap + perm/metaspace.
     */
    @Test
    void testPhysicalMemoryEqualJvmAllocation() {
        String jvmOptions = "-Xmx1024M -XX:MaxPermSize=128M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setPhysicalMemory(new Memory(1207959552, BYTES));
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY),
                Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.");
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    @Test
    void testPhysicalMemoryLessThanJvmAllocation() {
        String jvmOptions = "-Xmx1024M -XX:MaxPermSize=128M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setPhysicalMemory(new Memory(1207959551, BYTES));
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY),
                Analysis.ERROR_PHYSICAL_MEMORY + " analysis not identified.");
    }

    @Test
    void testLastTimestampNoEvents() {
        GcManager gcManager = new GcManager();
        gcManager.store(null, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertNull(jvmRun.getLastGcEvent(), "Last GC event not correct.");
    }

    @Test
    void testJdk8GcLogNoRotationFileOverwrite() {
        String jvmOptions = "-XX:+PrintGC -Xloggc:gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps ";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_GC_LOG_FILE_OVERWRITE),
                Analysis.WARN_GC_LOG_FILE_OVERWRITE + " analysis not identified.");
    }

    @Test
    void testJdk8GcLogRotationDisabledFileOverwrite() {
        String jvmOptions = "-XX:+PrintGC -Xloggc:gc.log -XX:+PrintGCDetails -XX:+PrintGCTimeStamps "
                + "-XX:-UseGCLogFileRotation";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_GC_LOG_FILE_OVERWRITE),
                Analysis.WARN_GC_LOG_FILE_OVERWRITE + " analysis not identified.");
    }

    @Test
    void testSummaryStatsParallel() {
        File testFile = TestUtil.getFile("dataset1.txt");
        GcManager gcManager = new GcManager();
        gcManager.store(testFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(kilobytes(248192), jvmRun.getMaxYoungSpace(), "Max young space not calculated correctly.");
        assertEquals(kilobytes(786432), jvmRun.getMaxOldSpace(), "Max old space not calculated correctly.");
        assertEquals((long) 3, jvmRun.getNewRatio(), "NewRatio not calculated correctly.");
        assertEquals(kilobytes(1034624), jvmRun.getMaxHeapSpace(), "Max heap space not calculated correctly.");
        assertEquals(kilobytes(792466), jvmRun.getMaxHeapAfterGc(), "Max heap after GC not calculated correctly.");
        assertEquals(kilobytes(1013058), jvmRun.getMaxHeapOccupancy(), "Max heap occupancy not calculated correctly.");
        assertEquals(2782, jvmRun.getMaxGcPause(), "Max pause not calculated correctly.");
        assertEquals(kilobytes(159936), jvmRun.getMaxPermSpace(), "Max perm gen space not calculated correctly.");
        assertEquals(kilobytes(76972), jvmRun.getMaxPermOccupancy(),
                "Max perm gen occupancy not calculated correctly.");
        assertEquals(kilobytes(76972), jvmRun.getMaxPermOccupancy(), "Max perm gen after GC not calculated correctly.");
        assertEquals((long) 5615, jvmRun.getTotalGcPause(), "Total GC duration not calculated correctly.");
        assertEquals(2, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD),
                JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING),
                Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL),
                Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis not identified.");
    }

    @Test
    void testSummaryStatsParNew() {
        File testFile = TestUtil.getFile("dataset2.txt");
        GcManager gcManager = new GcManager();
        gcManager.store(testFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(kilobytes(348864), jvmRun.getMaxYoungSpace(), "Max young space not calculated correctly.");
        assertEquals(kilobytes(699392), jvmRun.getMaxOldSpace(), "Max old space not calculated correctly.");
        assertEquals((long) 2, jvmRun.getNewRatio(), "NewRatio not calculated correctly.");
        assertEquals(kilobytes(1048256), jvmRun.getMaxHeapSpace(), "Max heap space not calculated correctly.");
        assertEquals(kilobytes(106395), jvmRun.getMaxHeapAfterGc(), "Max heap after GC not calculated correctly.");
        assertEquals(kilobytes(424192), jvmRun.getMaxHeapOccupancy(), "Max heap occupancy not calculated correctly.");
        assertEquals(1070, jvmRun.getMaxGcPause(), "Max pause not calculated correctly.");
        assertEquals(kilobytes(99804), jvmRun.getMaxPermSpace(), "Max perm gen space not calculated correctly.");
        assertEquals(kilobytes(60155), jvmRun.getMaxPermOccupancy(),
                "Max perm gen occupancy not calculated correctly.");
        assertEquals(kilobytes(60151), jvmRun.getMaxPermAfterGc(), "Max perm gen after GC not calculated correctly.");
        assertEquals((long) 1283, jvmRun.getTotalGcPause(), "Total GC duration not calculated correctly.");
        assertEquals(2, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                JdkUtil.LogEventType.PAR_NEW.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS),
                Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.");
    }

    @Test
    void testSummaryStatsShenandoah() {
        File testFile = TestUtil.getFile("dataset207.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FULL_GC),
                JdkUtil.LogEventType.SHENANDOAH_FULL_GC.toString() + " collector not identified.");
        assertEquals(megabytes(1589), jvmRun.getMaxHeapOccupancy(), "Max heap occupancy not calculated correctly.");
        assertEquals(megabytes(1002), jvmRun.getMaxHeapAfterGc(), "Max heap after GC not calculated correctly.");
        assertEquals(megabytes(1690), jvmRun.getMaxHeapSpace(), "Max heap space not calculated correctly.");
        assertEquals(kilobytes(282195), jvmRun.getMaxPermOccupancy(),
                "Max metaspace occupancy not calculated correctly.");
        assertEquals(kilobytes(281648), jvmRun.getMaxPermAfterGc(), "Max metaspace after GC not calculated correctly.");
        assertEquals(kilobytes(1314816), jvmRun.getMaxPermSpace(), "Max metaspace space not calculated correctly.");
        assertEquals(4077, jvmRun.getMaxGcPause(), "Max pause not calculated correctly.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SHENANDOAH_FULL_GC),
                Analysis.ERROR_SHENANDOAH_FULL_GC + " analysis not identified.");
    }

    /**
     * Test parsing logging with -XX:+PrintGCApplicationConcurrentTime and -XX:+PrintGCApplicationStoppedTime output.
     */
    @Test
    void testParseLoggingWithApplicationTime() {
        File testFile = TestUtil.getFile("dataset3.txt");
        GcManager gcManager = new GcManager();
        gcManager.store(testFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(kilobytes(1100288), jvmRun.getMaxYoungSpace(), "Max young space not calculated correctly.");
        assertEquals(kilobytes(1100288), jvmRun.getMaxOldSpace(), "Max old space not calculated correctly.");
        assertEquals((long) 1, jvmRun.getNewRatio(), "NewRatio not calculated correctly.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(0, jvmRun.getUnidentifiedLogLines().size(), "Should not be any unidentified log lines.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_NEW_RATIO_INVERTED),
                Analysis.INFO_NEW_RATIO_INVERTED + " analysis not identified.");
    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code> with underlying <code>ParallelCompactingOldEvent</code>
     * .
     */
    @Test
    void testSplitParallelOldCompactingEventLogging() {
        File testFile = TestUtil.getFile("dataset28.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED),
                Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED),
                Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.");
    }

    /**
     * Test preprocessing a combined <code>CmsConcurrentEvent</code> and <code>ApplicationConcurrentTimeEvent</code>
     * split across 2 lines.
     */
    @Test
    void testCombinedCmsConcurrentApplicationConcurrentTimeLogging() {
        File testFile = TestUtil.getFile("dataset19.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    /**
     * Test preprocessing a combined <code>CmsConcurrentEvent</code> and <code>ApplicationStoppedTimeEvent</code> split
     * across 2 lines.
     */
    @Test
    void testCombinedCmsConcurrentApplicationStoppedTimeLogging() {
        File testFile = TestUtil.getFile("dataset27.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
    }

    @Test
    void testRemoveBlankLines() {
        File testFile = TestUtil.getFile("dataset20.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME),
                Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.");

    }

    /**
     * Test <code>DateStampPreprocessAction</code>.
     */
    @Test
    void testDateStampPreprocessActionLogging() {
        File testFile = TestUtil.getFile("dataset25.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, parseDate("2010-02-26"));
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
    }

    @Test
    void testSummaryStatsStoppedTime() {
        File testFile = TestUtil.getFile("dataset41.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.APPLICATION_STOPPED_TIME),
                JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + " not identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertEquals((long) 62, jvmRun.getTotalGcPause(), "GC pause total not correct.");
        assertEquals((long) 2192, jvmRun.getFirstGcEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 2847, jvmRun.getLastGcEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(41453, jvmRun.getLastGcEvent().getDuration(), "GC last duration not correct.");
        assertEquals(6, jvmRun.getStoppedTimeEventCount(), "Stopped Time event count not correct.");
        assertEquals(1064, jvmRun.getTotalStoppedTime(), "Stopped time total not correct.");
        assertEquals((long) 964, jvmRun.getFirstStoppedEvent().getTimestamp(), "Stopped first timestamp not correct.");
        assertEquals((long) 3884, jvmRun.getLastStoppedEvent().getTimestamp(), "Stopped last timestamp not correct.");
        assertEquals(1000688, jvmRun.getLastStoppedEvent().getDuration(), "Stopped last duration not correct.");
        assertEquals((long) 964, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 3884, jvmRun.getLastEvent().getTimestamp(), "JVM last event timestamp not correct.");
        assertEquals((long) 4884, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 99, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 78, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_GC_STOPPED_RATIO),
                Analysis.WARN_GC_STOPPED_RATIO + " analysis not identified.");
    }

    @Test
    void testSummaryStatsUnifiedStoppedTime() {
        File testFile = TestUtil.getFile("dataset182.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.LogEventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_CONCURRENT),
                JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_REMARK),
                JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNIFIED_G1_CLEANUP),
                JdkUtil.LogEventType.UNIFIED_G1_CLEANUP.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SAFEPOINT),
                JdkUtil.LogEventType.SAFEPOINT.toString() + " not identified.");
        assertEquals(5, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertEquals((long) 24, jvmRun.getTotalGcPause(), "GC pause total not correct.");
        assertEquals((long) 53, jvmRun.getFirstGcEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 167, jvmRun.getLastGcEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(362, jvmRun.getLastGcEvent().getDuration(), "GC last duration not correct.");
        assertEquals(12, jvmRun.getSafepointEventCount(), "Safepoint event count not correct.");
        assertEquals(25, jvmRun.getTotalSafepointTime(), "Safepoint time total not correct.");
        assertEquals((long) 29, jvmRun.getFirstSafepointEvent().getTimestamp(),
                "Safepoint first timestamp not correct.");
        assertEquals((long) 167, jvmRun.getLastSafepointEvent().getTimestamp(),
                "Safepoint last timestamp not correct.");
        assertEquals(439, jvmRun.getLastSafepointEvent().getDuration(), "Safepoint last duration not correct.");
        assertEquals((long) 53, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 167, jvmRun.getLastEvent().getTimestamp(), "JVM last event timestamp not correct.");
        assertEquals((long) 167, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 86, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 85, jvmRun.getSafepointThroughput(), "Safepoint throughput not correct.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_GC_STOPPED_RATIO),
                Analysis.WARN_GC_STOPPED_RATIO + " analysis incorrectly identified.");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     */
    @Test
    void testExplicitGcAnalsysisParallelSerialOld() {
        File testFile = TestUtil.getFile("dataset56.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SCAVENGE),
                JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD),
                JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL),
                Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL),
                Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis not identified.");
    }

    /**
     * Test JVM Header parsing.
     * 
     */
    @Test
    void testHeaders() {
        File testFile = TestUtil.getFile("dataset59.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS),
                JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY),
                JdkUtil.LogEventType.HEADER_MEMORY.toString() + " not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION),
                JdkUtil.LogEventType.HEADER_VERSION.toString() + " not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_DISABLED),
                Analysis.WARN_EXPLICIT_GC_DISABLED + " analysis not identified.");
    }

    /**
     * Test <code>PrintTenuringDistributionPreprocessAction</code> with no space after "GC".
     * 
     */
    @Test
    void testPrintTenuringDistributionPreprocessActionNoSpaceAfterGc() {
        File testFile = TestUtil.getFile("dataset66.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
    }

    /**
     * Test application stopped time w/o timestamps.
     */
    @Test
    void testApplicationStoppedTimeNoTimestamps() {
        File testFile = TestUtil.getFile("dataset96.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals((long) 2097, jvmRun.getTotalGcPause(), "GC pause total not correct.");
        assertEquals((long) 16517, jvmRun.getFirstGcEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 31432, jvmRun.getLastGcEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(271019, jvmRun.getLastGcEvent().getDuration(), "GC last duration not correct.");
        assertEquals(1830, jvmRun.getTotalStoppedTime(), "Stopped time total not correct.");
        assertEquals((long) 0, jvmRun.getFirstStoppedEvent().getTimestamp(), "Stopped first timestamp not correct.");
        assertEquals((long) 0, jvmRun.getLastStoppedEvent().getTimestamp(), "Stopped last timestamp not correct.");
        assertEquals(50, jvmRun.getLastStoppedEvent().getDuration(), "Stopped last duration not correct.");
        assertEquals((long) 16517, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 31432, jvmRun.getLastEvent().getTimestamp(), "JVM last event timestamp not correct.");
        assertEquals((long) 31703, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 93, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 94, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
        assertEquals((long) 0, jvmRun.getInvertedParallelismCount(), "Inverted parallelism event count not correct.");
    }

    /**
     * Test summary stats for a partial log file (1st timestamp > Constants.FIRST_TIMESTAMP_THRESHOLD). Same data as
     * dataset41.txt with 1000 seconds added to each timestamp.
     */
    @Test
    void testSummaryStatsPartialLog() {
        File testFile = TestUtil.getFile("dataset98.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "GC event type count not correct.");
        assertEquals(2, jvmRun.getBlockingEventCount(), "GC blocking event count not correct.");
        assertEquals((long) 62, jvmRun.getTotalGcPause(), "GC pause total not correct.");
        assertEquals((long) 1002192, jvmRun.getFirstGcEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 1002847, jvmRun.getLastGcEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(41453, jvmRun.getLastGcEvent().getDuration(), "GC last duration not correct.");
        assertEquals(6, jvmRun.getStoppedTimeEventCount(), "Stopped Time event count not correct.");
        assertEquals(1064, jvmRun.getTotalStoppedTime(), "Stopped time total not correct.");
        assertEquals((long) 1000964, jvmRun.getFirstStoppedEvent().getTimestamp(),
                "Stopped first timestamp not correct.");
        assertEquals((long) 1003884, jvmRun.getLastStoppedEvent().getTimestamp(),
                "Stopped last timestamp not correct.");
        assertEquals(1000688, jvmRun.getLastStoppedEvent().getDuration(), "Stopped last duration not correct.");
        assertEquals((long) 1000964, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 1003884, jvmRun.getLastEvent().getTimestamp(), "JVM last event timestamp not correct.");
        assertEquals((long) 3920, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 98, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 73, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_GC_STOPPED_RATIO),
                Analysis.WARN_GC_STOPPED_RATIO + " analysis not identified.");
    }

    /**
     * Test summary stats with batching.
     */
    @Test
    void testStoppedTime() {
        File testFile = TestUtil.getFile("dataset103.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "GC event type count not correct.");
        assertEquals(160, jvmRun.getBlockingEventCount(), "GC blocking event count not correct.");
        assertEquals((long) 2568199, jvmRun.getTotalGcPause(), "GC pause total not correct.");
        assertEquals((long) 4364, jvmRun.getFirstGcEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 2801954, jvmRun.getLastGcEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(25963804, jvmRun.getLastGcEvent().getDuration(), "GC last duration not correct.");
        assertEquals(151, jvmRun.getStoppedTimeEventCount(), "Stopped Time event count not correct.");
        assertEquals(2721420, jvmRun.getTotalStoppedTime(), "Stopped time total not correct.");
        assertEquals((long) 0, jvmRun.getFirstStoppedEvent().getTimestamp(), "Stopped first timestamp not correct.");
        assertEquals((long) 0, jvmRun.getLastStoppedEvent().getTimestamp(), "Stopped last timestamp not correct.");
        assertEquals(36651675, jvmRun.getLastStoppedEvent().getDuration(), "Stopped last duration not correct.");
        assertEquals((long) 4364, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 2801954, jvmRun.getLastEvent().getTimestamp(), "JVM last timestamp not correct.");
        assertEquals((long) 2827917, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 9, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 4, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_GC_STOPPED_RATIO),
                Analysis.WARN_GC_STOPPED_RATIO + " analysis incorrectly identified.");
        assertEquals((long) 0, jvmRun.getInvertedParallelismCount(), "Inverted parallelism event count not correct.");
    }

    /**
     * Test no gc logging events, only stopped time events.
     */
    @Test
    void testStoppedTimeWithoutGcEvents() {
        File testFile = TestUtil.getFile("dataset108.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        gcManager.store(testFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals((long) 0, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
    }

    /**
     * Test identifying <code>ParNewEvent</code> running in incremental mode.
     */
    @Test
    void testPrintGcApplicationConcurrentTimeAnalysis() {
        File testFile = TestUtil.getFile("dataset104.txt");
        Jvm jvm = new Jvm(null, null);
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME),
                Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.");
    }

}
