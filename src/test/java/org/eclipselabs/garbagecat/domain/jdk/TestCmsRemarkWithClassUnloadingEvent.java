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
public class TestCmsRemarkWithClassUnloadingEvent extends TestCase {

    public void testLogLine() {
        String logLine = "76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: " + "[Rescan (parallel) , 0.0574180 secs]76694.785: " + "[weak refs processing, 0.0170540 secs]76694.802: "
                + "[class unloading, 0.0363010 secs]76694.838: " + "[scrub symbol & string tables, 0.0276600 secs] " + "[1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING.toString() + ".", CmsRemarkWithClassUnloadingEvent.match(logLine));
        CmsRemarkWithClassUnloadingEvent event = new CmsRemarkWithClassUnloadingEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 76694727, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 144, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: " + "[Rescan (parallel) , 0.0574180 secs]76694.785: " + "[weak refs processing, 0.0170540 secs]76694.802: "
                + "[class unloading, 0.0363010 secs]76694.838: " + "[scrub symbol & string tables, 0.0276600 secs] " + "[1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs]     ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING.toString() + ".", CmsRemarkWithClassUnloadingEvent.match(logLine));
    }

    public void testLogLineWithTimesData() {
        String logLine = "76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: " + "[Rescan (parallel) , 0.0574180 secs]76694.785: " + "[weak refs processing, 0.0170540 secs]76694.802: "
                + "[class unloading, 0.0363010 secs]76694.838: " + "[scrub symbol & string tables, 0.0276600 secs] " + "[1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs] "
                + "[Times: user=0.17 sys=0.00, real=0.15 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING.toString() + ".", CmsRemarkWithClassUnloadingEvent.match(logLine));
        CmsRemarkWithClassUnloadingEvent event = new CmsRemarkWithClassUnloadingEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 76694727, event.getTimestamp());
        Assert.assertEquals("Duration not parsed correctly.", 144, event.getDuration());
    }
}
