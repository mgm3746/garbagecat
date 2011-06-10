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
 * PAR_NEW_CMS_CONCURRENT
 * </p>
 * 
 * <p>
 * Combined {@link org.eclipselabs.garbagecat.domain.jdk.ParNewEvent} and
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent}.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 2210.281: [GC 2210.282: [ParNew2210.314: [CMS-concurrent-abortable-preclean: 0.043/0.144 secs]: 212981K-&gt;3156K(242304K), 0.0364435 secs] 4712182K-&gt;4502357K(4971420K), 0.0368807 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class ParNewCmsConcurrentEvent implements BlockingEvent, YoungCollection, YoungData, OldData {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP + ": \\[ParNew" + JdkRegEx.TIMESTAMP + ": \\[CMS-concurrent-(abortable-preclean|mark|sweep): "
            + JdkRegEx.DURATION_FRACTION + "\\]: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";
    private static Pattern pattern = Pattern.compile(ParNewCmsConcurrentEvent.REGEX);

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
     * Create ParNew detail logging event from log entry.
     */
    public ParNewCmsConcurrentEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            young = Integer.parseInt(matcher.group(6));
            youngEnd = Integer.parseInt(matcher.group(7));
            youngAvailable = Integer.parseInt(matcher.group(8));
            int totalBegin = Integer.parseInt(matcher.group(10));
            old = totalBegin - young;
            int totalEnd = Integer.parseInt(matcher.group(11));
            oldEnd = totalEnd - youngEnd;
            int totalAllocation = Integer.parseInt(matcher.group(12));
            oldAllocation = totalAllocation - youngAvailable;
            duration = JdkMath.convertSecsToMillis(matcher.group(13)).intValue();
        }
    }

    /**
     * Alternate constructor. Create ParNew detail logging event from values.
     * 
     * @param logEntry
     * @param timestamp
     * @param duration
     */
    public ParNewCmsConcurrentEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.PAR_NEW_CMS_CONCURRENT.toString();
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
