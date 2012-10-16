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
public class TestCmsConcurrentModeFailurePreprocessAction extends TestCase {

    public void testCmsSerialOldPrecleanLine() {
        String priorLogLine = "";
        String logLine = "77412.576: [Full GC 877412.576: [CMS877412.858: [CMS-concurrent-preclean: " + "4.917/4.997 secs] [Times: user=6.45 sys=0.44, real=5.00 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testCmsSerialOldAbortablePrecleanLine() {
        String priorLogLine = "";
        String logLine = "85217.903: [Full GC 85217.903: [CMS85217.919: [CMS-concurrent-abortable-preclean: " + "0.723/3.756 secs] [Times: user=2.54 sys=0.08, real=3.76 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testCmsSerialOldAbortedAbortablePrecleanLine() {
        String priorLogLine = "";
        String logLine = "1361123.817: [Full GC (System) 1361123.818: [CMS CMS: abort preclean due to time " + "1361123.838: [CMS-concurrent-abortable-preclean: 5.030/5.089 secs] "
                + "[Times: user=5.68 sys=0.30, real=5.09 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testCmsSerialOldMarkLine() {
        String priorLogLine = "";
        String logLine = "85238.030: [Full GC 85238.030: [CMS85238.672: [CMS-concurrent-mark: " + "0.666/0.686 secs] [Times: user=1.40 sys=0.01, real=0.69 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testCmsSerialOldSweepLine() {
        String priorLogLine = "";
        String logLine = "827123.748: [Full GC (System) 827123.748: [CMS827125.337: " + "[CMS-concurrent-sweep: 2.819/2.862 secs] [Times: user=3.11 sys=0.13, real=2.86 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testCmsSerialOldBailingOutLine() {
        String priorLogLine = "";
        String logLine = "4300.825: [Full GC 4300.825: [CMSbailing out to foreground collection";
        String nextLogLine = "4310.434: [CMS-concurrent-mark: 10.548/10.777 secs] " + "[Times: user=40.43 sys=3.94, real=10.78 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testParNewBailingOutLine() {
        String priorLogLine = "";
        String logLine = "2137.769: [GC 2137.769: [ParNew (promotion failed): 242304K->242304K(242304K), " + "8.4066690 secs]2146.176: [CMSbailing out to foreground collection";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testParNewHotSpotWarningBailingOutLine() {
        String priorLogLine = "";
        String logLine = "1901.217: [GC 1901.217: [ParNew: 261760K->261760K(261952K), 0.0000570 secs]" + "1901.217: [CMSJava HotSpot(TM) Server VM warning: bailing out to foreground collection";
        String nextLogLine = "1907.974: [CMS-concurrent-mark: 23.751/40.476 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testCmsSerialOldStandAloneMarkLine() {
        String priorLogLine = "4300.825: [Full GC 4300.825: [CMSbailing out to foreground collection";
        String logLine = "4310.434: [CMS-concurrent-mark: 10.548/10.777 secs] " + "[Times: user=40.43 sys=3.94, real=10.78 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testParNewPromotionFailedPrecleanLine() {
        String priorLogLine = "";
        String logLine = "47101.598: [GC 47101.599: [ParNew (promotion failed): 242304K->242304K(242304K), " + "3.4779001 secs]47105.077: [CMS47106.206: [CMS-concurrent-preclean: 1.592/5.189 secs] "
                + "[Times: user=7.56 sys=2.55, real=5.19 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testParNewPromotionFailedAbortablePrecleanLine() {
        String priorLogLine = "";
        String logLine = "233333.318: [GC 233333.319: [ParNew (promotion failed): 673108K->673108K(707840K), "
                + "1.5366054 secs]233334.855: [CMS233334.856: [CMS-concurrent-abortable-preclean: 12.033/27.431 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testParNewPromotionFailedAbortedAbortablePrecleanLine() {
        String priorLogLine = "";
        String logLine = "1319504.530: [GC 1319504.531: [ParNew: 786379K->786379K(917504K), " + "0.0000350 secs]1319504.531: [CMS CMS: abort preclean due to time 1319504.546: "
                + "[CMS-concurrent-abortable-preclean: 0.682/5.152 secs] " + "[Times: user=1.47 sys=0.31, real=5.15 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testParNewPromotionFailedMarkLine() {
        String priorLogLine = "";
        String logLine = "2746.109: [GC 2746.109: [ParNew (promotion failed): 242303K->242304K(242304K), " + "1.3009892 secs]2747.410: [CMS2755.518: [CMS-concurrent-mark: 11.734/13.504 secs] "
                + "[Times: user=55.81 sys=5.11, real=13.50 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testParNewMarkLine() {
        String priorLogLine = "";
        String logLine = "3070.289: [GC 3070.289: [ParNew: 207744K->207744K(242304K), 0.0000682 secs]" + "3070.289: [CMS3081.621: [CMS-concurrent-mark: 11.907/12.958 secs] "
                + "[Times: user=45.31 sys=3.93, real=12.96 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testParNewSweepLine() {
        String priorLogLine = "";
        String logLine = "5300.084: [GC 5300.084: [ParNew: 58640K->196K(242304K), 0.0278816 secs]" + "5300.112: [CMS5302.628: [CMS-concurrent-sweep: 7.250/7.666 secs] "
                + "[Times: user=14.81 sys=0.44, real=7.67 secs]";
        String nextLogLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testConcurrentModeFailurePrecleanLine() {
        String priorLogLine = "877412.576: [Full GC 877412.576: [CMS877412.858: [CMS-concurrent-preclean: " + "4.917/4.997 secs] [Times: user=6.45 sys=0.44, real=5.00 secs]";
        String logLine = " (concurrent mode failure): 1572863K->1547074K(1572864K), 10.7226790 secs] " + "2490334K->1547074K(2490368K), [CMS Perm : 46357K->46354K(77352K)], 10.7239680 secs] "
                + "[Times: user=10.72 sys=0.00, real=10.73 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testConcurrentModeFailureAbortablePrecleanLine() {
        String priorLogLine = "85217.903: [Full GC 85217.903: [CMS85217.919: [CMS-concurrent-abortable-preclean: " + "0.723/3.756 secs] [Times: user=2.54 sys=0.08, real=3.76 secs]";
        String logLine = " (concurrent mode failure): 439328K->439609K(4023936K), 2.7153820 secs] " + "448884K->439609K(4177280K), [CMS Perm : 262143K->262143K(262144K)], 2.7156150 secs] "
                + "[Times: user=3.35 sys=0.00, real=2.72 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testConcurrentModeFailureMarkLine() {
        String priorLogLine = "3070.289: [GC 3070.289: [ParNew: 207744K->207744K(242304K), 0.0000682 secs]" + "3070.289: [CMS3081.621: [CMS-concurrent-mark: 11.907/12.958 secs] "
                + "[Times: user=45.31 sys=3.93, real=12.96 secs]";
        String logLine = " (concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] " + "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
                + "[Times: user=104.69 sys=3.63, real=79.05 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testConcurrentModeInterruptedSweepLine() {
        String priorLogLine = "827123.748: [Full GC (System) 827123.748: [CMS827125.337: " + "[CMS-concurrent-sweep: 2.819/2.862 secs] [Times: user=3.11 sys=0.13, real=2.86 secs]";
        String logLine = " (concurrent mode interrupted): 1177627K->890349K(1572864K), 7.1730360 secs] " + "2048750K->890349K(2490368K), [CMS Perm : 46357K->46352K(77352K)], 7.1743670 secs] "
                + "[Times: user=7.17 sys=0.00, real=7.18 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testPromotionFailedNoConcurrentModeFailureLine() {
        String priorLogLine = "88063.609: [GC 88063.610: [ParNew (promotion failed): 513856K->513856K" + "(513856K), 4.0911197 secs]88067.701: [CMS88067.742: [CMS-concurrent-reset: 0.309/4.421 secs]"
                + " [Times: user=9.62 sys=3.73, real=4.42 secs]";
        String logLine = ": 10612422K->4373474K(11911168K), 76.7523274 secs] 11075362K->4373474K(12425024K), " + "[CMS Perm : 214530K->213777K(524288K)], 80.8440551 secs] "
                + "[Times: user=80.01 sys=5.57, real=80.84 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }

    public void testNoConcurrentModeFailureLine() {
        String priorLogLine = "198.712: [Full GC 198.712: [CMS198.733: [CMS-concurrent-reset: 0.061/1.405 secs]";
        String logLine = ": 14037K->31492K(1835008K), 0.7953140 secs] 210074K->31492K(2096960K), " + "[CMS Perm : 27817K->27784K(131072K)], 0.7955670 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString() + ".", CmsConcurrentModeFailurePreprocessAction.match(logLine,
                priorLogLine, nextLogLine));
    }
}
