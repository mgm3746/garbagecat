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
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.eclipselabs.garbagecat.TestUtil.parseDate;
import static org.eclipselabs.garbagecat.util.GcUtil.dateDiff;
import static org.eclipselabs.garbagecat.util.GcUtil.daysInMilliSeconds;
import static org.eclipselabs.garbagecat.util.GcUtil.getDatePlusTimestamp;
import static org.eclipselabs.garbagecat.util.GcUtil.getPropertyValue;
import static org.eclipselabs.garbagecat.util.GcUtil.isPartialLog;
import static org.eclipselabs.garbagecat.util.GcUtil.isValidStartDateTime;
import static org.eclipselabs.garbagecat.util.GcUtil.parseStartDateTime;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_HEAP_MIN_NOT_EQUAL_MAX;
import static org.eclipselabs.garbagecat.util.jdk.Analysis.WARN_THREAD_STACK_SIZE_NOT_SET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
class TestGcUtil {
    
    @Test
    void testStartDateTime() {
    	String startDateTime = "2009-09-18 00:00:08.172";
    	assertTrue(isValidStartDateTime(startDateTime), "Start date/time not recognized as a valid format.");
    }
    
    @Test
    void testConvertStartDateTimeStringToDate() {
    	String startDateTime = "2009-09-18 16:24:08.172";
    	Date date = parseStartDateTime(startDateTime);
    	assertEquals(parseDate("2009-09-18", "16:24:08.172"), date);
    }
    
    @Test
    void testNumberOfDaysInZeroMilliSeconds() {
    	long milliSeconds = 0;
    	assertEquals(0, daysInMilliSeconds(milliSeconds), "Number of days calculated wrong.");
    }
    
    @Test
    void testNumberOfDaysInMilliSecondsLessThanOneDay() {
    	long milliSeconds = DAYS.toMillis(1) - 1;
    	assertEquals(0, daysInMilliSeconds(milliSeconds), "Number of days calculated wrong.");
    }
    
    @Test
    void testNumberOfDaysInMilliSecondsEqualOneDay() {
    	long milliSeconds = DAYS.toMillis(1);
    	assertEquals(1, daysInMilliSeconds(milliSeconds), "Number of days calculated wrong.");
    }
    
    @Test
    void testNumberOfDaysInMilliSeconds9Days() {
    	long milliSeconds = DAYS.toMillis(10) - 1;
    	assertEquals(9, daysInMilliSeconds(milliSeconds), "Number of days calculated wrong.");
    }
    
    @Test
    void testAddingDateAndTimestampZero() {
    	Date date = parseDate("1966-08-18", "19:21:44.012");
    	long timestamp = 0L;
    	assertEquals(parseDate("1966-08-18", "19:21:44.012"), getDatePlusTimestamp(date, timestamp));
    }
    
    @Test
    void testAddingDateAndTimestamp10Ms() {
    	Date date = parseDate("1966-08-18", "19:21:44.012");
    	long timestamp = MILLISECONDS.toMillis(10);
    	assertEquals(parseDate("1966-08-18", "19:21:44.022"), getDatePlusTimestamp(date, timestamp));
    }
    
    @Test
    void testAddingDateAndTimestamp1Sec() {
    	Date date = parseDate("1966-08-18", "19:21:44.012");
    	long timestamp = SECONDS.toMillis(1);
    	assertEquals(parseDate("1966-08-18", "19:21:45.012"), getDatePlusTimestamp(date, timestamp));
    }
    
    @Test
    void testAddingDateAndTimestamp1Min() {
    	Date date = parseDate("1966-08-18", "19:21:44.012");
    	long timestamp = MINUTES.toMillis(1);
    	assertEquals(parseDate("1966-08-18", "19:22:44.012"), getDatePlusTimestamp(date, timestamp));
    }
    
    @Test
    void testAddingDateAndTimestamp1Hr() {
    	Date date = parseDate("1966-08-18", "19:21:44.012");
    	long timestamp = HOURS.toMillis(1);
    	assertEquals(parseDate("1966-08-18", "20:21:44.012"), getDatePlusTimestamp(date, timestamp));
    }
    
    @Test
    void testAddingDateAndTimestamp1Day() {
    	Date date = parseDate("1966-08-18", "19:21:44.012");
    	long timestamp = DAYS.toMillis(1);
    	assertEquals(parseDate("1966-08-19", "19:21:44.012"), getDatePlusTimestamp(date, timestamp));
    }
    
    @Test
    void testAddingDateAndTimestamp30Days() {
    	Date date = parseDate("1966-08-18", "19:21:44.012");
    	long timestamp = DAYS.toMillis(30);
    	assertEquals(parseDate("1966-09-17", "19:21:44.012"), getDatePlusTimestamp(date, timestamp));
    }
    
    @Test
    void testAddingDateWith2DigitMonth() {
    	String jvmStarted = "2009-11-01 02:30:52.917";
    	long gcLogTimestamp = 353647157L;
    	assertEquals(parseDate("2009-11-05", "04:45:00.074"),
    			getDatePlusTimestamp(parseStartDateTime(jvmStarted), gcLogTimestamp));
    }
    
    @Test
    void testGetPropertyValues() {
    	assertNotNull("Could not retrieve " + WARN_THREAD_STACK_SIZE_NOT_SET.getKey() + ".",
    			getPropertyValue("analysis", WARN_THREAD_STACK_SIZE_NOT_SET.getKey()));
    	assertNotNull("Could not retrieve " + WARN_HEAP_MIN_NOT_EQUAL_MAX.getKey() + ".",
    			getPropertyValue("analysis", WARN_HEAP_MIN_NOT_EQUAL_MAX.getKey()));
    }
    
    @Test
    void testConvertDateStampStringToDate() {
    	Date date = GcUtil.parseDateStamp("2010-02-26T09:32:12.486-0600");
    	assertEquals(parseDate("2010-02-26", "09:32:12.486"), date);
    }
    
    @Test
    void testDateDiff() {
    	Date start = parseDate("2010-02-26", "00:00:00.000");
    	Date end = parseDate("2010-02-27", "01:01:01.001");
    	assertEquals(90061001L, dateDiff(start, end));
    }
    
    @Test
    void testPartialLog() {
    	assertFalse(isPartialLog(59999), "Not a partial log.");
    	assertTrue(isPartialLog(60001), "Is a partial log.");
    }
}
