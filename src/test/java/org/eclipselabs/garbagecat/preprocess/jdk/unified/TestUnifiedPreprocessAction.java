/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk.unified;

import java.io.File;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedPreprocessAction extends TestCase {

    public void testLogLinePauseYoungThrowaway() {
        String logLine = "[0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePauseYoungThrowaway5Spaces() {
        String logLine = "[0.041s][info][gc,start     ] GC(0) Pause Young (Allocation Failure)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePauseFullThrowaway() {
        String logLine = "[0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineDefNew() {
        String logLine = "[0.112s][info][gc,heap        ] GC(3) DefNew: 1016K->128K(1152K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineTenured() {
        String logLine = "[0.112s][info][gc,heap        ] GC(3) Tenured: 929K->1044K(1552K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineMetaspace() {
        String logLine = "[0.112s][info][gc,metaspace   ] GC(3) Metaspace: 1222K->1222K(1056768K)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseMarkStart() {
        String logLine = "[4.057s][info][gc,phases,start] GC(2264) Phase 1: Mark live objects";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseMark() {
        String logLine = "[4.062s][info][gc,phases      ] GC(2264) Phase 1: Mark live objects 4.352ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseComputeStart() {
        String logLine = "[4.062s][info][gc,phases,start] GC(2264) Phase 2: Compute new object addresses";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseCompute() {
        String logLine = "[4.063s][info][gc,phases      ] GC(2264) Phase 2: Compute new object addresses 1.165ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseAdjustStart() {
        String logLine = "[4.063s][info][gc,phases,start] GC(2264) Phase 3: Adjust pointers";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseAdjust() {
        String logLine = "[4.065s][info][gc,phases      ] GC(2264) Phase 3: Adjust pointers 2.453ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseMoveStart() {
        String logLine = "[4.065s][info][gc,phases,start] GC(2264) Phase 4: Move objects";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePhaseMove() {
        String logLine = "[4.067s][info][gc,phases      ] GC(2264) Phase 4: Move objects 1.248ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePauseYoungRetain() {
        String logLine = "[0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M->1M(2M) 0.700ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLinePauseOldRetain() {
        String logLine = "[4.067s][info][gc             ] GC(2264) Pause Full (Allocation Failure) 5M->5M(8M) 9.355ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testLogLineTimesData() {
        String logLine = "[0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.UNIFIED.toString() + ".",
                UnifiedPreprocessAction.match(logLine));
    }

    public void testPreprocessing() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset170.txt");
        GcManager gcManager = new GcManager();
        File preprocessedFile = gcManager.preprocess(testFile, null);
        gcManager.store(preprocessedFile, false);
        JvmRun jvmRun = gcManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNIFIED_YOUNG));
    }
}
