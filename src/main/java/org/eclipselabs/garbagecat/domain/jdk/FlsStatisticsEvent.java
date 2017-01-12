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
 * FLS_STATISTICS
 * </p>
 * 
 * <p>
 * CMS Free List Space statistics.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <pre>
 * 2016-11-02T06:22:25.610-0400: 1.118: [GC Before GC:
 * Statistics for BinaryTreeDictionary:
 * ------------------------------------
 * Total Free Space: 536870912
 * Max   Chunk Size: 536870912
 * Number of Blocks: 1
 * Av.  Block  Size: 536870912
 * Tree      Height: 1
 * Before GC:
 * Statistics for BinaryTreeDictionary:
 * ------------------------------------
 * Total Free Space: 0
 * Max   Chunk Size: 0
 * Number of Blocks: 0
 * Tree      Height: 0
 * 1.118: [ParNew: 377487K-&gt;8426K(5505024K), 0.0535260 secs] 377487K-&gt;8426K(43253760K)After GC:
 * Statistics for BinaryTreeDictionary:
 * ------------------------------------
 * Total Free Space: 536854528
 * Max   Chunk Size: 536854528
 * Number of Blocks: 1
 * Av.  Block  Size: 536854528
 * Tree      Height: 1
 * After GC:
 * Statistics for BinaryTreeDictionary:
 * ------------------------------------
 * Total Free Space: 0
 * Max   Chunk Size: 0
 * Number of Blocks: 0
 * Tree      Height: 0
 * , 0.0536040 secs] [Times: user=0.89 sys=0.01, real=0.06 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 1.118: [GC 1.118: [ParNew: 377487K-&gt;8426K(5505024K), 0.0535260 secs] 377487K-&gt;8426K(43253760K), 0.0536040 secs] [Times: user=0.89 sys=0.01, real=0.06 secs]
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class FlsStatisticsEvent implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String[] REGEX = {
            /*
             * header
             */
            "^Statistics for BinaryTreeDictionary:$",
            /*
             * divider
             */
            "^------------------------------------$",
            /*
             * total free space
             */
            "^Total Free Space: (-)?" + JdkRegEx.SIZE_BYTES + "$",
            /*
             * max chunk size
             */
            "^Max   Chunk Size: (-)?" + JdkRegEx.SIZE_BYTES + "$",
            /*
             * # blocks
             */
            "^Number of Blocks: \\d{1,6}$",
            /*
             * av block size
             */
            "^Av.  Block  Size: (-)?" + JdkRegEx.SIZE_BYTES + "$",
            /*
             * tree height
             */
            "^Tree[ ]{6}Height: \\d{1,3}$",
            /*
             * before gc
             */
            "^Before GC:$",
            /*
             * after gc
             */
            "^After GC:$",
            /*
             * large block
             */
            "^CMS: Large block " + JdkRegEx.ADDRESS + "$",
            /*
             * large block with proximity
             */
            "^CMS: Large Block: " + JdkRegEx.ADDRESS + "; Proximity: " + JdkRegEx.ADDRESS + " -> " + JdkRegEx.ADDRESS
                    + "$" };

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
    public FlsStatisticsEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.FLS_STATISTICS.toString();
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
