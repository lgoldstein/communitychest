/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.value;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.axis.DateTickMarkPosition;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 5, 2009 3:28:41 PM
 */
public class DateTickMarkPositionValueStringInstantiator extends
		AbstractXmlValueStringInstantiator<DateTickMarkPosition> {
	public DateTickMarkPositionValueStringInstantiator ()
	{
		super(DateTickMarkPosition.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (DateTickMarkPosition inst) throws Exception
	{
		if (null == inst)
			return null;

		final DateTickMarkPosType	t=DateTickMarkPosType.fromPosition(inst);
		if (null == t)
			throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

		return t.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public DateTickMarkPosition newInstance (String vs) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(vs);
		if ((null == s) || (s.length() <= 0))
			return null;

		final DateTickMarkPosType	t=DateTickMarkPosType.fromString(s);
		if (null == t)
			throw new NoSuchElementException("newInstance(" + s + ") unknown value");

		return t.getPosition();
	}

	public static final DateTickMarkPositionValueStringInstantiator DEFAULT=
		new DateTickMarkPositionValueStringInstantiator();
}
