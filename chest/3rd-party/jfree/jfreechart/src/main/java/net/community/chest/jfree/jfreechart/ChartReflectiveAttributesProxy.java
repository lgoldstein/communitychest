/*
 * 
 */
package net.community.chest.jfree.jfreechart;

import java.awt.Color;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.jfree.jcommon.ui.CommonReflectiveAttributesProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Reflected type
 * @author Lyor G.
 * @since Jan 26, 2009 3:08:29 PM
 */
public abstract class ChartReflectiveAttributesProxy<V> extends CommonReflectiveAttributesProxy<V> {
	protected ChartReflectiveAttributesProxy (Class<V> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	/*
	 * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
	{
		if ((type != null) && Color.class.isAssignableFrom(type))
			return (ValueStringInstantiator<C>) ChartColorValueInstantiator.CHARTCOLOR;

		ValueStringInstantiator<C>	vsi=super.resolveAttributeInstantiator(name, type);
		if (null == vsi)
			vsi = ConvChart.getConverter(type); 

		return vsi;
	}
}
