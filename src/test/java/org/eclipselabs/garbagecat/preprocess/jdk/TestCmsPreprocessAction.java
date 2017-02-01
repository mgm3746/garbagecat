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
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.service.GcManager;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.PreprocessActionType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestCmsPreprocessAction extends TestCase {

    public void testLogLineParNewMixedConcurrent() {
        String logLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] [Times: user=1.56 sys=0.01, real=2.23 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineParNewMixedConcurrentWithWhitespaceEnd() {
        String logLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineEnd() {
        String logLine = ": 153599K->17023K(153600K), 0.0383370 secs] 229326K->114168K(494976K), 0.0384820 secs] "
                + "[Times: user=0.15 sys=0.01, real=0.04 secs]";
        String priorLogLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, null));
    }

    public void testLogLineEndWithWhitespaceEnd() {
        String logLine = ": 153599K->17023K(153600K), 0.0383370 secs] 229326K->114168K(494976K), 0.0384820 secs] "
                + "[Times: user=0.15 sys=0.01, real=0.04 secs]    ";
        String priorLogLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, null));
    }

    public void testLogLineParNewNoTriggerMixedConcurrent() {
        String logLine = "10.963: [GC10.963: [ParNew10.977: [CMS-concurrent-abortable-preclean: 0.088/0.197 secs] "
                + "[Times: user=0.33 sys=0.05, real=0.20 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineParNewPromotionFailed() {
        String logLine = "233333.318: [GC 233333.319: [ParNew (promotion failed): 673108K->673108K(707840K), "
                + "1.5366054 secs]233334.855: [CMS233334.856: [CMS-concurrent-abortable-preclean: 12.033/27.431 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineMiddle() {
        String logLine = "1907.974: [CMS-concurrent-mark: 23.751/40.476 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, null));
    }

    public void testLogLineParNewTriggerMixedConcurrentJdk8() {
        String logLine = "45.574: [GC (Allocation Failure) 45.574: [ParNew45.670: [CMS-concurrent-abortable-preclean: "
                + "3.276/4.979 secs] [Times: user=7.75 sys=0.28, real=4.98 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineParNewConcurrentModeFailureMixedConcurrentJdk8() {
        String logLine = "719.519: [GC (Allocation Failure) 719.521: [ParNew: 1382400K->1382400K(1382400K), "
                + "0.0000470 secs]719.521: [CMS722.601: [CMS-concurrent-mark: 3.567/3.633 secs] "
                + "[Times: user=10.91 sys=0.69, real=3.63 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineCmsSerialOldMixedConcurrentMark() {
        String logLine = "2016-02-26T16:37:58.740+1100: 44.684: [Full GC2016-02-26T16:37:58.740+1100: 44.684: [CMS"
                + "2016-02-26T16:37:58.933+1100: 44.877: [CMS-concurrent-mark: 1.508/2.428 secs] "
                + "[Times: user=3.44 sys=0.49, real=2.42 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, null));
    }

    public void testLogLineCmsSerialOldMixedConcurrentAbortablePreclean() {
        String logLine = "85217.903: [Full GC 85217.903: [CMS85217.919: [CMS-concurrent-abortable-preclean: "
                + "0.723/3.756 secs] [Times: user=2.54 sys=0.08, real=3.76 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, null));
    }

    public void testLogLineCmsSerialOldMixedConcurrentSpaceAfterGC() {
        String logLine = "85238.030: [Full GC 85238.030: [CMS85238.672: [CMS-concurrent-mark: 0.666/0.686 secs] "
                + "[Times: user=1.40 sys=0.01, real=0.69 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, null));
    }

    public void testLogLineCmsSerialOldWithTriggerMixedConcurrent() {
        String logLine = "706.707: [Full GC (Allocation Failure) 706.708: [CMS709.137: [CMS-concurrent-mark: "
                + "3.381/5.028 secs] [Times: user=23.92 sys=3.02, real=5.03 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, null));
    }

    public void testLogLineEndWithPerm() {
        String logLine = " (concurrent mode failure): 1218548K->413373K(1465840K), 1.3656970 secs] "
                + "1229657K->413373K(1581168K), [CMS Perm : 83805K->80520K(83968K)], 1.3659420 secs] "
                + "[Times: user=1.33 sys=0.01, real=1.37 secs]";
        String priorLogLine = "44.684: [Full GC44.684: [CMS44.877: [CMS-concurrent-mark: 1.508/2.428 secs] "
                + "[Times: user=3.44 sys=0.49, real=2.42 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, null));
    }

    public void testLogLineEndWithMetaspace() {
        String logLine = " (concurrent mode failure): 2655937K->2373842K(2658304K), 11.6746550 secs] "
                + "3973407K->2373842K(4040704K), [Metaspace: 72496K->72496K(1118208K)] icms_dc=77 , 11.6770830 secs] "
                + "[Times: user=14.05 sys=0.02, real=11.68 secs]";
        String priorLogLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, null));
    }

    public void testLogLineParNewNoTriggerMixedConcurrentWithCommas() {
        String logLine = "32552,602: [GC32552,602: [ParNew32552,610: "
                + "[CMS-concurrent-abortable-preclean: 3,090/4,993 secs] "
                + "[Times: user=3,17 sys=0,02, real=5,00 secs]";
        String nextLogLine = " (concurrent mode failure): 2542828K->2658278K(2658304K), 12.3447910 secs] "
                + "3925228K->2702358K(4040704K), [Metaspace: 72175K->72175K(1118208K)] icms_dc=100 , 12.3480570 secs] "
                + "[Times: user=15.38 sys=0.02, real=12.35 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineEndWithCommas() {
        String logLine = ": 289024K->17642K(306688K), 0,0788160 secs] 4086255K->3814874K(12548864K), 0,0792920 secs] "
                + "[Times: user=0,28 sys=0,00, real=0,08 secs]";
        String priorLogLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] "
                + "[Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, null));
    }

    public void testLogLineSerialBailing() {
        String logLine = "4300.825: [Full GC 4300.825: [CMSbailing out to foreground collection";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, null));
    }

    public void testLogLineParNewBailing() {
        String logLine = "2137.769: [GC 2137.769: [ParNew (promotion failed): 242304K->242304K(242304K), "
                + "8.4066690 secs]2146.176: [CMSbailing out to foreground collection";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, null));
    }

    public void testLogLineParNewHotspotBailing() {
        String logLine = "1901.217: [GC 1901.217: [ParNew: 261760K->261760K(261952K), 0.0000570 secs]1901.217: "
                + "[CMSJava HotSpot(TM) Server VM warning: bailing out to foreground collection";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, ""));
    }

    public void testLogLineParTriggerPromotionFailed() {
        String logLine = "182314.858: [GC 182314.859: [ParNew (promotion failed)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, ""));
    }

    public void testLogLineParNewConcurrentModeFailurePermDataMixedConcurrentSweep() {
        String logLine = "11202.526: [GC (Allocation Failure) 1202.528: [ParNew: 1355422K->1355422K(1382400K), "
                + "0.0000500 secs]1202.528: [CMS1203.491: [CMS-concurrent-sweep: 1.009/1.060 secs] "
                + "[Times: user=1.55 sys=0.12, real=1.06 secs]";
        String nextLogLine = " (concurrent mode failure): 2656311K->2658289K(2658304K), 9.3575580 secs] "
                + "4011734K->2725109K(4040704K), [Metaspace: 72111K->72111K(1118208K)], 9.3610080 secs] "
                + "[Times: user=9.35 sys=0.01, real=9.36 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineCmsSerialOldWithConcurrentModeFailureMixedConcurrentPreclean() {
        String logLine = "1278.200: [Full GC (Allocation Failure) 1278.202: [CMS1280.173: "
                + "[CMS-concurrent-preclean: 2.819/2.865 secs] [Times: user=6.97 sys=0.41, real=2.87 secs]";
        String nextLogLine = " (concurrent mode failure): 2658303K->2658303K(2658304K), 9.1546180 secs] "
                + "4040703K->2750110K(4040704K), [Metaspace: 72113K->72113K(1118208K)], 9.1581450 secs] "
                + "[Times: user=9.15 sys=0.00, real=9.16 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineCmsSerialOldWithConcurrentModeFailureMixedConcurrentSweep() {
        String logLine = "2440.336: [Full GC (Allocation Failure) 2440.338: [CMS"
                + "2440.542: [CMS-concurrent-sweep: 1.137/1.183 secs] [Times: user=5.33 sys=0.51, real=1.18 secs]";
        String nextLogLine = " (concurrent mode failure): 2658304K->2658303K(2658304K), 9.4908960 secs] "
                + "4040703K->2996946K(4040704K), [Metaspace: 72191K->72191K(1118208K)], 9.4942330 secs] "
                + "[Times: user=9.49 sys=0.00, real=9.49 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, null, nextLogLine));
    }

    public void testLogLineParNewMixedCmsConcurrentAbortablePreclean() {
        String priorLogLine = "";
        String logLine = "2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: "
                + "0.043/0.144 secs] [Times: user=0.58 sys=0.03, real=0.14 secs]";
        String nextLogLine = ": 212981K->3156K(242304K), 0.0364435 secs] 4712182K->4502357K(4971420K), "
                + "0.0368807 secs] [Times: user=0.18 sys=0.02, real=0.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineParNewMixedCmsConcurrentAbortablePreclean2() {
        String priorLogLine = "2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: "
                + "0.043/0.144 secs] [Times: user=0.58 sys=0.03, real=0.14 secs]";
        String logLine = ": 212981K->3156K(242304K), 0.0364435 secs] 4712182K->4502357K(4971420K), "
                + "0.0368807 secs] [Times: user=0.18 sys=0.02, real=0.04 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineParNewMixedCmsConcurrentSweep() {
        String priorLogLine = "";
        String logLine = "1821.661: [GC 1821.661: [ParNew1821.661: [CMS-concurrent-sweep: "
                + "42.841/48.076 secs] [Times: user=19.45 sys=0.45, real=48.06 secs]";
        String nextLogLine = ": 36500K->3770K(38336K), 0.1767060 secs] 408349K->375618K(2092928K), "
                + "0.1769190 secs] [Times: user=0.05 sys=0.00, real=0.18 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineConcurrentModeInterrupted() {
        String priorLogLine = "";
        String logLine = " (concurrent mode interrupted): 861863K->904027K(1797568K), 42.9053262 secs] "
                + "1045947K->904027K(2047232K), [CMS Perm : 252246K->252202K(262144K)], 42.9070278 secs] "
                + "[Times: user=43.11 sys=0.18, real=42.91 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLinePrintHeapAtGcBeginSerial() {
        String priorLogLine = "";
        String logLine = "28282.075: [Full GC {Heap before gc invocations=528:";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLinePrintHeapAtGcBeginParNew() {
        String priorLogLine = "";
        String logLine = "27067.966: [GC {Heap before gc invocations=498:";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogMiddleSerialConcurrentPrecleanMixed() {
        String priorLogLine = "";
        String logLine = "28282.075: [CMS28284.687: [CMS-concurrent-preclean: 3.706/3.706 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogMiddleParNewConcurrentAbortablePrecleanMixed() {
        String priorLogLine = "";
        String logLine = "27067.966: [ParNew: 261760K->261760K(261952K), 0.0000160 secs]27067.966: [CMS"
                + "27067.966: [CMS-concurrent-abortable-preclean: 2.272/29.793 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogMiddleParNewConcurrentMarkMixed() {
        String priorLogLine = "";
        String logLine = "28308.701: [ParNew (promotion failed): 261951K->261951K(261952K), 0.7470390 secs]28309.448: "
                + "[CMS28312.544: [CMS-concurrent-mark: 5.114/5.863 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLinePrintHeapAtGcMiddleSerial() {
        String priorLogLine = "";
        String logLine = "49830.934: [CMS: 1640998K->1616248K(3407872K), 11.0964500 secs] "
                + "1951125K->1616248K(4193600K), [CMS Perm : 507386K->499194K(786432K)]"
                + "Heap after gc invocations=147:";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLinePrintClassHistogramMiddleSerial() {
        String priorLogLine = "";
        String logLine = "11700.930: [CMS: 2844387K->635365K(7331840K), 46.4488813 secs]11747.379: [Class Histogram";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLinePrintHeapAtGcMiddleSerialConcurrentModeFailure() {
        String priorLogLine = "";
        String logLine = " (concurrent mode failure): 1179601K->1179648K(1179648K), 10.7510650 secs] "
                + "1441361K->1180553K(1441600K), [CMS Perm : 71172K->71171K(262144K)]Heap after gc invocations=529:";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLinePrintHeapAtGcParNewConcurrentModeFailure() {
        String priorLogLine = "";
        String logLine = " (concurrent mode failure): 1147900K->1155037K(1179648K), 7.3953900 secs] "
                + "1409660K->1155037K(1441600K)Heap after gc invocations=499:";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineDuration() {
        String priorLogLine = "";
        String logLine = ", 10.7515460 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineClassHistogramTrigger() {
        String priorLogLine = "";
        String logLine = "1662.232: [Full GC 11662.233: [Class Histogram:";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineRetainMiddleClassHistogram() {
        String priorLogLine = "";
        String logLine = ": 516864K->516864K(516864K), 2.0947428 secs]182316.954: [Class Histogram: ";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineRetainEndClassHistogram() {
        String priorLogLine = "";
        String logLine = " 3863904K->756393K(7848704K), [CMS Perm : 682507K->442221K(1048576K)], 107.6553710 secs]"
                + " [Times: user=112.83 sys=0.28, real=107.66 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineRetainBeginningConcurrentModeFailure() {
        String priorLogLine = "";
        String logLine = " (concurrent mode failure): 5355855K->991044K(7331840K), 58.3748587 secs]639860.666: "
                + "[Class Histogram";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineRetainMiddleSerialConcurrentMixed() {
        String priorLogLine = "";
        String logLine = ": 917504K->917504K(917504K), 5.5887120 secs]877375.047: [CMS877378.691: "
                + "[CMS-concurrent-mark: 5.714/11.380 secs] [Times: user=14.72 sys=4.81, real=11.38 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineRetainBeginningParNewNoSpaceAfterGc() {
        String priorLogLine = "";
        String logLine = "12.891: [GC12.891: [ParNew";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
    }

    public void testLogLineParNewPrefixed() {
        String priorLogLine = "";
        String logLine = "831626.089: [ParNew831628.158: [ParNew833918.729: [GC (Allocation Failure) 833918.729: "
                + "[ParNew: 595103K->12118K(619008K), 0.0559019 secs] 1247015K->664144K(4157952K), 0.0561698 secs] "
                + "[Times: user=0.09 sys=0.00, real=0.06 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, null, context);
        Assert.assertEquals("Log line not parsed correctly.",
                "833918.729: [GC (Allocation Failure) 833918.729: [ParNew: 595103K->12118K(619008K), 0.0559019 secs] "
                        + "1247015K->664144K(4157952K), 0.0561698 secs] [Times: user=0.09 sys=0.00, real=0.06 secs]",
                event.getLogEntry());
    }

    public void testLogLineBeginningSerialConcurrentWithJvmtiEnvForceGarbageCollectionTrigger() {
        String priorLogLine = "";
        String logLine = "262372.344: [Full GC (JvmtiEnv ForceGarbageCollection) 262372.344: [CMS262372.426: "
                + "[CMS-concurrent-mark: 0.082/0.083 secs] [Times: user=0.08 sys=0.00, real=0.09 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.",
                "262372.344: [Full GC (JvmtiEnv ForceGarbageCollection) 262372.344: [CMS", event.getLogEntry());
    }

    public void testLogLineBeginningSerialConcurrentWithMetadataGcThreshold() {
        String priorLogLine = "";
        String logLine = "262375.122: [Full GC (Metadata GC Threshold) 262375.122: [CMS262375.200: "
                + "[CMS-concurrent-mark: 0.082/0.082 secs] [Times: user=0.08 sys=0.00, real=0.08 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.",
                "262375.122: [Full GC (Metadata GC Threshold) 262375.122: [CMS", event.getLogEntry());
    }

    public void testLogLineBeginningSerialNoSpaceAfterTrigger() {
        String priorLogLine = "";
        String logLine = "5026.107: [Full GC (Allocation Failure)5026.108: [CMS"
                + "5027.062: [CMS-concurrent-sweep: 9.543/33.853 secs] "
                + "[Times: user=107.27 sys=5.82, real=33.85 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.", "5026.107: [Full GC (Allocation Failure)5026.108: [CMS",
                event.getLogEntry());
    }

    public void testLogLineBeginningSerialConcurrentWithGcLockerInitiatedGc() {
        String priorLogLine = "";
        String logLine = "58626.878: [Full GC (GCLocker Initiated GC)58626.878: [CMS"
                + "58630.075: [CMS-concurrent-sweep: 3.220/3.228 secs] [Times: user=3.38 sys=0.01, real=3.22 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.",
                "58626.878: [Full GC (GCLocker Initiated GC)58626.878: [CMS", event.getLogEntry());
    }

    public void testLogLineBeginningParNewWithFlsStatistics() {
        String priorLogLine = "";
        String logLine = "1.118: [GC Before GC:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.", "1.118: [GC ", event.getLogEntry());
    }

    public void testLogLineMiddleParNewWithFlsStatistics() {
        String priorLogLine = "";
        String logLine = "1.118: [ParNew: 377487K->8426K(5505024K), 0.0535260 secs] 377487K->8426K(43253760K)After GC:";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.",
                "1.118: [ParNew: 377487K->8426K(5505024K), 0.0535260 secs] 377487K->8426K(43253760K)",
                event.getLogEntry());
    }

    public void testLogLineDurationWithTimeStamp() {
        String priorLogLine = "";
        String logLine = ", 0.0536040 secs] [Times: user=0.89 sys=0.01, real=0.06 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    public void testLogLinParNewPromotionFailedTruncatedWithCmsConcurrentMark() {
        String priorLogLine = "";
        String logLine = "36455.096: [GC 36455.096: [ParNew (promotion failed): 153344K->153344K(153344K), "
                + "0.6818450 secs]36455.778: [CMS36459.090: [CMS-concurrent-mark: 3.439/4.155 secs] "
                + "[Times: user=8.27 sys=0.17, real=4.16 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.",
                "36455.096: [GC 36455.096: [ParNew (promotion failed): 153344K->153344K(153344K), 0.6818450 secs]"
                        + "36455.778: [CMS",
                event.getLogEntry());
    }

    public void testLogLinParNewPromotionFailedTruncatedWithCmsConcurrentPreclean() {
        String priorLogLine = "";
        String logLine = "65778.258: [GC65778.258: [ParNew (promotion failed): 8300210K->8088352K(8388608K), "
                + "1.4967400 secs]65779.755: [CMS65781.579: [CMS-concurrent-preclean: 2.150/47.638 secs] "
                + "[Times: user=81.22 sys=2.02, real=47.63 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.",
                "65778.258: [GC65778.258: [ParNew (promotion failed): 8300210K->8088352K(8388608K), "
                        + "1.4967400 secs]65779.755: [CMS",
                event.getLogEntry());
    }

    public void testLogLineBeginningParNewWithNoParNewWithCmsConcurrentPreclean() {
        String priorLogLine = "";
        String logLine = "3576157.596: [GC 3576157.596: [CMS-concurrent-abortable-preclean: 0.997/1.723 secs] "
                + "[Times: user=3.20 sys=0.03, real=1.73 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.", "3576157.596: [GC ", event.getLogEntry());
    }

    public void testLogLinParNewPromotionFailedWithCmsAbortPrecleanDueToTime() {
        String priorLogLine = "";
        String logLine = "73241.738: [GC (Allocation Failure)73241.738: [ParNew (promotion failed): "
                + "8205461K->8187503K(8388608K), 2.1449990 secs]73243.883: [CMS CMS: abort preclean due to time "
                + "3244.984: [CMS-concurrent-abortable-preclean: 3.335/9.080 secs] "
                + "[Times: user=43.26 sys=1.66, real=9.08 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(null, logLine, nextLogLine, entangledLogLines, context);
        Assert.assertEquals("Log line not parsed correctly.",
                "73241.738: [GC (Allocation Failure)73241.738: [ParNew (promotion failed): "
                        + "8205461K->8187503K(8388608K), 2.1449990 secs]73243.883: [CMS",
                event.getLogEntry());
    }

    public void testLogLineEndParNew() {
        String priorLogLine = "";
        String logLine = "3576157.596: [ParNew: 147599K->17024K(153344K), 0.0795160 secs] "
                + "2371401K->2244459K(6274432K), 0.0810030 secs] [Times: user=0.44 sys=0.00, real=0.08 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    public void testLogLineBeginningParNewDatestamp() {
        String priorLogLine = "";
        String logLine = "2016-09-07T16:59:44.005-0400: 26536.942: [GC"
                + "2016-09-07T16:59:44.005-0400: 26536.943: [ParNew";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }

    public void testLogLineCmsSerialOldCombinedConcurrentDatestamp() {
        String priorLogLine = "";
        String logLine = "2016-10-10T19:17:37.771-0700: 2030.108: [GC (Allocation Failure) "
                + "2016-10-10T19:17:37.771-0700: 2030.108: [ParNew2016-10-10T19:17:37.773-0700: "
                + "2030.110: [CMS-concurrent-abortable-preclean: 0.050/0.150 secs] "
                + "[Times: user=0.11 sys=0.03, real=0.15 secs]";
        String nextLogLine = "";
        Set<String> context = new HashSet<String>();
        Assert.assertTrue("Log line not recognized as " + PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine, priorLogLine, nextLogLine));
        List<String> entangledLogLines = new ArrayList<String>();
        CmsPreprocessAction event = new CmsPreprocessAction(priorLogLine, logLine, nextLogLine, entangledLogLines,
                context);
        Assert.assertEquals("Log line not parsed correctly.",
                "2016-10-10T19:17:37.771-0700: 2030.108: [GC (Allocation Failure) "
                        + "2016-10-10T19:17:37.771-0700: 2030.108: [ParNew",
                event.getLogEntry());
    }

    /**
     * Test preprocessing <code>PrintHeapAtGcEvent</code> with underlying <code>CmsSerialOldEvent</code>.
     */
    public void testSplitPrintHeapAtGcCmsSerialOldLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset6.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_HEAP_AT_GC));
    }

    /**
     * Test with underlying <code>CmsSerialOld</code> triggered by concurrent mode failure.
     */
    public void testSplitPrintHeapAtGcCmsSerialOldConcurrentModeFailureLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset8.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_HEAP_AT_GC));
    }

    /**
     * Test <code>CmsPreprocessAction</code>: split <code>CmsSerialOldEvent</code> and <code>CmsConcurrentEvent</code>.
     */
    public void testSplitCmsConcurrentModeFailureEventMarkLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset10.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(JdkUtil.TriggerType.CMS_CONCURRENT_MODE_FAILURE.toString() + " trigger not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
    }

    /**
     * Test <code>CmsPreprocessAction</code>: split <code>CmsSerialOldEvent</code> and <code>CmsConcurrentEvent</code>.
     */
    public void testSplitCmsConcurrentModeFailureEventAbortablePrecleanLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset11.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(JdkUtil.TriggerType.CMS_CONCURRENT_MODE_FAILURE.toString() + " trigger not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
    }

    /**
     * Test preprocessing <code>CmsSerialOldConcurrentModeFailureEvent</code> split over 3 lines.
     */
    public void testSplit3LinesCmsConcurrentModeFailureEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset14.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
    }

    /**
     * Test preprocessing a split <code>ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent</code> with
     * -XX:+PrintTenuringDistribution logging between the initial and final lines.
     */
    public void testSplitMixedTenuringParNewPromotionFailedEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset18.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
    }

    /**
     * Test preprocessing <code>PrintHeapAtGcEvent</code> with underlying <code>ParNewConcurrentModeFailureEvent</code>.
     */
    public void testSplitPrintHeapAtGcParNewPromotionFailedCmsConcurrentModeFailureEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset21.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_HEAP_AT_GC));
    }

    public void testParNewPromotionFailedTruncatedEventLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset23.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.CMS_CONCURRENT));
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(JdkUtil.LogEventType.PAR_NEW));
        Assert.assertTrue(Analysis.ERROR_CMS_PROMOTION_FAILED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_CMS_PROMOTION_FAILED));
    }

    /**
     * Test PAR_NEW mixed with CMS_CONCURRENT over 2 lines.
     * 
     */
    public void testParNewMixedCmsConcurrent() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset58.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(LogEventType.PAR_NEW.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        Assert.assertTrue(LogEventType.CMS_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
    }

    /**
     * Test CMS_SERIAL_OLD with concurrent mode failure trigger mixed with CMS_CONCURRENT over 2 lines.
     * 
     */
    public void testCmsSerialConcurrentModeFailureMixedCmsConcurrent() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset61.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertTrue(LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(LogEventType.CMS_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test split <code>ParNewEvent</code> with a trigger and -XX:+PrintTenuringDistribution logging between the initial
     * and final lines.
     */
    public void testSplitMixedTenuringParNewPromotionEventWithTriggerLogging() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset67.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertTrue("Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
    }

    /**
     * Test CMS_SERIAL_OLD with concurrent mode failure trigger mixed with CMS_CONCURRENT over 2 lines on JDK8.
     * 
     */
    public void testCmsSerialConcurrentModeFailureMixedCmsConcurrentJdk8() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset69.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue(LogEventType.CMS_SERIAL_OLD.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(LogEventType.CMS_CONCURRENT.toString() + " collector not identified.",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.WARN_CMS_INCREMENTAL_MODE + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_CMS_INCREMENTAL_MODE));
        Assert.assertTrue(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test CMS_SERIAL_OLD with concurrent mode interrupted trigger mixed with CMS_CONCURRENT over 2 lines.
     * 
     */
    public void testCmsSerialOldConcurrentModeInterruptedMixedCmsConcurrent() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset71.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue(JdkUtil.TriggerType.CMS_CONCURRENT_MODE_INTERRUPTED.toString() + " trigger not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
    }

    /**
     * Test preprocessing CMS_SERIAL_OLD triggered by <code>PrintClassHistogramEvent</code> across many lines.
     * 
     */
    public void testCmsSerialOldPrintClassHistogramTriggerAcross5Lines() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset81.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
    }

    /**
     * Test preprocessing PAR_NEW mixed with <code>PrintHeapAtGcEvent</code>.
     * 
     */
    public void testParNewPrintHeapAtGc() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset83.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        Assert.assertTrue(Analysis.WARN_PRINT_HEAP_AT_GC + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.WARN_PRINT_HEAP_AT_GC));
    }

    /**
     * Test preprocessing PAR_NEW with extraneous prefix.
     * 
     */
    public void testParNewPrefixed() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset89.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
    }

    /**
     * Test preprocessing CMS_SERIAL_OLD with JvmtiEnv ForceGarbageCollection and concurrent mode interrupted.
     * 
     */
    public void testCmsSerialOldTriggerJvmtiEnvForceGarbageCollectionWithConcurrentModeInterrupted() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset90.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED));
    }

    /**
     * Test preprocessing CMS_SERIAL_OLD with Metadata GC Threshold and concurrent mode interrupted.
     * 
     */
    public void testCmsSerialOldTriggerMetadataGcThresholdWithConcurrentModeInterrupted() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset91.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 2, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_SERIAL_OLD.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_SERIAL_OLD));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
        Assert.assertTrue(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE));
    }

    /**
     * Test preprocessing PAR_NEW with FLS_STATISTICS.
     * 
     */
    public void testParNewWithFlsStatistics() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset94.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 1, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        Assert.assertTrue(Analysis.INFO_PRINT_FLS_STATISTICS + " analysis not identified.",
                jvmRun.getAnalysisKeys().contains(Analysis.INFO_PRINT_FLS_STATISTICS));
    }

    public void testBeginningParNewWithNoParNewWithCmsConcurrentPreclean() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset105.txt");
        GcManager jvmManager = new GcManager();
        File preprocessedFile = jvmManager.preprocess(testFile, null);
        jvmManager.store(preprocessedFile, false);
        JvmRun jvmRun = jvmManager.getJvmRun(new Jvm(null, null), Constants.DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD);
        Assert.assertEquals("Event type count not correct.", 3, jvmRun.getEventTypes().size());
        Assert.assertFalse(LogEventType.UNKNOWN.toString() + " collector identified.",
                jvmRun.getEventTypes().contains(LogEventType.UNKNOWN));
        Assert.assertTrue("Log line not recognized as " + LogEventType.PAR_NEW.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.PAR_NEW));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING));
        Assert.assertTrue("Log line not recognized as " + LogEventType.CMS_CONCURRENT.toString() + ".",
                jvmRun.getEventTypes().contains(LogEventType.CMS_CONCURRENT));
    }
}
