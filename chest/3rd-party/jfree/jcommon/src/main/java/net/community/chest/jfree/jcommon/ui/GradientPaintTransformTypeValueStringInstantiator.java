/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.ui.GradientPaintTransformType;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Jun 8, 2009 1:38:55 PM
 */
public class GradientPaintTransformTypeValueStringInstantiator
		extends AbstractXmlValueStringInstantiator<GradientPaintTransformType> {
	public GradientPaintTransformTypeValueStringInstantiator ()
	{
		super(GradientPaintTransformType.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (GradientPaintTransformType inst) throws Exception
	{
		if (null == inst)
			return null;

		final GradientPaintTransformTypeEnum	a=GradientPaintTransformTypeEnum.fromType(inst);
		if (null == a)
			throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

		return a.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public GradientPaintTransformType newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final GradientPaintTransformTypeEnum	a=GradientPaintTransformTypeEnum.fromString(s);
		if (null == a)
			throw new NoSuchElementException("newInstance(" + s + ") unknown value");

		return a.getType();
	}

	public static final GradientPaintTransformTypeValueStringInstantiator	DEFAULT=
		new GradientPaintTransformTypeValueStringInstantiator();
}
