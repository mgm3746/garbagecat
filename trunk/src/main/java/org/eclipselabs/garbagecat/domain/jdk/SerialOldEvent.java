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
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * SERIAL_OLD
 * </p>
 * 
 * <p>
 * Enabled with the <code>-XX:+UseSerialGC</code> JVM option. Uses a mark-sweep-compact algorithm.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 187.159: [Full GC 187.160: [Tenured: 97171K-&gt;102832K(815616K), 0.6977443 secs] 152213K-&gt;102832K(907328K), [Perm : 49152K-&gt;49154K(49158K)], 0.6929258 secs]
 * </pre>
 * 
 * <p>
 * 2) JDK 1.6 format (Note "Full GC" vs. "Full GC (System)"):
 * </p>
 * 
 * <pre>
 * 2.457: [Full GC (System) 2.457: [Tenured: 1092K-&gt;2866K(116544K), 0.0489980 secs] 11012K-&gt;2866K(129664K), [Perm : 8602K-&gt;8602K(131072K)], 0.0490880 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class SerialOldEvent implements BlockingEvent, YoungCollection, OldCollection,
		PermCollection, YoungData, OldData, PermData {

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
			+ ": \\[(Full GC|Full GC \\(System\\)) " + JdkRegEx.TIMESTAMP + ": \\[Tenured: "
			+ JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
			+ JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\), \\[Perm : " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\)\\], " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK
			+ "?[ ]*$";

	/**
	 * Create serial old detail logging event from log entry.
	 */
	public SerialOldEvent(String logEntry) {
		this.logEntry = logEntry;
		Pattern pattern = Pattern.compile(SerialOldEvent.REGEX);
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			old = Integer.parseInt(matcher.group(4)) ;
			oldEnd = Integer.parseInt(matcher.group(5)) ;
			oldAllocation = Integer.parseInt(matcher.group(6)) ;
			int totalBegin = Integer.parseInt(matcher.group(8)) ;
			young = totalBegin - getOldOccupancyInit();
			int totalEnd = Integer.parseInt(matcher.group(9)) ;
			youngEnd = totalEnd - getOldOccupancyEnd();
			int totalAllocation = Integer.parseInt(matcher.group(10)) ;
			youngAvailable = totalAllocation - getOldSpace();
			// Do not need total begin/end/allocation, as these can be calculated.
			permGen = Integer.parseInt(matcher.group(11)) ;
			permGenEnd = Integer.parseInt(matcher.group(12)) ;
			permGenAllocation = Integer.parseInt(matcher.group(13)) ;
			duration = JdkMath.convertSecsToMillis(matcher.group(14)).intValue();
		}
	}

	/**
	 * Alternate constructor. Create serial old detail logging event from values.
	 * 
	 * @param logEntry
	 * @param timestamp
	 * @param duration
	 */
	public SerialOldEvent(String logEntry, long timestamp, int duration) {
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

	public int getPermOccupancyInit() {
		return permGen;
	}

	public int getPermOccupancyEnd() {
		return permGenEnd;
	}

	public int getPermSpace() {
		return permGenAllocation;
	}

	public String getName() {
		return JdkUtil.LogEventType.SERIAL_OLD.toString();
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
