/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
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
public class TestShenandoahCancellingGcEvent extends TestCase {

    public void testLineTriggerLearning() {
        String logLine = "[72.659s][info][gc] Cancelling GC: Stopping VM";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC.toString() + ".",
                ShenandoahCancellingGcEvent.match(logLine));
    }

    public void testIdentityEventType() {
        String logLine = "[72.659s][info][gc] Cancelling GC: Stopping VM";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[72.659s][info][gc] Cancelling GC: Stopping VM";
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahCancellingGcEvent);
    }

    public void testNotBlocking() {
        String logLine = "[72.659s][info][gc] Cancelling GC: Stopping VM";
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_CANCELLING_GC);
        Assert.assertTrue(JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC.toString() + " not indentified as unified.",
                JdkUtil.isUnifiedLogging(eventTypes));
    }

    public void testDetailed() {
        String logLine = "[69.941s][info][gc           ] Cancelling GC: Stopping VM";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC.toString() + ".",
                ShenandoahCancellingGcEvent.match(logLine));
    }

    public void testUptimeMillis() {
        String logLine = "[2019-02-05T15:10:08.997-0200][1357909ms] Cancelling GC: Stopping VM";
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.LogEventType.SHENANDOAH_CANCELLING_GC.toString() + ".",
                ShenandoahCancellingGcEvent.match(logLine));
    }
}
