/*
 *
 */
package net.community.chest.swing.component.progress;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 8, 2009 9:41:14 AM
 */
public class BarOrientationValueStringInstantiator extends AbstractXmlValueStringInstantiator<Integer> {
    public BarOrientationValueStringInstantiator ()
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

        final BarOrientation    st=BarOrientation.fromOrientation(inst.intValue());
        if (null == st)
            throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

        return st.toString();
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

        final BarOrientation    st=BarOrientation.fromString(s);
        if (null == st)
            throw new NoSuchElementException("newInstance(" + s + ") unknown value");

        return Integer.valueOf(st.getOrientation());
    }

    public static final BarOrientationValueStringInstantiator    DEFAULT=new BarOrientationValueStringInstantiator();
}
