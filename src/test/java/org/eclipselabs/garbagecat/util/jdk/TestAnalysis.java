/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
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
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_MIN_NOT_EQUAL_MAX),
                Analysis.WARN_HEAP_MIN_NOT_EQUAL_MAX + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_ADAPTIVE_SIZE_POLICY_DISABLED),
                Analysis.ERROR_ADAPTIVE_SIZE_POLICY_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_UNACCOUNTED_OPTIONS_DISABLED),
                Analysis.INFO_UNACCOUNTED_OPTIONS_DISABLED + " analysis incorrectly identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PERM_SIZE_NOT_SET),
                Analysis.WARN_PERM_SIZE_NOT_SET + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT),
                Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.");
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
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_APPLICATION_LOGGING),
                Analysis.WARN_APPLICATION_LOGGING + " analysis not identified.");
        assertTrue(jvmRun.getJvm().is64Bit(), "64-bit not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET),
                Analysis.WARN_THREAD_STACK_SIZE_NOT_SET + " analysis incorrectly identified.");

    }

    @Test
    void testApplicationStoppedTimeMissingNoData() {
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(null, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNKNOWN);
        jvmRun.setEventTypes(eventTypes);
        jvmRun.getAnalysis().clear();
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING),
                Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis incorrectly identified.");
    }

    /**
     * Test analysis background compilation disabled.
     */
    @Test
    void testBackgroundCompilationDisabled() {
        String jvmOptions = "-Xss128k -XX:-BackgroundCompilation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED),
                Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED + " analysis not identified.");
    }

    /**
     * Test analysis background compilation disabled.
     */
    @Test
    void testBackgroundCompilationDisabledXBatch() {
        String jvmOptions = "-Xss128k -Xbatch -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED),
                Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED + " analysis not identified.");
    }

    @Test
    void testBisasedLockingDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseBiasedLocking -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_BIASED_LOCKING_DISABLED),
                Analysis.WARN_BIASED_LOCKING_DISABLED + " analysis not identified.");
        jvmRun.getAnalysis().clear();
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.SHENANDOAH);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_BIASED_LOCKING_DISABLED),
                Analysis.WARN_BIASED_LOCKING_DISABLED + " analysis incorrectly identified.");

    }

    @Test
    void testCGroupMemoryLimit() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CGROUP_MEMORY_LIMIT),
                Analysis.WARN_CGROUP_MEMORY_LIMIT + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS),
                Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED),
                Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED),
                Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis incorrectly identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CLASS_UNLOADING_DISABLED),
                Analysis.WARN_CLASS_UNLOADING_DISABLED + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA),
                Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA + " analysis not identified.");
    }

    /**
     * Test CMS being used to collect old generation.
     */
    @Test
    void testCMSClassUnloadingEnabledMissing() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.CMS_CONCURRENT);
        jvmRun.setEventTypes(eventTypes);
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.CMS);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED),
                Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED),
                Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED + " analysis identified.");
    }

    /**
     * Test CMS handling perm/metaspace collections.
     */
    @Test
    void testCMSClassUnloadingEnabledMissingButNotCms() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED),
                Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM),
                Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis not identified.");
    }

    @Test
    void testCmsParallelInitialMarkDisabled() {
        String jvmOptions = "-XX:-CMSParallelInitialMarkEnabled";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED),
                Analysis.ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED + " analysis not identified.");
    }

    @Test
    void testCmsParallelRemarkDisabled() {
        String jvmOptions = "-XX:-CMSParallelRemarkEnabled";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PARALLEL_REMARK_DISABLED),
                Analysis.ERROR_CMS_PARALLEL_REMARK_DISABLED + " analysis not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS),
                Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED),
                Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED),
                Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED + " analysis incorrectly identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED),
                Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis not identified.");
    }

    /**
     * Test CMS being used to collect old generation.
     */
    @Test
    void testCmsYoungCmsOld() {
        String jvmOptions = "-Xss128k -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_SERIAL_OLD),
                Analysis.ERROR_CMS_SERIAL_OLD + " analysis identified.");
    }

    /**
     * Test CMS not being used to collect old generation.
     */
    @Test
    void testCmsYoungSerialOld() {
        String jvmOptions = "-Xss128k -XX:+UseParNewGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_SERIAL_OLD),
                Analysis.ERROR_CMS_SERIAL_OLD + " analysis not identified.");
    }

    /**
     * Test analysis just in time (JIT) compiler disabled.
     */
    @Test
    void testCompilationDisabled() {
        String jvmOptions = "-Xss128k -Xint -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_COMPILE_DISABLED),
                Analysis.WARN_BYTECODE_COMPILE_DISABLED + " analysis not identified.");
    }

    /**
     * Test analysis compilation on first invocation enabled.
     */
    @Test
    void testCompilationOnFirstInvocation() {
        String jvmOptions = "-Xss128k -Xcomp-Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_COMPILE_FIRST_INVOCATION),
                Analysis.WARN_BYTECODE_COMPILE_FIRST_INVOCATION + " analysis not identified.");
    }

    @Test
    void testCompressedClassPointersDisabledHeapLt32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedClassPointers -XX:+UseCompressedOops -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G),
                Analysis.ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G + " analysis not identified.");
    }

    @Test
    void testCompressedClassPointersDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedClassPointers -XX:+UseCompressedOops";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK),
                Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK + " analysis not identified.");
    }

    @Test
    void testCompressedClassPointersEnabledCompressedOopsDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedClassPointers -XX:-UseCompressedOops -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK),
                Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK + " analysis not identified.");
    }

    @Test
    void testCompressedClassPointersEnabledHeapGt32G() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xmx32g";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G),
                Analysis.ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G + " analysis not identified.");
    }

    @Test
    void testCompressedClassSpaceSizeWithCompressedClassPointersDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:CompressedClassSpaceSize=1G -XX:-UseCompressedClassPointers -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK),
                Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED),
                Analysis.INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED + " analysis not identified.");
    }

    @Test
    void testCompressedClassSpaceSizeWithCompressedOopsDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:CompressedClassSpaceSize=1G -XX:-UseCompressedOops -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK),
                Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED),
                Analysis.INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED + " analysis not identified.");
    }

    @Test
    void testCompressedOopsDisabledHeapEqual32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedOops -Xmx32G";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G),
                Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK),
                Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK + " analysis incorrectly identified.");
    }

    @Test
    void testCompressedOopsDisabledHeapGreater32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedOops -Xmx40G";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G),
                Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis incorrectly identified.");
    }

    @Test
    void testCompressedOopsDisabledHeapLess32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedOops -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G),
                Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("45097156608", jvmRun.getJvm().getMaxHeapValue(), "Max heap value not parsed correctly.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G),
                Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G),
                Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G + " analysis incorrectly identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_CLASS_SIZE_HEAP_GT_32G),
                Analysis.ERROR_COMP_CLASS_SIZE_HEAP_GT_32G + " analysis not identified.");
    }

    @Test
    void testCompressedOopsEnabledHeapGreater32G() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedOops -Xmx40G";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G),
                Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G + " analysis not identified.");
    }

    /**
     * Test analysis not small DGC intervals.
     */
    @Test
    void testDgcLargeIntervals() {
        String jvmOptions = "-Dsun.rmi.dgc.client.gcInterval=9223372036854775807 "
                + "-Dsun.rmi.dgc.server.gcInterval=9223372036854775807";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL),
                Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL + " analysis identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL),
                Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL + " analysis identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_LARGE),
                Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_LARGE),
                Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL + " analysis not identified.");
    }

    /**
     * Test DGC redundant options analysis.
     */
    @Test
    void testDgcRedundantOptions() {
        String jvmOptions = "-XX:+DisableExplicitGC -Dsun.rmi.dgc.client.gcInterval=14400000 "
                + "-Dsun.rmi.dgc.server.gcInterval=24400000";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT),
                Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT),
                Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT + " analysis not identified.");
    }

    /**
     * Test analysis small DGC intervals
     */
    @Test
    void testDgcSmallIntervals() {
        String jvmOptions = "-Dsun.rmi.dgc.client.gcInterval=3599999 -Dsun.rmi.dgc.server.gcInterval=3599999";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL),
                Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL),
                Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL + " analysis not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_JMX_ENABLED),
                Analysis.INFO_JMX_ENABLED + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED),
                Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED + " analysis not identified.");
    }

    /**
     * Test DisableExplicitGC in combination with ExplicitGCInvokesConcurrent.
     */
    @Test
    void testDisableExplictGcWithConcurrentHandling() {
        String jvmOptions = "-Xss128k -XX:+DisableExplicitGC -XX:+ExplicitGCInvokesConcurrent -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_DISABLED_CONCURRENT),
                Analysis.WARN_EXPLICIT_GC_DISABLED_CONCURRENT + " analysis not identified.");
    }

    @Test
    void testExperimentalOptionsEnabled() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        jvm.setVersion("1.8.0_91-b14");
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS),
                Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis not identified.");
    }

    @Test
    void testExplicitGcDiagnostic() throws IOException {
        File testFile = TestUtil.getFile("dataset249.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.G1_FULL_GC_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC_PARALLEL.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_DIAGNOSTIC),
                Analysis.WARN_EXPLICIT_GC_DIAGNOSTIC + " analysis not identified.");
    }

    /**
     * Test analysis explicit GC not concurrent.
     */
    @Test
    void testExplicitGcNotConcurrentCms() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.CMS_CONCURRENT);
        jvmRun.setEventTypes(eventTypes);
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.CMS);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT),
                Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.");
    }

    /**
     * Test analysis explicit GC not concurrent.
     */
    @Test
    void testExplicitGcNotConcurrentG1() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.G1_FULL_GC_SERIAL);
        jvmRun.setEventTypes(eventTypes);
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.G1);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT),
                Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.");
    }

    @Test
    void testFailedToReserveSharedMemoryErrNo12() throws IOException {
        File testFile = TestUtil.getFile("dataset233.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.VM_WARNING),
                JdkUtil.LogEventType.VM_WARNING.toString() + " collector identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SHARED_MEMORY_12),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_FAST_UNORDERED_TIMESTAMPS),
                Analysis.WARN_FAST_UNORDERED_TIMESTAMPS + " analysis incorrectly identified.");
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
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(5, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_HEAP_AT_GC),
                Analysis.WARN_PRINT_HEAP_AT_GC + " analysis identified.");
    }

    @Test
    void testG1SummarizeRSetStatsPeriod0() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+G1SummarizeRSetStats "
                + "-XX:G1SummarizeRSetStatsPeriod=0";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT),
                Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT + " analysis incorrectly identified.");
    }

    @Test
    void testGcLogFileRotationNotEnabled() {
        String jvmOptions = "-XX:+UseG1";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED),
                Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis incorrectly identified.");
        jvmRun.getAnalysis().clear();
        jvmRun.getEventTypes().add(LogEventType.PARALLEL_SCAVENGE);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED),
                Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis not identified.");
        jvmRun.getAnalysis().clear();
        jvmRun.getEventTypes().add(LogEventType.UNIFIED_CONCURRENT);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED),
                Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis incorrectly identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_GC_LOG_FILE_SIZE_SMALL),
                Analysis.WARN_GC_LOG_FILE_SIZE_SMALL + " analysis not identified.");
    }

    @Test
    void testHeaderLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset42.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS),
                JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " information not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY),
                JdkUtil.LogEventType.HEADER_MEMORY.toString() + " information not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION),
                JdkUtil.LogEventType.HEADER_VERSION.toString() + " information not identified.");
        // Usually no reason to set the thread stack size on 64 bit.
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET),
                Analysis.WARN_THREAD_STACK_SIZE_NOT_SET + " analysis incorrectly identified.");
    }

    /**
     * Test analysis if heap dump on OOME enabled.
     */
    @Test
    void testHeapDumpOnOutOfMemoryError() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_ON_OOME_MISSING),
                Analysis.WARN_HEAP_DUMP_ON_OOME_MISSING + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_HEAP_DUMP_PATH_MISSING),
                Analysis.INFO_HEAP_DUMP_PATH_MISSING + " analysis incorrectly identified.");
    }

    /**
     * Test HeapDumpOnOutOfMemoryError disabled.
     */
    @Test
    void testHeapDumpOnOutOfMemoryErrorDisabled() {
        String jvmOptions = "-Xss128k -XX:-HeapDumpOnOutOfMemoryError -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_ON_OOME_DISABLED),
                Analysis.WARN_HEAP_DUMP_ON_OOME_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_HEAP_DUMP_PATH_MISSING),
                Analysis.INFO_HEAP_DUMP_PATH_MISSING + " analysis incorrectly identified.");
    }

    @Test
    void testHeapDumpPathFilename() throws IOException {
        File testFile = TestUtil.getFile("dataset95.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_PATH_FILENAME),
                Analysis.WARN_HEAP_DUMP_PATH_FILENAME + " analysis not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_HEAP_DUMP_PATH_MISSING),
                Analysis.INFO_HEAP_DUMP_PATH_MISSING + " analysis not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_G1_HUMONGOUS_JDK_OLD),
                Analysis.ERROR_G1_HUMONGOUS_JDK_OLD + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_G1_MIXED_GC_LIVE_THRSHOLD_PRCNT),
                Analysis.WARN_G1_MIXED_GC_LIVE_THRSHOLD_PRCNT + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS),
                Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED),
                Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis incorrectly identified.");
    }

    @Test
    void testInfinispanThreadDump() throws IOException {
        // Check both preprocessed and not preprocessed
        File testFile = TestUtil.getFile("dataset234.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_THREAD_DUMP),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM),
                Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_SWAP_DISABLED),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM),
                Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis incorrectly identified.");
    }

    /**
     * Test analysis if instrumentation being used.
     */
    @Test
    void testInstrumentation() {
        String jvmOptions = "-Xss128k -Xms2048M -javaagent:byteman.jar=script:kill-3.btm,boot:byteman.jar -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_INSTRUMENTATION),
                Analysis.INFO_INSTRUMENTATION + " analysis not identified.");
    }

    @Test
    void testJdk8G1PriorUpdate40() {
        String jvmOptions = "";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_20-b32) ");
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.G1);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40),
                Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS),
                Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS),
                Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.");
    }

    @Test
    void testJdk8G1PriorUpdate40NoLoggingEvents() {
        String jvmOptions = "-XX:+UseG1GC";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_20-b32) ");
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40),
                Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS),
                Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS),
                Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.");
    }

    @Test
    void testJdk8G1PriorUpdate40WithRecommendedJvmOptions() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:G1MixedGCLiveThresholdPercent=85 "
                + "-XX:G1HeapWastePercent=5";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_20-b32) ");
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.G1);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40),
                Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS),
                Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS),
                Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.");
    }

    @Test
    void testJdk8NotG1PriorUpdate40() {
        String jvmOptions = "";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_20-b32) ");
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.CMS);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40),
                Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS),
                Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS),
                Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.");
    }

    @Test
    void testJdk8Update40() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_40-b26) ");
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS),
                Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40),
                Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS),
                Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis incorrectly identified.");
    }

    @Test
    void testLogFileNumberWithRotationDisabled() {
        String jvmOptions = "-Xss128k -XX:NumberOfGCLogFiles=5 -XX:-UseGCLogFileRotation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED),
                Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED),
                Analysis.WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED + " analysis not identified.");
    }

    @Test
    void testLogFileRotationDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseGCLogFileRotation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED),
                Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED + " analysis not identified.");
    }

    @Test
    void testMaxTenuringOverrideCms() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=14 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<CollectorFamily> collectors = new ArrayList<CollectorFamily>();
        collectors.add(JdkUtil.CollectorFamily.CMS);
        jvmRun.setCollectorFamilies(collectors);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_MAX_TENURING_OVERRIDE),
                Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.");
    }

    @Test
    void testMaxTenuringOverrideG1() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=6 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<CollectorFamily> collectors = new ArrayList<CollectorFamily>();
        collectors.add(JdkUtil.CollectorFamily.G1);
        jvmRun.setCollectorFamilies(collectors);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_MAX_TENURING_OVERRIDE),
                Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.");
    }

    @Test
    void testMaxTenuringOverrideParallel() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=6 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<CollectorFamily> collectors = new ArrayList<CollectorFamily>();
        collectors.add(JdkUtil.CollectorFamily.PARALLEL);
        jvmRun.setCollectorFamilies(collectors);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_MAX_TENURING_OVERRIDE),
                Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
    }

    /**
     * Test MaxMetaspaceSize is less than CompressedClassSpaceSize.
     */
    @Test
    void testMetaspaceSizeLtCompClassSize() {
        String jvmOptions = "-XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=512M -XX:CompressedClassSpaceSize=1024M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_METASPACE_SIZE_LT_COMP_CLASS_SIZE),
                Analysis.ERROR_METASPACE_SIZE_LT_COMP_CLASS_SIZE + " analysis not identified.");
    }

    /**
     * Test analysis if native library being used.
     */
    @Test
    void testNative() {
        String jvmOptions = "-Xss128k -Xms2048M -agentpath:/path/to/agent.so -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_NATIVE),
                Analysis.INFO_NATIVE + " analysis not identified.");
    }

    @Test
    void testOomeMetaspace() throws IOException {
        File testFile = TestUtil.getFile("dataset244.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(0, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_OOME_METASPACE),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SCAVENGE),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_PARALLEL),
                Analysis.WARN_EXPLICIT_GC_PARALLEL + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.SERIAL_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_INITIAL_MARK),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_REMARK),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_PAR_NEW_DISABLED),
                Analysis.WARN_CMS_PAR_NEW_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC),
                Analysis.ERROR_SERIAL_GC + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED),
                Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis incorrectly identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(bytes(1968287744L), jvmRun.getJvm().getPhysicalMemory(), "Physical not parsed correctly.");
        assertEquals(bytes(4718592000L), jvmRun.getJvm().getMaxHeapBytes(), "Heap size not parsed correctly.");
        assertEquals(bytes(0L), jvmRun.getJvm().getMaxMetaspaceBytes(), "Metaspace size not parsed correctly.");
        assertEquals(bytes(0L), jvmRun.getJvm().getCompressedClassSpaceSizeBytes(),
                "Class compressed pointer space size not parsed correctly.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(bytes(16728526848L), jvmRun.getJvm().getPhysicalMemory(), "Physical not parsed correctly.");
        assertEquals(bytes(5368709120L), jvmRun.getJvm().getMaxHeapBytes(), "Heap size not parsed correctly.");
        assertEquals(bytes(3221225472L), jvmRun.getJvm().getMaxMetaspaceBytes(),
                "Metaspace size not parsed correctly.");
        assertEquals(bytes(2147483648L), jvmRun.getJvm().getCompressedClassSpaceSizeBytes(),
                "Class compressed pointer space size not parsed correctly.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(bytes(50465866752L), jvmRun.getJvm().getPhysicalMemory(), "Physical not parsed correctly.");
        assertEquals(bytes(45097156608L), jvmRun.getJvm().getMaxHeapBytes(), "Heap size not parsed correctly.");
        assertEquals(bytes(5368709120L), jvmRun.getJvm().getMaxMetaspaceBytes(),
                "Metaspace size not parsed correctly.");
        // Class compressed pointer space has a size, but it is ignored when calculating JVM memory.
        assertEquals(bytes(1073741824L), jvmRun.getJvm().getCompressedClassSpaceSizeBytes(),
                "Class compressed pointer space size not parsed correctly.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY),
                Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.");
    }

    @Test
    void testPrintAdaptiveResizePolicyEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintAdaptiveSizePolicy -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED),
                Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED + " analysis not identified.");
    }

    @Test
    void testPrintApplicationConcurrentTime() {
        String jvmOptions = "-Xss128k -XX:+PrintGCApplicationConcurrentTime -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME),
                Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.");
    }

    @Test
    void testPrintClassHistogramAfterFullGcEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintClassHistogramAfterFullGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC),
                Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM),
                Analysis.WARN_PRINT_CLASS_HISTOGRAM + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC),
                Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC + " analysis incorrectly identified.");
    }

    @Test
    void testPrintClassHistogramBeforeFullGcEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintClassHistogramBeforeFullGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC),
                Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM),
                Analysis.WARN_PRINT_CLASS_HISTOGRAM + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC),
                Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC + " analysis incorrectly identified.");
    }

    @Test
    void testPrintClassHistogramEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintClassHistogram -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM),
                Analysis.WARN_PRINT_CLASS_HISTOGRAM + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC),
                Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC + " analysis incorrectly identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC),
                Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC + " analysis incorrectly identified.");
    }

    /**
     * Test PrintCommandLineFlags missing.
     */
    @Test
    void testPrintCommandlineFlagsMissing() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNKNOWN);
        jvmRun.setEventTypes(eventTypes);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_COMMANDLINE_FLAGS),
                Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis not identified.");
    }

    /**
     * Test PrintCommandLineFlags missing.
     */
    @Test
    void testPrintCommandlineFlagsNoGcLogging() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_COMMANDLINE_FLAGS),
                Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis identified.");
    }

    /**
     * Test PrintCommandLineFlags not missing.
     */
    @Test
    void testPrintCommandlineFlagsNotMissing() {
        String jvmOptions = "-Xss128k -XX:+PrintCommandLineFlags -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_COMMANDLINE_FLAGS),
                Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis identified.");
    }

    @Test
    void testPrintFlsStatistics() {
        String jvmOptions = "-Xss128k -XX:PrintFLSStatistics=1 -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_FLS_STATISTICS),
                Analysis.INFO_PRINT_FLS_STATISTICS + " analysis not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_FLS_STATISTICS),
                Analysis.INFO_PRINT_FLS_STATISTICS + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_PROMOTION_FAILURE),
                Analysis.INFO_PRINT_PROMOTION_FAILURE + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_USE_MEMBAR),
                Analysis.WARN_USE_MEMBAR + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING),
                Analysis.WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING + " analysis not identified.");
    }

    /**
     * Test PrintGCDetails disabled.
     */
    @Test
    void testPrintGCDetailsDisabled() {
        String jvmOptions = "-Xss128k -XX:-PrintGCDetails -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_DISABLED),
                Analysis.WARN_PRINT_GC_DETAILS_DISABLED + " not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.VERBOSE_GC_YOUNG),
                JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + " collector not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_DISABLED),
                Analysis.WARN_PRINT_GC_DETAILS_DISABLED + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING),
                Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis incorrectly identified.");
    }

    /**
     * Test PrintGCDetails missing.
     */
    @Test
    void testPrintGCDetailsMissing() {
        String jvmOptions = "-Xss128k -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getEventTypes().add(LogEventType.PARALLEL_SCAVENGE);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING),
                Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis not identified.");
        // Not applicable to unified logging
        jvmRun.getAnalysis().clear();
        jvmRun.getEventTypes().add(LogEventType.UNIFIED_CONCURRENT);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING),
                Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis incorrectly identified.");
    }

    /**
     * Test PrintGCDetails not missing.
     */
    @Test
    void testPrintGCDetailsNotMissing() {
        String jvmOptions = "-Xss128k -XX:+PrintGCDetails -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING),
                Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PROMOTION_FAILED),
                Analysis.ERROR_CMS_PROMOTION_FAILED + " analysis incorrectly identified.");
    }

    @Test
    void testSurvivorRatio() {
        String jvmOptions = "-Xss128k -XX:SurvivorRatio=6 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_SURVIVOR_RATIO),
                Analysis.INFO_SURVIVOR_RATIO + " analysis not identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_SWAP_DISABLED),
                Analysis.INFO_SWAP_DISABLED + " analysis not identified.");
    }

    @Test
    void testTargetSurvivorRatio() {
        String jvmOptions = "-Xss128k -XX:TargetSurvivorRatio=90 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_SURVIVOR_RATIO_TARGET),
                Analysis.INFO_SURVIVOR_RATIO_TARGET + " analysis not identified.");
    }

    @Test
    void testTenuringDisabled() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=0 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_TENURING_DISABLED),
                Analysis.WARN_TENURING_DISABLED + " analysis not identified.");
    }

    @Test
    void testThreadStackSizeAnalysis32Bit() throws IOException {
        File testFile = TestUtil.getFile("dataset87.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET),
                Analysis.WARN_THREAD_STACK_SIZE_NOT_SET + " analysis not identified.");
    }

    /**
     * Test passing JVM options on the command line.
     */
    @Test
    void testThreadStackSizeLarge() {
        String options = "-o \"-Xss1024k\"";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(options, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_LARGE),
                Analysis.WARN_THREAD_STACK_SIZE_LARGE + " analysis not identified.");
    }

    @Test
    void testTieredCompilation() {
        String jvmOptions = "-Xss128k -XX:+TieredCompilation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        String version = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        jvmRun.getJvm().setVersion(version);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_TIERED_COMPILATION_ENABLED),
                Analysis.WARN_TIERED_COMPILATION_ENABLED + " analysis not identified.");
    }

    @Test
    void testTraceClassUnloading() {
        String jvmOptions = "-Xss128k -XX:+TraceClassUnloading -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_TRACE_CLASS_UNLOADING),
                Analysis.WARN_TRACE_CLASS_UNLOADING + " analysis not identified.");
    }

    @Test
    void testUseFastUnorderedTimeStamps() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseFastUnorderedTimeStamps -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_FAST_UNORDERED_TIMESTAMPS),
                Analysis.WARN_FAST_UNORDERED_TIMESTAMPS + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS),
                Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.");
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        // VERGOSE_GC_OLD looks the same as G1_FULL without -XX:+PrintGCDetails
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN),
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
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN),
                Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.");
    }
}