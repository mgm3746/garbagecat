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
 * HEAP_AT_GC
 * </p>
 * 
 * <p>
 * <code>-XX:+PrintHeapAtGC</code> logging. This data is currently not being used for any analysis.
 * </p>
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent}:
 * </p>
 * 
 * <pre>
 * 49830.933: [Full GC {Heap before gc invocations=146:
 *  par new generation   total 785728K, used 310127K [0x00002aabdaab0000, 0x00002aac0aab0000, 0x00002aac0aab0000)
 *   eden space 785024K,  39% used [0x00002aabdaab0000, 0x00002aabed98be48, 0x00002aac0a950000)
 *   from space 704K,   0% used [0x00002aac0aa00000, 0x00002aac0aa00000, 0x00002aac0aab0000)
 *   to   space 704K,   0% used [0x00002aac0a950000, 0x00002aac0a950000, 0x00002aac0aa00000)
 *  concurrent mark-sweep generation total 3407872K, used 1640998K [0x00002aac0aab0000, 0x00002aacdaab0000, 0x00002aacdaab0000)
 *  concurrent-mark-sweep perm gen total 786432K, used 507386K [0x00002aacdaab0000, 0x00002aad0aab0000, 0x00002aad0aab0000)
 * 49830.934: [CMS: 1640998K-&gt;1616248K(3407872K), 11.0964500 secs] 1951125K-&gt;1616248K(4193600K), [CMS Perm : 507386K-&gt;499194K(786432K)]Heap after gc invocations=147:
 *  par new generation   total 785728K, used 0K [0x00002aabdaab0000, 0x00002aac0aab0000, 0x00002aac0aab0000)
 *   eden space 785024K,   0% used [0x00002aabdaab0000, 0x00002aabdaab0000, 0x00002aac0a950000)
 *   from space 704K,   0% used [0x00002aac0aa00000, 0x00002aac0aa00000, 0x00002aac0aab0000)
 *   to   space 704K,   0% used [0x00002aac0a950000, 0x00002aac0a950000, 0x00002aac0aa00000)
 *  concurrent mark-sweep generation total 3407872K, used 1616248K [0x00002aac0aab0000, 0x00002aacdaab0000, 0x00002aacdaab0000)
 *  concurrent-mark-sweep perm gen total 786432K, used 499194K [0x00002aacdaab0000, 0x00002aad0aab0000, 0x00002aad0aab0000)
 * }
 * , 11.0980780 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 49830.933: [Full GC 49830.934: [CMS: 1640998K-&gt;1616248K(3407872K), 11.0964500 secs] 1951125K-&gt;1616248K(4193600K), [CMS Perm : 507386K-&gt;499194K(786432K)], 11.0980780 secs]
 * </pre>
 * 
 * <p>
 * 2) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent}:
 * </p>
 * 
 * <pre>
 * 27067.966: [GC {Heap before gc invocations=498:
 *  par new generation   total 261952K, used 261760K [0x80b90000, 0x90b90000, 0x90b90000)
 *   eden space 261760K, 100% used [0x80b90000, 0x90b30000, 0x90b30000)
 *   from space 192K,   0% used [0x90b30000, 0x90b30000, 0x90b60000)
 *   to   space 192K,   0% used [0x90b60000, 0x90b60000, 0x90b90000)
 *  concurrent mark-sweep generation total 1179648K, used 1147900K [0x90b90000, 0xd8b90000, 0xd8b90000)
 *  concurrent-mark-sweep perm gen total 262144K, used 71227K [0xd8b90000, 0xe8b90000, 0xf0b90000)
 * 27067.966: [ParNew: 261760K-&gt;261760K(261952K), 0.0000160 secs]27067.966: [CMS27067.966: [CMS-concurrent-abortable-preclean: 2.272/29.793 secs]
 *  (concurrent mode failure): 1147900K-&gt;1155037K(1179648K), 7.3953900 secs] 1409660K-&gt;1155037K(1441600K)Heap after gc invocations=499:
 *  par new generation   total 261952K, used 0K [0x80b90000, 0x90b90000, 0x90b90000)
 *   eden space 261760K,   0% used [0x80b90000, 0x80b90000, 0x90b30000)
 *   from space 192K,   0% used [0x90b30000, 0x90b30000, 0x90b60000)
 *   to   space 192K,   0% used [0x90b60000, 0x90b60000, 0x90b90000)
 *  concurrent mark-sweep generation total 1179648K, used 1155037K [0x90b90000, 0xd8b90000, 0xd8b90000)
 *  concurrent-mark-sweep perm gen total 262144K, used 71187K [0xd8b90000, 0xe8b90000, 0xf0b90000)
 * }
 * , 7.3957620 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 27067.966: [GC 27067.966: [ParNew: 261760K-&gt;261760K(261952K), 0.0000160 secs]27067.966: [CMS27067.966: [CMS-concurrent-abortable-preclean: 2.272/29.793 secs] (concurrent mode failure): 1147900K-&gt;1155037K(1179648K), 7.3953900 secs] 1409660K-&gt;1155037K(1441600K), 7.3957620 secs]
 * </pre>
 * 
 * <p>
 * 3) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent} with concurrent mode failure trigger:
 * </p>
 * 
 * <pre>
 * 28282.075: [Full GC {Heap before gc invocations=528:
 *  par new generation   total 261952K, used 261760K [0x80b90000, 0x90b90000, 0x90b90000)
 *   eden space 261760K, 100% used [0x80b90000, 0x90b30000, 0x90b30000)
 *   from space 192K,   0% used [0x90b60000, 0x90b60000, 0x90b90000)
 *   to   space 192K,   0% used [0x90b30000, 0x90b30000, 0x90b60000)
 *  concurrent mark-sweep generation total 1179648K, used 1179601K [0x90b90000, 0xd8b90000, 0xd8b90000)
 *  concurrent-mark-sweep perm gen total 262144K, used 71172K [0xd8b90000, 0xe8b90000, 0xf0b90000)
 * 28282.075: [CMS28284.687: [CMS-concurrent-preclean: 3.706/3.706 secs]
 *  (concurrent mode failure): 1179601K-&gt;1179648K(1179648K), 10.7510650 secs] 1441361K-&gt;1180553K(1441600K), [CMS Perm : 71172K-&gt;71171K(262144K)]Heap after gc invocations=529:
 *  par new generation   total 261952K, used 905K [0x80b90000, 0x90b90000, 0x90b90000)
 *   eden space 261760K,   0% used [0x80b90000, 0x80c727c0, 0x90b30000)
 *   from space 192K,   0% used [0x90b60000, 0x90b60000, 0x90b90000)
 *   to   space 192K,   0% used [0x90b30000, 0x90b30000, 0x90b60000)
 *  concurrent mark-sweep generation total 1179648K, used 1179648K [0x90b90000, 0xd8b90000, 0xd8b90000)
 *  concurrent-mark-sweep perm gen total 262144K, used 71171K [0xd8b90000, 0xe8b90000, 0xf0b90000)
 * }
 * , 10.7515460 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 28282.075: [Full GC 28282.075 (concurrent mode failure): 1179601K-&gt;1179648K(1179648K), 10.7510650 secs] 1441361K-&gt;1180553K(1441600K), [CMS Perm : 71172K-&gt;71171K(262144K)], 10.7515460 secs]
 * </pre>
 * 
 * <p>
 * 4) Underlying {@link org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent}:
 * </p>
 * 
 * <pre>
 * 28308.701: [GC {Heap before gc invocations=529:
 *  par new generation   total 261952K, used 261951K [0x80b90000, 0x90b90000, 0x90b90000)
 *   eden space 261760K, 100% used [0x80b90000, 0x90b30000, 0x90b30000)
 *   from space 192K,  99% used [0x90b60000, 0x90b8ff28, 0x90b90000)
 *   to   space 192K,   0% used [0x90b30000, 0x90b30000, 0x90b60000)
 *  concurrent mark-sweep generation total 1179648K, used 1160073K [0x90b90000, 0xd8b90000, 0xd8b90000)
 *  concurrent-mark-sweep perm gen total 262144K, used 71172K [0xd8b90000, 0xe8b90000, 0xf0b90000)
 * 28308.701: [ParNew (promotion failed): 261951K-&gt;261951K(261952K), 0.7470390 secs]28309.448: [CMS28312.544: [CMS-concurrent-mark: 5.114/5.863 secs]
 *  (concurrent mode failure): 1179212K-&gt;1179647K(1179648K), 10.7159890 secs] 1422025K-&gt;1183557K(1441600K)Heap after gc invocations=530:
 *  par new generation   total 261952K, used 3909K [0x80b90000, 0x90b90000, 0x90b90000)
 *   eden space 261760K,   1% used [0x80b90000, 0x80f614f8, 0x90b30000)
 *   from space 192K,   0% used [0x90b60000, 0x90b60000, 0x90b90000)
 *   to   space 192K,   0% used [0x90b30000, 0x90b30000, 0x90b60000)
 *  concurrent mark-sweep generation total 1179648K, used 1179647K [0x90b90000, 0xd8b90000, 0xd8b90000)
 *  concurrent-mark-sweep perm gen total 262144K, used 71172K [0xd8b90000, 0xe8b90000, 0xf0b90000)
 * }
 * , 11.4633890 secs]
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 * 
 * <pre>
 * 28308.701: [GC 28308.701: [ParNew (promotion failed): 261951K-&gt;261951K(261952K), 0.7470390 secs]28309.448: [CMS28312.544: [CMS-concurrent-mark: 5.114/5.863 secs] (concurrent mode failure): 1179212K-&gt;1179647K(1179648K), 10.7159890 secs] 1422025K-&gt;1183557K(1441600K), 11.4633890 secs]
 * </pre>
 * 
 * <p>
 * 5) With -XX:+PrintGCDateStamps and enumerating (zero-based) the number of full collections:
 * </p>
 * 
 * <pre>
 * {Heap before GC invocations=0 (full 0):
 *  par new generation   total 169600K, used 150784K [0xb0c50000, 0xbc450000, 0xbc450000)
 *   eden space 150784K, 100% used [0xb0c50000, 0xb9f90000, 0xb9f90000)
 *   from space 18816K,   0% used [0xb9f90000, 0xb9f90000, 0xbb1f0000)
 *   to   space 18816K,   0% used [0xbb1f0000, 0xbb1f0000, 0xbc450000)
 *  concurrent mark-sweep generation total 860160K, used 0K [0xbc450000, 0xf0c50000, 0xf0c50000)
 *  concurrent-mark-sweep perm gen total 11392K, used 11291K [0xf0c50000, 0xf1770000, 0xf4c50000)
 * 2010-02-26T08:31:51.990-0600: [GC [ParNew: 150784K-&gt;4291K(169600K), 0.0246670 secs] 150784K-&gt;4291K(1029760K), 0.0247500 secs] [Times: user=0.06 sys=0.01, real=0.02 secs] 
 * Heap after GC invocations=1 (full 0):
 *  par new generation   total 169600K, used 4291K [0xb0c50000, 0xbc450000, 0xbc450000)
 *   eden space 150784K,   0% used [0xb0c50000, 0xb0c50000, 0xb9f90000)
 *   from space 18816K,  22% used [0xbb1f0000, 0xbb620eb0, 0xbc450000)
 *   to   space 18816K,   0% used [0xb9f90000, 0xb9f90000, 0xbb1f0000)
 *  concurrent mark-sweep generation total 860160K, used 0K [0xbc450000, 0xf0c50000, 0xf0c50000)
 *  concurrent-mark-sweep perm gen total 11392K, used 11291K [0xf0c50000, 0xf1770000, 0xf4c50000)
 * }
 * </pre>
 * 
 * <p>
 * 6) With Class Data Sharing (CDS) information:
 * </p>
 * 
 * <pre>
 * 19.494: [ParNew: 308477K-&gt;25361K(309696K), 0.0911180 secs] 326271K-&gt;75121K(1014208K)Heap after gc invocations=4:
 *  par new generation   total 309696K, used 25361K [0x00002aab1aab0000, 0x00002aab2fab0000, 0x00002aab2fab0000)
 *   eden space 275328K,   0% used [0x00002aab1aab0000, 0x00002aab1aab0000, 0x00002aab2b790000)
 *   from space 34368K,  73% used [0x00002aab2b790000, 0x00002aab2d0544a8, 0x00002aab2d920000)
 *   to   space 34368K,   0% used [0x00002aab2d920000, 0x00002aab2d920000, 0x00002aab2fab0000)
 *  tenured generation   total 704512K, used 49760K [0x00002aab2fab0000, 0x00002aab5aab0000, 0x00002aab7aab0000)
 *    the space 704512K,   7% used [0x00002aab2fab0000, 0x00002aab32b482a0, 0x00002aab32b48400, 0x00002aab5aab0000)
 *  compacting perm gen  total 262144K, used 65340K [0x00002aab7aab0000, 0x00002aab8aab0000, 0x00002aab8aab0000)
 *    the space 262144K,  24% used [0x00002aab7aab0000, 0x00002aab7ea7f138, 0x00002aab7ea7f200, 0x00002aab8aab0000)
 * No shared spaces configured.
 * }
 * </pre>
 * 
 * <p>
 * 7) G1 collector verbose:
 * </p>
 * 
 * <pre>
 * Heap
 *  garbage-first heap   total 60416K, used 6685K [0x00007f9128c00000, 0x00007f912c700000, 0x00007f9162e00000)
 *   region size 1024K, 6 young (6144K), 1 survivors (1024K)
 *  compacting perm gen  total 20480K, used 7323K [0x00007f9162e00000, 0x00007f9164200000, 0x00007f9168000000)
 *    the space 20480K,  35% used [0x00007f9162e00000, 0x00007f9163526df0, 0x00007f9163526e00, 0x00007f9164200000)
 * No shared spaces configured.
 * </pre>
 * 
 * <p>
 * 8) G1 collector minimized output:
 * </p>
 * 
 * <pre>
 *  garbage-first heap   total 60416K, used 6685K [0x00007f9128c00000, 0x00007f912c700000, 0x00007f9162e00000)
 *   region size 1024K, 6 young (6144K), 1 survivors (1024K)
 * </pre>
 * 
 * <p>
 * 9) JDK8 with "Metaspace" and "class space":
 * </p>
 * 
 * <pre>
 *  
 * Heap
 *  par new generation   total 1382400K, used 497192K [0x00000005c0000000, 0x000000061dc00000, 0x000000061dc00000)
 *   eden space 1228800K,  36% used [0x00000005c0000000, 0x00000005db3f6d28, 0x000000060b000000)
 *   from space 153600K,  33% used [0x0000000614600000, 0x00000006177932e8, 0x000000061dc00000)
 *   to   space 153600K,   0% used [0x000000060b000000, 0x000000060b000000, 0x0000000614600000)
 *  concurrent mark-sweep generation total 6852608K, used 800330K [0x000000061dc00000, 0x00000007c0000000, 0x00000007c0000000)
 *  Metaspace       used 73096K, capacity 79546K, committed 79732K, reserved 1118208K
 *   class space    used 8643K, capacity 10553K, committed 10632K, reserved 1048576K
 * 
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class HeapAtGcEvent implements ThrowAwayEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String[] REGEX = {
            //
            "^ (PSYoungGen|PSOldGen|ParOldGen|PSPermGen)[ ]+total " + JdkRegEx.SIZE + ", used " + JdkRegEx.SIZE + ".+$",
            //
            "^ (par new generation|concurrent mark-sweep generation|concurrent-mark-sweep perm gen"
                    + "|tenured generation|compacting perm gen)" + "[ ]+total " + JdkRegEx.SIZE + ", used "
                    + JdkRegEx.SIZE + ".+$",
            //
            "^  (eden|from|to|object| the)[ ]+space " + JdkRegEx.SIZE + ",[ ]+\\d{1,3}% used.+$",
            //
            "^ (Metaspace| class space)[ ]+used " + JdkRegEx.SIZE + ", capacity " + JdkRegEx.SIZE + ", committed "
                    + JdkRegEx.SIZE + ", reserved " + JdkRegEx.SIZE + "$",
            //
            "^}$",
            //
            "^\\{Heap before (GC|gc) invocations=\\d{1,10}( \\(full \\d{1,10}\\))?:$",
            //
            "^Heap after (GC|gc) invocations=\\d{1,10}( \\(full \\d{1,10}\\))?:$",
            //
            "No shared spaces configured.",
            //
            "^Heap$",
            //
            "^ garbage-first heap   total.+$",
            //
            "^  region size .+$",
            //
            "^  the space.+$" };

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
    public HeapAtGcEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.HEAP_AT_GC.toString();
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
