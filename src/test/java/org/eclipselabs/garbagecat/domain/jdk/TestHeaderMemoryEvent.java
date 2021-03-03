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
package org.eclipselabs.garbagecat.domain.jdk;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestHeaderMemoryEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "Memory: 4k page, physical 65806300k(58281908k free), swap 16777212k(16777212k free)";
        assertFalse(JdkUtil.LogEventType.HEADER_MEMORY.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLine() {
        String logLine = "Memory: 4k page, physical 65806300k(58281908k free), swap 16777212k(16777212k free)";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEADER_MEMORY.toString() + ".",
                HeaderMemoryEvent.match(logLine));
        HeaderMemoryEvent event = new HeaderMemoryEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 0, event.getTimestamp());
        assertEquals("Physical memory not parsed correctly.", 65806300, event.getPhysicalMemory());
        assertEquals("Physical memory free not parsed correctly.", 58281908, event.getPhysicalMemoryFree());
        assertEquals("Swap not parsed correctly.", 16777212, event.getSwap());
        assertEquals("Swap free not parsed correctly.", 16777212, event.getSwapFree());

    }

    @Test
    public void testLineMemoryPage8kNoSwapData() {
        String logLine = "Memory: 8k page, physical 535035904k(398522432k free)";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.HEADER_MEMORY.toString() + ".",
                HeaderMemoryEvent.match(logLine));
        HeaderMemoryEvent event = new HeaderMemoryEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 0, event.getTimestamp());
        assertEquals("Physical memory not parsed correctly.", 535035904, event.getPhysicalMemory());
        assertEquals("Physical memory free not parsed correctly.", 398522432, event.getPhysicalMemoryFree());
        assertEquals("Swap not parsed correctly.", 0, event.getSwap());
        assertEquals("Swap free not parsed correctly.", 0, event.getSwapFree());
    }

}
