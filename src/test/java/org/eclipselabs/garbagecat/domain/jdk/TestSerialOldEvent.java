/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestSerialOldEvent {

    @Test
    public void testIsBlocking() {
        String logLine = "187.159: [Full GC 187.160: "
                + "[Tenured: 97171K->102832K(815616K), 0.6977443 secs] 152213K->102832K(907328K), "
                + "[Perm : 49152K->49154K(49158K)], 0.6929258 secs]";
        assertTrue(JdkUtil.LogEventType.SERIAL_OLD.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLine() {
        String logLine = "187.159: [Full GC 187.160: "
                + "[Tenured: 97171K->102832K(815616K), 0.6977443 secs] 152213K->102832K(907328K), "
                + "[Perm : 49152K->49154K(49158K)], 0.6929258 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_OLD.toString() + ".",
                SerialOldEvent.match(logLine));
        SerialOldEvent event = new SerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 187159, event.getTimestamp());
        assertEquals("Trigger not parsed correctly.", null, event.getTrigger());
        assertEquals("Young begin size not parsed correctly.", 55042, event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", 91712, event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", 97171, event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", 102832, event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", 815616, event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", 49152, event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", 49154, event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", 49158, event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 692925, event.getDuration());
    }

    @Test
    public void testLogLineWhitespaceAtEnd() {
        String logLine = "187.159: [Full GC 187.160: "
                + "[Tenured: 97171K->102832K(815616K), 0.6977443 secs] 152213K->102832K(907328K), "
                + "[Perm : 49152K->49154K(49158K)], 0.6929258 secs]       ";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_OLD.toString() + ".",
                SerialOldEvent.match(logLine));
    }

    @Test
    public void testLogLineJdk16WithTrigger() {
        String logLine = "2.457: [Full GC (System) 2.457: "
                + "[Tenured: 1092K->2866K(116544K), 0.0489980 secs] 11012K->2866K(129664K), "
                + "[Perm : 8602K->8604K(131072K)], 0.0490880 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_OLD.toString() + ".",
                SerialOldEvent.match(logLine));
        SerialOldEvent event = new SerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 2457, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        assertEquals("Young begin size not parsed correctly.", 9920, event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", 0, event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", 13120, event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", 1092, event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", 2866, event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", 116544, event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", 8602, event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", 8604, event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", 131072, event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 49088, event.getDuration());
    }

    @Test
    public void testLogLineWithSerialNewBlock() {
        String logLine = "3727.365: [GC 3727.365: [DefNew: 400314K->400314K(400384K), 0.0000550 secs]"
                + "3727.365: [Tenured: 837793K->597490K(889536K), 44.7498530 secs] 1238107K->597490K(1289920K), "
                + "[Perm : 54745K->54745K(54784K)], 44.7501880 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_OLD.toString() + ".",
                SerialOldEvent.match(logLine));
        SerialOldEvent event = new SerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 3727365, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", 1238107 - 837793, event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", 597490 - 597490, event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", 1289920 - 889536, event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", 837793, event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", 597490, event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", 889536, event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", 54745, event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", 54745, event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", 54784, event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 44750188, event.getDuration());
    }

    @Test
    public void testLogLineWithDateStamps() {
        String logLine = "2017-03-26T13:16:18.668+0200: 24.296: [GC2017-03-26T13:16:18.668+0200: 24.296: [DefNew: "
                + "4928K->511K(4928K), 0.0035715 secs]2017-03-26T13:16:18.684+0200: 24.300: [Tenured: "
                + "11239K->9441K(11328K), 0.1110369 secs] 15728K->9441K(16256K), [Perm : 15599K->15599K(65536K)], "
                + "0.1148688 secs] [Times: user=0.11 sys=0.02, real=0.13 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_OLD.toString() + ".",
                SerialOldEvent.match(logLine));
        SerialOldEvent event = new SerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 24296, event.getTimestamp());
        assertEquals("Young begin size not parsed correctly.", 15728 - 11239, event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", 9441 - 9441, event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", 16256 - 11328, event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", 11239, event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", 9441, event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", 11328, event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", 15599, event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", 15599, event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", 65536, event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 114868, event.getDuration());
    }

    @Test
    public void testLogLineFullGcWithMetadatGcThresholdTrigger() {
        String logLine = "2.447: [Full GC (Metadata GC Threshold) 2.447: [Tenured: 0K->12062K(524288K), "
                + "0.1248607 secs] 62508K->12062K(760256K), [Metaspace: 20526K->20526K(1069056K)], 0.1249442 secs] "
                + "[Times: user=0.18 sys=0.08, real=0.13 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_OLD.toString() + ".",
                SerialOldEvent.match(logLine));
        SerialOldEvent event = new SerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 2447, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        assertEquals("Young begin size not parsed correctly.", 62508 - 0, event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", 12062 - 12062, event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", 760256 - 524288, event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", 0, event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", 12062, event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", 524288, event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", 20526, event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", 20526, event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", 1069056, event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 124944, event.getDuration());
    }

    @Test
    public void testLogLineGcWithAllocationFailureTrigger() {
        String logLine = "38.922: [GC (Allocation Failure) 38.922: [DefNew: 229570K->229570K(235968K), "
                + "0.0000182 secs]38.922: [Tenured: 459834K->79151K(524288K), 0.2871383 secs] "
                + "689404K->79151K(760256K), [Metaspace: 68373K->68373K(1114112K)], 0.2881307 secs] "
                + "[Times: user=0.30 sys=0.00, real=0.29 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_OLD.toString() + ".",
                SerialOldEvent.match(logLine));
        SerialOldEvent event = new SerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 38922, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        assertEquals("Young begin size not parsed correctly.", 689404 - 459834, event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", 79151 - 79151, event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", 760256 - 524288, event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", 459834, event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", 79151, event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", 524288, event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", 68373, event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", 68373, event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", 1114112, event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 288130, event.getDuration());
    }

    @Test
    public void testLogLineGcPromotionFailedTrigger() {
        String logLine = "116.957: [GC (Allocation Failure) 116.957: [DefNew (promotion failed) : "
                + "229660K->235967K(235968K), 0.2897884 secs]117.247: [Tenured: 524288K->144069K(524288K), "
                + "0.3905008 secs] 674654K->144069K(760256K), [Metaspace: 65384K->65384K(1114112K)], 0.6804246 secs] "
                + "[Times: user=0.67 sys=0.01, real=0.69 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL_OLD.toString() + ".",
                SerialOldEvent.match(logLine));
        SerialOldEvent event = new SerialOldEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 116957, event.getTimestamp());
        assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_PROMOTION_FAILED));
        assertEquals("Young begin size not parsed correctly.", 674654 - 524288, event.getYoungOccupancyInit());
        assertEquals("Young end size not parsed correctly.", 144069 - 144069, event.getYoungOccupancyEnd());
        assertEquals("Young available size not parsed correctly.", 760256 - 524288, event.getYoungSpace());
        assertEquals("Old begin size not parsed correctly.", 524288, event.getOldOccupancyInit());
        assertEquals("Old end size not parsed correctly.", 144069, event.getOldOccupancyEnd());
        assertEquals("Old allocation size not parsed correctly.", 524288, event.getOldSpace());
        assertEquals("Perm gen begin size not parsed correctly.", 65384, event.getPermOccupancyInit());
        assertEquals("Perm gen end size not parsed correctly.", 65384, event.getPermOccupancyEnd());
        assertEquals("Perm gen allocation size not parsed correctly.", 1114112, event.getPermSpace());
        assertEquals("Duration not parsed correctly.", 680424, event.getDuration());
    }
}
