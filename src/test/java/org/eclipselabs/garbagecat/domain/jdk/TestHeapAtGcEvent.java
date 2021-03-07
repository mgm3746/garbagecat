/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestHeapAtGcEvent {

    @Test
    void testNotBlocking() {
        String logLine = "{Heap before gc invocations=1:";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.HEAP_AT_GC.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testNotReportable() {
        String logLine = "{Heap before gc invocations=1:";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.HEAP_AT_GC.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testHeapBeforeLowerCaseGcLine() {
        String logLine = "{Heap before gc invocations=1:";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testHeapBeforeUpperCaseGcFullLine() {
        String logLine = "{Heap before GC invocations=261 (full 10):";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testHeapAfterLowerCaseGcLine() {
        String logLine = "Heap after gc invocations=362:";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testPsYoungGenLine() {
        String logLine = " PSYoungGen      total 434880K, used 89473K [0x00002b3998b80000, "
                + "0x00002b39b70d0000, 0x00002b39b70d0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testEdenSpaceLine() {
        String logLine = "  eden space 372800K, 24% used [0x00002b3998b80000,"
                + "0x00002b399e2e0660,0x00002b39af790000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    /**
     * One space before percent.
     */
    @Test
    void testEdenSpaceLineOneSpace() {
        String logLine = "  eden space 372800K, 24% used [0x00002b3998b80000,"
                + "0x00002b399e2e0660,0x00002b39af790000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    /**
     * Two spaces before percent.
     */
    @Test
    void testEdenSpaceLineTwoSpaces() {
        String logLine = "  eden space 785024K,  39% used [0x00002aabdaab0000, "
                + "0x00002aabed98be48, 0x00002aac0a950000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testFromSpaceLine() {
        String logLine = "  from space 62080K, 0% used [0x00002b39b3430000," + "0x00002b39b3430000,0x00002b39b70d0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testToSpaceLine() {
        String logLine = "  to   space 62080K, 0% used [0x00002b39af790000," + "0x00002b39af790000,0x00002b39b3430000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testPsOldGenLine() {
        String logLine = " PSOldGen        total 993984K, used 0K [0x00002b395c0d0000, "
                + "0x00002b3998b80000, 0x00002b3998b80000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testParOldGenLine() {
        String logLine = " ParOldGen       total 1572864K, used 1380722K [0x00002b5dd6bd0000, "
                + "0x00002b5e36bd0000, 0x00002b5e36bd0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testObjectSpaceLine() {
        String logLine = "  object space 993984K, 0% used [0x00002b395c0d0000,"
                + "0x00002b395c0d0000,0x00002b3998b80000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testPsPermGenLine() {
        String logLine = " PSPermGen       total 524288K, used 13076K [0x00002b393c0d0000, "
                + "0x00002b395c0d0000, 0x00002b395c0d0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testBraceLine() {
        String logLine = "}";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testParNewGenerationLine() {
        String logLine = " par new generation   total 785728K, used 310127K [0x00002aabdaab0000, "
                + "0x00002aac0aab0000, 0x00002aac0aab0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testCmsGenerationLine() {
        String logLine = " concurrent mark-sweep generation total 3407872K, used 1640998K "
                + "[0x00002aac0aab0000, 0x00002aacdaab0000, 0x00002aacdaab0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testCmsPermGenLine() {
        String logLine = " concurrent-mark-sweep perm gen total 786432K, used 507386K "
                + "[0x00002aacdaab0000, 0x00002aad0aab0000, 0x00002aad0aab0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testHeapBeforePrintGCDateStampsLine() {
        String logLine = "{Heap before GC invocations=0 (full 0):";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testHeapAfterPrintGCDateStampsLine() {
        String logLine = "Heap after GC invocations=1 (full 0):";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testClassDataSharingLine() {
        String logLine = "No shared spaces configured.";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testTenuredGenerationLine() {
        String logLine = " tenured generation   total 704512K, used 17793K [0x00002aab2fab0000, "
                + "0x00002aab5aab0000, 0x00002aab7aab0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testCompactingPermGenLine() {
        String logLine = " compacting perm gen  total 262144K, used 65340K [0x00002aab7aab0000, "
                + "0x00002aab8aab0000, 0x00002aab8aab0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testTheSpaceLine() {
        String logLine = "   the space 704512K,   2% used [0x00002aab2fab0000, 0x00002aab30c107e8, "
                + "0x00002aab30c10800, 0x00002aab5aab0000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testG1HeapLine() {
        String logLine = "Heap";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testG1GarbageFirstHeapLine() {
        String logLine = " garbage-first heap   total 60416K, used 6685K [0x00007f9128c00000, 0x00007f912c700000, "
                + "0x00007f9162e00000";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testG1RegionSizeLine() {
        String logLine = "  region size 1024K, 6 young (6144K), 1 survivors (1024K)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testG1TheSpaceLine() {
        String logLine = "  the space 20480K,  35% used [0x00007f9162e00000, 0x00007f9163526df0, 0x00007f9163526e00, "
                + "0x00007f9164200000)";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testJDK8MetaspaceLine() {
        String logLine = " Metaspace       used 73096K, capacity 79546K, committed 79732K, reserved 1118208K";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testMetaspaceLineWithDatestamp() {
        String logLine = "2017-03-21T15:06:10.427+1100:  Metaspace       used 625128K, capacity 943957K, "
                + "committed 951712K, reserved 1943552K";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testJDK8ClassSpaceLine() {
        String logLine = "  class space    used 8643K, capacity 10553K, committed 10632K, reserved 1048576K";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testClassSpaceLineWithTimestamp() {
        String logLine = "425018.340:   class space    used 37442K, capacity 57351K, committed 58624K, reserved "
                + "1048576K";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }

    @Test
    void testDefNewGeneration() {
        String logLine = " def new generation   total 39680K, used 11177K [0x04800000, 0x07300000, 0x19d50000)K";
        assertTrue(HeapAtGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP_AT_GC.toString() + ".");
    }
}
