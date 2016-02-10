/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2012 Red Hat, Inc.                                      *
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
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_YOUNG_INITIAL_MARK
 * </p>
 * 
 * <p>
 * G1 collector young generation initial marking. 
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 1.305: [GC pause (young) (initial-mark) 102M->24M(512M), 0.0254200 secs]
 * </pre>
 * 
 * <p>
 * 2) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [GC pause (young) (initial-mark) 102M->24M(512M), 0.0254200 secs]
 * </pre>
 * 
 * <p>
 * 3) Preprocessed:
 * </p>
 * 
 * <pre>
 * 2970.268: [GC pause (G1 Evacuation Pause) (young) (initial-mark), 0.0698627 secs] 13926M->13824M(30720M) [Times: user=0.28 sys=0.00, real=0.08 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1YoungInitialMarkEvent implements BlockingEvent, CombinedData, TriggerData {
    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[GC pause \\(young\\) \\(initial-mark\\) " + JdkRegEx.SIZE_G1 + "->" + JdkRegEx.SIZE_G1 + "\\("
            + JdkRegEx.SIZE_G1 + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";
    
    /**
     * Regular expression preprocessed.
     */
    private static final String REGEX_PREPROCESSED = "^" + JdkRegEx.TIMESTAMP + ": \\[GC pause (\\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + ")\\) )?\\(young\\) \\(initial-mark\\), " + JdkRegEx.DURATION
            + "\\] " + JdkRegEx.SIZE_G1 + "->" + JdkRegEx.SIZE_G1 + "\\(" + JdkRegEx.SIZE_G1 + "\\)"
            + JdkRegEx.TIMES_BLOCK + "?[ ]*$";
    
    /**
     * Pattern standard format.
     */
    private static final Pattern pattern = Pattern.compile(REGEX);

    /**
     * Pattern preprocessed.
     */
    private static final Pattern patternPreprocessed = Pattern.compile(REGEX_PREPROCESSED);

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
     * Combined generation size (kilobytes) at beginning of GC event.
     */
    private int combined;

    /**
     * Combined generation size (kilobytes) at end of GC event.
     */
    private int combinedEnd;

    /**
     * Available space in multiple generation (kilobytes).
     */
    private int combinedAvailable;
    
    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Create detail logging event from log entry.
     */
    public G1YoungInitialMarkEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            // standard format
            timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
            combined = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(13)), matcher.group(14).charAt(0));
            combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(15)), matcher.group(16).charAt(0));
            combinedAvailable = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(17)), matcher.group(18).charAt(0));
            duration = JdkMath.convertSecsToMillis(matcher.group(19)).intValue();
        } else {
            // preprocessed format
            matcher = patternPreprocessed.matcher(logEntry);
            if (matcher.find()) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                trigger = matcher.group(3);
                combined = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(5)), matcher.group(6).charAt(0));
                combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(7)), matcher.group(8).charAt(0));
                combinedAvailable = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(9)),
                        matcher.group(10).charAt(0));
                duration = JdkMath.convertSecsToMillis(matcher.group(4)).intValue();
            }
        }
    }

    /**
     * Alternate constructor. Create detail logging event from values.
     * 
     * @param logEntry
     * @param timestamp
     * @param duration
     */
    public G1YoungInitialMarkEvent(String logEntry, long timestamp, int duration) {
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

    public int getCombinedOccupancyInit() {
        return combined;
    }

    public int getCombinedOccupancyEnd() {
        return combinedEnd;
    }

    public int getCombinedSpace() {
        return combinedAvailable;
    }

    public String getName() {
        return JdkUtil.LogEventType.G1_YOUNG_INITIAL_MARK.toString();
    }
    
    public String getTrigger() {
        return trigger;
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX) || logLine.matches(REGEX_PREPROCESSED);
    }
}
