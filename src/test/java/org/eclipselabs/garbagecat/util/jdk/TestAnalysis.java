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

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestAnalysis extends TestCase {

    /**
     * Test if biased locking disabled with -XX:-UseBiasedLocking.
     */
    public void testBisasedLockingDisabled() {
        String jvmOptions = "Xss128k -XX:-UseBiasedLocking -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.KEY_BIASED_LOCKING_DISABLED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_BIASED_LOCKING_DISABLED));
    }

    /**
     * Test if biased locking disabled with -XX:-UseBiasedLocking.
     */
    public void testPrintClassHistogramEnabled() {
        String jvmOptions = "Xss128k -XX:+PrintClassHistogram -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.KEY_PRINT_CLASS_HISTOGRAM + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_PRINT_CLASS_HISTOGRAM));
    }

    /**
     * Test if biased locking disabled with -XX:-UseBiasedLocking.
     */
    public void testPrintApplicationConcurrentTime() {
        String jvmOptions = "Xss128k -XX:+PrintGCApplicationConcurrentTime -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.KEY_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_PRINT_GC_APPLICATION_CONCURRENT_TIME));
    }

    /**
     * Test for -XX:+TraceClassUnloading.
     */
    public void testTraceClassUnloading() {
        String jvmOptions = "Xss128k -XX:+TraceClassUnloading -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.KEY_TRACE_CLASS_UNLOADING + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_TRACE_CLASS_UNLOADING));
    }

    /**
     * Test for -XX:CompressedClassSpaceSize.
     */
    public void testCompressedClassSpaceSize() {
        String jvmOptions = "Xss128k -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.KEY_COMPRESSED_CLASS_SPACE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_COMPRESSED_CLASS_SPACE_NOT_SET));
    }

    /**
     * Test for -XX:CompressedClassSpaceSize.
     */
    public void testPrintFlsStatistics() {
        String jvmOptions = "Xss128k -XX:PrintFLSStatistics=1 -Xms2048M";
        GcManager jvmManager = new GcManager();
        Jvm jvm = new Jvm(jvmOptions, null);
        JvmRun jvmRun = jvmManager.getJvmRun(jvm, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        Assert.assertTrue(Analysis.KEY_PRINT_FLS_STATISTICS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_PRINT_FLS_STATISTICS));
    }

    public void testHeaderLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset42.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString() + " information not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_MEMORY.toString() + " information not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_MEMORY));
        Assert.assertTrue(JdkUtil.LogEventType.HEADER_VERSION.toString() + " information not identified.",
                jvmRun.getEventTypes().contains(LogEventType.HEADER_VERSION));
        // Usually no reason to set the thread stack size on 64 bit.
        Assert.assertFalse(Analysis.KEY_THREAD_STACK_SIZE_NOT_SET + " analysis incorrectly identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_THREAD_STACK_SIZE_NOT_SET));
    }

    /**
     * Test CMS_SERIAL_OLD caused by <code>Analysis.KEY_EXPLICIT_GC_SERIAL</code> does not return
     * <code>Analysis.KEY_SERIAL_GC_CMS</code>.
     */
    public void testCmsSerialOldExplicitGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset85.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        Assert.assertTrue(Analysis.KEY_EXPLICIT_GC_SERIAL_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_SERIAL_CMS));
        Assert.assertFalse(Analysis.KEY_SERIAL_GC_CMS + " analysis incorrectly identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_SERIAL_GC_CMS));
    }

    /**
     * Test PARALLEL_OLD_COMPACTING caused by <code>Analysis.KEY_EXPLICIT_GC_SERIAL</code>.
     */
    public void testParallelOldCompactingExplicitGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset86.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_SCAVENGE));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING));
        Assert.assertTrue(Analysis.KEY_EXPLICIT_GC_PARALLEL + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_EXPLICIT_GC_PARALLEL));
        Assert.assertFalse(Analysis.KEY_SERIAL_GC_PARALLEL + " analysis incorrectly identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_SERIAL_GC_PARALLEL));
    }

    public void testThreadStackSizeAnalysis32Bit() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset87.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertTrue(Analysis.KEY_THREAD_STACK_SIZE_NOT_SET + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_THREAD_STACK_SIZE_NOT_SET));
    }
}