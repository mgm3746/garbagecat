/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
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
import static org.eclipselabs.garbagecat.util.Memory.Unit.BYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_EXPLICIT_GC_SERIAL_G1;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_G1_HUMONGOUS_JDK_OLD;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_PHYSICAL_MEMORY;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_SERIAL_GC;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.ERROR_UNIDENTIFIED_LOG_LINES_PREPARSE;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_G1_HUMONGOUS_ALLOCATION;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_NEW_RATIO_INVERTED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_PERM_GEN;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_SWAPPING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_SWAP_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.INFO_UNIDENTIFIED_LOG_LINE_LAST;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_APPLICATION_STOPPED_TIME_MISSING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CMS_INCREMENTAL_MODE;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_GC_SAFEPOINT_RATIO;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_GC_STOPPED_RATIO;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PARALLELISM_INVERTED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PERM_MIN_NOT_EQUAL_MAX;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PERM_SIZE_NOT_SET;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_COMMANDLINE_FLAGS;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_GC_CAUSE_DISABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_GC_CAUSE_MISSING;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_SERIALISM_INVERTED;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_SYS_GT_USER;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_UNIDENTIFIED_LOG_LINE_REPORT;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipselabs.garbagecat.domain.jdk.unified.SafepointEventSummary;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedRegEx;
import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedUtil;
import org.github.joa.JvmOptions;
import org.github.joa.domain.GarbageCollector;

