/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestTimesData {

    @Test
    public void testTimesData() {
        String timesData = " [Times: user=0.44 sys=0.00, real=0.08 secs]";
        assertTrue("'" + timesData + "' is a valid duration.", timesData.matches(TimesData.REGEX));
    }

    @Test
    public void testTimesDataJdk9() {
        String timesData = " User=0.00s Sys=0.00s Real=0.00s";
        assertTrue("'" + timesData + "' is a valid duration.", timesData.matches(TimesData.REGEX_JDK9));
    }
}
