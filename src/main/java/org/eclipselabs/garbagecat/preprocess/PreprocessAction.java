/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2016 Red Hat, Inc.                                                                              *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Red Hat, Inc. - initial API and implementation                                                                  *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.preprocess;

/**
 * Base preprocessing action.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public interface PreprocessAction {

    /**
     * Indicates the current log entry is either the beginning of an event that spans multiple logging lines, or it is a
     * single line logging event.
     */
    public static final String TOKEN_BEGINNING_OF_EVENT = "TOKEN_BEGINNING_OF_EVENT";

    /**
     * @return The log entry for the action.
     */
    String getLogEntry();

    /**
     * @return The action identifier.
     */
    String getName();
}
