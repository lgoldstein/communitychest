/*
 * 
 */
package net.community.chest.jfree.jfreechart.axis.value;

import java.text.NumberFormat;
import java.util.Map;

import net.community.chest.convert.DoubleValueStringConstructor;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.StringUtil;
import net.community.chest.text.DecimalFormatValueStringInstantiator;

import org.jfree.chart.axis.NumberTickUnit;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since May 19, 2009 2:14:44 PM
 */
public class NumberTickUnitConverter implements XmlConvertible<NumberTickUnit>, XmlValueInstantiator<NumberTickUnit> {
	public NumberTickUnitConverter ()
	{
		super();
	}

	public Integer getCount (final Map<String,String> aMap, final String n)
	{
		final String	s=
			((null == aMap) || (aMap.size() <= 0) || (null == n) || (n.length() <= 0)) ? null : aMap.get(n),
						vs=
			StringUtil.getCleanStringValue(s);
		if ((null == vs) || (vs.length() <= 0))
			return null;

		return Integer.decode(vs);
	}

	public Double getSize (final Map<String,String> aMap, final String n) throws Exception
	{
		final String	s=
			((null == aMap) || (aMap.size() <= 0) || (null == n) || (n.length() <= 0)) ? null : aMap.get(n),
						vs=
			StringUtil.getCleanStringValue(s);
		if ((null == vs) || (vs.length() <= 0))
			return null;

		return DoubleValueStringConstructor.DEFAULT.newInstance(vs);
	}

	public NumberFormat getFormat (final Map<String,String> aMap, final String n) throws Exception
	{
		final String	s=
			((null == aMap) || (aMap.size() <= 0) || (null == n) || (n.length() <= 0)) ? null : aMap.get(n),
						vs=
			StringUtil.getCleanStringValue(s);
		if ((null == vs) || (vs.length() <= 0))
			return null;

		return DecimalFormatValueStringInstantiator.DEFAULT.newInstance(vs);
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public NumberTickUnit fromXml (Element elem) throws Exception
	{
		final Map<String,String>	aMap=DOMUtils.getNodeAttributes(elem, false);
		if ((null == aMap) || (aMap.size() <= 0))
			return null;

		final Double		sz=getSize(aMap, "size");
		final NumberFormat	fmt=getFormat(aMap, "format");
		final Integer		c=getCount(aMap, "count");
		if (null == fmt)
			return new NumberTickUnit(sz.doubleValue());
		else if (null == c)
			return new NumberTickUnit(sz.doubleValue(), fmt);
		else
			return new NumberTickUnit(sz.doubleValue(), fmt, c.intValue());
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		// TODO implement "toXml"
		throw new UnsupportedOperationException("toXml() N/A");
	}

	public static final NumberTickUnitConverter	DEFAULT=new NumberTickUnitConverter();
}
