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
public class TestG1YoungPauseEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testYoungPause() {
        String logLine = "1113.145: [GC pause (young) 849M->583M(968M), 0.0392710 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
    }

    public void testNotInitialMark() {
        String logLine = "1244.357: [GC pause (young) (initial-mark) 847M->599M(970M), 0.0566840 secs]";
        Assert.assertFalse("Log line recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
    }

    public void testLogLineKilobytes() {
        String logLine = "0.308: [GC pause (young) 8192K->2028K(59M), 0.0078140 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 308, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 8192, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2028, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 60416, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 7, event.getDuration());
    }

    public void testLogLinePreprocessedG1Details() {
        String logLine = "2.847: [GC pause (G1 Evacuation Pause) (young), 0.0414530 secs]"
                + "[Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 136.9M(30.0G)->70.9M(30.0G)]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Time stamp not parsed correctly.", 2847, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 140186, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 72602, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 31457280, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 41, event.getDuration());
    }

    public void testLogLinePreprocessedG1Sizes() {
        String logLine = "0.807: [GC pause (young), 0.00290200 secs][ 29M->2589K(59M)]"
                + " [Times: user=0.01 sys=0.00, real=0.01 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Time stamp not parsed correctly.", 807, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 29 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 2589, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 59 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 2, event.getDuration());
    }

    public void testLogLinePreprocessedG1DetailsTriggerGcLockerInitiatedGc() {
        String logLine = "5.293: [GC pause (GCLocker Initiated GC) (young), 0.0176868 secs]"
                + "[Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 415.0M(30.0G)->313.0M(30.0G)]"
                + " [Times: user=0.01 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC));
        Assert.assertEquals("Time stamp not parsed correctly.", 5293, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 415 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 313 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 30 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 17, event.getDuration());
    }

    public void testLogLinePreprocessedG1DetailsTriggerAfterYoungToSpaceExhausted() {
        String logLine = "27997.968: [GC pause (young) (to-space exhausted), 0.1208740 secs]"
                + "[Eden: 1280.0M(1280.0M)->0.0B(1288.0M) Survivors: 48.0M->40.0M Heap: 18.9G(26.0G)->17.8G(26.0G)]"
                + " [Times: user=0.41 sys=0.02, real=0.12 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED));
        Assert.assertEquals("Time stamp not parsed correctly.", 27997968, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 19818086, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 18664653, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 120, event.getDuration());
    }

    public void testLogLinePreprocessedNoTrigger() {
        String logLine = "44620.073: [GC pause (young), 0.2752700 secs]"
                + "[Eden: 11.3G(11.3G)->0.0B(11.3G) Survivors: 192.0M->176.0M Heap: 23.0G(26.0G)->11.7G(26.0G)]"
                + " [Times: user=1.09 sys=0.00, real=0.27 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Time stamp not parsed correctly.", 44620073, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 23 * 1024 * 1024,
                event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 12268339, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 275, event.getDuration());
    }

    public void testLogLinePreprocessedNoSizeDetails() {
        String logLine = "785,047: [GC pause (young), 0,73936800 secs][Eden: 4096M(4096M)->0B(3528M) "
                + "Survivors: 0B->568M Heap: 4096M(16384M)->567M(16384M)] [Times: user=4,42 sys=0,38, real=0,74 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger() == null);
        Assert.assertEquals("Time stamp not parsed correctly.", 785047, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 4096 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 567 * 1024, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 16384 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 739, event.getDuration());
    }

    public void testLogLinePreprocessedDoubleTrigger() {
        String logLine = "6049.175: [GC pause (G1 Evacuation Pause) (young) (to-space exhausted), 3.1713585 secs]"
                + "[Eden: 27.1G(50.7G)->0.0B(50.7G) Survivors: 112.0M->0.0B Heap: 27.9G(28.0G)->16.1G(28.0G)] "
                + "[Times: user=17.73 sys=0.00, real=3.18 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED));
        Assert.assertEquals("Time stamp not parsed correctly.", 6049175, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 29255270, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 16882074, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 28 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3171, event.getDuration());
    }

    public void testLogLinePreprocessedDatestamp() {
        String logLine = "2016-12-21T14:28:11.672-0500: 0.823: [GC pause (G1 Evacuation Pause) (young), "
                + "0.0124023 secs][Eden: 75.0M(75.0M)->0.0B(66.0M) Survivors: 0.0B->9216.0K "
                + "Heap: 75.0M(1500.0M)->8749.6K(1500.0M)] [Times: user=0.03 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString() + ".",
                G1YoungPauseEvent.match(logLine));
        G1YoungPauseEvent event = new G1YoungPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Time stamp not parsed correctly.", 823, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 75 * 1024, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 8750, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 1500 * 1024, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 12, event.getDuration());
    }
}
