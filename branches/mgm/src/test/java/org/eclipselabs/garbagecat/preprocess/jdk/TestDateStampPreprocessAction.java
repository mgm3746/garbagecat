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

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestDateStampPreprocessAction extends TestCase {

    public void testLogLine() {
        String logLine = "2010-02-26T09:32:12.486-0600: [GC [ParNew: 150784K->3817K(169600K), 0.0328800 secs]"
                + " 150784K->3817K(1029760K), 0.0329790 secs] [Times: user=0.00 sys=0.00, real=0.03 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.PreprocessActionType.DATE_STAMP.toString() + ".", DateStampPreprocessAction.match(logLine));
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date jvmStartDate = calendar.getTime();
        DateStampPreprocessAction preprocessAction = new DateStampPreprocessAction(logLine, jvmStartDate);
        String preprocessedLogLine = "34332.486: [GC [ParNew: 150784K->3817K(169600K), 0.0328800 secs]" + " 150784K->3817K(1029760K), 0.0329790 secs] [Times: user=0.00 sys=0.00, real=0.03 secs]";
        Assert.assertEquals("Log line not parsed correctly.", preprocessedLogLine, preprocessAction.getLogEntry());
    }
}
