/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.github.joa.domain.GarbageCollector;

/**
 * <p>
 * CMS_INITIAL_MARK
 * </p>
 * 
 * <p>
 * A stop-the-world phase of the concurrent low pause collector that identifies the initial set of live objects directly
 * reachable from GC roots. This event does not do any garbage collection, only marking of objects.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 251.763: [GC [1 CMS-initial-mark: 4133273K(8218240K)] 4150346K(8367360K), 0.0174433 secs]
 * </pre>
 * 
 * <p>
 * 2) JDK8 with trigger:
 * </p>
 * 
 * <pre>
 * 8.722: [GC (CMS Initial Mark) [1 CMS-initial-mark: 0K(989632K)] 187663K(1986432K), 0.0157899 secs] [Times: user=0.06 sys=0.00, real=0.02 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class CmsInitialMarkEvent extends CmsCollector implements BlockingEvent, TriggerData, ParallelEvent, TimesData {

    /**
     * Regular expressions defining the logging JDK8 and prior.
     */
    private static final String _REGEX = "^" + JdkRegEx.DECORATOR + " \\[GC (\\(("
            + GcTrigger.CMS_INITIAL_MARK.getRegex() + ")\\) )?\\[1 CMS-initial-mark: " + JdkRegEx.SIZE_K + "\\("
            + JdkRegEx.SIZE_K + "\\)\\] " + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), " + JdkRegEx.DURATION
            + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern pattern = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return pattern.matcher(logLine).matches();
    }

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long duration;

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The wall (clock) time in centiseconds.
     */
    private int timeReal = TimesData.NO_DATA;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * The time of all system (kernel) threads added together in centiseconds.
     */
    private int timeSys = TimesData.NO_DATA;

    /**
     * The time of all user (non-kernel) threads added together in centiseconds.
     */
    private int timeUser = TimesData.NO_DATA;

    /**
     * The trigger for the GC event.
     */
    private GcTrigger trigger;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public CmsInitialMarkEvent(String logEntry) {
        this.logEntry = logEntry;
        if (match(logEntry)) {
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(13) != null && matcher.group(13).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                } else if (matcher.group(1).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(1));
                }
                trigger = GcTrigger.getTrigger(matcher.group(15));
                duration = JdkMath.convertSecsToMicros(matcher.group(20)).intValue();
                if (matcher.group(23) != null) {
                    timeUser = JdkMath.convertSecsToCentis(matcher.group(24)).intValue();
                    timeSys = JdkMath.convertSecsToCentis(matcher.group(25)).intValue();
                    timeReal = JdkMath.convertSecsToCentis(matcher.group(26)).intValue();
                }
            }
        }
    }

    /**
     * Alternate constructor. Create detail logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public CmsInitialMarkEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.CMS;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.CMS_INITIAL_MARK.toString();
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

    public int getTimeReal() {
        return timeReal;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getTimeSys() {
        return timeSys;
    }

    public int getTimeUser() {
        return timeUser;
    }

    public GcTrigger getTrigger() {
        return trigger;
    }
}
