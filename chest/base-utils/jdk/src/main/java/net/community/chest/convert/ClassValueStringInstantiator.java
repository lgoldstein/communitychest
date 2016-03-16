/*
 *
 */
package net.community.chest.convert;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Sep 17, 2008 2:53:10 PM
 */
@SuppressWarnings({ "rawtypes" })
public class ClassValueStringInstantiator extends AbstractXmlValueStringInstantiator<Class> {
    public ClassValueStringInstantiator ()
    {
        super(Class.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (Class inst) throws Exception
    {
        return (null == inst) ? null : inst.getName();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Class<?> newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        return ClassUtil.loadClassByName(s);
    }

    public static final ClassValueStringInstantiator    DEFAULT=new ClassValueStringInstantiator();
}
