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

import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * CMS_SERIAL_OLD
 * </p>
 * 
 * <p>
 * The concurrent low pause collector does not compact. When fragmentation becomes an issue a
 * {@link org.eclipselabs.garbagecat.domain.jdk.SerialOldEvent} compacts the heap. Made a separate event for tracking
 * purposes.
 * </p>
 * 
 * <p>
 * It also happens for undetermined reasons, possibly the JVM requires a certain amount of heap or combination of
 * resources that is not being met, and consequently the concurrent low pause collector is not used despite being
 * specified with the <code>-XX:+UseConcMarkSweepGC</code> JVM option.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 5.980: [Full GC 5.980: [CMS: 5589K-&gt;5796K(122880K), 0.0889610 secs] 11695K-&gt;5796K(131072K), [CMS Perm : 13140K-&gt;13124K(131072K)], 0.0891270 secs]
 * </pre>
 * 
 * <p>
 * 2) JDK 1.6 format with trigger after "Full GC":
 * </p>
 * 
 * <pre>
 * 2.928: [Full GC (System) 2.929: [CMS: 0K-&gt;6501K(8218240K), 0.2525532 secs] 66502K-&gt;6501K(8367360K), [CMS Perm : 16640K-&gt;16623K(524288K)], 0.2527331 secs]
 * </pre>
 * 
 * <p>
 * 3) After {@link org.eclipselabs.garbagecat.preprocess.jdk.DateStampPrefixPreprocessAction} with no space after Full
 * GC:
 * </p>
 * 
 * <pre>
 * raw:
 * 2013-12-09T16:43:09.366+0000: 1504.625: [Full GC2013-12-09T16:43:09.366+0000: 1504.625: [CMS: 1172695K-&gt;840574K(1549164K), 3.7572507 secs] 1301420K-&gt;840574K(1855852K), [CMS Perm : 226817K-&gt;226813K(376168K)], 3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]
 * 
 * </pre>
 * 
 * <pre>
 * preprocessed:
 * 1504.625: [Full GC1504.625: [CMS: 1172695K-&gt;840574K(1549164K), 3.7572507 secs] 1301420K-&gt;840574K(1855852K), [CMS Perm : 226817K-&gt;226813K(376168K)], 3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]
 * </pre>
 * 
 * <p>
 * 4) After {@link org.eclipselabs.garbagecat.preprocess.jdk.DateStampPrefixPreprocessAction} with trigger after "CMS":
 * </p>
 * 
 * <pre>
 * raw:
 * 2013-12-09T16:43:09.366+0000: 1504.625: [Full GC2013-12-09T16:43:09.366+0000: 1504.625: [CMS: 1172695K-&gt;840574K(1549164K), 3.7572507 secs] 1301420K-&gt;840574K(1855852K), [CMS Perm : 226817K-&gt;226813K(376168K)], 3.7574584 secs] [Times: user=3.74 sys=0.00, real=3.76 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 */
public class CmsSerialOldEvent extends SerialOldEvent implements TriggerData, CmsCollection {

    /**
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[Full GC( )?(\\((" + JdkRegEx.TRIGGER_SYSTEM_GC
            + "|" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|" + JdkRegEx.TRIGGER_HEAP_INSPECTION_INITIATED_GC
            + ")\\) )?" + JdkRegEx.TIMESTAMP + "(: \\[CMS)?(bailing out to foreground collection)?( \\(("
            + JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE + "|" + JdkRegEx.TRIGGER_CONCURRENT_MODE_INTERRUPTED
            + ")\\))?( \\(" + JdkRegEx.TRIGGER_CONCURRENT_MODE_FAILURE + "\\)\\[YG occupancy: " + JdkRegEx.SIZE + " \\("
            + JdkRegEx.SIZE + "\\)\\]" + JdkRegEx.TIMESTAMP + ": \\[Rescan \\(parallel\\) , " + JdkRegEx.DURATION
            + "\\]" + JdkRegEx.TIMESTAMP + ": \\[weak refs processing, " + JdkRegEx.DURATION + "\\]"
            + JdkRegEx.TIMESTAMP + ": \\[class unloading, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP
            + ": \\[scrub symbol & string tables, " + JdkRegEx.DURATION + "\\])?: " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), \\[(CMS Perm |Metaspace): " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\]" + JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION
            + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";

    private static Pattern pattern = Pattern.compile(CmsSerialOldEvent.REGEX);

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public CmsSerialOldEvent(String logEntry) {

        super.setLogEntry(logEntry);
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            super.setTimestamp(JdkMath.convertSecsToMillis(matcher.group(1)).longValue());
            if (matcher.group(10) != null) {
                trigger = matcher.group(10);
            } else {
                trigger = matcher.group(4);
            }
            super.setOldOccupancyInit(Integer.parseInt(matcher.group(22)));
            super.setOldOccupancyEnd(Integer.parseInt(matcher.group(23)));
            super.setOldSpace(Integer.parseInt(matcher.group(24)));
            int totalBegin = Integer.parseInt(matcher.group(26));
            super.setYoungOccupancyInit(totalBegin - super.getOldOccupancyInit());
            int totalEnd = Integer.parseInt(matcher.group(27));
            super.setYoungOccupancyEnd(totalEnd - super.getOldOccupancyEnd());
            int totalAllocation = Integer.parseInt(matcher.group(28));
            super.setYoungSpace(totalAllocation - super.getOldSpace());
            super.setPermOccupancyInit(Integer.parseInt(matcher.group(30)));
            super.setPermOccupancyEnd(Integer.parseInt(matcher.group(31)));
            super.setPermSpace(Integer.parseInt(matcher.group(32)));
            super.setDuration(JdkMath.convertSecsToMillis(matcher.group(34)).intValue());
        }
    }

    /**
     * Alternate constructor. Create CMS logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event happened in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in milliseconds.
     */
    public CmsSerialOldEvent(String logEntry, long timestamp, int duration) {
        super.setLogEntry(logEntry);
        super.setTimestamp(timestamp);
        super.setDuration(duration);
    }

    public String getName() {
        return JdkUtil.LogEventType.CMS_SERIAL_OLD.toString();
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
    public static boolean match(String logLine) {
        return pattern.matcher(logLine).matches();
    }
}
