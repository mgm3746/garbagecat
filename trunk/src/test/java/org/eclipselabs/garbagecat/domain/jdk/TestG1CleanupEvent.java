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
public class TestG1CleanupEvent extends TestCase {
    public void testCleanup() {
        String logLine = "18.650: [GC cleanup 297M->236M(512M), 0.0014690 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_CLEANUP.toString() + ".", G1CleanupEvent.match(logLine));
    }
}
