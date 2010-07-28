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
public class TestParNewConcurrentModeFailurePermDataEvent extends TestCase {

	public void testMarkLogLine() {
		String logLine = "3070.289: [GC 3070.289: [ParNew: 207744K->207744K(242304K), 0.0000682 secs]"
				+ "3070.289: [CMS3081.621: [CMS-concurrent-mark: 11.907/12.958 secs] "
				+ "(concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] "
				+ "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
				ParNewConcurrentModeFailurePermDataEvent.match(logLine));
		ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 3070289, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (6217865 - 6010121), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (6028029 - 6014591), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (6256896 - 6014592),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 6010121, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 6014591, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 6014592, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 206688, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 206662, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 79050, event.getDuration());
	}

	public void testSweepLogLine() {
		String logLine = "5300.084: [GC 5300.084: [ParNew: 58640K->196K(242304K), 0.0278816 secs]5300.112: "
				+ "[CMS5302.628: [CMS-concurrent-sweep: 7.250/7.666 secs] (concurrent mode failure): "
				+ "2274119K->1412827K(4374712K), 17.8109394 secs] 2332760K->1412827K(4617016K), "
				+ "[CMS Perm : 242246K->231354K(262144K)], 17.8393597 secs] "
				+ "[Times: user=17.97 sys=0.03, real=17.84 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
				ParNewConcurrentModeFailurePermDataEvent.match(logLine));
		ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 5300084, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (2332760 - 2274119), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1412827 - 1412827), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (4617016 - 4374712),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 2274119, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1412827, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 4374712, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 242246, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 231354, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 17839, event.getDuration());
	}

	public void testAbortablePrecleanLogLine() {
		String logLine = "877434.435: [GC 877434.436: [ParNew: 786393K->786393K(917504K), 0.0000350 secs]"
				+ "877434.436: [CMS877434.471: [CMS-concurrent-abortable-preclean: 0.430/3.095 secs] "
				+ "(concurrent mode failure): 1547074K->1545776K(1572864K), 10.4623320 secs] "
				+ "2333467K->1545776K(2490368K), [CMS Perm : 46359K->46354K(77352K)], 10.4637840 secs] "
				+ "[Times: user=10.47 sys=0.00, real=10.47 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
				ParNewConcurrentModeFailurePermDataEvent.match(logLine));
		ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 877434435, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (2333467 - 1547074), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1545776 - 1545776), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (2490368 - 1572864),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1547074, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1545776, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1572864, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 46359, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 46354, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 77352, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 10463, event.getDuration());
	}

	public void testAbortedAbortablePrecleanLogLine() {
		String logLine = "1319504.530: [GC 1319504.531: [ParNew: 786379K->786379K(917504K), 0.0000350 secs]"
				+ "1319504.531: [CMS CMS: abort preclean due to time 1319504.546: "
				+ "[CMS-concurrent-abortable-preclean: 0.682/5.152 secs] (concurrent mode failure): "
				+ "1572393K->1558908K(1572864K), 10.4092880 secs] 2358773K->1558908K(2490368K), "
				+ "[CMS Perm : 46531K->46526K(77608K)], 10.4107800 secs] "
				+ "[Times: user=10.41 sys=0.00, real=10.41 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
				ParNewConcurrentModeFailurePermDataEvent.match(logLine));
		ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 1319504530, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (2358773 - 1572393), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1558908 - 1558908), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (2490368 - 1572864),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1572393, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1558908, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1572864, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 46531, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 46526, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 77608, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 10410, event.getDuration());
	}

	public void testPrecleanLogLine() {
		String logLine = "64559.018: [GC 64559.019: [ParNew: 242304K->242304K(242304K), 0.0000693 secs]"
				+ "64559.019: [CMS64563.293: [CMS-concurrent-preclean: 5.112/5.323 secs] "
				+ "(concurrent mode failure): 12199797K->11189714K(12306048K), 183.4844975 secs] "
				+ "12442101K->11189714K(12548352K), [CMS Perm : 225141K->225078K(262144K)], 183.4850018 secs] "
				+ "[Times: user=183.42 sys=0.08, real=183.49 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
				ParNewConcurrentModeFailurePermDataEvent.match(logLine));
		ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 64559018, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (12442101 - 12199797), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (11189714 - 11189714), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (12548352 - 12306048),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 12199797, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 11189714, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 12306048, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 225141, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 225078, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 183485, event.getDuration());
	}

	public void testIncrementalModeLine() {
		String logLine = "215624.131: [GC 215624.131: [ParNew: 2007040K->2007040K(2007040K), 0.0000474 secs]"
				+ "215624.131: [CMS215624.140: [CMS-concurrent-abortable-preclean: 0.102/0.115 secs] "
				+ "(concurrent mode failure): 9991384K->1839256K(10387456K), 37.1572093 secs] "
				+ "11998424K->1839256K(12394496K), [CMS Perm : 250682K->250673K(524288K)] icms_dc=100 , "
				+ "37.1577095 secs] [Times: user=37.15 sys=0.02, real=37.16 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
				ParNewConcurrentModeFailurePermDataEvent.match(logLine));
		ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 215624131, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (11998424 - 9991384), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1839256 - 1839256), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (12394496 - 10387456),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 9991384, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1839256, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 10387456, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 250682, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 250673, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 524288, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 37157, event.getDuration());
	}

	public void testLogLineWithTimesData() {
		String logLine = "3070.289: [GC 3070.289: [ParNew: 207744K->207744K(242304K), 0.0000682 secs]"
				+ "3070.289: [CMS3081.621: [CMS-concurrent-mark: 11.907/12.958 secs] "
				+ "(concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] "
				+ "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs] "
				+ "[Times: user=104.69 sys=3.63, real=79.05 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
				ParNewConcurrentModeFailurePermDataEvent.match(logLine));
		ParNewConcurrentModeFailurePermDataEvent event = new ParNewConcurrentModeFailurePermDataEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 3070289, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (6217865 - 6010121), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (6028029 - 6014591), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (6256896 - 6014592),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 6010121, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 6014591, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 6014592, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 206688, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 206662, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 79050, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "3070.289: [GC 3070.289: [ParNew: 207744K->207744K(242304K), 0.0000682 secs]"
				+ "3070.289: [CMS3081.621: [CMS-concurrent-mark: 11.907/12.958 secs] "
				+ "(concurrent mode failure): 6010121K->6014591K(6014592K), 79.0505229 secs] "
				+ "6217865K->6028029K(6256896K), [CMS Perm : 206688K->206662K(262144K)], 79.0509595 secs]  ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA.toString() + ".",
				ParNewConcurrentModeFailurePermDataEvent.match(logLine));
	}
}
