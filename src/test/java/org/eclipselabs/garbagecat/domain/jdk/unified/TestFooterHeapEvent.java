/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
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

    public void testLineHeapUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Heap";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineHeap1SpaceAfterExit() {
        String logLine = "[69.946s][info][gc,heap,exit ] Heap";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineHeap3SpacesAfterExit() {
        String logLine = "[32.839s][info][gc,heap,exit   ] Heap";
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

    public void testLineShenandoah() {
        String logLine = "[69.946s][info][gc,heap,exit ] Shenandoah Heap";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineShenandoahUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Shenandoah Heap";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineShenandoahTotalCommittedUsed() {
        String logLine = "[69.946s][info][gc,heap,exit ]  65536K total, 65536K committed, 55031K used";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineShenandoahTotalCommittedUsedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  1334272K total, 107008K committed, 80727K used";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineShenandoahRegions() {
        String logLine = "[69.946s][info][gc,heap,exit ]  256 x 256K regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineShenandoahRegionsUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  2606 x 512K regions";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineStatus() {
        String logLine = "[69.946s][info][gc,heap,exit ] Status: cancelled";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineStatusUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Status: cancelled";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineReservedRegion() {
        String logLine = "[69.946s][info][gc,heap,exit ] Reserved region:";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineReservedRegionUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Reserved region:";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineReservedRegionAddress() {
        String logLine = "[69.946s][info][gc,heap,exit ]  - [0x00000000fc000000, 0x0000000100000000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineReservedRegionAddressUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  - [0x00000000ae900000, 0x0000000100000000) ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineBlank() {
        String logLine = "[69.946s][info][gc,heap,exit ]";
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

    public void testLineMetaspaceUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  Metaspace       used 80841K, capacity 89293K, "
                + "committed 89600K, reserved 331776K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineMetaspace1SpaceAfterExit() {
        String logLine = "[69.946s][info][gc,heap,exit ]  Metaspace       used 4066K, capacity 7271K, committed "
                + "7296K, reserved 1056768K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineMetaspace3SpacesAfterExit() {
        String logLine = "[32.839s][info][gc,heap,exit   ]  Metaspace       used 4109K, capacity 7271K, committed "
                + "7296K, reserved 1056768K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineClass() {
        String logLine = "[25.016s][info][gc,heap,exit  ]   class space    used 909K, capacity 995K, committed 1024K, "
                + "reserved 1048576K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineClassUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]   class space    used 10193K, capacity 13027K, "
                + "committed 13056K, reserved 253952K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineClass1SpaceAfterExit() {
        String logLine = "[69.946s][info][gc,heap,exit ]   class space    used 299K, capacity 637K, committed 640K, "
                + "reserved 1048576K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testLineDefNew() {
        String logLine = "[32.839s][info][gc,heap,exit   ]  def new generation   total 11456K, used 4604K "
                + "[0x00000000fc000000, 0x00000000fcc60000, 0x00000000fd550000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testEden() {
        String logLine = "[32.839s][info][gc,heap,exit   ]   eden space 10240K,  43% used [0x00000000fc000000, "
                + "0x00000000fc463ed8, 0x00000000fca00000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testFromSpace() {
        String logLine = "[32.839s][info][gc,heap,exit   ]   from space 1216K,   8% used [0x00000000fca00000, "
                + "0x00000000fca1b280, 0x00000000fcb30000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testToSpace() {
        String logLine = "[32.839s][info][gc,heap,exit   ]   to   space 1216K,   0% used [0x00000000fcb30000, "
                + "0x00000000fcb30000, 0x00000000fcc60000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testTenured() {
        String logLine = "[32.839s][info][gc,heap,exit   ]  tenured generation   total 25240K, used 24218K "
                + "[0x00000000fd550000, 0x00000000fedf6000, 0x0000000100000000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }

    public void testTheSpace() {
        String logLine = "[32.839s][info][gc,heap,exit   ]    the space 25240K,  95% used [0x00000000fd550000, "
                + "0x00000000fecf6b58, 0x00000000fecf6c00, 0x00000000fedf6000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.FOOTER_HEAP.toString() + ".",
                FooterHeapEvent.match(logLine));
    }
}
