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
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestHeaderMemoryEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "Memory: 4k page, physical 65806300k(58281908k free), swap 16777212k(16777212k free)";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)), JdkUtil.LogEventType.HEADER_MEMORY.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    public void testLine() {
        String logLine = "Memory: 4k page, physical 65806300k(58281908k free), swap 16777212k(16777212k free)";
        assertTrue(HeaderMemoryEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.HEADER_MEMORY.toString() + ".");
        HeaderMemoryEvent event = new HeaderMemoryEvent(logLine);
        assertEquals((long) 0,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(65806300,event.getPhysicalMemory(),"Physical memory not parsed correctly.");
        assertEquals(58281908,event.getPhysicalMemoryFree(),"Physical memory free not parsed correctly.");
        assertEquals(16777212,event.getSwap(),"Swap not parsed correctly.");
        assertEquals(16777212,event.getSwapFree(),"Swap free not parsed correctly.");

    }

    @Test
    public void testLineMemoryPage8kNoSwapData() {
        String logLine = "Memory: 8k page, physical 535035904k(398522432k free)";
        assertTrue(HeaderMemoryEvent.match(logLine), "Log line not recognized as " + JdkUtil.LogEventType.HEADER_MEMORY.toString() + ".");
        HeaderMemoryEvent event = new HeaderMemoryEvent(logLine);
        assertEquals((long) 0,event.getTimestamp(),"Time stamp not parsed correctly.");
        assertEquals(535035904,event.getPhysicalMemory(),"Physical memory not parsed correctly.");
        assertEquals(398522432,event.getPhysicalMemoryFree(),"Physical memory free not parsed correctly.");
        assertEquals(0,event.getSwap(),"Swap not parsed correctly.");
        assertEquals(0,event.getSwapFree(),"Swap free not parsed correctly.");
    }

}
