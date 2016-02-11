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

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestCmsInitialMarkEvent extends TestCase {

    public void testLogLine() {
        String logLine = "251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] " + "4150346K(8367360K), 0.0174433 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".", CmsInitialMarkEvent.match(logLine));
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 251763, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 17, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] " + "4150346K(8367360K), 0.0174433 secs]         ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".", CmsInitialMarkEvent.match(logLine));
    }

    public void testLogLineWithTimesData() {
        String logLine = "251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] " + "4150346K(8367360K), 0.0174433 secs] "
                + "[Times: user=0.02 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as CMS Initial Mark event.", CmsInitialMarkEvent.match(logLine));
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 251763, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 17, event.getDuration());
    }
    
    public void testLogLineJdk8WithTrigger() {
        String logLine = "8.722: [GC (CMS Initial Mark) [1 CMS-initial-mark: 0K(989632K)] 187663K(1986432K), "
                + "0.0157899 secs] [Times: user=0.06 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as CMS Initial Mark event.", CmsInitialMarkEvent.match(logLine));
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 8722, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.", event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_INITIAL_MARK));
        Assert.assertEquals("Duration not parsed correctly.", 15, event.getDuration());
    }
}
