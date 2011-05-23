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
 * PAR_NEW_CONCURRENT_MODE_FAILURE
 * </p>
 * 
 * <p>
 * A {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} with a concurrent mode failure.
 * </p>
 * 
 * A concurrent mode failure event indicates that the concurrent collection of the old generation
 * did not finish before the old generation became full. The JVM initiates a full GC in an attempt
 * to free up space using the <code>CmsSerialOldEvent</code> old generation collector.
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
 * 1) Single line:
 * </p>
 * 
 * <pre>
 * 26683.209: [GC 26683.210: [ParNew: 261760K-&gt;261760K(261952K), 0.0000130 secs]26683.210: [CMS (concurrent mode failure): 1141548K-&gt;1078465K(1179648K), 7.3835370 secs] 1403308K-&gt;1078465K(1441600K), 7.3838390 secs]
 * </pre>
 * 
 * <p>
 * 2) Split into 2 lines then combined as 1 line by
 * {@link org.eclipselabs.garbagecat.preprocess.jdk.CmsConcurrentModeFailurePreprocessAction}.
 * Balanced brackets with additional CMS-concurrent block:
 * </p>
 * 
 * <pre>
 * 52.820: [GC 52.822: [ParNew: 966519K-&gt;96048K(1100288K), 0.5364770 secs] 966519K-&gt;96048K(4037120K), 0.5377870 secs] (concurrent mode failure): 844276K-&gt;399360K(907264K), 8.1624950 secs] 1441025K-&gt;399360K(1506304K), 11.7996390 secs] [Times: user=8.61 sys=0.04, real=11.80 secs]
 * </pre>
 * 
 * <p>
 * 3) With CMS-concurrent-abortable-preclean:
 * </p>
 * 
 * <pre>
 * 27067.966: [GC 27067.966: [ParNew: 261760K-&gt;261760K(261952K), 0.0000160 secs]27067.966: [CMS27067.966: [CMS-concurrent-abortable-preclean: 2.272/29.793 secs] (concurrent mode failure): 1147900K-&gt;1155037K(1179648K), 7.3953900 secs] 1409660K-&gt;1155037K(1441600K), 7.3957620 secs]
 * </pre>
 * 
 * <p>
 * 4) With CMS-concurrent-mark:
 * </p>
 * 
 * <pre>
 * 27636.893: [GC 27636.893: [ParNew: 261760K-&gt;261760K(261952K), 0.0000130 secs]27636.893: [CMS27639.231: [CMS-concurrent-mark: 4.803/4.803 secs] (concurrent mode failure): 1150993K-&gt;1147420K(1179648K), 9.9779890 secs] 1412753K-&gt;1147420K(1441600K), 9.9783140 secs]
 * </pre>
 * 
 * 
 * <p>
 * 5) In incremental mode (<code>-XX:+CMSIncrementalMode</code>):
 * </p>
 * 
 * <pre>
 * 5075.405: [GC 5075.405: [ParNew: 261760K-&gt;261760K(261952K), 0.0000750 secs]5075.405: [CMS5081.144: [CMS-concurrent-preclean: 14.653/31.189 secs] (concurrent mode failure): 1796901K-&gt;1078231K(1835008K), 96.6130290 secs] 2058661K-&gt;1078231K(2096960K) icms_dc=100 , 96.6140400 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 */
public class ParNewConcurrentModeFailureEvent implements BlockingEvent, OldCollection,
		PermCollection, YoungData, OldData {

	/**
	 * Regular expressions defining the logging.
	 */
	private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP
			+ ": \\[ParNew: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
			+ "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP + ": \\[CMS("
			+ JdkRegEx.TIMESTAMP
			+ ": \\[CMS-concurrent-(abortable-preclean|mark|preclean|reset|sweep): "
			+ JdkRegEx.DURATION_FRACTION + "\\])? \\(concurrent mode failure\\): " + JdkRegEx.SIZE
			+ "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\] "
			+ JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)"
			+ JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK
			+ "?[ ]*$";
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
	public ParNewConcurrentModeFailureEvent(String logEntry) {
		this.logEntry = logEntry;		
		Matcher matcher = pattern.matcher(logEntry);
		if (matcher.find()) {
			timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
			old = Integer.parseInt(matcher.group(12)) ;
			oldEnd = Integer.parseInt(matcher.group(13)) ;
			oldAllocation = Integer.parseInt(matcher.group(14)) ;
			int totalBegin = Integer.parseInt(matcher.group(16)) ;
			young = totalBegin - old;
			int totalEnd = Integer.parseInt(matcher.group(17)) ;
			youngEnd = totalEnd - oldEnd;
			int totalAllocation = Integer.parseInt(matcher.group(18)) ;
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
	public ParNewConcurrentModeFailureEvent(String logEntry, long timestamp, int duration) {
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
		return JdkUtil.LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE.toString();
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
