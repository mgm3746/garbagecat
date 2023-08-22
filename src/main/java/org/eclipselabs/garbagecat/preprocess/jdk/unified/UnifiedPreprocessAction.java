/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2023 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess.jdk.unified;

import static org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil.DECORATOR_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.OtherTime;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.jdk.unified.ToSpaceExhaustedEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedBlankLineEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedConcurrentEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint;

/**
 * <p>
 * Generic unified logging preprocessing.
 * </p>
 *
 * <h2>Example Logging</h2>
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
 * [0.369s][info][gc,start     ] GC(6) Pause Young (Normal) (G1 Evacuation Pause) Other: 0.1ms Humongous regions: 0-&gt;0 Metaspace: 9085K-&gt;9085K(1058816K) 3M-&gt;2M(7M) 0.929ms User=0.01s Sys=0.00s Real=0.01s
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
     * Regular expression for external root scanning block.
     *
     * Ext Root Scanning (ms): 1.8
     */
    public static final String REGEX_G1_EXT_ROOT_SCANNING = "(Ext Root Scanning \\(ms\\): (\\d{1,}[\\.,]\\d) )";

    /**
     * Regular expression for retained beginning @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1CleanupEvent}.
     * 
     * <pre>
     * [0.117s][info][gc            ] GC(2) Pause Cleanup 1M->1M(5M) 0.024ms
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_G1_CLEANUP = "^(" + UnifiedRegEx.DECORATOR + " Pause Cleanup "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + ")$";

    private static final Pattern REGEX_RETAIN_BEGINNING_G1_CLEANUP_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_G1_CLEANUP);

    /**
     * Regular expression for retained beginning @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1FullGCEvent}.
     * 
     * <pre>
     * [2021-03-13T03:37:40.051+0530][79853119ms] GC(8646) Pause Full (G1 Evacuation Pause)
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_G1_FULL_GC = "^(" + UnifiedRegEx.DECORATOR + " Pause Full \\(("
            + GcTrigger.G1_COMPACTION_PAUSE.getRegex() + "|" + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + ")\\))$";

    private static final Pattern REGEX_RETAIN_BEGINNING_G1_FULL_GC_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_G1_FULL_GC);

    /**
     * Regular expression for retained beginning @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSerialOldEvent} and @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelCompactingOldEvent}.
     * 
     * <pre>
     * [0.075s][info][gc,start     ] GC(2) Pause Full (Allocation Failure)
     * 
     * [0.091s][info][gc,start     ] GC(3) Pause Full (Ergonomics)
     * 
     * [2021-09-14T06:51:15.478-0500][3.530s][info][gc,start     ] GC(1) Pause Full (Metadata GC Threshold)
     * 
     * [2021-10-29T21:02:24.624+0000][info][gc,start       ] GC(23863) Pause Full (G1 Humongous Allocation)
     *
     * [2021-11-01T20:48:05.108+0000][240210707ms] GC(951) Pause Full (Heap Dump Initiated GC)
     * 
     * [2022-02-08T07:33:13.183+0000][7731431ms] GC(112) Pause Full (Metadata GC Clear Soft References)
     * 
     * [2022-05-12T14:53:58.573-0500][411066.724s][info][gc,start      ] GC(567) Pause Full (Diagnostic Command)
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_OLD = "^(" + UnifiedRegEx.DECORATOR + " Pause Full \\(("
            + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|" + GcTrigger.DIAGNOSTIC_COMMAND.getRegex() + "|"
            + GcTrigger.ERGONOMICS.getRegex() + "|" + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|"
            + GcTrigger.HEAP_DUMP_INITIATED_GC.getRegex() + "|" + GcTrigger.METADATE_GC_CLEAR_SOFT_REFERENCES.getRegex()
            + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + "|" + GcTrigger.SYSTEM_GC.getRegex() + ")\\))$";

    private static final Pattern REGEX_RETAIN_BEGINNING_OLD_PATTERN = Pattern.compile(REGEX_RETAIN_BEGINNING_OLD);

    /**
     * Regular expression for retained beginning @link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedYoungEvent}.
     * 
     * <pre>
     * [0.112s][info][gc,start       ] GC(3) Pause Young (Allocation Failure)
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_PAUSE_YOUNG = "^(" + UnifiedRegEx.DECORATOR + " Pause Young \\("
            + GcTrigger.ALLOCATION_FAILURE.getRegex() + "\\))$";

    private static final Pattern REGEX_RETAIN_BEGINNING_PAUSE_YOUNG_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_PAUSE_YOUNG);

    /**
     * Regular expression for retained 1st line of safepoint logging.
     * 
     * [2021-09-14T11:40:53.379-0500][144.035s][info][safepoint ] Entering safepoint region:
     * CollectForMetadataAllocation
     * 
     * [2021-10-27T13:03:16.629-0400] Entering safepoint region: HandshakeFallback
     */
    private static final String REGEX_RETAIN_BEGINNING_SAFEPOINT = "^(" + UnifiedRegEx.DECORATOR
            + " Entering safepoint region: " + UnifiedSafepoint.triggerRegEx() + ")$";

    private static final Pattern REGEX_RETAIN_BEGINNING_SAFEPOINT_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_SAFEPOINT);

    /**
     * Regular expression for retained beginning @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedCmsInitialMarkEvent}.
     * 
     * <pre>
     * [0.053s][info][gc           ] GC(1) Pause Initial Mark 0M->0M(2M) 0.278ms
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_UNIFIED_CMS_INITIAL_MARK = "^(" + UnifiedRegEx.DECORATOR
            + " Pause Initial Mark " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) "
            + JdkRegEx.DURATION_MS + ")[ ]{0,}$";

    private static final Pattern REGEX_RETAIN_BEGINNING_UNIFIED_CMS_INITIAL_MARK_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_UNIFIED_CMS_INITIAL_MARK);

    /**
     * Regular expression for retained beginning @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedRemarkEvent}.
     * 
     * <pre>
     * [0.055s][info][gc           ] GC(1) Pause Remark 0M->0M(2M) 0.332ms
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_UNIFIED_REMARK = "^(" + UnifiedRegEx.DECORATOR + " Pause Remark "
            + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS
            + ")[ ]{0,}$";

    private static final Pattern REGEX_RETAIN_BEGINNING_UNIFIED_REMARK_PATTERN = Pattern
            .compile(REGEX_RETAIN_BEGINNING_UNIFIED_REMARK);

    /**
     * Regular expression for retained beginning @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1YoungPauseEvent} and @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedParallelScavengeEvent}.
     * 
     * <pre>
     * [16.627s][info][gc,start      ] GC(1354) Pause Young (Prepare Mixed) (G1 Evacuation Pause)
     * 
     * [15.069s][info][gc,start     ] GC(1190) Pause Young (Concurrent Start) (G1 Evacuation Pause)
     * 
     * [2021-12-20T10:29:00.098-0500] GC(0) Pause Young (Concurrent Start) (G1 Humongous Allocation)
     * 
     * [2019-05-09T01:39:00.763+0000][5355ms] GC(0) Pause Young (Normal) (G1 Evacuation Pause)
     * 
     * [2019-05-09T01:39:07.136+0000][11728ms] GC(3) Pause Young (Normal) (GCLocker Initiated GC)
     * 
     * [16.629s][info][gc,start      ] GC(1355) Pause Young (Mixed) (G1 Evacuation Pause)
     * 
     * [2021-09-14T11:38:33.217-0500][3.874s][info][gc,start     ] GC(0) Pause Young (Concurrent Start) 
     * (Metadata GC Threshold)
     * 
     * [2021-09-14T06:51:15.471-0500][3.523s][info][gc,start     ] GC(0) Pause Young (Metadata GC Threshold)
     * 
     * [0.037s][info][gc,start    ] GC(0) Pause Young (Normal) (G1 Preventive Collection)
     * 
     * [2022-02-08T07:33:13.178+0000][7731426ms] GC(111) Pause Young (Metadata GC Clear Soft References)
     * </pre>
     */
    private static final String REGEX_RETAIN_BEGINNING_YOUNG = "^(" + UnifiedRegEx.DECORATOR
            + " Pause Young( \\((Normal|Prepare Mixed|Mixed|Concurrent Start)\\))? \\(("
            + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + "|" + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|"
            + GcTrigger.G1_PREVENTIVE_COLLECTION.getRegex() + "|" + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|"
            + GcTrigger.HEAP_DUMP_INITIATED_GC.getRegex() + "|" + GcTrigger.METADATE_GC_CLEAR_SOFT_REFERENCES.getRegex()
            + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + ")\\))$";

    private static final Pattern REGEX_RETAIN_BEGINNING_YOUNG_PATTERN = Pattern.compile(REGEX_RETAIN_BEGINNING_YOUNG);

    /**
     * Regular expression for retained 3rd line of safepoint logging.
     * 
     * <pre>
     * [2021-09-14T11:40:53.379-0500][144.036s][info][safepoint     ] Total time for which application threads were 
     * stopped: 0.0004546 seconds, Stopping threads took: 0.0002048 seconds
     * </pre>
     */
    private static final String REGEX_RETAIN_END_SAFEPOINT = "^(" + UnifiedRegEx.DECORATOR
            + " Total time for which application threads were stopped: (\\d{1,4}[\\.\\,]\\d{7}) seconds, "
            + "Stopping threads took: (\\d{1,4}[\\.\\,]\\d{7}) seconds)[ ]*$";

    private static final Pattern REGEX_RETAIN_END_SAFEPOINT_PATTERN = Pattern.compile(REGEX_RETAIN_END_SAFEPOINT);

    /**
     * Regular expression for retained end times data.
     * 
     * <pre>
     * [0.112s][info][gc,cpu         ] GC(3) User=0.00s Sys=0.00s Real=0.00s
     * 
     * [2019-05-09T01:39:00.821+0000][5413ms] GC(0) User=0.02s Sys=0.01s Real=0.06s
     * </pre>
     */
    private static final String REGEX_RETAIN_END_TIMES_DATA = "^" + UnifiedRegEx.DECORATOR + TimesData.REGEX_JDK9 + "$";

    private static final Pattern REGEX_RETAIN_END_TIMES_DATA_PATTERN = Pattern.compile(REGEX_RETAIN_END_TIMES_DATA);

    /**
     * Regular expression for retained external root scanning data. Root scanning is multi-threaded. Use the "Max" value
     * for the duration.
     * 
     * [2022-10-09T13:16:49.289+0000][3792.777s][debug][gc,phases ] GC(9) Ext Root Scanning (ms): Min: 0.9, Avg: 1.0,
     * Max: 1.0, Diff: 0.1, Sum: 7.8, Workers: 8
     */
    private static final String REGEX_RETAIN_MIDDLE_EXT_ROOT_SCANNING = "^" + UnifiedRegEx.DECORATOR
            + "[ ]{5}Ext Root Scanning \\(ms\\):   Min:  \\d{1,}[\\.,]\\d, Avg:  \\d{1,}[\\.,]\\d, "
            + "Max:  (\\d{1,}[\\.,]\\d), Diff:  \\d{1,}[\\.,]\\d, Sum:  \\d{1,}[\\.,]\\d, Workers: \\d{1,}$";

    private static final Pattern REGEX_RETAIN_MIDDLE_EXT_ROOT_SCANNING_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_EXT_ROOT_SCANNING);

    /**
     * Regular expression for retained G1 humongous data used to distinguish
     * {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1FullGcEvent} from
     * {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedOldEvent}
     * 
     * [2021-09-22T10:57:21.297-0500][5259442ms] GC(5172) Humongous regions: 13->13
     */
    private static final String REGEX_RETAIN_MIDDLE_G1_HUMONGOUS = "^" + UnifiedRegEx.DECORATOR
            + "( Humongous regions: \\d{1,}->\\d{1,})$";

    private static final Pattern REGEX_RETAIN_MIDDLE_G1_HUMONGOUS_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_G1_HUMONGOUS);

    /**
     * Regular expression for retained Pause Young data.
     *
     * <pre>
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
     * 
     * [2020-06-24T18:11:52.781-0700][58776ms] GC(44) Pause Young (Concurrent Start) (Metadata GC Threshold)
     * 733M->588M(1223M) 105.541ms
     * 
     * [2020-06-24T19:24:56.395-0700][4442390ms] GC(126) Pause Young (Concurrent Start) (G1 Humongous Allocation)
     * 882M->842M(1223M) 19.777ms
     * 
     * [0.038s][info][gc          ] GC(0) Pause Young (Normal) (G1 Preventive Collection) 1M->1M(4M) 0.792ms
     * </pre>
     */
    private static final String REGEX_RETAIN_MIDDLE_G1_YOUNG_DATA = "^" + UnifiedRegEx.DECORATOR
            + " Pause Young( \\((Normal|Mixed|Prepare Mixed|Concurrent Start)\\))? \\(("
            + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + "|" + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|"
            + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|" + GcTrigger.G1_PREVENTIVE_COLLECTION.getRegex() + "|"
            + GcTrigger.METADATA_GC_THRESHOLD.getRegex() + ")\\)( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + ")$";

    private static final Pattern REGEX_RETAIN_MIDDLE_G1_YOUNG_DATA_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_G1_YOUNG_DATA);
    /**
     * Regular expression for retained middle metaspace data.
     *
     * <p>
     * 1) JDK8/11 same REGEX_RETAIN_MIDDLE_SPACE_DATA:
     * </p>
     * 
     * <pre>
     * [0.032s][info][gc,metaspace ] GC(0) Metaspace: 120K-&gt;120K(1056768K)
     * 
     * [2019-05-09T01:39:00.821+0000][5413ms] GC(0) Metaspace: 26116K-&gt;26116K(278528K)
     * </pre>
     * 
     * <p>
     * 2) JDK17:
     * </p>
     * 
     * <pre>
     * [0.084s][info][gc,metaspace] GC(4) Metaspace: 1174K(1344K)->1174K(1344K) NonClass: 1078K(1152K)->1078K(1152K) 
     * Class: 95K(192K)->95K(192K)
     * </pre>
     */
    private static final String REGEX_RETAIN_MIDDLE_METASPACE_DATA = "^" + UnifiedRegEx.DECORATOR + "( Metaspace: "
            + JdkRegEx.SIZE + "(\\(" + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
            + "\\))( NonClass: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) Class: " + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\))?$";

    private static final Pattern REGEX_RETAIN_MIDDLE_METASPACE_DATA_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_METASPACE_DATA);

    /**
     * Regular expression for retained <code>OtherTime</code> data.
     * 
     * [2022-10-09T13:16:49.289+0000][3792.777s][info ][gc,phases ] GC(9) Other: 9569.7ms
     */
    private static final String REGEX_RETAIN_MIDDLE_OTHER_TIME = "^" + UnifiedRegEx.DECORATOR + "[ ]{1,}"
            + OtherTime.REGEX + "$";

    private static final Pattern REGEX_RETAIN_MIDDLE_OTHER_TIME_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_OTHER_TIME);

    /**
     * Regular expression for retained Pause Full data.
     * 
     * <pre>
     * [0.076s][info][gc             ] GC(2) Pause Full (Allocation Failure) 0M->0M(2M) 1.699ms
     * 
     * [0.092s][info][gc             ] GC(3) Pause Full (Ergonomics) 0M->0M(3M) 1.849ms
     * 
     * [2020-06-24T18:13:51.155-0700][177150ms] GC(74) Pause Full (System.gc()) 887M->583M(1223M) 3460.196ms
     * 
     * [2021-03-13T03:37:42.178+0530][79855246ms] GC(8646) Pause Full (G1 Evacuation Pause) 8186M-&gt;8178M(8192M) 
     * 2127.343ms
     *
     * [2021-03-13T03:45:46.526+0530][80339594ms] GC(9216) Pause Full (GCLocker Initiated GC) 8184M->8180M(8192M) 
     * 2101.341ms
     * 
     * [2021-05-25T16:02:21.733-0400][1217172136ms] GC(15111) Pause Full (Heap Dump Initiated GC)
     * 
     * [2021-10-29T21:02:33.467+0000][info][gc             ] GC(23863) Pause Full (G1 Humongous Allocation) 
     * 16339M-&gt;14486M(16384M) 8842.979ms
     *
     * [2021-11-01T20:48:05.297+0000][240210896ms] GC(951) Pause Full (Heap Dump Initiated GC) 166M-&gt;160M(1678M) 
     * 189.216ms
     * 
     * [2022-02-08T07:33:13.853+0000][7732101ms] GC(112) Pause Full (Metadata GC Clear Soft References) 
     * 141M->141M(2147M) 670.712ms
     * 
     * [2022-05-12T14:54:09.413-0500][411077.565s][info][gc             ] GC(567) Pause Full (Diagnostic Command) 
     * 41808M->35651M(49152M) 10840.271ms
     * </pre>
     */
    private static final String REGEX_RETAIN_MIDDLE_PAUSE_FULL_DATA = "^" + UnifiedRegEx.DECORATOR + " Pause Full \\(("
            + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|" + GcTrigger.DIAGNOSTIC_COMMAND.getRegex() + "|"
            + GcTrigger.ERGONOMICS.getRegex() + "|" + GcTrigger.G1_COMPACTION_PAUSE.getRegex() + "|"
            + GcTrigger.G1_EVACUATION_PAUSE.getRegex() + "|" + GcTrigger.G1_HUMONGOUS_ALLOCATION.getRegex() + "|"
            + GcTrigger.GCLOCKER_INITIATED_GC.getRegex() + "|" + GcTrigger.HEAP_DUMP_INITIATED_GC.getRegex() + "|"
            + GcTrigger.METADATE_GC_CLEAR_SOFT_REFERENCES.getRegex() + "|" + GcTrigger.METADATA_GC_THRESHOLD.getRegex()
            + "|" + GcTrigger.SYSTEM_GC.getRegex() + ")\\)( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS + ")$";

    private static final Pattern REGEX_RETAIN_MIDDLE_PAUSE_FULL_DATA_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_PAUSE_FULL_DATA);

    /**
     * Regular expression for retained Pause Young data.
     * 
     * <pre>
     * [0.112s][info][gc             ] GC(3) Pause Young (Allocation Failure) 1M->1M(2M) 0.700ms
     * 
     * [2022-02-08T07:33:13.183+0000][7731431ms] GC(111) Pause Young (Metadata GC Clear Soft References) 
     * 141M->141M(2147M) 4.151ms
     * </pre>
     */
    private static final String REGEX_RETAIN_MIDDLE_PAUSE_YOUNG_DATA = "^" + UnifiedRegEx.DECORATOR
            + " Pause Young \\((" + GcTrigger.ALLOCATION_FAILURE.getRegex() + "|"
            + GcTrigger.HEAP_DUMP_INITIATED_GC.getRegex() + "|" + GcTrigger.METADATE_GC_CLEAR_SOFT_REFERENCES.getRegex()
            + ")\\)( " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) " + JdkRegEx.DURATION_MS
            + ")$";

    private static final Pattern REGEX_RETAIN_MIDDLE_PAUSE_YOUNG_DATA_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_PAUSE_YOUNG_DATA);

    /**
     * Regular expression for retained "Promotion failed".
     * 
     * [2021-10-30T02:03:26.792+0000][404347ms] Promotion failed
     */
    private static final String REGEX_RETAIN_MIDDLE_PROMOTION_FAILED = "^(" + UnifiedRegEx.DECORATOR
            + "( Promotion failed)$)";

    private static final Pattern REGEX_RETAIN_MIDDLE_PROMOTION_FAILED_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_PROMOTION_FAILED);

    /**
     * Regular expression for retained 2nd line of safepoint logging.
     * 
     * [2021-09-14T11:40:53.379-0500][144.036s][info][safepoint ] Leaving safepoint region
     */
    private static final String REGEX_RETAIN_MIDDLE_SAFEPOINT = "^(" + UnifiedRegEx.DECORATOR
            + " Leaving safepoint region$)";

    private static final Pattern REGEX_RETAIN_MIDDLE_SAFEPOINT_PATTERN = Pattern.compile(REGEX_RETAIN_MIDDLE_SAFEPOINT);

    /**
     * Regular expression for retained middle space data.
     * 
     * <p>
     * 1) JDK 8/11:
     * </p>
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
     * [0.030s][info][gc,heap      ] GC(0) ParOldGen: 0K-&gt;8K(512K)
     * 
     * [0.053s][info][gc,heap      ] GC(0) ParNew: 974K-&gt;128K(1152K)
     * 
     * [0.053s][info][gc,heap      ] GC(0) CMS: 0K-&gt;518K(960K)
     *
     * [32.636s][info][gc,heap        ] GC(9239) Tenured: 24193K->24195K(25240K)
     * </pre>
     * 
     * <p>
     * 2) JDK17:
     * </p>
     * 
     * <pre>
     * [0.036s][info][gc,heap     ] GC(0) DefNew: 1022K(1152K)-&gt;127K(1152K) Eden: 1022K(1024K)-&gt;0K(1024K) From: 
     * 0K(128K)-&gt;127K(128K)
     *
     * [0.072s][info][gc,heap        ] GC(3) Tenured: 754K(768K)->1500K(2504K)
     * </pre>
     */
    private static final String REGEX_RETAIN_MIDDLE_SPACE_DATA = "^" + UnifiedRegEx.DECORATOR
            + "( (CMS|DefNew|Metaspace|ParNew|PSYoungGen|PSOldGen|ParOldGen|Tenured): " + JdkRegEx.SIZE + "(\\("
            + JdkRegEx.SIZE + "\\))?->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\))( Eden: " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\) From: " + JdkRegEx.SIZE + "\\("
            + JdkRegEx.SIZE + "\\)->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE + "\\))?$";

    private static final Pattern REGEX_RETAIN_MIDDLE_SPACE_DATA_PATTERN = Pattern
            .compile(REGEX_RETAIN_MIDDLE_SPACE_DATA);

    /**
     * Regular expressions for lines thrown away.
     * 
     * In general, concurrent logging without an ending duration is thrown away, and the corresponding logging with the
     * ending duration is retained with the concurrent event.
     * 
     * <pre>
     * Z:
     * 
     * [2021-12-01T10:04:06.358-0500] GC(0) Garbage Collection (Warmup)
     * 
     * [2021-12-01T10:04:06.358-0500] GC(0) Using 1 workers
     * 
     * [0.275s] GC(2) Load: 0.53/0.41/0.33
     *
     * [0.132s][info][gc,load     ] GC(0) Load: 0.42/0.38/0.32
     * 
     * [0.275s] GC(2) MMU: 2ms/99.6%, 5ms/99.8%, 10ms/99.9%, 20ms/99.9%, 50ms/99.9%, 100ms/100.0%
     * 
     * [0.275s] GC(2) Mark: 1 stripe(s), 1 proactive flush(es), 1 terminate flush(es), 0 completion(s), 
     * 0 continuation(s)
     * 
     * [0.275s] GC(2) Mark Stack Usage: 32M
     * 
     * [0.275s] GC(2) NMethods: 756 registered, 0 unregistered
     * 
     * [0.275s] GC(2) Metaspace: 3M used, 3M committed, 1032M reserved
     * 
     * [0.134s] GC(0) Soft: 3088 encountered, 0 discovered, 0 enqueued
     * 
     * [0.134s] GC(0) Weak: 225 encountered, 203 discovered, 43 enqueued
     * 
     * [0.134s] GC(0) Final: 2 encountered, 0 discovered, 0 enqueued
     * 
     * [0.134s] GC(0) Phantom: 25 encountered, 22 discovered, 20 enqueued
     * 
     * [0.134s] GC(0) Small Pages: 5 / 10M, Empty: 0M, Relocated: 3M, In-Place: 0
     * 
     * [0.134s] GC(0) Large Pages: 0 / 0M, Empty: 0M, Relocated: 0M, In-Place: 0
     * 
     * [0.134s] GC(0) Forwarding Usage: 0M
     * 
     * [0.132s][info][gc,heap     ] GC(0) Min Capacity: 32M(33%)
     * 
     * [0.132s][info][gc,heap     ] GC(0) Max Capacity: 96M(100%)     
     * 
     * [0.134s] GC(0) Soft Max Capacity: 96M(100%)
     * 
     * [0.134s] GC(0)                Mark Start          Mark End        Relocate Start      Relocate End           
     * High               Low
     *
     * [0.134s] GC(0)  Capacity:       32M (33%)          32M (33%)          32M (33%)          32M (33%)          
     * 32M (33%)          32M (33%)
     * 
     * [0.134s] GC(0)      Free:       86M (90%)          84M (88%)          84M (88%)          90M (94%)          
     * 90M (94%)          82M (85%)
     * 
     * [0.134s] GC(0)      Used:       10M (10%)          12M (12%)          12M (12%)           6M (6%)           
     * 14M (15%)           6M (6%)
     * 
     * [0.134s] GC(0)      Live:         -                 3M (4%)            3M (4%)            3M (4%)             
     * -                  -
     * 
     * [0.134s] GC(0) Allocated:         -                 2M (2%)            2M (2%)            1M (2%)             
     * -                  -
     * 
     * [0.134s] GC(0)   Garbage:         -                 6M (7%)            6M (7%)            0M (1%)             
     * -                  -
     * 
     * [0.134s] GC(0) Reclaimed:         -                  -                 0M (0%)            5M (6%)             
     * -                  -
     * 
     * [0.134s] GC(0) Garbage Collection (Warmup) 10M(10%)->6M(6%)
     * 
     * [0.262s] GC(2) Garbage Collection (Allocation Stall)
     * 
     * [0.262s] GC(2) Clearing All SoftReferences
     * 
     * [0.363s] Allocation Stall (main) 8.723ms
     * 
     * [3.394s] Allocation Stall (C1 CompilerThread0) 24.753ms
     * 
     * [0.407s] Relocation Stall (main) 0.668ms
     * 
     * [3.394s] Relocation Stall (C1 CompilerThread0) 0.334ms
     *
     * [0.424s] GC(7) Garbage Collection (Allocation Rate)
     *
     * [0.437s] GC(7) Garbage Collection (Allocation Rate) 54M(56%)->34M(35%)
     * 
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
     * [0.004s][info][gc,cds       ] Mark closed archive regions in map: [0x00000000fff00000, 0x00000000fff69ff8]
     * 
     * [0.004s][info][gc,cds       ] Mark open archive regions in map: [0x00000000ffe00000, 0x00000000ffe46ff8]
     * 
     * [2021-03-13T03:37:44.312+0530][79857380ms] GC(8651) Using 8 workers of 8 for full compaction
     * 
     * [2021-09-14T11:38:33.230-0500][3.887s][info][gc,task      ] GC(1) Using 2 workers of 2 for marking
     *
     * [2021-03-13T03:45:44.424+0530][80337492ms] Attempting maximally compacting collection
     *
     * [2021-03-13T03:37:44.312+0530][79857381ms] GC(8652) Concurrent Mark From Roots
     * 
     * [2021-09-14T11:41:18.173-0500][168.830s][info][gc,mmu        ] GC(26) MMU target violated: 201.0ms 
     * (200.0ms/201.0ms)
     * 
     * [0.038s][info][gc,phases   ] GC(0)   Merge Heap Roots: 0.1ms
     * 
     * [0.038s][info][gc,heap     ] GC(0) Archive regions: 2->2
     * 
     * [2022-10-09T13:16:39.707+0000][3783.195s][debug][gc,heap ] GC(9) Heap before GC invocations=9 (full 0): 
     * garbage-first heap total 10743808K, used 1819374K [0x0000000570400000, 0x0000000800000000)
     * 
     * [2023-01-11T16:09:59.244+0000][19084.784s] GC(300) Concurrent Undo Cycle 54.191ms
     * 
     * [2023-01-11T17:46:35.751+0000][24881.291s] GC(452)   Merge Optional Heap Roots: 0.3m
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
     * 
     * [0.053s][info][gc,start     ] GC(1) Pause Initial Mark
     * 
     * [0.053s][info][gc           ] GC(1) Concurrent Mark
     * 
     * [0.054s][info][gc           ] GC(1) Concurrent Preclean
     * 
     * [0.055s][info][gc           ] GC(1) Concurrent Sweep
     * 
     * [2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Cycle
     * 
     * [2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Clear Claimed Marks
     * 
     * [2021-03-13T03:37:44.312+0530][79857380ms] GC(8652) Concurrent Scan Root Regions
     * 
     * [2021-03-13T03:37:44.312+0530][79857381ms] GC(8652) Concurrent Mark From Roots
     * 
     * [0.055s][info][gc           ] GC(1) Concurrent Reset
     * 
     * [0.030s][info][safepoint    ] Application time: 0.0012757 seconds
     * 
     * [2021-09-22T10:59:49.112-0500][5455002ms] Entering safepoint region: Exit
     * 
     * [0.031s] Entering safepoint region: Halt
     * </pre>
     */
    private static final String[] REGEX_THROWAWAY = {
            // ***** generic *****
            "^" + UnifiedRegEx.DECORATOR + " Concurrent cleanup$",
            // ***** SERIAL *****
            "^" + UnifiedRegEx.DECORATOR + " Phase \\d: .+?$",
            // ***** G1 *****
            "^" + UnifiedRegEx.DECORATOR + " Using \\d{1,} workers of \\d{1,} for (evacuation|full compaction|"
                    + "marking)$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   (Pre Evacuate|Evacuate( Optional)?|Post Evacuate) Collection Set: "
                    + JdkRegEx.DURATION_MS + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + " (Eden|Survivor|Old) regions: \\d{1,}->\\d{1,}(\\(\\d{1,}\\))?$",
            "^" + UnifiedRegEx.DECORATOR + " Pause Remark$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " Cleaned string and symbol table, strings: \\d{1,} processed, \\d{1,} removed, "
                    + "symbols: \\d{1,} processed, \\d{1,} removed$",

            //
            "^" + UnifiedRegEx.DECORATOR + " Mark (closed|open) archive regions in map:.+$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Pause (Cleanup|Init Update Refs)$",
            //
            "^" + UnifiedRegEx.DECORATOR + " MMU target violated:.+$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Attempting full compaction$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Attempting maximally compacting collection$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Attempting maximum full compaction clearing soft references$",
            //
            "^" + UnifiedRegEx.DECORATOR + "   Merge (Optional )?Heap Roots:.+$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Archive regions:.+$",
            //
            "^" + UnifiedRegEx.DECORATOR + "[ ]{1,}Using \\d{1,} of \\d{1,} workers for "
                    + "concurrent class unloading$",
            // main headings
            "^" + UnifiedRegEx.DECORATOR
                    + " (---|  \\d{1,}|     elapsed|thr|Activated worker|Adaptive IHOP information|Basic information|"
                    + "Finish choosing CSet|GC Termination Stats|Heap (after|before) GC|Mutator Allocation stats|"
                    + "Old (other|PLAB|sizing)|Running G1|Skipped phase\\d{1,}|TLAB totals|Updated Refinement Zones|"
                    + "Young (other|PLAB|sizing)).*$",
            // Indented 5 spaces
            "^" + UnifiedRegEx.DECORATOR
                    + "     (AOT Root Scanning|Clear Card Table|Code Root Scanning|DerivedPointerTable Update|"
                    + "Expand Heap After Collection|Free Collection Set|GC Worker (Other|Total)|"
                    + "Humongous (Reclaim|Register)|Merge Per-Thread State|Object Copy|Redirty Cards|"
                    + "Reference Processing|Scan RS|Start New Collection Set|Termination|Update RS|Weak Processing).*$",
            // Indented 7 spaces
            "^" + UnifiedRegEx.DECORATOR
                    + "       (Claimed Cards|FinalReference|Notify and keep alive finalizable|Notify PhantomReferences|"
                    + "Notify Soft\\/WeakReferences|PhantomReference|Reconsider SoftReferences|Scanned Cards|"
                    + "Skipped Cards|SoftReference|Termination Attempts|WeakReference).*$",
            // Indented 9 spaces
            "^" + UnifiedRegEx.DECORATOR
                    + "         (Balance queues|Cleared|Discovered|FinalRef|PhantomRef|SoftRef|Total|WeakRef).*$",
            // ***** Parallel *****
            "^" + UnifiedRegEx.DECORATOR + " (Adjust Roots|Compaction Phase|Marking Phase|Post Compact|Summary Phase)( "
                    + JdkRegEx.DURATION_MS + ")?$",
            // ***** CMS *****
            "^" + UnifiedRegEx.DECORATOR + " Pause Initial Mark$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Old: " + JdkRegEx.SIZE + "->" + JdkRegEx.SIZE + "\\(" + JdkRegEx.SIZE
                    + "\\)$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " Concurrent (Clear Claimed (Marks|Roots)|Cycle|Mark|Mark Abort|Mark From Roots|"
                    + "Mark reset for overflow|Preclean|Reset|Scan Root Regions|Undo Cycle|Sweep)[ ]{0,}$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Application time:.+$",
            // ***** AdaptiveSizePolicy *****
            "^" + UnifiedRegEx.DECORATOR + " (PS)?AdaptiveSize.*$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " ((Adjusting|Scaled) eden|avg_promoted|avg_survived|Base_footprint:|    capacities|"
                    + "Desired survivor size|Do scavenge:|    eden:|[ ]{4}\\[[ ]{0,2}(eden|from|to)_start|"
                    + "  Eden, (from|to), (to|from):|    from:|Live_space:|  minor pause:|Minor_pause:|"
                    + "No full after scavenge|Old eden_size:|old_gen_capacity:|PSYoungGen::resize_spaces|      to:|"
                    + "Young generation size:).*$",
            // ***** Safepoint *****
            "^" + UnifiedRegEx.DECORATOR + " Entering safepoint region: (Exit|Halt)$",
            // ***** Z *****
            "^" + UnifiedRegEx.DECORATOR + " Garbage Collection \\((Allocation (Rate|Stall)|Warmup)\\).*$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Using \\d{1,} workers$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]{1,}(Allocated|Capacity|Final|Forwarding Usage|Free|Garbage|Large Pages|Live|Load|Mark|"
                    + "Mark Stack Usage|(Max|Min) Capacity|MMU|NMethods|Phantom|Reclaimed|Small Pages|Soft|"
                    + "Soft Max Capacity|Used|Weak):.+$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Metaspace: " + JdkRegEx.SIZE + " used, " + JdkRegEx.SIZE + " committed, "
                    + JdkRegEx.SIZE + " reserved$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + "[ ]+Mark Start[ ]+Mark End[ ]+Relocate Start[ ]+Relocate End[ ]+High[ ]+Low[ ]*$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Clearing All SoftReferences$",
            //
            "^" + UnifiedRegEx.DECORATOR + " Allocation Stall \\((main|C[12] CompilerThread\\d{1,})\\) "
                    + JdkRegEx.DURATION_MS + "$",
            //
            "^" + UnifiedRegEx.DECORATOR
                    + " Relocation Stall \\((main|C[12] CompilerThread\\d{1,}|Reference Handler)\\) "
                    + JdkRegEx.DURATION_MS + "$",
            //
            "^" + UnifiedRegEx.DECORATOR + " (Age table|- age).+$",
            //
            UnifiedBlankLineEvent.REGEX
            //
    };

    private static final List<Pattern> THROWAWAY_PATTERN_LIST = new ArrayList<>(REGEX_THROWAWAY.length);

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
     * Indicates the current log entry is either the beginning of a @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedG1FullGcEvent} that spans multiple logging lines, or it is a
     * single line logging event.
     */
    private static final String TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC = "TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC";

    /**
     * Indicates the current log entry is either the beginning of a @link
     * org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent} that spans multiple logging lines, or it is
     * a single line logging event.
     */
    private static final String TOKEN_BEGINNING_OF_UNIFIED_SAFEPOINT = "TOKEN_BEGINNING_OF_UNIFIED_SAFEPOINT";

    static {
        for (String regex : REGEX_THROWAWAY) {
            THROWAWAY_PATTERN_LIST.add(Pattern.compile(regex));
        }
    }

    /**
     * Determine if the log line is can be thrown away
     * 
     * @return true if the log line matches a throwaway pattern, false otherwise.
     */
    private static final boolean isThrowaway(String logLine) {
        boolean throwaway = false;
        for (int i = 0; i < THROWAWAY_PATTERN_LIST.size(); i++) {
            Pattern pattern = THROWAWAY_PATTERN_LIST.get(i);
            if (pattern.matcher(logLine).matches()) {
                throwaway = true;
                break;
            }
        }
        return throwaway;
    }

    /**
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        boolean match = false;
        if (REGEX_RETAIN_BEGINNING_UNIFIED_CMS_INITIAL_MARK_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_UNIFIED_REMARK_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_PAUSE_YOUNG_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_OLD_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_G1_FULL_GC_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_YOUNG_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_G1_CLEANUP_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_BEGINNING_SAFEPOINT_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_G1_HUMONGOUS_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_G1_YOUNG_DATA_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_OTHER_TIME_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_EXT_ROOT_SCANNING_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_PAUSE_YOUNG_DATA_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_PAUSE_FULL_DATA_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_SPACE_DATA_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_METASPACE_DATA_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_PROMOTION_FAILED_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_MIDDLE_SAFEPOINT_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_END_SAFEPOINT_PATTERN.matcher(logLine).matches()
                || REGEX_RETAIN_END_TIMES_DATA_PATTERN.matcher(logLine).matches()
                || JdkUtil.parseLogLine(logLine, null) instanceof ToSpaceExhaustedEvent
                || JdkUtil.parseLogLine(logLine, null) instanceof UnifiedConcurrentEvent) {
            match = true;
        } else if (isThrowaway(logLine)) {
            match = true;
        }
        return match;
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

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

        Matcher matcher;

        if ((matcher = REGEX_RETAIN_BEGINNING_UNIFIED_CMS_INITIAL_MARK_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_UNIFIED_REMARK_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_PAUSE_YOUNG_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            // Only report young collections that do not trigger an old collection
            if (nextLogEntry == null || !nextLogEntry.matches(REGEX_RETAIN_BEGINNING_OLD)) {
                Matcher pauseMatcher = REGEX_RETAIN_BEGINNING_PAUSE_YOUNG_PATTERN.matcher(logEntry);
                if (pauseMatcher.matches()) {
                    this.logEntry = pauseMatcher.group(1);
                }
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_OLD_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_G1_FULL_GC_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
                context.add(TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_YOUNG_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_G1_CLEANUP_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(1);
            }
            context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.add(TOKEN);
        } else if ((matcher = REGEX_RETAIN_BEGINNING_SAFEPOINT_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                if (nextLogEntry == null || REGEX_RETAIN_MIDDLE_SAFEPOINT_PATTERN.matcher(nextLogEntry).matches()) {
                    // Non GC safepoint
                    this.logEntry = matcher.group(1);
                    context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
                } else {
                    // GC safepoint, output after GC event
                    entangledLogLines.add(matcher.group(1));
                }
                context.add(TOKEN);
            }
            context.add(TOKEN_BEGINNING_OF_UNIFIED_SAFEPOINT);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_SPACE_DATA_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(DECORATOR_SIZE + 1);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_METASPACE_DATA_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(DECORATOR_SIZE + 1);
                context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            }
        } else if ((matcher = REGEX_RETAIN_MIDDLE_PAUSE_YOUNG_DATA_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                if (context.contains(TOKEN)) {
                    this.logEntry = matcher.group(DECORATOR_SIZE + 2);
                } else {
                    // Single line event
                    if (priorLogEntry == null) {
                        // first line in log file
                        this.logEntry = logEntry;
                    } else {
                        this.logEntry = Constants.LINE_SEPARATOR + logEntry;
                    }
                }
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_PAUSE_FULL_DATA_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (context.contains(PreprocessAction.TOKEN_BEGINNING_OF_EVENT)
                    || (nextLogEntry != null && REGEX_RETAIN_END_TIMES_DATA_PATTERN.matcher(nextLogEntry).matches())) {
                // Middle logging
                if (matcher.matches()) {
                    this.logEntry = matcher.group(DECORATOR_SIZE + 3);
                }
            } else {
                // Single line event
                if (priorLogEntry == null) {
                    // first line in log file
                    this.logEntry = logEntry;
                } else {
                    this.logEntry = Constants.LINE_SEPARATOR + logEntry;
                }
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_G1_HUMONGOUS_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(DECORATOR_SIZE + 1);
                context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            }
        } else if ((matcher = REGEX_RETAIN_MIDDLE_G1_YOUNG_DATA_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (!context.contains(PreprocessAction.NEWLINE)
                    || (nextLogEntry != null && REGEX_RETAIN_END_TIMES_DATA_PATTERN.matcher(nextLogEntry).matches())) {
                // Middle logging
                if (matcher.matches()) {
                    this.logEntry = matcher.group(DECORATOR_SIZE + 4);
                }
            } else {
                // Single line event
                if (priorLogEntry == null) {
                    // first line in log file
                    this.logEntry = logEntry;
                } else {
                    this.logEntry = Constants.LINE_SEPARATOR + logEntry;
                }
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_EXT_ROOT_SCANNING_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = " Ext Root Scanning (ms): " + matcher.group(DECORATOR_SIZE + 1);
                context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            }
        } else if ((matcher = REGEX_RETAIN_MIDDLE_OTHER_TIME_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = " " + matcher.group(DECORATOR_SIZE + 1);
                context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            }
        } else if ((matcher = REGEX_RETAIN_MIDDLE_PROMOTION_FAILED_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(DECORATOR_SIZE + 2);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if ((matcher = REGEX_RETAIN_MIDDLE_SAFEPOINT_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                boolean haveBeginningSafepointLogging = false;
                for (String logLine : entangledLogLines) {
                    if (logLine.matches(REGEX_RETAIN_BEGINNING_SAFEPOINT)) {
                        haveBeginningSafepointLogging = true;
                    }
                }
                if (haveBeginningSafepointLogging) {
                    entangledLogLines.add(matcher.group(1));
                } else {
                    this.logEntry = matcher.group(1);
                    context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
                }
            }
        } else if ((matcher = REGEX_RETAIN_END_SAFEPOINT_PATTERN.matcher(logEntry)).matches()) {
            matcher.reset();
            if (matcher.matches()) {
                String beginningSafepointLogging = null;
                String middleSafepointLogging = null;
                for (String logLine : entangledLogLines) {
                    if (logLine.matches(REGEX_RETAIN_BEGINNING_SAFEPOINT)) {
                        beginningSafepointLogging = logLine;
                    } else if (logLine.matches(REGEX_RETAIN_MIDDLE_SAFEPOINT)) {
                        middleSafepointLogging = logLine;
                    }
                }
                if (beginningSafepointLogging != null && middleSafepointLogging != null) {
                    this.logEntry = beginningSafepointLogging + middleSafepointLogging + matcher.group(1);
                    entangledLogLines.remove(beginningSafepointLogging);
                    entangledLogLines.remove(middleSafepointLogging);
                    context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
                } else {
                    this.logEntry = logEntry;
                }
                context.remove(TOKEN_BEGINNING_OF_UNIFIED_SAFEPOINT);
            }
        } else if ((matcher = REGEX_RETAIN_END_TIMES_DATA_PATTERN.matcher(logEntry)).matches()) {
            // End logging
            matcher.reset();
            if (matcher.matches()) {
                this.logEntry = matcher.group(DECORATOR_SIZE + 1);
            }
            if (!context.contains(TOKEN_BEGINNING_OF_UNIFIED_SAFEPOINT)) {
                clearEntangledLines(entangledLogLines);
            }
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            context.remove(TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC);
        } else if (JdkUtil.parseLogLine(logEntry, null) instanceof ToSpaceExhaustedEvent) {
            // output at end
            entangledLogLines.add(logEntry);
            context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
        } else if (JdkUtil.parseLogLine(logEntry, null) instanceof UnifiedConcurrentEvent && !isThrowaway(logEntry)) {
            // Stand alone eventlogEntry
            if (!context.contains(TOKEN_BEGINNING_OF_UNIFIED_G1_FULL_GC)) {
                this.logEntry = logEntry;
                context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            } else {
                // output intermingled lines at end
                entangledLogLines.add(logEntry);
                context.remove(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            }
        }
    }

    /**
     * Convenience method to write out any saved log lines.
     * 
     * @param entangledLogLines
     *            Log lines to be output out of order.
     * @return
     */
    private final void clearEntangledLines(List<String> entangledLogLines) {
        if (entangledLogLines != null && !entangledLogLines.isEmpty()) {
            // Output any entangled log lines
            for (String logLine : entangledLogLines) {
                // Add to prior line if current line is not an ending pattern
                if (this.logEntry.matches(TimesData.REGEX_JDK9) || !logLine.endsWith(Constants.LINE_SEPARATOR)) {
                    this.logEntry = this.logEntry + Constants.LINE_SEPARATOR + logLine;
                } else {
                    this.logEntry = this.logEntry + logLine;
                }
            }
            // Reset entangled log lines
            entangledLogLines.clear();
        }
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.PreprocessActionType.UNIFIED.toString();
    }
}