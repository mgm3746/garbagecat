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
    }

    public void testGetThreadStackShortBigK() {
        String jvmOptions = "-Xss128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss128K", jvm.getThreadStackSizeOption());
    }

    public void testGetThreadStackShortSmallM() {
        String jvmOptions = "-Xss1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss1m", jvm.getThreadStackSizeOption());
    }

    public void testGetThreadStackShortBigM() {
        String jvmOptions = "-Xss1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size not populated correctly.", "-Xss1M", jvm.getThreadStackSizeOption());
    }

    public void testGetThreadStackLongSmallK() {
        String jvmOptions = "-XX:ThreadStackSize=128k -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=128k", jvm.getThreadStackSizeOption());
    }

    public void testGetThreadStackLongBigK() {
        String jvmOptions = "-XX:ThreadStackSize=128K -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=128K", jvm.getThreadStackSizeOption());
    }

    public void testGetThreadStackLongSmallM() {
        String jvmOptions = "-XX:ThreadStackSize=1m -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=1m", jvm.getThreadStackSizeOption());
    }

    public void testGetThreadStackLongBigM() {
        String jvmOptions = "-XX:ThreadStackSize=1M -Xms1024m -Xmx2048m";
        Jvm jvm = new Jvm(jvmOptions, null);
        Assert.assertEquals("Thread stack size incorrect.", "-XX:ThreadStackSize=1M", jvm.getThreadStackSizeOption());
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

    public void testIsMinAndMaxPermSpaceEqual() {
        Jvm jvm = new Jvm("-XX:PermSize=256m -XX:MaxPermSize=256M", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
        jvm = new Jvm("-Xms1G -Xmx2G -XX:PermSize=256m", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
        jvm = new Jvm("-Xms1G -Xmx2G", null);
        Assert.assertTrue("Min and max heap are equal.", jvm.isMinAndMaxPermSpaceEqual());
        jvm = new Jvm("-XX:MaxPermSize=256M", null);
        Assert.assertFalse("Min and max heap are not equal.", jvm.isMinAndMaxPermSpaceEqual());
        jvm = new Jvm("-XX:PermSize=128m -XX:MaxPermSize=256M", null);
        Assert.assertFalse("Min and max heap are not equal.", jvm.isMinAndMaxPermSpaceEqual());
    }
}
