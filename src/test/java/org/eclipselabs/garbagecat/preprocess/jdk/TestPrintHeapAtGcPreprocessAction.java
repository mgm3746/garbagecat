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
public class TestPrintHeapAtGcPreprocessAction extends TestCase {

	public void testHeapBeforeLine() {
		String logLine = "{Heap before gc invocations=1:";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testHeapBeforeFullLine() {
		String logLine = "{Heap before GC invocations=261 (full 10):";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testHeapAfterLine() {
		String logLine = "Heap after gc invocations=362:";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testPsYoungGenLine() {
		String logLine = " PSYoungGen      total 434880K, used 89473K [0x00002b3998b80000, "
				+ "0x00002b39b70d0000, 0x00002b39b70d0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testEdenSpaceLine() {
		String logLine = "  eden space 372800K, 24% used [0x00002b3998b80000,"
				+ "0x00002b399e2e0660,0x00002b39af790000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	/**
	 * One space before percent.
	 */
	public void testEdenSpaceLineOneSpace() {
		String logLine = "  eden space 372800K, 24% used [0x00002b3998b80000,"
				+ "0x00002b399e2e0660,0x00002b39af790000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	/**
	 * Two spaces before percent.
	 */
	public void testEdenSpaceLineTwoSpaces() {
		String logLine = "  eden space 785024K,  39% used [0x00002aabdaab0000, "
				+ "0x00002aabed98be48, 0x00002aac0a950000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testFromSpaceLine() {
		String logLine = "  from space 62080K, 0% used [0x00002b39b3430000,"
				+ "0x00002b39b3430000,0x00002b39b70d0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testToSpaceLine() {
		String logLine = "  to   space 62080K, 0% used [0x00002b39af790000,"
				+ "0x00002b39af790000,0x00002b39b3430000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testPsOldGenLine() {
		String logLine = " PSOldGen        total 993984K, used 0K [0x00002b395c0d0000, "
				+ "0x00002b3998b80000, 0x00002b3998b80000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testParOldGenLine() {
		String logLine = " ParOldGen       total 1572864K, used 1380722K [0x00002b5dd6bd0000, "
				+ "0x00002b5e36bd0000, 0x00002b5e36bd0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testObjectSpaceLine() {
		String logLine = "  object space 993984K, 0% used [0x00002b395c0d0000,"
				+ "0x00002b395c0d0000,0x00002b3998b80000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testPsPermGenLine() {
		String logLine = " PSPermGen       total 524288K, used 13076K [0x00002b393c0d0000, "
				+ "0x00002b395c0d0000, 0x00002b395c0d0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testBraceLine() {
		String logLine = "}";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testFirstLineSplit() {
		String logLine = "24.919: [Full GC {Heap before gc invocations=0:";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
		PrintHeapAtGcPreprocessAction event = new PrintHeapAtGcPreprocessAction(logLine);
		Assert.assertEquals("Log line not parsed correctly.", "24.919: [Full GC ", event
				.getLogEntry());
	}

	public void testLastLineSplit() {
		String logLine = ", 11.0980780 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testParNewGenerationLine() {
		String logLine = " par new generation   total 785728K, used 310127K [0x00002aabdaab0000, "
				+ "0x00002aac0aab0000, 0x00002aac0aab0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testCmsGenerationLine() {
		String logLine = " concurrent mark-sweep generation total 3407872K, used 1640998K "
				+ "[0x00002aac0aab0000, 0x00002aacdaab0000, 0x00002aacdaab0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testCmsPermGenLine() {
		String logLine = " concurrent-mark-sweep perm gen total 786432K, used 507386K "
				+ "[0x00002aacdaab0000, 0x00002aad0aab0000, 0x00002aad0aab0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testParNewAbortablePrecleanLine() {
		String logLine = "27660.445: [ParNew: 261760K->261760K(261952K), 0.0000160 secs]27660.445: "
				+ "[CMS27660.445: [CMS-concurrent-abortable-preclean: 0.485/6.189 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testParNewMarkLine() {
		String logLine = "27636.893: [ParNew: 261760K->261760K(261952K), 0.0000130 secs]27636.893: "
				+ "[CMS27639.231: [CMS-concurrent-mark: 4.803/4.803 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testParNewPrecleanLine() {
		String logLine = "29614.168: [ParNew: 261760K->261760K(261952K), 0.0000130 secs]"
				+ "29614.168: [CMS29614.299: [CMS-concurrent-preclean: 0.202/0.225 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testParNewResetLine() {
		String logLine = "2184846.448: [ParNew: 261951K->261951K(261952K), 0.0000160 secs]"
				+ "2184846.448: [CMS2184846.472: [CMS-concurrent-reset: 0.029/0.029 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testCmsAbortablePrecleanLine() {
		String logLine = "30236.705: [CMS30236.865: [CMS-concurrent-abortable-preclean: 9.850/182.656 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testCmsMarkLine() {
		String logLine = "30250.335: [CMS30251.596: [CMS-concurrent-mark: 5.140/5.149 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testCmsPrecleanLine() {
		String logLine = "28282.075: [CMS28284.687: [CMS-concurrent-preclean: 3.706/3.706 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testCmsSweepLine() {
		String logLine = "29924.816: [CMS29925.342: [CMS-concurrent-sweep: 0.847/0.848 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testParNewPromotionFailedAbortablePrecleanLine() {
		String logLine = "28308.701: [ParNew (promotion failed): 261951K->261951K(261952K), 0.7470390 secs]"
				+ "28309.448: [CMS28312.544: [CMS-concurrent-mark: 5.114/5.863 secs]";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testHeapBeforePrintGCDateStampsLine() {
		String logLine = "{Heap before GC invocations=0 (full 0):";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testHeapAfterPrintGCDateStampsLine() {
		String logLine = "Heap after GC invocations=1 (full 0):";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testClassDataSharingLine() {
		String logLine = "No shared spaces configured.";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testTenuredGenerationLine() {
		String logLine = " tenured generation   total 704512K, used 17793K [0x00002aab2fab0000, "
				+ "0x00002aab5aab0000, 0x00002aab7aab0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testCompactingPermGenLine() {
		String logLine = " compacting perm gen  total 262144K, used 65340K [0x00002aab7aab0000, "
				+ "0x00002aab8aab0000, 0x00002aab8aab0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testTheSpaceLine() {
		String logLine = "   the space 704512K,   2% used [0x00002aab2fab0000, 0x00002aab30c107e8, "
				+ "0x00002aab30c10800, 0x00002aab5aab0000)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}

	public void testParNewLine() {
		String logLine = "45.502: [ParNew";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));

	}

	public void testParNewPromotionFailedLine() {
		String logLine = "18222.002: [ParNew (promotion failed)";
		Assert.assertTrue("Log line not recognized as "
				+ JdkUtil.PreprocessActionType.PRINT_HEAP_AT_GC.toString() + ".",
				PrintHeapAtGcPreprocessAction.match(logLine));
	}
}
