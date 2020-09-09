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

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * GC_LOCKER
 * </p>
 * 
 * <p>
 * After a JNI critical section was exited, the CMS collector tried to do a <code>ParNew</code> collection, but it
 * failed due to the full promotion guarantee. This will cause the serial collector to be invoked.
 * </p>
 * 
 * <p>
 * Retain logging, to allow analysis if trigger information is not being printed.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 2017-01-08T14:18:15.878+0300: 58626.878: [GC (GCLocker Initiated GC)2017-01-08T14:18:15.878+0300: 58626.878: [ParNew: 5908427K-&gt;5908427K(8388608K), 0.0000320 secs] 19349630K-&gt;19349630K(22020096K) icms_dc=100 , 0.0002560 secs] [Times: user=0.00 sys=0.00, real=0.00 secs]
 * GC locker: Trying a full collection because scavenge failed
 * 2017-01-08T14:18:15.879+0300: 58626.878: [Full GC (GCLocker Initiated GC)2017-01-08T14:18:15.879+0300: 58626.878: [CMS2017-01-08T14:18:19.075+0300: 58630.075: [CMS-concurrent-sweep: 3.220/3.228 secs] [Times: user=3.38 sys=0.01, real=3.22 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class GcLockerEvent implements LogEvent {

    /**
     * Regular expression defining the logging.
     */
    private static final String REGEX = "^GC locker: Trying a full collection because scavenge failed$";

    private static final Pattern PATTERN = Pattern.compile(REGEX);

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
    public GcLockerEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.GC_LOCKER.toString();
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
        return PATTERN.matcher(logLine).matches();
    }
}
