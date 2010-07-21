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

	public Date getStartDate() {
		return startDate;
	}

	public String getOptions() {
		return options;
	}

	/**
	 * Thread stack size. Specified with either the <code>-Xss</code> or
	 * <code>-XX:ThreadStackSize</code> options. For example:
	 * 
	 * <pre>
	 * -Xss128k
	 * </pre>
	 * 
	 * <pre>
	 * -XX:ThreadStackSize=128
	 * </pre>
	 * 
	 * The <code>-Xss</code> options does not work on Solaris, only the
	 * <code>-XX:ThreadStackSize</code> option.
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
			String regex = "(-Xms\\d{1,5}(m|M|g|G))";
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
	 * </pre>
	 * 
	 * @return The maximum heap space, or null if not explicitly set.
	 */
	public String getMaxHeapOption() {
		String option = null;
		if (options != null) {
			String regex = "(-Xmx\\d{1,5}(m|M|g|G))";
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
	 * Minimum permanent generation space. Specified with the <code>-XX:PermSize</code> option.
	 * For example:
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
			String regex = "(-XX:PermSize=\\d{1,5}(m|M|g|G))";
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
	 * Maximum permanent generation space. Specified with the <code>-XX:MaxPermSize</code> option.
	 * For example:
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
			String regex = "(-XX:MaxPermSize=\\d{1,5}(m|M|g|G))";
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
	 * FIXME: Consider different units.
	 * 
	 * @return True if the minimum and maximum permanent generation space are set equal.
	 */
	public boolean isMinAndMaxPermSpaceEqual() {
		return (getMaxPermValue() == null && getMinPermValue() == null)
				|| (getMaxPermValue() == null && getMinPermValue() != null)
				|| (getMaxPermValue() != null && getMinPermValue() != null && getMaxPermValue()
						.toUpperCase().equals(getMinPermValue().toUpperCase()));
	}
}
