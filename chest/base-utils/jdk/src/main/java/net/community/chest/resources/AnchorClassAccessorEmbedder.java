/*
 * 
 */
package net.community.chest.resources;

import java.net.URL;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Implements the {@link AnchoredResourceAccessor} interface by using
 * an anchor {@link Class} instance</P>
 * 
 * @author Lyor G.
 * @since Feb 4, 2009 11:22:05 AM
 */
public class AnchorClassAccessorEmbedder implements AnchoredResourceAccessor {
	private Class<?>	_ancClass	/* =null */;
	public Class<?> getAnchor ()
	{
		return _ancClass;
	}

	public void setAnchor (Class<?> c)
	{
		if (_ancClass != c)
			_ancClass = c;	// debug breakpoint
	}

	public AnchorClassAccessorEmbedder (Class<?> c)
	{
		_ancClass = c;
	}

	public AnchorClassAccessorEmbedder ()
	{
		this(null);
	}
	/*
	 * @see net.community.chest.resources.AnchoredResourceAccessor#getResource(java.lang.String)
	 */
	@Override
	public URL getResource (final String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;

		final Class<?>	a=getAnchor();
		if (null == a)
			return null;

		return a.getResource(name);
	}
}
