package net.community.chest.convert;

import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.NumberValueStringConstructor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Used for {@link Double} and {@link Float} that have special values -
 * e.g., {@link Double#NaN}, {@link Float#POSITIVE_INFINITY}</P>
 * 
 * @param <N> Type of constructed {@link Number}
 * @author Lyor G.
 * @since Jan 8, 2008 1:09:59 PM
 */
public abstract class NonIntegerNumberValueStringConstructor<N extends Number> extends NumberValueStringConstructor<N> {
	protected NonIntegerNumberValueStringConstructor (Class<N> prmClass, Class<N> clsClass) throws IllegalArgumentException
	{
		super(prmClass, clsClass);
	}

	public static final String	NAN_VALUE="NaN",
								POSITIVE_INFINITY_VALUE="+*",
								NEGATIVE_INFINITY_VALUE="-*";
	/**
	 * @param inst The {@link Number} to be checked if special encoding
	 * @return The {@link String} to be used - null/empty if not a special value
	 */
	public abstract String convertSpecialInstance (N inst);
	/*
	 * @see net.community.chest.reflect.ValueStringConstructor#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (N inst) throws Exception
	{
		final String	s=convertSpecialInstance(inst);
		if ((s != null) && (s.length() > 0))
			return s;

		return super.convertInstance(inst);
	}
	/**
	 * Checks if the supplied {@link String} is one of the special values
	 * @param s The string to be considered
	 * @return The matching {@link Number} - null if not a special value
	 */
	public abstract N newSpecialInstance (String s);
	/*
	 * @see net.community.chest.reflect.ValueStringConstructor#newInstance(java.lang.String)
	 */
	@Override
	public N newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		final N			inst=newSpecialInstance(s);
		if (inst != null)
			return inst;

		return super.newInstance(s);
	}
}
