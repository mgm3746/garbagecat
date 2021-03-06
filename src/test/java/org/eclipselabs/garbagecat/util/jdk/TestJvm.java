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
package org.eclipselabs.garbagecat.util.jdk;

import static org.eclipselabs.garbagecat.util.Memory.bytes;
import static org.eclipselabs.garbagecat.util.Memory.kilobytes;
import static org.eclipselabs.garbagecat.util.Memory.megabytes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipselabs.garbagecat.util.Memory;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJvm {

    @Test
    public void testNullJvmOptions() {
        String jvmOptions = null;
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull(jvm,"Jvm object creation failed.");
    }

    @Test
    public void testGetThreadStackSizeSsSmallK() {
        String jvmOptions = "-ss128k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-ss128k",jvm.getThreadStackSizeOption(),"Thread stack size not populated correctly.");
        assertEquals("128k",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeSsBigK() {
        String jvmOptions = "-ss128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-ss128K",jvm.getThreadStackSizeOption(),"Thread stack size not populated correctly.");
        assertEquals("128K",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeSsSmallM() {
        String jvmOptions = "-ss1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-ss1m",jvm.getThreadStackSizeOption(),"Thread stack size not populated correctly.");
        assertEquals("1m",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeXssSmallK() {
        String jvmOptions = "-Xss128k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xss128k",jvm.getThreadStackSizeOption(),"Thread stack size not populated correctly.");
        assertEquals("128k",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeXssBigK() {
        String jvmOptions = "-Xss128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xss128K",jvm.getThreadStackSizeOption(),"Thread stack size not populated correctly.");
        assertEquals("128K",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeXssSmallM() {
        String jvmOptions = "-Xss1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xss1m",jvm.getThreadStackSizeOption(),"Thread stack size not populated correctly.");
        assertEquals("1m",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeXssBigM() {
        String jvmOptions = "-Xss1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xss1M",jvm.getThreadStackSizeOption(),"Thread stack size not populated correctly.");
        assertEquals("1M",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeSmallK() {
        String jvmOptions = "-XX:ThreadStackSize=128k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:ThreadStackSize=128k",jvm.getThreadStackSizeOption(),"Thread stack size incorrect.");
        assertEquals("128k",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeBigK() {
        String jvmOptions = "-XX:ThreadStackSize=128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:ThreadStackSize=128K",jvm.getThreadStackSizeOption(),"Thread stack size incorrect.");
        assertEquals("128K",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeSmallM() {
        String jvmOptions = "-XX:ThreadStackSize=1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:ThreadStackSize=1m",jvm.getThreadStackSizeOption(),"Thread stack size incorrect.");
        assertEquals("1m",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeBigM() {
        String jvmOptions = "-XX:ThreadStackSize=1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:ThreadStackSize=1M",jvm.getThreadStackSizeOption(),"Thread stack size incorrect.");
        assertEquals("1M",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1234567 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:ThreadStackSize=1234567",jvm.getThreadStackSizeOption(),"Thread stack size incorrect.");
        assertEquals("1234567",jvm.getThreadStackSizeValue(),"Thread stack size value incorrect.");
    }

    @Test
    public void testGetThreadStackSizeOneLessThanLargeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048575 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertFalse(jvm.hasLargeThreadStackSize(), "Thread stack size is not large.");
    }

    @Test
    public void testGetThreadStackSizeEqualsLargeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048576 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertTrue(jvm.hasLargeThreadStackSize(), "Thread stack size is large.");
    }

    @Test
    public void testGetThreadStackSizeOneGreaterLargeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048577 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertTrue(jvm.hasLargeThreadStackSize(), "Thread stack size is large.");
    }

    @Test
    public void testGetThreadStackSizeOneLessThanLargeByteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048575b -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertFalse(jvm.hasLargeThreadStackSize(), "Thread stack size is not large.");
    }

    @Test
    public void testGetThreadStackSizeEqualsLargeByteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048576B -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertTrue(jvm.hasLargeThreadStackSize(), "Thread stack size is large.");
    }

    @Test
    public void testGetThreadStackSizeOneGreaterLargeByteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048577B -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertTrue(jvm.hasLargeThreadStackSize(), "Thread stack size is large.");
    }

    @Test
    public void testGetThreadStackSizeOneLessThanLargeKilobyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1023k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertFalse(jvm.hasLargeThreadStackSize(), "Thread stack size is not large.");
    }

    @Test
    public void testGetThreadStackSizeEqualsLargeKilobyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1024K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertTrue(jvm.hasLargeThreadStackSize(), "Thread stack size is large.");
    }

    @Test
    public void testGetThreadStackSizeOneGreaterLargeKilobyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1025K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertTrue(jvm.hasLargeThreadStackSize(), "Thread stack size is large.");
    }

    @Test
    public void testGetThreadStackSizeEqualsLargeMegabyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertTrue(jvm.hasLargeThreadStackSize(), "Thread stack size is large.");
    }

    @Test
    public void testGetThreadStackSizeOneGreaterLargeMegabyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertTrue(jvm.hasLargeThreadStackSize(), "Thread stack size is large.");
    }

    @Test
    public void testGetThreadStackSizeLargeGigabyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1G -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertTrue(jvm.hasLargeThreadStackSize(), "Thread stack size is large.");
    }

    @Test
    public void testGetMinHeapSmallM() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xms1024m",jvm.getMinHeapOption(),"Min heap option incorrect.");
        assertEquals("1024m",jvm.getMinHeapValue(),"Min heap value incorrect.");
    }

    @Test
    public void testGetMinHeapBigM() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1024M -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xms1024M",jvm.getMinHeapOption(),"Min heap option incorrect.");
        assertEquals("1024M",jvm.getMinHeapValue(),"Min heap value incorrect.");
    }

    @Test
    public void testGetMinHeapSmallG() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1g -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xms1g",jvm.getMinHeapOption(),"Min heap option incorrect.");
        assertEquals("1g",jvm.getMinHeapValue(),"Min heap value incorrect.");
    }

    @Test
    public void testGetMinHeapBigG() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1G -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xms1G",jvm.getMinHeapOption(),"Min heap option incorrect.");
        assertEquals("1G",jvm.getMinHeapValue(),"Min heap value incorrect.");
    }

    @Test
    public void testGetMaxHeapSmallM() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xmx2048m",jvm.getMaxHeapOption(),"Min heap option incorrect.");
        assertEquals("2048m",jvm.getMaxHeapValue(),"Min heap value incorrect.");
    }

    @Test
    public void testGetMaxHeapBigM() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1024M -Xmx2048M";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xmx2048M",jvm.getMaxHeapOption(),"Min heap option incorrect.");
        assertEquals("2048M",jvm.getMaxHeapValue(),"Min heap value incorrect.");
    }

    @Test
    public void testGetMaxHeapSmallG() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1g -Xmx2g";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xmx2g",jvm.getMaxHeapOption(),"Min heap option incorrect.");
        assertEquals("2g",jvm.getMaxHeapValue(),"Min heap value incorrect.");
    }

    @Test
    public void testGetMaxHeapBigG() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1G -Xmx2G";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xmx2G",jvm.getMaxHeapOption(),"Min heap option incorrect.");
        assertEquals("2G",jvm.getMaxHeapValue(),"Min heap value incorrect.");
    }

    @Test
    public void testIsMinAndMaxHeapSpaceEqual() {
        Jvm jvm = new Jvm("-Xms2g -Xmx2G", null);
        assertTrue(jvm.isMinAndMaxHeapSpaceEqual(), "Min and max heap are equal.");
        jvm = new Jvm("-Xms1G -Xmx2G", null);
        assertFalse(jvm.isMinAndMaxHeapSpaceEqual(), "Min and max heap are not equal.");
        jvm = new Jvm("-Xms256k -Xmx256M", null);
        assertFalse(jvm.isMinAndMaxHeapSpaceEqual(), "Min and max heap are not equal.");
    }

    @Test
    public void testIsMinAndMaxHeapSpaceEqualVerboseOptions() {
        Jvm jvm = new Jvm("-XX:InitialHeapSize=1234567890 -XX:MaxHeapSize=1234567890", null);
        assertTrue(jvm.isMinAndMaxHeapSpaceEqual(), "Min and max heap are equal.");
        jvm = new Jvm("-XX:InitialHeapSize=1234567890 -XX:MaxHeapSize=1234567891", null);
        assertFalse(jvm.isMinAndMaxHeapSpaceEqual(), "Min and max heap are not equal.");
    }

    @Test
    public void testGetMinPermSmallM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=128m -XX:MaxPermSize=128m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:PermSize=128m",jvm.getMinPermOption(),"Min permanent generation option incorrect.");
        assertEquals("128m",jvm.getMinPermValue(),"Min permanent generation value incorrect.");
    }

    @Test
    public void testGetMinPermBigM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=128M -XX:MaxPermSize=128M";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:PermSize=128M",jvm.getMinPermOption(),"Min permanent generation option incorrect.");
        assertEquals("128M",jvm.getMinPermValue(),"Min permanent generation value incorrect.");
    }

    @Test
    public void testGetMinPermSmallG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=1g -XX:MaxPermSize=1g";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:PermSize=1g",jvm.getMinPermOption(),"Min permanent generation option incorrect.");
        assertEquals("1g",jvm.getMinPermValue(),"Min permanent generation value incorrect.");
    }

    @Test
    public void testGetMinPermBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=1G -XX:MaxPermSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:PermSize=1G",jvm.getMinPermOption(),"Min permanent generation option incorrect.");
        assertEquals("1G",jvm.getMinPermValue(),"Min permanent generation value incorrect.");
    }

    @Test
    public void testGetMaxPermSmallM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=128m -XX:MaxPermSize=128m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MaxPermSize=128m",jvm.getMaxPermOption(),"Max permanent generation optiion incorrect.");
        assertEquals("128m",jvm.getMaxPermValue(),"Max permanent generation value incorrect.");
    }

    @Test
    public void testGetMaxPermBigM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=128M -XX:MaxPermSize=128M";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MaxPermSize=128M",jvm.getMaxPermOption(),"Max permanent generation option incorrect.");
        assertEquals("128M",jvm.getMaxPermValue(),"Max permanent generation value incorrect.");
    }

    @Test
    public void testGetMaxPermSmallG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=1g -XX:MaxPermSize=1g";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MaxPermSize=1g",jvm.getMaxPermOption(),"Max permanent generation option incorrect.");
        assertEquals("1g",jvm.getMaxPermValue(),"Max permanent generation value incorrect.");
    }

    @Test
    public void testGetMaxPermBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=1G -XX:MaxPermSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MaxPermSize=1G",jvm.getMaxPermOption(),"Max permanent generation option incorrect.");
        assertEquals("1G",jvm.getMaxPermValue(),"Max permanent generation value incorrect.");
    }

    @Test
    public void testIsMinAndMaxPermSpaceEqualDifferentCaseM() {
        Jvm jvm = new Jvm("-XX:PermSize=256m -XX:MaxPermSize=256M", null);
        assertTrue(jvm.isMinAndMaxPermSpaceEqual(), "Min and max heap are equal.");
    }

    @Test
    public void testIsMinAndMaxPermSpaceEqualDifferentCaseG() {
        Jvm jvm = new Jvm("-XX:PermSize=1G -XX:MaxPermSize=1g", null);
        assertTrue(jvm.isMinAndMaxPermSpaceEqual(), "Min and max heap are equal.");
    }

    @Test
    public void testIsMinAndMaxPermSpaceEqualMissingMin() {
        Jvm jvm = new Jvm("-XX:MaxPermSize=256M", null);
        assertFalse(jvm.isMinAndMaxPermSpaceEqual(), "Min and max heap are not equal.");
    }

    @Test
    public void testIsMinAndMaxPermSpaceEqualDifferentUnitsMG() {
        Jvm jvm = new Jvm("-XX:PermSize=2048m -XX:MaxPermSize=2g", null);
        assertTrue(jvm.isMinAndMaxPermSpaceEqual(), "Min and max heap are equal.");
    }

    @Test
    public void testIsMinAndMaxPermSpaceEqualDifferentUnitsKM() {
        Jvm jvm = new Jvm("-XX:PermSize=1024K -XX:MaxPermSize=1m", null);
        assertTrue(jvm.isMinAndMaxPermSpaceEqual(), "Min and max heap are equal.");
    }

    @Test
    public void testIsMinAndMaxPermSpaceEqualDifferentUnitsNoneG() {
        Jvm jvm = new Jvm("-XX:PermSize=1073741824 -XX:MaxPermSize=1G", null);
        assertTrue(jvm.isMinAndMaxPermSpaceEqual(), "Min and max heap are equal.");
    }

    @Test
    public void testIsMinAndMaxPermSpaceEqualDifferentUnitsBG() {
        Jvm jvm = new Jvm("-XX:PermSize=1073741824b -XX:MaxPermSize=1G", null);
        assertTrue(jvm.isMinAndMaxPermSpaceEqual(), "Min and max heap are equal.");
    }

    @Test
    public void testIsMinAndMaxPermSpaceEqualVerboseOptions() {
        Jvm jvm = new Jvm("-XX:MaxPermSize=1234567890 -XX:PermSize=1234567890", null);
        assertTrue(jvm.isMinAndMaxPermSpaceEqual(), "Min and max heap are equal.");
        jvm = new Jvm("-XX:MaxPermSize=1234567890 -XX:PermSize=1234567891", null);
        assertFalse(jvm.isMinAndMaxPermSpaceEqual(), "Min and max heap are not equal.");
    }

    @Test
    public void testGetMinMetaspaceSmallG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1g -XX:MaxMetaspaceSize=1g";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MetaspaceSize=1g",jvm.getMinMetaspaceOption(),"Min Metaspace generation option incorrect.");
        assertEquals("1g",jvm.getMinMetaspaceValue(),"Min Metaspace generation value incorrect.");
    }

    @Test
    public void testGetMinMetaspaceBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MetaspaceSize=1G",jvm.getMinMetaspaceOption(),"Min Metaspace generation option incorrect.");
        assertEquals("1G",jvm.getMinMetaspaceValue(),"Min Metaspace generation value incorrect.");
    }

    @Test
    public void testGetMaxMetaspaceSmallM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MaxMetaspaceSize=128m",jvm.getMaxMetaspaceOption(),"Max Metaspace generation optiion incorrect.");
        assertEquals("128m",jvm.getMaxMetaspaceValue(),"Max Metaspace generation value incorrect.");
    }

    @Test
    public void testGetMaxMetaspaceBigM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=128M";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MaxMetaspaceSize=128M",jvm.getMaxMetaspaceOption(),"Max Metaspace generation option incorrect.");
        assertEquals("128M",jvm.getMaxMetaspaceValue(),"Max Metaspace generation value incorrect.");
    }

    @Test
    public void testGetMaxMetaspaceSmallG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1g -XX:MaxMetaspaceSize=1g";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MaxMetaspaceSize=1g",jvm.getMaxMetaspaceOption(),"Max Metaspace generation option incorrect.");
        assertEquals("1g",jvm.getMaxMetaspaceValue(),"Max Metaspace generation value incorrect.");
    }

    @Test
    public void testGetMaxMetaspaceBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:MaxMetaspaceSize=1G",jvm.getMaxMetaspaceOption(),"Max Metaspace generation option incorrect.");
        assertEquals("1G",jvm.getMaxMetaspaceValue(),"Max Metaspace generation value incorrect.");
    }

    @Test
    public void testDisableExplicitGc() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G "
                + "-XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+DisableExplicitGC",jvm.getDisableExplicitGCOption(),"Disable explicit gc option incorrect.");
    }

    @Test
    public void testRmiDgcServerGcIntervalValue() {
        String jvmOptions = "-Dsun.rmi.dgc.client.gcInterval=14400000 -Dsun.rmi.dgc.server.gcInterval=24400000";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Dsun.rmi.dgc.client.gcInterval=14400000",jvm.getRmiDgcClientGcIntervalOption(),"sun.rmi.dgc.client.gcInterval option incorrect.");
        assertEquals("14400000",jvm.getRmiDgcClientGcIntervalValue(),"sun.rmi.dgc.client.gcInterval value incorrect.");
        assertEquals("-Dsun.rmi.dgc.server.gcInterval=24400000",jvm.getRmiDgcServerGcIntervalOption(),"sun.rmi.dgc.server.gcInterval option incorrect.");
        assertEquals("24400000",jvm.getRmiDgcServerGcIntervalValue(),"sun.rmi.dgc.server.gcInterval value incorrect.");
    }

    @Test
    public void testJavaagent() {
        String jvmOptions = "-Xss128k -Xms2048M -javaagent:byteman.jar=script:kill-3.btm,boot:byteman.jar -Xmx2048M "
                + "-XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-javaagent:byteman.jar=script:kill-3.btm,boot:byteman.jar",jvm.getJavaagentOption(),"-javaagent option incorrect.");
    }

    @Test
    public void testAgentpath() {
        String jvmOptions = "-Xss128k -Xms2048M -agentpath:C:/agent/agent.dll -Xmx2048M "
                + "-XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-agentpath:C:/agent/agent.dll",jvm.getAgentpathOption(),"-agentpath option incorrect.");
    }

    @Test
    public void testXBatch() {
        String jvmOptions = "-Xss128k -Xbatch -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xbatch",jvm.getXBatchOption(),"-Xbatch option incorrect.");
    }

    @Test
    public void testBackGroundCompilationDisabled() {
        String jvmOptions = "-Xss128k -XX:-BackgroundCompilation -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:-BackgroundCompilation",jvm.getDisableBackgroundCompilationOption(),"-XX:-BackgroundCompilation option incorrect.");
    }

    @Test
    public void testXcomp() {
        String jvmOptions = "-Xss128k -Xcomp -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xcomp",jvm.getXCompOption(),"-Xcomp option incorrect.");
    }

    @Test
    public void testXInt() {
        String jvmOptions = "-Xss128k -Xint -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-Xint",jvm.getXIntOption(),"-Xint option incorrect.");
    }

    @Test
    public void testExplicitGCInvokesConcurrent() {
        String jvmOptions = "-Xss128k -XX:+ExplicitGCInvokesConcurrent -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+ExplicitGCInvokesConcurrent",jvm.getExplicitGcInvokesConcurrentOption(),"-XX:+ExplicitGCInvokesConcurrent option incorrect.");
    }

    @Test
    public void testPrintCommandLineFlags() {
        String jvmOptions = "-Xss128k -XX:+PrintCommandLineFlags -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+PrintCommandLineFlags",jvm.getPrintCommandLineFlagsOption(),"-XX:+PrintCommandLineFlags option incorrect.");
    }

    @Test
    public void testPrintGCDetails() {
        String jvmOptions = "-Xss128k -XX:+PrintGCDetails -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+PrintGCDetails",jvm.getPrintGCDetailsOption(),"-XX:+PrintGCDetails option incorrect.");
    }

    @Test
    public void testUseParNewGC() {
        String jvmOptions = "-Xss128k -XX:+UseParNewGC -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+UseParNewGC",jvm.getUseParNewGCOption(),"-XX:+UseParNewGC option incorrect.");
    }

    @Test
    public void testUseConcMarkSweepGC() {
        String jvmOptions = "-Xss128k -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+UseConcMarkSweepGC",jvm.getUseConcMarkSweepGCOption(),"-XX:+UseConcMarkSweepGC option incorrect.");
    }

    @Test
    public void testCMSClassUnloadingEnabled() {
        String jvmOptions = "-Xss128k -XX:+CMSClassUnloadingEnabled -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+CMSClassUnloadingEnabled",jvm.getCMSClassUnloadingEnabled(),"-XX:+CMSClassUnloadingEnabled option incorrect.");
    }

    @Test
    public void testCMSClassUnloadingDisabled() {
        String jvmOptions = "-Xss128k -XX:-CMSClassUnloadingEnabled -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:-CMSClassUnloadingEnabled",jvm.getCMSClassUnloadingDisabled(),"-XX:-CMSClassUnloadingEnabled option incorrect.");
    }

    @Test
    public void testPrintReferenceGC() {
        String jvmOptions = "-Xss128k -XX:+PrintReferenceGC -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+PrintReferenceGC",jvm.getPrintReferenceGC(),"-XX:+PrintReferenceGC option incorrect.");
    }

    @Test
    public void testPrintGCCause() {
        String jvmOptions = "-Xss128k -XX:+PrintGCCause -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+PrintGCCause",jvm.getPrintGCCause(),"-XX:+PrintGCCause option incorrect.");
    }

    @Test
    public void testPrintGCCauseDisabled() {
        String jvmOptions = "-Xss128k -XX:-PrintGCCause -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:-PrintGCCause",jvm.getPrintGCCauseDisabled(),"-XX:-PrintGCCause option incorrect.");
    }

    /**
     * Test if JDK7 by inspecting version header.
     */
    @Test
    public void testJdk7() {
        String version = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        Jvm jvm = new Jvm(null, null);
        jvm.setVersion(version);
        assertEquals(7,jvm.JdkNumber(),"JDK7 not identified");
    }

    /**
     * Test JDK7 update version by inspecting version header.
     */
    @Test
    public void testJdkUpdate7() {
        String version = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        Jvm jvm = new Jvm(null, null);
        jvm.setVersion(version);
        assertEquals(91,jvm.JdkUpdate(),"JDK7 not identified");
    }

    /**
     * Test if JDK8 by inspecting version header.
     */
    @Test
    public void testJdk8() {
        String version = "Java HotSpot(TM) 64-Bit Server VM (25.73-b02) for linux-amd64 JRE (1.8.0_73-b02), "
                + "built on Jan 29 2016 17:39:45 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        Jvm jvm = new Jvm(null, null);
        jvm.setVersion(version);
        assertEquals(8,jvm.JdkNumber(),"JDK8 not identified");
    }

    /**
     * Test JDK8 update version by inspecting version header.
     */
    @Test
    public void testJdkUpdate8() {
        String version = "Java HotSpot(TM) 64-Bit Server VM (25.73-b02) for linux-amd64 JRE (1.8.0_73-b02), "
                + "built on Jan 29 2016 17:39:45 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        Jvm jvm = new Jvm(null, null);
        jvm.setVersion(version);
        assertEquals(73,jvm.JdkUpdate(),"JDK8 not identified");
    }

    @Test
    public void testTieredCompilation() {
        String jvmOptions = "-Xss128k -XX:+TieredCompilation -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+TieredCompilation",jvm.getTieredCompilation(),"-XX:+TieredCompilation option incorrect.");
    }

    @Test
    public void testPrintStringDeduplicationStatistics() {
        String jvmOptions = "-Xss128k -XX:+PrintStringDeduplicationStatistics -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+PrintStringDeduplicationStatistics",jvm.getPrintStringDeduplicationStatistics(),"-XX:+TieredCompilation option incorrect.");
    }

    @Test
    public void testCmsInitiatingOccupancyFraction() {
        String jvmOptions = "-Xss128k -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:CMSInitiatingOccupancyFraction=70",jvm.getCMSInitiatingOccupancyFraction(),"-XX:CMSInitiatingOccupancyFraction option incorrect.");
    }

    @Test
    public void testUseCmsInitiatingOccupancyOnlyEnabled() {
        String jvmOptions = "-Xss128k -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly "
                + "-XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+UseCMSInitiatingOccupancyOnly",jvm.getCMSInitiatingOccupancyOnlyEnabled(),"-XX:+UseCMSInitiatingOccupancyOnly option incorrect.");
    }

    @Test
    public void testBiasedLockingDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseBiasedLocking -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:-UseBiasedLocking",jvm.getBiasedLockingDisabled(),"-XX:-UseBiasedLocking option incorrect.");
    }

    @Test
    public void testPrintApplicationConcurrentTime() {
        String jvmOptions = "-Xss128k -XX:+PrintGCApplicationConcurrentTime -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+PrintGCApplicationConcurrentTime",jvm.getPrintGcApplicationConcurrentTime(),"-XX:-PrintGCApplicationConcurrentTime option incorrect.");
    }

    @Test
    public void testIs64Bit() {
        String jvmOptions = "-Xss128k -XX:+PrintGCApplicationConcurrentTime -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        String version = "Version: Java HotSpot(TM) 64-Bit Server VM (25.65-b01) for linux-amd64 "
                + "JRE (1.8.0_65-b17), built on Oct  6 2015 17:16:12 by \"java_re\" with gcc 4.3.0 20080428 "
                + "(Red Hat 4.3.0-8)";
        jvm.setVersion(version);
        assertTrue(jvm.is64Bit(), "Jvm not identified as 64-bit.");
    }

    @Test
    public void testIsNot64Bit() {
        String jvmOptions = "-Xss128k -XX:+PrintGCApplicationConcurrentTime -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        String version = "Version: Java HotSpot(TM) 32-Bit Server VM (25.65-b01) for linux-amd64 "
                + "JRE (1.8.0_65-b17), built on Oct  6 2015 17:16:12 by \"java_re\" with gcc 4.3.0 20080428 "
                + "(Red Hat 4.3.0-8)";
        jvm.setVersion(version);
        assertFalse(jvm.is64Bit(), "Jvm incorrectly not identified as 64-bit.");
    }

    @Test
    public void testUseCompressedClassPointers() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedClassPointers -XX:+UseCompressedOops "
                + "-XX:+PrintGCApplicationConcurrentTime -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("UseCompressedClassPointers not enabled.", jvm.getUseCompressedClassPointersEnabled());
    }

    @Test
    public void testCompressedClassSpaceSize() {
        String jvmOptions = "-Xss128k -XX:MetaspaceSize=1280 -XX:MaxMetaspaceSize=1280m "
                + "-XX:CompressedClassSpaceSize=768m -XX:+PrintGCApplicationConcurrentTime "
                + "-XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("CompressedClassSpaceSize not found.", jvm.getCompressedClassSpaceSizeOption());
    }

    @Test
    public void testPrintTenuringDistribution() {
        String jvmOptions = "-Xss128k -XX:+PrintTenuringDistribution -XX:MaxMetaspaceSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+PrintTenuringDistribution",jvm.getPrintTenuringDistribution(),"-XX:+PrintTenuringDistribution option incorrect.");
    }

    @Test
    public void testMaxHeapBytes() {
        String jvmOptions = "-Xss128k -Xmx2048m -XX:MaxMetaspaceSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals(bytes(2147483648L),jvm.getMaxHeapBytes(),"Max heap bytes incorrect.");
    }

    @Test
    public void testMaxHeapBytesUnknown() {
        String jvmOptions = "-Xss128k -XX:MaxMetaspaceSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals(bytes(0L),jvm.getMaxHeapBytes(),"Max heap bytes incorrect.");
    }

    @Test
    public void testMaxPermBytes() {
        String jvmOptions = "-Xss128k -Xmx2048m -XX:MaxPermSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals(bytes(1342177280),jvm.getMaxPermBytes(),"Max perm space bytes incorrect.");
    }

    @Test
    public void testMaxPermBytesUnknown() {
        String jvmOptions = "-Xss128k";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals(bytes(0L),jvm.getMaxPermBytes(),"Max perm space bytes incorrect.");
    }

    @Test
    public void testMaxMetaspaceBytes() {
        String jvmOptions = "-Xss128k -Xmx2048m -XX:MaxMetaspaceSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals(bytes(1342177280),jvm.getMaxMetaspaceBytes(),"Max metaspace bytes incorrect.");
    }

    @Test
    public void testMaxMetaspaceBytesUnknown() {
        String jvmOptions = "-Xss128k";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals(bytes(0L),jvm.getMaxMetaspaceBytes(),"Max metaspace bytes incorrect.");
    }

    @Test
    public void testgetCompressedClassSpaceSizeBytes() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals(bytes(805306368),jvm.getCompressedClassSpaceSizeBytes(),"Compressed class space size bytes incorrect.");
    }

    @Test
    public void testD64() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-d64 not found.", jvm.getD64());
    }

    @Test
    public void testPrintPromotionFailure() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:+PrintPromotionFailure -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:+PrintPromotionFailure not found.", jvm.getPrintPromotionFailureEnabled());
    }

    @Test
    public void testUseMembar() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:+UseMembar -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:+UseMembar not found.", jvm.getUseMembarEnabled());
    }

    @Test
    public void testPrintAdaptiveResizePolicyDisabled() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:-PrintAdaptiveSizePolicy -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:-PrintAdaptiveSizePolicy not found.", jvm.getPrintAdaptiveResizePolicyDisabled());
    }

    @Test
    public void testPrintAdaptiveResizePolicyEnabled() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:+PrintAdaptiveSizePolicy -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:+PrintAdaptiveSizePolicy not found.", jvm.getPrintAdaptiveResizePolicyEnabled());
    }

    @Test
    public void testUnlockExperimentalVmOptionsEnabled() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:+UnlockExperimentalVMOptions -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:+UnlockExperimentalVMOptions not found.", jvm.getUnlockExperimentalVmOptionsEnabled());
    }

    @Test
    public void testUseFastUnorderedTimeStampsEnabled() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseFastUnorderedTimeStamps -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:+UseFastUnorderedTimeStamps not found.", jvm.getUseFastUnorderedTimeStampsEnabled());
    }

    @Test
    public void testG1MixedGcLiveThresholdPercent() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:G1MixedGCLiveThresholdPercent=85 -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:G1MixedGCLiveThresholdPercent=NN not found.", jvm.getG1MixedGCLiveThresholdPercent());
        assertEquals("85",jvm.getG1MixedGCLiveThresholdPercentValue(),"G1MixedGCLiveThresholdPercent incorrect.");
    }

    @Test
    public void testG1HeapWastePercent() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:G1HeapWastePercent=5 -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:G1HeapWastePercent=NN not found.", jvm.getG1HeapWastePercent());
        assertEquals("5",jvm.getG1HeapWastePercentValue(),"G1HeapWastePercent incorrect.");
    }

    @Test
    public void testUseGcLogFileRotationEnabled() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseGCLogFileRotation -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:+UseGCLogFileRotation not found.", jvm.getUseGcLogFileRotationEnabled());
    }

    @Test
    public void testGcLogFileSizeSetBytes() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=8192 -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        assertEquals("8192",jvm.getGcLogFileSizeValue(),"Log file size value incorrect.");
        assertEquals(kilobytes(8),jvm.getGcLogFileSizeBytes(),"Log file size bytes incorrect.");
    }

    @Test
    public void testGcLogFileSizeSetSmallK() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=8192k -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        assertEquals("8192k",jvm.getGcLogFileSizeValue(),"Log file size value incorrect.");
        assertEquals(megabytes(8),jvm.getGcLogFileSizeBytes(),"Log file size bytes incorrect.");
    }

    @Test
    public void testGcLogFileSizeSetLargeK() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=8192K -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        assertEquals("8192K",jvm.getGcLogFileSizeValue(),"Log file size value incorrect.");
        assertEquals(megabytes(8),jvm.getGcLogFileSizeBytes(),"Log file size bytes incorrect.");
    }

    @Test
    public void testGcLogFileSizeSetSmallM() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=5m -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        assertEquals("5m",jvm.getGcLogFileSizeValue(),"Log file size value incorrect.");
        assertEquals(Memory.megabytes(5),jvm.getGcLogFileSizeBytes(),"Log file size bytes incorrect.");
    }

    @Test
    public void testGcLogFileSizeSetLargeM() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=5M -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        assertEquals("5M",jvm.getGcLogFileSizeValue(),"Log file size value incorrect.");
        assertEquals(megabytes(5),jvm.getGcLogFileSizeBytes(),"Log file size bytes incorrect.");
    }

    @Test
    public void testUseG1Gc() {
        String jvmOptions = "-Xmx2048m -XX:+UseG1GC -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:+UseG1GC not found.", jvm.getUseG1Gc());
    }

    @Test
    public void testCmsParallelInitialMarkDisabled() {
        String jvmOptions = "-Xss128k -XX:-CMSParallelInitialMarkEnabled -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:-CMSParallelInitialMarkEnabled",jvm.getCmsParallelInitialMarkDisabled(),"-XX:-CMSParallelInitialMarkEnabled option incorrect.");
    }

    @Test
    public void testCmsParallelRemarkDisabled() {
        String jvmOptions = "-Xss128k -XX:-CMSParallelRemarkEnabled -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:-CMSParallelRemarkEnabled",jvm.getCmsParallelRemarkDisabled(),"-XX:-CMSParallelRemarkEnabled option incorrect.");
    }

    @Test
    public void testG1SummarizeRSetStatsEnabled() {
        String jvmOptions = "-Xss128k -XX:+UnlockExperimentalVMOptions -XX:+G1SummarizeRSetStats -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals("-XX:+G1SummarizeRSetStats",jvm.getG1SummarizeRSetStatsEnabled(),"-XX:+G1SummarizeRSetStats option incorrect.");
    }

    @Test
    public void testG1SummarizeRSetStatsPeriod() {
        String jvmOptions = "-Xss128k -XX:+UnlockExperimentalVMOptions -XX:+G1SummarizeRSetStats "
                + "-XX:G1SummarizeRSetStatsPeriod=1 -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:G1SummarizeRSetStatsPeriod=NNN not found.", jvm.getG1SummarizeRSetStatsPeriod());
        assertEquals("1",jvm.getG1SummarizeRSetStatsPeriodValue(),"G1SummarizeRSetStatsPeriod incorrect.");
    }

    @Test
    public void testHeapDumpPathDir() {
        String jvmOptions = "-Xss128k -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:HeapDumpPath=/path/ not found.", jvm.getHeapDumpPathOption());
        assertEquals("/path/",jvm.getHeapDumpPathValue(),"Heap dump path value incorrect.");
    }

    @Test
    public void testHeapDumpPathFilename() {
        String jvmOptions = "-Xss128k -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/to/heap.dump";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertNotNull("-XX:HeapDumpPath=/path/to/heap.dump not found.", jvm.getHeapDumpPathOption());
        assertEquals("/path/to/heap.dump",jvm.getHeapDumpPathValue(),"Heap dump path value incorrect.");
    }

    @Test
    public void testDisabledOptions() {
        String jvmOptions = "-Xss128K -XX:-BackgroundCompilation -Xms1024m -Xmx2048m -XX:-UseCompressedClassPointers "
                + "-XX:-UseCompressedOops -XX:-TraceClassUnloading";
        Jvm jvm = new Jvm(jvmOptions, null);
        assertEquals(4,jvm.getDisabledOptions().size(),"Disabled options count incorrect.");
        assertTrue(jvm.getDisabledOptions().contains("-XX:-BackgroundCompilation"), "-XX:-BackgroundCompilation not identified as disabled option.");
        assertTrue(jvm.getDisabledOptions().contains("-XX:-UseCompressedClassPointers"), "-XX:-UseCompressedClassPointers not identified as disabled option.");
        assertTrue(jvm.getDisabledOptions().contains("-XX:-UseCompressedOops"), "-XX:-UseCompressedOops not identified as disabled option.");
        assertTrue(jvm.getDisabledOptions().contains("-XX:-TraceClassUnloading"), "-XX:-TraceClassUnloading not identified as disabled option.");
        assertNotNull("Unaccounted disabled options not identified.", jvm.getUnaccountedDisabledOptions());
        assertEquals("-XX:-TraceClassUnloading",jvm.getUnaccountedDisabledOptions(),"Unaccounted disabled options incorrect.");
    }
}