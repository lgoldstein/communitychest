package net.community.chest.awt.font;

import java.awt.Font;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents the various available font styles</P>
 * @author Lyor G.
 * @since Jul 30, 2007 7:27:26 AM
 */
public enum FontStyleType {
	PLAIN("plain", Font.PLAIN),
	BOLD("bold", Font.BOLD),
	ITALIC("italic", Font.ITALIC),
	BOLDITALIC("bolditalic", Font.BOLD | Font.ITALIC);

	private final String	_styleName;
	public String getStyleName ()
	{
		return _styleName;
	}

	private final int	_styleValue;
	public int getStyleValue ()
	{
		return _styleValue;
	}

	FontStyleType (String name, int value)
	{
		_styleName = name;
		_styleValue = value;
	}

	public static final List<FontStyleType>	VALUES=Collections.unmodifiableList(Arrays.asList(values()));
	public static final FontStyleType fromString (final String s)
	{
		return CollectionsUtils.fromString(VALUES, s, false);
	}

	public static final FontStyleType fromStyleName (final String name)
	{
		return fromString(name);
	}

	public static final FontStyleType fromStyleValue (final int value)
	{
		for (final FontStyleType v : VALUES)
		{
			if ((v != null) && (v.getStyleValue() == value))
				return v;
		}

		return null;	// no match found
	}
	
	public static final FontStyleType fromFont (final Font f)
	{
		if (null == f)
			return null;
		else if (f.isBold())
			return (f.isItalic() ? BOLDITALIC : BOLD);
		else if (f.isItalic())
			return ITALIC;
		else
			return PLAIN;
	}
}
