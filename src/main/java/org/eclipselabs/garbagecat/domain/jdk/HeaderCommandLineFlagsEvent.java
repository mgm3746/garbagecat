/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
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
 * Flags header the displays a combination of options passed to the JVM and options/settings triggered by the options
 * passed in. It is not just the options passed to the JVM.
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
 * <h3>Example Logging</h3>
 * 
 * 1) Standard JVM format:
 * 
 * <pre>
 * CommandLine flags: -XX:+CMSClassUnloadingEnabled -XX:CMSInitiatingOccupancyFraction=75 -XX:+CMSScavengeBeforeRemark -XX:+ExplicitGCInvokesConcurrentAndUnloadsClasses -XX:GCLogFileSize=8388608 -XX:InitialHeapSize=13958643712 -XX:MaxHeapSize=13958643712 -XX:MaxPermSize=402653184 -XX:MaxTenuringThreshold=6 -XX:NewRatio=2 -XX:NumberOfGCLogFiles=8 -XX:OldPLABSize=16 -XX:PermSize=402653184 -XX:+PrintGC -XX:+PrintGCDateStamps -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+UseCompressedOops -XX:+UseConcMarkSweepGC -XX:+UseGCLogFileRotation -XX:+UseParNewGC
 * </pre>
 * 
 * 2) JBoss header in standard out:
 * 
 * <pre>
 *   JAVA_OPTS: -Dprogram.name=run.sh -d64 -server -Xms10000m -Xmx10000m -ss512k -XX:PermSize=512m -XX:MaxPermSize=512m -XX:NewSize=3000m -XX:MaxNewSize=3000m -XX:SurvivorRatio=6 -XX:TargetSurvivorRatio=90 -XX:MaxTenuringThreshold=5 -verbose:gc -XX:+PrintGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:-TraceClassUnloading -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:+CMSParallelRemarkEnabled -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly -XX:ParallelGCThreads=8 -XX:+CMSScavengeBeforeRemark -Dcom.sun.management.jmxremote.port=12345 -Dcom.sun.management.jmxremote.ssl=false -Djava.net.preferIPv4Stack=true -Djboss.platform.mbeanserver -Djavax.management.builder.initial=org.jboss.system.server.jmx.MBeanServerBuilderImpl -XX:ErrorFile=/opt/jboss/jboss-eap-4.3/jboss-as/server/path/log/crash/hs_err_pid%p.log -XX:+DisableExplicitGC -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000 -Dsun.lang.ClassLoader.allowArraySyntax=true
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class HeaderCommandLineFlagsEvent implements LogEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^(CommandLine flags:|  JAVA_OPTS:)[ ]{1,2}(.+)$";

    private static Pattern pattern = Pattern.compile(HeaderCommandLineFlagsEvent.REGEX);

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
