/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.dom.DOMUtils;
import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.ui.VerticalAlignment;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 27, 2009 3:33:07 PM
 */
public enum VAlignment {
	TOP(VerticalAlignment.TOP),
	BOTTOM(VerticalAlignment.BOTTOM),
	CENTER(VerticalAlignment.CENTER);

	private final VerticalAlignment	_align;
	public final VerticalAlignment getAlignment ()
	{
		return _align;
	}

	VAlignment (VerticalAlignment a)
	{
		_align = a;
	}

	public static final List<VAlignment>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final VAlignment fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final VAlignment fromAlignment (final VerticalAlignment a)
	{
		if (null == a)
			return null;

		for (final VAlignment  v : VALUES)
		{
			if ((v != null) && a.equals(v.getAlignment()))
				return v;
		}

		return null;
	}

	public static final String	VALIGN_ATTR=VerticalAlignment.class.getSimpleName(), VALIGN_ALIAS="valign";
	public static final Attr getAlignmentAttribute (Element elem)
	{
		return DOMUtils.findFirstAttribute(elem, false, VALIGN_ALIAS, VALIGN_ATTR);
	}

	public static final VAlignment getAlignment (Element elem) throws DOMException
	{
		final Attr		aa=getAlignmentAttribute(elem);
		final String	s=(null == aa) ? null : aa.getValue();
		if ((null == s) || (s.length() <= 0))
			return null;

		final VAlignment	a=fromString(s);
		if (null == a)
			throw new DOMException(DOMException.NOT_FOUND_ERR, "getAlignment(" + DOMUtils.toString(elem) + ") unknown value: " + s);

		return a;
	}

	public static final VerticalAlignment getAlignmentValue (Element elem) throws DOMException
	{
		final VAlignment	a=getAlignment(elem);
		if (null == a)
			return null;

		return a.getAlignment();
	}

	public static final VerticalAlignment getAlignmentValue (Element elem, VerticalAlignment defAlign) throws DOMException
	{
		final VerticalAlignment	a=getAlignmentValue(elem);
		if (a != null)
			return a;
		else
			return defAlign;
	}
}
