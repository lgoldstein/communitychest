package net.community.chest.io.jar;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.zip.ZipFile;

import net.community.chest.io.FileUtil;
import net.community.chest.io.file.FilePathComparator;
import net.community.chest.io.input.InputStreamEmbedder;
import net.community.chest.io.url.URLComparator;
import net.community.chest.lang.ExtendedURLClassLoader;
import net.community.chest.lang.StringUtil;
import net.community.chest.lang.SysPropsEnum;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.compare.URIComparator;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 21, 2007 8:53:25 AM
 */
public final class JarUtils {
    private JarUtils ()
    {
        // no instance
    }
    /**
     * Enumerates all {@link JarEntry} in a {@link JarInputStream} via a
     * {@link JarEntryHandler} callback mechanism.
     * @param in The {@link JarInputStream} to query for existing {@link JarEntry}-es
     * @param hndlr The {@link JarEntryHandler} to use for handling the entries
     * @return Result code from call to {@link JarEntryHandler#handleJAREntry(JarEntry)}
     * if non-zero. Zero if all calls returned zero or no entries at all.
     * @throws IOException if cannot access the JAR entries
     */
    public static final int enumerateJAREntries (JarInputStream in, JarEntryHandler hndlr) throws IOException
    {
        if ((null == in) || (null == hndlr))
            throw new IOException(ClassUtil.getExceptionLocation(JarUtils.class, "enumerateJAREntries") + " no input stream/handler provided");

        for (JarEntry    je=in.getNextJarEntry(); je != null; je=in.getNextJarEntry())
        {
            final int    retCode=hndlr.handleJAREntry(je);
            if (retCode != 0)
                return retCode;
        }

        return 0;
    }
    /**
     * Enumerates all {@link JarEntry} in a {@link JarInputStream} via a
     * {@link JarEntryHandler} callback mechanism.
     * @param in The {@link InputStream} representing the JAR data - <B>Note:</B>
     * at the end of the enumeration the stream's read position may have changed,
     * but it still <U>open</U> - i.e., the caller should close it.
     * @param hndlr The {@link JarEntryHandler} to use for handling the entries
     * @return Result code from call to {@link JarEntryHandler#handleJAREntry(JarEntry)}
     * if non-zero. Zero if all calls returned zero or no entries at all.
     * @throws IOException if cannot access the JAR entries
     */
    public static final int enumerateJAREntries (InputStream in, JarEntryHandler hndlr) throws IOException
    {
        if ((null == in) || (null == hndlr))
            throw new IOException(ClassUtil.getExceptionLocation(JarUtils.class, "enumerateJAREntries") + " no input stream/handler provided");

        JarInputStream    jin=null;
        try
        {
            jin = new JarInputStream(new InputStreamEmbedder(in, false));
            return enumerateJAREntries(jin, hndlr);
        }
        finally
        {
            FileUtil.closeAll(jin);
        }
    }
    /**
     * Enumerates all {@link JarEntry} in a specified JAR {@link URL} via a
     * {@link JarEntryHandler} callback mechanism.
     * @param url The {@link URL} to enumerate entries from
     * @param hndlr The {@link JarEntryHandler} to use for handling the entries
     * @return Result code from call to {@link JarEntryHandler#handleJAREntry(JarEntry)}
     * if non-zero. Zero if all calls returned zero or no entries at all.
     * @throws IOException if cannot access the JAR entries
     */
    public static final int enumerateJAREntries (URL url, JarEntryHandler hndlr) throws IOException
    {
        if ((null == url) || (null == hndlr))
            throw new IOException(ClassUtil.getExceptionLocation(JarUtils.class, "enumerateJAREntries") + " no URL/handler provided");

        final JarURLHandler    urlh=(hndlr instanceof JarURLHandler) ? (JarURLHandler) hndlr : null;
        int                    nErr=(urlh != null) ? urlh.handleJarURL(url, true, 0) : 0;
        if (nErr != 0)
            return nErr;

        InputStream    in=url.openStream();
        try
        {
            nErr = enumerateJAREntries(in, hndlr);
            if (urlh != null)
                nErr = urlh.handleJarURL(url, false, nErr);
            return nErr;
        }
        finally
        {
            FileUtil.closeAll(in);
        }
    }
    /**
     * Enumerates all {@link JarEntry} in a specified JAR {@link File} via a
     * {@link JarEntryHandler} callback mechanism.
     * @param f The {@link File} to enumerate entries from
     * @param hndlr The {@link JarEntryHandler} to use for handling the entries
     * @return Result code from call to {@link JarEntryHandler#handleJAREntry(JarEntry)}
     * if non-zero. Zero if all calls returned zero or no entries at all.
     * @throws IOException if cannot access the JAR entries
     */
    public static final int enumerateJAREntries (File f, JarEntryHandler hndlr) throws IOException
    {
        if ((null == f) || (null == hndlr))
            throw new IOException(ClassUtil.getExceptionLocation(JarUtils.class, "enumerateJAREntries") + " no input file/handler provided");

        final JarURLHandler    urlh=(hndlr instanceof JarURLHandler) ? (JarURLHandler) hndlr : null;
        final URL            url=(null == urlh) ? null : FileUtil.toURL(f);
        int                    nErr=(null == url) ? 0 : urlh.handleJarURL(url, true, 0);
        if (nErr != 0)
            return nErr;

        JarFile    jf=null;
        try
        {
            jf = new JarFile(f, true, ZipFile.OPEN_READ);
            for (final Enumeration<JarEntry>    jee=jf.entries();
                 (jee != null) && jee.hasMoreElements();
                 )
            {
                final JarEntry    je=jee.nextElement();
                if ((nErr=hndlr.handleJAREntry(je)) != 0)
                    break;
            }
        }
        finally
        {
            if (jf != null)
            {
                jf.close();
                jf = null;
            }
        }

        if (url != null)
            nErr = urlh.handleJarURL(url, false, nErr);
        return nErr;
    }

