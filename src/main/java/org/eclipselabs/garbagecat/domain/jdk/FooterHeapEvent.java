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
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;

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
 * <p>
 * 1) G1:
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
 * 2) Shenandoah JDK8:
 * </p>
 * 
 * <pre>
 * Heap
 * Shenandoah Heap
 *  128M total, 128M committed, 102M used
 *  512 x 256K regions
 * Status: has forwarded objects, cancelled
 * Reserved region:
 *  - [0x00000000f8000000, 0x0000000100000000)
 * Collection set:
 *  - map (vanilla): 0x00007f271b2e5e00
 *  - map (biased):  0x00007f271b2e2000
 * </pre>
 * 
 * <p>
 * 3) Shenandoah Unified:
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
 * 4) Serial:
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
 * 5) Parallel Serial:
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
 * 6) Parallel Serial Compacting:
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
 * 7) CMS:
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
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class FooterHeapEvent implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String REGEX[] = {
            //
            "^" + UnifiedRegEx.DECORATOR + "  garbage-first heap   total " + JdkRegEx.SIZE + ", used " + JdkRegEx.SIZE
                    + " \\[" + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)[ ]*$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   region size " + JdkRegEx.SIZE + ", \\d{1,3} young \\(" + JdkRegEx.SIZE
                    + "\\), \\d{1,2} survivors \\(" + JdkRegEx.SIZE + "\\)[ ]*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )? - \\[" + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)[ ]*$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?(Shenandoah )?[h|H]eap$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )? " + JdkRegEx.SIZE + " (total|max)(, " + JdkRegEx.SIZE + " soft max)?, "
                    + JdkRegEx.SIZE + " committed, " + JdkRegEx.SIZE + " used$",
            //
            "^" + UnifiedRegEx.DECORATOR + "  Metaspace       used " + JdkRegEx.SIZE + ", capacity " + JdkRegEx.SIZE
                    + ", committed " + JdkRegEx.SIZE + ", reserved " + JdkRegEx.SIZE + "$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )? \\d{1,4} x " + JdkRegEx.SIZE + " regions$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   class space    used " + JdkRegEx.SIZE + ", capacity " + JdkRegEx.SIZE
                    + ", " + "committed " + JdkRegEx.SIZE + ", reserved " + JdkRegEx.SIZE + "$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "  ((concurrent mark-sweep|def new|par new|tenured) generation|ParOldGen|PSOldGen|PSYoungGen)"
                    + "[ ]{1,8}total " + JdkRegEx.SIZE + ", used " + JdkRegEx.SIZE + " " + "\\[" + JdkRegEx.ADDRESS
                    + ", " + JdkRegEx.ADDRESS + ", " + JdkRegEx.ADDRESS + "\\)$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   (eden|from|object| the|to  ) space " + JdkRegEx.SIZE
                    + ",[ ]{1,3}\\d{1,3}% used \\[" + JdkRegEx.ADDRESS + ",[ ]{0,1}" + JdkRegEx.ADDRESS + ",[ ]{0,1}"
                    + JdkRegEx.ADDRESS + "(,[ ]{0,1}" + JdkRegEx.ADDRESS + ")?\\)$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Status: (has forwarded objects, )?cancelled$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Reserved region:$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )?Collection set:$",
            //
            "^(" + UnifiedRegEx.DECORATOR + " )? - map \\((biased|vanilla)\\):[ ]{1,2}" + JdkRegEx.ADDRESS + "$"
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
        boolean match = false;
        for (int i = 0; i < REGEX.length; i++) {
            if (logLine.matches(REGEX[i])) {
                match = true;
                break;
            }
        }
        return match;
    }
}
