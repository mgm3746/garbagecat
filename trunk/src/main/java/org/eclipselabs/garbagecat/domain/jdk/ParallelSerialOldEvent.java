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
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermCollection;
import org.eclipselabs.garbagecat.domain.PermData;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * PARALLEL_SERIAL_OLD
 * </p>
 * 
 * <p>
 * Enabled with <code>-XX:+UseParallelGC</code> JVM option. This is really a
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent}; however, the logging is different.
 * Treat as a separate event for now.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 3.600: [Full GC [PSYoungGen: 5424K-&gt;0K(38208K)] [PSOldGen: 488K-&gt;5786K(87424K)] 5912K-&gt;5786K(125632K) [PSPermGen: 13092K-&gt;13094K(131072K)], 0.0699360 secs]
 * </pre>
 * 
 * <p>
 * 2) Alternate format (Note "Full GC" vs. "Full GC (System)"):
 * </p>
 * 
 * <pre>
 * 4.165: [Full GC (System) [PSYoungGen: 1784K-&gt;0K(12736K)] [PSOldGen: 1081K-&gt;2855K(116544K)] 2865K-&gt;2855K(129280K) [PSPermGen: 8600K-&gt;8600K(131072K)], 0.0427680 secs]
 * </pre>
 * 
 * TODO: Expand or extend {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent}.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class ParallelSerialOldEvent implements BlockingEvent, OldCollection, PermCollection,
		YoungData, OldData, PermData {

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
	 * Young generation size (kilobytes) at beginning of GC event.
	 */
	private int young;

	/**
	 * Young generation size (kilobytes) at end of GC event.
	 */
	private int youngEnd;

	/**
	 * Available space in young generation (kilobytes). Equals young generation allocation minus one
	 * survivor space.
	 */
	private int youngAvailable;

	/**
	 * Old generation size (kilobytes) at beginning of GC event.
	 */
	private int old;

	/**
	 * Old generation size (kilobytes) at end of GC event.
	 */
	private int oldEnd;

	/**
	 * Space allocated to old generation (kilobytes).
	 */
	private int oldAllocation;

	/**
	 * Permanent generation size (kilobytes) at beginning of GC event.
	 */
	private int permGen;

	/**
	 * Permanent generation size (kilobytes) at end of GC event.
	 */
	private int permGenEnd;

	/**
	 * Space allocated to permanent generation (kilobytes).
	 */
	private int permGenAllocation;

	/**
	 * Regular expressions defining the logging.
	 */
	private static final String REGEX = "^" + JdkRegEx.TIMESTAMP
			+ ": \\[(Full GC|Full GC \\(System\\)) \\[PSYoungGen: " + JdkRegEx.SIZE + "->"
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] \\[PSOldGen: " + JdkRegEx.SIZE + "->"
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] " + JdkRegEx.SIZE + "->"
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) \\[PSPermGen: " + JdkRegEx.SIZE + "->"
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\], " + JdkRegEx.DURATION + "\\]"
			+ JdkRegEx.TIMES_BLOCK + "?[ ]*$";

	/**
	 * Create parallel old detail logging event from log entry.
	 */
	public ParallelSerialOldEvent(String logEntry) {
		this.logEntry = logEntry;
		Pattern pattern = Pattern.compile(ParallelSerialOldEvent.REGEX);
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                        young = Integer.parseInt(matcher.group(3)) ;
			youngEnd = Integer.parseInt(matcher.group(4)) ;
			youngAvailable = Integer.parseInt(matcher.group(5)) ;
			old = Integer.parseInt(matcher.group(6)) ;
			oldEnd = Integer.parseInt(matcher.group(7)) ;
			oldAllocation = Integer.parseInt(matcher.group(8)) ;
			// Do not need total begin/end/allocation, as these can be calculated.
			permGen = Integer.parseInt(matcher.group(12)) ;
			permGenEnd = Integer.parseInt(matcher.group(13)) ;
			permGenAllocation = Integer.parseInt(matcher.group(14)) ;
			duration = JdkMath.convertSecsToMillis(matcher.group(15)).intValue();
		}
	}
 
	/**
	 * Alternate constructor. Create parallel old detail logging event from values.
	 * 
	 * @param logEntry
	 * @param timestamp
	 * @param duration
	 */
	public ParallelSerialOldEvent(String logEntry, long timestamp, int duration) {
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

	public int getYoungOccupancyInit() {
		return young;
	}

	public int getYoungOccupancyEnd() {
		return youngEnd;
	}

	public int getYoungSpace() {
		return youngAvailable;
	}

	public int getOldOccupancyInit() {
		return old;
	}

	public int getOldOccupancyEnd() {
		return oldEnd;
	}

	public int getOldSpace() {
		return oldAllocation;
	}

	public String getName() {
		return JdkUtil.LogEventType.PARALLEL_SERIAL_OLD.toString();
	}

	public int getPermOccupancyInit() {
		return permGen;
	}

	public int getPermOccupancyEnd() {
		return permGenEnd;
	}

	public int getPermSpace() {
		return permGenAllocation;
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
