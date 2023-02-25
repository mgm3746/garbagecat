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
class TestShenandoahTriggerEvent {

    @Test
    void testIdentityEventType() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        assertEquals(JdkUtil.LogEventType.SHENANDOAH_TRIGGER, JdkUtil.identifyEventType(logLine, null),
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER + "not identified.");
    }

    @Test
    void testJdk8TriggerLearning() {
        String logLine = "Trigger: Learning 1 of 5. Free (45118K) is below initial threshold (45875K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testParseLogLine() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        assertTrue(JdkUtil.parseLogLine(logLine, null) instanceof ShenandoahTriggerEvent,
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " not parsed.");
    }

    @Test
    void testReportable() {
        assertFalse(JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_TRIGGER),
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as reportable.");
    }

    @Test
    void testTriggerAverageGc() {
        String logLine = "Trigger: Average GC time (12.56 ms) is above the time for allocation rate (899 MB/s) to "
                + "deplete free headroom (11466K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testTriggerAverageGcRateBb() {
        String logLine = "Trigger: Average GC time (6458.98 ms) is above the time for allocation rate (89583 BB/s) "
                + "to deplete free headroom (0B)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_TRIGGER);
        assertFalse(UnifiedUtil.isUnifiedLogging(eventTypes),
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as unified.");
    }

    @Test
    void testUnifiedFreeTimeSinceLastGcUptimeMillis() {
        String logLine = "[2019-02-05T15:10:00.671-0200][1349583ms] Trigger: Time since last GC (300004 ms) is larger "
                + "than guaranteed interval (300000 ms)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTrigger() {
        String logLine = "[2019-02-05T14:47:34.156-0200][3068ms] Trigger: Learning 1 of 5. Free (912M) is below "
                + "initial threshold (912M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerAverageGc() {
        String logLine = "[41.917s][info][gc           ] Trigger: Average GC time (26.32 ms) is above the time for "
                + "allocation rate (324.68 MB/s) to deplete free headroom (8M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerAverageGcAllocationRateKb6Digits() {
        String logLine = "[63.328s][info][gc           ] Trigger: Average GC time (77.12 ms) is above the time for "
                + "allocation rate (101894 KB/s) to deplete free headroom (6846K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerAverageGcAllocationRateWholeNumber() {
        String logLine = "[1.757s][info][gc           ] Trigger: Average GC time (9.74 ms) is above the time for "
                + "allocation rate (1244 MB/s) to deplete free headroom (11236K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerAverageGcTimeAverageAllocation() {
        String logLine = "[10.508s][info][gc          ] Trigger: Average GC time (16.09 ms) is above the time for "
                + "average allocation rate (409 MB/s) to deplete free headroom (5742K) (margin of error = 1.80)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerAverageGcTimeInstantaneousAllocation() {
        String logLine = "[11.569s] Trigger: Average GC time (11.12 ms) is above the time for instantaneous allocation "
                + "rate (651 MB/s) to deplete free headroom (6262K) (spike threshold = 1.80)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerAverageGcUptimeMillis() {
        String logLine = "[2019-02-05T14:48:05.666-0200][34578ms] Trigger: Average GC time (52.77 ms) is above the "
                + "time for allocation rate (1313.84 MB/s) to deplete free headroom (67M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerFree() {
        String logLine = "[24.356s][info][gc] Trigger: Free (6M) is below minimum threshold (6M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerFreeLearning() {
        String logLine = "[0.410s][info][gc           ] Trigger: Learning 3 of 5. Free (45613K) is below initial "
                + "threshold (45875K)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerFreeNoDecorator() {
        String logLine = "Trigger: Free (168M) is below minimum threshold (168M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerFreeSpacesAfterGc() {
        String logLine = "[49.186s][info][gc           ] Trigger: Free (6M) is below minimum threshold (6M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerFreeUptimeMillis() {
        String logLine = "[2019-02-05T14:47:49.297-0200][18209ms] Trigger: Free (128M) is below minimum threshold "
                + "(130M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerHandleAllocationFailure() {
        String logLine = "[52.883s][info][gc           ] Trigger: Handle Allocation Failure";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerLearning() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }

    @Test
    void testUnifiedTriggerMetadataGcThreshold() {
        String logLine = "[2023-02-22T12:31:34.603+0000][2243][gc           ] Trigger: Metadata GC Threshold";
        assertTrue(ShenandoahTriggerEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".");
    }
}
