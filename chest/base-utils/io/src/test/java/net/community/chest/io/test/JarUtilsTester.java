package net.community.chest.io.test;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.community.chest.io.FileUtil;
import net.community.chest.io.IOCopier;
import net.community.chest.io.file.FilePathComparator;
import net.community.chest.io.jar.JarURLHandler;
import net.community.chest.io.jar.JarUtils;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.test.TestBase;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Nov 25, 2007 10:54:50 AM
 */
public final class JarUtilsTester extends TestBase {
	private JarUtilsTester ()
	{
		// no instance
	}

	public static final int testJarEntriesHandler (final PrintStream out, final BufferedReader in, final File jarFile)
	{
		for (final String	filePath=jarFile.getAbsolutePath(); ; )
		{
			try
			{
				final int	nErr=JarUtils.enumerateJAREntries(jarFile, new JarURLHandler() {
						/*
						 * @see net.community.chest.io.jar.JarURLHandler#handleJarURL(java.net.URL, boolean, int)
						 */
						@Override
						public int handleJarURL (URL url, boolean starting, int errCode)
						{
							out.println("\t" + url + " " + (starting ? "start" : "end"));
							return errCode;
						}
						/*
						 * @see net.community.chest.util.jar.JarEntryHandler#handleJAREntry(java.util.jar.JarEntry)
						 */
						@Override
						public int handleJAREntry (JarEntry je)
						{
							out.println("\t\t" + je.getName());
							return 0;
						}
					});
				if (nErr != 0)
				{
					final PrintStream	ps=(nErr > 0) ? out : System.err;
					ps.println(filePath + " - err=" + nErr);
				}
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}

			final String	ans=getval(out, in, "again (y)/[n]");
			if ((null == ans) || (ans.length() <= 0) || (Character.toLowerCase(ans.charAt(0)) != 'y'))
				break;
		}

		return 0;
	}

	// each argument is JAR file
	public static final int testJarEntriesHandler (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	inPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "JAR input path (or Quit)");
			if ((null == inPath) || (inPath.length() <= 0))
				continue;
			if (isQuit(inPath))
				break;

			testJarEntriesHandler(out, in, new File(inPath));
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static final int testJarClassesLoader (
			final PrintStream out, final BufferedReader in, final Class<?> baseClass, final Collection<? extends File> files) throws MalformedURLException
	{
		final ClassLoader	uc;
		{
			final int						numFiles=(null == files) ? 0 : files.size();
			final Iterator<? extends File>	fi=(numFiles <= 0) ? null : files.iterator(); 
			if (null == fi)
				return 0;

			final URL[]	urls=new URL[numFiles];
			for (int	uIndex=0; fi.hasNext(); uIndex++)
				urls[uIndex] = FileUtil.toURL(fi.next());
			
			uc = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
		}

		for ( ; ; )
		{
			try
			{
				final Map<? extends File,? extends Collection<Class<?>>>	cMap=
					JarUtils.getMatchingFileClasses(baseClass, uc, files);
				final Collection<? extends Map.Entry<? extends File,? extends Collection<Class<?>>>>	cl=
					((null == cMap) || (cMap.size() <= 0)) ? null : cMap.entrySet();
				if ((cl != null) && (cl.size() > 0))
				{
					for (final Map.Entry<? extends File,? extends Collection<Class<?>>>	ce : cl)
					{
						final File					f=(null == ce) ? null : ce.getKey();
						final Collection<Class<?>>	ml=(null == ce) ? null : ce.getValue();
						if ((null == ml) || (ml.size() <= 0))
							continue;

						out.println("\t" + f);
						for (final Class<?> cc : ml)
							out.println("\t\t" + cc.getName());
					}
				}
				else
					out.println("No matches found");
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}

			final String	ans=getval(out, in, "again (y)/[n]");
			if ((null == ans) || (ans.length() <= 0) || (Character.toLowerCase(ans.charAt(0)) != 'y'))
				break;
		}

		return 0;
	}
	
