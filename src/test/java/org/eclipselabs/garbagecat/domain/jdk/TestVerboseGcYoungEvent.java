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
public class TestVerboseGcYoungEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 2205570508L, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 1726387, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 773247, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3097984, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 231, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "2205570.508: [GC 1726387K->773247K(3097984K), 0.2318035 secs]        ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
    }

    public void testLogLineMissingBeginningOccupancy() {
        String logLine = "90.168: [GC 876593K(1851392K), 0.0701780 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 90168, event.getTimestamp());
        // We set beginging to end occupancy
        Assert.assertEquals("Combined begin size not parsed correctly.", 876593, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 876593, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 1851392, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 70, event.getDuration());
    }

    public void testLogLineTriggerAllocationFailure() {
        String logLine = "4.970: [GC (Allocation Failure)  136320K->18558K(3128704K), 0.1028162 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 4970, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 136320, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 18558, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 102, event.getDuration());
    }

    public void testLogLineTriggerCmsInitialMark() {
        String logLine = "12.915: [GC (CMS Initial Mark)  59894K(3128704K), 0.0058845 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 12915, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_INITIAL_MARK));
        // We set beginging to end occupancy
        Assert.assertEquals("Combined begin size not parsed correctly.", 59894, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 59894, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 5, event.getDuration());
    }

    public void testLogLineTriggerCmsFinalRemark() {
        String logLine = "70.096: [GC (CMS Final Remark)  521627K(3128704K), 0.2481277 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 70096, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_FINAL_REMARK));
        // We set beginging to end occupancy
        Assert.assertEquals("Combined begin size not parsed correctly.", 521627, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 521627, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 248, event.getDuration());
    }

    public void testLogLineTriggerGcLockerInitiatedGc() {
        String logLine = "37.357: [GC (GCLocker Initiated GC)  128035K->124539K(3128704K), 0.0713498 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 37357, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 128035, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 124539, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 71, event.getDuration());
    }

    public void testLogLineWithDashes() {
        String logLine = "1582.746: [GC-- 5524217K->5911480K(5911488K), 1.5564360 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 1582746, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 5524217, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 5911480, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 5911488, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1556, event.getDuration());
    }

    public void testLogLineWithDatestamp() {
        String logLine = "2016-07-22T11:49:00.678+0100: 4.970: [GC (Allocation Failure)  136320K->18558K(3128704K), "
                + "0.1028162 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 4970, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 136320, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 18558, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 102, event.getDuration());
    }

    public void testLogLineTriggerMetadataGcThreshold() {
        String logLine = "20.748: [GC (Metadata GC Threshold)  288163K->251266K(1253376K), 0.0183041 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 20748, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        Assert.assertEquals("Combined begin size not parsed correctly.", 288163, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 251266, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 1253376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 18, event.getDuration());
    }

    public void testLogLineTriggerExplicitGc() {
        String logLine = "8453.745: [GC (System.gc())  525225K->457601K(939520K), 0.0325441 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString() + ".",
                VerboseGcYoungEvent.match(logLine));
        VerboseGcYoungEvent event = new VerboseGcYoungEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_YOUNG.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 8453745, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 525225, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 457601, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 939520, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 32, event.getDuration());
    }
}
