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
public class TestParallelSerialOldEvent extends TestCase {

	public void testLogLine() {
		String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] "
				+ "[PSOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) "
				+ "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".", ParallelSerialOldEvent
				.match(logLine));
		ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 3600, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 5424, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 38208, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 488, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 5786, event.getOldOccupancyEnd());
		Assert
				.assertEquals("Old allocation size not parsed correctly.", 87424, event
						.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 13092, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 13094, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 69, event.getDuration());
	}

	public void testLogLineWhiteSpaceAtEnd() {
		String logLine = "3.600: [Full GC [PSYoungGen: 5424K->0K(38208K)] "
				+ "[PSOldGen: 488K->5786K(87424K)] 5912K->5786K(125632K) "
				+ "[PSPermGen: 13092K->13094K(131072K)], 0.0699360 secs]  ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".", ParallelSerialOldEvent
				.match(logLine));
	}

	public void testLogLineJdk16() {
		String logLine = "4.165: [Full GC (System) [PSYoungGen: 1784K->0K(12736K)] "
				+ "[PSOldGen: 1081K->2855K(116544K)] 2865K->2855K(129280K) "
				+ "[PSPermGen: 8600K->8600K(131072K)], 0.0427680 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString() + ".", ParallelSerialOldEvent
				.match(logLine));
		ParallelSerialOldEvent event = new ParallelSerialOldEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 4165, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 1784, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 12736, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1081, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 2855, event.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 116544, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 8600, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 8600, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 42, event.getDuration());
	}
}
