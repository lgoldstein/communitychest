package net.community.chest.awt.dom.converter;

import java.awt.Insets;
import java.util.List;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * Copyright 2007 as per GPLv2
 * @author Lyor G.
 * @since Jul 15, 2007 12:19:33 PM
 */
public class InsetsValueInstantiator extends AbstractXmlValueStringInstantiator<Insets> {
	public InsetsValueInstantiator ()
	{
		super(Insets.class);
	}

	/**
	 * Empty {@link Insets} constant
	 */
	public static final Insets	NO_INSETS=new Insets(0, 0, 0, 0);
	/**
	 * Value that can be used in call to {@link #fromString(String)} to refer
	 * to the {@link #NO_INSETS} constant
	 */
	public static final String	NONE_VALUE="none";
	public static final Insets fromString (final String v) throws NumberFormatException
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;
		if (NONE_VALUE.equalsIgnoreCase(s))
			return NO_INSETS;

		final List<String>	vals=StringUtil.splitString(s, ',');
		if ((null == vals) || (vals.size() < 4))
			throw new NumberFormatException("fromString(" + s + ") not enough components");

		final int	top=Integer.parseInt(StringUtil.getCleanStringValue(vals.get(0))),
					left=Integer.parseInt(StringUtil.getCleanStringValue(vals.get(1))),
					bottom=Integer.parseInt(StringUtil.getCleanStringValue(vals.get(2))),
					right=Integer.parseInt(StringUtil.getCleanStringValue(vals.get(3)));

		return new Insets(top, left, bottom, right);
	}
	/*
	 * @see net.community.chest.reflect.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public Insets newInstance (final String s) throws Exception
	{
		return fromString(s);
	}
	// NOTE: returns {@link #NONE_VALUE} if same as {@link #NO_INSETS}
	public static final String toString (final Insets i)
	{
		if (null == i)
			return null;

		if (NO_INSETS.equals(i))
			return NONE_VALUE;

		return new StringBuilder(64)
						.append(i.top)
			.append(',').append(i.left)
			.append(',').append(i.bottom)
			.append(',').append(i.right)
			.toString()
			;
	}
	/*
	 * @see net.community.chest.reflect.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (Insets i) throws Exception
	{
		return toString(i);
	}

	public static final InsetsValueInstantiator	DEFAULT=new InsetsValueInstantiator();
}
