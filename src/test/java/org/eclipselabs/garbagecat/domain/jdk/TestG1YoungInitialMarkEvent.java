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
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1YoungInitialMarkEvent extends TestCase {
    public void testInitialMark() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1244357, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 867328, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 613376, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 993280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 56, event.getDuration());
    }

    public void testNotYoungPause() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        Assert.assertFalse("Log line recognized as " + JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
    }

    public void testLogLineMetadataGCThresholdTrigger() {
        String logLine = "1.471: [GC pause (Metadata GC Threshold) (young) (initial-mark) 992M->22M(110G), "
                + "0.0210012 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1471, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        Assert.assertEquals("Combined begin size not parsed correctly.", 992 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 22 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 110 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 21, event.getDuration());
    }

    public void testLogLineGCLockerInitiatedGCTriggerBeforeInitialMark() {
        String logLine = "2.443: [GC pause (GCLocker Initiated GC) (young) (initial-mark) 1061M->52M(110G), "
                + "0.0280096 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2443, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 1061 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 52 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 110 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 28, event.getDuration());
    }

    public void testLogLineToSpaceExhaustedTriggerAfterInitialMark() {
        String logLine = "60346.050: [GC pause (young) (initial-mark) (to-space exhausted), 1.0224350 secs]"
                + "[Eden: 14.2G(14.5G)->0.0B(1224.0M) Survivors: 40.0M->104.0M Heap: 22.9G(26.0G)->19.2G(26.0G)]"
                + " [Times: user=3.03 sys=0.02, real=1.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 60346050, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED));
        Assert.assertEquals("Combined begin size not parsed correctly.", 24012390, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 20132659, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1022, event.getDuration());
    }

    public void testLogLineNoTriggerNoInitialMark() {
        String logLine = "44620.073: [GC pause (young), 0.2752700 secs]"
                + "[Eden: 11.3G(11.3G)->0.0B(11.3G) Survivors: 192.0M->176.0M Heap: 23.0G(26.0G)->11.7G(26.0G)]"
                + " [Times: user=1.09 sys=0.00, real=0.27 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 44620073, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Combined begin size not parsed correctly.", 23 * 1024 * 1024,
                event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 12268339, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 275, event.getDuration());
    }

    public void testLogLinePreprocessedNoTrigger() {
        String logLine = "27474.176: [GC pause (young) (initial-mark), 0.4234530 secs]"
                + "[Eden: 5376.0M(7680.0M)->0.0B(6944.0M) Survivors: 536.0M->568.0M "
                + "Heap: 13.8G(26.0G)->8821.4M(26.0G)] [Times: user=1.66 sys=0.02, real=0.43 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 27474176, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Combined begin size not parsed correctly.", 14470349, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 9033114, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 423, event.getDuration());
    }

    public void testLogLinePreprocessedTriggerMetadataGcThreshold() {
        String logLine = "87.830: [GC pause (Metadata GC Threshold) (young) (initial-mark), 0.2932700 secs]"
                + "[Eden: 716.0M(1850.0M)->0.0B(1522.0M) Survivors: 96.0M->244.0M "
                + "Heap: 2260.0M(5120.0M)->1831.0M(5120.0M)] [Times: user=0.56 sys=0.04, real=0.29 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 87830, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_METADATA_GC_THRESHOLD));
        Assert.assertEquals("Combined begin size not parsed correctly.", 2260 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 1831 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 5120 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 293, event.getDuration());
    }

    public void testLogLinePreprocessedTriggerGcLockerInitiatedGc() {
        String logLine = "6896.482: [GC pause (GCLocker Initiated GC) (young) (initial-mark), 0.0525160 secs]"
                + "[Eden: 16.0M(3072.0M)->0.0B(3070.0M) Survivors: 0.0B->2048.0K "
                + "Heap: 828.8M(5120.0M)->814.8M(5120.0M)] [Times: user=0.09 sys=0.00, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungInitialMarkEvent.match(logLine));
        G1YoungInitialMarkEvent event = new G1YoungInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 6896482, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Combined begin size not parsed correctly.", 848691, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 834355, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 5120 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 52, event.getDuration());
    }

    public void testIsBlocking() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }
}
