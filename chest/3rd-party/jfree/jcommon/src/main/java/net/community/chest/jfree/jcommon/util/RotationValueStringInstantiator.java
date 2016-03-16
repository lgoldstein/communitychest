/*
 *
 */
package net.community.chest.jfree.jcommon.util;

import java.util.NoSuchElementException;

import org.jfree.util.Rotation;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 1, 2009 2:52:58 PM
 */
public class RotationValueStringInstantiator extends AbstractXmlValueStringInstantiator<Rotation> {
    public RotationValueStringInstantiator ()
    {
        super(Rotation.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (Rotation inst) throws Exception
    {
        if (null == inst)
            return null;

        final RotationType    rt=RotationType.fromRotation(inst);
        if (null == rt)
            throw new NoSuchElementException("convertInstance(" + inst + ") uknown value");

        return rt.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Rotation newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final RotationType    rt=RotationType.fromString(s);
        if (null == rt)
            throw new NoSuchElementException("newInstance(" + s + ") uknown value");

        return rt.getRotation();
    }

    public static final RotationValueStringInstantiator    DEFAULT=new RotationValueStringInstantiator();
}
