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
public class TestFooterHeapEvent extends TestCase {

    public void testLineHeap() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        Assert.assertEquals(JdkUtil.LogEventType.FOOTER_HEAP + "not identified.", JdkUtil.LogEventType.FOOTER_HEAP,
                JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        Assert.assertTrue(JdkUtil.LogEventType.FOOTER_HEAP.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof FooterHeapEvent);
    }

    public void testNotBlocking() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        Assert.assertFalse(JdkUtil.LogEventType.FOOTER_HEAP.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(JdkUtil.LogEventType.FOOTER_HEAP.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.FOOTER_HEAP));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.FOOTER_HEAP);
        Assert.assertTrue(JdkUtil.LogEventType.FOOTER_HEAP.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineGarbageFirst() {
        String logLine = "[25.016s][info][gc,heap,exit  ]  garbage-first heap   total 59392K, used 38015K "
                + "[0x00000000fc000000, 0x0000000100000000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineRegion() {
        String logLine = "[25.016s][info][gc,heap,exit  ]   region size 1024K, 13 young (13312K), 1 survivors (1024K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineMetaspace() {
        String logLine = "[25.016s][info][gc,heap,exit  ]  Metaspace       used 11079K, capacity 11287K, "
                + "committed 11520K, reserved 1060864K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineClass() {
        String logLine = "[25.016s][info][gc,heap,exit  ]   class space    used 909K, capacity 995K, committed 1024K, "
                + "reserved 1048576K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }
}
