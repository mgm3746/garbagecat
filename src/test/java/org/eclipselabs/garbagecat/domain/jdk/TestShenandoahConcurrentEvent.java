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
package org.eclipselabs.garbagecat.domain.jdk;

import org.junit.Test;

import static org.eclipselabs.garbagecat.Memory.kilobytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestShenandoahConcurrentEvent {

    @Test
    public void testLogLineJdk8() {
        String logLine = "2020-03-10T08:03:29.364-0400: 0.426: [Concurrent reset 16434K->16466K(21248K), 0.091 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 426, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(16434), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(16466), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(21248), event.getCombinedSpace());
    }

    @Test
    public void testLogLineUnified() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 437 - 4, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(15 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(16 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(64 * 1024), event.getCombinedSpace());
    }

    @Test
    public void testLogLineJdk8PreprocessedWithMetaspace() {
        String logLine = "2020-08-21T09:40:29.929-0400: 0.467: [Concurrent cleanup 21278K->4701K(37888K), 0.048 ms]"
                + ", [Metaspace: 6477K->6481K(1056768K)]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 467, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(21278), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(4701), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(37888), event.getCombinedSpace());
        assertEquals("Metaspace begin size not parsed correctly.", kilobytes(6477), event.getPermOccupancyInit());
        assertEquals("Metaspace end size not parsed correctly.", kilobytes(6481), event.getPermOccupancyEnd());
        assertEquals("Metaspace allocation size not parsed correctly.", kilobytes(1056768), event.getPermSpace());
    }

    @Test
    public void testIdentityEventType() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT, JdkUtil.identifyEventType(logLine));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahConcurrentEvent);
    }

    @Test
    public void testNotBlocking() {
        String logLine = "[0.437s][info][gc] GC(0) Concurrent reset 15M->16M(64M) 4.701ms";
        assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        assertTrue(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_CONCURRENT));
    }

    @Test
    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_CONCURRENT);
        assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + " incorrectly indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testJdk8Marking() {
        String logLine = "2020-03-10T08:03:29.365-0400: 0.427: [Concurrent marking 16498K->17020K(21248K), 2.462 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedMarking() {
        String logLine = "[0.528s][info][gc] GC(1) Concurrent marking 16M->17M(64M) 7.045ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testJdk8MarkingProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.315-0400: 0.377: [Concurrent marking (process weakrefs) "
                + "17759K->19325K(19456K), 6.892 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedMarkingProcessWeakrefs() {
        String logLine = "[0.454s][info][gc] GC(0) Concurrent marking (process weakrefs) 17M->19M(64M) 15.264ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testJdk8MarkingUpdateRefs() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Concurrent update references 9862K->12443K(23296K), "
                + "3.463 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedMarkingUpdateRefs() {
        String logLine = "[10.458s][info][gc] GC(279) Concurrent marking (update refs) 47M->48M(64M) 5.559ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testJdk8MarkingUpdateRefsProcessWeakrefs() {
        String logLine = "2020-03-10T08:03:29.315-0400: 0.377: [Concurrent marking (process weakrefs) "
                + "17759K->19325K(19456K), 6.892 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedMarkingUpdateRefsProcessWeakrefs() {
        String logLine = "[11.012s][info][gc] GC(300) Concurrent marking (update refs) (process weakrefs) "
                + "49M->49M(64M) 5.416ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testJdk8Precleaning() {
        String logLine = "2020-03-10T08:03:29.322-0400: 0.384: [Concurrent precleaning 19325K->19357K(19456K), "
                + "0.092 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedPrecleaning() {
        String logLine = "[0.455s][info][gc] GC(0) Concurrent precleaning 19M->19M(64M) 0.202ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testJdk8Evacuation() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Concurrent evacuation 9712K->9862K(23296K), 0.144 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedEvacuation() {
        String logLine = "[0.465s][info][gc] GC(0) Concurrent evacuation 17M->19M(64M) 6.528ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testJdk8UpdateReferences() {
        String logLine = "2020-03-10T08:03:29.427-0400: 0.489: [Concurrent update references 9862K->12443K(23296K), "
                + "3.463 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedUpdateReferences() {
        String logLine = "[0.470s][info][gc] GC(0) Concurrent update references 19M->19M(64M) 4.708ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testJdk8Cleanup() {
        String logLine = "2020-03-10T08:03:29.431-0400: 0.493: [Concurrent cleanup 12501K->8434K(23296K), 0.034 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedCleanup() {
        String logLine = "[0.472s][info][gc] GC(0) Concurrent cleanup 18M->15M(64M) 0.036ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testResetNoSizes() {
        String logLine = "[41.892s][info][gc,start     ] GC(1500) Concurrent reset";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedDetailsResetNoSizes() {
        String logLine = "2020-08-13T16:38:29.318+0000: 432034.969: [Concurrent reset, 26.427 ms]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedDetailsReset() {
        String logLine = "[41.893s][info][gc           ] GC(1500) Concurrent reset 50M->50M(64M) 0.126ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] GC(0) Concurrent reset";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedUncommitUptimeMillis() {
        String logLine = "[2019-02-05T14:52:31.138-0200][300050ms] Concurrent uncommit 874M->874M(1303M) 5.654ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 300050 - 5, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(874 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(874 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(1303 * 1024), event.getCombinedSpace());
    }

    @Test
    public void testUnifiedUncommitUptimeMillisNoGcEventNumber() {
        String logLine = "[2019-02-05T14:52:31.132-0200][300044ms] Concurrent uncommit";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    @Test
    public void testUnifiedPreprocessedWithMetaspace() {
        String logLine = "[0.484s][info][gc           ] GC(0) Concurrent cleanup 24M->10M(34M) 0.051ms Metaspace: "
                + "3231K->3239K(1056768K)";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
        ShenandoahConcurrentEvent event = new ShenandoahConcurrentEvent(logLine);
        assertEquals("Event name incorrect.", JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString(),
                event.getName());
        assertEquals("Time stamp not parsed correctly.", 484 - 0, event.getTimestamp());
        assertEquals("Combined begin size not parsed correctly.", kilobytes(24 * 1024), event.getCombinedOccupancyInit());
        assertEquals("Combined end size not parsed correctly.", kilobytes(10 * 1024), event.getCombinedOccupancyEnd());
        assertEquals("Combined allocation size not parsed correctly.", kilobytes(34 * 1024), event.getCombinedSpace());
        assertEquals("Metaspace begin size not parsed correctly.", kilobytes(3231), event.getPermOccupancyInit());
        assertEquals("Metaspace end size not parsed correctly.", kilobytes(3239), event.getPermOccupancyEnd());
        assertEquals("Metaspace allocation size not parsed correctly.", kilobytes(1056768), event.getPermSpace());
    }

    @Test
    public void testUnifiedUnloadClasses() {
        String logLine = "[5.601s][info][gc           ] GC(99) Concurrent marking (unload classes) 7.346ms";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CONCURRENT.toString() + ".",
                ShenandoahConcurrentEvent.match(logLine));
    }

    /**
     * Test max heap space and occupancy data.
     */
    @Test
    public void testUnifiedMaxHeapData() {
        File testFile = TestUtil.getFile("dataset167.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        assertEquals("Max heap occupancy for a non blocking event not parsed correctly.", kilobytes(19 * 1024), jvmRun.getMaxHeapOccupancyNonBlocking());
        assertEquals("Max heap space for a non blocking event not parsed correctly.", kilobytes(33 * 1024), jvmRun.getMaxHeapSpaceNonBlocking());
    }
}
