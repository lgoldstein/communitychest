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

package org.apache.maven.classpath.munger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.apache.maven.classpath.munger.logging.AbstractJULWrapper;
import org.apache.maven.classpath.munger.logging.Log;
import org.apache.maven.classpath.munger.logging.LogCreator;
import org.apache.maven.classpath.munger.logging.LogFactory;
import org.apache.maven.classpath.munger.util.UrlUtil;
import org.apache.maven.classpath.munger.util.properties.PropertiesUtil;
import org.apache.maven.classpath.munger.util.properties.PropertySource;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;

/**
 * @author Lyor G.
 * @since Dec 24, 2013 11:21:09 AM
 */
public abstract class AbstractTestSupport extends Assert {
    @Rule public final TestName TEST_NAME_HOLDER=new TestName();
    public static final String TEMP_SUBFOLDER_NAME="temp";

    protected final Log logger;
    private File    targetFolder;
    private File    testTempFolder;

    protected AbstractTestSupport() {
        logger = getLogger(getClass());
    }

    public final String getCurrentTestName() {
        return TEST_NAME_HOLDER.getMethodName();
    }

    protected Log getCurrentTestLogger() {
        return getLogger(getClass().getSimpleName() + "[" + getCurrentTestName() + "]");
    }

    protected InputStream getClassResourceAsStream (final String name) {
        return getClass().getResourceAsStream(name);
    }

    public static final LogCreator  TEST_LOGGER=new LogCreator() {
            @Override
            public Log createLogger(final String name) {
                return new AbstractJULWrapper() {
                    @Override
                    public void log(Level level, Object message, Throwable t) {
                        if (isEnabled(level)) {
                            @SuppressWarnings("resource")
                            PrintStream stdout=(Level.SEVERE.equals(level) || Level.WARNING.equals(level))
                                    ? System.err
                                    : System.out
                                    ;
                            stdout.append("\t[").append(name).append("] ").append(level.getName()).append(": ").println(message);
                            if (t != null) {
                                System.err.append("\t\t ").append(t.getClass().getSimpleName()).append(": ").println(t.getMessage());
                                t.printStackTrace(System.err);
                            }
                        }
                    }

                    @Override
                    public boolean isEnabled(Level level) {
                        return true;
                    }
                };
            }
        };
    public static final Log getLogger(Class<?> c) {
        return getLogger(c.getName());
    }

    public static final Log getLogger(String name) {
        return TEST_LOGGER.createLogger(name);
    }

    @BeforeClass
    public static final void setupLogFactory() {
        LogFactory.setupLogFactory(TEST_LOGGER);
    }

    public static final void assertPropertySourceContents(Map<String,String> valuesMap, PropertySource source) {
        for (Map.Entry<String,String> ee : valuesMap.entrySet()) {
            String  name=ee.getKey(), expected=ee.getValue(), actual=source.getProperty(name);
            assertEquals(name + ": mismatched value", expected, actual);
        }
    }

    protected File ensureTempFolderExists () throws IllegalStateException {
        synchronized(TEMP_SUBFOLDER_NAME) {
            if (testTempFolder == null) {
                final File  parent=detectTargetFolder();
                testTempFolder = new File(parent, TEMP_SUBFOLDER_NAME);
            }
        }

        return ensureFolderExists(testTempFolder);
    }

    public static final File ensureFolderExists (final File folder) throws IllegalStateException {
        if (folder == null) {
            throw new IllegalArgumentException("No folder to ensure existence");
        }

        if ((!folder.exists()) && (!folder.mkdirs())) {
            throw new IllegalStateException("Failed to create " + folder.getAbsolutePath());
        }

        return folder;
    }

    protected File detectTargetFolder () throws IllegalStateException {
        synchronized(TEMP_SUBFOLDER_NAME) {
            if (targetFolder == null) {
                if ((targetFolder=detectTargetFolder(getClass())) == null) {
                    throw new IllegalStateException("Failed to detect target folder");
                }
            }
        }

        return targetFolder;
    }

    /**
     * @param anchor An anchor {@link Class} whose container we want to use
     * as the starting point for the &quot;target&quot; folder lookup up the
     * hierarchy
     * @return The &quot;target&quot; <U>folder</U> - <code>null</code> if not found
     * @see #detectTargetFolder(File)
     */
    public static final File detectTargetFolder (Class<?> anchor) {
        return detectTargetFolder(getClassContainerLocationFile(anchor));
    }

    public static final List<String>    TARGET_FOLDER_NAMES=    // NOTE: order is important
            Collections.unmodifiableList(Arrays.asList("target" /* Maven */, "build" /* Gradle */));

    /**
     * @param anchorFile An anchor {@link File} we want to use
     * as the starting point for the &quot;target&quot; or &quot;build&quot; folder
     * lookup up the hierarchy
     * @return The &quot;target&quot; <U>folder</U> - <code>null</code> if not found
     */
    public static final File detectTargetFolder (File anchorFile) {
        for (File   file=anchorFile; file != null; file=file.getParentFile()) {
            if (!file.isDirectory()) {
                continue;
            }

            String name=file.getName();
            if (TARGET_FOLDER_NAMES.contains(name)) {
                return file;
            }
        }

        return null;
    }

