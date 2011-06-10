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
import org.eclipselabs.garbagecat.domain.OldCollection;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermCollection;
import org.eclipselabs.garbagecat.domain.PermData;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA
 * </p>
 * 
 * <p>
 * Combined {@link org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedEvent} and
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewConcurrentModeFailurePermDataEvent}. This appears to be a result
 * of a chain reaction where the {@link org.eclipselabs.garbagecat.domain.jdk.ParNewPromotionFailedEvent} initiates a
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} which leads to a
 * {@link org.eclipselabs.garbagecat.domain.jdk.ParNewConcurrentModeFailurePermDataEvent}.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * Split into 2 lines then combined as 1 line by
 * {@link org.eclipselabs.garbagecat.preprocess.jdk.CmsConcurrentModeFailurePreprocessAction} with CMS-concurrent-mark:
 * </p>
 * 
 * <pre>
 * 2746.109: [GC 2746.109: [ParNew (promotion failed): 242303K-&gt;242304K(242304K), 1.3009892 secs]2747.410: [CMS2755.518: [CMS-concurrent-mark: 11.734/13.504 secs] (concurrent mode failure): 5979868K-&gt;5968004K(6014592K), 78.3207206 secs] 6205857K-&gt;5968004K(6256896K), [CMS Perm : 207397K-&gt;207212K(262144K)], 79.6222096 secs]
 * </pre>
 * 
 * <p>
 * The CMS collector run in incremental mode (icms), enabled with <code>-XX:+CMSIncrementalMode</code>. In this mode,
 * the CMS collector does not hold the processor for the entire long concurrent phases but periodically stops them and
 * yields the processor back to other threads in application. It divides the work to be done in concurrent phases into
 * small chunks called duty cycles and schedules them between minor collections. This is very useful for applications
 * that need low pause times and are run on machines with a small number of processors.
 * </p>
 * 
 * <pre>
 * 4555.706: [GC 4555.706: [ParNew (promotion failed): 1304576K-&gt;1304575K(1304576K), 4.5501949 secs]4560.256: [CMS CMS: abort preclean due to time 4562.921: [CMS-concurrent-abortable-preclean: 2.615/14.874 secs] (concurrent mode failure): 924455K-&gt;679155K(4886528K), 6.2285220 secs] 1973973K-&gt;679155K(6191104K), [CMS Perm : 198322K-&gt;198277K(524288K)] icms_dc=24 , 10.7789303 secs] [Times: user=9.49 sys=1.83, real=10.78 secs]
 * </pre>
 * 
 * <p>
 * Split into 2 lines then combined as 1 line by
 * {@link org.eclipselabs.garbagecat.preprocess.jdk.CmsConcurrentModeFailurePreprocessAction} with concurrent mode
 * failure missing:
 * </p>
 * 
 * <pre>
 * 88063.609: [GC 88063.610: [ParNew (promotion failed): 513856K-&gt;513856K(513856K), 4.0911197 secs]88067.701: [CMS88067.742: [CMS-concurrent-reset: 0.309/4.421 secs]: 10612422K-&gt;4373474K(11911168K), 76.7523274 secs] 11075362K-&gt;4373474K(12425024K), [CMS Perm : 214530K-&gt;213777K(524288K)], 80.8440551 secs] [Times: user=80.01 sys=5.57, real=80.84 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 */
public class ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent implements BlockingEvent, OldCollection, PermCollection, YoungData, OldData, PermData {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + JdkRegEx.TIMESTAMP + ": \\[GC " + JdkRegEx.TIMESTAMP + ": \\[ParNew \\(promotion failed\\): " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMESTAMP + ": \\[CMS( CMS: abort preclean due to time )?(" + JdkRegEx.TIMESTAMP
            + ": \\[CMS-concurrent-(abortable-preclean|mark|preclean|reset|sweep): " + JdkRegEx.DURATION_FRACTION + "\\])?( \\(concurrent mode failure\\))?: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE
            + "\\(" + JdkRegEx.SIZE + "\\), " + JdkRegEx.DURATION + "\\] " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\), \\[CMS Perm : " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)\\]" + JdkRegEx.ICMS_DC_BLOCK + "?, " + JdkRegEx.DURATION + "\\]" + JdkRegEx.TIMES_BLOCK + "?[ ]*$";
    private static Pattern pattern = Pattern.compile(REGEX);

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
     * Permanent generation size (kilobytes) at beginning of GC event.
     */
    private int permGen;

    /**
     * Permanent generation size (kilobytes) at end of GC event.
     */
    private int permGenEnd;

    /**
     * Space allocated to permanent generation (kilobytes).
     */
    private int permGenAllocation;

    /**
     * Create ParNew detail logging event from log entry.
     */
    public ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(String logEntry) {
        this.logEntry = logEntry;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            old = Integer.parseInt(matcher.group(14));
            oldEnd = Integer.parseInt(matcher.group(15));
            oldAllocation = Integer.parseInt(matcher.group(16));
            int totalBegin = Integer.parseInt(matcher.group(18));
            young = totalBegin - old;
            int totalEnd = Integer.parseInt(matcher.group(19));
            youngEnd = totalEnd - oldEnd;
            int totalAllocation = Integer.parseInt(matcher.group(20));
            youngAvailable = totalAllocation - oldAllocation;
            permGen = Integer.parseInt(matcher.group(21));
            permGenEnd = Integer.parseInt(matcher.group(22));
            permGenAllocation = Integer.parseInt(matcher.group(23));
            duration = JdkMath.convertSecsToMillis(matcher.group(25)).intValue();
        }
    }

    /**
     * Alternate constructor. Create ParNew detail logging event from values.
     * 
     * @param logEntry
     * @param timestamp
     * @param duration
     */
    public ParNewPromotionFailedCmsConcurrentModeFailurePermDataEvent(String logEntry, long timestamp, int duration) {
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

    public int getPermOccupancyInit() {
        return permGen;
    }

    public int getPermOccupancyEnd() {
        return permGenEnd;
    }

    public int getPermSpace() {
        return permGenAllocation;
    }

    public String getName() {
        return JdkUtil.LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA.toString();
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
