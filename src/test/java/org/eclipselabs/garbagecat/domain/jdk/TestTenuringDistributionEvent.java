/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestTenuringDistributionEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertFalse(
                JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertTrue(
                JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " incorrectly indentified as not reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testIdentifyEventType() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertTrue(JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " not indentified.",
                JdkUtil.identifyEventType(logLine).equals(LogEventType.TENURING_DISTRIBUTION));
    }

    @Test
    public void testParseLogLine() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertTrue(JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + " not indentified.",
                JdkUtil.parseLogLine(logLine) instanceof TenuringDistributionEvent);
    }

    @Test
    public void testDesiredSurvivorSizeLine() {
        String logLine = "Desired survivor size 2228224 bytes, new threshold 1 (max 15)";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + ".",
                TenuringDistributionEvent.match(logLine));
    }

    @Test
    public void testAgeLine() {
        String logLine = "- age 1: 3177664 bytes, 3177664 total";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TENURING_DISTRIBUTION.toString() + ".",
                TenuringDistributionEvent.match(logLine));
    }

}
