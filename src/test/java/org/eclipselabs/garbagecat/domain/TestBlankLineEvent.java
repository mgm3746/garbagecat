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
package org.eclipselabs.garbagecat.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestBlankLineEvent {

    @Test
    void testParseLogLine() {
        String logLine = "";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof BlankLineEvent,
                JdkUtil.LogEventType.BLANK_LINE.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        String logLine = "";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.BLANK_LINE.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testLogLine() {
        String logLine = "";
        assertTrue(BlankLineEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.BLANK_LINE.toString() + ".");
    }

}
