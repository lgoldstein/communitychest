package net.community.chest.convert;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 20, 2007 9:49:16 AM
 */
public class CharacterStringInstantiator extends AbstractXmlValueStringInstantiator<Character> {
	public CharacterStringInstantiator ()
	{
		super(Character.class);
	}
	// NOTE: returns null for null/empty string
	public static final Character fromString (final String s) throws IndexOutOfBoundsException
	{
		final int	sLen=(null == s) ? 0 : s.length();
		if (sLen <= 0)
			return null;

		if (sLen != 1)
			throw new StringIndexOutOfBoundsException("fromString(" + s + ") too many characters (only 1 allowed)");

		return Character.valueOf(s.charAt(0));
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (final Character inst) throws Exception
	{
		return (null == inst) ? null : inst.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public Character newInstance (final String s) throws Exception
	{
		return fromString(s);
	}

	public static final CharacterStringInstantiator	DEFAULT=new CharacterStringInstantiator();
}
