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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestUnifiedShenandoahCancellingGcEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[72.659s][info][gc] Cancelling GC: Stopping VM";
        assertEquals(JdkUtil.EventType.UNIFIED_SHENANDOAH_CANCELLING_GC,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_CANCELLING_GC + "not identified.");
    }

    @Test
    void testLineJdk8() {
        String logLine = "Cancelling GC: Stopping VM";
        assertTrue(UnifiedShenandoahCancellingGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_CANCELLING_GC.toString() + ".");
    }

    @Test
    void testLineUnified() {
        String logLine = "[72.659s][info][gc] Cancelling GC: Stopping VM";
        assertTrue(UnifiedShenandoahCancellingGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_CANCELLING_GC.toString() + ".");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[72.659s][info][gc] Cancelling GC: Stopping VM";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null,
                        CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahCancellingGcEvent,
                JdkUtil.EventType.UNIFIED_SHENANDOAH_CANCELLING_GC.toString() + " not parsed.");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_SHENANDOAH_CANCELLING_GC);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_CANCELLING_GC.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testUnifiedDetailed() {
        String logLine = "[69.941s][info][gc           ] Cancelling GC: Stopping VM";
        assertTrue(UnifiedShenandoahCancellingGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_CANCELLING_GC.toString() + ".");
    }

    @Test
    void testUnifiedUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.997-0200][1357909ms] Cancelling GC: Stopping VM";
        assertTrue(UnifiedShenandoahCancellingGcEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_CANCELLING_GC.toString() + ".");
    }
}
