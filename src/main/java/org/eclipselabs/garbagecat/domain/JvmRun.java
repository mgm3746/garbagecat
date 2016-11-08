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
package org.eclipselabs.garbagecat.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
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
     * Maximum heap size (kilobytes).
     */
    private int maxHeapSpace;

    /**
     * Maximum heap occupancy (kilobytes).
     */
    private int maxHeapOccupancy;

    /**
     * Maximum perm gen size (kilobytes).
     */
    private int maxPermSpace;

    /**
     * Maximum perm gen occupancy (kilobytes).
     */
    private int maxPermOccupancy;

    /**
     * Maximum GC pause duration (milliseconds).
     */
    private int maxGcPause;

    /**
     * Total GC pause duration (milliseconds).
     */
    private int totalGcPause;

    /**
     * Time of the first blocking event, in milliseconds after JVM startup.
     */
    private long firstGcTimestamp;

    /**
     * Time of the last blocking event, in milliseconds after JVM startup.
     */
    private long lastGcTimestamp;

    /**
     * Duration of the last blocking event (milliseconds). Required to compute throughput for very short JVM runs.
     */
    private long lastGcDuration;

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
     * Time of the first stopped event, in milliseconds after JVM startup.
     */
    private long firstStoppedTimestamp;

    /**
     * Time of the last stopped event, in milliseconds after JVM startup.
     */
    private long lastStoppedTimestamp;

    /**
     * Duration of the last stopped event (microseconds). Required to compute throughput for very short JVM runs.
     */
    private long lastStoppedDuration;

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
     * Analysis property keys.
     */
    private List<String> analysisKeys;

    /**
     * Collector families.
     */
    private List<CollectorFamily> collectorFamilies;

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

    public int getMaxHeapSpace() {
        return maxHeapSpace;
    }

    public void setMaxHeapSpace(int maxHeapSpace) {
        this.maxHeapSpace = maxHeapSpace;
    }

    public int getMaxHeapOccupancy() {
        return maxHeapOccupancy;
    }

    public void setMaxHeapOccupancy(int maxHeapOccupancy) {
        this.maxHeapOccupancy = maxHeapOccupancy;
    }

    public int getMaxPermSpace() {
        return maxPermSpace;
    }

    public void setMaxPermSpace(int maxPermSpace) {
        this.maxPermSpace = maxPermSpace;
    }

    public int getMaxPermOccupancy() {
        return maxPermOccupancy;
    }

    public void setMaxPermOccupancy(int maxPermOccupancy) {
        this.maxPermOccupancy = maxPermOccupancy;
    }

    public int getMaxGcPause() {
        return maxGcPause;
    }

    public void setMaxPause(int maxPause) {
        this.maxGcPause = maxPause;
    }

    public int getTotalGcPause() {
        return totalGcPause;
    }

    public void setTotalGcPause(int totalGcPause) {
        this.totalGcPause = totalGcPause;
    }

    public long getFirstGcTimestamp() {
        return firstGcTimestamp;
    }

    public void setFirstGcTimestamp(long firstGcTimestamp) {
        this.firstGcTimestamp = firstGcTimestamp;
    }

    public long getLastGcTimestamp() {
        return lastGcTimestamp;
    }

    public void setLastGcTimestamp(long lastGcTimestamp) {
        this.lastGcTimestamp = lastGcTimestamp;
    }

    public long getLastGcDuration() {
        return lastGcDuration;
    }

    public void setLastGcDuration(long lastGcDuration) {
        this.lastGcDuration = lastGcDuration;
    }

    public int getBlockingEventCount() {
        return blockingEventCount;
    }

    public void setBlockingEventCount(int blockingEventCount) {
        this.blockingEventCount = blockingEventCount;
    }

    public long getFirstStoppedTimestamp() {
        return firstStoppedTimestamp;
    }

    public void setFirstStoppedTimestamp(long firstStoppedTimestamp) {
        this.firstStoppedTimestamp = firstStoppedTimestamp;
    }

    public long getLastStoppedTimestamp() {
        return lastStoppedTimestamp;
    }

    public void setLastStoppedTimestamp(long lastStoppedTimestamp) {
        this.lastStoppedTimestamp = lastStoppedTimestamp;
    }

    public long getLastStoppedDuration() {
        return lastStoppedDuration;
    }

    public void setLastStoppedDuration(long lastStoppedDuration) {
        this.lastStoppedDuration = lastStoppedDuration;
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

    public List<String> getAnalysisKeys() {
        return analysisKeys;
    }

    public void setAnalysisKeys(List<String> analysisKeys) {
        this.analysisKeys = analysisKeys;
    }

    public void setCollectorFamiles(List<CollectorFamily> collectorFamilies) {
        this.collectorFamilies = collectorFamilies;
    }

    /*
     * Throughput based only on garbage collection as a percent rounded to the nearest integer. CG throughput is the
     * percent of time not spent doing GC. 0 means all time was spent doing GC. 100 means no time was spent doing GC.
     */
    public long getGcThroughput() {
        long gcThroughput;
        if (blockingEventCount > 0) {
            long timeTotal;
            if (lastGcTimestamp > firstGcTimestamp && firstGcTimestamp > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000) {
                // Partial log. Use the timestamp of the first GC event, not 0, in order to determine
                // throughput more accurately.
                timeTotal = lastGcTimestamp + new Long(lastGcDuration).longValue() - firstGcTimestamp;
            } else {
                // Complete log or a log with only 1 event.
                timeTotal = lastGcTimestamp + new Long(lastGcDuration).longValue();
            }
            long timeNotGc = timeTotal - new Long(totalGcPause).longValue();
            BigDecimal throughput = new BigDecimal(timeNotGc);
            throughput = throughput.divide(new BigDecimal(timeTotal), 2, RoundingMode.HALF_EVEN);
            throughput = throughput.movePointRight(2);
            gcThroughput = throughput.longValue();

        } else {
            gcThroughput = 100L;
        }
        return gcThroughput;
    }

    /*
     * Throughput based on stopped time as a percent rounded to the nearest integer. Stopped time throughput is the
     * percent of total time the JVM threads were running (not in a safepoint). 0 means all stopped time. 100 means no
     * stopped time.
     */
    public long getStoppedTimeThroughput() {
        long stoppedTimeThroughput;
        if (stoppedTimeEventCount > 0) {
            long timeTotal;
            if (lastStoppedTimestamp > firstStoppedTimestamp
                    && firstStoppedTimestamp > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000) {
                // Partial log. Use the timestamp of the first stopped event, not 0, in order to determine
                // throughput more accurately.
                timeTotal = lastStoppedTimestamp + JdkMath.convertMicrosToMillis(lastStoppedDuration).longValue()
                        - firstStoppedTimestamp;
            } else {
                // Complete log or a log with only 1 event.
                timeTotal = lastStoppedTimestamp + JdkMath.convertMicrosToMillis(lastStoppedDuration).longValue();
            }
            if (timeTotal == 0) {
                // Use GC timestamps
                if (lastGcTimestamp > firstGcTimestamp
                        && firstGcTimestamp > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000) {
                    // Partial log. Use the timestamp of the first GC event, not 0, in order to determine
                    // throughput more accurately.
                    timeTotal = lastGcTimestamp + new Long(lastGcDuration).longValue() - firstGcTimestamp;
                } else {
                    // Complete log or a log with only 1 event.
                    timeTotal = lastGcTimestamp + new Long(lastGcDuration).longValue();
                }
            }
            long timeNotGc = timeTotal - new Long(totalStoppedTime).longValue();
            BigDecimal throughput = new BigDecimal(timeNotGc);
            throughput = throughput.divide(new BigDecimal(timeTotal), 2, RoundingMode.HALF_EVEN);
            throughput = throughput.movePointRight(2);
            stoppedTimeThroughput = throughput.longValue();

        } else {
            stoppedTimeThroughput = 100L;
        }
        return stoppedTimeThroughput;
    }

    /**
     * 
     * @return Ratio of GC to Stopped Time as a percent rounded to the nearest integer. 100 means all stopped time spent
     *         doing GC. 0 means none of the stopped time was due to GC.
     */
    public long getGcStoppedRatio() {
        long gcStoppedRatio;
        if (totalGcPause > 0 && totalStoppedTime > 0) {
            BigDecimal ratio = new BigDecimal(totalGcPause);
            ratio = ratio.divide(new BigDecimal(totalStoppedTime), 2, RoundingMode.HALF_EVEN);
            ratio = ratio.movePointRight(2);
            gcStoppedRatio = ratio.longValue();

        } else {
            gcStoppedRatio = 100L;
        }
        return gcStoppedRatio;
    }

    /**
     * Do analysis.
     */
    public void doAnalysis() {

        if (jvm.getOptions() != null) {
            doJvmOptionsAnalysis();
        }

        // 1) Check for partial log
        if (GcUtil.isPartialLog(firstGcTimestamp)) {
            analysisKeys.add(Analysis.KEY_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED);
        }

        // 2) Check to see if -XX:+PrintGCApplicationStoppedTime enabled
        if (!eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME)) {
            analysisKeys.add(Analysis.KEY_APPLICATION_STOPPED_TIME_MISSING);
        }

        // 3) Check for significant stopped time unrelated to GC
        if (eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME)
                && getGcStoppedRatio() < Constants.GC_STOPPED_RATIO_THRESHOLD
                && getStoppedTimeThroughput() != getGcThroughput()) {
            analysisKeys.add(Analysis.KEY_GC_STOPPED_RATIO);
        }

        // 4) Check if logging indicates gc details missing
        if (!analysisKeys.contains(Analysis.KEY_PRINT_GC_DETAILS_MISSING)) {
            if (getEventTypes().contains(LogEventType.VERBOSE_GC_OLD)
                    || getEventTypes().contains(LogEventType.VERBOSE_GC_YOUNG)) {
                analysisKeys.add(Analysis.KEY_PRINT_GC_DETAILS_MISSING);
            }
        }

        // 5) Check for concurrent mode failure by logging event type. This is needed to deal with legacy code that does
        // not understand triggers and creates many unnecessary events instead of preparsing them into their component
        // events and/or triggers.
        if (!analysisKeys.contains(Analysis.KEY_CMS_CONCURRENT_MODE_FAILURE)) {
            if (getEventTypes().contains(LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE)
                    || getEventTypes().contains(LogEventType.PAR_NEW_CONCURRENT_MODE_FAILURE_PERM_DATA)
                    || getEventTypes().contains(LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE)
                    || getEventTypes()
                            .contains(LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_CONCURRENT_MODE_FAILURE_PERM_DATA)) {
                analysisKeys.add(Analysis.KEY_CMS_CONCURRENT_MODE_FAILURE);
            }
        }

        // 6) Check if CMS handling Perm/Metaspace collections by collector analysis (if no jvm options available and
        // class unloading has not already been detected).
        if (!analysisKeys.contains(Analysis.KEY_CMS_CLASS_UNLOADING_DISABLED)) {
            if (getEventTypes().contains(LogEventType.CMS_REMARK)
                    && !getEventTypes().contains(LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING)) {
                analysisKeys.add(Analysis.KEY_CMS_CLASS_UNLOADING_DISABLED);
            }

        }

        // 7) Check for CMS promotion failed by event type
        if (!analysisKeys.contains(Analysis.KEY_CMS_PROMOTION_FAILED)) {
            if (getEventTypes().contains(LogEventType.PAR_NEW_PROMOTION_FAILED)
                    || getEventTypes().contains(LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD)
                    || getEventTypes().contains(LogEventType.PAR_NEW_PROMOTION_FAILED_CMS_SERIAL_OLD_PERM_DATA)
                    || getEventTypes().contains(LogEventType.PAR_NEW_PROMOTION_FAILED_TRUNCATED)) {
                analysisKeys.add(Analysis.KEY_CMS_PROMOTION_FAILED);
            }
        }

        // 8) Check for -XX:+PrintReferenceGC by event type
        if (!analysisKeys.contains(Analysis.KEY_PRINT_REFERENCE_GC_ENABLED)) {
            if (getEventTypes().contains(LogEventType.REFERENCE_GC)) {
                analysisKeys.add(Analysis.KEY_PRINT_REFERENCE_GC_ENABLED);
            }
        }

        // 9) Check for print application concurrent time.
        if (!analysisKeys.contains(Analysis.KEY_PRINT_GC_APPLICATION_CONCURRENT_TIME)) {
            if (getEventTypes().contains(LogEventType.APPLICATION_CONCURRENT_TIME)) {
                analysisKeys.add(Analysis.KEY_PRINT_GC_APPLICATION_CONCURRENT_TIME);
            }
        }
    }

    /**
     * Do JVM options analysis.
     */
    private void doJvmOptionsAnalysis() {

        // Check to see if thread stack size explicitly set
        if (jvm.getThreadStackSizeOption() == null && !jvm.is64Bit()) {
            analysisKeys.add(Analysis.KEY_THREAD_STACK_SIZE_NOT_SET);
        }

        // Check to see if min and max heap sizes are the same
        if (!jvm.isMinAndMaxHeapSpaceEqual()) {
            analysisKeys.add(Analysis.KEY_MIN_HEAP_NOT_EQUAL_MAX_HEAP);
        }

        // Check to see if min and max perm gen sizes are the same
        if (!jvm.isMinAndMaxPermSpaceEqual()) {
            analysisKeys.add(Analysis.KEY_MIN_PERM_NOT_EQUAL_MAX_PERM);
        }

        // Check to see if min and max metaspace sizes are the same
        if (!jvm.isMinAndMaxMetaspaceEqual()) {
            analysisKeys.add(Analysis.KEY_MIN_METASPACE_NOT_EQUAL_MAX_METASPACE);
        }

        // Check to see if permanent generation or metaspace size explicitly set
        switch (jvm.JdkNumber()) {
        case 5:
        case 6:
        case 7:
            if (jvm.getMinPermOption() == null && jvm.getMaxPermOption() == null) {
                analysisKeys.add(Analysis.KEY_PERM_SIZE_NOT_SET);
            }
            break;
        case 8:
            if (jvm.getMinMetaspaceOption() == null && jvm.getMaxMetaspaceOption() == null) {
                analysisKeys.add(Analysis.KEY_METASPACE_SIZE_NOT_SET);
            }
            break;
        default:
            if (jvm.getMinPermOption() == null && jvm.getMaxPermOption() == null && jvm.getMinMetaspaceOption() == null
                    && jvm.getMaxMetaspaceOption() == null) {
                analysisKeys.add(Analysis.KEY_PERM_METASPACE_SIZE_NOT_SET);
            }
        }

        // Check to see if explicit gc is disabled
        if (jvm.getDisableExplicitGCOption() != null) {
            analysisKeys.add(Analysis.KEY_EXPLICIT_GC_DISABLED);
        }

        // Check for large thread stack size
        if (jvm.hasLargeThreadStackSize() && !jvm.is64Bit()) {
            analysisKeys.add(Analysis.KEY_THREAD_STACK_SIZE_LARGE);
        }

        // Check if the RMI Distributed Garbage Collection (DGC) is managed.
        if (jvm.getRmiDgcClientGcIntervalOption() == null && jvm.getRmiDgcServerGcIntervalOption() == null
                && jvm.getDisableExplicitGCOption() == null) {
            analysisKeys.add(Analysis.KEY_RMI_DGC_NOT_MANAGED);
        }

        // Check for setting DGC intervals when explicit GC is disabled.
        if (jvm.getDisableExplicitGCOption() != null && jvm.getRmiDgcClientGcIntervalOption() != null) {
            analysisKeys.add(Analysis.KEY_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT);
        }
        if (jvm.getDisableExplicitGCOption() != null && jvm.getRmiDgcServerGcIntervalOption() != null) {
            analysisKeys.add(Analysis.KEY_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT);
        }

        // Check for small DGC intervals.
        if (jvm.getRmiDgcClientGcIntervalOption() != null) {
            long rmiDgcClientGcInterval = new Long(jvm.getRmiDgcClientGcIntervalValue()).longValue();
            if (rmiDgcClientGcInterval < 3600000) {
                analysisKeys.add(Analysis.KEY_RMI_DGC_CLIENT_GCINTERVAL_SMALL);
            }
        }
        if (jvm.getRmiDgcServerGcIntervalOption() != null) {
            long rmiDgcServerGcInterval = new Long(jvm.getRmiDgcServerGcIntervalValue()).longValue();
            if (rmiDgcServerGcInterval < 3600000) {
                analysisKeys.add(Analysis.KEY_RMI_DGC_SERVER_GCINTERVAL_SMALL);
            }
        }

        // Check if explicit gc should be handled concurrently.
        if ((collectorFamilies.contains(CollectorFamily.CMS) || collectorFamilies.contains(CollectorFamily.G1))
                && jvm.getDisableExplicitGCOption() == null && jvm.getExplicitGcInvokesConcurrentOption() == null) {
            analysisKeys.add(Analysis.KEY_EXPLICIT_GC_NOT_CONCURRENT);
        }

        // Specifying that explicit gc be collected concurrently makes no sense if explicit gc is disabled.
        if (jvm.getDisableExplicitGCOption() != null && jvm.getExplicitGcInvokesConcurrentOption() != null) {
            analysisKeys.add(Analysis.KEY_EXPLICIT_GC_DISABLED_CONCURRENT);
        }

        // Check to see if heap dump on OOME disabled or missing.
        if (jvm.getHeapDumpOnOutOfMemoryErrorDisabledOption() != null) {
            analysisKeys.add(Analysis.KEY_HEAP_DUMP_ON_OOME_DISABLED);
        } else if (jvm.getHeapDumpOnOutOfMemoryErrorEnabledOption() == null) {
            analysisKeys.add(Analysis.KEY_HEAP_DUMP_ON_OOME_MISSING);
        }

        // Check if instrumentation being used.
        if (jvm.getJavaagentOption() != null) {
            analysisKeys.add(Analysis.KEY_INSTRUMENTATION);
        }

        // Check if native library being used.
        if (jvm.getAgentpathOption() != null) {
            analysisKeys.add(Analysis.KEY_NATIVE);
        }

        // Check if background compilation disabled.
        if (jvm.getXBatchOption() != null || jvm.getDisableBackgroundCompilationOption() != null) {
            analysisKeys.add(Analysis.KEY_BYTECODE_BACKGROUND_COMPILE_DISABLED);
        }

        // Check if compilation being forced on first invocation.
        if (jvm.getXCompOption() != null) {
            analysisKeys.add(Analysis.KEY_BYTECODE_COMPILE_FIRST_INVOCATION);
        }

        // Check if just in time (JIT) compilation disabled.
        if (jvm.getXIntOption() != null) {
            analysisKeys.add(Analysis.KEY_BYTECODE_COMPILE_DISABLED);
        }

        // Check for command line flags output.
        if (jvm.getPrintCommandLineFlagsOption() == null
                && !getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS)) {
            analysisKeys.add(Analysis.KEY_PRINT_COMMANDLINE_FLAGS);
        }

        // Check if print gc details option disabled
        if (jvm.getPrintGCDetailsDisabled() != null) {
            analysisKeys.add(Analysis.KEY_PRINT_GC_DETAILS_DISABLED);
        } else {
            // Check if print gc details option missing
            if (jvm.getPrintGCDetailsOption() == null) {
                analysisKeys.add(Analysis.KEY_PRINT_GC_DETAILS_MISSING);
            }
        }

        // Check if CMS not being used for old collections
        if (jvm.getUseParNewGCOption() != null && jvm.getUseConcMarkSweepGCOption() == null) {
            analysisKeys.add(Analysis.KEY_CMS_NEW_SERIAL_OLD);
        }

        // Check if CMS handling Perm/Metaspace collections.
        if ((collectorFamilies.contains(CollectorFamily.CMS)
                && !eventTypes.contains(LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING)
                && jvm.getCMSClassUnloadingEnabled() == null)) {
            analysisKeys.add(Analysis.KEY_CMS_CLASS_UNLOADING_DISABLED);
        }

        // Check for -XX:+PrintReferenceGC.
        if (jvm.getPrintReferenceGC() != null) {
            analysisKeys.add(Analysis.KEY_PRINT_REFERENCE_GC_ENABLED);
        }

        // Check for -XX:+PrintGCCause missing.
        if (jvm.getPrintGCCause() == null && jvm.JdkNumber() == 7) {
            analysisKeys.add(Analysis.KEY_PRINT_GC_CAUSE_MISSING);
        }

        // Check for -XX:-PrintGCCause (PrintGCCause disabled).
        if (jvm.getPrintGCCauseDisabled() != null) {
            analysisKeys.add(Analysis.KEY_PRINT_GC_CAUSE_DISABLED);
        }

        // Check for -XX:+TieredCompilation.
        if (jvm.getTieredCompilation() != null && jvm.JdkNumber() == 7) {
            analysisKeys.add(Analysis.KEY_JDK7_TIERED_COMPILATION_ENABLED);
        }

        // Check for -XX:+PrintStringDeduplicationStatistics.
        if (jvm.getPrintStringDeduplicationStatistics() != null) {
            analysisKeys.add(Analysis.KEY_PRINT_STRING_DEDUP_STATS_ENABLED);
        }

        // Check for incremental mode in combination with -XX:CMSInitiatingOccupancyFraction=<n>.
        if (analysisKeys.contains(Analysis.KEY_CMS_INCREMENTAL_MODE)
                && jvm.getCMSInitiatingOccupancyFraction() != null) {
            analysisKeys.add(Analysis.KEY_CMS_INC_MODE_INIT_OCCUP_FRACT_CONFLICT);
        }

        // Check for biased locking disabled with -XX:-UseBiasedLocking.
        if (jvm.getBiasedLockingDisabled() != null) {
            analysisKeys.add(Analysis.KEY_BIASED_LOCKING_DISABLED);
        }

        // Check for print class histogram enabled with -XX:+PrintClassHistogram.
        if (!analysisKeys.contains(Analysis.KEY_PRINT_CLASS_HISTOGRAM)) {
            if (jvm.getPrintClassHistogramEnabled() != null) {
                analysisKeys.add(Analysis.KEY_PRINT_CLASS_HISTOGRAM);
            }
        }

        // Check for print class histogram enabled with -XX:+PrintGCApplicationConcurrentTime
        if (!analysisKeys.contains(Analysis.KEY_PRINT_GC_APPLICATION_CONCURRENT_TIME)) {
            if (jvm.getPrintGcApplicationConcurrentTime() != null) {
                analysisKeys.add(Analysis.KEY_PRINT_GC_APPLICATION_CONCURRENT_TIME);
            }
        }

        // Check for trace class unloading enabled with -XX:+TraceClassUnloading
        if (!analysisKeys.contains(Analysis.KEY_TRACE_CLASS_UNLOADING)) {
            if (jvm.getTraceClassUnloading() != null) {
                analysisKeys.add(Analysis.KEY_TRACE_CLASS_UNLOADING);
            }
        }

        // Check for CompressedClassPointers enabled without setting CompressedClassSpaceSize
        if (jvm.getUseCompressedClassPointersEnabled() != null && jvm.getCompressedClassSpaceSize() == null) {
            analysisKeys.add(Analysis.KEY_COMPRESSED_CLASS_SPACE_NOT_SET);
        }

        // Check for PrintFLSStatistics option is being used
        if (jvm.getPrintFLStatistics() != null) {
            analysisKeys.add(Analysis.KEY_PRINT_FLS_STATISTICS);
        }
    }

    /**
     * @return Time of the first gc or stopped event, in milliseconds after JVM startup.
     */
    public long getFirstTimestamp() {
        long firstTimeStamp;
        if (Math.min(firstGcTimestamp, firstStoppedTimestamp) == 0) {
            firstTimeStamp = Math.max(firstGcTimestamp, firstStoppedTimestamp);
        } else {
            firstTimeStamp = Math.min(firstGcTimestamp, firstStoppedTimestamp);
        }
        return firstTimeStamp;
    }

    /**
     * @return Time of the lsst gc or stopped event, in milliseconds after JVM startup.
     */
    public long getLastTimestamp() {
        return Math.max(lastGcTimestamp, lastStoppedTimestamp);
    }
}
