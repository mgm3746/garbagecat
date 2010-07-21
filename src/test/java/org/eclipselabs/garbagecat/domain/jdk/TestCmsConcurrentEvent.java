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
public class TestCmsConcurrentEvent extends TestCase {

	public void testMarkStart() {
		String logLine = "251.781: [CMS-concurrent-mark-start]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testMark() {
		String logLine = "252.707: [CMS-concurrent-mark: 0.796/0.926 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testPrecleanStart() {
		String logLine = "252.707: [CMS-concurrent-preclean-start]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testPreclean() {
		String logLine = "252.888: [CMS-concurrent-preclean: 0.141/0.182 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testAbortablePrecleanStart() {
		String logLine = "252.889: [CMS-concurrent-abortable-preclean-start]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testAbortablePreclean() {
		String logLine = "253.102: [CMS-concurrent-abortable-preclean: 0.083/0.214 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testAbortPrecleanDueToTime() {
		String logLine = " CMS: abort preclean due to time 32633.935: "
				+ "[CMS-concurrent-abortable-preclean: 0.622/5.054 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testSweepStart() {
		String logLine = "253.189: [CMS-concurrent-sweep-start]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testSweep() {
		String logLine = "258.265: [CMS-concurrent-sweep: 4.134/5.076 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testResetStart() {
		String logLine = "258.265: [CMS-concurrent-reset-start]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testReset() {
		String logLine = "258.344: [CMS-concurrent-reset: 0.079/0.079 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}

	public void testPrecleanConcurrentModeFailure() {
		String logLine = "253.102: [CMS-concurrent-abortable-preclean: 0.083/0.214 secs] "
				+ "[Times: user=1.23 sys=0.02, real=0.21 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.LogEventType.CMS_CONCURRENT.toString() + ".", CmsConcurrentEvent
				.match(logLine));
	}
}
