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
public class TestParNewPromotionFailedEvent extends TestCase {

	public void testLogLine() {
		String logLine = "144501.626: [GC 144501.627: [ParNew (promotion failed): "
				+ "680066K->680066K(707840K), 3.7067346 secs] 1971073K->1981370K(2018560K), 3.7084059 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED.toString() + ".",
				ParNewPromotionFailedEvent.match(logLine));
		ParNewPromotionFailedEvent event = new ParNewPromotionFailedEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 144501626, event.getTimestamp());
		Assert.assertEquals("Duration not parsed correctly.", 3708, event.getDuration());
	}

	public void testIncrementalModeLogLine() {
		String logLine = "159275.552: [GC 159275.552: [ParNew (promotion failed): 2007040K->2007040K(2007040K), "
				+ "4.3393411 secs] 5167424K->5187429K(12394496K) icms_dc=7 , 4.3398519 secs] "
				+ "[Times: user=4.96 sys=1.91, real=4.34 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED.toString() + ".",
				ParNewPromotionFailedEvent.match(logLine));
		ParNewPromotionFailedEvent event = new ParNewPromotionFailedEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 159275552, event.getTimestamp());
		Assert.assertEquals("Duration not parsed correctly.", 4339, event.getDuration());
	}

	public void testLogLineWithTimesData() {
		String logLine = "144501.626: [GC 144501.627: [ParNew (promotion failed): "
				+ "680066K->680066K(707840K), 3.7067346 secs] 1971073K->1981370K(2018560K), 3.7084059 secs]"
				+ " [Times: user=0.29 sys=0.02, real=3.97 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED.toString() + ".",
				ParNewPromotionFailedEvent.match(logLine));
		ParNewPromotionFailedEvent event = new ParNewPromotionFailedEvent(logLine);
		Assert.assertEquals("Time stamp not parsed correctly.", 144501626, event.getTimestamp());
		Assert.assertEquals("Duration not parsed correctly.", 3708, event.getDuration());
	}

	public void testLogLineWhitespaceAtEnd() {
		String logLine = "144501.626: [GC 144501.627: [ParNew (promotion failed): "
				+ "680066K->680066K(707840K), 3.7067346 secs] 1971073K->1981370K(2018560K), "
				+ "3.7084059 secs]   ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED.toString() + ".",
				ParNewPromotionFailedEvent.match(logLine));
	}
}
