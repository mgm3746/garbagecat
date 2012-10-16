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
    }

    public void testRootRegionScanEnd() {
        String logLine = "50.136: [GC concurrent-root-region-scan-end, 0.0346620]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
    }

    public void testMarkStart() {
        String logLine = "50.136: [GC concurrent-mark-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
    }

    public void testMarkEnd() {
        String logLine = "50.655: [GC concurrent-mark-end, 0.5186330 sec]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
    }

    public void testCleanupStart() {
        String logLine = "50.685: [GC concurrent-cleanup-start]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
    }

    public void testCleanupEnd() {
        String logLine = "50.685: [GC concurrent-cleanup-end, 0.0001080]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".", G1ConcurrentEvent.match(logLine));
    }
}
