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
 * 
 */
public class TestUnifiedRemarkEvent extends TestCase {

    public void testLogLine() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".",
                UnifiedRemarkEvent.match(logLine));
        UnifiedRemarkEvent event = new UnifiedRemarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 7944 - 1, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 1, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms           ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.UNIFIED_REMARK.toString() + ".",
                UnifiedRemarkEvent.match(logLine));
    }

    public void testIdentity() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        Assert.assertEquals(JdkUtil.LogEventType.UNIFIED_REMARK + "not identified.",
                JdkUtil.LogEventType.UNIFIED_REMARK, JdkUtil.identifyEventType(logLine));
    }

    public void testIsBlocking() {
        String logLine = "[7.944s][info][gc] GC(6432) Pause Remark 8M->8M(10M) 1.767ms";
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }
}
