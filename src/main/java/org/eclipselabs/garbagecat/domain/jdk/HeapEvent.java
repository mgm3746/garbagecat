/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.HeaderEvent;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;

/**
 * <p>
 * HEAP
 * </p>
 * 
 * <p>
 * Heap information at the end of gc logging.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Serial:
 * </p>
 * 
 * <pre>
 * Heap
 *  def new generation   total 153600K, used 5463K [0x00000005cda00000, 0x00000005d80a0000, 0x0000000673c00000)
 *   eden space 136576K,   4% used [0x00000005cda00000, 0x00000005cdf55da8, 0x00000005d5f60000)
 *   from space 17024K,   0% used [0x00000005d5f60000, 0x00000005d5f60000, 0x00000005d7000000)
 *   to   space 17024K,   0% used [0x00000005d7000000, 0x00000005d7000000, 0x00000005d80a0000)
 *  tenured generation   total 341376K, used 0K [0x0000000673c00000, 0x0000000688960000, 0x00000007c0000000)
 *    the space 341376K,   0% used [0x0000000673c00000, 0x0000000673c00000, 0x0000000673c00200, 0x0000000688960000)
 *  Metaspace       used 2469K, capacity 4480K, committed 4480K, reserved 1056768K
 *   class space    used 241K, capacity 384K, committed 384K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * 2) Parallel:
 * </p>
 * 
 * <pre>
 * Heap
 *  PSYoungGen      total 149504K, used 2570K [0x0000000719d00000, 0x0000000724380000, 0x00000007c0000000)
 *   eden space 128512K, 2% used [0x0000000719d00000,0x0000000719f82960,0x0000000721a80000)
 *   from space 20992K, 0% used [0x0000000721a80000,0x0000000721a80000,0x0000000722f00000)
 *   to   space 20992K, 0% used [0x0000000722f00000,0x0000000722f00000,0x0000000724380000)
 *  ParOldGen       total 341504K, used 269K [0x00000005cd600000, 0x00000005e2380000, 0x0000000719d00000)
 *   object space 341504K, 0% used [0x00000005cd600000,0x00000005cd6436a0,0x00000005e2380000)
 *  Metaspace       used 3080K, capacity 4486K, committed 4864K, reserved 1056768K
 *   class space    used 294K, capacity 386K, committed 512K, reserved 1048576K
 * </pre>
 *
 * <p>
 * 3) CMS:
 * </p>
 * 
 * <pre>
 * Heap
 *  par new generation   total 153600K, used 5463K [0x00000005cda00000, 0x00000005d80a0000, 0x0000000601a00000)
 *   eden space 136576K,   4% used [0x00000005cda00000, 0x00000005cdf55da8, 0x00000005d5f60000)
 *   from space 17024K,   0% used [0x00000005d5f60000, 0x00000005d5f60000, 0x00000005d7000000)
 *   to   space 17024K,   0% used [0x00000005d7000000, 0x00000005d7000000, 0x00000005d80a0000)
 *  concurrent mark-sweep generation total 341376K, used 0K [0x0000000601a00000, 0x0000000616760000, 0x00000007c0000000)
 *  Metaspace       used 2469K, capacity 4480K, committed 4480K, reserved 1056768K
 *   class space    used 241K, capacity 384K, committed 384K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * 4) G1:
 * </p>
 * 
 * <pre>
 * [25.016s][info][gc,heap,exit  ] Heap
 * [25.016s][info][gc,heap,exit  ]  garbage-first heap   total 59392K, used 38015K [0x00000000fc000000, 0x0000000100000000)
 * [25.016s][info][gc,heap,exit  ]   region size 1024K, 13 young (13312K), 1 survivors (1024K)
 * [25.016s][info][gc,heap,exit  ]  Metaspace       used 11079K, capacity 11287K, committed 11520K, reserved 1060864K
 * [25.016s][info][gc,heap,exit  ]   class space    used 909K, capacity 995K, committed 1024K, reserved 1048576K
 * </pre>
 *
 * <p>
 * 5) Shenandoah:
 * </p>
 * 
 * <pre>
 * Heap
 * Shenandoah Heap
 *  7974M max, 7974M soft max, 500M committed, 768K used
 *  3987 x 2048K regions
 * Status: cancelled
 * Reserved region:
 *  - [0x00000005cda00000, 0x00000007c0000000)
 * Collection set:
 *  - map (vanilla): 0x0000000000012e6d
 *  - map (biased):  0x0000000000010000
 * 
 *  Metaspace       used 2469K, capacity 4480K, committed 4480K, reserved 1056768K
 *   class space    used 241K, capacity 384K, committed 384K, reserved 1048576K
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class HeapEvent implements HeaderEvent, ThrowAwayEvent {

    /**
     * Regular expression for the header.
     */
    public static final String _REGEX_HEADER = "^Heap$";

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX[] = {
            //
            _REGEX_HEADER,
            //
            "^ garbage-first heap   total " + JdkRegEx.SIZE + ", used " + JdkRegEx.SIZE + " \\[" + JdkRegEx.ADDRESS
                    + ", " + JdkRegEx.ADDRESS + "(, " + JdkRegEx.ADDRESS + ")?\\)[ ]*$",
            //
            "^  region size " + JdkRegEx.SIZE + ", \\d{1,} young \\(" + JdkRegEx.SIZE + "\\), \\d{1,} survivors \\("
                    + JdkRegEx.SIZE + "\\)[ ]*$",
            //
            " - \\[" + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)[ ]*$",
            //
            "Shenandoah Heap$",
            //
            "^ " + JdkRegEx.SIZE + " (total|max)(, " + JdkRegEx.SIZE + " soft max)?, " + JdkRegEx.SIZE + " committed, "
                    + JdkRegEx.SIZE + " used$",
            //
            "^(" + JdkRegEx.DECORATOR + " )? Metaspace       used " + JdkRegEx.SIZE + "(, capacity " + JdkRegEx.SIZE
                    + ")?, committed " + JdkRegEx.SIZE + ", reserved " + JdkRegEx.SIZE + "$",
            //
            "^ \\d{1,} x " + JdkRegEx.SIZE + " regions$",
            //
            "^(" + JdkRegEx.DECORATOR + " )?  class space    used " + JdkRegEx.SIZE + "(, capacity " + JdkRegEx.SIZE
                    + ")?, " + "committed " + JdkRegEx.SIZE + ", reserved " + JdkRegEx.SIZE + "$",
            //
            "^ ((concurrent mark-sweep|def new|par new|tenured) generation)" + "[ ]{1,}total " + JdkRegEx.SIZE
                    + ", used " + JdkRegEx.SIZE + " " + "\\[" + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + ", "
                    + JdkRegEx.ADDRESS + "\\)$",
            //
            "^ (compacting perm gen|concurrent-mark-sweep perm gen|ParOldGen|PSOldGen|PSPermGen|PSYoungGen)"
                    + "[ ]{1,}total " + JdkRegEx.SIZE + ", used " + JdkRegEx.SIZE + " " + "\\[" + JdkRegEx.ADDRESS
                    + ", " + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)$",
            //
            "^  (eden|from|object| the|to  ) space " + JdkRegEx.SIZE + ",[ ]{1,3}\\d{1,3}% used \\[" + JdkRegEx.ADDRESS
                    + ",[ ]{0,1}" + JdkRegEx.ADDRESS + ",[ ]{0,1}" + JdkRegEx.ADDRESS + "(,[ ]{0,1}" + JdkRegEx.ADDRESS
                    + ")?\\)$",
            //
            "^Status:(,{0,1} (cancelled|concurrent weak roots|evacuating|has forwarded objects|marking|"
                    + "updating refs)){1,}$",

            "^Reserved region:$",
            //
            "^Collection set:$",
            //
            "^ - map \\((biased|vanilla)\\):[ ]{1,2}" + JdkRegEx.ADDRESS + "$",
            //
            "^No shared spaces configured.$"
            //
    };

    private static final List<Pattern> REGEX_PATTERN_LIST = new ArrayList<>(REGEX.length);

    static {
        for (String regex : REGEX) {
            REGEX_PATTERN_LIST.add(Pattern.compile(regex));
        }
    }

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        for (int i = 0; i < REGEX_PATTERN_LIST.size(); i++) {
            Pattern pattern = REGEX_PATTERN_LIST.get(i);
            if (pattern.matcher(logLine).matches()) {
                match = true;
                break;
            }
        }
        return match;
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
    public HeapEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.HEAP;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isHeader() {
        boolean isHeader = false;
        if (this.logEntry != null) {
            isHeader = logEntry.matches(_REGEX_HEADER);
        }
        return isHeader;
    }
}