    public static final String    CLASS_SUFFIX="class";
    public static final boolean isClassFile (final String filePath)
    {
        return FileUtil.isMatchingFileSuffix(filePath, CLASS_SUFFIX);
    }

    public static final boolean isClassFile (final URL fileURL)
    {
        return (null == fileURL) ? false : isClassFile(fileURL.getPath());
    }

    public static final boolean isClassFile (final File f)
    {
        return ((null == f) || (!f.isFile())) ? false : isClassFile(f.getName());
    }

    public static final boolean isClassFile (final JarEntry je)
    {
        return (null == je) ? false : isClassFile(je.getName());
    }
    /**
     * Enumerates all {@link JarEntry} in a specified JAR file via a
     * {@link JarEntryHandler} callback mechanism.
     * @param inPath The JAR file path
     * @param hndlr The {@link JarEntryHandler} to use for handling the entries
     * @return Result code from call to {@link JarEntryHandler#handleJAREntry(JarEntry)}
     * if non-zero. Zero if all calls returned non-zero or no entries at all.
     * @throws IOException if cannot access the JAR entries
     */
    public static final int enumerateJAREntries (String inPath, JarEntryHandler hndlr) throws IOException
    {
        if ((null == inPath) || (inPath.length() <= 0) || (null == hndlr))
            throw new IOException(ClassUtil.getExceptionLocation(JarUtils.class, "enumerateJAREntries") + " no input file/handler provided");

        return enumerateJAREntries(new File(inPath), hndlr);
    }
    /**
     * Helper class
     */
    private static final class ClasspathJarEntryHandler implements JarURLHandler {
        private final JarEntryHandler    _hndlr;
        public final JarEntryHandler getEntryHandler ()
        {
            return _hndlr;
        }

        private final JarURLHandler    _urlh;
        public final JarURLHandler getURLHandler ()
        {
            return _urlh;
        }

