/*
 * 
 */
package net.community.chest.awt.dom;

import java.awt.Toolkit;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <T> The reflected {@link Toolkit} instance
 * @author Lyor G.
 * @since Dec 2, 2008 1:47:03 PM
 */
public class ToolkitReflectiveProxy<T extends Toolkit> extends UIReflectiveAttributesProxy<T> {
	public ToolkitReflectiveProxy (Class<T> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected ToolkitReflectiveProxy (Class<T> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public static final String	PROPERTY_ELEMNAME="property";
	public boolean isPropertyElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, PROPERTY_ELEMNAME);
	}

	public XmlValueInstantiator<?> getValueInstantiator (String aName, Element elem) throws Exception
	{
		final String						cn=(null == elem) ? null : elem.getAttribute(CLASS_ATTR);
		final Class<?>						cc=((null == cn) || (cn.length() <= 0)) ? String.class : ClassUtil.loadClassByName(cn);
		final ValueStringInstantiator<?>	vsi=resolveAttributeInstantiator(aName, cc);
		if (!(vsi instanceof XmlValueInstantiator<?>))
			throw new NoSuchElementException("getValueInstantiator(" + aName + ") no instantiator for class=" + cn);

		return (XmlValueInstantiator<?>) vsi;
	}

	private Method	_propSetter	/* =null */;
	public synchronized Method getDesktopPropertySetter () throws Exception
	{
		if (null == _propSetter)
		{
			final Class<?>	vc=getValuesClass();
			if (null == (_propSetter=vc.getMethod("setDesktopProperty", String.class, Object.class)))
				throw new NoSuchMethodException("getDesktopPropertySetter() no method found");
			if (!_propSetter.isAccessible())
				_propSetter.setAccessible(true);
		}

		return _propSetter;
	}

	public static final String	PROPNAME_ATTR=NAME_ATTR;
	public Object setProperty (T src, Element elem) throws Exception
	{
		final String	name=elem.getAttribute(PROPNAME_ATTR);
		if ((null == name) || (name.length() <= 0))
			throw new IllegalStateException("No desktop attribute name in element=" + DOMUtils.toString(elem));

		final XmlValueInstantiator<?>	proxy=getValueInstantiator(name, elem);
		final Object					o=(null == proxy) ? null : proxy.fromXml(elem);
		if (o != null)
		{
			final Method	sm=getDesktopPropertySetter();
			sm.invoke(src, name, o);
		}

		return o;
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public T fromXmlChild (T src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isPropertyElement(elem, tagName))
		{
			setProperty(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final ToolkitReflectiveProxy<Toolkit>	TOOLKIT=
			new ToolkitReflectiveProxy<Toolkit>(Toolkit.class, true) {
				/*
				 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
				 */
				@Override
				public Toolkit createInstance (Element elem) throws Exception
				{
					return (null == elem) ? null : Toolkit.getDefaultToolkit();
				}
		};
}
