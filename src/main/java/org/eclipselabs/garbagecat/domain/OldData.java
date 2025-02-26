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

import org.eclipselabs.garbagecat.util.Memory;

/**
 * Old generation data.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface OldData {

    /**
     * @return Old generation end occupancy.
     */
    Memory getOldOccupancyEnd();

    /**
     * @return Old generation initial occupancy.
     */
    Memory getOldOccupancyInit();

    /**
     * @return Total old generation space at the end of the event (i.e. it reflects any resizing).
     */
    Memory getOldSpace();

    /**
     * @return Young generation end occupancy.
     */
    Memory getYoungOccupancyEnd();

    /**
     * @return Young generation initial occupancy.
     */
    Memory getYoungOccupancyInit();

    /**
     * @return Total young generation space at the end of the event (i.e. it reflects any resizing). Equals young
     *         generation allocation minus one survivor space.
     */
    Memory getYoungSpace();
}
