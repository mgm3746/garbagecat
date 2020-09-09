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
package org.eclipselabs.garbagecat.domain.jdk;

/**
 * CMS collector with incremental mode capability.
 * 
 * Enabled with the <code>-XX:+CMSIncrementalMode</code> JVM option.
 * 
 * In this mode, the CMS collector does not hold the processor(s) for the entire long concurrent phases but periodically
 * stops them and yields the processor back to other threads in the application. It divides the work to be done in
 * concurrent phases into small chunks called duty cycles and schedules them between minor collections. This is very
 * useful for applications that need low pause times and are run on machines with a small number of processors.
 * 
 * The icms_dc value is the time in percentage that the concurrent work took between two young generation collections.
 * 
 * For example: below the icms_dc=20 means that 20% of the time between two young generation collections was for a
 * concurrent duty cycle:
 * 
 * .20 (91.976 - 85.725) = 1.250 seconds
 * 
 * 85.725: [GC85.726: [ParNew: 4934405K-&gt;939526K(8388608K), 0.5656110 secs] 5854991K-&gt;2237120K(22020096K)
 * icms_dc=5 , 0.5657690 secs] [Times: user=2.03 sys=0.00, real=0.56 secs]
 * 
 * 91.976: [GC91.976: [ParNew: 8279558K-&gt;1048576K(8388608K), 1.1009830 secs] 9577056K-&gt;3192351K(22020096K)
 * icms_dc=20 , 1.1011330 secs] [Times: user=2.68 sys=0.01, real=1.11 secs]
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class CmsIncrementalModeCollector extends CmsCollector {

    /**
     * Whether or not the collector is running in Incremental Mode.
     */
    private boolean incrementalMode = false;

    /**
     * @return True if running in Incremental Mode, false otherwise.
     */
    public boolean isIncrementalMode() {
        return incrementalMode;
    }

    /**
     * @param incrementalMode
     *            Flag indicating if collector is running in Incremental Mode.
     */
    public void setIncrementalMode(boolean incrementalMode) {
        this.incrementalMode = incrementalMode;
    }

}