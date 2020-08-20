/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk.unified;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedRegEx extends TestCase {

    public void testTimestampUnifiedLogging() {
        String timestamp = "0.002s";
        Assert.assertTrue("'" + timestamp + "' is a valid timestamp.", timestamp.matches(UnifiedRegEx.UPTIME));
    }

    public void testDurationJdk9() {
        String duration = "2.969ms";
        Assert.assertTrue("'" + duration + "' is a valid duration.", duration.matches(UnifiedRegEx.DURATION));
    }

    public void testDurationJdk9WithSpace() {
        String duration = "15.91 ms";
        Assert.assertTrue("'" + duration + "' is a valid duration.", duration.matches(UnifiedRegEx.DURATION));
    }

    public void testGcEventId() {
        String id = "GC(1326)";
        Assert.assertTrue("'" + id + "' is a valid GC event id.", id.matches(UnifiedRegEx.GC_EVENT_NUMBER));
    }

    public void testGcEventId7Digits() {
        String id = "GC(1234567)";
        Assert.assertTrue("'" + id + "' is a valid GC event id.", id.matches(UnifiedRegEx.GC_EVENT_NUMBER));
    }

    public void testDecoratorUptime() {
        String decorator = "[25.016s]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedRegEx.DECORATOR));
    }

    public void testDecoratorUptimeMillis() {
        String decorator = "[25016ms]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedRegEx.DECORATOR));
    }

    public void testDecoratorUptimeMillis9Digits() {
        String decorator = "[100159717ms]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedRegEx.DECORATOR));
    }

    public void testDecoratorTime() {
        String decorator = "[2020-02-14T15:21:55.207-0500]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedRegEx.DECORATOR));
    }

    public void testDecoratorTimeUptime() {
        String decorator = "[2020-02-14T15:21:55.207-0500][25.016s]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedRegEx.DECORATOR));
    }

    public void testDecoratorTimeUptimemillis() {
        String decorator = "[2020-02-14T15:21:55.207-0500][25016ms]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedRegEx.DECORATOR));
    }

    public void testDecoratorSafepoint() {
        String decorator = "[0.031s][info][safepoint    ]";
        Assert.assertTrue("Decorator " + decorator + " not recognized.", decorator.matches(UnifiedRegEx.DECORATOR));
    }

    public void testDecoratorGcCds() {
        String decorator = "[0.004s][info][gc,cds       ]";
        Assert.assertTrue("Decorator " + decorator + " not recognized.", decorator.matches(UnifiedRegEx.DECORATOR));
    }
}
