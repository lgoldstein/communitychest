/*
 * 
 */
package net.community.chest.jfree.jcommon.util;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.util.SortOrder;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 3:56:51 PM
 */
public class SortOrderEnumStringInstantiator extends AbstractXmlValueStringInstantiator<SortOrder> {
	public SortOrderEnumStringInstantiator ()
	{
		super(SortOrder.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (SortOrder inst) throws Exception
	{
		if (null == inst)
			return null;

		final SortOrderEnum	o=SortOrderEnum.fromSortOrder(inst);
		if (null == o)
			throw new NoSuchElementException("convertInstance(" + inst + ") uknown value");

		return o.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public SortOrder newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final SortOrderEnum	o=SortOrderEnum.fromString(s);
		if (null == o)
			throw new NoSuchElementException("newInstance(" + s + ") uknown value");

		return o.getSortOrder();
	}

	public static final SortOrderEnumStringInstantiator	DEFAULT=new SortOrderEnumStringInstantiator();
}
