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
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * PARALLEL_SCAVENGE
 * </p>
 * 
 * <p>
 * Young generation collector used when <code>-XX:+UseParallelGC</code> or <code>-XX:+UseParallelOldGC</code> JVM
 * options specified.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 19810.091: [GC [PSYoungGen: 27808K-&gt;632K(28032K)] 160183K-&gt;133159K(585088K), 0.0225213 secs]
 * </pre>
 * 
 * <p>
 * 2) With 2 dashes after the GC. This seems to happen when the JVM is stressed out doing continuous full GCs:
 * </p>
 * 
 * <pre>
 * 14112.691: [GC-- [PSYoungGen: 313864K-&gt;313864K(326656K)] 879670K-&gt;1012935K(1025728K), 0.9561947 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class ParallelScavengeEvent implements BlockingEvent, YoungCollection, YoungData, OldData {

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
     * Available space in young generation (kilobytes). Equals young generation allocation minus one survivor space.
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
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC(--)? \\[PSYoungGen: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";
    private static final Pattern pattern = Pattern.compile(ParallelScavengeEvent.REGEX);

    /**
     * Create parallel scavenge logging event from log entry.
     */
    public ParallelScavengeEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            young = Integer.parseInt(matcher.group(3));
            youngEnd = Integer.parseInt(matcher.group(4));
            youngAvailable = Integer.parseInt(matcher.group(5));
            int totalBegin = Integer.parseInt(matcher.group(6));
            old = totalBegin - young;
            int totalEnd = Integer.parseInt(matcher.group(7));
            oldEnd = totalEnd - youngEnd;
            int totalAllocation = Integer.parseInt(matcher.group(8));
            oldAllocation = totalAllocation - youngAvailable;
            duration = JdkMath.convertSecsToMillis(matcher.group(9)).intValue();
        }
    }

    /**
     * Alternate constructor. Create parallel scavenge logging event from values.
     * 
     * @param logEntry
     * @param timestamp
     * @param duration
     */
    public ParallelScavengeEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.PARALLEL_SCAVENGE.toString();
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
