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

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * HEADER_COMMAND_LINE_FLAGS
 * </p>
 * 
 * <p>
 * Flags header.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * CommandLine flags: -XX:+CMSClassUnloadingEnabled -XX:CMSInitiatingOccupancyFraction=75 -XX:+CMSScavengeBeforeRemark -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses -XX:GCLogFileSize=8388608 -XX:InitialHeapSize=13958643712 -XX:MaxHeapSize=13958643712 -XX:MaxPermSize=402653184 -XX:MaxTenuringThreshold=6 -XX:NewRatio=2 -XX:NumberOfGCLogFiles=8 -XX:OldPLABSize=16 -XX:PermSize=402653184 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseGCLogFileRotation -XX:+UseParNewGC
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class HeaderCommandLineFlagsEvent implements LogEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(CommandLine flags: )(.+)$";
    private static Pattern pattern = Pattern.compile(HeaderCommandLineFlagsEvent.REGEX);

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create CommandLineFlags detail logging event from log entry.
     */
    public HeaderCommandLineFlagsEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.HEADER_COMMAND_LINE_FLAGS.toString();
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
    
    /**
     * @return JVM options.
     */
    public String getJvmOptions() {
        String jvmOptions = null;
        Matcher matcher = pattern.matcher(logEntry);
        if (matcher.find()) {
            jvmOptions = matcher.group(2);
        }
        return jvmOptions;
    }

}
