/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.plot.DatasetRenderingOrder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 4:06:59 PM
 */
public class DatasetRenderOrderValueStringInstantiator extends
		AbstractXmlValueStringInstantiator<DatasetRenderingOrder> {
	public DatasetRenderOrderValueStringInstantiator ()
	{
		super(DatasetRenderingOrder.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (DatasetRenderingOrder inst) throws Exception
	{
		if (null == inst)
			return null;

		final DatasetRenderOrderValue	o=DatasetRenderOrderValue.fromOrder(inst);
		if (null == o)
			throw new NoSuchElementException("convertInstance(" + inst + ") uknown value");

		return o.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public DatasetRenderingOrder newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final DatasetRenderOrderValue	o=DatasetRenderOrderValue.fromString(s);
		if (null == o)
			throw new NoSuchElementException("newInstance(" + s + ") uknown value");

		return o.getOrder();
	}

	public static final DatasetRenderOrderValueStringInstantiator	DEFAULT=new DatasetRenderOrderValueStringInstantiator();
}
