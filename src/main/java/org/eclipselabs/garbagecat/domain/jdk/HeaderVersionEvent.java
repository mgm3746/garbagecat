/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * HEADER_VERSION
 * </p>
 * 
 * <p>
 * Version header.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) OpenJDK:
 * </p>
 * 
 * <pre>
 * OpenJDK 64-Bit Server VM (24.95-b01) for linux-amd64 JRE (1.7.0_95-b00), built on Jan 18 2016 21:57:50 by "mockbuild" with gcc 4.8.5 20150623 (Red Hat 4.8.5-4)
 * </pre>
 * 
 * <p>
 * 2) Oracle JDK:
 * </p>
 * 
 * <pre>
 * Java HotSpot(TM) 64-Bit Server VM (24.85-b08) for linux-amd64 JRE (1.7.0_85-b34), built on Sep 29 2015 08:44:21 by "java_re" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class HeaderVersionEvent implements LogEvent {

    private static Pattern pattern = Pattern.compile(HeaderVersionEvent.REGEX);

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(Java HotSpot\\(TM\\)|OpenJDK)( 64-Bit)? Server VM \\(.+\\) for "
            + "(linux|windows|solaris)-(amd64|ppc64|ppc64le|sparc|x86) JRE (\\(Zulu.+\\) )?\\("
            + JdkRegEx.RELEASE_STRING + "\\).+ built on " + JdkRegEx.BUILD_DATE_TIME + ".+$";

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
    public HeaderVersionEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.HEADER_VERSION.toString();
    }

    public long getTimestamp() {
        return timestamp;
    }
}
