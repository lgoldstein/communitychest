/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.ui.VerticalAlignment;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 29, 2009 9:28:05 AM
 */
public class VAlignmentValueStringInstantiator extends
		AbstractXmlValueStringInstantiator<VerticalAlignment> {
	public VAlignmentValueStringInstantiator ()
	{
		super(VerticalAlignment.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (VerticalAlignment inst) throws Exception
	{
		if (null == inst)
			return null;

		final VAlignment	a=VAlignment.fromAlignment(inst);
		if (null == a)
			throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

		return a.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public VerticalAlignment newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final VAlignment	a=VAlignment.fromString(s);
		if (null == a)
			throw new NoSuchElementException("newInstance(" + s + ") unknown value");

		return a.getAlignment();
	}
	/*
	 * @see net.community.chest.dom.AbstractXmlValueStringInstantiator#resolveValueString(org.w3c.dom.Element)
	 */
	@Override
	public String resolveValueString (final Element elem) throws Exception
	{
		final Attr	a=VAlignment.getAlignmentAttribute(elem);
		if (a != null)
			return a.getValue();

		return super.resolveValueString(elem);
	}

	public static final VAlignmentValueStringInstantiator	DEFAULT=new VAlignmentValueStringInstantiator();

}
