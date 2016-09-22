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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParNewEvent extends TestCase {

    public void testLogLine() {
        String logLine = "20.189: [GC 20.190: [ParNew: 86199K->8454K(91712K), 0.0375060 secs] "
                + "89399K->11655K(907328K), 0.0387074 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 20189, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 86199, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 8454, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 91712, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 3200, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 3201, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 815616, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 38, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "68331.885: [GC 68331.885: [ParNew: 149120K->18211K(149120K), "
                + "0.0458577 secs] 4057776K->3931241K(8367360K), 0.0461448 secs] "
                + "[Times: user=0.34 sys=0.01, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 68331885, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 149120, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 18211, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 149120, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 3908656, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 3913030, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 8218240, event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 46, event.getDuration());
    }

    public void testLogLineWithIcmsDcData() {
        String logLine = "42514.965: [GC 42514.966: [ParNew: 54564K->1006K(59008K), 0.0221640 secs] "
                + "417639K->364081K(1828480K) icms_dc=0 , 0.0225090 secs] "
                + "[Times: user=0.05 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 42514965, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", 54564, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 1006, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 59008, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (417639 - 54564), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (364081 - 1006), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (1828480 - 59008), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 22, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "68331.885: [GC 68331.885: [ParNew: 149120K->18211K(149120K), "
                + "0.0458577 secs] 4057776K->3931241K(8367360K), 0.0461448 secs] "
                + "[Times: user=0.34 sys=0.01, real=0.05 secs]    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
    }

    public void testLogLineHugeTimestamp() {
        String logLine = "4687597.901: [GC 4687597.901: [ParNew: 342376K->16369K(368640K), "
                + "0.0865160 secs] 1561683K->1235676K(2056192K), 0.0869060 secs]";
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4687597901L, event.getTimestamp());
    }

    public void testLogLineAfterPreprocessing() {
        String logLine = "13.086: [GC13.086: [ParNew: 272640K->33532K(306688K), 0.0381419 secs] "
                + "272640K->33532K(1014528K), 0.0383306 secs] " + "[Times: user=0.11 sys=0.02, real=0.04 secs]";
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 13086L, event.getTimestamp());
    }

    public void testLogJdk8WithTrigger() {
        String logLine = "6.703: [GC (Allocation Failure) 6.703: [ParNew: 886080K->11485K(996800K), 0.0193349 secs] "
                + "886080K->11485K(1986432K), 0.0198375 secs] [Times: user=0.09 sys=0.01, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6703, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Young begin size not parsed correctly.", 886080, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 11485, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 996800, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (886080 - 886080), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (11485 - 11485), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (1986432 - 996800), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 19, event.getDuration());
    }

    public void testLogJdk8NoSpaceAfterTrigger() {
        String logLine = "1.948: [GC (Allocation Failure)1.948: [ParNew: 136576K->17023K(153600K), 0.0303800 secs] "
                + "136576K->19515K(494976K), 0.0305360 secs] [Times: user=0.10 sys=0.01, real=0.03 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1948, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Young begin size not parsed correctly.", 136576, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 17023, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 153600, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (136576 - 136576), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (19515 - 17023), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (494976 - 153600), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 30, event.getDuration());
    }

    public void testLogGcLockerTrigger() {
        String logLine = "2.480: [GC (GCLocker Initiated GC) 2.480: [ParNew: 1228800K->30695K(1382400K), "
                + "0.0395910 secs] 1228800K->30695K(8235008K), 0.0397980 secs] "
                + "[Times: user=0.23 sys=0.01, real=0.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                ParNewEvent.match(logLine));
        ParNewEvent event = new ParNewEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2480, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Young begin size not parsed correctly.", 1228800, event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", 30695, event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", 1382400, event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", (1228800 - 1228800), event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", (30695 - 30695), event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", (8235008 - 1382400), event.getOldSpace());
        Assert.assertEquals("Duration not parsed correctly.", 39, event.getDuration());
    }
}
