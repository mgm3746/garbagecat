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
 * PARALLEL_OLD_COMPACTING
 * </p>
 * 
 * <p>
 * New throughput collector introduced in JDK 5 update 6 and significantly enhanced in JDK 6.
 * Enabled with the <code>-XX:+UseParallelOldGC</code> JVM option. I have seen reports saying this
 * is enabled by default in JDK 6, but I don't see that reflected in the logging, so I don't think
 * that is true.
 * </p>
 * 
 * <p>
 * If not specified, full collections are performed single-threaded with the
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelSerialOldEvent} collector.
 * </p>
 * 
 * <p>
 * Performing full collections in parallel results in lower garbage collection overhead and better
 * application performance, particularly for applications with large heaps running on multiprocessor
 * hardware.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 2182.541: [Full GC [PSYoungGen: 1940K-&gt;0K(98560K)] [ParOldGen: 813929K-&gt;422305K(815616K)] 815869K-&gt;422305K(914176K) [PSPermGen: 81960K-&gt;81783K(164352K)], 2.4749181 secs]
 * </pre>
 * 
 * <p>
 * 2) Alternate format (Note "Full GC" vs. "Full GC (System)"):
 * </p>
 * 
 * <pre>
 * 2.417: [Full GC (System) [PSYoungGen: 1788K-&gt;0K(12736K)] [ParOldGen: 1084K-&gt;2843K(116544K)] 2872K-&gt;2843K(129280K) [PSPermGen: 8602K-&gt;8593K(131072K)], 0.1028360 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ParallelOldCompactingEvent implements BlockingEvent, OldCollection, PermCollection,
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
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] \\[ParOldGen: " + JdkRegEx.SIZE
			+ "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] " + JdkRegEx.SIZE + "->"
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) \\[PSPermGen: " + JdkRegEx.SIZE + "->"
			+ JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\], " + JdkRegEx.DURATION + "\\]"
			+ JdkRegEx.TIMES_BLOCK + "?[ ]*$";

	/**
	 * Create parallel old detail logging event from log entry.
	 */
	public ParallelOldCompactingEvent(String logEntry) {
		this.logEntry = logEntry;
		Pattern pattern = Pattern.compile(ParallelOldCompactingEvent.REGEX);
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			young = new Integer(matcher.group(3)).intValue();
			youngEnd = new Integer(matcher.group(4)).intValue();
			youngAvailable = new Integer(matcher.group(5)).intValue();
			old = new Integer(matcher.group(6)).intValue();
			oldEnd = new Integer(matcher.group(7)).intValue();
			oldAllocation = new Integer(matcher.group(8)).intValue();
			// Do not need total begin/end/allocation, as these can be calculated.
			permGen = new Integer(matcher.group(12)).intValue();
			permGenEnd = new Integer(matcher.group(13)).intValue();
			permGenAllocation = new Integer(matcher.group(14)).intValue();
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
	public ParallelOldCompactingEvent(String logEntry, long timestamp, int duration) {
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
		return JdkUtil.LogEventType.PARALLEL_OLD_COMPACTING.toString();
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
