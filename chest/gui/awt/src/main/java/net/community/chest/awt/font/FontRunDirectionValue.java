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
 * <P>Encapsulate pre-defined {@link TextAttribute#RUN_DIRECTION} values</P>
 * 
 * @author Lyor G.
 * @since Jun 18, 2009 8:12:49 AM
 */
public enum FontRunDirectionValue {
	LTR(TextAttribute.RUN_DIRECTION_LTR),
	RTL(TextAttribute.RUN_DIRECTION_RTL),
	DEFAULT(null);

	private final Boolean	_v;
	public final Boolean getAttributeValue ()
	{
		return _v;
	}
	
	FontRunDirectionValue (Boolean v)
	{
		_v = v;
	}

	public static final List<FontRunDirectionValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FontRunDirectionValue fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}
	
	public static final FontRunDirectionValue fromValue (Boolean b)
	{
		if (null == b)
			return DEFAULT;

		for (final FontRunDirectionValue v : VALUES)
		{
			if ((v != null) && b.equals(v.getAttributeValue()))
				return v;
		}

		return null;
	}
}
