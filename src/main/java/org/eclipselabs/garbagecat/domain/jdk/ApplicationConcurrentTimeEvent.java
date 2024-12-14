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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * APPLICATION_CONCURRENT_TIME
 * </p>
 * 
 * <p>
 * Logging enabled with the <code>-XX:+PrintGCApplicationConcurrentTime</code> JVM option. It shows the time the
 * application runs between collection pauses.
 * 
 * <p>
 * This option is redundant, as the same information can be calculated from the GC logging timestamps and durations.
 * Therefore, advise against using it, as it adds overhead with no analysis value.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <pre>
 * Application time: 130.5284640 seconds
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ApplicationConcurrentTimeEvent implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^(" + JdkRegEx.DECORATOR
            + " )?Application time: \\d{1,4}\\.\\d{7} seconds[ ]*$";

    /**
     * RegEx pattern.
     */
    private static Pattern PATTERN = Pattern.compile(_REGEX);

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
    public ApplicationConcurrentTimeEvent(String logEntry) {
        this.logEntry = logEntry;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.APPLICATION_CONCURRENT_TIME;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getTimestamp() {
        return 0;
    }

}
