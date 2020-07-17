/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk.unified;

import java.util.ArrayList;
import java.util.List;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestUnifiedUtil extends TestCase {

    public void testUsingSerialIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_SERIAL);
        Assert.assertTrue(JdkUtil.LogEventType.USING_SERIAL.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUsingParallelIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_PARALLEL);
        Assert.assertTrue(JdkUtil.LogEventType.USING_PARALLEL.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUsingCmsIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_CMS);
        Assert.assertTrue(JdkUtil.LogEventType.USING_CMS.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUsingG1IsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.USING_G1);
        Assert.assertTrue(JdkUtil.LogEventType.USING_G1.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUnifiedYoungIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_YOUNG);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_YOUNG.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUnifiedOldIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_OLD);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_OLD.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUnifiedCmsInitialMarkIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CMS_INITIAL_MARK);
        Assert.assertTrue(
                JdkUtil.LogEventType.UNIFIED_CMS_INITIAL_MARK.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUnifiedCmsConcurrentIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CONCURRENT);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUnifiedG1ConcurrentIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_CONCURRENT);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_CONCURRENT.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUnifiedRemarkIsUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNIFIED_REMARK);
        Assert.assertTrue(JdkUtil.LogEventType.UNIFIED_REMARK.toString() + " should be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }

    public void testUnknownIsNotUnifiedLogging() {
        List<LogEventType> eventTypes = new ArrayList<LogEventType>();
        eventTypes.add(LogEventType.UNKNOWN);
        Assert.assertFalse(JdkUtil.LogEventType.UNKNOWN.toString() + " should not be identified as unified.",
                UnifiedUtil.isUnifiedLogging(eventTypes));
    }
}
