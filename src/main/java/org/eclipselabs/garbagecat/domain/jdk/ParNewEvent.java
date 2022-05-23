/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * PAR_NEW
 * </p>
 * 
 * <p>
 * Concurrent low pause collector young generation collection. Works the same as
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParallelScavengeEvent}.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 20.189: [GC 20.190: [ParNew: 86199K-&gt;8454K(91712K), 0.0375060 secs] 89399K-&gt;11655K(907328K), 0.0387074 secs]
 * </pre>
 * 
 * <p>
 * 2) With erroneous "Full GC":
 * </p>
 * 
 * <pre>
 * 18934.651: [Full GC 18934.651: [ParNew: 253303K-&gt;7680K(254464K), 0.2377648 secs] 866808K-&gt;648302K(1040896K), 0.2380553 secs]
 * </pre>
 * 
 * <p>
 * 3) With -XX:+PrintGCDateStamps:
 * </p>
 * 
 * <pre>
 * 2010-02-26T08:31:51.990-0600: [GC [ParNew: 150784K-&gt;4291K(169600K), 0.0246670 secs] 150784K-&gt;4291K(1029760K), 0.0247500 secs] [Times: user=0.06 sys=0.01, real=0.02 secs]
 * </pre>
 * 
 * <p>
 * 4) No space after GC:
 * </p>
 * 
 * <pre>
 * 2013-12-09T16:18:17.813+0000: 13.086: [GC2013-12-09T16:18:17.813+0000: 13.086: [ParNew: 272640K-&gt;33532K(306688K), 0.0381419 secs] 272640K-&gt;33532K(1014528K), 0.0383306 secs] [Times: user=0.11 sys=0.02, real=0.04 secs]
 * </pre>
 * 
 * <p>
 * 5) JDK8 with trigger:
 * </p>
 * 
 * <pre>
 * 6.703: [GC (Allocation Failure) 6.703: [ParNew: 886080K-&gt;11485K(996800K), 0.0193349 secs] 886080K-&gt;11485K(1986432K), 0.0198375 secs] [Times: user=0.09 sys=0.01, real=0.02 secs]
 * </pre>
 * 
 * <p>
 * 6) Initiated by -XX:+CMSScavengeBeforeRemark:
 * </p>
 * 
 * <pre>
 * 7236.341: [GC[YG occupancy: 1388745 K (4128768 K)]7236.341: [GC7236.341: [ParNew: 1388745K-&gt;458752K(4128768K), 0.5246295 secs] 2977822K-&gt;2161212K(13172736K), 0.5248785 secs] [Times: user=0.92 sys=0.03, real=0.51 secs]
 * </pre>
 * 
 *
 * <p>
 * 7) CMS Final Remark trigger initiated by -XX:+CMSScavengeBeforeRemark JDK8:
 * </p>
 * 
 * <pre>
 * 4.506: [GC (CMS Final Remark) [YG occupancy: 100369 K (153344 K)]4.506: [GC (CMS Final Remark) 4.506: [ParNew: 100369K-&gt;10116K(153344K), 0.0724021 secs] 100369K-&gt;16685K(4177280K), 0.0724907 secs] [Times: user=0.13 sys=0.01, real=0.07 secs]
 * </pre>
 * 
 *
 * <p>
 * 8) CMS Final Remark trigger initiated by -XX:+CMSScavengeBeforeRemark JDK8 without <code>-XX:+PrintGCDetails</code>:
 * </p>
 * 
 * <pre>
 * 2017-04-03T03:12:02.133-0500: 30.385: [GC (CMS Final Remark) 2017-04-03T03:12:02.134-0500: 30.385: [GC (CMS Final Remark) 890910K-&gt;620060K(7992832K), 0.1223879 secs] 620060K(7992832K), 0.2328529 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class ParNewEvent extends CmsIncrementalModeCollector
        implements BlockingEvent, YoungCollection, ParallelEvent, YoungData, OldData, TriggerData, TimesData {

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_SYSTEM_GC + "|"
            + JdkRegEx.TRIGGER_CMS_FINAL_REMARK + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DECORATOR + " \\[GC( \\(" + JdkRegEx.TRIGGER_CMS_FINAL_REMARK
            + "\\)[ ]{0,1})?(\\[YG occupancy: " + JdkRegEx.SIZE_K + " \\(" + JdkRegEx.SIZE_K + "\\)\\])?)?"
            + JdkRegEx.DECORATOR + " \\[(Full)?[ ]{0,1}GC( )?(\\(" + TRIGGER + "\\))?( )?((" + JdkRegEx.DECORATOR
            + " )?\\[ParNew( \\((" + JdkRegEx.TRIGGER_PROMOTION_FAILED + ")\\))?:)? " + JdkRegEx.SIZE_K + "->"
            + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\), " + JdkRegEx.DURATION + "\\] (" + JdkRegEx.SIZE_K
            + "->)?" + JdkRegEx.SIZE_K + "\\(" + JdkRegEx.SIZE_K + "\\)" + JdkRegEx.ICMS_DC_BLOCK + "?, "
            + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern pattern = Pattern.compile(ParNewEvent.REGEX);
    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The elapsed clock time for the GC event in microseconds (rounded).
     */
    private long duration;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Young generation size at beginning of GC event.
     */
    private Memory young;

    /**
     * Young generation size at end of GC event.
     */
    private Memory youngEnd;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngAvailable;

    /**
     * Old generation size at beginning of GC event.
     */
    private Memory old;

    /**
     * Old generation size at end of GC event.
     */
    private Memory oldEnd;

    /**
     * Space allocated to old generation.
     */
    private Memory oldAllocation;

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * The time of all user (non-kernel) threads added together in centiseconds.
     */
    private int timeUser;

    /**
     * The time of all system (kernel) threads added together in centiseconds.
     */
    private int timeSys;

    /**
     * The wall (clock) time in centiseconds.
     */
    private int timeReal;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ParNewEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            if (matcher.group(14) != null && matcher.group(14).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(14)).longValue();
            } else if (matcher.group(2) != null) {
                if (matcher.group(2).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(2)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(2));
                }
            } else if (matcher.group(31) != null && matcher.group(31).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(31)).longValue();
            } else if (matcher.group(19) != null) {
                if (matcher.group(19).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(19)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(19));
                }
            }
        }
        if (matcher.group(54) != null) {
            trigger = matcher.group(54);
        } else {
            trigger = matcher.group(35);
        }
        young = kilobytes(matcher.group(55));
        youngEnd = kilobytes(matcher.group(56));
        youngAvailable = kilobytes(matcher.group(57));
        oldEnd = kilobytes(matcher.group(63)).minus(youngEnd);
        old = matcher.group(61) == null ? oldEnd : kilobytes(matcher.group(62)).minus(young);
        oldAllocation = kilobytes(matcher.group(64)).minus(youngAvailable);
        duration = JdkMath.convertSecsToMicros(matcher.group(66)).intValue();
        if (matcher.group(65) != null) {
            super.setIncrementalMode(true);
        } else {
            super.setIncrementalMode(false);
        }
        if (matcher.group(69) != null) {
            timeUser = JdkMath.convertSecsToCentis(matcher.group(70)).intValue();
            timeSys = JdkMath.convertSecsToCentis(matcher.group(71)).intValue();
            timeReal = JdkMath.convertSecsToCentis(matcher.group(72)).intValue();
        }
    }

    /**
     * Alternate constructor. Create ParNew detail logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event started in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in microseconds.
     */
    public ParNewEvent(String logEntry, long timestamp, int duration) {
        this.logEntry = logEntry;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getDuration() {
        return duration;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public Memory getYoungOccupancyInit() {
        return young;
    }

    public Memory getYoungOccupancyEnd() {
        return youngEnd;
    }

    public Memory getYoungSpace() {
        return youngAvailable;
    }

    public Memory getOldOccupancyInit() {
        return old;
    }

    public Memory getOldOccupancyEnd() {
        return oldEnd;
    }

    public Memory getOldSpace() {
        return oldAllocation;
    }

    public String getName() {
        return JdkUtil.LogEventType.PAR_NEW.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    public int getTimeUser() {
        return timeUser;
    }

    public int getTimeSys() {
        return timeSys;
    }

    public int getTimeReal() {
        return timeReal;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeSys, timeReal);
    }

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
}
