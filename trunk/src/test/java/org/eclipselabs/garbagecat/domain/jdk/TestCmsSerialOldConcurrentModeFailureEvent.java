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
public class TestCmsSerialOldConcurrentModeFailureEvent extends TestCase {

	public void testLogLineExtraRtBracket() {
		String logLine = "28282.075: [Full GC 28282.075 (concurrent mode failure): "
				+ "1179601K->1179648K(1179648K), 10.7510650 secs] 1441361K->1180553K(1441600K), "
				+ "[CMS Perm : 71172K->71171K(262144K)], 10.7515460 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 28282075, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (1441361 - 1179601), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1180553 - 1179648), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1179601, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1179648, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 71172, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 71171, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 10751, event.getDuration());
	}

	public void testLogLineBalancedBrackets() {
		String logLine = "6942.991: [Full GC 6942.991: [CMS (concurrent mode failure): "
				+ "907264K->907262K(907264K), 11.8579830 secs] 1506304K->1202006K(1506304K), "
				+ "[CMS Perm : 92801K->92800K(157352K)], 11.8585290 secs] "
				+ "[Times: user=11.80 sys=0.06, real=11.85 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 6942991, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (1506304 - 907264), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1202006 - 907262), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (1506304 - 907264), event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 907264, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 907262, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 907264, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 92801, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 92800, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 157352, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 11858, event.getDuration());
	}

	public void testLogLineBalancedBracketsWithCmsConcurrentMarkBlock() {
		String logLine = "85238.030: [Full GC 85238.030: [CMS85238.672: "
				+ "[CMS-concurrent-mark: 0.666/0.686 secs] (concurrent mode failure): "
				+ "439328K->439609K(4023936K), 2.7153820 secs] 448884K->439609K(4177280K), "
				+ "[CMS Perm : 262143K->262143K(262144K)], 2.7156150 secs] "
				+ "[Times: user=3.35 sys=0.00, real=2.72 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 85238030, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (448884 - 439328), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (439609 - 439609), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (4177280 - 4023936),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 439328, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 439609, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 4023936, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 262143, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 262143, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 2715, event.getDuration());
	}

	public void testLogLineBalancedBracketsWithCmsConcurrentPrecleanBlock() {
		String logLine = "877412.576: [Full GC 877412.576: [CMS877412.858: [CMS-concurrent-preclean: "
				+ "4.917/4.997 secs] (concurrent mode failure): 1572863K->1547074K(1572864K), "
				+ "10.7226790 secs] 2490334K->1547074K(2490368K), [CMS Perm : 46357K->46354K(77352K)], "
				+ "10.7239680 secs] [Times: user=10.72 sys=0.00, real=10.73 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 877412576, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (2490334 - 1572863), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1547074 - 1547074), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (2490368 - 1572864),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1572863, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1547074, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1572864, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 46357, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 46354, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 77352, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 10723, event.getDuration());
	}

	public void testLogLineBalancedBracketsWithCmsConcurrentAbortedAbortablePrecleanBlock() {
		String logLine = "1361123.817: [Full GC (System) 1361123.818: [CMS CMS: abort preclean due to time "
				+ "1361123.838: [CMS-concurrent-abortable-preclean: 5.030/5.089 secs] (concurrent mode "
				+ "interrupted): 1361457K->1353147K(1572864K), 8.9740110 secs] 1958450K->1353147K(2490368K), "
				+ "[CMS Perm : 46537K->46533K(77608K)], 8.9754050 secs] "
				+ "[Times: user=8.98 sys=0.00, real=8.98 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 1361123817, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (1958450 - 1361457), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1353147 - 1353147), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (2490368 - 1572864),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1361457, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1353147, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1572864, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 46537, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 46533, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 77608, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 8975, event.getDuration());
	}

	/**
	 * JDK6 uses "Full GC (System)" vs. "Full GC" and "interrupted" vs. "failure".
	 */
	public void testLogLineBalancedBracketsWithCmsConcurrentBlockJdk6() {
		String logLine = "827123.748: [Full GC (System) 827123.748: [CMS827125.337: [CMS-concurrent-sweep: "
				+ "2.819/2.862 secs] (concurrent mode interrupted): 1177627K->890349K(1572864K), "
				+ "7.1730360 secs] 2048750K->890349K(2490368K), [CMS Perm : 46357K->46352K(77352K)], "
				+ "7.1743670 secs] [Times: user=7.17 sys=0.00, real=7.18 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 827123748, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (2048750 - 1177627), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (890349 - 890349), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (2490368 - 1572864),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1177627, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 890349, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1572864, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 46357, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 46352, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 77352, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 7174, event.getDuration());
	}

