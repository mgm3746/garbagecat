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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Calendar;
import java.util.Date;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.github.joa.domain.Arch;
import org.github.joa.domain.BuiltBy;
import org.github.joa.domain.Os;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestHeaderVmInfoEvent {

    @Test
    void testIdentity() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (24.85-b08) for linux-amd64 JRE (1.7.0_85-b34), built on "
                + "Sep 29 2015 08:44:21 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        assertTrue(HeaderVmInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEADER_VM_INFO.toString() + ".");
    }

    /**
     * Test if JDK7 by inspecting version header.
     */
    @Test
    void testJdk7() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        assertTrue(HeaderVmInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEADER_VM_INFO.toString() + ".");
        HeaderVmInfoEvent event = new HeaderVmInfoEvent(logLine);
        assertEquals(Arch.X86_64, event.getArch(), "Arch not identified");
        assertEquals(BuiltBy.JAVA_RE, event.getBuiltBy(), "Builder not identified");
        assertEquals("1.7.0_91-b15", event.getJdkReleaseString(), "JDK release string not identified");
        assertEquals(7, event.getJdkVersionMajor(), "JDK version major not identified");
        assertEquals(91, event.getJdkVersionMinor(), "JDK version minor not identified");
        assertEquals(Os.WINDOWS, event.getOs(), "OS not identified");
        Date buildDate = event.getBuildDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(buildDate);
        // Java Calendar month is 0 based
        assertEquals(9, calendar.get(Calendar.MONTH), "Start month not parsed correctly.");
        assertEquals(2, calendar.get(Calendar.DAY_OF_MONTH), "Start day not parsed correctly.");
        assertEquals(2015, calendar.get(Calendar.YEAR), "Start year not parsed correctly.");
        assertEquals(3, calendar.get(Calendar.HOUR_OF_DAY), "Start hour not parsed correctly.");
        assertEquals(26, calendar.get(Calendar.MINUTE), "Start minute not parsed correctly.");
        assertEquals(24, calendar.get(Calendar.SECOND), "Start second not parsed correctly.");
    }

    /**
     * Test if JDK8 by inspecting version header.
     */
    @Test
    void testJdk8() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (25.73-b02) for linux-amd64 JRE (1.8.0_73-b02), "
                + "built on Jan 29 2016 17:39:45 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        assertTrue(HeaderVmInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEADER_VM_INFO.toString() + ".");
        HeaderVmInfoEvent event = new HeaderVmInfoEvent(logLine);
        assertEquals(Arch.X86_64, event.getArch(), "Arch not identified");
        assertEquals(BuiltBy.JAVA_RE, event.getBuiltBy(), "Builder not identified");
        assertEquals("1.8.0_73-b02", event.getJdkReleaseString(), "JDK release string not identified");
        assertEquals(8, event.getJdkVersionMajor(), "JDK version major not identified");
        assertEquals(73, event.getJdkVersionMinor(), "JDK version minor not identified");
        assertEquals(Os.LINUX, event.getOs(), "OS not identified");
        Date buildDate = event.getBuildDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(buildDate);
        // Java Calendar month is 0 based
        assertEquals(0, calendar.get(Calendar.MONTH), "Start month not parsed correctly.");
        assertEquals(29, calendar.get(Calendar.DAY_OF_MONTH), "Start day not parsed correctly.");
        assertEquals(2016, calendar.get(Calendar.YEAR), "Start year not parsed correctly.");
        assertEquals(17, calendar.get(Calendar.HOUR_OF_DAY), "Start hour not parsed correctly.");
        assertEquals(39, calendar.get(Calendar.MINUTE), "Start minute not parsed correctly.");
        assertEquals(45, calendar.get(Calendar.SECOND), "Start second not parsed correctly.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (24.85-b08) for linux-amd64 JRE (1.7.0_85-b34), built on "
                + "Sep 29 2015 08:44:21 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.EventType.HEADER_VM_INFO.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testOpenJdk() {
        String logLine = "OpenJDK 64-Bit Server VM (24.95-b01) for linux-amd64 JRE (1.7.0_95-b00), built on "
                + "Jan 18 2016 21:57:50 by \"mockbuild\" with gcc 4.8.5 20150623 (Red Hat 4.8.5-4)";
        assertTrue(HeaderVmInfoEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.HEADER_VM_INFO.toString() + ".");
        HeaderVmInfoEvent event = new HeaderVmInfoEvent(logLine);
        assertEquals(Arch.X86_64, event.getArch(), "Arch not identified");
        assertEquals(BuiltBy.MOCKBUILD, event.getBuiltBy(), "Builder not identified");
        assertEquals("1.7.0_95-b00", event.getJdkReleaseString(), "JDK release string not identified");
        assertEquals(7, event.getJdkVersionMajor(), "JDK version major not identified");
        assertEquals(95, event.getJdkVersionMinor(), "JDK version minor not identified");
        assertEquals(Os.LINUX, event.getOs(), "OS not identified");
        Date buildDate = event.getBuildDate();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(buildDate);
        // Java Calendar month is 0 based
        assertEquals(0, calendar.get(Calendar.MONTH), "Start month not parsed correctly.");
        assertEquals(18, calendar.get(Calendar.DAY_OF_MONTH), "Start day not parsed correctly.");
        assertEquals(2016, calendar.get(Calendar.YEAR), "Start year not parsed correctly.");
        assertEquals(21, calendar.get(Calendar.HOUR_OF_DAY), "Start hour not parsed correctly.");
        assertEquals(57, calendar.get(Calendar.MINUTE), "Start minute not parsed correctly.");
        assertEquals(50, calendar.get(Calendar.SECOND), "Start second not parsed correctly.");
    }
}
