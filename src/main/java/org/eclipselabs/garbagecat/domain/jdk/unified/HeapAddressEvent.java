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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * HEAP_ADDRESS
 * </p>
 * 
 * <p>
 * Heap address information printed at the beginning of gc logging.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) With <code>-Xlog:gc*:file=&lt;file&gt;</code>:
 * </p>
 * 
 * <pre>
 * [0.004s][info][gc,heap,coops] Heap address: 0x00000000fc000000, size: 64 MB, Compressed Oops mode: 32-bit
 * </pre>
 * 
 * <pre>
 * [0.019s][info][gc,heap,coops] Heap address: 0x00000006c2800000, size: 4056 MB, Compressed Oops mode: Zero based, Oop shift amount: 3
 * </pre>
 * 
 * <p>
 * 2) With <code>-Xlog:gc*:file=&lt;file&gt;:time,uptimemillis</code>:
 * </p>
 * 
 * <pre>
 * [2019-02-05T14:47:31.092-0200][4ms] Heap address: 0x00000000ae900000, size: 1303 MB, Compressed Oops mode: 32-bit
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class HeapAddressEvent implements UnifiedLogging, ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX = "^" + UnifiedRegEx.DECORATOR + " Heap address: " + JdkRegEx.ADDRESS
            + ", size: \\d{1,8} MB, Compressed Oops mode: (32-bit|Zero based, Oop shift amount: \\d)$";

    private static final Pattern pattern = Pattern.compile(REGEX);

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
    public HeapAddressEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.HEAP_ADDRESS.toString();
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
}
