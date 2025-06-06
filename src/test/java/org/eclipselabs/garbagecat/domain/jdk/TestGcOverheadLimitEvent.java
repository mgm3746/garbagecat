/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestGcOverheadLimitEvent {

    @Test
    void testLineIsExceeding() {
        String logLine = "GC time is exceeding GCTimeLimit of 98%";
        assertTrue(GcOverheadLimitEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.GC_OVERHEAD_LIMIT.toString() + ".");
    }

    @Test
    void testLineWouldExceed() {
        String logLine = "GC time would exceed GCTimeLimit of 98%";
        assertTrue(GcOverheadLimitEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.GC_OVERHEAD_LIMIT.toString() + ".");
    }
}
