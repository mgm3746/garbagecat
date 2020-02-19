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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedBlankLineEvent extends TestCase {

    public void testIdentityEventType() {
        String logLine = "[69.946s][info][gc,stats     ]";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_BLANK_LINE + "not identified.",
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE, JdkUtil.identifyEventType(logLine));
    }

    public void testReportable() {
        String logLine = "[69.946s][info][gc,stats     ]";
        Assert.assertFalse(
                JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_BLANK_LINE);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + " not indentified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineUnifiedFooterStats() {
        String logLine = "[69.946s][info][gc,stats     ]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }

    public void testLineUnifiedFooterHeap() {
        String logLine = "[69.946s][info][gc,heap,exit ]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }

    public void testLineTimeUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }

    public void testLineUptimeMillis() {
        String logLine = "[1357910ms]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[2019-02-05T15:10:08.998-0200][1357910ms]   ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_BLANK_LINE.toString() + ".",
                UnifiedBlankLineEvent.match(logLine));
    }
}
