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
package org.eclipselabs.garbagecat.util;

import java.math.BigDecimal;

/**
 * Global constants.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class Constants {

    /**
     * The threshold for the time (seconds) for the first log entry for a GC log to be considered complete. First log
     * entries with timestamps below the threshold may indicate a partial GC log or GC events that were not a
     * recognizable format.
     */
    public static final int FIRST_TIMESTAMP_THRESHOLD = 60;

    /**
     * The minimum throughput (percent of time spent not doing garbage collection for a given time interval) to not be
     * flagged a bottleneck.
     */
    public static final int DEFAULT_BOTTLENECK_THROUGHPUT_THRESHOLD = 90;

    /**
     * kilobyte
     */
    public static final BigDecimal KILOBYTE = new BigDecimal("1024");

    /**
     * megabyte
     */
    public static final BigDecimal MEGABYTE = new BigDecimal("1048576");

    /**
     * gigabyte
     */
    public static final BigDecimal GIGABYTE = new BigDecimal("1073741824");

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private Constants() {

    }
}
