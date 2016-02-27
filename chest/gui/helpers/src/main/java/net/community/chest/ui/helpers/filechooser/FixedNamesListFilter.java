/*
 * 
 */
package net.community.chest.ui.helpers.filechooser;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashSet;
import java.util.TreeSet;

import net.community.chest.dom.DOMUtils;
import net.community.chest.io.filter.AbstractRootFolderFilesFilter;
import net.community.chest.lang.StringUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 9, 2009 2:44:56 PM
 */
public class FixedNamesListFilter extends AbstractRootFolderFilesFilter {
	/**
	 * Allowed file(s) name(s) - if null/empty then no file matches
	 */
	private Collection<String>	_namesList;
	public Collection<String> getNamesList ()
	{
		return _namesList;
	}

	public Collection<String> addName (final String n)
	{
		Collection<String>	nl=getNamesList();
		if ((n != null) && (n.length() > 0))
		{
			if (null == nl)
			{
				nl = isCaseSensitive()
					? new HashSet<String>()
					: new TreeSet<String>(String.CASE_INSENSITIVE_ORDER)
					;
				setNamesList(nl);
			}

			nl.add(n);
		}

		return nl;
	}

	public void setNamesList (Collection<String> namesList)
	{
		_namesList = namesList;
	}
	
	public FixedNamesListFilter (String description, Collection<String> nl, boolean caseSensitive)
	{
		_namesList = nl;
		setDescription(description);
		setCaseSensitive(caseSensitive);
	}
	
	public FixedNamesListFilter (String description, Collection<String> nl)
	{
		this(description, nl, false);
	}

	public FixedNamesListFilter (Collection<String> nl, boolean caseSensitive)
	{
		this(null, nl, caseSensitive);
	}

	public FixedNamesListFilter (Collection<String> nl)
	{
		this(nl, false);
	}
	
	public FixedNamesListFilter (String description, boolean caseSensitive)
	{
		this(description, null, caseSensitive);
	}
	
	public FixedNamesListFilter (String description)
	{
		this(description, false);
	}
	
	public FixedNamesListFilter ()
	{
		this(null, null, false);
	}
	/*
	 * @see net.community.chest.swing.component.filechooser.BaseFileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept (File f)
	{
		if (!super.accept(f))
			return false;

		final String				n=f.getName();
		final Collection<String>	nl=getNamesList();
		if ((null == nl) || (nl.size() <= 0))
			return false;

		final boolean	caseSensitive=isCaseSensitive();
		for (final String nv : nl)
		{
			if (StringUtil.compareDataStrings(n, nv, caseSensitive) == 0)
				return true;
		}

		return false;
	}

	public static final String	NAMESLIST_ATTR="namesList";
	public Collection<String> setNamesList (Element elem)
	{
		final String				nvl=
			(null == elem) ? null : elem.getAttribute(NAMESLIST_ATTR);
		final Collection<String>	nl=StringUtil.splitString(nvl, ',');
		if ((nl != null) && (nl.size() > 0))
			setNamesList(nl);
		return nl;
	}
	/*
	 * @see net.community.chest.swing.component.filechooser.BaseFileFilter#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public FixedNamesListFilter fromXml (Element elem) throws Exception
	{
		final FilenameFilter	f=super.fromXml(elem);
		if (f != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + " mismatched recovered instance");

		setNamesList(elem);
		return this;
	}
	/*
	 * @see net.community.chest.swing.component.filechooser.BaseFileFilter#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final Element	elem=super.toXml(doc);
		final String	nvl=StringUtil.asStringList(',', getNamesList());
		if ((nvl != null) && (nvl.length() > 0))
			elem.setAttribute(NAMESLIST_ATTR, nvl);
		return elem;
	}
}
