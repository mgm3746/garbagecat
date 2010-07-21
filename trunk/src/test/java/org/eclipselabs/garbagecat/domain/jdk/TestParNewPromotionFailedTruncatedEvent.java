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
public class TestParNewPromotionFailedTruncatedEvent extends TestCase {

	public void testLogLine() {
		String logLine = "5881.424: [GC 5881.424: [ParNew (promotion failed): 153272K->152257K(153344K), "
				+ "0.2143850 secs]5881.639: [CMS";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_TRUNCATED.toString() + ".",
				ParNewPromotionFailedTruncatedEvent.match(logLine));
		ParNewPromotionFailedTruncatedEvent event = new ParNewPromotionFailedTruncatedEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 5881424, event.getTimestamp());
		Assert.assertEquals("Duration not parsed correctly.", 214, event.getDuration());
	}

	public void testLogLineWithSpaces() {
		String logLine = "5881.424: [GC 5881.424: [ParNew (promotion failed): 153272K->152257K(153344K), "
				+ "0.2143850 secs]5881.639: [CMS     ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_TRUNCATED.toString() + ".",
				ParNewPromotionFailedTruncatedEvent.match(logLine));
		ParNewPromotionFailedTruncatedEvent event = new ParNewPromotionFailedTruncatedEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 5881424, event.getTimestamp());
		Assert.assertEquals("Duration not parsed correctly.", 214, event.getDuration());
	}

	public void testLogLineWithCmsConcurrentEvent() {
		String logLine = "36455.096: [GC 36455.096: [ParNew (promotion failed): 153344K->153344K(153344K), "
				+ "0.6818450 secs]36455.778: [CMS36459.090: [CMS-concurrent-mark: 3.439/4.155 secs] "
				+ "[Times: user=8.27 sys=0.17, real=4.16 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_TRUNCATED.toString() + ".",
				ParNewPromotionFailedTruncatedEvent.match(logLine));
		ParNewPromotionFailedTruncatedEvent event = new ParNewPromotionFailedTruncatedEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 36455096, event.getTimestamp());
		Assert.assertEquals("Duration not parsed correctly.", 681, event.getDuration());
	}
}
