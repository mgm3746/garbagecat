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
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1CleanupEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "2972.698: [GC cleanup 13G->12G(30G), 0.0358748 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.G1_CLEANUP.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testCleanup() {
        String logLine = "18.650: [GC cleanup 297M->236M(512M), 0.0014690 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".",
                G1CleanupEvent.match(logLine));
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 18650, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 304128, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 241664, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 524288, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1469, event.getDuration());
    }

    public void testCleanupWhiteSpacesAtEnd() {
        String logLine = "18.650: [GC cleanup 297M->236M(512M), 0.0014690 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".",
                G1CleanupEvent.match(logLine));
    }

    public void testLogLineGigabytes() {
        String logLine = "2972.698: [GC cleanup 13G->12G(30G), 0.0358748 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".",
                G1CleanupEvent.match(logLine));
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2972698, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 13631488, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 12582912, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 35874, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "2016-11-08T09:36:22.388-0800: 35290.131: [GC cleanup 5252M->3592M(12G), 0.0154490 secs] "
                + "[Times: user=0.19 sys=0.00, real=0.01 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".",
                G1CleanupEvent.match(logLine));
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 35290131, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 5252 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 3592 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 12 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 15449, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 19, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 1900, event.getParallelism());
    }

    public void testLogLineMissingSizes() {
        String logLine = "2017-05-09T00:46:14.766+1000: 288368.997: [GC cleanup, 0.0000910 secs] "
                + "[Times: user=0.00 sys=0.00, real=0.00 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".",
                G1CleanupEvent.match(logLine));
        G1CleanupEvent event = new G1CleanupEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 288368997, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 0, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 0, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 0, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 91, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 0, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 0, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }
}
