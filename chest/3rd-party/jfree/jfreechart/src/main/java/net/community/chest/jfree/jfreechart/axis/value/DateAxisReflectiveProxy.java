/*
 *
 */
package net.community.chest.jfree.jfreechart.axis.value;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.transform.XmlValueInstantiator;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickMarkPosition;
import org.jfree.chart.axis.DateTickUnit;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> Type of {@link DateAxis} being reflected
 * @author Lyor G.
 * @since May 5, 2009 2:48:34 PM
 */
public class DateAxisReflectiveProxy<A extends DateAxis> extends ValueAxisReflectiveProxy<A> {
    protected DateAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public DateAxisReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    /*
     * @see net.community.chest.jfree.jfreechart.ChartReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if ((type != null) && DateTickMarkPosition.class.isAssignableFrom(type))
            return (ValueStringInstantiator<C>)  DateTickMarkPositionValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    public static final String    TICK_UNIT_ELEM_NAME="tickUnit";
    public boolean isTickUnitElement (Element elem, String tagName)
    {
        return isMatchingElement(elem, tagName, TICK_UNIT_ELEM_NAME);
    }

    public XmlValueInstantiator<? extends DateTickUnit> getTickUnitConverter (Element elem)
    {
        return (null == elem) ? null : DateTickUnitConverter.DEFAULT;
    }

    public DateTickUnit setTickUnit (A src, Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends DateTickUnit>    p=getTickUnitConverter(elem);
        final DateTickUnit                                    u=(null == p) ? null : p.fromXml(elem);
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

    public static final DateAxisReflectiveProxy<DateAxis>    DATE=
        new DateAxisReflectiveProxy<DateAxis>(DateAxis.class, true);
}
