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
package org.eclipselabs.garbagecat.util.jdk;

import static org.eclipselabs.garbagecat.Memory.bytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.junit.Test;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestAnalysis {

    /**
     * Verify analysis file property key/value lookup.
     */
    @Test
    public void testPropertyKeyValueLookup() {
        Analysis[] analysis = Analysis.values();
        for (int i = 0; i < analysis.length; i++) {
            assertNotNull(analysis[i].getKey() + " not found.", analysis[i].getValue());
        }
    }

    @Test
    public void testBisasedLockingDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseBiasedLocking -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_BIASED_LOCKING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BIASED_LOCKING_DISABLED));
        jvmRun.getAnalysis().clear();
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.SHENANDOAH);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertFalse(Analysis.WARN_BIASED_LOCKING_DISABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BIASED_LOCKING_DISABLED));

    }

    @Test
    public void testPrintClassHistogramEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintClassHistogram -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_CLASS_HISTOGRAM + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM));
        assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC));
        assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC));
    }

    @Test
    public void testPrintClassHistogramAfterFullGcEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintClassHistogramAfterFullGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC));
        assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM));
        assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC));
    }

    @Test
    public void testPrintClassHistogramBeforeFullGcEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintClassHistogramBeforeFullGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC));
        assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM));
        assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC));
    }

    @Test
    public void testPrintApplicationConcurrentTime() {
        String jvmOptions = "-Xss128k -XX:+PrintGCApplicationConcurrentTime -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME));
    }

    @Test
    public void testTraceClassUnloading() {
        String jvmOptions = "-Xss128k -XX:+TraceClassUnloading -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_TRACE_CLASS_UNLOADING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_TRACE_CLASS_UNLOADING));
    }

    @Test
    public void testCompressedClassPointersEnabledCompressedOopsDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedClassPointers -XX:-UseCompressedOops -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK));
    }

    @Test
    public void testCompressedClassPointersEnabledHeapGt32G() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xmx32g";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G));
    }

    @Test
    public void testCompressedClassPointersDisabledHeapLt32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedClassPointers -XX:+UseCompressedOops -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G));
    }

    @Test
    public void testCompressedClassPointersDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedClassPointers -XX:+UseCompressedOops";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK));
    }

    @Test
    public void testCompressedClassSpaceSizeWithCompressedOopsDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:CompressedClassSpaceSize=1G -XX:-UseCompressedOops -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK));
        assertTrue(Analysis.INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED));
    }

    @Test
    public void testCompressedClassSpaceSizeWithCompressedClassPointersDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:CompressedClassSpaceSize=1G -XX:-UseCompressedClassPointers -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK));
        assertTrue(Analysis.INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED));
    }

    @Test
    public void testCompressedOopsDisabledHeapLess32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedOops -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G));
    }

    @Test
    public void testCompressedOopsDisabledHeapEqual32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedOops -Xmx32G";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G));
        assertFalse(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK));
    }

    @Test
    public void testCompressedOopsDisabledHeapGreater32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedOops -Xmx40G";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G));
    }

    @Test
    public void testCompressedOopsEnabledHeapGreater32G() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedOops -Xmx40G";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G));
    }

    @Test
    public void testPrintFlsStatistics() {
        String jvmOptions = "-Xss128k -XX:PrintFLSStatistics=1 -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_PRINT_FLS_STATISTICS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_FLS_STATISTICS));
    }

    @Test
    public void testTieredCompilation() {
        String jvmOptions = "-Xss128k -XX:+TieredCompilation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        String version = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        jvmRun.getJvm().setVersion(version);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_TIERED_COMPILATION_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_TIERED_COMPILATION_ENABLED));
    }

    @Test
    public void testLogFileRotationDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseGCLogFileRotation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED));
    }

    @Test
    public void testLogFileNumberWithRotationDisabled() {
        String jvmOptions = "-Xss128k -XX:NumberOfGCLogFiles=5 -XX:-UseGCLogFileRotation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED));
        assertTrue(Analysis.WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED));
    }

    /**
     * Test passing JVM options on the command line.
     */
    @Test
    public void testThreadStackSizeLarge() {
        String options = "-o \"-Xss1024k\"";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(options, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_THREAD_STACK_SIZE_LARGE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_LARGE));
    }

    /**
     * Test DGC redundant options analysis.
     */
    @Test
    public void testDgcRedundantOptions() {
        String jvmOptions = "-XX:+DisableExplicitGC -Dsun.rmi.dgc.client.gcInterval=14400000 "
                + "-Dsun.rmi.dgc.server.gcInterval=24400000";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT));
        assertTrue(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT));
    }

    /**
     * Test analysis not small DGC intervals.
     */
    @Test
    public void testDgcNotSmallIntervals() {
        String jvmOptions = "-Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL));
        assertFalse(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL));
    }

    /**
     * Test analysis small DGC intervals
     */
    @Test
    public void testDgcSmallIntervals() {
        String jvmOptions = "-Dsun.rmi.dgc.client.gcInterval=3599999 -Dsun.rmi.dgc.server.gcInterval=3599999";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL));
        assertTrue(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL));
    }

    /**
     * Test analysis if heap dump on OOME enabled.
     */
    @Test
    public void testHeapDumpOnOutOfMemoryError() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_HEAP_DUMP_ON_OOME_MISSING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_ON_OOME_MISSING));
        assertFalse(Analysis.INFO_HEAP_DUMP_PATH_MISSING + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_HEAP_DUMP_PATH_MISSING));
    }

    /**
     * Test analysis if instrumentation being used.
     */
    @Test
    public void testInstrumentation() {
        String jvmOptions = "-Xss128k -Xms2048M -javaagent:byteman.jar=script:kill-3.btm,boot:byteman.jar -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.INFO_INSTRUMENTATION + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_INSTRUMENTATION));
    }

    /**
     * Test analysis if native library being used.
     */
    @Test
    public void testNative() {
        String jvmOptions = "-Xss128k -Xms2048M -agentpath:/path/to/agent.so -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.INFO_NATIVE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_NATIVE));
    }

    /**
     * Test analysis background compilation disabled.
     */
    @Test
    public void testBackgroundCompilationDisabled() {
        String jvmOptions = "-Xss128k -XX:-BackgroundCompilation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED));
    }

    /**
     * Test analysis background compilation disabled.
     */
    @Test
    public void testBackgroundCompilationDisabledXBatch() {
        String jvmOptions = "-Xss128k -Xbatch -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED));
    }

    /**
     * Test analysis compilation on first invocation enabled.
     */
    @Test
    public void testCompilationOnFirstInvocation() {
        String jvmOptions = "-Xss128k -Xcomp-Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_BYTECODE_COMPILE_FIRST_INVOCATION + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_COMPILE_FIRST_INVOCATION));
    }

    /**
     * Test analysis just in time (JIT) compiler disabled.
     */
    @Test
    public void testCompilationDisabled() {
        String jvmOptions = "-Xss128k -Xint -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_BYTECODE_COMPILE_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_COMPILE_DISABLED));
    }

    /**
     * Test MaxMetaspaceSize is less than CompressedClassSpaceSize.
     */
    @Test
    public void testMetaspaceSizeLtCompClassSize() {
        String jvmOptions = "-XX:MetaspaceSize=512M -XX:MaxMetaspaceSize=512M -XX:CompressedClassSpaceSize=1024M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.ERROR_METASPACE_SIZE_LT_COMP_CLASS_SIZE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_METASPACE_SIZE_LT_COMP_CLASS_SIZE));
    }

    /**
     * Test analysis explicit GC not concurrent.
     */
    @Test
    public void testExplicitGcNotConcurrentG1() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.G1_FULL_GC);
        jvmRun.setEventTypes(eventTypes);
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.G1);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT));
    }

    /**
     * Test analysis explicit GC not concurrent.
     */
    @Test
    public void testExplicitGcNotConcurrentCms() {
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
        assertTrue(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT));
    }

    /**
     * Test DisableExplicitGC in combination with ExplicitGCInvokesConcurrent.
     */
    @Test
    public void testDisableExplictGcWithConcurrentHandling() {
        String jvmOptions = "-Xss128k -XX:+DisableExplicitGC -XX:+ExplicitGCInvokesConcurrent -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_EXPLICIT_GC_DISABLED_CONCURRENT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_DISABLED_CONCURRENT));
    }

    /**
     * Test HeapDumpOnOutOfMemoryError disabled.
     */
    @Test
    public void testHeapDumpOnOutOfMemoryErrorDisabled() {
        String jvmOptions = "-Xss128k -XX:-HeapDumpOnOutOfMemoryError -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_HEAP_DUMP_ON_OOME_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_ON_OOME_DISABLED));
        assertFalse(Analysis.INFO_HEAP_DUMP_PATH_MISSING + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_HEAP_DUMP_PATH_MISSING));
    }

    /**
     * Test PrintCommandLineFlags missing.
     */
    @Test
    public void testPrintCommandlineFlagsNoGcLogging() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_COMMANDLINE_FLAGS));
    }

    /**
     * Test PrintCommandLineFlags missing.
     */
    @Test
    public void testPrintCommandlineFlagsMissing() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNKNOWN);
        jvmRun.setEventTypes(eventTypes);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_COMMANDLINE_FLAGS));
    }

    /**
     * Test PrintCommandLineFlags not missing.
     */
    @Test
    public void testPrintCommandlineFlagsNotMissing() {
        String jvmOptions = "-Xss128k -XX:+PrintCommandLineFlags -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_COMMANDLINE_FLAGS));
    }

    /**
     * Test PrintGCDetails missing.
     */
    @Test
    public void testPrintGCDetailsMissing() {
        String jvmOptions = "-Xss128k -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING));
    }

    /**
     * Test PrintGCDetails not missing.
     */
    @Test
    public void testPrintGCDetailsNotMissing() {
        String jvmOptions = "-Xss128k -XX:+PrintGCDetails -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING));
    }

    /**
     * Test PrintGCDetails disabled.
     */
    @Test
    public void testPrintGCDetailsDisabled() {
        String jvmOptions = "-Xss128k -XX:-PrintGCDetails -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_PRINT_GC_DETAILS_DISABLED + " not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_DISABLED));
    }

    /**
     * Test CMS not being used to collect old generation.
     */
    @Test
    public void testCmsYoungSerialOld() {
        String jvmOptions = "-Xss128k -XX:+UseParNewGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.ERROR_CMS_SERIAL_OLD + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_SERIAL_OLD));
    }

    /**
     * Test CMS being used to collect old generation.
     */
    @Test
    public void testCmsYoungCmsOld() {
        String jvmOptions = "-Xss128k -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(Analysis.ERROR_CMS_SERIAL_OLD + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_SERIAL_OLD));
    }

    /**
     * Test CMS being used to collect old generation.
     */
    @Test
    public void testCMSClassUnloadingEnabledMissing() {
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
        assertTrue(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED));
        assertFalse(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED));
    }

    /**
     * Test CMS handling perm/metaspace collections.
     */
    @Test
    public void testCMSClassUnloadingEnabledMissingButNotCms() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED));
    }

    @Test
    public void testPrintAdaptiveResizePolicyEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintAdaptiveSizePolicy -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED));
    }

    @Test
    public void testTenuringDisabled() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=0 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_TENURING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_TENURING_DISABLED));
    }

    @Test
    public void testMaxTenuringOverrideParallel() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=6 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<CollectorFamily> collectors = new ArrayList<CollectorFamily>();
        collectors.add(JdkUtil.CollectorFamily.PARALLEL);
        jvmRun.setCollectorFamilies(collectors);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_MAX_TENURING_OVERRIDE));
    }

    @Test
    public void testMaxTenuringOverrideCms() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=14 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<CollectorFamily> collectors = new ArrayList<CollectorFamily>();
        collectors.add(JdkUtil.CollectorFamily.CMS);
        jvmRun.setCollectorFamilies(collectors);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_MAX_TENURING_OVERRIDE));
    }

    @Test
    public void testMaxTenuringOverrideG1() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=6 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<CollectorFamily> collectors = new ArrayList<CollectorFamily>();
        collectors.add(JdkUtil.CollectorFamily.G1);
        jvmRun.setCollectorFamilies(collectors);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_MAX_TENURING_OVERRIDE));
    }

    @Test
    public void testSurvivorRatio() {
        String jvmOptions = "-Xss128k -XX:SurvivorRatio=6 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_SURVIVOR_RATIO + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_SURVIVOR_RATIO));
    }

    @Test
    public void testTargetSurvivorRatio() {
        String jvmOptions = "-Xss128k -XX:TargetSurvivorRatio=90 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_SURVIVOR_RATIO_TARGET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_SURVIVOR_RATIO_TARGET));
    }

    @Test
    public void testExperimentalOptionsEnabled() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        jvm.setVersion("1.8.0_91-b14");
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    @Test
    public void testUseFastUnorderedTimeStamps() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseFastUnorderedTimeStamps -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_FAST_UNORDERED_TIMESTAMPS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_FAST_UNORDERED_TIMESTAMPS));
        assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    @Test
    public void testJdk8G1PriorUpdate40() {
        String jvmOptions = "";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_20-b32) ");
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.G1);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
        assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    @Test
    public void testJdk8G1PriorUpdate40NoLoggingEvents() {
        String jvmOptions = "-XX:+UseG1GC";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_20-b32) ");
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
        assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    @Test
    public void testJdk8NotG1PriorUpdate40() {
        String jvmOptions = "";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_20-b32) ");
        List<CollectorFamily> collectorFamilies = new ArrayList<CollectorFamily>();
        collectorFamilies.add(CollectorFamily.CMS);
        jvmRun.setCollectorFamilies(collectorFamilies);
        jvmRun.doAnalysis();
        assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
        assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    @Test
    public void testJdk8G1PriorUpdate40WithRecommendedJvmOptions() {
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
        assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
        assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    @Test
    public void testJdk8Update40() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_40-b26) ");
        jvmRun.doAnalysis();
        assertTrue(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
        assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
    }

    @Test
    public void testCmsParallelInitialMarkDisabled() {
        String jvmOptions = "-XX:-CMSParallelInitialMarkEnabled";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED));
    }

    @Test
    public void testCmsParallelRemarkDisabled() {
        String jvmOptions = "-XX:-CMSParallelRemarkEnabled";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.ERROR_CMS_PARALLEL_REMARK_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PARALLEL_REMARK_DISABLED));
    }

    @Test
    public void testG1SummarizeRSetStatsPeriod0() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+G1SummarizeRSetStats "
                + "-XX:G1SummarizeRSetStatsPeriod=0";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT));
    }

    @Test
    public void testApplicationStoppedTimeMissingNoData() {
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(null, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNKNOWN);
        jvmRun.setEventTypes(eventTypes);
        jvmRun.getAnalysis().clear();
        jvmRun.doAnalysis();
        assertFalse(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING));
    }

    @Test
    public void testCGroupMemoryLimit() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseCGroupMemoryLimitForHeap";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_CGROUP_MEMORY_LIMIT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CGROUP_MEMORY_LIMIT));
        assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    @Test
    public void testAdaptiveSizePolicy() {
        String jvmOptions = "-XX:InitialHeapSize=2147483648 -XX:MaxHeapSize=8589934592 -XX:-UseAdaptiveSizePolicy";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(Analysis.WARN_HEAP_MIN_NOT_EQUAL_MAX + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_MIN_NOT_EQUAL_MAX));
        assertTrue(Analysis.ERROR_ADAPTIVE_SIZE_POLICY_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_ADAPTIVE_SIZE_POLICY_DISABLED));
        assertFalse(Analysis.INFO_UNACCOUNTED_OPTIONS_DISABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_UNACCOUNTED_OPTIONS_DISABLED));
    }

    @Test
    public void testHeaderLogging() {
        File testFile = TestUtil.getFile("dataset42.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " information not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS));
        assertTrue(JdkUtil.LogEventType.HEADER_MEMORY.toString() + " information not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY));
        assertTrue(JdkUtil.LogEventType.HEADER_VERSION.toString() + " information not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION));
        // Usually no reason to set the thread stack size on 64 bit.
        assertFalse(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET));
    }

    /**
     * Test analysis perm gen or metaspace size not set.
     * 
     */
    @Test
    public void testAnalysisPermSizeNotSet() {
        File testFile = TestUtil.getFile("dataset60.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_PERM_SIZE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PERM_SIZE_NOT_SET));
        assertFalse(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT));
    }

    /**
     * Test CMS_SERIAL_OLD caused by <code>Analysis.KEY_EXPLICIT_GC_SERIAL</code> does not return
     * <code>Analysis.KEY_SERIAL_GC_CMS</code>.
     */
    @Test
    public void testCmsSerialOldExplicitGc() {
        File testFile = TestUtil.getFile("dataset85.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        assertTrue(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS));
        assertFalse(Analysis.ERROR_SERIAL_GC_CMS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    /**
     * Test PARALLEL_COMPACTING_OLD caused by <code>Analysis.KEY_EXPLICIT_GC_SERIAL</code>.
     */
    @Test
    public void testParallelOldCompactingExplicitGc() {
        File testFile = TestUtil.getFile("dataset86.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SCAVENGE));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_COMPACTING_OLD));
        assertTrue(Analysis.WARN_EXPLICIT_GC_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_PARALLEL));
        assertFalse(Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL));
        assertEquals("Inverted parallelism event count not correct.", 0, jvmRun.getInvertedParallelismCount());
    }

    @Test
    public void testThreadStackSizeAnalysis32Bit() {
        File testFile = TestUtil.getFile("dataset87.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET));
    }

    @Test
    public void testHeapDumpPathFilename() {
        File testFile = TestUtil.getFile("dataset95.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_HEAP_DUMP_PATH_FILENAME + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_PATH_FILENAME));
    }

    /**
     * Test PAR_NEW disabled with -XX:-UseParNewGC.
     */
    @Test
    public void testParNewDisabled() {
        File testFile = TestUtil.getFile("dataset101.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 4, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.SERIAL_NEW));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_INITIAL_MARK));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_REMARK));
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        assertTrue(Analysis.WARN_CMS_PAR_NEW_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_PAR_NEW_DISABLED));
        assertFalse(Analysis.ERROR_SERIAL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC));
        assertFalse(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED));
    }

    /**
     * Test compressed oops disabled with heap >= 32G.
     */
    @Test
    public void testCompressedOopsDisabledLargeHeap() {
        File testFile = TestUtil.getFile("dataset106.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Max heap value not parsed correctly.", "45097156608", jvmRun.getJvm().getMaxHeapValue());
        assertFalse(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G));
        assertFalse(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G));
        assertTrue(Analysis.ERROR_COMP_CLASS_SIZE_HEAP_GT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_CLASS_SIZE_HEAP_GT_32G));
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    @Test
    public void testPhysicalMemoryLessThanJvmMemoryWithoutCompressedClassPointerSpace() {
        File testFile = TestUtil.getFile("dataset106.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Physical not parsed correctly.", bytes(50465866752L), jvmRun.getJvm().getPhysicalMemory());
        assertEquals("Heap size not parsed correctly.", bytes(45097156608L), jvmRun.getJvm().getMaxHeapBytes());
        assertEquals("Metaspace size not parsed correctly.", bytes(5368709120L),
                jvmRun.getJvm().getMaxMetaspaceBytes());
        // Class compressed pointer space has a size, but it is ignored when calculating JVM memory.
        assertEquals("Class compressed pointer space size not parsed correctly.", bytes(1073741824L),
                jvmRun.getJvm().getCompressedClassSpaceSizeBytes());
        assertFalse(Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

    /**
     * Test PrintGCDetails disabled with VERBOSE_GC logging.
     */
    @Test
    public void testPrintGcDetailsDisabledWithVerboseGc() {
        File testFile = TestUtil.getFile("dataset107.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.VERBOSE_GC_YOUNG));
        assertTrue(Analysis.WARN_PRINT_GC_DETAILS_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_DISABLED));
        assertFalse(Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING));
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    @Test
    public void testPhysicalMemoryLessThanJvmMemoryWithCompressedClassPointerSpace() {
        File testFile = TestUtil.getFile("dataset107.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
		assertEquals("Physical not parsed correctly.", bytes(16728526848L), jvmRun.getJvm().getPhysicalMemory());
		assertEquals("Heap size not parsed correctly.", bytes(5368709120L), jvmRun.getJvm().getMaxHeapBytes());
		assertEquals("Metaspace size not parsed correctly.", bytes(3221225472L),
				jvmRun.getJvm().getMaxMetaspaceBytes());
		assertEquals("Class compressed pointer space size not parsed correctly.", bytes(2147483648L),
				jvmRun.getJvm().getCompressedClassSpaceSizeBytes());
        assertFalse(Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    @Test
    public void testPhysicalMemoryLessThanHeapAllocation() {
        File testFile = TestUtil.getFile("dataset109.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Physical not parsed correctly.", bytes(1968287744L), jvmRun.getJvm().getPhysicalMemory());
        assertEquals("Heap size not parsed correctly.", bytes(4718592000L), jvmRun.getJvm().getMaxHeapBytes());
        assertEquals("Metaspace size not parsed correctly.", bytes(0L), jvmRun.getJvm().getMaxMetaspaceBytes());
		assertEquals("Class compressed pointer space size not parsed correctly.", bytes(0L),
				jvmRun.getJvm().getCompressedClassSpaceSizeBytes());
        assertTrue(Analysis.ERROR_PHYSICAL_MEMORY + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

	/**
     * Test CMS class unloading disabled.
     */
    @Test
    public void testCmsClassunloadingDisabled() {
        File testFile = TestUtil.getFile("dataset110.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED));
        assertFalse(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED));
        assertTrue(Analysis.WARN_CLASS_UNLOADING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CLASS_UNLOADING_DISABLED));
        assertTrue(Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA));
    }

    /**
     * Test application/gc logging mixed.
     */
    @Test
    public void testApplicationLogging() {
        File testFile = TestUtil.getFile("dataset114.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_APPLICATION_LOGGING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_APPLICATION_LOGGING));
        assertTrue("64-bit not identified.", jvmRun.getJvm().is64Bit());
        assertFalse(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET));

    }

    /**
     * Test <code>-XX:PrintFLSStatistics</code> and <code>-XX:PrintPromotionFailure</code>.
     */
    @Test
    public void testPrintFlsStatisticsPrintPromotionFailure() {
        File testFile = TestUtil.getFile("dataset115.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.INFO_PRINT_FLS_STATISTICS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_FLS_STATISTICS));
        assertTrue(Analysis.INFO_PRINT_PROMOTION_FAILURE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_PROMOTION_FAILURE));
        assertTrue(Analysis.WARN_USE_MEMBAR + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_USE_MEMBAR));
        assertTrue(Analysis.WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING));
    }

    /**
     * Test humongous allocations on old JDK not able to reclaim humongous objects during young collections.
     */
    @Test
    public void testHumongousAllocationsNotCollectedYoung() {
        File testFile = TestUtil.getFile("dataset118.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.ERROR_G1_HUMONGOUS_JDK_OLD + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_G1_HUMONGOUS_JDK_OLD));
        assertTrue(Analysis.WARN_G1_MIXED_GC_LIVE_THRSHOLD_PRCNT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_MIXED_GC_LIVE_THRSHOLD_PRCNT));
        assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
        assertFalse(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED));
    }

    /**
     * Test CMS_SERIAL_OLD triggered by GCLocker promotion failure.
     */
    @Test
    public void testCmsSerialOldGcLocker() {
        File testFile = TestUtil.getFile("dataset119.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED));
        assertFalse(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED));
        assertTrue(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED));
    }

    /**
     * Test VERBOSE_GC_OLD triggered by explicit GC.
     */
    @Test
    public void testVerboseGcOldExplicitGc() {
        File testFile = TestUtil.getFile("dataset125.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        // VERGOSE_GC_OLD looks the same as G1_FULL without -XX:+PrintGCDetails
        assertTrue(Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN));
    }

    /**
     * Test VERBOSE_GC_YOUNG triggered by explicit GC.
     */
    @Test
    public void testVerboseGcYoungExplicitGc() {
        File testFile = TestUtil.getFile("dataset126.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN));
    }

    /**
     * Test serial promotion failed is not reported as cms promotion failed.
     */
    @Test
    public void testSerialPromotionFailed() {
        File testFile = TestUtil.getFile("dataset129.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(Analysis.ERROR_CMS_PROMOTION_FAILED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PROMOTION_FAILED));
    }

    /**
     * Test CMS initial mark low parallelism.
     */
    @Test
    public void testCmsInitialMarkSerial() {
        File testFile = TestUtil.getFile("dataset130.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM));
    }

    /**
     * Test CMS remark low parallelism.
     */
    @Test
    public void testCmsRemarkSerial() {
        File testFile = TestUtil.getFile("dataset131.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM));
    }

    /**
     * Test CMS remark low parallelism not reported with pause times less than zero.
     */
    @Test
    public void testInitialMarkLowParallelismFalseReportZeroReal() {
        File testFile = TestUtil.getFile("dataset137.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM));
    }

    /**
     * Test CMS remark low parallelism not reported with pause times less than times data centisecond precision.
     */
    @Test
    public void testInitialMarkLowParallelismFalseReportSmallPause() {
        File testFile = TestUtil.getFile("dataset138.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM));
        assertFalse(Analysis.INFO_SWAP_DISABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_SWAP_DISABLED));
    }

    /**
     * Test small gc log file size.
     */
    @Test
    public void testGcLogFileSizeSmall() {
        File testFile = TestUtil.getFile("dataset181.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_GC_LOG_FILE_SIZE_SMALL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_GC_LOG_FILE_SIZE_SMALL));
    }

    /**
     * Test heap dump location missing
     */
    @Test
    public void testHeapDumpPathMissing() {
        File testFile = TestUtil.getFile("dataset181.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.INFO_HEAP_DUMP_PATH_MISSING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_HEAP_DUMP_PATH_MISSING));
    }

    /**
     * Test swap disabled.
     */
    @Test
    public void testSwapDisabled() {
        File testFile = TestUtil.getFile("dataset187.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.INFO_SWAP_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_SWAP_DISABLED));
    }

    /**
     * Test diagnostic options
     */
    @Test
    public void testDiagnosticOptions() {
        File testFile = TestUtil.getFile("dataset192.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.INFO_JMX_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_JMX_ENABLED));
        assertTrue(Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED));
    }

    /**
     * Test -XX:+UseFastUnorderedTimeStamps
     */
    @Test
    public void testFastUnorderedTimestamps() {
        File testFile = TestUtil.getFile("dataset193.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(Analysis.WARN_FAST_UNORDERED_TIMESTAMPS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_FAST_UNORDERED_TIMESTAMPS));
    }

    /**
     * Test Metadata GC Threshold
     */
    @Test
    public void testMetadataGcThreshold() {
        File testFile = TestUtil.getFile("dataset199.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
    }
}