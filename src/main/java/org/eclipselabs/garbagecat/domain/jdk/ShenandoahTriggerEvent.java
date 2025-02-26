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

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * SHENANDOAH_TRIGGER
 * </p>
 * 
 * <p>
 * Trigger information logging. Broken out from {@link org.eclipselabs.garbagecat.domain.jdk.GcInfoEvent} for possible
 * future analysis.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * Trigger: Learning 1 of 5. Free (45118K) is below initial threshold (45875K)
 * </pre>
 * 
 * <pre>
 * Trigger: Average GC time (12.56 ms) is above the time for allocation rate (899 MB/s) to deplete free headroom (11466K)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ShenandoahTriggerEvent extends ShenandoahCollector implements ThrowAwayEvent {
    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^Trigger: .*$";

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
    public ShenandoahTriggerEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.SHENANDOAH_TRIGGER;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getTimestamp() {
        return 0;
    }
}
