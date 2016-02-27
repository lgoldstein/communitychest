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
 * <P>Encapsulate well-known constant for {@link TextAttribute#WEIGHT}</P>
 * 
 * @author Lyor G.
 * @since Jun 18, 2009 7:30:47 AM
 */
public enum FontWeightValue implements FontFloatAttributeValue {
	EXTRALIGGHT(TextAttribute.WEIGHT_EXTRA_LIGHT),
	LIGHT(TextAttribute.WEIGHT_LIGHT),
	DEMILIGHT(TextAttribute.WEIGHT_DEMILIGHT),
	REGULAR(TextAttribute.WEIGHT_REGULAR),
	SEMIBOLD(TextAttribute.WEIGHT_SEMIBOLD),
	MEDIUM(TextAttribute.WEIGHT_MEDIUM),
	DEMIBOLD(TextAttribute.WEIGHT_DEMIBOLD),
	BOLD(TextAttribute.WEIGHT_BOLD),
	HEAVY(TextAttribute.WEIGHT_HEAVY),
	EXTRABOLD(TextAttribute.WEIGHT_EXTRABOLD),
	ULTRABOLD(TextAttribute.WEIGHT_ULTRABOLD);

	private final Float	_v;
	/*
	 * @see net.community.chest.awt.font.FontFloatAttributeValue#getAttributeValue()
	 */
	@Override
	public final Float getAttributeValue ()
	{
		return _v;
	}
	
	FontWeightValue (Float v)
	{
		_v = v;
	}

	public static final List<FontWeightValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FontWeightValue fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final FontWeightValue fromValue (final float f)
	{
		if (Float.isInfinite(f) || Float.isNaN(f))
			return null;

		return FontUtils.fromAttributeValue(f, VALUES);
	} 

	public static final FontWeightValue fromValue (final Float f)
	{
		return (null == f) ? null : fromValue(f.floatValue());
	}
}
