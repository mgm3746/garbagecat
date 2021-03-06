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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestBlankLineEvent {

    @Test
    public void testParseLogLine() {
        String logLine = "";
        assertTrue(JdkUtil.LogEventType.BLANK_LINE.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof BlankLineEvent);
    }

    @Test
    public void testReportable() {
        String logLine = "";
        assertFalse(JdkUtil.LogEventType.BLANK_LINE.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLine() {
        String logLine = "";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.BLANK_LINE.toString() + ".",
                BlankLineEvent.match(logLine));
    }
}
