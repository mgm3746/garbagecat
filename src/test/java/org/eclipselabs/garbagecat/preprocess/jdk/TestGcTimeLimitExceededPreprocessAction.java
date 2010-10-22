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
public class TestGcTimeLimitExceededPreprocessAction extends TestCase {

	public void testPsOldGenWouldExceedLine() {
		String priorLogLine = "";
		String logLine = "3743.645: [Full GC [PSYoungGen: 419840K->415020K(839680K)] [PSOldGen: "
				+ "5008922K->5008922K(5033984K)] 5428762K->5423942K(5873664K) [PSPermGen: "
				+ "193275K->193275K(262144K)]      GC time would exceed GCTimeLimit of 98%";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.GC_TIME_LIMIT_EXCEEDED.toString() + ".",
				GcTimeLimitExceededPreprocessAction.match(logLine, priorLogLine));
	}

	public void testPsOldGenIsExceedingLine() {
		String priorLogLine = "";
		String logLine = "3924.453: [Full GC [PSYoungGen: 419840K->418436K(839680K)] [PSOldGen: "
				+ "5008601K->5008601K(5033984K)] 5428441K->5427038K(5873664K) [PSPermGen: "
				+ "193278K->193278K(262144K)]      GC time is exceeding GCTimeLimit of 98%";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.GC_TIME_LIMIT_EXCEEDED.toString() + ".",
				GcTimeLimitExceededPreprocessAction.match(logLine, priorLogLine));
	}

	public void testLine2() {
		String priorLogLine = "3743.645: [Full GC [PSYoungGen: 419840K->415020K(839680K)] [PSOldGen: "
				+ "5008922K->5008922K(5033984K)] 5428762K->5423942K(5873664K) [PSPermGen: "
				+ "193275K->193275K(262144K)]      GC time would exceed GCTimeLimit of 98%";
		String logLine = ", 33.6887649 secs] [Times: user=33.68 sys=0.02, real=33.69 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.GC_TIME_LIMIT_EXCEEDED.toString() + ".",
				GcTimeLimitExceededPreprocessAction.match(logLine, priorLogLine));
	}

	public void testParOldGenIsExceedingMoreSpacesLine() {
		String priorLogLine = "";
		String logLine = "52843.722: [Full GC [PSYoungGen: 109696K->95191K(184960K)] [ParOldGen: "
				+ "1307240K->1307182K(1310720K)] 1416936K->1402374K(1495680K) [PSPermGen: "
				+ "113631K->113623K(196608K)]	GC time is exceeding GCTimeLimit of 98%";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.GC_TIME_LIMIT_EXCEEDED.toString() + ".",
				GcTimeLimitExceededPreprocessAction.match(logLine, priorLogLine));
	}
}
