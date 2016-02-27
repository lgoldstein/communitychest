package net.community.chest.io;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.StreamCorruptedException;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.community.chest.util.map.MapEntryImpl;
import net.community.chest.util.set.SetsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Useful file(s) related utilities</P>
 * 
 * @author Lyor G.
 * @since Jul 25, 2007 2:05:58 PM
 */
public final class FileUtil {
	private FileUtil ()
	{
		// no instance
	}
	/**
	 * @param org extension value to be checked - if null/empty then nothing
	 * is done
	 * @param addDot TRUE=add a preceding '.' if one is not already there.
	 * FALSE=remove preceding '.' if any
	 * @return adjusted result - may be null/empty if null/empty string to
	 * being with or nothing left after stripping a '.'
	 */
	public static final String adjustExtension (final String org, final boolean addDot)
	{
		final String	ext=(null == org) ? null : org.trim();
		if ((null == ext) || (ext.length() <= 0))
			return ext;

		if (ext.charAt(0) == '.')
		{
			if (addDot)
				return ext;	// already have the dot
			else
				return ext.substring(1);	// don't want the existing dot
		}
		else	// not starting with a dot
		{
			if (addDot)
				return "." + ext;	// add the missing dot
			else
				return ext;	// no preceding dot to begin with
		}
	}
	/**
	 * @param name The original name that may (or may not) contain a dot
	 * @param withDot TRUE=retrieve the extension with the dot, FALSE=return
	 * only the &quot;pure&quot; extension
	 * @return The extension with/out the dot after the last dot (if any) -
	 * <code>null</code> if no extension found
	 */
	public static final String getExtension (final String name, final boolean withDot)
	{
		final int	nLen=(null == name) ? 0 : name.length(),
					ePos=(nLen <= 0) ? (-1) : name.lastIndexOf('.');
		if ((ePos < 0) || (ePos >= (nLen-1)))
			return null;

		return withDot ? name.substring(ePos) : name.substring(ePos + 1);
	}
	/**
	 * Adds an extension to the file name if not already exists
	 * @param name The file name - if <code>null</code>/empty then nothing
	 * is done
	 * @param ext The extension to add - if <code>null</code>/empty then
	 * nothing is done and input parameter is returned unchanged
	 * @return The file name with the extension added to it. If the name
	 * already ends in the specified extension (case <U>insensitive</U>) then
	 * nothing is done and the same input is returned
	 */
	public static final String adjustFileName (final String name, final String ext)
	{
		final int	nLen=(null == name) ? 0 : name.length(),
					eLen=(null == ext) ? 0 : ext.length();
		if ((nLen <= 0) || (eLen <= 0))
			return name;

		final String	extComp=adjustExtension(ext, true),
						sfxValue=getExtension(name, true);
		if (extComp.equalsIgnoreCase(sfxValue))
			return name;

		return name + extComp;
	}
	/**
	 * Strips the specified extension - if found in the file name
	 * @param name The file name - if <code>null</code>/empty then nothing
	 * is done
	 * @param ext The extension to remove - if <code>null</code>/empty then
	 * nothing is done and input parameter is returned unchanged
	 * @return The input name with the extension removed - if found in the
	 * file name (case <U>insensitive</U>). If nothing stripped, then input
	 * name is returned unchanged
	 */
	public static final String stripExtension (final String name, final String ext)
	{
		final int	nLen=(null == name) ? 0 : name.length(),
					eLen=(null == ext) ? 0 : ext.length();
		if ((nLen <= 0) || (eLen <= 0))
			return name;

		final String	extComp=adjustExtension(ext, true),
						sfxValue=getExtension(name, true);
		if (extComp.equalsIgnoreCase(sfxValue))
			return name.substring(0, nLen - sfxValue.length());
	
		return name;
	}
	/**
	 * Writes the specified data to the {@link Appendable} and after it the
	 * {@link System#getProperty(String)} of the <code>line.separator</code>
	 * @param <A> The {@link Appendable} type to be used
	 * @param writer The {@link Appendable} to write to
	 * @param c data to be written <U>before</U> the newline - ignored
	 * if null/empty
	 * @return same as input {@link Appendable} instance
	 * @throws IOException if no {@link Appendable} or failed to write
	 */
	public static final <A extends Appendable> A writeln (final A writer, final CharSequence c) throws IOException
	{
		if (null == writer)
			throw new StreamCorruptedException("writeln(" + c + ") no " + Writer.class.getSimpleName() + " instance");
		if ((c != null) && (c.length() > 0))
			writer.append(c);

		return EOLStyle.LOCAL.appendEOL(writer);
	}
	/**
	 * Writes the {@link System#getProperty(String)} of the <code>line.separator</code>
	 * @param <A> The {@link Appendable} type to be used
	 * @param writer The {@link Appendable} to write to
	 * @return same as input {@link Appendable} instance
	 * @throws IOException if no {@link Appendable} or failed to write
	 */
	public static final <A extends Appendable> A writeln (A writer) throws IOException
	{
		return writeln(writer, null);
	}
	/**
	 * @param path The file name/path - may be null/empty
	 * @param exts The extension(s) to match with - may be null/empty (in which
	 * case no match is assumed) and may (or not) contain the '.'
	 * @return <code>true</code> if file name/path ends in specified extension
	 */
	public static final boolean isMatchingFileSuffix (final String path, final Collection<String> exts)
	{
		final int	pLen=(null == path) ? 0 : path.length();
		if ((pLen <= 0) || (null == exts) || (exts.size() <= 0))
			return false;

		final String	sfx=getExtension(path, true);
		if ((null == sfx) || (sfx.length() <= 0))
			return false;

		for (final String	e : exts)
		{
			final String	efx=adjustExtension(e, true);
			if (sfx.equalsIgnoreCase(efx))
				return true;
		}

		return false;
	}
	/**
	 * @param path The file name/path - may be null/empty
	 * @param exts The extension(s) to match with - may be null/empty (in which
	 * case no match is assumed) and may (or not) contain the '.'
	 * @return <code>true</code> if file name/path ends in specified extension
	 */
	public static final boolean isMatchingFileSuffix (final String path, final String ... exts)
	{
		return isMatchingFileSuffix(path, SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, exts));
	}
	/**
	 * Locates all <U>files</U> in a folder whose extension matches the
	 * provided one(s)
	 * @param org Original {@link Collection} of {@link File}-s to which to
	 * add the matches - if <code>null</code> and matches found, a new one
	 * will be created
	 * @param root Root <U>folder</U> to search - if <code>null</code> or
	 * not a folder then nothing is updated
	 * @param recursive <code>true</code>=if any of the sub-files is a folder
	 * then recursively scan it as well
	 * @param exts A {@link Collection} of extensions to be matched - if
	 * <code>null</code>/empty then nothing is updated
	 * @return Updated {@link Collection} of all <U>files</U> - may be same
	 * as input (including <code>null</code>/empty) if nothing updated
	 */
	public static final Collection<File> updateMatchingFilesBySuffix (
			final Collection<File> org, final File root, final boolean recursive, final Collection<String> exts)
	{
		final File[]	fa=((null == root) || (!root.isDirectory())) ? null : root.listFiles();
		if ((null == fa) || (fa.length <= 0)
		 || (null == exts) || (exts.size() <= 0))
			return org;

		Collection<File>	ret=org;
		for (final File f : fa)
		{
			final String	fn=(null == f) ? null : f.getName();
			if ((null == fn) || (fn.length() <= 0))
				continue;

			if (f.isDirectory())
			{
				if (recursive)
					ret = updateMatchingFilesBySuffix(ret, f, recursive, exts);
				continue;
			}

			if (!f.isFile())
				continue;

			for (final String ext : exts)
			{
				if (!isMatchingFileSuffix(fn, ext))
					continue;

				if (null == ret)
					ret = new LinkedList<File>();
				ret.add(f);
				break;
			}
		}

		return ret;
	}

	public static final Collection<File> updateMatchingFilesBySuffix (
			final Collection<File> org, final File root, final boolean recursive, final String ...exts)
	{
		return updateMatchingFilesBySuffix(org, root, recursive, SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, exts));
	}

	public static final Collection<File> getMatchingFilesBySuffix (final File root, final boolean recursive, final Collection<String> exts)
	{
		return updateMatchingFilesBySuffix(null, root, recursive, exts);
	}

	public static final Collection<File> getMatchingFilesBySuffix (final File root, final boolean recursive, final String ... exts)
	{
		return getMatchingFilesBySuffix(root, recursive, SetsUtils.setOf(String.CASE_INSENSITIVE_ORDER, exts));
	}

	public static final String	FILE_PROTOCOL_NAME="file";
	public static final boolean isFileResource (final URL url)
    {
    	final String	scheme=(null == url) ? null : url.getProtocol();
    	if (FILE_PROTOCOL_NAME.equalsIgnoreCase(scheme))
    		return true;
  
    	return false;
    }
    // must ask "isFileResource" first
    public static final File toFile (final URL url) throws IllegalArgumentException
    {
    	try
    	{
    		final URI	uri=(null == url) ? null : url.toURI();
    		return (null == uri) ? null : new File(uri);
    	}
    	catch(URISyntaxException e)
    	{
    		throw new IllegalArgumentException("toFile(" + url + ") " + e.getClass().getName() + ": " + e.getMessage());
    	}
    }

    public static final URL toURL (final File f) throws MalformedURLException
    {
		final URI	uri=(null == f) ? null : f.toURI();
		return (null == uri) ? null : uri.toURL();
    }

    public static final List<URL> toURL (final Collection<? extends File> files) throws MalformedURLException
    {
		final int	numFiles=(files == null) ? 0 : files.size();
		if (numFiles <= 0)
			return Collections.emptyList();

		final List<URL>	urls=new ArrayList<URL>(numFiles);
		for (final File f : files)
		{
			final URL	url=toURL(f);
			if (url == null)
				continue;
			if (!urls.add(url))
				continue;	// debug breakpoint
		}

		return urls;
    }
    
    public static final List<URL> toURL (final File ... files) throws MalformedURLException
    {
    	return toURL(((null == files) || (files.length <= 0)) ? null : Arrays.asList(files));
    }
    /**
     * Recursively scans a folder and removes all files/sub-folders in it
     * @param rootFolder The root <U>folder</U> to be scanned - ignored if
     * <code>null</code>/not exists/not a folder
     * @param delRoot <code>true</code>=delete the provided root folder as
     * well (<u>after</u> having deleted its contents)
     * @return A {@link Collection} of result "pairs" representing the
     * deletion results - key=the {@link File}, value=the deletion
     * success/fail result {@link Boolean} (<B>Note:</B> if not required to
     * delete the provided root folder then it will not be a part of the
     * returned result).
     */
    public static final Collection<Map.Entry<File,Boolean>> deleteAll (final File rootFolder, final boolean delRoot)
    {
    	Collection<Map.Entry<File,Boolean>>	ret=null;
    	if ((null == rootFolder) || (!rootFolder.exists()) || (!rootFolder.isDirectory()))
    		return null;

    	final File[]	files=rootFolder.listFiles();
    	if ((files != null) && (files.length > 0))
    	{
	    	for (final File f : files)
	    	{
	    		if (null == f)
	    			continue;
	    		if (f.isFile())
	    		{
	    			final boolean	delRes=f.delete();
	    			if (null == ret)
	    				ret = new LinkedList<Map.Entry<File,Boolean>>();
	    			ret.add(new MapEntryImpl<File,Boolean>(f, Boolean.valueOf(delRes)));
	    		}
	    		else if (f.isDirectory())
	    		{
	    			final Collection<Map.Entry<File,Boolean>>	subDel=deleteAll(f, true);
	    			if (null == ret)
	    				ret = subDel;
	    			else
	    				ret.addAll(subDel);
	    		}
	    		else
	    		{
	    			if (null == ret)
	    				ret = new LinkedList<Map.Entry<File,Boolean>>();
	    			ret.add(new MapEntryImpl<File,Boolean>(f, Boolean.FALSE));
	    		}
	    	}
    	}

    	if (delRoot)
    	{
			final boolean	delRes=rootFolder.delete();
			if (null == ret)
				ret = new LinkedList<Map.Entry<File,Boolean>>();
			ret.add(new MapEntryImpl<File,Boolean>(rootFolder, Boolean.valueOf(delRes)));
    	}

    	return ret;
    }
	/**
	 * Closes all the provided streams - if any of them throws an {@link IOException}
	 * while its {@link Closeable#close()} method is called, it is caught and
	 * deferred till the <U>end</U> of closing all the remaining streams
	 * @param streams The {@link Closeable} streams to close
	 * @throws IOException If any stream threw an {@link IOException} while its
	 * {@link Closeable#close()} method was called
	 */
	public static void closeAll (Collection<? extends Closeable> streams) throws IOException
	{
		final int	numStreams=(streams == null) ? 0 : streams.size();
		if (numStreams <= 0)
			return;
	
		IOException	err=null;	// TODO return more information about which streams threw an exception
		for (final Closeable c : streams)
		{
			if (c == null)
				continue;
	
			try
			{
				c.close();
			}
			catch(IOException e)
			{
				err = e;
			}
		}
	
		if (err != null)
			throw err;
	}

	public static void closeAll (Closeable ... streams) throws IOException
	{
		closeAll(((streams == null) || (streams.length <= 0)) ? null : Arrays.asList(streams));
	}
}
