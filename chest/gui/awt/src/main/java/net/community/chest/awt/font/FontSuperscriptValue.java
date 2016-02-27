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
 * <P>Encapsulate pre-defined {@link TextAttribute#SUPERSCRIPT} values</P>
 * 
 * @author Lyor G.
 * @since Jun 18, 2009 8:03:37 AM
 */
public enum FontSuperscriptValue implements FontIntAttributeValue {
	SUPER(TextAttribute.SUPERSCRIPT_SUPER),
	SUB(TextAttribute.SUPERSCRIPT_SUB);

	private final Integer	_v;
	/*
	 * @see net.community.chest.awt.font.FontIntAttributeValue#getAttributeValue()
	 */
	@Override
	public final Integer getAttributeValue ()
	{
		return _v;
	}
	
	FontSuperscriptValue (Integer v)
	{
		_v = v;
	}

	public static final List<FontSuperscriptValue>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FontSuperscriptValue fromString (final String name)
	{
		return CollectionsUtils.fromString(VALUES, name, false);
	}

	public static final FontSuperscriptValue fromValue (final int n)
	{
		return FontUtils.fromAttributeValue(n, VALUES);
	}

	public static final FontSuperscriptValue fromValue (final Integer n)
	{
		return (null == n) ? null : fromValue(n.intValue());
	}
}
