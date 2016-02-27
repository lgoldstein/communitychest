/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.thermometer;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;

/**
 * <P>Copyright 2010 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 21, 2010 2:16:22 PM
 */
public class ThermometerUnitValueStringInstantiator extends AbstractXmlValueStringInstantiator<Integer> {
	public ThermometerUnitValueStringInstantiator ()
			throws IllegalArgumentException
	{
		super(Integer.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (Integer inst) throws Exception
	{
		if (null == inst)
			return null;

		final ThermometerUnitValue	v=ThermometerUnitValue.fromUnitValue(inst.intValue());
		if (null == v)
			throw new NoSuchElementException("convertInstance(" + inst + ") no match");

		return v.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public Integer newInstance (String s) throws Exception
	{
		final int	sLen=(null == s) ? 0 : s.length();
		if (sLen <= 0)
			return null;

		ThermometerUnitValue	v=ThermometerUnitValue.fromString(s);
		if ((null == v) && (1 == sLen))	// try the character
			v = ThermometerUnitValue.fromUnitChar(s.charAt(0));
		if (null == v)
			throw new NoSuchElementException("newInstance(" + s + ") no match");

		return Integer.valueOf(v.getUnitValue());
	}

	public static final ThermometerUnitValueStringInstantiator	DEFAULT=
		new ThermometerUnitValueStringInstantiator();
}
