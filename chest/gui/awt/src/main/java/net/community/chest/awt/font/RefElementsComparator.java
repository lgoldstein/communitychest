package net.community.chest.awt.font;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to organize the XML {@link Element}-s in the {@link FontUtils#updateFontsMap(Map, Map)}
 * call so that base {@link java.awt.Font} definitions come first, and derived ones
 * come last.</P>
 * 
 * @author Lyor G.
 * @since Jul 30, 2007 8:56:32 AM
 */
public final class RefElementsComparator implements Comparator<Map.Entry<String,? extends Element>>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2402931698318358556L;
	public RefElementsComparator ()
	{
		// do nothing
	}

	public static final int compare (final String r1, final String r2)
	{
		if ((null == r1) || (r1.length() <= 0))
			return ((null == r2) || (r2.length() <= 0)) ? 0 : (-1);	// "push" null ref-id(s) first
		else
			return (+1);
	}

	public static final int compare (final Element e1, final Element e2)
	{
		return compare((null == e1) ? null : e1.getAttribute(FontUtils.REFID_ATTR), (null == e2) ? null : e2.getAttribute(FontUtils.REFID_ATTR));
	}
	/*
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare (final Map.Entry<String, ? extends Element> o1, final Map.Entry<String, ? extends Element> o2)
	{
		return compare((null == o1) ? null : o1.getValue(), (null == o2) ? null : o2.getValue());	
	}
}