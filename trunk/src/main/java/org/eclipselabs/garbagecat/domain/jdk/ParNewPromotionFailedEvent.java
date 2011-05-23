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
 * PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT
 * </p>
 * 
 * <p>
 * Occurs when objects cannot be moved from the young to the old generation due to lack of space or
 * fragmentation. The young generation collection backs out of the young collection and initiates a
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent} full collection in an attempt to
 * free up and compact space. This is an expensive operation that typically results in large pause
 * times.
 * </p>
 * 
 * <p>
 * The CMS collector is not a compacting collector. It discovers garbage and adds the memory to free
 * lists of available space that it maintains based on popular object sizes. If many objects of
 * varying sizes are allocated, the free lists will be split. This can lead to many free lists whose
 * total size is large enough to satisfy the calculated free space needed for promotions; however,
 * there is not enough contiguous space for one of the objects being promoted.
 * </p>
 * 
 * <p>
 * Prior to Java 5.0 the space requirement was the worst-case scenario that all young generation
 * objects get promoted to the old generation (the young generation guarantee). Starting in Java 5.0
 * the space requirement is an estimate based on recent promotion history and is usually much less
 * than the young generation guarantee.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 144501.626: [GC 144501.627: [ParNew (promotion failed): 680066K-&gt;680066K(707840K), 3.7067346 secs] 1971073K-&gt;1981370K(2018560K), 3.7084059 secs]
 * </pre>
 * 
 * <p>
 * In incremental mode (<code>-XX:+CMSIncrementalMode</code>):
 * </p>
 * 
 * <pre>
 * 159275.552: [GC 159275.552: [ParNew (promotion failed): 2007040K-&gt;2007040K(2007040K), 4.3393411 secs] 5167424K-&gt;5187429K(12394496K) icms_dc=7 , 4.3398519 secs] [Times: user=4.96 sys=1.91, real=4.34 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class ParNewPromotionFailedEvent implements BlockingEvent {

	/**
	 * Regular expressions defining the logging.
	 */
	private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP
			+ ": \\[ParNew \\(promotion failed\\): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->"
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)" + JdkRegEx.ICMS_DC_BLOCK + "?, "
			+ JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";
        private static Pattern pattern = Pattern.compile(REGEX);

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
	 * Create ParNew detail logging event from log entry.
	 */
	public ParNewPromotionFailedEvent(String logEntry) {
		this.logEntry = logEntry;
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			duration = JdkMath.convertSecsToMillis(matcher.group(11)).intValue();
		}
	}

	/**
	 * Alternate constructor. Create ParNew detail logging event from values.
	 * 
	 * @param logEntry
	 * @param timestamp
	 * @param duration
	 */
	public ParNewPromotionFailedEvent(String logEntry, long timestamp, int duration) {
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
		return JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED.toString();
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
