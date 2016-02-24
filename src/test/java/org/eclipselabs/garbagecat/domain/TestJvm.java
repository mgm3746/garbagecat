/******************************************************************************
 * Garbage Cat                                                                *
 *                                                                            *
 * Copyright (c) 2008-2010 Red Hat, Inc.                                      *
 * All rights reserved. This program and the accompanying materials           *
 * are made available under the terms of the Eclipse Public License v1.0      *
 * which accompanies this distribution, and is available at                   *
 * http://www.eclipse.org/legal/epl-v10.html                                  *
 *                                                                            *
 * Contributors:                                                              *
 *    Red Hat, Inc. - initial API and implementation                          *
 ******************************************************************************/
package org.eclipselabs.garbagecat.domain;

import org.eclipselabs.garbagecat.util.jdk.Jvm;

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

    public void testGetThreadStackShortSmallK() {
        String jvmOptions = "-Xss128k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss128k", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128k", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackShortBigK() {
        String jvmOptions = "-Xss128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss128K", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128K", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackShortSmallM() {
        String jvmOptions = "-Xss1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss1m", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1m", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackShortBigM() {
        String jvmOptions = "-Xss1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss1M", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1M", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackLongSmallK() {
        String jvmOptions = "-XX:ThreadStackSize=128k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=128k", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128k", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackLongBigK() {
        String jvmOptions = "-XX:ThreadStackSize=128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=128K", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "128K", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackLongSmallM() {
        String jvmOptions = "-XX:ThreadStackSize=1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=1m", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1m", jvm.getThreadStackSizeValue());
    }

    public void testGetThreadStackLongBigM() {
        String jvmOptions = "-XX:ThreadStackSize=1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=1M", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1M", jvm.getThreadStackSizeValue());
    }
    
    public void testGetThreadStackNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1234567 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=1234567", jvm.getThreadStackSizeOption());
        Assert.assertEquals("Thread stack size value incorrect.", "1234567", jvm.getThreadStackSizeValue());
    }
    
    public void testGetThreadStackOneLessThanLargeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048575 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertFalse("Thread stack size is not large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackEqualsLargeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048576 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackOneGreaterLargeNoUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048577 -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackOneLessThanLargeByteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048575b -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertFalse("Thread stack size is not large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackEqualsLargeByteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048576B -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackOneGreaterLargeByteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1048577B -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackOneLessThanLargeKilobyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1023k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertFalse("Thread stack size is not large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackEqualsLargeKilobyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1024K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackOneGreaterLargeKilobyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1025K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackEqualsLargeMegabyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackOneGreaterLargeMegabyteUnits() {
        String jvmOptions = "-XX:ThreadStackSize=1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertTrue("Thread stack size is large.", jvm.hasLargeThreadStackSize());
    }
    
    public void testGetThreadStackLargeGigabyteUnits() {
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
        Assert.assertEquals("Max permanent generation optiion incorrect.", "-XX:MaxPermSize=128m", jvm.getMaxPermOption());
        Assert.assertEquals("Max permanent generation value incorrect.", "128m", jvm.getMaxPermValue());
    }

    public void testGetMaxPermBigM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:PermSize=128M -XX:MaxPermSize=128M";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max permanent generation option incorrect.", "-XX:MaxPermSize=128M", jvm.getMaxPermOption());
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
        Assert.assertEquals("Min Metaspace generation option incorrect.", "-XX:MetaspaceSize=1g", jvm.getMinMetaspaceOption());
        Assert.assertEquals("Min Metaspace generation value incorrect.", "1g", jvm.getMinMetaspaceValue());
    }

    public void testGetMinMetaspaceBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Min Metaspace generation option incorrect.", "-XX:MetaspaceSize=1G", jvm.getMinMetaspaceOption());
        Assert.assertEquals("Min Metaspace generation value incorrect.", "1G", jvm.getMinMetaspaceValue());
    }

    public void testGetMaxMetaspaceSmallM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=128m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max Metaspace generation optiion incorrect.", "-XX:MaxMetaspaceSize=128m", jvm.getMaxMetaspaceOption());
        Assert.assertEquals("Max Metaspace generation value incorrect.", "128m", jvm.getMaxMetaspaceValue());
    }

    public void testGetMaxMetaspaceBigM() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=128M -XX:MaxMetaspaceSize=128M";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max Metaspace generation option incorrect.", "-XX:MaxMetaspaceSize=128M", jvm.getMaxMetaspaceOption());
        Assert.assertEquals("Max Metaspace generation value incorrect.", "128M", jvm.getMaxMetaspaceValue());
    }

    public void testGetMaxMetaspaceSmallG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1g -XX:MaxMetaspaceSize=1g";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max Metaspace generation option incorrect.", "-XX:MaxMetaspaceSize=1g", jvm.getMaxMetaspaceOption());
        Assert.assertEquals("Max Metaspace generation value incorrect.", "1g", jvm.getMaxMetaspaceValue());
    }

    public void testGetMaxMetaspaceBigG() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Max Metaspace generation option incorrect.", "-XX:MaxMetaspaceSize=1G", jvm.getMaxMetaspaceOption());
        Assert.assertEquals("Max Metaspace generation value incorrect.", "1G", jvm.getMaxMetaspaceValue());
    }

    public void testIsMinAndMaxMetaspaceSpaceEqual() {
        Jvm jvm = new Jvm("-XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=256M", null);
        Assert.assertTrue("Min and max metaspace are equal.", jvm.isMinAndMaxMetaspaceEqual());
        jvm = new Jvm("-Xms1G -Xmx2G -XX:MetaspaceSize=256m", null);
        Assert.assertFalse("Min and max metaspace are equal.", jvm.isMinAndMaxMetaspaceEqual());
        jvm = new Jvm("-Xms1G -Xmx2G", null);
        Assert.assertTrue("Min and max metaspace are equal.", jvm.isMinAndMaxMetaspaceEqual());
        jvm = new Jvm("-XX:MaxMetaspaceSize=256M", null);
        Assert.assertFalse("Min and max metaspace are not equal.", jvm.isMinAndMaxMetaspaceEqual());
        jvm = new Jvm("-XX:MetaspaceSize=128k -XX:MaxMetaspaceSize=256K", null);
        Assert.assertFalse("Min and max metaspace are not equal.", jvm.isMinAndMaxMetaspaceEqual());
        jvm = new Jvm("-XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256M", null);
        Assert.assertFalse("Min and max metaspace are not equal.", jvm.isMinAndMaxMetaspaceEqual());
        jvm = new Jvm("-XX:MetaspaceSize=2048k -XX:MaxMetaspaceSize=2M", null);
        Assert.assertTrue("Min and max metaspace are equal.", jvm.isMinAndMaxMetaspaceEqual());
        jvm = new Jvm("-XX:MetaspaceSize=1024m -XX:MaxMetaspaceSize=1g", null);
        Assert.assertTrue("Min and max metaspace are equal.", jvm.isMinAndMaxMetaspaceEqual());
    }
    
    public void testIsMinAndMaxMetaspaceSpaceEqualVerboseOptions() {
        Jvm jvm = new Jvm("-XX:MaxMetaspaceSize=1234567890 -XX:MetaspaceSize=1234567890", null);
        Assert.assertTrue("Min and max metaspace are equal.", jvm.isMinAndMaxMetaspaceEqual());
        jvm = new Jvm("-XX:MaxMetaspaceSize=1234567890 -XX:MetaspaceSize=1234567891", null);
        Assert.assertFalse("Min and max metaspace are not equal.", jvm.isMinAndMaxMetaspaceEqual());
    }
    
    public void testDisableExplicitGc() {
        String jvmOptions = "-Xss128k -Xms2048M -Xmx2048M -XX:MetaspaceSize=1G -XX:MaxMetaspaceSize=1G -XX:+DisableExplicitGC";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Disable explicit gc option incorrect.", "-XX:+DisableExplicitGC", jvm.getDisableExplicitGCOption());        
    }
}
