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
 * @since Jun 21, 2010 2:42:57 PM
 */
public class ThermometerLocationValueStringInstantiator extends AbstractXmlValueStringInstantiator<Integer> {
	public ThermometerLocationValueStringInstantiator ()
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

		final ThermometerLocationValue	v=ThermometerLocationValue.fromLocationValue(inst.intValue());
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
		if ((null == s) || (s.length() <= 0))
			return null;

		final ThermometerLocationValue	v=ThermometerLocationValue.fromString(s);
		if (null == v)
			throw new NoSuchElementException("newInstance(" + s + ") no match");

		return Integer.valueOf(v.getLocationValue());
	}

	public static final ThermometerLocationValueStringInstantiator	DEFAULT=
		new ThermometerLocationValueStringInstantiator();
}
