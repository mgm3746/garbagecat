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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class TestUnifiedG1ConcurrentEvent extends TestCase {

    public void testLogLineConcurrentCycle() {
        String logLine = "[14.859s][info][gc] GC(1083) Concurrent Cycle";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                UnifiedG1ConcurrentEvent.match(logLine));
        UnifiedG1ConcurrentEvent event = new UnifiedG1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 14859, event.getTimestamp());
    }

    public void testLogLineConcurrentCycleWithDuration() {
        String logLine = "[14.904s][info][gc] GC(1083) Concurrent Cycle 45.374ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                UnifiedG1ConcurrentEvent.match(logLine));
        UnifiedG1ConcurrentEvent event = new UnifiedG1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 14904 - 45, event.getTimestamp());
    }

    public void testLogLineConcurrentPauseCleanup() {
        String logLine = "[15.101s][info][gc] GC(1099) Pause Cleanup 30M->30M(44M) 0.058ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CONCURRENT.toString() + ".",
                UnifiedG1ConcurrentEvent.match(logLine));
        UnifiedG1ConcurrentEvent event = new UnifiedG1ConcurrentEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 15101 - 0, event.getTimestamp());
    }

    public void testIdentity() {
        String logLine = "[14.859s][info][gc] GC(1083) Concurrent Cycle";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_G1_CONCURRENT + "not identified.",
                JdkUtil.LogEventType.UNIFIED_G1_CONCURRENT, JdkUtil.identifyEventType(logLine));
    }

    public void testNotBlocking() {
        String logLine = "[14.859s][info][gc] GC(1083) Concurrent Cycle";
        Assert.assertFalse(
                JdkUtil.LogEventType.UNIFIED_G1_CONCURRENT.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }
}
