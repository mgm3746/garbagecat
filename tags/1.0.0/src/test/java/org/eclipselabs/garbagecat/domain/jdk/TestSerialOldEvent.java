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
public class TestSerialOldEvent extends TestCase {

	public void testLogLine() {
		String logLine = "187.159: [Full GC 187.160: "
				+ "[Tenured: 97171K->102832K(815616K), 0.6977443 secs] 152213K->102832K(907328K), "
				+ "[Perm : 49152K->49154K(49158K)], 0.6929258 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.SERIAL_OLD.toString() + ".", SerialOldEvent.match(logLine));
		SerialOldEvent event = new SerialOldEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 187159, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 55042, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 91712, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 97171, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 102832, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 815616, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 49152, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 49154, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 49158, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 692, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "187.159: [Full GC 187.160: "
				+ "[Tenured: 97171K->102832K(815616K), 0.6977443 secs] 152213K->102832K(907328K), "
				+ "[Perm : 49152K->49154K(49158K)], 0.6929258 secs]       ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.SERIAL_OLD.toString() + ".", SerialOldEvent.match(logLine));
	}

	public void testLogLineJdf16() {
		String logLine = "2.457: [Full GC (System) 2.457: "
				+ "[Tenured: 1092K->2866K(116544K), 0.0489980 secs] 11012K->2866K(129664K), "
				+ "[Perm : 8602K->8604K(131072K)], 0.0490880 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.SERIAL_OLD.toString() + ".", SerialOldEvent.match(logLine));
		SerialOldEvent event = new SerialOldEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 2457, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 9920, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 13120, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1092, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 2866, event.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 116544, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 8602, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 8604, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 49, event.getDuration());
	}
}
