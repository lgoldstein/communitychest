/*
 *
 */
package net.community.chest.jfree.jfreechart.plot;

import java.util.NoSuchElementException;

import net.community.chest.dom.AbstractXmlValueStringInstantiator;
import net.community.chest.lang.StringUtil;

import org.jfree.chart.plot.PlotOrientation;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 5, 2009 3:11:59 PM
 */
public class PlotOrientationValueStringInstantiator extends
        AbstractXmlValueStringInstantiator<PlotOrientation> {
    public PlotOrientationValueStringInstantiator ()
    {
        super(PlotOrientation.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (PlotOrientation inst) throws Exception
    {
        if (null == inst)
            return null;

        final PlotOrientationValue    o=PlotOrientationValue.fromOrientation(inst);
        if (null == o)
            throw new NoSuchElementException("convertInstance(" + inst + ") unknown value");

        return o.toString();
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public PlotOrientation newInstance (String v) throws Exception
    {
        final String    s=StringUtil.getCleanStringValue(v);
        if ((null == s) || (s.length() <= 0))
            return null;

        final PlotOrientationValue    o=PlotOrientationValue.fromString(s);
        if (null == o)
            throw new NoSuchElementException("newInstance(" + s + ") unknown value");

        return o.getOrientation();
    }
    /*
     * @see net.community.chest.dom.AbstractXmlValueStringInstantiator#resolveValueString(org.w3c.dom.Element)
     */
    @Override
    public String resolveValueString (final Element elem) throws Exception
    {
        final Attr    a=PlotOrientationValue.getOrientationAttribute(elem);
        if (a != null)
            return a.getValue();

        return super.resolveValueString(elem);
    }

    public static final PlotOrientationValueStringInstantiator    DEFAULT=new PlotOrientationValueStringInstantiator();
}
