/**********************************************************************************************************************
 * garbagecat                                                                                                         *
 *                                                                                                                    *
 * Copyright (c) 2008-2020 Mike Millson                                                                               *
 *                                                                                                                    * 
 * All rights reserved. This program and the accompanying materials are made available under the terms of the Eclipse *
 * Public License v1.0 which accompanies this distribution, and is available at                                       *
 * http://www.eclipse.org/legal/epl-v10.html.                                                                         *
 *                                                                                                                    *
 * Contributors:                                                                                                      *
 *    Mike Millson - initial API and implementation                                                                   *
 *********************************************************************************************************************/
package org.eclipselabs.garbagecat.util.jdk;

import java.math.BigDecimal;

import org.eclipselabs.garbagecat.util.Constants;

import junit.framework.Assert;
import junit.framework.TestCase;

/**
 * @author <a href="mailto:mmillson@redhat.com">Mike Millson</a>
 * 
 */
public class TestJvm extends TestCase {

    public void testNullJvmOptions() {
        String jvmOptions = null;
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("Jvm object creation failed.", jvm);
    }

    public void testGetThreadStackSizeSsSmallK() {
        String jvmOptions = "-ss128k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-ss128k", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128k", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeSsBigK() {
        String jvmOptions = "-ss128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-ss128K", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128K", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeSsSmallM() {
        String jvmOptions = "-ss1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-ss1m", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1m", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeXssSmallK() {
        String jvmOptions = "-Xss128k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss128k", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128k", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeXssBigK() {
        String jvmOptions = "-Xss128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss128K", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128K", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeXssSmallM() {
        String jvmOptions = "-Xss1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss1m", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1m", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeXssBigM() {
        String jvmOptions = "-Xss1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss1M", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1M", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeSmallK() {
        String jvmOptions = "-XX:ThreadStackSize=128k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=128k", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128k", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeBigK() {
        String jvmOptions = "-XX:ThreadStackSize=128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=128K", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128K", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeSmallM() {
        String jvmOptions = "-XX:ThreadStackSize=1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=1m", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1m", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeBigM() {
        String jvmOptions = "-XX:ThreadStackSize=1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=1M", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1M", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1234567 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=1234567",
                jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1234567", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackSizeOneLessThanLargeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048575 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertFalse("Thread stack size is not large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeEqualsLargeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048576 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeOneGreaterLargeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048577 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeOneLessThanLargeByteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048575b -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertFalse("Thread stack size is not large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeEqualsLargeByteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048576B -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeOneGreaterLargeByteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048577B -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeOneLessThanLargeKilobyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1023k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertFalse("Thread stack size is not large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeEqualsLargeKilobyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1024K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeOneGreaterLargeKilobyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1025K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeEqualsLargeMegabyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeOneGreaterLargeMegabyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetThreadStackSizeLargeGigabyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1G -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }

    public void testGetMinHeapSmallM() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min heap option incorrect.", "-Xms1024m", jvm.getMinHeapOption());
        Assert.assertEquals("Min heap value incorrect.", "1024m", jvm.getMinHeapValue());
    }

