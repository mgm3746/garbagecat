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
public class TestParNewCmsConcurrentPreprocessAction extends TestCase {

    public void testLine1() {
        String priorLogLine = "";
        String logLine = "2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: " + "0.043/0.144 secs] [Times: user=0.58 sys=0.03, real=0.14 secs]";
        String nextLogLine = ": 212981K->3156K(242304K), 0.0364435 secs] 4712182K->4502357K(4971420K), " + "0.0368807 secs] [Times: user=0.18 sys=0.02, real=0.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PAR_NEW_CMS_CONCURRENT.toString() + ".", ParNewCmsConcurrentPreprocessAction.match(logLine, priorLogLine,
                nextLogLine));
    }

    public void testLine2() {
        String priorLogLine = "2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: " + "0.043/0.144 secs] [Times: user=0.58 sys=0.03, real=0.14 secs]";
        String logLine = ": 212981K->3156K(242304K), 0.0364435 secs] 4712182K->4502357K(4971420K), " + "0.0368807 secs] [Times: user=0.18 sys=0.02, real=0.04 secs]";
        String nextLogLine = "";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PAR_NEW_CMS_CONCURRENT.toString() + ".", ParNewCmsConcurrentPreprocessAction.match(logLine, priorLogLine,
                nextLogLine));
    }

    public void testLine1Sweep() {
        String priorLogLine = "";
        String logLine = "1821.661: [GC 1821.661: [ParNew1821.661: [CMS-concurrent-sweep: " + "42.841/48.076 secs] [Times: user=19.45 sys=0.45, real=48.06 secs]";
        String nextLogLine = ": 36500K->3770K(38336K), 0.1767060 secs] 408349K->375618K(2092928K), " + "0.1769190 secs] [Times: user=0.05 sys=0.00, real=0.18 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.PAR_NEW_CMS_CONCURRENT.toString() + ".", ParNewCmsConcurrentPreprocessAction.match(logLine, priorLogLine,
                nextLogLine));
    }
}
