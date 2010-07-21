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
public class TestCmsSerialOldEvent extends TestCase {

	public void testLogLine() {
		String logLine = "5.980: [Full GC 5.980: "
				+ "[CMS: 5589K->5796K(122880K), 0.0889610 secs] 11695K->5796K(131072K), "
				+ "[CMS Perm : 13140K->13124K(131072K)], 0.0891270 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".", CmsSerialOldEvent
				.match(logLine));
		CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 5980, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 6106, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 8192, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 5589, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 5796, event.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 122880, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 13140, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 13124, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 89, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "5.980: [Full GC 5.980: "
				+ "[CMS: 5589K->5796K(122880K), 0.0889610 secs] 11695K->5796K(131072K), "
				+ "[CMS Perm : 13140K->13124K(131072K)], 0.0891270 secs] ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".", CmsSerialOldEvent
				.match(logLine));
	}

	public void testLogLineJdk16() {
		String logLine = "2.425: [Full GC (System) 2.425: "
				+ "[CMS: 1231K->2846K(114688K), 0.0827010 secs] 8793K->2846K(129472K), "
				+ "[CMS Perm : 8602K->8593K(131072K)], 0.0828090 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".", CmsSerialOldEvent
				.match(logLine));
		CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 2425, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 7562, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 14784, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1231, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 2846, event.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 114688, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 8602, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 8593, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 82, event.getDuration());
	}

	public void testLogLineIcmsDcData() {
		String logLine = "165.805: [Full GC 165.805: [CMS: 101481K->97352K(1572864K), 1.1183800 secs] "
				+ "287075K->97352K(2080768K), [CMS Perm : 68021K->67965K(262144K)] icms_dc=10 , 1.1186020 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD.toString() + ".", CmsSerialOldEvent
				.match(logLine));
		CmsSerialOldEvent event = new CmsSerialOldEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 165805, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (287075 - 101481), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (97352 - 97352), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (2080768 - 1572864),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 101481, event
				.getOldOccupancyInit());
		Assert
				.assertEquals("Old end size not parsed correctly.", 97352, event
						.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1572864, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 68021, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 67965, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 1118, event.getDuration());
	}
}
