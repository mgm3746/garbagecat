/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain;

import org.junit.Assert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestTimesData extends TestCase {

    public void testTimesData() {
        String timesData = " [Times: user=0.44 sys=0.00, real=0.08 secs]";
        Assert.assertTrue("'" + timesData + "' is a valid duration.", timesData.matches(TimesData.REGEX));
    }

    public void testTimesDataJdk9() {
        String timesData = " User=0.00s Sys=0.00s Real=0.00s";
        Assert.assertTrue("'" + timesData + "' is a valid duration.", timesData.matches(TimesData.REGEX_JDK9));
    }
}