        protected ClasspathJarEntryHandler (JarEntryHandler hndlr) throws IllegalArgumentException
        {
            if (null == (_hndlr=hndlr))
                throw new IllegalArgumentException("No handler provided");
            _urlh = (hndlr instanceof JarURLHandler) ? (JarURLHandler) hndlr : null;
        }
        /*
         * @see net.community.chest.io.jar.JarURLHandler#handleJarURL(java.net.URL, boolean, int)
         */
        @Override
        public int handleJarURL (URL url, boolean starting, int errCode)
        {
            final JarURLHandler    h=getURLHandler();
            if (null == h)
                return 0;

            return h.handleJarURL(url, starting, errCode);
        }
        /*
         * @see net.community.chest.io.jar.JarEntryHandler#handleJAREntry(java.util.jar.JarEntry)
         */
        @Override
        public int handleJAREntry (final JarEntry je)
        {
            if (!isClassFile(je))
                return 0;

            final JarEntryHandler    h=getEntryHandler();
            if (null == h)
                return Integer.MIN_VALUE;

            return h.handleJAREntry(je);
        }
    }
    /**
     * Goes over all <U>class</U> entries (if any) in all the specified file(s)
     * @param hndlr The {@link JarEntryHandler} instance to use
     * @param files A {@link Collection} of {@link File}-s to scan
     * @return The last error code returned by any of the {@link JarEntryHandler}
     * instance implementation
     * @throws IOException If failed to access a file
     * @throws IllegalArgumentException If no {@link JarEntryHandler} instance
     * provided and have some file(s) to scan
     */
    public static final int enumerateClasspathFileEntries (JarEntryHandler hndlr, Collection<? extends File> files) throws IOException
    {
        if ((null == files) || (files.size() <= 0))
            return 0;

        final ClasspathJarEntryHandler    h=new ClasspathJarEntryHandler(hndlr);
        int                                nErr=0;
        for (final File f : files)
        {
            if ((nErr=enumerateJAREntries(f, h)) != 0)
            {
                if (nErr != 1)
                    break;
            }
        }

        return nErr;
    }
    /**
     * Goes over all <U>class</U> entries (if any) in all the specified URL(s)
     * @param hndlr The {@link JarEntryHandler} instance to use
     * @param files An array of {@link File}-s to scan
     * @return The last error code returned by any of the {@link JarEntryHandler}
     * instance implementation
     * @throws IOException If failed to access a URL
     * @throws IllegalArgumentException If no {@link JarEntryHandler} instance
     * provided and have some URL(s) to scan
     */
    public static final int enumerateClasspathFileEntries (JarEntryHandler hndlr, File ... files) throws IOException
    {
        return enumerateClasspathFileEntries(hndlr, ((null == files) || (files.length <= 0)) ? null : Arrays.asList(files));
    }
    /**
     * Goes over all <U>class</U> entries (if any) in all the specified URL(s)
     * @param hndlr The {@link JarEntryHandler} instance to use
     * @param urls A {@link Collection} of {@link URL}-s to scan
     * @return The last error code returned by any of the {@link JarEntryHandler}
     * instance implementation
     * @throws IOException If failed to access a URL
     * @throws IllegalArgumentException If no {@link JarEntryHandler} instance
     * provided and have some URL(s) to scan
     */
    public static final int enumerateClasspathURLEntries (JarEntryHandler hndlr, Collection<? extends URL> urls) throws IOException
    {
        if ((null == urls) || (urls.size() <= 0))
            return 0;

        final ClasspathJarEntryHandler    h=new ClasspathJarEntryHandler(hndlr);
        int                                nErr=0;
        for (final URL u : urls)
        {
            if ((nErr=enumerateJAREntries(u, h)) != 0)
            {
                if (nErr != 1)
                    break;
            }
        }

        return nErr;
    }
    /**
     * Goes over all <U>class</U> entries (if any) in all the specified URL(s)
     * @param hndlr The {@link JarEntryHandler} instance to use
     * @param urls An array of {@link URL}-s to scan
     * @return The last error code returned by any of the {@link JarEntryHandler}
     * instance implementation
     * @throws IOException If failed to access a URL
     * @throws IllegalArgumentException If no {@link JarEntryHandler} instance
     * provided and have some URL(s) to scan
     */
    public static final int enumerateClasspathURLEntries (JarEntryHandler hndlr, URL ... urls) throws IOException
    {
        return enumerateClasspathURLEntries(hndlr, ((null == urls) || (urls.length <= 0)) ? null : Arrays.asList(urls));
    }
    /**
     * Goes over all <U>class</U> entries (if any) in all the {@link URL}
     * currently set for the {@link URLClassLoader}
     * @param cl tThe {@link URLClassLoader} instance - ignored if null or no
     * current {@link URL}-s set
     * @param hndlr The {@link JarEntryHandler} instance to use
     * @return The last error code returned by any of the {@link JarEntryHandler}
     * instance implementation
     * @throws IOException If failed to access a URL
     * @throws IllegalArgumentException If no {@link JarEntryHandler} instance
     * provided and have some URL(s) to scan
     * @see URLClassLoader#getURLs()
     */
    public static final int enumerateClasspathEntries (URLClassLoader cl, JarEntryHandler hndlr) throws IOException
    {
        return enumerateClasspathURLEntries(hndlr, (null == cl) ? null : cl.getURLs());
    }
    /**
     * Scans the given JAR {@link URL} for all classes that are (optionally)
     * assignable to some base class.
     * @param baseClass The base {@link Class} to check for compatibility - if
     * <code>null</code> then all classes are deemed as matching
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param url The {@link URL} to scan
     * @return A {@link Collection} of all the matches - may be
     * <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access the JAR
     * @throws ClassNotFoundException If failed to load a class
     */
    public static final Collection<Class<?>> getMatchingClasses (
            final Class<?> baseClass, final ClassLoader ol, final URL url)
        throws IOException, ClassNotFoundException
    {
        if (null == url)
            return null;

        final ClassesEntryHandler    hndlr=new ClassesEntryHandler(baseClass, ol);
        final int                    nErr=enumerateClasspathURLEntries(hndlr, url);
        if (nErr != 0)
        {
            final ClassNotFoundException    e=hndlr.getClassNotFoundException();
            if (null == e)
                throw new IOException("getMatchingClasses(" + url + ") error=" + nErr + " on enumerate class entries");
            else
                throw e;
        }

        return hndlr;
    }
    /**
     * Scans the given JAR {@link File} for all classes that are (optionally)
     * assignable to some base class.
     * @param baseClass The base {@link Class} to check for compatibility - if
     * <code>null</code> then all classes are deemed as matching
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param f The {@link File} to scan
     * @return A {@link Collection} of all the matches - may be
     * <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access the JAR
     * @throws ClassNotFoundException If failed to load a class
     */
    public static final Collection<Class<?>> getMatchingClasses (
            final Class<?> baseClass, final ClassLoader ol, final File f)
        throws IOException, ClassNotFoundException
    {
        if (null == f)
            return null;

        final ClassesEntryHandler    hndlr=new ClassesEntryHandler(baseClass, ol);
        final int                    nErr=enumerateClasspathFileEntries(hndlr, f);
        if (nErr != 0)
        {
            final ClassNotFoundException    e=hndlr.getClassNotFoundException();
            if (null == e)
                throw new IOException("getMatchingClasses(" + f + ") error=" + nErr + " on enumerate class entries");
            else
                throw e;
        }

        return hndlr;
    }
    /**
     * Scans the given JAR {@link URL}-s for all classes that are (optionally)
     * assignable to some base class.
     * @param baseClass The base {@link Class} to check for compatibility - if
     * <code>null</code> then all classes are deemed as matching
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
    public static final Map<URI,Collection<Class<?>>> getMatchingURLClasses (
            final Class<?> baseClass, final ClassLoader ol, final Collection<? extends URL> urls)
        throws IOException, ClassNotFoundException, URISyntaxException
    {
        if ((null == urls) || (urls.size() <= 0))
            return null;

        final ClassLoader                cl=
            (null == ol) ? Thread.currentThread().getContextClassLoader() : ol;
        Map<URI,Collection<Class<?>>>    ret=null;
        for (final URL u : urls)
        {
            final Collection<Class<?>>    ml=getMatchingClasses(baseClass, cl, u);
            if ((null == ml) || (ml.size() <= 0))
                continue;

            if (null == ret)
                ret = new TreeMap<URI,Collection<Class<?>>>(URIComparator.ASCENDING);
            ret.put(u.toURI(), ml);
        }

        return ret;
    }
    /**
     * Scans the given JAR {@link URL}-s for all classes that are (optionally)
     * assignable to some base class.
     * @param baseClass The base {@link Class} to check for compatibility - if
     * <code>null</code> then all classes are deemed as matching
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
    public static final Map<URI,Collection<Class<?>>> getMatchingURLClasses (
            final Class<?> baseClass, final ClassLoader ol, final URL ... urls)
        throws IOException, ClassNotFoundException, URISyntaxException
    {
        return getMatchingURLClasses(baseClass, ol, SetsUtils.setOf(URLComparator.ASCENDING, urls));
    }
    /**
     * Scans the current {@link URL}-s in the {@link URLClassLoader} for all
     * classes that are (optionally) assignable to some base class.
     * @param baseClass The base {@link Class} to check for compatibility - if
     * <code>null</code> then all classes are deemed as matching
     * @param cl The default {@link URLClassLoader} to use to scan and load the
     * classes found in the JAR(s) - if <code>null</code> then nothing is done
     * @return A {@link Map} of all the matches where key=the {@link URI} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     * @throws URISyntaxException If failed to convert {@link URL} to {@link URI}
     */
    public static final Map<URI,Collection<Class<?>>> getMatchingClasses (
                final Class<?> baseClass, final URLClassLoader cl)
        throws IOException, ClassNotFoundException, URISyntaxException
    {
        return (null == cl) ? null : getMatchingURLClasses(baseClass, cl, cl.getURLs());
    }
    /**
     * Scans the given JAR {@link File}-s for all classes that are (optionally)
     * assignable to some base class.
     * @param baseClass The base {@link Class} to check for compatibility - if
     * <code>null</code> then all classes are deemed as matching
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param files The {@link File}-s to scan
     * @return A {@link Map} of all the matches where key=the {@link File} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     */
    public static final Map<File,Collection<Class<?>>> getMatchingFileClasses (
            final Class<?> baseClass, final ClassLoader ol, final Collection<? extends File> files)
        throws IOException, ClassNotFoundException
    {
        if ((null == files) || (files.size() <= 0))
            return null;

        final ClassLoader                cl=
            (null == ol) ? Thread.currentThread().getContextClassLoader() : ol;
        Map<File,Collection<Class<?>>>    ret=null;
        for (final File f : files)
        {
            final Collection<Class<?>>    ml=getMatchingClasses(baseClass, cl, f);
            if ((null == ml) || (ml.size() <= 0))
                continue;

            if (null == ret)
                ret = new TreeMap<File,Collection<Class<?>>>(FilePathComparator.ASCENDING);
            ret.put(f, ml);
        }

        return ret;
    }
    /**
     * Scans the given JAR {@link File}-s for all classes that are (optionally)
     * assignable to some base class.
     * @param baseClass The base {@link Class} to check for compatibility - if
     * <code>null</code> then all classes are deemed as matching
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @param files The {@link File}-s to scan
     * @return A {@link Map} of all the matches where key=the {@link File} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     */
    public static final Map<File,Collection<Class<?>>> getMatchingFileClasses (
            final Class<?> baseClass, final ClassLoader ol, final File ... files)
        throws IOException, ClassNotFoundException
    {
        if ((null == files) || (files.length <= 0))
            return null;

        return getMatchingFileClasses(baseClass, ol, SetsUtils.setOf(FilePathComparator.ASCENDING, files));
    }

