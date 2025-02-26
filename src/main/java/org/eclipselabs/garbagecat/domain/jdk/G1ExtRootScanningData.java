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
package org.eclipselabs.garbagecat.domain.jdk;

import org.eclipselabs.garbagecat.domain.LogEvent;

/**
 * <p>
 * At the beginning of a young collection, the parallel GC worker threads scan external roots (objects allocated outside
 * the Java heap) to find any that reach into the current collection set (CSet). Objects in the CSet referenced by an
 * external root are "live" and not eligible to be collected.
 * </p>
 * 
 * <p>
 * External roots:
 * </p>
 * 
 * <ul>
 * <li>Thread stack roots</li>
 * <li>JNI Global variables</li>
 * <li>System dictionary</li>
 * <li>Synchronized monitors that have been inflated to an OS level monitor (e.g. BiasedLocking disabled and there is
 * contention on the lock)</li>
 * </ul>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface G1ExtRootScanningData extends LogEvent {

    /**
     * Use for logging events that do not include external root scanning data.
     */
    public static final int NO_DATA = 0;

    /**
     * @return The elapsed clock time to scan external roots in microseconds (rounded).
     */
    long getExtRootScanningTime();

}
