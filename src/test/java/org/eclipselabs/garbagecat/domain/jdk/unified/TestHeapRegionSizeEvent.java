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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestHeapRegionSizeEvent extends TestCase {

    public void testLine() {
        String logLine = "[0.003s][info][gc,heap] Heap region size: 1M";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + ".",
                HeapRegionSizeEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[0.003s][info][gc,heap] Heap region size: 1M";
        Assert.assertEquals(JdkUtil.LogEventType.HEAP_REGION_SIZE + "not identified.",
                JdkUtil.LogEventType.HEAP_REGION_SIZE, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.003s][info][gc,heap] Heap region size: 1M";
        Assert.assertTrue(JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof HeapRegionSizeEvent);
    }

    public void testNotBlocking() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        Assert.assertFalse(JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.HEAP_REGION_SIZE));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.HEAP_REGION_SIZE);
        Assert.assertTrue(JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testUptimeMillis() {
        String logLine = "[2019-02-05T14:47:31.091-0200][3ms] Regions: 2606 x 512K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_REGION_SIZE.toString() + ".",
                HeapRegionSizeEvent.match(logLine));
    }
}
