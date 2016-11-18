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

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestTenuringDistributionEvent extends TestCase {

    public void testIdentifyEventType() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        Assert.assertTrue(JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " not indentified.",
                JdkUtil.identifyEventType(logLine).equals(LogEventType.TENURING_DISTRIBUTION));
    }

    public void testParseLogLine() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        Assert.assertTrue(JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " not indentified.",
                JdkUtil.parseLogLine(logLine) instanceof TenuringDistributionEvent);
    }

    public void testNotBlocking() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        Assert.assertFalse(
                JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        Assert.assertTrue(
                JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " incorrectly indentified as not reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testDesiredSurvivorSizeLine() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + ".",
                TenuringDistributionEvent.match(logLine));
    }

    public void testAgeLine() {
        String logLine = "- age 1: 3177664 bytes, 3177664 total";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + ".",
                TenuringDistributionEvent.match(logLine));
    }

}
