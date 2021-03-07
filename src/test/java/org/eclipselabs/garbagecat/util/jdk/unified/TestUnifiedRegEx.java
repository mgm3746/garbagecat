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
package org.eclipselabs.garbagecat.util.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedRegEx {

    @Test
    void testTimestampUnifiedLogging() {
        String timestamp = "0.002s";
        assertTrue(timestamp.matches(UnifiedRegEx.UPTIME), "'" + timestamp + "' is a valid timestamp.");
    }

    @Test
    void testDurationJdk9() {
        String duration = "2.969ms";
        assertTrue(duration.matches(UnifiedRegEx.DURATION), "'" + duration + "' is a valid duration.");
    }

    @Test
    void testDurationJdk9WithSpace() {
        String duration = "15.91 ms";
        assertTrue(duration.matches(UnifiedRegEx.DURATION), "'" + duration + "' is a valid duration.");
    }

    @Test
    void testGcEventId() {
        String id = "GC(1326)";
        assertTrue(id.matches(UnifiedRegEx.GC_EVENT_NUMBER), "'" + id + "' is a valid GC event id.");
    }

    @Test
    void testGcEventId7Digits() {
        String id = "GC(1234567)";
        assertTrue(id.matches(UnifiedRegEx.GC_EVENT_NUMBER), "'" + id + "' is a valid GC event id.");
    }

    @Test
    void testDecoratorUptime() {
        String decorator = "[25.016s]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Time decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorUptimeMillis() {
        String decorator = "[25016ms]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Time decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorUptimeMillis9Digits() {
        String decorator = "[100159717ms]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Time decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorTime() {
        String decorator = "[2020-02-14T15:21:55.207-0500]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Time decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorTimeUptime() {
        String decorator = "[2020-02-14T15:21:55.207-0500][25.016s]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Time decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorTimeUptimemillis() {
        String decorator = "[2020-02-14T15:21:55.207-0500][25016ms]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Time decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorSafepoint() {
        String decorator = "[0.031s][info][safepoint    ]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorGcCds() {
        String decorator = "[0.004s][info][gc,cds       ]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }
}
