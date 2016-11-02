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
    }

    public void testLogLineAfterPreprocessingNoSpaceAfterFullGC() {
        String logLine = "1504.625: [Full GC1504.625: [CMS: 1172695K->840574K(1549164K), 3.7572507 secs] "
                + "1301420K->840574K(1855852K), [CMS Perm : 226817K->226813K(376168K)], "
                + "3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                CmsSerialOldEvent.match(logLine));
        CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1504625L, event.getTimestamp());
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
    }

    public void testLogLineBailing() {
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
        Assert.assertTrue(Analysis.KEY_HEAP_INSPECTION_INITIATED_GC + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_HEAP_INSPECTION_INITIATED_GC));
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
        Assert.assertTrue(Analysis.KEY_HEAP_DUMP_INITIATED_GC + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_HEAP_DUMP_INITIATED_GC));
    }
}
