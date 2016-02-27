/*
 * 
 */
package net.community.chest.jfree.jfreechart.plot.pie;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.plot.PieLabelLinkStyle;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 16, 2009 2:52:26 PM
 */
public class PieLabelLinkStyleValueStringInstantiator extends
				AbstractXmlValueStringInstantiator<PieLabelLinkStyle> {
	public PieLabelLinkStyleValueStringInstantiator ()
	{
		super(PieLabelLinkStyle.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (PieLabelLinkStyle inst) throws Exception
	{
		if (null == inst)
			return null;

		final PieLabelLinkStyleEnum	t=PieLabelLinkStyleEnum.fromStyle(inst);
		if (null == t)
			throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

		return t.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public PieLabelLinkStyle newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;
		
		final PieLabelLinkStyleEnum	t=PieLabelLinkStyleEnum.fromString(s);
		if (null == t)
			throw new NoSuchElementException("newInstance(" + s + ") unknown value");

		return t.getStyle();
	}

	public static final PieLabelLinkStyleValueStringInstantiator	DEFAULT=new PieLabelLinkStyleValueStringInstantiator();
}
