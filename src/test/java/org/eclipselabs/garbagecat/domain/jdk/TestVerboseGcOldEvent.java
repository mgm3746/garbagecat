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

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestVerboseGcOldEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 2143132151L, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 1606823, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 1409859, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 2976064, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 12085559, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
    }

    public void testLogLineTriggerMetadataGcThreshold() {
        String logLine = "18129.496: [Full GC (Metadata GC Threshold)  629455K->457103K(3128704K), 4.4946967 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 18129496, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        Assert.assertEquals("Combined begin size not parsed correctly.", 629455, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 457103, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 4494696, event.getDuration());
    }

    public void testLogLineTriggerLastDitchCollection() {
        String logLine = "18134.427: [Full GC (Last ditch collection)  457103K->449140K(3128704K), 5.6081071 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 18134427, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION));
        Assert.assertEquals("Combined begin size not parsed correctly.", 457103, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 449140, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 5608107, event.getDuration());
    }

    public void testLogLineDatestamp() {
        String logLine = "2016-06-22T14:04:51.080+0100: 22561.627: [Full GC (Last ditch collection)  "
                + "500269K->500224K(3128704K), 4.2311820 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 22561627, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_LAST_DITCH_COLLECTION));
        Assert.assertEquals("Combined begin size not parsed correctly.", 500269, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 500224, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 3128704, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 4231182, event.getDuration());
    }

    public void testLogLineG1Sizes() {
        String logLine = "2017-03-20T04:30:01.936+0800: 2950.666: [Full GC 8134M->2349M(8192M), 10.3726320 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 2950666, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Combined begin size not parsed correctly.", 8329216, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2405376, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 8388608, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 10372632, event.getDuration());
    }

    public void testLogLineTriggerAllocationFailure() {
        String logLine = "2017-04-06T15:22:40.708-0500: 303068.960: [Full GC (Allocation Failure)  "
                + "7455264K->4498878K(7992832K), 13.2445067 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 303068960, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_ALLOCATION_FAILURE));
        Assert.assertEquals("Combined begin size not parsed correctly.", 7455264, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 4498878, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 7992832, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 13244506, event.getDuration());
    }

    public void testLogLineTriggerErgonomics() {
        String logLine = "2412.683: [Full GC (Ergonomics)  728595K->382365K(932352K), 1.2268902 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 2412683, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_ERGONOMICS));
        Assert.assertEquals("Combined begin size not parsed correctly.", 728595, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 382365, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 932352, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1226890, event.getDuration());
    }

    public void testLogLineTriggerExplicitGc() {
        String logLine = "8453.778: [Full GC (System.gc())  457601K->176797K(939520K), 1.5623937 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".",
                VerboseGcOldEvent.match(logLine));
        VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
        Assert.assertEquals("Event name incorrect.", JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
        Assert.assertEquals("Time stamp not parsed correctly.", 8453778, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_SYSTEM_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 457601, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 176797, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined allocation size not parsed correctly.", 939520, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1562393, event.getDuration());
    }
}
