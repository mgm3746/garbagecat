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
    void testClassSpaceAfterExitUnified() {
        String logLine = "[69.946s][info][gc,heap,exit ]   class space    used 299K, capacity 637K, committed 640K, "
                + "reserved 1048576K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceJdk17Unified() {
        String logLine = "[1.656s][info][gc,heap,exit   ]   class space    used 317K, committed 384K, reserved "
                + "1048576K";
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
    void testClassSpaceUnified() {
        String logLine = "[25.016s][info][gc,heap,exit  ]   class space    used 909K, capacity 995K, committed 1024K, "
                + "reserved 1048576K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]   class space    used 10193K, capacity 13027K, "
                + "committed 13056K, reserved 253952K";
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
    void testCollectionSetUnified() {
        String logLine = "[103.682s][info][gc,heap,exit ] Collection set:";
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
    void testDefNewUnified() {
        String logLine = "[32.839s][info][gc,heap,exit   ]  def new generation   total 11456K, used 4604K "
                + "[0x00000000fc000000, 0x00000000fcc60000, 0x00000000fd550000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testEdenSpace() {
        String logLine = "  eden space 193024K, 28% used [0x0000000719d00000,0x000000071d1e5300,0x0000000725980000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testEdenSpaceNoSpacesBetweenAddressesUnified() {
        String logLine = "[37.098s][info][gc,heap,exit   ]   eden space 20480K, 33% used [0x00000000feb00000,"
                + "0x00000000ff1cb940,0x00000000fff00000)";
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
    void testEdenSpaceUnified() {
        String logLine = "[32.839s][info][gc,heap,exit   ]   eden space 10240K,  43% used [0x00000000fc000000, "
                + "0x00000000fc463ed8, 0x00000000fca00000)";
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
    void testFromSpaceUnified() {
        String logLine = "[32.839s][info][gc,heap,exit   ]   from space 1216K,   8% used [0x00000000fca00000, "
                + "0x00000000fca1b280, 0x00000000fcb30000)";
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
    void testGarbageFirstHeapUnified() {
        String logLine = "[25.016s][info][gc,heap,exit  ]  garbage-first heap   total 59392K, used 38015K "
                + "[0x00000000fc000000, 0x0000000100000000)";
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
    void testHeapUnified() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testHeapUnified1SpaceAfterExit() {
        String logLine = "[69.946s][info][gc,heap,exit ] Heap";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testHeapUnified3SpacesAfterExit() {
        String logLine = "[32.839s][info][gc,heap,exit   ] Heap";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testHeapUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Heap";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertEquals(JdkUtil.LogEventType.HEAP, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.HEAP + "not identified.");
    }

    @Test
    void testMapBiased() {
        String logLine = " - map (biased):  0x00007f271b2e2000";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testMapBiasedUnified() {
        String logLine = "[103.683s][info][gc,heap,exit ]  - map (biased):  0x00007fa7ea116000";
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
    void testMapVanillaUnified() {
        String logLine = "[103.683s][info][gc,heap,exit ]  - map (vanilla): 0x00007fa7ea119f00";
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
    void testMetaspaceUnified() {
        String logLine = "[25.016s][info][gc,heap,exit  ]  Metaspace       used 11079K, capacity 11287K, "
                + "committed 11520K, reserved 1060864K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceUnified1SpaceAfterExit() {
        String logLine = "[69.946s][info][gc,heap,exit ]  Metaspace       used 4066K, capacity 7271K, committed "
                + "7296K, reserved 1056768K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceUnified3SpacesAfterExit() {
        String logLine = "[32.839s][info][gc,heap,exit   ]  Metaspace       used 4109K, capacity 7271K, committed "
                + "7296K, reserved 1056768K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceUnifiedJdk17() {
        String logLine = "[1.656s][info][gc,heap,exit   ]  Metaspace       used 3990K, committed 4160K, "
                + "reserved 1056768K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  Metaspace       used 80841K, capacity 89293K, "
                + "committed 89600K, reserved 331776K";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.HEAP.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testObjectSpace() {
        String logLine = "  object space 341504K, 27% used [0x00000005cd600000,0x00000005d322aa70,0x00000005e2380000)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.HEAP.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testObjectSpaceUnified() {
        String logLine = "[37.098s][info][gc,heap,exit   ]   object space 32768K, 83% used [0x00000000fc000000,"
                + "0x00000000fda99f58,0x00000000fe000000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testParCmsGenerationUnified() {
        String logLine = "[59.713s][info][gc,heap,exit ]  concurrent mark-sweep generation total 31228K, used 25431K "
                + "[0x00000000fd550000, 0x00000000ff3cf000, 0x0000000100000000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testParNewGeneration() {
        String logLine = " par new generation   total 785728K, used 310127K [0x00002aabdaab0000, "
                + "0x00002aac0aab0000, 0x00002aac0aab0000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testParNewGenerationUnified() {
        String logLine = "[59.713s][info][gc,heap,exit ]  par new generation   total 1152K, used 713K "
                + "[0x00000000fc000000, 0x00000000fc140000, 0x00000000fd550000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testParOldGen() {
        String logLine = " ParOldGen       total 341504K, used 94378K [0x00000005cd600000, 0x00000005e2380000, "
                + "0x0000000719d00000)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
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
    void testParOldGenUnified() {
        String logLine = "[37.742s][info][gc,heap,exit   ]  ParOldGen       total 30720K, used 27745K "
                + "[0x00000000fc000000, 0x00000000fde00000, 0x00000000feb00000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof HeapEvent,
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
    void testPsOldGenUnified() {
        String logLine = "[37.098s][info][gc,heap,exit   ]  PSOldGen        total 32768K, used 27239K "
                + "[0x00000000fc000000, 0x00000000fe000000, 0x00000000feb00000)";
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
    void testPsYoungGenUnified() {
        String logLine = "[37.098s][info][gc,heap,exit   ]  PSYoungGen      total 20992K, used 7054K "
                + "[0x00000000feb00000, 0x0000000100000000, 0x0000000100000000)";
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
    void testRegionUnified() {
        String logLine = "[25.016s][info][gc,heap,exit  ]   region size 1024K, 13 young (13312K), 1 survivors (1024K)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testRegionUnified3DigitYoung2DigutSurvivors() {
        String logLine = "[2020-03-12T13:13:49.821-0400][26578ms]   region size 1024K, 260 young (266240K), 26 "
                + "survivors (26624K)";
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
    void testReservedRegionAddressUnified() {
        String logLine = "[69.946s][info][gc,heap,exit ]  - [0x00000000fc000000, 0x0000000100000000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testReservedRegionAddressUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  - [0x00000000ae900000, 0x0000000100000000) ";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testReservedRegionUnified() {
        String logLine = "[69.946s][info][gc,heap,exit ] Reserved region:";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testReservedRegionUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Reserved region:";
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
    void testShenandoahRegionsUnified() {
        String logLine = "[69.946s][info][gc,heap,exit ]  256 x 256K regions";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testShenandoahRegionsUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  2606 x 512K regions";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testShenandoahSoftMax() {
        String logLine = "[2021-01-25T17:44:28.636-0500]  98304K max, 98304K soft max, 98304K committed, 58219K used";
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
    void testShenandoahTotalCommittedUsedUnified() {
        String logLine = "[69.946s][info][gc,heap,exit ]  65536K total, 65536K committed, 55031K used";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testShenandoahTotalCommittedUsedUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  1334272K total, 107008K committed, 80727K used";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testShenandoahUnified() {
        String logLine = "[69.946s][info][gc,heap,exit ] Shenandoah Heap";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testShenandoahUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Shenandoah Heap";
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
    void testStatusHasForwardedEvacuatingUnified() {
        String logLine = "[2022-05-20T11:20:57.559-0400] Status: has forwarded objects, evacuating, concurrent weak "
                + "roots, cancelled";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testStatusHasForwardedUnified() {
        String logLine = "[103.682s][info][gc,heap,exit ] Status: has forwarded objects, cancelled";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testStatusMarking() {
        String logLine = "[4.421s][info][gc,heap,exit  ] Status: marking, cancelled";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testStatusUnified() {
        String logLine = "[69.946s][info][gc,heap,exit ] Status: cancelled";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testStatusUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Status: cancelled";
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
    void testTenuredUnified() {
        String logLine = "[32.839s][info][gc,heap,exit   ]  tenured generation   total 25240K, used 24218K "
                + "[0x00000000fd550000, 0x00000000fedf6000, 0x0000000100000000)";
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
    void testTheSpaceUnified() {
        String logLine = "[32.839s][info][gc,heap,exit   ]    the space 25240K,  95% used [0x00000000fd550000, "
                + "0x00000000fecf6b58, 0x00000000fecf6c00, 0x00000000fedf6000)";
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
    void testToSpaceUnified() {
        String logLine = "[32.839s][info][gc,heap,exit   ]   to   space 1216K,   0% used [0x00000000fcb30000, "
                + "0x00000000fcb30000, 0x00000000fcc60000)";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<>();
        eventTypes.add(LogEventType.HEAP);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.HEAP.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testZHeap() {
        String logLine = "[2.640s]  ZHeap           used 86M, capacity 96M, max capacity 96M";
        assertTrue(HeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEAP.toString() + ".");
    }
}
