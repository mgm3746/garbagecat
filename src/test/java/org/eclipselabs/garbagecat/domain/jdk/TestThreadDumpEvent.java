/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
class TestThreadDumpEvent {

    @Test
    void testBeginningDateTime() {
        String logLine = "2009-12-29 14:17:17";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testInfinispanAt() {
        String logLine = "    at jdk.internal.misc.Unsafe.park(Unsafe.java:-2)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testInfinispanThreadName() {
        String logLine = "main:";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testInfinispanThreadNameComplex() {
        String logLine = "Connection.Receiver "
                + "[254.14.9.208:7800 - 254.12.11.128:39223]-448,rhdg-cluster-w-prod-3-27192:";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testJavaThreadList() {
        String logLine = "_java_thread_list=0x0000562dca51dbd0, length=361, elements={";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testJavaThreadListAddresses() {
        String logLine = "0x0000562dbd1f2800, 0x0000562dbe5d5000, 0x0000562dbe5d7800, 0x0000562dbe5e5800,";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testJniGlobalRefs() {
        String logLine = "JNI global refs: 121703, weak refs: 0";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testNoCompileTask() {
        String logLine = "   No compile task";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testNotBlocking() {
        String logLine = "Full thread dump Java HotSpot(TM) Server VM (11.0-b16 mixed mode):";
        assertFalse(JdkUtil.isBlocking(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.THREAD_DUMP.toString() + " incorrectly indentified as blocking.");
    }

    @Test
    void testReportable() {
        String logLine = "Full thread dump Java HotSpot(TM) Server VM (11.0-b16 mixed mode):";
        assertTrue(JdkUtil.isReportable(JdkUtil.identifyEventType(logLine, null)),
                JdkUtil.LogEventType.THREAD_DUMP.toString() + " incorrectly indentified as not reportable.");
    }

    @Test
    void testStackTraceEventLocked() {
        String logLine = "\t- locked <0x94fa4fb0> (a org.apache.tomcat.util.net.JIoEndpoint$Worker)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testStackTraceEventParkingToWaitFor() {
        String logLine = "\t- parking to wait for  <0x000000041d61b760> "
                + "(a java.util.concurrent.CompletableFuture$Signaller)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testStackTraceEventWaitingOn() {
        String logLine = "\t- waiting on <0x889d3168> (a java.lang.Object)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testStackTraceEventWaitingToLock() {
        String logLine = "\t- waiting to lock <0x4b2da3f0> (a com.voxmobili.impl.pim.ab.BAddressBookContext)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testStackTraceEventWaitingToReLock() {
        String logLine = "\t- waiting to re-lock in wait() <0x0000000419cd45b0> (a java.lang.ref.ReferenceQueue$Lock)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testStackTraceLocation() {
        String logLine = "\tat java.lang.Object.wait(Native Method)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testSummaryCmsGenerationTotal() {
        String logLine = " concurrent mark-sweep generation total 1572864K, used 1572863K "
                + "[0x84c40000, 0xe4c40000, 0xe4c40000)";
        assertFalse(ThreadDumpEvent.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testSummaryCmsPermGenTotal() {
        String logLine = " concurrent-mark-sweep perm gen total 77736K, used 46547K "
                + "[0xe4c40000, 0xe982a000, 0xf4c40000)";
        assertFalse(ThreadDumpEvent.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testSummaryEdenSpace() {
        String logLine = "  eden space 786432K, 100% used [0x44c40000, 0x74c40000, 0x74c40000)";
        assertFalse(ThreadDumpEvent.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testSummaryFromSpace() {
        String logLine = "  from space 131072K,  17% used [0x7cc40000, 0x7e20e790, 0x84c40000)";
        assertFalse(ThreadDumpEvent.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testSummaryHeap() {
        String logLine = "Heap";
        assertFalse(ThreadDumpEvent.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testSummaryJni() {
        String logLine = "JNI global references: 844";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testSummaryParNewGeneration() {
        String logLine = " par new generation   total 917504K, used 808761K [0x44c40000, 0x84c40000, 0x84c40000)";
        assertFalse(ThreadDumpEvent.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testSummaryToSpace() {
        String logLine = "  to   space 131072K,   0% used [0x74c40000, 0x74c40000, 0x7cc40000)";
        assertFalse(ThreadDumpEvent.match(logLine),
                "Log line incorrectly recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadData16Bytes() {
        String logLine = "\"LDAPConnThread 10.235.5.15:1502\" daemon prio=1 tid=0x0000002bace3e260 "
                + "nid=0x2de1 runnable [0x00000000582d0000..0x00000000582d0e30]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataBlocked3SpaceNid() {
        String logLine = "\"ajp-127.0.0.1-8009-65\" daemon prio=10 tid=0x3ee5f800 nid=0x702 "
                + "waiting for monitor entry [0x3ce62000..0x3ce630b0]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataBlocked4SpaceNid() {
        String logLine = "\"ajp-127.0.0.1-8009-2032\" daemon prio=10 tid=0x3df37000 nid=0x1d0f "
                + "waiting for monitor entry [0x25363000..0x25363eb0]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataNameNoSpaceBeforeAddressRange() {
        String logLine = "\"ContainerBackgroundProcessor[StandardEngine[jboss.web]]\" daemon prio=10 "
                + "tid=0x088a2000 nid=0x797a sleeping[0x3ea9f000..0x3ea9fe30]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataNameWithBrackets() {
        String logLine = "\"ContainerBackgroundProcessor[StandardEngine[jboss.web]]\" daemon prio=10 "
                + "tid=0x088a2000 nid=0x797a waiting on condition [0x3ea9f000..0x3ea9fe30]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataNameWithForwardSlash() {
        String logLine = "\"/com/my/MyThread-10\" prio=1 tid=0x093c3318 nid=0x7c5e runnable [0x49fdd000..0x49fdde30]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataNameWithParenthesis() {
        String logLine = "\"Gang worker#0 (Parallel GC Threads)\" prio=10 tid=0x0805d800 nid=0x795b " + "runnable";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataNameWithSpaces() {
        String logLine = "\"Cache Object Manager Monitor\" prio=10 tid=0x095aa000 nid=0x7992 in Object.wait() "
                + "[0x3e315000..0x3e315e30]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataNameWithSpacesAtEnd() {
        String logLine = "\"Gang worker#0 (Parallel GC Threads)\" prio=10 tid=0x0805d800 nid=0x795b " + "runnable   ";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataNonDaemon() {
        String logLine = "\"JBossLifeThread\" prio=10 tid=0x08f4f400 nid=0x7996 in Object.wait() "
                + "[0x3e992000..0x3e993030]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataRunnable4SpaceNid() {
        String logLine = "\"Thread-144233478\" daemon prio=10 tid=0x22817800 nid=0x77f5 "
                + "runnable [0x25174000..0x25175030]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadDataWaiting4SpaceNid() {
        String logLine = "\"ajp-127.0.0.1-8009-2038\" daemon prio=10 tid=0x3eebb000 nid=0x1d16 "
                + "in Object.wait() [0x252be000..0x252befb0]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadNameNoPrio() {
        String logLine = "\"VM Thread\" os_prio=0 cpu=92316.68ms elapsed=895823.84s tid=0x0000562dbe5d1800 nid=0x9b "
                + "runnable";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadNameWithComma() {
        String logLine = "\"Timer runner-3,rhdg-cluster-w-prod-8-28051\" #20 prio=5 os_prio=0 cpu=569155.45ms "
                + "elapsed=895821.32s tid=0x0000562dc0fbc800 nid=0xb5 waiting on condition  [0x00007f20ce4e5000]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadNameWithCpuAndElapsed() {
        String logLine = "\"main\" #1 prio=5 os_prio=0 cpu=3680.53ms elapsed=895837.95s tid=0x0000562dbd1f2800 "
                + "nid=0x95 waiting on condition  [0x00007f21164a3000]";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadsClassSmrInfo() {
        // thread name pattern picks this up
        String logLine = "Threads class SMR info:";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadStateBlocked() {
        String logLine = "   java.lang.Thread.State: BLOCKED (on object monitor)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadStateRunnable() {
        String logLine = "   java.lang.Thread.State: RUNNABLE";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadStateTimedWaitingOnMonitor() {
        String logLine = "   java.lang.Thread.State: TIMED_WAITING (on object monitor)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadStateTimedWaitingSleeping() {
        String logLine = "   java.lang.Thread.State: TIMED_WAITING (sleeping)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testThreadStateWaiting() {
        String logLine = "   java.lang.Thread.State: WAITING (on object monitor)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testTimedWaitingParking() {
        String logLine = "   java.lang.Thread.State: TIMED_WAITING (parking)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testTitle() {
        String logLine = "Full thread dump Java HotSpot(TM) Server VM (11.0-b16 mixed mode):";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }

    @Test
    void testWaitingParking() {
        String logLine = "   java.lang.Thread.State: WAITING (parking)";
        assertTrue(ThreadDumpEvent.match(logLine),
                "Log line not recognized as " + JdkUtil.LogEventType.THREAD_DUMP.toString() + ".");
    }
}
