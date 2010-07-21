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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * CMS_CONCURRENT_MODE_FAILURE
 * </p>
 * 
 * <p>
 * Combine {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldConcurrentModeFailureEvent} and
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewConcurrentModeFailurePermDataEvent} logging
 * that is split across multiple lines into a single line.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldConcurrentModeFailureEvent}:
 * </p>
 * 
 * <pre>
 * 85217.903: [Full GC 85217.903: [CMS85217.919: [CMS-concurrent-abortable-preclean: 0.723/3.756 secs] [Times: user=2.54 sys=0.08, real=3.76 secs]
 *  (concurrent mode failure): 439328K-&gt;439609K(4023936K), 2.7153820 secs] 448884K-&gt;439609K(4177280K), [CMS Perm : 262143K-&gt;262143K(262144K)], 2.7156150 secs] [Times: user=3.35 sys=0.00, real=2.72 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 85217.903: [Full GC 85217.903: [CMS85217.919: [CMS-concurrent-abortable-preclean: 0.723/3.756 secs] (concurrent mode failure): 439328K-&gt;439609K(4023936K), 2.7153820 secs] 448884K-&gt;439609K(4177280K), [CMS Perm : 262143K-&gt;262143K(262144K)], 2.7156150 secs] [Times: user=3.35 sys=0.00, real=2.72 secs]
 *</pre>
 * 
 * <p>
 * 2) {@link org.eclipselabs.garbagecat.domain.jdk.ParNewConcurrentModeFailurePermDataEvent}:
 * </p>
 * 
 * <pre>
 * 3070.289: [GC 3070.289: [ParNew: 207744K-&gt;207744K(242304K), 0.0000682 secs]3070.289: [CMS3081.621: [CMS-concurrent-mark: 11.907/12.958 secs] [Times: user=45.31 sys=3.93, real=12.96 secs]
 *  (concurrent mode failure): 6010121K-&gt;6014591K(6014592K), 79.0505229 secs] 6217865K-&gt;6028029K(6256896K), [CMS Perm : 206688K-&gt;206662K(262144K)], 79.0509595 secs] [Times: user=104.69 sys=3.63, real=79.05 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 3070.289: [GC 3070.289: [ParNew: 207744K-&gt;207744K(242304K), 0.0000682 secs]3070.289: [CMS3081.621: [CMS-concurrent-mark: 11.907/12.958 secs] (concurrent mode failure): 6010121K-&gt;6014591K(6014592K), 79.0505229 secs] 6217865K-&gt;6028029K(6256896K), [CMS Perm : 206688K-&gt;206662K(262144K)], 79.0509595 secs] [Times: user=104.69 sys=3.63, real=79.05 secs]
 * </pre>
 * 
 * <p>
 * 3) {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldConcurrentModeFailureEvent} across 3
 * lines:
 * </p>
 * 
 * <pre>
 * 4300.825: [Full GC 4300.825: [CMSbailing out to foreground collection
 * 4310.434: [CMS-concurrent-mark: 10.548/10.777 secs] [Times: user=40.43 sys=3.94, real=10.78 secs]
 *  (concurrent mode failure): 6014591K-&gt;6014592K(6014592K), 79.9352305 secs] 6256895K-&gt;6147510K(6256896K), [CMS Perm : 206989K-&gt;206977K(262144K)], 79.9356622 secs] [Times: user=101.02 sys=3.09, real=79.94 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 4300.825: [Full GC 4300.825: [CMS4310.434: [CMS-concurrent-mark: 10.548/10.777 secs] (concurrent mode failure): 6014591K-&gt;6014592K(6014592K), 79.9352305 secs] 6256895K-&gt;6147510K(6256896K), [CMS Perm : 206989K-&gt;206977K(262144K)], 79.9356622 secs] [Times: user=101.02 sys=3.09, real=79.94 secs]
 * </pre>
 * 
 * <p>
 * 4)
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent}
 * with "concurrent mode failure" missing:
 * </p>
 * 
 * <pre>
 * 88063.609: [GC 88063.610: [ParNew (promotion failed): 513856K-&gt;513856K(513856K), 4.0911197 secs]88067.701: [CMS88067.742: [CMS-concurrent-reset: 0.309/4.421 secs] [Times: user=9.62 sys=3.73, real=4.42 secs]
 * : 10612422K-&gt;4373474K(11911168K), 76.7523274 secs] 11075362K-&gt;4373474K(12425024K), [CMS Perm : 214530K-&gt;213777K(524288K)], 80.8440551 secs] [Times: user=80.01 sys=5.57, real=80.84 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class CmsConcurrentModeFailurePreprocessAction implements PreprocessAction {

	/**
	 * Regular expression(s) defining the 1st logging line.
	 */
	private static final String[] REGEX_LINE_INIT = {
			// Serial old
			"^(" + JdkRegEx.TIMESTAMP + ": \\[Full GC (\\(System\\) )?" + JdkRegEx.TIMESTAMP
					+ ": \\[CMS( CMS: abort preclean due to time )?" + JdkRegEx.TIMESTAMP
					+ ": \\[CMS-concurrent-(abortable-preclean|mark|preclean|sweep): "
					+ JdkRegEx.DURATION_FRACTION + "\\])" + JdkRegEx.TIMES_BLOCK + "?[ ]*$",
			// ParNew
			"^(" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP
					+ ": \\[ParNew( \\(promotion failed\\))?: " + JdkRegEx.SIZE + "->"
					+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]"
					+ JdkRegEx.TIMESTAMP + ": \\[CMS( CMS: abort preclean due to time )?"
					+ JdkRegEx.TIMESTAMP
					+ ": \\[CMS-concurrent-(abortable-preclean|mark|preclean|reset|sweep): "
					+ JdkRegEx.DURATION_FRACTION + "\\])" + JdkRegEx.TIMES_BLOCK + "?[ ]*$",
			// ParNew bailing out (splits the initial line into 2 lines)
			"^(" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP
					+ ": \\[ParNew( \\(promotion failed\\))?: " + JdkRegEx.SIZE + "->"
					+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]"
					+ JdkRegEx.TIMESTAMP + ": \\[CMS)bailing out to foreground collection"
					+ JdkRegEx.TIMES_BLOCK + "?[ ]*$",
			// Added for dataset14 where logging is split across 3 lines
			"^(" + JdkRegEx.TIMESTAMP + ": \\[Full GC " + JdkRegEx.TIMESTAMP
					+ ": \\[CMS)bailing out to foreground collection" + JdkRegEx.TIMES_BLOCK
					+ "?[ ]*$",
			"^(" + JdkRegEx.TIMESTAMP + ": \\[CMS-concurrent-mark: " + JdkRegEx.DURATION_FRACTION
					+ "\\])" + JdkRegEx.TIMES_BLOCK + "?[ ]*$" };

	/**
	 * Regular expression(s) defining the end logging line. In JDK 6 "failure" has been replaced
	 * with "interrupted".
	 */
	private static final String[] REGEX_LINE_END = {
			"^ \\(concurrent mode (failure|interrupted)\\).+$",
			"^: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
					+ JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
					+ JdkRegEx.SIZE + "\\), \\[CMS Perm : " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
					+ "\\(" + JdkRegEx.SIZE + "\\)\\], " + JdkRegEx.DURATION + "\\]"
					+ JdkRegEx.TIMES_BLOCK + "?[ ]*$" };

	/**
	 * The log entry for the event. Can be used for debugging purposes.
	 */
	private String logEntry;

	/**
	 * Create event from log entry.
	 * 
	 * @param logEntry
	 *            The log entry being processed.
	 */
	public CmsConcurrentModeFailurePreprocessAction(String logEntry) {
		if (isLastLineMatch(logEntry)) {
			this.logEntry = logEntry + System.getProperty("line.separator");
		} else {
			Pattern pattern;
			for (int i = 0; i < REGEX_LINE_INIT.length; i++) {
				pattern = Pattern.compile(REGEX_LINE_INIT[i]);
				Matcher matcher = pattern.matcher(logEntry);
				if (matcher.find() && matcher.group(1) != null) {
					this.logEntry = matcher.group(1);
					break;
				}
			}
		}
	}

	public String getLogEntry() {
		return logEntry;
	}

	public String getName() {
		return JdkUtil.PreprocessActionType.CMS_CONCURRENT_MODE_FAILURE.toString();
	}

	/**
	 * Determine if the logLine matches the logging pattern(s) for this event.
	 * 
	 * @param logLine
	 *            The log line to test.
	 * @param priorLogLine
	 *            The last log entry processed.
	 * @param nextLogLine
	 *            The next log entry processed.
	 * @return true if the log line matches the event pattern, false otherwise.
	 */
	public static final boolean match(String logLine, String priorLogLine, String nextLogLine) {
		return ((isInitialLineMatch(logLine) && (isInitialLineMatch(nextLogLine) || isLastLineMatch(nextLogLine))) || (isLastLineMatch(logLine) && isInitialLineMatch(priorLogLine)));
	}

	/**
	 * Check log line against initial line patterns to determine if the log line is a match or not.
	 * 
	 * @param logLine
	 *            The log line to test.
	 * @return true if the log line matches a line1 pattern, false otherwise.
	 */
	private static final boolean isInitialLineMatch(String logLine) {
		boolean isInitialLineMatch = false;
		for (int i = 0; i < REGEX_LINE_INIT.length; i++) {
			if (logLine.matches(REGEX_LINE_INIT[i])) {
				isInitialLineMatch = true;
				break;
			}
		}
		return isInitialLineMatch;
	}

	/**
	 * Check log line against last line patterns to determine if the log line is a match or not.
	 * 
	 * @param logLine
	 *            The log line to test.
	 * @return true if the log line matches a line2 pattern, false otherwise.
	 */
	private static final boolean isLastLineMatch(String logLine) {
		boolean isLastLineMatch = false;
		for (int i = 0; i < REGEX_LINE_END.length; i++) {
			if (logLine.matches(REGEX_LINE_END[i])) {
				isLastLineMatch = true;
				break;
			}
		}
		return isLastLineMatch;
	}
}
