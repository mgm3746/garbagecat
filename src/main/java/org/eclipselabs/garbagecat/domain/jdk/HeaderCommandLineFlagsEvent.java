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
 * A flags header that displays a combination and/or subset of the following: (1) Options passed to the JVM. (2) Options
 * set by ergonomics.
 * 
 * <p>
 * It is not a definitive list but a summary of some options (e.g. it does not include log file name, rotation details,
 * etc.).
 * </p>
 * 
 * <p>
 * For example, when I pass these options:
 * </p>
 * 
 * <pre>
 * -XX:+UseConcMarkSweepGC -verbose:gc-verbose:gc -Xloggc:gc.log.`date +%Y%m%d%H%M%S`
 * </pre>
 * 
 * <p>
 * I see this output:
 * </p>
 * 
 * <pre>
 * CommandLine flags: -XX:InitialHeapSize=257840832 -XX:MaxHeapSize=4125453312 -XX:MaxNewSize=697933824 -XX:MaxTenuringThreshold=6 -XX:OldPLABSize=16 -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedClassPointers -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseParNewGC
 * </pre>
 * 
 * <h2>Example Logging</h2>
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
    private static final String _REGEX = "^CommandLine flags: (.+)$";

    private static final Pattern PATTERN = Pattern.compile(_REGEX);

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
    public HeaderCommandLineFlagsEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    /**
     * @return JVM options.
     */
    public String getJvmOptions() {
        String jvmOptions = null;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            jvmOptions = matcher.group(1);
        }
        return jvmOptions;
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

}
