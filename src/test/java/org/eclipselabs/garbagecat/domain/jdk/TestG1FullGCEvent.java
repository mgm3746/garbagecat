/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2012 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 ******************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author James Livingston
 */
public class TestG1FullGCEvent extends TestCase {
    public void testFullSystemGC() {
        String logLine = "1302.524: [Full GC (System.gc()) 653M->586M(979M), 1.6364900 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_FULL_GC.toString() + ".", G1FullGCEvent.match(logLine));
    }
}
