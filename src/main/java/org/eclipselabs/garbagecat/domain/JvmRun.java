/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2021 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain;

import static java.math.RoundingMode.HALF_EVEN;
import static org.eclipselabs.garbagecat.util.Memory.gigabytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_ADAPTIVE_SIZE_POLICY_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_CMS_PARALLEL_REMARK_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_CMS_SERIAL_OLD;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_COMP_CLASS_SIZE_HEAP_GT_32G;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_EXPLICIT_GC_SERIAL_G1;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_G1_HUMONGOUS_JDK_OLD;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_METASPACE_SIZE_LT_COMP_CLASS_SIZE;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_PHYSICAL_MEMORY;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_SERIAL_GC;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_EXPERIMENTAL_VM_OPTIONS;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_G1_HUMONGOUS_ALLOCATION;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_HEAP_DUMP_PATH_MISSING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_INSTRUMENTATION;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_JMX_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_MAX_TENURING_OVERRIDE;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_NATIVE;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_NEW_RATIO_INVERTED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_PERM_GEN;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_PRINT_FLS_STATISTICS;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_PRINT_PROMOTION_FAILURE;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_SURVIVOR_RATIO;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_SURVIVOR_RATIO_TARGET;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_SWAPPING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_SWAP_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_UNACCOUNTED_OPTIONS_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_BIASED_LOCKING_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_BYTECODE_COMPILE_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_BYTECODE_COMPILE_FIRST_INVOCATION;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CGROUP_MEMORY_LIMIT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CLASS_UNLOADING_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CMS_INCREMENTAL_MODE;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CMS_PAR_NEW_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_EXPLICIT_GC_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_EXPLICIT_GC_DISABLED_CONCURRENT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_FAST_UNORDERED_TIMESTAMPS;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_G1_JDK8_PRIOR_U40;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_G1_JDK8_PRIOR_U40_RECS;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_G1_MIXED_GC_LIVE_THRSHOLD_PRCNT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_GC_LOG_FILE_SIZE_SMALL;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_GC_STOPPED_RATIO;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_HEAP_DUMP_ON_OOME_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_HEAP_DUMP_ON_OOME_MISSING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_HEAP_DUMP_PATH_FILENAME;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_HEAP_MIN_NOT_EQUAL_MAX;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PARALLELISM_INVERTED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PERM_MIN_NOT_EQUAL_MAX;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PERM_SIZE_NOT_SET;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_CLASS_HISTOGRAM;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_COMMANDLINE_FLAGS;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_GC_CAUSE_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_GC_CAUSE_MISSING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_GC_DETAILS_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_GC_DETAILS_MISSING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_REFERENCE_GC_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_STRING_DEDUP_STATS_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_TENURING_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_THREAD_STACK_SIZE_LARGE;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_THREAD_STACK_SIZE_NOT_SET;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_TIERED_COMPILATION_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_TRACE_CLASS_UNLOADING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_USE_MEMBAR;
import static org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx.DATESTAMP_EVENT;

import java.math.BigDecimal;
import java.util.List;

import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.Jvm;

