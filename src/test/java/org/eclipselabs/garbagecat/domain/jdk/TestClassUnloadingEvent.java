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
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestClassUnloadingEvent extends TestCase {

    public void testLine() {
        String logLine = "[Unloading class $Proxy61]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_UNLOADING.toString() + ".",
                ClassUnloadingEvent.match(logLine));
    }

    public void testLineWithUnderline() {
        String logLine = "[Unloading class MyClass_1234153487841_717989]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CLASS_UNLOADING.toString() + ".",
                ClassUnloadingEvent.match(logLine));
    }
}
