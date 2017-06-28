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
package org.eclipselabs.garbagecat.util.jdk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestAnalysis extends TestCase {

    /**
     * Verify analysis file property key/value lookup.
     */
    public void testPropertyKeyValueLookup() {
        Analysis[] analysis = Analysis.values();
        for (int i = 0; i < analysis.length; i++) {
            Assert.assertNotNull(analysis[i].getKey() + " not found.", analysis[i].getValue());
        }
    }

    public void testBisasedLockingDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseBiasedLocking -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_BIASED_LOCKING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BIASED_LOCKING_DISABLED));
    }

    public void testPrintClassHistogramEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintClassHistogram -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_CLASS_HISTOGRAM + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM));
        Assert.assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC));
        Assert.assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC));
    }

    public void testPrintClassHistogramAfterFullGcEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintClassHistogramAfterFullGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC));
        Assert.assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM));
        Assert.assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC));
    }

    public void testPrintClassHistogramBeforeFullGcEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintClassHistogramBeforeFullGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC));
        Assert.assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM));
        Assert.assertFalse(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC));
    }

    public void testPrintApplicationConcurrentTime() {
        String jvmOptions = "-Xss128k -XX:+PrintGCApplicationConcurrentTime -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME));
    }

    public void testTraceClassUnloading() {
        String jvmOptions = "-Xss128k -XX:+TraceClassUnloading -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_TRACE_CLASS_UNLOADING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_TRACE_CLASS_UNLOADING));
    }

    public void testCompressedClassSpaceSize() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET));
    }

    public void testCompressedClassPointersEnabledCompressedOopsDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedClassPointers -XX:-UseCompressedOops -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET));
        Assert.assertTrue(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK));
    }

    public void testCompressedClassPointersEnabledHeapGt32G() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xmx32g";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET));
        Assert.assertTrue(Analysis.ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G));
    }

    public void testCompressedClassPointersDisabledHeapLt32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedClassPointers -XX:+UseCompressedOops -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G));
    }

    public void testCompressedClassPointersDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedClassPointers -XX:+UseCompressedOops";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK));
    }

    public void testCompressedClassSpaceSizeWithCompressedOopsDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:CompressedClassSpaceSize=1G -XX:-UseCompressedOops -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET));
        Assert.assertTrue(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK));
        Assert.assertTrue(Analysis.INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED));
    }

    public void testCompressedClassSpaceSizeWithCompressedClassPointersDisabledHeapUnknown() {
        String jvmOptions = "-Xss128k -XX:CompressedClassSpaceSize=1G -XX:-UseCompressedClassPointers -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET));
        Assert.assertTrue(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK));
        Assert.assertTrue(Analysis.INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED));
    }

    public void testCompressedOopsDisabledHeapLess32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedOops -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G));
    }

    public void testCompressedOopsDisabledHeapEqual32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedOops -Xmx32G";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G));
        Assert.assertFalse(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK));
    }

    public void testCompressedOopsDisabledHeapGreater32G() {
        String jvmOptions = "-Xss128k -XX:-UseCompressedOops -Xmx40G";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G));
    }

    public void testCompressedOopsEnabledHeapGreater32G() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedOops -Xmx40G";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G));
    }

    public void testPrintFlsStatistics() {
        String jvmOptions = "-Xss128k -XX:PrintFLSStatistics=1 -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_PRINT_FLS_STATISTICS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_FLS_STATISTICS));
    }

    public void testPermMetatspaceNotSet() {
        String jvmOptions = "-Xss128k -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PERM_METASPACE_SIZE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PERM_METASPACE_SIZE_NOT_SET));
    }

    public void testTieredCompilation() {
        String jvmOptions = "-Xss128k -XX:+TieredCompilation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        String version = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        jvmRun.getJvm().setVersion(version);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_TIERED_COMPILATION_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_TIERED_COMPILATION_ENABLED));
    }

    public void testLogFileRotationDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseGCLogFileRotation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED));
    }

    public void testLogFileNumberWithRotationDisabled() {
        String jvmOptions = "-Xss128k -XX:NumberOfGCLogFiles=5 -XX:-UseGCLogFileRotation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED));
        Assert.assertTrue(Analysis.WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED));
    }

    /**
     * Test passing JVM options on the command line.
     */
    public void testThreadStackSizeLarge() {
        String options = "-o \"-Xss1024k\"";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(options, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_THREAD_STACK_SIZE_LARGE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_LARGE));
    }

    /**
     * Test DGC not managed analysis.
     */
    public void testDgcNotManaged() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_RMI_DGC_NOT_MANAGED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_NOT_MANAGED));
    }

    /**
     * Test DGC redundant options analysis.
     */
    public void testDgcRedundantOptions() {
        String jvmOptions = "-XX:+DisableExplicitGC -Dsun.rmi.dgc.client.gcInterval=14400000 "
                + "-Dsun.rmi.dgc.server.gcInterval=24400000";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT));
        Assert.assertTrue(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT));
    }

    /**
     * Test analysis not small DGC intervals.
     */
    public void testDgcNotSmallIntervals() {
        String jvmOptions = "-Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL));
        Assert.assertFalse(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL));
    }

    /**
     * Test analysis small DGC intervals
     */
    public void testDgcSmallIntervals() {
        String jvmOptions = "-Dsun.rmi.dgc.client.gcInterval=3599999 -Dsun.rmi.dgc.server.gcInterval=3599999";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL));
        Assert.assertTrue(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL));
    }

    /**
     * Test analysis if heap dump on OOME enabled.
     */
    public void testHeapDumpOnOutOfMemoryError() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_HEAP_DUMP_ON_OOME_MISSING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_ON_OOME_MISSING));
    }

    /**
     * Test analysis if instrumentation being used.
     */
    public void testInstrumentation() {
        String jvmOptions = "Xss128k -Xms2048M -javaagent:byteman.jar=script:kill-3.btm,boot:byteman.jar -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.INFO_INSTRUMENTATION + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_INSTRUMENTATION));
    }

    /**
     * Test analysis if native library being used.
     */
    public void testNative() {
        String jvmOptions = "Xss128k -Xms2048M -agentpath:/path/to/agent.so -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.INFO_NATIVE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_NATIVE));
    }

    /**
     * Test analysis background compilation disabled.
     */
    public void testBackgroundCompilationDisabled() {
        String jvmOptions = "Xss128k -XX:-BackgroundCompilation -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED));
    }

    /**
     * Test analysis background compilation disabled.
     */
    public void testBackgroundCompilationDisabledXBatch() {
        String jvmOptions = "Xss128k -Xbatch -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED));
    }

    /**
     * Test analysis compilation on first invocation enabled.
     */
    public void testCompilationOnFirstInvocation() {
        String jvmOptions = "Xss128k -Xcomp-Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_BYTECODE_COMPILE_FIRST_INVOCATION + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_COMPILE_FIRST_INVOCATION));
    }

    /**
     * Test analysis just in time (JIT) compiler disabled.
     */
    public void testCompilationDisabled() {
        String jvmOptions = "Xss128k -Xint -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_BYTECODE_COMPILE_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_BYTECODE_COMPILE_DISABLED));
    }

    /**
     * Test analysis explicit GC not concurrent.
     */
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
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT));
    }

    /**
     * Test analysis explicit GC not concurrent.
     */
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
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT));
    }

    /**
     * Test DisableExplicitGC in combination with ExplicitGCInvokesConcurrent.
     */
    public void testDisableExplictGcWithConcurrentHandling() {
        String jvmOptions = "Xss128k -XX:+DisableExplicitGC -XX:+ExplicitGCInvokesConcurrent -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_DISABLED_CONCURRENT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_DISABLED_CONCURRENT));
    }

    /**
     * Test HeapDumpOnOutOfMemoryError disabled.
     */
    public void testHeapDumpOnOutOfMemoryErrorDisabled() {
        String jvmOptions = "Xss128k -XX:-HeapDumpOnOutOfMemoryError -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_HEAP_DUMP_ON_OOME_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_ON_OOME_DISABLED));
    }

    /**
     * Test PrintCommandLineFlags missing.
     */
    public void testPrintCommandlineFlagsMissing() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_COMMANDLINE_FLAGS));
    }

    /**
     * Test PrintCommandLineFlags not missing.
     */
    public void testPrintCommandlineFlagsNotMissing() {
        String jvmOptions = "Xss128k -XX:+PrintCommandLineFlags -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.WARN_PRINT_COMMANDLINE_FLAGS + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_COMMANDLINE_FLAGS));
    }

    /**
     * Test PrintGCDetails missing.
     */
    public void testPrintGCDetailsMissing() {
        String jvmOptions = "Xss128k -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING));
    }

    /**
     * Test PrintGCDetails not missing.
     */
    public void testPrintGCDetailsNotMissing() {
        String jvmOptions = "Xss128k -XX:+PrintGCDetails -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING));
    }

    /**
     * Test CMS not being used to collect old generation.
     */
    public void testCmsYoungSerialOld() {
        String jvmOptions = "Xss128k -XX:+UseParNewGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.ERROR_CMS_SERIAL_OLD + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_SERIAL_OLD));
    }

    /**
     * Test CMS being used to collect old generation.
     */
    public void testCmsYoungCmsOld() {
        String jvmOptions = "Xss128k -XX:+UseParNewGC -XX:+UseConcMarkSweepGC -Xms2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.ERROR_CMS_SERIAL_OLD + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_SERIAL_OLD));
    }

    /**
     * Test CMS being used to collect old generation.
     */
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
        Assert.assertTrue(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED));
        Assert.assertFalse(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED));
    }

    /**
     * Test CMS handling perm/metaspace collections.
     */
    public void testCMSClassUnloadingEnabledMissingButNotCms() {
        String jvmOptions = "MGM";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertFalse(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED));
    }

    public void testPrintAdaptiveResizePolicyEnabled() {
        String jvmOptions = "-Xss128k -XX:+PrintAdaptiveSizePolicy -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED));
    }

    public void testTenuringDisabledZero() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=0 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_TENURING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_TENURING_DISABLED));
    }

    public void testTenuringDisabledGreater15() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=32 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_TENURING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_TENURING_DISABLED));
    }

    public void testMaxTenuringParallel() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=6 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<CollectorFamily> collectors = new ArrayList<CollectorFamily>();
        collectors.add(JdkUtil.CollectorFamily.PARALLEL);
        jvmRun.setCollectorFamilies(collectors);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_MAX_TENURING_OVERRIDE));
    }

    public void testMaxTenuringCms() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=14 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<CollectorFamily> collectors = new ArrayList<CollectorFamily>();
        collectors.add(JdkUtil.CollectorFamily.CMS);
        jvmRun.setCollectorFamilies(collectors);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_MAX_TENURING_OVERRIDE));
    }

    public void testMaxTenuringG1() {
        String jvmOptions = "-Xss128k -XX:MaxTenuringThreshold=6 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        List<CollectorFamily> collectors = new ArrayList<CollectorFamily>();
        collectors.add(JdkUtil.CollectorFamily.G1);
        jvmRun.setCollectorFamilies(collectors);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_MAX_TENURING_OVERRIDE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_MAX_TENURING_OVERRIDE));
    }

    public void testSurvivorRatio() {
        String jvmOptions = "-Xss128k -XX:SurvivorRatio=6 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_SURVIVOR_RATIO + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_SURVIVOR_RATIO));
    }

    public void testTargetSurvivorRatio() {
        String jvmOptions = "-Xss128k -XX:TargetSurvivorRatio=90 -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_SURVIVOR_RATIO_TARGET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_SURVIVOR_RATIO_TARGET));
    }

    public void testExperimentalOptionsEnabled() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        jvm.setVersion("1.8.0_91-b14");
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    public void testUseFastUnorderedTimeStamps() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseFastUnorderedTimeStamps -Xmx2048M";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_FAST_UNORDERED_TIMESTAMPS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_FAST_UNORDERED_TIMESTAMPS));
        Assert.assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

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
        Assert.assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        Assert.assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
        Assert.assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    public void testJdk8G1PriorUpdate40NoLoggingEvents() {
        String jvmOptions = "-XX:+UseG1GC";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_20-b32) ");
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        Assert.assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
        Assert.assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

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
        Assert.assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        Assert.assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
        Assert.assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

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
        Assert.assertTrue(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        Assert.assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
        Assert.assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
    }

    public void testJdk8Update40() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.getJvm().setVersion(" JRE (1.8.0_40-b26) ");
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
        Assert.assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40 + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40));
        Assert.assertFalse(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_G1_JDK8_PRIOR_U40_RECS));
    }

    public void testCmsParallelInitialMarkDisabled() {
        String jvmOptions = "-XX:-CMSParallelInitialMarkEnabled";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED));
    }

    public void testCmsParallelRemarkDisabled() {
        String jvmOptions = "-XX:-CMSParallelRemarkEnabled";
        GcManager gcManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = gcManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.ERROR_CMS_PARALLEL_REMARK_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PARALLEL_REMARK_DISABLED));
    }

    public void testHeaderLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset42.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " information not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_MEMORY.toString() + " information not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_VERSION.toString() + " information not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION));
        // Usually no reason to set the thread stack size on 64 bit.
        Assert.assertFalse(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET));
    }

    /**
     * Test analysis perm gen or metaspace size not set.
     * 
     */
    public void testAnalysisPermMetaspaceNotSet() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset60.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_PERM_SIZE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PERM_SIZE_NOT_SET));
        Assert.assertFalse(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT));
    }

    /**
     * Test CMS_SERIAL_OLD caused by <code>Analysis.KEY_EXPLICIT_GC_SERIAL</code> does not return
     * <code>Analysis.KEY_SERIAL_GC_CMS</code>.
     */
    public void testCmsSerialOldExplicitGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset85.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        Assert.assertTrue(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS));
        Assert.assertFalse(Analysis.ERROR_SERIAL_GC_CMS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    /**
     * Test PARALLEL_OLD_COMPACTING caused by <code>Analysis.KEY_EXPLICIT_GC_SERIAL</code>.
     */
    public void testParallelOldCompactingExplicitGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset86.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SCAVENGE));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING));
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_PARALLEL));
        Assert.assertFalse(Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL));
        Assert.assertEquals("Inverted parallelism event count not correct.", 0, jvmRun.getInvertedParallelismCount());
    }

    public void testThreadStackSizeAnalysis32Bit() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset87.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET));
    }

    public void testMetaspaceSizeNotSet() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset95.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_METASPACE_SIZE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_METASPACE_SIZE_NOT_SET));
    }

    /**
     * Test PAR_NEW disabled with -XX:-UseParNewGC.
     */
    public void testParNewDisabled() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset101.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 4, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.SERIAL_NEW));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_INITIAL_MARK));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_REMARK));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.WARN_CMS_PAR_NEW_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_PAR_NEW_DISABLED));
        Assert.assertFalse(Analysis.ERROR_SERIAL_GC + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC));
        Assert.assertFalse(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED));
    }

    /**
     * Test compressed oops disabled with heap >= 32G.
     */
    public void testCompressedOopsDisabledLargeHeap() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset106.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Max heap value not parsed correctly.", "45097156608", jvmRun.getJvm().getMaxHeapValue());
        Assert.assertFalse(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G));
        Assert.assertFalse(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G));
        Assert.assertTrue(Analysis.ERROR_COMP_CLASS_SIZE_HEAP_GT_32G + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_COMP_CLASS_SIZE_HEAP_GT_32G));
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    public void testPhysicalMemoryLessThanJvmMemoryWithoutCompressedClassPointerSpace() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset106.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Physical not parsed correctly.", 50465866752L, jvmRun.getJvm().getPhysicalMemory());
        Assert.assertEquals("Heap size not parsed correctly.", 45097156608L, jvmRun.getJvm().getMaxHeapBytes());
        Assert.assertEquals("Metaspace size not parsed correctly.", 5368709120L,
                jvmRun.getJvm().getMaxMetaspaceBytes());
        // Class compressed pointer space has a size, but it is ignored when calculating JVM memory.
        Assert.assertEquals("Class compressed pointer space size not parsed correctly.", 1073741824L,
                jvmRun.getJvm().getCompressedClassSpaceSizeBytes());
        Assert.assertFalse(Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

    /**
     * Test PrintGCDetails disabled with VERBOSE_GC logging.
     */
    public void testPrintGcDetailsDisabledWithVerboseGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset107.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.VERBOSE_GC_YOUNG));
        Assert.assertTrue(Analysis.WARN_PRINT_GC_DETAILS_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_DISABLED));
        Assert.assertFalse(Analysis.WARN_PRINT_GC_DETAILS_MISSING + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING));
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    public void testPhysicalMemoryLessThanJvmMemoryWithCompressedClassPointerSpace() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset107.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Physical not parsed correctly.", 16728526848L, jvmRun.getJvm().getPhysicalMemory());
        Assert.assertEquals("Heap size not parsed correctly.", 5368709120L, jvmRun.getJvm().getMaxHeapBytes());
        Assert.assertEquals("Metaspace size not parsed correctly.", 3221225472L,
                jvmRun.getJvm().getMaxMetaspaceBytes());
        Assert.assertEquals("Class compressed pointer space size not parsed correctly.", 2147483648L,
                jvmRun.getJvm().getCompressedClassSpaceSizeBytes());
        Assert.assertFalse(Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    public void testPhysicalMemoryLessThanHeapAllocation() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset109.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Physical not parsed correctly.", 1968287744L, jvmRun.getJvm().getPhysicalMemory());
        Assert.assertEquals("Heap size not parsed correctly.", 4718592000L, jvmRun.getJvm().getMaxHeapBytes());
        Assert.assertEquals("Metaspace size not parsed correctly.", 0L, jvmRun.getJvm().getMaxMetaspaceBytes());
        Assert.assertEquals("Class compressed pointer space size not parsed correctly.", 0L,
                jvmRun.getJvm().getCompressedClassSpaceSizeBytes());
        Assert.assertTrue(Analysis.ERROR_PHYSICAL_MEMORY + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_PHYSICAL_MEMORY));
    }

    /**
     * Test CMS class unloading disabled.
     */
    public void testCmsClassunloadingDisabled() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset110.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED));
        Assert.assertFalse(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED));
        Assert.assertTrue(Analysis.WARN_CLASS_UNLOADING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CLASS_UNLOADING_DISABLED));
        Assert.assertTrue(Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA));
    }

    /**
     * Test application/gc logging mixed.
     */
    public void testApplicationLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset114.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_APPLICATION_LOGGING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_APPLICATION_LOGGING));
        Assert.assertTrue("64-bit not identified.", jvmRun.getJvm().is64Bit());
        Assert.assertFalse(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET));

    }

    /**
     * Test <code>-XX:PrintFLSStatistics</code> and <code>-XX:PrintPromotionFailure</code>.
     */
    public void testPrintFlsStatisticsPrintPromotionFailure() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset115.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.INFO_PRINT_FLS_STATISTICS + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_FLS_STATISTICS));
        Assert.assertTrue(Analysis.INFO_PRINT_PROMOTION_FAILURE + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_PRINT_PROMOTION_FAILURE));
        Assert.assertTrue(Analysis.WARN_USE_MEMBAR + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_USE_MEMBAR));
        Assert.assertTrue(Analysis.WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING));
    }

    /**
     * Test humongous allocations on old JDK not able to reclaim humongous objects during young collections.
     */
    public void testHumongousAllocationsNotCollectedYoung() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset118.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.ERROR_G1_HUMONGOUS_JDK_OLD + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_G1_HUMONGOUS_JDK_OLD));
        Assert.assertTrue(Analysis.WARN_GA_MIXED_GC_LIVE_THRSHOLD_PRCNT + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_GA_MIXED_GC_LIVE_THRSHOLD_PRCNT));
        Assert.assertFalse(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_EXPERIMENTAL_VM_OPTIONS));
        Assert.assertFalse(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED));
    }

    /**
     * Test CMS_SERIAL_OLD triggered by GCLocker promotion failure.
     */
    public void testCmsSerialOldGcLocker() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset119.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED));
        Assert.assertFalse(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED));
        Assert.assertTrue(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED));
    }

    /**
     * Test VERBOSE_GC_OLD triggered by explicit GC.
     */
    public void testVerboseGcOldExplicitGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset125.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN));
    }

    /**
     * Test VERBOSE_GC_YOUNG triggered by explicit GC.
     */
    public void testVerboseGcYoungExplicitGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset126.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_EXPLICIT_GC_UNKNOWN + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN));
    }

    /**
     * Test serial promotion failed is not reported as cms promotion failed.
     */
    public void testSerialPromotionFailed() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset129.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(Analysis.ERROR_CMS_PROMOTION_FAILED + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.ERROR_CMS_PROMOTION_FAILED));
    }

    /**
     * Test CMS initial mark low parallelism.
     */
    public void testCmsInitialMarkSerial() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset130.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM));
    }

    /**
     * Test CMS remark low parallelism.
     */
    public void testCmsRemarkSerial() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset131.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM + " analysis not identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM));
    }

    /**
     * Test CMS remark low parallelism not reported with pause times less than zero.
     */
    public void testInitialMarkLowParallelismFalseReportZeroReal() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset137.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM));
    }

    /**
     * Test CMS remark low parallelism not reported with pause times less than times data centosecond precision.
     */
    public void testInitialMarkLowParallelismFalseReportSmallPause() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset138.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM + " analysis incorrectly identified.",
                jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM));
    }
}