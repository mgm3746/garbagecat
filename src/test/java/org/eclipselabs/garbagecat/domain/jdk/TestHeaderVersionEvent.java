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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestHeaderVersionEvent {

    /**
     * Test if JDK7 by inspecting version header.
     */
    @Test
    void testJdk7() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        assertTrue(HeaderVersionEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEADER_VERSION.toString() + ".");
        HeaderVersionEvent event = new HeaderVersionEvent(logLine);
        assertEquals(7, event.getJdkVersionMajor(), "JDK7 not identified");
    }

    /**
     * Test if JDK8 by inspecting version header.
     */
    @Test
    void testJdk8() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (25.73-b02) for linux-amd64 JRE (1.8.0_73-b02), "
                + "built on Jan 29 2016 17:39:45 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        assertTrue(HeaderVersionEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEADER_VERSION.toString() + ".");
        HeaderVersionEvent event = new HeaderVersionEvent(logLine);
        assertEquals(8, event.getJdkVersionMajor(), "JDK8 not identified");
    }

    /**
     * Test JDK7 update version by inspecting version header.
     */
    @Test
    void testJdkUpdate7() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        assertTrue(HeaderVersionEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEADER_VERSION.toString() + ".");
        HeaderVersionEvent event = new HeaderVersionEvent(logLine);
        assertEquals(91, event.getJdkVersionMinor(), "Update 91 not identified");
    }

    /**
     * Test JDK8 update version by inspecting version header.
     */
    @Test
    void testJdkUpdate8() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (25.73-b02) for linux-amd64 JRE (1.8.0_73-b02), "
                + "built on Jan 29 2016 17:39:45 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        assertTrue(HeaderVersionEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEADER_VERSION.toString() + ".");
        HeaderVersionEvent event = new HeaderVersionEvent(logLine);
        assertEquals(73, event.getJdkVersionMinor(), "Update 73 not identified");
    }

    @Test
    void testLine() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (24.85-b08) for linux-amd64 JRE (1.7.0_85-b34), built on "
                + "Sep 29 2015 08:44:21 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        assertTrue(HeaderVersionEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEADER_VERSION.toString() + ".");
    }

    @Test
    void testLineOpenJdk() {
        String logLine = "OpenJDK 64-Bit Server VM (24.95-b01) for linux-amd64 JRE (1.7.0_95-b00), built on "
                + "Jan 18 2016 21:57:50 by \"mockbuild\" with gcc 4.8.5 20150623 (Red Hat 4.8.5-4)";
        assertTrue(HeaderVersionEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.HEADER_VERSION.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (24.85-b08) for linux-amd64 JRE (1.7.0_85-b34), built on "
                + "Sep 29 2015 08:44:21 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.HEADER_VERSION.toString() + " incorrectly indentified as blocking.");
    }
}
