/*
 *
 */
package net.community.chest.db;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;

import net.community.chest.io.file.FilePathComparator;
import net.community.chest.io.jar.JarUtils;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2009 9:48:20 AM
 */
public final class DriverUtils {
    private DriverUtils ()
    {
        // no instance
    }
    /**
     * Character used to separate components of a JDBC URL
     */
    public static final char URL_FRAGMENT_SEPARATOR_CHAR=':';
    /**
     * Default prefix of a JDBC URL
     */
    public static final String    DEFAULT_JDBC_URL_TYPE="jdbc";
    /**
     * Builds a {@link #URL_FRAGMENT_SEPARATOR_CHAR} separated JDBC URL using
     * the provided components in the <U>order</U> in which they are provided
     * @param comps The components to use - <B>Note:</B> must also include the
     * {@link #DEFAULT_JDBC_URL_TYPE} value
     * @return JDBC URL - null/empty if no components or all components null/empty
     */
    public static final String buildJDBCUrl (final Collection<String> comps)
    {
        final int    numComps=(null == comps) ? 0 : comps.size();
        if (numComps <= 0)
            return null;

        final StringBuilder    sb=new StringBuilder(numComps * 16);
        for (final String c : comps)
        {
            final int    cLen=(null == c) ? 0 : c.length();
            if (cLen <= 0)
                continue;

            if (sb.length() > 0)    // separate from previous fragment
                sb.append(URL_FRAGMENT_SEPARATOR_CHAR);
            sb.append(c);
        }

        return sb.toString();
    }
    /**
     * Builds a {@link #URL_FRAGMENT_SEPARATOR_CHAR} separated JDBC URL using
     * the provided components in the <U>order</U> in which they are provided
     * @param comps The components to use - <B>Note:</B> must also include the
     * {@link #DEFAULT_JDBC_URL_TYPE} value
     * @return JDBC URL - null/empty if no components or all components null/empty
     * @see #buildJDBCUrl(Collection)
     */
    public static final String buildJDBCUrl (final String ... comps)
    {
        return buildJDBCUrl(((null == comps) || (comps.length <= 0)) ? null : Arrays.asList(comps));
    }
    /**
     * Scans the given JAR {@link File} for all classes that that implement the
     * {@link Driver} interface
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param f The {@link File} to scan
     * @return A {@link Collection} of all the matches - may be
     * <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access the JAR
     * @throws ClassNotFoundException If failed to load a class
     */
    public static final Collection<Class<?>> getMatchingDrivers (
                    final ClassLoader ol, final File f)
        throws IOException, ClassNotFoundException
    {
        return JarUtils.getMatchingClasses(Driver.class, ol, f);
    }
    /**
     * Scans the given JAR {@link URL} for all classes that that implement the
     * {@link Driver} interface
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param url The {@link URL} to scan
     * @return A {@link Collection} of all the matches - may be
     * <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access the JAR
     * @throws ClassNotFoundException If failed to load a class
     */
    public static final Collection<Class<?>> getMatchingDrivers (
            final ClassLoader ol, final URL url)
        throws IOException, ClassNotFoundException
    {
        return JarUtils.getMatchingClasses(Driver.class, ol, url);
    }
    /**
     * Scans the given JAR {@link File}-s for all classes that implement the
     * {@link Driver} interface
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR(s) - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param files The {@link File}-s to scan
     * @return A {@link Map} of all the matches where key=the {@link File} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     */
    public static final Map<File,Collection<Class<?>>> getMatchingFileDrivers (
            final ClassLoader ol, final Collection<? extends File> files)
        throws IOException, ClassNotFoundException
    {
        return JarUtils.getMatchingFileClasses(Driver.class, ol, files);
    }
    /**
     * Scans the given JAR {@link File}-s for all classes that implement the
     * {@link Driver} interface
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR(s) - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param files The {@link File}-s to scan
     * @return A {@link Map} of all the matches where key=the {@link File} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     */
    public static final Map<File,Collection<Class<?>>> getMatchingFileDrivers (
            final ClassLoader ol, final File ... files)
        throws IOException, ClassNotFoundException
    {
        return getMatchingFileDrivers(ol, SetsUtils.setOf(FilePathComparator.ASCENDING, files));
    }
    /**
     * Scans the JAR {@link File}-s in the current classpath for all classes
     * that implement the {@link Driver} interface
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR(s) - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @return A {@link Map} of all the matches where key=the {@link File} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     * @see JarUtils#getClasspathJarFiles()
     */
    public static final Map<File,Collection<Class<?>>> getMatchingClasspathDrivers (final ClassLoader ol)
            throws IOException, ClassNotFoundException
    {
        return getMatchingFileDrivers(ol, JarUtils.getClasspathJarFiles());
    }
    /**
     * Scans the given JAR {@link URL}-s for all classes that implement the
     * {@link Driver} interface
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param urls The {@link URL}-s to scan
     * @return A {@link Map} of all the matches where key=the {@link URI} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     * @throws URISyntaxException If failed to convert {@link URL} to {@link URI}
     */
    public static final Map<URI,Collection<Class<?>>> getMatchingURLDrivers (
            final ClassLoader ol, final Collection<? extends URL> urls)
        throws IOException, ClassNotFoundException, URISyntaxException
    {
        return JarUtils.getMatchingURLClasses(Driver.class, ol, urls);
    }
    /**
     * Scans the given JAR {@link URL}-s for all classes that implement the
     * {@link Driver} interface
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param urls The {@link URL}-s to scan
     * @return A {@link Map} of all the matches where key=the {@link URI} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     * @throws URISyntaxException If failed to convert {@link URL} to {@link URI}
     */
    public static final Map<URI,Collection<Class<?>>> getMatchingURLDrivers (
            final ClassLoader ol, final URL... urls)
        throws IOException, ClassNotFoundException, URISyntaxException
    {
        return getMatchingURLDrivers(ol, ((null == urls) || (urls.length <= 0)) ? null : Arrays.asList(urls));
    }
    /**
     * @param cl The {@link URLClassLoader} to use both for loading and for
     * the {@link URL}-s to scan
     * @return A {@link Map} of all the matches where key=the {@link URI} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     * @throws URISyntaxException If failed to convert {@link URL} to {@link URI}
     * @see URLClassLoader#getURLs()
     */
    public static final Map<URI,Collection<Class<?>>> getMatchingDrivers (final URLClassLoader cl)
        throws IOException, ClassNotFoundException, URISyntaxException
    {
        return getMatchingURLDrivers(cl, (null == cl) ? null : cl.getURLs());
    }
    /**
     * Registers an instance of the specified {@link Driver} if one not
     * already registered
     * @param <D> Type of {@link Driver} class being registered
     * @param driverClass Registered driver {@link Class}
     * @return Registration result as a {@link java.util.Map.Entry} whose key=the
     * {@link Driver} instance and the value=a {@link Boolean} indicating
     * if a driver instance was created (TRUE) or already registered (FALSE)
     * @throws Exception If failed to instantiate the driver instance
     */
    public static final <D extends Driver> Map.Entry<D,Boolean> registerDriver (
            final Class<D> driverClass)
        throws Exception
    {
        for (final Enumeration<? extends Driver>    dvl=DriverManager.getDrivers();
              (dvl != null) && dvl.hasMoreElements();
             )
        {
            final Driver    d=dvl.nextElement();
            final Class<?>    c=(null == d) ? null : d.getClass();
            if ((c != null) && driverClass.isAssignableFrom(c))
                return new MapEntryImpl<D,Boolean>(driverClass.cast(d), Boolean.FALSE);
        }

        final D    driver=driverClass.newInstance();
        DriverManager.registerDriver(driver);
        return new MapEntryImpl<D,Boolean>(driver, Boolean.TRUE);
    }
    /**
     * Registers an instance of the specified {@link Driver} if one not
     * already registered
     * @param driverClass Registered driver <U>fully qualified class name</U>
     * @return Registration result as a {@link java.util.Map.Entry} whose key=the
     * {@link Driver} instance and the value=a {@link Boolean} indicating
     * if a driver instance was created (TRUE) or already registered (FALSE)
     * @throws Exception If failed to instantiate the driver instance
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final Map.Entry<Driver,Boolean> registerDriver (
            final String driverClass)
        throws Exception
    {
        return registerDriver((Class) ClassUtil.loadClassByName(driverClass));
    }
    /**
     * Registers the built-in JDK JDBC-ODBC bridge driver
     * @return Registration result as a {@link java.util.Map.Entry} whose key=the
     * {@link Driver} instance and the value=a {@link Boolean} indicating
     * if a driver instance was created (TRUE) or already registered (FALSE)
     * @throws Exception If failed to instantiate the driver instance
     */
    public static final Map.Entry<? extends Driver,Boolean> registerBuiltInODBCDriver () throws Exception
    {
        @SuppressWarnings("unchecked")
        final Class<? extends Driver>    driverClass=
            (Class<? extends Driver>) ClassUtil.loadClassByName("sun.jdbc.odbc.JdbcOdbcDriver");
        return registerDriver(driverClass);
    }

    private static final Properties    EMPTY_PROPERTIES=new Properties();
    public static final Connection getBuiltInODBCConnection (final String DSNName, final Properties props) throws Exception
    {
        if ((null == DSNName) || (DSNName.length() <= 0))
            throw new IllegalArgumentException("No DSN name provided");

        final Map.Entry<? extends Driver,Boolean>    rr=registerBuiltInODBCDriver();
        final Driver                                d=(null == rr) ? null : rr.getKey();
        return (null == d) ? null : d.connect("jdbc:odbc:" + DSNName, (null == props) ? EMPTY_PROPERTIES : props);
    }
}
