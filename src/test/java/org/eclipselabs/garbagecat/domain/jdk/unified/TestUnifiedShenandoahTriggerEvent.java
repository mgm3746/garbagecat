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
class TestUnifiedShenandoahTriggerEvent {

    @Test
    void testFreeTimeSinceLastGcUptimeMillis() {
        String logLine = "[2019-02-05T15:10:00.671-0200][1349583ms] Trigger: Time since last GC (300004 ms) is larger "
                + "than guaranteed interval (300000 ms)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testIdentityEventType() {
        String logLine = "[3068ms] Trigger: Learning 1 of 5. Free (912M) is below initial threshold (912M)";
        assertEquals(JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER,
                JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER + "not identified.");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[3068ms] Trigger: Learning 1 of 5. Free (912M) is below initial threshold (912M)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null, CollectorFamily.UNKNOWN)),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[3068ms] Trigger: Learning 1 of 5. Free (912M) is below initial threshold (912M)";
        assertTrue(
                JdkUtil.parseLogLine(logLine, null, CollectorFamily.UNKNOWN) instanceof UnifiedShenandoahTriggerEvent,
                JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testTrigger() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] Trigger: Learning 1 of 5. Free (912M) is below "
                + "initial threshold (912M)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerAverageGc() {
        String logLine = "[41.917s][info][gc           ] Trigger: Average GC time (26.32 ms) is above the time for "
                + "allocation rate (324.68 MB/s) to deplete free headroom (8M)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerAverageGcAllocationRateKb6Digits() {
        String logLine = "[63.328s][info][gc           ] Trigger: Average GC time (77.12 ms) is above the time for "
                + "allocation rate (101894 KB/s) to deplete free headroom (6846K)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerAverageGcAllocationRateWholeNumber() {
        String logLine = "[1.757s][info][gc           ] Trigger: Average GC time (9.74 ms) is above the time for "
                + "allocation rate (1244 MB/s) to deplete free headroom (11236K)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerAverageGcTimeAverageAllocation() {
        String logLine = "[10.508s][info][gc          ] Trigger: Average GC time (16.09 ms) is above the time for "
                + "average allocation rate (409 MB/s) to deplete free headroom (5742K) (margin of error = 1.80)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerAverageGcTimeInstantaneousAllocation() {
        String logLine = "[11.569s] Trigger: Average GC time (11.12 ms) is above the time for instantaneous allocation "
                + "rate (651 MB/s) to deplete free headroom (6262K) (spike threshold = 1.80)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerAverageGcUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Trigger: Average GC time (52.77 ms) is above the "
                + "time for allocation rate (1313.84 MB/s) to deplete free headroom (67M)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerFree() {
        String logLine = "[24.356s][info][gc] Trigger: Free (6M) is below minimum threshold (6M)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerFreeLearning() {
        String logLine = "[0.410s][info][gc           ] Trigger: Learning 3 of 5. Free (45613K) is below initial "
                + "threshold (45875K)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerFreeSpacesAfterGc() {
        String logLine = "[49.186s][info][gc           ] Trigger: Free (6M) is below minimum threshold (6M)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:49.297-0200][18209ms] Trigger: Free (128M) is below minimum threshold "
                + "(130M)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerHandleAllocationFailure() {
        String logLine = "[52.883s][info][gc           ] Trigger: Handle Allocation Failure";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerLearning() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerMetadataGcThreshold() {
        String logLine = "[2023-02-22T12:31:34.603+0000][2243][gc           ] Trigger: Metadata GC Threshold";
        assertTrue(UnifiedShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnified() {
        List<EventType> eventTypes = new ArrayList<EventType>();
        eventTypes.add(EventType.UNIFIED_SHENANDOAH_TRIGGER);
        assertTrue(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.EventType.UNIFIED_SHENANDOAH_TRIGGER.toString() + " not indentified as unified.");
    }
}
