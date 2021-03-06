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
package org.eclipselabs.garbagecat.preprocess.jdk;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.jdk.JdkUtil;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestApplicationStoppedTimePreprocessAction {

    @Test
    public void testLine2CmsConcurrent() {
        String priorLogLine = "6545.692Total time for which application threads were stopped: 0.0007993 seconds";
        String logLine = ": [CMS-concurrent-abortable-preclean: 0.025/0.042 secs] "
                + "[Times: user=0.04 sys=0.00, real=0.04 secs]";
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, priorLogLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
    }

    @Test
    public void testLine2TimesBlock() {
        String priorLogLine = "234784.781: [CMS-concurrent-abortable-preclean: 0.038/0.118 secs]Total time for"
                + " which application threads were stopped: 0.0123330 seconds";
        String logLine = " [Times: user=0.10 sys=0.00, real=0.12 secs]";
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, priorLogLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
    }

    @Test
    public void testLine2TimesBlockWhitespaceAtEnd() {
        String priorLogLine = "234784.781: [CMS-concurrent-abortable-preclean: 0.038/0.118 secs]Total time for"
                + " which application threads were stopped: 0.0123330 seconds";
        String logLine = " [Times: user=0.10 sys=0.00, real=0.12 secs]   ";
        assertTrue(ApplicationStoppedTimePreprocessAction.match(logLine, priorLogLine), "Log line not recognized as " + JdkUtil.PreprocessActionType.APPLICATION_STOPPED_TIME.toString() + ".");
    }
}
