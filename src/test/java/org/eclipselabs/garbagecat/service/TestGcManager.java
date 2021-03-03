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
package org.eclipselabs.garbagecat.service;

import org.junit.Test;

import static org.junit.Assert.fail;

import java.io.File;

import org.eclipselabs.garbagecat.util.Constants;



/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestGcManager {

    /**
     * Test for NullPointerException caused by Issue 17:
     * http://code.google.com/a/eclipselabs.org/p/garbagecat/issues/detail?id=17
     */
    @Test
    public void testNullPointerExceptionNotRaised() {
        File testFile = new File(Constants.TEST_DATA_DIR + "dataset31.txt");
        new GcManager().preprocess(testFile, null);
    }
}
