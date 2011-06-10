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
public class TestTruncatedEvent extends TestCase {

    public void testCmsSerialOldLine() {
        String logLine = "100.714: [Full GC 100.714: [CMS";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TRUNCATED.toString() + ".", TruncatedEvent.match(logLine));
        TruncatedEvent event = new TruncatedEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 100714, event.getTimestamp());
    }

    public void testParNewLine() {
        String logLine = "9641.622: [GC 9641.622: [ParNew9641.696: [CMS-concurrent-abortable-preclean: " + "0.029/0.129 secs] [Times: user=0.25 sys=0.07, real=0.13 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.TRUNCATED.toString() + ".", TruncatedEvent.match(logLine));
        TruncatedEvent event = new TruncatedEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 9641622, event.getTimestamp());
    }
}
