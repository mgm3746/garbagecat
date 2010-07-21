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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * CMS_INITIAL_MARK
 * </p>
 * 
 * <p>
 * A stop-the-world phase of the concurrent low pause collector that identifies the initial set of
 * live objects directly reachable from GC roots. This event does not do any garbage collection,
 * only marking of objects.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] 4150346K(8367360K), 0.0174433 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class CmsInitialMarkEvent implements BlockingEvent {

	/**
	 * The log entry for the event. Can be used for debugging purposes.
	 */
	private String logEntry;

	/**
	 * The elapsed clock time for the GC event in milliseconds (rounded).
	 */
	private int duration;

	/**
	 * The time when the GC event happened in milliseconds after JVM startup.
	 */
	private long timestamp;

	/**
	 * Regular expressions defining the logging.
	 */
	private static final String REGEX = "^" + JdkRegEx.TIMESTAMP
			+ ": \\[GC \\[1 CMS-initial-mark: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] "
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]"
			+ JdkRegEx.TIMES_BLOCK + "?[ ]*$";

	/**
	 * Create CMS Initial Mark logging event from log entry.
	 */
	public CmsInitialMarkEvent(String logEntry) {
		this.logEntry = logEntry;
		Pattern pattern = Pattern.compile(CmsInitialMarkEvent.REGEX);
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			duration = JdkMath.convertSecsToMillis(matcher.group(6)).intValue();
		}
	}

	/**
	 * Alternate constructor. Create CMS Initial Mark from values.
	 * 
	 * @param logEntry
	 * @param timestamp
	 * @param duration
	 */
	public CmsInitialMarkEvent(String logEntry, long timestamp, int duration) {
		this.logEntry = logEntry;
		this.timestamp = timestamp;
		this.duration = duration;
	}

	public String getLogEntry() {
		return logEntry;
	}

	public int getDuration() {
		return duration;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getName() {
		return JdkUtil.LogEventType.CMS_INITIAL_MARK.toString();
	}

	/**
	 * Determine if the logLine matches the logging pattern(s) for this event.
	 * 
	 * @param logLine
	 *            The log line to test.
	 * @return true if the log line matches the event pattern, false otherwise.
	 */
	public static final boolean match(String logLine) {
		return logLine.matches(REGEX);
	}
}
