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
package org.eclipselabs.garbagecat.preprocess.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestCmsPreprocessAction {

    @Test
    void testBeginningParNewWithNoParNewWithCmsConcurrentPreclean() throws IOException {
        File testFile = TestUtil.getFile("dataset105.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                "Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_REMARK),
                "Log line not recognized as " + LogEventType.CMS_REMARK.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED),
                Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED + " analysis identified.");
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED),
                org.github.joa.util.Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED + " analysis identified.");
    }

    @Test
    void testCmsConcurrentMixedApplicationConcurrentTime() throws IOException {
        File testFile = TestUtil.getFile("dataset135.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_STOPPED_TIME),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_STOPPED_TIME.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME),
                "Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".");
    }

    @Test
    void testCmsScavengeBeforeRemarkJdk8MixedHeapAtGc() throws IOException {
        File testFile = TestUtil.getFile("dataset136.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                "Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_REMARK),
                "Log line not recognized as " + LogEventType.CMS_REMARK.toString() + ".");
    }

    @Test
    void testCmsScavengeBeforeRemarkJMixedHeapAtGc() throws IOException {
        File testFile = TestUtil.getFile("dataset140.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                "Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_REMARK),
                "Log line not recognized as " + LogEventType.CMS_REMARK.toString() + ".");
    }

    @Test
    void testCmsScavengeBeforeRemarkNoPrintGcDetails() throws IOException {
        File testFile = TestUtil.getFile("dataset120.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(4, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
    }

    /**
     * Test CMS_SERIAL_OLD with concurrent mode failure trigger mixed with CMS_CONCURRENT over 2 lines.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testCmsSerialConcurrentModeFailureMixedCmsConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset61.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                LogEventType.CMS_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.");
    }

    /**
     * Test CMS_SERIAL_OLD with concurrent mode failure trigger mixed with CMS_CONCURRENT over 2 lines on JDK8.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testCmsSerialConcurrentModeFailureMixedCmsConcurrentJdk8() throws IOException {
        File testFile = TestUtil.getFile("dataset69.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                LogEventType.CMS_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_CMS_INCREMENTAL_MODE),
                Analysis.WARN_CMS_INCREMENTAL_MODE + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.");
    }

    @Test
    void testCmsSerialOldConcurrentModeFailureMixedConcurrentMark() throws IOException {
        File testFile = TestUtil.getFile("dataset124.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.");
    }

    /**
     * Test CMS_SERIAL_OLD with concurrent mode interrupted trigger mixed with CMS_CONCURRENT over 2 lines.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testCmsSerialOldConcurrentModeInterruptedMixedCmsConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset71.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED),
                JdkUtil.TriggerType.CMS_CONCURRENT_MODE_INTERRUPTED.toString() + " trigger not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    /**
     * Test preprocessing CMS_SERIAL_OLD triggered by <code>PrintClassHistogramEvent</code> across many lines.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testCmsSerialOldPrintClassHistogramTriggerAcross5Lines() throws IOException {
        File testFile = TestUtil.getFile("dataset81.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
    }

    /**
     * Test preprocessing CMS_SERIAL_OLD with JvmtiEnv ForceGarbageCollection and concurrent mode interrupted.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testCmsSerialOldTriggerJvmtiEnvForceGarbageCollectionWithConcurrentModeInterrupted() throws IOException {
        File testFile = TestUtil.getFile("dataset90.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED),
                Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED + " analysis not identified.");
    }

    /**
     * Test preprocessing CMS_SERIAL_OLD with Metadata GC Threshold and concurrent mode interrupted.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testCmsSerialOldTriggerMetadataGcThresholdWithConcurrentModeInterrupted() throws IOException {
        File testFile = TestUtil.getFile("dataset91.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.");
    }

    @Test
    void testLineConcurrentDoubleDatestampMixedApplicationTime() {
        String priorLogLine = "";
        String logLine = "2017-06-18T05:23:03.452-0500: 2.182: 2017-06-18T05:23:03.452-0500: [CMS-concurrent-preclean: "
                + "0.016/0.048 secs]2.182: Application time: 0.0055079 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2017-06-18T05:23:03.452-0500: 2.182: [CMS-concurrent-preclean: 0.016/0.048 secs]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLineConcurrentMixedApplicationTime() {
        String priorLogLine = "";
        String logLine = "408365.532: [CMS-concurrent-mark: 0.476/10.257 secs]Application time: 0.0576080 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("408365.532: [CMS-concurrent-mark: 0.476/10.257 secs]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLineConcurrentMixedStoppedTime() {
        String priorLogLine = "";
        String logLine = "234784.781: [CMS-concurrent-abortable-preclean: 0.038/0.118 secs]Total time for"
                + " which application threads were stopped: 0.0123330 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("234784.781: [CMS-concurrent-abortable-preclean: 0.038/0.118 secs]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningCmsConcurrentMixedApplicationConcurrentTime() {
        String priorLogLine = "";
        String logLine = "2017-06-18T05:23:03.452-0500: 2.182: 2017-06-18T05:23:03.452-0500: "
                + "[CMS-concurrent-preclean: 0.016/0.048 secs]2.182: Application time: 0.0055079 seconds";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2017-06-18T05:23:03.452-0500: 2.182: [CMS-concurrent-preclean: 0.016/0.048 secs]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningParNewConcurrentModeFailureClassHistogram() {
        String priorLogLine = "";
        String logLine = "2017-04-22T12:43:48.008+0100: 466904.470: [GC 466904.473: [ParNew: "
                + "516864K->516864K(516864K), 0.0001999 secs]466904.473: [Class Histogram:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningParNewConcurrentModeFailureClassHistogramWithDatestamps() {
        String priorLogLine = "";
        String logLine = "2017-05-03T14:47:00.002-0400: 1784.661: [GC 2017-05-03T14:47:00.006-0400: 1784.664: "
                + "[ParNew: 4147200K->4147200K(4147200K), 0.0677200 secs]"
                + "2017-05-03T14:47:00.075-0400: 1784.735: [Class Histogram:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningParNewDatestamp() {
        String priorLogLine = "";
        String logLine = "2016-09-07T16:59:44.005-0400: 26536.942: [GC"
                + "2016-09-07T16:59:44.005-0400: 26536.943: [ParNew";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningParNewMixedHeapAtGc() {
        String priorLogLine = "";
        String logLine = "4237.297: [GC[YG occupancy: 905227 K (4194240 K)]{Heap before GC invocations=85 (full 1):";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("4237.297: [GC[YG occupancy: 905227 K (4194240 K)]", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningParNewTenuringDistribution() {
        String priorLogLine = "";
        String logLine = "2016-09-23T09:05:18.745-0700: 2.372: [GC (Allocation Failure) "
                + "2016-09-23T09:05:18.745-0700: 2.372: [ParNew";
        String nextLogLine = "Desired survivor size 78643200 bytes, new threshold 15 (max 15)";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningParNewWithFlsStatistics() {
        String priorLogLine = "";
        String logLine = "1.118: [GC Before GC:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("1.118: [GC ", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningParNewWithNoParNewWithCmsConcurrentPreclean() {
        String priorLogLine = "";
        String logLine = "3576157.596: [GC 3576157.596: [CMS-concurrent-abortable-preclean: 0.997/1.723 secs] "
                + "[Times: user=3.20 sys=0.03, real=1.73 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("3576157.596: [GC ", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningSerialConcurrentWithGcLockerInitiatedGc() {
        String priorLogLine = "";
        String logLine = "58626.878: [Full GC (GCLocker Initiated GC)58626.878: [CMS"
                + "58630.075: [CMS-concurrent-sweep: 3.220/3.228 secs] [Times: user=3.38 sys=0.01, real=3.22 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("58626.878: [Full GC (GCLocker Initiated GC)58626.878: [CMS", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningSerialConcurrentWithJvmtiEnvForceGarbageCollectionTrigger() {
        String priorLogLine = "";
        String logLine = "262372.344: [Full GC (JvmtiEnv ForceGarbageCollection) 262372.344: [CMS262372.426: "
                + "[CMS-concurrent-mark: 0.082/0.083 secs] [Times: user=0.08 sys=0.00, real=0.09 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("262372.344: [Full GC (JvmtiEnv ForceGarbageCollection) 262372.344: [CMS", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningSerialConcurrentWithMetadataGcThreshold() {
        String priorLogLine = "";
        String logLine = "262375.122: [Full GC (Metadata GC Threshold) 262375.122: [CMS262375.200: "
                + "[CMS-concurrent-mark: 0.082/0.082 secs] [Times: user=0.08 sys=0.00, real=0.08 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("262375.122: [Full GC (Metadata GC Threshold) 262375.122: [CMS", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningSerialMixedClassHistogramWithDatestamp() {
        String priorLogLine = "";
        String logLine = "2017-05-03T14:51:32.659-0400: 2057.323: [Full GC "
                + "2017-05-03T14:51:32.680-0400: 2057.341: [Class Histogram:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginningSerialNoSpaceAfterTrigger() {
        String priorLogLine = "";
        String logLine = "5026.107: [Full GC (Allocation Failure)5026.108: [CMS"
                + "5027.062: [CMS-concurrent-sweep: 9.543/33.853 secs] "
                + "[Times: user=107.27 sys=5.82, real=33.85 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("5026.107: [Full GC (Allocation Failure)5026.108: [CMS", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineBeginParNewCombinedFlsStatistics() {
        String priorLogLine = "";
        String logLine = "2017-02-27T14:29:54.533+0000: 2.730: [GC (Allocation Failure) Before GC:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals("2017-02-27T14:29:54.533+0000: 2.730: [GC (Allocation Failure) ", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineClassHistogramTrigger() {
        String priorLogLine = "";
        String logLine = "1662.232: [Full GC 11662.233: [Class Histogram:";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineCmsRemarkWithoutGcDetails() {
        String priorLogLine = "";
        String logLine = "2017-04-03T03:12:02.134-0500: 30.385: [GC (CMS Final Remark)  890910K->620060K(7992832K), "
                + "0.1223879 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineCmsSerialOldCombinedConcurrentDatestamp() {
        String priorLogLine = "";
        String logLine = "2016-10-10T19:17:37.771-0700: 2030.108: [GC (Allocation Failure) "
                + "2016-10-10T19:17:37.771-0700: 2030.108: [ParNew2016-10-10T19:17:37.773-0700: "
                + "2030.110: [CMS-concurrent-abortable-preclean: 0.050/0.150 secs] "
                + "[Times: user=0.11 sys=0.03, real=0.15 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(
                "2016-10-10T19:17:37.771-0700: 2030.108: [GC (Allocation Failure) "
                        + "2016-10-10T19:17:37.771-0700: 2030.108: [ParNew",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineCmsSerialOldMixedAbortPrecleanConcurrentAbortablePreclean() {
        String logLine = "2017-06-22T21:22:03.269-0400: 23.858: [Full GC 23.859: [CMS CMS: abort preclean due to "
                + "time 2017-06-22T21:22:03.269-0400: 23.859: [CMS-concurrent-abortable-preclean: 0.338/5.115 secs] "
                + "[Times: user=14.57 sys=0.83, real=5.11 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
        String nextLogLine = " (concurrent mode failure): 8156K->36298K(7864320K), 1.0166580 secs] "
                + "89705K->36298K(8336192K), [CMS Perm : 34431K->34268K(34548K)], 1.0172840 secs] "
                + "[Times: user=0.86 sys=0.14, real=1.02 secs]";
        Set<String> context = new HashSet<String>();
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("2017-06-22T21:22:03.269-0400: 23.858: [Full GC 23.859: [CMS", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineCmsSerialOldMixedConcurrentAbortablePreclean() {
        String logLine = "85217.903: [Full GC 85217.903: [CMS85217.919: [CMS-concurrent-abortable-preclean: "
                + "0.723/3.756 secs] [Times: user=2.54 sys=0.08, real=3.76 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineCmsSerialOldMixedConcurrentMark() {
        String logLine = "2016-02-26T16:37:58.740+1100: 44.684: [Full GC2016-02-26T16:37:58.740+1100: 44.684: [CMS"
                + "2016-02-26T16:37:58.933+1100: 44.877: [CMS-concurrent-mark: 1.508/2.428 secs] "
                + "[Times: user=3.44 sys=0.49, real=2.42 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineCmsSerialOldMixedConcurrentSpaceAfterGC() {
        String logLine = "85238.030: [Full GC 85238.030: [CMS85238.672: [CMS-concurrent-mark: 0.666/0.686 secs] "
                + "[Times: user=1.40 sys=0.01, real=0.69 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineCmsSerialOldWithConcurrentModeFailureMixedConcurrentPreclean() {
        String logLine = "1278.200: [Full GC (Allocation Failure) 1278.202: [CMS1280.173: "
                + "[CMS-concurrent-preclean: 2.819/2.865 secs] [Times: user=6.97 sys=0.41, real=2.87 secs]";
        String nextLogLine = " (concurrent mode failure): 2658303K->2658303K(2658304K), 9.1546180 secs] "
                + "4040703K->2750110K(4040704K), [Metaspace: 72113K->72113K(1118208K)], 9.1581450 secs] "
                + "[Times: user=9.15 sys=0.00, real=9.16 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineCmsSerialOldWithConcurrentModeFailureMixedConcurrentSweep() {
        String logLine = "2440.336: [Full GC (Allocation Failure) 2440.338: [CMS"
                + "2440.542: [CMS-concurrent-sweep: 1.137/1.183 secs] [Times: user=5.33 sys=0.51, real=1.18 secs]";
        String nextLogLine = " (concurrent mode failure): 2658304K->2658303K(2658304K), 9.4908960 secs] "
                + "4040703K->2996946K(4040704K), [Metaspace: 72191K->72191K(1118208K)], 9.4942330 secs] "
                + "[Times: user=9.49 sys=0.00, real=9.49 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineCmsSerialOldWithTriggerMixedConcurrent() {
        String logLine = "706.707: [Full GC (Allocation Failure) 706.708: [CMS709.137: [CMS-concurrent-mark: "
                + "3.381/5.028 secs] [Times: user=23.92 sys=3.02, real=5.03 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineConcurrentModeInterrupted() {
        String priorLogLine = "";
        String logLine = " (concurrent mode interrupted): 861863K->904027K(1797568K), 42.9053262 secs] "
                + "1045947K->904027K(2047232K), [CMS Perm : 252246K->252202K(262144K)], 42.9070278 secs] "
                + "[Times: user=43.11 sys=0.18, real=42.91 secs]";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineDateStampClassHistogramTrigger() {
        String priorLogLine = "";
        String logLine = "2017-04-24T21:07:32.713+0100: 669928.617: [Full GC 669928.619: [Class Histogram:";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineDuration() {
        String priorLogLine = "";
        String logLine = ", 10.7515460 secs]";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineDurationWithTimeStamp() {
        String priorLogLine = "";
        String logLine = ", 0.0536040 secs] [Times: user=0.89 sys=0.01, real=0.06 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineEnd() {
        String logLine = ": 153599K->17023K(153600K), 0.0383370 secs] 229326K->114168K(494976K), 0.0384820 secs] "
                + "[Times: user=0.15 sys=0.01, real=0.04 secs]";
        String priorLogLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineEndCmsScavengeBeforeRemark() {
        String priorLogLine = "";
        String logLine = " 1677988K(7992832K), 0.3055773 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(" 1677988K(7992832K), 0.3055773 secs]", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndCmsSerialOld() {
        String priorLogLine = "";
        String logLine = " 7778348K->1168095K(7848704K), [CMS Perm : 481281K->451017K(771512K)], 123.0277354 secs] "
                + "[Times: user=123.19 sys=0.18, real=123.03 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndParNew() {
        String priorLogLine = "";
        String logLine = "3576157.596: [ParNew: 147599K->17024K(153344K), 0.0795160 secs] "
                + "2371401K->2244459K(6274432K), 0.0810030 secs] [Times: user=0.44 sys=0.00, real=0.08 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndPromotionFailedConcurrentModeFailure() {
        String logLine = " (promotion failed): 471871K->471872K(471872K), 0.7685416 secs]66645.266: [CMS (concurrent "
                + "mode failure): 1572864K->1572863K(1572864K), 6.3611861 secs] 2001479K->1657572K(2044736K), "
                + "[Metaspace: 567956K->567956K(1609728K)], 7.1304658 secs] "
                + "[Times: user=8.60 sys=0.01, real=7.13 secs]";
        String priorLogLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineEndTimesData() {
        String priorLogLine = "2017-06-18T05:23:03.452-0500: 2.182: 2017-06-18T05:23:03.452-0500: "
                + "[CMS-concurrent-preclean: 0.016/0.048 secs]2.182: Application time: 0.0055079 seconds";
        String logLine = " [Times: user=0.15 sys=0.02, real=0.05 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineEndWithCommas() {
        String logLine = ": 289024K->17642K(306688K), 0,0788160 secs] 4086255K->3814874K(12548864K), 0,0792920 secs] "
                + "[Times: user=0,28 sys=0,00, real=0,08 secs]";
        String priorLogLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineEndWithMetaspace() {
        String logLine = " (concurrent mode failure): 2655937K->2373842K(2658304K), 11.6746550 secs] "
                + "3973407K->2373842K(4040704K), [Metaspace: 72496K->72496K(1118208K)] icms_dc=77 , 11.6770830 secs] "
                + "[Times: user=14.05 sys=0.02, real=11.68 secs]";
        String priorLogLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineEndWithPerm() {
        String logLine = " (concurrent mode failure): 1218548K->413373K(1465840K), 1.3656970 secs] "
                + "1229657K->413373K(1581168K), [CMS Perm : 83805K->80520K(83968K)], 1.3659420 secs] "
                + "[Times: user=1.33 sys=0.01, real=1.37 secs]";
        String priorLogLine = "44.684: [Full GC44.684: [CMS44.877: [CMS-concurrent-mark: 1.508/2.428 secs] "
                + "[Times: user=3.44 sys=0.49, real=2.42 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineEndWithWhitespaceEnd() {
        String logLine = ": 153599K->17023K(153600K), 0.0383370 secs] 229326K->114168K(494976K), 0.0384820 secs] "
                + "[Times: user=0.15 sys=0.01, real=0.04 secs]    ";
        String priorLogLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineMiddle() {
        String logLine = "1907.974: [CMS-concurrent-mark: 23.751/40.476 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineMiddleCmsRemarkJdk8() {
        String priorLogLine = "";
        String logLine = "2017-06-18T05:23:16.634-0500: 15.364: [GC (CMS Final Remark) 2017-06-18T05:23:16.634-0500: "
                + "15.364: [ParNew";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleCmsSerialOldMixedAbortPrecleanDueToTime() {
        String priorLogLine = "";
        String logLine = "471419.156: [CMS CMS: abort preclean due to time 2017-04-22T13:59:06.831+0100: 471423.282: "
                + "[CMS-concurrent-abortable-preclean: 3.663/31.735 secs] "
                + "[Times: user=39.81 sys=0.23, real=31.74 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals("471419.156: [CMS", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleCmsSerialOldMixedConcurrentSweep() {
        String priorLogLine = "";
        String logLine = "669950.539: [CMS2017-04-24T21:08:04.965+0100: 669960.868: [CMS-concurrent-sweep: "
                + "13.324/39.970 secs] [Times: user=124.31 sys=2.44, real=39.97 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals("669950.539: [CMS", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleCmsSerialOldWithDatestampMixedConcurrentSweep() {
        String priorLogLine = "";
        String logLine = "2017-05-03T14:47:16.910-0400: 1801.570: [CMS2017-05-03T14:47:22.416-0400: 1807.075: "
                + "[CMS-concurrent-mark: 29.707/71.001 secs] [Times: user=121.03 sys=35.41, real=70.99 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals("2017-05-03T14:47:16.910-0400: 1801.570: [CMS", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleConcurrentModeFailureMixedClassHistogram() {
        String priorLogLine = "";
        String logLine = " (concurrent mode failure): 7835032K->8154090K(9216000K), 56.0787320 secs]"
                + "2017-05-03T14:48:13.002-0400: 1857.661: [Class Histogram";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(logLine, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleParNewCombinedFlsStatistics() {
        String priorLogLine = "";
        String logLine = "2017-02-27T14:29:54.534+0000: 2.730: [ParNew: 2048000K->191475K(2304000K), 0.0366288 secs] "
                + "2048000K->191475K(7424000K)After GC:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals("2017-02-27T14:29:54.534+0000: 2.730: [ParNew: 2048000K->191475K(2304000K), 0.0366288 secs] "
                + "2048000K->191475K(7424000K)", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleParNewCombinedFlsStatisticsPrintPromotionFailure() {
        String priorLogLine = "";
        String logLine = "2017-02-28T00:43:55.587+0000: 36843.783: [ParNew (0: promotion failure size = 200)  "
                + "(1: promotion failure size = 8)  (2: promotion failure size = 200)  "
                + "(3: promotion failure size = 200)  (4: promotion failure size = 200)  "
                + "(5: promotion failure size = 200)  (6: promotion failure size = 200)  "
                + "(7: promotion failure size = 200)  (8: promotion failure size = 10)  "
                + "(9: promotion failure size = 10)  (10: promotion failure size = 10)  "
                + "(11: promotion failure size = 200)  (12: promotion failure size = 200)  "
                + "(13: promotion failure size = 10)  (14: promotion failure size = 200)  "
                + "(15: promotion failure size = 200)  (16: promotion failure size = 200)  "
                + "(17: promotion failure size = 200)  (18: promotion failure size = 200)  "
                + "(19: promotion failure size = 200)  (20: promotion failure size = 10)  "
                + "(21: promotion failure size = 200)  (22: promotion failure size = 10)  "
                + "(23: promotion failure size = 45565)  (24: promotion failure size = 10)  "
                + "(25: promotion failure size = 4)  (26: promotion failure size = 200)  "
                + "(27: promotion failure size = 200)  (28: promotion failure size = 10)  "
                + "(29: promotion failure size = 200)  (30: promotion failure size = 200)  "
                + "(31: promotion failure size = 200)  (32: promotion failure size = 200)  "
                + "(promotion failed): 2304000K->2304000K(2304000K), 0.4501923 secs]"
                + "2017-02-28T00:43:56.037+0000: 36844.234: [CMSCMS: Large block 0x0000000730892bb8";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        String logLinePreprocessed = "2017-02-28T00:43:55.587+0000: 36843.783: [ParNew "
                + "(0: promotion failure size = 200)  (1: promotion failure size = 8)  "
                + "(2: promotion failure size = 200)  (3: promotion failure size = 200)  "
                + "(4: promotion failure size = 200)  (5: promotion failure size = 200)  "
                + "(6: promotion failure size = 200)  (7: promotion failure size = 200)  "
                + "(8: promotion failure size = 10)  (9: promotion failure size = 10)  "
                + "(10: promotion failure size = 10)  (11: promotion failure size = 200)  "
                + "(12: promotion failure size = 200)  (13: promotion failure size = 10)  "
                + "(14: promotion failure size = 200)  (15: promotion failure size = 200)  "
                + "(16: promotion failure size = 200)  (17: promotion failure size = 200)  "
                + "(18: promotion failure size = 200)  (19: promotion failure size = 200)  "
                + "(20: promotion failure size = 10)  (21: promotion failure size = 200)  "
                + "(22: promotion failure size = 10)  (23: promotion failure size = 45565)  "
                + "(24: promotion failure size = 10)  (25: promotion failure size = 4)  "
                + "(26: promotion failure size = 200)  (27: promotion failure size = 200)  "
                + "(28: promotion failure size = 10)  (29: promotion failure size = 200)  "
                + "(30: promotion failure size = 200)  (31: promotion failure size = 200)  "
                + "(32: promotion failure size = 200)  (promotion failed): 2304000K->2304000K(2304000K), "
                + "0.4501923 secs]2017-02-28T00:43:56.037+0000: 36844.234: [CMS";
        assertEquals(logLinePreprocessed, event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleParNewTruncatedBeginMixedConcurrentFlsStatistics2() {
        String priorLogLine = "";
        String logLine = "2017-03-19T11:48:55.207+0000: 356616.193: [ParNew2017-03-19T11:48:55.211+0000: 356616.198: "
                + "[CMS-concurrent-abortable-preclean: 1.046/3.949 secs] [Times: user=1.16 sys=0.05, real=3.95 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals("2017-03-19T11:48:55.207+0000: 356616.193: [ParNew", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleParNewTruncatedEndMixedConcurrentFlsStatistics2() {
        String priorLogLine = "";
        String logLine = ": 66097K->7194K(66368K), 0.0440189 secs] 5274098K->5219953K(10478400K)After GC:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(": 66097K->7194K(66368K), 0.0440189 secs] 5274098K->5219953K(10478400K)", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleParNewWithFlsStatistics() {
        String priorLogLine = "";
        String logLine = "1.118: [ParNew: 377487K->8426K(5505024K), 0.0535260 secs] 377487K->8426K(43253760K)After GC:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("1.118: [ParNew: 377487K->8426K(5505024K), 0.0535260 secs] 377487K->8426K(43253760K)",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineMiddleSerialFlsStatistics() {
        String priorLogLine = "";
        String logLine = ": 2818067K->2769354K(5120000K), 3.8341757 secs] 5094036K->2769354K(7424000K), "
                + "[Metaspace: 18583K->18583K(1067008K)]After GC:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals(
                ": 2818067K->2769354K(5120000K), 3.8341757 secs] 5094036K->2769354K(7424000K), "
                        + "[Metaspace: 18583K->18583K(1067008K)]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineParNewBailing() {
        String logLine = "2137.769: [GC 2137.769: [ParNew (promotion failed): 242304K->242304K(242304K), "
                + "8.4066690 secs]2146.176: [CMSbailing out to foreground collection";
        assertTrue(CmsPreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewConcurrentModeFailureMixedConcurrentJdk8() {
        String logLine = "719.519: [GC (Allocation Failure) 719.521: [ParNew: 1382400K->1382400K(1382400K), "
                + "0.0000470 secs]719.521: [CMS722.601: [CMS-concurrent-mark: 3.567/3.633 secs] "
                + "[Times: user=10.91 sys=0.69, real=3.63 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewConcurrentModeFailurePermDataMixedConcurrentSweep() {
        String logLine = "11202.526: [GC (Allocation Failure) 1202.528: [ParNew: 1355422K->1355422K(1382400K), "
                + "0.0000500 secs]1202.528: [CMS1203.491: [CMS-concurrent-sweep: 1.009/1.060 secs] "
                + "[Times: user=1.55 sys=0.12, real=1.06 secs]";
        String nextLogLine = " (concurrent mode failure): 2656311K->2658289K(2658304K), 9.3575580 secs] "
                + "4011734K->2725109K(4040704K), [Metaspace: 72111K->72111K(1118208K)], 9.3610080 secs] "
                + "[Times: user=9.35 sys=0.01, real=9.36 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewHotspotBailing() {
        String logLine = "1901.217: [GC 1901.217: [ParNew: 261760K->261760K(261952K), 0.0000570 secs]1901.217: "
                + "[CMSJava HotSpot(TM) Server VM warning: bailing out to foreground collection";
        assertTrue(CmsPreprocessAction.match(logLine, null, ""),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewMixedCmsConcurrentAbortablePreclean() {
        String priorLogLine = "";
        String logLine = "2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: "
                + "0.043/0.144 secs] [Times: user=0.58 sys=0.03, real=0.14 secs]";
        String nextLogLine = ": 212981K->3156K(242304K), 0.0364435 secs] 4712182K->4502357K(4971420K), "
                + "0.0368807 secs] [Times: user=0.18 sys=0.02, real=0.04 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewMixedCmsConcurrentAbortablePreclean2() {
        String priorLogLine = "2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: "
                + "0.043/0.144 secs] [Times: user=0.58 sys=0.03, real=0.14 secs]";
        String logLine = ": 212981K->3156K(242304K), 0.0364435 secs] 4712182K->4502357K(4971420K), "
                + "0.0368807 secs] [Times: user=0.18 sys=0.02, real=0.04 secs]";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewMixedCmsConcurrentSweep() {
        String priorLogLine = "";
        String logLine = "1821.661: [GC 1821.661: [ParNew1821.661: [CMS-concurrent-sweep: "
                + "42.841/48.076 secs] [Times: user=19.45 sys=0.45, real=48.06 secs]";
        String nextLogLine = ": 36500K->3770K(38336K), 0.1767060 secs] 408349K->375618K(2092928K), "
                + "0.1769190 secs] [Times: user=0.05 sys=0.00, real=0.18 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewMixedConcurrent() {
        String logLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] [Times: user=1.56 sys=0.01, real=2.23 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewMixedConcurrentReset() {
        String priorLogLine = null;
        String logLine = "2017-03-21T16:02:23.633+0530: 53277.279: [GC 53277.279: [ParNew: "
                + "2853312K->2853312K(2853312K), 0.0000310 secs]53277.279: [CMS2017-03-21T16:02:23.655+0530: "
                + "53277.301: [CMS-concurrent-reset: 0.019/0.023 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]";
        String nextLogLine = ": 8943881K->8813432K(9412608K), 7.7851270 secs] 11797193K->9475525K(12265920K), [CMS "
                + "Perm : 460344K->460331K(770956K)], 7.7854740 secs] [Times: user=7.79 sys=0.01, real=7.78 secs]";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        assertEquals("2017-03-21T16:02:23.633+0530: 53277.279: [GC 53277.279: [ParNew: 2853312K->2853312K(2853312K), "
                + "0.0000310 secs]53277.279: [CMS", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineParNewMixedConcurrentWithWhitespaceEnd() {
        String logLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewNoTriggerMixedConcurrent() {
        String logLine = "10.963: [GC10.963: [ParNew10.977: [CMS-concurrent-abortable-preclean: 0.088/0.197 secs] "
                + "[Times: user=0.33 sys=0.05, real=0.20 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewNoTriggerMixedConcurrentWithCommas() {
        String logLine = "32552,602: [GC32552,602: [ParNew32552,610: "
                + "[CMS-concurrent-abortable-preclean: 3,090/4,993 secs] "
                + "[Times: user=3,17 sys=0,02, real=5,00 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewPrefixed() {
        String priorLogLine = "";
        String logLine = "831626.089: [ParNew831628.158: [ParNew833918.729: [GC (Allocation Failure) 833918.729: "
                + "[ParNew: 595103K->12118K(619008K), 0.0559019 secs] 1247015K->664144K(4157952K), 0.0561698 secs] "
                + "[Times: user=0.09 sys=0.00, real=0.06 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, null, context);
        assertEquals(
                "833918.729: [GC (Allocation Failure) 833918.729: [ParNew: 595103K->12118K(619008K), 0.0559019 secs] "
                        + "1247015K->664144K(4157952K), 0.0561698 secs] [Times: user=0.09 sys=0.00, real=0.06 secs]",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLineParNewPromotionFailed() {
        String logLine = "233333.318: [GC 233333.319: [ParNew (promotion failed): 673108K->673108K(707840K), "
                + "1.5366054 secs]233334.855: [CMS233334.856: [CMS-concurrent-abortable-preclean: 12.033/27.431 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineParNewTriggerMixedConcurrentJdk8() {
        String logLine = "45.574: [GC (Allocation Failure) 45.574: [ParNew45.670: [CMS-concurrent-abortable-preclean: "
                + "3.276/4.979 secs] [Times: user=7.75 sys=0.28, real=4.98 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        assertTrue(CmsPreprocessAction.match(logLine, null, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        Set<String> context = new HashSet<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("45.574: [GC (Allocation Failure) 45.574: [ParNew", event.getLogEntry(),
                "Log line not parsed correctly.");
    }

    @Test
    void testLogLineParTriggerPromotionFailed() {
        String logLine = "182314.858: [GC 182314.859: [ParNew (promotion failed)";
        assertTrue(CmsPreprocessAction.match(logLine, null, ""),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLinePrintClassHistogramMiddleSerial() {
        String priorLogLine = "";
        String logLine = "11700.930: [CMS: 2844387K->635365K(7331840K), 46.4488813 secs]11747.379: [Class Histogram";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLinePrintHeapAtGcBeginCmsRemark() {
        String priorLogLine = "";
        String logLine = "2017-04-03T08:55:45.544-0500: 20653.796: [GC (CMS Final Remark) {Heap before GC "
                + "invocations=686 (full 15):";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLinePrintHeapAtGcBeginCmsRemarkJdk8() {
        String priorLogLine = "";
        String logLine = "2017-06-18T05:23:16.634-0500: 15.364: [GC (CMS Final Remark) [YG occupancy: 576424 K "
                + "(1677760 K)]{Heap before GC invocations=8 (full 2):";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLinePrintHeapAtGcBeginParNew() {
        String priorLogLine = "";
        String logLine = "27067.966: [GC {Heap before gc invocations=498:";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLinePrintHeapAtGcBeginSerial() {
        String priorLogLine = "";
        String logLine = "28282.075: [Full GC {Heap before gc invocations=528:";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLinePrintHeapAtGcMiddleSerial() {
        String priorLogLine = "";
        String logLine = "49830.934: [CMS: 1640998K->1616248K(3407872K), 11.0964500 secs] "
                + "1951125K->1616248K(4193600K), [CMS Perm : 507386K->499194K(786432K)]"
                + "Heap after gc invocations=147:";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLinePrintHeapAtGcMiddleSerialConcurrentModeFailure() {
        String priorLogLine = "";
        String logLine = " (concurrent mode failure): 1179601K->1179648K(1179648K), 10.7510650 secs] "
                + "1441361K->1180553K(1441600K), [CMS Perm : 71172K->71171K(262144K)]Heap after gc invocations=529:";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLinePrintHeapAtGcParNewConcurrentModeFailure() {
        String priorLogLine = "";
        String logLine = " (concurrent mode failure): 1147900K->1155037K(1179648K), 7.3953900 secs] "
                + "1409660K->1155037K(1441600K)Heap after gc invocations=499:";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineRetainBeginningConcurrentModeFailure() {
        String priorLogLine = "";
        String logLine = " (concurrent mode failure): 5355855K->991044K(7331840K), 58.3748587 secs]639860.666: "
                + "[Class Histogram";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineRetainBeginningParNewNoSpaceAfterGc() {
        String priorLogLine = "";
        String logLine = "12.891: [GC12.891: [ParNew";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineRetainEndClassHistogram() {
        String priorLogLine = "";
        String logLine = " 3863904K->756393K(7848704K), [CMS Perm : 682507K->442221K(1048576K)], 107.6553710 secs]"
                + " [Times: user=112.83 sys=0.28, real=107.66 secs]";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineRetainMiddleClassHistogram() {
        String priorLogLine = "";
        String logLine = ": 516864K->516864K(516864K), 2.0947428 secs]182316.954: [Class Histogram: ";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineRetainMiddleSerialConcurrentMixed() {
        String priorLogLine = "";
        String logLine = ": 917504K->917504K(917504K), 5.5887120 secs]877375.047: [CMS877378.691: "
                + "[CMS-concurrent-mark: 5.714/11.380 secs] [Times: user=14.72 sys=4.81, real=11.38 secs]";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLineSerialBailing() {
        String logLine = "4300.825: [Full GC 4300.825: [CMSbailing out to foreground collection";
        assertTrue(CmsPreprocessAction.match(logLine, null, null),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogLinParNewPromotionFailedTruncatedWithCmsConcurrentMark() {
        String priorLogLine = "";
        String logLine = "36455.096: [GC 36455.096: [ParNew (promotion failed): 153344K->153344K(153344K), "
                + "0.6818450 secs]36455.778: [CMS36459.090: [CMS-concurrent-mark: 3.439/4.155 secs] "
                + "[Times: user=8.27 sys=0.17, real=4.16 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("36455.096: [GC 36455.096: [ParNew (promotion failed): 153344K->153344K(153344K), 0.6818450 secs]"
                + "36455.778: [CMS", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLinParNewPromotionFailedTruncatedWithCmsConcurrentPreclean() {
        String priorLogLine = "";
        String logLine = "65778.258: [GC65778.258: [ParNew (promotion failed): 8300210K->8088352K(8388608K), "
                + "1.4967400 secs]65779.755: [CMS65781.579: [CMS-concurrent-preclean: 2.150/47.638 secs] "
                + "[Times: user=81.22 sys=2.02, real=47.63 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals("65778.258: [GC65778.258: [ParNew (promotion failed): 8300210K->8088352K(8388608K), "
                + "1.4967400 secs]65779.755: [CMS", event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogLinParNewPromotionFailedWithCmsAbortPrecleanDueToTime() {
        String priorLogLine = "";
        String logLine = "73241.738: [GC (Allocation Failure)73241.738: [ParNew (promotion failed): "
                + "8205461K->8187503K(8388608K), 2.1449990 secs]73243.883: [CMS CMS: abort preclean due to time "
                + "3244.984: [CMS-concurrent-abortable-preclean: 3.335/9.080 secs] "
                + "[Times: user=43.26 sys=1.66, real=9.08 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + PreprocessActionType.CMS.toString() + ".");
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        assertEquals(
                "73241.738: [GC (Allocation Failure)73241.738: [ParNew (promotion failed): "
                        + "8205461K->8187503K(8388608K), 2.1449990 secs]73243.883: [CMS",
                event.getLogEntry(), "Log line not parsed correctly.");
    }

    @Test
    void testLogMiddleParNewConcurrentAbortablePrecleanMixed() {
        String priorLogLine = "";
        String logLine = "27067.966: [ParNew: 261760K->261760K(261952K), 0.0000160 secs]27067.966: [CMS"
                + "27067.966: [CMS-concurrent-abortable-preclean: 2.272/29.793 secs]";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogMiddleParNewConcurrentMarkMixed() {
        String priorLogLine = "";
        String logLine = "28308.701: [ParNew (promotion failed): 261951K->261951K(261952K), 0.7470390 secs]28309.448: "
                + "[CMS28312.544: [CMS-concurrent-mark: 5.114/5.863 secs]";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogMiddleParNewTruncatedConcurrentMarkMixed() {
        String priorLogLine = "";
        String logLine = ": 153344K->153344K(153344K), 0.2049130 secs]2017-02-15T16:22:05.602+0900: 1223922.433: "
                + "[CMS2017-02-15T16:22:06.001+0900: 1223922.832: [CMS-concurrent-mark: 3.589/4.431 secs] "
                + "[Times: user=6.13 sys=0.89, real=4.43 secs]";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testLogMiddleSerialConcurrentPrecleanMixed() {
        String priorLogLine = "";
        String logLine = "28282.075: [CMS28284.687: [CMS-concurrent-preclean: 3.706/3.706 secs]";
        String nextLogLine = "";
        assertTrue(CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine),
                "Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".");
    }

    @Test
    void testParNewCmsConcurrentOver3Lines() throws IOException {
        File testFile = TestUtil.getFile("dataset112.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.");
    }

    @Test
    void testParNewConcurrentModeFailureMixedAbortPrecleanDueToTime() throws IOException {
        File testFile = TestUtil.getFile("dataset121.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.");
    }

    @Test
    void testParNewConcurrentModeFailureMixedConcurrentMark() throws IOException {
        File testFile = TestUtil.getFile("dataset123.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.");
    }

    @Test
    void testParNewConcurrentModeFailureMixedConcurrentPreclean() throws IOException {
        File testFile = TestUtil.getFile("dataset122.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.");
    }

    /**
     * Test PAR_NEW mixed with CMS_CONCURRENT over 2 lines.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testParNewMixedCmsConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset58.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                LogEventType.PAR_NEW.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                LogEventType.CMS_CONCURRENT.toString() + " collector not identified.");
    }

    @Test
    void testParNewMixedHeapAtGc() throws IOException {
        File testFile = TestUtil.getFile("dataset141.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                "Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".");
    }

    /**
     * Test preprocessing PAR_NEW with extraneous prefix.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testParNewPrefixed() throws IOException {
        File testFile = TestUtil.getFile("dataset89.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                "Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".");
    }

    /**
     * Test preprocessing PAR_NEW mixed with <code>PrintHeapAtGcEvent</code>.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testParNewPrintHeapAtGc() throws IOException {
        File testFile = TestUtil.getFile("dataset83.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                "Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_PRINT_HEAP_AT_GC),
                Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.");
    }

    @Test
    void testParNewPromotionFailedTruncatedEventLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset23.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_PROMOTION_FAILED),
                Analysis.ERROR_CMS_PROMOTION_FAILED + " analysis not identified.");
    }

    /**
     * Test preprocessing PAR_NEW with FLS_STATISTICS.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testParNewWithFlsStatistics() throws IOException {
        File testFile = TestUtil.getFile("dataset94.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                "Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.FLS_STATISTICS),
                "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_FLS_STATISTICS),
                org.github.joa.util.Analysis.INFO_JDK8_PRINT_FLS_STATISTICS + " analysis not identified.");
    }

    @Test
    void testPrintFLSStatistics2ParNewOver4Lines() throws IOException {
        File testFile = TestUtil.getFile("dataset117.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.FLS_STATISTICS),
                "Log line not recognized as " + JdkUtil.LogEventType.FLS_STATISTICS.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_FLS_STATISTICS),
                org.github.joa.util.Analysis.INFO_JDK8_PRINT_FLS_STATISTICS + " analysis not identified.");
    }

    @Test
    void testPrintPromotionFailure() throws IOException {
        File testFile = TestUtil.getFile("dataset115.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(5, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_PROMOTION_FAILED),
                Analysis.ERROR_CMS_PROMOTION_FAILED + " analysis not identified.");
    }

    /**
     * Test preprocessing <code>CmsSerialOldConcurrentModeFailureEvent</code> split over 3 lines.
     * 
     * @throws IOException
     */
    @Test
    void testSplit3LinesCmsConcurrentModeFailureEventLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset14.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>CmsPreprocessAction</code>: split <code>CmsSerialOldEvent</code> and <code>CmsConcurrentEvent</code>.
     * 
     * @throws IOException
     */
    @Test
    void testSplitCmsConcurrentModeFailureEventAbortablePrecleanLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset11.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                JdkUtil.TriggerType.CMS_CONCURRENT_MODE_FAILURE.toString() + " trigger not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    /**
     * Test <code>CmsPreprocessAction</code>: split <code>CmsSerialOldEvent</code> and <code>CmsConcurrentEvent</code>.
     * 
     * @throws IOException
     */
    @Test
    void testSplitCmsConcurrentModeFailureEventMarkLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset10.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                JdkUtil.TriggerType.CMS_CONCURRENT_MODE_FAILURE.toString() + " trigger not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    /**
     * Test split <code>ParNewEvent</code> with a trigger and -XX:+PrintTenuringDistribution logging between the initial
     * and final lines.
     * 
     * @throws IOException
     */
    @Test
    void testSplitMixedTenuringParNewPromotionEventWithTriggerLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset67.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.PAR_NEW),
                "Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".");
    }

    /**
     * Test preprocessing a split <code>ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent</code> with
     * -XX:+PrintTenuringDistribution logging between the initial and final lines.
     * 
     * @throws IOException
     */
    @Test
    void testSplitMixedTenuringParNewPromotionFailedEventLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset18.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
    }

    /**
     * Test with underlying <code>CmsSerialOld</code> triggered by concurrent mode failure.
     * 
     * @throws IOException
     */
    @Test
    void testSplitPrintHeapAtGcCmsSerialOldConcurrentModeFailureLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset8.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE),
                Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_PRINT_HEAP_AT_GC),
                Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.");
    }

    /**
     * Test preprocessing <code>PrintHeapAtGcEvent</code> with underlying <code>CmsSerialOldEvent</code>.
     * 
     * @throws IOException
     */
    @Test
    void testSplitPrintHeapAtGcCmsSerialOldLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset6.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_PRINT_HEAP_AT_GC),
                Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.");
    }

    /**
     * Test preprocessing <code>PrintHeapAtGcEvent</code> with underlying <code>ParNewConcurrentModeFailureEvent</code>.
     * 
     * @throws IOException
     */
    @Test
    void testSplitPrintHeapAtGcParNewPromotionFailedCmsConcurrentModeFailureEventLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset21.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD),
                "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_PRINT_HEAP_AT_GC),
                Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.");
    }

    @Test
    void testUnknownWithCmsConcurrent() throws IOException {
        File testFile = TestUtil.getFile("dataset111.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        String lastLogLineUnprocessed = "130454.251: [Full GC (Allocation Failure) 130454.251: [CMS130456.427: "
                + "[CMS-concurrent-mark: 2.176/2.182 secs] [Times: user=2.18 sys=0.00, real=2.18 secs]";
        assertEquals(lastLogLineUnprocessed, gcManager.getLastLogLineUnprocessed(),
                "Last unprocessed log line not correct.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                "Log line not recognized as " + LogEventType.UNKNOWN.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT),
                "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        // Not the last preprocessed line, but part of last unpreprocessed line
        assertTrue(jvmRun.hasAnalysis(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST),
                Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST + " analysis not identified.");
    }
}
