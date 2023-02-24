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
package org.eclipselabs.garbagecat.service;

import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.Unit.BYTES;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;

import org.eclipselabs.garbagecat.Main;
import org.eclipselabs.garbagecat.dao.JvmDao;
import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.JvmRun;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.OtherTime;
import org.eclipselabs.garbagecat.domain.ParallelEvent;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.SafepointEvent;
import org.eclipselabs.garbagecat.domain.SerialCollection;
import org.eclipselabs.garbagecat.domain.ThrowAwayEvent;
import org.eclipselabs.garbagecat.domain.TimeWarpException;
import org.eclipselabs.garbagecat.domain.TimesData;
import org.eclipselabs.garbagecat.domain.TriggerData;
import org.eclipselabs.garbagecat.domain.UnknownEvent;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsIncrementalModeCollector;
import org.eclipselabs.garbagecat.domain.jdk.CmsInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsRemarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.CmsSerialOldEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1Collector;
import org.eclipselabs.garbagecat.domain.jdk.G1ExtRootScanningData;
import org.eclipselabs.garbagecat.domain.jdk.G1FullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungInitialMarkEvent;
import org.eclipselabs.garbagecat.domain.jdk.G1YoungPauseEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcLockerScavengeFailedEvent;
import org.eclipselabs.garbagecat.domain.jdk.GcOverheadLimitEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderCommandLineFlagsEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderMemoryEvent;
import org.eclipselabs.garbagecat.domain.jdk.HeaderVersionEvent;
import org.eclipselabs.garbagecat.domain.jdk.LogFileEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahConcurrentEvent;
import org.eclipselabs.garbagecat.domain.jdk.ShenandoahFullGcEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.UnifiedSafepointEvent;
import org.eclipselabs.garbagecat.domain.jdk.unified.VmWarningEvent;
import org.eclipselabs.garbagecat.preprocess.PreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ApplicationStoppedTimePreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.CmsPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.G1PreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ParallelPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.SerialPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.ShenandoahPreprocessAction;
import org.eclipselabs.garbagecat.preprocess.jdk.unified.UnifiedPreprocessAction;
import org.eclipselabs.garbagecat.util.Constants;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.GcTrigger;
import org.eclipselabs.garbagecat.util.jdk.JdkMath;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;
import org.github.joa.JvmOptions;
import org.github.joa.domain.Bit;
import org.github.joa.domain.GarbageCollector;

/**
 * <p>
 * Provides garbage collection analysis services to other layers.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * @author jborelo
 * 
 */
public class GcManager {

    private static boolean greater(Memory memory, int value) {
        return memory != null && memory.getValue(KILOBYTES) > value;
    }

    /**
     * The JVM data access object.
     */
    private JvmDao jvmDao;

    /**
     * The date and time the JVM was started.
     */
    private Date jvmStartDate;

    /**
     * Last log line unprocessed.
     */
    private String lastLogLineUnprocessed;

    /**
     * Whether or not the JVM events are from a preprocessed file.
     */
    private boolean preprocessed;

    /**
     * Default constructor.
     */
    public GcManager() {
        this(null);
    }

    /**
     * Alternate constructor.
     * 
     * @param jvmStartDate
     *            The JVM start date.
     */
    public GcManager(Date jvmStartDate) {
        this.jvmDao = new JvmDao();
        this.jvmStartDate = jvmStartDate;
    }

    /**
     * Allocation rate in KB per second.
     */
    private BigDecimal getAllocationRate() {
        List<BlockingEvent> blockingEvents = jvmDao.getBlockingEvents(LogEventType.G1_YOUNG_PAUSE);

        if (blockingEvents.isEmpty())
            return BigDecimal.ZERO;

        long allocatedKb = 0;
        G1YoungPauseEvent prior = null;
        long firstEventTs = 0;
        for (BlockingEvent event : blockingEvents) {
            G1YoungPauseEvent young = (G1YoungPauseEvent) event;
            if (prior == null) {
                // skip the first event since we don't know if this is a complete JVM run
                // and therefore can't accurately calculate allocation rate prior to the first log
                // youngGc pause event
                prior = young;
                firstEventTs = prior.getTimestamp();
                continue;
            }
            // will not have eden information if gc details not being logged
            if (young.getEdenOccupancyInit() != null && prior.getEdenOccupancyEnd() != null) {
                allocatedKb += young.getEdenOccupancyInit().minus(prior.getEdenOccupancyEnd()).getValue(KILOBYTES);
            }
            prior = young;
        }

        BigDecimal durationMs = BigDecimal.valueOf(prior.getTimestamp() - firstEventTs);
        if (durationMs.longValue() <= 0)
            return BigDecimal.ZERO;

        Memory allocated = Memory.kilobytes(allocatedKb);

        BigDecimal kilobytesPerSec = BigDecimal.valueOf(allocated.getValue(KILOBYTES) / durationMs.longValue());

        return kilobytesPerSec.multiply(BigDecimal.valueOf(1000));
    }

