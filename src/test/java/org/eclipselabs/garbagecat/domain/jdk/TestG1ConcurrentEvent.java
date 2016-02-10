/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2012 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 ******************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author James Livingston
 */
public class TestG1ConcurrentEvent extends TestCase {
    public void testRootRegionScanStart() {
        String logLine = "50.101: [GC concurrent-root-region-scan-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent  event = new G1ConcurrentEvent (logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50101, event.getTimestamp());
    }

    public void testRootRegionScanEnd() {
        String logLine = "50.136: [GC concurrent-root-region-scan-end, 0.0346620]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent  event = new G1ConcurrentEvent (logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50136, event.getTimestamp());
    }

    public void testMarkStart() {
        String logLine = "50.136: [GC concurrent-mark-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent  event = new G1ConcurrentEvent (logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50136, event.getTimestamp());
    }

    public void testMarkEnd() {
        String logLine = "50.655: [GC concurrent-mark-end, 0.5186330 sec]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent  event = new G1ConcurrentEvent (logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50655, event.getTimestamp());
    }

    public void testCleanupStart() {
        String logLine = "50.685: [GC concurrent-cleanup-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent  event = new G1ConcurrentEvent (logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50685, event.getTimestamp());
    }

    public void testCleanupEnd() {
        String logLine = "50.685: [GC concurrent-cleanup-end, 0.0001080]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent  event = new G1ConcurrentEvent (logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 50685, event.getTimestamp());
    }
    
    public void testDateStamp(){
        String logLine = "2016-02-09T06:22:10.399-0500: 28039.161: [GC concurrent-root-region-scan-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent  event = new G1ConcurrentEvent (logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 28039161, event.getTimestamp());
    }
    
    public void testPreprocessed(){
        String logLine = "27744.494: [GC concurrent-mark-start], 0.3349320 secs] 10854M->9765M(26624M) [Times: user=0.98 sys=0.00, real=0.33 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
        G1ConcurrentEvent  event = new G1ConcurrentEvent (logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27744494, event.getTimestamp());
    }
}
