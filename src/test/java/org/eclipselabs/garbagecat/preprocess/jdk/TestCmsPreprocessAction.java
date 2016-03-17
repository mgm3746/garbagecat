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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

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
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
    
    public void testLogLineParNewMixedConcurrentWithWhitespaceEnd() {
        String logLine = "46674.719: [GC (Allocation Failure)46674.719: [ParNew46674.749: "
                + "[CMS-concurrent-abortable-preclean: 1.427/2.228 secs] [Times: user=1.56 sys=0.01, real=2.23 secs]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
    
    public void testLogLineEnd() {
        String logLine = ": 153599K->17023K(153600K), 0.0383370 secs] 229326K->114168K(494976K), 0.0384820 secs] "
                + "[Times: user=0.15 sys=0.01, real=0.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
    
    public void testLogLineEndWithWhitespaceEnd() {
        String logLine = ": 153599K->17023K(153600K), 0.0383370 secs] 229326K->114168K(494976K), 0.0384820 secs] "
                + "[Times: user=0.15 sys=0.01, real=0.04 secs]    ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
    
    public void testLogLineParNewNoTriggerMixedConcurrent() {
        String logLine = "10.963: [GC10.963: [ParNew10.977: [CMS-concurrent-abortable-preclean: 0.088/0.197 secs] "
                + "[Times: user=0.33 sys=0.05, real=0.20 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
    
    public void testLogLineParNewTriggerMixedConcurrentJdk8() {
        String logLine = "45.574: [GC (Allocation Failure) 45.574: [ParNew45.670: [CMS-concurrent-abortable-preclean: "
                + "3.276/4.979 secs] [Times: user=7.75 sys=0.28, real=4.98 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
    
    public void testLogLineCmsSerialOldMixedConcurrent() {
        String logLine = "44.684: [Full GC44.684: [CMS44.877: [CMS-concurrent-mark: 1.508/2.428 secs] "
                + "[Times: user=3.44 sys=0.49, real=2.42 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
    
    public void testLogLineEndWithPerm() {
        String logLine = " (concurrent mode failure): 1218548K->413373K(1465840K), 1.3656970 secs] "
                + "1229657K->413373K(1581168K), [CMS Perm : 83805K->80520K(83968K)], 1.3659420 secs] "
                + "[Times: user=1.33 sys=0.01, real=1.37 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
    
    public void testLogLineParNewNoTriggerMixedConcurrentWithCommas() {
        String logLine = "32552,602: [GC32552,602: [ParNew32552,610: [CMS-concurrent-abortable-preclean: 3,090/4,993 secs] "
                + "[Times: user=3,17 sys=0,02, real=5,00 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
    
    public void testLogLineEndWithCommas() {
        String logLine = ": 289024K->17642K(306688K), 0,0788160 secs] 4086255K->3814874K(12548864K), 0,0792920 secs] "
                + "[Times: user=0,28 sys=0,00, real=0,08 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.CMS.toString() + ".",
                CmsPreprocessAction.match(logLine));
    }
}
