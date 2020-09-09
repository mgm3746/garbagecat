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
 * <h3>Example Logging with -XX:PrintFLSStatistics=1</h3>
 * 
 * <pre>
 * 1.118: [GC Before GC:
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
 * <h3>Example Logging with -XX:PrintFLSStatistics=2</h3>
 * 
 * <pre>
 * 2017-03-19T08:29:28.879+0000: 344649.865: [GC (Allocation Failure) Before GC:
 * Statistics for BinaryTreeDictionary:
 * ------------------------------------
 * Total Free Space: 667063835
 * Max   Chunk Size: 482125404
 * Number of Blocks: 22259
 * Av.  Block  Size: 29968
 * Tree      Height: 72
 * Statistics for IndexedFreeLists:
 * --------------------------------
 * Total Free Space: 494627725
 * Max   Chunk Size: 256
 * Number of Blocks: 64523015
 * Av.  Block  Size: 7
  * free=1161691560 frag=0.8231
 * 2017-03-19T08:29:28.880+0000: 344649.866: [ParNew: 63446K-&gt;7360K(66368K), 0.0391181 secs] 1399635K-&gt;1344580K(10478400K)After GC:
 * Statistics for BinaryTreeDictionary:
 * ------------------------------------
 * Total Free Space: 667063835
 * Max   Chunk Size: 482125404
 * Number of Blocks: 22259
 * Av.  Block  Size: 29968
 * Tree      Height: 72
 * Statistics for IndexedFreeLists:
 * --------------------------------
 * Total Free Space: 494495716
 * Max   Chunk Size: 256
 * Number of Blocks: 64505638
 * Av.  Block  Size: 7
 *  free=1161559551 frag=0.8231
 * , 0.0405917 secs] [Times: user=0.08 sys=0.00, real=0.04 secs]
 * ...
 * 2017-03-19T12:47:59.265+0000: 360160.251: [CMS-concurrent-sweep-start]
 * size[3] : demand: 3471401, old_rate: 742.511902, current_rate: 980.253113, new_rate: 966.402527, old_desired: 13570211, new_desired: 9460020
 * size[4] : demand: 19601900, old_rate: 5146.810547, current_rate: 5535.178223, new_rate: 5518.769531, old_desired: 94063552, new_desired: 54022696
 * ...
 * demand: 0, old_rate: 0.000000, current_rate: 0.000000, new_rate: 0.000000, old_desired: 0, new_desired: 0
 * demand: 1, old_rate: 0.000000, current_rate: 0.000282, new_rate: 0.000282, old_desired: 0, new_desired: 2
 * CMS: Large Block: 0x000000075d6b71f0; Proximity: 0x00000006d60c73f0 -&gt; 0x00000007580cee20
 * CMS: Large block 0x000000075d658970
 * 2017-03-19T12:48:01.450+0000: 360162.436: [CMS-concurrent-sweep: 2.184/2.184 secs] [Times: user=2.27 sys=0.02, real=2.19 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 2017-03-19T08:29:28.879+0000: 344649.865: [GC (Allocation Failure) 2017-03-19T08:29:28.880+0000: 344649.866: [ParNew: 63446K-&gt;7360K(66368K), 0.0391181 secs] 1399635K-&gt;1344580K(10478400K), 0.0405917 secs]
 * 2017-03-19T12:47:59.265+0000: 360160.251: [CMS-concurrent-sweep-start]
 * 2017-03-19T12:48:01.450+0000: 360162.436: [CMS-concurrent-sweep: 2.184/2.184 secs] [Times: user=2.27 sys=0.02, real=2.19 secs]
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
            "^Number of Blocks: \\d{1,8}$",
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
                    + "$",
            // divider
            "^--------------------------------$",
            // statistics for IndexedFreeLists
            "^Statistics for IndexedFreeLists:$",
            // free/frag
            "^ free=\\d{1,10} frag=\\d\\.\\d{4}$",
            // sweep size
            "^size\\[\\d{1,3}\\] : demand: \\d{1,9}, old_rate: \\d{1,4}\\.\\d{6}, current_rate: \\d{1,4}\\.\\d{6}, "
                    + "new_rate: \\d{1,4}\\.\\d{6}, old_desired: \\d{1,9}, new_desired: \\d{1,9}$",
            // sweep demand
            "^demand: \\d, old_rate: \\d\\.\\d{6}, current_rate: \\d\\.\\d{6}, new_rate: \\d\\.\\d{6}, old_desired: "
                    + "\\d, new_desired: \\d$",
            //
    };

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
