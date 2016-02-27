/*
 * 
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.ui.TextAnchor;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 26, 2009 1:34:26 PM
 */
public class TextAnchorValueStringInstantiator extends AbstractXmlValueStringInstantiator<TextAnchor> {
	public TextAnchorValueStringInstantiator ()
	{
		super(TextAnchor.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (TextAnchor inst) throws Exception
	{
		if (null == inst)
			return null;

		final TextAnchorEnum	e=TextAnchorEnum.fromAnchor(inst);
		if (null == e)
			throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

		return e.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public TextAnchor newInstance (String v) throws Exception
	{
		final String	s=StringUtil.getCleanStringValue(v);
		if ((null == s) || (s.length() <= 0))
			return null;

		final TextAnchorEnum	e=TextAnchorEnum.fromString(s);
		if (null == e)
			throw new NoSuchElementException("newInstance(" + s + ") unknown value");

		return e.getAnchor();
	}

	public static final TextAnchorValueStringInstantiator	DEFAULT=new TextAnchorValueStringInstantiator();
}
