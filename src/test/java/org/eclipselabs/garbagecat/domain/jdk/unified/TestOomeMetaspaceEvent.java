/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestOomeMetaspaceEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms] Metaspace (data) allocation failed for size 11";
        assertEquals(JdkUtil.LogEventType.OOME_METASPACE, JdkUtil.identifyEventType(logLine),
                JdkUtil.LogEventType.OOME_METASPACE + "not identified.");
    }

    @Test
    void testLineTimeUptimeMillis() {
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms] Metaspace (data) allocation failed for size 11";
        assertTrue(OomeMetaspaceEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.OOME_METASPACE.toString() + ".");
    }

    @Test
    void testLineUptimeMillis() {
        String logLine = "[7732788ms] Metaspace (data) allocation failed for size 11";
        assertTrue(OomeMetaspaceEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.OOME_METASPACE.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms] Metaspace (data) allocation failed for size 11";
        assertTrue(JdkUtil.parseLogLine(logLine) instanceof OomeMetaspaceEvent,
                JdkUtil.LogEventType.OOME_METASPACE.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        String logLine = "[2022-02-08T07:33:14.540+0000][7732788ms] Metaspace (data) allocation failed for size 11";
        assertFalse(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)),
                JdkUtil.LogEventType.OOME_METASPACE.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.OOME_METASPACE);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.OOME_METASPACE.toString() + " not indentified as unified.");
    }
}