    public void testGetMinHeapBigM() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1024M -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min heap option incorrect.", "-Xms1024M", jvm.getMinHeapOption());
        Assert.assertEquals("Min heap value incorrect.", "1024M", jvm.getMinHeapValue());
    }

    public void testGetMinHeapSmallG() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1g -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min heap option incorrect.", "-Xms1g", jvm.getMinHeapOption());
        Assert.assertEquals("Min heap value incorrect.", "1g", jvm.getMinHeapValue());
    }

    public void testGetMinHeapBigG() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1G -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min heap option incorrect.", "-Xms1G", jvm.getMinHeapOption());
        Assert.assertEquals("Min heap value incorrect.", "1G", jvm.getMinHeapValue());
    }

    public void testGetMaxHeapSmallM() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min heap option incorrect.", "-Xmx2048m", jvm.getMaxHeapOption());
        Assert.assertEquals("Min heap value incorrect.", "2048m", jvm.getMaxHeapValue());
    }

    public void testGetMaxHeapBigM() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1024M -Xmx2048M";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min heap option incorrect.", "-Xmx2048M", jvm.getMaxHeapOption());
        Assert.assertEquals("Min heap value incorrect.", "2048M", jvm.getMaxHeapValue());
    }

    public void testGetMaxHeapSmallG() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1g -Xmx2g";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min heap option incorrect.", "-Xmx2g", jvm.getMaxHeapOption());
        Assert.assertEquals("Min heap value incorrect.", "2g", jvm.getMaxHeapValue());
    }

    public void testGetMaxHeapBigG() {
        String jvmOptions = "-XX:ThreadStackSize=128 -Xms1G -Xmx2G";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min heap option incorrect.", "-Xmx2G", jvm.getMaxHeapOption());
        Assert.assertEquals("Min heap value incorrect.", "2G", jvm.getMaxHeapValue());
    }

    public void testIsMinAndMaxHeapSpaceEqual() {
        Jvm jvm = new Jvm("-Xms2g -Xmx2G", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxHeapSpaceEqual());
        jvm = new Jvm("-Xms1G -Xmx2G", null);
        Assert.assertFalse("Min and max heap are not equal.", jvm.isMinAndMaxHeapSpaceEqual());
        jvm = new Jvm("-Xms256k -Xmx256M", null);
        Assert.assertFalse("Min and max heap are not equal.", jvm.isMinAndMaxHeapSpaceEqual());
    }

    public void testIsMinAndMaxHeapSpaceEqualVerboseOptions() {
        Jvm jvm = new Jvm("-XX:InitialHeapSize=1234567890 -XX:MaxHeapSize=1234567890", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxHeapSpaceEqual());
        jvm = new Jvm("-XX:InitialHeapSize=1234567890 -XX:MaxHeapSize=1234567891", null);
        Assert.assertFalse("Min and max heap are not equal.", jvm.isMinAndMaxHeapSpaceEqual());
    }

    public void testGetMinPermSmallM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=128m -XX:MaxPermSize=128m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min permanent generation option incorrect.", "-XX:PermSize=128m", jvm.getMinPermOption());
        Assert.assertEquals("Min permanent generation value incorrect.", "128m", jvm.getMinPermValue());
    }

    public void testGetMinPermBigM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=128M -XX:MaxPermSize=128M";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min permanent generation option incorrect.", "-XX:PermSize=128M", jvm.getMinPermOption());
        Assert.assertEquals("Min permanent generation value incorrect.", "128M", jvm.getMinPermValue());
    }

    public void testGetMinPermSmallG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=1g -XX:MaxPermSize=1g";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min permanent generation option incorrect.", "-XX:PermSize=1g", jvm.getMinPermOption());
        Assert.assertEquals("Min permanent generation value incorrect.", "1g", jvm.getMinPermValue());
    }

    public void testGetMinPermBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=1G -XX:MaxPermSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min permanent generation option incorrect.", "-XX:PermSize=1G", jvm.getMinPermOption());
        Assert.assertEquals("Min permanent generation value incorrect.", "1G", jvm.getMinPermValue());
    }

    public void testGetMaxPermSmallM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=128m -XX:MaxPermSize=128m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max permanent generation optiion incorrect.", "-XX:MaxPermSize=128m",
                jvm.getMaxPermOption());
        Assert.assertEquals("Max permanent generation value incorrect.", "128m", jvm.getMaxPermValue());
    }

    public void testGetMaxPermBigM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=128M -XX:MaxPermSize=128M";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max permanent generation option incorrect.", "-XX:MaxPermSize=128M",
                jvm.getMaxPermOption());
        Assert.assertEquals("Max permanent generation value incorrect.", "128M", jvm.getMaxPermValue());
    }

    public void testGetMaxPermSmallG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=1g -XX:MaxPermSize=1g";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max permanent generation option incorrect.", "-XX:MaxPermSize=1g", jvm.getMaxPermOption());
        Assert.assertEquals("Max permanent generation value incorrect.", "1g", jvm.getMaxPermValue());
    }

    public void testGetMaxPermBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=1G -XX:MaxPermSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max permanent generation option incorrect.", "-XX:MaxPermSize=1G", jvm.getMaxPermOption());
        Assert.assertEquals("Max permanent generation value incorrect.", "1G", jvm.getMaxPermValue());
    }

    public void testIsMinAndMaxPermSpaceEqualDifferentCaseM() {
        Jvm jvm = new Jvm("-XX:PermSize=256m -XX:MaxPermSize=256M", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
    }

    public void testIsMinAndMaxPermSpaceEqualDifferentCaseG() {
        Jvm jvm = new Jvm("-XX:PermSize=1G -XX:MaxPermSize=1g", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
    }

    public void testIsMinAndMaxPermSpaceEqualMissingMin() {
        Jvm jvm = new Jvm("-XX:MaxPermSize=256M", null);
        Assert.assertFalse("Min and max heap are not equal.", jvm.isMinAndMaxPermSpaceEqual());
    }

    public void testIsMinAndMaxPermSpaceEqualDifferentUnitsMG() {
        Jvm jvm = new Jvm("-XX:PermSize=2048m -XX:MaxPermSize=2g", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
    }

    public void testIsMinAndMaxPermSpaceEqualDifferentUnitsKM() {
        Jvm jvm = new Jvm("-XX:PermSize=1024K -XX:MaxPermSize=1m", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
    }

    public void testIsMinAndMaxPermSpaceEqualDifferentUnitsNoneG() {
        Jvm jvm = new Jvm("-XX:PermSize=1073741824 -XX:MaxPermSize=1G", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
    }

    public void testIsMinAndMaxPermSpaceEqualDifferentUnitsBG() {
        Jvm jvm = new Jvm("-XX:PermSize=1073741824b -XX:MaxPermSize=1G", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
    }

    public void testIsMinAndMaxPermSpaceEqualVerboseOptions() {
        Jvm jvm = new Jvm("-XX:MaxPermSize=1234567890 -XX:PermSize=1234567890", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
        jvm = new Jvm("-XX:MaxPermSize=1234567890 -XX:PermSize=1234567891", null);
        Assert.assertFalse("Min and max heap are not equal.", jvm.isMinAndMaxPermSpaceEqual());
    }

    public void testGetMinMetaspaceSmallG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1g -XX:MaxMetaspaceSize=1g";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min Metaspace generation option incorrect.", "-XX:MetaspaceSize=1g",
                jvm.getMinMetaspaceOption());
        Assert.assertEquals("Min Metaspace generation value incorrect.", "1g", jvm.getMinMetaspaceValue());
    }

    public void testGetMinMetaspaceBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min Metaspace generation option incorrect.", "-XX:MetaspaceSize=1G",
                jvm.getMinMetaspaceOption());
        Assert.assertEquals("Min Metaspace generation value incorrect.", "1G", jvm.getMinMetaspaceValue());
    }

    public void testGetMaxMetaspaceSmallM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max Metaspace generation optiion incorrect.", "-XX:MaxMetaspaceSize=128m",
                jvm.getMaxMetaspaceOption());
        Assert.assertEquals("Max Metaspace generation value incorrect.", "128m", jvm.getMaxMetaspaceValue());
    }

    public void testGetMaxMetaspaceBigM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=128M";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max Metaspace generation option incorrect.", "-XX:MaxMetaspaceSize=128M",
                jvm.getMaxMetaspaceOption());
        Assert.assertEquals("Max Metaspace generation value incorrect.", "128M", jvm.getMaxMetaspaceValue());
    }

    public void testGetMaxMetaspaceSmallG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1g -XX:MaxMetaspaceSize=1g";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max Metaspace generation option incorrect.", "-XX:MaxMetaspaceSize=1g",
                jvm.getMaxMetaspaceOption());
        Assert.assertEquals("Max Metaspace generation value incorrect.", "1g", jvm.getMaxMetaspaceValue());
    }

    public void testGetMaxMetaspaceBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max Metaspace generation option incorrect.", "-XX:MaxMetaspaceSize=1G",
                jvm.getMaxMetaspaceOption());
        Assert.assertEquals("Max Metaspace generation value incorrect.", "1G", jvm.getMaxMetaspaceValue());
    }

    public void testDisableExplicitGc() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G "
                + "-XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Disable explicit gc option incorrect.", "-XX:+DisableExplicitGC",
                jvm.getDisableExplicitGCOption());
    }

    public void testRmiDgcServerGcIntervalValue() {
        String jvmOptions = "-Dsun.rmi.dgc.client.gcInterval=14400000 -Dsun.rmi.dgc.server.gcInterval=24400000";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("sun.rmi.dgc.client.gcInterval option incorrect.",
                "-Dsun.rmi.dgc.client.gcInterval=14400000", jvm.getRmiDgcClientGcIntervalOption());
        Assert.assertEquals("sun.rmi.dgc.client.gcInterval value incorrect.", "14400000",
                jvm.getRmiDgcClientGcIntervalValue());
        Assert.assertEquals("sun.rmi.dgc.server.gcInterval option incorrect.",
                "-Dsun.rmi.dgc.server.gcInterval=24400000", jvm.getRmiDgcServerGcIntervalOption());
        Assert.assertEquals("sun.rmi.dgc.server.gcInterval value incorrect.", "24400000",
                jvm.getRmiDgcServerGcIntervalValue());
    }

    public void testJavaagent() {
        String jvmOptions = "-Xss128k -Xms2048M -javaagent:byteman.jar=script:kill-3.btm,boot:byteman.jar -Xmx2048M "
                + "-XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-javaagent option incorrect.", "-javaagent:byteman.jar=script:kill-3.btm,boot:byteman.jar",
                jvm.getJavaagentOption());
    }

    public void testAgentpath() {
        String jvmOptions = "-Xss128k -Xms2048M -agentpath:C:/agent/agent.dll -Xmx2048M "
                + "-XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-agentpath option incorrect.", "-agentpath:C:/agent/agent.dll", jvm.getAgentpathOption());
    }

    public void testXBatch() {
        String jvmOptions = "-Xss128k -Xbatch -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-Xbatch option incorrect.", "-Xbatch", jvm.getXBatchOption());
    }

    public void testBackGroundCompilationDisabled() {
        String jvmOptions = "-Xss128k -XX:-BackgroundCompilation -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:-BackgroundCompilation option incorrect.", "-XX:-BackgroundCompilation",
                jvm.getDisableBackgroundCompilationOption());
    }

    public void testXcomp() {
        String jvmOptions = "-Xss128k -Xcomp -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-Xcomp option incorrect.", "-Xcomp", jvm.getXCompOption());
    }

    public void testXInt() {
        String jvmOptions = "-Xss128k -Xint -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-Xint option incorrect.", "-Xint", jvm.getXIntOption());
    }

    public void testExplicitGCInvokesConcurrent() {
        String jvmOptions = "-Xss128k -XX:+ExplicitGCInvokesConcurrent -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+ExplicitGCInvokesConcurrent option incorrect.", "-XX:+ExplicitGCInvokesConcurrent",
                jvm.getExplicitGcInvokesConcurrentOption());
    }

    public void testPrintCommandLineFlags() {
        String jvmOptions = "-Xss128k -XX:+PrintCommandLineFlags -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+PrintCommandLineFlags option incorrect.", "-XX:+PrintCommandLineFlags",
                jvm.getPrintCommandLineFlagsOption());
    }

    public void testPrintGCDetails() {
        String jvmOptions = "-Xss128k -XX:+PrintGCDetails -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+PrintGCDetails option incorrect.", "-XX:+PrintGCDetails",
                jvm.getPrintGCDetailsOption());
    }

    public void testUseParNewGC() {
        String jvmOptions = "-Xss128k -XX:+UseParNewGC -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+UseParNewGC option incorrect.", "-XX:+UseParNewGC", jvm.getUseParNewGCOption());
    }

    public void testUseConcMarkSweepGC() {
        String jvmOptions = "-Xss128k -XX:+UseConcMarkSweepGC -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+UseConcMarkSweepGC option incorrect.", "-XX:+UseConcMarkSweepGC",
                jvm.getUseConcMarkSweepGCOption());
    }

    public void testCMSClassUnloadingEnabled() {
        String jvmOptions = "-Xss128k -XX:+CMSClassUnloadingEnabled -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+CMSClassUnloadingEnabled option incorrect.", "-XX:+CMSClassUnloadingEnabled",
                jvm.getCMSClassUnloadingEnabled());
    }

    public void testCMSClassUnloadingDisabled() {
        String jvmOptions = "-Xss128k -XX:-CMSClassUnloadingEnabled -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:-CMSClassUnloadingEnabled option incorrect.", "-XX:-CMSClassUnloadingEnabled",
                jvm.getCMSClassUnloadingDisabled());
    }

    public void testPrintReferenceGC() {
        String jvmOptions = "-Xss128k -XX:+PrintReferenceGC -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+PrintReferenceGC option incorrect.", "-XX:+PrintReferenceGC",
                jvm.getPrintReferenceGC());
    }

    public void testPrintGCCause() {
        String jvmOptions = "-Xss128k -XX:+PrintGCCause -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+PrintGCCause option incorrect.", "-XX:+PrintGCCause", jvm.getPrintGCCause());
    }

    public void testPrintGCCauseDisabled() {
        String jvmOptions = "-Xss128k -XX:-PrintGCCause -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:-PrintGCCause option incorrect.", "-XX:-PrintGCCause", jvm.getPrintGCCauseDisabled());
    }

    /**
     * Test if JDK7 by inspecting version header.
     */
    public void testJdk7() {
        String version = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        Jvm jvm = new Jvm(null, null);
        jvm.setVersion(version);
        Assert.assertEquals("JDK7 not identified", 7, jvm.JdkNumber());
    }

    /**
     * Test JDK7 update version by inspecting version header.
     */
    public void testJdkUpdate7() {
        String version = "Java HotSpot(TM) 64-Bit Server VM (24.91-b03) for windows-amd64 JRE (1.7.0_91-b15), built on "
                + "Oct  2 2015 03:26:24 by \"java_re\" with unknown MS VC++:1600";
        Jvm jvm = new Jvm(null, null);
        jvm.setVersion(version);
        Assert.assertEquals("JDK7 not identified", 91, jvm.JdkUpdate());
    }

    /**
     * Test if JDK8 by inspecting version header.
     */
    public void testJdk8() {
        String version = "Java HotSpot(TM) 64-Bit Server VM (25.73-b02) for linux-amd64 JRE (1.8.0_73-b02), "
                + "built on Jan 29 2016 17:39:45 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        Jvm jvm = new Jvm(null, null);
        jvm.setVersion(version);
        Assert.assertEquals("JDK8 not identified", 8, jvm.JdkNumber());
    }

    /**
     * Test JDK8 update version by inspecting version header.
     */
    public void testJdkUpdate8() {
        String version = "Java HotSpot(TM) 64-Bit Server VM (25.73-b02) for linux-amd64 JRE (1.8.0_73-b02), "
                + "built on Jan 29 2016 17:39:45 by \"java_re\" with gcc 4.3.0 20080428 (Red Hat 4.3.0-8)";
        Jvm jvm = new Jvm(null, null);
        jvm.setVersion(version);
        Assert.assertEquals("JDK8 not identified", 73, jvm.JdkUpdate());
    }

    public void testTieredCompilation() {
        String jvmOptions = "-Xss128k -XX:+TieredCompilation -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+TieredCompilation option incorrect.", "-XX:+TieredCompilation",
                jvm.getTieredCompilation());
    }

    public void testPrintStringDeduplicationStatistics() {
        String jvmOptions = "-Xss128k -XX:+PrintStringDeduplicationStatistics -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+TieredCompilation option incorrect.", "-XX:+PrintStringDeduplicationStatistics",
                jvm.getPrintStringDeduplicationStatistics());
    }

    public void testCmsInitiatingOccupancyFraction() {
        String jvmOptions = "-Xss128k -XX:CMSInitiatingOccupancyFraction=70 -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:CMSInitiatingOccupancyFraction option incorrect.",
                "-XX:CMSInitiatingOccupancyFraction=70", jvm.getCMSInitiatingOccupancyFraction());
    }

    public void testUseCmsInitiatingOccupancyOnlyEnabled() {
        String jvmOptions = "-Xss128k -XX:CMSInitiatingOccupancyFraction=70 -XX:+UseCMSInitiatingOccupancyOnly "
                + "-XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+UseCMSInitiatingOccupancyOnly option incorrect.",
                "-XX:+UseCMSInitiatingOccupancyOnly", jvm.getCMSInitiatingOccupancyOnlyEnabled());
    }

    public void testBiasedLockingDisabled() {
        String jvmOptions = "-Xss128k -XX:-UseBiasedLocking -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:-UseBiasedLocking option incorrect.", "-XX:-UseBiasedLocking",
                jvm.getBiasedLockingDisabled());
    }

    public void testPrintApplicationConcurrentTime() {
        String jvmOptions = "-Xss128k -XX:+PrintGCApplicationConcurrentTime -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:-PrintGCApplicationConcurrentTime option incorrect.",
                "-XX:+PrintGCApplicationConcurrentTime", jvm.getPrintGcApplicationConcurrentTime());
    }

    public void testIs64Bit() {
        String jvmOptions = "-Xss128k -XX:+PrintGCApplicationConcurrentTime -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        String version = "Version: Java HotSpot(TM) 64-Bit Server VM (25.65-b01) for linux-amd64 "
                + "JRE (1.8.0_65-b17), built on Oct  6 2015 17:16:12 by \"java_re\" with gcc 4.3.0 20080428 "
                + "(Red Hat 4.3.0-8)";
        jvm.setVersion(version);
        Assert.assertTrue("Jvm not identified as 64-bit.", jvm.is64Bit());
    }

    public void testIsNot64Bit() {
        String jvmOptions = "-Xss128k -XX:+PrintGCApplicationConcurrentTime -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        String version = "Version: Java HotSpot(TM) 32-Bit Server VM (25.65-b01) for linux-amd64 "
                + "JRE (1.8.0_65-b17), built on Oct  6 2015 17:16:12 by \"java_re\" with gcc 4.3.0 20080428 "
                + "(Red Hat 4.3.0-8)";
        jvm.setVersion(version);
        Assert.assertFalse("Jvm incorrectly not identified as 64-bit.", jvm.is64Bit());
    }

    public void testUseCompressedClassPointers() {
        String jvmOptions = "-Xss128k -XX:+UseCompressedClassPointers -XX:+UseCompressedOops "
                + "-XX:+PrintGCApplicationConcurrentTime -XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("UseCompressedClassPointers not enabled.", jvm.getUseCompressedClassPointersEnabled());
    }

    public void testCompressedClassSpaceSize() {
        String jvmOptions = "-Xss128k -XX:MetaspaceSize=1280 -XX:MaxMetaspaceSize=1280m "
                + "-XX:CompressedClassSpaceSize=768m -XX:+PrintGCApplicationConcurrentTime "
                + "-XX:+CMSParallelRemarkEnabled";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("CompressedClassSpaceSize not found.", jvm.getCompressedClassSpaceSizeOption());
    }

    public void testPrintTenuringDistribution() {
        String jvmOptions = "-Xss128k -XX:+PrintTenuringDistribution -XX:MaxMetaspaceSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+PrintTenuringDistribution option incorrect.", "-XX:+PrintTenuringDistribution",
                jvm.getPrintTenuringDistribution());
    }

    public void testMaxHeapBytes() {
        String jvmOptions = "-Xss128k -Xmx2048m -XX:MaxMetaspaceSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        BigDecimal size = new BigDecimal("2147483648");
        Assert.assertEquals("Max heap bytes incorrect.", size.longValue(), jvm.getMaxHeapBytes());
    }

    public void testMaxHeapBytesUnknown() {
        String jvmOptions = "-Xss128k -XX:MaxMetaspaceSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max heap bytes incorrect.", 0L, jvm.getMaxHeapBytes());
    }

    public void testMaxPermBytes() {
        String jvmOptions = "-Xss128k -Xmx2048m -XX:MaxPermSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        BigDecimal size = new BigDecimal("1342177280");
        Assert.assertEquals("Max perm space bytes incorrect.", size.longValue(), jvm.getMaxPermBytes());
    }

    public void testMaxPermBytesUnknown() {
        String jvmOptions = "-Xss128k";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max perm space bytes incorrect.", 0L, jvm.getMaxPermBytes());
    }

    public void testMaxMetaspaceBytes() {
        String jvmOptions = "-Xss128k -Xmx2048m -XX:MaxMetaspaceSize=1280m";
        Jvm jvm = new Jvm(jvmOptions, null);
        BigDecimal size = new BigDecimal("1342177280");
        Assert.assertEquals("Max metaspace bytes incorrect.", size.longValue(), jvm.getMaxMetaspaceBytes());
    }

    public void testMaxMetaspaceBytesUnknown() {
        String jvmOptions = "-Xss128k";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max metaspace bytes incorrect.", 0L, jvm.getMaxMetaspaceBytes());
    }

    public void testgetCompressedClassSpaceSizeBytes() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Compressed class space size bytes incorrect.", 805306368,
                jvm.getCompressedClassSpaceSizeBytes());
    }

    public void testD64() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-d64 not found.", jvm.getD64());
    }

    public void testPrintPromotionFailure() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:+PrintPromotionFailure -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:+PrintPromotionFailure not found.", jvm.getPrintPromotionFailureEnabled());
    }

    public void testUseMembar() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:+UseMembar -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:+UseMembar not found.", jvm.getUseMembarEnabled());
    }

    public void testPrintAdaptiveResizePolicyDisabled() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:-PrintAdaptiveSizePolicy -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:-PrintAdaptiveSizePolicy not found.", jvm.getPrintAdaptiveResizePolicyDisabled());
    }

    public void testPrintAdaptiveResizePolicyEnabled() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:+PrintAdaptiveSizePolicy -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:+PrintAdaptiveSizePolicy not found.", jvm.getPrintAdaptiveResizePolicyEnabled());
    }

    public void testUnlockExperimentalVmOptionsEnabled() {
        String jvmOptions = "-XX:CompressedClassSpaceSize=768m -XX:+UnlockExperimentalVMOptions -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:+UnlockExperimentalVMOptions not found.",
                jvm.getUnlockExperimentalVmOptionsEnabled());
    }

    public void testUseFastUnorderedTimeStampsEnabled() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseFastUnorderedTimeStamps -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:+UseFastUnorderedTimeStamps not found.", jvm.getUseFastUnorderedTimeStampsEnabled());
    }

    public void testG1MixedGcLiveThresholdPercent() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:G1MixedGCLiveThresholdPercent=85 -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:G1MixedGCLiveThresholdPercent=NN not found.", jvm.getG1MixedGCLiveThresholdPercent());
        Assert.assertEquals("G1MixedGCLiveThresholdPercent incorrect.", "85",
                jvm.getG1MixedGCLiveThresholdPercentValue());
    }

    public void testG1HeapWastePercent() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:G1HeapWastePercent=5 -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:G1HeapWastePercent=NN not found.", jvm.getG1HeapWastePercent());
        Assert.assertEquals("G1HeapWastePercent incorrect.", "5", jvm.getG1HeapWastePercentValue());
    }

    public void testUseGcLogFileRotationEnabled() {
        String jvmOptions = "-XX:+UnlockExperimentalVMOptions -XX:+UseGCLogFileRotation -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:+UseGCLogFileRotation not found.", jvm.getUseGcLogFileRotationEnabled());
    }

    public void testGcLogFileSizeSetBytes() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=8192 -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        Assert.assertEquals("Log file size value incorrect.", "8192", jvm.getGcLogFileSizeValue());
        BigDecimal size = new BigDecimal("8192");
        Assert.assertEquals("Log file size bytes incorrect.", size.longValue(), jvm.getGcLogFileSizeBytes());
    }

    public void testGcLogFileSizeSetSmallK() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=8192k -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        Assert.assertEquals("Log file size value incorrect.", "8192k", jvm.getGcLogFileSizeValue());
        BigDecimal size = new BigDecimal("8192").multiply(Constants.KILOBYTE);
        Assert.assertEquals("Log file size bytes incorrect.", size.longValue(), jvm.getGcLogFileSizeBytes());
    }

    public void testGcLogFileSizeSetLargeK() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=8192K -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        Assert.assertEquals("Log file size value incorrect.", "8192K", jvm.getGcLogFileSizeValue());
        BigDecimal size = new BigDecimal("8192").multiply(Constants.KILOBYTE);
        Assert.assertEquals("Log file size bytes incorrect.", size.longValue(), jvm.getGcLogFileSizeBytes());
    }

    public void testGcLogFileSizeSetSmallM() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=5m -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        Assert.assertEquals("Log file size value incorrect.", "5m", jvm.getGcLogFileSizeValue());
        BigDecimal size = new BigDecimal("5").multiply(Constants.MEGABYTE);
        Assert.assertEquals("Log file size bytes incorrect.", size.longValue(), jvm.getGcLogFileSizeBytes());
    }

    public void testGcLogFileSizeSetLargeM() {
        String jvmOptions = "-XX:+UseGCLogFileRotation -XX:GCLogFileSize=5M -XX:NumberOfGCLogFiles=5";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:GCLogFileSize not found.", jvm.getGcLogFileSize());
        Assert.assertEquals("Log file size value incorrect.", "5M", jvm.getGcLogFileSizeValue());
        BigDecimal size = new BigDecimal("5").multiply(Constants.MEGABYTE);
        Assert.assertEquals("Log file size bytes incorrect.", size.longValue(), jvm.getGcLogFileSizeBytes());
    }

    public void testUseG1Gc() {
        String jvmOptions = "-Xmx2048m -XX:+UseG1GC -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:+UseG1GC not found.", jvm.getUseG1Gc());
    }

    public void testCmsParallelInitialMarkDisabled() {
        String jvmOptions = "-Xss128k -XX:-CMSParallelInitialMarkEnabled -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:-CMSParallelInitialMarkEnabled option incorrect.",
                "-XX:-CMSParallelInitialMarkEnabled", jvm.getCmsParallelInitialMarkDisabled());
    }

    public void testCmsParallelRemarkDisabled() {
        String jvmOptions = "-Xss128k -XX:-CMSParallelRemarkEnabled -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:-CMSParallelRemarkEnabled option incorrect.", "-XX:-CMSParallelRemarkEnabled",
                jvm.getCmsParallelRemarkDisabled());
    }

    public void testG1SummarizeRSetStatsEnabled() {
        String jvmOptions = "-Xss128k -XX:+UnlockExperimentalVMOptions -XX:+G1SummarizeRSetStats -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("-XX:+G1SummarizeRSetStats option incorrect.", "-XX:+G1SummarizeRSetStats",
                jvm.getG1SummarizeRSetStatsEnabled());
    }

    public void testG1SummarizeRSetStatsPeriod() {
        String jvmOptions = "-Xss128k -XX:+UnlockExperimentalVMOptions -XX:+G1SummarizeRSetStats "
                + "-XX:G1SummarizeRSetStatsPeriod=1 -d64";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:G1SummarizeRSetStatsPeriod=NNN not found.", jvm.getG1SummarizeRSetStatsPeriod());
        Assert.assertEquals("G1SummarizeRSetStatsPeriod incorrect.", "1", jvm.getG1SummarizeRSetStatsPeriodValue());
    }

    public void testHeapDumpPathDir() {
        String jvmOptions = "-Xss128k -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:HeapDumpPath=/path/ not found.", jvm.getHeapDumpPathOption());
        Assert.assertEquals("Heap dump path value incorrect.", "/path/", jvm.getHeapDumpPathValue());
    }

    public void testHeapDumpPathFilename() {
        String jvmOptions = "-Xss128k -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/path/to/heap.dump";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertNotNull("-XX:HeapDumpPath=/path/to/heap.dump not found.", jvm.getHeapDumpPathOption());
        Assert.assertEquals("Heap dump path value incorrect.", "/path/to/heap.dump", jvm.getHeapDumpPathValue());
    }
}