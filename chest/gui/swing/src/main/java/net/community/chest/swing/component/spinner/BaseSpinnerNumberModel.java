/*
 * 
 */
package net.community.chest.swing.component.spinner;

import javax.swing.SpinnerNumberModel;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 11, 2009 9:56:07 AM
 *
 */
public class BaseSpinnerNumberModel extends SpinnerNumberModel implements XmlConvertible<BaseSpinnerNumberModel> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6141711954723728555L;
	public BaseSpinnerNumberModel (Number value, Comparable<?> minimum, Comparable<?> maximum, Number stepSize)
	{
		super(value, minimum, maximum, stepSize);
	}

	public BaseSpinnerNumberModel (int value, int minimum, int maximum, int stepSize)
	{
		this(Integer.valueOf(value), Integer.valueOf(minimum), Integer.valueOf(maximum), Integer.valueOf(stepSize));
	}

	public BaseSpinnerNumberModel ()
	{
		this(Integer.valueOf(0), null, null, Integer.valueOf(1));
	}

	public BaseSpinnerNumberModel (double value, double minimum, double maximum, double stepSize)
	{
		this(Double.valueOf(value), Double.valueOf(minimum), Double.valueOf(maximum), Double.valueOf(stepSize));
	}

	protected XmlProxyConvertible<?> getModelConverter (Element elem)
	{
		return (null == elem) ? null : SpinnerNumberModelReflectiveProxy.NUMMODEL;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	public BaseSpinnerNumberModel fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getModelConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					o=
			(null == proxy) ? this : ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched instances");
		return this;
	}

	public BaseSpinnerNumberModel (Element elem) throws Exception
	{
		final Object	o=fromXml(elem);
		if (o != this)
			throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched instances");
	}

	public String getRootElementName ()
	{
		return JSpinnerReflectiveProxy.MODEL_ATTR;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
	 */
	@Override
	public Element toXml (Document doc) throws Exception
	{
		final String					tagName=getRootElementName();
		final Element					elem=doc.createElement(tagName);
		final XmlProxyConvertible<?>	proxy=getModelConverter(elem);
		@SuppressWarnings("unchecked")
		final Element					o=
			(null == proxy) ? elem : ((XmlProxyConvertible<Object>) proxy).toXml(this, doc, elem);
		if (o != elem)
			throw new IllegalStateException("toXml(" + DOMUtils.toString(elem) + ") mismatched instances");

		return elem;
	}
}