    public static final String    JAR_SUFFIX="jar";
    public static final boolean isJarFile (final String filePath)
    {
        return FileUtil.isMatchingFileSuffix(filePath, JAR_SUFFIX);
    }

    public static final boolean isJarFile (final URL fileURL)
    {
        return (null == fileURL) ? false : isJarFile(fileURL.getPath());
    }

    public static final boolean isJarFile (final File f)
    {
        return ((null == f) || (!f.isFile())) ? false : isJarFile(f.getName());
    }

    public static final String    JAVA_SUFFIX="java";
    public static final boolean isJavaFile (final String filePath)
    {
        return FileUtil.isMatchingFileSuffix(filePath, JAVA_SUFFIX);
    }

    public static final boolean isJavaFile (final URL fileURL)
    {
        return (null == fileURL) ? false : isJavaFile(fileURL.getPath());
    }

    public static final boolean isJavaFile (final File f)
    {
        return ((null == f) || (!f.isFile())) ? false : isJavaFile(f.getName());
    }

    public static final boolean isJavaFile (final JarEntry je)
    {
        return (null == je) ? false : isJavaFile(je.getName());
    }
    /**
     * Locates all JAR(s) in a given <U>folder</U> and returns them as
     * {@link URL}-s so they can be used for class loading (e.g. via
     * a {@link URLClassLoader}
     * @param org Original {@link Collection} of {@link URL}-s - if
     * <code>null</code>/empty and need to add a URL then one will be
     * created
     * @param root Root <U>folder</U> to search (if not a folder then
     * nothing is updated)
     * @param recursive <code>true</code>=if any of the sub-files is a folder
     * then recursively scan it as well
     * @return Updated {@link Collection} of all JAR(s) URL(s) - may be same
     * as input (including <code>null</code>/empty) if nothing updated
     * @throws MalformedURLException If failed to convert any of the matching
     * {@link File}-s to its {@link URL} equivalent.
     */
    public static final Collection<URL> updateJarURLs (final Collection<URL> org, final File root, final boolean recursive) throws MalformedURLException
    {
        final Collection<? extends File>    fl=FileUtil.getMatchingFilesBySuffix(root, recursive, JAR_SUFFIX);
        if ((null == fl) || (fl.size() <= 0))
            return org;

        Collection<URL>    ret=org;
        for (final File f : fl)
        {
            final URL    url=FileUtil.toURL(f);
            if (null == url)
                continue;

            if (null == ret)
                ret = new LinkedList<URL>();
            ret.add(url);
        }

        return ret;
    }

