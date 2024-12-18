/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain;

import static org.eclipselabs.garbagecat.util.Memory.bytes;
import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.eclipselabs.garbagecat.util.Memory.Unit.BYTES;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.github.joa.JvmOptions;
import org.github.joa.domain.JvmContext;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestJvmRun {

    /**
     * Test application stopped time w/o timestamps.
     * 
     * @throws IOException
     */
    @Test
    void testApplicationStoppedTimeNoTimestamps() throws IOException {
        File testFile = TestUtil.getFile("dataset96.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals((long) 2097502, jvmRun.getDurationTotal(), "GC pause total not correct.");
        assertEquals((long) 16517, jvmRun.getFirstBlockingEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 31432, jvmRun.getLastBlockingEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(271019, jvmRun.getLastBlockingEvent().getDurationMicros(), "GC last duration not correct.");
        assertEquals(1830511, jvmRun.getStoppedTimeTotal(), "Stopped time total not correct.");
        assertEquals((long) 0, jvmRun.getFirstSafepointEvent().getTimestamp(), "Stopped first timestamp not correct.");
        assertEquals((long) 0, jvmRun.getLastSafepointEvent().getTimestamp(), "Stopped last timestamp not correct.");
        assertEquals(50, jvmRun.getLastSafepointEvent().getDurationMicros(), "Stopped last duration not correct.");
        assertEquals((long) 16517, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 31432, jvmRun.getLastEvent().getTimestamp(), "JVM last event timestamp not correct.");
        assertEquals((long) 31703, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 93, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 94, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
        assertEquals((long) 0, jvmRun.getInvertedParallelismCount(), "Inverted parallelism event count not correct.");
    }

    /**
     * Test preprocessing a combined <code>CmsConcurrentEvent</code> and <code>ApplicationConcurrentTimeEvent</code>
     * split across 2 lines.
     * 
     * @throws IOException
     */
    @Test
    void testCombinedCmsConcurrentApplicationConcurrentTimeLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset19.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.CMS_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.CMS_CONCURRENT.toString() + ".");
    }

    /**
     * Test preprocessing a combined <code>CmsConcurrentEvent</code> and <code>ApplicationStoppedTimeEvent</code> split
     * across 2 lines.
     * 
     * @throws IOException
     */
    @Test
    void testCombinedCmsConcurrentApplicationStoppedTimeLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset27.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.CMS_CONCURRENT),
                "Log line not recognized as " + JdkUtil.EventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.APPLICATION_STOPPED_TIME),
                "Log line not recognized as " + JdkUtil.EventType.APPLICATION_STOPPED_TIME.toString() + ".");
    }

    /**
     * Test <code>DateStampPreprocessAction</code>.
     * 
     * @throws IOException
     */
    @Test
    void testDateStampPreprocessActionLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset25.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.EventType.PAR_NEW.toString() + ".");
    }

    /**
     * Test <code>G1PreprocessAction</code> for mixed G1_YOUNG_PAUSE and G1_CONCURRENT with ergonomics.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testExplicitGcAnalsysisParallelSerialOld() throws IOException {
        File testFile = TestUtil.getFile("dataset56.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.PARALLEL_SCAVENGE),
                JdkUtil.EventType.PARALLEL_SCAVENGE.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.PARALLEL_SERIAL_OLD),
                JdkUtil.EventType.PARALLEL_SERIAL_OLD.toString() + " event not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL.getKey()),
                Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL + " analysis not identified.");
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_PARALLEL.getKey()),
                Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis incorrectly identified.");
    }

    /**
     * @throws IOException
     * @throws ParseException
     */
    @Test
    void testFirstAndLastEventDatestamp() throws IOException, ParseException {
        File testFile = TestUtil.getFile("dataset86.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasDatestamps(), "Datestamp incorrectly identified.");
        assertEquals("2016-10-18 01:50:54.000", jvmRun.getFirstEventDatestamp(), "First event datestamp not correct.");
        assertEquals("2016-10-18 01:50:54.036", jvmRun.getLastEventDatestamp(), "Last event datestamp not correct.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_DATESTAMP_APPROXIMATE.getKey()),
                Analysis.WARN_DATESTAMP_APPROXIMATE + " analysis not identified.");
    }

    @Test
    void testgetCompressedClassSpaceSizeBytes() {
        JvmRun jvmRun = new JvmRun(0, null);
        String opts = "-XX:CompressedClassSpaceSize=768m";
        JvmContext jvmContext = new JvmContext(opts);
        JvmOptions jvmOptions = new JvmOptions(jvmContext);
        jvmRun.setJvmOptions(jvmOptions);
        assertEquals(bytes(805306368), jvmRun.getCompressedClassSpaceSizeBytes(),
                "Compressed class space size bytes incorrect.");
    }

    /**
     * Test JVM Header parsing.
     * 
     * @throws IOException
     * 
     */
    @Test
    void testHeaders() throws IOException {
        File testFile = TestUtil.getFile("dataset59.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(3, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.HEADER_COMMAND_LINE_FLAGS),
                JdkUtil.EventType.HEADER_COMMAND_LINE_FLAGS.toString() + " not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.HEADER_MEMORY),
                JdkUtil.EventType.HEADER_MEMORY.toString() + " not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.HEADER_VM_INFO),
                JdkUtil.EventType.HEADER_VM_INFO.toString() + " not identified.");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.WARN_EXPLICIT_GC_DISABLED.getKey()),
                org.github.joa.util.Analysis.WARN_EXPLICIT_GC_DISABLED + " analysis not identified.");
        Date buildDate = jvmRun.getJvmContext().getBuildDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(buildDate);
        // Java Calendar month is 0 based
        assertEquals(3, calendar.get(Calendar.MONTH), "Start month not parsed correctly.");
        assertEquals(10, calendar.get(Calendar.DAY_OF_MONTH), "Start day not parsed correctly.");
        assertEquals(2015, calendar.get(Calendar.YEAR), "Start year not parsed correctly.");
        assertEquals(19, calendar.get(Calendar.HOUR_OF_DAY), "Start hour not parsed correctly.");
        assertEquals(53, calendar.get(Calendar.MINUTE), "Start minute not parsed correctly.");
        assertEquals(14, calendar.get(Calendar.SECOND), "Start second not parsed correctly.");
        assertTrue(jvmRun.hasAnalysis(Analysis.INFO_JDK_ANCIENT.getKey()),
                Analysis.INFO_JDK_ANCIENT + " analysis not identified.");
        assertTrue(
                jvmRun.getAnalysisLiteral(Analysis.INFO_JDK_ANCIENT.getKey())
                        .matches("^The JDK is very old \\(\\d{1,}\\.\\d years\\)\\.$"),
                Analysis.INFO_JDK_ANCIENT + " not correct.");
    }

    /**
     * Test passing JVM options on the command line.
     * 
     */
    @Test
    void testJvmOptionsPassedInOnCommandLine() {
        String jvmOptions = "MGM was here!";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.doAnalysis();
        assertTrue(jvmRun.getJvmOptions().getJvmContext().getOptions().equals(jvmOptions),
                "JVM options passed in are missing or have changed.");
    }

    @Test
    void testLastTimestampNoEvents() {
        GcManager gcManager = new GcManager();
        List<String> logLines = null;
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertNull(jvmRun.getLastBlockingEvent(), "Last GC event not correct.");
    }

    /**
     * Test if logging to stdout.
     * 
     * JDK8 GC logging "CommandLine flags" header will not include the -Xloggc option. It's not possible to distinguish
     * between logging to stdout and -Xloggc with no other logging options, so assume if there are log file options
     * (e.g. size, rotation), the logging is sent to a file.
     * 
     * @throws IOException
     */
    @Test
    void testLoggingToStdOutNot() throws IOException {
        File testFile = TestUtil.getFile("dataset107.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_GC_LOG_STDOUT.getKey()),
                org.github.joa.util.Analysis.INFO_GC_LOG_STDOUT + " analysis incorrectly identified.");
    }

    @Test
    void testMaxHeapBytes() {
        JvmRun jvmRun = new JvmRun(0, null);
        String opts = "-Xss128k -Xmx2048m -XX:MaxMetaspaceSize=1280m";
        JvmContext jvmContext = new JvmContext(opts);
        JvmOptions jvmOptions = new JvmOptions(jvmContext);
        jvmRun.setJvmOptions(jvmOptions);
        assertEquals(bytes(2147483648L), jvmRun.getMaxHeapBytes(), "Max heap bytes incorrect.");
    }

    @Test
    void testMaxHeapBytesUnknown() {
        JvmRun jvmRun = new JvmRun(0, null);
        String opts = "-Xss128k -XX:MaxMetaspaceSize=1280m";
        JvmContext jvmContext = new JvmContext(opts);
        JvmOptions jvmOptions = new JvmOptions(jvmContext);
        jvmRun.setJvmOptions(jvmOptions);
        assertEquals(bytes(0L), jvmRun.getMaxHeapBytes(), "Max heap bytes incorrect.");
    }

    @Test
    void testMaxMetaspaceBytes() {
        JvmRun jvmRun = new JvmRun(0, null);
        String opts = "-Xss128k -Xmx2048m -XX:MaxMetaspaceSize=1280m";
        JvmContext jvmContext = new JvmContext(opts);
        JvmOptions jvmOptions = new JvmOptions(jvmContext);
        jvmRun.setJvmOptions(jvmOptions);
        assertEquals(megabytes(2048), jvmRun.getMaxHeapBytes(), "Max heap bytes incorrect.");
        assertEquals(bytes(1342177280), jvmRun.getMaxMetaspaceBytes(), "Max metaspace bytes incorrect.");
    }

    @Test
    void testMaxMetaspaceBytesUnknown() {
        JvmRun jvmRun = new JvmRun(0, null);
        String opts = "-Xss128k";
        JvmContext jvmContext = new JvmContext(opts);
        JvmOptions jvmOptions = new JvmOptions(jvmContext);
        jvmRun.setJvmOptions(jvmOptions);
        assertEquals(bytes(0L), jvmRun.getMaxMetaspaceBytes(), "Max metaspace bytes incorrect.");
    }

    @Test
    void testMaxPermBytes() {
        JvmRun jvmRun = new JvmRun(0, null);
        String opts = "-Xss128k -Xmx2048m -XX:MaxPermSize=1280m";
        JvmContext jvmContext = new JvmContext(opts);
        JvmOptions jvmOptions = new JvmOptions(jvmContext);
        jvmRun.setJvmOptions(jvmOptions);
        assertEquals(bytes(1342177280), jvmRun.getMaxPermSpaceBytes(), "Max perm space bytes incorrect.");
    }

    @Test
    void testMaxPermBytesUnknown() {
        JvmRun jvmRun = new JvmRun(0, null);
        String opts = "-Xss128k";
        JvmContext jvmContext = new JvmContext(opts);
        JvmOptions jvmOptions = new JvmOptions(jvmContext);
        jvmRun.setJvmOptions(jvmOptions);
        assertEquals(bytes(0L), jvmRun.getMaxPermSpaceBytes(), "Max perm space bytes incorrect.");
    }

    @Test
    void testOtherTime() {
        String logLine1 = "2023-02-10T11:47:06.266+0000: [GC pause (G1 Humongous Allocation) (young), 0.0831454 secs]"
                + "[Ext Root Scanning (ms): 7.2][Other: 14.3 ms][Eden: 380.0M(2426.0M)->0.0B(144.0M) Survivors: "
                + "30.0M->60.0M Heap: 3253.4M(4096.0M)->566.2M(4096.0M)] [Times: user=0.12 sys=0.00, real=0.08 secs]";
        String logLine2 = "2023-02-10T11:47:06.266+0000: [GC pause (G1 Humongous Allocation) (young), 0.0831454 secs]"
                + "[Ext Root Scanning (ms): 7.2][Other: 114.3 ms][Eden: 380.0M(2426.0M)->0.0B(144.0M) Survivors: "
                + "30.0M->60.0M Heap: 3253.4M(4096.0M)->566.2M(4096.0M)] [Times: user=0.12 sys=0.00, real=0.08 secs]";
        List<String> logLines = new ArrayList<String>();
        logLines.add(logLine1);
        logLines.add(logLine2);
        GcManager gcManager = new GcManager();
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(114300, jvmRun.getOtherTimeMax(), "Other time max not correct.");
        assertEquals(128600, jvmRun.getOtherTimeTotal(), "Other time total not correct.");
    }

    /**
     * Test parsing logging with -XX:+PrintGCApplicationConcurrentTime and -XX:+PrintGCApplicationStoppedTime output.
     * 
     * @throws IOException
     */
    @Test
    void testParseLoggingWithApplicationTime() throws IOException {
        File testFile = TestUtil.getFile("dataset3.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(kilobytes(1100288), jvmRun.getMaxYoungSpace(), "Max young space not calculated correctly.");
        assertEquals(kilobytes(1100288), jvmRun.getMaxOldSpace(), "Max old space not calculated correctly.");
        assertEquals((long) 1, jvmRun.getNewRatio(), "NewRatio not calculated correctly.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event count not correct.");
        assertEquals(0, jvmRun.getUnidentifiedLogLines().size(), "Should not be any unidentified log lines.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.EventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.APPLICATION_STOPPED_TIME),
                "Log line not recognized as " + JdkUtil.EventType.APPLICATION_STOPPED_TIME.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.INFO_NEW_RATIO_INVERTED.getKey()),
                Analysis.INFO_NEW_RATIO_INVERTED + " analysis not identified.");
    }

    /**
     * Test percent swap free at threshold.
     */
    @Test
    void testPercentSwapFreeAtThreshold() {
        String jvmOptions = null;
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.setSwap(new Memory(1000, BYTES));
        jvmRun.setSwapFree(new Memory(946, BYTES));
        jvmRun.doAnalysis();
        assertEquals((long) 95, jvmRun.getPercentSwapFree(), "Percent swap free not correct.");
        assertFalse(jvmRun.hasAnalysis(Analysis.INFO_SWAPPING.getKey()),
                Analysis.INFO_SWAPPING + " analysis incorrectly identified.");
    }

    /**
     * Test percent swap free below threshold.
     */
    @Test
    void testPercentSwapFreeBelowThreshold() {
        String jvmOptions = null;
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.setSwap(new Memory(1000, BYTES));
        jvmRun.setSwapFree(new Memory(945, BYTES));
        jvmRun.doAnalysis();
        assertEquals((long) 94, jvmRun.getPercentSwapFree(), "Percent swap free not correct.");
        assertTrue(jvmRun.hasAnalysis(Analysis.INFO_SWAPPING.getKey()),
                Analysis.INFO_SWAPPING + " analysis not identified.");
    }

    /**
     * Test physical memory equals heap + perm/metaspace.
     */
    @Test
    void testPhysicalMemoryEqualJvmAllocation() {
        String jvmOptions = "-Xmx1024M -XX:MaxPermSize=128M";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.setPhysicalMemory(new Memory(1207959552, BYTES));
        jvmRun.doAnalysis();
        assertFalse(jvmRun.hasAnalysis(Analysis.ERROR_PHYSICAL_MEMORY.getKey()),
                Analysis.ERROR_PHYSICAL_MEMORY + " analysis incorrectly identified.");
    }

    /**
     * Test physical memory less than heap + perm/metaspace.
     */
    @Test
    void testPhysicalMemoryLessThanJvmAllocation() {
        String jvmOptions = "-Xmx1024M -XX:MaxPermSize=128M";
        GcManager gcManager = new GcManager();
        JvmRun jvmRun = gcManager.getJvmRun(jvmOptions, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        jvmRun.setPhysicalMemory(new Memory(1207959551, BYTES));
        jvmRun.doAnalysis();
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_PHYSICAL_MEMORY.getKey()),
                Analysis.ERROR_PHYSICAL_MEMORY + " analysis not identified.");
    }

    /**
     * Test identifying <code>ParNewEvent</code> running in incremental mode.
     * 
     * @throws IOException
     */
    @Test
    void testPrintGcApplicationConcurrentTimeAnalysis() throws IOException {
        File testFile = TestUtil.getFile("dataset104.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_PRINT_GC_APPLICATION_CONCURRENT_TIME.getKey()),
                org.github.joa.util.Analysis.INFO_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.");
    }

    /**
     * Test <code>PrintTenuringDistributionPreprocessAction</code> with no space after "GC".
     * 
     * @throws IOException
     * 
     */
    @Test
    void testPrintTenuringDistributionPreprocessActionNoSpaceAfterGc() throws IOException {
        File testFile = TestUtil.getFile("dataset66.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.EventType.PAR_NEW.toString() + ".");
    }

    @Test
    void testRemoveBlankLines() throws IOException {
        File testFile = TestUtil.getFile("dataset20.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.PAR_NEW),
                "Log line not recognized as " + JdkUtil.EventType.PAR_NEW.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(org.github.joa.util.Analysis.INFO_PRINT_GC_APPLICATION_CONCURRENT_TIME.getKey()),
                org.github.joa.util.Analysis.INFO_PRINT_GC_APPLICATION_CONCURRENT_TIME + " analysis not identified.");

    }

    /**
     * Test preprocessing <code>GcTimeLimitExceededEvent</code> with underlying <code>ParallelCompactingOldEvent</code>
     * .
     * 
     * @throws IOException
     */
    @Test
    void testSplitParallelOldCompactingEventLogging() throws IOException {
        File testFile = TestUtil.getFile("dataset28.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.PARALLEL_COMPACTING_OLD),
                "Log line not recognized as " + JdkUtil.EventType.PARALLEL_COMPACTING_OLD.toString() + ".");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED.getKey()),
                Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED.getKey()),
                Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED + " analysis not identified.");
    }

    /**
     * Test summary stats with batching.
     * 
     * @throws IOException
     */
    @Test
    void testStoppedTime() throws IOException {
        File testFile = TestUtil.getFile("dataset103.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3, jvmRun.getEventTypes().size(), "GC event type count not correct.");
        assertEquals(160, jvmRun.getBlockingEventCount(), "GC blocking event count not correct.");
        assertEquals((long) 2568199604L, jvmRun.getDurationTotal(), "GC pause total not correct.");
        assertEquals((long) 4364, jvmRun.getFirstBlockingEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 2801954, jvmRun.getLastBlockingEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(25963804, jvmRun.getLastBlockingEvent().getDurationMicros(), "GC last duration not correct.");
        assertEquals(151, jvmRun.getStoppedTimeEventCount(), "Stopped Time event count not correct.");
        assertEquals(2721420359L, jvmRun.getStoppedTimeTotal(), "Stopped time total not correct.");
        assertEquals((long) 0, jvmRun.getFirstSafepointEvent().getTimestamp(), "Stopped first timestamp not correct.");
        assertEquals((long) 0, jvmRun.getLastSafepointEvent().getTimestamp(), "Stopped last timestamp not correct.");
        assertEquals(36651675, jvmRun.getLastSafepointEvent().getDurationMicros(),
                "Stopped last duration not correct.");
        assertEquals((long) 4364, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 2801954, jvmRun.getLastEvent().getTimestamp(), "JVM last timestamp not correct.");
        assertEquals((long) 2827917, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 9, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 4, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_GC_STOPPED_RATIO.getKey()),
                Analysis.WARN_GC_STOPPED_RATIO + " analysis incorrectly identified.");
        assertEquals((long) 0, jvmRun.getInvertedParallelismCount(), "Inverted parallelism event count not correct.");
    }

    /**
     * Test no gc logging events, only stopped time events.
     * 
     * @throws IOException
     */
    @Test
    void testStoppedTimeWithoutGcEvents() throws IOException {
        File testFile = TestUtil.getFile("dataset108.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals((long) 0, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
    }

    @Test
    void testSummaryStatsG1ExtRootScanning() throws IOException {
        File testFile = TestUtil.getFile("dataset262.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals((long) 400, jvmRun.getExtRootScanningTimeMax(),
                "Max ext root scanning time not calculated correctly.");
        assertEquals((long) 800, jvmRun.getExtRootScanningTimeTotal(),
                "Total ext root scanning time not calculated correctly.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(5, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE),
                JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + " event not identified.");
    }

    @Test
    void testSummaryStatsParallel() throws IOException {
        File testFile = TestUtil.getFile("dataset1.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(kilobytes(248192), jvmRun.getMaxYoungSpace(), "Max young space not calculated correctly.");
        assertEquals(kilobytes(786432), jvmRun.getMaxOldSpace(), "Max old space not calculated correctly.");
        assertEquals((long) 3, jvmRun.getNewRatio(), "NewRatio not calculated correctly.");
        assertEquals(kilobytes(1034624), jvmRun.getMaxHeap(), "Max heap space not calculated correctly.");
        assertEquals(kilobytes(792466), jvmRun.getMaxHeapAfterGc(), "Max heap after GC not calculated correctly.");
        assertEquals(kilobytes(1013058), jvmRun.getMaxHeapOccupancy(), "Max heap occupancy not calculated correctly.");
        assertEquals(2782175, jvmRun.getDurationMax(), "Max pause not calculated correctly.");
        assertEquals(kilobytes(159936), jvmRun.getMaxClassSpace(), "Max perm gen space not calculated correctly.");
        assertEquals(kilobytes(76972), jvmRun.getMaxClassSpaceOccupancy(),
                "Max perm gen occupancy not calculated correctly.");
        assertEquals(kilobytes(76972), jvmRun.getMaxClassSpaceOccupancy(),
                "Max perm gen after GC not calculated correctly.");
        assertEquals((long) 5615401, jvmRun.getDurationTotal(), "Total GC duration not calculated correctly.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.PARALLEL_SCAVENGE),
                JdkUtil.EventType.PARALLEL_SCAVENGE.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.PARALLEL_SERIAL_OLD),
                JdkUtil.EventType.PARALLEL_SERIAL_OLD.toString() + " event not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING.getKey()),
                Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING + " analysis not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_PARALLEL.getKey()),
                Analysis.ERROR_SERIAL_GC_PARALLEL + " analysis not identified.");
    }

    @Test
    void testSummaryStatsParNew() throws IOException {
        File testFile = TestUtil.getFile("dataset2.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(kilobytes(348864), jvmRun.getMaxYoungSpace(), "Max young space not calculated correctly.");
        assertEquals(kilobytes(699392), jvmRun.getMaxOldSpace(), "Max old space not calculated correctly.");
        assertEquals((long) 2, jvmRun.getNewRatio(), "NewRatio not calculated correctly.");
        assertEquals(kilobytes(1048256), jvmRun.getMaxHeap(), "Max heap space not calculated correctly.");
        assertEquals(kilobytes(106395), jvmRun.getMaxHeapAfterGc(), "Max heap after GC not calculated correctly.");
        assertEquals(kilobytes(424192), jvmRun.getMaxHeapOccupancy(), "Max heap occupancy not calculated correctly.");
        assertEquals(1070434, jvmRun.getDurationMax(), "Max pause not calculated correctly.");
        assertEquals(kilobytes(99804), jvmRun.getMaxClassSpace(), "Max perm gen space not calculated correctly.");
        assertEquals(kilobytes(60155), jvmRun.getMaxClassSpaceOccupancy(),
                "Max perm gen occupancy not calculated correctly.");
        assertEquals(kilobytes(60151), jvmRun.getMaxClassSpaceAfterGc(),
                "Max perm gen after GC not calculated correctly.");
        assertEquals((long) 1283369, jvmRun.getDurationTotal(), "Total GC duration not calculated correctly.");
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.PAR_NEW),
                JdkUtil.EventType.PAR_NEW.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.CMS_SERIAL_OLD),
                JdkUtil.EventType.CMS_SERIAL_OLD.toString() + " event not identified.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_SERIAL_GC_CMS.getKey()),
                Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.");
    }

    /**
     * Test summary stats for a partial log file (1st timestamp > Constants.FIRST_TIMESTAMP_THRESHOLD). Same data as
     * dataset41.txt with 1000 seconds added to each timestamp.
     * 
     * @throws IOException
     */
    @Test
    void testSummaryStatsPartialLog() throws IOException {
        File testFile = TestUtil.getFile("dataset98.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "GC event type count not correct.");
        assertEquals(2, jvmRun.getBlockingEventCount(), "GC blocking event count not correct.");
        assertEquals((long) 82616, jvmRun.getDurationTotal(), "GC pause total not correct.");
        assertEquals((long) 1002192, jvmRun.getFirstBlockingEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 1002847, jvmRun.getLastBlockingEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(53453, jvmRun.getLastBlockingEvent().getDurationMicros(), "GC last duration not correct.");
        assertEquals(6, jvmRun.getStoppedTimeEventCount(), "Stopped Time event count not correct.");
        assertEquals(1064937, jvmRun.getStoppedTimeTotal(), "Stopped time total not correct.");
        assertEquals((long) 1000964, jvmRun.getFirstSafepointEvent().getTimestamp(),
                "Stopped first timestamp not correct.");
        assertEquals((long) 1002884, jvmRun.getLastSafepointEvent().getTimestamp(),
                "Stopped last timestamp not correct.");
        assertEquals(1000688, jvmRun.getLastSafepointEvent().getDurationMicros(), "Stopped last duration not correct.");
        assertEquals((long) 1000964, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 1002884, jvmRun.getLastEvent().getTimestamp(), "JVM last event timestamp not correct.");
        assertEquals((long) 2920, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 97, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 64, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_GC_STOPPED_RATIO.getKey()),
                Analysis.WARN_GC_STOPPED_RATIO + " analysis not identified.");
    }

    @Test
    void testSummaryStatsShenandoah() throws IOException {
        File testFile = TestUtil.getFile("dataset207.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.SHENANDOAH_FULL_GC),
                JdkUtil.EventType.SHENANDOAH_FULL_GC.toString() + " event not identified.");
        assertEquals(megabytes(1589), jvmRun.getMaxHeapOccupancy(), "Max heap occupancy not calculated correctly.");
        assertEquals(megabytes(1002), jvmRun.getMaxHeapAfterGc(), "Max heap after GC not calculated correctly.");
        assertEquals(megabytes(1690), jvmRun.getMaxHeap(), "Max heap space not calculated correctly.");
        assertEquals(kilobytes(282195), jvmRun.getMaxClassSpaceOccupancy(),
                "Max metaspace occupancy not calculated correctly.");
        assertEquals(kilobytes(281648), jvmRun.getMaxClassSpaceAfterGc(),
                "Max metaspace after GC not calculated correctly.");
        assertEquals(kilobytes(1314816), jvmRun.getMaxClassSpace(), "Max metaspace space not calculated correctly.");
        assertEquals(4077274, jvmRun.getDurationMax(), "Max pause not calculated correctly.");
        assertEquals(4077274, jvmRun.getDurationTotal(), "Total GC duration not calculated correctly.");
        assertTrue(jvmRun.hasAnalysis(Analysis.ERROR_SHENANDOAH_FULL_GC.getKey()),
                Analysis.ERROR_SHENANDOAH_FULL_GC + " analysis not identified.");
    }

    @Test
    void testSummaryStatsStoppedTime() throws IOException {
        File testFile = TestUtil.getFile("dataset41.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.G1_YOUNG_PAUSE),
                JdkUtil.EventType.G1_YOUNG_PAUSE.toString() + " event not identified.");
        assertEquals((long) 82616, jvmRun.getDurationTotal(), "GC pause total not correct.");
        assertEquals((long) 2192, jvmRun.getFirstBlockingEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 2847, jvmRun.getLastBlockingEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(53453, jvmRun.getLastBlockingEvent().getDurationMicros(), "GC last duration not correct.");
        assertEquals(6, jvmRun.getStoppedTimeEventCount(), "Stopped Time event count not correct.");
        assertEquals(1064937, jvmRun.getStoppedTimeTotal(), "Stopped time total not correct.");
        assertEquals((long) 964, jvmRun.getFirstSafepointEvent().getTimestamp(),
                "Stopped first timestamp not correct.");
        assertEquals((long) 2884, jvmRun.getLastSafepointEvent().getTimestamp(), "Stopped last timestamp not correct.");
        assertEquals(1000688, jvmRun.getLastSafepointEvent().getDurationMicros(), "Stopped last duration not correct.");
        assertEquals((long) 964, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 2884, jvmRun.getLastEvent().getTimestamp(), "JVM last event timestamp not correct.");
        assertEquals((long) 3884, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 98, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 73, jvmRun.getStoppedTimeThroughput(), "Stopped time throughput not correct.");
        assertTrue(jvmRun.hasAnalysis(Analysis.WARN_GC_STOPPED_RATIO.getKey()),
                Analysis.WARN_GC_STOPPED_RATIO + " analysis not identified.");
    }

    @Test
    void testSummaryStatsUnifiedStoppedTime() throws IOException {
        File testFile = TestUtil.getFile("dataset182.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " collector incorrectly identified.");
        assertEquals(5, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.UNIFIED_G1_YOUNG_PAUSE),
                JdkUtil.EventType.UNIFIED_G1_YOUNG_PAUSE.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.UNIFIED_CONCURRENT),
                JdkUtil.EventType.UNIFIED_CONCURRENT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.UNIFIED_REMARK),
                JdkUtil.EventType.UNIFIED_REMARK.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.UNIFIED_G1_CLEANUP),
                JdkUtil.EventType.UNIFIED_G1_CLEANUP.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.UNIFIED_SAFEPOINT),
                JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + " not identified.");
        assertEquals((long) 24656, jvmRun.getDurationTotal(), "GC pause total not correct.");
        assertEquals((long) 53, jvmRun.getFirstBlockingEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 167, jvmRun.getLastBlockingEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(362, jvmRun.getLastBlockingEvent().getDurationMicros(), "GC last duration not correct.");
        assertEquals(12, jvmRun.getUnifiedSafepointEventCount(), "Safepoint event count not correct.");
        assertEquals(25565000, jvmRun.getUnifiedSafepointTimeTotal(), "Safepoint time total not correct.");
        assertEquals((long) 29, jvmRun.getFirstSafepointEvent().getTimestamp(),
                "Safepoint first timestamp not correct.");
        assertEquals((long) 166, jvmRun.getLastSafepointEvent().getTimestamp(),
                "Safepoint last timestamp not correct.");
        assertEquals(439, jvmRun.getLastSafepointEvent().getDurationMicros(), "Safepoint last duration not correct.");
        assertEquals((long) 29, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 167, jvmRun.getLastEvent().getTimestamp(), "JVM last event timestamp not correct.");
        assertEquals((long) 167, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 86, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 25565000, jvmRun.getUnifiedSafepointTimeTotal(), "Safepoint total time not correct.");
        assertEquals((long) 85, jvmRun.getUnifiedSafepointThroughput(), "Safepoint throughput not correct.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_GC_STOPPED_RATIO.getKey()),
                Analysis.WARN_GC_STOPPED_RATIO + " analysis incorrectly identified.");
        assertEquals((long) 96, jvmRun.getGcUnifiedSafepointRatio(), "GC/Safepoint ratio not correct.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_GC_SAFEPOINT_RATIO.getKey()),
                Analysis.WARN_GC_SAFEPOINT_RATIO + " incorrectly not identified.");
    }

    @Test
    void testSummaryStatsZ() throws IOException {
        File testFile = TestUtil.getFile("dataset243.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " collector incorrectly identified.");
        assertEquals(4, jvmRun.getEventTypes().size(), "GC Event count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.Z_MARK_START),
                JdkUtil.EventType.Z_MARK_START.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.Z_MARK_END),
                JdkUtil.EventType.Z_MARK_END.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.UNIFIED_SAFEPOINT),
                JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + " event not identified.");
        assertTrue(jvmRun.getEventTypes().contains(EventType.UNIFIED_HEADER),
                JdkUtil.EventType.UNIFIED_HEADER.toString() + " event not identified.");
        assertEquals(megabytes(0), jvmRun.getMaxHeapOccupancy(), "Max heap occupancy not calculated correctly.");
        assertEquals(megabytes(0), jvmRun.getMaxHeapAfterGc(), "Max heap after GC not calculated correctly.");
        assertEquals(megabytes(0), jvmRun.getMaxHeap(), "Max heap space not calculated correctly.");
        assertEquals(kilobytes(0), jvmRun.getMaxClassSpaceOccupancy(),
                "Max metaspace occupancy not calculated correctly.");
        assertEquals(kilobytes(0), jvmRun.getMaxClassSpaceAfterGc(),
                "Max metaspace after GC not calculated correctly.");
        assertEquals(kilobytes(0), jvmRun.getMaxClassSpace(), "Max metaspace space not calculated correctly.");
        assertEquals(37, jvmRun.getDurationMax(), "Max pause not calculated correctly.");
        assertEquals(969, jvmRun.getDurationTotal(), "Total GC duration not calculated correctly.");
        assertEquals((long) 125, jvmRun.getFirstBlockingEvent().getTimestamp(), "GC first timestamp not correct.");
        assertEquals((long) 2625, jvmRun.getLastBlockingEvent().getTimestamp(), "GC last timestamp not correct.");
        assertEquals(4, jvmRun.getLastBlockingEvent().getDurationMicros(), "GC last duration not correct.");
        assertEquals(154, jvmRun.getUnifiedSafepointEventCount(), "Safepoint event count not correct.");
        assertEquals(12839885, jvmRun.getUnifiedSafepointTimeTotal(), "Safepoint time total not correct.");
        assertEquals((long) 125, jvmRun.getFirstSafepointEvent().getTimestamp(),
                "Safepoint first timestamp not correct.");
        assertEquals((long) 2625, jvmRun.getLastSafepointEvent().getTimestamp(),
                "Safepoint last timestamp not correct.");
        assertEquals(158, jvmRun.getLastSafepointEvent().getDurationMicros(), "Safepoint last duration not correct.");
        assertEquals((long) 125, jvmRun.getFirstEvent().getTimestamp(), "JVM first event timestamp not correct.");
        assertEquals((long) 2625, jvmRun.getLastEvent().getTimestamp(), "JVM last event timestamp not correct.");
        assertEquals((long) 2625, jvmRun.getJvmRunDuration(), "JVM run duration not correct.");
        assertEquals((long) 100, jvmRun.getGcThroughput(), "GC throughput not correct.");
        assertEquals((long) 100, jvmRun.getUnifiedSafepointThroughput(), "Safepoint throughput not correct.");
        assertEquals((long) 100, jvmRun.getGcStoppedRatio(), "GC/Stopped ratio not correct.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_GC_STOPPED_RATIO.getKey()),
                Analysis.WARN_GC_STOPPED_RATIO + " analysis incorrectly identified.");
        assertEquals((long) 8, jvmRun.getGcUnifiedSafepointRatio(), "GC/Safepoint ratio not correct.");
        assertFalse(jvmRun.hasAnalysis(Analysis.WARN_GC_SAFEPOINT_RATIO_JDK17.getKey()),
                Analysis.WARN_GC_SAFEPOINT_RATIO_JDK17 + " analysis incorrectly identified.");
    }
}
