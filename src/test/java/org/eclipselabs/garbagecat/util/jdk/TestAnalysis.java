/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

import static org.eclipselabs.garbagecat.util.Memory.bytes;
import static org.eclipselabs.garbagecat.util.Memory.Unit.MEGABYTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.github.joa.domain.Bit;
import org.github.joa.domain.GarbageCollector;
import org.github.joa.domain.JvmContext;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestAnalysis {

    @Test
    void testAdaptiveSizePolicy() {
        String jvmOptions = "-XX:InitialHeapSize=2147483648 -XX:MaxHeapSize=8589934592 -XX:-UseAdaptiveSizePolicy";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_HEAP_MIN_NOT_EQUAL_MAX.getKey()),
                org.github.joa.util.Analysis.INFO_HEAP_MIN_NOT_EQUAL_MAX + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_ADAPTIVE_SIZE_POLICY_DISABLED.getKey()),
                org.github.joa.util.Analysis.WARN_ADAPTIVE_SIZE_POLICY_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.getJvmOptions().hasAnalysis(org.github.joa.util.Analysis.INFO_UNACCOUNTED_OPTIONS_DISABLED),
                org.github.joa.util.Analysis.INFO_UNACCOUNTED_OPTIONS_DISABLED + " analysis incorrectly identified.");
    }

    /**
     * Test analysis perm gen or metaspace size not set.
     * 
     * @throws IOException
     */
    @Test
    void testAnalysisPermSizeNotSet() throws IOException {
        File testFile = TestUtil.getFile("dataset60.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_PERM_SIZE_NOT_SET.getKey()),
                Analysis.WARN_PERM_SIZE_NOT_SET + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT.getKey()),
                org.github.joa.util.Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.");
    }

    /**
     * Test application/gc logging mixed.
     * 
     * @throws IOException
     */
    @Test
    void testApplicationLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset114.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_APPLICATION_LOGGING.getKey()),
                Analysis.WARN_APPLICATION_LOGGING + " analysis not identified.");
        assertEquals(Bit.BIT64, jvmRun.getJvmOptions().getJvmContext().getBit(), "64-bit not identified.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_THREAD_STACK_SIZE_NOT_SET_32.getKey()),
                org.github.joa.util.Analysis.WARN_THREAD_STACK_SIZE_NOT_SET_32 + " analysis incorrectly identified.");

    }

    @Test
    void testApplicationStoppedTimeMissingNoData() {
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNKNOWN);
        jvmRun.setEventTypes(eventTypes);
        jvmRun.getAnalysis().clear();
        jvmRun.doAnalysis();
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING.getKey()),
                Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis incorrectly identified.");
    }

    @Test
    void testCGroupMemoryLimit() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_CGROUP_MEMORY_LIMIT.getKey()),
                org.github.joa.util.Analysis.WARN_CGROUP_MEMORY_LIMIT + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_EXPERIMENTAL_VM_OPTIONS_ENABLED.getKey()),
                org.github.joa.util.Analysis.WARN_EXPERIMENTAL_VM_OPTIONS_ENABLED + " analysis not identified.");
    }

    /**
     * Test CMS class unloading disabled.
     * 
     * @throws IOException
     */
    @Test
    void testCmsClassunloadingDisabled() throws IOException {
        File testFile = TestUtil.getFile("dataset110.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED.getKey()),
                org.github.joa.util.Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED.getKey()),
                Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis incorrectly identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_CLASS_UNLOADING_DISABLED.getKey()),
                org.github.joa.util.Analysis.WARN_CLASS_UNLOADING_DISABLED + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA.getKey()),
                org.github.joa.util.Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA + " analysis not identified.");
    }

    /**
     * Test CMS initial mark low parallelism.
     * 
     * @throws IOException
     */
    @Test
    void testCmsInitialMarkSerial() throws IOException {
        File testFile = TestUtil.getFile("dataset130.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM.getKey()),
                Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis not identified.");
    }

    /**
     * Test CMS remark low parallelism.
     * 
     * @throws IOException
     */
    @Test
    void testCmsRemarkSerial() throws IOException {
        File testFile = TestUtil.getFile("dataset131.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM.getKey()),
                Analysis.WARN_CMS_REMARK_LOW_PARALLELISM + " analysis not identified.");
    }

    /**
     * Test CMS_SERIAL_OLD caused by <code>Analysis.KEY_EXPLICIT_GC_SERIAL</code> does not return
     * <code>Analysis.KEY_SERIAL_GC_CMS</code>.
     * 
     * @throws IOException
     */
    @Test
    void testCmsSerialOldExplicitGc() throws IOException {
        File testFile = TestUtil.getFile("dataset85.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.EventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.EventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS.getKey()),
                Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_CMS.getKey()),
                Analysis.ERROR_SERIAL_GC_CMS + " analysis incorrectly identified.");
    }

    /**
     * Test CMS_SERIAL_OLD triggered by GCLocker promotion failure.
     * 
     * @throws IOException
     */
    @Test
    void testCmsSerialOldGcLocker() throws IOException {
        File testFile = TestUtil.getFile("dataset119.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED.getKey()),
                Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED.getKey()),
                Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED + " analysis incorrectly identified.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_GC_LOG_STDOUT.getKey()),
                org.github.joa.util.Analysis.INFO_GC_LOG_STDOUT + " analysis incorrectly identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_JDK8_GC_LOG_FILE_ROTATION_NOT_ENABLED.getKey()),
                org.github.joa.util.Analysis.WARN_JDK8_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis not identified.");
    }

    /**
     * Test compressed oops disabled with heap >= 32G.
     * 
     * @throws IOException
     */
    @Test
    void testCompressedOopsDisabledLargeHeap() throws IOException {
        File testFile = TestUtil.getFile("dataset106.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("-XX:MaxHeapSize=45097156608", jvmRun.getJvmOptions().getMaxHeapSize(),
                "Max heap value not parsed correctly.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_COMP_OOPS_DISABLED_HEAP_32G_LT.getKey()),
                org.github.joa.util.Analysis.WARN_COMP_OOPS_DISABLED_HEAP_32G_LT + " analysis incorrectly identified.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_COMP_OOPS_ENABLED_HEAP_32G_GTE.getKey()),
                org.github.joa.util.Analysis.WARN_COMP_OOPS_ENABLED_HEAP_32G_GTE + " analysis incorrectly identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_COMP_CLASS_SIZE_HEAP_32G_GTE.getKey()),
                org.github.joa.util.Analysis.WARN_COMP_CLASS_SIZE_HEAP_32G_GTE + " analysis not identified.");
    }

    /**
     * Test diagnostic options
     * 
     * @throws IOException
     */
    @Test
    void testDiagnosticOptions() throws IOException {
        File testFile = TestUtil.getFile("dataset192.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_JMX_ENABLED.getKey()),
                org.github.joa.util.Analysis.INFO_JMX_ENABLED + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED.getKey()),
                org.github.joa.util.Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED + " analysis not identified.");
    }

    /**
     * Test -XX:+HeapDumpOnOutOfMemoryError -XX:+ExitOnOutOfMemoryError
     * 
     * @throws IOException
     */
    @Test
    void testExitOnOutOfMemoryError() throws IOException {
        File testFile = TestUtil.getFile("dataset291.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_OOME_EXIT.getKey()),
                Analysis.ERROR_OOME_EXIT + " analysis not identified.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
    }

    @Test
    void testExplicitGcDiagnostic() throws IOException {
        File testFile = TestUtil.getFile("dataset249.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.UNIFIED_G1_FULL_GC_PARALLEL),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_G1_FULL_GC_PARALLEL.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_EXPLICIT_GC_DIAGNOSTIC.getKey()),
                Analysis.WARN_EXPLICIT_GC_DIAGNOSTIC + " analysis not identified.");
    }

    @Test
    void testFailedToReserveSharedMemoryErrNo12() throws IOException {
        File testFile = TestUtil.getFile("dataset233.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.VM_WARNING),
                JdkUtil.EventType.VM_WARNING.toString() + " event identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_SHARED_MEMORY_12.getKey()),
                Analysis.ERROR_SHARED_MEMORY_12 + " analysis identified.");
    }

    /**
     * Test -XX:+UseFastUnorderedTimeStamps
     * 
     * @throws IOException
     */
    @Test
    void testFastUnorderedTimestamps() throws IOException {
        File testFile = TestUtil.getFile("dataset193.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_FAST_UNORDERED_TIMESTAMPS.getKey()),
                org.github.joa.util.Analysis.WARN_FAST_UNORDERED_TIMESTAMPS + " analysis not identified.");
    }

    @Test
    void testFirstTimestampThreshhold() throws IOException {
        File testFile = TestUtil.getFile("dataset250.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasAnalysis(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED.getKey()),
                Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED + " analysis incorrectly identified.");
    }

    /**
     * Test FooterHeapEvent not incorrectly identified as PrintHeapAtGc
     * 
     * @throws IOException
     */
    @Test
    void testFooterHeapEvent() throws IOException {
        File testFile = TestUtil.getFile("dataset208.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(5, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.HEAP_AT_GC),
                JdkUtil.EventType.HEAP_AT_GC.toString() + " incorrectly identified.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_HEAP_AT_GC.getKey()),
                org.github.joa.util.Analysis.INFO_JDK8_PRINT_HEAP_AT_GC + " analysis incorrectly identified.");
    }

    @Test
    void testG1SummarizeRSetStatsPeriod0() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+G1SummarizeRSetStats "
                + "-XX:G1SummarizeRSetStatsPeriod=0";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT.getKey()),
                org.github.joa.util.Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT + " analysis incorrectly identified.");
    }

    @Test
    void testGcLocker() {
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<GcTrigger> triggers = new ArrayList<GcTrigger>();
        triggers.add(GcTrigger.GCLOCKER_INITIATED_GC);
        jvmRun.setGcTriggers(triggers);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_GC_LOCKER.getKey()),
                Analysis.WARN_GC_LOCKER + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_GC_LOCKER_RETRY.getKey()),
                Analysis.ERROR_GC_LOCKER_RETRY + " analysis incorrectly identified.");
    }

    @Test
    void testGcLockerRetry() {
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_GC_LOCKER_RETRY);
        jvmRun.setEventTypes(eventTypes);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_GC_LOCKER_RETRY.getKey()),
                Analysis.ERROR_GC_LOCKER_RETRY + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_GC_LOCKER.getKey()),
                Analysis.WARN_GC_LOCKER + " analysis incorrectly identified.");
    }

    /**
     * Test small gc log file size.
     * 
     * @throws IOException
     */
    @Test
    void testGcLogFileSizeSmall() throws IOException {
        File testFile = TestUtil.getFile("dataset181.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_JDK8_GC_LOG_FILE_SIZE_SMALL.getKey()),
                org.github.joa.util.Analysis.WARN_JDK8_GC_LOG_FILE_SIZE_SMALL + " analysis not identified.");
    }

    @Test
    void testHeaderLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset42.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getEventTypes().contains(EventType.HEADER_COMMAND_LINE_FLAGS),
                JdkUtil.EventType.HEADER_COMMAND_LINE_FLAGS.toString() + " information not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.HEADER_MEMORY),
                JdkUtil.EventType.HEADER_MEMORY.toString() + " information not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.HEADER_VM_INFO),
                JdkUtil.EventType.HEADER_VM_INFO.toString() + " information not identified.");
        // Usually no reason to set the thread stack size on 64 bit.
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_THREAD_STACK_SIZE_NOT_SET_32.getKey()),
                org.github.joa.util.Analysis.WARN_THREAD_STACK_SIZE_NOT_SET_32 + " analysis incorrectly identified.");
    }

    @Test
    void testHeapDumpBeforeAndAfterFullGc() throws IOException {
        File testFile = TestUtil.getFile("dataset293.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_HEAP_DUMP_AFTER_FULL_GC.getKey()),
                org.github.joa.util.Analysis.WARN_HEAP_DUMP_AFTER_FULL_GC + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_HEAP_DUMP_BEFORE_FULL_GC.getKey()),
                org.github.joa.util.Analysis.WARN_HEAP_DUMP_BEFORE_FULL_GC + " analysis not identified.");
    }

    @Test
    void testHeapDumpPathFilename() throws IOException {
        File testFile = TestUtil.getFile("dataset95.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_HEAP_DUMP_PATH_FILENAME.getKey()),
                org.github.joa.util.Analysis.WARN_HEAP_DUMP_PATH_FILENAME + " analysis not identified.");
    }

    /**
     * Test heap dump location missing
     * 
     * @throws IOException
     */
    @Test
    void testHeapDumpPathMissing() throws IOException {
        File testFile = TestUtil.getFile("dataset181.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_HEAP_DUMP_PATH_MISSING.getKey()),
                org.github.joa.util.Analysis.INFO_HEAP_DUMP_PATH_MISSING + " analysis not identified.");
    }

    /**
     * Test humongous allocations on old JDK not able to reclaim humongous objects during young collections.
     * 
     * @throws IOException
     */
    @Test
    void testHumongousAllocationsNotCollectedYoung() throws IOException {
        File testFile = TestUtil.getFile("dataset118.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_G1_HUMONGOUS_JDK_OLD.getKey()),
                Analysis.ERROR_G1_HUMONGOUS_JDK_OLD + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_G1_MIXED_GC_LIVE_THRSHOLD_PRCNT.getKey()),
                org.github.joa.util.Analysis.WARN_G1_MIXED_GC_LIVE_THRSHOLD_PRCNT + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_EXPERIMENTAL_VM_OPTIONS_ENABLED.getKey()),
                org.github.joa.util.Analysis.WARN_EXPERIMENTAL_VM_OPTIONS_ENABLED + " analysis not identified.");
        assertFalse(
                jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_JDK8_GC_LOG_FILE_ROTATION_NOT_ENABLED.getKey()),
                org.github.joa.util.Analysis.WARN_JDK8_GC_LOG_FILE_ROTATION_NOT_ENABLED
                        + " analysis incorrectly identified.");
    }

    @Test
    void testInfinispanThreadDump() throws IOException {
        // Check both preprocessed and not preprocessed
        File testFile = TestUtil.getFile("dataset234.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.INFO_THREAD_DUMP.getKey()),
                Analysis.INFO_THREAD_DUMP + " analysis identified.");
    }

    /**
     * Test CMS remark low parallelism not reported with pause times less than times data centisecond precision.
     * 
     * @throws IOException
     */
    @Test
    void testInitialMarkLowParallelismFalseReportSmallPause() throws IOException {
        File testFile = TestUtil.getFile("dataset138.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM.getKey()),
                Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis incorrectly identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.INFO_SWAP_DISABLED.getKey()),
                Analysis.INFO_SWAP_DISABLED + " analysis incorrectly identified.");
    }

    /**
     * Test CMS remark low parallelism not reported with pause times less than zero.
     * 
     * @throws IOException
     */
    @Test
    void testInitialMarkLowParallelismFalseReportZeroReal() throws IOException {
        File testFile = TestUtil.getFile("dataset137.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM.getKey()),
                Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis incorrectly identified.");
    }

    @Test
    void testJvmOptionsOverride() throws IOException {
        File testFile = TestUtil.getFile("dataset294.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        String jvmOptions = "-Xmx2g";
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("-Xmx2g", jvmRun.getJvmOptions().getMaxHeapSize(), "Max heap value not parsed correctly.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_JVM_OPTIONS_OVERRIDE.getKey()),
                Analysis.WARN_JVM_OPTIONS_OVERRIDE + " analysis not identified.");
    }

    @Test
    void testMaxTenuringOverrideCms() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=14 -Xmx2048M";
        JvmContext jvmContext = new JvmContext(jvmOptions);
        List<GarbageCollector> collectors = new ArrayList<GarbageCollector>();
        collectors.add(GarbageCollector.CMS);
        jvmContext.setGarbageCollectors(collectors);
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_MAX_TENURING_OVERRIDE.getKey()),
                org.github.joa.util.Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.");
    }

    @Test
    void testMaxTenuringOverrideG1() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=6 -Xmx2048M";
        JvmContext jvmContext = new JvmContext(jvmOptions);
        List<GarbageCollector> collectors = new ArrayList<GarbageCollector>();
        collectors.add(GarbageCollector.G1);
        jvmContext.setGarbageCollectors(collectors);
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_MAX_TENURING_OVERRIDE.getKey()),
                org.github.joa.util.Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.");
    }

    @Test
    void testMaxTenuringOverrideParallel() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=6 -Xmx2048M";
        JvmContext jvmContext = new JvmContext(jvmOptions);
        List<GarbageCollector> collectors = new ArrayList<GarbageCollector>();
        collectors.add(GarbageCollector.PARALLEL_OLD);
        jvmContext.setGarbageCollectors(collectors);
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_MAX_TENURING_OVERRIDE.getKey()),
                org.github.joa.util.Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.");
    }

    /**
     * Test Metadata GC Threshold
     * 
     * @throws IOException
     */
    @Test
    void testMetadataGcThreshold() throws IOException {
        File testFile = TestUtil.getFile("dataset199.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
    }

    @Test
    void testOomeMetaspace() throws IOException {
        File testFile = TestUtil.getFile("dataset244.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(0, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_OOME_METASPACE.getKey()),
                Analysis.ERROR_OOME_METASPACE + " analysis identified.");
    }

    /**
     * Test PARALLEL_COMPACTING_OLD caused by <code>Analysis.KEY_EXPLICIT_GC_SERIAL</code>.
     * 
     * @throws IOException
     */
    @Test
    void testParallelOldCompactingExplicitGc() throws IOException {
        File testFile = TestUtil.getFile("dataset86.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.PARALLEL_SCAVENGE),
                "Log line not recognized as " + JdkUtil.EventType.PARALLEL_SCAVENGE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.PARALLEL_COMPACTING_OLD),
                "Log line not recognized as " + JdkUtil.EventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_EXPLICIT_GC_PARALLEL.getKey()),
                Analysis.WARN_EXPLICIT_GC_PARALLEL + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_PARALLEL.getKey()),
                Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis incorrectly identified.");
        assertEquals((long) 0, jvmRun.getInvertedParallelismCount(), "Inverted parallelism event count not correct.");
    }

    /**
     * Test PAR_NEW disabled with -XX:-UseParNewGC.
     * 
     * @throws IOException
     */
    @Test
    void testParNewDisabled() throws IOException {
        File testFile = TestUtil.getFile("dataset101.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.SERIAL_NEW),
                "Log line not recognized as " + JdkUtil.EventType.SERIAL_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.CMS_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.EventType.CMS_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.CMS_REMARK),
                "Log line not recognized as " + JdkUtil.EventType.CMS_REMARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.CMS_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.ERROR_JDK8_CMS_PAR_NEW_DISABLED.getKey()),
                org.github.joa.util.Analysis.ERROR_JDK8_CMS_PAR_NEW_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_SERIAL_GC.getKey()),
                Analysis.WARN_SERIAL_GC + " analysis incorrectly identified.");
        assertFalse(
                jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_JDK8_GC_LOG_FILE_ROTATION_NOT_ENABLED.getKey()),
                org.github.joa.util.Analysis.WARN_JDK8_GC_LOG_FILE_ROTATION_NOT_ENABLED
                        + " analysis incorrectly identified.");
    }

    @Test
    void testPartialLogDatestamps() throws IOException {
        File testFile = TestUtil.getFile("dataset279.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasAnalysis(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED.getKey()),
                Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED + " analysis incorrectly identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_SAFEPOINT_STATS.getKey()),
                Analysis.WARN_SAFEPOINT_STATS + " analysis not identified.");
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     * 
     * @throws IOException
     */
    @Test
    void testPhysicalMemoryLessThanHeapAllocation() throws IOException {
        File testFile = TestUtil.getFile("dataset109.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(bytes(1968287744L), jvmRun.getPhysicalMemory(), "Physical not parsed correctly.");
        assertEquals(org.github.joa.util.JdkUtil.convertSize(1968287744L, 'B', org.github.joa.util.Constants.UNITS),
                jvmRun.getJvmOptions().getJvmContext().getMemory(), "JVM context memory not correct.");
        assertEquals(bytes(4718592000L), jvmRun.getMaxHeapBytes(), "Heap size not parsed correctly.");
        assertEquals(bytes(0L), jvmRun.getMaxMetaspaceBytes(), "Metaspace size not parsed correctly.");
        assertEquals(bytes(0L), jvmRun.getCompressedClassSpaceSizeBytes(),
                "Class compressed pointer space size not parsed correctly.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_PHYSICAL_MEMORY.getKey()),
                Analysis.ERROR_PHYSICAL_MEMORY + " analysis not identified.");
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     * 
     * @throws IOException
     */
    @Test
    void testPhysicalMemoryLessThanJvmMemoryWithCompressedClassPointerSpace() throws IOException {
        File testFile = TestUtil.getFile("dataset107.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(bytes(16728526848L), jvmRun.getPhysicalMemory(), "Physical not parsed correctly.");
        assertEquals(bytes(5368709120L), jvmRun.getMaxHeapBytes(), "Heap size not parsed correctly.");
        assertEquals(bytes(3221225472L), jvmRun.getMaxMetaspaceBytes(), "Metaspace size not parsed correctly.");
        assertEquals(bytes(2147483648L), jvmRun.getCompressedClassSpaceSizeBytes(),
                "Class compressed pointer space size not parsed correctly.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_PHYSICAL_MEMORY.getKey()),
                Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.");
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     * 
     * @throws IOException
     */
    @Test
    void testPhysicalMemoryLessThanJvmMemoryWithoutCompressedClassPointerSpace() throws IOException {
        File testFile = TestUtil.getFile("dataset106.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(bytes(50465866752L), jvmRun.getPhysicalMemory(), "Physical not parsed correctly.");
        assertEquals(bytes(45097156608L), jvmRun.getMaxHeapBytes(), "Heap size not parsed correctly.");
        assertEquals(bytes(5368709120L), jvmRun.getMaxMetaspaceBytes(), "Metaspace size not parsed correctly.");
        // Class compressed pointer space has a size, but it is ignored when calculating JVM memory.
        assertEquals(bytes(1073741824L), jvmRun.getCompressedClassSpaceSizeBytes(),
                "Class compressed pointer space size not parsed correctly.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_PHYSICAL_MEMORY.getKey()),
                Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.");
    }

    /**
     * Test PrintCommandLineFlags missing.
     */
    @Test
    void testPrintCommandlineFlagsDisabled() {
        String jvmOptions = "-XX:-PrintCommandLineFlags";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_PRINT_COMMANDLINE_FLAGS_DISABLED.getKey()),
                Analysis.WARN_PRINT_COMMANDLINE_FLAGS_DISABLED + " not identified.");
    }

    /**
     * Test PrintCommandLineFlags missing.
     */
    @Test
    void testPrintCommandlineFlagsGcLogging() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getEventTypes().add(EventType.PAR_NEW);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_PRINT_COMMANDLINE_FLAGS.getKey()),
                Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis not identified.");
    }

    /**
     * Test PrintCommandLineFlags missing.
     */
    @Test
    void testPrintCommandlineFlagsNoGcLogging() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_PRINT_COMMANDLINE_FLAGS.getKey()),
                Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis incorrectly identified.");
    }

    /**
     * Test PrintCommandLineFlags not missing.
     */
    @Test
    void testPrintCommandlineFlagsNotMissing() {
        String jvmOptions = "-Xss128k -XX:+PrintCommandLineFlags -Xms2048M";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getEventTypes().add(EventType.PAR_NEW);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_PRINT_COMMANDLINE_FLAGS.getKey()),
                Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis incorrectly identified.");
    }

    /**
     * Test <code>-XX:PrintFLSStatistics</code> and <code>-XX:PrintPromotionFailure</code>.
     * 
     * @throws IOException
     */
    @Test
    void testPrintFlsStatisticsPrintPromotionFailure() throws IOException {
        File testFile = TestUtil.getFile("dataset115.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_FLS_STATISTICS.getKey()),
                org.github.joa.util.Analysis.INFO_JDK8_PRINT_FLS_STATISTICS + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_PROMOTION_FAILURE.getKey()),
                org.github.joa.util.Analysis.INFO_JDK8_PRINT_PROMOTION_FAILURE + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_USE_MEMBAR.getKey()),
                org.github.joa.util.Analysis.WARN_USE_MEMBAR + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_CMS_INIT_OCCUPANCY_ONLY_MISSING.getKey()),
                org.github.joa.util.Analysis.INFO_CMS_INIT_OCCUPANCY_ONLY_MISSING + " analysis not identified.");
    }

    /**
     * Test PrintGCDetails disabled with VERBOSE_GC logging.
     * 
     * @throws IOException
     */
    @Test
    void testPrintGcDetailsDisabledWithVerboseGc() throws IOException {
        File testFile = TestUtil.getFile("dataset107.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getEventTypes().contains(EventType.VERBOSE_GC_YOUNG),
                JdkUtil.EventType.VERBOSE_GC_YOUNG.toString() + " event not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_JDK8_PRINT_GC_DETAILS_DISABLED.getKey()),
                org.github.joa.util.Analysis.WARN_JDK8_PRINT_GC_DETAILS_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_JDK8_PRINT_GC_DETAILS_MISSING.getKey()),
                org.github.joa.util.Analysis.WARN_JDK8_PRINT_GC_DETAILS_MISSING + " analysis incorrectly identified.");
    }

    /**
     * Verify analysis file property key/value lookup.
     */
    @Test
    void testPropertyKeyValueLookup() {
        Analysis[] analysis = Analysis.values();
        for (int i = 0; i < analysis.length; i++) {
            assertNotNull(analysis[i].getKey() + " not found.", analysis[i].getValue());
        }
    }

    /**
     * Test serial promotion failed is not reported as cms promotion failed.
     * 
     * @throws IOException
     */
    @Test
    void testSerialPromotionFailed() throws IOException {
        File testFile = TestUtil.getFile("dataset129.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_CMS_PROMOTION_FAILED.getKey()),
                Analysis.ERROR_CMS_PROMOTION_FAILED + " analysis incorrectly identified.");
    }

    /**
     * Test swap disabled.
     * 
     * @throws IOException
     */
    @Test
    void testSwapDisabled() throws IOException {
        File testFile = TestUtil.getFile("dataset187.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.INFO_SWAP_DISABLED.getKey()),
                Analysis.INFO_SWAP_DISABLED + " analysis not identified.");
    }

    @Test
    void testThreadStackSizeAnalysis32Bit() throws IOException {
        File testFile = TestUtil.getFile("dataset87.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_THREAD_STACK_SIZE_NOT_SET_32.getKey()),
                org.github.joa.util.Analysis.WARN_THREAD_STACK_SIZE_NOT_SET_32 + " analysis incorrectly identified.");
    }

    /**
     * Test passing JVM options on the command line.
     */
    @Test
    void testThreadStackSizeLarge() {
        String jvmOptions = "-Xss1025k";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_THREAD_STACK_SIZE_LARGE.getKey()),
                org.github.joa.util.Analysis.WARN_THREAD_STACK_SIZE_LARGE + " analysis not identified.");
    }

    @Test
    void testUnifiedMemory() throws IOException {
        File testFile = TestUtil.getFile("dataset293.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("31900M", jvmRun.getMemory(), "memory string not correct.");
        assertEquals(new Memory(31900, MEGABYTES), jvmRun.getPhysicalMemory(), "physical memory not correct.");
    }

    /**
     * Test VERBOSE_GC_OLD triggered by explicit GC.
     * 
     * @throws IOException
     */
    @Test
    void testVerboseGcOldExplicitGc() throws IOException {
        File testFile = TestUtil.getFile("dataset125.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        // VERGOSE_GC_OLD looks the same as G1_FULL without -XX:+PrintGCDetails
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_EXPLICIT_GC_UNKNOWN.getKey()),
                Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.");
    }

    /**
     * Test VERBOSE_GC_YOUNG triggered by explicit GC.
     * 
     * @throws IOException
     */
    @Test
    void testVerboseGcYoungExplicitGc() throws IOException {
        File testFile = TestUtil.getFile("dataset126.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_EXPLICIT_GC_UNKNOWN.getKey()),
                Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.");
    }

    @Test
    void testZStatisticsInterval() throws IOException {
        File testFile = TestUtil.getFile("dataset274.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.INFO_Z_STATISTICS_INTERVAL.getKey()),
                Analysis.INFO_Z_STATISTICS_INTERVAL + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED.getKey()),
                org.github.joa.util.Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED + " analysis incorrectly identified.");
    }

    @Test
    void testZStatisticsIntervalPreprocess() throws IOException {
        File testFile = TestUtil.getFile("dataset274.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.INFO_Z_STATISTICS_INTERVAL.getKey()),
                Analysis.INFO_Z_STATISTICS_INTERVAL + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED.getKey()),
                org.github.joa.util.Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED + " analysis incorrectly identified.");
    }
}