    /**
     * Determine <code>BlockingEvent</code>s where throughput since last event does not meet the throughput goal.
     * 
     * @param throughputThreshold
     *            The bottleneck reporting throughput threshold.
     * @return A <code>List</code> of <code>BlockingEvent</code>s where the throughput between events is less than the
     *         throughput threshold goal.
     */
    private List<String> getGcBottlenecks(int throughputThreshold) {
        List<String> bottlenecks = new ArrayList<String>();
        List<BlockingEvent> blockingEvents = jvmDao.getBlockingEvents();
        BlockingEvent priorEvent = null;
        for (BlockingEvent event : blockingEvents) {
            if (priorEvent != null && JdkUtil.isBottleneck(event, priorEvent, throughputThreshold)) {
                if (bottlenecks.isEmpty()) {
                    // Add current and prior event
                    if (jvmStartDate != null) {
                        // Convert uptime to datetime
                        bottlenecks.add(
                                JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(), jvmStartDate));
                        bottlenecks
                                .add(JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(), jvmStartDate));
                    } else {
                        bottlenecks.add(priorEvent.getLogEntry());
                        bottlenecks.add(event.getLogEntry());
                    }
                } else {
                    if (jvmStartDate != null) {
                        // Compare datetime, since bottleneck has datetime
                        if (!JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(), jvmStartDate)
                                .equals(bottlenecks.get(bottlenecks.size() - 1))) {
                            bottlenecks.add("...");
                            bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(),
                                    jvmStartDate));
                            bottlenecks.add(
                                    JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(), jvmStartDate));
                        } else {
                            bottlenecks.add(
                                    JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(), jvmStartDate));
                        }
                    } else {
                        // Compare timestamps, since bottleneck has timestamp
                        if (!priorEvent.getLogEntry().equals(bottlenecks.get(bottlenecks.size() - 1))) {
                            bottlenecks.add("...");
                            bottlenecks.add(priorEvent.getLogEntry());
                            bottlenecks.add(event.getLogEntry());
                        } else {
                            bottlenecks.add(event.getLogEntry());
                        }
                    }
                }
            }
            priorEvent = event;
        }
        return bottlenecks;
    }

    /**
     * Get JVM run data.
     * 
     * @param jvmOptions
     *            The JVM options passed in on the command line. Is is assumed command line options are more definitive
     *            than options found in <code>HeaderCommandLineFlagsEvent</code>, which is only summary of some options
     *            (e.g. it does not include log file name, rotation details, etc.).
     * @param throughputThreshold
     *            The throughput threshold for bottleneck reporting.
     * @return The JVM run data.
     */
    public JvmRun getJvmRun(String jvmOptions, int throughputThreshold) {
        JvmRun jvmRun = new JvmRun(throughputThreshold, jvmStartDate);
        // Use jvm options passed in on the command line if none found in the logging
        // TODO: jvm options passed on the command line should override options found in the logging header because the
        // logging header doesn't include every option (e.g. log file, rotation).
        if (jvmOptions != null && jvmDao.getJvmContext().getOptions() == null) {
            jvmDao.getJvmContext().setOptions(jvmOptions);
        }
        jvmRun.setJvmOptions(new JvmOptions(jvmDao.getJvmContext()));

        jvmRun.setAllocationRate(getAllocationRate());
        jvmRun.setAnalysis(jvmDao.getAnalysis());
        jvmRun.setBlockingEventCount(jvmDao.getBlockingEventCount());
        jvmRun.setEventTypes(jvmDao.getEventTypes());
        jvmRun.setExtRootScanningTimeMax(jvmDao.getExtRootScanningTimeMax());
        jvmRun.setExtRootScanningTimeTotal(jvmDao.getExtRootScanningTimeTotal());
        jvmRun.setFirstGcEvent(jvmDao.getFirstGcEvent());
        jvmRun.setFirstSafepointEvent(jvmDao.getFirstSafepointEvent());
        jvmRun.setGcBottlenecks(getGcBottlenecks(throughputThreshold));
        jvmRun.setGcPauseMax(jvmDao.getDurationMax());
        jvmRun.setGcPauseTotal(jvmDao.getDurationTotal());
        jvmRun.setGcTriggers(jvmDao.getGcTriggers());
        jvmRun.setInvertedParallelismCount(jvmDao.getInvertedParallelismCount());
        jvmRun.setInvertedSerialismCount(jvmDao.getInvertedSerialismCount());
        jvmRun.setLogEndingUnidentified(jvmDao.isLogEndingUnidentified());
        jvmRun.setJdkVersion(jvmDao.getJdkVersion());
        jvmRun.setLastGcEvent(jvmDao.getLastGcEvent());
        jvmRun.setLastLogLineUnprocessed(lastLogLineUnprocessed);
        jvmRun.setLastSafepointEvent(jvmDao.getLastSafepointEvent());
        jvmRun.setLogFileDate(jvmDao.getLogFileDate());
        jvmRun.setMaxHeapAfterGc(kilobytes(jvmDao.getMaxHeapAfterGc()));
        jvmRun.setMaxHeapOccupancy(kilobytes(jvmDao.getMaxHeapOccupancy()));
        jvmRun.setMaxHeapOccupancyNonBlocking(kilobytes(jvmDao.getMaxHeapOccupancyNonBlocking()));
        jvmRun.setMaxHeapSpace(kilobytes(jvmDao.getMaxHeapSpace()));
        jvmRun.setMaxHeapSpaceNonBlocking(kilobytes(jvmDao.getMaxHeapSpaceNonBlocking()));
        jvmRun.setMaxOldSpace(kilobytes(jvmDao.getMaxOldSpace()));
        jvmRun.setMaxPermAfterGc(kilobytes(jvmDao.getMaxPermAfterGc()));
        jvmRun.setMaxPermOccupancy(kilobytes(jvmDao.getMaxPermOccupancy()));
        jvmRun.setMaxPermOccupancyNonBlocking(kilobytes(jvmDao.getMaxPermOccupancyNonBlocking()));
        jvmRun.setMaxPermSpace(kilobytes(jvmDao.getMaxPermSpace()));
        jvmRun.setMaxPermSpaceNonBlocking(kilobytes(jvmDao.getMaxPermSpaceNonBlocking()));
        jvmRun.setMaxYoungSpace(kilobytes(jvmDao.getMaxYoungSpace()));
        jvmRun.setMemory(jvmDao.getMemory());
        jvmRun.setOtherTimeMax(jvmDao.getOtherTimeMax());
        jvmRun.setOtherTimeTotal(jvmDao.getOtherTimeTotal());
        jvmRun.setParallelCount(jvmDao.getParallelCount());
        jvmRun.setPhysicalMemory(new Memory(jvmDao.getPhysicalMemory(), BYTES));
        jvmRun.setPhysicalMemoryFree(new Memory(jvmDao.getPhysicalMemoryFree(), BYTES));
        jvmRun.setSafepointBottlenecks(getSafepointBottlenecks(jvmStartDate, throughputThreshold));
        jvmRun.setSafepointEventSummaries(jvmDao.getSafepointEventSummaries());
        jvmRun.setSerialCount(jvmDao.getSerialCount());
        jvmRun.setStoppedTimeEventCount(jvmDao.getStoppedTimeEventCount());
        jvmRun.setStoppedTimeMax(jvmDao.getStoppedTimeMax());
        jvmRun.setStoppedTimeTotal(jvmDao.getStoppedTimeTotal());
        jvmRun.setSwap(new Memory(jvmDao.getSwap(), BYTES));
        jvmRun.setSwapFree(new Memory(jvmDao.getSwapFree(), BYTES));
        jvmRun.setSysGtUserCount(jvmDao.getSysGtUserCount());
        jvmRun.setUnidentifiedLogLines(jvmDao.getUnidentifiedLogLines());
        jvmRun.setUnifiedSafepointEventCount(jvmDao.getUnifiedSafepointEventCount());
        jvmRun.setUnifiedSafepointTimeMax(jvmDao.getUnifiedSafepointTimeMax());
        jvmRun.setUnifiedSafepointTimeTotal(jvmDao.getUnifiedSafepointTimeTotal());
        jvmRun.setWorstInvertedParallelismEvent(jvmDao.getWorstInvertedParallelismEvent());
        jvmRun.setWorstInvertedSerialismEvent(jvmDao.getWorstInvertedSerialismEvent());
        jvmRun.setWorstSysGtUserEvent(jvmDao.getWorstSysGtUserEvent());
        jvmRun.setPreprocessed(this.preprocessed);
        jvmRun.setPreprocessEvents(jvmDao.getPreprocessEvents());

        if (!jvmRun.hasDatestamps() && jvmStartDate == null && jvmRun.getLogFileDate() != null
                && jvmRun.getFirstEvent() != null && jvmRun.getFirstEvent().getLogEntry() != null) {
            // Approximate JVM start date: log file create date - first event timestamp
            jvmRun.setStartDate(
                    GcUtil.getDateMinusTimestamp(jvmRun.getLogFileDate(), jvmRun.getFirstEvent().getTimestamp()));
            jvmDao.getAnalysis().add(0, Analysis.WARN_DATESTAMP_APPROXIMATE);
            jvmRun.setAnalysis(jvmDao.getAnalysis());
        }

        jvmRun.doAnalysis();

        return jvmRun;
    }

    public String getLastLogLineUnprocessed() {
        return lastLogLineUnprocessed;
    }

    /**
     * Determine the preprocessed log entry.
     * 
     * @param currentLogLine
     *            The current log line.
     * @param priorLogLine
     *            The previous log line. Needed to prevent preprocessing overlap where preprocessors have common
     *            patterns that are treated in different ways (e.g. removing vs. keeping matches, line break at end vs.
     *            no line break, etc.).
     * @param nextLogLine
     *            The next log line. Needed to distinguish between truncated and split logging. A truncated log entry
     *            can look exactly the same as the initial line of split logging.
     * @param jvmStartDate
     *            The date and time the JVM was started.
     * @param entangledLogLines
     *            Log lines mixed in with other logging events. Used for de-tangling intermingled logging events that
     *            span multiple lines. It follows the convention that the previous entry determines if the current entry
     *            is added to the previous entry (it's part of a multi-line event) or a new entry (it's a single-line
     *            event.
     * @param context
     *            Information to make preprocessing decisions. For example, the context collector type accounts for
     *            common logging patterns across collector families (e.g. , 0.0209631 secs).
     * @return The preprocessed log line(s), or null if it will be thrown away. Multiple lines are delimited by a
     *         newline.
     */
    public String getPreprocessedLogEntry(String currentLogLine, String priorLogLine, String nextLogLine,
            Date jvmStartDate, List<String> entangledLogLines, Set<String> context) {

        String preprocessedLogLine = null;

        if (currentLogLine != null) {
            if (isThrowawayEvent(currentLogLine)) {
                LogEvent throwAwayEvent = JdkUtil.parseLogLine(currentLogLine);
                JdkUtil.LogEventType throwAwayEventType = JdkUtil.determineEventType(throwAwayEvent.getName());
                if (!jvmDao.getEventTypes().contains(throwAwayEventType)) {
                    jvmDao.getEventTypes().add(throwAwayEventType);
                }
                currentLogLine = null;
            } else if (!context.contains(SerialPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                    && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(ParallelPreprocessAction.TOKEN)
                    && ShenandoahPreprocessAction.match(currentLogLine)) {
                // ShenandoahPreprocessAction leverages UnifiedPreprocessAction
                ShenandoahPreprocessAction action = new ShenandoahPreprocessAction(priorLogLine, currentLogLine,
                        nextLogLine, entangledLogLines, context);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (!context.contains(SerialPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                    && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(ParallelPreprocessAction.TOKEN)
                    && UnifiedPreprocessAction.match(currentLogLine)) {
                // UnifiedPreprocessAction is used by ShenandoahPreprocessAction
                UnifiedPreprocessAction action = new UnifiedPreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                        entangledLogLines, context);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (!context.contains(SerialPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                    && !context.contains(G1PreprocessAction.TOKEN) && !context.contains(UnifiedPreprocessAction.TOKEN)
                    && ParallelPreprocessAction.match(currentLogLine)) {
                ParallelPreprocessAction action = new ParallelPreprocessAction(priorLogLine, currentLogLine,
                        nextLogLine, entangledLogLines, context);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (!context.contains(SerialPreprocessAction.TOKEN)
                    && !context.contains(ParallelPreprocessAction.TOKEN) && !context.contains(G1PreprocessAction.TOKEN)
                    && !context.contains(ShenandoahPreprocessAction.TOKEN)
                    && !context.contains(UnifiedPreprocessAction.TOKEN)
                    && CmsPreprocessAction.match(currentLogLine, priorLogLine, nextLogLine)) {
                CmsPreprocessAction action = new CmsPreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                        entangledLogLines, context);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (!context.contains(SerialPreprocessAction.TOKEN)
                    && !context.contains(ParallelPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                    && !context.contains(ShenandoahPreprocessAction.TOKEN)
                    && !context.contains(UnifiedPreprocessAction.TOKEN)
                    && G1PreprocessAction.match(currentLogLine, priorLogLine, nextLogLine)) {
                G1PreprocessAction action = new G1PreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                        entangledLogLines, context, jvmDao.getPreprocessEvents());
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (!context.contains(ParallelPreprocessAction.TOKEN) && !context.contains(CmsPreprocessAction.TOKEN)
                    && !context.contains(G1PreprocessAction.TOKEN)
                    && !context.contains(ShenandoahPreprocessAction.TOKEN)
                    && !context.contains(UnifiedPreprocessAction.TOKEN)
                    && SerialPreprocessAction.match(currentLogLine)) {
                SerialPreprocessAction action = new SerialPreprocessAction(priorLogLine, currentLogLine, nextLogLine,
                        entangledLogLines, context);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else if (ApplicationStoppedTimePreprocessAction.match(currentLogLine)) {
                // single line preprocessing
                ApplicationStoppedTimePreprocessAction action = new ApplicationStoppedTimePreprocessAction(priorLogLine,
                        currentLogLine, nextLogLine, entangledLogLines, context);
                if (action.getLogEntry() != null) {
                    preprocessedLogLine = action.getLogEntry();
                }
            } else {
                // Output any entangled log lines
                if (entangledLogLines != null && !entangledLogLines.isEmpty()) {
                    for (String logLine : entangledLogLines) {
                        if (preprocessedLogLine == null) {
                            preprocessedLogLine = logLine;
                        } else {
                            preprocessedLogLine = preprocessedLogLine + Constants.LINE_SEPARATOR + logLine;
                        }
                    }
                    // Reset entangled log lines
                    entangledLogLines.clear();
                }
                if (preprocessedLogLine == null) {
                    preprocessedLogLine = currentLogLine;
                } else {
                    preprocessedLogLine = preprocessedLogLine + Constants.LINE_SEPARATOR + currentLogLine;
                }
                context.add(PreprocessAction.TOKEN_BEGINNING_OF_EVENT);
            }
        }
        return preprocessedLogLine;
    }

    /**
     * Determine <code>SafepointEvent</code>s where throughput since last event does not meet the throughput goal.
     * 
     * @param jvmStartDate
     *            The JVM start date.
     * @param throughputThreshold
     *            The bottleneck reporting throughput threshold.
     * @return A <code>List</code> of <code>SafepointEvent</code>s where the throughput between events is less than the
     *         throughput threshold goal.
     */
    private List<String> getSafepointBottlenecks(Date jvmStartDate, int throughputThreshold) {
        List<String> bottlenecks = new ArrayList<String>();
        List<SafepointEvent> safepointEvents = jvmDao.getSafepointEvents();
        SafepointEvent priorEvent = null;
        for (SafepointEvent event : safepointEvents) {
            if (priorEvent != null && JdkUtil.isBottleneck(event, priorEvent, throughputThreshold)) {
                if (bottlenecks.isEmpty()) {
                    // Add current and prior event
                    if (jvmStartDate != null) {
                        // Convert timestamps to date/time
                        bottlenecks.add(
                                JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(), jvmStartDate));
                        bottlenecks
                                .add(JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(), jvmStartDate));
                    } else {
                        bottlenecks.add(priorEvent.getLogEntry());
                        bottlenecks.add(event.getLogEntry());
                    }
                } else {
                    if (jvmStartDate != null) {
                        // Compare datetime, since bottleneck has datetime
                        if (!JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(), jvmStartDate)
                                .equals(bottlenecks.get(bottlenecks.size() - 1))) {
                            bottlenecks.add("...");
                            bottlenecks.add(JdkUtil.convertLogEntryTimestampsToDateStamp(priorEvent.getLogEntry(),
                                    jvmStartDate));
                            bottlenecks.add(
                                    JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(), jvmStartDate));
                        } else {
                            bottlenecks.add(
                                    JdkUtil.convertLogEntryTimestampsToDateStamp(event.getLogEntry(), jvmStartDate));
                        }
                    } else {
                        // Compare timestamps, since bottleneck has timestamp
                        if (!priorEvent.getLogEntry().equals(bottlenecks.get(bottlenecks.size() - 1))) {
                            bottlenecks.add("...");
                            bottlenecks.add(priorEvent.getLogEntry());
                            bottlenecks.add(event.getLogEntry());
                        } else {
                            bottlenecks.add(event.getLogEntry());
                        }
                    }
                }
            }
            priorEvent = event;
        }
        return bottlenecks;
    }

    public boolean isPreprocessed() {
        return preprocessed;
    }

    /**
     * Determine whether or not the logging line is essential for GC analysis.
     * 
     * @param logLine
     *            The log line to test.
     * @return True if the logging event can be thrown away, false if it should be kept.
     */
    private boolean isThrowawayEvent(String logLine) {
        return JdkUtil.parseLogLine(logLine) instanceof ThrowAwayEvent;
    }

    /**
     * Preprocess. Remove extraneous information and format for parsing.
     * 
     * @param logLines
     *            Raw garbage collection logging.
     * @param jvmStartDate
     *            The date and time the JVM was started.
     * @return Preprocessed garbage collection logging.
     */
    public List<String> preprocess(List<String> logLines, Date jvmStartDate) {
        if (logLines == null)
            throw new IllegalArgumentException("logLines == null!!");

        List<String> preprocessedLogList = new ArrayList<String>();

        if (!logLines.isEmpty()) {

            // Used for de-tangling intermingled logging events that span multiple lines
            List<String> entangledLogLines = new ArrayList<String>();
            // Used to provide context for preprocessing decisions
            Set<String> context = new HashSet<String>();

            String priorLogEntry = Constants.LINE_SEPARATOR;

            Iterator<String> iterator = logLines.iterator();

            String currentLogLine = iterator.next();
            String priorLogLine = "";
            String preprocessedLogLine = "";
            String nextLogLine = null;

            if (iterator.hasNext()) {
                nextLogLine = iterator.next();
            }

            while (nextLogLine != null) {
                preprocessedLogLine = getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine, jvmStartDate,
                        entangledLogLines, context);
                if (preprocessedLogLine != null) {
                    String[] preprocessedLogLines = preprocessedLogLine.split(Constants.LINE_SEPARATOR);
                    if (context.contains(PreprocessAction.TOKEN_BEGINNING_OF_EVENT)
                            && !priorLogEntry.endsWith(Constants.LINE_SEPARATOR)) {
                        for (int i = 0; i < preprocessedLogLines.length; i++) {
                            if (preprocessedLogLines[i] != "") {
                                preprocessedLogList.add(preprocessedLogLines[i]);
                            }
                        }
                    } else {
                        if (preprocessedLogList.isEmpty()) {
                            preprocessedLogList.add(preprocessedLogLine);

                        } else {
                            if (!priorLogEntry.endsWith(Constants.LINE_SEPARATOR)) {
                                String lastPreprocessedLogEntry = preprocessedLogList
                                        .get(preprocessedLogList.size() - 1);
                                preprocessedLogList.remove(preprocessedLogList.size() - 1);
                                preprocessedLogList.add(lastPreprocessedLogEntry + preprocessedLogLines[0]);
                                if (preprocessedLogLines.length > 1) {
                                    for (int i = 1; i < preprocessedLogLines.length; i++) {
                                        if (preprocessedLogLines[i] != "") {
                                            preprocessedLogList.add(preprocessedLogLines[i]);
                                        }
                                    }
                                }
                            } else {
                                if (preprocessedLogLines.length > 1) {
                                    for (int i = 0; i < preprocessedLogLines.length; i++) {
                                        if (preprocessedLogLines[i] != "") {
                                            preprocessedLogList.add(preprocessedLogLines[i]);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    priorLogEntry = preprocessedLogLine;
                }

                priorLogLine = currentLogLine;
                currentLogLine = nextLogLine;
                if (iterator.hasNext()) {
                    nextLogLine = iterator.next();
                } else {
                    nextLogLine = null;
                }
            }

            // Process last line
            lastLogLineUnprocessed = currentLogLine;
            preprocessedLogLine = getPreprocessedLogEntry(currentLogLine, priorLogLine, nextLogLine, jvmStartDate,
                    entangledLogLines, context);
            if (preprocessedLogLine != null) {
                String[] preprocessedLogLines = preprocessedLogLine.split(Constants.LINE_SEPARATOR);
                if (context.contains(PreprocessAction.TOKEN_BEGINNING_OF_EVENT)) {
                    // Output on new line
                    for (int i = 0; i < preprocessedLogLines.length; i++) {
                        if (preprocessedLogLines[i] != "") {
                            preprocessedLogList.add(preprocessedLogLines[i]);
                        }
                    }
                } else {
                    if (preprocessedLogList.isEmpty()) {
                        preprocessedLogList.add(preprocessedLogLine);
                    } else {
                        // Add to prior line if prior line does not end with LINE_SEPARATOR
                        String lastPreprocessedLogEntry = preprocessedLogList.get(preprocessedLogList.size() - 1);
                        if (!lastPreprocessedLogEntry.endsWith(Constants.LINE_SEPARATOR)) {
                            preprocessedLogList.remove(preprocessedLogList.size() - 1);
                            preprocessedLogList.add(lastPreprocessedLogEntry + preprocessedLogLines[0]);
                            if (preprocessedLogLines.length > 1) {
                                for (int i = 1; i < preprocessedLogLines.length; i++) {
                                    if (preprocessedLogLines[i] != "") {
                                        preprocessedLogList.add(preprocessedLogLines[i]);
                                    }
                                }
                            }
                        } else {
                            if (preprocessedLogLines.length > 1) {
                                for (int i = 0; i < preprocessedLogLines.length; i++) {
                                    if (preprocessedLogLines[i] != "") {
                                        preprocessedLogList.add(preprocessedLogLines[i]);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // output entangled log lines
            if (!entangledLogLines.isEmpty()) {
                for (String logLine : entangledLogLines) {
                    preprocessedLogList.add(logLine);
                }
                // Reset entangled log lines
                entangledLogLines.clear();
            }

            preprocessed = true;
        }

        return preprocessedLogList;
    }

    /**
     * Parse the garbage collection logging for the JVM run and store the data in the data store.
     * 
     * @param logLines
     *            The garbage collection loggine.
     * @param reorder
     *            Whether or not to allow logging to be reordered by timestamp.
     */
    public void store(List<String> logLines, boolean reorder) {

        if (logLines == null || logLines.isEmpty()) {
            return;
        }

        String logLine = null;
        BlockingEvent priorEvent = null;

        Iterator<String> iterator = logLines.iterator();
        while (iterator.hasNext()) {
            logLine = iterator.next();
            // If event has no timestamp, use most recent blocking timestamp.
            LogEvent event = JdkUtil.parseLogLine(logLine);
            if (event instanceof BlockingEvent) {
                jvmDao.setLogEndingUnidentified(false);

                // Verify logging in correct order. If overridden, logging will be stored and reordered by timestamp
                // for analysis.
                if (!reorder && priorEvent != null && event.getTimestamp() < priorEvent.getTimestamp()) {
                    throw new TimeWarpException("Logging reversed: " + Constants.LINE_SEPARATOR
                            + priorEvent.getLogEntry() + Constants.LINE_SEPARATOR + event.getLogEntry());
                }

                jvmDao.addBlockingEvent((BlockingEvent) event);

                // Analysis

                // 1) Explicit GC
                if (event instanceof TriggerData) {
                    GcTrigger trigger = ((TriggerData) event).getTrigger();
                    if (trigger == GcTrigger.SYSTEM_GC) {
                        GarbageCollector garbageCollector = ((GcEvent) event).getGarbageCollector();
                        switch (garbageCollector) {
                        case G1:
                            if (!jvmDao.getAnalysis().contains(Analysis.ERROR_EXPLICIT_GC_SERIAL_G1)
                                    && event instanceof G1FullGcEvent) {
                                jvmDao.addAnalysis(Analysis.ERROR_EXPLICIT_GC_SERIAL_G1);
                            } else if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_G1_YOUNG_INITIAL_MARK)
                                    && event instanceof G1YoungInitialMarkEvent) {
                                jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_G1_YOUNG_INITIAL_MARK);
                            }
                            break;
                        case PARALLEL_OLD:
                            if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_PARALLEL)) {
                                jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_PARALLEL);
                            }
                            break;
                        case PARALLEL_SERIAL_OLD:
                            if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL)) {
                                jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_SERIAL_PARALLEL);
                            }
                            break;
                        case SERIAL_NEW:
                            if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_SERIAL)) {
                                jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_SERIAL);
                            }
                            break;
                        case SERIAL_OLD:
                            if (!jvmDao.getAnalysis().contains(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS)
                                    && event instanceof CmsSerialOldEvent) {
                                jvmDao.addAnalysis(Analysis.ERROR_EXPLICIT_GC_SERIAL_CMS);
                            }
                            break;
                        case SHENANDOAH:
                            break;
                        case UNKNOWN:
                            if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_UNKNOWN)) {
                                jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_UNKNOWN);
                            }
                            break;
                        default:
                            break;
                        }
                    }
                }

                // 2) Serial collections not caused by explicit GC
                if (event instanceof SerialCollection) {
                    GcTrigger trigger = null;
                    if (event instanceof TriggerData) {
                        trigger = ((TriggerData) event).getTrigger();
                    }
                    if (trigger == null || !(trigger == GcTrigger.SYSTEM_GC || trigger == GcTrigger.CLASS_HISTOGRAM
                            || trigger == GcTrigger.HEAP_INSPECTION_INITIATED_GC
                            || trigger == GcTrigger.HEAP_DUMP_INITIATED_GC)) {
                        JdkUtil.LogEventType eventType = JdkUtil.determineEventType(event.getName());
                        switch (eventType) {
                        case G1_FULL_GC_SERIAL:
                            if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_G1)) {
                                jvmDao.addAnalysis(Analysis.ERROR_SERIAL_GC_G1);
                            }
                            break;
                        case CMS_SERIAL_OLD:
                            if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_CMS)) {
                                jvmDao.addAnalysis(Analysis.ERROR_SERIAL_GC_CMS);
                            }
                            break;
                        case PARALLEL_SERIAL_OLD:
                            if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SERIAL_GC_PARALLEL)) {
                                jvmDao.addAnalysis(Analysis.ERROR_SERIAL_GC_PARALLEL);
                            }
                            break;
                        case SERIAL_OLD:
                            if (!jvmDao.getAnalysis().contains(Analysis.WARN_SERIAL_GC)) {
                                jvmDao.addAnalysis(Analysis.WARN_SERIAL_GC);
                            }
                            break;
                        case UNKNOWN:
                            break;
                        default:
                            break;
                        }
                    }
                }

                // 3) CMS concurrent mode failure
                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE)) {
                    if (event instanceof CmsSerialOldEvent) {
                        GcTrigger trigger = ((TriggerData) event).getTrigger();
                        if (trigger == GcTrigger.CONCURRENT_MODE_FAILURE) {
                            jvmDao.addAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_FAILURE);
                        }
                    }
                }

                // 4) CMS concurrent mode interrupted
                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED)) {
                    if (event instanceof CmsSerialOldEvent) {
                        GcTrigger trigger = ((TriggerData) event).getTrigger();
                        if (trigger == GcTrigger.CONCURRENT_MODE_INTERRUPTED) {
                            jvmDao.addAnalysis(Analysis.ERROR_CMS_CONCURRENT_MODE_INTERRUPTED);
                        }
                    }
                }

                // 5) CMS incremental mode
                if (!jvmDao.getAnalysis().contains(Analysis.WARN_CMS_INCREMENTAL_MODE)) {
                    if (event instanceof CmsIncrementalModeCollector) {
                        if (((CmsIncrementalModeCollector) event).isIncrementalMode()) {
                            jvmDao.addAnalysis(Analysis.WARN_CMS_INCREMENTAL_MODE);
                        }
                    }
                }

                // 6) Heap dump initiated gc
                if (!jvmDao.getAnalysis().contains(Analysis.WARN_HEAP_DUMP_INITIATED_GC)) {
                    if (event instanceof TriggerData) {
                        GcTrigger trigger = ((TriggerData) event).getTrigger();
                        if (trigger == GcTrigger.HEAP_DUMP_INITIATED_GC) {
                            jvmDao.addAnalysis(Analysis.WARN_HEAP_DUMP_INITIATED_GC);
                        }
                    }
                }

                // 7) Heap inspection initiated gc
                if (!jvmDao.getAnalysis().contains(Analysis.WARN_HEAP_INSPECTION_INITIATED_GC)) {
                    if (event instanceof TriggerData) {
                        GcTrigger trigger = ((TriggerData) event).getTrigger();
                        if (trigger == GcTrigger.HEAP_INSPECTION_INITIATED_GC) {
                            jvmDao.addAnalysis(Analysis.WARN_HEAP_INSPECTION_INITIATED_GC);
                        }
                    }
                }

                // 8) Metaspace allocation failure
                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_METASPACE_ALLOCATION_FAILURE)) {
                    if (event instanceof TriggerData) {
                        GcTrigger trigger = ((TriggerData) event).getTrigger();
                        if (trigger == GcTrigger.LAST_DITCH_COLLECTION) {
                            jvmDao.addAnalysis(Analysis.ERROR_METASPACE_ALLOCATION_FAILURE);
                        }
                    }
                }

                // 9) JV TI explicit gc
                if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_JVMTI)) {
                    if (event instanceof TriggerData) {
                        GcTrigger trigger = ((TriggerData) event).getTrigger();
                        if (trigger == GcTrigger.JVMTI_FORCED_GARBAGE_COLLECTION) {
                            jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_JVMTI);
                        }
                    }
                }

                // 10) G1 evacuation failure
                if (event instanceof TriggerData) {
                    GcTrigger trigger = ((TriggerData) event).getTrigger();
                    if ((trigger == GcTrigger.TO_SPACE_EXHAUSTED || trigger == GcTrigger.TO_SPACE_OVERFLOW)) {
                        if (!jvmDao.getAnalysis().contains(Analysis.ERROR_G1_EVACUATION_FAILURE)) {
                            jvmDao.addAnalysis(Analysis.ERROR_G1_EVACUATION_FAILURE);
                        }
                    }
                }

                // 11) CMS promotion failure
                if (event instanceof TriggerData) {
                    GcTrigger trigger = ((TriggerData) event).getTrigger();
                    if (trigger == GcTrigger.PROMOTION_FAILED) {
                        if (!jvmDao.getAnalysis().contains(Analysis.ERROR_CMS_PROMOTION_FAILED)
                                && event instanceof CmsSerialOldEvent) {
                            jvmDao.addAnalysis(Analysis.ERROR_CMS_PROMOTION_FAILED);
                        }
                    }
                }

                // 12) -XX:+PrintGCCause is essential for troubleshooting G1 full GCs
                if (event instanceof G1FullGcEvent) {
                    GcTrigger trigger = ((TriggerData) event).getTrigger();
                    if (trigger == GcTrigger.NONE) {
                        if (!jvmDao.getAnalysis().contains(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED)) {
                            jvmDao.addAnalysis(Analysis.WARN_PRINT_GC_CAUSE_NOT_ENABLED);
                        }
                    }
                }

                // 13) CMS_REMARK class unloading
                if (event instanceof CmsRemarkEvent && !((CmsRemarkEvent) event).isClassUnloading()
                        && !jvmDao.getAnalysis().contains(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED)) {
                    jvmDao.addAnalysis(Analysis.WARN_CMS_CLASS_UNLOADING_NOT_ENABLED);
                }

                // 14) Humongous allocation
                if (event instanceof G1Collector && event instanceof TriggerData
                        && !jvmDao.getAnalysis().contains(Analysis.INFO_G1_HUMONGOUS_ALLOCATION)) {
                    GcTrigger trigger = ((TriggerData) event).getTrigger();
                    if (trigger == GcTrigger.G1_HUMONGOUS_ALLOCATION) {
                        jvmDao.addAnalysis(Analysis.INFO_G1_HUMONGOUS_ALLOCATION);
                    }
                }

                // 15) Inverted parallelism
                if (event instanceof ParallelEvent && event instanceof TimesData) {
                    if (((TimesData) event).getTimeUser() != TimesData.NO_DATA
                            && ((TimesData) event).getTimeSys() != TimesData.NO_DATA
                            && ((TimesData) event).getTimeReal() != TimesData.NO_DATA) {
                        jvmDao.setParallelCount(jvmDao.getParallelCount() + 1);
                        if (((TimesData) event).getTimeUser() > 0
                                && JdkMath.isInvertedParallelism(((TimesData) event).getParallelism())) {
                            jvmDao.setInvertedParallelismCount(jvmDao.getInvertedParallelismCount() + 1);
                            if (jvmDao.getWorstInvertedParallelismEvent() == null) {
                                jvmDao.setWorstInvertedParallelismEvent(event);
                            } else {
                                if (((TimesData) event)
                                        .getParallelism() < ((TimesData) jvmDao.getWorstInvertedParallelismEvent())
                                                .getParallelism()) {
                                    // Update lowest "low"
                                    jvmDao.setWorstInvertedParallelismEvent(event);
                                }
                            }
                        }
                    }
                    // sys > user: ignore sys - user = .01 secs
                    if (((TimesData) event).getTimeSys() > 0 && ((TimesData) event).getTimeUser() > 0
                            && ((TimesData) event).getTimeSys() > ((TimesData) event).getTimeUser() + 1) {
                        jvmDao.setSysGtUserCount(jvmDao.getSysGtUserCount() + 1);
                        if (jvmDao.getWorstSysGtUserEvent() == null) {
                            jvmDao.setWorstSysGtUserEvent(event);
                        } else {
                            if ((((TimesData) event).getTimeSys() - ((TimesData) event)
                                    .getTimeUser()) > (((TimesData) jvmDao.getWorstSysGtUserEvent()).getTimeSys()
                                            - ((TimesData) jvmDao.getWorstSysGtUserEvent()).getTimeUser())) {
                                // Update greatest user - sys
                                jvmDao.setWorstSysGtUserEvent(event);
                            }
                        }
                    }
                }

                // 16) Check for CMS initial mark low parallelism
                if (event instanceof CmsInitialMarkEvent && ((TimesData) event).getTimeUser() > 0
                        && ((TimesData) event).getTimeReal() > 0 && ((BlockingEvent) event).getDuration() >= 10000
                        && JdkMath.isLowParallelism(((TimesData) event).getParallelism())) {
                    if (!jvmDao.getAnalysis().contains(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM)) {
                        jvmDao.addAnalysis(Analysis.WARN_CMS_INITIAL_MARK_LOW_PARALLELISM);
                    }
                }

                // 17) Check for CMS remark low parallelism
                if (event instanceof CmsRemarkEvent && ((TimesData) event).getTimeUser() > 0
                        && ((TimesData) event).getTimeReal() > 0 && ((BlockingEvent) event).getDuration() >= 10000
                        && JdkMath.isLowParallelism(((TimesData) event).getParallelism())) {
                    if (!jvmDao.getAnalysis().contains(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM)) {
                        jvmDao.addAnalysis(Analysis.WARN_CMS_REMARK_LOW_PARALLELISM);
                    }
                }

                // 18) Check for old JDKs using perm gen
                if (event instanceof PermMetaspaceData && event.getLogEntry() != null
                        && event.getLogEntry().matches("^.*Perm.*$")) {
                    if (!jvmDao.getAnalysis().contains(Analysis.INFO_PERM_GEN)) {
                        jvmDao.addAnalysis(Analysis.INFO_PERM_GEN);
                    }
                }

                // 19) Shenandoah Full GC
                if (event instanceof ShenandoahFullGcEvent) {
                    if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SHENANDOAH_FULL_GC)) {
                        jvmDao.addAnalysis(Analysis.ERROR_SHENANDOAH_FULL_GC);
                    }
                }

                // 20) Diagnostic explicit gc
                if (!jvmDao.getAnalysis().contains(Analysis.WARN_EXPLICIT_GC_DIAGNOSTIC)) {
                    if (event instanceof TriggerData) {
                        GcTrigger trigger = ((TriggerData) event).getTrigger();
                        if (trigger == GcTrigger.DIAGNOSTIC_COMMAND) {
                            jvmDao.addAnalysis(Analysis.WARN_EXPLICIT_GC_DIAGNOSTIC);
                        }
                    }
                }

                // 21) Inverted serialism
                if (event instanceof SerialCollection && event instanceof TimesData) {
                    if (((TimesData) event).getTimeUser() != TimesData.NO_DATA
                            && ((TimesData) event).getTimeSys() != TimesData.NO_DATA
                            && ((TimesData) event).getTimeReal() != TimesData.NO_DATA) {
                        jvmDao.setSerialCount(jvmDao.getSerialCount() + 1);
                        // Inverted serialism: Ignore real vs user + sys < .1 secs
                        if (((TimesData) event).getTimeUser() > 0
                                && JdkMath.isInvertedSerialism(((TimesData) event).getParallelism())
                                && (((TimesData) event).getTimeReal() - ((TimesData) event).getTimeUser()
                                        - ((TimesData) event).getTimeSys() > 10)) {
                            jvmDao.setInvertedSerialismCount(jvmDao.getInvertedSerialismCount() + 1);
                            if (jvmDao.getWorstInvertedSerialismEvent() == null) {
                                jvmDao.setWorstInvertedSerialismEvent(event);
                            } else {
                                if (((TimesData) event)
                                        .getParallelism() < ((TimesData) jvmDao.getWorstInvertedSerialismEvent())
                                                .getParallelism()) {
                                    // Update lowest "low"
                                    jvmDao.setWorstInvertedSerialismEvent(event);
                                }
                            }
                        }
                        // sys > user: ignore sys - user = .01 secs
                        if (((TimesData) event).getTimeSys() > 0 && ((TimesData) event).getTimeUser() > 0
                                && ((TimesData) event).getTimeSys() > ((TimesData) event).getTimeUser() + 1) {
                            jvmDao.setSysGtUserCount(jvmDao.getSysGtUserCount() + 1);
                            if (jvmDao.getWorstSysGtUserEvent() == null) {
                                jvmDao.setWorstSysGtUserEvent(event);
                            } else {
                                if ((((TimesData) event).getTimeSys() - ((TimesData) event)
                                        .getTimeUser()) > (((TimesData) jvmDao.getWorstSysGtUserEvent()).getTimeSys()
                                                - ((TimesData) jvmDao.getWorstSysGtUserEvent()).getTimeUser())) {
                                    // Update greatest user - sys
                                    jvmDao.setWorstSysGtUserEvent(event);
                                }
                            }
                        }
                    }
                }

                // 22) <code>G1ExtRootScanningData</code>
                if (event instanceof G1ExtRootScanningData
                        && ((G1ExtRootScanningData) event).getExtRootScanningTime() != G1ExtRootScanningData.NO_DATA) {
                    long extRootScanningTime = ((G1ExtRootScanningData) event).getExtRootScanningTime();
                    if (extRootScanningTime > 0) {
                        if (extRootScanningTime > jvmDao.getExtRootScanningTimeMax()) {
                            jvmDao.setExtRootScanningTimeMax(extRootScanningTime);
                        }
                        jvmDao.setExtRootScanningTimeTotal(jvmDao.getExtRootScanningTimeTotal() + extRootScanningTime);
                    }
                }

                // 23) "Other" time
                if (event instanceof OtherTime && ((OtherTime) event).getOtherTime() != OtherTime.NO_DATA) {
                    long otherTime = ((OtherTime) event).getOtherTime();
                    if (otherTime > 0) {
                        if (otherTime > jvmDao.getOtherTimeMax()) {
                            jvmDao.setOtherTimeMax(otherTime);
                        }
                        jvmDao.setOtherTimeTotal(jvmDao.getOtherTimeTotal() + otherTime);
                    }
                }

                priorEvent = (BlockingEvent) event;

            } else if (event instanceof ApplicationStoppedTimeEvent) {
                jvmDao.setLogEndingUnidentified(false);
                jvmDao.addStoppedTimeEvent((ApplicationStoppedTimeEvent) event);
            } else if (event instanceof UnifiedSafepointEvent) {
                jvmDao.setLogEndingUnidentified(false);
                jvmDao.addSafepointEvent((UnifiedSafepointEvent) event);
            } else if (event instanceof HeaderCommandLineFlagsEvent) {
                jvmDao.setLogEndingUnidentified(false);
                jvmDao.getJvmContext().setOptions(((HeaderCommandLineFlagsEvent) event).getJvmOptions());
            } else if (event instanceof HeaderMemoryEvent) {
                jvmDao.setLogEndingUnidentified(false);
                jvmDao.setMemory(((HeaderMemoryEvent) event).getLogEntry());
                jvmDao.setPhysicalMemory((long) KILOBYTES.toBytes(((HeaderMemoryEvent) event).getPhysicalMemory()));
                jvmDao.getJvmContext().setMemory(org.github.joa.util.JdkUtil.convertSize(jvmDao.getPhysicalMemory(),
                        'B', org.github.joa.util.Constants.UNITS));
                jvmDao.setPhysicalMemoryFree(
                        (long) KILOBYTES.toBytes(((HeaderMemoryEvent) event).getPhysicalMemoryFree()));
                jvmDao.setSwap((long) KILOBYTES.toBytes(((HeaderMemoryEvent) event).getSwap()));
                jvmDao.setSwapFree((long) KILOBYTES.toBytes(((HeaderMemoryEvent) event).getSwapFree()));
            } else if (event instanceof HeaderVersionEvent) {
                jvmDao.setLogEndingUnidentified(false);
                jvmDao.getJvmContext().setVersionMajor(((HeaderVersionEvent) event).getJdkVersionMajor());
                jvmDao.getJvmContext().setVersionMinor(((HeaderVersionEvent) event).getJdkVersionMinor());
                if (((HeaderVersionEvent) event).is32Bit()) {
                    jvmDao.getJvmContext().setBit(Bit.BIT32);
                }
                jvmDao.setJdkVersion(((HeaderVersionEvent) event).getLogEntry());
                jvmDao.getJvmContext().setOs(((HeaderVersionEvent) event).getOs());
            } else if (event instanceof LogFileEvent) {
                jvmDao.setLogEndingUnidentified(false);
                if (((LogFileEvent) event).isCreated()) {
                    Matcher matcher = LogFileEvent.pattern.matcher(((LogFileEvent) event).getLogEntry());
                    if (matcher.find()) {
                        jvmDao.setLogFileDate(GcUtil.parseDatetime(logLine));
                    }
                }
            } else if (event instanceof GcOverheadLimitEvent) {
                jvmDao.setLogEndingUnidentified(false);
                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED)) {
                    jvmDao.getAnalysis().add(Analysis.ERROR_GC_TIME_LIMIT_EXCEEEDED);
                }
            } else if (event instanceof GcLockerScavengeFailedEvent) {
                jvmDao.setLogEndingUnidentified(false);
                if (!jvmDao.getAnalysis().contains(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED)) {
                    jvmDao.addAnalysis(Analysis.ERROR_CMS_PAR_NEW_GC_LOCKER_FAILED);
                }
            } else if (event instanceof ShenandoahConcurrentEvent) {
                jvmDao.setLogEndingUnidentified(false);
                if (greater(((CombinedData) event).getCombinedOccupancyInit(),
                        jvmDao.getMaxHeapOccupancyNonBlocking())) {
                    jvmDao.setMaxHeapOccupancyNonBlocking(
                            (int) ((CombinedData) event).getCombinedOccupancyInit().getValue(KILOBYTES));
                }
                if (greater(((CombinedData) event).getCombinedSpace(), jvmDao.getMaxHeapSpaceNonBlocking())) {
                    jvmDao.setMaxHeapSpaceNonBlocking(
                            (int) ((CombinedData) event).getCombinedSpace().getValue(KILOBYTES));
                }
                if (greater(((PermMetaspaceData) event).getPermOccupancyInit(),
                        jvmDao.getMaxPermOccupancyNonBlocking())) {
                    jvmDao.setMaxPermOccupancyNonBlocking(
                            (int) ((PermMetaspaceData) event).getPermOccupancyInit().getValue(KILOBYTES));
                }
                if (greater(((PermMetaspaceData) event).getPermSpace(), jvmDao.getMaxPermSpaceNonBlocking())) {
                    jvmDao.setMaxPermSpaceNonBlocking(
                            (int) ((PermMetaspaceData) event).getPermSpace().getValue(KILOBYTES));
                }
            } else if (event instanceof VmWarningEvent) {
                jvmDao.setLogEndingUnidentified(false);
                if (((VmWarningEvent) event).getErrNo().equals("12")) {
                    if (!jvmDao.getAnalysis().contains(Analysis.ERROR_SHARED_MEMORY_12)) {
                        jvmDao.addAnalysis(Analysis.ERROR_SHARED_MEMORY_12);
                    }
                }
            } else if (event instanceof UnknownEvent) {
                jvmDao.setLogEndingUnidentified(true);
                if (jvmDao.getUnidentifiedLogLines().size() < Main.REJECT_LIMIT) {
                    jvmDao.getUnidentifiedLogLines().add(logLine);
                }
            }
            // Populate events list.
            JdkUtil.LogEventType eventType = JdkUtil.determineEventType(event.getName());
            if (!jvmDao.getEventTypes().contains(eventType)) {
                jvmDao.getEventTypes().add(eventType);
            }

            // Populate triggers list.
            if (event instanceof TriggerData) {
                if (!jvmDao.getGcTriggers().contains(((TriggerData) event).getTrigger())) {
                    jvmDao.getGcTriggers().add(((TriggerData) event).getTrigger());
                }
            }

            // Populate collector list.
            if (event instanceof GcEvent) {
                if (!jvmDao.getJvmContext().getGarbageCollectors().contains(((GcEvent) event).getGarbageCollector())) {
                    jvmDao.getJvmContext().getGarbageCollectors().add(((GcEvent) event).getGarbageCollector());
                }
            }
        }
    }
}
