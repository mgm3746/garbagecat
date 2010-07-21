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
 * CMS_REMARK_WITH_CLASS_UNLOADING
 * </p>
 * 
 * <p>
 * A {@link org.eclipselabs.garbagecat.domain.jdk.CmsRemarkEvent} with the
 * <code>-XX:+CMSClassUnloadingEnabled</code> JVM option enabled to allow perm gen collections. The
 * concurrent low pause collector does not allow for class unloading by default.
 * 
 * <h3>Example Logging</h3>
 * </p>
 * 
 * <pre>
 * 76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: [Rescan (parallel) , 0.0574180 secs]76694.785: [weak refs processing, 0.0170540 secs]76694.802: [class unloading, 0.0363010 secs]76694.838: [scrub symbol &amp; string tables, 0.0276600 secs] [1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class CmsRemarkWithClassUnloadingEvent implements BlockingEvent {

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
	private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC\\[YG occupancy: "
			+ JdkRegEx.SIZE + " \\(" + JdkRegEx.SIZE + "\\)\\]" + JdkRegEx.TIMESTAMP
			+ ": \\[Rescan \\(parallel\\) , " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
			+ ": \\[weak refs processing, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
			+ ": \\[class unloading, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
			+ ": \\[scrub symbol & string tables, " + JdkRegEx.DURATION + "\\] \\[1 CMS-remark: "
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] " + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

	/**
	 * Create CMS Remark with class unloading logging event from log entry.
	 * 
	 * @param logEntry
	 */
	public CmsRemarkWithClassUnloadingEvent(String logEntry) {
		this.logEntry = logEntry;
		Pattern pattern = Pattern.compile(CmsRemarkWithClassUnloadingEvent.REGEX);
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			// The last duration is the total duration for the phase.
			duration = JdkMath.convertSecsToMillis(matcher.group(16)).intValue();
		}
	}

	/**
	 * Alternate constructor. Create CMS Remark with class unloading logging event from values.
	 * 
	 * @param logEntry
	 * @param timestamp
	 * @param duration
	 */
	public CmsRemarkWithClassUnloadingEvent(String logEntry, long timestamp, int duration) {
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
		return JdkUtil.LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING.toString();
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
