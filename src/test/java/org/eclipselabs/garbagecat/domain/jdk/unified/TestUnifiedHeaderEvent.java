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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedHeaderEvent {

    @Test
    void testAddressSpaceSize() {
        String logLine = "[0.014s][info][gc,init] Address Space Size: 1536M x 3 = 4608M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testAddressSpaceType() {
        String logLine = "[0.014s][info][gc,init] Address Space Type: Contiguous/Unrestricted/Complete";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testAlignments() {
        String logLine = "[0.013s][info][gc,init] Alignments: Space 512K, Generation 512K, Heap 2M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testAvailableSpaceFilesystem() {
        String logLine = "[0.015s][info][gc,init] Available space on backing filesystem: N/A";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testCdsArchivesMappedAt() {
        String logLine = "[0.013s][info][gc,metaspace] CDS archive(s) mapped at: "
                + "[0x0000000800000000-0x0000000800be2000-0x0000000800be2000), size 12460032, SharedBaseAddress: "
                + "0x0000000800000000, ArchiveRelocationMode: 0.";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testCompressedClassSpaceMappedAt() {
        String logLine = "[0.013s][info][gc,metaspace] Compressed class space mapped at: "
                + "0x0000000800c00000-0x0000000840c00000, reserved size: 1073741824";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testCompressedOops() {
        String logLine = "[0.013s][info][gc,init] Compressed Oops: Enabled (32-bit)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testConcurrentRefinementWorkers() {
        String logLine = "[0.014s][info][gc,init] Concurrent Refinement Workers: 10";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testConcurrentWorkers() {
        String logLine = "[0.014s][info][gc,init] Concurrent Workers: 3";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testCpus() {
        String logLine = "[0.013s][info][gc,init] CPUs: 12 total, 12 available";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testGcWorkers() {
        String logLine = "[0.014s][info][gc,init] GC Workers: 1 (dynamic)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapBackingFile() {
        String logLine = "[0.014s][info][gc,init] Heap Backing File: /memfd:java_heap";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapBackingFilesystem() {
        String logLine = "[0.014s][info][gc,init] Heap Backing Filesystem: tmpfs (0x1021994)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapInitialCapacity() {
        String logLine = "[0.013s][info][gc,init] Heap Initial Capacity: 2M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapMaxCapacity() {
        String logLine = "[0.013s][info][gc,init] Heap Max Capacity: 64M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapMinCapacity() {
        String logLine = "[0.013s][info][gc,init] Heap Min Capacity: 2M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapRegionCount() {
        String logLine = "[0.014s][info][gc,init] Heap Region Count: 384";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeapRegionSize() {
        String logLine = "[0.014s][info][gc,init] Heap Region Size: 1M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHeuristics() {
        String logLine = "[0.014s][info][gc,init] Heuristics: Adaptive";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testHumongousObjectThreshold() {
        String logLine = "[0.014s][info][gc,init] Humongous Object Threshold: 256K";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testInitialCapacity() {
        String logLine = "[0.015s][info][gc,init] Initial Capacity: 32M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testInitializingTheZCollector() {
        String logLine = "[0.014s][info][gc,init] Initializing The Z Garbage Collector";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testLargePageSupport() {
        String logLine = "[0.013s][info][gc,init] Large Page Support: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testLineWithSpaces() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)   ";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        UnifiedHeaderEvent event = new UnifiedHeaderEvent(logLine);
        assertEquals((long) 13, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testMaxCapacity() {
        String logLine = "[0.015s][info][gc,init] Max Capacity: 96M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testMediumPageSize() {
        String logLine = "[0.015s][info][gc,init] Medium Page Size: N/A";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testMemory() {
        String logLine = "[0.013s][info][gc,init] Memory: 31907M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testMinCapacity() {
        String logLine = "[0.015s][info][gc,init] Min Capacity: 32M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testMode() {
        String logLine = "[0.014s][info][gc,init] Mode: Snapshot-At-The-Beginning (SATB)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testNarrowKlassBase() {
        String logLine = "[0.013s][info][gc,metaspace] Narrow klass base: 0x0000000800000000, Narrow klass shift: 0, "
                + "Narrow klass range: 0x100000000";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testNumaSupport() {
        String logLine = "[0.013s][info][gc,init] NUMA Support: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testParallelWorkers() {
        String logLine = "[0.013s][info][gc,init] Parallel Workers: 10";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof UnifiedHeaderEvent,
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " not parsed.");
    }

    @Test
    void testPeriodicGc() {
        String logLine = "[0.014s][info][gc,init] Periodic GC: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testPreTouch() {
        String logLine = "[0.013s][info][gc,init] Pre-touch: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testRuntimeWorkers() {
        String logLine = "[0.018s][info][gc,init] Runtime Workers: 1";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testTimeUptime() {
        String logLine = "[2021-03-09T14:45:02.441-0300][12.082s] Version: 17.0.1+12-LTS (release)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testTlabSizeMax() {
        String logLine = "[0.014s][info][gc,init] TLAB Size Max: 256K";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testUncommit() {
        String logLine = "[0.015s][info][gc,init] Uncommit: Enabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testUncommitDelay() {
        String logLine = "[0.015s][info][gc,init] Uncommit Delay: 300s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.UNIFIED_HEADER + "not identified.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_HEADER);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " not indentified as unified.");
    }

    @Test
    void testSerial() {
        File testFile = TestUtil.getFile("dataset237.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_SERIAL),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_SERIAL.toString() + ".");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testParallel() {
        File testFile = TestUtil.getFile("dataset238.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_PARALLEL),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_PARALLEL.toString() + ".");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testG1() {
        File testFile = TestUtil.getFile("dataset239.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_G1),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_G1.toString() + ".");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testShenandoah() {
        File testFile = TestUtil.getFile("dataset240.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_SHENANDOAH),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_SHENANDOAH.toString() + ".");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testZ() {
        File testFile = TestUtil.getFile("dataset241.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.USING_Z),
                "Log line not recognized as " + JdkUtil.LogEventType.USING_Z.toString() + ".");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }
}
