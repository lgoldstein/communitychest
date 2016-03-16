/*
 *
 */
package net.community.chest.awt.layout;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 8, 2009 8:58:16 AM
 */
public class FlowLayoutAlignmentValueStringInstantiator extends AbstractXmlValueStringInstantiator<Integer> {
    public FlowLayoutAlignmentValueStringInstantiator ()
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

        final FlowLayoutAlignment    st=FlowLayoutAlignment.fromAlignment(inst.intValue());
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

        final FlowLayoutAlignment    st=FlowLayoutAlignment.fromString(s);
        if (null == st)
            throw new NoSuchElementException("newInstance(" + s + ") unknown value");

        return Integer.valueOf(st.getAlignment());
    }

    public static final FlowLayoutAlignmentValueStringInstantiator    DEFAULT=new FlowLayoutAlignmentValueStringInstantiator();
}
