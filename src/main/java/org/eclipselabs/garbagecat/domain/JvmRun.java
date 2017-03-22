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

import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
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
     * Analysis property keys.
     */
    private List<String> analysisKeys;

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

    public List<String> getAnalysisKeys() {
        return analysisKeys;
    }

    public void setAnalysisKeys(List<String> analysisKeys) {
        this.analysisKeys = analysisKeys;
    }

    public void setCollectorFamiles(List<CollectorFamily> collectorFamilies) {
        this.collectorFamilies = collectorFamilies;
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

    /*
     * Throughput based only on garbage collection as a percent rounded to the nearest integer. CG throughput is the
     * percent of time not spent doing GC. 0 means all time was spent doing GC. 100 means no time was spent doing GC.
     */
    public long getGcThroughput() {
        long gcThroughput;
        if (blockingEventCount > 0) {
            long timeNotGc = getJvmRunDuration() - new Long(totalGcPause).longValue();
            BigDecimal throughput = new BigDecimal(timeNotGc);
            throughput = throughput.divide(new BigDecimal(getJvmRunDuration()), 2, RoundingMode.HALF_EVEN);
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
            if (getJvmRunDuration() > 0) {
                long timeNotStopped = getJvmRunDuration() - new Long(totalStoppedTime).longValue();
                BigDecimal throughput = new BigDecimal(timeNotStopped);
                throughput = throughput.divide(new BigDecimal(getJvmRunDuration()), 2, RoundingMode.HALF_EVEN);
                throughput = throughput.movePointRight(2);
                stoppedTimeThroughput = throughput.longValue();
            } else {
                stoppedTimeThroughput = 0L;
            }
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
        if (firstGcEvent != null && GcUtil.isPartialLog(firstGcEvent.getTimestamp())) {
            analysisKeys.add(Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED);
        }

        // 2) Check to see if -XX:+PrintGCApplicationStoppedTime enabled
        if (!eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME)) {
            analysisKeys.add(Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING);
        }

        // 3) Check for significant stopped time unrelated to GC
        if (eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME)
                && getGcStoppedRatio() < Constants.GC_STOPPED_RATIO_THRESHOLD
                && getStoppedTimeThroughput() != getGcThroughput()) {
            analysisKeys.add(Analysis.WARN_GC_STOPPED_RATIO);
        }

        // 4) Check if logging indicates gc details missing
        if (!analysisKeys.contains(Analysis.WARN_PRINT_GC_DETAILS_MISSING)
                && !analysisKeys.contains(Analysis.WARN_PRINT_GC_DETAILS_DISABLED)) {
            if (getEventTypes().contains(LogEventType.VERBOSE_GC_OLD)
                    || getEventTypes().contains(LogEventType.VERBOSE_GC_YOUNG)) {
                analysisKeys.add(Analysis.WARN_PRINT_GC_DETAILS_MISSING);
            }
        }

        // 5) Check if CMS handling Perm/Metaspace collections by collector analysis (if no jvm options available and
        // class unloading has not already been detected).
        if (!analysisKeys.contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED)
                && !analysisKeys.contains(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED)) {
            if (getEventTypes().contains(LogEventType.CMS_REMARK)
                    && !getEventTypes().contains(LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING)) {
                analysisKeys.add(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED);
            }

        }

        // 6) Check for -XX:+PrintReferenceGC by event type
        if (!analysisKeys.contains(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED)) {
            if (getEventTypes().contains(LogEventType.REFERENCE_GC)) {
                analysisKeys.add(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED);
            }
        }

        // 7) Check for PAR_NEW disabled.
        if (getEventTypes().contains(LogEventType.SERIAL_NEW) && collectorFamilies.contains(CollectorFamily.CMS)) {
            // Replace general gc.serial analysis
            if (analysisKeys.contains(Analysis.ERROR_SERIAL_GC)) {
                analysisKeys.remove(Analysis.ERROR_SERIAL_GC);
            }
            if (!analysisKeys.contains(Analysis.WARN_CMS_PAR_NEW_DISABLED)) {
                analysisKeys.add(Analysis.WARN_CMS_PAR_NEW_DISABLED);
            }
        }

        // 8) Check for swappiness
        if (getJvm().getPercentSwapFree() < 95) {
            analysisKeys.add(Analysis.INFO_SWAPPING);
        }

        // 9) Check for insufficient physical memory
        if (getJvm().getPhysicalMemory() > 0) {
            Long jvmMemory;
            if (jvm.getUseCompressedOopsDisabled() == null && jvm.getUseCompressedClassPointersDisabled() == null) {
                // Using compressed class pointers space
                jvmMemory = getJvm().getMaxHeapBytes() + getJvm().getMaxPermBytes() + getJvm().getMaxMetaspaceBytes()
                        + getJvm().getCompressedClassSpaceSizeBytes();
            } else {
                // Not using compressed class pointers space
                jvmMemory = getJvm().getMaxHeapBytes() + getJvm().getMaxPermBytes() + getJvm().getMaxMetaspaceBytes();
            }
            if (jvmMemory > getJvm().getPhysicalMemory()) {
                analysisKeys.add(Analysis.ERROR_PHYSICAL_MEMORY);
            }
        }

        // 10) Unidentified logging lines
        if (getUnidentifiedLogLines().size() > 0) {
            if (!preprocessed) {
                analysisKeys.add(Analysis.ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE);
                // Don't double report
                if (analysisKeys.contains(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST)) {
                    analysisKeys.remove(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST);
                }
            } else if (getUnidentifiedLogLines().size() == 1) {
                // Check if the unidentified line is not the last preprocessed line but it is the beginning of the last
                // unpreprocessed line (the line was split).
                if (!analysisKeys.contains(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST)
                        && lastLogLineUnprocessed.startsWith(getUnidentifiedLogLines().get(0))) {
                    analysisKeys.add(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST);
                }
            } else {
                analysisKeys.add(0, Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT);
                // Don't double report
                if (analysisKeys.contains(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST)) {
                    analysisKeys.remove(Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST);
                }
            }
        }
    }

    /**
     * Do JVM options analysis.
     */
    private void doJvmOptionsAnalysis() {

        // Check to see if thread stack size explicitly set
        if (jvm.getThreadStackSizeOption() == null && !jvm.is64Bit()) {
            analysisKeys.add(Analysis.WARN_THREAD_STACK_SIZE_NOT_SET);
        }

        // Check to see if min and max heap sizes are the same
        if (!jvm.isMinAndMaxHeapSpaceEqual()) {
            analysisKeys.add(Analysis.WARN_HEAP_MIN_NOT_EQUAL_MAX);
        }

        // Check to see if min and max perm gen sizes are the same
        if (!jvm.isMinAndMaxPermSpaceEqual()) {
            analysisKeys.add(Analysis.WARN_PERM_MIN_NOT_EQUAL_MAX);
        }

        // Check to see if min and max metaspace sizes are the same
        if (!jvm.isMinAndMaxMetaspaceEqual()) {
            analysisKeys.add(Analysis.WARN_METASPACE_MIN_NOT_EQUAL_MAX);
        }

        // Check to see if permanent generation or metaspace size explicitly set
        switch (jvm.JdkNumber()) {
        case 5:
        case 6:
        case 7:
            if (jvm.getMinPermOption() == null && jvm.getMaxPermOption() == null) {
                analysisKeys.add(Analysis.WARN_PERM_SIZE_NOT_SET);
            }
            break;
        case 8:
            if (jvm.getMinMetaspaceOption() == null && jvm.getMaxMetaspaceOption() == null) {
                analysisKeys.add(Analysis.WARN_METASPACE_SIZE_NOT_SET);
            }
            break;
        default:
            if (jvm.getMinPermOption() == null && jvm.getMaxPermOption() == null && jvm.getMinMetaspaceOption() == null
                    && jvm.getMaxMetaspaceOption() == null) {
                analysisKeys.add(Analysis.WARN_PERM_METASPACE_SIZE_NOT_SET);
            }
        }

        // Check to see if explicit gc is disabled
        if (jvm.getDisableExplicitGCOption() != null) {
            analysisKeys.add(Analysis.WARN_EXPLICIT_GC_DISABLED);
        }

        // Check for large thread stack size
        if (jvm.hasLargeThreadStackSize() && !jvm.is64Bit()) {
            analysisKeys.add(Analysis.WARN_THREAD_STACK_SIZE_LARGE);
        }

        // Check if the RMI Distributed Garbage Collection (DGC) is managed.
        if (jvm.getRmiDgcClientGcIntervalOption() == null && jvm.getRmiDgcServerGcIntervalOption() == null
                && jvm.getDisableExplicitGCOption() == null) {
            analysisKeys.add(Analysis.WARN_RMI_DGC_NOT_MANAGED);
        }

        // Check for setting DGC intervals when explicit GC is disabled.
        if (jvm.getDisableExplicitGCOption() != null && jvm.getRmiDgcClientGcIntervalOption() != null) {
            analysisKeys.add(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_REDUNDANT);
        }
        if (jvm.getDisableExplicitGCOption() != null && jvm.getRmiDgcServerGcIntervalOption() != null) {
            analysisKeys.add(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_REDUNDANT);
        }

        // Check for small DGC intervals.
        if (jvm.getRmiDgcClientGcIntervalOption() != null) {
            long rmiDgcClientGcInterval = new Long(jvm.getRmiDgcClientGcIntervalValue()).longValue();
            if (rmiDgcClientGcInterval < 3600000) {
                analysisKeys.add(Analysis.WARN_RMI_DGC_CLIENT_GCINTERVAL_SMALL);
            }
        }
        if (jvm.getRmiDgcServerGcIntervalOption() != null) {
            long rmiDgcServerGcInterval = new Long(jvm.getRmiDgcServerGcIntervalValue()).longValue();
            if (rmiDgcServerGcInterval < 3600000) {
                analysisKeys.add(Analysis.WARN_RMI_DGC_SERVER_GCINTERVAL_SMALL);
            }
        }

        // Check if explicit gc should be handled concurrently.
        if ((collectorFamilies.contains(CollectorFamily.CMS) || collectorFamilies.contains(CollectorFamily.G1))
                && jvm.getDisableExplicitGCOption() == null && jvm.getExplicitGcInvokesConcurrentOption() == null) {
            analysisKeys.add(Analysis.ERROR_EXPLICIT_GC_NOT_CONCURRENT);
        }

        // Specifying that explicit gc be collected concurrently makes no sense if explicit gc is disabled.
        if (jvm.getDisableExplicitGCOption() != null && jvm.getExplicitGcInvokesConcurrentOption() != null) {
            analysisKeys.add(Analysis.WARN_EXPLICIT_GC_DISABLED_CONCURRENT);
        }

        // Check to see if heap dump on OOME disabled or missing.
        if (jvm.getHeapDumpOnOutOfMemoryErrorDisabledOption() != null) {
            analysisKeys.add(Analysis.WARN_HEAP_DUMP_ON_OOME_DISABLED);
        } else if (jvm.getHeapDumpOnOutOfMemoryErrorEnabledOption() == null) {
            analysisKeys.add(Analysis.WARN_HEAP_DUMP_ON_OOME_MISSING);
        }

        // Check if instrumentation being used.
        if (jvm.getJavaagentOption() != null) {
            analysisKeys.add(Analysis.INFO_INSTRUMENTATION);
        }

        // Check if native library being used.
        if (jvm.getAgentpathOption() != null) {
            analysisKeys.add(Analysis.INFO_NATIVE);
        }

        // Check if background compilation disabled.
        if (jvm.getXBatchOption() != null || jvm.getDisableBackgroundCompilationOption() != null) {
            analysisKeys.add(Analysis.WARN_BYTECODE_BACKGROUND_COMPILE_DISABLED);
        }

        // Check if compilation being forced on first invocation.
        if (jvm.getXCompOption() != null) {
            analysisKeys.add(Analysis.WARN_BYTECODE_COMPILE_FIRST_INVOCATION);
        }

        // Check if just in time (JIT) compilation disabled.
        if (jvm.getXIntOption() != null) {
            analysisKeys.add(Analysis.WARN_BYTECODE_COMPILE_DISABLED);
        }

        // Check for command line flags output.
        if (jvm.getPrintCommandLineFlagsOption() == null
                && !getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS)) {
            analysisKeys.add(Analysis.WARN_PRINT_COMMANDLINE_FLAGS);
        }

        // Check if print gc details option disabled
        if (jvm.getPrintGCDetailsDisabled() != null) {
            analysisKeys.add(Analysis.WARN_PRINT_GC_DETAILS_DISABLED);
        } else {
            // Check if print gc details option missing
            if (jvm.getPrintGCDetailsOption() == null) {
                analysisKeys.add(Analysis.WARN_PRINT_GC_DETAILS_MISSING);
            }
        }

        // Check if CMS not being used for old collections
        if (jvm.getUseParNewGCOption() != null && jvm.getUseConcMarkSweepGCOption() == null) {
            analysisKeys.add(Analysis.ERROR_CMS_NEW_SERIAL_OLD);
        }

        // Check if CMS handling Perm/Metaspace collections.
        if (jvm.getCMSClassUnloadingDisabled() != null) {
            analysisKeys.add(Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED);
        } else {
            if ((collectorFamilies.contains(CollectorFamily.CMS)
                    && !eventTypes.contains(LogEventType.CMS_REMARK_WITH_CLASS_UNLOADING)
                    && jvm.getCMSClassUnloadingEnabled() == null)) {
                analysisKeys.add(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED);
            }
        }

        // Check for -XX:+PrintReferenceGC.
        if (jvm.getPrintReferenceGC() != null) {
            analysisKeys.add(Analysis.WARN_PRINT_REFERENCE_GC_ENABLED);
        }

        // Check for -XX:+PrintGCCause missing.
        if (jvm.getPrintGCCause() == null && jvm.JdkNumber() == 7
                && !analysisKeys.contains(Analysis.WARN_PRINT_GC_CAUSE_MISSING)) {
            analysisKeys.add(Analysis.WARN_PRINT_GC_CAUSE_MISSING);
            // Don't double report
            if (analysisKeys.contains(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED)) {
                analysisKeys.remove(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED);
            }
        }

        // Check for -XX:-PrintGCCause (PrintGCCause disabled).
        if (jvm.getPrintGCCauseDisabled() != null) {
            analysisKeys.add(Analysis.WARN_PRINT_GC_CAUSE_DISABLED);
            // Don't double report
            if (analysisKeys.contains(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED)) {
                analysisKeys.remove(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED);
            }
        }

        // Check for -XX:+TieredCompilation.
        if (jvm.getTieredCompilation() != null) {
            analysisKeys.add(Analysis.WARN_TIERED_COMPILATION_ENABLED);
        }

        // Check for -XX:+PrintStringDeduplicationStatistics.
        if (jvm.getPrintStringDeduplicationStatistics() != null) {
            analysisKeys.add(Analysis.WARN_PRINT_STRING_DEDUP_STATS_ENABLED);
        }

        // Check for incremental mode in combination with -XX:CMSInitiatingOccupancyFraction=<n>.
        if (analysisKeys.contains(Analysis.WARN_CMS_INCREMENTAL_MODE)
                && jvm.getCMSInitiatingOccupancyFraction() != null) {
            analysisKeys.add(Analysis.WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT);
        }

        // Check for biased locking disabled with -XX:-UseBiasedLocking.
        if (jvm.getBiasedLockingDisabled() != null) {
            analysisKeys.add(Analysis.WARN_BIASED_LOCKING_DISABLED);
        }

        // Check for print class histogram output enabled with -XX:+PrintClassHistogram,
        // -XX:+PrintClassHistogramBeforeFullGC, or -XX:+PrintClassHistogramAfterFullGC.
        if (jvm.getPrintClassHistogramEnabled() != null) {
            analysisKeys.add(Analysis.WARN_PRINT_CLASS_HISTOGRAM);
        }
        if (jvm.getPrintClassHistogramAfterFullGcEnabled() != null) {
            analysisKeys.add(Analysis.WARN_PRINT_CLASS_HISTOGRAM_AFTER_FULL_GC);
        }
        if (jvm.getPrintClassHistogramBeforeFullGcEnabled() != null) {
            analysisKeys.add(Analysis.WARN_PRINT_CLASS_HISTOGRAM_BEFORE_FULL_GC);
        }

        // Check for outputting application concurrent time
        if (!analysisKeys.contains(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME)) {
            if (jvm.getPrintGcApplicationConcurrentTime() != null) {
                analysisKeys.add(Analysis.WARN_PRINT_GC_APPLICATION_CONCURRENT_TIME);
            }
        }

        // Check for trace class unloading enabled with -XX:+TraceClassUnloading
        if (!analysisKeys.contains(Analysis.WARN_TRACE_CLASS_UNLOADING)) {
            if (jvm.getTraceClassUnloading() != null) {
                analysisKeys.add(Analysis.WARN_TRACE_CLASS_UNLOADING);
            }
        }

        // Compressed object references should only be used when heap < 32G
        boolean heapLessThan32G = true;
        BigDecimal thirtyTwoGigabytes = new BigDecimal("32").multiply(Constants.GIGABYTE);
        if (jvm.getMaxHeapBytes() >= thirtyTwoGigabytes.longValue()) {
            heapLessThan32G = false;
        }

        if (heapLessThan32G) {
            // Should use compressed object pointers
            if (jvm.getUseCompressedOopsDisabled() != null) {

                if (jvm.getMaxHeapBytes() == 0) {
                    // Heap size unknown
                    analysisKeys.add(Analysis.WARN_COMP_OOPS_DISABLED_HEAP_UNK);
                } else {
                    // Heap < 32G
                    analysisKeys.add(Analysis.ERROR_COMP_OOPS_DISABLED_HEAP_LT_32G);
                }
                if (jvm.getCompressedClassSpaceSizeOption() != null) {
                    analysisKeys.add(Analysis.INFO_COMP_CLASS_SIZE_COMP_OOPS_DISABLED);
                }
            }

            // Should use compressed class pointers
            if (jvm.getUseCompressedClassPointersDisabled() != null) {
                if (jvm.getMaxHeapBytes() == 0) {
                    // Heap size unknown
                    analysisKeys.add(Analysis.WARN_COMP_CLASS_DISABLED_HEAP_UNK);
                } else {
                    // Heap < 32G
                    analysisKeys.add(Analysis.ERROR_COMP_CLASS_DISABLED_HEAP_LT_32G);
                }
                if (jvm.getCompressedClassSpaceSizeOption() != null) {
                    analysisKeys.add(Analysis.INFO_COMP_CLASS_SIZE_COMP_CLASS_DISABLED);
                }
            }

            if (jvm.getUseCompressedClassPointersEnabled() != null && jvm.getCompressedClassSpaceSizeOption() == null) {
                analysisKeys.add(Analysis.INFO_COMP_CLASS_SIZE_NOT_SET);
            }
        } else {
            // Should not use compressed object pointers
            if (jvm.getUseCompressedOopsEnabled() != null) {
                analysisKeys.add(Analysis.ERROR_COMP_OOPS_ENABLED_HEAP_GT_32G);
            }

            // Should not use compressed class pointers
            if (jvm.getUseCompressedClassPointersEnabled() != null) {
                analysisKeys.add(Analysis.ERROR_COMP_CLASS_ENABLED_HEAP_GT_32G);
            }

            // Should not be setting class pointer space size
            if (jvm.getCompressedClassSpaceSizeOption() != null) {
                analysisKeys.add(Analysis.ERROR_COMP_CLASS_SIZE_HEAP_GT_32G);
            }
        }

        // Check for PrintFLSStatistics option is being used
        if (jvm.getPrintFLStatistics() != null && !analysisKeys.contains(Analysis.INFO_PRINT_FLS_STATISTICS)) {
            analysisKeys.add(Analysis.INFO_PRINT_FLS_STATISTICS);
        }

        // Check if PARN_NEW collector disabled
        if (jvm.getUseParNewGcDisabled() != null && !analysisKeys.contains(Analysis.WARN_CMS_PAR_NEW_DISABLED)) {
            analysisKeys.add(Analysis.WARN_CMS_PAR_NEW_DISABLED);
        }

        // Check if log file rotation disabled
        if (jvm.getUseGcLogFileRotationDisabled() != null) {
            analysisKeys.add(Analysis.INFO_GC_LOG_FILE_ROTATION_DISABLED);
        }

        // Check if number of log files specified with log file rotation disabled
        if (jvm.getNumberOfGcLogFiles() != null && jvm.getUseGcLogFileRotationDisabled() != null) {
            analysisKeys.add(Analysis.WARN_GC_LOG_FILE_NUM_ROTATION_DISABLED);
        }

        // If explicit gc is disabled, don't need to set explicit gc options
        if (jvm.getExplicitGcInvokesConcurrentAndUnloadsClassesDisabled() != null
                && jvm.getDisableExplicitGCOption() != null) {
            analysisKeys.add(Analysis.INFO_CRUFT_EXP_GC_INV_CON_AND_UNL_CLA);
        }

        // Check for class unloading disabled
        if (jvm.getClassUnloadingDisabled() != null) {
            analysisKeys.add(Analysis.WARN_CLASS_UNLOADING_DISABLED);
        }

        // Check for -XX:+PrintPromotionFailure option being used
        if (jvm.getPrintPromotionFailureEnabled() != null
                && !analysisKeys.contains(Analysis.INFO_PRINT_PROMOTION_FAILURE)) {
            analysisKeys.add(Analysis.INFO_PRINT_PROMOTION_FAILURE);
        }

        // Check for -XX:+UseMembar option being used
        if (jvm.getUseMembarEnabled() != null) {
            analysisKeys.add(Analysis.WARN_USE_MEMBAR);
        }

        // Check for -XX:-PrintAdaptiveSizePolicy option being used
        if (jvm.getPrintAdaptiveResizePolicyDisabled() != null) {
            analysisKeys.add(Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_DISABLED);
        }

        // Check for -XX:+PrintAdaptiveSizePolicy option being used
        if (jvm.getPrintAdaptiveResizePolicyEnabled() != null) {
            analysisKeys.add(Analysis.INFO_PRINT_ADAPTIVE_RESIZE_PLCY_ENABLED);
        }

        // Check for-XX:CMSInitiatingOccupancyFraction without -XX:+UseCMSInitiatingOccupancyOnly.
        if (jvm.getCMSInitiatingOccupancyFraction() != null && jvm.getCMSInitiatingOccupancyOnlyEnabled() == null) {
            analysisKeys.add(Analysis.WARN_CMS_INIT_OCCUPANCY_ONLY_MISSING);
        }

        // Check for tenuring disabled or default overriden
        if (jvm.getMaxTenuringThresholdOption() != null) {
            String maxTenuringThreshold = JdkUtil.getOptionValue(jvm.getMaxTenuringThresholdOption());
            if (maxTenuringThreshold != null) {
                int tenuring = Integer.parseInt(maxTenuringThreshold);
                if (tenuring == 0 || tenuring > 15) {
                    analysisKeys.add(Analysis.WARN_TENURING_DISABLED);
                } else if ((collectorFamilies.contains(CollectorFamily.CMS) && tenuring != 6)
                        || ((collectorFamilies.contains(CollectorFamily.PARALLEL)
                                || collectorFamilies.contains(CollectorFamily.G1)) && tenuring != 15)) {
                    analysisKeys.add(Analysis.INFO_MAX_TENURING_OVERRIDE);
                }
            }
        }

        // Check for -XX:SurvivorRatio option being used
        if (jvm.getSurvivorRatio() != null) {
            analysisKeys.add(Analysis.INFO_SURVIVOR_RATIO);
        }

        // Check for -XX:TargetSurvivorRatio option being used
        if (jvm.getTargetSurvivorRatio() != null) {
            analysisKeys.add(Analysis.INFO_SURVIVOR_RATIO_TARGET);
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
        LogEvent event = null;

        long lastGcEventTimeStamp = 0;
        if (lastGcEvent != null) {
            lastGcEventTimeStamp = lastGcEvent.getTimestamp();
        }
        long lastStoppedEventTimestamp = 0;
        if (lastStoppedEvent != null) {
            lastStoppedEventTimestamp = lastStoppedEvent.getTimestamp();
        }

        if (lastGcEvent != null && lastGcEventTimeStamp >= lastStoppedEventTimestamp) {
            event = lastGcEvent;
        } else {
            event = lastStoppedEvent;
        }

        return event;
    }

    /**
     * @return JVM run duration (milliseconds).
     */
    public long getJvmRunDuration() {

        long start = 0;
        if (getFirstEvent() != null && getFirstEvent().getTimestamp() > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000) {
            // partial log
            start = getFirstEvent().getTimestamp();
        }

        long end = 0;
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

        if (lastStoppedEventTimestamp > lastGcEventTimeStamp) {
            end = lastStoppedEventTimestamp + JdkMath.convertMicrosToMillis(lastStoppedEventDuration).longValue();
        } else {
            end = lastGcEventTimeStamp + new Long(lastGcEventDuration).longValue();
        }

        return end - start;
    }
}
