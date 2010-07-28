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

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * DATE_STAMP
 * </p>
 * 
 * <p>
 * Logging with a datestamp instead of a timestamp indicating the number of seconds after JVM
 * startup. Enabled with the <code>-XX:+PrintGCDateStamps</code> option added in JDK 1.6 update 4.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Datestamp in place of a timestamp:
 * </p>
 * 
 * <pre>
 * 2010-02-26T09:32:12.486-0600: [GC [ParNew: 150784K-&gt;3817K(169600K), 0.0328800 secs] 150784K-&gt;3817K(1029760K), 0.0329790 secs] [Times: user=0.00 sys=0.00, real=0.03 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 142.973: [GC [ParNew: 150784K-&gt;3817K(169600K), 0.0328800 secs] 150784K-&gt;3817K(1029760K), 0.0329790 secs] [Times: user=0.00 sys=0.00, real=0.03 secs]
 * </pre>
 * 
 * 
 * <p>
 * 2) Datestamp prefix:
 * </p>
 * 
 * <pre>
 * 2010-04-16T12:11:18.979+0200: 84.335: [GC 84.336: [ParNew: 273152K-&gt;858K(341376K), 0.0030008 secs] 273152K-&gt;858K(980352K), 0.0031183 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
 * </pre>
 * 
 * Preprocessed:
 * 
 * <pre>
 * 84.335: [GC 84.336: [ParNew: 273152K-&gt;858K(341376K), 0.0030008 secs] 273152K-&gt;858K(980352K), 0.0031183 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class DateStampPreprocessAction implements PreprocessAction {

	/**
	 * Regular expressions defining the logging line.
	 */
	private static final String REGEX_LINE = "^" + JdkRegEx.DATESTAMP + ": (.*)$";

	/**
	 * The log entry for the event. Can be used for debugging purposes.
	 */
	private String logEntry;

	/**
	 * Create event from log entry.
	 * 
	 * @param logEntry
	 *            The log entry.
	 * @param jvmStartDate
	 *            The date and time the JVM was started.
	 */
	public DateStampPreprocessAction(String logEntry, Date jvmStartDate) {
		Pattern pattern = Pattern.compile(REGEX_LINE);
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			String logEntryMinusDateStamp = matcher.group(11);
			if (JdkUtil.identifyEventType(logEntryMinusDateStamp).equals(
					JdkUtil.LogEventType.UNKNOWN)) {
				// Logging with datestamp in place of a timestamp
				StringBuffer sb = new StringBuffer();
				Date datestamp = GcUtil.parseDateStamp(matcher.group(1));
				long diff = GcUtil.dateDiff(jvmStartDate, datestamp);
				sb.append((JdkMath.convertMillisToSecs(diff)).toString());
				sb.append(": ");
				sb.append(logEntryMinusDateStamp);
				this.logEntry = sb.toString();
			} else {
				// Logging with datestamp prefix:
				this.logEntry = logEntryMinusDateStamp;
			}
		}
	}

	public String getLogEntry() {
		return logEntry;
	}

	public String getName() {
		return JdkUtil.PreprocessActionType.DATE_STAMP.toString();
	}

	/**
	 * Determine if the logLine matches the logging pattern(s) for this event.
	 * 
	 * @param logLine
	 *            The log line to test.
	 * @return true if the log line matches the event pattern, false otherwise.
	 */
	public static final boolean match(String logLine) {
		return logLine.matches(REGEX_LINE);
	}
}
