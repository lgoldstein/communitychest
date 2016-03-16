/*
 *
 */
package net.community.chest.jfree.jcommon.ui;

import java.util.NoSuchElementException;

import org.jfree.ui.HorizontalAlignment;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 29, 2009 9:23:45 AM
 */
public class HAlignmentValueStringInstantiator extends
        AbstractXmlValueStringInstantiator<HorizontalAlignment> {
    public HAlignmentValueStringInstantiator ()
    {
        super(HorizontalAlignment.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (HorizontalAlignment inst) throws Exception
    {
        if (null == inst)
            return null;

        final HAlignment    a=HAlignment.fromAlignment(inst);
        if (null == a)
            throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

        return a.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public HorizontalAlignment newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final HAlignment    a=HAlignment.fromString(s);
        if (null == a)
            throw new NoSuchElementException("newInstance(" + s + ") unknown value");

        return a.getAlignment();
    }
    /*
     * @see net.community.chest.dom.AbstractXmlValueStringInstantiator#resolveValueString(org.w3c.dom.Element)
     */
    @Override
    public String resolveValueString (final Element elem) throws Exception
    {
        final Attr    a=HAlignment.getAlignmentAttribute(elem);
        if (a != null)
            return a.getValue();

        return super.resolveValueString(elem);
    }

    public static final HAlignmentValueStringInstantiator    DEFAULT=new HAlignmentValueStringInstantiator();
}
