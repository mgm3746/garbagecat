/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.Jvm;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

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
    private long firstTimestamp;

    /**
     * Time of the last blocking event, in milliseconds after JVM startup.
     */
    private long lastTimestamp;

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

    public long getFirstTimestamp() {
        return firstTimestamp;
    }

    public void setFirstTimestamp(long firstTimestamp) {
        this.firstTimestamp = firstTimestamp;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
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

    /*
     * Throughput based only on garbage collection as a percent rounded to the nearest integer. CG throughput is the
     * percent of time not spent doing GC. 0 means all time was spent doing GC. 100 means no time was spent doing GC.
     */
    public long getGcThroughput() {
        long gcThroughput;
        if (blockingEventCount > 0) {
            long timeTotal;
            if (lastTimestamp > firstTimestamp && firstTimestamp > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000) {
                // Partial log. Use the timestamp of the first GC event, not 0, in order to determine
                // throughput more accurately.
                timeTotal = lastTimestamp + new Long(lastGcDuration).longValue() - firstTimestamp;
            } else {
                // Complete log or a log with only 1 event.
                timeTotal = lastTimestamp + new Long(lastGcDuration).longValue();
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
            if (lastTimestamp > firstTimestamp && firstTimestamp > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000) {
                // Partial log. Use the timestamp of the first GC event, not 0, in order to determine
                // throughput more accurately.
                timeTotal = lastTimestamp + new Long(lastGcDuration).longValue() - firstTimestamp;
            } else {
                // Complete log or a log with only 1 event.
                timeTotal = lastTimestamp + new Long(lastGcDuration).longValue();
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
     * Ratio of GC to Stopped Time as a percent rounded to the nearest integer. 100 means all stopped time spent doing
     * GC. 0 means none of the stopped time was due to GC.
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
     * 
     * @return A <code>List</code> of analysis points based on the JVM options and data.
     */
    public void doAnalysis() {

        if (jvm.getOptions() != null) {
            doJvmOptionsAnalysis();
        }

        // 1) Check for partial log
        if (GcUtil.isPartialLog(firstTimestamp)) {
            analysisKeys.add(Analysis.KEY_FIRST_TIMESTAMP_THRESHOLD_EXCEEDED);
        }

        // 2) Check to see if -XX:+PrintGCApplicationStoppedTime enabled
        if (!eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME)) {
            analysisKeys.add(Analysis.KEY_APPLICATION_STOPPED_TIME_MISSING);
        }

        // 3) Check for significant stopped time unrelated to GC
        if (eventTypes.contains(LogEventType.APPLICATION_STOPPED_TIME) && getGcStoppedRatio() < 80) {
            analysisKeys.add(Analysis.KEY_GC_STOPPED_RATIO);
        }

        // 4) Check for throughput collector serial collection
        if (eventTypes.contains(LogEventType.PARALLEL_SERIAL_OLD)) {
            analysisKeys.add(Analysis.KEY_THROUGHPUT_SERIAL_GC);
        }

        // 5) Check for CMS collector serial collection
        if (eventTypes.contains(LogEventType.CMS_SERIAL_OLD)) {
            analysisKeys.add(Analysis.KEY_CMS_SERIAL_GC);
        }
    }

    /**
     * Do JVM options analysis.
     */
    private void doJvmOptionsAnalysis() {

        // Check to see if thread stack size explicitly set
        if (jvm.getThreadStackSizeOption() == null) {
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
        if (jvm.getMinPermOption() == null && jvm.getMaxPermOption() == null && jvm.getMinMetaspaceOption() == null
                && jvm.getMaxMetaspaceOption() == null) {
            analysisKeys.add(Analysis.KEY_PERM_METASPACE_NOT_SET);
        }

        // Check to see if explicit gc is disabled
        if (jvm.getDisableExplicitGCOption() != null) {
            analysisKeys.add(Analysis.KEY_EXPLICIT_GC_DISABLED);
        }

        // Check for large thread stack size
        if (jvm.hasLargeThreadStackSize()) {
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
        
        

        // TODO: Check to see if explicit GC interval is disabled or set.

        // TODO: If explicit GC interval is set, try disabling explicit GC.

        // TODO: Check for instrumentation.

        // TODO: -Xbatch warning
    }
}
