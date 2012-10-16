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
public class TestG1MixedPause extends TestCase {
    public void testMixedPause() {
        String logLine = "72.598: [GC pause (mixed) 643M->513M(724M), 0.1686650 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.G1_MIXED_PAUSE.toString() + ".", G1MixedPause.match(logLine));
    }
}
