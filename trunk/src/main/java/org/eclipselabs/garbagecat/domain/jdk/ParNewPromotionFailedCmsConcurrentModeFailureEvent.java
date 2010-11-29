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
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE
 * </p>
 * 
 * <p>
 * Combined {@link org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedEvent} and
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewConcurrentModeFailureEvent}. This appears to
 * be a result of a chain reaction where the
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedEvent} initiates a
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} which leads to a
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewConcurrentModeFailureEvent}.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Single line:
 * </p>
 * 
 * <pre>
 * 25281.015: [GC 25281.015: [ParNew (promotion failed): 261760K-&gt;261760K(261952K), 0.1785000 secs]25281.193: [CMS (concurrent mode failure): 1048384K-&gt;1015603K(1179648K), 7.6767910 secs] 1292923K-&gt;1015603K(1441600K), 7.8557660 secs]
 * </pre>
 * 
 * <p>
 * 2) Split into 2 lines then combined as 1 line by
 * {@link org.eclipselabs.garbagecat.preprocess.jdk.CmsConcurrentModeFailurePreprocessAction} with
 * CMS-concurrent-abortable-preclean:
 * </p>
 * 
 * <pre>
 * 233333.318: [GC 233333.319: [ParNew (promotion failed): 673108K-&gt;673108K(707840K), 1.5366054 secs]233334.855: [CMS233334.856: [CMS-concurrent-abortable-preclean: 12.033/27.431 secs] (concurrent mode failure): 1125100K-&gt;1156809K(1310720K), 36.8003032 secs] 1791073K-&gt;1156809K(2018560K), 38.3378201 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 */
public class ParNewPromotionFailedCmsConcurrentModeFailureEvent implements BlockingEvent,
		OldCollection, PermCollection, YoungData, OldData {

	/**
	 * Regular expressions defining the logging.
	 */
	private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP
			+ ": \\[ParNew \\(promotion failed\\): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
			+ ": \\[CMS(" + JdkRegEx.TIMESTAMP
			+ ": \\[CMS-concurrent-(abortable-preclean|mark|preclean|sweep): "
			+ JdkRegEx.DURATION_FRACTION + "\\])? \\(concurrent mode failure\\): " + JdkRegEx.SIZE
			+ "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\] "
			+ JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)"
			+ JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK
			+ "?[ ]*$";

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
	 * Create ParNew detail logging event from log entry.
	 */
	public ParNewPromotionFailedCmsConcurrentModeFailureEvent(String logEntry) {
		this.logEntry = logEntry;
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			old = new Integer(matcher.group(12)).intValue();
			oldEnd = new Integer(matcher.group(13)).intValue();
			oldAllocation = new Integer(matcher.group(14)).intValue();
			int totalBegin = new Integer(matcher.group(16)).intValue();
			young = totalBegin - old;
			int totalEnd = new Integer(matcher.group(17)).intValue();
			youngEnd = totalEnd - oldEnd;
			int totalAllocation = new Integer(matcher.group(18)).intValue();
			youngAvailable = totalAllocation - oldAllocation;
			duration = JdkMath.convertSecsToMillis(matcher.group(20)).intValue();
		}
	}

	/**
	 * Alternate constructor. Create ParNew detail logging event from values.
	 * 
	 * @param logEntry
	 * @param timestamp
	 * @param duration
	 */
	public ParNewPromotionFailedCmsConcurrentModeFailureEvent(String logEntry, long timestamp,
			int duration) {
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
		return JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE.toString();
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
