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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestShenandoahConsiderClUnloadConcMarkEvent extends TestCase {

    public void testLineUnifie() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads";
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString() + ".",
                ShenandoahConsiderClassUnloadingConcMarkEvent.match(logLine));
        ShenandoahConsiderClassUnloadingConcMarkEvent event = new ShenandoahConsiderClassUnloadingConcMarkEvent(
                logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 1, event.getTimestamp());
    }

    public void testIdentityEventType() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads";
        Assert.assertEquals(JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK + "not identified.",
                JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK, JdkUtil.identifyEventType(logLine));
    }

    public void testParseLogLine() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads";
        Assert.assertTrue(
                JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString() + " not parsed.",
                JdkUtil.parseLogLine(logLine) instanceof ShenandoahConsiderClassUnloadingConcMarkEvent);
    }

    public void testNotBlocking() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads";
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString()
                        + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testReportable() {
        Assert.assertFalse(
                JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString()
                        + " indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK));
    }

    public void testUnified() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK);
        Assert.assertFalse(JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString()
                + " incorrectly indentified as unified.", UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testLineWithSpaces() {
        String logLine = "[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are "
                + "observed on class-unloading sensitive workloads     ";
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString() + ".",
                ShenandoahConsiderClassUnloadingConcMarkEvent.match(logLine));
    }

    public void testLineTimeUptimemillis() {
        String logLine = "[2019-02-05T14:47:31.090-0200][2ms] Consider -XX:+ClassUnloadingWithConcurrentMark if large "
                + "pause times are observed on class-unloading sensitive workloads";
        Assert.assertTrue(
                "Log line not recognized as "
                        + JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString() + ".",
                ShenandoahConsiderClassUnloadingConcMarkEvent.match(logLine));
        ShenandoahConsiderClassUnloadingConcMarkEvent event = new ShenandoahConsiderClassUnloadingConcMarkEvent(
                logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 2, event.getTimestamp());
    }
}
