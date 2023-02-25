/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestVmWarningEvent {

    @Test
    void testFailedToReserveSharedMemoryErrNo12() {
        String logLine = "OpenJDK 64-Bit Server VM warning: Failed to reserve shared memory. (error = 12)";
        VmWarningEvent event = new VmWarningEvent(logLine);
        assertEquals("12", event.getErrNo(), "errno not correct.");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "OpenJDK 64-Bit Server VM warning: Failed to reserve shared memory. (error = 12)";
        assertEquals(JdkUtil.LogEventType.VM_WARNING, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.VM_WARNING + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "OpenJDK 64-Bit Server VM warning: Failed to reserve shared memory. (error = 12)";
        assertTrue(VmWarningEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.VM_WARNING.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "OpenJDK 64-Bit Server VM warning: Failed to reserve shared memory. (error = 12)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.VM_WARNING.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "OpenJDK 64-Bit Server VM warning: Failed to reserve shared memory. (error = 12)";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof VmWarningEvent,
                JdkUtil.LogEventType.VM_WARNING.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.VM_WARNING),
                JdkUtil.LogEventType.VM_WARNING.toString() + " indentified as reportable.");
    }
}
