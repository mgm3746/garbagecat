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
package org.eclipselabs.garbagecat.domain.jdk;

import java.io.File;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestCmsSerialOldEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "262372.344: [Full GC (JvmtiEnv ForceGarbageCollection) 262372.344: [CMS "
                + "(concurrent mode interrupted): 49392K->48780K(1756416K), 0.2620228 secs] "
                + "49392K->48780K(2063104K), [Metaspace: 256552K->256552K(1230848K)], 0.2624794 secs] "
                + "[Times: user=0.26 sys=0.00, real=0.27 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "5.980: [Full GC 5.980: "
                + "[CMS: 5589K->5796K(122880K), 0.0889610 secs] 11695K->5796K(131072K), "
                + "[CMS Perm : 13140K->13124K(131072K)], 0.0891270 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 5980, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 6106, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 8192, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 5589, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 5796, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 122880, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 13140, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 13124, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 89, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "5.980: [Full GC 5.980: "
                + "[CMS: 5589K->5796K(122880K), 0.0889610 secs] 11695K->5796K(131072K), "
                + "[CMS Perm : 13140K->13124K(131072K)], 0.0891270 secs] ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
    }

    public void testLogLineJdk16WithTrigger() {
        String logLine = "2.425: [Full GC (System) 2.425: "
                + "[CMS: 1231K->2846K(114688K), 0.0827010 secs] 8793K->2846K(129472K), "
                + "[CMS Perm : 8602K->8593K(131072K)], 0.0828090 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        Assert.assertEquals("Time stamp not parsed correctly.", 2425, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 7562, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 14784, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1231, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 2846, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 114688, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 8602, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 8593, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 82, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineIcmsDcData() {
        String logLine = "165.805: [Full GC 165.805: [CMS: 101481K->97352K(1572864K), 1.1183800 secs] "
                + "287075K->97352K(2080768K), [CMS Perm : 68021K->67965K(262144K)] icms_dc=10 , 1.1186020 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 165805, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (287075 - 101481), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (97352 - 97352), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (2080768 - 1572864), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 101481, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 97352, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1572864, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 68021, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 67965, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1118, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineAfterPreprocessingNoSpaceAfterFullGC() {
        String logLine = "1504.625: [Full GC1504.625: [CMS: 1172695K->840574K(1549164K), 3.7572507 secs] "
                + "1301420K->840574K(1855852K), [CMS Perm : 226817K->226813K(376168K)], "
                + "3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1504625L, event.getTimestamp());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineTriggerConcurrentModeFailure() {
        String logLine = "44.684: [Full GC44.684: [CMS (concurrent mode failure): 1218548K->413373K(1465840K), "
                + "1.3656970 secs] 1229657K->413373K(1581168K), [CMS Perm : 83805K->80520K(83968K)], 1.3659420 secs] "
                + "[Times: user=1.33 sys=0.01, real=1.37 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 44684, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1229657 - 1218548, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 413373 - 413373, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1581168 - 1465840, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1218548, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 413373, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1465840, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 83805, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 80520, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 83968, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1365, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineTriggerConcurrentModeInterrupted() {
        String logLine = "85030.389: [Full GC 85030.390: [CMS (concurrent mode interrupted): "
                + "861863K->904027K(1797568K), 42.9053262 secs] 1045947K->904027K(2047232K), "
                + "[CMS Perm : 252246K->252202K(262144K)], 42.9070278 secs] "
                + "[Times: user=43.11 sys=0.18, real=42.91 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_INTERRUPTED));
        Assert.assertEquals("Time stamp not parsed correctly.", 85030389, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1045947 - 861863, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 904027 - 904027, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 2047232 - 1797568, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 861863, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 904027, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1797568, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 252246, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 252202, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 42907, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineTriggerHeapInspectionInitiatedGc() {
        String logLine = "2854.464: [Full GC (Heap Inspection Initiated GC) 2854.465: "
                + "[CMS: 945496K->961540K(4755456K), 0.8503670 secs] 1432148K->961540K(6137856K), "
                + "[Metaspace: 73362K->73362K(1118208K)], 0.8553350 secs] "
                + "[Times: user=0.83 sys=0.03, real=0.86 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC));
        Assert.assertEquals("Time stamp not parsed correctly.", 2854464, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1432148 - 945496, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 961540 - 961540, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 6137856 - 4755456, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 945496, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 961540, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 4755456, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 73362, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 73362, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1118208, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 855, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testFullGcBailing() {
        String logLine = "4300.825: [Full GC 4300.825: [CMSbailing out to foreground collection (concurrent mode "
                + "failure): 6014591K->6014592K(6014592K), 79.9352305 secs] 6256895K->6147510K(6256896K), [CMS Perm "
                + ": 206989K->206977K(262144K)], 79.9356622 secs] [Times: user=101.02 sys=3.09, real=79.94 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 4300825, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 6256895 - 6014591, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 6147510 - 6014592, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 6256896 - 6014592, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 6014591, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 6014592, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 6014592, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 206989, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 206977, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 79935, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineIncrementalModeMetaspace() {
        String logLine = "706.707: [Full GC (Allocation Failure) 706.708: [CMS (concurrent mode failure): "
                + "2655937K->2373842K(2658304K), 11.6746550 secs] 3973407K->2373842K(4040704K), "
                + "[Metaspace: 72496K->72496K(1118208K)] icms_dc=77 , 11.6770830 secs] "
                + "[Times: user=14.05 sys=0.02, real=11.68 secs]";

        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 706707, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 3973407 - 2655937, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 2373842 - 2373842, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 4040704 - 2658304, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 2655937, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 2373842, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 2658304, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 72496, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 72496, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1118208, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 11677, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLinePreprocessedClassHistogram() {
        String logLine = "11662.232: [Full GC 11662.233: [Class Histogram:, 38.6969442 secs]11700.930: "
                + "[CMS: 2844387K->635365K(7331840K), 46.4488813 secs]11747.379: [Class Histogram, 9.7637786 secs] "
                + "3198859K->635365K(7848704K), [CMS Perm : 851635K->408849K(1048576K)], 94.9116214 secs] "
                + "[Times: user=94.88 sys=0.24, real=94.91 secs]";

        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CLASS_HISTOGRAM));
        Assert.assertEquals("Time stamp not parsed correctly.", 11662232, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 3198859 - 2844387, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 635365 - 635365, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 7848704 - 7331840, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 2844387, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 635365, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 7331840, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 851635, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 408849, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1048576, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 94911, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLinePreprocessedConcurrentModeFailureRemarkBlock() {
        String logLine = "85217.903: [Full GC 85217.903: [CMS (concurrent mode failure) (concurrent mode failure)"
                + "[YG occupancy: 33620K (153344K)]85217.919: [Rescan (parallel) , 0.0116680 secs]85217.931: "
                + "[weak refs processing, 0.0167100 secs]85217.948: [class unloading, 0.0571300 secs]85218.005: "
                + "[scrub symbol & string tables, 0.0291210 secs]: 423728K->423633K(4023936K), 0.5165330 secs] "
                + "457349K->457254K(4177280K), [CMS Perm : 260428K->260406K(262144K)], 0.5167600 secs] "
                + "[Times: user=0.55 sys=0.01, real=0.52 secs]";

        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 85217903, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 457349 - 423728, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 457254 - 423633, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 4177280 - 4023936, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 423728, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 423633, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 4023936, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 260428, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 260406, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 516, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLinePreprocessedClassHistogramWithOldData() {
        String logLine = "11662.232: [Full GC 11662.233: [Class Histogram:, 38.6969442 secs]11700.930: "
                + "[CMS: 2844387K->635365K(7331840K), 46.4488813 secs]11747.379: [Class Histogram, 9.7637786 secs] "
                + "3198859K->635365K(7848704K), [CMS Perm : 851635K->408849K(1048576K)], 94.9116214 secs] "
                + "[Times: user=94.88 sys=0.24, real=94.91 secs]";

        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CLASS_HISTOGRAM));
        Assert.assertEquals("Time stamp not parsed correctly.", 11662232, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 3198859 - 2844387, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 635365 - 635365, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 7848704 - 7331840, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 2844387, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 635365, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 7331840, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 851635, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 408849, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1048576, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 94911, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineTriggerMetadataGcThreshold() {
        String logLine = "262371.895: [Full GC (Metadata GC Threshold) 262371.895: [CMS: 42863K->49512K(1756416K), "
                + "0.2337314 secs] 176820K->49512K(2063104K), [Metaspace: 256586K->256586K(1230848K)], "
                + "0.2343092 secs] [Times: user=0.23 sys=0.00, real=0.23 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        Assert.assertEquals("Time stamp not parsed correctly.", 262371895, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 176820 - 42863, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 49512 - 49512, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 2063104 - 1756416, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 42863, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 49512, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1756416, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 256586, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 256586, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1230848, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 234, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineTriggerLastDitchCollection() {
        String logLine = "262372.130: [Full GC (Last ditch collection) 262372.130: [CMS: 49512K->49392K(1756416K), "
                + "0.2102326 secs] 49512K->49392K(2063104K), [Metaspace: 256586K->256586K(1230848K)], 0.2108635 secs] "
                + "[Times: user=0.20 sys=0.00, real=0.21 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION));
        Assert.assertEquals("Time stamp not parsed correctly.", 262372130, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 49512 - 49512, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 49392 - 49392, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 2063104 - 1756416, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 49512, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 49392, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1756416, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 256586, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 256586, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1230848, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 210, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineTriggerJvmtiEnvForceGarbageCollectionWithConcurrentModeInterrupted() {
        String logLine = "262372.344: [Full GC (JvmtiEnv ForceGarbageCollection) 262372.344: [CMS "
                + "(concurrent mode interrupted): 49392K->48780K(1756416K), 0.2620228 secs] "
                + "49392K->48780K(2063104K), [Metaspace: 256552K->256552K(1230848K)], 0.2624794 secs] "
                + "[Times: user=0.26 sys=0.00, real=0.27 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_INTERRUPTED));
        Assert.assertEquals("Time stamp not parsed correctly.", 262372344, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 49392 - 49392, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 48780 - 48780, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 2063104 - 1756416, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 49392, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 48780, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1756416, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 256552, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 256552, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1230848, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 262, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineParNewPromotionFailed() {
        String logLine = "144501.626: [GC 144501.627: [ParNew (promotion failed): 680066K->680066K(707840K), "
                + "3.7067346 secs] 1971073K->1981370K(2018560K), 3.7084059 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 144501626, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 680066, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 707840, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1971073 - 680066, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 0, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 2018560 - 707840, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 0, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 0, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 0, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3708, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewPromotionFailedIncrementalMode() {
        String logLine = "159275.552: [GC 159275.552: [ParNew (promotion failed): 2007040K->2007040K(2007040K), "
                + "4.3393411 secs] 5167424K->5187429K(12394496K) icms_dc=7 , 4.3398519 secs] "
                + "[Times: user=4.96 sys=1.91, real=4.34 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 159275552, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 4339, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineParNewPromotionFailedWithCmsBlock() {
        String logLine = "1181.943: [GC 1181.943: [ParNew (promotion failed): 145542K->142287K(149120K), "
                + "0.1316193 secs]1182.075: [CMS: 6656483K->548489K(8218240K), 9.1244297 secs] "
                + "6797120K->548489K(8367360K), 9.2564476 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 1181943, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 145542, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 548489 - 548489, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 149120, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 6656483, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 548489, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 8218240, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 0, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 0, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 0, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 9256, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewPromotionFailedTruncated() {
        String logLine = "5881.424: [GC 5881.424: [ParNew (promotion failed): 153272K->152257K(153344K), "
                + "0.2143850 secs]5881.639: [CMS";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 5881424, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 214, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testFirstLineOfMultiLineParallelScavengeEvent() {
        String logLine = "10.392: [GC";
        Assert.assertFalse("Log line incorrectly recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
    }

    public void testLogLineParNewPromotionFailedMissingTrigger() {
        String logLine = "3546.690: [GC 3546.691: [ParNew: 532480K->532480K(599040K), 0.0000400 secs]3546.691: "
                + "[CMS: 887439K->893801K(907264K), 9.6413020 secs] 1419919K->893801K(1506304K), 9.6419180 secs] "
                + "[Times: user=9.54 sys=0.10, real=9.65 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 3546690, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 532480, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 599040, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 887439, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 893801, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 907264, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 9641, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    /**
     * Has "Tenured" instead of "CMS" label in old generation block.
     */
    public void testLogLineParNewPromotionFailedTenuredLabel() {
        String logLine = "289985.117: [GC 289985.117: [ParNew (promotion failed): 144192K->144192K(144192K), "
                + "0.1347360 secs]289985.252: [Tenured: 1281600K->978341K(1281600K), 3.6577930 secs] "
                + "1409528K->978341K(1425792K), 3.7930200 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 289985117, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 144192, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 978341 - 978341, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 144192, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1281600, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 978341, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1281600, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3793, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewPromotionFailedCmsSerialOldPermData() {
        String logLine = "395950.370: [GC 395950.370: [ParNew (promotion failed): "
                + "53094K->53606K(59008K), 0.0510880 secs]395950.421: "
                + "[CMS: 664527K->317110K(1507328K), 2.9523520 secs] 697709K->317110K(1566336K), "
                + "[CMS Perm : 83780K->83711K(131072K)], 3.0039040 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 395950370, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 53094, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (317110 - 317110), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 59008, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 664527, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 317110, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1507328, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 83780, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 83711, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3003, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewPromotionFailedCmsSerialOldPermDataIncrementalMode() {
        String logLine = "4595.651: [GC 4595.651: [ParNew (promotion failed): 1304576K->1304576K(1304576K), "
                + "1.7740754 secs]4597.425: [CMS: 967034K->684015K(4886528K), 3.2678588 secs] "
                + "2022731K->684015K(6191104K), [CMS Perm : 201541K->201494K(524288K)] icms_dc=21 , "
                + "5.0421688 secs] [Times: user=5.54 sys=0.01, real=5.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 4595651, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1304576, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (684015 - 684015), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1304576, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 967034, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 684015, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 4886528, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 201541, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 201494, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 524288, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 5042, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewPromotionFailedNoSpaceAfterGc() {
        String logLine = "108537.519: [GC108537.520: [ParNew (promotion failed): 1409215K->1426861K(1567616K), "
                + "0.4259330 secs]108537.946: [CMS: 13135135K->4554003K(16914880K), 14.7637760 secs] "
                + "14542753K->4554003K(18482496K), [CMS Perm : 227503K->226115K(378908K)], 15.1927120 secs] "
                + "[Times: user=16.31 sys=0.21, real=15.19 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 108537519, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1409215, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (4554003 - 4554003), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1567616, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 13135135, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 4554003, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 16914880, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 227503, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 226115, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 378908, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 15192, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewPromotionFailedCmsSerialOldPermDataPreprocessedPrintClassHistogram() {
        String logLine = "182314.858: [GC 182314.859: [ParNew (promotion failed): 516864K->516864K(516864K), "
                + "2.0947428 secs]182316.954: [Class Histogram: , 41.3875632 secs]182358.342: "
                + "[CMS: 3354568K->756393K(7331840K), 53.1398170 secs]182411.482: [Class Histogram, 11.0299920 secs]"
                + " 3863904K->756393K(7848704K), [CMS Perm : 682507K->442221K(1048576K)], 107.6553710 secs]"
                + " [Times: user=112.83 sys=0.28, real=107.66 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 182314858, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 516864, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (756393 - 756393), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 516864, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 3354568, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 756393, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 7331840, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 682507, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 442221, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1048576, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 107655, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewConcurrentModeFailure() {
        String logLine = "26683.209: [GC 26683.210: [ParNew: 261760K->261760K(261952K), "
                + "0.0000130 secs]26683.210: [CMS (concurrent mode failure): 1141548K->1078465K(1179648K), "
                + "7.3835370 secs] 1403308K->1078465K(1441600K), 7.3838390 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 26683209, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (1403308 - 1141548),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1078465 - 1078465), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1141548, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 1078465, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 7383, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewPromotionFailedConcurrentModeFailure() {
        String logLine = "25281.015: [GC 25281.015: [ParNew (promotion failed): 261760K->261760K(261952K), "
                + "0.1785000 secs]25281.193: [CMS (concurrent mode failure): 1048384K->1015603K(1179648K), "
                + "7.6767910 secs] 1292923K->1015603K(1441600K), 7.8557660 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 25281015, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 261760, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (1015603 - 1015603), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 261952, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1048384, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 1015603, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 7855, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewCmsSerialOld() {
        String logLine = "42782.086: [GC 42782.086: [ParNew: 254464K->7680K(254464K), 0.2853553 secs]"
                + "42782.371: [Tenured: 1082057K->934941K(1082084K), 6.2719770 secs] "
                + "1310721K->934941K(1336548K), 6.5587770 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 42782086, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 254464, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 254464, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1082057, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 934941, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1082084, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 6558, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewCmsSerialOldWithPerm() {
        String logLine = "6.102: [GC6.102: [ParNew: 19648K->2176K(19648K), 0.0184470 secs]6.121: "
                + "[Tenured: 44849K->25946K(44864K), 0.2586250 secs] 60100K->25946K(64512K), "
                + "[Perm : 43759K->43759K(262144K)], 0.2773070 secs] [Times: user=0.16 sys=0.01, real=0.28 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 6102, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 19648, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 25946 - 25946, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 64512 - 44864, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 44849, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 25946, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 44864, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 43759, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 43759, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 277, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewCmsSerialOldJdk8() {
        String logLine = "1817.644: [GC (Allocation Failure) 1817.646: [ParNew: 1382383K->1382383K(1382400K), "
                + "0.0000530 secs]1817.646: [CMS: 2658303K->2658303K(2658304K), 8.7951430 secs] "
                + "4040686K->2873414K(4040704K), [Metaspace: 72200K->72200K(1118208K)], 8.7986750 secs] "
                + "[Times: user=8.79 sys=0.01, real=8.80 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 1817644, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1382383, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 2873414 - 2658303, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 4040704 - 2658304, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 2658303, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 2658303, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 2658304, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 72200, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 72200, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 1118208, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 8798, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewConcurrentModeFailurePermData() {
        String logLine = "3070.289: [GC 3070.289: [ParNew: 207744K->207744K(242304K), 0.0000682 secs]3070.289: "
                + "[CMS (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] "
                + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 3070289, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (6217865 - 6010121),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (6028029 - 6014591), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (6256896 - 6014592), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 6010121, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 6014591, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 6014592, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 206688, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 206662, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 79050, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testGcBailing() {
        String logLine = "1901.217: [GC 1901.217: [ParNew: 261760K->261760K(261952K), 0.0000570 secs]1901.217: "
                + "[CMSJava HotSpot(TM) Server VM warning: bailing out to foreground collection (concurrent mode "
                + "failure): 1794415K->909664K(1835008K), 124.5953890 secs] 2056175K->909664K(2096960K) "
                + "icms_dc=100 , 124.5963320 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 1901217, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (2056175 - 1794415),
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (909664 - 909664), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (2096960 - 1835008), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 1794415, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 909664, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1835008, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 124596, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewConcurrentModeFailurePermDataMetaspaceIcrementalMode() {
        String logLine = "719.519: [GC (Allocation Failure) 719.521: [ParNew: 1382400K->1382400K(1382400K), "
                + "0.0000470 secs] (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 719519, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1382400, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (2702358 - 2658278), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (4040704 - 2658304), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 2542828, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 2658278, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 2658304, event.getOldSpace());
        Assert.assertEquals("Metaspace begin size not parsed correctly.", 72175, event.getPermOccupancyInit());
        Assert.assertEquals("Metaspace end size not parsed correctly.", 72175, event.getPermOccupancyEnd());
        Assert.assertEquals("Metaspace allocation size not parsed correctly.", 1118208, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 12348, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewConcurrentModeFailurePermDataMetaspaceNotIcrementalMode() {
        String logLine = "1202.526: [GC (Allocation Failure) 1202.528: [ParNew: 1355422K->1355422K(1382400K), "
                + "0.0000500 secs]1202.528: [CMS (concurrent mode failure): 2656311K->2658289K(2658304K), "
                + "9.3575580 secs] 4011734K->2725109K(4040704K), [Metaspace: 72111K->72111K(1118208K)], "
                + "9.3610080 secs] [Times: user=9.35 sys=0.01, real=9.36 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 1202526, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 1355422, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (2725109 - 2658289), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (4040704 - 2658304), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 2656311, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 2658289, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 2658304, event.getOldSpace());
        Assert.assertEquals("Metaspace begin size not parsed correctly.", 72111, event.getPermOccupancyInit());
        Assert.assertEquals("Metaspace end size not parsed correctly.", 72111, event.getPermOccupancyEnd());
        Assert.assertEquals("Metaspace allocation size not parsed correctly.", 1118208, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 9361, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewConcurrentModeFailurePermDataPreProcessedClassHistogram() {
        String logLine = "572264.304: [GC 572264.306: [ParNew (promotion failed): 516864K->516864K(516864K), "
                + "1.4978605 secs]572265.804: [Class Histogram:, 23.6901531 secs]"
                + "572289.495: [CMS (concurrent mode failure): 5350445K->891234K(7331840K), 59.8600601 secs]"
                + "572349.355: [Class Histogram, 12.1674045 secs] 5825751K->891234K(7848704K), "
                + "[CMS Perm : 500357K->443269K(1048576K)], 97.2188825 secs] "
                + "[Times: user=100.78 sys=0.18, real=97.22 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 572264304, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 516864, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (891234 - 891234), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (7848704 - 7331840), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 5350445, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 891234, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 7331840, event.getOldSpace());
        Assert.assertEquals("Metaspace begin size not parsed correctly.", 500357, event.getPermOccupancyInit());
        Assert.assertEquals("Metaspace end size not parsed correctly.", 443269, event.getPermOccupancyEnd());
        Assert.assertEquals("Metaspace allocation size not parsed correctly.", 1048576, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 97218, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testParNewConcurrentModeFailurePermDataPreProcessedClassHistogramConcurrentModeFailure() {
        String logLine = "576460.444: [GC 576460.446: [ParNew (promotion failed): 516864K->516864K(516864K), "
                + "1.9697779 secs]576462.416: [Class Histogram:, 23.3548450 secs]"
                + "576485.771: [CMS: 5074711K->905970K(7331840K), 46.0517345 secs]"
                + "576531.823: [Class Histogram, 12.2976631 secs] 5566845K->905970K(7848704K), "
                + "[CMS Perm : 498279K->443366K(1048576K)], 83.6775207 secs] "
                + "[Times: user=87.62 sys=0.20, real=83.68 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Time stamp not parsed correctly.", 576460444, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 516864, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (905970 - 905970), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (7848704 - 7331840), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 5074711, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 905970, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 7331840, event.getOldSpace());
        Assert.assertEquals("Metaspace begin size not parsed correctly.", 498279, event.getPermOccupancyInit());
        Assert.assertEquals("Metaspace end size not parsed correctly.", 443366, event.getPermOccupancyEnd());
        Assert.assertEquals("Metaspace allocation size not parsed correctly.", 1048576, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 83677, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testTriggerGcLockerInitiatedGc() {
        String logLine = "58626.878: [Full GC (GCLocker Initiated GC)58626.878: [CMS (concurrent mode failure): "
                + "13441202K->12005469K(13631488K), 23.1836190 secs] 19349630K->12005469K(22020096K), "
                + "[CMS Perm : 1257346K->1257346K(2097152K)] icms_dc=100 , 23.1838500 secs] "
                + "[Times: user=22.77 sys=0.39, real=23.18 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE));
        Assert.assertEquals("Time stamp not parsed correctly.", 58626878, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 19349630 - 13441202,
                event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 12005469 - 12005469, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 22020096 - 13631488, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 13441202, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 12005469, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 13631488, event.getOldSpace());
        Assert.assertEquals("Perm begin size not parsed correctly.", 1257346, event.getPermOccupancyInit());
        Assert.assertEquals("Perm end size not parsed correctly.", 1257346, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm allocation size not parsed correctly.", 2097152, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 23183, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testSplitParNewPromotionFailedCmsConcurrentModeFailure() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset5.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_INITIAL_MARK));
        Assert.assertTrue(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    /**
     * Test preprocessing <code>HeapAtGcEvent</code> with underlying <code>CmsSerialOldEvent</code>.
     */
    public void testSplitPrintHeapAtGcParNewConcurrentModeFailureEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset7.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_HEAP_AT_GC));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    public void testSplitParNewPromotionFailedCmsConcurrentModeFailurePermData() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset12.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    public void testSplitParNewCmsConcurrentModeFailurePermData() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset13.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    public void testSplit3LinesParNewPromotionFailedCmsConcurrentModeFailurePermDataEventMarkLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset16.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    /**
     * Test preprocessing <code>ParNewConcurrentModeFailureEvent</code> split over 3 lines.
     * 
     */
    public void testSplit3LinesParNewConcurrentModeFailureEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset29.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.WARN_CMS_INCREMENTAL_MODE + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_CMS_INCREMENTAL_MODE));
    }

    public void testParNewConcurrentModeFailureMixedCmsConcurrentJdk8() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset70.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(LogEventType.CMS_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
        Assert.assertTrue(Analysis.WARN_CMS_INCREMENTAL_MODE + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_CMS_INCREMENTAL_MODE));
    }

    /**
     * Test CMS_SERIAL_OLD heap inspection initiate gc trigger.
     * 
     */
    public void testHeapInspectionInitiatedGcAnalysis() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset72.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(Analysis.WARN_HEAP_INSPECTION_INITIATED_GC + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_HEAP_INSPECTION_INITIATED_GC));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    /**
     * Test preprocessing CMS_SERIAL_OLD triggered by <code>PrintClassHistogramEvent</code> across many lines.
     * 
     */
    public void testParNewPromotionFailedCmsSerialOldPermDataPrintClassHistogramTriggerAcross6Lines() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset82.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }

    /**
     * Test CMS_SERIAL_OLD heap inspection initiate gc trigger.
     * 
     */
    public void testLogLineTriggerHeapDumpedInitiatedGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset92.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(Analysis.WARN_HEAP_DUMP_INITIATED_GC + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_HEAP_DUMP_INITIATED_GC));
        Assert.assertTrue(Analysis.ERROR_SERIAL_GC_CMS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_SERIAL_GC_CMS));
    }
}
