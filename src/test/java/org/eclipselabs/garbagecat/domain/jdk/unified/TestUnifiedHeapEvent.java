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
package org.eclipselabs.garbagecat.domain.jdk.unified;

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
class TestUnifiedHeapEvent {

    @Test
    void testClassSpace() {
        String logLine = "[25.016s][info][gc,heap,exit  ]   class space    used 909K, capacity 995K, committed 1024K, "
                + "reserved 1048576K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceAfterExit() {
        String logLine = "[69.946s][info][gc,heap,exit ]   class space    used 299K, capacity 637K, committed 640K, "
                + "reserved 1048576K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceJdk17() {
        String logLine = "[1.656s][info][gc,heap,exit   ]   class space    used 317K, committed 384K, reserved "
                + "1048576K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]   class space    used 10193K, capacity 13027K, "
                + "committed 13056K, reserved 253952K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceZGenerationalMajor() {
        String logLine = "[65.488s][debug][gc,heap         ] GC(0) Y:   class space    used 523K, committed 640K, "
                + "reserved 1048576K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceZGenerationalMinor() {
        String logLine = "[2994.846s][debug][gc,heap         ] GC(8) y:   class space    used 5444K, committed 6272K, "
                + "reserved 1048576K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testClassSpaceZGenerationalOld() {
        String logLine = "[66.259s][debug][gc,heap         ] GC(0) O:   class space    used 523K, committed 640K, "
                + "reserved 1048576K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testCollectionSet() {
        String logLine = "[103.682s][info][gc,heap,exit ] Collection set:";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testDefNew() {
        String logLine = "[32.839s][info][gc,heap,exit   ]  def new generation   total 11456K, used 4604K "
                + "[0x00000000fc000000, 0x00000000fcc60000, 0x00000000fd550000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testEdenSpace() {
        String logLine = "[32.839s][info][gc,heap,exit   ]   eden space 10240K,  43% used [0x00000000fc000000, "
                + "0x00000000fc463ed8, 0x00000000fca00000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testEdenSpaceNoSpacesBetweenAddresses() {
        String logLine = "[37.098s][info][gc,heap,exit   ]   eden space 20480K, 33% used [0x00000000feb00000,"
                + "0x00000000ff1cb940,0x00000000fff00000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testFromSpace() {
        String logLine = "[32.839s][info][gc,heap,exit   ]   from space 1216K,   8% used [0x00000000fca00000, "
                + "0x00000000fca1b280, 0x00000000fcb30000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testGarbageFirstHeap() {
        String logLine = "[25.016s][info][gc,heap,exit  ]  garbage-first heap   total 59392K, used 38015K "
                + "[0x00000000fc000000, 0x0000000100000000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testGarbageFirstHeapHuge() {
        String logLine = "[2023-02-20T18:09:02.299+0200][info][gc,heap,exit  ]  garbage-first heap   total "
                + "4823449600K, used 4285038591K [0x00007ae490000000, 0x00007f6290000000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testHeap() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testHeap1SpaceAfterExit() {
        String logLine = "[69.946s][info][gc,heap,exit ] Heap";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testHeap3SpacesAfterExit() {
        String logLine = "[32.839s][info][gc,heap,exit   ] Heap";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testHeapUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Heap";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertEquals(JdkUtil.LogEventType.UNIFIED_HEAP,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.LogEventType.UNIFIED_HEAP + "not identified.");
    }

    @Test
    void testMapBiased() {
        String logLine = "[103.683s][info][gc,heap,exit ]  - map (biased):  0x00007fa7ea116000";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testMapVanilla() {
        String logLine = "[103.683s][info][gc,heap,exit ]  - map (vanilla): 0x00007fa7ea119f00";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testMetaspace() {
        String logLine = "[25.016s][info][gc,heap,exit  ]  Metaspace       used 11079K, capacity 11287K, "
                + "committed 11520K, reserved 1060864K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testMetaspace1SpaceAfterExit() {
        String logLine = "[69.946s][info][gc,heap,exit ]  Metaspace       used 4066K, capacity 7271K, committed "
                + "7296K, reserved 1056768K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testMetaspace3SpacesAfterExit() {
        String logLine = "[32.839s][info][gc,heap,exit   ]  Metaspace       used 4109K, capacity 7271K, committed "
                + "7296K, reserved 1056768K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceJdk17() {
        String logLine = "[1.656s][info][gc,heap,exit   ]  Metaspace       used 3990K, committed 4160K, "
                + "reserved 1056768K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  Metaspace       used 80841K, capacity 89293K, "
                + "committed 89600K, reserved 331776K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceZGenerationalOld() {
        String logLine = "[66.259s][debug][gc,heap         ] GC(0) O:  Metaspace       used 7507K, committed 7808K, "
                + "reserved 1114112K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceZGenerationalYoungMajor() {
        String logLine = "[65.488s][debug][gc,heap         ] GC(0) Y:  Metaspace       used 7505K, committed 7808K, "
                + "reserved 1114112K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testMetaspaceZGenerationalYoungMinor() {
        String logLine = "[2994.846s][debug][gc,heap         ] GC(8) y:  Metaspace       used 55637K, committed "
                + "57280K, reserved 1114112K";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.LogEventType.UNIFIED_HEAP.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testObjectSpace() {
        String logLine = "[37.098s][info][gc,heap,exit   ]   object space 32768K, 83% used [0x00000000fc000000,"
                + "0x00000000fda99f58,0x00000000fe000000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testParCmsGeneration() {
        String logLine = "[59.713s][info][gc,heap,exit ]  concurrent mark-sweep generation total 31228K, used 25431K "
                + "[0x00000000fd550000, 0x00000000ff3cf000, 0x0000000100000000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testParNewGeneration() {
        String logLine = "[59.713s][info][gc,heap,exit ]  par new generation   total 1152K, used 713K "
                + "[0x00000000fc000000, 0x00000000fc140000, 0x00000000fd550000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testParOldGen() {
        String logLine = "[37.742s][info][gc,heap,exit   ]  ParOldGen       total 30720K, used 27745K "
                + "[0x00000000fc000000, 0x00000000fde00000, 0x00000000feb00000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[25.016s][info][gc,heap,exit  ] Heap";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedHeapEvent,
                JdkUtil.LogEventType.UNIFIED_HEAP.toString() + " not parsed.");
    }

    @Test
    void testPsOldGen() {
        String logLine = "[37.098s][info][gc,heap,exit   ]  PSOldGen        total 32768K, used 27239K "
                + "[0x00000000fc000000, 0x00000000fe000000, 0x00000000feb00000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testPsYoung() {
        String logLine = "[37.098s][info][gc,heap,exit   ]  PSYoungGen      total 20992K, used 7054K "
                + "[0x00000000feb00000, 0x0000000100000000, 0x0000000100000000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testRegion() {
        String logLine = "[25.016s][info][gc,heap,exit  ]   region size 1024K, 13 young (13312K), 1 survivors (1024K)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testRegion3DigitYoung2DigutSurvivors() {
        String logLine = "[2020-03-12T13:13:49.821-0400][26578ms]   region size 1024K, 260 young (266240K), 26 "
                + "survivors (26624K)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testRegionHuge() {
        String logLine = "[2023-02-20T18:09:02.299+0200][info][gc,heap,exit  ]   region size 32768K, 3212 young "
                + "(105250816K), 61 survivors (1998848K)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_HEAP),
                JdkUtil.LogEventType.UNIFIED_HEAP.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testReservedRegion() {
        String logLine = "[69.946s][info][gc,heap,exit ] Reserved region:";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testReservedRegionAddress() {
        String logLine = "[69.946s][info][gc,heap,exit ]  - [0x00000000fc000000, 0x0000000100000000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testReservedRegionAddressUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  - [0x00000000ae900000, 0x0000000100000000) ";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testReservedRegionUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Reserved region:";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testShenandoah() {
        String logLine = "[69.946s][info][gc,heap,exit ] Shenandoah Heap";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testShenandoahRegions() {
        String logLine = "[69.946s][info][gc,heap,exit ]  256 x 256K regions";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testShenandoahRegionsUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  2606 x 512K regions";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testShenandoahSoftMax() {
        String logLine = "[2021-01-25T17:44:28.636-0500]  98304K max, 98304K soft max, 98304K committed, 58219K used";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testShenandoahTotalCommittedUsed() {
        String logLine = "[69.946s][info][gc,heap,exit ]  65536K total, 65536K committed, 55031K used";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testShenandoahTotalCommittedUsedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]  1334272K total, 107008K committed, 80727K used";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testShenandoahUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Shenandoah Heap";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testStatus() {
        String logLine = "[69.946s][info][gc,heap,exit ] Status: cancelled";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testStatusHasForwarded() {
        String logLine = "[103.682s][info][gc,heap,exit ] Status: has forwarded objects, cancelled";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testStatusHasForwardedEvacuating() {
        String logLine = "[2022-05-20T11:20:57.559-0400] Status: has forwarded objects, evacuating, concurrent weak "
                + "roots, cancelled";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testStatusMarking() {
        String logLine = "[4.421s][info][gc,heap,exit  ] Status: marking, cancelled";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testStatusUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms] Status: cancelled";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testTenured() {
        String logLine = "[32.839s][info][gc,heap,exit   ]  tenured generation   total 25240K, used 24218K "
                + "[0x00000000fd550000, 0x00000000fedf6000, 0x0000000100000000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testTheSpace() {
        String logLine = "[32.839s][info][gc,heap,exit   ]    the space 25240K,  95% used [0x00000000fd550000, "
                + "0x00000000fecf6b58, 0x00000000fecf6c00, 0x00000000fedf6000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testToSpace() {
        String logLine = "[32.839s][info][gc,heap,exit   ]   to   space 1216K,   0% used [0x00000000fcb30000, "
                + "0x00000000fcb30000, 0x00000000fcc60000)";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<>();
        eventTypes.add(LogEventType.UNIFIED_HEAP);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.UNIFIED_HEAP.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testZHeap() {
        String logLine = "[2.640s]  ZHeap           used 86M, capacity 96M, max capacity 96M";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testZHeapGenerationalOld() {
        String logLine = "[66.259s][debug][gc,heap         ] GC(0) O:  ZHeap           used 2228M, capacity 27648M, "
                + "max capacity 27648M";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testZHeapGenerationalYoungMajor() {
        String logLine = "[65.488s][debug][gc,heap         ] GC(0) Y:  ZHeap           used 2768M, capacity 27648M, "
                + "max capacity 27648M";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }

    @Test
    void testZHeapGenerationalYoungMinor() {
        String logLine = "[2994.846s][debug][gc,heap         ] GC(8) y:  ZHeap           used 26230M, capacity 27648M, "
                + "max capacity 27648M";
        assertTrue(UnifiedHeapEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_HEAP.toString() + ".");
    }
}
