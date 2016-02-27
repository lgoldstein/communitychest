/*
 * 
 */
package net.community.chest.awt.font;

import java.awt.font.TextAttribute;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulate pre-defined {@link TextAttribute#INPUT_METHOD_UNDERLINE} values</P>
 *
 * @author Lyor G.
 * @since Jun 18, 2009 8:19:17 AM
 */
public enum FontInputMethodUnderline implements FontIntAttributeValue {
	NONE(Integer.valueOf(-1)),
	LOW1PIXEL(TextAttribute.UNDERLINE_LOW_ONE_PIXEL),
	LOW2PIXEL(TextAttribute.UNDERLINE_LOW_TWO_PIXEL),
	LOWDOTTED(TextAttribute.UNDERLINE_LOW_DOTTED),
	LOWGRAY(TextAttribute.UNDERLINE_LOW_GRAY),
	LOWDASHED(TextAttribute.UNDERLINE_LOW_DASHED);

	private final Integer	_v;
	/*
	 * @see net.community.chest.awt.font.FontIntAttributeValue#getAttributeValue()
	 */
	@Override
	public final Integer getAttributeValue ()
	{
		return _v;
	}
	
	FontInputMethodUnderline (Integer v)
	{
		_v = v;
	}

	public static final List<FontInputMethodUnderline>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FontInputMethodUnderline fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final FontInputMethodUnderline fromValue (final int n)
	{
		return FontUtils.fromAttributeValue(n, VALUES);
	}

	public static final FontInputMethodUnderline fromValue (final Integer n)
	{
		return (null == n) ? null : fromValue(n.intValue());
	}
}
