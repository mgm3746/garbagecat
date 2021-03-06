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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.cli.CommandLine;
import org.eclipselabs.garbagecat.util.Constants;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class TestMain {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void testShortOptions() throws Exception {
        // Method arguments
        String[] args = new String[] { //
                "-h", //
                "-j", //
                "-Xmx2048m", //
                "-p", //
                "-s", //
                "2009-09-18 00:00:08,172", //
                "-t", //
                "80", //
                "-r", //
                "-o", //
                "12345678.txt", //
                "-v", //
                "-l", //
                // Instead of a file, use a location sure to exist.
                temporaryFolder.getRoot().getAbsolutePath() //
        };
        CommandLine cmd = OptionsParser.parseOptions(args);
        assertNotNull(cmd);
        assertTrue("'-" + Constants.OPTION_HELP_SHORT + "' is a valid option",
                cmd.hasOption(Constants.OPTION_HELP_SHORT));
        assertTrue("'-" + Constants.OPTION_JVMOPTIONS_SHORT + "' is a valid option",
                cmd.hasOption(Constants.OPTION_JVMOPTIONS_SHORT));
        assertTrue("'-" + Constants.OPTION_PREPROCESS_SHORT + "' is a valid option",
                cmd.hasOption(Constants.OPTION_PREPROCESS_SHORT));
        assertTrue("'-" + Constants.OPTION_STARTDATETIME_SHORT + "' is a valid option",
                cmd.hasOption(Constants.OPTION_STARTDATETIME_SHORT));
        assertTrue("'-" + Constants.OPTION_THRESHOLD_SHORT + "' is a valid option",
                cmd.hasOption(Constants.OPTION_THRESHOLD_SHORT));
        assertTrue("'-" + Constants.OPTION_REORDER_SHORT + "' is a valid option",
                cmd.hasOption(Constants.OPTION_REORDER_SHORT));
        assertTrue("'-" + Constants.OPTION_OUTPUT_SHORT + "' is a valid option",
                cmd.hasOption(Constants.OPTION_OUTPUT_SHORT));
        assertTrue("'-" + Constants.OPTION_VERSION_SHORT + "' is a valid option",
                cmd.hasOption(Constants.OPTION_VERSION_SHORT));
        assertTrue("'-" + Constants.OPTION_LATEST_VERSION_SHORT + "' is a valid option",
                cmd.hasOption(Constants.OPTION_LATEST_VERSION_SHORT));
    }

    @Test
    public void testLongOptions() throws Exception {
        // Method arguments
        String[] args = new String[] { //
                "--help", //
                "--jvmoptions", //
                "-Xmx2048m", //
                "--preprocess", //
                "--startdatetime", //
                "2009-09-18 00:00:08,172", //
                "--threshold", //
                "80", //
                "--reorder", //
                "--output", //
                "12345678.txt", //
                "--version", //
                "--latest", //
                // Instead of a file, use a location sure to exist.
                temporaryFolder.getRoot().getAbsolutePath() //
        };
        CommandLine cmd = OptionsParser.parseOptions(args);
        assertNotNull(cmd);
        assertTrue("'-" + Constants.OPTION_HELP_LONG + "' is a valid option",
                cmd.hasOption(Constants.OPTION_HELP_LONG));
        assertTrue("'-" + Constants.OPTION_JVMOPTIONS_LONG + "' is a valid option",
                cmd.hasOption(Constants.OPTION_JVMOPTIONS_LONG));
        assertTrue("'-" + Constants.OPTION_PREPROCESS_LONG + "' is a valid option",
                cmd.hasOption(Constants.OPTION_PREPROCESS_LONG));
        assertTrue("'-" + Constants.OPTION_STARTDATETIME_LONG + "' is a valid option",
                cmd.hasOption(Constants.OPTION_STARTDATETIME_LONG));
        assertTrue("'-" + Constants.OPTION_THRESHOLD_LONG + "' is a valid option",
                cmd.hasOption(Constants.OPTION_THRESHOLD_LONG));
        assertTrue("'-" + Constants.OPTION_REORDER_LONG + "' is a valid option",
                cmd.hasOption(Constants.OPTION_REORDER_LONG));
        assertTrue("'-" + Constants.OPTION_OUTPUT_LONG + "' is a valid option",
                cmd.hasOption(Constants.OPTION_OUTPUT_LONG));
        assertTrue("'-" + Constants.OPTION_VERSION_LONG + "' is a valid option",
                cmd.hasOption(Constants.OPTION_VERSION_LONG));
        assertTrue("'-" + Constants.OPTION_LATEST_VERSION_LONG + "' is a valid option",
                cmd.hasOption(Constants.OPTION_LATEST_VERSION_LONG));
    }

    @Test
    public void testShortHelpOption() throws Exception {
        // Method arguments
        String[] args = new String[] { "-h" };
        CommandLine cmd = OptionsParser.parseOptions(args);
        // CommandLine will be null if only the help option is passed in.
        assertNull(cmd);
        assertTrue("'-h' is a valid option", true);
    }

    @Test
    public void testLongHelpOption() throws Exception {
        // Method arguments
        String[] args = new String[] { "--help" };
        // Pass null object since parseOptions is static
        CommandLine cmd = OptionsParser.parseOptions(args);
        // CommandLine will be null if only the help option is passed in.
        assertNull(cmd);
        assertTrue("'--help' is a valid option", true);
    }

}
