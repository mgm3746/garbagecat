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
package org.eclipselabs.garbagecat.domain;

/**
 * A safepoint is when all threads are stopped and reachable by the JVM. Many JVM operations require that all threads be
 * in a safepoint to execute. For example: a "stop the world" garbage collection, thread dump, heap dump, etc.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface SafepointEvent extends LogEvent {

    /**
     * @return The elapsed clock time for the event in microseconds (rounded).
     */
    int getDuration();

}
