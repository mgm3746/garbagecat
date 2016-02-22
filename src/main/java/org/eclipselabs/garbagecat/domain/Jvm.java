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

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;

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
        String option = null;
        if (options != null) {
            String regex1 = "-Xss\\d{1,4}(k|K|m|M)";
            String regex2 = "-XX:ThreadStackSize=\\d{1,4}(k|K|m|M)?";
            String regex = "((" + regex1 + ")|(" + regex2 + "))";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(options);
            if (matcher.find()) {
                option = matcher.group(1);
            }
        }
        return option;
    }
    
    /**
     * Whether or not the option to disable explicit garbage collection (<code>-XX:+DisableExplicitGC</code>) is used.
     * 
     * @return True if -XX:+DisableExplicitGC option exists, false otherwise.
     */
    public boolean hasDisableExplicitGCOption() {
        String regex = "-XX:\\+DisableExplicitGC";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(options);
        return matcher.find();
    }

    /**
     * Minimum heap space. Specified with the <code>-Xms</code> option. For example:
     * 
     * <pre>
     * -Xms1024m
     * </pre>
     * 
     * @return The minimum heap space, or null if not explicitly set.
     */
    public String getMinHeapOption() {
        String option = null;
        if (options != null) {
            String regex = "((-Xms\\d{1,5}(m|M|g|G))|(-XX:InitialHeapSize=\\d{1,12}(m|M|g|G)?))";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(options);
            if (matcher.find()) {
                option = matcher.group(1);
            }
        }
        return option;
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
     * -XX:InitialHeapSize=1234567890
     * </pre>
     * 
     * @return The maximum heap space, or null if not explicitly set.
     */
    public String getMaxHeapOption() {
        String option = null;
        if (options != null) {
            String regex = "((-Xmx\\d{1,5}(m|M|g|G))|(-XX:MaxHeapSize=\\d{1,12}(m|M|g|G)?))";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(options);
            if (matcher.find()) {
                option = matcher.group(1);
            }
        }
        return option;
    }

    /**
     * @return The maximum heap space value, or null if not set.
     */
    public String getMaxHeapValue() {
        return JdkUtil.getOptionValue(getMaxHeapOption());
    }

    /**
     * FIXME: Consider different units.
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
        String option = null;
        if (options != null) {
            String regex = "(-XX:PermSize=\\d{1,10}(m|M|g|G)?)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(options);
            if (matcher.find()) {
                option = matcher.group(1);
            }
        }
        return option;
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
        String option = null;
        if (options != null) {
            String regex = "(-XX:MetaspaceSize=\\d{1,10}(m|M|g|G)?)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(options);
            if (matcher.find()) {
                option = matcher.group(1);
            }
        }
        return option;
    }

    /**
     * @return The minimum permanent generation space value, or null if not set.
     */
    public String getMinPermValue() {
        return JdkUtil.getOptionValue(getMinPermOption());
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
        String option = null;
        if (options != null) {
            String regex = "(-XX:MaxPermSize=\\d{1,10}(m|M|g|G)?)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(options);
            if (matcher.find()) {
                option = matcher.group(1);
            }
        }
        return option;
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
        String option = null;
        if (options != null) {
            String regex = "(-XX:MaxMetaspaceSize=\\d{1,10}(m|M|g|G)?)";
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(options);
            if (matcher.find()) {
                option = matcher.group(1);
            }
        }
        return option;
    }

    /**
     * @return The maximum permanent generation space value, or null if not set.
     */
    public String getMaxPermValue() {
        return JdkUtil.getOptionValue(getMaxPermOption());
    }

    /**
     * @return The maximum Metsapce value, or null if not set.
     */
    public String getMaxMetaspaceValue() {
        return JdkUtil.getOptionValue(getMaxMetaspaceOption());
    }

    /**
     * FIXME: Consider different units.
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
     * FIXME: Consider different units.
     * 
     * @return True if the minimum and maximum Metaspace are set equal.
     */
    public boolean isMinAndMaxMetaspaceEqual() {
        return (getMaxMetaspaceValue() == null && getMinMetaspaceValue() == null)
                || (getMaxMetaspaceValue() == null && getMinMetaspaceValue() != null)
                || (getMaxMetaspaceValue() != null && getMinMetaspaceValue() != null
                        && getMaxMetaspaceValue().toUpperCase().equals(getMinMetaspaceValue().toUpperCase()));
    }
}
