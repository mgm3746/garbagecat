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
 * CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE
 * </p>
 * 
 * <p>
 * A concurrent mode failure event indicates that the concurrent collection of the old generation
 * did not finish before the old generation became full. The JVM initiates a full GC in an attempt
 * to free up space using the {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent} old
 * generation collector.
 * </p>
 * 
 * <p>
 * The concurrent low pause collector measures the rate at which the the old generation is filling
 * and the amount of time between collections and uses this historical data to calculate when to
 * start the concurrent collection (plus adds some padding) so that it will finish just in time
 * before the old generation becomes full.
 * </p>
 * 
 * <p>
 * This happens because there is not enough space in the old generation to support the rate of
 * promotion from the young generation. Possible causes:
 * <ol>
 * <li>The heap is too small.</li>
 * <li>There is a change in application behavior (e.g. a load increase) that causes the young
 * promotion rate to exceed historical data. If this is the case, the concurrent mode failures will
 * happen near the change in behavior, then after a few collections the CMS collector will adjust
 * based on the new promotion rate. Performance will suffer for a short period until the CMS
 * collector recalibrates. The <code>-XX:CMSInitiatingOccupancyFraction=NN</code> (default 92) JVM
 * option can be used to handle changes in application behavior; however, the tradeoff is that there
 * will be more collections.</li>
 * <li>The application has large variances in object allocation rates, causing large variances in
 * young generation promotion rates, leading to the CMS collector not being able to accurately
 * predict the time between collections. The <code>-XX:CMSIncrementalSafetyFactor=NN</code> (default
 * 10) JVM option can be used to start the concurrent collection NN% sooner than the calculated
 * time.</li>
 * <li>There is premature promotion from the young to the old generation, causing the old generation
 * to fill up with short-lived objects. The default value for <code>-XX:MaxTenuringThreshold</code>
 * for the CMS collector is 0, meaning that objects surviving a young collection are immediately
 * promoted to the old generation. Add the following JVM option to allow more time for objects to
 * expire in the young generation: <code>-XX:MaxTenuringThreshold=32</code>.</li>
 * <li>If the old generation has available space, the cause is likely fragmentation. Fragmentation
 * can be avoided by: (1) increasing the heap size, (2) enabling large pages support with large
 * heaps (> 2GB) with the following JVM options:
 * <code>-XX:+UseLargePages XX:LargePageSizeInBytes=64k</code>, (3) on Windows <a
 * href="http://www.microsoft.com/whdc/system/platform/server/PAE/PAEmem.mspx">enable the 3GB switch
 * in boot.ini</a> to extend address space from 2GB to 3GB (<a
 * href="http://java.sun.com/docs/hotspot/HotSpotFAQ.html#gc_heap_32bit">not supported starting JDK
 * 1.6</a>).</li>
 * </ol>
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Logging on single line where some logging appears to be missing, as evidenced by an extra
 * right bracket:
 * </p>
 * 
 * <pre>
 * 28282.075: [Full GC 28282.075 (concurrent mode failure): 1179601K-&gt;1179648K(1179648K), 10.7510650 secs] 1441361K-&gt;1180553K(1441600K), [CMS Perm : 71172K-&gt;71171K(262144K)], 10.7515460 secs]
 * </pre>
 * 
 * <p>
 * 2) Logging on single line with balanced brackets:
 * </p>
 * 
 * <pre>
 * 6942.991: [Full GC 6942.991: [CMS (concurrent mode failure): 907264K-&gt;907262K(907264K), 11.8579830 secs] 1506304K-&gt;1202006K(1506304K), [CMS Perm : 92801K-&gt;92800K(157352K)], 11.8585290 secs] [Times: user=11.80 sys=0.06, real=11.85 secs]
 * </pre>
 * 
 * <p>
 * 3) Logging combined as 1 line by
 * {@link org.eclipselabs.garbagecat.preprocess.jdk.CmsConcurrentModeFailurePreprocessAction}.
 * Balanced brackets with additional CMS-concurrent block:
 * </p>
 * 
 * <pre>
 * 85238.030: [Full GC 85238.030: [CMS85238.672: [CMS-concurrent-mark: 0.666/0.686 secs] (concurrent mode failure): 439328K-&gt;439609K(4023936K), 2.7153820 secs] 448884K-&gt;439609K(4177280K), [CMS Perm : 262143K-&gt;262143K(262144K)], 2.7156150 secs] [Times: user=3.35 sys=0.00, real=2.72 secs]
 * </pre>
 * 
 * <p>
 * 4) Logging combined as 1 line by
 * {@link org.eclipselabs.garbagecat.preprocess.jdk.CmsConcurrentModeFailurePreprocessAction} with
 * detailed CMS events:
 * </p>
 * 
 * <pre>
 * 85217.903: [Full GC 85217.903: [CMS85217.919: [CMS-concurrent-abortable-preclean: 0.723/3.756 secs] (concurrent mode failure) (concurrent mode failure)[YG occupancy: 33620K (153344K)]85217.919: [Rescan (parallel) , 0.0116680 secs]85217.931: [weak refs processing, 0.0167100 secs]85217.948: [class unloading, 0.0571300 secs]85218.005: [scrub symbol &amp; string tables, 0.0291210 secs]: 423728K-&gt;423633K(4023936K), 0.5165330 secs] 457349K-&gt;457254K(4177280K), [CMS Perm : 260428K-&gt;260406K(262144K)], 0.5167600 secs] [Times: user=0.55 sys=0.01, real=0.52 secs]
 * </pre>
 * 
 * <p>
 * Split into 2 lines then combined as 1 line by
 * {@link org.eclipselabs.garbagecat.preprocess.jdk.CmsConcurrentModeFailurePreprocessAction} with
 * concurrent mode failure missing:
 * </p>
 * 
 * <pre>
 * 198.712: [Full GC 198.712: [CMS198.733: [CMS-concurrent-reset: 0.061/1.405 secs]: 14037K-&gt;31492K(1835008K), 0.7953140 secs] 210074K-&gt;31492K(2096960K), [CMS Perm : 27817K-&gt;27784K(131072K)], 0.7955670 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 */
public class CmsSerialOldConcurrentModeFailureEvent implements BlockingEvent, OldCollection,
		PermCollection, YoungData, OldData, PermData {

	/**
	 * Regular expressions defining the logging.
	 */
	private static final String REGEX = "^"
			+ JdkRegEx.TIMESTAMP
			+ ": \\[Full GC (\\(System\\) )?"
			+ JdkRegEx.TIMESTAMP
			+ "(: \\[CMS)?( CMS: abort preclean due to time )?("
			+ JdkRegEx.TIMESTAMP
			+ ": \\[CMS-concurrent-(abortable-preclean|mark|preclean|reset|sweep): "
			+ JdkRegEx.DURATION_FRACTION
			+ "\\])?( \\(concurrent mode (failure|interrupted)\\))?( \\(concurrent mode failure\\)\\"
			+ "[YG occupancy: " + JdkRegEx.SIZE + " \\(" + JdkRegEx.SIZE + "\\)\\]"
			+ JdkRegEx.TIMESTAMP + ": \\[Rescan \\(parallel\\) , " + JdkRegEx.DURATION + "\\]"
			+ JdkRegEx.TIMESTAMP + ": \\[weak refs processing, " + JdkRegEx.DURATION + "\\]"
			+ JdkRegEx.TIMESTAMP + ": \\[class unloading, " + JdkRegEx.DURATION + "\\]"
			+ JdkRegEx.TIMESTAMP + ": \\[scrub symbol & string tables, " + JdkRegEx.DURATION
			+ "\\])?: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), "
			+ JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\), \\[CMS Perm : " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
			+ JdkRegEx.SIZE + "\\)\\]" + JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION + "\\]"
			+ JdkRegEx.TIMES_BLOCK + "?[ ]*$";

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
	 * Create ParNew detail logging event from log entry.
	 */
	public CmsSerialOldConcurrentModeFailureEvent(String logEntry) {
		this.logEntry = logEntry;
		Pattern pattern = Pattern.compile(REGEX);
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			old = Integer.parseInt(matcher.group(23)) ;
			oldEnd = Integer.parseInt(matcher.group(24)) ;
			oldAllocation = Integer.parseInt(matcher.group(25)) ;
			int totalBegin = Integer.parseInt(matcher.group(27)) ;
			young = totalBegin - old;
			int totalEnd = Integer.parseInt(matcher.group(28)) ;
			youngEnd = totalEnd - oldEnd;
			int totalAllocation = Integer.parseInt(matcher.group(29)) ;
			youngAvailable = totalAllocation - oldAllocation;
			permGen = Integer.parseInt(matcher.group(30)) ;
			permGenEnd = Integer.parseInt(matcher.group(31)) ;
			permGenAllocation = Integer.parseInt(matcher.group(32)) ;
			duration = JdkMath.convertSecsToMillis(matcher.group(34)).intValue();
		}
	}

	/**
	 * Alternate constructor. Create ParNew detail logging event from values.
	 * 
	 * @param logEntry
	 * @param timestamp
	 * @param duration
	 */
	public CmsSerialOldConcurrentModeFailureEvent(String logEntry, long timestamp, int duration) {
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
		return JdkUtil.LogEventType.CMS_SERIAL_OLD_CONCURRENT_MODE_FAILURE.toString();
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
