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
package org.eclipselabs.garbagecat;

import java.io.File;
import java.net.URISyntaxException;

/**
 * @author <a href="https://github.com/pfichtner">Peter Fichtner</a>
 */
public final class TestUtil {

    private TestUtil() {
        super();
    }

    public static File getFile(String name) {
        try {
            return new File(TestUtil.class.getClassLoader().getResource("data/" + name).toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
