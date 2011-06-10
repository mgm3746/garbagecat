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

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestApplicationStoppedTimeEvent extends TestCase {

    public void testLogLine() {
        String logLine = "Total time for which application threads were stopped: 0.0968457 seconds";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".", ApplicationStoppedTimeEvent.match(logLine));
    }

    public void testLogLineWithSpacesAtEnd() {
        String logLine = "Total time for which application threads were stopped: 0.0968457 seconds  ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.APPLICATION_CONCURRENT_TIME.toString() + ".", ApplicationStoppedTimeEvent.match(logLine));
    }
}
