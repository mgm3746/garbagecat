/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2022 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat;

import static org.eclipselabs.garbagecat.util.Constants.OPTION_HELP_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_HELP_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_JVMOPTIONS_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_LATEST_VERSION_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_LATEST_VERSION_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_OUTPUT_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_PREPROCESS_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_PREPROCESS_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REORDER_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_REORDER_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_STARTDATETIME_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_THRESHOLD_SHORT;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_VERSION_LONG;
import static org.eclipselabs.garbagecat.util.Constants.OPTION_VERSION_SHORT;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.apache.commons.cli.CommandLine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TestMain {

    private static void assertHasOption(CommandLine cmd, String option) {
        assertTrue(cmd.hasOption(option), "'-" + option + "' is a valid option");
    }

    @Test
    void testLongHelpOption() throws Exception {
        // Method arguments
        String[] args = new String[] { "--help" };
        // Pass null object since parseOptions is static
        CommandLine cmd = OptionsParser.parseOptions(args);
        // CommandLine will be null if only the help option is passed in.
        assertNull(cmd);
    }

    @Test
    void testLongOptions(@TempDir File tmpFolder) throws Exception {
        // Method arguments
        String[] args = new String[] { //
                "--help", //
                "--jvmoptions", //
                "-Xmx2048m", //
                "--preprocess", //
                "--startdatetime", //
                "2009-09-18 00:00:08.172", //
                "--threshold", //
                "80", //
                "--reorder", //
                "--output", //
                "12345678.txt", //
                "--version", //
                "--latest", //
                // Instead of a file, use a location sure to exist.
                tmpFolder.getAbsolutePath() //
        };
        CommandLine cmd = OptionsParser.parseOptions(args);
        assertNotNull(cmd);
        assertHasOption(cmd, OPTION_HELP_LONG);
        assertHasOption(cmd, OPTION_JVMOPTIONS_LONG);
        assertHasOption(cmd, OPTION_PREPROCESS_LONG);
        assertHasOption(cmd, OPTION_STARTDATETIME_LONG);
        assertHasOption(cmd, OPTION_THRESHOLD_LONG);
        assertHasOption(cmd, OPTION_REORDER_LONG);
        assertHasOption(cmd, OPTION_OUTPUT_LONG);
        assertHasOption(cmd, OPTION_VERSION_LONG);
        assertHasOption(cmd, OPTION_LATEST_VERSION_LONG);
    }

    @Test
    void testShortHelpOption() throws Exception {
        // Method arguments
        String[] args = new String[] { "-h" };
        CommandLine cmd = OptionsParser.parseOptions(args);
        // CommandLine will be null if only the help option is passed in.
        assertNull(cmd);
    }

    @Test
    void testShortOptions(@TempDir File temporaryFolder) throws Exception {
        // Method arguments
        String[] args = new String[] { //
                "-h", //
                "-j", //
                "-Xmx2048m", //
                "-p", //
                "-s", //
                "2009-09-18 00:00:08.172", //
                "-t", //
                "80", //
                "-r", //
                "-o", //
                "12345678.txt", //
                "-v", //
                "-l", //
                // Instead of a file, use a location sure to exist.
                temporaryFolder.getAbsolutePath() //
        };
        CommandLine cmd = OptionsParser.parseOptions(args);
        assertNotNull(cmd);
        assertHasOption(cmd, OPTION_HELP_SHORT);
        assertHasOption(cmd, OPTION_JVMOPTIONS_SHORT);
        assertHasOption(cmd, OPTION_PREPROCESS_SHORT);
        assertHasOption(cmd, OPTION_STARTDATETIME_SHORT);
        assertHasOption(cmd, OPTION_THRESHOLD_SHORT);
        assertHasOption(cmd, OPTION_REORDER_SHORT);
        assertHasOption(cmd, OPTION_OUTPUT_SHORT);
        assertHasOption(cmd, OPTION_VERSION_SHORT);
        assertHasOption(cmd, OPTION_LATEST_VERSION_SHORT);
        assertHasOption(cmd, OPTION_LATEST_VERSION_SHORT);
    }

}
