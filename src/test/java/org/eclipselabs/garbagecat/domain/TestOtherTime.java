/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
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
class TestOtherTime {

    @Test
    void testJdk8() {
        String otherTime = "[Other: 0.9 ms]";
        assertTrue(otherTime.matches(OtherTime.REGEX),
                "'" + otherTime + "' not identified as a valid 'other' time block.");
    }

    @Test
    void testUnified() {
        String otherTime = "Other: 9569.7ms";
        assertTrue(otherTime.matches(OtherTime.REGEX),
                "'" + otherTime + "' not identified as a valid 'other' time block.");
    }
}
