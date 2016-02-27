/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.axis.AxisLocation;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 3:51:21 PM
 */
public class AxisLocationValueStringInstantiator extends
		AbstractXmlValueStringInstantiator<AxisLocation> {
	public AxisLocationValueStringInstantiator ()
	{
		super(AxisLocation.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (AxisLocation inst) throws Exception
	{
		if (null == inst)
			return null;

		final AxisLocationValue	o=AxisLocationValue.fromLocation(inst);
		if (null == o)
			throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

		return o.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public AxisLocation newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final AxisLocationValue	o=AxisLocationValue.fromString(s);
		if (null == o)
			throw new NoSuchElementException("newInstance(" + s + ") unknown value");

		return o.getLocation();
	}

	public static final AxisLocationValueStringInstantiator	DEFAULT=new AxisLocationValueStringInstantiator();
}
