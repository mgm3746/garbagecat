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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestShenandoahConcurrentEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT + "not identified.");
    }

    @Test
    void testJdk11PreprocessedWithMetaspace() {
        String logLine = "[0.266s][info][gc           ] GC(0) Concurrent cleanup 34M->20M(36M) 0.028ms Metaspace: "
                + "3692K(7168K)->3714K(7168K)";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 266, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(megabytes(34), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(megabytes(20), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(megabytes(36), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(kilobytes(3692), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3714), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(7168), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testJdk8() {
        String logLine = "2020-03-10T08:03:29.364-0400: 0.426: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 426, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(16434), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(16466), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(21248), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
    }

    @Test
    void testJdk8Cleanup() {
        String logLine = "2020-03-10T08:03:29.431-0400: 0.493: [Concurrent cleanup 12501K->8434K(23296K), 0.034 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testJdk8Datestamp() {
        String logLine = "2020-03-10T08:03:29.364-0400: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals(637139009364L, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testJdk8Evacuation() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Concurrent evacuation 9712K->9862K(23296K), 0.144 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testJdk8Marking() {
        String logLine = "2020-03-10T08:03:29.365-0400: 0.427: [Concurrent marking 16498K->17020K(21248K), 2.462 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testJdk8MarkingProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.315-0400: 0.377: [Concurrent marking (process weakrefs) "
                + "17759K->19325K(19456K), 6.892 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testJdk8MarkingUpdateRefs() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Concurrent update references 9862K->12443K(23296K), "
                + "3.463 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testJdk8MarkingUpdateRefsProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.315-0400: 0.377: [Concurrent marking (process weakrefs) "
                + "17759K->19325K(19456K), 6.892 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testJdk8Precleaning() {
        String logLine = "2020-03-10T08:03:29.322-0400: 0.384: [Concurrent precleaning 19325K->19357K(19456K), "
                + "0.092 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testJdk8PreprocessedWithMetaspace() {
        String logLine = "2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]"
                + ", [Metaspace: 6477K->6481K(1056768K)]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 467, event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(21278), event.getCombinedOccupancyInit(), "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(4701), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(37888), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(kilobytes(6477), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(6481), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testJdk8Timestamp() {
        String logLine = "0.426: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) 426, event.getTimestamp(), "Time stamp not parsed correctly.");
    }

    @Test
    void testJdk8UpdateReferences() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Concurrent update references 9862K->12443K(23296K), "
                + "3.463 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof ShenandoahConcurrentEvent,
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertTrue(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " not indentified as reportable.");
    }

    @Test
    void testResetNoSizes() {
        String logLine = "[41.892s][info][gc,start     ] GC(1500) Concurrent reset";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testStrongRoots() {
        String logLine = "[0.192s][info][gc,start    ] GC(0) Concurrent strong roots";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testStrongRootsWithDuration() {
        String logLine = "[0.192s][info][gc          ] GC(0) Concurrent strong roots 0.302ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testThreadRoots() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent thread roots";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testThreadRootsWithDuration() {
        String logLine = "[0.191s][info][gc          ] GC(0) Concurrent thread roots 0.442ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_CONCURRENT);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testUnifiedClassUnloading() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent class unloading";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedClassUnloadingWithDuration() {
        String logLine = "[0.192s][info][gc          ] GC(0) Concurrent class unloading 0.343ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedCleanup() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent cleanup";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedCleanupWithSizeAndDuration() {
        String logLine = "[0.472s][info][gc] GC(0) Concurrent cleanup 18M->15M(64M) 0.036ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedDetailsReset() {
        String logLine = "[41.893s][info][gc           ] GC(1500) Concurrent reset 50M->50M(64M) 0.126ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedDetailsResetNoSizes() {
        String logLine = "2020-08-13T16:38:29.318+0000: 432034.969: [Concurrent reset, 26.427 ms]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedEvacuation() {
        String logLine = "[0.465s][info][gc] GC(0) Concurrent evacuation 17M->19M(64M) 6.528ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedMarking() {
        String logLine = "[0.528s][info][gc] GC(1) Concurrent marking 16M->17M(64M) 7.045ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedMarkingProcessWeakrefs() {
        String logLine = "[0.454s][info][gc] GC(0) Concurrent marking (process weakrefs) 17M->19M(64M) 15.264ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedMarkingRoots() {
        String logLine = "[0.188s][info][gc,start    ] GC(0) Concurrent marking roots";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedMarkingRootsWithDuration() {
        String logLine = "[0.188s][info][gc          ] GC(0) Concurrent marking roots 0.435ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedMarkingUpdateRefs() {
        String logLine = "[10.458s][info][gc] GC(279) Concurrent marking (update refs) 47M->48M(64M) 5.559ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "[11.012s][info][gc] GC(300) Concurrent marking (update refs) (process weakrefs) "
                + "49M->49M(64M) 5.416ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    /**
     * Test max heap space and occupancy data.
     * 
     * @throws IOException
     */
    @Test
    void testUnifiedMaxHeapData() throws IOException {
        File testFile = TestUtil.getFile("dataset167.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines, null);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(5, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN),
                JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_MARK),
                JdkUtil.LogEventType.SHENANDOAH_INIT_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_MARK),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_MARK.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_CONCURRENT),
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_INIT_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_INIT_UPDATE.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.SHENANDOAH_FINAL_UPDATE),
                JdkUtil.LogEventType.SHENANDOAH_FINAL_UPDATE.toString() + " collector not identified.");
        assertEquals(kilobytes(19 * 1024), jvmRun.getMaxHeapOccupancyNonBlocking(),
                "Max heap occupancy for a non blocking event not parsed correctly.");
        assertEquals(kilobytes(33 * 1024), jvmRun.getMaxHeapSpaceNonBlocking(),
                "Max heap space for a non blocking event not parsed correctly.");
    }

    @Test
    void testUnifiedPrecleaning() {
        String logLine = "[0.455s][info][gc] GC(0) Concurrent precleaning 19M->19M(64M) 0.202ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedPreprocessedWithMetaspace() {
        String logLine = "[0.484s][info][gc           ] GC(0) Concurrent cleanup 24M->10M(34M) 0.051ms Metaspace: "
                + "3231K->3239K(1056768K)";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (484 - 0), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(24 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(10 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(34 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
        assertEquals(kilobytes(3231), event.getPermOccupancyInit(), "Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(3239), event.getPermOccupancyEnd(), "Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1056768), event.getPermSpace(), "Metaspace allocation size not parsed correctly.");
    }

    @Test
    void testUnifiedReset() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (437 - 4), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(15 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(16 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(64 * 1024), event.getCombinedSpace(), "Combined allocation size not parsed correctly.");
    }

    @Test
    void testUnifiedUncommitStart() {
        String logLine = "2021-03-12T06:36:18.692+0000: 58175.759: [Concurrent uncommit, start]";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedUncommitUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Concurrent uncommit 874M->874M(1303M) 5.654ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(), event.getName(), "Event name incorrect.");
        assertEquals((long) (300050 - 5), event.getTimestamp(), "Time stamp not parsed correctly.");
        assertEquals(kilobytes(874 * 1024), event.getCombinedOccupancyInit(),
                "Combined begin size not parsed correctly.");
        assertEquals(kilobytes(874 * 1024), event.getCombinedOccupancyEnd(), "Combined end size not parsed correctly.");
        assertEquals(kilobytes(1303 * 1024), event.getCombinedSpace(),
                "Combined allocation size not parsed correctly.");
    }

    @Test
    void testUnifiedUncommitUptimeMillisNoGcEventNumber() {
        String logLine = "[2019-02-05T14:52:31.132-0200][300044ms] Concurrent uncommit";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedUnloadClasses() {
        String logLine = "[5.601s][info][gc           ] GC(99) Concurrent marking (unload classes) 7.346ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedUpdateReferences() {
        String logLine = "[0.470s][info][gc] GC(0) Concurrent update references 19M->19M(64M) 4.708ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedUpdateThreadRoots() {
        String logLine = "[0.195s][info][gc,start    ] GC(0) Concurrent update thread roots";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedUpdateThreadRootsWithDuration() {
        String logLine = "[0.195s][info][gc          ] GC(0) Concurrent update thread roots 0.359ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] GC(0) Concurrent reset";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testUnifiedWearkReferences() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent weak references";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testWeakRoots() {
        String logLine = "[0.191s][info][gc,start    ] GC(0) Concurrent weak roots";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }

    @Test
    void testWeakRootsWithDuration() {
        String logLine = "[0.191s][info][gc          ] GC(0) Concurrent weak roots 0.262ms";
        assertTrue(ShenandoahConcurrentEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".");
    }
}
