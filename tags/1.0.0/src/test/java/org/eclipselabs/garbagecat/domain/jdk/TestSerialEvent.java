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
public class TestSerialEvent extends TestCase {

	public void testLogLine() {
		String logLine = "7.798: [GC 7.798: [DefNew: 37172K->3631K(39296K), 0.0209300 secs] "
				+ "41677K->10314K(126720K), 0.0210210 secs]";
		Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL.toString()
				+ ".", SerialEvent.match(logLine));
		SerialEvent event = new SerialEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 7798, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 37172, event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", 3631, event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 39296, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 4505, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 6683, event.getOldOccupancyEnd());
		Assert
				.assertEquals("Old allocation size not parsed correctly.", 87424, event
						.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 21, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "7.798: [GC 7.798: [DefNew: 37172K->3631K(39296K), 0.0209300 secs] "
				+ "41677K->10314K(126720K), 0.0210210 secs] ";
		Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.SERIAL.toString()
				+ ".", SerialEvent.match(logLine));
	}
}
