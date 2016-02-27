/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.dom.DOMUtils;
import net.community.chest.util.collection.CollectionsUtils;

import org.jfree.ui.HorizontalAlignment;
import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 27, 2009 3:29:00 PM
 */
public enum HAlignment {
	LEFT(HorizontalAlignment.LEFT),
	RIGHT(HorizontalAlignment.RIGHT),
	CENTER(HorizontalAlignment.CENTER);

	private final HorizontalAlignment	_align;
	public final HorizontalAlignment getAlignment ()
	{
		return _align;
	}

	HAlignment (HorizontalAlignment a)
	{
		_align = a;
	}

	public static final List<HAlignment>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final HAlignment fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final HAlignment fromAlignment (final HorizontalAlignment a)
	{
		if (null == a)
			return null;

		for (final HAlignment  v : VALUES)
		{
			if ((v != null) && a.equals(v.getAlignment()))
				return v;
		}

		return null;
	}

	public static final String	HALIGN_ATTR=HorizontalAlignment.class.getSimpleName(), HALIGN_ALIAS="halign";
	public static final Attr getAlignmentAttribute (Element elem)
	{
		return DOMUtils.findFirstAttribute(elem, false, HALIGN_ALIAS, HALIGN_ATTR);
	}

	public static final HAlignment getAlignment (Element elem) throws DOMException
	{
		final Attr		aa=getAlignmentAttribute(elem);
		final String	s=(null == aa) ? null : aa.getValue();
		if ((null == s) || (s.length() <= 0))
			return null;

		final HAlignment	a=fromString(s);
		if (null == a)
			throw new DOMException(DOMException.NOT_FOUND_ERR, "getAlignment(" + DOMUtils.toString(elem) + ") unknown value: " + s);

		return a;
	}

	public static final HorizontalAlignment getAlignmentValue (Element elem) throws DOMException
	{
		final HAlignment	a=getAlignment(elem);
		if (null == a)
			return null;

		return a.getAlignment();
	}

	public static final HorizontalAlignment getAlignmentValue (Element elem, HorizontalAlignment defAlign) throws DOMException
	{
		final HorizontalAlignment	a=getAlignmentValue(elem);
		if (a != null)
			return a;
		else
			return defAlign;
	}
}
