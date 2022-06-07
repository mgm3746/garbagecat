/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestTimesData {

    @Test
    void testTimesData() {
        String timesData = " [Times: user=0.44 sys=0.00, real=0.08 secs]";
        assertTrue(timesData.matches(TimesData.REGEX), "'" + timesData + "' is a valid duration.");
    }

    @Test
    void testTimesDataJdk9() {
        String timesData = " User=0.00s Sys=0.00s Real=0.00s";
        assertTrue(timesData.matches(TimesData.REGEX_JDK9), "'" + timesData + "' is a valid duration.");
    }
}
