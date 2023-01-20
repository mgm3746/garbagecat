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

import org.github.joa.domain.GarbageCollector;

/**
 * G1 collector.
 * 
 * There are 3 main phases:
 * 
 * 1) One or more {@code org.eclipselabs.garbagecat.domain.jdk.G1YoungPauseEvent} collections.
 * 
 * 2) Marking cycle (triggered when <code>InitiatingHeapOccupancyPercent</code> reached).
 * 
 * 3) One or more {@code org.eclipselabs.garbagecat.domain.jdk.G1MixedPauseEvent} until the amount of space that can be
 * reclaimed in the Collection Set (CSet) is less than <code>G1HeapWastePercent</code> (default 5%).
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class G1Collector extends GcEvent {

    @Override
    public GarbageCollector getGarbageCollector() {
        return GarbageCollector.G1;
    }
}