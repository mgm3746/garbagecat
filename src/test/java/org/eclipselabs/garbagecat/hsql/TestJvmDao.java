/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.hsql;

import java.util.List;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.jdk.ParNewEvent;
import org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJvmDao extends TestCase {

    public void testSameTimestampOrdering() {
        JvmDao jvmDao = new JvmDao();
        ParNewEvent event1 = new ParNewEvent("3010778.296: [GC 3010778.296: [ParNew: 337824K->32173K(368640K),"
                + " 0.0803880 secs] 806117K->500466K(1187840K), 0.0805980 secs]");
        jvmDao.addBlockingEvent(event1);
        ParNewEvent event2 = new ParNewEvent(
                "3010786.012: [GC 3010786.012: [ParNew: 356703K->356703K(368640K), 0.0000190 secs]"
                        + " 824995K->824995K(1187840K), 0.0001460 secs]");
        jvmDao.addBlockingEvent(event2);
        SerialOldEvent event3 = new SerialOldEvent("3010786.012: [Full GC 3010786.012:"
                + " [Tenured: 468292K->482213K(819200K), 1.9920590 secs] 824995K->482213K(1187840K),"
                + " [Perm : 123092K->122684K(262144K)], 1.9924510 secs]");
        jvmDao.addBlockingEvent(event3);
        jvmDao.processBlockingBatch();

        // check they are the correct way around
        List<BlockingEvent> events = jvmDao.getBlockingEvents();
        Assert.assertTrue(events.get(1) instanceof ParNewEvent);
        Assert.assertTrue(events.get(2) instanceof SerialOldEvent);
    }
}