/**
 * JVM run data.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JvmRun {

    /**
     * JVM environment information.
     */
    private Jvm jvm;

    /**
     * Minimum throughput (percent of time spent not doing garbage collection for a given time interval) to not be
     * flagged a bottleneck.
     */
    private int throughputThreshold;

    /**
     * Maximum young space size.
     */
    private Memory maxYoungSpace;

    /**
     * Maximum old space size.
     */
    private Memory maxOldSpace;

    /**
     * Maximum heap size.
     */
    private Memory maxHeapSpace;

    /**
     * Maximum heap occupancy.
     */
    private Memory maxHeapOccupancy;

    /**
     * Maximum heap after gc.
     */
    private Memory maxHeapAfterGc;

    public Memory getMaxHeapAfterGc() {
        return maxHeapAfterGc;
    }

    public void setMaxHeapAfterGc(Memory maxHeapAfterGc) {
        this.maxHeapAfterGc = maxHeapAfterGc;
    }

    /**
     * Maximum perm gen size (kilobytes).
     */
    private Memory maxPermSpace;

    /**
     * Maximum perm gen occupancy (kilobytes).
     */
    private Memory maxPermOccupancy;

    /**
     * Maximum perm gen after gC (kilobytes).
     */
    private Memory maxPermAfterGc;

    public Memory getMaxPermAfterGc() {
        return maxPermAfterGc;
    }

    public void setMaxPermAfterGc(Memory maxPermAfterGc) {
        this.maxPermAfterGc = maxPermAfterGc;
    }

    /**
     * Maximum GC pause duration (milliseconds).
     */
    private int maxGcPause;

    /**
     * Total GC pause duration (microseconds).
     */
    private long totalGcPause;

    /**
     * The first blocking event.
     */
    private BlockingEvent firstGcEvent;

    /**
     * The last blocking event.
     */
    private BlockingEvent lastGcEvent;

    /**
     * Total number of blocking events.
     */
    private int blockingEventCount;

    /**
     * Maximum stopped time duration (milliseconds).
     */
    private int maxStoppedTime;

    /**
     * Total stopped time duration (milliseconds).
     */
    private int totalStoppedTime;

    /**
     * The first stopped event.
     */
    private ApplicationStoppedTimeEvent firstStoppedEvent;

    /**
     * The last stopped event.
     */
    private ApplicationStoppedTimeEvent lastStoppedEvent;

    /**
     * Total number of {@link org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent}.
     */
    private int stoppedTimeEventCount;

    /**
     * <code>BlockingEvent</code>s where throughput does not meet the throughput goal.
     */
    private List<String> bottlenecks;

    /**
     * Log lines that do not match any existing logging patterns.
     */
    private List<String> unidentifiedLogLines;

    /**
     * Event types.
     */
    private List<LogEventType> eventTypes;

    /**
     * Analysis.
     */
    private List<Analysis> analysis;

    /**
     * Collector families.
     */
    private List<CollectorFamily> collectorFamilies;

    /**
     * Whether or not the JVM events are from a preprocessed file.
     */
    private boolean preprocessed;

    /**
     * Last log line unprocessed.
     */
    private String lastLogLineUnprocessed;

    /**
     * Number of <code>ParallelCollection</code> events.
     */
    private long parallelCount;

    /**
     * Number of <code>ParallelCollection</code> with "inverted" parallelism.
     */
    private long invertedParallelismCount;

    /**
     * <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    private LogEvent worstInvertedParallelismEvent;

    /**
     * Used for tracking max heap space outside of <code>BlockingEvent</code>s.
     */
    private Memory maxHeapSpaceNonBlocking;

    /**
     * Used for tracking max heap occupancy outside of <code>BlockingEvent</code>s.
     */
    private Memory maxHeapOccupancyNonBlocking;

    /**
     * Used for tracking max perm space outside of <code>BlockingEvent</code>s.
     */
    private Memory maxPermSpaceNonBlocking;

    /**
     * Used for tracking max perm occupancy outside of <code>BlockingEvent</code>s.
     */
    private Memory maxPermOccupancyNonBlocking;

    /**
     * Constructor accepting throughput threshold, JVM services, and JVM environment information.
     * 
     * @param throughputThreshold
     *            throughput threshold for identifying bottlenecks.
     * @param jvm
     *            JVM environment information.
     */
    public JvmRun(Jvm jvm, int throughputThreshold) {
        this.jvm = jvm;
        this.throughputThreshold = throughputThreshold;
    }

    public int getThroughputThreshold() {
        return throughputThreshold;
    }

    public void setThroughputThreshold(int throughputThreshold) {
        this.throughputThreshold = throughputThreshold;
    }

    public Jvm getJvm() {
        return jvm;
    }

    public void setJvm(Jvm jvm) {
        this.jvm = jvm;
    }

    public Memory getMaxYoungSpace() {
        return maxYoungSpace;
    }

    public void setMaxYoungSpace(Memory maxYoungSpace) {
        this.maxYoungSpace = maxYoungSpace;
    }

    public Memory getMaxOldSpace() {
        return maxOldSpace;
    }

    public void setMaxOldSpace(Memory maxOldSpace) {
        this.maxOldSpace = maxOldSpace;
    }

    public Memory getMaxHeapSpace() {
        return maxHeapSpace;
    }

    public void setMaxHeapSpace(Memory maxHeapSpace) {
        this.maxHeapSpace = maxHeapSpace;
    }

    public Memory getMaxHeapOccupancy() {
        return maxHeapOccupancy;
    }

    public void setMaxHeapOccupancy(Memory maxHeapOccupancy) {
        this.maxHeapOccupancy = maxHeapOccupancy;
    }

    public Memory getMaxPermSpace() {
        return maxPermSpace;
    }

    public void setMaxPermSpace(Memory maxPermSpace) {
        this.maxPermSpace = maxPermSpace;
    }

    public Memory getMaxPermOccupancy() {
        return maxPermOccupancy;
    }

    public void setMaxPermOccupancy(Memory maxPermOccupancy) {
        this.maxPermOccupancy = maxPermOccupancy;
    }

    public int getMaxGcPause() {
        return maxGcPause;
    }

    public void setMaxGcPause(int maxPause) {
        this.maxGcPause = maxPause;
    }

    public long getTotalGcPause() {
        return totalGcPause;
    }

    public void setTotalGcPause(long totalGcPause) {
        this.totalGcPause = totalGcPause;
    }

    public BlockingEvent getFirstGcEvent() {
        return firstGcEvent;
    }

    public void setFirstGcEvent(BlockingEvent firstGcEvent) {
        this.firstGcEvent = firstGcEvent;
    }

    public BlockingEvent getLastGcEvent() {
        return lastGcEvent;
    }

    public void setLastGcEvent(BlockingEvent lastGcEvent) {
        this.lastGcEvent = lastGcEvent;
    }

    public int getBlockingEventCount() {
        return blockingEventCount;
    }

    public void setBlockingEventCount(int blockingEventCount) {
        this.blockingEventCount = blockingEventCount;
    }

    public ApplicationStoppedTimeEvent getFirstStoppedEvent() {
        return firstStoppedEvent;
    }

    public void setFirstStoppedEvent(ApplicationStoppedTimeEvent firstStoppedEvent) {
        this.firstStoppedEvent = firstStoppedEvent;
    }

    public ApplicationStoppedTimeEvent getLastStoppedEvent() {
        return lastStoppedEvent;
    }

    public void setLastStoppedEvent(ApplicationStoppedTimeEvent lastStoppedEvent) {
        this.lastStoppedEvent = lastStoppedEvent;
    }

    public int getStoppedTimeEventCount() {
        return stoppedTimeEventCount;
    }

    public void setStoppedTimeEventCount(int stoppedTimeEventCount) {
        this.stoppedTimeEventCount = stoppedTimeEventCount;
    }

    public int getMaxStoppedTime() {
        return maxStoppedTime;
    }

    public void setMaxStoppedTime(int maxStoppedTime) {
        this.maxStoppedTime = maxStoppedTime;
    }

    public int getTotalStoppedTime() {
        return totalStoppedTime;
    }

    public void setTotalStoppedTime(int totalStoppedTime) {
        this.totalStoppedTime = totalStoppedTime;
    }

    public List<String> getBottlenecks() {
        return bottlenecks;
    }

    public void setBottlenecks(List<String> bottlenecks) {
        this.bottlenecks = bottlenecks;
    }

    public List<String> getUnidentifiedLogLines() {
        return unidentifiedLogLines;
    }

    public void setUnidentifiedLogLines(List<String> unidentifiedLogLines) {
        this.unidentifiedLogLines = unidentifiedLogLines;
    }

    public List<LogEventType> getEventTypes() {
        return eventTypes;
    }

    public void setEventTypes(List<LogEventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public List<Analysis> getAnalysis() {
        return analysis;
    }

    public void setAnalysis(List<Analysis> analysis) {
        this.analysis = analysis;
    }

    public void setCollectorFamilies(List<CollectorFamily> collectorFamilies) {
        this.collectorFamilies = collectorFamilies;
    }

    public List<CollectorFamily> getCollectorFamilies() {
        return collectorFamilies;
    }

    public boolean isPreprocessed() {
        return preprocessed;
    }

    public void setPreprocessed(boolean preprocessed) {
        this.preprocessed = preprocessed;
    }

    public String getLastLogLineUnprocessed() {
        return lastLogLineUnprocessed;
    }

    public void setLastLogLineUnprocessed(String lastLogLineUnprocessed) {
        this.lastLogLineUnprocessed = lastLogLineUnprocessed;
    }

    public long getParallelCount() {
        return parallelCount;
    }

    public void setParallelCount(long parallelCount) {
        this.parallelCount = parallelCount;
    }

    public long getInvertedParallelismCount() {
        return invertedParallelismCount;
    }

    public void setInvertedParallelismCount(long invertedParallelismCount) {
        this.invertedParallelismCount = invertedParallelismCount;
    }

    public LogEvent getWorstInvertedParallelismEvent() {
        return worstInvertedParallelismEvent;
    }

    public void setWorstInvertedParallelismEvent(LogEvent worstInvertedParallelismEvent) {
        this.worstInvertedParallelismEvent = worstInvertedParallelismEvent;
    }

    public Memory getMaxHeapSpaceNonBlocking() {
        return maxHeapSpaceNonBlocking;
    }

    public void setMaxHeapSpaceNonBlocking(Memory maxHeapSpaceNonBlocking) {
        this.maxHeapSpaceNonBlocking = maxHeapSpaceNonBlocking;
    }

    public Memory getMaxHeapOccupancyNonBlocking() {
        return maxHeapOccupancyNonBlocking;
    }

    public void setMaxHeapOccupancyNonBlocking(Memory maxHeapOccupancyNonBlocking) {
        this.maxHeapOccupancyNonBlocking = maxHeapOccupancyNonBlocking;
    }

    public Memory getMaxPermSpaceNonBlocking() {
        return maxPermSpaceNonBlocking;
    }

    public void setMaxPermSpaceNonBlocking(Memory maxPermSpaceNonBlocking) {
        this.maxPermSpaceNonBlocking = maxPermSpaceNonBlocking;
    }

    public Memory getMaxPermOccupancyNonBlocking() {
        return maxPermOccupancyNonBlocking;
    }

    public void setMaxPermOccupancyNonBlocking(Memory maxPermOccupancyNonBlocking) {
        this.maxPermOccupancyNonBlocking = maxPermOccupancyNonBlocking;
    }

    /**
     * @return Throughput based only on garbage collection as a percent rounded to the nearest integer. CG throughput is
     *         the percent of time not spent doing GC. 0 means all time was spent doing GC. 100 means no time was spent
     *         doing GC.
     */
    public long getGcThroughput() {
        if (blockingEventCount <= 0) {
            return 100L;
        }
        long timeNotGc = getJvmRunDuration() - totalGcPause;
        BigDecimal throughput = new BigDecimal(timeNotGc);
        throughput = throughput.divide(new BigDecimal(getJvmRunDuration()), 2, HALF_EVEN);
        return throughput.movePointRight(2).longValue();
    }

    /**
     * @return Throughput based on stopped time as a percent rounded to the nearest integer. Stopped time throughput is
     *         the percent of total time the JVM threads were running (not in a safepoint). 0 means all stopped time.
     *         100 means no stopped time.
     */
    public long getStoppedTimeThroughput() {
        if (stoppedTimeEventCount <= 0) {
            return 100L;
        }
        if (getJvmRunDuration() <= 0) {
            return 0L;
        }
        long timeNotStopped = getJvmRunDuration() - totalStoppedTime;
        BigDecimal throughput = new BigDecimal(timeNotStopped);
        throughput = throughput.divide(new BigDecimal(getJvmRunDuration()), 2, HALF_EVEN);
        return throughput.movePointRight(2).longValue();
    }

    /**
     * @return Ratio of old/young space sizes rounded to whole number.
     */
    public long getNewRatio() {
        if (maxYoungSpace == null || maxOldSpace.getValue(KILOBYTES) == 0) {
            return 0;
        }
        BigDecimal ratio = new BigDecimal(maxOldSpace.getValue(KILOBYTES));
        ratio = ratio.divide(new BigDecimal(maxYoungSpace.getValue(KILOBYTES)), 0, HALF_EVEN);
        return ratio.intValue();
    }

    /**
     * 
     * @return Ratio of GC to Stopped Time as a percent rounded to the nearest integer. 100 means all stopped time spent
     *         doing GC. 0 means none of the stopped time was due to GC.
     */
    public long getGcStoppedRatio() {
        if (totalGcPause <= 0 || totalStoppedTime <= 0) {
            return 100L;
        }
        BigDecimal ratio = new BigDecimal(totalGcPause);
        ratio = ratio.divide(new BigDecimal(totalStoppedTime), 2, HALF_EVEN);
        return ratio.movePointRight(2).longValue();
    }

    /**
     * Do analysis.
     */
    public void doAnalysis() {

        if (jvm.getOptions() != null) {
            doJvmOptionsAnalysis();
        }

        // Unidentified logging lines
        if (!getUnidentifiedLogLines().isEmpty()) {
            if (!preprocessed) {
                analysis.add(ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE);
                // Don't double report
                if (analysis.contains(INFO_UNIDENTIFIED_LOG_LINE_LAST)) {
                    analysis.remove(INFO_UNIDENTIFIED_LOG_LINE_LAST);
                }
            } else if (getUnidentifiedLogLines().size() == 1) {
                // Check if the unidentified line is not the last preprocessed line but it is the beginning of the last
                // unpreprocessed line (the line was split).
                if (!analysis.contains(INFO_UNIDENTIFIED_LOG_LINE_LAST)
                        && lastLogLineUnprocessed.startsWith(getUnidentifiedLogLines().get(0))) {
                    analysis.add(INFO_UNIDENTIFIED_LOG_LINE_LAST);
                }
            } else {
                analysis.add(0, WARN_UNIDENTIFIED_LOG_LINE_REPORT);
                // Don't double report
                if (analysis.contains(INFO_UNIDENTIFIED_LOG_LINE_LAST)) {
                    analysis.remove(INFO_UNIDENTIFIED_LOG_LINE_LAST);
                }
            }
        }

        if (haveData()) {
            doDataAnalysis();
        }
    }

    /**
     * Do JVM options analysis.
     */
    private void doJvmOptionsAnalysis() {

        // Check to see if thread stack size explicitly set
        if (jvm.getThreadStackSizeOption() == null && !jvm.is64Bit()) {
            analysis.add(WARN_THREAD_STACK_SIZE_NOT_SET);
        }

        // Check to see if min and max heap sizes are the same
        if (!jvm.isMinAndMaxHeapSpaceEqual()) {
            analysis.add(WARN_HEAP_MIN_NOT_EQUAL_MAX);
            if (jvm.getUseAdaptiveSizePolicyDisabledOption() != null) {
                analysis.add(ERROR_ADAPTIVE_SIZE_POLICY_DISABLED);
            }
        }

        // Check to see if min and max perm gen sizes are the same
        if (!jvm.isMinAndMaxPermSpaceEqual()) {
            analysis.add(WARN_PERM_MIN_NOT_EQUAL_MAX);
        }

        // Check to see if perm gen explicitly set
        if (analysis.contains(INFO_PERM_GEN)) {
            if (jvm.getMinPermOption() == null && jvm.getMaxPermOption() == null) {
                analysis.add(WARN_PERM_SIZE_NOT_SET);
            }
        }

        // Check to see if explicit gc is disabled
        if (jvm.getDisableExplicitGCOption() != null) {
            analysis.add(WARN_EXPLICIT_GC_DISABLED);
        }

        // Check for large thread stack size
        if (jvm.hasLargeThreadStackSize() && !jvm.is64Bit()) {
            analysis.add(WARN_THREAD_STACK_SIZE_LARGE);
        }

        // Check for setting DGC intervals when explicit GC is disabled.
        if (jvm.getDisableExplicitGCOption() != null && jvm.getRmiDgcClientGcIntervalOption() != null) {
            analysis.add(WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT);
        }
        if (jvm.getDisableExplicitGCOption() != null && jvm.getRmiDgcServerGcIntervalOption() != null) {
            analysis.add(WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT);
        }

        // Check for small DGC intervals.
        if (jvm.getRmiDgcClientGcIntervalOption() != null) {
            long rmiDgcClientGcInterval = Long.parseLong(jvm.getRmiDgcClientGcIntervalValue());
            if (rmiDgcClientGcInterval < 3_600_000) {
                analysis.add(WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL);
            }
        }
        if (jvm.getRmiDgcServerGcIntervalOption() != null) {
            long rmiDgcServerGcInterval = Long.parseLong(jvm.getRmiDgcServerGcIntervalValue());
            if (rmiDgcServerGcInterval < 3_600_000) {
                analysis.add(WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL);
            }
        }

        // Check if explicit gc not detected, but JVM not configured to handle it concurrently.
        if ((collectorFamilies.contains(CollectorFamily.CMS) || collectorFamilies.contains(CollectorFamily.G1))
                && jvm.getDisableExplicitGCOption() == null && jvm.getExplicitGcInvokesConcurrentOption() == null
                && !analysis.contains(ERROR_EXPLICIT_GC_SERIAL_G1)
                && !analysis.contains(ERROR_EXPLICIT_GC_SERIAL_CMS)) {
            analysis.add(WARN_EXPLICIT_GC_NOT_CONCURRENT);
        }

        // Specifying that explicit gc be collected concurrently makes no sense if explicit gc is disabled.
        if (jvm.getDisableExplicitGCOption() != null && jvm.getExplicitGcInvokesConcurrentOption() != null) {
            analysis.add(WARN_EXPLICIT_GC_DISABLED_CONCURRENT);
        }

        // Check to see if heap dump on OOME disabled or missing.
        if (jvm.getHeapDumpOnOutOfMemoryErrorDisabledOption() != null) {
            analysis.add(WARN_HEAP_DUMP_ON_OOME_DISABLED);
        } else if (jvm.getHeapDumpOnOutOfMemoryErrorEnabledOption() == null) {
            analysis.add(WARN_HEAP_DUMP_ON_OOME_MISSING);
        }

        // Check if heap dump path missing.
        if (jvm.getHeapDumpOnOutOfMemoryErrorEnabledOption() != null && jvm.getHeapDumpPathOption() == null) {
            analysis.add(INFO_HEAP_DUMP_PATH_MISSING);
        }

        // Check if instrumentation being used.
        if (jvm.getJavaagentOption() != null) {
            analysis.add(INFO_INSTRUMENTATION);
        }

        // Check if native library being used.
        if (jvm.getAgentpathOption() != null) {
            analysis.add(INFO_NATIVE);
        }

        // Check if background compilation disabled.
        if (jvm.getXBatchOption() != null || jvm.getDisableBackgroundCompilationOption() != null) {
            analysis.add(WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED);
        }

        // Check if compilation being forced on first invocation.
        if (jvm.getXCompOption() != null) {
            analysis.add(WARN_BYTECODE_COMPILE_FIRST_INVOCATION);
        }

        // Check if just in time (JIT) compilation disabled.
        if (jvm.getXIntOption() != null) {
            analysis.add(WARN_BYTECODE_COMPILE_DISABLED);
        }

        // Check for command line flags output.
        if (jvm.getPrintCommandLineFlagsOption() == null && !getEventTypes().isEmpty()
                && !getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS)) {
            analysis.add(WARN_PRINT_COMMANDLINE_FLAGS);
        }

        // Check if print gc details option disabled
        if (jvm.getPrintGCDetailsDisabled() != null) {
            analysis.add(WARN_PRINT_GC_DETAILS_DISABLED);
        } else {
            // Check if print gc details option missing
            if (jvm.getPrintGCDetailsOption() == null) {
                analysis.add(WARN_PRINT_GC_DETAILS_MISSING);
            }
        }

        // Check if CMS not being used for old collections
        if (jvm.getUseParNewGCOption() != null && jvm.getUseConcMarkSweepGCOption() == null) {
            analysis.add(ERROR_CMS_SERIAL_OLD);
        }

        // Check if CMS handling Perm/Metaspace collections is explictily disabled or just not set.
        if (jvm.getCMSClassUnloadingDisabled() != null) {
            // Explicitly disabled.
            analysis.add(WARN_CMS_CLASS_UNLOADING_DISABLED);
            //
            if (analysis.contains(WARN_CMS_CLASS_UNLOADING_NOT_ENABLED)) {
                analysis.remove(WARN_CMS_CLASS_UNLOADING_NOT_ENABLED);
            }
        } else {
            // Not enabled
            if ((collectorFamilies.contains(CollectorFamily.CMS) && jvm.getCMSClassUnloadingEnabled() == null)) {
                if (!analysis.contains(WARN_CMS_CLASS_UNLOADING_NOT_ENABLED)) {
                    analysis.add(WARN_CMS_CLASS_UNLOADING_NOT_ENABLED);
                }
            }
        }

        // Check for -XX:+PrintReferenceGC.
        if (jvm.getPrintReferenceGC() != null && !analysis.contains(WARN_PRINT_REFERENCE_GC_ENABLED)) {
            analysis.add(WARN_PRINT_REFERENCE_GC_ENABLED);
        }

        // Check for -XX:+PrintGCCause missing.
        if (jvm.getPrintGCCause() == null && jvm.JdkNumber() == 7 && !analysis.contains(WARN_PRINT_GC_CAUSE_MISSING)) {
            analysis.add(WARN_PRINT_GC_CAUSE_MISSING);
            // Don't double report
            if (analysis.contains(WARN_PRINT_GC_CAUSE_NOT_ENABLED)) {
                analysis.remove(WARN_PRINT_GC_CAUSE_NOT_ENABLED);
            }
        }

        // Check for -XX:-PrintGCCause (PrintGCCause disabled).
        if (jvm.getPrintGCCauseDisabled() != null) {
            analysis.add(WARN_PRINT_GC_CAUSE_DISABLED);
            // Don't double report
            if (analysis.contains(WARN_PRINT_GC_CAUSE_NOT_ENABLED)) {
                analysis.remove(WARN_PRINT_GC_CAUSE_NOT_ENABLED);
            }
        }

        // Check for -XX:+TieredCompilation.
        if (jvm.getTieredCompilation() != null) {
            analysis.add(WARN_TIERED_COMPILATION_ENABLED);
        }

        // Check for -XX:+PrintStringDeduplicationStatistics.
        if (jvm.getPrintStringDeduplicationStatistics() != null) {
            analysis.add(WARN_PRINT_STRING_DEDUP_STATS_ENABLED);
        }

        // Check for incremental mode in combination with -XX:CMSInitiatingOccupancyFraction=<n>.
        if (analysis.contains(WARN_CMS_INCREMENTAL_MODE) && jvm.getCMSInitiatingOccupancyFraction() != null) {
            analysis.add(WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT);
        }

        // Check for biased locking disabled with -XX:-UseBiasedLocking.
        if (jvm.getBiasedLockingDisabled() != null && !collectorFamilies.contains(CollectorFamily.SHENANDOAH)) {
            analysis.add(WARN_BIASED_LOCKING_DISABLED);
        }

        // Check for print class histogram output enabled with -XX:+PrintClassHistogram,
        // -XX:+PrintClassHistogramBeforeFullGC, or -XX:+PrintClassHistogramAfterFullGC.
        if (jvm.getPrintClassHistogramEnabled() != null) {
            analysis.add(WARN_PRINT_CLASS_HISTOGRAM);
        }
        if (jvm.getPrintClassHistogramAfterFullGcEnabled() != null) {
            analysis.add(WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC);
        }
        if (jvm.getPrintClassHistogramBeforeFullGcEnabled() != null) {
            analysis.add(WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC);
        }

        // Check for outputting application concurrent time
        if (!analysis.contains(WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME)) {
            if (jvm.getPrintGcApplicationConcurrentTime() != null) {
                analysis.add(WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME);
            }
        }

        // Check for trace class unloading enabled with -XX:+TraceClassUnloading
        if (!analysis.contains(WARN_TRACE_CLASS_UNLOADING)) {
            if (jvm.getTraceClassUnloading() != null) {
                analysis.add(WARN_TRACE_CLASS_UNLOADING);
            }
        }

        // Compressed object references should only be used when heap < 32G
        boolean heapLessThan32G = jvm.getMaxHeapBytes().lessThan(gigabytes(32));

        if (heapLessThan32G) {
            // Should use compressed object pointers
            if (jvm.getUseCompressedOopsDisabled() != null) {

                if (jvm.getMaxHeapBytes().isZero()) {
                    // Heap size unknown
                    analysis.add(WARN_COMP_OOPS_DISABLED_HEAP_UNK);
                } else {
                    // Heap < 32G
                    analysis.add(ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G);
                }
                if (jvm.getCompressedClassSpaceSizeOption() != null) {
                    analysis.add(INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED);
                }
            }

            // Should use compressed class pointers
            if (jvm.getUseCompressedClassPointersDisabled() != null) {
                if (jvm.getMaxHeapBytes().isZero()) {
                    // Heap size unknown
                    analysis.add(WARN_COMP_CLASS_DISABLED_HEAP_UNK);
                } else {
                    // Heap < 32G
                    analysis.add(ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G);
                }
                if (jvm.getCompressedClassSpaceSizeOption() != null) {
                    analysis.add(INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED);
                }
            }
        } else {
            // Should not use compressed object pointers
            if (jvm.getUseCompressedOopsEnabled() != null) {
                analysis.add(ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G);
            }

            // Should not use compressed class pointers
            if (jvm.getUseCompressedClassPointersEnabled() != null) {
                analysis.add(ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G);
            }

            // Should not be setting class pointer space size
            if (jvm.getCompressedClassSpaceSizeOption() != null) {
                analysis.add(ERROR_COMP_CLASS_SIZE_HEAP_GT_32G);
            }
        }

        // Check for PrintFLSStatistics option is being used
        if (jvm.getPrintFLStatistics() != null && !analysis.contains(INFO_PRINT_FLS_STATISTICS)) {
            analysis.add(INFO_PRINT_FLS_STATISTICS);
        }

        // Check if PAR_NEW collector disabled
        if (jvm.getUseParNewGcDisabled() != null && !analysis.contains(WARN_CMS_PAR_NEW_DISABLED)) {
            analysis.add(WARN_CMS_PAR_NEW_DISABLED);
        }

        // Check if log file rotation disabled or missing
        if (jvm.getUseGcLogFileRotationDisabled() != null) {
            analysis.add(INFO_GC_LOG_FILE_ROTATION_DISABLED);
        } else {
            if (jvm.getUseGcLogFileRotationEnabled() == null) {
                analysis.add(INFO_GC_LOG_FILE_ROTATION_NOT_ENABLED);
            }
        }

        // Check if number of log files specified with log file rotation disabled
        if (jvm.getNumberOfGcLogFiles() != null && jvm.getUseGcLogFileRotationDisabled() != null) {
            analysis.add(WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED);
        }

        // Check if log file size is small
        if (jvm.getGcLogFileSize() != null) {
            if (jvm.getGcLogFileSizeBytes().lessThan(megabytes(5))) {
                analysis.add(WARN_GC_LOG_FILE_SIZE_SMALL);
            }
        }

        // If explicit gc is disabled, don't need to set explicit gc options
        if (jvm.getExplicitGcInvokesConcurrentAndUnloadsClassesDisabled() != null
                && jvm.getDisableExplicitGCOption() != null) {
            analysis.add(INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA);
        }

        // Check for class unloading disabled
        if (jvm.getClassUnloadingDisabled() != null) {
            analysis.add(WARN_CLASS_UNLOADING_DISABLED);
        }

        // Check for -XX:+PrintPromotionFailure option being used
        if (jvm.getPrintPromotionFailureEnabled() != null && !analysis.contains(INFO_PRINT_PROMOTION_FAILURE)) {
            analysis.add(INFO_PRINT_PROMOTION_FAILURE);
        }

        // Check for -XX:+UseMembar option being used
        if (jvm.getUseMembarEnabled() != null) {
            analysis.add(WARN_USE_MEMBAR);
        }

        // Check for -XX:-PrintAdaptiveSizePolicy option being used
        if (jvm.getPrintAdaptiveResizePolicyDisabled() != null) {
            analysis.add(INFO_PRINT_ADAPTIVE_RESIZE_PLCY_DISABLED);
        }

        // Check for -XX:+PrintAdaptiveSizePolicy option being used
        if (jvm.getPrintAdaptiveResizePolicyEnabled() != null) {
            analysis.add(INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED);
        }

        // Check for-XX:CMSInitiatingOccupancyFraction without -XX:+UseCMSInitiatingOccupancyOnly.
        if (jvm.getCMSInitiatingOccupancyFraction() != null && jvm.getCMSInitiatingOccupancyOnlyEnabled() == null) {
            analysis.add(WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING);
        }

        // Check for tenuring disabled or default overriden
        if (jvm.getMaxTenuringThresholdOption() != null) {
            String maxTenuringThreshold = JdkUtil.getOptionValue(jvm.getMaxTenuringThresholdOption());
            if (maxTenuringThreshold != null) {
                int tenuring = Integer.parseInt(maxTenuringThreshold);
                if (tenuring == 0) {
                    analysis.add(WARN_TENURING_DISABLED);
                } else if (tenuring != 15) {
                    analysis.add(INFO_MAX_TENURING_OVERRIDE);
                }
            }
        }

        // Check for -XX:SurvivorRatio option being used
        if (jvm.getSurvivorRatio() != null) {
            analysis.add(INFO_SURVIVOR_RATIO);
        }

        // Check for -XX:TargetSurvivorRatio option being used
        if (jvm.getTargetSurvivorRatio() != null) {
            analysis.add(INFO_SURVIVOR_RATIO_TARGET);
        }

        // Check for experimental options being used
        if (jvm.getUnlockExperimentalVmOptionsEnabled() != null) {
            analysis.add(INFO_EXPERIMENTAL_VM_OPTIONS);
        }
        if (jvm.getUseFastUnorderedTimeStampsEnabled() != null) {
            analysis.add(WARN_FAST_UNORDERED_TIMESTAMPS);
            // Don't double report
            if (analysis.contains(INFO_EXPERIMENTAL_VM_OPTIONS)) {
                analysis.remove(INFO_EXPERIMENTAL_VM_OPTIONS);
            }
        }
        if (jvm.getG1MixedGCLiveThresholdPercent() != null) {
            analysis.add(WARN_G1_MIXED_GC_LIVE_THRSHOLD_PRCNT);
            // Don't double report
            if (analysis.contains(INFO_EXPERIMENTAL_VM_OPTIONS)) {
                analysis.remove(INFO_EXPERIMENTAL_VM_OPTIONS);
            }
        }
        if (jvm.getUseCGroupMemoryLimitForHeap() != null) {
            analysis.add(WARN_CGROUP_MEMORY_LIMIT);
            // Don't double report
            if (analysis.contains(INFO_EXPERIMENTAL_VM_OPTIONS)) {
                analysis.remove(INFO_EXPERIMENTAL_VM_OPTIONS);
            }
        }

        // Check for JDK < u40 recommendations (require experimental options)
        if ((collectorFamilies.contains(CollectorFamily.G1) || jvm.getUseG1Gc() != null) && jvm.JdkNumber() == 8
                && jvm.JdkUpdate() < 40) {
            if (jvm.getG1MixedGCLiveThresholdPercent() == null
                    || !jvm.getG1MixedGCLiveThresholdPercentValue().equals("85") || jvm.getG1HeapWastePercent() == null
                    || !jvm.getG1HeapWastePercentValue().equals("5")) {
                analysis.add(WARN_G1_JDK8_PRIOR_U40_RECS);
                if (analysis.contains(INFO_EXPERIMENTAL_VM_OPTIONS)) {
                    analysis.remove(INFO_EXPERIMENTAL_VM_OPTIONS);
                }
            }
        }

        // Check for multi-threaded CMS initial mark disabled
        if (jvm.getCmsParallelInitialMarkDisabled() != null
                && !analysis.contains(ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED)) {
            analysis.add(ERROR_CMS_PARALLEL_INITIAL_MARK_DISABLED);
        }

        // Check for multi-threaded CMS remark disabled
        if (jvm.getCmsParallelRemarkDisabled() != null && !analysis.contains(ERROR_CMS_PARALLEL_REMARK_DISABLED)) {
            analysis.add(ERROR_CMS_PARALLEL_REMARK_DISABLED);
        }

        // Check if summarized remembered set processing information being output
        if (collectorFamilies.contains(CollectorFamily.G1) && jvm.getG1SummarizeRSetStatsEnabled() != null
                && jvm.getG1SummarizeRSetStatsPeriodValue() != null) {
            int period = Integer.parseInt(jvm.getG1SummarizeRSetStatsPeriodValue());
            if (period > 0) {
                analysis.add(INFO_G1_SUMMARIZE_RSET_STATS_OUTPUT);
            }
        }

        // Check if MaxMetaspaceSize is less than CompressedClassSpaceSize.
        if (jvm.getMaxMetaspaceOption() != null) {
            Memory compressedClassSpaceSize = Memory.ZERO;
            if (jvm.getCompressedClassSpaceSizeOption() != null) {
                compressedClassSpaceSize = jvm.getCompressedClassSpaceSizeBytes();
            } else {
                // Default is 1g
                compressedClassSpaceSize = gigabytes(1);
            }
            if (jvm.getMaxMetaspaceBytes().lessThan(compressedClassSpaceSize)) {
                analysis.add(ERROR_METASPACE_SIZE_LT_COMP_CLASS_SIZE);
            }
        }

        // Check if heap dump filename specified
        if (jvm.getHeapDumpPathValue() != null && !jvm.getHeapDumpPathValue().matches("^\\s*[\\//]$")) {
            analysis.add(WARN_HEAP_DUMP_PATH_FILENAME);
        }

        // Check for unaccounted disabled options
        if (jvm.getUnaccountedDisabledOptions() != null) {
            analysis.add(INFO_UNACCOUNTED_OPTIONS_DISABLED);
        }
    }

    /**
     * Do data analysis.
     */
    private void doDataAnalysis() {
        // Check for partial log
        if (firstGcEvent != null && !firstGcEvent.getLogEntry().matches(DATESTAMP_EVENT)
                && GcUtil.isPartialLog(firstGcEvent.getTimestamp())) {
            analysis.add(INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED);
        }

        // Check to see if application stopped time enabled
        if (!(eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME)
                || eventTypes.contains(LogEventType.UNIFIED_APPLICATION_STOPPED_TIME))) {
            analysis.add(WARN_APPLICATION_STOPPED_TIME_MISSING);
        }

        // Check for significant stopped time unrelated to GC
        if (eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME)
                && getGcStoppedRatio() < Constants.GC_STOPPED_RATIO_THRESHOLD
                && getStoppedTimeThroughput() != getGcThroughput()) {
            analysis.add(WARN_GC_STOPPED_RATIO);
        }

        // Check if logging indicates gc details missing
        if (!analysis.contains(WARN_PRINT_GC_DETAILS_MISSING) && !analysis.contains(WARN_PRINT_GC_DETAILS_DISABLED)) {
            if (getEventTypes().contains(LogEventType.VERBOSE_GC_OLD)
                    || getEventTypes().contains(LogEventType.VERBOSE_GC_YOUNG)) {
                analysis.add(WARN_PRINT_GC_DETAILS_MISSING);
            }
        }

        // Check for -XX:+PrintReferenceGC by event type
        if (!analysis.contains(WARN_PRINT_REFERENCE_GC_ENABLED)) {
            if (getEventTypes().contains(LogEventType.REFERENCE_GC)) {
                analysis.add(WARN_PRINT_REFERENCE_GC_ENABLED);
            }
        }

        // Check for PAR_NEW disabled.
        if (getEventTypes().contains(LogEventType.SERIAL_NEW) && collectorFamilies.contains(CollectorFamily.CMS)) {
            // Replace general gc.serial analysis
            if (analysis.contains(ERROR_SERIAL_GC)) {
                analysis.remove(ERROR_SERIAL_GC);
            }
            if (!analysis.contains(WARN_CMS_PAR_NEW_DISABLED)) {
                analysis.add(WARN_CMS_PAR_NEW_DISABLED);
            }
        }

        // Check for swappiness
        if (getJvm().getPercentSwapFree() < 95) {
            analysis.add(INFO_SWAPPING);
        }
        // Check for swap disabled
        if (getJvm().getSwap().isZero()) {
            analysis.add(INFO_SWAP_DISABLED);
        }

        // Check for insufficient physical memory
        if (!getJvm().getPhysicalMemory().isZero()) {
            Memory jvmMemory;
            if (jvm.getUseCompressedOopsDisabled() == null && jvm.getUseCompressedClassPointersDisabled() == null) {
                // Using compressed class pointers space
                jvmMemory = getJvm().getMaxHeapBytes().plus(getJvm().getMaxPermBytes())
                        .plus(getJvm().getMaxMetaspaceBytes()).plus(getJvm().getCompressedClassSpaceSizeBytes());
            } else {
                // Not using compressed class pointers space
                jvmMemory = getJvm().getMaxHeapBytes().plus(getJvm().getMaxPermBytes())
                        .plus(getJvm().getMaxMetaspaceBytes());
            }
            if (jvmMemory.greaterThan(getJvm().getPhysicalMemory())) {
                analysis.add(ERROR_PHYSICAL_MEMORY);
            }
        }

        // Check for humongous allocations on old JDK not able to fully reclaim them in a young collection
        if (collectorFamilies.contains(CollectorFamily.G1) && analysis.contains(INFO_G1_HUMONGOUS_ALLOCATION)
                && (jvm.JdkNumber() == 7 || (jvm.JdkNumber() == 8 && jvm.JdkUpdate() < 60))) {
            // Don't double report
            analysis.remove(INFO_G1_HUMONGOUS_ALLOCATION);
            analysis.add(ERROR_G1_HUMONGOUS_JDK_OLD);
        }

        // Check for using G1 collector JDK < u40
        if ((collectorFamilies.contains(CollectorFamily.G1) || jvm.getUseG1Gc() != null) && jvm.JdkNumber() == 8
                && jvm.JdkUpdate() < 40) {
            analysis.add(WARN_G1_JDK8_PRIOR_U40);
        }

        // Check for young space >= old space
        if (maxYoungSpace != null && maxOldSpace != null && maxYoungSpace.getValue(KILOBYTES) > 0
                && maxYoungSpace.compareTo(maxOldSpace) >= 0) {
            analysis.add(INFO_NEW_RATIO_INVERTED);
        }

        // Check for inverted parallelism
        if (getInvertedParallelismCount() > 0) {
            analysis.add(WARN_PARALLELISM_INVERTED);
        }

        // Check for diagnostic options enabled
        if (jvm.getUnlockDiagnosticVmOptions() != null) {
            analysis.add(INFO_DIAGNOSTIC_VM_OPTIONS_ENABLED);
        }

        // Check for JMX enabled
        if (jvm.IsJmxEnabled()) {
            analysis.add(INFO_JMX_ENABLED);
        }
    }

    /**
     * @return The first gc or stopped event.
     */
    public LogEvent getFirstEvent() {
        LogEvent event = null;

        long firstGcEventTimeStamp = 0;
        if (firstGcEvent != null) {
            firstGcEventTimeStamp = firstGcEvent.getTimestamp();
        }
        long firstStoppedEventTimestamp = 0;
        if (firstStoppedEvent != null) {
            firstStoppedEventTimestamp = firstStoppedEvent.getTimestamp();
        }

        if (Math.min(firstGcEventTimeStamp, firstStoppedEventTimestamp) == 0) {
            if (firstGcEvent != null && firstGcEventTimeStamp >= firstStoppedEventTimestamp) {
                event = firstGcEvent;
            } else {
                event = firstStoppedEvent;
            }
        } else {
            if (firstGcEventTimeStamp <= firstStoppedEventTimestamp) {
                event = firstGcEvent;
            } else {
                event = firstStoppedEvent;
            }
        }

        return event;
    }

    /**
     * @return The last gc or stopped event.
     */
    public LogEvent getLastEvent() {
        long lastGcEventTimeStamp = lastGcEvent == null ? 0 : lastGcEvent.getTimestamp();
        long lastStoppedEventTimestamp = lastStoppedEvent == null ? 0 : lastStoppedEvent.getTimestamp();
        return lastGcEvent != null && lastGcEventTimeStamp >= lastStoppedEventTimestamp ? lastGcEvent
                : lastStoppedEvent;
    }

    /**
     * @return JVM run duration (milliseconds).
     */
    public long getJvmRunDuration() {

        long start = getFirstEvent() == null
                || getFirstEvent().getTimestamp() <= Constants.FIRST_TIMESTAMP_THRESHOLD * 1000 ? 0
                        : getFirstEvent().getTimestamp();

        // Use either last gc or last timestamp and add duration of gc/stop
        long lastGcEventTimeStamp = 0;
        long lastGcEventDuration = 0;
        if (lastGcEvent != null) {
            lastGcEventTimeStamp = lastGcEvent.getTimestamp();
            lastGcEventDuration = lastGcEvent.getDuration();
        }
        long lastStoppedEventTimestamp = 0;
        long lastStoppedEventDuration = 0;
        if (lastStoppedEvent != null) {
            lastStoppedEventTimestamp = lastStoppedEvent.getTimestamp();
            lastStoppedEventDuration = lastStoppedEvent.getDuration();
        }

        long end = lastStoppedEventTimestamp > lastGcEventTimeStamp
                ? lastStoppedEventTimestamp + JdkMath.convertMicrosToMillis(lastStoppedEventDuration).longValue()
                : lastGcEventTimeStamp + JdkMath.convertMicrosToMillis(lastGcEventDuration).longValue();
        return end - start;
    }

    /**
     * @return true if there is data, false otherwise (e.g. no logging lines recognized).
     */
    public boolean haveData() {
        return getEventTypes().size() >= 2 || !getEventTypes().contains(LogEventType.UNKNOWN);
    }
}
