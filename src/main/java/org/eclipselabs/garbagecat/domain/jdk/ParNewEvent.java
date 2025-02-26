/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import static org.eclipselabs.garbagecat.util.Memory.memory;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

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
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.github.joa.domain.GarbageCollector;

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
     * Trigger(s) regular expression.
     */
    private static final String __TRIGGER = "(" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|" + GcTrigger.SYSTEM_GC.getRegex() + "|"
            + GcTrigger.CMS_FINAL_REMARK.getRegex() + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^(" + JdkRegEx.DECORATOR + " \\[GC( \\("
            + GcTrigger.CMS_FINAL_REMARK.getRegex() + "\\)[ ]{0,1})?(\\[YG occupancy: " + JdkRegEx.SIZE + " \\("
            + JdkRegEx.SIZE + "\\)\\])?)?" + JdkRegEx.DECORATOR + " \\[(Full)?[ ]{0,1}GC( )?(\\(" + __TRIGGER
            + "\\))?( )?((" + JdkRegEx.DECORATOR + " )?\\[ParNew( \\((" + GcTrigger.PROMOTION_FAILED.getRegex()
            + ")\\))?:)? " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION
            + "\\] (" + JdkRegEx.SIZE + "->)?" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)" + JdkRegEx.ICMS_DC_BLOCK
            + "?, " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    private static final Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return PATTERN.matcher(logLine).matches();
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
     * Old generation occupancy at end of GC event.
     */
    private Memory oldOccupancyEnd;

    /**
     * Old generation occupancy at beginning of GC event.
     */
    private Memory oldOccupancyInit;

    /**
     * Space allocated to old generation.
     */
    private Memory oldSpace;

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
     * Young generation occupancy at end of GC event.
     */
    private Memory youngOccupancyEnd;

    /**
     * Young generation occupancy at beginning of GC event.
     */
    private Memory youngOccupancyInit;

    /**
     * Available space in young generation. Equals young generation allocation minus one survivor space.
     */
    private Memory youngSpace;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ParNewEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = PATTERN.matcher(logEntry);
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
            } else if (matcher.group(35) != null && matcher.group(35).matches(JdkRegEx.TIMESTAMP)) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(35)).longValue();
            } else if (matcher.group(23) != null) {
                if (matcher.group(23).matches(JdkRegEx.TIMESTAMP)) {
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(23)).longValue();
                } else {
                    // Datestamp only.
                    timestamp = JdkUtil.convertDatestampToMillis(matcher.group(23));
                }
            }
        }
        if (matcher.group(58) != null) {
            trigger = GcTrigger.getTrigger(matcher.group(58));
        } else if (matcher.group(39) != null) {
            trigger = GcTrigger.getTrigger(matcher.group(39));
        } else {
            trigger = GcTrigger.UNKNOWN;
        }
        youngOccupancyInit = memory(matcher.group(59), matcher.group(61).charAt(0)).convertTo(KILOBYTES);
        youngOccupancyEnd = memory(matcher.group(62), matcher.group(64).charAt(0)).convertTo(KILOBYTES);
        youngSpace = memory(matcher.group(65), matcher.group(67).charAt(0)).convertTo(KILOBYTES);
        oldOccupancyEnd = memory(matcher.group(75), matcher.group(77).charAt(0)).convertTo(KILOBYTES)
                .minus(youngOccupancyEnd);
        oldOccupancyInit = matcher.group(71) == null ? oldOccupancyEnd
                : memory(matcher.group(72), matcher.group(74).charAt(0)).convertTo(KILOBYTES).minus(youngOccupancyInit);
        oldSpace = memory(matcher.group(78), matcher.group(80).charAt(0)).convertTo(KILOBYTES).minus(youngSpace);
        duration = JdkMath.convertSecsToMicros(matcher.group(82)).intValue();
        if (matcher.group(81) != null) {
            super.setIncrementalMode(true);
        } else {
            super.setIncrementalMode(false);
        }
        if (matcher.group(85) != null) {
            timeUser = JdkMath.convertSecsToCentis(matcher.group(86)).intValue();
            timeSys = JdkMath.convertSecsToCentis(matcher.group(87)).intValue();
            timeReal = JdkMath.convertSecsToCentis(matcher.group(88)).intValue();
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

    public long getDurationMicros() {
        return duration;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.PAR_NEW;
    }

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.PAR_NEW;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public Memory getOldOccupancyEnd() {
        return oldOccupancyEnd;
    }

    public Memory getOldOccupancyInit() {
        return oldOccupancyInit;
    }

    public Memory getOldSpace() {
        return oldSpace;
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

    public Memory getYoungOccupancyEnd() {
        return youngOccupancyEnd;
    }

    public Memory getYoungOccupancyInit() {
        return youngOccupancyInit;
    }

    public Memory getYoungSpace() {
        return youngSpace;
    }
}
