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
package org.eclipselabs.garbagecat.preprocess.jdk;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestThreadDumpPreprocessAction extends TestCase {

	public void testBeginningDateTime() {
		String logLine = "2009-12-29 14:17:17";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testTitle() {
		String logLine = "Full thread dump Java HotSpot(TM) Server VM (11.0-b16 mixed mode):";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataRunnable4SpaceNid() {
		String logLine = "\"Thread-144233478\" daemon prio=10 tid=0x22817800 nid=0x77f5 "
				+ "runnable [0x25174000..0x25175030]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataWaiting4SpaceNid() {
		String logLine = "\"ajp-127.0.0.1-8009-2038\" daemon prio=10 tid=0x3eebb000 nid=0x1d16 "
				+ "in Object.wait() [0x252be000..0x252befb0]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataBlocked3SpaceNid() {
		String logLine = "\"ajp-127.0.0.1-8009-65\" daemon prio=10 tid=0x3ee5f800 nid=0x702 "
				+ "waiting for monitor entry [0x3ce62000..0x3ce630b0]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataBlocked4SpaceNid() {
		String logLine = "\"ajp-127.0.0.1-8009-2032\" daemon prio=10 tid=0x3df37000 nid=0x1d0f "
				+ "waiting for monitor entry [0x25363000..0x25363eb0]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataNonDaemon() {
		String logLine = "\"JBossLifeThread\" prio=10 tid=0x08f4f400 nid=0x7996 in Object.wait() "
				+ "[0x3e992000..0x3e993030]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataNameWithSpaces() {
		String logLine = "\"Cache Object Manager Monitor\" prio=10 tid=0x095aa000 nid=0x7992 in Object.wait() "
				+ "[0x3e315000..0x3e315e30]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataNameWithBrackets() {
		String logLine = "\"ContainerBackgroundProcessor[StandardEngine[jboss.web]]\" daemon prio=10 "
				+ "tid=0x088a2000 nid=0x797a waiting on condition [0x3ea9f000..0x3ea9fe30]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataNameWithParenthesis() {
		String logLine = "\"Gang worker#0 (Parallel GC Threads)\" prio=10 tid=0x0805d800 nid=0x795b "
				+ "runnable";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataNameWithSpacesAtEnd() {
		String logLine = "\"Gang worker#0 (Parallel GC Threads)\" prio=10 tid=0x0805d800 nid=0x795b "
				+ "runnable   ";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadDataNameNoSpaceBeforeAddressRange() {
		String logLine = "\"ContainerBackgroundProcessor[StandardEngine[jboss.web]]\" daemon prio=10 "
				+ "tid=0x088a2000 nid=0x797a sleeping[0x3ea9f000..0x3ea9fe30]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadData16Bytes() {
		String logLine = "\"LDAPConnThread 10.235.5.15:1502\" daemon prio=1 tid=0x0000002bace3e260 "
				+ "nid=0x2de1 runnable [0x00000000582d0000..0x00000000582d0e30]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadStateRunnable() {
		String logLine = "   java.lang.Thread.State: RUNNABLE";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadStateWaiting() {
		String logLine = "   java.lang.Thread.State: WAITING (on object monitor)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadStateTimedWaitingOnMonitor() {
		String logLine = "   java.lang.Thread.State: TIMED_WAITING (on object monitor)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadStateTimedWaitingSleeping() {
		String logLine = "   java.lang.Thread.State: TIMED_WAITING (sleeping)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testThreadStateBlocked() {
		String logLine = "   java.lang.Thread.State: BLOCKED (on object monitor)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testStackTraceLocation() {
		String logLine = "	at java.lang.Object.wait(Native Method)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testStackTraceEventLocked() {
		String logLine = "	- locked <0x94fa4fb0> (a org.apache.tomcat.util.net.JIoEndpoint$Worker)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testStackTraceEventWaitingToLock() {
		String logLine = "	- waiting to lock <0x4b2da3f0> (a com.voxmobili.impl.pim.ab.BAddressBookContext)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testStackTraceEventWaitingOn() {
		String logLine = "	- waiting on <0x889d3168> (a java.lang.Object)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testSummaryJni() {
		String logLine = "JNI global references: 844";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testSummaryHeap() {
		String logLine = "Heap";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testSummaryParNewGeneration() {
		String logLine = " par new generation   total 917504K, used 808761K [0x44c40000, 0x84c40000, 0x84c40000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testSummaryEdenSpace() {
		String logLine = "  eden space 786432K, 100% used [0x44c40000, 0x74c40000, 0x74c40000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testSummaryFromSpace() {
		String logLine = "  from space 131072K,  17% used [0x7cc40000, 0x7e20e790, 0x84c40000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testSummaryToSpace() {
		String logLine = "  to   space 131072K,   0% used [0x74c40000, 0x74c40000, 0x7cc40000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testSummaryCmsGenerationTotal() {
		String logLine = " concurrent mark-sweep generation total 1572864K, used 1572863K [0x84c40000, 0xe4c40000, 0xe4c40000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}

	public void testSummaryCmsPermGenTotal() {
		String logLine = " concurrent-mark-sweep perm gen total 77736K, used 46547K [0xe4c40000, 0xe982a000, 0xf4c40000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.THREAD_DUMP.toString() + ".",
				ThreadDumpPreprocessAction.match(logLine));
	}
}
