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
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestHeapAtGcEvent extends TestCase {

    public void testNotBlocking() {
        String logLine = "{Heap before gc invocations=1:";
        Assert.assertFalse(JdkUtil.LogEventType.HEAP_AT_GC.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testNotReportable() {
        String logLine = "{Heap before gc invocations=1:";
        Assert.assertFalse(JdkUtil.LogEventType.HEAP_AT_GC.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testHeapBeforeLowerCaseGcLine() {
        String logLine = "{Heap before gc invocations=1:";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testHeapBeforeUpperCaseGcFullLine() {
        String logLine = "{Heap before GC invocations=261 (full 10):";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testHeapAfterLowerCaseGcLine() {
        String logLine = "Heap after gc invocations=362:";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testPsYoungGenLine() {
        String logLine = " PSYoungGen      total 434880K, used 89473K [0x00002b3998b80000, "
                + "0x00002b39b70d0000, 0x00002b39b70d0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testEdenSpaceLine() {
        String logLine = "  eden space 372800K, 24% used [0x00002b3998b80000,"
                + "0x00002b399e2e0660,0x00002b39af790000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    /**
     * One space before percent.
     */
    public void testEdenSpaceLineOneSpace() {
        String logLine = "  eden space 372800K, 24% used [0x00002b3998b80000,"
                + "0x00002b399e2e0660,0x00002b39af790000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    /**
     * Two spaces before percent.
     */
    public void testEdenSpaceLineTwoSpaces() {
        String logLine = "  eden space 785024K,  39% used [0x00002aabdaab0000, "
                + "0x00002aabed98be48, 0x00002aac0a950000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testFromSpaceLine() {
        String logLine = "  from space 62080K, 0% used [0x00002b39b3430000," + "0x00002b39b3430000,0x00002b39b70d0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testToSpaceLine() {
        String logLine = "  to   space 62080K, 0% used [0x00002b39af790000," + "0x00002b39af790000,0x00002b39b3430000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testPsOldGenLine() {
        String logLine = " PSOldGen        total 993984K, used 0K [0x00002b395c0d0000, "
                + "0x00002b3998b80000, 0x00002b3998b80000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testParOldGenLine() {
        String logLine = " ParOldGen       total 1572864K, used 1380722K [0x00002b5dd6bd0000, "
                + "0x00002b5e36bd0000, 0x00002b5e36bd0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testObjectSpaceLine() {
        String logLine = "  object space 993984K, 0% used [0x00002b395c0d0000,"
                + "0x00002b395c0d0000,0x00002b3998b80000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testPsPermGenLine() {
        String logLine = " PSPermGen       total 524288K, used 13076K [0x00002b393c0d0000, "
                + "0x00002b395c0d0000, 0x00002b395c0d0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testBraceLine() {
        String logLine = "}";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testParNewGenerationLine() {
        String logLine = " par new generation   total 785728K, used 310127K [0x00002aabdaab0000, "
                + "0x00002aac0aab0000, 0x00002aac0aab0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testCmsGenerationLine() {
        String logLine = " concurrent mark-sweep generation total 3407872K, used 1640998K "
                + "[0x00002aac0aab0000, 0x00002aacdaab0000, 0x00002aacdaab0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testCmsPermGenLine() {
        String logLine = " concurrent-mark-sweep perm gen total 786432K, used 507386K "
                + "[0x00002aacdaab0000, 0x00002aad0aab0000, 0x00002aad0aab0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testHeapBeforePrintGCDateStampsLine() {
        String logLine = "{Heap before GC invocations=0 (full 0):";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testHeapAfterPrintGCDateStampsLine() {
        String logLine = "Heap after GC invocations=1 (full 0):";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testClassDataSharingLine() {
        String logLine = "No shared spaces configured.";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testTenuredGenerationLine() {
        String logLine = " tenured generation   total 704512K, used 17793K [0x00002aab2fab0000, "
                + "0x00002aab5aab0000, 0x00002aab7aab0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testCompactingPermGenLine() {
        String logLine = " compacting perm gen  total 262144K, used 65340K [0x00002aab7aab0000, "
                + "0x00002aab8aab0000, 0x00002aab8aab0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testTheSpaceLine() {
        String logLine = "   the space 704512K,   2% used [0x00002aab2fab0000, 0x00002aab30c107e8, "
                + "0x00002aab30c10800, 0x00002aab5aab0000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testG1HeapLine() {
        String logLine = "Heap";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testG1GarbageFirstHeapLine() {
        String logLine = " garbage-first heap   total 60416K, used 6685K [0x00007f9128c00000, 0x00007f912c700000, "
                + "0x00007f9162e00000";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testG1RegionSizeLine() {
        String logLine = "  region size 1024K, 6 young (6144K), 1 survivors (1024K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testG1TheSpaceLine() {
        String logLine = "  the space 20480K,  35% used [0x00007f9162e00000, 0x00007f9163526df0, 0x00007f9163526e00, "
                + "0x00007f9164200000)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testJDK8MetaspaceLine() {
        String logLine = " Metaspace       used 73096K, capacity 79546K, committed 79732K, reserved 1118208K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testMetaspaceLineWithDatestamp() {
        String logLine = "2017-03-21T15:06:10.427+1100:  Metaspace       used 625128K, capacity 943957K, "
                + "committed 951712K, reserved 1943552K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testJDK8ClassSpaceLine() {
        String logLine = "  class space    used 8643K, capacity 10553K, committed 10632K, reserved 1048576K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testClassSpaceLineWithTimestamp() {
        String logLine = "425018.340:   class space    used 37442K, capacity 57351K, committed 58624K, reserved "
                + "1048576K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }

    public void testDefNewGeneration() {
        String logLine = " def new generation   total 39680K, used 11177K [0x04800000, 0x07300000, 0x19d50000)K";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".",
                HeapAtGcEvent.match(logLine));
    }
}
