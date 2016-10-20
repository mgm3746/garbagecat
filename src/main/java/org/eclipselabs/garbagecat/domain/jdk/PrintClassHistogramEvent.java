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
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * CLASS_HISTOGRAM
 * </p>
 * 
 * <p>
 * Logging enabled with the <code>-XX:+PrintClassHistogram</code> option used to determine the objects in a heap and how
 * much space they are consuming. The output is triggered manually with a thread dump.
 * </p>
 * 
 * <p>
 * This is generally not a useful option for the following reasons:
 * </p>
 * 
 * <ul>
 * <li>It is a heavyweight option. It forces a full collection and can output tens of thousands of logging lines.</li>
 * <li>A class histogram is limited troubleshooting memory leaks compared to a full heap dump.</li>
 * </ul>
 * 
 * <p>
 * Generally memory leaks are investigated by getting a heap dump, but there are use cases where this option can be
 * useful.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 2016-09-13T06:54:06.773+0100: 11662.232: [Full GC 11662.233: [Class Histogram:
 *  num     #instances         #bytes  class name
 * ----------------------------------------------
 *    1:       5249662      476131272  [C
 *    2:       4326648      166452736  java.lang.String
 *    3:       1213019      140458680  &lt;constMethodKlass&gt;
 *    4:       1149724      132421736  [Ljava.lang.Object;
 *    5:       2212385      108495400  javax.servlet.jsp.tagext.TagAttributeInfo
 * ...
 * 27722:             1             16  com.example.MyClass
 * Total      16227637     1059670840
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class PrintClassHistogramEvent implements ThrowAwayEvent {

    /**
     * Regular expression for cruft left after class histogram preprocessing.
     */
    public static final String REGEX_PREPROCESSED = JdkRegEx.TIMESTAMP + ": \\[Class Histogram(:)?, "
            + JdkRegEx.DURATION + "\\]";

    /**
     * Regular expressions defining the logging.
     */
    private static final String[] REGEX = {
            /*
             * Column names
             */
            "^ num     #instances         #bytes  class name$",
            /*
             * Header divider
             */
            "^----------------------------------------------$",
            /*
             * Instance data
             */
            "^[ ]{0,3}\\d{1,5}:[ ]{7,13}\\d{1,9}[ ]{5,13}\\d{1,10}[ ]{2}[a-zA-Z0-9<>\\[\\$\\._;]+$",
            /*
             * Footer
             */
            "^Total( ){6}\\d{1,10}( ){5}\\d{1,10}$" };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event happened in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public PrintClassHistogramEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.PRINT_CLASS_HISTOGRAM.toString();
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
        boolean isMatch = false;
        for (int i = 0; i < REGEX.length; i++) {
            if (logLine.matches(REGEX[i])) {
                isMatch = true;
                break;
            }
        }
        return isMatch;
    }
}
