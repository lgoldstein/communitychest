/*
 * 
 */
package net.community.apps.tools.xslapply;

import java.io.File;

import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used to populate a combox box or a list with {@link File} items that
 * can be in a selected state (or not) - represented by their {@link Boolean}
 * value (a <code>null</code> value means that selection state is "unknown")</P>
 *  
 * @author Lyor G.
 * @since Dec 11, 2008 9:15:48 AM
 */
public class SelectionFilePair extends MapEntryImpl<File,Boolean> {
	public SelectionFilePair ()
	{
		super();
	}

	public SelectionFilePair (final File f, final Boolean selState)
	{
		super(f, selState);
	}
	// NOTE !!! marks file as selected by default
	public SelectionFilePair (final File f)
	{
		this(f, Boolean.TRUE);
	}

	public File getFile ()
	{
		return getKey();
	}

	public void setFile (final File f)
	{
		setKey(f);
	}

	public Boolean getSelectionState ()
	{
		return getValue();
	}

	public void setSelectionState (Boolean s)
	{
		setValue(s);
	}
	// NOTE !!! interprets "null" as not-selected
	public boolean isSelected ()
	{
		final Boolean	s=getSelectionState();
		return (s != null) && s.booleanValue();
	}

	public void setSelected (boolean s)
	{
		setSelectionState(Boolean.valueOf(s));
	}
	/*
	 * @see net.community.chest.util.map.MapEntryImpl#toString()
	 */
	@Override
	public String toString ()
	{
		final File	f=getKey();
		return (null == f) ? "" : f.getAbsolutePath();
	}
}
