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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * VM_WARNING
 * </p>
 * 
 * <p>
 * VM warning information.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * OpenJDK 64-Bit Server VM warning: Failed to reserve shared memory. (error = 12)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class VmWarningEvent implements UnifiedLogging {
    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^OpenJDK 64-Bit Server VM warning: (.+\\(error = (\\d{1,2})\\))$";

    private static final Pattern PATTERN = Pattern.compile(VmWarningEvent._REGEX);

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
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public VmWarningEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    /**
     * @return The warning errno.
     */
    public String getErrNo() {
        String errNo = null;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            errNo = matcher.group(2);
        }
        return errNo;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.VM_WARNING;
    }

    public String getLogEntry() {
        return logEntry;
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isEndstamp() {
        boolean isEndStamp = false;
        return isEndStamp;
    }
}
