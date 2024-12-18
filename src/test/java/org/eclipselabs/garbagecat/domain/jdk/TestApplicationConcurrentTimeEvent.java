/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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
class TestApplicationConcurrentTimeEvent {

    @Test
    void testLogLine() {
        String logLine = "Application time: 130.5284640 seconds";
        assertTrue(ApplicationConcurrentTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.APPLICATION_CONCURRENT_TIME.toString() + ".");
    }

    @Test
    void testLogLineDatestamp() {
        String logLine = "2016-12-21T14:28:11.159-0500: Application time: 0.0060964 seconds";
        assertTrue(ApplicationConcurrentTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.APPLICATION_CONCURRENT_TIME.toString() + ".");
    }

    @Test
    void testLogLineDatestampTimestamp() {
        String logLine = "2016-12-21T14:28:11.159-0500: 0.311: Application time: 0.0060964 seconds";
        assertTrue(ApplicationConcurrentTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.APPLICATION_CONCURRENT_TIME.toString() + ".");
    }

    @Test
    void testLogLineTimestamp() {
        String logLine = "0.311: Application time: 0.0060964 seconds";
        assertTrue(ApplicationConcurrentTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.APPLICATION_CONCURRENT_TIME.toString() + ".");
    }

    @Test
    void testLogLineWithSpacesAtEnd() {
        String logLine = "Application time: 130.5284640 seconds   ";
        assertTrue(ApplicationConcurrentTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.APPLICATION_CONCURRENT_TIME.toString() + ".");
    }

    @Test
    void testLogLineWithTimestamp() {
        String logLine = "0.193: Application time: 0.0430320 seconds";
        assertTrue(ApplicationConcurrentTimeEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.APPLICATION_CONCURRENT_TIME.toString() + ".");
    }
}
