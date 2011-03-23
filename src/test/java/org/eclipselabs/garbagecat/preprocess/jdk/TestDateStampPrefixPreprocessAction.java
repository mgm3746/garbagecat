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
package org.eclipselabs.garbagecat.preprocess.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestDateStampPrefixPreprocessAction extends TestCase {

	public void testLogLine() {
		String logLine = "2010-04-16T12:11:18.979+0200: 84.335: [GC 84.336: [ParNew: 273152K->858K(341376K), "
				+ "0.0030008 secs] 273152K->858K(980352K), 0.0031183 secs] "
				+ "[Times: user=0.00 sys=0.00, real=0.00 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.DATE_STAMP.toString() + ".",
				DateStampPrefixPreprocessAction.match(logLine));
		DateStampPrefixPreprocessAction preprocessAction = new DateStampPrefixPreprocessAction(
				logLine);
		String preprocessedLogLine = "84.335: [GC 84.336: [ParNew: 273152K->858K(341376K), 0.0030008 secs] "
				+ "273152K->858K(980352K), 0.0031183 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]";
		Assert.assertEquals("Log line not parsed correctly.",
				preprocessedLogLine, preprocessAction.getLogEntry());
	}
}
