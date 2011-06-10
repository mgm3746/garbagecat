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
package org.eclipselabs.garbagecat.preprocess.jdk;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * THREAD_DUMP
 * </p>
 * 
 * <p>
 * Remove thread dump output. Garbage collection logging goes to standard out by default, the same location as thread
 * dumps.
 * </p>
 * 
 * <p>
 * It is recommended to log garbage collection logging to a dedicated file with the <code>-Xverboselog:</code> option to
 * avoid mixing thread dumps with garbage collection logging.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Thread dump date/time line (added with JDK 1.6):
 * </p>
 * 
 * <pre>
 * 2009-12-29 14:17:17
 * </pre>
 * 
 * <p>
 * 2) Thread dump title line:
 * </p>
 * 
 * <pre>
 * Full thread dump Java HotSpot(TM) Server VM (11.0-b16 mixed mode):
 * </pre>
 * 
 * <p>
 * 3) Thread data line:
 * </p>
 * 
 * <pre>
 * &quot;Thread-144233478&quot; daemon prio=10 tid=0x22817800 nid=0x77f5 runnable [0x25174000..0x25175030]
 * </pre>
 * 
 * <p>
 * 4) Thread state line:
 * </p>
 * 
 * <pre>
 * java.lang.Thread.State: BLOCKED (on object monitor)
 * </pre>
 * 
 * <p>
 * 5) Stack trace location line:
 * </p>
 * 
 * <pre>
 * at java.lang.Object.wait(Object.java:485)
 * </pre>
 * 
 * <p>
 * 6) Stack trace event line:
 * </p>
 * 
 * <pre>
 * - locked &lt;0x94fa4fb0&gt; (a org.apache.tomcat.util.net.JIoEndpoint$Worker)
 * </pre>
 * 
 * <p>
 * 7) JDK6 summary information:
 * </p>
 * 
 * <pre>
 * JNI global references: 844
 * </pre>
 * 
 * <pre>
 * Heap
 *  par new generation   total 917504K, used 808761K [0x44c40000, 0x84c40000, 0x84c40000)
 *   eden space 786432K, 100% used [0x44c40000, 0x74c40000, 0x74c40000)
 *   from space 131072K,  17% used [0x7cc40000, 0x7e20e790, 0x84c40000)
 *   to   space 131072K,   0% used [0x74c40000, 0x74c40000, 0x7cc40000)
 *  concurrent mark-sweep generation total 1572864K, used 1572863K [0x84c40000, 0xe4c40000, 0xe4c40000)
 *  concurrent-mark-sweep perm gen total 77736K, used 46547K [0xe4c40000, 0xe982a000, 0xf4c40000)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class ThreadDumpPreprocessAction implements PreprocessAction {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX[] = {
            // beginning date/time
            "^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$",
            // title
            "^Full thread dump(.*):$",
            // thread data
            "^\"[a-zA-z0-9\\-\\.\\@ \\:\\#\\(\\)\\[\\]/]+\" (daemon )?prio=\\d{1,2} tid=0x[a-z0-9]{8,16} " + "nid=0x[a-z0-9]{3,4} (runnable|in Object.wait\\(\\)|waiting for monitor entry|"
                    + "waiting on condition|sleeping)( )?(\\[0x[a-z0-9]{8,16}\\.\\.0x[a-z0-9]{8,16}\\])?[ ]*$",
            // thread state
            "^   java.lang.Thread.State: (RUNNABLE|WAITING \\(on object monitor\\)|" + "BLOCKED \\(on object monitor\\)|TIMED_WAITING \\(on object monitor\\)|TERMINATED|"
                    + "TIMED_WAITING \\(sleeping\\))$",
            // stack trace location line
            "^\\tat (.*)$",
            // stack trace event line
            "^\\t- (locked|waiting to lock|waiting on) (.*)$",
            // Heap summary lines
            "^JNI global references: \\d{1,6}$", "^Heap$", "^ par new generation   total.*$", "^  eden space.*$", "^  from space.*$", "^  to   space.*$",
            "^ concurrent mark-sweep generation total.*$", "^ concurrent-mark-sweep perm gen total.*$" };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Create thread dump event from log entry.
     */
    public ThreadDumpPreprocessAction(String logEntry) {
        this.logEntry = logEntry;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.THREAD_DUMP.toString();
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
