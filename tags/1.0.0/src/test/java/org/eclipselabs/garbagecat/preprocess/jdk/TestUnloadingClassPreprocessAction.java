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
public class TestUnloadingClassPreprocessAction extends TestCase {

	public void testLogLine() {
		String logLine = "1187039.034: [Full GC[Unloading class sun.reflect.GeneratedSerializationConstructorAccessor13565]";
		String nextLogLine = null;
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.UNLOADING_CLASS.toString() + ".",
				UnloadingClassPreprocessAction.match(logLine));
		UnloadingClassPreprocessAction event = new UnloadingClassPreprocessAction(logLine,
				nextLogLine);
		Assert.assertEquals("Log line not parsed correctly.", "1187039.034: [Full GC"
				+ System.getProperty("line.separator"), event.getLogEntry());
	}

	public void testLogLineWithUnderline() {
		String logLine = "33872.769: [Full GC[Unloading class CargoClaimCoverLetter_1234153487841_717989]";
		String nextLogLine = null;
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.UNLOADING_CLASS.toString() + ".",
				UnloadingClassPreprocessAction.match(logLine));
		UnloadingClassPreprocessAction event = new UnloadingClassPreprocessAction(logLine,
				nextLogLine);
		Assert.assertEquals("Log line not parsed correctly.", "33872.769: [Full GC"
				+ System.getProperty("line.separator"), event.getLogEntry());
	}

}