    public static final Collection<URL> getJarURLs (final File root, final boolean recursive) throws MalformedURLException
    {
        return updateJarURLs(null, root, recursive);
    }

    public static final BaseURLClassLoader updateJarsClassLoader (
            final BaseURLClassLoader cl, final File root, final boolean recursive) throws MalformedURLException
    {
        if (null == cl)
            return cl;

        final Collection<? extends URL>    urls=getJarURLs(root, recursive);
        if ((urls != null) && (urls.size() > 0))
            cl.addAll(urls);
        return cl;
    }
    /**
     * Creates a new {@link ClassLoader} that also includes scanning all the
     * JAR(s) found in the root folder
     * @param parent Parent {@link ClassLoader} - <code>null</code> not recommended
     * @param factory Used by the <code>URL</code> class to create a
     * <code>URLStreamHandler</code> for a specific protocol - may be <code>null</code>.
     * @param root Root <U>folder</U> to search (if not a folder then
     * nothing is updated)
     * @param recursive <code>true</code>=if any of the sub-files is a folder
     * then recursively scan it as well
     * @return An updated {@link ClassLoader} - the parent if no URL(s) found
     * or if the parent is already an {@link URLClassLoader} and no specific
     * {@link URLStreamHandlerFactory} instance provided
     * @throws MalformedURLException If failed to convert any of the matching
     * {@link File}-s to its {@link URL} equivalent.
     */
    public static final ClassLoader getJarsClassLoader (
                    final ClassLoader                 parent,
                    final URLStreamHandlerFactory    factory,
                    final File                        root,
                    final boolean                    recursive)
        throws MalformedURLException
    {
        final Collection<? extends URL>    urlc=getJarURLs(root, recursive);
        final int                        numURLs=(null == urlc) ? 0 : urlc.size();
        if (numURLs <= 0)
            return parent;

        if (null == factory)
        {
            if (parent instanceof URLClassLoader)
                return ExtendedURLClassLoader.addURL((URLClassLoader) parent, urlc);

            return new ExtendedURLClassLoader(parent, urlc);
        }
        else
        {
            return new ExtendedURLClassLoader(parent, factory, urlc);
        }
    }

