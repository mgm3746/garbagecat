/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.hsql;

import java.io.File;
import java.util.List;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.Jvm;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJvmDao extends TestCase {

    public void testSummaryStatsParallel() {

        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset1.txt");
        GcManager jvmManager = new GcManager();
        jvmManager.store(testFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);

        Assert.assertEquals("Max heap space not calculated correctly.", 1034624, jvmRun.getMaxHeapSpace());
        Assert.assertEquals("Max pause not calculated correctly.", 2782, jvmRun.getMaxPause());
        Assert.assertEquals("Max heap space not calculated correctly.", 1034624, jvmRun.getMaxHeapSpace());
        Assert.assertEquals("Max heap occupancy not calculated correctly.", 1013058, jvmRun.getMaxHeapOccupancy());
        Assert.assertEquals("Max perm gen space not calculated correctly.", 159936, jvmRun.getMaxPermSpace());
        Assert.assertEquals("Max perm gen occupancy not calculated correctly.", 76972, jvmRun.getMaxPermOccupancy());
        Assert.assertEquals("Total duration not calculated correctly.", 5614, jvmRun.getTotalPause());
        Assert.assertEquals("Event count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SCAVENGE));
        Assert.assertTrue(JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.PARALLEL_SERIAL_OLD));
    }

    public void testSummaryStatsParNew() {

        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset2.txt");
        GcManager jvmManager = new GcManager();
        jvmManager.store(testFile);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);

        Assert.assertEquals("Max heap space not calculated correctly.", 1048256, jvmRun.getMaxHeapSpace());
        Assert.assertEquals("Max heap occupancy not calculated correctly.", 424192, jvmRun.getMaxHeapOccupancy());
        Assert.assertEquals("Max pause not calculated correctly.", 1070, jvmRun.getMaxPause());
        Assert.assertEquals("Max heap space not calculated correctly.", 1048256, jvmRun.getMaxHeapSpace());
        Assert.assertEquals("Max heap occupancy not calculated correctly.", 424192, jvmRun.getMaxHeapOccupancy());
        Assert.assertEquals("Max perm gen space not calculated correctly.", 99804, jvmRun.getMaxPermSpace());
        Assert.assertEquals("Max perm gen occupancy not calculated correctly.", 60155, jvmRun.getMaxPermOccupancy());
        Assert.assertEquals("Total duration not calculated correctly.", 1282, jvmRun.getTotalPause());
        Assert.assertEquals("Event count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(JdkUtil.LogEventType.PAR_NEW.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        Assert.assertTrue(JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.", jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
    }

    public void testLastTimestampNoEvents() {
        GcManager jvmManager = new GcManager();
        jvmManager.store(null);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Last timestamp not correct.", 0, jvmRun.getLastTimestamp());
    }
    
    public void testSameTimestampOrdering() {
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
        jvmDao.processBatch();

        // check they are the correct way around
        List<BlockingEvent> events = jvmDao.getBlockingEvents();
        Assert.assertTrue(events.get(1) instanceof ParNewEvent);
        Assert.assertTrue(events.get(2) instanceof SerialOldEvent);
    }      
}
