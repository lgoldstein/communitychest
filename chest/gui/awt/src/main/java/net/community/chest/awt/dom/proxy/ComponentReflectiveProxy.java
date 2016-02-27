package net.community.chest.awt.dom.proxy;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import net.community.chest.awt.ComponentSizeType;
import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <C> The reflected {@link Component} type
 * @author Lyor G.
 * @since Mar 20, 2008 8:16:49 AM
 */
public class ComponentReflectiveProxy<C extends Component> extends UIReflectiveAttributesProxy<C> {
	public ComponentReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected ComponentReflectiveProxy (Class<C> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}
	/*
	 * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#handleUnknownAttribute(java.lang.Object, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
	protected C handleUnknownAttribute (C src, String name, String value, Map<String,? extends Method> accsMap) throws Exception
	{
		final ComponentSizeType	t=getRelativeComponentSizeAttribute(name);
		if (t != null)
		{
			final Dimension	sz=getRelativeScreenSize(name, value);
			t.setSize(src, sz);
			return src;
		}

		return super.handleUnknownAttribute(src, name, value, accsMap);
	}

	public Font setFont (C src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends Font>	conv=getFontConverter(elem);
		final Font									f=conv.fromXml(elem);
		if (f != null)
			src.setFont(f);

		return f;
	}
	/*
	 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
	 */
	@Override
	public C fromXmlChild (C src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		// NOTE: font may also be set as an attribute string - see FontValueStringInstantiator
		if (isFontElement(elem, tagName))
		{
			setFont(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}
	// returns the used XmlProxyConvertible instance (null if none applied)
	public static final XmlProxyConvertible<?> applyComponentSection (final Component c, final Element elem) throws RuntimeException
	{
		if ((null == c) || (null == elem))
			return null;	// ignore if no component/element

		final XmlProxyConvertible<?>	proxy=getDefaultObjectProxy(c, Component.class);
		if (null == proxy)	// OK if no proxy found
			return null;

		try
		{
			@SuppressWarnings("unchecked")
			final Object	po=
				((XmlProxyConvertible<Object>) proxy).fromXml(c, elem);
			if (po != c)
				throw new IllegalStateException("applyComponentSection(" + c.getName() + ") mismatched re-constructed instances for element=" + DOMUtils.toString(elem));
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		return proxy;
	}
	// returns the used Element - null if none used/found
	public static final Element applyComponentSection (
			final Component c, final String cn, final Map<String, ? extends Element> sMap) throws RuntimeException
	{
		if ((null == c) || (null == sMap) || (sMap.size() <= 0))
			return null;

		final Element					elem=
			((null == cn) || (cn.length() <= 0)) ? null : sMap.get(cn);
		final XmlProxyConvertible<?>	p=
			(null == elem) ? null : applyComponentSection(c, elem);
		if (null == p)
			return null;

		return elem;
	}
	// returns the used Element - null if none used/found
	public static final Element applyComponentSection (
			final Component c, final Map<String, ? extends Element> sMap)  throws RuntimeException
	{

		return (null == c) ? null : applyComponentSection(c, c.getName(), sMap);
	}
	/**
	 * @param sMap The sections/components {@link Map} - key={@link Component#getName()} 
	 * (preferably case <U>insensitive</U>), value=the XML
	 * {@link Element} to be used to initialize the component
	 * @param comps The {@link Component}-s on which to apply the sections
	 * @return A {@link Collection} of "pairs" of all the {@link Component} on
	 * which XML element has been applied - key=the {@link Component}, value=
	 * the XML {@link Element} that was used to initialize the component
	 * @throws RuntimeException If failed to apply the XML element
	 * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#getDefaultObjectProxy(Object, Class)
	 */
	public static final Collection<Map.Entry<Component,Element>> applyComponentsSections (
			final Map<String, ? extends Element> sMap, final Collection<? extends Component> comps) throws RuntimeException
	{
		if ((null == sMap) || (sMap.size() <= 0)
		 || (null == comps) || (comps.size() <= 0))
			return null;

		Collection<Map.Entry<Component,Element>>	ret=null;
		for (final Component c : comps)
		{
			final Element	elem=applyComponentSection(c, sMap);
			if (null == elem)
				continue;

			if (null == ret)
				ret = new LinkedList<Map.Entry<Component,Element>>();
			ret.add(new MapEntryImpl<Component,Element>(c,elem));
		}

		return ret;
	}
	/**
	 * @param sMap The sections/components {@link Map} - key={@link Component#getName()} 
	 * (preferably case <U>insensitive</U>), value=the XML
	 * {@link Element} to be used to initialize the component
	 * @param comps The {@link Component}-s on which to apply the sections
	 * @return A {@link Collection} of "pairs" of all the {@link Component} on
	 * which XML element has been applied - key=the {@link Component}, value=
	 * the XML {@link Element} that was used to initialize the component
	 * @throws RuntimeException If failed to apply the XML element
	 * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#getDefaultObjectProxy(Object, Class)
	 */
	public static final Collection<Map.Entry<Component,Element>> applyComponentsSections (
			final Map<String, ? extends Element> sMap, final Component ... comps) throws RuntimeException
	{
		if ((null == sMap) || (sMap.size() <= 0)
		 || (null == comps) || (comps.length <= 0))
			return null;

		return applyComponentsSections(sMap, Arrays.asList(comps));
	}

	public static final ComponentReflectiveProxy<Component>	COMPONENT=
			new ComponentReflectiveProxy<Component>(Component.class, true);
}
