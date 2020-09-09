/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
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
 * Old generation data.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface OldData {

    /**
     * @return Young generation initial occupancy (kilobytes).
     */
    int getYoungOccupancyInit();

    /**
     * @return Total young generation space (kilobytes) at the end of the event (i.e. it reflects any resizing). Equals
     *         young generation allocation minus one survivor space.
     */
    int getYoungSpace();

    /**
     * @return Young generation end occupancy (kilobytes).
     */
    int getYoungOccupancyEnd();

    /**
     * @return Old generation initial occupancy (kilobytes).
     */
    int getOldOccupancyInit();

    /**
     * @return Total old generation space (kilobytes) at the end of the event (i.e. it reflects any resizing).
     */
    int getOldSpace();

    /**
     * @return Old generation end occupancy (kilobytes).
     */
    int getOldOccupancyEnd();
}
