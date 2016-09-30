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

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

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

    public void testLogLineJdk16() {
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

    public void testLogLineAfterPreprocessing() {
        String logLine = "1504.625: [Full GC1504.625: [CMS: 1172695K->840574K(1549164K), 3.7572507 secs] "
                + "1301420K->840574K(1855852K), [CMS Perm : 226817K->226813K(376168K)], "
                + "3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]";
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

    public void testLogLineIncrementatlModeMetaspace() {
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
}
