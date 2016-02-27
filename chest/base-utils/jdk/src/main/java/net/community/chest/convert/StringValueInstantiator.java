/*
 * 
 */
package net.community.chest.convert;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 7, 2009 10:47:44 AM
 */
public class StringValueInstantiator extends AbstractXmlValueStringInstantiator<String> {
	public StringValueInstantiator ()
	{
		super(String.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (String inst) throws Exception
	{
		return inst;
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public String newInstance (String s) throws Exception
	{
		return s;
	}

	public static final StringValueInstantiator	DEFAULT=new StringValueInstantiator();
}
