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
public class TestParNewPromotionFailedCmsSerialOldEvent extends TestCase {

	public void testLogLine() {
		String logLine = "1181.943: [GC 1181.943: [ParNew (promotion failed): "
				+ "145542K->142287K(149120K), 0.1316193 secs]1182.075: "
				+ "[CMS: 6656483K->548489K(8218240K), 9.1244297 secs] "
				+ "6797120K->548489K(8367360K), 9.2564476 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD.toString() + ".",
				ParNewPromotionFailedCmsSerialOldEvent.match(logLine));
		ParNewPromotionFailedCmsSerialOldEvent event = new ParNewPromotionFailedCmsSerialOldEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 1181943, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 140637, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 149120, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 6656483, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 548489, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 8218240, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 9256, event.getDuration());
	}

	public void testLogLineWithTimesData() {
		String logLine = "1181.943: [GC 1181.943: [ParNew (promotion failed): "
				+ "145542K->142287K(149120K), 0.1316193 secs]1182.075: "
				+ "[CMS: 6656483K->548489K(8218240K), 9.1244297 secs] "
				+ "6797120K->548489K(8367360K), 9.2564476 secs] "
				+ "[Times: user=9.52 sys=0.03, real=9.26 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD.toString() + ".",
				ParNewPromotionFailedCmsSerialOldEvent.match(logLine));
		ParNewPromotionFailedCmsSerialOldEvent event = new ParNewPromotionFailedCmsSerialOldEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 1181943, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 140637, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 149120, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 6656483, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 548489, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 8218240, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 9256, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "1181.943: [GC 1181.943: [ParNew (promotion failed): "
				+ "145542K->142287K(149120K), 0.1316193 secs]1182.075: "
				+ "[CMS: 6656483K->548489K(8218240K), 9.1244297 secs] "
				+ "6797120K->548489K(8367360K), 9.2564476 secs]   ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD.toString() + ".",
				ParNewPromotionFailedCmsSerialOldEvent.match(logLine));
	}

	public void testLogLineMissingPromotionFailedMessage() {
		String logLine = "3546.690: [GC 3546.691: [ParNew: 532480K->532480K(599040K), 0.0000400 secs]"
				+ "3546.691: [CMS: 887439K->893801K(907264K), 9.6413020 secs] "
				+ "1419919K->893801K(1506304K), 9.6419180 secs] "
				+ "[Times: user=9.54 sys=0.10, real=9.65 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD.toString() + ".",
				ParNewPromotionFailedCmsSerialOldEvent.match(logLine));
		ParNewPromotionFailedCmsSerialOldEvent event = new ParNewPromotionFailedCmsSerialOldEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 3546690, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 532480, event
				.getYoungOccupancyInit());
		Assert
				.assertEquals("Young end size not parsed correctly.", 0, event
						.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 599040, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 887439, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 893801, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 907264, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 9641, event.getDuration());
	}

	/**
	 * Has "Tenured" instead of "CMS" label in old generation block.
	 */
	public void testLogLineTenuredLabel() {
		String logLine = "289985.117: [GC 289985.117: [ParNew (promotion failed): "
				+ "144192K->144192K(144192K), 0.1347360 secs]289985.252: "
				+ "[Tenured: 1281600K->978341K(1281600K), 3.6577930 secs] "
				+ "1409528K->978341K(1425792K), 3.7930200 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD.toString() + ".",
				ParNewPromotionFailedCmsSerialOldEvent.match(logLine));
		ParNewPromotionFailedCmsSerialOldEvent event = new ParNewPromotionFailedCmsSerialOldEvent(
				logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 289985117, event.getTimestamp());
		Assert.assertEquals("Young begin size not parsed correctly.", 1409528 - 1281600, event
				.getYoungOccupancyInit());
		Assert.assertEquals("Young end size not parsed correctly.", 978341 - 978341, event
				.getYoungOccupancyEnd());
		Assert.assertEquals("Young available size not parsed correctly.", 1425792 - 1281600, event
				.getYoungSpace());
		Assert.assertEquals("Old begin size not parsed correctly.", 1281600, event
				.getOldOccupancyInit());
		Assert.assertEquals("Old end size not parsed correctly.", 978341, event
				.getOldOccupancyEnd());
		Assert.assertEquals("Old allocation size not parsed correctly.", 1281600, event
				.getOldSpace());
		Assert.assertEquals("Duration not parsed correctly.", 3793, event.getDuration());
	}
}
