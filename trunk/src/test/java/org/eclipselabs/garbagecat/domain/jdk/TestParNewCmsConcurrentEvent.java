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
public class TestParNewCmsConcurrentEvent extends TestCase {

	public void testLogLine() {
		String logLine = "2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: "
				+ "0.043/0.144 secs]: 212981K->3156K(242304K), 0.0364435 secs] 4712182K->4502357K(4971420K), "
				+ "0.0368807 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CMS_CONCURRENT.toString() + ".",
				ParNewCmsConcurrentEvent.match(logLine));
		ParNewCmsConcurrentEvent event = new ParNewCmsConcurrentEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 2210281, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 212981, event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", 3156, event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 242304, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", (4712182 - 212981), event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", (4502357 - 3156), event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", (4971420 - 242304), event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 36, event.getDuration());
	}

	public void testLogLineWithTimesData() {
		String logLine = "2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: "
				+ "0.043/0.144 secs]: 212981K->3156K(242304K), 0.0364435 secs] 4712182K->4502357K(4971420K), "
				+ "0.0368807 secs] [Times: user=0.18 sys=0.02, real=0.04 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CMS_CONCURRENT.toString() + ".",
				ParNewCmsConcurrentEvent.match(logLine));
		ParNewCmsConcurrentEvent event = new ParNewCmsConcurrentEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 2210281, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 212981, event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", 3156, event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 242304, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", (4712182 - 212981), event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", (4502357 - 3156), event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", (4971420 - 242304), event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 36, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: "
				+ "0.043/0.144 secs]: 212981K->3156K(242304K), 0.0364435 secs] 4712182K->4502357K(4971420K), "
				+ "0.0368807 secs]    ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CMS_CONCURRENT.toString() + ".",
				ParNewCmsConcurrentEvent.match(logLine));
	}
}
