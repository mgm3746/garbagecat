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
package org.eclipselabs.garbagecat.util;

import static java.util.concurrent.TimeUnit.DAYS;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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
     * Arbitrary date for determining time intervals when gc logging with a datestamp only (i.e. JVM start date
     * unknown).
     */
    public static final Date JVM_START_DATE = parseDateStamp("2000-01-01T00:00:00.000-0500");

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

    /**
     * @param start
     *            Start date.
     * @param end
     *            End date.
     * @return The number of days between 2 dates.
     */
    public static final int dayDiff(Date start, Date end) {
        long millisDiff = millisDiff(start, end);
        return daysInMilliSeconds(millisDiff);
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
     * Convert date parts to a <code>Date</code>.
     * 
     * @param MMM
     *            The month.
     * @param d
     *            The day.
     * @param yyyy
     *            The year.
     * @param HH
     *            The hour.
     * @param mm
     *            The minute.
     * @param ss
     *            The seconds.
     * @return The date part strings converted to a <code>Date</code>
     */
    public static final Date getDate(String MMM, String d, String yyyy, String HH, String mm, String ss) {
        if (MMM == null || d == null || yyyy == null || HH == null || mm == null || ss == null) {
            throw new IllegalArgumentException("One or more date parts are missing.");
        }

        Calendar calendar = Calendar.getInstance();
        // Java Calendar month is 0 based
        switch (MMM) {
        case "Jan":
            calendar.set(Calendar.MONTH, 0);
            break;
        case "Feb":
            calendar.set(Calendar.MONTH, 1);
            break;
        case "Mar":
            calendar.set(Calendar.MONTH, 2);
            break;
        case "Apr":
            calendar.set(Calendar.MONTH, 3);
            break;
        case "May":
            calendar.set(Calendar.MONTH, 4);
            break;
        case "Jun":
            calendar.set(Calendar.MONTH, 5);
            break;
        case "Jul":
            calendar.set(Calendar.MONTH, 6);
            break;
        case "Aug":
            calendar.set(Calendar.MONTH, 7);
            break;
        case "Sep":
            calendar.set(Calendar.MONTH, 8);
            break;
        case "Oct":
            calendar.set(Calendar.MONTH, 9);
            break;
        case "Nov":
            calendar.set(Calendar.MONTH, 10);
            break;
        case "Dec":
            calendar.set(Calendar.MONTH, 11);
            break;
        default:
            throw new IllegalArgumentException("Unexpected month: " + MMM);
        }
        calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(d).intValue());
        calendar.set(Calendar.YEAR, Integer.valueOf(yyyy));
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(HH).intValue());
        calendar.set(Calendar.MINUTE, Integer.valueOf(mm).intValue());
        calendar.set(Calendar.SECOND, Integer.valueOf(ss).intValue());
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * Subtract milliseconds from a given <code>Date</code>.
     * 
     * @param start
     *            Start <code>Date</code>.
     * @param timestamp
     *            Time interval in milliseconds.
     * @return start <code>Date</code> - timestamp.
     */
    public static Date getDateMinusTimestamp(Date start, long timestamp) {
        return new Date(start.getTime() - timestamp);
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
     * Determine whether the first JVM event timestamp indicates a partial log file.
     * 
     * @param firstTimestamp
     *            The first JVM event timestamp (milliseconds).
     * @return True if the first timestamp is within the first timestamp threshold, false otherwise.
     */
    public static boolean isPartialLog(long firstTimestamp) {
        return firstTimestamp > Constants.FIRST_TIMESTAMP_THRESHOLD * 1000;
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
     * Calculate the number of milliseconds between two dates.
     * 
     * @param start
     *            Start <code>Date</code>.
     * @param end
     *            End <code>Date</code>.
     * @return The interval between two dates in milliseconds.
     */
    private static final long millisDiff(Date start, Date end) {
        long millisDiff = 0;
        if (start != null && end != null) {
            millisDiff = end.getTime() - start.getTime();
        }
        return millisDiff;
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
     * Convert datetime <code>String</code> to a <code>Date</code>.
     * 
     * @param datetime
     *            The datetime <code>String</code> in <code>JdkRegEx.DATETIME</code> format.
     * @return the datetime in <code>Date</code> format.
     */
    public static Date parseDatetime(String datetime) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(datetime);
        } catch (ParseException e) {
            return null;
        }
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
     * Make default constructor private so the class cannot be instantiated.
     */
    private GcUtil() {
        super();
    }
}
