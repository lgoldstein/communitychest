/*
 * 
 */
package net.community.chest.dom.xpath.manip;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import org.w3c.dom.Element;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.NodeTypeEnum;
import net.community.chest.dom.proxy.ReflectiveAttributesProxy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <D> Type of {@link XPathManipulationData} being reflected
 * @author Lyor G.
 * @since May 7, 2009 8:24:50 AM
 */
public class XPathManipulationDataReflectiveProxy<D extends XPathManipulationData> extends ReflectiveAttributesProxy<D> {
	protected XPathManipulationDataReflectiveProxy (Class<D> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public XPathManipulationDataReflectiveProxy (Class<D> objClass)
			throws IllegalArgumentException
	{
		this(objClass, false);
	}
	/* We never want to process the children since that is where the data resides
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#getXmlChildren(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	protected Map.Entry<D,Collection<Element>> getXmlChildren (final D src, final Element elem) throws Exception
	{
		return null;
	}

	public static final XPathManipulationDataReflectiveProxy<XPathManipulationData>	DEFAULT=
		new XPathManipulationDataReflectiveProxy<XPathManipulationData>(XPathManipulationData.class, true) {
			/*
			 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
			 */
			@Override
			public XPathManipulationData createInstance (Element elem) throws Exception
			{
				final String		tagName=(null == elem) ? null : elem.getTagName();
				final NodeTypeEnum	t=NodeTypeEnum.fromString(tagName);
				if (null == t)
					throw new NoSuchElementException("createInstance(" + DOMUtils.toString(elem) + ") unknown node type");

				final XPathManipulationData	d=new XPathManipulationData();
				d.setDataElement(elem);
				d.setNodeType(t);
				return d;
			}
		};
}
