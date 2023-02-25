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
class TestShenandoahMetaspaceEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.196s][info][gc,metaspace] Metaspace: 3118K(3328K)->3130K(3328K) NonClass: "
                + "2860K(2944K)->2872K(2944K) Class: 258K(384K)->258K(384K)";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_METASPACE, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.SHENANDOAH_METASPACE + "not identified.");
    }

    @Test
    void testLogLine() {
        String logLine = "[0.196s][info][gc,metaspace] Metaspace: 3118K(3328K)->3130K(3328K) NonClass: "
                + "2860K(2944K)->2872K(2944K) Class: 258K(384K)->258K(384K)";
        assertTrue(ShenandoahMetaspaceEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_METASPACE.toString() + ".");
    }

    @Test
    void testNoLogLevelNoTags() {
        String logLine = "[2022-08-09T17:56:59.141-0400] Metaspace: 3448K(3648K)->3465K(3648K) NonClass: "
                + "3163K(3264K)->3179K(3264K) Class: 285K(384K)->285K(384K)";
        assertTrue(ShenandoahMetaspaceEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_METASPACE.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.196s][info][gc,metaspace] Metaspace: 3118K(3328K)->3130K(3328K) NonClass: "
                + "2860K(2944K)->2872K(2944K) Class: 258K(384K)->258K(384K)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.SHENANDOAH_METASPACE.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testNotConfusedWithPreparsingLine() {
        String logLine = "[0.068s][info][gc,metaspace] GC(3) Metaspace: 1071K(1280K)->1071K(1280K) NonClass: "
                + "981K(1088K)->981K(1088K) Class: 89K(192K)->89K(192K)";
        assertFalse(ShenandoahMetaspaceEvent.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.LogEventType.SHENANDOAH_METASPACE.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.196s][info][gc,metaspace] Metaspace: 3118K(3328K)->3130K(3328K) NonClass: "
                + "2860K(2944K)->2872K(2944K) Class: 258K(384K)->258K(384K)";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ShenandoahMetaspaceEvent,
                JdkUtil.LogEventType.SHENANDOAH_METASPACE.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_METASPACE),
                JdkUtil.LogEventType.SHENANDOAH_METASPACE.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_METASPACE);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_METASPACE.toString() + " incorrectly indentified as unified.");
    }
}