/**
 * JVM run data.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JvmRun {

    /**
     * Memory being allocated per second (kilobytes).
     */
    private BigDecimal allocationRate;

    /**
     * Analysis.
     */
    private List<Analysis> analysis;

    /**
     * Total number of blocking events.
     */
    private int blockingEventCount;

    /**
     * Number of <code>BlockingEvent</code>s where duration &gt; <code>TimesData</code> "real" time.
     */
    private long durationGtRealCount;

    /**
     * Maximum GC pause duration (microseconds).
     */
    private long durationMax;

    /**
     * Total GC pause duration (microseconds).
     */
    private long durationTotal;

    /**
     * Event types.
     */
    private List<LogEventType> eventTypes;

    /**
     * Maximum external root scanning time (microseconds).
     */
    private long extRootScanningTimeMax;

    /**
     * Total external root scanning time (microseconds).
     */
    private long extRootScanningTimeTotal;

    /**
     * The first blocking event.
     */
    private BlockingEvent firstGcEvent;

    /**
     * The first safepoint event.
     */
    private SafepointEvent firstSafepointEvent;

    /**
     * <code>BlockingEvent</code>s where throughput does not meet the throughput goal.
     */
    private List<String> gcBottlenecks;

    /**
     * Number of <code>ParallelCollection</code> with "inverted" parallelism.
     */
    private long invertedParallelismCount;

    /**
     * Number of <code>SerialCollection</code> with "inverted" serialism.
     */
    private long invertedSerialismCount;

    /**
     * Convenience field for the JDK version string.
     */
    private String jdkVersion;

    /**
     * JVM options.
     */
    private JvmOptions jvmOptions;

    /**
     * The last blocking event.
     */
    private BlockingEvent lastGcEvent;

    /**
     * Last log line unprocessed.
     */
    private String lastLogLineUnprocessed;

    /**
     * The last safepoint event.
     */
    private SafepointEvent lastSafepointEvent;

    /**
     * Maximum heap after gc.
     */
    private Memory maxHeapAfterGc;

    /**
     * Maximum heap occupancy.
     */
    private Memory maxHeapOccupancy;

    /**
     * Used for tracking max heap occupancy outside of <code>BlockingEvent</code>s.
     */
    private Memory maxHeapOccupancyNonBlocking;

    /**
     * Maximum heap size.
     */
    private Memory maxHeapSpace;

    /**
     * Used for tracking max heap space outside of <code>BlockingEvent</code>s.
     */
    private Memory maxHeapSpaceNonBlocking;

    /**
     * Maximum old space size.
     */
    private Memory maxOldSpace;

    /**
     * Maximum perm gen after gC (kilobytes).
     */
    private Memory maxPermAfterGc;

    /**
     * Maximum perm gen occupancy (kilobytes).
     */
    private Memory maxPermOccupancy;

    /**
     * Used for tracking max perm occupancy outside of <code>BlockingEvent</code>s.
     */
    private Memory maxPermOccupancyNonBlocking;

    /**
     * Maximum perm gen size (kilobytes).
     */
    private Memory maxPermSpace;

    /**
     * Used for tracking max perm space outside of <code>BlockingEvent</code>s.
     */
    private Memory maxPermSpaceNonBlocking;

    /**
     * Maximum young space size.
     */
    private Memory maxYoungSpace;

    /**
     * JVM memory information.
     */
    private String memory;

    /**
     * Number of <code>ParallelCollection</code> events.
     */
    private long parallelCount;

    /**
     * Physical memory.
     */
    private Memory physicalMemory = Memory.ZERO;

    /**
     * Physical memory free.
     */
    private Memory physicalMemoryFree = Memory.ZERO;

    /**
     * Whether or not the JVM events are from a preprocessed file.
     */
    private boolean preprocessed;

    /**
     * <code>SafepointEvent</code>s where throughput does not meet the throughput goal.
     */
    private List<String> safepointBottlenecks;

    /**
     * <code>SafepointEventSummary</code> used for reporting.
     */
    private List<SafepointEventSummary> safepointEventSummaries;

    /**
     * Number of <code>SerialCollection</code> events.
     */
    private long serialCount;

    /**
     * The date and time the JVM was started.
     */
    private Date startDate;

    /**
     * Total number of {@link org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent}.
     */
    private int stoppedTimeEventCount;

    /**
     * Maximum stopped time duration (microseconds).
     */
    private long stoppedTimeMax;

    /**
     * Total stopped time duration (microseconds).
     */
    private long stoppedTimeTotal;

    /**
     * Swap size.
     */
    private Memory swap = Memory.ZERO;

    /**
     * Swap free.
     */
    private Memory swapFree = Memory.ZERO;

    /**
     * Number of<code>ParallelCollection</code> or <code>Serial Collection</code> where sys exceeds user time.
     */
    private long sysGtUserCount;

    /**
     * Minimum throughput (percent of time spent not doing garbage collection for a given time interval) to not be
     * flagged a bottleneck.
     */
    private int throughputThreshold;

    /**
     * Log lines that do not match any existing logging patterns.
     */
    private List<String> unidentifiedLogLines;

    /**
     * Total number of {@link org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent}.
     */
    private int unifiedSafepointEventCount;

    /**
     * Maximum safepoint time duration (nanoseconds).
     */
    private long unifiedSafepointTimeMax;

    /**
     * Total unified safepoint time duration (nanoseconds).
     */
    private long unifiedSafepointTimeTotal;

    /**
     * The <code>BlockingEvent</code> with the greatest difference between the <code>BlockingEvent</code> duration and
     * the <code>TimesData</code> "other" time.
     */
    private LogEvent worstDurationGtRealTimeEvent;

    /**
     * <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    private LogEvent worstInvertedParallelismEvent;

    /**
     * <code>Serial Collection</code> event with the lowest "inverted" serialism.
     */
    private LogEvent worstInvertedSerialismEvent;

    /**
     * <code>ParallelCollection</code> or <code>Serial Collection</code> event with the greatest sys - user.
     */
    private LogEvent worstSysGtUserEvent;

    /**
     * Constructor accepting throughput threshold.
     * 
     * @param throughputThreshold
     *            throughput threshold for identifying bottlenecks.
     */
    public JvmRun(int throughputThreshold) {
        this.throughputThreshold = throughputThreshold;
    }

    /**
     * Do analysis.
     */
    public void doAnalysis() {
        if (jvmOptions != null) {
            jvmOptions.doAnalysis();
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
        // Try to infer event types when gc details are missing
        if (getEventTypes().contains(LogEventType.VERBOSE_GC_OLD)) {
            if (jvmOptions.getJvmContext().getGarbageCollectors().contains(GarbageCollector.G1)) {
                if (jvmOptions.getJvmContext().getVersionMajor() == 8) {
                    if (!getEventTypes().contains(LogEventType.G1_FULL_GC_SERIAL)) {
                        getEventTypes().add(LogEventType.G1_FULL_GC_SERIAL);
                        getEventTypes().remove(LogEventType.VERBOSE_GC_OLD);
                    }
                    if (!hasAnalysis(Analysis.ERROR_SERIAL_GC_G1)) {
                        analysis.add(Analysis.ERROR_SERIAL_GC_G1);
                    }
                }
            }
        }
        // Don't double report
        if (hasAnalysis(org.github.joa.util.Analysis.WARN_CMS_CLASS_UNLOADING_DISABLED)
                && hasAnalysis(WARN_CMS_CLASS_UNLOADING_NOT_ENABLED)) {
            analysis.remove(WARN_CMS_CLASS_UNLOADING_NOT_ENABLED);
        }
        // Check for partial log
        if (firstGcEvent != null || firstSafepointEvent != null) {
            long firstTimeStamp = Long.MIN_VALUE;
            if (firstGcEvent != null && !firstGcEvent.getLogEntry().matches(JdkRegEx.DATESTAMP_EVENT)
                    && !firstGcEvent.getLogEntry().matches(UnifiedRegEx.DATESTAMP_EVENT)) {
                firstTimeStamp = firstGcEvent.getTimestamp();
            }
            if (firstSafepointEvent != null && !firstSafepointEvent.getLogEntry().matches(JdkRegEx.DATESTAMP_EVENT)
                    && !firstSafepointEvent.getLogEntry().matches(UnifiedRegEx.DATESTAMP_EVENT)
                    && firstSafepointEvent.getTimestamp() < firstTimeStamp) {
                firstTimeStamp = firstSafepointEvent.getTimestamp();
            }
            if (GcUtil.isPartialLog(firstTimeStamp)) {
                analysis.add(INFO_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED);
            }
        }

        // Check to see if application stopped time enabled
        if (getBlockingEventCount() > 0 && !(eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME)
                || eventTypes.contains(LogEventType.UNIFIED_SAFEPOINT))) {
            analysis.add(WARN_APPLICATION_STOPPED_TIME_MISSING);
        }

        // Check for significant stopped time unrelated to GC
        if (eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME)
                && getGcStoppedRatio() < Constants.GC_SAFEPOINT_RATIO_THRESHOLD
                && getStoppedTimeThroughput() != getGcThroughput()) {
            analysis.add(WARN_GC_STOPPED_RATIO);
        }

        // Check for significant safepoint time unrelated to GC
        if (eventTypes.contains(LogEventType.UNIFIED_SAFEPOINT)
                && getGcUnifiedSafepointRatio() < Constants.GC_SAFEPOINT_RATIO_THRESHOLD && getJvmRunDuration() > 0
                && getUnifiedSafepointThroughput() != getGcThroughput()) {
            analysis.add(WARN_GC_SAFEPOINT_RATIO);
        }

        // Check if logging indicates gc details missing
        if (!hasAnalysis(org.github.joa.util.Analysis.WARN_JDK8_PRINT_GC_DETAILS_MISSING)
                && !hasAnalysis(org.github.joa.util.Analysis.WARN_JDK8_PRINT_GC_DETAILS_DISABLED)) {
            if (getEventTypes().contains(LogEventType.VERBOSE_GC_OLD)
                    || getEventTypes().contains(LogEventType.VERBOSE_GC_YOUNG)) {
                jvmOptions.addAnalysis(org.github.joa.util.Analysis.WARN_JDK8_PRINT_GC_DETAILS_MISSING);
            }
        }

        // Check for -XX:+PrintReferenceGC by event type
        if (!hasAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_REFERENCE_GC_ENABLED)) {
            if (getEventTypes().contains(LogEventType.REFERENCE_GC)) {
                jvmOptions.addAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_REFERENCE_GC_ENABLED);
            }
        }

        // Check for PAR_NEW disabled.
        if (getEventTypes().contains(LogEventType.SERIAL_NEW)
                && jvmOptions.getJvmContext().getGarbageCollectors().contains(GarbageCollector.CMS)) {
            // Replace general gc.serial analysis
            if (analysis.contains(ERROR_SERIAL_GC)) {
                analysis.remove(ERROR_SERIAL_GC);
            }
            if (!jvmOptions.hasAnalysis(org.github.joa.util.Analysis.WARN_JDK8_CMS_PAR_NEW_DISABLED)) {
                jvmOptions.addAnalysis(org.github.joa.util.Analysis.WARN_JDK8_CMS_PAR_NEW_DISABLED);
            }
        }

        // Check for swappiness
        if (getPercentSwapFree() < 95) {
            analysis.add(INFO_SWAPPING);
        }
        // Check for swap disabled
        if (getSwap().isZero()) {
            analysis.add(INFO_SWAP_DISABLED);
        }

        // Check for insufficient physical memory
        if (!getPhysicalMemory().isZero()) {
            Memory jvmMemory;
            if (!org.github.joa.util.JdkUtil.isOptionDisabled(jvmOptions.getUseCompressedOops())
                    && !org.github.joa.util.JdkUtil.isOptionDisabled(jvmOptions.getUseCompressedClassPointers())) {
                // Using compressed class pointers space
                jvmMemory = getMaxHeapBytes().plus(getMaxPermBytes()).plus(getMaxMetaspaceBytes())
                        .plus(getCompressedClassSpaceSizeBytes());

            } else {
                // Not using compressed class pointers space
                jvmMemory = getMaxHeapBytes().plus(getMaxPermBytes()).plus(getMaxMetaspaceBytes());
            }
            if (jvmMemory.greaterThan(getPhysicalMemory())) {
                analysis.add(ERROR_PHYSICAL_MEMORY);
            }
        }

        // Check for humongous allocations on old JDK not able to fully reclaim them in a young collection
        if (jvmOptions.getJvmContext().getGarbageCollectors().contains(GarbageCollector.G1)
                && analysis.contains(INFO_G1_HUMONGOUS_ALLOCATION)
                && (jvmOptions.getJvmContext().getVersionMajor() == 7
                        || (jvmOptions.getJvmContext().getVersionMajor() == 8
                                && jvmOptions.getJvmContext().getVersionMinor() < 60))) {
            // Don't double report
            analysis.remove(INFO_G1_HUMONGOUS_ALLOCATION);
            analysis.add(ERROR_G1_HUMONGOUS_JDK_OLD);
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
        // Check for inverted serialism
        if (getInvertedSerialismCount() > 0) {
            analysis.add(WARN_SERIALISM_INVERTED);
        }
        // Check for inverted serialism
        if (getSysGtUserCount() > 0) {
            analysis.add(WARN_SYS_GT_USER);
        }
        // Check to see if perm gen explicitly set
        if (analysis.contains(INFO_PERM_GEN)) {
            if (jvmOptions.getPermSize() == null && jvmOptions.getMaxPermSize() == null) {
                analysis.add(WARN_PERM_SIZE_NOT_SET);
            }
            // Check to see if min and max perm gen sizes are the same
            if (jvmOptions != null) {
                if (jvmOptions.getPermSize() != null && jvmOptions.getMaxPermSize() != null) {
                    if (org.github.joa.util.JdkUtil.getByteOptionBytes(org.github.joa.util.JdkUtil.getByteOptionValue(
                            jvmOptions.getPermSize())) != org.github.joa.util.JdkUtil.getByteOptionBytes(
                                    org.github.joa.util.JdkUtil.getByteOptionValue(jvmOptions.getMaxPermSize()))) {
                        analysis.add(WARN_PERM_MIN_NOT_EQUAL_MAX);
                    }
                }
            }
        }
        // Don't double report: If explicit gc detected, remove generic warning.
        if (hasAnalysis(org.github.joa.util.Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT)
                && (analysis.contains(ERROR_EXPLICIT_GC_SERIAL_G1)
                        || analysis.contains(ERROR_EXPLICIT_GC_SERIAL_CMS))) {
            jvmOptions.removeAnalysis(org.github.joa.util.Analysis.WARN_EXPLICIT_GC_NOT_CONCURRENT);
        }
        // Check for command line flags output.
        if (jvmOptions.getPrintCommandLineFlags() == null && !UnifiedUtil.isUnifiedLogging(getEventTypes())
                && !getEventTypes().isEmpty() && !getEventTypes().contains(LogEventType.HEADER_COMMAND_LINE_FLAGS)) {
            analysis.add(WARN_PRINT_COMMANDLINE_FLAGS);
        }

        // Determine why PrintGCCause is missing
        if (analysis.contains(WARN_PRINT_GC_CAUSE_NOT_ENABLED)) {
            if (jvmOptions.getPrintGcCause() == null && jvmOptions.getJvmContext().getVersionMajor() == 7) {
                analysis.add(WARN_PRINT_GC_CAUSE_MISSING);
                analysis.remove(WARN_PRINT_GC_CAUSE_NOT_ENABLED);
            }
            if (org.github.joa.util.JdkUtil.isOptionDisabled(jvmOptions.getPrintGcCause())) {
                analysis.add(WARN_PRINT_GC_CAUSE_DISABLED);
                analysis.remove(WARN_PRINT_GC_CAUSE_NOT_ENABLED);
            }
        }
        // Check for incremental mode in combination with -XX:CMSInitiatingOccupancyFraction=<n>.
        if (analysis.contains(WARN_CMS_INCREMENTAL_MODE) && jvmOptions.getCmsInitiatingOccupancyFraction() != null) {
            analysis.add(WARN_CMS_INC_MODE_WITH_INIT_OCCUP_FRACT);
        }
        // Check events for outputting application concurrent time
        if (!jvmOptions.hasAnalysis(org.github.joa.util.Analysis.INFO_PRINT_GC_APPLICATION_CONCURRENT_TIME)
                && getEventTypes().contains(LogEventType.APPLICATION_CONCURRENT_TIME)) {
            jvmOptions.addAnalysis(org.github.joa.util.Analysis.INFO_PRINT_GC_APPLICATION_CONCURRENT_TIME);
        }
        // Check events for trace class unloading enabled
        if (!jvmOptions.hasAnalysis(org.github.joa.util.Analysis.INFO_TRACE_CLASS_UNLOADING)
                && getEventTypes().contains(LogEventType.CLASS_UNLOADING)) {
            jvmOptions.addAnalysis(org.github.joa.util.Analysis.INFO_TRACE_CLASS_UNLOADING);
        }
        // Check events for PrintFLSStatistics
        if (!jvmOptions.hasAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_FLS_STATISTICS)
                && getEventTypes().contains(LogEventType.FLS_STATISTICS)) {
            jvmOptions.addAnalysis(org.github.joa.util.Analysis.INFO_JDK8_PRINT_FLS_STATISTICS);
        }
    }

    /**
     *
     * @return The amount of memory allocated per time unit expressed in MB/sec
     */
    public BigDecimal getAllocationRate() {
        return allocationRate;
    }

    /**
     * @return Analysis as a <code>List</code> of String arrays with 2 elements, the first the key, the second the
     *         display literal.
     */
    public List<String[]> getAnalysis() {
        List<String[]> a = new ArrayList<String[]>();
        Iterator<Analysis> itFelAnalysis = analysis.iterator();
        while (itFelAnalysis.hasNext()) {
            Analysis item = itFelAnalysis.next();
            a.add(new String[] { item.getKey(), item.getValue() });
        }
        if (jvmOptions != null) {
            Iterator<String[]> itJvmOptionsAnalysis = jvmOptions.getAnalysis().iterator();
            while (itJvmOptionsAnalysis.hasNext()) {
                String[] item = itJvmOptionsAnalysis.next();
                a.add(item);
            }
        }
        return a;
    }

    public int getBlockingEventCount() {
        return blockingEventCount;
    }

    /**
     * @return The compressed class space in bytes, or 0 if not set.
     */
    public Memory getCompressedClassSpaceSizeBytes() {
        return jvmOptions.getCompressedClassSpaceSize() == null ? Memory.ZERO
                : Memory.fromOptionSize(
                        org.github.joa.util.JdkUtil.getByteOptionValue(jvmOptions.getCompressedClassSpaceSize()));
    }

    public long getDurationGtRealCount() {
        return durationGtRealCount;
    }

    public long getDurationMax() {
        return durationMax;
    }

    public long getDurationTotal() {
        return durationTotal;
    }

    public List<LogEventType> getEventTypes() {
        return eventTypes;
    }

    public long getExtRootScanningTimeMax() {
        return extRootScanningTimeMax;
    }

    public long getExtRootScanningTimeTotal() {
        return extRootScanningTimeTotal;
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
        long firstSafepointEventTimestamp = 0;
        if (firstSafepointEvent != null) {
            firstSafepointEventTimestamp = firstSafepointEvent.getTimestamp();
        }

        if (Math.min(firstGcEventTimeStamp, firstSafepointEventTimestamp) == 0) {
            if (firstGcEvent != null && firstGcEventTimeStamp >= firstSafepointEventTimestamp) {
                event = firstGcEvent;
            } else {
                event = firstSafepointEvent;
            }
        } else {
            if (firstGcEventTimeStamp <= firstSafepointEventTimestamp) {
                event = firstGcEvent;
            } else {
                event = firstSafepointEvent;
            }
        }

        return event;
    }

    public BlockingEvent getFirstGcEvent() {
        return firstGcEvent;
    }

    public SafepointEvent getFirstSafepointEvent() {
        return firstSafepointEvent;
    }

    public List<String> getGcBottlenecks() {
        return gcBottlenecks;
    }

    /**
     * 
     * @return Ratio of GC to Stopped Time as a percent rounded to the nearest integer. 100 means all stopped time spent
     *         doing GC. 0 means none of the stopped time was due to GC.
     */
    public long getGcStoppedRatio() {
        if (durationTotal <= 0 || stoppedTimeTotal <= 0) {
            return 100L;
        }
        BigDecimal ratio = new BigDecimal(durationTotal);
        ratio = ratio.divide(new BigDecimal(stoppedTimeTotal), 2, HALF_EVEN);
        return ratio.movePointRight(2).longValue();
    }

    /**
     * @return Throughput based only on garbage collection as a percent rounded to the nearest integer. CG throughput is
     *         the percent of time not spent doing GC. 0 means all time was spent doing GC. 100 means no time was spent
     *         doing GC.
     */
    public long getGcThroughput() {
        if (blockingEventCount <= 0 || getJvmRunDuration() == 0) {
            return 100L;
        }
        long timeNotGc = getJvmRunDuration() - JdkMath.convertMicrosToMillis(durationTotal).longValue();
        BigDecimal throughput = new BigDecimal(timeNotGc);
        throughput = throughput.divide(new BigDecimal(getJvmRunDuration()), 2, HALF_EVEN);
        return throughput.movePointRight(2).longValue();
    }

    /**
     * 
     * @return Ratio of GC (microseconds) to unified safepoint (nanoseconds) time as a percent rounded to the nearest
     *         integer. 100 means all safepoint time spent doing GC. 0 means none of the safepoint time was due to GC.
     */
    public long getGcUnifiedSafepointRatio() {
        long unifiedSafepointTimeTotalMicros = JdkMath.convertNanosToMicros(unifiedSafepointTimeTotal).longValue();
        if (durationTotal <= 0 || unifiedSafepointTimeTotalMicros <= 0) {
            return 100L;
        }
        BigDecimal ratio = new BigDecimal(durationTotal);
        ratio = ratio.divide(new BigDecimal(unifiedSafepointTimeTotalMicros), 2, HALF_EVEN);
        return ratio.movePointRight(2).longValue();
    }

    public long getInvertedParallelismCount() {
        return invertedParallelismCount;
    }

    public long getInvertedSerialismCount() {
        return invertedSerialismCount;
    }

    public String getJdkVersion() {
        return jdkVersion;
    }

    public JvmOptions getJvmOptions() {
        return jvmOptions;
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
        if (lastSafepointEvent != null) {
            lastStoppedEventTimestamp = lastSafepointEvent.getTimestamp();
            lastStoppedEventDuration = lastSafepointEvent.getDuration();
        }

        long end = lastStoppedEventTimestamp > lastGcEventTimeStamp
                ? lastStoppedEventTimestamp + JdkMath.convertMicrosToMillis(lastStoppedEventDuration).longValue()
                : lastGcEventTimeStamp + JdkMath.convertMicrosToMillis(lastGcEventDuration).longValue();
        return end - start;
    }

    /**
     * @return The last gc or stopped event.
     */
    public LogEvent getLastEvent() {
        long lastGcEventTimeStamp = lastGcEvent == null ? 0 : lastGcEvent.getTimestamp();
        long lastStoppedEventTimestamp = lastSafepointEvent == null ? 0 : lastSafepointEvent.getTimestamp();
        return lastGcEvent != null && lastGcEventTimeStamp >= lastStoppedEventTimestamp ? lastGcEvent
                : lastSafepointEvent;
    }

    public BlockingEvent getLastGcEvent() {
        return lastGcEvent;
    }

    public String getLastLogLineUnprocessed() {
        return lastLogLineUnprocessed;
    }

    public SafepointEvent getLastSafepointEvent() {
        return lastSafepointEvent;
    }

    public Memory getMaxHeapAfterGc() {
        return maxHeapAfterGc;
    }

    /**
     * @return The maximum heap space, or 0 if not set.
     */
    public Memory getMaxHeapBytes() {
        return jvmOptions.getMaxHeapSize() == null ? Memory.ZERO
                : Memory.fromOptionSize(org.github.joa.util.JdkUtil.getByteOptionValue(jvmOptions.getMaxHeapSize()));
    }

    public Memory getMaxHeapOccupancy() {
        return maxHeapOccupancy;
    }

    public Memory getMaxHeapOccupancyNonBlocking() {
        return maxHeapOccupancyNonBlocking;
    }

    public Memory getMaxHeapSpace() {
        return maxHeapSpace;
    }

    public Memory getMaxHeapSpaceNonBlocking() {
        return maxHeapSpaceNonBlocking;
    }

    /**
     * @return The maximum metaspace in bytes, or 0 if not set.
     */
    public Memory getMaxMetaspaceBytes() {
        return jvmOptions.getMaxMetaspaceSize() == null ? Memory.ZERO
                : Memory.fromOptionSize(
                        org.github.joa.util.JdkUtil.getByteOptionValue(jvmOptions.getMaxMetaspaceSize()));
    }

    public Memory getMaxOldSpace() {
        return maxOldSpace;
    }

    public Memory getMaxPermAfterGc() {
        return maxPermAfterGc;
    }

    /**
     * @return The maximum perm space in bytes, or 0 if not set.
     */
    public Memory getMaxPermBytes() {
        return jvmOptions.getMaxPermSize() == null ? Memory.ZERO
                : Memory.fromOptionSize(org.github.joa.util.JdkUtil.getByteOptionValue(jvmOptions.getMaxPermSize()));
    }

    public Memory getMaxPermOccupancy() {
        return maxPermOccupancy;
    }

    public Memory getMaxPermOccupancyNonBlocking() {
        return maxPermOccupancyNonBlocking;
    }

    public Memory getMaxPermSpace() {
        return maxPermSpace;
    }

    public Memory getMaxPermSpaceNonBlocking() {
        return maxPermSpaceNonBlocking;
    }

    public Memory getMaxYoungSpace() {
        return maxYoungSpace;
    }

    /**
     * @return The JVM memory information.
     */
    public String getMemory() {
        return memory;
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

    public long getParallelCount() {
        return parallelCount;
    }

    /**
     * @return The percentage of swap that is free. 100 means no swap used. 0 means all swap used.
     */
    public long getPercentSwapFree() {
        if (!swap.greaterThan(Memory.ZERO)) {
            return 100L;
        }
        BigDecimal percentFree = new BigDecimal(swapFree.getValue(BYTES));
        percentFree = percentFree.divide(new BigDecimal(swap.getValue(BYTES)), 2, RoundingMode.HALF_EVEN);
        percentFree = percentFree.movePointRight(2);
        return percentFree.longValue();
    }

    public Memory getPhysicalMemory() {
        return physicalMemory;
    }

    public Memory getPhysicalMemoryFree() {
        return physicalMemoryFree;
    }

    public List<String> getSafepointBottlenecks() {
        return safepointBottlenecks;
    }

    public List<SafepointEventSummary> getSafepointEventSummaries() {
        return safepointEventSummaries;
    }

    public long getSerialCount() {
        return serialCount;
    }

    /**
     * @return The date and time the JVM was started.
     */
    public Date getStartDate() {
        return startDate;
    }

    public int getStoppedTimeEventCount() {
        return stoppedTimeEventCount;
    }

    public long getStoppedTimeMax() {
        return stoppedTimeMax;
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
        long timeNotStopped = getJvmRunDuration() - JdkMath.convertMicrosToMillis(stoppedTimeTotal).longValue();
        BigDecimal throughput = new BigDecimal(timeNotStopped);
        throughput = throughput.divide(new BigDecimal(getJvmRunDuration()), 2, HALF_EVEN);
        return throughput.movePointRight(2).longValue();
    }

    public long getStoppedTimeTotal() {
        return stoppedTimeTotal;
    }

    public Memory getSwap() {
        return swap;
    }

    public Memory getSwapFree() {
        return swapFree;
    }

    public long getSysGtUserCount() {
        return sysGtUserCount;
    }

    public int getThroughputThreshold() {
        return throughputThreshold;
    }

    public List<String> getUnidentifiedLogLines() {
        return unidentifiedLogLines;
    }

    public int getUnifiedSafepointEventCount() {
        return unifiedSafepointEventCount;
    }

    /**
     * @return Throughput based on safepoint time as a percent rounded to the nearest integer. Safepoint time throughput
     *         is the percent of total time the JVM threads were running (not in a safepoint). 0 means all safepoint
     *         time. 100 means no safepoint time.
     */
    public long getUnifiedSafepointThroughput() {
        if (unifiedSafepointEventCount <= 0) {
            return 100L;
        }
        if (getJvmRunDuration() <= 0) {
            return 0L;
        }
        long timeNotSafepoint = getJvmRunDuration()
                - JdkMath.convertNanosToMillis(unifiedSafepointTimeTotal).longValue();
        BigDecimal throughput = new BigDecimal(timeNotSafepoint);
        throughput = throughput.divide(new BigDecimal(getJvmRunDuration()), 2, HALF_EVEN);
        return throughput.movePointRight(2).longValue();
    }

    public long getUnifiedSafepointTimeMax() {
        return unifiedSafepointTimeMax;
    }

    public long getUnifiedSafepointTimeTotal() {
        return unifiedSafepointTimeTotal;
    }

    public LogEvent getWorstDurationGtRealTimeEvent() {
        return worstDurationGtRealTimeEvent;
    }

    public LogEvent getWorstInvertedParallelismEvent() {
        return worstInvertedParallelismEvent;
    }

    public LogEvent getWorstInvertedSerialismEvent() {
        return worstInvertedSerialismEvent;
    }

    public LogEvent getWorstSysGtUserEvent() {
        return worstSysGtUserEvent;
    }

    /**
     * @param key
     *            The {@link org.eclipselabs.garbagecat.util.jdk.Analysis} to check.
     * @return True if the {@link org.eclipselabs.garbagecat.util.jdk.Analysis} exists, false otherwise.
     */
    public boolean hasAnalysis(Analysis key) {
        return analysis.contains(key);
    }

    /**
     * @param key
     *            The {@link org.github.joa.util.Analysis} to check.
     * @return True if the {@link org.github.joa.util.Analysis} exists, false otherwise.
     */
    public boolean hasAnalysis(org.github.joa.util.Analysis key) {
        boolean hasAnalysis = false;
        if (jvmOptions != null && jvmOptions.hasAnalysis(key)) {
            hasAnalysis = true;
        }
        return hasAnalysis;
    }

    /**
     * @return true if there is data, false otherwise (e.g. no logging lines recognized).
     */
    public boolean haveData() {
        return getEventTypes().size() >= 2 || !getEventTypes().contains(LogEventType.UNKNOWN);
    }

    public boolean isPreprocessed() {
        return preprocessed;
    }

    public void setAllocationRate(BigDecimal mbPerSecond) {
        allocationRate = mbPerSecond;
    }

    public void setAnalysis(List<Analysis> analysis) {
        this.analysis = analysis;
    }

    public void setBlockingEventCount(int blockingEventCount) {
        this.blockingEventCount = blockingEventCount;
    }

    public void setDurationGtRealCount(long durationGtRealCount) {
        this.durationGtRealCount = durationGtRealCount;
    }

    public void setEventTypes(List<LogEventType> eventTypes) {
        this.eventTypes = eventTypes;
    }

    public void setExtRootScanningTimeMax(long extRootScanningTimeMax) {
        this.extRootScanningTimeMax = extRootScanningTimeMax;
    }

    public void setExtRootScanningTimeTotal(long extRootScanningTimeTotal) {
        this.extRootScanningTimeTotal = extRootScanningTimeTotal;
    }

    public void setFirstGcEvent(BlockingEvent firstGcEvent) {
        this.firstGcEvent = firstGcEvent;
    }

    public void setFirstSafepointEvent(SafepointEvent firstSafepointEvent) {
        this.firstSafepointEvent = firstSafepointEvent;
    }

    public void setGcBottlenecks(List<String> gcBottlenecks) {
        this.gcBottlenecks = gcBottlenecks;
    }

    public void setGcPauseMax(long gcPauseMax) {
        this.durationMax = gcPauseMax;
    }

    public void setGcPauseTotal(long gcPauseTotal) {
        this.durationTotal = gcPauseTotal;
    }

    public void setInvertedParallelismCount(long invertedParallelismCount) {
        this.invertedParallelismCount = invertedParallelismCount;
    }

    public void setInvertedSerialismCount(long invertedSerialismCount) {
        this.invertedSerialismCount = invertedSerialismCount;
    }

    public void setJdkVersion(String jdkVersion) {
        this.jdkVersion = jdkVersion;
    }

    public void setJvmOptions(JvmOptions jvmOptions) {
        this.jvmOptions = jvmOptions;
    }

    public void setLastGcEvent(BlockingEvent lastGcEvent) {
        this.lastGcEvent = lastGcEvent;
    }

    public void setLastLogLineUnprocessed(String lastLogLineUnprocessed) {
        this.lastLogLineUnprocessed = lastLogLineUnprocessed;
    }

    public void setLastSafepointEvent(SafepointEvent lastSafepointEvent) {
        this.lastSafepointEvent = lastSafepointEvent;
    }

    public void setMaxHeapAfterGc(Memory maxHeapAfterGc) {
        this.maxHeapAfterGc = maxHeapAfterGc;
    }

    public void setMaxHeapOccupancy(Memory maxHeapOccupancy) {
        this.maxHeapOccupancy = maxHeapOccupancy;
    }

    public void setMaxHeapOccupancyNonBlocking(Memory maxHeapOccupancyNonBlocking) {
        this.maxHeapOccupancyNonBlocking = maxHeapOccupancyNonBlocking;
    }

    public void setMaxHeapSpace(Memory maxHeapSpace) {
        this.maxHeapSpace = maxHeapSpace;
    }

    public void setMaxHeapSpaceNonBlocking(Memory maxHeapSpaceNonBlocking) {
        this.maxHeapSpaceNonBlocking = maxHeapSpaceNonBlocking;
    }

    public void setMaxOldSpace(Memory maxOldSpace) {
        this.maxOldSpace = maxOldSpace;
    }

    public void setMaxPermAfterGc(Memory maxPermAfterGc) {
        this.maxPermAfterGc = maxPermAfterGc;
    }

    public void setMaxPermOccupancy(Memory maxPermOccupancy) {
        this.maxPermOccupancy = maxPermOccupancy;
    }

    public void setMaxPermOccupancyNonBlocking(Memory maxPermOccupancyNonBlocking) {
        this.maxPermOccupancyNonBlocking = maxPermOccupancyNonBlocking;
    }

    public void setMaxPermSpace(Memory maxPermSpace) {
        this.maxPermSpace = maxPermSpace;
    }

    public void setMaxPermSpaceNonBlocking(Memory maxPermSpaceNonBlocking) {
        this.maxPermSpaceNonBlocking = maxPermSpaceNonBlocking;
    }

    public void setMaxYoungSpace(Memory maxYoungSpace) {
        this.maxYoungSpace = maxYoungSpace;
    }

    /**
     * @param memory
     *            The JVM memory information to set.
     */
    public void setMemory(String memory) {
        this.memory = memory;
    }

    public void setParallelCount(long parallelCount) {
        this.parallelCount = parallelCount;
    }

    public void setPhysicalMemory(Memory physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    public void setPhysicalMemoryFree(Memory physicalMemoryFree) {
        this.physicalMemoryFree = physicalMemoryFree;
    }

    public void setPreprocessed(boolean preprocessed) {
        this.preprocessed = preprocessed;
    }

    public void setSafepointBottlenecks(List<String> safepointBottlenecks) {
        this.safepointBottlenecks = safepointBottlenecks;
    }

    public void setSafepointEventSummaries(List<SafepointEventSummary> safepointEventSummaries) {
        this.safepointEventSummaries = safepointEventSummaries;
    }

    public void setSerialCount(long serialCount) {
        this.serialCount = serialCount;
    }

    public void setStoppedTimeEventCount(int stoppedTimeEventCount) {
        this.stoppedTimeEventCount = stoppedTimeEventCount;
    }

    public void setStoppedTimeMax(long stoppedTimeMax) {
        this.stoppedTimeMax = stoppedTimeMax;
    }

    public void setStoppedTimeTotal(long stoppedTimeTotal) {
        this.stoppedTimeTotal = stoppedTimeTotal;
    }

    public void setSwap(Memory swap) {
        this.swap = swap;
    }

    public void setSwapFree(Memory swapFree) {
        this.swapFree = swapFree;
    }

    public void setSysGtUserCount(long sysGtUserCount) {
        this.sysGtUserCount = sysGtUserCount;
    }

    public void setThroughputThreshold(int throughputThreshold) {
        this.throughputThreshold = throughputThreshold;
    }

    public void setUnidentifiedLogLines(List<String> unidentifiedLogLines) {
        this.unidentifiedLogLines = unidentifiedLogLines;
    }

    public void setUnifiedSafepointEventCount(int unifiedSafepointEventCount) {
        this.unifiedSafepointEventCount = unifiedSafepointEventCount;
    }

    public void setUnifiedSafepointTimeMax(long unifiedSafepointTimeMax) {
        this.unifiedSafepointTimeMax = unifiedSafepointTimeMax;
    }

    public void setUnifiedSafepointTimeTotal(long unifiedSafepointTimeTotal) {
        this.unifiedSafepointTimeTotal = unifiedSafepointTimeTotal;
    }

    public void setWorstDurationGtRealTimeEvent(LogEvent worstDurationGtRealTimeEvent) {
        this.worstDurationGtRealTimeEvent = worstDurationGtRealTimeEvent;
    }

    public void setWorstInvertedParallelismEvent(LogEvent worstInvertedParallelismEvent) {
        this.worstInvertedParallelismEvent = worstInvertedParallelismEvent;
    }

    public void setWorstInvertedSerialismEvent(LogEvent worstInvertedSerialismEvent) {
        this.worstInvertedSerialismEvent = worstInvertedSerialismEvent;
    }

    public void setWorstSysGtUserEvent(LogEvent worstSysGtUserEvent) {
        this.worstSysGtUserEvent = worstSysGtUserEvent;
    }
}
