package net.community.chest.convert;

import java.util.NoSuchElementException;

import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;


/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 1:08:21 PM
 */
public class DoubleValueStringConstructor extends NonIntegerNumberValueStringConstructor<Double> {
	public DoubleValueStringConstructor ()
	{
		super(Double.TYPE, Double.class);
	}

	public static final String convertSpecialInstance (final double n)
	{
		if (Double.isNaN(n))
			return NAN_VALUE;

		if (Double.isInfinite(n))
		{
			if (n == Double.POSITIVE_INFINITY)
				return POSITIVE_INFINITY_VALUE;
			else if (n == Double.NEGATIVE_INFINITY)
				return NEGATIVE_INFINITY_VALUE;
			else
				throw new NoSuchElementException("convertSpecialInstance(" + n + ") unknown infinity value");
		}

		return null;
	}
	/*
	 * @see net.community.chest.convert.NonIntegerNumberValueStringConstructor#convertSpecialInstance(java.lang.Number)
	 */
	@Override
	public String convertSpecialInstance (final Double inst)
	{
		if (null == inst)
			return null;

		return convertSpecialInstance(inst.doubleValue());
	}

	public String convertInstance (final double n)
	{
		final String	v=convertSpecialInstance(n);
		if ((v != null) && (v.length() > 0))
			return v;

		return String.valueOf(n);
	}

	public static final Double	NAN_NUMBER=Double.valueOf(Double.NaN),
								POSINF_NUMBER=Double.valueOf(Double.POSITIVE_INFINITY),
								NEGINF_NUMBER=Double.valueOf(Double.NEGATIVE_INFINITY);
	public static final Double convertSpecialInstance (final String s)
	{
		if ((null == s) || (s.length() <= 0))
			return null;
		else if (NAN_VALUE.equalsIgnoreCase(s))
			return NAN_NUMBER;
		else if (POSITIVE_INFINITY_VALUE.equalsIgnoreCase(s))
			return POSINF_NUMBER;
		else if (NEGATIVE_INFINITY_VALUE.equalsIgnoreCase(s))
			return NEGINF_NUMBER;

		return null;
	}
	/*
	 * @see net.community.chest.convert.NonIntegerNumberValueStringConstructor#newSpecialInstance(java.lang.String)
	 */
	@Override
	public Double newSpecialInstance (final String v)
	{
		return convertSpecialInstance(StringUtil.getCleanStringValue(v));
	}

	public double fromString (final String v) throws RuntimeException
	{
		try
		{
			final String	s=StringUtil.getCleanStringValue(v);
			final Double	d=newInstance(s);
			if (null == d)
				throw new IllegalArgumentException("fromString(" + s + ") no value extracted");
			return d.doubleValue();
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}

	public static final DoubleValueStringConstructor	DEFAULT=new DoubleValueStringConstructor();
}
