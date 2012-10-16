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
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1
 * </p>
 * 
 * <p>
 * Young generation collector used when <code>-XX:+UseG1GC</code> JVM option specified.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 1.557: [GC pause (young) 30M-&gt;2128K(59M), 0.0006970 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class G1YoungEvent implements BlockingEvent, YoungCollection, YoungData {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC pause \\(young\\) " + JdkRegEx.SIZE_JDK7
            + "->" + JdkRegEx.SIZE_JDK7 + "\\(" + JdkRegEx.SIZE_JDK7 + "\\), " + JdkRegEx.DURATION + "\\]"
            + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    private static final Pattern pattern = Pattern.compile(G1YoungEvent.REGEX);
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
     * Create G1 detail logging event from log entry.
     */
    public G1YoungEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            young = Integer.parseInt(matcher.group(2));
            if (matcher.group(3).equals(JdkRegEx.MEGABYTES)) {
                young = young * 1024;
            }
            youngEnd = Integer.parseInt(matcher.group(4));
            if (matcher.group(5).equals(JdkRegEx.MEGABYTES)) {
                youngEnd = youngEnd * 1024;
            }
            youngAvailable = Integer.parseInt(matcher.group(6));
            if (matcher.group(7).equals(JdkRegEx.MEGABYTES)) {
                youngAvailable = youngAvailable * 1024;
            }
            duration = JdkMath.convertSecsToMillis(matcher.group(8)).intValue();
        }
    }

    /**
     * Alternate constructor. Create G1 detail logging event from values.
     * 
     * @param logEntry
     * @param timestamp
     * @param duration
     */
    public G1YoungEvent(String logEntry, long timestamp, int duration) {
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

    public String getName() {
        return JdkUtil.LogEventType.G1_YOUNG.toString();
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
