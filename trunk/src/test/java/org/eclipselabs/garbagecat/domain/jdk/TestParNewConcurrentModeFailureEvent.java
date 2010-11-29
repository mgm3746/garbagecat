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
public class TestParNewConcurrentModeFailureEvent extends TestCase {

	public void testLogLine() {
		String logLine = "26683.209: [GC 26683.210: [ParNew: 261760K->261760K(261952K), "
				+ "0.0000130 secs]26683.210: [CMS (concurrent mode failure): 1141548K->1078465K(1179648K), "
				+ "7.3835370 secs] 1403308K->1078465K(1441600K), 7.3838390 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString() + ".",
				ParNewConcurrentModeFailureEvent.match(logLine));
		ParNewConcurrentModeFailureEvent event = new ParNewConcurrentModeFailureEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 26683209, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (1403308 - 1141548), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1078465 - 1078465), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1141548, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1078465, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 7383, event.getDuration());
	}

	public void testAbortablePrecleanLogLine() {
		String logLine = "27067.966: [GC 27067.966: [ParNew: 261760K->261760K(261952K), 0.0000160 secs]"
				+ "27067.966: [CMS27067.966: [CMS-concurrent-abortable-preclean: 2.272/29.793 secs] "
				+ "(concurrent mode failure): 1147900K->1155037K(1179648K), 7.3953900 secs] "
				+ "1409660K->1155037K(1441600K), 7.3957620 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString() + ".",
				ParNewConcurrentModeFailureEvent.match(logLine));
		ParNewConcurrentModeFailureEvent event = new ParNewConcurrentModeFailureEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 27067966, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (1409660 - 1147900), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1155037 - 1155037), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1147900, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1155037, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 7395, event.getDuration());
	}

	public void testPrecleanIncrementalModeLogLine() {
		String logLine = "5075.405: [GC 5075.405: [ParNew: 261760K->261760K(261952K), 0.0000750 secs]"
				+ "5075.405: [CMS5081.144: [CMS-concurrent-preclean: 14.653/31.189 secs] "
				+ "(concurrent mode failure): 1796901K->1078231K(1835008K), 96.6130290 secs] "
				+ "2058661K->1078231K(2096960K) icms_dc=100 , 96.6140400 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString() + ".",
				ParNewConcurrentModeFailureEvent.match(logLine));
		ParNewConcurrentModeFailureEvent event = new ParNewConcurrentModeFailureEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 5075405, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (2058661 - 1796901), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1078231 - 1078231), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (2096960 - 1835008),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1796901, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1078231, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1835008, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 96614, event.getDuration());
	}

	public void testSweepIncrementalModeLogLine() {
		String logLine = "5062.472: [GC 5062.472: [ParNew: 261760K->261760K(261952K), 0.0000610 secs]"
				+ "5062.473: [CMS5073.876: [CMS-concurrent-sweep: 11.563/14.268 secs] "
				+ "(concurrent mode failure): 1808263K->1078640K(1835008K), 88.9224480 secs] "
				+ "2070023K->1078640K(2096960K) icms_dc=100 , 88.9234900 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString() + ".",
				ParNewConcurrentModeFailureEvent.match(logLine));
		ParNewConcurrentModeFailureEvent event = new ParNewConcurrentModeFailureEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 5062472, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (2070023 - 1808263), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1078640 - 1078640), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (2096960 - 1835008),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1808263, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1078640, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1835008, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 88923, event.getDuration());
	}

	public void testMarkLogLine() {
		String logLine = "27636.893: [GC 27636.893: [ParNew: 261760K->261760K(261952K), 0.0000130 secs]"
				+ "27636.893: [CMS27639.231: [CMS-concurrent-mark: 4.803/4.803 secs] "
				+ "(concurrent mode failure): 1150993K->1147420K(1179648K), 9.9779890 secs] "
				+ "1412753K->1147420K(1441600K), 9.9783140 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString() + ".",
				ParNewConcurrentModeFailureEvent.match(logLine));
		ParNewConcurrentModeFailureEvent event = new ParNewConcurrentModeFailureEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 27636893, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (1412753 - 1150993), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1147420 - 1147420), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1150993, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1147420, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 9978, event.getDuration());
	}

	public void testLogLineWithTimesData() {
		String logLine = "26683.209: [GC 26683.210: [ParNew: 261760K->261760K(261952K), "
				+ "0.0000130 secs]26683.210: [CMS (concurrent mode failure): 1141548K->1078465K(1179648K), "
				+ "7.3835370 secs] 1403308K->1078465K(1441600K), 7.3838390 secs]"
				+ " [Times: user=0.29 sys=0.02, real=3.97 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString() + ".",
				ParNewConcurrentModeFailureEvent.match(logLine));
		ParNewConcurrentModeFailureEvent event = new ParNewConcurrentModeFailureEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 26683209, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (1403308 - 1141548), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1078465 - 1078465), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1141548, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1078465, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 7383, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "26683.209: [GC 26683.210: [ParNew: 261760K->261760K(261952K), "
				+ "0.0000130 secs]26683.210: [CMS (concurrent mode failure): 1141548K->1078465K(1179648K), "
				+ "7.3835370 secs] 1403308K->1078465K(1441600K), 7.3838390 secs] ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString() + ".",
				ParNewConcurrentModeFailureEvent.match(logLine));
	}
}
