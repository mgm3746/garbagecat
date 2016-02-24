/******************************************************************************
 * Garbage Cat * * Copyright (c) 2008-2010 Red Hat, Inc. * All rights reserved. This program and the accompanying
 * materials * are made available under the terms of the Eclipse Public License v1.0 * which accompanies this
 * distribution, and is available at * http://www.eclipse.org/legal/epl-v10.html * * Contributors: * Red Hat, Inc. -
 * initial API and implementation *
 ******************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

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
     * @return The thread stack size value, or null if not set.
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
     * @return The minimum heap space value, or null if not set.
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
     * @return The maximum heap space value, or null if not set.
     */
    public String getMaxHeapValue() {
        return JdkUtil.getOptionValue(getMaxHeapOption());
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
        String regex = "(-XX:PermSize=(\\d{1,10})(" + JdkRegEx.OPTION_SIZE + ")?)";
        return getJvmOption(regex);
    }

    /**
     * @return The minimum permanent generation space value, or null if not set.
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
     * @return The minimum Metaspace value, or null if not set.
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
     * @return The maximum permanent generation space value, or null if not set.
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
     * @return The maximum Metaspace value, or null if not set.
     */
    public String getMaxMetaspaceValue() {
        return JdkUtil.getOptionValue(getMaxMetaspaceOption());
    }

    /**
     * 
     * @return True if the minimum and maximum permanent generation space are set equal.
     */
    public boolean isMinAndMaxPermSpaceEqual() {
        return (getMaxPermValue() == null && getMinPermValue() == null)
                || (getMaxPermValue() == null && getMinPermValue() != null)
                || (getMaxPermValue() != null && getMinPermValue() != null
                        && getMaxPermValue().toUpperCase().equals(getMinPermValue().toUpperCase()));
    }

    /**
     * 
     * @return True if the minimum and maximum Metaspace are set equal.
     */
    public boolean isMinAndMaxMetaspaceEqual() {
        return (getMaxMetaspaceValue() == null && getMinMetaspaceValue() == null)
                || (getMaxMetaspaceValue() == null && getMinMetaspaceValue() != null)
                || (getMaxMetaspaceValue() != null && getMinMetaspaceValue() != null
                        && getMaxMetaspaceValue().toUpperCase().equals(getMinMetaspaceValue().toUpperCase()));
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
     * @return True if stack size >= 1024k, false otherwise.
     */
    public boolean hasLargeThreadStackSize() {
        boolean hasLargeThreadStackSize = false;
        
        String threadStackSize = getThreadStackSizeValue();
        if (threadStackSize != null && JdkUtil.convertSizeToBytes(threadStackSize) >= Constants.MEGABYTE.longValue()) {
            hasLargeThreadStackSize = true;
        }
        
        return hasLargeThreadStackSize;
    }
}
