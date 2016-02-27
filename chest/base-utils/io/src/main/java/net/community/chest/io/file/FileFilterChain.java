/*
 * 
 */
package net.community.chest.io.file;

import java.io.File;
import java.io.FileFilter;
import java.util.Collection;

import net.community.chest.util.set.SetsUtils;
import net.community.chest.util.set.UniqueInstanceSet;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 13, 2009 2:18:19 PM
 */
public class FileFilterChain extends UniqueInstanceSet<FileFilter> implements FileFilter {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1590637771368808877L;
	private boolean	_conjFilter;
	public boolean isConjunctiveFilter ()
	{
		return _conjFilter;
	}

	public void setConjunctiveFilter (boolean f)
	{
		_conjFilter = f;
	}

	public FileFilterChain (boolean conjFilter)
	{
		super(FileFilter.class);
		_conjFilter = conjFilter;
	}

	public FileFilterChain (boolean conjFilter, Collection<? extends FileFilter> c)
	{
		super(FileFilter.class, c);
		_conjFilter = conjFilter;
	}

	public FileFilterChain (Collection<? extends FileFilter> c)
	{
		this(true, c);
	}

	public FileFilterChain (boolean conjFilter, FileFilter ... filters)
	{
		this(conjFilter, SetsUtils.uniqueSetOf(filters));
	}

	public FileFilterChain (FileFilter ... filters)
	{
		this(true, filters);
	}

	public FileFilterChain ()
	{
		this(true);
	}

	public static final boolean accept (final File f, final boolean conjFilter, final Collection<? extends FileFilter> fs)
	{
		if (null == f)
			return false;

		if ((null == fs) || (fs.size() <= 0))	// OK if no filters set
			return true;

		for (final FileFilter ff : fs)
		{
			if (null == ff)
				continue;

			final boolean	fa=ff.accept(f);
			if (conjFilter)
			{
				if (!fa)	// for AND 1st failure is enough
					return false;
			}
			else
			{
				if (fa)	// for OR 1st success is enough
					return true;
			}
		}

		return conjFilter;
	}

	public static final boolean accept (final File f, final boolean conjFilter, final FileFilter ... fs)
	{
		return accept(f, conjFilter, ((null == f) || (null == fs) || (fs.length <= 0)) ? null : SetsUtils.uniqueSetOf(fs));
	}
	/*
	 * @see java.io.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept (File f)
	{
		return accept(f, isConjunctiveFilter(), this);
	}
}
