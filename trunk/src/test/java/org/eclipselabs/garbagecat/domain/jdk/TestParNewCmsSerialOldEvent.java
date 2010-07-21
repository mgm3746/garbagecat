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
public class TestParNewCmsSerialOldEvent extends TestCase {

	public void testLogLine() {
		String logLine = "42782.086: [GC 42782.086: [ParNew: 254464K->7680K(254464K), 0.2853553 secs]"
				+ "42782.371: [Tenured: 1082057K->934941K(1082084K), 6.2719770 secs] "
				+ "1310721K->934941K(1336548K), 6.5587770 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CMS_SERIAL_OLD.toString() + ".",
				ParNewCmsSerialOldEvent.match(logLine));
		ParNewCmsSerialOldEvent event = new ParNewCmsSerialOldEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 42782086, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 254464, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 254464, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1082057, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 934941, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1082084, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 6558, event.getDuration());
	}

	public void testLogLineWithTimesData() {
		String logLine = "42782.086: [GC 42782.086: [ParNew: 254464K->7680K(254464K), 0.2853553 secs]"
				+ "42782.371: [Tenured: 1082057K->934941K(1082084K), 6.2719770 secs] "
				+ "1310721K->934941K(1336548K), 6.5587770 secs] "
				+ "[Times: user=0.34 sys=0.01, real=0.05 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CMS_SERIAL_OLD.toString() + ".",
				ParNewCmsSerialOldEvent.match(logLine));
		ParNewCmsSerialOldEvent event = new ParNewCmsSerialOldEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 42782086, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 254464, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 254464, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1082057, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 934941, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1082084, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 6558, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "42782.086: [GC 42782.086: [ParNew: 254464K->7680K(254464K), 0.2853553 secs]"
				+ "42782.371: [Tenured: 1082057K->934941K(1082084K), 6.2719770 secs] "
				+ "1310721K->934941K(1336548K), 6.5587770 secs]    ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CMS_SERIAL_OLD.toString() + ".",
				ParNewCmsSerialOldEvent.match(logLine));
	}
}
