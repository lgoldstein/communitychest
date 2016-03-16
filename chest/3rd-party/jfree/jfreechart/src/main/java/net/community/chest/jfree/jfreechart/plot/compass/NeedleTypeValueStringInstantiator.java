/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.compass;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 16, 2009 2:27:33 PM
 */
public class NeedleTypeValueStringInstantiator extends AbstractXmlValueStringInstantiator<Integer> {
    public NeedleTypeValueStringInstantiator ()
    {
        super(Integer.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (Integer inst) throws Exception
    {
        if (null == inst)
            return null;

        final NeedleType    t=NeedleType.fromOrderValue(inst.intValue());
        if (null == t)
            throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");
        return t.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Integer newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final NeedleType    t=NeedleType.fromString(s);
        if (null == t)
            throw new NoSuchElementException("newInstance(" + s + ") unknown value");

        return Integer.valueOf(t.getOrderValue());
    }

    public static final NeedleTypeValueStringInstantiator    DEFAULT=new NeedleTypeValueStringInstantiator();
}