    public static final ClassLoader getJarsClassLoader (final ClassLoader    parent,
                                                        final File            root,
                                                        final boolean        recursive)
        throws MalformedURLException
    {
        return getJarsClassLoader(parent, null, root, recursive);
    }

    public static final ClassLoader getJarsClassLoader (final URLStreamHandlerFactory    factory,
                                                        final File                        root,
                                                        final boolean                    recursive)
        throws MalformedURLException
    {
        final Thread        t=Thread.currentThread();
        final ClassLoader    cl=(null == t) ? null : t.getContextClassLoader();
        return getJarsClassLoader(cl, factory, root, recursive);
    }
    /**
     * Creates a new {@link ClassLoader} that also includes scanning all the
     * JAR(s) found in the root folder - uses the current {@link Thread}-s
     * context loader as the parent
     * @param root Root <U>folder</U> to search (if not a folder then
     * nothing is updated)
     * @param recursive <code>true</code>=if any of the sub-files is a folder
     * then recursively scan it as well
     * @return An updated {@link ClassLoader} - the current {@link Thread}-s
     * context loader if no URL(s) found or updated context loader if already
     * a {@link URLClassLoader} instance
     * @throws MalformedURLException If failed to convert any of the matching
     * {@link File}-s to its {@link URL} equivalent.
     */
    public static final ClassLoader getJarsClassLoader (final File root, final boolean recursive)
        throws MalformedURLException
    {
        return getJarsClassLoader((URLStreamHandlerFactory) null, root, recursive);
    }
    /**
     * @return A {@link List} of all the file paths found in the <code>java.class.path</code>
     * property
     */
    public static final List<String> getClasspathFilePaths ()
    {
        final String        cp=SysPropsEnum.JAVACLASSPATH.getPropertyValue();
        return StringUtil.splitString(cp, File.pathSeparatorChar);
    }

