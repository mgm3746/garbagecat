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

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestGcLockerEvent extends TestCase {

    public void testLine() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_LOCKER.toString() + ".",
                GcLockerEvent.match(logLine));
    }

    public void testParseLogLine() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        LogEvent event = JdkUtil.parseLogLine(logLine);
        Assert.assertTrue(JdkUtil.LogEventType.GC_LOCKER.toString() + " event not identified.",
                event instanceof GcLockerEvent);
    }

    public void testIdentifyEventType() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        Assert.assertTrue(JdkUtil.LogEventType.GC_LOCKER.toString() + " event not identified.",
                JdkUtil.identifyEventType(logLine) == JdkUtil.LogEventType.GC_LOCKER);
    }

    public void testNotBlocking() {
        String logLine = "GC locker: Trying a full collection because scavenge failed";
        Assert.assertFalse(JdkUtil.LogEventType.GC_LOCKER.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }
}
