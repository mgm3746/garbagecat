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
public class TestVerboseGcOldEvent extends TestCase {

	public void testLogLine() {
		String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".", VerboseGcOldEvent
				.match(logLine));
		VerboseGcOldEvent event = new VerboseGcOldEvent(logLine);
		Assert.assertEquals("Event name incorrect.",
				JdkUtil.LogEventType.VERBOSE_GC_OLD.toString(), event.getName());
		Assert.assertEquals("Time stamp not parsed correctly.", 2143132151L, event.getTimestamp());
		Assert.assertEquals("Combined begin size not parsed correctly.", 1606823, event
				.getCombinedOccupancyInit());
		Assert.assertEquals("Combined end size not parsed correctly.", 1409859, event
				.getCombinedOccupancyEnd());
		Assert.assertEquals("Combined allocation size not parsed correctly.", 2976064, event
				.getCombinedSpace());
		Assert.assertEquals("Duration not parsed correctly.", 12085, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "2143132.151: [Full GC 1606823K->1409859K(2976064K), 12.0855599 secs]    ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.VERBOSE_GC_OLD.toString() + ".", VerboseGcOldEvent
				.match(logLine));
	}
}
