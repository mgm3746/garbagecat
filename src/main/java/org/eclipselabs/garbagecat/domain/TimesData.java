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
package org.eclipselabs.garbagecat.domain;

/**
 * Times data block output at the end of some garbage collection logging events.
 * 
 * <h3>Example Logging</h3>
 * 
 * <p>
 * [Times: user=0.31 sys=0.00, real=0.04 secs]
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface TimesData {

    /**
     * Regular expression for times data block.
     */
    String REGEX = "( \\[Times: user=(\\d{1,4}[\\.\\,]\\d{2}) sys=\\d{1,4}[\\.\\,]\\d{2}, "
            + "real=(\\d{1,4}[\\.\\,]\\d{2}) secs\\])";

    /**
     * @return The time of all threads added together in centoseconds.
     */
    int getTimeUser();

    /**
     * @return The wall (clock) time in centoseconds.
     */
    int getTimeReal();

    /**
     * @return Percent user:real time rounded up the the nearest whole number. With good parallelism, the user time will
     *         be (# threads) x (real time).
     */
    int getParallelism();
}
