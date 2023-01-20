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
package org.eclipselabs.garbagecat.domain.jdk.unified;

import org.eclipselabs.garbagecat.util.jdk.unified.UnifiedSafepoint.Trigger;

/**
 * <code>SafepointEvent</code> summary used for reporting
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class SafepointEventSummary {

    /**
     * Total number of events.
     */
    private long count;

    /**
     * Max pause time (milliseconds).
     */
    private int pauseMax;

    /**
     * Total pause time (milliseconds).
     */
    private long pauseTotal;

    /**
     * The <code>Trigger</code>
     */
    private Trigger trigger;

    /**
     * Default constructor.
     * 
     * @param trigger
     *            The <code>Trigger</code>.
     * @param count
     *            Number of events.
     * @param pauseTotal
     *            Total pause time of events
     * @param pauseMax
     *            Max pause time of events
     */
    public SafepointEventSummary(Trigger trigger, long count, long pauseTotal, int pauseMax) {
        this.trigger = trigger;
        this.count = count;
        this.pauseTotal = pauseTotal;
        this.pauseMax = pauseMax;
    }

    public long getCount() {
        return count;
    }

    public long getPauseMax() {
        return pauseMax;
    }

    public long getPauseTotal() {
        return pauseTotal;
    }

    public Trigger getTrigger() {
        return trigger;
    }
}
