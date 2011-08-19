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
package org.eclipselabs.garbagecat.domain.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestParNewPromotionFailedCmsSerialOldPermDataEvent extends TestCase {

    public void testLogLine() {
        String logLine = "395950.370: [GC 395950.370: [ParNew (promotion failed): " + "53094K->53606K(59008K), 0.0510880 secs]395950.421: "
                + "[CMS: 664527K->317110K(1507328K), 2.9523520 secs] 697709K->317110K(1566336K), " + "[CMS Perm : 83780K->83711K(131072K)], 3.0039040 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA.toString() + ".", ParNewPromotionFailedCmsSerialOldPermDataEvent
                .match(logLine));
        ParNewPromotionFailedCmsSerialOldPermDataEvent event = new ParNewPromotionFailedCmsSerialOldPermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 395950370, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (697709 - 664527), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (317110 - 317110), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (1566336 - 1507328), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 664527, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 317110, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1507328, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 83780, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 83711, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3003, event.getDuration());
    }

    public void testLogLineIncrementalMode() {
        String logLine = "4595.651: [GC 4595.651: [ParNew (promotion failed): 1304576K->1304576K(1304576K), " + "1.7740754 secs]4597.425: [CMS: 967034K->684015K(4886528K), 3.2678588 secs] "
                + "2022731K->684015K(6191104K), [CMS Perm : 201541K->201494K(524288K)] icms_dc=21 , " + "5.0421688 secs] [Times: user=5.54 sys=0.01, real=5.04 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA.toString() + ".", ParNewPromotionFailedCmsSerialOldPermDataEvent
                .match(logLine));
        ParNewPromotionFailedCmsSerialOldPermDataEvent event = new ParNewPromotionFailedCmsSerialOldPermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 4595651, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (2022731 - 967034), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (684015 - 684015), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (6191104 - 4886528), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 967034, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 684015, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 4886528, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 201541, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 201494, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 524288, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 5042, event.getDuration());
    }

    public void testLogLineWithTimesData() {
        String logLine = "395950.370: [GC 395950.370: [ParNew (promotion failed): " + "53094K->53606K(59008K), 0.0510880 secs]395950.421: "
                + "[CMS: 664527K->317110K(1507328K), 2.9523520 secs] 697709K->317110K(1566336K), " + "[CMS Perm : 83780K->83711K(131072K)], 3.0039040 secs] "
                + "[Times: user=3.03 sys=0.00, real=3.01 secs]";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA.toString() + ".", ParNewPromotionFailedCmsSerialOldPermDataEvent
                .match(logLine));
        ParNewPromotionFailedCmsSerialOldPermDataEvent event = new ParNewPromotionFailedCmsSerialOldPermDataEvent(logLine);
        Assert.assertEquals("Time stamp not parsed correctly.", 395950370, event.getTimestamp());
        Assert.assertEquals("Young begin size not parsed correctly.", (697709 - 664527), event.getYoungOccupancyInit());
        Assert.assertEquals("Young end size not parsed correctly.", (317110 - 317110), event.getYoungOccupancyEnd());
        Assert.assertEquals("Young available size not parsed correctly.", (1566336 - 1507328), event.getYoungSpace());
        Assert.assertEquals("Old begin size not parsed correctly.", 664527, event.getOldOccupancyInit());
        Assert.assertEquals("Old end size not parsed correctly.", 317110, event.getOldOccupancyEnd());
        Assert.assertEquals("Old allocation size not parsed correctly.", 1507328, event.getOldSpace());
        Assert.assertEquals("Perm gen begin size not parsed correctly.", 83780, event.getPermOccupancyInit());
        Assert.assertEquals("Perm gen end size not parsed correctly.", 83711, event.getPermOccupancyEnd());
        Assert.assertEquals("Perm gen allocation size not parsed correctly.", 131072, event.getPermSpace());
        Assert.assertEquals("Duration not parsed correctly.", 3003, event.getDuration());
    }

    public void testLogLineWhitespaceAtEnd() {
        String logLine = "395950.370: [GC 395950.370: [ParNew (promotion failed): " + "53094K->53606K(59008K), 0.0510880 secs]395950.421: "
                + "[CMS: 664527K->317110K(1507328K), 2.9523520 secs] 697709K->317110K(1566336K), " + "[CMS Perm : 83780K->83711K(131072K)], 3.0039040 secs] ";
        Assert.assertTrue("Log line not recognized as " + JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA.toString() + ".", ParNewPromotionFailedCmsSerialOldPermDataEvent
                .match(logLine));
    }
}
