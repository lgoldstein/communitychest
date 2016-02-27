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
 * <P>Encapsulate well-known constant for {@link TextAttribute#WIDTH}</P>
 * @author Lyor G.
 * @since Jun 18, 2009 7:38:38 AM
 */
public enum FontWidthValue implements FontFloatAttributeValue {
	CONDENSED(TextAttribute.WIDTH_CONDENSED),
	SEMICONDENSED(TextAttribute.WIDTH_SEMI_CONDENSED),
	REGULAR(TextAttribute.WIDTH_REGULAR),
	SEMIEXTENDED(TextAttribute.WIDTH_SEMI_EXTENDED),
	EXTENDED(TextAttribute.WIDTH_EXTENDED); 

	private final Float	_v;
	/*
	 * @see net.community.chest.awt.font.FontFloatAttributeValue#getAttributeValue()
	 */
	@Override
	public final Float getAttributeValue ()
	{
		return _v;
	}
	
	FontWidthValue (Float v)
	{
		_v = v;
	}

	public static final List<FontWidthValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FontWidthValue fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final FontWidthValue fromValue (final float f)
	{
		if (Float.isInfinite(f) || Float.isNaN(f))
			return null;

		return FontUtils.fromAttributeValue(f, VALUES);
	} 

	public static final FontWidthValue fromValue (final Float f)
	{
		return (null == f) ? null : fromValue(f.floatValue());
	}
}
