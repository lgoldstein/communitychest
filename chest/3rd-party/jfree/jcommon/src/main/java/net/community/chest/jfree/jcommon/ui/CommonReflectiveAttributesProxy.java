/*
 *
 */
package net.community.chest.jfree.jcommon.ui;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.convert.ValueStringInstantiator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Reflected type
 * @author Lyor G.
 * @since Jan 27, 2009 4:31:09 PM
 */
public class CommonReflectiveAttributesProxy<V> extends UIReflectiveAttributesProxy<V> {
    protected CommonReflectiveAttributesProxy (Class<V> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        ValueStringInstantiator<C>    vsi=super.resolveAttributeInstantiator(name, type);
        if (null == vsi)
            vsi = ConvCommon.getConverter(type);

        return vsi;
    }

}
