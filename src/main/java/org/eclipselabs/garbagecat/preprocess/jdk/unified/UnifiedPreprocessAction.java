/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk.unified;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

/**
 * <p>
 * Generic unified logging preprocessing.
 * </p>
 *
 * <h3>Example Logging</h3>
 * 
 * <p>
 * 1) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialNewEvent}:
 * </p>
 *
 * <pre>
 * [0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)
 * [0.112s][info][gc,heap        ] GC(3) DefNew: 1016K-&gt;128K(1152K)
 * [0.112s][info][gc,heap        ] GC(3) Tenured: 929K-&gt;1044K(1552K)
 * [0.112s][info][gc,metaspace   ] GC(3) Metaspace: 1222K-&gt;1222K(1056768K)
 * [0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M-&gt;1M(2M) 0.700ms
 * [0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: 512K-&gt;464K(1024K) PSOldGen: 0K-&gt;8K(512K) Metaspace: 120K-&gt;120K(1056768K) 0M-&gt;0M(1M) 1.195ms User=0.01s Sys=0.01s Real=0.00s
 * </pre>
 * 
 * <p>
 * 2) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialOldEvent} triggered by a@link
 * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialNewEvent} young generation guarantee:
 * </p>
 *
 * <pre>
 * [0.073s][info][gc,start     ] GC(1) Pause Young (Allocation Failure)
 * [0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure)
 * [0.075s][info][gc,phases,start] GC(2) Phase 1: Mark live objects
 * [0.076s][info][gc,phases      ] GC(2) Phase 1: Mark live objects 0.875ms
 * [0.076s][info][gc,phases,start] GC(2) Phase 2: Compute new object addresses
 * [0.076s][info][gc,phases      ] GC(2) Phase 2: Compute new object addresses 0.167ms
 * [0.076s][info][gc,phases,start] GC(2) Phase 3: Adjust pointers
 * [0.076s][info][gc,phases      ] GC(2) Phase 3: Adjust pointers 0.474ms
 * [0.076s][info][gc,phases,start] GC(2) Phase 4: Move objects
 * [0.076s][info][gc,phases      ] GC(2) Phase 4: Move objects 0.084ms
 * [0.076s][info][gc             ] GC(2) Pause Full (Allocation Failure) 0M-&gt;0M(2M) 1.699ms
 * [0.076s][info][gc,heap        ] GC(1) DefNew: 1152K-&gt;0K(1152K)
 * [0.076s][info][gc,heap        ] GC(1) Tenured: 458K-&gt;929K(960K)
 * [0.076s][info][gc,metaspace   ] GC(1) Metaspace: 697K-&gt;697K(1056768K)
 * [0.076s][info][gc             ] GC(1) Pause Young (Allocation Failure) 1M-&gt;0M(2M) 3.061ms
 * [0.076s][info][gc,cpu         ] GC(1) User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 *
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure) DefNew: 1152K-&gt;0K(1152K) Tenured: 458K-&gt;929K(960K) Metaspace: 697K-&gt;697K(1056768K) 1M-&gt;0M(2M) 3.061ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * 3) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelScavengeEvent} in combination with @link
 * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialOldEvent}:
 * </p>
 * 
 * <pre>
 * [15.030s][info][gc,start       ] GC(1199) Pause Young (Allocation Failure)
 * [15.031s][info][gc,heap        ] GC(1199) PSYoungGen: 20544K-&gt;64K(20992K)
 * [15.031s][info][gc,heap        ] GC(1199) PSOldGen: 15496K-&gt;15504K(17920K)
 * [15.031s][info][gc,metaspace   ] GC(1199) Metaspace: 3779K-&gt;3779K(1056768K)
 * [15.031s][info][gc             ] GC(1199) Pause Young (Allocation Failure) 35M-&gt;15M(38M) 0.402ms
 * [15.031s][info][gc,cpu         ] GC(1199) User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [0.031s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: 512K-&gt;464K(1024K) PSOldGen: 0K-&gt;8K(512K) Metaspace: 120K-&gt;120K(1056768K) 0M-&gt;0M(1M) 1.195ms User=0.01s Sys=0.01s Real=0.00s
 * </pre>
 * 
 * *
 * <p>
 * 4) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelScavengeEvent} in combination with @link
 * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelCompactingOldEvent}:
 * </p>
 * 
 * <pre>
 * [0.029s][info][gc,start     ] GC(0) Pause Young (Allocation Failure)
 * [0.030s][info][gc,heap      ] GC(0) PSYoungGen: 512K-&gt;432K(1024K)
 * [0.030s][info][gc,heap      ] GC(0) ParOldGen: 0K-&gt;8K(512K)
 * [0.030s][info][gc,metaspace ] GC(0) Metaspace: 121K-&gt;121K(1056768K)
 * [0.030s][info][gc           ] GC(0) Pause Young (Allocation Failure) 0M-&gt;0M(1M) 0.762ms
 * [0.030s][info][gc,cpu       ] GC(0) User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [0.029s][info][gc,start     ] GC(0) Pause Young (Allocation Failure) PSYoungGen: 512K-&gt;432K(1024K) ParOldGen: 0K-&gt;8K(512K) Metaspace: 121K-&gt;121K(1056768K) 0M-&gt;0M(1M) 0.762ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * *
 * <p>
 * 5) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelCompactingOldEvent}:
 * </p>
 * 
 * <pre>
 * [0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics)
 * [0.083s][info][gc,phases,start] GC(3) Marking Phase
 * [0.084s][info][gc,phases      ] GC(3) Marking Phase 1.032ms
 * [0.084s][info][gc,phases,start] GC(3) Summary Phase
 * [0.084s][info][gc,phases      ] GC(3) Summary Phase 0.005ms
 * [0.084s][info][gc,phases,start] GC(3) Adjust Roots
 * [0.084s][info][gc,phases      ] GC(3) Adjust Roots 0.666ms
 * [0.084s][info][gc,phases,start] GC(3) Compaction Phase
 * [0.087s][info][gc,phases      ] GC(3) Compaction Phase 2.540ms
 * [0.087s][info][gc,phases,start] GC(3) Post Compact
 * [0.087s][info][gc,phases      ] GC(3) Post Compact 0.012ms
 * [0.087s][info][gc,heap        ] GC(3) PSYoungGen: 502K-&gt;496K(1536K)
 * [0.087s][info][gc,heap        ] GC(3) ParOldGen: 472K-&gt;432K(2048K)
 * [0.087s][info][gc,metaspace   ] GC(3) Metaspace: 701K-&gt;701K(1056768K)
 * [0.087s][info][gc             ] GC(3) Pause Full (Ergonomics) 0M-&gt;0M(3M) 4.336ms
 * [0.087s][info][gc,cpu         ] GC(3) User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [0.083s][info][gc,start     ] GC(3) Pause Full (Ergonomics) PSYoungGen: 502K-&gt;496K(1536K) ParOldGen: 472K-&gt;432K(2048K) Metaspace: 701K-&gt;701K(1056768K) 0M-&gt;0M(3M) 4.336ms User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 * 
 * <p>
 * 6) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPauseEvent}:
 * </p>
 * 
 * <pre>
 * [0.369s][info][gc,start     ] GC(6) Pause Young (Normal) (G1 Evacuation Pause)
 * [0.369s][info][gc,task      ] GC(6) Using 2 workers of 4 for evacuation
 * [0.370s][info][gc,phases    ] GC(6)   Pre Evacuate Collection Set: 0.0ms
 * [0.370s][info][gc,phases    ] GC(6)   Evacuate Collection Set: 0.7ms
 * [0.370s][info][gc,phases    ] GC(6)   Post Evacuate Collection Set: 0.1ms
 * [0.370s][info][gc,phases    ] GC(6)   Other: 0.1ms
 * [0.370s][info][gc,heap      ] GC(6) Eden regions: 1-&gt;0(1)
 * [0.370s][info][gc,heap      ] GC(6) Survivor regions: 1-&gt;1(1)
 * [0.370s][info][gc,heap      ] GC(6) Old regions: 3-&gt;3
 * [0.370s][info][gc,heap      ] GC(6) Humongous regions: 0-&gt;0
 * [0.370s][info][gc,metaspace ] GC(6) Metaspace: 9085K-&gt;9085K(1058816K)
 * [0.370s][info][gc           ] GC(6) Pause Young (Normal) (G1 Evacuation Pause) 3M-&gt;2M(7M) 0.929ms
 * [0.370s][info][gc,cpu       ] GC(6) User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [0.369s][info][gc,start     ] GC(6) Pause Young (Normal) (G1 Evacuation Pause) Metaspace: 9085K-&gt;9085K(1058816K) 3M-&gt;2M(7M) 0.929ms User=0.01s Sys=0.00s Real=0.01s
 * </pre>
 * 
 * <p>
 * 7) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedRemarkEvent}:
 * </p>
 * 
 * <pre>
 * [16.051s][info][gc,start     ] GC(969) Pause Remark
 * [16.053s][info][gc,stringtable] GC(969) Cleaned string and symbol table, strings: 5786 processed, 4 removed, symbols: 38663 processed, 11 removed
 * [16.053s][info][gc            ] GC(969) Pause Remark 29M-&gt;29M(46M) 2.328ms
 * [16.053s][info][gc,cpu        ] GC(969) User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [16.053s][info][gc            ] GC(969) Pause Remark 29M-&gt;29M(46M) 2.328ms User=0.01s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * 8) @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1CleanupEvent}:
 * </p>
 * 
 * <pre>
 * [16.081s][info][gc,start      ] GC(969) Pause Cleanup
 * [16.082s][info][gc            ] GC(969) Pause Cleanup 28M-&gt;28M(46M) 0.064ms
 * [16.082s][info][gc,cpu        ] GC(969) User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * <p>
 * Preprocessed:
 * </p>
 *
 * <pre>
 * [16.082s][info][gc            ] GC(969) Pause Cleanup 28M-&gt;28M(46M) 0.064ms User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 *
 */
