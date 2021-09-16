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
package org.eclipselabs.garbagecat.dao;

import static java.util.Collections.binarySearch;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.summingLong;
import static java.util.stream.Collectors.toList;
import static org.eclipselabs.garbagecat.util.Memory.ZERO;
import static org.eclipselabs.garbagecat.util.Memory.Unit.KILOBYTES;
import static org.eclipselabs.garbagecat.util.jdk.JdkMath.convertMicrosToMillis;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.eclipselabs.garbagecat.domain.BlockingEvent;
import org.eclipselabs.garbagecat.domain.CombinedData;
import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.domain.OldData;
import org.eclipselabs.garbagecat.domain.PermMetaspaceData;
import org.eclipselabs.garbagecat.domain.YoungData;
import org.eclipselabs.garbagecat.domain.jdk.ApplicationStoppedTimeEvent;
import org.eclipselabs.garbagecat.util.Memory;
import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.CollectorFamily;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil.LogEventType;

/**
 * <p>
 * Manage storing and retrieving JVM data from streams.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class JvmDao {

    private static final Comparator<BlockingEvent> COMPARE_BY_TIMESTAMP = comparing(BlockingEvent::getTimestamp);

    /**
     * List of all event types associate with JVM run.
     */
    List<LogEventType> eventTypes = new ArrayList<>();

    /**
     * Analysis property keys.
     */
    private List<Analysis> analysis = new ArrayList<>();

    /**
     * Collector families for JVM run.
     */
    List<CollectorFamily> collectorFamilies = new ArrayList<>();

    /**
     * Logging lines that do not match any known GC events.
     */
    private List<String> unidentifiedLogLines = new ArrayList<>();

    /**
     * Batch blocking inserts for improved performance.
     */
    private List<BlockingEvent> blockingEvents = new ArrayList<>();

    /**
     * Batch stopped time inserts for improved performance.
     */
    private List<ApplicationStoppedTimeEvent> stoppedTimeEvents = new ArrayList<>();

    /**
     * The JVM options for the JVM run.
     */
    private String options;

    /**
     * JVM version.
     */
    private String version;

    /**
     * JVM memory information.
     */
    private String memory;

    /**
     * Physical memory (bytes).
     */
    private long physicalMemory;

    /**
     * Physical memory free (bytes).
     */
    private long physicalMemoryFree;

    /**
     * Swap size (bytes).
     */
    // prevent false positives of Analysis.INFO_SWAP_DISABLED
    private long swap = -1;

    /**
     * Swap free (bytes).
     */
    private long swapFree;

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
    private int maxHeapSpaceNonBlocking;

    /**
     * Used for tracking max heap occupancy outside of <code>BlockingEvent</code>s.
     */
    private int maxHeapOccupancyNonBlocking;

    /**
     * Used for tracking max perm space outside of <code>BlockingEvent</code>s.
     */
    private int maxPermSpaceNonBlocking;

    /**
     * Used for tracking max perm occupancy outside of <code>BlockingEvent</code>s.
     */
    private int maxPermOccupancyNonBlocking;

    private static boolean created;

    public JvmDao() {
        if (created) {
            cleanup();
        } else {
            created = true;
        }
    }

    public List<String> getUnidentifiedLogLines() {
        return unidentifiedLogLines;
    }

    public List<LogEventType> getEventTypes() {
        return eventTypes;
    }

    public List<Analysis> getAnalysis() {
        return analysis;
    }

    public void addAnalysis(Analysis analysis) {
        if (!this.analysis.contains(analysis)) {
            this.analysis.add(analysis);
        }
    }

    public List<CollectorFamily> getCollectorFamilies() {
        return collectorFamilies;
    }

    public void addBlockingEvent(BlockingEvent event) {
        blockingEvents.add(insertPosition(event), event);
    }

    private int insertPosition(BlockingEvent event) {
        int size = blockingEvents.size();
        if (size > 0 && COMPARE_BY_TIMESTAMP.compare(blockingEvents.get(size - 1), event) <= 0) {
            return size;
        }
        // here we could raise an Exception: Add param boolean reorderingAllowed to method
        // if (!reorderingAllowed) throw new TimeWarpException("bad order")
        return -binarySearch(blockingEvents, event, COMPARE_BY_TIMESTAMP) - 1;
    }

    public void addStoppedTimeEvent(ApplicationStoppedTimeEvent event) {
        stoppedTimeEvents.add(event);
    }

    /**
     * @return The JVM options.
     */
    public String getOptions() {
        return options;
    }

    /**
     * @param options
     *            The JVM options to set.
     */
    public void setOptions(String options) {
        this.options = options;
    }

    /**
     * @return The JVM version information.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param version
     *            The JVM version information to set.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * @return The JVM memory information.
     */
    public String getMemory() {
        return memory;
    }

    /**
     * @param memory
     *            The JVM memory information to set.
     */
    public void setMemory(String memory) {
        this.memory = memory;
    }

    /**
     * @return The JVM environment physical memory (bytes).
     */
    public long getPhysicalMemory() {
        return physicalMemory;
    }

    /**
     * @param physicalMemory
     *            The JVM physical memory to set.
     */
    public void setPhysicalMemory(long physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    /**
     * @return The JVM environment physical free memory (bytes).
     */
    public long getPhysicalMemoryFree() {
        return physicalMemoryFree;
    }

    /**
     * @param physicalMemoryFree
     *            The JVM physical free memory to set.
     */
    public void setPhysicalMemoryFree(long physicalMemoryFree) {
        this.physicalMemoryFree = physicalMemoryFree;
    }

    /**
     * @return The JVM environment swap size (bytes).
     */
    public long getSwap() {
        return swap;
    }

    /**
     * @param swap
     *            The JVM swap to set.
     */
    public void setSwap(long swap) {
        this.swap = swap;
    }

    /**
     * @return The JVM environment swap free (bytes).
     */
    public long getSwapFree() {
        return swapFree;
    }

    /**
     * @param swapFree
     *            The JVM swap free to set.
     */
    public void setSwapFree(long swapFree) {
        this.swapFree = swapFree;
    }

    /**
     * @return The number of <code>ParallelCollection</code> events.
     */
    public long getParallelCount() {
        return parallelCount;
    }

    /**
     * @param parallelCount
     *            The number of <code>ParallelCollection</code> events.
     */
    public void setParallelCount(long parallelCount) {
        this.parallelCount = parallelCount;
    }

    /**
     * @return The number of "inverted" parallelism events.
     */
    public long getInvertedParallelismCount() {
        return invertedParallelismCount;
    }

    /**
     * @param invertedParallelismCount
     *            The number of "low" parallelism events.
     */
    public void setInvertedParallelismCount(long invertedParallelismCount) {
        this.invertedParallelismCount = invertedParallelismCount;
    }

    /**
     * @return The <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    public LogEvent getWorstInvertedParallelismEvent() {
        return worstInvertedParallelismEvent;
    }

    /**
     * @param worstInvertedParallelismEvent
     *            The <code>ParallelCollection</code> event with the lowest "inverted" parallelism.
     */
    public void setWorstInvertedParallelismEvent(LogEvent worstInvertedParallelismEvent) {
        this.worstInvertedParallelismEvent = worstInvertedParallelismEvent;
    }

    /**
     * @return The maximum heap space in non <code>BlockingEvent</code>s.
     */
    public int getMaxHeapSpaceNonBlocking() {
        return maxHeapSpaceNonBlocking;
    }

    /**
     * @param maxHeapSpaceNonBlocking
     *            The maximum heap space in non <code>BlockingEvent</code>s.
     */
    public void setMaxHeapSpaceNonBlocking(int maxHeapSpaceNonBlocking) {
        this.maxHeapSpaceNonBlocking = maxHeapSpaceNonBlocking;
    }

    /**
     * @return The maximum heap occupancy in non <code>BlockingEvent</code>s.
     */
    public int getMaxHeapOccupancyNonBlocking() {
        return maxHeapOccupancyNonBlocking;
    }

    /**
     * @param maxHeapOccupancyNonBlocking
     *            The maximum heap occupancy in non <code>BlockingEvent</code>s.
     */
    public void setMaxHeapOccupancyNonBlocking(int maxHeapOccupancyNonBlocking) {
        this.maxHeapOccupancyNonBlocking = maxHeapOccupancyNonBlocking;
    }

    /**
     * @return The maximum perm space in non <code>BlockingEvent</code>s.
     */
    public int getMaxPermSpaceNonBlocking() {
        return maxPermSpaceNonBlocking;
    }

    /**
     * @param maxPermSpaceNonBlocking
     *            The maximum perm space in non <code>BlockingEvent</code>s.
     */
    public void setMaxPermSpaceNonBlocking(int maxPermSpaceNonBlocking) {
        this.maxPermSpaceNonBlocking = maxPermSpaceNonBlocking;
    }

    /**
     * @return The maximum perm occupancy in non <code>BlockingEvent</code>s.
     */
    public int getMaxPermOccupancyNonBlocking() {
        return maxPermOccupancyNonBlocking;
    }

    /**
     * @param maxPermOccupancyNonBlocking
     *            The maximum perm occupancy in non <code>BlockingEvent</code>s.
     */
    public void setMaxPermOccupancyNonBlocking(int maxPermOccupancyNonBlocking) {
        this.maxPermOccupancyNonBlocking = maxPermOccupancyNonBlocking;
    }

    /**
     * The maximum GC blocking event pause time.
     * 
     * @return maximum pause duration (milliseconds).
     */
    public synchronized int getMaxGcPause() {
        return convertMicrosToMillis(
                ints(this.blockingEvents, BlockingEvent::getDuration).mapToInt(Integer::valueOf).max().orElse(0))
                        .intValue();
    }

    /**
     * The total blocking event pause time.
     * 
     * @return total pause duration (milliseconds).
     */
    public synchronized long getTotalGcPause() {
        return convertMicrosToMillis(
                ints(this.blockingEvents, BlockingEvent::getDuration).collect(summingLong(Long::valueOf))).longValue();
    }

    private static <T> Stream<Integer> ints(List<T> list, Function<T, Integer> function) {
        return list.stream().map(function).filter(Objects::nonNull);
    }

    /**
     * The first blocking event.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The first blocking event.
     */
    public synchronized BlockingEvent getFirstGcEvent() {
        // TODO JdkUtil#parseLogLine no longer needed?
        return this.blockingEvents.isEmpty() ? null : this.blockingEvents.get(0);
    }

    /**
     * Retrieve the last blocking event.
     * 
     * TODO: Should this consider non-blocking events?
     * 
     * @return The last blocking event.
     */
    public synchronized BlockingEvent getLastGcEvent() {
        return this.blockingEvents.isEmpty() ? null : this.blockingEvents.get(blockingEvents.size() - 1);
    }

    /**
     * Delete table(s). Useful when running in server mode during development.
     */
    public synchronized void cleanup() {
        this.blockingEvents.clear();
        JvmDao.created = false;
    }

    /**
     * Retrieve all <code>BlockingEvent</code>s.
     * 
     * @return <code>List</code> of events.
     */
    public synchronized List<BlockingEvent> getBlockingEvents() {
        return this.blockingEvents.stream().map(JvmDao::toBlockingEvent).collect(toList());
    }

    /**
     * Retrieve all <code>BlockingEvent</code>s of the specified type.
     * 
     * @param eventType
     *            The event type to retrieve.
     * @return <code>List</code> of events.
     */
    public synchronized List<BlockingEvent> getBlockingEvents(LogEventType eventType) {
        return this.blockingEvents.stream().filter(e -> e.getName().equals(eventType.toString()))
                .map(JvmDao::toBlockingEvent).collect(toList());
    }

    private static BlockingEvent toBlockingEvent(BlockingEvent e) {
        return e;
        // TODO JdkUtil#hydrateBlockingEvent no longer needed
        // return hydrateBlockingEvent(determineEventType(e.getName()), e.getLogEntry(),
        // e.getTimestamp(), e.getDuration());
    }

    /**
     * The total number of blocking events.
     * 
     * @return total number of blocking events.
     */
    public synchronized int getBlockingEventCount() {
        return this.blockingEvents.size();
    }

    /**
     * The maximum young space size during the JVM run.
     * 
     * @return maximum young space size (kilobytes).
     */
    public synchronized int getMaxYoungSpace() {
        return (int) kilobytes(YoungData.class, YoungData::getYoungSpace).max().orElse(0);
    }

    /**
     * The maximum old space size during the JVM run.
     * 
     * @return maximum old space size (kilobytes).
     */
    public synchronized int getMaxOldSpace() {
        return (int) kilobytes(OldData.class, OldData::getOldSpace).max().orElse(0);
    }

    /**
     * The maximum heap space size during the JVM run.
     * 
     * @return maximum heap size (kilobytes).
     */
    public synchronized int getMaxHeapSpace() {
        return (int) this.blockingEvents.stream() //
                .map(e -> {
                    if (e instanceof OldData) {
                        OldData old = (OldData) e;
                        return add(old.getYoungSpace(), old.getOldSpace());
                    } else if (e instanceof CombinedData) {
                        return ((CombinedData) e).getCombinedSpace();
                    } else {
                        return ZERO;
                    }
                }) //
                .filter(Objects::nonNull) //
                .mapToLong(m -> m.getValue(KILOBYTES)).max().orElse(0);
    }

    /**
     * The maximum heap occupancy during the JVM run.
     * 
     * @return maximum heap occupancy (kilobytes).
     */
    public synchronized int getMaxHeapOccupancy() {
        return (int) this.blockingEvents.stream() //
                .map(e -> {
                    if (e instanceof OldData) {
                        OldData old = (OldData) e;
                        return add(old.getYoungOccupancyInit(), old.getOldOccupancyInit());
                    } else if (e instanceof CombinedData) {
                        return ((CombinedData) e).getCombinedOccupancyInit();
                    } else {
                        return ZERO;
                    }
                }) //
                .filter(Objects::nonNull) //
                .mapToLong(m -> m.getValue(KILOBYTES)).max().orElse(0);
    }

    /**
     * The maximum heap after GC during the JVM run.
     * 
     * @return maximum heap after GC (kilobytes).
     */
    public synchronized int getMaxHeapAfterGc() {
        int oldMaxHeapAfterGc = (int) kilobytes(OldData.class,
                t -> add(t.getYoungOccupancyEnd(), t.getOldOccupancyEnd())).max().orElse(0);
        int combinedMaxHeapAfterGc = (int) kilobytes(CombinedData.class, CombinedData::getCombinedOccupancyEnd).max()
                .orElse(0);
        return Math.max(oldMaxHeapAfterGc, combinedMaxHeapAfterGc);
    }

    /**
     * The maximum perm/metaspace size during the JVM run.
     * 
     * @return maximum perm/metaspace footprint (kilobytes).
     */
    public synchronized int getMaxPermSpace() {
        return (int) kilobytes(PermMetaspaceData.class, PermMetaspaceData::getPermSpace).max().orElse(0);
    }

    /**
     * The maximum perm/metaspace occupancy during the JVM run.
     * 
     * @return maximum perm/metaspac occupancy (kilobytes).
     */
    public synchronized int getMaxPermOccupancy() {
        return (int) kilobytes(PermMetaspaceData.class, PermMetaspaceData::getPermOccupancyInit).max().orElse(0);
    }

    /**
     * The maximum perm/metaspace after GC during the JVM run.
     * 
     * @return maximum perm/metaspac after GC (kilobytes).
     */
    public synchronized int getMaxPermAfterGc() {
        return (int) kilobytes(PermMetaspaceData.class, PermMetaspaceData::getPermOccupancyEnd).max().orElse(0);
    }

    private <T> LongStream kilobytes(Class<T> clazz, Function<T, Memory> func) {
        return this.blockingEvents.stream() //
                .filter(clazz::isInstance) //
                .map(clazz::cast).map(func) //
                .filter(Objects::nonNull) //
                .mapToLong(m -> m.getValue(KILOBYTES));
    }

    private static Memory add(Memory m1, Memory m2) {
        return m1 == null ? nullSafe(m2) : m1.plus(nullSafe(m2));
    }

    private static Memory nullSafe(Memory memory) {
        return memory == null ? ZERO : memory;
    }

    /**
     * The first stopped event.
     * 
     * @return The time first stopped event.
     */
    public synchronized ApplicationStoppedTimeEvent getFirstStoppedEvent() {
        return stoppedTimeEvents.isEmpty() ? null : stoppedTimeEvents.get(0);
    }

    /**
     * Retrieve the last stopped event.
     * 
     * @return The last stopped event.
     */
    public synchronized ApplicationStoppedTimeEvent getLastStoppedEvent() {
        return stoppedTimeEvents.isEmpty() ? null : stoppedTimeEvents.get(stoppedTimeEvents.size() - 1);
    }

    /**
     * Retrieve the last stopped event.
     * 
     * @return The last stopped event.
     */

    /**
     * The maximum stopped time event pause time.
     * 
     * @return maximum pause duration (milliseconds).
     */
    public synchronized int getMaxStoppedTime() {
        return convertMicrosToMillis(ints(this.stoppedTimeEvents, ApplicationStoppedTimeEvent::getDuration)
                .mapToInt(Integer::valueOf).max().orElse(0)).intValue();
    }

    /**
     * The total stopped time event pause time.
     * 
     * @return total pause duration (milliseconds).
     */
    public synchronized int getTotalStoppedTime() {
        return convertMicrosToMillis(ints(this.stoppedTimeEvents, ApplicationStoppedTimeEvent::getDuration)
                .collect(summingLong(Long::valueOf))).intValue();
    }

    /**
     * The total number of stopped time events.
     * 
     * @return total number of stopped time events.
     */
    public synchronized int getStoppedTimeEventCount() {
        return this.stoppedTimeEvents.size();
    }

}
