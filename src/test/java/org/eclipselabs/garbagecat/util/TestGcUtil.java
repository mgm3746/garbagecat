/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.domain.jdk.SerialNewEvent;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestGcUtil {

    @Test
    void testPartialLogDatestamp() {
        String logLine = "2016-11-22T09:07:01.358+0100: [GC 2016-11-22T09:07:01.358+0100: "
                + "[DefNew: 37172K->3631K(39296K), 0.0209300 secs] 41677K->10314K(126720K), 0.0210210 secs]";
        SerialNewEvent firstEvent = new SerialNewEvent(logLine);
        assertTrue(GcUtil.isPartialLog(firstEvent.getTimestamp()), "Not identified as partial logging.");
    }

    @Test
    void testPartialLogTimestamp() {
        String logLine = "7.798: [GC 7.798: [DefNew: 37172K->3631K(39296K), 0.0209300 secs] 41677K->10314K(126720K), "
                + "0.0210210 secs]";
        SerialNewEvent firstEvent = new SerialNewEvent(logLine);
        assertFalse(GcUtil.isPartialLog(firstEvent.getTimestamp()), "Incorrectly identified as partial logging.");
    }
}