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
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.YoungCollection;
import org.eclipselabs.garbagecat.domain.YoungData;
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
 * <h3>Example Logging</h3>
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
 * 6) With -XX:+CMSScavengeBeforeRemark:
 * </p>
 * 
 * <pre>
 * 7236.341: [GC[YG occupancy: 1388745 K (4128768 K)]7236.341: [GC7236.341: [ParNew: 1388745K-&gt;458752K(4128768K), 0.5246295 secs] 2977822K-&gt;2161212K(13172736K), 0.5248785 secs] [Times: user=0.92 sys=0.03, real=0.51 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class ParNewEvent extends CmsIncrementalModeCollector
        implements BlockingEvent, YoungCollection, YoungData, OldData, TriggerData {

    /**
     * Trigger(s) regular expression(s).
     */
    private static final String TRIGGER = "(" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|"
            + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + "|" + JdkRegEx.TRIGGER_SYSTEM_GC + ")";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(" + JdkRegEx.DATESTAMP + ": )?(" + JdkRegEx.TIMESTAMP
            + ": \\[GC\\[YG occupancy: " + JdkRegEx.SIZE + " \\(" + JdkRegEx.SIZE + "\\)\\])?(" + JdkRegEx.DATESTAMP
            + ": )?" + JdkRegEx.TIMESTAMP + ": \\[(Full )?GC( )?(\\(" + TRIGGER + "\\))?( )?(" + JdkRegEx.DATESTAMP
            + ": )?(" + JdkRegEx.TIMESTAMP + ": )?\\[ParNew( \\(" + JdkRegEx.TRIGGER_PROMOTION_FAILED + "\\))?: "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\] "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)" + JdkRegEx.ICMS_DC_BLOCK + "?, "
            + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    private static final Pattern pattern = Pattern.compile(ParNewEvent.REGEX);
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
     * The trigger for the GC event.
     */
    private String trigger;

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
            timestamp = JdkMath.convertSecsToMillis(matcher.group(27)).longValue();
            trigger = matcher.group(31);
            young = Integer.parseInt(matcher.group(48));
            youngEnd = Integer.parseInt(matcher.group(49));
            youngAvailable = Integer.parseInt(matcher.group(50));
            int totalBegin = Integer.parseInt(matcher.group(54));
            old = totalBegin - young;
            int totalEnd = Integer.parseInt(matcher.group(55));
            oldEnd = totalEnd - youngEnd;
            int totalAllocation = Integer.parseInt(matcher.group(56));
            oldAllocation = totalAllocation - youngAvailable;
            duration = JdkMath.convertSecsToMillis(matcher.group(58)).intValue();
            if (matcher.group(57) != null) {
                super.setIncrementalMode(true);
            } else {
                super.setIncrementalMode(false);
            }
        }
    }

    /**
     * Alternate constructor. Create ParNew detail logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event happened in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in milliseconds.
     */
    public ParNewEvent(String logEntry, long timestamp, int duration) {
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
        return JdkUtil.LogEventType.PAR_NEW.toString();
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
        return pattern.matcher(logLine).matches();
    }
}
