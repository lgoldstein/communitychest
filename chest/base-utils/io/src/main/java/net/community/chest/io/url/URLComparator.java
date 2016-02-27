/*
 * 
 */
package net.community.chest.io.url;

import java.net.URL;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Compares 2 URL by comparing the protocol and then the path - all
 * case <U>insensitive</U></P>
 * @author Lyor G.
 * @since Nov 10, 2008 4:02:45 PM
 */
public class URLComparator extends ByPathURLComparator {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5629573517967154970L;

	public URLComparator (boolean ascending) throws IllegalArgumentException
	{
		super(ascending);
	}
	/*
	 * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compareValues (final URL v1, final URL v2)
	{
		final String	p1=(null == v1) ? null : v1.getProtocol(),
						p2=(null == v2) ? null : v2.getProtocol();
		final int		nRes=StringUtil.compareDataStrings(p1, p2, false);
		if (nRes != 0)
			return nRes;

		return super.compareValues(v1, v2);
	}

	public static final URLComparator	ASCENDING=new URLComparator(true),
										DESCENDING=new URLComparator(false);
}
