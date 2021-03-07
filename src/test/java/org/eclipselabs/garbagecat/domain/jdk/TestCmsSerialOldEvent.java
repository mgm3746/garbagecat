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

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.eclipselabs.garbagecat.TestUtil;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestCmsSerialOldEvent {

    @Test
    void testIsBlocking() {
        String logLine = "262372.344: [Full GC (JvmtiEnv ForceGarbageCollection) 262372.344: [CMS "
                + "(concurrent mode interrupted): 49392K->48780K(1756416K), 0.2620228 secs] "
                + "49392K->48780K(2063104K), [Metaspace: 256552K->256552K(1230848K)], 0.2624794 secs] "
                + "[Times: user=0.26 sys=0.00, real=0.27 secs]";
        assertTrue(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " not indentified as blocking.");
    }

    @Test
    void testLogLine() {
        String logLine = "5.980: [Full GC 5.980: "
                + "[CMS: 5589K->5796K(122880K), 0.0889610 secs] 11695K->5796K(131072K), "
                + "[CMS Perm : 13140K->13124K(131072K)], 0.0891270 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertEquals((long) 5980,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(6106),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(0),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(8192),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(5589),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(5796),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(122880),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(13140),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(13124),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(131072),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(89127,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineWhitespaceAtEnd() {
        String logLine = "5.980: [Full GC 5.980: "
                + "[CMS: 5589K->5796K(122880K), 0.0889610 secs] 11695K->5796K(131072K), "
                + "[CMS Perm : 13140K->13124K(131072K)], 0.0891270 secs] ";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
    }

    @Test
    void testLogLineJdk16WithTrigger() {
        String logLine = "2.425: [Full GC (System) 2.425: "
                + "[CMS: 1231K->2846K(114688K), 0.0827010 secs] 8793K->2846K(129472K), "
                + "[CMS Perm : 8602K->8593K(131072K)], 0.0828090 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC), "Trigger not parsed correctly.");
        assertEquals((long) 2425,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(7562),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(0),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(14784),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(1231),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(2846),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(114688),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(8602),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(8593),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(131072),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(82809,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineIcmsDcData() {
        String logLine = "165.805: [Full GC 165.805: [CMS: 101481K->97352K(1572864K), 1.1183800 secs] "
                + "287075K->97352K(2080768K), [CMS Perm : 68021K->67965K(262144K)] icms_dc=10 , 1.1186020 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertEquals((long) 165805,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes((287075 - 101481)),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((97352 - 97352)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes((2080768 - 1572864)),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(101481),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(97352),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1572864),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(68021),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(67965),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(262144),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(1118602,event.getDuration(),"Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineAfterPreprocessingNoSpaceAfterFullGC() {
        String logLine = "1504.625: [Full GC1504.625: [CMS: 1172695K->840574K(1549164K), 3.7572507 secs] "
                + "1301420K->840574K(1855852K), [CMS Perm : 226817K->226813K(376168K)], "
                + "3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertEquals(1504625L,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineTriggerConcurrentModeFailure() {
        String logLine = "44.684: [Full GC44.684: [CMS (concurrent mode failure): 1218548K->413373K(1465840K), "
                + "1.3656970 secs] 1229657K->413373K(1581168K), [CMS Perm : 83805K->80520K(83968K)], 1.3659420 secs] "
                + "[Times: user=1.33 sys=0.01, real=1.37 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 44684,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(1229657 - 1218548),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(413373 - 413373),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(1581168 - 1465840),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(1218548),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(413373),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1465840),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(83805),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(80520),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(83968),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(1365942,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineTriggerConcurrentModeInterrupted() {
        String logLine = "85030.389: [Full GC 85030.390: [CMS (concurrent mode interrupted): "
                + "861863K->904027K(1797568K), 42.9053262 secs] 1045947K->904027K(2047232K), "
                + "[CMS Perm : 252246K->252202K(262144K)], 42.9070278 secs] "
                + "[Times: user=43.11 sys=0.18, real=42.91 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_INTERRUPTED), "Trigger not parsed correctly.");
        assertEquals((long) 85030389,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(1045947 - 861863),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(904027 - 904027),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(2047232 - 1797568),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(861863),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(904027),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1797568),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(252246),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(252202),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(262144),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(42907027,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineTriggerHeapInspectionInitiatedGc() {
        String logLine = "2854.464: [Full GC (Heap Inspection Initiated GC) 2854.465: "
                + "[CMS: 945496K->961540K(4755456K), 0.8503670 secs] 1432148K->961540K(6137856K), "
                + "[Metaspace: 73362K->73362K(1118208K)], 0.8553350 secs] "
                + "[Times: user=0.83 sys=0.03, real=0.86 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC), "Trigger not parsed correctly.");
        assertEquals((long) 2854464,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(1432148 - 945496),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(961540 - 961540),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(6137856 - 4755456),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(945496),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(961540),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(4755456),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(73362),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(73362),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1118208),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(855335,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testFullGcBailing() {
        String logLine = "4300.825: [Full GC 4300.825: [CMSbailing out to foreground collection (concurrent mode "
                + "failure): 6014591K->6014592K(6014592K), 79.9352305 secs] 6256895K->6147510K(6256896K), [CMS Perm "
                + ": 206989K->206977K(262144K)], 79.9356622 secs] [Times: user=101.02 sys=3.09, real=79.94 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 4300825,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(6256895 - 6014591),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(6147510 - 6014592),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(6256896 - 6014592),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(6014591),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(6014592),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(6014592),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(206989),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(206977),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(262144),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(79935662,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineIncrementalModeMetaspace() {
        String logLine = "706.707: [Full GC (Allocation Failure) 706.708: [CMS (concurrent mode failure): "
                + "2655937K->2373842K(2658304K), 11.6746550 secs] 3973407K->2373842K(4040704K), "
                + "[Metaspace: 72496K->72496K(1118208K)] icms_dc=77 , 11.6770830 secs] "
                + "[Times: user=14.05 sys=0.02, real=11.68 secs]";

        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 706707,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(3973407 - 2655937),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(2373842 - 2373842),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(4040704 - 2658304),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(2655937),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(2373842),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(2658304),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(72496),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(72496),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1118208),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(11677083,event.getDuration(),"Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedClassHistogram() {
        String logLine = "11662.232: [Full GC 11662.233: [Class Histogram:, 38.6969442 secs]11700.930: "
                + "[CMS: 2844387K->635365K(7331840K), 46.4488813 secs]11747.379: [Class Histogram, 9.7637786 secs] "
                + "3198859K->635365K(7848704K), [CMS Perm : 851635K->408849K(1048576K)], 94.9116214 secs] "
                + "[Times: user=94.88 sys=0.24, real=94.91 secs]";

        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CLASS_HISTOGRAM), "Trigger not parsed correctly.");
        assertEquals((long) 11662232,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(3198859 - 2844387),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(635365 - 635365),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(7848704 - 7331840),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(2844387),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(635365),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(7331840),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(851635),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(408849),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1048576),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(94911621,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedConcurrentModeFailureRemarkBlock() {
        String logLine = "85217.903: [Full GC 85217.903: [CMS (concurrent mode failure) (concurrent mode failure)"
                + "[YG occupancy: 33620K (153344K)]85217.919: [Rescan (parallel) , 0.0116680 secs]85217.931: "
                + "[weak refs processing, 0.0167100 secs]85217.948: [class unloading, 0.0571300 secs]85218.005: "
                + "[scrub symbol & string tables, 0.0291210 secs]: 423728K->423633K(4023936K), 0.5165330 secs] "
                + "457349K->457254K(4177280K), [CMS Perm : 260428K->260406K(262144K)], 0.5167600 secs] "
                + "[Times: user=0.55 sys=0.01, real=0.52 secs]";

        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 85217903,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(457349 - 423728),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(457254 - 423633),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(4177280 - 4023936),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(423728),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(423633),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(4023936),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(260428),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(260406),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(262144),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(516760,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLinePreprocessedClassHistogramWithOldData() {
        String logLine = "11662.232: [Full GC 11662.233: [Class Histogram:, 38.6969442 secs]11700.930: "
                + "[CMS: 2844387K->635365K(7331840K), 46.4488813 secs]11747.379: [Class Histogram, 9.7637786 secs] "
                + "3198859K->635365K(7848704K), [CMS Perm : 851635K->408849K(1048576K)], 94.9116214 secs] "
                + "[Times: user=94.88 sys=0.24, real=94.91 secs]";

        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CLASS_HISTOGRAM), "Trigger not parsed correctly.");
        assertEquals((long) 11662232,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(3198859 - 2844387),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(635365 - 635365),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(7848704 - 7331840),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(2844387),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(635365),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(7331840),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(851635),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(408849),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1048576),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(94911621,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineTriggerMetadataGcThreshold() {
        String logLine = "262371.895: [Full GC (Metadata GC Threshold) 262371.895: [CMS: 42863K->49512K(1756416K), "
                + "0.2337314 secs] 176820K->49512K(2063104K), [Metaspace: 256586K->256586K(1230848K)], "
                + "0.2343092 secs] [Times: user=0.23 sys=0.00, real=0.23 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD), "Trigger not parsed correctly.");
        assertEquals((long) 262371895,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(176820 - 42863),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(49512 - 49512),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(2063104 - 1756416),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(42863),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(49512),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1756416),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(256586),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(256586),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1230848),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(234309,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineTriggerLastDitchCollection() {
        String logLine = "262372.130: [Full GC (Last ditch collection) 262372.130: [CMS: 49512K->49392K(1756416K), "
                + "0.2102326 secs] 49512K->49392K(2063104K), [Metaspace: 256586K->256586K(1230848K)], 0.2108635 secs] "
                + "[Times: user=0.20 sys=0.00, real=0.21 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION), "Trigger not parsed correctly.");
        assertEquals((long) 262372130,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(49512 - 49512),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(49392 - 49392),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(2063104 - 1756416),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(49512),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(49392),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1756416),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(256586),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(256586),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1230848),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(210863,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineTriggerJvmtiEnvForceGarbageCollectionWithConcurrentModeInterrupted() {
        String logLine = "262372.344: [Full GC (JvmtiEnv ForceGarbageCollection) 262372.344: [CMS "
                + "(concurrent mode interrupted): 49392K->48780K(1756416K), 0.2620228 secs] "
                + "49392K->48780K(2063104K), [Metaspace: 256552K->256552K(1230848K)], 0.2624794 secs] "
                + "[Times: user=0.26 sys=0.00, real=0.27 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_INTERRUPTED), "Trigger not parsed correctly.");
        assertEquals((long) 262372344,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(49392 - 49392),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(48780 - 48780),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(2063104 - 1756416),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(49392),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(48780),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1756416),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(256552),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(256552),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1230848),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(262479,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineParNewPromotionFailed() {
        String logLine = "144501.626: [GC 144501.627: [ParNew (promotion failed): 680066K->680066K(707840K), "
                + "3.7067346 secs] 1971073K->1981370K(2018560K), 3.7084059 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 144501626,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(680066),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(0),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(707840),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(1971073 - 680066),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(0),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(2018560 - 707840),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(0),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(0),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(0),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(3708405,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewPromotionFailedIncrementalMode() {
        String logLine = "159275.552: [GC 159275.552: [ParNew (promotion failed): 2007040K->2007040K(2007040K), "
                + "4.3393411 secs] 5167424K->5187429K(12394496K) icms_dc=7 , 4.3398519 secs] "
                + "[Times: user=4.96 sys=1.91, real=4.34 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 159275552,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(4339851,event.getDuration(),"Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testLogLineParNewPromotionFailedWithCmsBlock() {
        String logLine = "1181.943: [GC 1181.943: [ParNew (promotion failed): 145542K->142287K(149120K), "
                + "0.1316193 secs]1182.075: [CMS: 6656483K->548489K(8218240K), 9.1244297 secs] "
                + "6797120K->548489K(8367360K), 9.2564476 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 1181943,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(145542),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(548489 - 548489),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(149120),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(6656483),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(548489),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(8218240),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(0),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(0),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(0),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(9256447,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewPromotionFailedTruncated() {
        String logLine = "5881.424: [GC 5881.424: [ParNew (promotion failed): 153272K->152257K(153344K), "
                + "0.2143850 secs]5881.639: [CMS";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 5881424,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(214385,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testFirstLineOfMultiLineParallelScavengeEvent() {
        String logLine = "10.392: [GC";
        assertFalse(CmsSerialOldEvent.match(logLine), "Log line incorrectly recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
    }

    @Test
    void testLogLineParNewPromotionFailedMissingTrigger() {
        String logLine = "3546.690: [GC 3546.691: [ParNew: 532480K->532480K(599040K), 0.0000400 secs]3546.691: "
                + "[CMS: 887439K->893801K(907264K), 9.6413020 secs] 1419919K->893801K(1506304K), 9.6419180 secs] "
                + "[Times: user=9.54 sys=0.10, real=9.65 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 3546690,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(532480),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(0),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(599040),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(887439),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(893801),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(907264),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(9641918,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    /**
     * Has "Tenured" instead of "CMS" label in old generation block.
     */
    @Test
    void testLogLineParNewPromotionFailedTenuredLabel() {
        String logLine = "289985.117: [GC 289985.117: [ParNew (promotion failed): 144192K->144192K(144192K), "
                + "0.1347360 secs]289985.252: [Tenured: 1281600K->978341K(1281600K), 3.6577930 secs] "
                + "1409528K->978341K(1425792K), 3.7930200 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 289985117,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(144192),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(978341 - 978341),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(144192),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(1281600),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(978341),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1281600),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(3793020,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewPromotionFailedCmsSerialOldPermData() {
        String logLine = "395950.370: [GC 395950.370: [ParNew (promotion failed): "
                + "53094K->53606K(59008K), 0.0510880 secs]395950.421: "
                + "[CMS: 664527K->317110K(1507328K), 2.9523520 secs] 697709K->317110K(1566336K), "
                + "[CMS Perm : 83780K->83711K(131072K)], 3.0039040 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 395950370,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(53094),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((317110 - 317110)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(59008),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(664527),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(317110),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1507328),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(83780),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(83711),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(131072),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(3003904,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewPromotionFailedCmsSerialOldPermDataIncrementalMode() {
        String logLine = "4595.651: [GC 4595.651: [ParNew (promotion failed): 1304576K->1304576K(1304576K), "
                + "1.7740754 secs]4597.425: [CMS: 967034K->684015K(4886528K), 3.2678588 secs] "
                + "2022731K->684015K(6191104K), [CMS Perm : 201541K->201494K(524288K)] icms_dc=21 , "
                + "5.0421688 secs] [Times: user=5.54 sys=0.01, real=5.04 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 4595651,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(1304576),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((684015 - 684015)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(1304576),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(967034),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(684015),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(4886528),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(201541),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(201494),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(524288),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(5042168,event.getDuration(),"Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewPromotionFailedNoSpaceAfterGc() {
        String logLine = "108537.519: [GC108537.520: [ParNew (promotion failed): 1409215K->1426861K(1567616K), "
                + "0.4259330 secs]108537.946: [CMS: 13135135K->4554003K(16914880K), 14.7637760 secs] "
                + "14542753K->4554003K(18482496K), [CMS Perm : 227503K->226115K(378908K)], 15.1927120 secs] "
                + "[Times: user=16.31 sys=0.21, real=15.19 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 108537519,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(1409215),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((4554003 - 4554003)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(1567616),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(13135135),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(4554003),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(16914880),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(227503),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(226115),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(378908),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(15192712,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewPromotionFailedCmsSerialOldPermDataPreprocessedPrintClassHistogram() {
        String logLine = "182314.858: [GC 182314.859: [ParNew (promotion failed): 516864K->516864K(516864K), "
                + "2.0947428 secs]182316.954: [Class Histogram: , 41.3875632 secs]182358.342: "
                + "[CMS: 3354568K->756393K(7331840K), 53.1398170 secs]182411.482: [Class Histogram, 11.0299920 secs]"
                + " 3863904K->756393K(7848704K), [CMS Perm : 682507K->442221K(1048576K)], 107.6553710 secs]"
                + " [Times: user=112.83 sys=0.28, real=107.66 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 182314858,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(516864),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((756393 - 756393)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(516864),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(3354568),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(756393),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(7331840),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(682507),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(442221),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1048576),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(107655371,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewConcurrentModeFailure() {
        String logLine = "26683.209: [GC 26683.210: [ParNew: 261760K->261760K(261952K), "
                + "0.0000130 secs]26683.210: [CMS (concurrent mode failure): 1141548K->1078465K(1179648K), "
                + "7.3835370 secs] 1403308K->1078465K(1441600K), 7.3838390 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 26683209,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes((1403308 - 1141548)),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((1078465 - 1078465)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes((1441600 - 1179648)),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(1141548),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(1078465),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1179648),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(7383839,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewPromotionFailedConcurrentModeFailure() {
        String logLine = "25281.015: [GC 25281.015: [ParNew (promotion failed): 261760K->261760K(261952K), "
                + "0.1785000 secs]25281.193: [CMS (concurrent mode failure): 1048384K->1015603K(1179648K), "
                + "7.6767910 secs] 1292923K->1015603K(1441600K), 7.8557660 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 25281015,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(261760),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((1015603 - 1015603)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(261952),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(1048384),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(1015603),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1179648),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(7855766,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewPromotionFailedPrintPromotionFailure() {
        String logLine = "2017-02-28T00:43:55.587+0000: 36843.783: [GC (Allocation Failure) "
                + "2017-02-28T00:43:55.587+0000: 36843.783: [ParNew (promotion failed): 2304000K->2304000K(2304000K), "
                + "0.4501923 secs]2017-02-28T00:43:56.037+0000: 36844.234: [CMS: 2818067K->2769354K(5120000K), "
                + "3.8341757 secs] 5094036K->2769354K(7424000K), [Metaspace: 18583K->18583K(1067008K)], "
                + "4.2847962 secs] [Times: user=9.11 sys=0.00, real=4.29 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 36843783,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(2304000),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((2769354 - 2769354)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(2304000),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(2818067),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(2769354),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(5120000),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(4284796,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewCmsSerialOld() {
        String logLine = "42782.086: [GC 42782.086: [ParNew: 254464K->7680K(254464K), 0.2853553 secs]"
                + "42782.371: [Tenured: 1082057K->934941K(1082084K), 6.2719770 secs] "
                + "1310721K->934941K(1336548K), 6.5587770 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 42782086,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(254464),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(0),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(254464),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(1082057),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(934941),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1082084),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(6558777,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewCmsSerialOldWithPerm() {
        String logLine = "6.102: [GC6.102: [ParNew: 19648K->2176K(19648K), 0.0184470 secs]6.121: "
                + "[Tenured: 44849K->25946K(44864K), 0.2586250 secs] 60100K->25946K(64512K), "
                + "[Perm : 43759K->43759K(262144K)], 0.2773070 secs] [Times: user=0.16 sys=0.01, real=0.28 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 6102,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(19648),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(25946 - 25946),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(64512 - 44864),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(44849),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(25946),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(44864),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(43759),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(43759),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(262144),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(277307,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewCmsSerialOldJdk8() {
        String logLine = "1817.644: [GC (Allocation Failure) 1817.646: [ParNew: 1382383K->1382383K(1382400K), "
                + "0.0000530 secs]1817.646: [CMS: 2658303K->2658303K(2658304K), 8.7951430 secs] "
                + "4040686K->2873414K(4040704K), [Metaspace: 72200K->72200K(1118208K)], 8.7986750 secs] "
                + "[Times: user=8.79 sys=0.01, real=8.80 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 1817644,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(1382383),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(2873414 - 2658303),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(4040704 - 2658304),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(2658303),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(2658303),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(2658304),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(72200),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(72200),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(1118208),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(8798675,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewConcurrentModeFailurePermData() {
        String logLine = "3070.289: [GC 3070.289: [ParNew: 207744K->207744K(242304K), 0.0000682 secs]3070.289: "
                + "[CMS (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] "
                + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 3070289,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes((6217865 - 6010121)),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((6028029 - 6014591)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes((6256896 - 6014592)),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(6010121),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(6014591),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(6014592),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(206688),event.getPermOccupancyInit(),"Perm gen begin size not parsed correctly.");
        assertEquals(kilobytes(206662),event.getPermOccupancyEnd(),"Perm gen end size not parsed correctly.");
        assertEquals(kilobytes(262144),event.getPermSpace(),"Perm gen allocation size not parsed correctly.");
        assertEquals(79050959,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testGcBailing() {
        String logLine = "1901.217: [GC 1901.217: [ParNew: 261760K->261760K(261952K), 0.0000570 secs]1901.217: "
                + "[CMSJava HotSpot(TM) Server VM warning: bailing out to foreground collection (concurrent mode "
                + "failure): 1794415K->909664K(1835008K), 124.5953890 secs] 2056175K->909664K(2096960K) "
                + "icms_dc=100 , 124.5963320 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 1901217,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes((2056175 - 1794415)),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((909664 - 909664)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes((2096960 - 1835008)),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(1794415),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(909664),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(1835008),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(124596332,event.getDuration(),"Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewConcurrentModeFailurePermDataMetaspaceIcrementalMode() {
        String logLine = "719.519: [GC (Allocation Failure) 719.521: [ParNew: 1382400K->1382400K(1382400K), "
                + "0.0000470 secs] (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 719519,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(1382400),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((2702358 - 2658278)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes((4040704 - 2658304)),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(2542828),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(2658278),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(2658304),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(72175),event.getPermOccupancyInit(),"Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(72175),event.getPermOccupancyEnd(),"Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1118208),event.getPermSpace(),"Metaspace allocation size not parsed correctly.");
        assertEquals(12348057,event.getDuration(),"Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewConcurrentModeFailurePermDataMetaspaceNotIcrementalMode() {
        String logLine = "1202.526: [GC (Allocation Failure) 1202.528: [ParNew: 1355422K->1355422K(1382400K), "
                + "0.0000500 secs]1202.528: [CMS (concurrent mode failure): 2656311K->2658289K(2658304K), "
                + "9.3575580 secs] 4011734K->2725109K(4040704K), [Metaspace: 72111K->72111K(1118208K)], "
                + "9.3610080 secs] [Times: user=9.35 sys=0.01, real=9.36 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 1202526,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(1355422),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((2725109 - 2658289)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes((4040704 - 2658304)),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(2656311),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(2658289),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(2658304),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(72111),event.getPermOccupancyInit(),"Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(72111),event.getPermOccupancyEnd(),"Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1118208),event.getPermSpace(),"Metaspace allocation size not parsed correctly.");
        assertEquals(9361008,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewConcurrentModeFailurePermDataPreProcessedClassHistogram() {
        String logLine = "572264.304: [GC 572264.306: [ParNew (promotion failed): 516864K->516864K(516864K), "
                + "1.4978605 secs]572265.804: [Class Histogram:, 23.6901531 secs]"
                + "572289.495: [CMS (concurrent mode failure): 5350445K->891234K(7331840K), 59.8600601 secs]"
                + "572349.355: [Class Histogram, 12.1674045 secs] 5825751K->891234K(7848704K), "
                + "[CMS Perm : 500357K->443269K(1048576K)], 97.2188825 secs] "
                + "[Times: user=100.78 sys=0.18, real=97.22 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 572264304,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(516864),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((891234 - 891234)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes((7848704 - 7331840)),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(5350445),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(891234),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(7331840),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(500357),event.getPermOccupancyInit(),"Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(443269),event.getPermOccupancyEnd(),"Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1048576),event.getPermSpace(),"Metaspace allocation size not parsed correctly.");
        assertEquals(97218882,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewConcurrentModeFailurePermDataPreProcessedClassHistogramConcurrentModeFailure() {
        String logLine = "576460.444: [GC 576460.446: [ParNew (promotion failed): 516864K->516864K(516864K), "
                + "1.9697779 secs]576462.416: [Class Histogram:, 23.3548450 secs]"
                + "576485.771: [CMS: 5074711K->905970K(7331840K), 46.0517345 secs]"
                + "576531.823: [Class Histogram, 12.2976631 secs] 5566845K->905970K(7848704K), "
                + "[CMS Perm : 498279K->443366K(1048576K)], 83.6775207 secs] "
                + "[Times: user=87.62 sys=0.20, real=83.68 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED), "Trigger not parsed correctly.");
        assertEquals((long) 576460444,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(516864),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes((905970 - 905970)),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes((7848704 - 7331840)),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(5074711),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(905970),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(7331840),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(498279),event.getPermOccupancyInit(),"Metaspace begin size not parsed correctly.");
        assertEquals(kilobytes(443366),event.getPermOccupancyEnd(),"Metaspace end size not parsed correctly.");
        assertEquals(kilobytes(1048576),event.getPermSpace(),"Metaspace allocation size not parsed correctly.");
        assertEquals(83677520,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testTriggerGcLockerInitiatedGc() {
        String logLine = "58626.878: [Full GC (GCLocker Initiated GC)58626.878: [CMS (concurrent mode failure): "
                + "13441202K->12005469K(13631488K), 23.1836190 secs] 19349630K->12005469K(22020096K), "
                + "[CMS Perm : 1257346K->1257346K(2097152K)] icms_dc=100 , 23.1838500 secs] "
                + "[Times: user=22.77 sys=0.39, real=23.18 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 58626878,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(19349630 - 13441202),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(12005469 - 12005469),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(22020096 - 13631488),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(13441202),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(12005469),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(13631488),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(1257346),event.getPermOccupancyInit(),"Perm begin size not parsed correctly.");
        assertEquals(kilobytes(1257346),event.getPermOccupancyEnd(),"Perm end size not parsed correctly.");
        assertEquals(kilobytes(2097152),event.getPermSpace(),"Perm allocation size not parsed correctly.");
        assertEquals(23183850,event.getDuration(),"Duration not parsed correctly.");
        assertTrue(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testJdk6ParNewPromotionFailedConcurrentModeFailureWithClassHistogram() {
        String logLine = "2017-04-22T13:58:35.287+0100: 471391.741: [GC 471391.743: [ParNew (promotion failed): "
                + "516864K->516864K(516864K), 2.1875738 secs]471393.931: [Class Histogram:, 25.2253758 secs]"
                + "471419.156: [CMS (concurrent mode failure): 5977264K->1290167K(7331840K), 59.2834068 secs]"
                + "471478.440: [Class Histogram, 15.6352805 secs] 6449325K->1290167K(7848704K), [CMS Perm : "
                + "473438K->450663K(771512K)], 102.3357902 secs] [Times: user=106.25 sys=0.21, real=102.34 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 471391741,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(516864),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(1290167 - 1290167),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(7848704 - 7331840),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(5977264),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(1290167),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(7331840),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(473438),event.getPermOccupancyInit(),"Perm begin size not parsed correctly.");
        assertEquals(kilobytes(450663),event.getPermOccupancyEnd(),"Perm end size not parsed correctly.");
        assertEquals(kilobytes(771512),event.getPermSpace(),"Perm allocation size not parsed correctly.");
        assertEquals(102335790,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testParNewPromotionFailedConcurrentModeFailureWithClassHistogramDatestamps() {
        String logLine = "2017-05-03T14:51:32.659-0400: 2057.323: [Full GC 2017-05-03T14:51:32.680-0400: 2057.341: "
                + "[Class Histogram:, 13.8859570 secs]2017-05-03T14:51:46.579-0400: "
                + "2071.240: [CMS (concurrent mode failure): 9216000K->9215999K(9216000K), 62.4046040 secs]"
                + "2017-05-03T14:52:48.982-0400: 2133.641: [Class Histogram, 11.9525850 secs] "
                + "13363199K->9728622K(13363200K), [CMS Perm : 376898K->376894K(524288K)], 88.2785270 secs] "
                + "[Times: user=86.32 sys=0.39, real=88.27 secs]";
        assertTrue(CmsSerialOldEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        assertTrue(event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE), "Trigger not parsed correctly.");
        assertEquals((long) 2057323,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(kilobytes(13363199 - 9216000),event.getYoungOccupancyInit(),"Young begin size not parsed correctly.");
        assertEquals(kilobytes(9728622 - 9215999),event.getYoungOccupancyEnd(),"Young end size not parsed correctly.");
        assertEquals(kilobytes(13363200 - 9216000),event.getYoungSpace(),"Young available size not parsed correctly.");
        assertEquals(kilobytes(9216000),event.getOldOccupancyInit(),"Old begin size not parsed correctly.");
        assertEquals(kilobytes(9215999),event.getOldOccupancyEnd(),"Old end size not parsed correctly.");
        assertEquals(kilobytes(9216000),event.getOldSpace(),"Old allocation size not parsed correctly.");
        assertEquals(kilobytes(376898),event.getPermOccupancyInit(),"Perm begin size not parsed correctly.");
        assertEquals(kilobytes(376894),event.getPermOccupancyEnd(),"Perm end size not parsed correctly.");
        assertEquals(kilobytes(524288),event.getPermSpace(),"Perm allocation size not parsed correctly.");
        assertEquals(88278527,event.getDuration(),"Duration not parsed correctly.");
        assertFalse(event.isIncrementalMode(), "Incremental Mode not parsed correctly.");
    }

    @Test
    void testSplitParNewPromotionFailedCmsConcurrentModeFailure() {
        File testFile = TestUtil.getFile("dataset5.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(3,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT), "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_INITIAL_MARK), "Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED), Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS), Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.");
    }

    /**
     * Test preprocessing <code>HeapAtGcEvent</code> with underlying <code>CmsSerialOldEvent</code>.
     */
    @Test
    void testSplitPrintHeapAtGcParNewConcurrentModeFailureEventLogging() {
        File testFile = TestUtil.getFile("dataset7.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), LogEventType.UNKNOWN.toString() + " collector identified.");
        assertEquals(2,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD), "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT), "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_PRINT_HEAP_AT_GC), Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS), Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.");
    }

    @Test
    void testSplitParNewPromotionFailedCmsConcurrentModeFailurePermData() {
        File testFile = TestUtil.getFile("dataset12.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT), "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS), Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.");
    }

    @Test
    void testSplitParNewCmsConcurrentModeFailurePermData() {
        File testFile = TestUtil.getFile("dataset13.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT), "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS), Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.");
    }

    @Test
    void testSplit3LinesParNewPromotionFailedCmsConcurrentModeFailurePermDataEventMarkLogging() {
        File testFile = TestUtil.getFile("dataset16.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT), "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS), Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.");
    }

    /**
     * Test preprocessing <code>ParNewConcurrentModeFailureEvent</code> split over 3 lines.
     * 
     */
    @Test
    void testSplit3LinesParNewConcurrentModeFailureEventLogging() {
        File testFile = TestUtil.getFile("dataset29.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD), "Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT), "Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INCREMENTAL_MODE), Analysis.WARN_CMS_INCREMENTAL_MODE + " analysis not identified.");
    }

    @Test
    void testParNewConcurrentModeFailureMixedCmsConcurrentJdk8() {
        File testFile = TestUtil.getFile("dataset70.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD), LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT), LogEventType.CMS_CONCURRENT.toString() + " collector not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS), Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_CMS_INCREMENTAL_MODE), Analysis.WARN_CMS_INCREMENTAL_MODE + " analysis not identified.");
    }

    /**
     * Test CMS_SERIAL_OLD heap inspection initiate gc trigger.
     * 
     */
    @Test
    void testHeapInspectionInitiatedGcAnalysis() {
        File testFile = TestUtil.getFile("dataset72.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD), JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_INSPECTION_INITIATED_GC), Analysis.WARN_HEAP_INSPECTION_INITIATED_GC + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS), Analysis.ERROR_SERIAL_GC_CMS + " analysis incorrectly identified.");
    }

    /**
     * Test preprocessing CMS_SERIAL_OLD triggered by <code>PrintClassHistogramEvent</code> across many lines.
     * 
     */
    @Test
    void testParNewPromotionFailedCmsSerialOldPermDataPrintClassHistogramTriggerAcross6Lines() {
        File testFile = TestUtil.getFile("dataset82.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(2,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD), "Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT), "Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS), Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.");
    }

    /**
     * Test CMS_SERIAL_OLD heap inspection initiate gc trigger.
     * 
     */
    @Test
    void testLogLineTriggerHeapDumpedInitiatedGc() {
        File testFile = TestUtil.getFile("dataset92.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        assertEquals(1,jvmRun.getEventTypes().size(),"Event type count not correct.");
        assertFalse(jvmRun.getEventTypes().contains(LogEventType.UNKNOWN), JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.");
        assertTrue(jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD), JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.");
        assertTrue(jvmRun.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_INITIATED_GC), Analysis.WARN_HEAP_DUMP_INITIATED_GC + " analysis not identified.");
        assertFalse(jvmRun.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS), Analysis.ERROR_SERIAL_GC_CMS + " analysis incorrectly identified.");
    }
}
