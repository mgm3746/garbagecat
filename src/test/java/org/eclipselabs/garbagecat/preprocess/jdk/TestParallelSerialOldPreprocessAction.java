/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk;

import java.util.HashSet;
import java.util.Set;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParallelSerialOldPreprocessAction extends TestCase {

    public void testLogLineClassUnloading() {
        String logLine = "1187039.034: [Full GC"
                + "[Unloading class sun.reflect.GeneratedSerializationConstructorAccessor13565]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldPreprocessAction.match(logLine));
        ParallelSerialOldPreprocessAction event = new ParallelSerialOldPreprocessAction(logLine, nextLogLine, context);
        Assert.assertEquals("Log line not parsed correctly.", "1187039.034: [Full GC", event.getLogEntry());
    }

    public void testLogLineEnd() {
        String logLine = " [PSYoungGen: 32064K->0K(819840K)] [PSOldGen: 355405K->387085K(699072K)] "
                + "387470K->387085K(1518912K) [PSPermGen: 115215K->115215K(238912K)], 1.5692400 secs]";
        String nextLogLine = null;
        Set<String> context = new HashSet<String>();
        Assert.assertTrue(
                "Log line not recognized as " + JdkUtil.PreprocessActionType.PARALLEL_SERIAL_OLD.toString() + ".",
                ParallelSerialOldPreprocessAction.match(logLine));
        ParallelSerialOldPreprocessAction event = new ParallelSerialOldPreprocessAction(logLine, nextLogLine, context);
        Assert.assertEquals("Log line not parsed correctly.", logLine, event.getLogEntry());
    }
}
