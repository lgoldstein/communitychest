/*
 * Copyright 2013 Lyor Goldstein
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.maven.classpath.munger.validation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.apache.maven.classpath.munger.AbstractTestSupport;
import org.apache.maven.classpath.munger.util.properties.NamedPropertySource;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

/**
 * @author Lyor G.
 * @since Mar 24, 2014 2:46:44 PM
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class JarValidationUtilsTest extends AbstractTestSupport {
    public JarValidationUtilsTest() {
        super();
    }

    @Test
    public void testSignatureOnSameJar() throws Exception {
        URL orgData=getClassContainerLocationURL(Assert.class);
        assertNotNull("Cannot find source URL", orgData);
        
        File    cpyData=createTempFile(getCurrentTestName(), ".jar");
        try(InputStream input=orgData.openStream()) {
            try(OutputStream output=new FileOutputStream(cpyData)) {
                long    cpySize=IOUtils.copyLarge(input, output);
                logger.info("Copy(" + orgData.toExternalForm() + ")[" + cpyData.getAbsolutePath() + "]: " + cpySize + " bytes");
            }
        }
        
        NamedPropertySource  expected=JarValidationUtils.createJarSignature(orgData);
        NamedPropertySource  actual=JarValidationUtils.createJarSignature(cpyData);
        JarValidationUtils.validateJarSignature(expected, actual);

        if (logger.isDebugEnabled()) {
            for (String name : expected.getAvailableNames()) {
                String  digestString=expected.getProperty(name);
                byte[]  digestValue=DatatypeConverter.parseBase64Binary(digestString);
                logger.debug("    " + name + ": " + DatatypeConverter.printHexBinary(digestValue));
            }
        }
    }
    
    @Test
    public void testSignatureOnModifiedOneByte() throws Exception {
        byte[]              TEST_DATA="the quick brown fox jumps over the lazy dog back".getBytes("UTF-8");
        NamedPropertySource expected=JarValidationUtils.createJarSignature(createTestJar(TEST_DATA));
        for (int index=0; index < TEST_DATA.length; index++) {
            byte    orgValue=TEST_DATA[index];
            try {
                if (TEST_DATA[index] == ' ') {
                    continue;
                }

                TEST_DATA[index] = (byte) (Character.toUpperCase((char) (orgValue & 0xFF)) & 0xFF);
                NamedPropertySource  actual=JarValidationUtils.createJarSignature(createTestJar(TEST_DATA));
                try {
                    JarValidationUtils.validateJarSignature(expected, actual);
                    fail("Unexpected success for " + new String(TEST_DATA));
                } catch(SecurityException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug(e.getMessage());
                    }
                }
            } finally {
                TEST_DATA[index] = orgValue;
            }
        }
    }
    
    @Test
    public void testSignatureOnShuffledContents() throws Exception {
        byte[]              TEST_DATA=(getClass().getName() + "#" + getCurrentTestName()).getBytes("UTF-8");
        NamedPropertySource expected=JarValidationUtils.createJarSignature(createTestJar(TEST_DATA));
        Random      rnd=new Random(System.nanoTime());
        for (int index=0; index < Long.SIZE; index++) {
            shuffle(rnd, TEST_DATA);

            NamedPropertySource  actual=JarValidationUtils.createJarSignature(createTestJar(TEST_DATA));
            try {
                JarValidationUtils.validateJarSignature(expected, actual);
                fail("Unexpected success for " + new String(TEST_DATA));
            } catch(SecurityException e) {
                if (logger.isDebugEnabled()) {
                    logger.debug(e.getMessage());
                }
            }
        }
    }

    @Test
    public void testSignatureOnDifferentDirectoryEntries() throws Exception {
        byte[]          TEST_DATA=(getClass().getName() + "#" + getCurrentTestName()).getBytes("UTF-8");
        List<ZipEntry>  entriesList=createTestZipEntries();
        NamedPropertySource expected=JarValidationUtils.createJarSignature(createTestJar(entriesList, TEST_DATA));

        entriesList.add(0, new ZipEntry("before/"));
        entriesList.add(new ZipEntry("after/"));

        NamedPropertySource actual=JarValidationUtils.createJarSignature(createTestJar(entriesList, TEST_DATA));
        try {
            JarValidationUtils.validateJarSignature(expected, actual);
            fail("Unexpected success");
        } catch(SecurityException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(e.getMessage());
            }
        }
    }

    private byte[] createTestJar(byte[] data) throws IOException {
        return createTestJar(createTestZipEntries(), data);
    }

    private  List<ZipEntry> createTestZipEntries() {
        return createZipEntries(getClass().getPackage().getName(), getClass().getSimpleName(), getCurrentTestName());
    }

    private static byte[] createTestJar(Collection<? extends ZipEntry> entriesList, byte ... data) throws IOException {
        ByteArrayOutputStream   baos=new ByteArrayOutputStream(data.length + entriesList.size() * 64 * 2 + Byte.MAX_VALUE);
        try(JarOutputStream jarFile=new JarOutputStream(baos)) {
            for (ZipEntry zipEntry : entriesList) {
                jarFile.putNextEntry(zipEntry);
                try {
                    if (zipEntry.isDirectory()) {
                        continue;
                    }
                    jarFile.write(data);
                } finally {
                    jarFile.closeEntry();
                }
            }
        } finally {
            baos.close();
        }
        
        return baos.toByteArray();
    }

    private static List<ZipEntry> createZipEntries(String ... path) {
        List<ZipEntry>  entriesList=new ArrayList<>(path.length);
        StringBuilder   curPath=new StringBuilder(path.length * 64);
        for (int index=0; index < path.length; index++) {
            curPath.append(path[index]);
            if (index < (path.length - 1)) {
                curPath.append('/');
            }
            
            entriesList.add(new ZipEntry(curPath.toString()));
        }
        
        return entriesList;
    }
    /////////////////////////////////////////
    
    public static final byte[] shuffle(Random rnd, byte ... vals) {
        if ((vals == null) || (vals.length <= 1)) {
            return vals;
        }
        
        for (int i=vals.length; i>1; i--) {
            swap(vals, i-1, rnd.nextInt(i));
        }
        
        return vals;
    }
    
    public static final byte[] swap(byte[] arr, int i, int j) {
        byte tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
        return arr;
    }

}
