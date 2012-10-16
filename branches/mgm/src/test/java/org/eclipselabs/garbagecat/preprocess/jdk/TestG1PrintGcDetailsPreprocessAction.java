/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestG1PrintGcDetailsPreprocessAction extends TestCase {

    public void testGcPauseLine() {
        String logLine = "0.304: [GC pause (young), 0.00376500 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testParallelTimeLine() {
        String logLine = "   [Parallel Time:   3.6 ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testGcWorkerStartTimeLine() {
        String logLine = "      [GC Worker Start Time (ms):  304.3  304.4  305.7  305.7]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testUpdateRSLine() {
        String logLine = "      [Update RS (ms):  0.0  0.0  0.0  0.0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testAvgLine() {
        String logLine = "       Avg:   0.0, Min:   0.0, Max:   0.0]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testMarkStackScanningLine() {
        String logLine = "      [Mark Stack Scanning (ms):  0.0  0.0  0.0  0.0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testScanRSLine() {
        String logLine = "      [Scan RS (ms):  0.0  0.0  0.0  0.0";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testObjectCopyLine() {
        String logLine = "      [Object Copy (ms):  1.9  1.3  1.2  1.2";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testTerminationLine() {
        String logLine = "      [Termination (ms):  0.1  0.0  0.5  0.5";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testTerminationAttemptsLine() {
        String logLine = "         [Termination Attempts : 1 1 1 1";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testSumLine() {
        String logLine = "          Sum: 4, Avg: 1, Min: 1, Max: 1]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testGcWorkerEndTimeLine() {
        String logLine = "      [GC Worker End Time (ms):  308.0  308.0  308.0  308.0]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testOther1Line() {
        String logLine = "      [Other:   0.7 ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testOther2Line() {
        String logLine = "   [Other:   0.1 ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testClearCTLine() {
        String logLine = "   [Clear CT:   0.0 ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testChooseCSetLine() {
        String logLine = "      [Choose CSet:   0.0 ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testSizeLine() {
        String logLine = "   [ 8192K->2112K(59M)]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }

    public void testTimesBlockLine() {
        String logLine = " [Times: user=0.01 sys=0.00, real=0.01 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.G1_PRINT_GC_DETAILS.toString()
                + ".", G1PrintGcDetailsPreprocessAction.match(logLine));
    }
}
