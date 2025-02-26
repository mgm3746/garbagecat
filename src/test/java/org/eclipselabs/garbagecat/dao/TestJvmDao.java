/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestJvmDao {

    @Test
    void testSafepointSummary() {
        JvmDao jvmDao = new JvmDao();
        UnifiedSafepointEvent event1 = new UnifiedSafepointEvent(
                "[123.192s][info][safepoint   ] Safepoint \"CleanClassLoaderDataMetaspaces\", Time since last: "
                        + "1223019 ns, Reaching safepoint: 138450 ns, At safepoint: 10173766 ns, Total: 10212216 ns");
        jvmDao.addSafepointEvent(event1);
        UnifiedSafepointEvent event2 = new UnifiedSafepointEvent(
                "[1.964s][info][safepoint   ] Safepoint \"ClassLoaderStatsOperation\", Time since last: 4597148 ns, "
                        + "Reaching safepoint: 19270 ns, Cleanup: 47719 ns, At safepoint: 100686473 ns, Total: "
                        + "100753462 ns");
        jvmDao.addSafepointEvent(event2);
        UnifiedSafepointEvent event3 = new UnifiedSafepointEvent(
                "[12.064s][info][safepoint   ] Safepoint \"ClassLoaderStatsOperation\", Time since last: 4597148 ns, "
                        + "Reaching safepoint: 19270 ns, Cleanup: 47719 ns, At safepoint: 100586473 ns, Total: "
                        + "100653462 ns");
        jvmDao.addSafepointEvent(event3);
        List<Map.Entry<Trigger, LongSummaryStatistics>> metrics = jvmDao.getSafepointMetrics();
        // Will be in this order if properly sorted
        assertEquals(Trigger.CLASSLOADER_STATS_OPERATION, metrics.get(0).getKey(),
                Trigger.CLASSLOADER_STATS_OPERATION + " not found.");
        LongSummaryStatistics stats1 = metrics.get(0).getValue();
        assertEquals(2, stats1.getCount(), Trigger.CLASSLOADER_STATS_OPERATION + " count incorrect.");
        assertEquals(201310, stats1.getSum(), Trigger.CLASSLOADER_STATS_OPERATION + " sum incorrect.");
        assertEquals(100705, stats1.getMax(), Trigger.CLASSLOADER_STATS_OPERATION + " max incorrect.");
        assertEquals(Trigger.CLEAN_CLASSLOADER_DATA_METASPACES, metrics.get(1).getKey(),
                Trigger.CLEAN_CLASSLOADER_DATA_METASPACES + " not found.");
        LongSummaryStatistics stats2 = metrics.get(1).getValue();
        assertEquals(1, stats2.getCount(), Trigger.CLEAN_CLASSLOADER_DATA_METASPACES + " count incorrect.");
        assertEquals(10312, stats2.getSum(), Trigger.CLEAN_CLASSLOADER_DATA_METASPACES + " sum incorrect.");
        assertEquals(10312, stats2.getMax(), Trigger.CLEAN_CLASSLOADER_DATA_METASPACES + " max incorrect.");
    }

    @Test
    void testSafepointSummaryTotals() throws IOException {
        File testFile = TestUtil.getFile("dataset280.txt");
        GcManager gcManager = new GcManager();
        URI logFileUri = testFile.toURI();
        List<String> logLines = Files.readAllLines(Paths.get(logFileUri));
        logLines = gcManager.preprocess(logLines);
        gcManager.store(logLines, false);
        JvmRun jvmRun = gcManager.getJvmRun(null, Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(EventType.UNKNOWN),
                JdkUtil.EventType.UNKNOWN.toString() + " event identified.");
        assertEquals(2, jvmRun.getEventTypes().size(), "Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.UNIFIED_HEADER),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_HEADER.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.EventType.UNIFIED_SAFEPOINT),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SAFEPOINT.toString() + ".");
        // Summary time is in microseconds
        long safepointTimeMax = Long.MIN_VALUE;
        long safepointTimeTotal = 0;
        List<Map.Entry<Trigger, LongSummaryStatistics>> metrics = jvmRun.getSafepointMetrics();
        Iterator<Map.Entry<Trigger, LongSummaryStatistics>> iterator = metrics.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Trigger, LongSummaryStatistics> entry = iterator.next();
            if (entry.getValue().getMax() > safepointTimeMax) {
                safepointTimeMax = entry.getValue().getMax();
            }
            safepointTimeTotal += entry.getValue().getSum();
        }
        assertEquals(15910, safepointTimeMax, "Safepoint Summary safepoint time max not correct.");
        assertEquals(19458, safepointTimeTotal, "Safepoint Summary safepoint time total not correct.");
        assertEquals(15910741, jvmRun.getUnifiedSafepointTimeMax(), "JVM Run safepoint time max not correct.");
        assertEquals(19464321, jvmRun.getUnifiedSafepointTimeTotal(), "JVM Run safepoint time total not correct.");
        assertEquals(JdkMath.convertMicrosToSecs(safepointTimeMax),
                JdkMath.convertNanosToSecs(jvmRun.getUnifiedSafepointTimeMax()),
                "Safepoint Summary vs. JVM Run max safepoint time mismatch.");
        assertEquals(JdkMath.convertMicrosToSecs(safepointTimeTotal),
                JdkMath.convertNanosToSecs(jvmRun.getUnifiedSafepointTimeTotal()),
                "Safepoint Summary vs. JVM Run total safepoint time mismatch.");
    }

    @Test
    void testSameTimestampOrdering() {
        JvmDao jvmDao = new JvmDao();
        ParNewEvent event1 = new ParNewEvent("3010778.296: [GC 3010778.296: [ParNew: 337824K->32173K(368640K),"
                + " 0.0803880 secs] 806117K->500466K(1187840K), 0.0805980 secs]");
        jvmDao.addBlockingEvent(event1);
        ParNewEvent event2 = new ParNewEvent(
                "3010786.012: [GC 3010786.012: [ParNew: 356703K->356703K(368640K), 0.0000190 secs]"
                        + " 824995K->824995K(1187840K), 0.0001460 secs]");
        jvmDao.addBlockingEvent(event2);
        SerialOldEvent event3 = new SerialOldEvent("3010786.012: [Full GC 3010786.012:"
                + " [Tenured: 468292K->482213K(819200K), 1.9920590 secs] 824995K->482213K(1187840K),"
                + " [Perm : 123092K->122684K(262144K)], 1.9924510 secs]");
        jvmDao.addBlockingEvent(event3);

        // check they are the correct way around
        List<BlockingEvent> events = jvmDao.getBlockingEvents();
        assertTrue(events.get(1) instanceof ParNewEvent);
        assertTrue(events.get(2) instanceof SerialOldEvent);
    }
}