	public void testLogLineBalancedBracketsCmsIncrementalMode() {
		String logLine = "159279.892: [Full GC 159279.892: [CMS159285.305: [CMS-concurrent-preclean: "
				+ "8.152/89.566 secs] (concurrent mode failure): 3180389K->926504K(10387456K), "
				+ "23.1741673 secs] 5187429K->926504K(12394496K), [CMS Perm : 250564K->244739K(524288K)] "
				+ "icms_dc=7 , 23.1745571 secs] [Times: user=23.06 sys=0.04, real=23.17 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 159279892, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (5187429 - 3180389), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (926504 - 926504), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (12394496 - 10387456),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 3180389, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 926504, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 10387456, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 250564, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 244739, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 524288, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 23174, event.getDuration());
	}

	public void testLogLineDetailedCmsEvents() {
		String logLine = "85217.903: [Full GC 85217.903: [CMS85217.919: "
				+ "[CMS-concurrent-abortable-preclean: 0.723/3.756 secs] "
				+ "(concurrent mode failure) (concurrent mode failure)"
				+ "[YG occupancy: 33620K (153344K)]85217.919: [Rescan (parallel) , 0.0116680 secs]"
				+ "85217.931: [weak refs processing, 0.0167100 secs]"
				+ "85217.948: [class unloading, 0.0571300 secs]"
				+ "85218.005: [scrub symbol & string tables, 0.0291210 secs]: "
				+ "423728K->423633K(4023936K), 0.5165330 secs] 457349K->457254K(4177280K), "
				+ "[CMS Perm : 260428K->260406K(262144K)], 0.5167600 secs] "
				+ "[Times: user=0.55 sys=0.01, real=0.52 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 85217903, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (457349 - 423728), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (457254 - 423633), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (4177280 - 4023936),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 423728, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 423633, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 4023936, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 260428, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 260406, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 516, event.getDuration());
	}

	public void testLogLineWithNoConcurrentModeFailure() {
		String logLine = "198.712: [Full GC 198.712: [CMS198.733: [CMS-concurrent-reset: 0.061/1.405 secs]: "
				+ "14037K->31492K(1835008K), 0.7953140 secs] 210074K->31492K(2096960K), "
				+ "[CMS Perm : 27817K->27784K(131072K)], 0.7955670 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 198712, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (210074 - 14037), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (31492 - 31492), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (2096960 - 1835008),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 14037, event
				.getOldOccupancyInit());
		Assert
				.assertEquals("Old end size not parsed correctly.", 31492, event
						.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1835008, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 27817, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 27784, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 795, event.getDuration());
	}

	public void testLogLineWithTimesData() {
		String logLine = "28282.075: [Full GC 28282.075 (concurrent mode failure): "
				+ "1179601K->1179648K(1179648K), 10.7510650 secs] 1441361K->1180553K(1441600K), "
				+ "[CMS Perm : 71172K->71171K(262144K)], 10.7515460 secs]"
				+ " [Times: user=0.29 sys=0.02, real=3.97 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
		CmsSerialOldConcurrentModeFailureEvent event = new CmsSerialOldConcurrentModeFailureEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 28282075, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", (1441361 - 1179601), event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", (1180553 - 1179648), event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", (1441600 - 1179648),
				event.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1179601, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 1179648, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1179648, event
				.getOldSpace());
		Assert.assertEquals("Perm gen begin size not parsed correctly.", 71172, event
				.getPermOccupancyInit());
		Assert.assertEquals("Perm gen end size not parsed correctly.", 71171, event
				.getPermOccupancyEnd());
		Assert.assertEquals("Perm gen allocation size not parsed correctly.", 262144, event
				.getPermSpace());
		Assert.assertEquals("Duration not parsed correctly.", 10751, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "28282.075: [Full GC 28282.075 (concurrent mode failure): "
				+ "1179601K->1179648K(1179648K), 10.7510650 secs] 1441361K->1180553K(1441600K), "
				+ "[CMS Perm : 71172K->71171K(262144K)], 10.7515460 secs]    ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString() + ".",
				CmsSerialOldConcurrentModeFailureEvent.match(logLine));
	}
}