    public static final List<File> getClasspathJarFiles ()
    {
        final Collection<String>    pl=getClasspathFilePaths();
        final int                    numPaths=(null == pl) ? 0 : pl.size();
        if (numPaths <= 0)
            return null;

        List<File>    fl=null;
        for (final String    p : pl)
        {
            final File    f=((null == p) || (p.length() <= 0)) ? null : new File(p);
            if ((!isJarFile(f)) || (!f.exists()) || (!f.canRead()))
                continue;

            if (null == fl)
                fl = new ArrayList<File>(numPaths);
            fl.add(f);
        }

        return fl;
    }
    /**
     * Scans all the JAR {@link File}-s in the current classpath for all
     * classes that are (optionally) assignable to some base class.
     * @param baseClass The base {@link Class} to check for compatibility - if
     * <code>null</code> then all classes are deemed as matching
     * @param ol The default {@link ClassLoader} to use to load the classes
     * found in the JAR - if <code>null</code> then the default context loader
     * for the current {@link Thread} is used
     * @return A {@link Map} of all the matches where key=the {@link File} where
     * the match was found, value=a {@link Collection} of all the matches. May
     * be <code>null</code>/empty if no matches or no classes found in the JAR
     * @throws IOException If failed to access a JAR
     * @throws ClassNotFoundException If failed to load a class
     * @see #getClasspathJarFiles()
     */
    public static final Map<File,Collection<Class<?>>> getMatchingClasspathClasses (
                final Class<?> baseClass, final ClassLoader ol)
            throws IOException, ClassNotFoundException
    {
        return getMatchingFileClasses(baseClass, ol, getClasspathJarFiles());
    }
}
