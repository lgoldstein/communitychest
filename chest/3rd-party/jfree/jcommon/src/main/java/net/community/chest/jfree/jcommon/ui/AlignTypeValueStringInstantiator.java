/*
 *
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 8, 2009 9:52:59 AM
 */
public class AlignTypeValueStringInstantiator extends AbstractXmlValueStringInstantiator<Integer> {
    public AlignTypeValueStringInstantiator ()
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

        final AlignType    st=AlignType.fromAlignment(inst.intValue());
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

        final AlignType    st=AlignType.fromString(s);
        if (null == st)
            throw new NoSuchElementException("newInstance(" + s + ") unknown value");

        return Integer.valueOf(st.getAlignment());
    }

    public static final AlignTypeValueStringInstantiator    DEFAULT=new AlignTypeValueStringInstantiator();
}
