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
package org.eclipselabs.garbagecat.domain.jdk;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipselabs.garbagecat.domain.LogEvent;
import org.eclipselabs.garbagecat.util.GcUtil;
import org.eclipselabs.garbagecat.util.jdk.JdkRegEx;
import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.github.joa.domain.Arch;
import org.github.joa.domain.BuiltBy;
import org.github.joa.domain.Os;

/**
 * <p>
 * HEADER_VM_INFO
 * </p>
 * 
 * <p>
 * JVM environment information unique to the JDK build. A version string embedded in libjvm.so/jvm.dll. The same as the
 * fatal error log vm_info. JDK &lt;=8.
 * </p>
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * 1) OpenJDK:
 * </p>
 * 
 * <pre>
 * OpenJDK 64-Bit Server VM (24.95-b01) for linux-amd64 JRE (1.7.0_95-b00), built on Jan 18 2016 21:57:50 by "mockbuild" with gcc 4.8.5 20150623 (Red Hat 4.8.5-4)
 * </pre>
 * 
 * <p>
 * 2) Oracle JDK:
 * </p>
 * 
 * <pre>
 * Java HotSpot(TM) 64-Bit Server VM (24.85-b08) for linux-amd64 JRE (1.7.0_85-b34), built on Sep 29 2015 08:44:21 by "java_re" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class HeaderVmInfoEvent implements LogEvent {

    /**
     * Regular expressions defining the logging.
     */
    private static final String _REGEX = "^(Java HotSpot\\(TM\\)|OpenJDK)( 64-Bit)? Server VM \\(.+\\) for "
            + "(linux|windows|solaris)-(amd64|ppc64|ppc64le|sparc|x86) JRE (\\(Zulu.+\\) )?\\("
            + JdkRegEx.RELEASE_STRING + "\\).+ built on " + JdkRegEx.BUILD_DATE_TIME + ".+$";

    private static Pattern PATTERN = Pattern.compile(_REGEX);

    /**
     * Determine if the logLine matches the logging pattern(s) for this event.
     * 
     * @param logLine
     *            The log line to test.
     * @return true if the log line matches the event pattern, false otherwise.
     */
    public static final boolean match(String logLine) {
        return PATTERN.matcher(logLine).matches();
    }

    /**
     * The log entry for the event. Can be used for debugging purposes.
     */
    private String logEntry;

    /**
     * The time when the GC event started in milliseconds after JVM startup.
     */
    private long timestamp;

    /**
     * Create event from log entry.
     * 
     * @param logEntry
     *            The log entry for the event.
     */
    public HeaderVmInfoEvent(String logEntry) {
        this.logEntry = logEntry;
        this.timestamp = 0L;
    }

    /**
     * @return The chip architecture.
     */
    public Arch getArch() {
        Arch arch = Arch.UNKNOWN;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            int indexArch = 4;
            if (matcher.group(indexArch).equals("amd64") || matcher.group(indexArch).equals("linux64")) {
                arch = Arch.X86_64;
            } else if (matcher.group(indexArch).equals("ppc64le")) {
                arch = Arch.PPC64LE;
            } else if (matcher.group(indexArch).equals("ppc64")) {
                arch = Arch.PPC64;
            } else if (matcher.group(indexArch).equals("x86")) {
                arch = Arch.X86;
            }
        }
        return arch;
    }

    /**
     * @return The JDK build date/time.
     */
    public Date getBuildDate() {
        Date date = null;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            date = GcUtil.getDate(matcher.group(8), matcher.group(9), matcher.group(10), matcher.group(11),
                    matcher.group(12), matcher.group(13));
        }
        return date;
    }

    /**
     * @return JDK builder.
     */
    public BuiltBy getBuiltBy() {
        BuiltBy builtBy = BuiltBy.UNKNOWN;
        if (logEntry.matches(".+\"build\".+")) {
            builtBy = BuiltBy.BUILD;
        } else if (logEntry.matches(".+\"buildslave\".+")) {
            builtBy = BuiltBy.BUILDSLAVE;
        } else if (logEntry.matches(".+\"\".+")) {
            builtBy = BuiltBy.EMPTY;
        } else if (logEntry.matches(".+\"jenkins\".+")) {
            // AdoptOpenJDK
            builtBy = BuiltBy.JENKINS;
        } else if (logEntry.matches(".+\"java_re\".+")) {
            // Oracle current
            builtBy = BuiltBy.JAVA_RE;
        } else if (logEntry.matches(".+\"mach5one\".+")) {
            // Oracle previous
            builtBy = BuiltBy.MACH5ONE;
        } else if (logEntry.matches(".+\"mockbuild\".+")) {
            // Red Hat, CentOS
            builtBy = BuiltBy.MOCKBUILD;
        } else if (logEntry.matches(".+\"temurin\".+")) {
            // Adoptium temurin
            builtBy = BuiltBy.TEMURIN;
        } else if (logEntry.matches(".+\"tester\".+")) {
            // Azul
            builtBy = BuiltBy.TESTER;
        } else if (logEntry.matches(".+\"vsts\".+")) {
            // Microsoft
            builtBy = BuiltBy.VSTS;
        } else if (logEntry.matches(".+\"zulu_re\".+")) {
            // Azul
            builtBy = BuiltBy.ZULU_RE;
        }
        return builtBy;
    }

    /**
     * The Java release string. For example:
     * 
     * <pre>
     * 1.8.0_332-b09-1
     * 11.0.15+9-LTS-1
     * 17.0.3+6-LTS-2
     * </pre>
     * 
     * @return The Java release string.
     */
    public String getJdkReleaseString() {
        String jdkReleaseString = null;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            jdkReleaseString = matcher.group(6);
        }
        return jdkReleaseString;
    }

    /**
     * @return The JDK version (e.g. '8'), or <code>org.github.joa.domain.JvmContext.UNKNOWN</code> if it cannot be
     *         determined. Not available in unified logging (JDK11+).
     */
    public int getJdkVersionMajor() {
        int jdkVersionMajor = org.github.joa.domain.JvmContext.UNKNOWN;
        String regex = "^.+JRE \\(1\\.(5|6|7|8|9|10).+$";
        Pattern pattern = Pattern.compile(regex);
        if (logEntry != null) {
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(1) != null) {
                    jdkVersionMajor = Integer.parseInt(matcher.group(1));
                }
            }
        }
        return jdkVersionMajor;
    }

    /**
     * @return The JDK update (e.g. '60'), or <code>org.github.joa.domain.JvmContext.UNKNOWN</code> if it cannot be
     *         determined.
     */
    public int getJdkVersionMinor() {
        int jdkVersionMinor = org.github.joa.domain.JvmContext.UNKNOWN;
        String regex = "^.+JRE \\(1\\.(5|6|7|8|9|10)\\.\\d_(\\d{1,3})-.+$";
        if (logEntry != null) {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(logEntry);
            if (matcher.find()) {
                if (matcher.group(1) != null) {
                    jdkVersionMinor = Integer.parseInt(matcher.group(2));
                }
            }
        }
        return jdkVersionMinor;
    }

    public String getLogEntry() {
        return logEntry;
    }

    public String getName() {
        return JdkUtil.LogEventType.HEADER_VM_INFO.toString();
    }

    /**
     * @return The OS type.
     */
    public Os getOs() {
        Os osType = Os.UNIDENTIFIED;
        Matcher matcher = PATTERN.matcher(logEntry);
        if (matcher.find()) {
            int indexOs = 3;
            if (matcher.group(indexOs).equals("linux")) {
                osType = Os.LINUX;
            } else if (matcher.group(indexOs).equals("windows")) {
                osType = Os.WINDOWS;
            } else if (matcher.group(indexOs).equals("solaris")) {
                osType = Os.SOLARIS;
            }
        }
        return osType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    /**
     * @return True if 32 bit, false otherwise.
     */
    public boolean is32Bit() {
        boolean is32Bit = false;
        if (logEntry != null) {
            is32Bit = logEntry.matches("^.+32-Bit.+$");
        }
        return is32Bit;
    }

}
