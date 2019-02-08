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

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestCmsInitialMarkEvent extends TestCase {

    public void testIsBlocking() {
        String logLine = "8.722: [GC (CMS Initial Mark) [1 CMS-initial-mark: 0K(989632K)] 187663K(1986432K), "
                + "0.0157899 secs] [Times: user=0.06 sys=0.00, real=0.02 secs]";
        Assert.assertTrue(JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + " not indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    public void testLogLine() {
        String logLine = "251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] "
                + "4150346K(8367360K), 0.0174433 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".",
                CmsInitialMarkEvent.match(logLine));
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 251763, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 17443, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] "
                + "4150346K(8367360K), 0.0174433 secs]         ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_INITIAL_MARK.toString() + ".",
                CmsInitialMarkEvent.match(logLine));
    }

    public void testLogLineWithTimesData() {
        String logLine = "251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] "
                + "4150346K(8367360K), 0.0174433 secs] " + "[Times: user=0.02 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as CMS Initial Mark event.", CmsInitialMarkEvent.match(logLine));
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 251763, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 17443, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 2, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 100, event.getParallelism());
    }

    public void testLogLineJdk8WithTrigger() {
        String logLine = "8.722: [GC (CMS Initial Mark) [1 CMS-initial-mark: 0K(989632K)] 187663K(1986432K), "
                + "0.0157899 secs] [Times: user=0.06 sys=0.00, real=0.02 secs]";
        Assert.assertTrue("Log line not recognized as CMS Initial Mark event.", CmsInitialMarkEvent.match(logLine));
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 8722, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_INITIAL_MARK));
        Assert.assertEquals("Duration not parsed correctly.", 15789, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 6, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 2, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 300, event.getParallelism());
    }

    public void testLogLineDatestamp() {
        String logLine = "2016-10-10T18:43:50.728-0700: 3.065: [GC (CMS Initial Mark) [1 CMS-initial-mark: "
                + "6993K(8218240K)] 26689K(8371584K), 0.0091989 secs] [Times: user=0.03 sys=0.00, real=0.01 secs]";
        Assert.assertTrue("Log line not recognized as CMS Initial Mark event.", CmsInitialMarkEvent.match(logLine));
        CmsInitialMarkEvent event = new CmsInitialMarkEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 3065, event.getTimestamp());
        Assert.assertTrue("Trigger not parsed correctly.",
                event.getTrigger().matches(JdkRegEx.TRIGGER_CMS_INITIAL_MARK));
        Assert.assertEquals("Duration not parsed correctly.", 9198, event.getDuration());
        Assert.assertEquals("User time not parsed correctly.", 3, event.getTimeUser());
        Assert.assertEquals("Real time not parsed correctly.", 1, event.getTimeReal());
        Assert.assertEquals("Parallelism not calculated correctly.", 300, event.getParallelism());
    }
}
