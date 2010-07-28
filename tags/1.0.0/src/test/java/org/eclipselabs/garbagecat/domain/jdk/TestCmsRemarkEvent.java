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
public class TestCmsRemarkEvent extends TestCase {

	public void testLogLine() {
		String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
				+ "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
				+ "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_REMARK.toString() + ".", CmsRemarkEvent.match(logLine));
		CmsRemarkEvent event = new CmsRemarkEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 253103, event.getTimestamp());
		Assert.assertEquals("Duration not parsed correctly.", 85, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
				+ "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
				+ "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]  ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_REMARK.toString() + ".", CmsRemarkEvent.match(logLine));
	}

	public void testLogLineWithTimesData() {
		String logLine = "253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: "
				+ "[Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] "
				+ "[1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs] "
				+ "[Times: user=0.15 sys=0.01, real=0.09 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_REMARK.toString() + ".", CmsRemarkEvent.match(logLine));
		CmsRemarkEvent event = new CmsRemarkEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 253103, event.getTimestamp());
		Assert.assertEquals("Duration not parsed correctly.", 85, event.getDuration());
	}
}
