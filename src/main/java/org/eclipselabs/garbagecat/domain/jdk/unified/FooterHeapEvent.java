/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * FOOTER_HEAP
 * </p>
 * 
 * <p>
 * Heap information printed at the end of gc logging with unified detailed logging
 * (<code>-Xlog:gc*:file=&lt;file&gt;</code>).
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * [25.016s][info][gc,heap,exit  ] Heap
 * [25.016s][info][gc,heap,exit  ]  garbage-first heap   total 59392K, used 38015K [0x00000000fc000000, 0x0000000100000000)
 * [25.016s][info][gc,heap,exit  ]   region size 1024K, 13 young (13312K), 1 survivors (1024K)
 * [25.016s][info][gc,heap,exit  ]  Metaspace       used 11079K, capacity 11287K, committed 11520K, reserved 1060864K
 * [25.016s][info][gc,heap,exit  ]   class space    used 909K, capacity 995K, committed 1024K, reserved 1048576K
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class FooterHeapEvent implements UnifiedLogging, LogEvent, ThrowAwayEvent {

    /**
     * Regular expression for heap line.
     */
    private static final String REGEX_HEAP = "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,heap,exit  \\] Heap$";

    /**
     * Regular expression for garbage-first line.
     */
    private static final String REGEX_GARBAGE_FIRST = "^\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc,heap,exit  \\]  garbage-first heap   total " + JdkRegEx.SIZE + ", used "
            + JdkRegEx.SIZE + " \\[" + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)$";

    /**
     * Regular expression for region line.
     */
    private static final String REGEX_REGION = "^\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc,heap,exit  \\]   region size " + JdkRegEx.SIZE + ", \\d{1,2} young \\("
            + JdkRegEx.SIZE + "\\), \\d{1} survivors \\(" + JdkRegEx.SIZE + "\\)$";

    /**
     * Regular expression for metaspace line.
     */
    private static final String REGEX_METASPACE = "^\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc,heap,exit  \\]  Metaspace       used " + JdkRegEx.SIZE + ", capacity "
            + JdkRegEx.SIZE + ", committed " + JdkRegEx.SIZE + ", reserved " + JdkRegEx.SIZE + "$";

    /**
     * Regular expression for class line.
     */
    private static final String REGEX_CLASS = "^\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc,heap,exit  \\]   class space    used " + JdkRegEx.SIZE + ", capacity "
            + JdkRegEx.SIZE + ", committed " + JdkRegEx.SIZE + ", reserved " + JdkRegEx.SIZE + "$";

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
    public FooterHeapEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.FOOTER_HEAP.toString();
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
        return (logLine.matches(REGEX_HEAP) || logLine.matches(REGEX_GARBAGE_FIRST) || logLine.matches(REGEX_REGION)
                || logLine.matches(REGEX_METASPACE) || logLine.matches(REGEX_CLASS));
    }
}
