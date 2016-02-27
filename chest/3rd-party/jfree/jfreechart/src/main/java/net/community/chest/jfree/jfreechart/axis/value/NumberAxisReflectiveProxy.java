/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.value;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jfree.jfreechart.data.RangeTypeValueStringInstantiator;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.data.RangeType;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <A> Type of {@link NumberAxis} being reflected
 * @author Lyor G.
 * @since May 19, 2009 2:08:46 PM
 */
public class NumberAxisReflectiveProxy<A extends NumberAxis> extends ValueAxisReflectiveProxy<A> {
	protected NumberAxisReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public NumberAxisReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
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
		if ((type != null) && RangeType.class.isAssignableFrom(type))
			return (ValueStringInstantiator<C>) RangeTypeValueStringInstantiator.DEFAULT;

		return super.resolveAttributeInstantiator(name, type);
	}

	public static final String	TICK_UNIT_ELEM_NAME="tickUnit";
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
		final XmlValueInstantiator<? extends NumberTickUnit>	p=getTickUnitConverter(elem);
		final NumberTickUnit									u=(null == p) ? null : p.fromXml(elem);
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
		final String	tagName=elem.getTagName();
		if (isTickUnitElement(elem, tagName))
		{
			setTickUnit(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final NumberAxisReflectiveProxy<NumberAxis>	NUMBER=
		new NumberAxisReflectiveProxy<NumberAxis>(NumberAxis.class, true);
}
