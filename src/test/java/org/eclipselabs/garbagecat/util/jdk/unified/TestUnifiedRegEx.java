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
package org.eclipselabs.garbagecat.util.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedRegEx {

    @Test
    void testDecoratorGcAge() {
        String decorator = "[2022-08-03T06:58:41.313+0000][gc,age      ] GC(0)";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorGcCds() {
        String decorator = "[0.004s][info][gc,cds       ]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorGcRef() {
        String decorator = "[0.212s][info][gc,ref      ]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorGcStringdedup() {
        String decorator = "[2021-10-13T13:31:38.618+0400][info][gc,stringdedup]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorNoInfo() {
        String decorator = "[2022-08-03T06:58:38.019+0000][safepoint   ]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorSafepoint() {
        String decorator = "[0.031s][info][safepoint    ]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorSize() {
        assertEquals(25, UnifiedRegEx.DECORATOR_SIZE, "Decorator size not correct.");
    }

    @Test
    void testDecoratorTimePidTags() {
        String decorator = "[2022-08-03T06:58:37.646+0000][1863][gc,init]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorTimeUptimeLevelInforTagsSafepoint() {
        String decorator = "[2021-09-14T11:40:53.379-0500][144.035s][info][safepoint     ]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorTimeUptimemillis() {
        String decorator = "[2020-02-14T15:21:55.207-0500][25016ms]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorUptimeMillis() {
        String decorator = "[25016ms]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorUptimeMillis15Digits() {
        String decorator = "[115443156312345ms]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testDecoratorWithSpaces() {
        String decorator = "[932126.909s][info   ][safepoint     ]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testGcEventId() {
        String id = "GC(1326)";
        assertTrue(id.matches(UnifiedRegEx.GC_EVENT_NUMBER), "'" + id + "' not recognized as a valid GC event id.");
    }

    @Test
    void testGcEventId7Digits() {
        String id = "GC(1234567)";
        assertTrue(id.matches(UnifiedRegEx.GC_EVENT_NUMBER), "'" + id + "' not recognized as a valid GC event id.");
    }

    @Test
    void testHostname() {
        String hostname = "[localhost.localdomain]";
        assertTrue(hostname.matches(UnifiedRegEx.HOSTNAME), "Hostname " + hostname + " not recognized.");
    }

    @Test
    void testHostnameWithNumbers() {
        String hostname = "[myhost123]";
        assertTrue(hostname.matches(UnifiedRegEx.HOSTNAME), "Hostname " + hostname + " not recognized.");
    }

    @Test
    void testLevelInfo() {
        String id = "[info]";
        assertTrue(id.matches(UnifiedRegEx.LEVEL), "'" + id + "' not recognized as a valid tag.");
    }

    @Test
    void testLevelInfoSpaces() {
        String id = "[info   ]";
        assertTrue(id.matches(UnifiedRegEx.LEVEL), "'" + id + "' not recognized as a valid tag.");
    }

    @Test
    void testReleaseStringJdk11() {
        String release = "11.0.9+11-LTS";
        assertTrue(release.matches(UnifiedRegEx.RELEASE_STRING), "Release string " + release + " not recognized.");
    }

    @Test
    void testReleaseStringJdk12() {
        String release = "12.0.1+12";
        assertTrue(release.matches(UnifiedRegEx.RELEASE_STRING), "Release string " + release + " not recognized.");
    }

    @Test
    void testReleaseStringJdk17() {
        String release = "17.0.1+12-LTS";
        assertTrue(release.matches(UnifiedRegEx.RELEASE_STRING), "Release string " + release + " not recognized.");
    }

    @Test
    void testTagsGc() {
        String id = "[gc          ]";
        assertTrue(id.matches(UnifiedRegEx.TAGS), "'" + id + "' not recognized as a valid tag.");
    }

    @Test
    void testTagsGcHeapExit() {
        String id = "[gc,heap,exit]";
        assertTrue(id.matches(UnifiedRegEx.TAGS), "'" + id + "' not recognized as a valid tag.");
    }

    @Test
    void testTagsGcMetaspace() {
        String id = "[gc,metaspace]";
        assertTrue(id.matches(UnifiedRegEx.TAGS), "'" + id + "' not recognized as a valid tag.");
    }

    @Test
    void testTagsGcStats() {
        String id = "[gc,stats      ]";
        assertTrue(id.matches(UnifiedRegEx.TAGS), "'" + id + "' not recognized as a valid tag.");
    }

    @Test
    void testTimestampTime() {
        String decorator = "[2020-02-14T15:21:55.207-0500]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testTimestampTimeUptime() {
        String decorator = "[2020-02-14T15:21:55.207-0500][25.016s]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }

    @Test
    void testTimestampUnifiedLogging() {
        String timestamp = "0.002s";
        assertTrue(timestamp.matches(UnifiedRegEx.UPTIME), "'" + timestamp + "' is a valid timestamp.");
    }

    @Test
    void testTimestampUptime() {
        String decorator = "[25.016s]";
        assertTrue(decorator.matches(UnifiedRegEx.DECORATOR), "Decorator " + decorator + " not recognized.");
    }
}
