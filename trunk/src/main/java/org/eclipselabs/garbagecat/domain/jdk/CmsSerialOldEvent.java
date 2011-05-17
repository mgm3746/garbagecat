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
 * CMS_SERIAL_OLD
 * </p>
 * 
 * <p>
 * The concurrent low pause collector does not compact. When fragmentation becomes an issue a
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent} compacts the heap. Made a separate
 * event for tracking purposes.
 * </p>
 * 
 * <p>
 * It also happens for undetermined reasons, possibly the JVM requires a certain amount of heap or
 * combination of resources that is not being met, and consequently the concurrent low pause
 * collector is not used despite being specified with the <code>-XX:+UseConcMarkSweepGC</code> JVM
 * option.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 5.980: [Full GC 5.980: [CMS: 5589K-&gt;5796K(122880K), 0.0889610 secs] 11695K-&gt;5796K(131072K), [CMS Perm : 13140K-&gt;13124K(131072K)], 0.0891270 secs]
 * </pre>
 * 
 * <p>
 * 2) JDK 1.6 format (Note "Full GC" vs. "Full GC (System)"):
 * </p>
 * 
 * <pre>
 * 2.928: [Full GC (System) 2.929: [CMS: 0K-&gt;6501K(8218240K), 0.2525532 secs] 66502K-&gt;6501K(8367360K), [CMS Perm : 16640K-&gt;16623K(524288K)], 0.2527331 secs]
 * </pre>
 * 
 * TODO: Extend {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent}.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 */
public class CmsSerialOldEvent implements BlockingEvent, OldCollection, PermCollection, YoungData,
		OldData, PermData {

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
			+ ": \\[(Full GC|Full GC \\(System\\)) " + JdkRegEx.TIMESTAMP + ": \\[CMS: "
			+ JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
			+ JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\), \\[CMS Perm : " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\)\\]" + JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION + "\\]"
			+ JdkRegEx.TIMES_BLOCK + "?[ ]*$";

	/**
	 * Create CMS logging event from log entry.
	 */
	public CmsSerialOldEvent(String logEntry) {
		this.logEntry = logEntry;
		Pattern pattern = Pattern.compile(CmsSerialOldEvent.REGEX);
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			old = Integer.parseInt(matcher.group(4)) ;
			oldEnd = Integer.parseInt(matcher.group(5)) ;
			oldAllocation = Integer.parseInt(matcher.group(6)) ;
			int totalBegin = Integer.parseInt(matcher.group(8)) ;
			young = totalBegin - old;
			int totalEnd = Integer.parseInt(matcher.group(9)) ;
			youngEnd = totalEnd - oldEnd;
			int totalAllocation = Integer.parseInt(matcher.group(10)) ;
			youngAvailable = totalAllocation - oldAllocation;
			permGen = Integer.parseInt(matcher.group(11)) ;
			permGenEnd = Integer.parseInt(matcher.group(12)) ;
			permGenAllocation = Integer.parseInt(matcher.group(13)) ;
			duration = JdkMath.convertSecsToMillis(matcher.group(15)).intValue();
		}
	}

	/**
	 * Alternate constructor. Create CMS logging event from values.
	 * 
	 * @param logEntry
	 * @param timestamp
	 * @param duration
	 */
	public CmsSerialOldEvent(String logEntry, long timestamp, int duration) {
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
		return JdkUtil.LogEventType.CMS_SERIAL_OLD.toString();
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
