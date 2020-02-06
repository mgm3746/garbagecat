/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * UNIFIED_CMS_CONCURRENT
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} with unified logging (JDK9+).
 * </p>
 * 
 * <p>
 * {@link org.eclipselabs.garbagecat.domain.jdk.CmsConcurrentEvent} with unified logging (JDK9+). Any number of events
 * that happen concurrently with the JVM's execution of application threads. These events are not included in the GC
 * analysis since there is no application pause time.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * [0.082s][info][gc] GC(1) Concurrent Mark
 * </pre>
 * 
 * <pre>
 * [0.083s][info][gc] GC(1) Concurrent Mark 1.428ms
 * </pre>
 * 
 * <pre>
 * [0.083s][info][gc] GC(1) Concurrent Preclean
 * </pre>
 * 
 * <pre>
 * [0.083s][info][gc] GC(1) Concurrent Preclean 0.032ms
 * </pre>
 * 
 * <pre>
 * [0.084s][info][gc] GC(1) Concurrent Sweep
 * </pre>
 * 
 * <pre>
 * [0.085s][info][gc] GC(1) Concurrent Sweep 0.364ms
 * </pre>
 * 
 * <pre>
 * [0.085s][info][gc] GC(1) Concurrent Reset
 * </pre>
 * 
 * <pre>
 * [0.086s][info][gc] GC(1) Concurrent Reset 0.841ms
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedCmsConcurrentEvent extends CmsCollector implements UnifiedLogging, LogEvent, ParallelEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc\\] "
            + JdkRegEx.GC_EVENT_NUMBER + " Concurrent (Mark|Preclean|Sweep|Reset)( " + JdkRegEx.DURATION_JDK9
            + ")?[ ]*$";

    private static final Pattern pattern = Pattern.compile(REGEX);

    public String getLogEntry() {
        throw new UnsupportedOperationException("Event does not include log entry information");
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_CMS_CONCURRENT.toString();
    }

    public long getTimestamp() {
        throw new UnsupportedOperationException("Event does not include timestamp information");
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
