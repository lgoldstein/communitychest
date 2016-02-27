/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.plot.SeriesRenderingOrder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 8, 2009 2:45:15 PM
 */
public class SeriesRenderOrderValueStringInstantiator extends
		AbstractXmlValueStringInstantiator<SeriesRenderingOrder> {
	public SeriesRenderOrderValueStringInstantiator ()
	{
		super(SeriesRenderingOrder.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (SeriesRenderingOrder inst) throws Exception
	{
		if (null == inst)
			return null;

		final SeriesRenderOrderValue	o=SeriesRenderOrderValue.fromOrder(inst);
		if (null == o)
			throw new NoSuchElementException("convertInstance(" + inst + ") uknown value");

		return o.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public SeriesRenderingOrder newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final SeriesRenderOrderValue	o=SeriesRenderOrderValue.fromString(s);
		if (null == o)
			throw new NoSuchElementException("newInstance(" + s + ") uknown value");

		return o.getOrder();
	}

	public static final SeriesRenderOrderValueStringInstantiator	DEFAULT=new SeriesRenderOrderValueStringInstantiator();
}
