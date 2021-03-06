/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestGcOverheadLimitEvent {

    @Test
    public void testLineWouldExceed() {
        String logLine = "GC time would exceed GCTimeLimit of 98%";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString() + ".",
                GcOverheadLimitEvent.match(logLine));
    }

    @Test
    public void testLineIsExceeding() {
        String logLine = "GC time is exceeding GCTimeLimit of 98%";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString() + ".",
                GcOverheadLimitEvent.match(logLine));
    }

    @Test
    public void testNotBlocking() {
        String logLine = "GC time would exceed GCTimeLimit of 98%";
        assertFalse(JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        String logLine = "GC time would exceed GCTimeLimit of 98%";
        assertFalse(JdkUtil.LogEventType.GC_OVERHEAD_LIMIT.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }
}
