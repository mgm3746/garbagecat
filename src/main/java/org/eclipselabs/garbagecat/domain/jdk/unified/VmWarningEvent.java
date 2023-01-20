/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
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

    private static final Pattern pattern = Pattern.compile(VmWarningEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^OpenJDK 64-Bit Server VM warning: (.+\\(error = (\\d{1,2})\\))$";

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
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            errNo = matcher.group(2);
        }
        return errNo;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.VM_WARNING.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
