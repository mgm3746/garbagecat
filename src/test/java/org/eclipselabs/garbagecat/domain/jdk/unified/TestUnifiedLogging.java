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

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedLogging extends TestCase {

    public void testDecoratorUptime() {
        String decorator = "[25.016s]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedLogging.DECORATOR));
    }

    public void testDecoratorUptimeMillis() {
        String decorator = "[25016ms]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedLogging.DECORATOR));
    }

    public void testDecoratorTime() {
        String decorator = "[2020-02-14T15:21:55.207-0500]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedLogging.DECORATOR));
    }

    public void testDecoratorTimeUptime() {
        String decorator = "[2020-02-14T15:21:55.207-0500][25.016s]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedLogging.DECORATOR));
    }

    public void testDecoratorTimeUptimemillis() {
        String decorator = "[2020-02-14T15:21:55.207-0500][25016ms]";
        Assert.assertTrue("Time decorator " + decorator + " not recognized.",
                decorator.matches(UnifiedLogging.DECORATOR));
    }
}
