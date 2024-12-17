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
class TestShenandoahTriggerEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "Trigger: Learning 1 of 5. Free (45118K) is below initial threshold (45875K)";
        assertEquals(JdkUtil.EventType.SHENANDOAH_TRIGGER,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.SHENANDOAH_TRIGGER + "not identified.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "Trigger: Learning 1 of 5. Free (45118K) is below initial threshold (45875K)";
        assertTrue(JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof ShenandoahTriggerEvent,
                JdkUtil.EventType.SHENANDOAH_TRIGGER.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.EventType.SHENANDOAH_TRIGGER),
                JdkUtil.EventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testTriggerAverageGc() {
        String logLine = "Trigger: Average GC time (12.56 ms) is above the time for allocation rate (899 MB/s) to "
                + "deplete free headroom (11466K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerAverageGcRateBb() {
        String logLine = "Trigger: Average GC time (6458.98 ms) is above the time for allocation rate (89583 BB/s) "
                + "to deplete free headroom (0B)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerFreeNoDecorator() {
        String logLine = "Trigger: Free (168M) is below minimum threshold (168M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerLearning() {
        String logLine = "Trigger: Learning 1 of 5. Free (45118K) is below initial threshold (45875K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.SHENANDOAH_TRIGGER);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as unified.");
    }
}
