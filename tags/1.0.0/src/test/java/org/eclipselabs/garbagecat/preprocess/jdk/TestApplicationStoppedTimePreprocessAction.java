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
public class TestApplicationStoppedTimePreprocessAction extends TestCase {

	public void testLine2CmsConcurrent() {
		String priorLogLine = "6545.692Total time for which application threads were stopped: 0.0007993 seconds";
		String logLine = ": [CMS-concurrent-abortable-preclean: 0.025/0.042 secs] [Times: user=0.04 sys=0.00, real=0.04 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".",
				ApplicationStoppedTimePreprocessAction.match(logLine, priorLogLine));
	}
}
