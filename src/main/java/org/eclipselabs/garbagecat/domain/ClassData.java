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
package org.eclipselabs.garbagecat.domain;

import org.eclipselabs.garbagecat.util.Memory;

/**
 * Permanent generation or metaspace data.
 * 
 * <p>
 * In JDK7 the decommissioning of the perm gen space began when interned strings and class static variables were moved
 * to the Java heap, and symbols were moved to the native heap.
 * </p>
 * 
 * <p>
 * In JDK8 the perm gen space was fully replaced by the metaspace, a native space holding only class metadata. The value
 * in the gc logging is the sum of the committed compressed class space (CompressedClassSpaceSize) and the other class
 * metadata.
 * </p>
 * 
 * <p>
 * Unlike perm gen, it is typically not necessary to set a max/min metaspace size:
 * </p>
 * 
 * <ul>
 * <li>Metaspace size is unlimited by default. Since it only holds class metadata, size requirements are minimal.</li>
 * <li>It does not require a full gc to resize the metaspace, as it did with the perm generation.</li>
 * </ul>
 * 
 * <p>
 * Reference: JEP 122: Remove the Permanent Generation, https://openjdk.java.net/jeps/122.
 * </p>
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface ClassData {

    /**
     * @return Perm generation or metaspace end occupancy in kilobytes.
     */
    Memory getClassOccupancyEnd();

    /**
     * @return Perm generation or metaspace initial occupancy in kilobytes.
     */
    Memory getClassOccupancyInit();

    /**
     * @return Total perm generation or metaspace space at the end of the event (i.e. it reflects any resizing) in
     *         kilobytes.
     */
    Memory getClassSpace();
}
