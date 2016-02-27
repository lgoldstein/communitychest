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
 * <P>Encapsulate pre-defined {@link TextAttribute#UNDERLINE} values</P>
 * 
 * @author Lyor G.
 * @since Jun 18, 2009 8:10:20 AM
 */
public enum FontUnderlineValue implements FontIntAttributeValue {
	ON(TextAttribute.UNDERLINE_ON),
	OFF(Integer.valueOf(-1));

	private final Integer	_v;
	/*
	 * @see net.community.chest.awt.font.FontIntAttributeValue#getAttributeValue()
	 */
	@Override
	public final Integer getAttributeValue ()
	{
		return _v;
	}
	
	FontUnderlineValue (Integer v)
	{
		_v = v;
	}

	public static final List<FontUnderlineValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FontUnderlineValue fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final FontUnderlineValue fromValue (final int n)
	{
		return FontUtils.fromAttributeValue(n, VALUES);
	}

	public static final FontUnderlineValue fromValue (final Integer n)
	{
		return (null == n) ? null : fromValue(n.intValue());
	}
}
