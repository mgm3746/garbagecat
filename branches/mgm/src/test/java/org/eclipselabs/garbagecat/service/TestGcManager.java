/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.service;

import java.io.File;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestGcManager extends TestCase {

    /**
     * Test for NullPointerException caused by Issue 17:
     * http://code.google.com/a/eclipselabs.org/p/garbagecat/issues/detail?id=17
     */
    public void testNullPointerExceptionNotRaised() {
        // TODO: Create File in platform independent way.
        File testFile = new File("src/test/data/dataset31.txt");
        GcManager jvmManager = new GcManager();
        try {
            jvmManager.preprocess(testFile, null);
        } catch (NullPointerException e) {
            Assert.fail("Preprocessing results in NullPointerException.");
        }
    }
}
