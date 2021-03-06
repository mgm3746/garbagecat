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
public class TestReferenceGcEvent {

    @Test
    public void testNotBlocking() {
        String logLine = "0.341: [GC (Allocation Failure) 0.344: [SoftReference, 0 refs, 0.0000327 secs]0.344: "
                + "[WeakReference, 19 refs, 0.0000049 secs]0.344: [FinalReference, 296 refs, 0.0002385 secs]0.344: "
                + "[PhantomReference, 0 refs, 0 refs, 0.0000033 secs]0.344: [JNI Weak Reference, 0.0000041 secs]"
                + "[PSYoungGen: 63488K->3151K(73728K)] 63488K->3159K(241664K), 0.0032820 secs] "
                + "[Times: user=0.02 sys=0.00, real=0.00 secs]";
        assertFalse(JdkUtil.LogEventType.REFERENCE_GC.toString() + " incorrectly indentified as blocking.",
                JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testReportable() {
        String logLine = "0.341: [GC (Allocation Failure) 0.344: [SoftReference, 0 refs, 0.0000327 secs]0.344: "
                + "[WeakReference, 19 refs, 0.0000049 secs]0.344: [FinalReference, 296 refs, 0.0002385 secs]0.344: "
                + "[PhantomReference, 0 refs, 0 refs, 0.0000033 secs]0.344: [JNI Weak Reference, 0.0000041 secs]"
                + "[PSYoungGen: 63488K->3151K(73728K)] 63488K->3159K(241664K), 0.0032820 secs] "
                + "[Times: user=0.02 sys=0.00, real=0.00 secs]";
        assertFalse(JdkUtil.LogEventType.REFERENCE_GC.toString() + " incorrectly indentified as reportable.",
                JdkUtil.isReportable(JdkUtil.identifyEventType(logLine)));
    }

    @Test
    public void testLogLineParallelScavenge() {
        String logLine = "0.341: [GC (Allocation Failure) 0.344: [SoftReference, 0 refs, 0.0000327 secs]0.344: "
                + "[WeakReference, 19 refs, 0.0000049 secs]0.344: [FinalReference, 296 refs, 0.0002385 secs]0.344: "
                + "[PhantomReference, 0 refs, 0 refs, 0.0000033 secs]0.344: [JNI Weak Reference, 0.0000041 secs]"
                + "[PSYoungGen: 63488K->3151K(73728K)] 63488K->3159K(241664K), 0.0032820 secs] "
                + "[Times: user=0.02 sys=0.00, real=0.00 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.REFERENCE_GC.toString() + ".",
                ReferenceGcEvent.match(logLine));
        ReferenceGcEvent event = new ReferenceGcEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 341, event.getTimestamp());
    }

    @Test
    public void testLogLineConcurrent() {
        String logLine = "6.698: [Preclean SoftReferences, 0.0000060 secs]6.698: [Preclean WeakReferences, "
                + "0.0000045 secs]6.698: [Preclean FinalReferences, 0.0000025 secs]6.698: "
                + "[Preclean PhantomReferences, 0.0000026 secs]2015-12-12T08:59:10.539+0000: 6.717: "
                + "[CMS-concurrent-preclean: 0.019/0.019 secs] [Times: user=0.02 sys=0.00, real=0.02 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.REFERENCE_GC.toString() + ".",
                ReferenceGcEvent.match(logLine));
        ReferenceGcEvent event = new ReferenceGcEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 6698, event.getTimestamp());
    }

    @Test
    public void testLogLineDatestamps() {
        String logLine = "2017-04-05T09:07:18.552-0500: 201524.276: [SoftReference, 0 refs, 0.0002257 secs]"
                + "2017-04-05T09:07:18.552-0500: 201524.277: [WeakReference, 48 refs, 0.0001397 secs]"
                + "2017-04-05T09:07:18.552-0500: 201524.277: [FinalReference, 2813 refs, 0.0026465 secs]"
                + "2017-04-05T09:07:18.555-0500: 201524.279: [PhantomReference, 13 refs, 18 refs, 0.0002374 secs]"
                + "2017-04-05T09:07:18.555-0500: 201524.280: [JNI Weak Reference, 0.0000167 secs], 0.0319874 secs]";
        assertTrue("Log line not recognized as " + JdkUtil.LogEventType.REFERENCE_GC.toString() + ".",
                ReferenceGcEvent.match(logLine));
        ReferenceGcEvent event = new ReferenceGcEvent(logLine);
        assertEquals("Time stamp not parsed correctly.", 201524276, event.getTimestamp());
    }
}
