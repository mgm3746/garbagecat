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
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author James Livingston
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestG1MixedPauseEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPauseEvent.match(logLine));
        G1MixedPauseEvent event = new G1MixedPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 72598, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 658432, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 525312, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 741376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 168, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs] "
                + "[Times: user=0.22 sys=0.00, real=0.22 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPauseEvent.match(logLine));
        G1MixedPauseEvent event = new G1MixedPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 72598, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 658432, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 525312, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 741376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 168, event.getDuration());
    }

    public void testLogLineSpacesAtEnd() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs] "
                + "[Times: user=0.22 sys=0.00, real=0.22 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPauseEvent.match(logLine));
        G1MixedPauseEvent event = new G1MixedPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 72598, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 658432, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 525312, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 741376, event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 168, event.getDuration());
    }

    public void testLogLinePreprocessedTriggerBeforeG1EvacuationPause() {
        String logLine = "2973.338: [GC pause (G1 Evacuation Pause) (mixed), 0.0457502 secs]"
                + "[Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors: 16.0M->16.0M Heap: 12.9G(30.0G)->11.3G(30.0G)]"
                + " [Times: user=0.19 sys=0.00, real=0.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPauseEvent.match(logLine));
        G1MixedPauseEvent event = new G1MixedPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE));
        Assert.assertEquals("Time stamp not parsed correctly.", 2973338, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 13526630, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 11848909, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 30 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 45, event.getDuration());
    }

    public void testLogLinePreprocessedNoTrigger() {
        String logLine = "3082.652: [GC pause (mixed), 0.0762060 secs]"
                + "[Eden: 1288.0M(1288.0M)->0.0B(1288.0M) Survivors: 40.0M->40.0M Heap: 11.8G(26.0G)->9058.4M(26.0G)]"
                + " [Times: user=0.30 sys=0.00, real=0.08 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPauseEvent.match(logLine));
        G1MixedPauseEvent event = new G1MixedPauseEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3082652, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 12373197, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 9275802, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 26 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 76, event.getDuration());
    }

    public void testLogLinePreprocessedTriggerAfterToSpaceExhausted() {
        String logLine = "615375.044: [GC pause (mixed) (to-space exhausted), 1.5026320 secs]"
                + "[Eden: 3416.0M(3416.0M)->0.0B(3464.0M) Survivors: 264.0M->216.0M Heap: 17.7G(18.0G)->17.8G(18.0G)] "
                + "[Times: user=11.35 sys=0.00, real=1.50 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".",
                G1MixedPauseEvent.match(logLine));
        G1MixedPauseEvent event = new G1MixedPauseEvent(logLine);
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED));
        Assert.assertEquals("Time stamp not parsed correctly.", 615375044, event.getTimestamp());
        Assert.assertEquals("Combined begin size not parsed correctly.", 18559795, event.getCombinedOccupancyInit());
        Assert.assertEquals("Combined end size not parsed correctly.", 18664653, event.getCombinedOccupancyEnd());
        Assert.assertEquals("Combined available size not parsed correctly.", 18 * 1024 * 1024,
                event.getCombinedSpace());
        Assert.assertEquals("Duration not parsed correctly.", 1502, event.getDuration());
    }

    /**
     * Test preprocessing TRIGGER_TO_SPACE_EXHAUSTED after "mixed".
     * 
     */
    public void testLogLineTriggerHeapDumpedInitiatedGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset99.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.G1_MIXED_PAUSE));
        Assert.assertTrue(Analysis.KEY_G1_EVACUATION_FAILURE + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.KEY_G1_EVACUATION_FAILURE));
    }
}
