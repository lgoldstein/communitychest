/*
 *
 */
package net.community.chest.jfree.jfreechart.axis;

import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy;

import org.jfree.chart.axis.Axis;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <A> The reflected {@link Axis} type
 * @author Lyor G.
 * @since Feb 5, 2009 3:26:30 PM
 */
public abstract class AxisReflectiveProxy<A extends Axis> extends ChartReflectiveAttributesProxy<A> {
    protected AxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final boolean isAxisElement (final Element elem, final String tagName)
    {
        return AbstractXmlProxyConverter.isDefaultMatchingElement(elem, tagName, Axis.class.getSimpleName());
    }

    public static final AxisReflectiveProxy<? extends Axis> getAxisConverter (final Element elem)
    {
        return (null == elem) ? null : AxisType.getAxisConverter(elem.getAttribute(CLASS_ATTR));
    }
}
