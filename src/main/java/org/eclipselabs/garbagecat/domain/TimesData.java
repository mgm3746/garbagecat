/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2025 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.domain;

/**
 * CPU time at the end of some <code>LogEvent</code>, computed with a different clock than the clock used to compute
 * <code>BlockingEvent</code> duration.
 * 
 * <code>BlockingEvent</code> duration includes <code>OtherTime</code> when CPU time may not be used (e.g. I/O delays
 * writing to a log file), so it's possible that the "real" time is less than the <code>BlockingEvent</code> duration.
 * 
 * <h2>Example Logging</h2>
 * 
 * <p>
 * JDK8 and prior:
 * </p>
 * 
 * <pre>
 * [Times: user=0.31 sys=0.00, real=0.04 secs]
 * </pre>
 * 
 * <p>
 * JDK9+:
 * </p>
 * 
 * <pre>
 * User=0.00s Sys=0.00s Real=0.00s
 * </pre>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface TimesData {

    /**
     * Use for logging events that do not include times data.
     */
    public static final int NO_DATA = -Integer.MIN_VALUE;

    /**
     * Regular expression for times data block.
     * 
     * [Times: user=0.44 sys=0.00, real=0.08 secs]
     */
    public static final String REGEX = "( \\[Times: user=(\\d{1,5}[\\.\\,]\\d{2}) sys=(\\d{1,5}[\\.\\,]\\d{2}), "
            + "real=(\\d{1,5}[\\.\\,]\\d{2}) secs\\])";

    /**
     * Regular expression for times data block JDK9+.
     * 
     * User=0.00s Sys=0.00s Real=0.00s
     */
    public static final String REGEX_JDK9 = "( User=(\\d{1,5}[\\.\\,]\\d{2})s Sys=(\\d{1,5}[\\.\\,]\\d{2})s "
            + "Real=(\\d{1,5}[\\.\\,]\\d{2})s)";

    /**
     * @return Percent (user+sys):real time rounded up the the nearest whole number. With good parallelism, the user+sys
     *         time will be (# threads) x (real time).
     */
    int getParallelism();

    /**
     * @return The wall (clock) cpu time in centiseconds when cpu is being used.
     */
    int getTimeReal();

    /**
     * @return The system (kernel) cpu time of all threads added together in centiseconds.
     */
    int getTimeSys();

    /**
     * @return The user (non-kernel) cpu time of all threads added together in centiseconds.
     */
    int getTimeUser();
}
