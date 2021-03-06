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
package org.eclipselabs.garbagecat.util.jdk.unified;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedUtil {

    @Test
    public void testUsingSerialIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_SERIAL);
        assertTrue(JdkUtil.LogEventType.USING_SERIAL.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUsingParallelIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_PARALLEL);
        assertTrue(JdkUtil.LogEventType.USING_PARALLEL.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUsingCmsIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_CMS);
        assertTrue(JdkUtil.LogEventType.USING_CMS.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUsingG1IsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_G1);
        assertTrue(JdkUtil.LogEventType.USING_G1.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUnifiedYoungIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_YOUNG);
        assertTrue(JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUnifiedOldIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_OLD);
        assertTrue(JdkUtil.LogEventType.UNIFIED_OLD.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUnifiedCmsInitialMarkIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CMS_INITIAL_MARK);
        assertTrue(
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUnifiedCmsConcurrentIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CONCURRENT);
        assertTrue(JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUnifiedG1ConcurrentIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CONCURRENT);
        assertTrue(JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUnifiedRemarkIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_REMARK);
        assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    @Test
    public void testUnknownIsNotUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNKNOWN);
        assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " should not be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }
}
