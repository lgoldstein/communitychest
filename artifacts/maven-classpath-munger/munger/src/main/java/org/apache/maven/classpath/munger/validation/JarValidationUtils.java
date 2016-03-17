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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Objects;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.bind.DatatypeConverter;

import org.apache.maven.classpath.munger.util.properties.NamedPropertySource;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;

/**
 * @author Lyor G.
 * @since Mar 24, 2014 1:56:08 PM
 */
public final class JarValidationUtils {
    private JarValidationUtils() {
        throw new UnsupportedOperationException("No instance");
    }

    public static final NamedPropertySource createJarSignature(File file) throws IOException {
        try(InputStream inputStream=new FileInputStream(file)) {
            return createJarSignature(inputStream);
        }
    }

    public static final NamedPropertySource createJarSignature(URL url) throws IOException {
        try(InputStream inputStream=url.openStream()) {
            return createJarSignature(inputStream);
        }
    }

    public static final NamedPropertySource createJarSignature(byte...jarData) throws IOException {
        try(ByteArrayInputStream    inputStream=new ByteArrayInputStream(jarData)) {
            return createJarSignature(inputStream);
        }
    }

    public static final NamedPropertySource createJarSignature(InputStream inputStream) throws IOException {
        try(ZipInputStream  jarStream=new ZipInputStream(inputStream)) {
            return createJarSignature(jarStream);
        }
    }

    /*
     * NOTE: we use a ZipInputStream since the JarInputStream executes some special
     * logic for the manifest, which interferes with our signature that includes it
     */
    public static final NamedPropertySource createJarSignature(ZipInputStream jarStream) throws IOException {
        final MessageDigest   messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
        } catch(NoSuchAlgorithmException e) {
            throw new IOException("Failed (" + e.getClass().getSimpleName() + ") to instantiate digester: " + e.getMessage(), e);
        }

        Properties  entriesMap=new Properties();
        for (ZipEntry entry=jarStream.getNextEntry(); entry != null; entry=jarStream.getNextEntry()) {
            String  name=entry.getName();
            if (PropertiesUtil.isEmpty(name)) {
                throw new StreamCorruptedException("Null/empty entry found");
            }
            /*
             *  NOTE: in standard java signature, entries in the META-INF folder
             *  or directory entries do not participate in the signature. We sign
             *  ALL of them in order to ensure that no manipulations have been made
             *  of ANY kind - no extra empty folders, no extra files in META-INF -
             *  regardless of whether we can think of a security issue for such a
             *  manipulation
             */

            try {
                final String  digestValue;
                if (entry.isDirectory()) {
                    digestValue = updateDigest(messageDigest, name);
                } else {
                    digestValue = updateDigest(messageDigest, jarStream);
                }
                Object  prev=entriesMap.setProperty(name, digestValue);
                if (prev != null) {
                    throw new StreamCorruptedException("Multiple digest entries for " + name);
                }
            } finally {
                jarStream.closeEntry();
            }
        }

        return PropertiesUtil.asPropertySource(entriesMap);
    }

    public static final void validateJarSignature(NamedPropertySource expSignature, NamedPropertySource actSignature) throws SecurityException {
        Collection<String>  expNames=expSignature.getAvailableNames(), actNames=actSignature.getAvailableNames();
        if (expNames.size() <= 0) {
            throw new SecurityException("Empty expected signature");
        }

        assertEquals("Mismatched signature sizes", expNames.size(), actNames.size());

        for (String name : expNames) {
            String  expValue=expSignature.getProperty(name), actValue=actSignature.getProperty(name);
            assertEquals("Mismatched signature value for " + name, expValue, actValue);
        }
    }

    private static final void assertEquals(String message, int expected, int actual) throws SecurityException {
        assertEquals(message, Integer.valueOf(expected), Integer.valueOf(actual));
    }

    private static final void assertEquals(String message, Object expected, Object actual) throws SecurityException {
        if (!Objects.equals(expected, actual)) {
            throw new SecurityException(message + ": expected=" + expected + ", actual=" + actual);
        }
    }

    private static String updateDigest(MessageDigest digest, InputStream inputStream) throws IOException {
        byte[]  buffer=new byte[4096];
        int     read=0;
        while ((read=inputStream.read(buffer)) > 0) {
            digest.update(buffer, 0, read);
        }

        return getDigestValue(digest);
    }

    private static String updateDigest(MessageDigest digest, String value) throws UnsupportedEncodingException {
        return updateDigest(digest, value.getBytes("UTF-8"));
    }

    private static String updateDigest(MessageDigest digest, byte ... data) {
        digest.update(data);
        return getDigestValue(digest);
    }

    private static String getDigestValue(MessageDigest digest) {
        byte[]  digestValue=digest.digest();
        return DatatypeConverter.printBase64Binary(digestValue);
    }
}
