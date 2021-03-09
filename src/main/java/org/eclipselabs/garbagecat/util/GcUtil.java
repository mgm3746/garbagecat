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
package org.eclipselabs.garbagecat.util;

import static java.util.concurrent.TimeUnit.DAYS;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;

/**
 * Common garbage collection utility methods and constants.
 * 
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public final class GcUtil {

    /**
     * Make default constructor private so the class cannot be instantiated.
     */
    private GcUtil() {
    	super();
    }

    /**
     * Check to see if the entered startdatetime is a valid format.
     * 
     * @param startDateTime
     *            The startdatetime <code>String</code>.
     * @return <code>true</code> if a valid format, <code>false</code> otherwise.
     */
    public static boolean isValidStartDateTime(String startDateTime) {
    	return parseStartDateTime(startDateTime) != null;
    }

    /**
     * Convert startdatetime <code>String</code> to a <code>Date</code>.
     * 
     * @param startDateTime
     *            The startdatetime <code>String</code> in <code>START_DATE_TIME_REGEX</code> format.
     * @return the startdatetime <code>Date</code>.
     */
    public static Date parseStartDateTime(String startDateTime) {
		try {
		    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse(startDateTime);
		} catch (ParseException e) {
			return null;
		}
    }

    /**
     * Convert datestamp <code>String</code> to a <code>Date</code>.
     * 
     * @param datestamp
     *            The datestamp <code>String</code> in <code>JdkRegEx.DATESTAMP</code> format.
     * @return the datestamp in <code>Date</code> format.
     */
    public static Date parseDateStamp(String datestamp) {
    	try {
		    return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZ").parse(datestamp);
		} catch (ParseException e) {
			return null;
		}
    }

    /**
     * Add milliseconds to a given <code>Date</code>.
     * 
     * @param start
     *            Start <code>Date</code>.
     * @param timestamp
     *            Time interval in milliseconds.
     * @return start <code>Date</code> + timestamp.
     */
    public static Date getDatePlusTimestamp(Date start, long timestamp) {
        return new Date(start.getTime() + timestamp);
    }

    /**
     * Calculate the number of whole days (24 hour periods) for a given number of milliseconds
     * 
     * @param timestamp
     *            Time in milliseconds.
     * @return the number of whole days.
     */
    public static int daysInMilliSeconds(long timestamp) {
		return (int) (timestamp / DAYS.toMillis(1));
    }

    /**
     * Determine whether the first JVM event timestamp indicates a partial log file or events that were not in a
     * recognizable format.
     * 
     * @param firstTimestamp
     *            The first JVM event timestamp (milliseconds).
     * @return True if the first timestamp is within the first timestamp threshold, false otherwise.
     */
    public static boolean isPartialLog(long firstTimestamp) {
        return firstTimestamp > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000;
    }

    /**
     * Retrieve the value for a given property file and key.
     * 
     * @param propertyFile
     *            The property file.
     * @param key
     *            The property key.
     * @return The value for the given property file and key.
     */
    public static String getPropertyValue(String propertyFile, String key) {
        return ResourceBundle.getBundle("META-INF." + propertyFile).getString(key);
    }

    /**
     * Calculate the number of milliseconds between two dates.
     * 
     * @param start
     *            Start <code>Date</code>.
     * @param end
     *            End <code>Date</code>.
     * @return The interval between two dates in milliseconds.
     */
    public static long dateDiff(Date start, Date end) {
        return end.getTime() - start.getTime();
    }
}
