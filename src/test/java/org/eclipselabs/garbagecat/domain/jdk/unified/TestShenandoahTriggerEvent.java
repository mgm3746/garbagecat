/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahTriggerEvent extends TestCase {

    public void testLineTriggerLearning() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_TRIGGER + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahTriggerEvent);
    }

    public void testNotBlocking() {
        String logLine = "[0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)";
        Assert.assertFalse(JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_TRIGGER));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_TRIGGER);
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineTriggerAverage() {
        String logLine = "[0.864s][info][gc] Trigger: Average GC time (15.91 ms) is above the time for allocation rate "
                + "(829.64 MB/s) to deplete free headroom (11M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }

    public void testLineTriggerFree() {
        String logLine = "[24.356s][info][gc] Trigger: Free (6M) is below minimum threshold (6M)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_TRIGGER.toString() + ".",
                ShenandoahTriggerEvent.match(logLine));
    }
}
