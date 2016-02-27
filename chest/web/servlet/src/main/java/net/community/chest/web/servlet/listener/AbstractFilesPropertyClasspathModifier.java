/*
 * 
 */
package net.community.chest.web.servlet.listener;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.community.chest.lang.StringUtil;

/**
 * Provides a {@link ServletContextListener#contextInitialized(ServletContextEvent)} implementation
 * that adds {@link File}-s to the current {@link Thread#getContextClassLoader()}
 * by accessing a {@link String} property value that contains the list of file paths
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Jun 6, 2011 10:48:57 AM
 */
public abstract class AbstractFilesPropertyClasspathModifier extends AbstractFilesClasspathModifier {
	protected AbstractFilesPropertyClasspathModifier ()
	{
		super();
	}
	/**
	 * @return A {@link String} containing a list of file paths separated by
	 * the character specified in {@link #getClasspathFilesListSeparator()} -
	 * ignored if <code>null</code>/empty
	 */
	protected abstract String getClasspathFilesList ();
	/**
	 * @return The character used to separate the files list in the {@link String}
	 * return by the {@link #getClasspathFilesList()} call - default={@link File#pathSeparatorChar}
	 */
	protected char getClasspathFilesListSeparator ()
	{
		return File.pathSeparatorChar;
	}
	/*
	 * @see net.community.chest.web.servlet.listener.AbstractFilesClasspathModifier#getClasspathFiles()
	 */
	@Override
	protected Collection<? extends File> getClasspathFiles ()
	{
		final List<String>	paths=StringUtil.splitString(getClasspathFilesList(), getClasspathFilesListSeparator());
		final int			numPaths=(paths == null) ? 0 : paths.size();
		if (numPaths <= 0)
			return Collections.emptyList();

		final List<File>	files=new ArrayList<File>(numPaths);
		for (final String p : paths)
		{
			final File	f=((p == null) || (p.length() <= 0)) ? null : new File(p);
			if (f == null)
				continue;
			if (!files.add(f))
				continue;	// debug breakpoint
		}

		return files;
	}

}
