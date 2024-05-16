/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
class TestHeapEvent {

    @Test
    void testClassDataSharingLine() {
        String logLine = "No shared spaces configured.";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceLine() {
        String logLine = "  class space    used 8643K, capacity 10553K, committed 10632K, reserved 1048576K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceWithTimestamp() {
        String logLine = "425018.340:   class space    used 37442K, capacity 57351K, committed 58624K, reserved "
                + "1048576K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testCmsGeneration() {
        String logLine = " concurrent mark-sweep generation total 3407872K, used 1640998K "
                + "[0x00002aac0aab0000, 0x00002aacdaab0000, 0x00002aacdaab0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testCmsPermGen() {
        String logLine = " concurrent-mark-sweep perm gen total 786432K, used 507386K "
                + "[0x00002aacdaab0000, 0x00002aad0aab0000, 0x00002aad0aab0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testCollection() {
        String logLine = "Collection set:";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testCompactingPermGen() {
        String logLine = " compacting perm gen  total 262144K, used 65340K [0x00002aab7aab0000, "
                + "0x00002aab8aab0000, 0x00002aab8aab0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testDefNewGeneration() {
        String logLine = " def new generation   total 39680K, used 11177K [0x04800000, 0x07300000, 0x19d50000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testEdenSpace() {
        String logLine = "  eden space 193024K, 28% used [0x0000000719d00000,0x000000071d1e5300,0x0000000725980000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    /**
     * One space before percent.
     */
    @Test
    void testEdenSpaceOneSpace() {
        String logLine = "  eden space 372800K, 24% used [0x00002b3998b80000,0x00002b399e2e0660,0x00002b39af790000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    /**
     * Two spaces before percent.
     */
    @Test
    void testEdenSpaceTwoSpaces() {
        String logLine = "  eden space 785024K,  39% used [0x00002aabdaab0000, "
                + "0x00002aabed98be48, 0x00002aac0a950000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testFromSpace() {
        String logLine = "  from space 62080K, 0% used [0x00002b39b3430000," + "0x00002b39b3430000,0x00002b39b70d0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testGarbageFirstHeap() {
        String logLine = " garbage-first heap   total 60416K, used 6685K [0x00007f9128c00000, 0x00007f912c700000, "
                + "0x00007f9162e00000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testHeap() {
        String logLine = "Heap";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "Heap";
        assertEquals(JdkUtil.LogEventType.HEAP, JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.HEAP + "not identified.");
    }

    @Test
    void testMapBiased() {
        String logLine = " - map (biased):  0x00007f271b2e2000";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testMapVanilla() {
        String logLine = " - map (vanilla): 0x00007f271b2e5e00";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testMetaspace() {
        String logLine = " Metaspace       used 73096K, capacity 79546K, committed 79732K, reserved 1118208K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceDatestamp() {
        String logLine = "2017-03-21T15:06:10.427+1100:  Metaspace       used 625128K, capacity 943957K, "
                + "committed 951712K, reserved 1943552K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceDatestampTime() {
        String logLine = "2017-03-21T15:06:10.427+1100: 123.456:  Metaspace       used 625128K, capacity 943957K, "
                + "committed 951712K, reserved 1943552K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "Heap";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.HEAP.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testObjectSpace() {
        String logLine = "  object space 341504K, 27% used [0x00000005cd600000,0x00000005d322aa70,0x00000005e2380000)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.HEAP.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParNewGeneration() {
        String logLine = " par new generation   total 785728K, used 310127K [0x00002aabdaab0000, "
                + "0x00002aac0aab0000, 0x00002aac0aab0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testParOldGen() {
        String logLine = " ParOldGen       total 341504K, used 94378K [0x00000005cd600000, 0x00000005e2380000, "
                + "0x0000000719d00000)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.HEAP.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParOldGenLine() {
        String logLine = " ParOldGen       total 1572864K, used 1380722K [0x00002b5dd6bd0000, "
                + "0x00002b5e36bd0000, 0x00002b5e36bd0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "Heap";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof HeapEvent,
                JdkUtil.LogEventType.HEAP.toString() + " not parsed.");
    }

    @Test
    void testPsOldGen() {
        String logLine = " PSOldGen        total 993984K, used 0K [0x00002b395c0d0000, "
                + "0x00002b3998b80000, 0x00002b3998b80000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testPsPermGen() {
        String logLine = " PSPermGen       total 524288K, used 13076K [0x00002b393c0d0000, "
                + "0x00002b395c0d0000, 0x00002b395c0d0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testPsYoungGen() {
        String logLine = " PSYoungGen      total 193536K, used 54452K [0x0000000719d00000, 0x0000000755900000, "
                + "0x00000007c0000000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testRegionSize() {
        String logLine = "  region size 1024K, 6 young (6144K), 1 survivors (1024K)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testRegionSize4Digit() {
        String logLine = "  region size 8192K, 1702 young (13942784K), 10 survivors (81920K)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.HEAP),
                JdkUtil.LogEventType.HEAP.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testReservedRegion() {
        String logLine = "Reserved region:";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testReservedRegionAddress() {
        String logLine = " - [0x00000000f8000000, 0x0000000100000000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testShenandoahRegions() {
        String logLine = " 512 x 256K regions";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testShenandoahSoftMax() {
        String logLine = " 128M max, 128M soft max, 126M committed, 35271K used";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testShenandoahTotalCommittedUsed() {
        String logLine = " 128M total, 128M committed, 102M used";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testStatus() {
        String logLine = "Status: has forwarded objects, updating refs, cancelled";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testStatusHasForwarded() {
        String logLine = "Status: has forwarded objects, cancelled";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testTenuredGeneration() {
        String logLine = " tenured generation   total 704512K, used 17793K [0x00002aab2fab0000, "
                + "0x00002aab5aab0000, 0x00002aab7aab0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testTheSpace() {
        String logLine = "   the space 704512K,   2% used [0x00002aab2fab0000, 0x00002aab30c107e8, "
                + "0x00002aab30c10800, 0x00002aab5aab0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testTo() {
        String logLine = "  to   space 512K, 0% used [0x0000000755800000,0x0000000755800000,0x0000000755880000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testToSpace() {
        String logLine = "  to   space 62080K, 0% used [0x00002b39af790000," + "0x00002b39af790000,0x00002b39b3430000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.HEAP);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.HEAP.toString() + " incorrectly indentified as unified.");
    }
}