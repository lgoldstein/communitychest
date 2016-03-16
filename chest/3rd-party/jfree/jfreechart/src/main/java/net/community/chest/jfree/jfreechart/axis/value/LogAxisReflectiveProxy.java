/*
 *
 */
package net.community.chest.jfree.jfreechart.axis.value;

import net.community.chest.dom.transform.XmlValueInstantiator;

import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> Type of {@link LogAxis} being reflected
 * @author Lyor G.
 * @since May 19, 2009 2:28:19 PM
 */
public class LogAxisReflectiveProxy<A extends LogAxis> extends ValueAxisReflectiveProxy<A> {
    protected LogAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public LogAxisReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    TICK_UNIT_ELEM_NAME="tickUnit";
    public boolean isTickUnitElement (Element elem, String tagName)
    {
        return isMatchingElement(elem, tagName, TICK_UNIT_ELEM_NAME);
    }

    public XmlValueInstantiator<? extends NumberTickUnit> getTickUnitConverter (Element elem)
    {
        return (null == elem) ? null : NumberTickUnitConverter.DEFAULT;
    }

    public NumberTickUnit setTickUnit (A src, Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends NumberTickUnit>    p=getTickUnitConverter(elem);
        final NumberTickUnit                                    u=(null == p) ? null : p.fromXml(elem);
        if (u != null)
            src.setTickUnit(u);
        return u;
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public A fromXmlChild (A src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isTickUnitElement(elem, tagName))
        {
            setTickUnit(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final LogAxisReflectiveProxy<LogAxis>    LOG=
        new LogAxisReflectiveProxy<LogAxis>(LogAxis.class, true);
}
