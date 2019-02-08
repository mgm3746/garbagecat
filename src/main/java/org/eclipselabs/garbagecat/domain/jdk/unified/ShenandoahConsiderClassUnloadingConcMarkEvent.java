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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * CONSIDER_CLASS_UNLOADING_CONC_MARK
 * </p>
 * 
 * <p>
 * Logging output at the beginning of gc log when using the Shenandoah collector.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * [[0.001s][info][gc] Consider -XX:+ClassUnloadingWithConcurrentMark if large pause times are observed on class-unloading sensitive workloads
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahConsiderClassUnloadingConcMarkEvent extends ShenandoahCollector
        implements UnifiedLogging, LogEvent, ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc\\] Consider -XX:\\+ClassUnloadingWithConcurrentMark if large pause times are "
            + "observed on class-unloading sensitive workloads[ ]*$";

    private static Pattern pattern = Pattern.compile(REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public ShenandoahConsiderClassUnloadingConcMarkEvent(String logEntry) {
        this.logEntry = logEntry;

        if (logEntry.matches(REGEX)) {
            Pattern pattern = Pattern.compile(REGEX);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                timestamp = JdkMath.convertSecsToMillis(matcher.group(1)).longValue();
            }
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.SHENANDOAH_CONSIDER_CLASS_UNLOADING_CONC_MARK.toString();
    }

    public long getTimestamp() {
        return timestamp;
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
