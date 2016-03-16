/*
 *
 */
package net.community.chest.jfree.jfreechart.plot.dial;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.plot.DialShape;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 9, 2009 11:22:10 AM
 */
public class DialShapeValueStringInstantiator extends
        AbstractXmlValueStringInstantiator<DialShape> {
    public DialShapeValueStringInstantiator ()
    {
        super(DialShape.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (DialShape inst) throws Exception
    {
        if (null == inst)
            return null;

        final DialShapeValue    o=DialShapeValue.fromShape(inst);
        if (null == o)
            throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

        return o.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public DialShape newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final DialShapeValue    o=DialShapeValue.fromString(s);
        if (null == o)
            throw new NoSuchElementException("newInstance(" + s + ") unknown value");

        return o.getShape();
    }

    public static final DialShapeValueStringInstantiator    DEFAULT=new DialShapeValueStringInstantiator();
}
