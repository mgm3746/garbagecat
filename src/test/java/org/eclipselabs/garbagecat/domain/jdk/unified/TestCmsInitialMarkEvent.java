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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestCmsInitialMarkEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".",
                UnifiedCmsInitialMarkEvent.match(logLine));
        UnifiedCmsInitialMarkEvent event = new UnifiedCmsInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 178 - 0, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 157, event.getDuration());
    }

    public void testIdentityEventType() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK + "not identified.",
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof UnifiedCmsInitialMarkEvent);
    }

    public void testBlocking() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CMS_INITIAL_MARK);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[0.178s][info][gc] GC(5) Pause Initial Mark 1M->1M(2M) 0.157ms     ";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + ".",
                UnifiedCmsInitialMarkEvent.match(logLine));
    }
}
