/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestHeaderVersionEvent extends TestCase {

    public void testLine() {
        String logLine = "Java HotSpot(TM) 64-Bit Server VM (24.85-b08) for linux-amd64 JRE (1.7.0_85-b34), built on "
                + "Sep 29 2015 08:44:21 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEADER_VERSION.toString() + ".",
                HeaderVersionEvent.match(logLine));
    }
    
    public void testLineOpenJdk() {
        String logLine = "OpenJDK 64-Bit Server VM (24.95-b01) for linux-amd64 JRE (1.7.0_95-b00), built on "
                + "Jan 18 2016 21:57:50 by \"mockbuild\" with gcc 4.8.5 20150623 (Red Hat 4.8.5-4)";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEADER_VERSION.toString() + ".",
                HeaderVersionEvent.match(logLine));
    }
}
