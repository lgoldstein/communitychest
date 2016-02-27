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
 * <P>Encapsulate pre-defined {@link TextAttribute#LIGATURES} values</P>
 * @author Lyor G.
 * @since Jun 18, 2009 8:26:22 AM
 */
public enum FontLigaturesValue implements FontIntAttributeValue {
	ON(TextAttribute.LIGATURES_ON),
	OFF(Integer.valueOf(0));

	private final Integer	_v;
	/*
	 * @see net.community.chest.awt.font.FontIntAttributeValue#getAttributeValue()
	 */
	@Override
	public final Integer getAttributeValue ()
	{
		return _v;
	}
	
	FontLigaturesValue (Integer v)
	{
		_v = v;
	}

	public static final List<FontLigaturesValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FontLigaturesValue fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final FontLigaturesValue fromValue (final int n)
	{
		return FontUtils.fromAttributeValue(n, VALUES);
	}

	public static final FontLigaturesValue fromValue (final Integer n)
	{
		return (null == n) ? null : fromValue(n.intValue());
	}
}
