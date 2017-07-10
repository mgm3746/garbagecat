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
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * CMS_REMARK
 * </p>
 * 
 * <p>
 * The second stop-the-world phase of the concurrent low pause collector. All live objects are marked, starting with the
 * objects identified in the {@link org.eclipselabs.garbagecat.domain.jdk.CmsInitialMarkEvent}. This event does not do
 * any garbage collection. It rescans objects directly reachable from GC roots, processes weak references, and remarks
 * objects. It is actually 3 events, but for GC analysis, it is treated as one event.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Standard format:
 * </p>
 * 
 * <pre>
 * 253.103: [GC[YG occupancy: 16172 K (149120 K)]253.103: [Rescan (parallel) , 0.0226730 secs]253.126: [weak refs processing, 0.0624566 secs] [1 CMS-remark: 4173470K(8218240K)] 4189643K(8367360K), 0.0857010 secs]
 * </pre>
 * 
 * <p>
 * 2) JDK8 with trigger, no space after trigger, and no space after "scrub symbol table":
 * </p>
 * 
 * <pre>
 * 13.749: [GC (CMS Final Remark)[YG occupancy: 149636 K (153600 K)]13.749: [Rescan (parallel) , 0.0216980 secs]13.771: [weak refs processing, 0.0005180 secs]13.772: [scrub string table, 0.0015820 secs] [1 CMS-remark: 217008K(341376K)] 366644K(494976K), 0.0239510 secs] [Times: user=0.18 sys=0.00, real=0.02 secs]
 * </pre>
 * 
 * <p>
 * 4) With datestamp:
 * </p>
 * 
 * <pre>
 * 2016-10-27T19:06:06.651-0400: 6.458: [GC[YG occupancy: 480317 K (5505024 K)]6.458: [Rescan (parallel) , 0.0103480 secs]6.469: [weak refs processing, 0.0000110 secs]6.469: [scrub string table, 0.0001750 secs] [1 CMS-remark: 0K(37748736K)] 480317K(43253760K), 0.0106300 secs] [Times: user=0.23 sys=0.01, real=0.01 secs]
 * </pre>
 * 
 * <p>
 * 5) JDK 1.6 with class unloading:
 * </p>
 * 
 * <pre>
 * 76694.727: [GC[YG occupancy: 80143 K (153344 K)]76694.727: [Rescan (parallel) , 0.0574180 secs]76694.785: [weak refs processing, 0.0170540 secs]76694.802: [class unloading, 0.0363010 secs]76694.838: [scrub symbol &amp; string tables, 0.0276600 secs] [1 CMS-remark: 443542K(4023936K)] 523686K(4177280K), 0.1446880 secs]
 * </pre>
 * 
 * <p>
 * After the collection there was young occupancy 80143K, old occupancy 443542K, and total occupancy 523686K.
 * </p>
 * 
 * <p>
 * 6) JDK 1.7 with class unloading with "scrub symbol table" and "scrub string table" vs.
 * "scrub symbol &amp; string tables":
 * </p>
 * 
 * <pre>
 * 75.500: [GC[YG occupancy: 163958 K (306688 K)]75.500: [Rescan (parallel) , 0.0491823 secs]75.549: [weak refs processing, 0.0088472 secs]75.558: [class unloading, 0.0049468 secs]75.563: [scrub symbol table, 0.0034342 secs]75.566: [scrub string table, 0.0005542 secs] [1 CMS-remark: 378031K(707840K)] 541989K(1014528K), 0.0687411 secs] [Times: user=0.13 sys=0.00, real=0.07 secs]
 * </pre>
 * 
 * <p>
 * 7) JDK8 with class unloading with trigger and no space after "scrub symbol table":
 * </p>
 * 
 * <pre>
 * 13.758: [GC (CMS Final Remark) [YG occupancy: 235489 K (996800 K)]13.758: [Rescan (parallel) , 0.0268664 secs]13.785: [weak refs processing, 0.0000365 secs]13.785: [class unloading, 0.0058936 secs]13.791: [scrub symbol table, 0.0081277 secs]13.799: [scrub string table, 0.0007018 secs][1 CMS-remark: 0K(989632K)] 235489K(1986432K), 0.0430349 secs] [Times: user=0.36 sys=0.00, real=0.04 secs]
 * </pre>
 * 
 * <p>
 * 8) JDK7 with class unloading with trigger with non-parallel rescan: "grey object" and "root" rescans:
 * </p>
 * 
 * <pre>
 * 7.294: [GC[YG occupancy: 42599 K (76672 K)]7.294: [Rescan (non-parallel) 7.294: [grey object rescan, 0.0049340 secs]7.299: [root rescan, 0.0230250 secs], 0.0280700 secs]7.322: [weak refs processing, 0.0001950 secs]7.322: [class unloading, 0.0034660 secs]7.326: [scrub symbol table, 0.0047330 secs]7.330: [scrub string table, 0.0006570 secs] [1 CMS-remark: 7720K(1249088K)] 50319K(1325760K), 0.0375310 secs] [Times: user=0.03 sys=0.01, real=0.03 secs]
 * </pre>
 * 
 * <p>
 * 9) JDK8 with class unloading without the initial GC|YG block:
 * </p>
 * 
 * <pre>
 * 4.578: [Rescan (parallel) , 0.0185521 secs]4.597: [weak refs processing, 0.0008993 secs]4.598: [class unloading, 0.0046742 secs]4.603: [scrub symbol table, 0.0044444 secs]4.607: [scrub string table, 0.0005670 secs][1 CMS-remark: 6569K(4023936K)] 16685K(4177280K), 0.1025102 secs] [Times: user=0.17 sys=0.01, real=0.10 secs]
 * </pre>
 * 
 * <p>
 * 10) JDK7 with class unloading with trigger with non-parallel rescan: "grey object" and "root" rescans:
 * </p>
 * 
 * <pre>
 * 7.294: [GC[YG occupancy: 42599 K (76672 K)]7.294: [Rescan (non-parallel) 7.294: [grey object rescan, 0.0049340 secs]7.299: [root rescan, 0.0230250 secs], 0.0280700 secs]7.322: [weak refs processing, 0.0001950 secs]7.322: [class unloading, 0.0034660 secs]7.326: [scrub symbol table, 0.0047330 secs]7.330: [scrub string table, 0.0006570 secs] [1 CMS-remark: 7720K(1249088K)] 50319K(1325760K), 0.0375310 secs] [Times: user=0.03 sys=0.01, real=0.03 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class CmsRemarkEvent extends CmsIncrementalModeCollector
        implements BlockingEvent, TriggerData, ParallelEvent, TimesData {

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
     * The trigger for the GC event.
     */
    private String trigger;

    /**
     * Whether or not the <code>-XX:+CMSClassUnloadingEnabled</code> JVM option is enabled to allow perm gen / metaspace
     * collections. The concurrent low pause collector does not allow for class unloading by default.
     */
    private boolean classUnloading;

    /**
     * The time of all threads added together in centoseconds.
     */
    private int timeUser;

    /**
     * The wall (clock) time in centoseconds.
     */
    private int timeReal;

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^((" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[GC( \\(("
            + JdkRegEx.TRIGGER_CMS_FINAL_REMARK + ")\\)[ ]{0,1})?\\[YG occupancy: " + JdkRegEx.SIZE + " \\("
            + JdkRegEx.SIZE + "\\)\\])?(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[Rescan \\(parallel\\) , " + JdkRegEx.DURATION + "\\](" + JdkRegEx.DATESTAMP + ": )?"
            + JdkRegEx.TIMESTAMP + ": \\[weak refs processing, " + JdkRegEx.DURATION + "\\]((" + JdkRegEx.DATESTAMP
            + ": )?" + JdkRegEx.TIMESTAMP + ": \\[scrub string table, " + JdkRegEx.DURATION
            + "\\])?[ ]{0,1}\\[1 CMS-remark: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] " + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    /**
     * Regular expression for class unloading enabled with <code>-XX:+CMSClassUnloadingEnabled</code>.
     * 
     * TODO: Combine with REGEX.
     */
    private static final String REGEX_CLASS_UNLOADING = "^((" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[GC( \\((" + JdkRegEx.TRIGGER_CMS_FINAL_REMARK + ")\\)[ ]{0,1})?\\[YG occupancy: " + JdkRegEx.SIZE
            + " \\(" + JdkRegEx.SIZE + "\\)\\])?(" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[Rescan \\((non-)?parallel\\) ((" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP
            + ": \\[grey object rescan, " + JdkRegEx.DURATION + "\\](" + JdkRegEx.DATESTAMP + ": )?"
            + JdkRegEx.TIMESTAMP + ": \\[root rescan, " + JdkRegEx.DURATION + "\\])?, " + JdkRegEx.DURATION + "\\]("
            + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[weak refs processing, " + JdkRegEx.DURATION
            + "\\](" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": \\[class unloading, " + JdkRegEx.DURATION
            + "\\](" + JdkRegEx.DATESTAMP + ": )?" + JdkRegEx.TIMESTAMP + ": ((\\[scrub symbol & string tables, "
            + JdkRegEx.DURATION + "\\])|(\\[scrub symbol table, " + JdkRegEx.DURATION + "\\](" + JdkRegEx.DATESTAMP
            + ": )?" + JdkRegEx.TIMESTAMP + ": \\[scrub string table, " + JdkRegEx.DURATION
            + "\\]))( )?\\[1 CMS-remark: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\] " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + TimesData.REGEX + "?[ ]*$";

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public CmsRemarkEvent(String logEntry) {
        this.logEntry = logEntry;

        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(1) != null) {
                    // Initial GC[YG block exists
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                    trigger = matcher.group(15);
                } else {
                    // Initial GC[YG block missing
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(29)).longValue();
                }
                // The last duration is the total duration for the phase.
                duration = JdkMath.convertSecsToMillis(matcher.group(68)).intValue();
                if (matcher.group(71) != null) {
                    timeUser = JdkMath.convertSecsToCentos(matcher.group(72)).intValue();
                    timeReal = JdkMath.convertSecsToCentos(matcher.group(73)).intValue();
                }
            }
            classUnloading = false;
        } else if (logEntry.matches(REGEX_CLASS_UNLOADING)) {
            Pattern pattern = Pattern.compile(REGEX_CLASS_UNLOADING);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(1) != null) {
                    // Initial GC[YG block exists
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(13)).longValue();
                    trigger = matcher.group(15);
                } else {
                    // Initial GC[YG block missing
                    timestamp = JdkMath.convertSecsToMillis(matcher.group(29)).longValue();
                }
                // The last duration is the total duration for the phase.
                duration = JdkMath.convertSecsToMillis(matcher.group(136)).intValue();
                if (matcher.group(139) != null) {
                    timeUser = JdkMath.convertSecsToCentos(matcher.group(140)).intValue();
                    timeReal = JdkMath.convertSecsToCentos(matcher.group(141)).intValue();
                }
            }
            classUnloading = true;
        }
    }

    /**
     * Alternate constructor. Create CMS Remark logging event from values.
     * 
     * @param logEntry
     *            The log entry for the event.
     * @param timestamp
     *            The time when the GC event happened in milliseconds after JVM startup.
     * @param duration
     *            The elapsed clock time for the GC event in milliseconds.
     */
    public CmsRemarkEvent(String logEntry, long timestamp, int duration) {
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

    public String getName() {
        return JdkUtil.LogEventType.CMS_REMARK.toString();
    }

    public String getTrigger() {
        return trigger;
    }

    public boolean isClassUnloading() {
        return classUnloading;
    }

    public int getTimeUser() {
        return timeUser;
    }

    public int getTimeReal() {
        return timeReal;
    }

    public int getParallelism() {
        return JdkMath.calcParallelism(timeUser, timeReal);
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return logLine.matches(REGEX) || logLine.matches(REGEX_CLASS_UNLOADING);
    }
}
