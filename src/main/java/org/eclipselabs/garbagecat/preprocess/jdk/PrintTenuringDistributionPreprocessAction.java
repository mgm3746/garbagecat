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
 * PRINT_TENURING_DISTRIBUTION
 * </p>
 * 
 * <p>
 * Remove <code>-XX:+PrintTenuringDistribution</code> logging from the underlying garbage collection
 * event. This data is currently not being used for any analysis.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.SerialEvent}:
 * </p>
 * 
 * <pre>
 * 10.204: [GC 10.204: [DefNew
 * Desired survivor size 2228224 bytes, new threshold 1 (max 15)
 * - age   1:    3177664 bytes,    3177664 total
 * - age   2:    1278784 bytes,    4456448 total
 * : 36825K-&gt;4352K(39424K), 0.0224830 secs] 44983K-&gt;14441K(126848K), 0.0225800 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 10.204: [GC 10.204: [DefNew: 36825K-&gt;4352K(39424K), 0.0224830 secs] 44983K-&gt;14441K(126848K), 0.0225800 secs]
 * </pre>
 * 
 * <p>
 * 2) Underlying
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent}
 * :
 * </p>
 * 
 * <pre>
 * 877369.458: [GC 877369.459: [ParNew (promotion failed)
 * Desired survivor size 120795952 bytes, new threshold 3 (max 31)
 * - age   1:   92513688 bytes,   92513688 total
 * - age   2:   16401312 bytes,  108915000 total
 * - age   3:   19123776 bytes,  128038776 total
 * - age   4:    6178856 bytes,  134217632 total
 * : 917504K-&gt;917504K(917504K), 5.5887120 secs]877375.047: [CMS877378.691: [CMS-concurrent-mark: 5.714/11.380 secs] [Times: user=14.72 sys=4.81, real=11.38 secs]
 *  (concurrent mode failure): 1567700K-&gt;1571451K(1572864K), 14.6444240 secs] 2370842K-&gt;1694149K(2490368K), [CMS Perm : 46359K-&gt;46354K(77352K)], 20.2345470 secs] [Times: user=22.17 sys=4.56, real=20.23 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 877369.458: [GC 877369.459: [ParNew (promotion failed): 917504K-&gt;917504K(917504K), 5.5887120 secs]877375.047: [CMS877378.691: [CMS-concurrent-mark: 5.714/11.380 secs] (concurrent mode failure): 1567700K-&gt;1571451K(1572864K), 14.6444240 secs] 2370842K-&gt;1694149K(2490368K), [CMS Perm : 46359K-&gt;46354K(77352K)], 20.2345470 secs] [Times: user=22.17 sys=4.56, real=20.23 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class PrintTenuringDistributionPreprocessAction implements PreprocessAction {

	/**
	 * Regular expressions for the beginning part of a line retained.
	 */
	private static final String[] REGEX_RETAIN_BEGINNING = {
			"^(" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP
					+ ": \\[(Def|Par)New( \\(promotion failed\\))?)$",
			// Concurrent mode failure. Treat it like a beginning line.
			"(: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
					+ JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
					+ ": \\[CMS( CMS: abort preclean due to time )?" + JdkRegEx.TIMESTAMP
					+ ": \\[CMS-concurrent-(abortable-preclean|mark|preclean|sweep): "
					+ JdkRegEx.DURATION_FRACTION + "\\])" + JdkRegEx.TIMES_BLOCK + "?[ ]*$" };

	/**
	 * Regular expression for the end part of a line retained.
	 */
	private static final String[] REGEX_RETAIN_END = {
	// Normal young collection
	"^: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
			+ JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$", };

	/**
	 * Regular expressions for lines or parts of lines thrown away.
	 */
	private static final String[] REGEX_THROWAWAY = {
			"^Desired survivor size \\d{1,11} bytes, new threshold \\d{1,2} \\(max \\d{1,2}\\)$",
			"^- age[ ]+\\d{1,2}:[ ]+\\d{1,11} bytes,[ ]+\\d{1,11} total$",
			"^" + JdkRegEx.TIMES_BLOCK + "[ ]*$" };

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
	public PrintTenuringDistributionPreprocessAction(String logEntry) {
		// Handle split logging. Keep parts of log lines needed for re-composing.
		Pattern pattern;
		Matcher matcher;
		// Check to see if beginning of line should be retained.
		boolean retainBeginning = false;
		for (int i = 0; i < REGEX_RETAIN_BEGINNING.length; i++) {
			pattern = Pattern.compile(REGEX_RETAIN_BEGINNING[i]);
			matcher = pattern.matcher(logEntry);
			if (matcher.find() && matcher.group(1) != null) {
				// Retain beginning of line.
				this.logEntry = matcher.group(1);
				retainBeginning = true;
			}
		}
		// Check to see if end of line should be retained.
		if (!retainBeginning) {
			boolean retain = false;
			for (int i = 0; i < REGEX_RETAIN_END.length; i++) {
				if (logEntry.matches(REGEX_RETAIN_END[i])) {
					retain = true;
					break;
				}
			}
			if (retain) {
				this.logEntry = logEntry + "\n";
			}
		}
	}

	public String getLogEntry() {
		return logEntry;
	}

	public String getName() {
		return JdkUtil.PreprocessActionType.PRINT_TENURING_DISTRIBUTION.toString();
	}

	/**
	 * Determine if the logLine matches the logging pattern(s) for this event.
	 * 
	 * @param logLine
	 *            The log line to test.
	 * @return true if the log line matches the event pattern, false otherwise.
	 */
	public static final boolean match(String logLine) {
		boolean match = false;
		for (int i = 0; i < REGEX_THROWAWAY.length; i++) {
			if (logLine.matches(REGEX_THROWAWAY[i])) {
				match = true;
				break;
			}
		}
		if (!match) {
			for (int i = 0; i < REGEX_RETAIN_BEGINNING.length; i++) {
				if (logLine.matches(REGEX_RETAIN_BEGINNING[i])) {
					match = true;
					break;
				}
			}
		}
		if (!match) {
			for (int i = 0; i < REGEX_RETAIN_END.length; i++) {
				if (logLine.matches(REGEX_RETAIN_END[i])) {
					match = true;
					break;
				}
			}
		}
		return match;
	}
}
