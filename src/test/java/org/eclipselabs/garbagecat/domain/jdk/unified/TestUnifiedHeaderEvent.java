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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
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
import org.eclipselabs.garbagecat.domain.NullEvent;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedHeaderEvent {

    @Test
    void testActivateRegions() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.034s][debug][gc,heap,region] Activate regions [0, 1152)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testAddressSpaceSize() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Address Space Size: 1536M x 3 = 4608M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testAddressSpaceType() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Address Space Type: Contiguous/Unrestricted/Complete";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testAlignments() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Alignments: Space 512K, Generation 512K, Heap 2M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testAvailableSpaceFilesystem() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Available space on backing filesystem: N/A";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    /**
     * Added in JDK18.
     */
    @Test
    void testCardTableEntrySize() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.016s][info][gc,init] CardTable entry size: 512";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testCdsArchivesMappedAt() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,metaspace] CDS archive(s) mapped at: "
                + "[0x0000000800000000-0x0000000800be2000-0x0000000800be2000), size 12460032, SharedBaseAddress: "
                + "0x0000000800000000, ArchiveRelocationMode: 0.";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testCdsArcivesNotMapped() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.035s][info ][gc,metaspace  ] CDS archive(s) not mapped";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testCompressedClassSpaceMappedAt() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,metaspace] Compressed class space mapped at: "
                + "0x0000000800c00000-0x0000000840c00000, reserved size: 1073741824";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testCompressedOops() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Compressed Oops: Enabled (32-bit)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testConcGcThreads() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.011s][debug][gc           ] ConcGCThreads: 3 offset 29";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testConcurrentRefinementWorkers() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Concurrent Refinement Workers: 10";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testConcurrentWorkers() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Concurrent Workers: 3";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testConsiderClassUnloadingWithConcurrentMark() {
        NullEvent priorLogEvent = new NullEvent();
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testCpus() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] CPUs: 12 total, 12 available";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testExpandTheHeap() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.011s][debug][gc,ergo,heap ] Expand the heap. requested expansion amount: 19327352832B "
                + "expansion amount: 19327352832B";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testG1() throws IOException {
        File testFile = TestUtil.getFile("dataset239.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.GC_INFO),
                JdkUtil.LogEventType.GC_INFO.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertEquals("17.0.1+12-LTS", jvmRun.getJvmContext().getReleaseString(), "JDK version string not correct.");
        assertEquals(17, jvmRun.getJvmOptions().getJvmContext().getVersionMajor(), "JDK major version not correct.");
        assertEquals(1, jvmRun.getJvmOptions().getJvmContext().getVersionMinor(), "JDK minor version not correct.");
    }

    @Test
    void testGcThreads() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-02-22T12:31:30.330+0000][2243][gc,init] GC threads: 2 parallel, 1 concurrent";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testGcThreadsInfo() {
        String logLine = "[0.006s][info][gc,init] GC threads: 2 parallel, 1 concurrent";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testGcWorkers() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] GC Workers: 1 (dynamic)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeapAddress() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.019s][info][gc,heap,coops] Heap address: 0x00000006c2800000, size: 4056 MB, "
                + "Compressed Oops mode: Zero based, Oop shift amount: 3";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeapBackingFile() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Heap Backing File: /memfd:java_heap";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeapBackingFilesystem() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Heap Backing Filesystem: tmpfs (0x1021994)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeapInitialCapacity() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Heap Initial Capacity: 2M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeapMaxCapacity() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Heap Max Capacity: 64M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeapMinCapacity() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Heap Min Capacity: 2M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeapRegionCount() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Heap Region Count: 384";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeapRegionSize() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Heap Region Size: 1M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeapRegionSizeJdk11SmallRSmallS() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2024-04-11T20:17:25.790-0400] Heap region size: 1M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeuristicsAdaptive() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Heuristics: Adaptive";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeuristicsErgonomicallySets() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc] Heuristics ergonomically sets -XX:+ShenandoahImplicitGCInvokesConcurrent";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHeuristicsExplicitGcInvokesConcurrent() {
        String logLine = "[0.006s][info][gc     ] Heuristics ergonomically sets -XX:+ExplicitGCInvokesConcurrent";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testHeuristicsShenandoahImplicitGcInvokesConcurrent() {
        String logLine = "[0.006s][info][gc     ] Heuristics ergonomically sets "
                + "-XX:+ShenandoahImplicitGCInvokesConcurrent";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testHumongous() {
        String logLine = "[0.006s][info][gc,init] Humongous object threshold: 256K";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testHumongousObjectThreshold() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Humongous object threshold: 512K";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testHumongousObjectThresholdLowercase() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-02-22T12:31:30.329+0000][2243][gc,init] Humongous object threshold: 2048K";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testHumongousObjectThresholdUppercase() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Humongous Object Threshold: 256K";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testIdentityEventType() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Initial Capacity: 32M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testInitialCapacity() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Initial Capacity: 32M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testInitializeMarkStack() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.011s][debug][gc           ] Initialize mark stack with 4096 chunks, maximum 524288";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testInitializeShenandoahHeap() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-02-22T12:31:32.306+0000][2243][gc,init] Initialize Shenandoah heap: 6144M initial, "
                + "6144M min, 6144M max";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testInitializeShenandoahHeapInfo() {
        String logLine = "[0.007s][info][gc,init] Initialize Shenandoah heap: 32768K initial, 32768K min, 65536K max";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testInitializingTheZCollector() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Initializing The Z Garbage Collector";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testInitialRefinementZones() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.034s][debug][gc,ergo,refine] Initial Refinement Zones: green: 3328, yellow: 9984, "
                + "red: 16640, min yellow size: 6656";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testJavaClassPathInitial() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.005s][info][arguments] java_class_path (initial): java/.:java/my.jar";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testJavaCommand() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.005s][info][arguments] java_command: <unknown>";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testJvmArgs() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.005s][info][arguments] jvm_args: -Djava.awt.headless=true";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testLargePageSupport() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Large Page Support: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testLauncherType() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.005s][info][arguments] Launcher Type: generic";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testLineWithSpaces() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Max Capacity: 96M    ";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testLogLine() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Max Capacity: 96M";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        UnifiedHeaderEvent event = new UnifiedHeaderEvent(logLine);
        assertEquals((long) 15, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testMarkClosedArchiveRegionsInMap() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[10ms] Mark closed archive regions in map: [0x00000007bfe00000, 0x00000007bfe6cff8]";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testMarkOpenArchiveRegionsInMap() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[10ms] Mark open archive regions in map: [0x00000007bfc00000, 0x00000007bfc47ff8]";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testMaxCapacity() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Max Capacity: 96M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testMaxTlab() {
        String logLine = "[0.006s][info][gc,init] Max TLAB size: 256K";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testMaxTlabSize() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-02-22T12:31:30.329+0000][2243][gc,init] Max TLAB size: 2048K";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testMediumPageSize() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Medium Page Size: N/A";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testMemory() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Memory: 31907M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testMinCapacity() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Min Capacity: 32M";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testMinHeapEqualsMaxHeapDisablingShenandoahUncommit() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-02-22T12:31:30.322+0000][2243][gc] Min heap equals to max heap, disabling "
                + "ShenandoahUncommit";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
        List<String> logLines = new ArrayList<>();
        logLines.add(logLine);
        GcManager gcManager = new GcManager();
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertTrue(jvmRun.hasAnalysis(Analysis.INFO_SHENANDOAH_UNCOMMIT_DISABLED.getKey()),
                Analysis.INFO_SHENANDOAH_UNCOMMIT_DISABLED + " analysis not identified.");
    }

    @Test
    void testMinimumHeapInitialHeapMaximumHeap() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.006s][debug][gc,heap] Minimum heap 28991029248  Initial heap 28991029248  Maximum heap "
                + "28991029248";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testMode() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Mode: Snapshot-At-The-Beginning (SATB)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testNarrowKlassBase() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,metaspace] Narrow klass base: 0x0000000800000000, Narrow klass shift: 0, "
                + "Narrow klass range: 0x100000000";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testNotBlocking() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] NUMA Support: Disabled";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " incorrectly indentified as blocking.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testNumaNodes() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-12-12T08:47:56.693+0200][info][gc,init] NUMA Nodes: 4";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + "incorrectly identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + "incorrectly identified.");
    }

    @Test
    void testNumaSupport() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] NUMA Support: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testPacerForIdle() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Pacer for Idle. Initial: 26M, Alloc Tax Rate: 1.0x";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testPacerForIdleInfo() {
        String logLine = "[0.007s][info][gc,ergo] Pacer for Idle. Initial: 1310K, Alloc Tax Rate: 1.0x";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testParallel() throws IOException {
        File testFile = TestUtil.getFile("dataset238.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.GC_INFO),
                JdkUtil.LogEventType.GC_INFO.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
    }

    @Test
    void testParallelGcThreads() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.011s][debug][gc           ] ParallelGCThreads: 13";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testParallelWorkers() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Parallel Workers: 10";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testParseLogLine() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Version: 17.0.1+12-LTS (release)";
        assertTrue(JdkUtil.parseLogLine(logLine, priorLogEvent, CollectorFamily.UNKNOWN) instanceof UnifiedHeaderEvent,
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " not parsed.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testPeriodicGc() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] Periodic GC: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testPreTouch() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.013s][info][gc,init] Pre-touch: Disabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testReferenceProcessing() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Reference processing: parallel";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testRegions() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-02-22T12:31:30.329+0000][2243][gc,init] Regions: 3072 x 2048K";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testRegions4Digits() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Regions: 2606 x 512K";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testRuntimeWorkers() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.018s][info][gc,init] Runtime Workers: 1";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testSafepointingMechanism() {
        String logLine = "[2019-02-05T14:47:31.092-0200][4ms] Safepointing mechanism: global-page poll";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testSafepointingMechanismInfo() {
        String logLine = "[0.007s][info][gc,init] Safepointing mechanism: global-page poll";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testSafepointMechanism() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-02-22T12:31:32.306+0000][2243][gc,init] Safepointing mechanism: global-page poll";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testSerial() throws IOException {
        File testFile = TestUtil.getFile("dataset237.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.GC_INFO),
                JdkUtil.LogEventType.GC_INFO.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testShanandoahHeuristics() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Shenandoah heuristics: adaptive";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testShanandoahHeuristicsInfo() {
        String logLine = "[0.006s][info][gc,init] Shenandoah heuristics: adaptive";
        assertTrue(UnifiedHeaderEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testShenandoah() throws IOException {
        File testFile = TestUtil.getFile("dataset240.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_SHENANDOAH_TRIGGER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testShenandoahGcMode() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-02-22T12:31:30.330+0000][2243][gc,init] Shenandoah GC mode: Snapshot-At-The-Beginning "
                + "(SATB)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testShenandoahHeuristics() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-02-22T12:31:30.330+0000][2243][gc,init] Shenandoah heuristics: Adaptive";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testStringDeduplication() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[20.715s][info ][stringdedup,init] String Deduplication is enabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testTargetOccupancyUpdates() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.034s][debug][gc,ihop       ] Target occupancy update: old: 0B, new: 19327352832B";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testTimestampTimeUptime() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2021-03-09T14:45:02.441-0300][12.082s] TLAB Size Max: 256K";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testTlabSizeMax() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.014s][info][gc,init] TLAB Size Max: 256K";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testUncommit() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Uncommit: Enabled";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testUncommitDelay() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.015s][info][gc,init] Uncommit Delay: 300s";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_HEADER);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " not indentified as unified.");
    }

    @Test
    void testUsingCms() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[3ms] GC(6) Using Concurrent Mark Sweep";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
        UnifiedHeaderEvent event = new UnifiedHeaderEvent(logLine);
        assertTrue(event.isGarbageCollector(), "Garbage collector information not identified.");
        assertEquals(CollectorFamily.CMS, event.getCollectorFamily(), "CollectorFamily not correct.");
    }

    @Test
    void testUsingCmsParsin() throws IOException {
        File testFile = TestUtil.getFile("dataset151.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEADER.toString() + ".");
    }

    @Test
    void testUsingG1() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2019-05-09T01:38:55.426+0000][18ms] Using G1";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testUsingParallel() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.002s][info][gc] Using Parallel";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testUsingSerial() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.003s][info][gc] Using Serial";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testUsingShenandoah() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.006s][info][gc] Using Shenandoah";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testUsingZ() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.018s][info][gc     ] Using The Z Garbage Collector";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testVersionJdk17() {
        String logLine = "[2022-12-29T10:13:48.750+0000][0.028s] Version: 17.0.5+8-LTS (release)";
        UnifiedHeaderEvent event = new UnifiedHeaderEvent(logLine);
        assertTrue(event.isVersion(), "Version information not identified.");
        assertEquals(17, event.getJdkVersionMajor(), "JDK major version not correct.");
        assertEquals(5, event.getJdkVersionMinor(), "JDK minor version not correct.");
        assertEquals("17.0.5+8-LTS", event.getJdkReleaseString(), "JDK release string not correct.");
    }

    @Test
    void testVersionJdk17U10() {
        String logLine = "[2024-01-30T13:48:05.616-0500] Version: 17.0.10+7-LTS (release)";
        UnifiedHeaderEvent event = new UnifiedHeaderEvent(logLine);
        assertTrue(event.isVersion(), "Version information not identified.");
        assertEquals(17, event.getJdkVersionMajor(), "JDK major version not correct.");
        assertEquals(10, event.getJdkVersionMinor(), "JDK minor version not correct.");
        assertEquals("17.0.10+7-LTS", event.getJdkReleaseString(), "JDK release string not correct.");
    }

    @Test
    void testVersionJdk21() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.006s][info][gc,init] Version: 21.0.1+12-LTS (release)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + "incorrectly identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + "incorrectly identified.");
    }

    @Test
    void testVersionJdk21NoMinorVersion() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-12-12T08:47:56.693+0200][info][gc,init] Version: 21+35-2513 (release)";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + "incorrectly identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + "incorrectly identified.");
    }

    @Test
    void testVmArguments() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[0.005s][info][arguments] VM Arguments:";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }

    @Test
    void testZ() throws IOException {
        File testFile = TestUtil.getFile("dataset241.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " event identified.");
        assertFalse(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.Z_STATS),
                JdkUtil.LogEventType.Z_STATS.toString() + " incorrectly identified.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.GC_INFO),
                JdkUtil.LogEventType.GC_INFO.toString() + " incorrectly identified.");
        assertEquals(1, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.UNIFIED_HEADER),
                JdkUtil.LogEventType.UNIFIED_HEADER.toString() + " not identified.");
    }

    @Test
    void testZJdk21UsingLegacySingleGenerationMode() {
        UnifiedHeaderEvent priorLogEvent = new UnifiedHeaderEvent("");
        String logLine = "[2023-11-16T07:13:24.592-0500] Using legacy single-generation mode";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEADER,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEADER + " not identified.");
        assertNotEquals(JdkUtil.LogEventType.GC_INFO,
                JdkUtil.identifyEventType(logLine, priorLogEvent, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.GC_INFO + " not identified.");
    }
}