	public static final int testJarClassesLoader (
			final PrintStream out, final BufferedReader in, final Class<?> baseClass, final File ... files) throws MalformedURLException, IllegalArgumentException
	{
		return testJarClassesLoader(out, in, baseClass, ((null == files) || (files.length <= 0)) ? null : SetsUtils.setOf(FilePathComparator.ASCENDING, files));
	}
	// args[0]=base class (or none) - each following argument is JAR file
	public static final int testJarClassesLoader (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		Class<?>	baseClass=null;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	bcn=
				((numArgs > 0) && (aIndex <= 0)) ? args[0] : getval(out, in, "base class [ENTER=none]/(Q)uit");
			if (isQuit(bcn))
				return 0;

			if ((null == bcn) || (bcn.length() <= 0) || "NONE".equalsIgnoreCase(bcn))
				break;

			try
			{
				if (null == (baseClass=ClassUtil.loadClassByName(bcn)))
					throw new ClassNotFoundException("No class instance");
				break;
			}
			catch(ClassNotFoundException e)
			{
				System.err.println(e.getClass().getName() + "[" + bcn + "]: " + e.getMessage());
			}
		}

		for (int	aIndex=1; ; aIndex++)
		{
			final String	inPath=(aIndex < numArgs) ? args[aIndex] : getval(out, in, "JAR input path (or Quit)");
			if ((null == inPath) || (inPath.length() <= 0))
				continue;
			if (isQuit(inPath))
				break;

			try
			{
				testJarClassesLoader(out, in, baseClass, new File(inPath));
			}
			catch(Exception e)
			{
				System.err.println(e.getClass().getName() + ": " + e.getMessage());
			}
		}

		return 0;
	}
	
	//////////////////////////////////////////////////////////////////////////

	// args[i]=ZIP file path
	public static final int testZipEntriesEnum (final PrintStream out, final BufferedReader in, final String ... args)
	{
		final int	numArgs=(null == args) ? 0 : args.length;
		for (int	aIndex=0; ; aIndex++)
		{
			final String	filePath=
				(aIndex < numArgs) ? args[aIndex] : getval(out, in, "ZIP file path (or Quit)");
			if ((null == filePath) || (filePath.length() <= 0))
				continue;
			if (isQuit(filePath)) break;
			
			for ( ; ; )
			{
				out.println(filePath);

				try
				{
					final ZipInputStream	zipFile=
						new ZipInputStream(
								new BufferedInputStream(
										new FileInputStream(filePath), IOCopier.DEFAULT_COPY_SIZE));
					try
					{
						for (ZipEntry	ze=zipFile.getNextEntry(); ze != null; ze=zipFile.getNextEntry())
						{
							out.append(ze.getName())
							   .append('[').append(String.valueOf(ze.getSize())).append(']')
							   .append('\t')
							   .append(ze.getComment())
							   .println()
							   ;
						}
					}
					finally
					{
						FileUtil.closeAll(zipFile);
					}
				}
				catch(Exception e)
				{
					System.err.println(e.getClass().getName() + ": " + e.getMessage());
				}

				final String	ans=getval(out, in, "again [y]/n");
				if ((ans != null) && (ans.length() > 0) && (Character.toLowerCase(ans.charAt(0)) != 'y'))
					break;
			}
		}

		return 0;
	}

	//////////////////////////////////////////////////////////////////////////

	public static void main (String[] args)
	{
		final BufferedReader	in=getStdin();
//		final int				nErr=testJarEntriesHandler(System.out, in, args);
//		final int				nErr=testJarClassesLoader(System.out, in, args);
		final int				nErr=testZipEntriesEnum(System.out, in, args);
		if (nErr != 0)
			System.err.println("test failed (err=" + nErr + ")");
		else
			System.out.println("OK");
	}
}
