package net.community.chest.ui.helpers.filechooser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.SortedMap;
import java.util.TreeMap;

import net.community.chest.CoVariantReturn;
import net.community.chest.io.FileUtil;
import net.community.chest.io.filter.AbstractRootFolderFilesFilter;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 26, 2007 2:04:28 PM
 */
public class SuffixesFileFilter extends AbstractRootFolderFilesFilter {
	private final SortedMap<String,String>	_suffixesMap=new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);
	/**
	 * Adds a suffix to the list of {@link #accept(File)}-ed files
	 * @param s original suffix - ignored if null/empty or only "."
	 */
	public void addSuffix (final String s)
	{
		final String	ext=FileUtil.adjustExtension(s, true);
		if ((ext != null) && (ext.length() > 1))
			_suffixesMap.put(ext, ext);
	}

	public void addSuffixes (final String ... exts)
	{
		if ((null == exts) || (exts.length <= 0))
			return;

		for (final String s : exts)
			addSuffix(s);
	}
	/**
	 * Checks which/if a suffix is actually being {@link #accept(File)}-ed
	 * by the filter. <B>Note:</B> suffix is being checked according to the
	 * define case sensitivity acceptance "mode"
	 * @param s original suffix
	 * @return TRUE if this suffix is being filtered by
	 */
	public boolean isFilteredSuffix (final String s)
	{
		final String	ext=FileUtil.adjustExtension(s, true);
		if ((ext != null) && (ext.length() > 1))
		{
			final String	v=_suffixesMap.get(ext);
			if (isCaseSensitive())
				return ext.equals(v);
			else
				return (v != null) && (v.length() > 0);
		}

		return false;
	}
	/**
	 * @param description original description - may be null/empty
	 * @param caseSensitive TRUE=suffixes checking is case sensitive
	 */
	public SuffixesFileFilter (final String description, final boolean caseSensitive)
	{
		setDescription(description);
		setCaseSensitive(caseSensitive);
	}
	/**
	 * Creates a case <U>insensitive</U> filter
	 * @param description original description - may be null/empty
	 */
	public SuffixesFileFilter (String description)
	{
		this(description, false);
	}
	/**
	 * No initial description
	 * @param caseSensitive TRUE=suffixes checking is case sensitive
	 */
	public SuffixesFileFilter (final boolean caseSensitive)
	{
		this(null, caseSensitive);
	}
	/**
	 * Default/empty constructor - creates a case <U>insensitive</U> filter
	 */
	public SuffixesFileFilter ()
	{
		this(false);
	}
	/*
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept (final File f)
	{
		if (!super.accept(f))
			return false;

		final String	name=f.getName();
		final int		nLen=(null == name) /* should not happen */ ? 0 : name.length(),
						sPos=(nLen <= 1) ? 0 : name.lastIndexOf('.');
		final String	sfx=((sPos <= 0) || (sPos >= (nLen-1))) ? null : name.substring(sPos),
						ext=((null == sfx) || (sfx.length() <= 0)) ? null : _suffixesMap.get(sfx);
		if ((ext != null) && (ext.length() > 0))
		{
			if (isCaseSensitive())
				return ext.equals(sfx);
			else
				return true;
		}

		return false;
	}

	public static final String	SUFFIXES_ATTR="suffixes";
	public static final char SUFFIX_DELIM=',';
	// returns read suffixes list - null/empty if none set
	public String addSuffixes (final Element elem) throws Exception
	{
		if (null == elem)
			throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(getClass(), "addSuffixes") + " no " + Element.class.getName() + " instance");

		final String				s=elem.getAttribute(SUFFIXES_ATTR);
		final Collection<String>	vals=StringUtil.splitString(s,SUFFIX_DELIM);
		if ((vals != null) && (vals.size() > 0))
		{
			for (final String v : vals)
			{
				final String	cv=StringUtil.getCleanStringValue(v);
				if ((null == cv) || (cv.length() <= 0))
					continue;
				addSuffix(cv);
			}
		}

		return s;
	}
	/*
	 * @see net.community.chest.swing.component.filechooser.BaseFileFilter#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public SuffixesFileFilter fromXml (final Element elem) throws Exception
	{
		final FilenameFilter	f=super.fromXml(elem);
		if (f != this)
			throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched recovered instance");

		addSuffixes(elem);
		return this;
	}

	public SuffixesFileFilter (final Element elem) throws Exception
	{
		final SuffixesFileFilter	f=fromXml(elem);
		if (f != this)
			throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched recovered instance");
	}
}
