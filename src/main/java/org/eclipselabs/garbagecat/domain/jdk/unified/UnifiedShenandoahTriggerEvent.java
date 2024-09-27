/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2024 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahCollector;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_SHENANDOAH_TRIGGER
 * </p>
 * 
 * <p>
 * Trigger information logging. Broken out from {@link org.eclipselabs.garbagecat.domain.jdk.GcInfoEvent} for possible
 * future analysis.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Standard:
 * </p>
 * 
 * <pre>
 * [0.448s][info][gc] Trigger: Learning 1 of 5. Free (44M) is below initial threshold (44M)
 * </pre>
 * 
 * <pre>
 * [0.814s][info][gc] Trigger: Average GC time (18.86 ms) is above the time for allocation rate (328.75 MB/s) to deplete free headroom (5M)
 * </pre>
 * 
 * <pre>
 * [24.356s][info][gc] Trigger: Free (6M) is below minimum threshold (6M)
 * </pre>
 * 
 * <p>
 * 2) JDK17:
 * </p>
 * 
 * <pre>
 * [10.508s][info][gc          ] Trigger: Average GC time (16.09 ms) is above the time for average allocation rate (409 MB/s) to deplete free headroom (5742K) (margin of error = 1.80)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedShenandoahTriggerEvent extends ShenandoahCollector implements UnifiedLogging, ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^" + UnifiedRegEx.DECORATOR + " Trigger: .*$";

    public static final Pattern PATTERN = Pattern.compile(_REGEX);

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
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public UnifiedShenandoahTriggerEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.UNIFIED_SHENANDOAH_TRIGGER.toString();
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return 0;
    }

    public boolean isEndstamp() {
        boolean isEndStamp = false;
        return isEndStamp;
    }
}
