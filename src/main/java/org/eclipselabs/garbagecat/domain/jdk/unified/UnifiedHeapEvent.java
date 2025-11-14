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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.EventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

/**
 * <p>
 * UNIFIED_HEAP
 * </p>
 * 
 * <p>
 * Heap information at the end of unified detailed gc logging (<code>-Xlog:gc*:file=&lt;file&gt;</code>).
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) Serial:
 * </p>
 * 
 * <pre>
 * [32.839s][info][gc,heap,exit   ] Heap
 * [32.839s][info][gc,heap,exit   ]  def new generation   total 11456K, used 4604K [0x00000000fc000000, 0x00000000fcc60000, 0x00000000fd550000)
 * [32.839s][info][gc,heap,exit   ]   eden space 10240K,  43% used [0x00000000fc000000, 0x00000000fc463ed8, 0x00000000fca00000)
 * [32.839s][info][gc,heap,exit   ]   from space 1216K,   8% used [0x00000000fca00000, 0x00000000fca1b280, 0x00000000fcb30000)
 * [32.839s][info][gc,heap,exit   ]   to   space 1216K,   0% used [0x00000000fcb30000, 0x00000000fcb30000, 0x00000000fcc60000)
 * [32.839s][info][gc,heap,exit   ]  tenured generation   total 25240K, used 24218K [0x00000000fd550000, 0x00000000fedf6000, 0x0000000100000000)
 * [32.839s][info][gc,heap,exit   ]    the space 25240K,  95% used [0x00000000fd550000, 0x00000000fecf6b58, 0x00000000fecf6c00, 0x00000000fedf6000)
 * [32.839s][info][gc,heap,exit   ]  Metaspace       used 4109K, capacity 7271K, committed 7296K, reserved 1056768K
 * [32.839s][info][gc,heap,exit   ]   class space    used 299K, capacity 637K, committed 640K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * JDK25
 * </p>
 * 
 * <pre>
 * [2025-10-30T12:12:08.703-0400] Heap
 * [2025-10-30T12:12:08.703-0400]  DefNew     total 1152K, used 594K [0x00000000fc000000, 0x00000000fc140000, 0x00000000fd550000)
 * [2025-10-30T12:12:08.703-0400]   eden space 1024K,  45% used [0x00000000fc000000, 0x00000000fc074910, 0x00000000fc100000)
 * [2025-10-30T12:12:08.703-0400]   from space 128K, 100% used [0x00000000fc100000, 0x00000000fc120000, 0x00000000fc120000)
 * [2025-10-30T12:12:08.703-0400]   to   space 128K,   0% used [0x00000000fc120000, 0x00000000fc120000, 0x00000000fc140000)
 * [2025-10-30T12:12:08.703-0400]  Tenured    total 18036K, used 18016K [0x00000000fd550000, 0x00000000fe6ed000, 0x0000000100000000)
 * [2025-10-30T12:12:08.703-0400]   the  space 18036K,  99% used [0x00000000fd550000, 0x00000000fe6e8288, 0x00000000fe6ed000)
 * [2025-10-30T12:12:08.703-0400]  Metaspace       used 3161K, committed 3392K, reserved 1114112K
 * [2025-10-30T12:12:08.703-0400]   class space    used 203K, committed 320K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * 2) Parallel Serial:
 * </p>
 * 
 * <pre>
 * [37.098s][info][gc,heap,exit   ] Heap
 * [37.098s][info][gc,heap,exit   ]  PSYoungGen      total 20992K, used 7054K [0x00000000feb00000, 0x0000000100000000, 0x0000000100000000)
 * [37.098s][info][gc,heap,exit   ]   eden space 20480K, 33% used [0x00000000feb00000,0x00000000ff1cb940,0x00000000fff00000)
 * [37.098s][info][gc,heap,exit   ]   from space 512K, 18% used [0x00000000fff80000,0x00000000fff98000,0x0000000100000000)
 * [37.098s][info][gc,heap,exit   ]   to   space 512K, 0% used [0x00000000fff00000,0x00000000fff00000,0x00000000fff80000)
 * [37.098s][info][gc,heap,exit   ]  PSOldGen        total 32768K, used 27239K [0x00000000fc000000, 0x00000000fe000000, 0x00000000feb00000)
 * [37.098s][info][gc,heap,exit   ]   object space 32768K, 83% used [0x00000000fc000000,0x00000000fda99f58,0x00000000fe000000)
 * [37.098s][info][gc,heap,exit   ]  Metaspace       used 4222K, capacity 7436K, committed 7680K, reserved 1056768K
 * [37.098s][info][gc,heap,exit   ]   class space    used 309K, capacity 671K, committed 768K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * 3) Parallel Serial Compacting:
 * </p>
 * 
 * <pre>
 * [37.742s][info][gc,heap,exit   ] Heap
 * [37.742s][info][gc,heap,exit   ]  PSYoungGen      total 20992K, used 7500K [0x00000000feb00000, 0x0000000100000000, 0x0000000100000000)
 * [37.742s][info][gc,heap,exit   ]   eden space 20480K, 35% used [0x00000000feb00000,0x00000000ff233060,0x00000000fff00000)
 * [37.742s][info][gc,heap,exit   ]   from space 512K, 25% used [0x00000000fff80000,0x00000000fffa0000,0x0000000100000000)
 * [37.742s][info][gc,heap,exit   ]   to   space 512K, 0% used [0x00000000fff00000,0x00000000fff00000,0x00000000fff80000)
 * [37.742s][info][gc,heap,exit   ]  ParOldGen       total 30720K, used 27745K [0x00000000fc000000, 0x00000000fde00000, 0x00000000feb00000)
 * [37.742s][info][gc,heap,exit   ]   object space 30720K, 90% used [0x00000000fc000000,0x00000000fdb18680,0x00000000fde00000)
 * [37.742s][info][gc,heap,exit   ]  Metaspace       used 4218K, capacity 7436K, committed 7680K, reserved 1056768K
 * [37.742s][info][gc,heap,exit   ]   class space    used 309K, capacity 671K, committed 768K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * 4) CMS:
 * </p>
 * 
 * <pre>
 * [59.713s][info][gc,heap,exit ] Heap
 * [59.713s][info][gc,heap,exit ]  par new generation   total 1152K, used 713K [0x00000000fc000000, 0x00000000fc140000, 0x00000000fd550000)
 * [59.713s][info][gc,heap,exit ]   eden space 1024K,  67% used [0x00000000fc000000, 0x00000000fc0ac590, 0x00000000fc100000)
 * [59.713s][info][gc,heap,exit ]   from space 128K,  18% used [0x00000000fc120000, 0x00000000fc1260c0, 0x00000000fc140000)
 * [59.713s][info][gc,heap,exit ]   to   space 128K,   0% used [0x00000000fc100000, 0x00000000fc100000, 0x00000000fc120000)
 * [59.713s][info][gc,heap,exit ]  concurrent mark-sweep generation total 31228K, used 25431K [0x00000000fd550000, 0x00000000ff3cf000, 0x0000000100000000)
 * [59.713s][info][gc,heap,exit ]  Metaspace       used 4223K, capacity 7436K, committed 7680K, reserved 1056768K
 * [59.713s][info][gc,heap,exit ]   class space    used 309K, capacity 671K, committed 768K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * 5) G1:
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
 * JDK25
 * </p>
 *
 * <p>
 * [1.429s] garbage-first heap total reserved 98304K, committed 53248K, used 37940K [0x00000000fa000000,
 * 0x0000000100000000)
 * </p>
 * 
 * <p>
 * 6) G1 NUMA Support Enabled:
 * </p>
 * 
 * <pre>
 * [123.766s][info][gc,heap,exit] Heap 
 * [123.766s][info][gc,heap,exit]  garbage-first heap   total 16777216K, used 13278872K [0x00000003c0000000, 0x00000007c0000000)
 * [123.766s][info][gc,heap,exit]   region size 32768K, 5 young (163840K), 1 survivors (32768K)
 * [123.766s][info][gc,heap,exit]   remaining free region(s) on each NUMA node: 0=28 1=24 2=26 3=24 
 * [123.766s][info][gc,heap,exit]  Metaspace       used 13231K, committed 13632K, reserved 1114112K 
 * [123.766s][info][gc,heap,exit]   class space    used 1257K, committed 1408K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * 7) Shenandoah:
 * </p>
 * 
 * <pre>
 * [103.682s][info][gc,heap,exit ] Heap
 * [103.682s][info][gc,heap,exit ] Shenandoah Heap
 * [103.682s][info][gc,heap,exit ]  65536K total, 65536K committed, 50162K used
 * [103.682s][info][gc,heap,exit ]  256 x 256K regions
 * [103.682s][info][gc,heap,exit ] Status: has forwarded objects, cancelled
 * [103.682s][info][gc,heap,exit ] Reserved region:
 * [103.682s][info][gc,heap,exit ]  - [0x00000000fc000000, 0x0000000100000000)
 * [103.682s][info][gc,heap,exit ] Collection set:
 * [103.683s][info][gc,heap,exit ]  - map (vanilla): 0x00007fa7ea119f00
 * [103.683s][info][gc,heap,exit ]  - map (biased):  0x00007fa7ea116000
 * [103.683s][info][gc,heap,exit ]
 * [103.683s][info][gc,heap,exit ]  Metaspace       used 4230K, capacity 7436K, committed 7680K, reserved 1056768K
 * [103.683s][info][gc,heap,exit ]   class space    used 309K, capacity 671K, committed 768K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * 8) Z Non-generational:
 * </p>
 * 
 * <pre>
 * [2.640s] Heap
 * [2.640s]  ZHeap           used 86M, capacity 96M, max capacity 96M
 * [2.640s]  Metaspace       used 3992K, committed 4160K, reserved 1056768K
 * [2.640s]   class space    used 315K, committed 384K, reserved 1048576K
 * </pre>
 * 
 * <p>
 * JDK25
 * </p>
 * 
 * <pre>
 * [2025-10-16T12:08:35.913+0200] Heap
 * [2025-10-16T12:08:35.913+0200]  ZHeap           used 11700M, capacity 20480M, max capacity 20480M
 * [2025-10-16T12:08:35.913+0200]   Cache          8780M (317)
 * [2025-10-16T12:08:35.913+0200]    size classes  2M (18), 4M (33), 8M (94), 16M (106), 32M (45), 64M (17), 128M (1), 256M (1), 512M (2)
 * [2025-10-16T12:08:35.913+0200]  Metaspace       used 149746K, committed 150656K, reserved 1179648K
 * [2025-10-16T12:08:35.913+0200]   class space    used 33681K, committed 34048K, reserved 104857
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class UnifiedHeapEvent implements UnifiedLogging, ThrowAwayEvent {
    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX[] = {
            //
            "^" + UnifiedRegEx.DECORATOR + "  garbage-first heap   (total " + JdkRegEx.SIZE + "|total reserved "
                    + JdkRegEx.SIZE + ", committed " + JdkRegEx.SIZE + "), used " + JdkRegEx.SIZE + " \\["
                    + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)[ ]*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   region size " + JdkRegEx.SIZE + ", \\d{1,} young \\(" + JdkRegEx.SIZE
                    + "\\), \\d{1,} survivors \\(" + JdkRegEx.SIZE + "\\)[ ]*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   remaining free region\\(s\\) on each NUMA node: 0=\\d{1,}.*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "[ ]{2,3}- \\[" + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)[ ]*$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Heap$",
            //
            "^" + UnifiedRegEx.DECORATOR + "[ ]{1,2}Shenandoah Heap$",
            //
            "^" + UnifiedRegEx.DECORATOR + "  " + JdkRegEx.SIZE + " (total|max)(, " + JdkRegEx.SIZE + " soft max)?, "
                    + JdkRegEx.SIZE + " committed, " + JdkRegEx.SIZE + " used$",
            //
            "^" + UnifiedRegEx.DECORATOR + "( [OYy]:)?  Metaspace       used " + JdkRegEx.SIZE + "(, capacity "
                    + JdkRegEx.SIZE + ")?, committed " + JdkRegEx.SIZE + ", reserved " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + "  \\d{1,} x " + JdkRegEx.SIZE + " regions$",
            //
            "^" + UnifiedRegEx.DECORATOR + "( [OYy]:)?   class space    used " + JdkRegEx.SIZE + "(, capacity "
                    + JdkRegEx.SIZE + ")?, committed " + JdkRegEx.SIZE + ", reserved " + JdkRegEx.SIZE + "$",

            //
            "^" + UnifiedRegEx.DECORATOR
                    + "  ((concurrent mark-sweep|def new|DefNew|par new|tenured) generation)[ ]{1,}total "
                    + JdkRegEx.SIZE + ", used " + JdkRegEx.SIZE + " " + "\\[" + JdkRegEx.ADDRESS + ", "
                    + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)$",
            //
            "^" + UnifiedRegEx.DECORATOR + "  (DefNew|ParOldGen|PSOldGen|PSYoungGen|Tenured)[ ]{1,}total "
                    + JdkRegEx.SIZE + ", used " + JdkRegEx.SIZE + " " + "\\[" + JdkRegEx.ADDRESS + ", "
                    + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   (eden|from|object| the|the |to  ) space " + JdkRegEx.SIZE
                    + ",[ ]{1,3}\\d{1,3}% used \\[" + JdkRegEx.ADDRESS + ",[ ]{0,1}" + JdkRegEx.ADDRESS + ",[ ]{0,1}"
                    + JdkRegEx.ADDRESS + "(,[ ]{0,1}" + JdkRegEx.ADDRESS + ")?\\)$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{1,2}Status:(,{0,1} (cancelled|concurrent weak roots|evacuating|has forwarded objects|"
                    + "marking|updating refs)){1,}$",
            //
            "^" + UnifiedRegEx.DECORATOR + "[ ]{1,2}Reserved region:$",
            //
            "^" + UnifiedRegEx.DECORATOR + "[ ]{1,2}Collection set:$",
            //
            "^" + UnifiedRegEx.DECORATOR + "[ ]{2,3}- map \\((biased|vanilla)\\):[ ]{1,2}" + JdkRegEx.ADDRESS + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + "( [OYy]:)?  ZHeap           used " + JdkRegEx.SIZE + ", capacity "
                    + JdkRegEx.SIZE + ", max capacity " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   Cache          " + JdkRegEx.SIZE + " \\(\\d{1,}\\)$",
            //
            "^" + UnifiedRegEx.DECORATOR + "    size classes  .*$"
            //

    };
    private static final List<Pattern> REGEX_PATTERN_LIST = new ArrayList<>(_REGEX.length);
    static {
        for (String regex : _REGEX) {
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
    public UnifiedHeapEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public EventType getEventType() {
        return JdkUtil.EventType.UNIFIED_HEAP;
    }

    public String getLogEntry() {
        return logEntry;
    }

    @Override
    public Tag getTag() {
        return Tag.UNKNOWN;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public boolean isEndstamp() {
        return false;
    }
}
