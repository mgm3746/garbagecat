/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * G1_YOUNG_PAUSE
 * </p>
 * 
 * <p>
 * G1 young generation collection. Live objects from Eden and Survivor regions are copied to new regions, either to a
 * survivor region or promoted to the old space.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 1.305: [GC pause (young) 102M-&gt;24M(512M), 0.0254200 secs]
 * </pre>
 * 
 * <p>
 * 2) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [GC pause (young) 102M-&gt;24M(512M), 0.0254200 secs]
 * </pre>
 * 
 * <p>
 * 3) After {@link org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction}:
 * </p>
 * 
 * <pre>
 * 
 * 0.807: [GC pause (young), 0.00290200 secs][ 29M-&gt;2589K(59M)] [Times: user=0.01 sys=0.00, real=0.01 secs]
 * </pre>
 * 
 * <p>
 * 3) After {@link org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction} with trigger:
 * </p>
 * 
 * <pre>
 * 2.847: [GC pause (G1 Evacuation Pause) (young), 0.0414530 secs] [Eden: 112.0M(112.0M)-&gt;0.0B(112.0M) Survivors: 16.0M-&gt;16.0M Heap: 136.9M(30.0G)-&gt;70.9M(30.0G)]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author James Livingston
 * 
 */
public class G1YoungPauseEvent extends G1Collector
        implements BlockingEvent, YoungCollection, CombinedData, TriggerData {

    /**
     * Regular expression standard format.
     * 
     * 1.234: [GC pause (young) 102M-&gt;24M(512M), 0.0254200 secs]
     */
    private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC pause (\\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + ")\\) )?\\(young\\) " + JdkRegEx.SIZE_G1 + "->" + JdkRegEx.SIZE_G1
            + "\\(" + JdkRegEx.SIZE_G1 + "\\), " + JdkRegEx.DURATION + "\\]";

    /**
     * Regular expression preprocessed with G1 details.
     * 
     * Trigger before (young):
     * 
     * 2.847: [GC pause (G1 Evacuation Pause) (young), 0.0414530 secs] [Eden: 112.0M(112.0M)->0.0B(112.0M) Survivors:
     * 16.0M->16.0M Heap: 136.9M(30.0G)->70.9M(30.0G)]
     * 
     * Trigger after (young):
     * 
     * 7997.968: [GC pause (young) (to-space exhausted), 0.1208740 secs][Eden: 1280.0M(1280.0M)->0.0B(1288.0M)
     * Survivors: 48.0M->40.0M Heap: 18.9G(26.0G)->17.8G(26.0G)] [Times: user=0.41 sys=0.02, real=0.12 secs]
     * 
     * No trigger:
     * 
     * 44620.073: [GC pause (young), 0.2752700 secs][Eden: 11.3G(11.3G)->0.0B(11.3G) Survivors: 192.0M->176.0M Heap:
     * 23.0G(26.0G)->11.7G(26.0G)] [Times: user=1.09 sys=0.00, real=0.27 secs]
     */
    private static final String REGEX_PREPROCESSED_DETAILS = "^(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[GC pause (\\((" + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + ")\\) )?\\(young\\)( \\((" + JdkRegEx.TRIGGER_TO_SPACE_EXHAUSTED
            + ")\\))?, " + JdkRegEx.DURATION + "\\]\\[Eden: " + JdkRegEx.SIZE_G1 + "\\(" + JdkRegEx.SIZE_G1 + "\\)->"
            + JdkRegEx.SIZE_G1 + "\\(" + JdkRegEx.SIZE_G1 + "\\) Survivors: " + JdkRegEx.SIZE_G1 + "->"
            + JdkRegEx.SIZE_G1 + " Heap: " + JdkRegEx.SIZE_G1 + "\\(" + JdkRegEx.SIZE_G1 + "\\)->" + JdkRegEx.SIZE_G1
            + "\\(" + JdkRegEx.SIZE_G1 + "\\)\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    /**
     * Regular expression preprocessed, no details.
     * 
     * 0.807: [GC pause (young), 0.00290200 secs][ 29M-&gt;2589K(59M)] [Times: user=0.01 sys=0.00, real=0.01 secs]
     * 
     */
    private static final String REGEX_PREPROCESSED = "^" + JdkRegEx.TIMESTAMP + ": \\[GC pause \\(young\\), "
            + JdkRegEx.DURATION + "\\]\\[ " + JdkRegEx.SIZE_G1 + "->" + JdkRegEx.SIZE_G1 + "\\(" + JdkRegEx.SIZE_G1
            + "\\)\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

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
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public G1YoungPauseEvent(String logEntry) {
        this.logEntry = logEntry;
        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                combined = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(4)), matcher.group(6).charAt(0));
                combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(7)), matcher.group(9).charAt(0));
                combinedAvailable = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(10)),
                        matcher.group(12).charAt(0));
                duration = JdkMath.convertSecsToMillis(matcher.group(13)).intValue();
            }
        } else if (logEntry.matches(REGEX_PREPROCESSED_DETAILS)) {
            Pattern pattern = Pattern.compile(REGEX_PREPROCESSED_DETAILS);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(12)).longValue();
                if (matcher.group(16) != null) {
                    // trigger after (young):
                    trigger = matcher.group(16);
                } else {
                    // trigger before (young):
                    trigger = matcher.group(14);
                }
                duration = JdkMath.convertSecsToMillis(matcher.group(17)).intValue();
                combined = JdkMath.convertSizeG1DetailsToKilobytes(matcher.group(38), matcher.group(40).charAt(0));
                combinedEnd = JdkMath.convertSizeG1DetailsToKilobytes(matcher.group(44), matcher.group(46).charAt(0));
                combinedAvailable = JdkMath.convertSizeG1DetailsToKilobytes(matcher.group(47),
                        matcher.group(49).charAt(0));
            }
        } else if (logEntry.matches(REGEX_PREPROCESSED)) {
            Pattern pattern = Pattern.compile(REGEX_PREPROCESSED);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                duration = JdkMath.convertSecsToMillis(matcher.group(2)).intValue();
                combined = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(5)), matcher.group(7).charAt(0));
                combinedEnd = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(8)), matcher.group(10).charAt(0));
                combinedAvailable = JdkMath.calcKilobytes(Integer.parseInt(matcher.group(11)),
                        matcher.group(13).charAt(0));
            }
        }
    }

    /**
     * Alternate constructor. Create detail logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event happened in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in milliseconds.
     */
    public G1YoungPauseEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.G1_YOUNG_PAUSE.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX) || logLine.matches(REGEX_PREPROCESSED_DETAILS)
                || logLine.matches(REGEX_PREPROCESSED);
    }
}