    protected File createTempFile (final String prefix, final String suffix) throws IOException {
        final File  destFolder=ensureTempFolderExists();
        final File  file=File.createTempFile(prefix, suffix, destFolder);
        file.deleteOnExit();
        return file;
    }
    /**
     * @param clazz A {@link Class} object
     * @return A {@link File} of the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws IllegalArgumentException If location is not a valid
     * {@link File} location
     * @see #getClassContainerLocationURI(Class)
     * @see ExtendedFileUtils#asFile(URI)
     */
    public static final File getClassContainerLocationFile (Class<?> clazz)
            throws IllegalArgumentException {
        try {
            URI uri=getClassContainerLocationURI(clazz);
            return (uri == null) ? null : new File(uri);
        } catch(URISyntaxException e) {
            throw new IllegalArgumentException(e.getClass().getSimpleName() + ": " + e.getMessage(), e);
        }
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URI} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     * @throws URISyntaxException if location is not a valid URI
     * @see #getClassContainerLocationURL(Class)
     */
    public static final URI getClassContainerLocationURI (Class<?> clazz) throws URISyntaxException {
        URL url=getClassContainerLocationURL(clazz);
        return (url == null) ? null : url.toURI();
    }

    /**
     * @param clazz A {@link Class} object
     * @return A {@link URL} to the location of the class bytes container
     * - e.g., the root folder, the containing JAR, etc.. Returns
     * <code>null</code> if location could not be resolved
     */
    public static final URL getClassContainerLocationURL (Class<?> clazz) {
        ProtectionDomain    pd=clazz.getProtectionDomain();
        CodeSource          cs=(pd == null) ? null : pd.getCodeSource();
        URL                 url=(cs == null) ? null : cs.getLocation();
        if (url == null) {
            if ((url=getClassBytesURL(clazz)) == null) {
                return null;
            }

            String  srcForm=UrlUtil.getURLSource(url);
            if (PropertiesUtil.isEmpty(srcForm)) {
                return null;
            }

            try {
                url = new URL(srcForm);
            } catch(MalformedURLException e) {
                throw new IllegalArgumentException("getClassContainerLocationURL(" + clazz.getName() + ")"
                                                 + "Failed to create URL=" + srcForm + " from " + url.toExternalForm()
                                                 + ": " + e.getMessage());
            }
        }

        return url;
    }

    /**
     * @param clazz The request {@link Class}
     * @return A {@link URL} to the location of the <code>.class</code> file
     * - <code>null</code> if location could not be resolved
     */
    public static final URL getClassBytesURL (Class<?> clazz) {
        String  className=clazz.getName();
        int     sepPos=className.indexOf('$');
        // if this is an internal class, then need to use its parent as well
        if (sepPos > 0) {
            if ((sepPos=className.lastIndexOf('.')) > 0) {
                className = className.substring(sepPos + 1);
            }
        } else {
            className = clazz.getSimpleName();
        }

        return clazz.getResource(className + ".class");
    }

    /**
     * Deletes a directory recursively.
     *
     * @param directory  directory to delete
     * @throws IOException in case deletion is unsuccessful
     */
    public static final void deleteDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            return;
        }

        cleanDirectory(directory);

        if (!directory.delete()) {
            throw new IOException("Unable to delete directory " + directory + ".");
        }
    }

    /**
     * Cleans a directory without deleting it.
     *
     * @param directory directory to clean
     * @throws IOException in case cleaning is unsuccessful
     */
    public static final void cleanDirectory(File directory) throws IOException {
        if (!directory.exists()) {
            throw new IllegalArgumentException(directory + " does not exist");
        }

        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }

        File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            throw new IOException("Failed to list contents of " + directory);
        }

        IOException exception = null;
        for (File file : files) {
            try {
                forceDelete(file);
            } catch (IOException ioe) {
                exception = ioe;
            }
        }

        if (exception != null) {
            throw exception;
        }
    }

    /**
     * Deletes a file. If file is a directory, delete it and all sub-directories.
     * <p>
     * The difference between File.delete() and this method are:
     * <ul>
     * <li>A directory to be deleted does not have to be empty.</li>
     * <li>You get exceptions when a file or directory cannot be deleted.
     *      (java.io.File methods returns a boolean)</li>
     * </ul>
     *
     * @param file  file or directory to delete, must not be {@code null}
     * @throws NullPointerException if the directory is {@code null}
     * @throws FileNotFoundException if the file was not found
     * @throws IOException in case deletion is unsuccessful
     */
    public static final void forceDelete(File file) throws IOException {
        if (!file.exists()) {
            return;
        }

        if (file.isDirectory()) {
            deleteDirectory(file);
            return;
        }

        if (!file.delete()) {
            throw new IOException("Unable to delete file: " + file);
        }
    }
}
