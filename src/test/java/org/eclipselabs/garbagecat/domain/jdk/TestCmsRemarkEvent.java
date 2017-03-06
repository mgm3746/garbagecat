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
public class TestCmsRemarkEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "4.506: [GC (CMS Final Remark) [YG occupancy: 100369 K (153344 K)]"
                + "4.506: [GC (CMS Final Remark) 4.506: [ParNew: 100369K->10116K(153344K), 0.0724021 secs] "
                + "100369K->16685K(4177280K), 0.0724907 secs] [Times: user=0.13 sys=0.01, real=0.07 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.CMS_REMARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 253103, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 85, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]  ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
    }

    public void testLogLineWithTimesData() {
        String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
                + "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
                + "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs] "
                + "[Times: user=0.15 sys=0.01, real=0.09 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 253103, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 85, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineJdk8WithTriggerAndDatestamps() {
        String logLine = "13.749: [GC (CMS Final Remark)[YG occupancy: 149636 K (153600 K)]13.749: "
                + "[Rescan (parallel) , 0.0216980 secs]13.771: [weak refs processing, 0.0005180 secs]13.772: "
                + "[scrub string table, 0.0015820 secs] [1 CMS-remark: 217008K(341376K)] "
                + "366644K(494976K), 0.0239510 secs] [Times: user=0.18 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 13749, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        Assert.assertEquals("Duration not parsed correctly.", 23, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineJdk8WithParNewEnd() {
        String logLine = "4.506: [GC (CMS Final Remark) [YG occupancy: 100369 K (153344 K)]"
                + "4.506: [GC (CMS Final Remark) 4.506: [ParNew: 100369K->10116K(153344K), 0.0724021 secs] "
                + "100369K->16685K(4177280K), 0.0724907 secs] [Times: user=0.13 sys=0.01, real=0.07 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4506, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        Assert.assertEquals("Duration not parsed correctly.", 72, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineNoSpaceAfterTrigger() {
        String logLine = "78.251: [GC (CMS Final Remark)[YG occupancy: 2619547 K (8388608 K)]"
                + "78.251: [GC (CMS Final Remark)78.251: [ParNew: 2619547K->569438K(8388608K), 0.3405110 secs] "
                + "6555444K->5043068K(22020096K) icms_dc=100 , 0.3406250 secs] "
                + "[Times: user=2.12 sys=0.01, real=0.34 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 78251, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        Assert.assertEquals("Duration not parsed correctly.", 340, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineParNewTrigger() {
        String logLine = "58427.547: [GC (CMS Final Remark)[YG occupancy: 5117539 K (8388608 K)]"
                + "58427.548: [GC (CMS Final Remark)58427.548: [ParNew (promotion failed): "
                + "5117539K->5001473K(8388608K), 27.6557600 secs] 17958061K->18622281K(22020096K) icms_dc=57 , "
                + "27.6560550 secs] [Times: user=49.10 sys=6.01, real=27.65 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 58427547, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        Assert.assertEquals("Duration not parsed correctly.", 27656, event.getDuration());
        Assert.assertTrue("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineDatestamp() {
        String logLine = "2016-10-27T19:06:06.651-0400: 6.458: [GC[YG occupancy: 480317 K (5505024 K)]6.458: "
                + "[Rescan (parallel) , 0.0103480 secs]6.469: [weak refs processing, 0.0000110 secs]6.469: "
                + "[scrub string table, 0.0001750 secs] [1 CMS-remark: 0K(37748736K)] 480317K(43253760K), "
                + "0.0106300 secs] [Times: user=0.23 sys=0.01, real=0.01 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6458, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 10, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineAllDatestamps() {
        String logLine = "2017-03-04T05:36:05.691-0500: 214.303: [GC[YG occupancy: 1674105 K (2752512 K)]"
                + "2017-03-04T05:36:05.691-0500: 214.303: [Rescan (parallel) , 0.2958890 secs]"
                + "2017-03-04T05:36:05.987-0500: 214.599: [weak refs processing, 0.0046990 secs]"
                + "2017-03-04T05:36:05.992-0500: 214.604: [scrub string table, 0.0023080 secs] "
                + "[1 CMS-remark: 6775345K(7340032K)] 8449451K(10092544K), 0.3035200 secs] "
                + "[Times: user=0.98 sys=0.01, real=0.31 secs] ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 214303, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 303, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }

    public void testLogLineSpaceBeforeYgBlockAndNoSpaceBeforeCmsRemarkBlock() {
        String logLine = "61.013: [GC (CMS Final Remark) [YG occupancy: 237181 K (471872 K)]61.014: [Rescan (parallel)"
                + " , 0.0335675 secs]61.047: [weak refs processing, 0.0011687 secs][1 CMS-remark: 1137616K(1572864K)] "
                + "1374798K(2044736K), 0.0351204 secs] [Times: user=0.12 sys=0.00, real=0.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK.toString() + ".",
                CmsRemarkEvent.match(logLine));
        CmsRemarkEvent event = new CmsRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 61013, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 35, event.getDuration());
        Assert.assertFalse("Incremental Mode not parsed correctly.", event.isIncrementalMode());
    }
}
