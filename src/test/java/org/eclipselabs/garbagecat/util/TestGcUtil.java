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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipselabs.garbagecat.util.jdk.Analysis;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestGcUtil {

    @Test
    void testStartDateTime() {
        String startDateTime = "2009-09-18 00:00:08,172";
        assertTrue(GcUtil.isValidStartDateTime(startDateTime), "Start date/time not recognized as a valid format.");
    }

    @Test
    void testInvalidStartDateTime() {
        // Replace comma with space
        String startDateTime = "2009-09-18 00:00:08 172";
        assertFalse(GcUtil.isValidStartDateTime(startDateTime), "Start date/time recognized as a valid format.");
    }

    @Test
    void testConvertStartDateTimeStringToDate() {
        String startDateTime = "2009-09-18 16:24:08,172";
        Date date = GcUtil.parseStartDateTime(startDateTime);
        assertNotNull(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertEquals(2009,calendar.get(Calendar.YEAR),"Start year not parsed correctly.");
        assertEquals(8,calendar.get(Calendar.MONTH),"Start month not parsed correctly.");
        assertEquals(18,calendar.get(Calendar.DAY_OF_MONTH),"Start day not parsed correctly.");
        assertEquals(16,calendar.get(Calendar.HOUR_OF_DAY),"Start hour not parsed correctly.");
        assertEquals(24,calendar.get(Calendar.MINUTE),"Start minute not parsed correctly.");
        assertEquals(8,calendar.get(Calendar.SECOND),"Start second not parsed correctly.");
        assertEquals(172,calendar.get(Calendar.MILLISECOND),"Start millisecond not parsed correctly.");
    }

    @Test
    void testNumberOfDaysInZeroMilliSeconds() {
        long milliSeconds = 0;
        assertEquals(0,GcUtil.daysInMilliSeconds(milliSeconds),"Number of days calculated wrong.");
    }

    @Test
    void testNumberOfDaysInMilliSecondsLessThanOneDay() {
        long milliSeconds = 82800000L;
        assertEquals(0,GcUtil.daysInMilliSeconds(milliSeconds),"Number of days calculated wrong.");
    }

    @Test
    void testNumberOfDaysInMilliSecondsEqualOneDay() {
        long milliSeconds = 86400000L;
        assertEquals(1,GcUtil.daysInMilliSeconds(milliSeconds),"Number of days calculated wrong.");
    }

    @Test
    void testNumberOfDaysInMilliSeconds9Days() {
        long milliSeconds = 863999999L;
        assertEquals(9,GcUtil.daysInMilliSeconds(milliSeconds),"Number of days calculated wrong.");
    }

    @Test
    void testAddingDateAndTimestampZero() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 0L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        assertEquals("1966-08-18 19:21:44,012",formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)),"Date calculated wrong.");
    }

    @Test
    void testAddingDateAndTimestamp10Ms() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 10L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        assertEquals("1966-08-18 19:21:44,022",formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)),"Date calculated wrong.");
    }

    @Test
    void testAddingDateAndTimestamp1Sec() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 1000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        assertEquals("1966-08-18 19:21:45,012",formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)),"Date calculated wrong.");
    }

    @Test
    void testAddingDateAndTimestamp1Min() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 60000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        assertEquals("1966-08-18 19:22:44,012",formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)),"Date calculated wrong.");
    }

    @Test
    void testAddingDateAndTimestamp1Hr() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 3600000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        assertEquals("1966-08-18 20:21:44,012",formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)),"Date calculated wrong.");
    }

    @Test
    void testAddingDateAndTimestamp1Day() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 86400000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        assertEquals("1966-08-19 19:21:44,012",formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)),"Date calculated wrong.");
    }

    @Test
    void testAddingDateAndTimestamp30Days() {
        // 1966-08-18 19:21:44,012
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1966);
        calendar.set(Calendar.MONTH, Calendar.AUGUST);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 21);
        calendar.set(Calendar.SECOND, 44);
        calendar.set(Calendar.MILLISECOND, 12);
        long timestamp = 2592000000L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        assertEquals("1966-09-17 19:21:44,012",formatter.format(GcUtil.getDatePlusTimestamp(calendar.getTime(), timestamp)),"Date calculated wrong.");
    }

    @Test
    void testAddingDateWith2DigitMonth() {
        String jvmStarted = "2009-11-01 02:30:52,917";
        long gcLogTimestamp = 353647157L;
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS");
        assertEquals("2009-11-05 04:45:00,074",formatter.format(GcUtil.getDatePlusTimestamp(GcUtil.parseStartDateTime(jvmStarted), gcLogTimestamp)),"Date calculated wrong.");
    }

    @Test
    void testGetPropertyValues() {
        assertNotNull("Could not retrieve " + Analysis.WARN_THREAD_STACK_SIZE_NOT_SET.getKey() + ".",
                GcUtil.getPropertyValue("analysis", Analysis.WARN_THREAD_STACK_SIZE_NOT_SET.getKey()));
        assertNotNull("Could not retrieve " + Analysis.WARN_HEAP_MIN_NOT_EQUAL_MAX.getKey() + ".",
                GcUtil.getPropertyValue("analysis", Analysis.WARN_HEAP_MIN_NOT_EQUAL_MAX.getKey()));
    }

    @Test
    void testConvertDateStampStringToDate() {
        String datestamp = "2010-02-26T09:32:12.486-0600";
        Date date = GcUtil.parseDateStamp(datestamp);
        assertNotNull(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        assertEquals(2010,calendar.get(Calendar.YEAR),"Datestamp year not parsed correctly.");
        assertEquals(1,calendar.get(Calendar.MONTH),"Datestamp month not parsed correctly.");
        assertEquals(26,calendar.get(Calendar.DAY_OF_MONTH),"Datestamp day not parsed correctly.");
        assertEquals(9,calendar.get(Calendar.HOUR_OF_DAY),"Datestamp hour not parsed correctly.");
        assertEquals(32,calendar.get(Calendar.MINUTE),"Datestamp minute not parsed correctly.");
        assertEquals(12,calendar.get(Calendar.SECOND),"Datestamp second not parsed correctly.");
        assertEquals(486,calendar.get(Calendar.MILLISECOND),"Datestamp millisecond not parsed correctly.");
    }

    @Test
    void testDateDiff() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2010);
        calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
        calendar.set(Calendar.DAY_OF_MONTH, 26);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date start = calendar.getTime();

        calendar.set(Calendar.DAY_OF_MONTH, 27);
        calendar.set(Calendar.HOUR_OF_DAY, 1);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 1);
        calendar.set(Calendar.MILLISECOND, 1);
        Date end = calendar.getTime();

        assertEquals(90061001L,GcUtil.dateDiff(start, end),"Date difference incorrect.");
    }

    @Test
    void testPartialLog() {
        assertFalse(GcUtil.isPartialLog(59999), "Not a partial log.");
        assertTrue(GcUtil.isPartialLog(60001), "Is a partial log.");
    }
}