public class UnifiedPreprocessAction implements PreprocessAction {

    /**
     * Regular expression for retained beginning @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedYoungEvent}.
     * 
     * <pre>
     * [0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_PAUSE_YOUNG = "^((\\[" + JdkRegEx.DATESTAMP + "\\])?\\[("
            + JdkRegEx.TIMESTAMP + "s|" + JdkRegEx.TIMESTAMP_MILLIS + ")\\]\\[info\\]\\[gc,start[ ]{0,7}\\] "
            + JdkRegEx.GC_EVENT_NUMBER + " Pause Young \\(" + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "\\))$";

    /**
     * Regular expression for retained beginning @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialOldEvent}.
     * 
     * <pre>
     * [0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure)
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_SERIAL_OLD = "^(\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc,start[ ]{0,7}\\] " + JdkRegEx.GC_EVENT_NUMBER + " Pause Full \\("
            + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "\\))$";

    /**
     * Regular expression for retained beginning @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPauseEvent}.
     * 
     * <pre>
     * [16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause)
     * 
     * [15.069s][info][gc,start     ] GC(1190) Pause Young (Concurrent Start) (G1 Evacuation Pause)
     * 
     * [2019-05-09T01:39:00.763+0000][5355ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * 
     * [2019-05-09T01:39:07.136+0000][11728ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC)
     * 
     * [16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause)
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_G1_YOUNG = "^((\\[" + JdkRegEx.DATESTAMP + "\\])?\\[("
            + JdkRegEx.TIMESTAMP + "s|" + JdkRegEx.TIMESTAMP_MILLIS + ")\\](\\[info\\]\\[gc,start[ ]{0,7}\\])? "
            + JdkRegEx.GC_EVENT_NUMBER + " Pause Young( \\((Normal|Prepare Mixed|Mixed|Concurrent Start)\\))? \\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + ")\\))$";

    /**
     * Regular expression for retained space data.
     * 
     * <pre>
     * [0.112s][info][gc,heap        ] GC(3) DefNew: 1016K-&gt;128K(1152K)
     * 
     * [0.112s][info][gc,heap        ] GC(3) Tenured: 929K-&gt;1044K(1552K)
     * 
     * [0.032s][info][gc,heap      ] GC(0) PSYoungGen: 512K-&gt;464K(1024K)
     * 
     * [0.032s][info][gc,heap      ] GC(0) PSOldGen: 0K-&gt;8K(512K)
     * 
     * [0.032s][info][gc,metaspace ] GC(0) Metaspace: 120K-&gt;120K(1056768K)
     * 
     * [2019-05-09T01:39:00.821+0000][5413ms] GC(0) Metaspace: 26116K-&gt;26116K(278528K)
     * 
     * [0.030s][info][gc,heap      ] GC(0) ParOldGen: 0K-&gt;8K(512K)
     * 
     * [0.053s][info][gc,heap      ] GC(0) ParNew: 974K-&gt;128K(1152K)
     * 
     * [0.053s][info][gc,heap      ] GC(0) CMS: 0K-&gt;518K(960K)
     * </pre>
     */
    private static final String REGEX_RETAIN_MIDDLE_SPACE_DATA = "^(\\[" + JdkRegEx.DATESTAMP + "\\])?\\[("
            + JdkRegEx.TIMESTAMP + "s|" + JdkRegEx.TIMESTAMP_MILLIS
            + ")\\](\\[info\\]\\[gc,(heap|metaspace)[ ]{0,8}\\])? " + JdkRegEx.GC_EVENT_NUMBER
            + "( (CMS|DefNew|Metaspace|ParNew|PSYoungGen|PSOldGen|ParOldGen|Tenured): " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\))$";

    /**
     * Regular expression for retained Pause Young data.
     * 
     * <pre>
     * [0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M->1M(2M) 0.700ms
     * </pre>
     */
    private static final String REGEX_RETAIN_MIDDLE_PAUSE_YOUNG_DATA = "^\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc[ ]{0,13}\\] " + JdkRegEx.GC_EVENT_NUMBER + " Pause Young \\("
            + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "\\)( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + ")$";

    /**
     * Regular expression for retained Pause Full data.
     * 
     * <pre>
     * [0.076s][info][gc             ] GC(2) Pause Full (Allocation Failure) 0M->0M(2M) 1.699ms
     * 
     * [0.092s][info][gc             ] GC(3) Pause Full (Ergonomics) 0M->0M(3M) 1.849ms
     * </pre>
     */
    private static final String REGEX_RETAIN_MIDDLE_PAUSE_FULL_DATA = "^\\[" + JdkRegEx.TIMESTAMP
            + "s\\]\\[info\\]\\[gc[ ]{0,13}\\] " + JdkRegEx.GC_EVENT_NUMBER + " Pause Full \\(("
            + JdkRegEx.TRIGGER_ALLOCATION_FAILURE + "|" + JdkRegEx.TRIGGER_ERGONOMICS + ")\\)( " + JdkRegEx.SIZE + "->"
            + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + ")$";

    /**
     * Regular expression for retained Pause Young data.
     * 
     * [15.060s][info][gc ] GC(1189) Pause Young (Normal) (G1 Evacuation Pause) 25M->13M(31M) 0.355ms
     * 
     * [0.337s][info][gc ] GC(0) Pause Young (G1 Evacuation Pause) 25M->4M(254M) 3.523ms
     * 
     * [2019-05-09T01:39:00.821+0000][5413ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause) 65M->8M(1304M) 57.263ms
     * 
     * [2019-05-09T01:39:07.172+0000][11764ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC) 78M->22M(1304M)
     * 35.722ms
     * 
     * [16.630s][info][gc ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause) 15M->12M(31M) 1.202ms
     */
    private static final String REGEX_RETAIN_MIDDLE_G1_YOUNG_DATA = "^(\\[" + JdkRegEx.DATESTAMP + "\\])?\\[("
            + JdkRegEx.TIMESTAMP + "s|" + JdkRegEx.TIMESTAMP_MILLIS + ")\\](\\[info\\]\\[gc[ ]{0,13}\\])? "
            + JdkRegEx.GC_EVENT_NUMBER + " Pause Young( \\((Normal|Mixed|Prepare Mixed|Concurrent Start)\\))? \\(("
            + JdkRegEx.TRIGGER_G1_EVACUATION_PAUSE + "|" + JdkRegEx.TRIGGER_GCLOCKER_INITIATED_GC + ")\\)( "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_JDK9 + ")$";

    /**
     * Regular expression for retained end times data.
     * 
     * <pre>
     * [0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s
     * 
     * [2019-05-09T01:39:00.821+0000][5413ms] GC(0) User=0.02s Sys=0.01s Real=0.06s
     * </pre>
     */
    private static final String REGEX_RETAIN_END_TIMES_DATA = "^(\\[" + JdkRegEx.DATESTAMP + "\\])?\\[("
            + JdkRegEx.TIMESTAMP + "s|" + JdkRegEx.TIMESTAMP_MILLIS + ")\\](\\[info\\]\\[gc,cpu[ ]{0,9}\\])? "
            + JdkRegEx.GC_EVENT_NUMBER + TimesData.REGEX_JDK9 + "$";

    /**
     * Regular expressions for lines thrown away.
     * 
     * <pre>
     * G1:
     * 
     * [4.057s][info][gc,phases,start] GC(2264) Phase 1: Mark live objects
     * 
     * [4.062s][info][gc,phases      ] GC(2264) Phase 1: Mark live objects 4.352ms
     * 
     * [4.062s][info][gc,phases,start] GC(2264) Phase 2: Compute new object addresses
     * 
     * [4.063s][info][gc,phases      ] GC(2264) Phase 2: Compute new object addresses 1.165ms
     * 
     * [4.063s][info][gc,phases,start] GC(2264) Phase 3: Adjust pointers
     * 
     * [4.065s][info][gc,phases      ] GC(2264) Phase 3: Adjust pointers 2.453ms
     * 
     * [4.065s][info][gc,phases,start] GC(2264) Phase 4: Move objects
     * 
     * [4.067s][info][gc,phases      ] GC(2264) Phase 4: Move objects 1.248ms
     * 
     * PARALLEL_COMPACTING_OLD:
     * 
     * [0.083s][info][gc,phases,start] GC(3) Marking Phase
     * 
     * [0.084s][info][gc,phases      ] GC(3) Marking Phase 1.032ms
     * 
     * [0.084s][info][gc,phases,start] GC(3) Summary Phase
     * 
     * [0.084s][info][gc,phases      ] GC(3) Summary Phase 0.005ms
     * 
     * [0.084s][info][gc,phases,start] GC(3) Adjust Roots
     * 
     * [0.084s][info][gc,phases      ] GC(3) Adjust Roots 0.666ms
     * 
     * [0.084s][info][gc,phases,start] GC(3) Compaction Phase
     * 
     * [0.087s][info][gc,phases      ] GC(3) Compaction Phase 2.540ms
     * 
     * [0.087s][info][gc,phases,start] GC(3) Post Compact
     * 
     * [0.087s][info][gc,phases      ] GC(3) Post Compact 0.012ms
     * 
     * CMS:
     * 
     * [0.053s][info][gc,start     ] GC(1) Pause Initial Mark
     * 
     * [0.056s][info][gc,heap      ] GC(1) Old: 518K->518K(960K)
     * </pre>
     */
    private static final String[] REGEX_THROWAWAY = {
            // SERIAL
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,phases(,start)?[ ]{0,6}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Phase \\d: .+?$",
            // G1
            "^(\\[" + JdkRegEx.DATESTAMP + "\\])?\\[(" + JdkRegEx.TIMESTAMP + "s|" + JdkRegEx.TIMESTAMP_MILLIS
                    + ")\\](\\[info\\]\\[gc,task[ ]{6,7}\\])? " + JdkRegEx.GC_EVENT_NUMBER
                    + " Using \\d{1,2} workers of \\d{1,2} for (evacuation|marking)$",
            //
            "^(\\[" + JdkRegEx.DATESTAMP + "\\])?\\[(" + JdkRegEx.TIMESTAMP + "s|" + JdkRegEx.TIMESTAMP_MILLIS
                    + ")\\](\\[info\\]\\[gc,phases[ ]{4,5}\\])? " + JdkRegEx.GC_EVENT_NUMBER
                    + "   ((Pre Evacuate|Evacuate|Post Evacuate|Other) Collection Set|Other): " + JdkRegEx.DURATION_JDK9
                    + "$",
            //
            "^(\\[" + JdkRegEx.DATESTAMP + "\\])?\\[(" + JdkRegEx.TIMESTAMP + "s|" + JdkRegEx.TIMESTAMP_MILLIS
                    + ")\\](\\[info\\]\\[gc,heap[ ]{6,7}\\])? " + JdkRegEx.GC_EVENT_NUMBER
                    + " (Eden|Survivor|Old|Humongous) regions: \\d{1,3}->\\d{1,3}(\\(\\d{1,3}\\))?$",
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,start[ ]{5,6}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Pause Remark$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,stringtable\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Cleaned string and symbol table, strings: \\d{1,4} processed, \\d removed, "
                    + "symbols: \\d{1,5} processed, \\d{1,2} removed$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,start      \\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Pause Cleanup$",
            // Parallel
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,phases(,start)?[ ]{0,6}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " (Adjust Roots|Compaction Phase|Marking Phase|Post Compact|Summary Phase)( "
                    + JdkRegEx.DURATION_JDK9 + ")?$",
            // CMS
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,start[ ]{0,5}\\] " + JdkRegEx.GC_EVENT_NUMBER
                    + " Pause Initial Mark?$",
            //
            "^\\[" + JdkRegEx.TIMESTAMP + "s\\]\\[info\\]\\[gc,heap[ ]{0,6}\\] " + JdkRegEx.GC_EVENT_NUMBER + " Old: "
                    + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)$"
            //
    };

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * Log entry in the entangle log list used to indicate the current high level preprocessor (e.g. CMS, G1). This
     * context is necessary to detangle multi-line events where logging patterns are shared among preprocessors.
     * 
     * For example, it is used with the <code>UnifiedPreprocessAction</code> to identify concurrent events intermingled
     * with non-concurrent events to store them in the intermingled log lines list for output after the non-concurrent
     * event.
     */
    public static final String TOKEN = "UNIFIED_PREPROCESS_ACTION_TOKEN";

    /**
     * Create event from log entry.
     *
     * @param priorLogEntry
     *            The prior log line.
     * @param logEntry
     *            The log line.
     * @param nextLogEntry
     *            The next log line.
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @param context
     *            Information to make preprocessing decisions.
     */
    public UnifiedPreprocessAction(String priorLogEntry, String logEntry, String nextLogEntry,
            List<String> entangledLogLines, Set<String> context) {
        // Beginning logging
        if (logEntry.matches(REGEX_RETAIN_BEGINNING_PAUSE_YOUNG)) {
            // Only report young collections that do not trigger an old collection
            if (!nextLogEntry.matches(REGEX_RETAIN_BEGINNING_SERIAL_OLD)) {
                Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_PAUSE_YOUNG);
                Matcher matcher = pattern.matcher(logEntry);
                if (matcher.matches()) {
                    this.logEntry = matcher.group(1);
                }
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_SERIAL_OLD)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_SERIAL_OLD);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_BEGINNING_G1_YOUNG)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_BEGINNING_G1_YOUNG);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_SPACE_DATA)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_SPACE_DATA);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(17);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_PAUSE_YOUNG_DATA)) {
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_PAUSE_YOUNG_DATA);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                if (context.contains(TOKEN)) {
                    this.logEntry = matcher.group(2);
                } else {
                    // Single line event
                    this.logEntry = Constants.LINE_SEPARATOR + logEntry;
                }
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_PAUSE_FULL_DATA)) {
            if (nextLogEntry != null && nextLogEntry.matches(REGEX_RETAIN_END_TIMES_DATA)) {
                Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_PAUSE_FULL_DATA);
                Matcher matcher = pattern.matcher(logEntry);
                if (matcher.matches()) {
                    this.logEntry = matcher.group(3);
                }
            } else if (!context.contains(TOKEN)) {
                // Single line event
                this.logEntry = Constants.LINE_SEPARATOR + logEntry;
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (logEntry.matches(REGEX_RETAIN_MIDDLE_G1_YOUNG_DATA)) {
            // Middle logging
            Pattern pattern = Pattern.compile(REGEX_RETAIN_MIDDLE_G1_YOUNG_DATA);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(19);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (logEntry.matches(REGEX_RETAIN_END_TIMES_DATA)) {
            // End logging
            Pattern pattern = Pattern.compile(REGEX_RETAIN_END_TIMES_DATA);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.matches()) {
                this.logEntry = matcher.group(16);
            }
            clearEntangledLines(entangledLogLines);
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.remove(TOKEN);
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.UNIFIED.toString();
    }

    /**
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        if (logLine.matches(REGEX_RETAIN_BEGINNING_PAUSE_YOUNG) || logLine.matches(REGEX_RETAIN_BEGINNING_SERIAL_OLD)
                || logLine.matches(REGEX_RETAIN_BEGINNING_G1_YOUNG)
                || logLine.matches(REGEX_RETAIN_MIDDLE_G1_YOUNG_DATA)
                || logLine.matches(REGEX_RETAIN_MIDDLE_PAUSE_YOUNG_DATA)
                || logLine.matches(REGEX_RETAIN_MIDDLE_PAUSE_FULL_DATA)
                || logLine.matches(REGEX_RETAIN_MIDDLE_SPACE_DATA) || logLine.matches(REGEX_RETAIN_END_TIMES_DATA)) {
            match = true;
        } else {
            // TODO: Get rid of this and make them throwaway events?
            for (int i = 0; i < REGEX_THROWAWAY.length; i++) {
                if (logLine.matches(REGEX_THROWAWAY[i])) {
                    match = true;
                    break;
                }
            }
        }
        return match;
    }

    /**
     * Convenience method to write out any saved log lines.
     * 
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @return
     */
    private final void clearEntangledLines(List<String> entangledLogLines) {
        if (entangledLogLines != null && entangledLogLines.size() > 0) {
            // Output any entangled log lines
            Iterator<String> iterator = entangledLogLines.iterator();
            while (iterator.hasNext()) {
                String logLine = iterator.next();
                this.logEntry = this.logEntry + Constants.LINE_SEPARATOR + logLine;
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
    }
}