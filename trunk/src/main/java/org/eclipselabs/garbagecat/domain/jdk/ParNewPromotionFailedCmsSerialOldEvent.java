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
 * PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD
 * </p>
 * 
 * <p>
 * Combined {@link org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedEvent} and
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent}. Occurs when objects cannot be
 * moved from the young to the old generation due to lack of space or fragmentation. The young
 * generation collection backs out of the young collection and initiates a
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
 * <p>
 * 1) Standard logging:
 * </p>
 * 
 * <pre>
 * 1181.943: [GC 1181.943: [ParNew (promotion failed): 145542K-&gt;142287K(149120K), 0.1316193 secs]1182.075: [CMS: 6656483K-&gt;548489K(8218240K), 9.1244297 secs] 6797120K-&gt;548489K(8367360K), 9.2564476 secs]
 * </pre>
 * 
 * <p>
 * 2) With "promotion failed" missing:
 * </p>
 * 
 * <pre>
 * 3546.690: [GC 3546.691: [ParNew: 532480K-&gt;532480K(599040K), 0.0000400 secs]3546.691: [CMS: 887439K-&gt;893801K(907264K), 9.6413020 secs] 1419919K-&gt;893801K(1506304K), 9.6419180 secs]
 * </pre>
 * 
 * <p>
 * 3) With "Tenured" instead of "CMS" in old generation block:
 * </p>
 * 
 * <pre>
 * 289985.117: [GC 289985.117: [ParNew (promotion failed): 144192K-&gt;144192K(144192K), 0.1347360 secs]289985.252: [Tenured: 1281600K-&gt;978341K(1281600K), 3.6577930 secs] 1409528K-&gt;978341K(1425792K), 3.7930200 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 */
public class ParNewPromotionFailedCmsSerialOldEvent implements BlockingEvent, OldCollection,
		PermCollection, YoungData, OldData {

	/**
	 * Regular expressions defining the logging.
	 */
	private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP
			+ ": \\[ParNew( \\(promotion failed\\))?: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
			+ "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
			+ ": \\[(CMS|Tenured): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
			+ "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";
        private static Pattern pattern = Pattern.compile(ParNewPromotionFailedCmsSerialOldEvent.REGEX);

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
	public ParNewPromotionFailedCmsSerialOldEvent(String logEntry) {
		this.logEntry = logEntry;
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			old = Integer.parseInt(matcher.group(10)) ;
			oldEnd = Integer.parseInt(matcher.group(11)) ;
			oldAllocation = Integer.parseInt(matcher.group(12)) ;
			int totalBegin = Integer.parseInt(matcher.group(14)) ;
			// Don't use ParNew values because those are presumably sbefore the promotion failure.
			young = totalBegin - old;
			int totalEnd = Integer.parseInt(matcher.group(15)) ;
			youngEnd = totalEnd - oldEnd;
			int totalAllocation = Integer.parseInt(matcher.group(16)) ;
			youngAvailable = totalAllocation - oldAllocation;
			duration = JdkMath.convertSecsToMillis(matcher.group(17)).intValue();
		}
	}

	/**
	 * Alternate constructor. Create ParNew detail logging event from values.
	 * 
	 * @param logEntry
	 * @param timestamp
	 * @param duration
	 */
	public ParNewPromotionFailedCmsSerialOldEvent(String logEntry, long timestamp, int duration) {
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
		return JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD.toString();
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
