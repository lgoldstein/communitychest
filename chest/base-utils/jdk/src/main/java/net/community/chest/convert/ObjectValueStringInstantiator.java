/*
 * 
 */
package net.community.chest.convert;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 7:38:19 AM
 */
public class ObjectValueStringInstantiator
		extends AbstractXmlValueStringInstantiator<Object> {
	public ObjectValueStringInstantiator ()
	{
		super(Object.class);
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
	 */
	@Override
	public String convertInstance (Object inst) throws Exception
	{
		return (null == inst) ? null : inst.toString();
	}
	/*
	 * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
	 */
	@Override
	public Object newInstance (String s) throws Exception
	{
		return s;
	}

	public static final ObjectValueStringInstantiator	DEFAULT=
		new ObjectValueStringInstantiator();
}
