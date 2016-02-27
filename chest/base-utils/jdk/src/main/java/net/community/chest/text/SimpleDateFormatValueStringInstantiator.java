/*
 * 
 */
package net.community.chest.text;

import java.text.SimpleDateFormat;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The generated {@link SimpleDateFormat} class
 * @author Lyor G.
 * @since Feb 18, 2009 1:11:14 PM
 */
public abstract class SimpleDateFormatValueStringInstantiator<F extends SimpleDateFormat> extends AbstractXmlValueStringInstantiator<F> {
	protected SimpleDateFormatValueStringInstantiator (Class<F> objClass) throws IllegalArgumentException
	{
		super(objClass);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (F inst) throws Exception
	{
		return (null == inst) ? null : inst.toPattern();
	}

	public static final SimpleDateFormatValueStringInstantiator<SimpleDateFormat>	DEFAULT=
			new SimpleDateFormatValueStringInstantiator<SimpleDateFormat>(SimpleDateFormat.class) {
				/*
				 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
				 */
				@Override
				public SimpleDateFormat newInstance (String v) throws Exception
				{
					final String	s=StringUtil.getCleanStringValue(v);
					if ((null == s) || (s.length() <= 0))
						return null;

					return new SimpleDateFormat(s);
				}				
			};
}