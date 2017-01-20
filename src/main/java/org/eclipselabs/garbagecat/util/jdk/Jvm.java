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
package org.eclipselabs.garbagecat.util.jdk;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.Constants;

/**
 * JVM environment information.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class Jvm {

    /**
     * The date and time the JVM was started.
     */
    private Date startDate;

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
    private long swap;

    /**
     * Swap free (bytes).
     */
    private long swapFree;

    /**
     * Constructor accepting list of JVM options.
     * 
     * @param jvmOptions
     *            The JVM options for the JVM run.
     * @param jvmStartDate
     *            The date and time the JVM was started.
     */
    public Jvm(String jvmOptions, Date jvmStartDate) {
        this.options = jvmOptions;
        this.startDate = jvmStartDate;
    }

    /**
     * @return The date and time the JVM was started.
     */
    public Date getStartDate() {
        return startDate;
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

    public long getPhysicalMemory() {
        return physicalMemory;
    }

    public void setPhysicalMemory(long physicalMemory) {
        this.physicalMemory = physicalMemory;
    }

    public long getPhysicalMemoryFree() {
        return physicalMemoryFree;
    }

    public void setPhysicalMemoryFree(long physicalMemoryFree) {
        this.physicalMemoryFree = physicalMemoryFree;
    }

    public long getSwap() {
        return swap;
    }

    public void setSwap(long swap) {
        this.swap = swap;
    }

    public long getSwapFree() {
        return swapFree;
    }

    public void setSwapFree(long swapFree) {
        this.swapFree = swapFree;
    }

    /**
     * Thread stack size. Specified with either the <code>-Xss</code> or <code>-XX:ThreadStackSize</code> options. For
     * example:
     * 
     * <pre>
     * -Xss128k
     * </pre>
     * 
     * <pre>
     * -XX:ThreadStackSize=128
     * </pre>
     * 
     * The <code>-Xss</code> options does not work on Solaris, only the <code>-XX:ThreadStackSize</code> option.
     * 
     * @return The JVM thread stack size setting, or null if not explicitly set.
     */
    public String getThreadStackSizeOption() {
        String regex = "(-X(ss|X:ThreadStackSize=)(\\d{1,12})(" + JdkRegEx.OPTION_SIZE + ")?)";
        return getJvmOption(regex);
    }

    /**
     * @return The thread stack size value, or null if not set. For example:
     * 
     *         <pre>
     * 256K
     *         </pre>
     */
    public String getThreadStackSizeValue() {
        return JdkUtil.getOptionValue(getThreadStackSizeOption());
    }

    /**
     * Whether or not the option to disable explicit garbage collection (<code>-XX:+DisableExplicitGC</code>) is used.
     * 
     * @return True if -XX:+DisableExplicitGC option exists, false otherwise.
     */
    public String getDisableExplicitGCOption() {
        String regex = "(-XX:\\+DisableExplicitGC)";
        return getJvmOption(regex);
    }

    /**
     * Minimum heap space. Specified with the <code>-Xms</code> option. For example:
     * 
     * <pre>
     * -Xms1024m
     * -XX:HeapSize=1234567890
     * </pre>
     * 
     * @return The minimum heap space, or null if not explicitly set.
     */
    public String getMinHeapOption() {
        String regex = "(-X(ms|X:InitialHeapSize=)(\\d{1,12})(" + JdkRegEx.OPTION_SIZE + ")?)";
        return getJvmOption(regex);
    }

    /**
     * @return The minimum heap space value, or null if not set. For example:
     * 
     *         <pre>
     * 2048M
     *         </pre>
     */
    public String getMinHeapValue() {
        return JdkUtil.getOptionValue(getMinHeapOption());
    }

    /**
     * Maximum heap space. Specified with the <code>-Xmx</code> option. For example:
     * 
     * <pre>
     * -Xmx1024m
     * -XX:MaxHeapSize=1234567890
     * </pre>
     * 
     * @return The maximum heap space, or null if not explicitly set.
     */
    public String getMaxHeapOption() {
        String regex = "(-X(mx|X:MaxHeapSize=)(\\d{1,12})(" + JdkRegEx.OPTION_SIZE + ")?)";
        return getJvmOption(regex);
    }

    /**
     * @return The maximum heap space value, or null if not set. For example:
     * 
     *         <pre>
     * 2048M
     *         </pre>
     */
    public String getMaxHeapValue() {
        return JdkUtil.getOptionValue(getMaxHeapOption());
    }

    /**
     * @return The maximum heap space in bytes, or 0 if not set.
     */
    public long getMaxHeapBytes() {
        long maxHeapBytes = 0;
        if (getMaxHeapValue() != null) {
            maxHeapBytes = JdkUtil.convertOptionSizeToBytes(getMaxHeapValue());
        }
        return maxHeapBytes;
    }

    /**
     * @return The maximum perm space in bytes, or 0 if not set.
     */
    public long getMaxPermBytes() {
        long maxPermBytes = 0;
        if (getMaxPermValue() != null) {
            maxPermBytes = JdkUtil.convertOptionSizeToBytes(getMaxPermValue());
        }
        return maxPermBytes;
    }

    /**
     * @return The maximum metaspace in bytes, or 0 if not set.
     */
    public long getMaxMetaspaceBytes() {
        long maxMetaspaceBytes = 0;
        if (getMaxMetaspaceValue() != null) {
            maxMetaspaceBytes = JdkUtil.convertOptionSizeToBytes(getMaxMetaspaceValue());
        }
        return maxMetaspaceBytes;
    }

    /**
     * 
     * @return True if the minimum and maximum heap space are set equal.
     */
    public boolean isMinAndMaxHeapSpaceEqual() {
        return getMaxHeapValue() != null && getMinHeapValue() != null
                && getMaxHeapValue().toUpperCase().equals(getMinHeapValue().toUpperCase());
    }

    /**
     * Minimum permanent generation space. Specified with the <code>-XX:PermSize</code> option. For example:
     * 
     * <pre>
     * -XX:PermSize=128M
     * </pre>
     * 
     * @return The minimum permanent generation space, or null if not explicitly set.
     */
    public String getMinPermOption() {
        String regex = "(-XX:PermSize=(\\d{1,12})(" + JdkRegEx.OPTION_SIZE + ")?)";
        return getJvmOption(regex);
    }

    /**
     * @return The minimum permanent generation space value, or null if not set. For example:
     * 
     *         <pre>
     * 128M
     *         </pre>
     */
    public String getMinPermValue() {
        return JdkUtil.getOptionValue(getMinPermOption());
    }

    /**
     * Minimum Metaspace. Specified with the <code>-XX:MetsspaceSize</code> option. For example:
     * 
     * <pre>
     * -XX:MetaspaceSize=128M
     * </pre>
     * 
     * @return The minimum permanent generation space, or null if not explicitly set.
     */
    public String getMinMetaspaceOption() {
        String regex = "(-XX:MetaspaceSize=(\\d{1,10})(" + JdkRegEx.OPTION_SIZE + ")?)";
        return getJvmOption(regex);
    }

    /**
     * @return The minimum Metaspace value, or null if not set. For example:
     * 
     *         <pre>
     * 128M
     *         </pre>
     */
    public String getMinMetaspaceValue() {
        return JdkUtil.getOptionValue(getMinMetaspaceOption());
    }

    /**
     * Maximum permanent generation space (<code>-XX:MaxPermSize</code>). For example:
     * 
     * <pre>
     * -XX:MaxPermSize=128M
     * </pre>
     * 
     * @return The maximum permanent generation space, or null if not explicitly set.
     */
    public String getMaxPermOption() {
        String regex = "(-XX:MaxPermSize=(\\d{1,10})(" + JdkRegEx.OPTION_SIZE + ")?)";
        return getJvmOption(regex);
    }

    /**
     * @return The maximum permanent generation space value, or null if not set. For example:
     * 
     *         <pre>
     * 128M
     *         </pre>
     */
    public String getMaxPermValue() {
        return JdkUtil.getOptionValue(getMaxPermOption());
    }

    /**
     * Maximum Metaspace (<code>-XX:MaxMetaspaceSize</code>). For example:
     * 
     * <pre>
     * -XX:MaxMetaspaceSize=128M
     * </pre>
     * 
     * @return The maximum Metaspace, or null if not explicitly set.
     */
    public String getMaxMetaspaceOption() {
        String regex = "(-XX:MaxMetaspaceSize=(\\d{1,10})(" + JdkRegEx.OPTION_SIZE + ")?)";
        return getJvmOption(regex);
    }

    /**
     * @return The maximum Metaspace value, or null if not set. For example:
     * 
     *         <pre>
     * 128M
     *         </pre>
     */
    public String getMaxMetaspaceValue() {
        return JdkUtil.getOptionValue(getMaxMetaspaceOption());
    }

    /**
     * Client Distributed Garbage Collection (DGC) interval in milliseconds.
     * 
     * <pre>
     * -Dsun.rmi.dgc.client.gcInterval=14400000
     * </pre>
     * 
     * @return The client Distributed Garbage Collection (DGC), or null if not explicitly set.
     */
    public String getRmiDgcClientGcIntervalOption() {
        String regex = "(-Dsun.rmi.dgc.client.gcInterval=(\\d{1,12}))";
        return getJvmOption(regex);
    }

    /**
     * @return The client Distributed Garbage Collection (DGC) interval value in (milliseconds), or null if not set. For
     *         example:
     * 
     *         <pre>
     *         14400000
     *         </pre>
     */
    public String getRmiDgcClientGcIntervalValue() {
        return JdkUtil.getOptionValue(getRmiDgcClientGcIntervalOption());
    }

    /**
     * Server Distributed Garbage Collection (DGC) interval in milliseconds.
     * 
     * <pre>
     * -Dsun.rmi.dgc.server.gcInterval=14400000
     * </pre>
     * 
     * @return The server Distributed Garbage Collection (DGC), or null if not explicitly set.
     */
    public String getRmiDgcServerGcIntervalOption() {
        String regex = "(-Dsun.rmi.dgc.server.gcInterval=(\\d{1,12}))";
        return getJvmOption(regex);
    }

    /**
     * @return The server Distributed Garbage Collection (DGC) interval value in (milliseconds), or null if not set. For
     *         example:
     * 
     *         <pre>
     *         14400000
     *         </pre>
     */
    public String getRmiDgcServerGcIntervalValue() {
        return JdkUtil.getOptionValue(getRmiDgcServerGcIntervalOption());
    }

    /**
     * The option to disable writing out a heap dump when OutOfMemoryError. For example:
     * 
     * <pre>
     * -XX:-HeapDumpOnOutOfMemoryError
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getHeapDumpOnOutOfMemoryErrorDisabledOption() {
        String regex = "(-XX:-HeapDumpOnOutOfMemoryError)";
        return getJvmOption(regex);
    }

    /**
     * The option to write out a heap dump when OutOfMemoryError. For example:
     * 
     * <pre>
     * -XX:+HeapDumpOnOutOfMemoryError
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getHeapDumpOnOutOfMemoryErrorEnabledOption() {
        String regex = "(-XX:\\+HeapDumpOnOutOfMemoryError)";
        return getJvmOption(regex);
    }

    /**
     * -javaagent instrumentation option. For example:
     * 
     * <pre>
     * -javaagent:byteman.jar=script:kill-3.btm,boot:byteman.jar
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getJavaagentOption() {
        String regex = "(-javaagent:[\\S]+)";
        return getJvmOption(regex);
    }

    /**
     * agentpath instrumentation option. For example:
     * 
     * <pre>
     * -agentpath:/path/to/agent.so
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getAgentpathOption() {
        String regex = "(-agentpath:[\\S]+)";
        return getJvmOption(regex);
    }

    /**
     * The option to disable background compilation of bytecode. For example:
     * 
     * <pre>
     * -Xbatch
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getXBatchOption() {
        String regex = "(-Xbatch)";
        return getJvmOption(regex);
    }

    /**
     * The option to disable background compilation of bytecode. For example:
     * 
     * <pre>
     * -XX:-BackgroundCompilation
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getDisableBackgroundCompilationOption() {
        String regex = "(-XX:-BackgroundCompilation)";
        return getJvmOption(regex);
    }

    /**
     * The option to enable compilation of bytecode on first invocation. For example:
     * 
     * <pre>
     * -Xcomp
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getXCompOption() {
        String regex = "(-Xcomp)";
        return getJvmOption(regex);
    }

    /**
     * The option to disable just in time (JIT) compilation. For example:
     * 
     * <pre>
     * -Xint
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getXIntOption() {
        String regex = "(-Xint)";
        return getJvmOption(regex);
    }

    /**
     * The option to allow explicit garbage collection to be handled concurrently by the CMS and G1 collectors. For
     * example:
     * 
     * <pre>
     * -XX:+ExplicitGCInvokesConcurrent
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getExplicitGcInvokesConcurrentOption() {
        String regex = "(-XX:\\+ExplicitGCInvokesConcurrent)";
        return getJvmOption(regex);
    }

    /**
     * The option to output JVM command line options at the beginning of gc logging. For example:
     * 
     * <pre>
     * -XX:+PrintCommandLineFlags
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintCommandLineFlagsOption() {
        String regex = "(-XX:\\+PrintCommandLineFlags)";
        return getJvmOption(regex);
    }

    /**
     * The option to output details at gc. For example:
     * 
     * <pre>
     * -XX:+PrintGCDetails
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintGCDetailsOption() {
        String regex = "(-XX:\\+PrintGCDetails)";
        return getJvmOption(regex);
    }

    /**
     * The option to output details at gc disabled. For example:
     * 
     * <pre>
     * -XX:-PrintGCDetails
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintGCDetailsDisabled() {
        String regex = "(-XX:\\-PrintGCDetails)";
        return getJvmOption(regex);
    }

    /**
     * The option for the CMS young collector. For example:
     * 
     * <pre>
     * -XX:+UseParNewGC
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getUseParNewGCOption() {
        String regex = "(-XX:\\+UseParNewGC)";
        return getJvmOption(regex);
    }

    /**
     * The option to disable the PAR_NEW collector. For example:
     * 
     * <pre>
     * -XX:-UseParNewGC
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getUseParNewGcDisabled() {
        String regex = "(-XX:\\-UseParNewGC)";
        return getJvmOption(regex);
    }

    /**
     * The option for the CMS old collector. For example:
     * 
     * <pre>
     * -XX:+UseConcMarkSweepGC
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getUseConcMarkSweepGCOption() {
        String regex = "(-XX:\\+UseConcMarkSweepGC)";
        return getJvmOption(regex);
    }

    /**
     * The option for allowing the CMS collector to collect Perm/Metaspace. For example:
     * 
     * <pre>
     * -XX:+CMSClassUnloadingEnabled
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getCMSClassUnloadingEnabled() {
        String regex = "(-XX:\\+CMSClassUnloadingEnabled)";
        return getJvmOption(regex);
    }

    /**
     * The option for outputting times for reference processing (weak, soft,JNI). For example:
     * 
     * <pre>
     * -XX:+PrintReferenceGC
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintReferenceGC() {
        String regex = "(-XX:\\+PrintReferenceGC)";
        return getJvmOption(regex);
    }

    /**
     * The option for printing trigger information. For example:
     * 
     * <pre>
     * -XX:+PrintGCCause
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintGCCause() {
        String regex = "(-XX:\\+PrintGCCause)";
        return getJvmOption(regex);
    }

    /**
     * The option for printing trigger information disabled. For example:
     * 
     * <pre>
     * -XX:-PrintGCCause
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintGCCauseDisabled() {
        String regex = "(-XX:\\-PrintGCCause)";
        return getJvmOption(regex);
    }

    /**
     * The option for enabling tiered compilation. For example:
     * 
     * <pre>
     * -XX:+TieredCompilation
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getTieredCompilation() {
        String regex = "(-XX:\\+TieredCompilation)";
        return getJvmOption(regex);
    }

    /**
     * The option for string deduplication statistics. For example:
     * 
     * <pre>
     * -XX:+PrintStringDeduplicationStatistics
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintStringDeduplicationStatistics() {
        String regex = "(-XX:\\+PrintStringDeduplicationStatistics)";
        return getJvmOption(regex);
    }

    /**
     * The option for setting CMS initiating occupancy fraction, the tenured generation occupancy percentage that
     * triggers a concurrent collection. For example:
     * 
     * <pre>
     * -XX:CMSInitiatingOccupancyFraction=70
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getCMSInitiatingOccupancyFraction() {
        String regex = "(-XX:CMSInitiatingOccupancyFraction=\\d{1,3})";
        return getJvmOption(regex);
    }

    /**
     * The option for bias locking disabled. For example:
     * 
     * <pre>
     * -XX:-UseBiasedLocking
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getBiasedLockingDisabled() {
        String regex = "(-XX:\\-UseBiasedLocking)";
        return getJvmOption(regex);
    }

    /**
     * The option for printing a class histogram when a thread dump is taken:
     * 
     * <pre>
     * -XX:+PrintClassHistogram
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintClassHistogramEnabled() {
        String regex = "(-XX:\\+PrintClassHistogram)\\b";
        return getJvmOption(regex);
    }

    /**
     * The option for printing a class histogram before every full collection.
     * 
     * <pre>
     * -XX:+PrintClassHistogramAfterFullGC
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintClassHistogramAfterFullGcEnabled() {
        String regex = "(-XX:\\+PrintClassHistogramAfterFullGC)";
        return getJvmOption(regex);
    }

    /**
     * The option for printing a class histogram before every full collection:
     * 
     * <pre>
     * -XX:+PrintClassHistogramBeforeFullGC
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintClassHistogramBeforeFullGcEnabled() {
        String regex = "(-XX:\\+PrintClassHistogramBeforeFullGC)";
        return getJvmOption(regex);
    }

    /**
     * The option for printing application concurrent time:
     * 
     * <pre>
     * -XX:+PrintGCApplicationConcurrentTime
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintGcApplicationConcurrentTime() {
        String regex = "(-XX:\\+PrintGCApplicationConcurrentTime)";
        return getJvmOption(regex);
    }

    /**
     * The option for trace class unloading output:
     * 
     * <pre>
     * -XX:+TraceClassUnloading
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getTraceClassUnloading() {
        String regex = "(-XX:\\+TraceClassUnloading)";
        return getJvmOption(regex);
    }

    /**
     * The option for disabling compressed object references.
     * 
     * <pre>
     * -XX:-UseCompressedOops
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getUseCompressedOopsDisabled() {
        String regex = "(-XX:\\-UseCompressedOops)";
        return getJvmOption(regex);
    }

    /**
     * The option for enabling compressed object references.
     * 
     * <pre>
     * -XX:+UseCompressedOops
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getUseCompressedOopsEnabled() {
        String regex = "(-XX:\\+UseCompressedOops)";
        return getJvmOption(regex);
    }

    /**
     * The option for disabling log file rotation.
     * 
     * <pre>
     * -XX:-UseGCLogFileRotation
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getUseGcLogFileRotationDisabled() {
        String regex = "(-XX:\\-UseGCLogFileRotation)";
        return getJvmOption(regex);
    }

    /**
     * The option for setting number of log files to rotate. For example:
     * 
     * <pre>
     * -XX:NumberOfGCLogFiles=5
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getNumberOfGcLogFiles() {
        String regex = "(-XX:NumberOfGCLogFiles=\\d{1,2})";
        return getJvmOption(regex);
    }

    /**
     * The option for compressed class pointers enabled.
     * 
     * <pre>
     * -XX:+UseCompressedClassPointers
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getUseCompressedClassPointersEnabled() {
        String regex = "(-XX:\\+UseCompressedClassPointers)";
        return getJvmOption(regex);
    }

    /**
     * The option for compressed class pointers disabled.
     * 
     * <pre>
     * -XX:-UseCompressedClassPointers
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getUseCompressedClassPointersDisabled() {
        String regex = "(-XX:\\-UseCompressedClassPointers)";
        return getJvmOption(regex);
    }

    /**
     * The option for setting CompressedClassSpaceSize.
     * 
     * <pre>
     * -XX:CompressedClassSpaceSize=768m
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getCompressedClassSpaceSizeOption() {
        String regex = "(-XX:CompressedClassSpaceSize=((\\d{1,10})(" + JdkRegEx.OPTION_SIZE + ")?))";
        return getJvmOption(regex);
    }

    /**
     * @return The compressed class space size value, or null if not set. For example:
     * 
     *         <pre>
     * 768m
     *         </pre>
     */
    public String getCompressedClassSpaceSizeValue() {
        return JdkUtil.getOptionValue(getCompressedClassSpaceSizeOption());
    }

    /**
     * @return The compressed class space in bytes, or 0 if not set.
     */
    public long getCompressedClassSpaceSizeBytes() {
        long compressedClassSpaceSizeBytes = 0;
        if (getCompressedClassSpaceSizeValue() != null) {
            compressedClassSpaceSizeBytes = JdkUtil.convertOptionSizeToBytes(getCompressedClassSpaceSizeValue());
        }
        return compressedClassSpaceSizeBytes;
    }

    /**
     * The option for outputting statistics for the CMS FreeListSpace.
     * 
     * <pre>
     * -XX:PrintFLSStatistics=1
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintFLStatistics() {
        String regex = "(-XX:PrintFLSStatistics=(\\d))";
        return getJvmOption(regex);
    }

    /**
     * The option for outputting tenuring distribution information.
     * 
     * <pre>
     * -XX:+PrintTenuringDistribution
     * </pre>
     * 
     * @return the option if it exists, null otherwise.
     */
    public String getPrintTenuringDistribution() {
        String regex = "(-XX:\\+PrintTenuringDistribution)";
        return getJvmOption(regex);
    }

    /**
     * 
     * @return True if the minimum and maximum permanent generation space are set equal.
     */
    public boolean isMinAndMaxPermSpaceEqual() {
        return (getMinPermValue() == null && getMaxPermValue() == null) || (getMinPermValue() != null
                && getMaxPermValue() != null && JdkUtil.convertOptionSizeToBytes(getMinPermValue()) == JdkUtil
                        .convertOptionSizeToBytes(getMaxPermValue()));
    }

    /**
     * 
     * @return True if the minimum and maximum Metaspace are set equal.
     */
    public boolean isMinAndMaxMetaspaceEqual() {
        return (getMinMetaspaceValue() == null && getMaxMetaspaceValue() == null) || (getMinMetaspaceValue() != null
                && getMaxMetaspaceValue() != null && JdkUtil.convertOptionSizeToBytes(getMinMetaspaceValue()) == JdkUtil
                        .convertOptionSizeToBytes(getMaxMetaspaceValue()));
    }

    /**
     * @param regex
     *            The option regular expression.
     * @return The JVM option, or null if not explicitly set.
     */
    public String getJvmOption(final String regex) {
        String option = null;
        if (options != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(options);
            if (matcher.find()) {
                option = matcher.group(1);
            }
        }
        return option;
    }

    /**
     * @return True if stack size &gt;= 1024k, false otherwise.
     */
    public boolean hasLargeThreadStackSize() {
        boolean hasLargeThreadStackSize = false;

        String threadStackSize = getThreadStackSizeValue();
        if (threadStackSize != null
                && JdkUtil.convertOptionSizeToBytes(threadStackSize) >= Constants.MEGABYTE.longValue()) {
            hasLargeThreadStackSize = true;
        }

        return hasLargeThreadStackSize;
    }

    /**
     * @return The JDK version (e.g. '8'), or `0` if it could not be determined.
     */
    public int JdkNumber() {
        String regex = "^.+JRE \\(1\\.(5|6|7|8|9).+$";
        int number = 0;
        if (version != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(version);
            if (matcher.find()) {
                if (matcher.group(1) != null) {
                    number = Integer.parseInt(matcher.group(1));
                }
            }
        }
        return number;
    }

    /**
     * @return True if 64 bit, false otherwise.
     */
    public boolean is64Bit() {
        boolean is64Bit = false;
        if (version != null) {
            is64Bit = version.matches("^.+64-Bit.+$");
        }
        return is64Bit;
    }

    /**
     * @return The percentage of swap that is free. 100 means no swap used. 0 means all swap used.
     */
    public long getPercentSwapFree() {
        long percentSwapFree;
        if (swap > 0) {
            BigDecimal percentFree = new BigDecimal(swapFree);
            percentFree = percentFree.divide(new BigDecimal(swap), 2, RoundingMode.HALF_EVEN);
            percentFree = percentFree.movePointRight(2);
            percentSwapFree = percentFree.longValue();
        } else {
            percentSwapFree = 100L;
        }
        return percentSwapFree;
    }
}
