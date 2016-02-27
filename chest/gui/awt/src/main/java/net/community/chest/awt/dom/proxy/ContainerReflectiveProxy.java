package net.community.chest.awt.dom.proxy;

import java.awt.Component;
import java.awt.Container;
import java.awt.FocusTraversalPolicy;
import java.awt.LayoutManager;
import java.util.Collection;
import java.util.Map;

import net.community.chest.awt.focus.FocusTraversalPolicyReflectiveProxy;
import net.community.chest.awt.layout.dom.AbstractLayoutConstraintXmlValueInstantiator;
import net.community.chest.awt.layout.dom.AbstractLayoutManagerReflectiveProxy;
import net.community.chest.awt.layout.dom.LayoutConstraintXmlValueInstantiator;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.ExceptionUtil;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <C> The reflected {@link Container} type
 * @author Lyor G.
 * @since Mar 20, 2008 8:35:34 AM
 */
public class ContainerReflectiveProxy<C extends Container> extends ComponentReflectiveProxy<C> {
	public ContainerReflectiveProxy (Class<C> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	protected ContainerReflectiveProxy (Class<C> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public boolean isLayoutElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, AbstractLayoutManagerReflectiveProxy.LAYOUT_ELEMNAME);
	}

	public XmlValueInstantiator<? extends LayoutManager> getLayoutConverter (final Element elem) throws Exception
	{
		return AbstractLayoutManagerReflectiveProxy.getLayoutConverter(elem);
	}

	public LayoutManager createLayoutInstance (final C src, final Element elem) throws Exception
	{
		return AbstractLayoutManagerReflectiveProxy.createLayoutManager(src, elem);
	}

	public LayoutManager setLayout (final C src, final Element elem) throws Exception
	{
		final LayoutManager	l=createLayoutInstance(src, elem);
		if (l != null)
			src.setLayout(l);

		return l;
	}

	public LayoutConstraintXmlValueInstantiator<?,?> getConstraintInstantiator (C src, Element elem, boolean useSourceLayout) throws Exception
	{
		return useSourceLayout
			? AbstractLayoutConstraintXmlValueInstantiator.getLayoutConstraintInstantiator(src)
			: AbstractLayoutConstraintXmlValueInstantiator.getLayoutConstraintInstantiator(elem)
			;
	}

	public <V extends Component> V addConstrainedComponent (C src, V comp, Element elem, boolean useSourceLayout) throws Exception
	{
		if ((null == comp) || (null == elem))
			return null;

		final LayoutConstraintXmlValueInstantiator<?,?>	lci=
			getConstraintInstantiator(src, elem, useSourceLayout);
		final Object									cv=
			(null == lci) ? null : lci.fromXmlContainer(elem);
		if (null == cv)
			src.add(comp);
		else
			src.add(comp, cv);
		return comp;
	}
	
	public boolean isFocusTraversalPolicy (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, FocusTraversalPolicy.class.getSimpleName());
	}

	protected XmlValueInstantiator<? extends FocusTraversalPolicy> getFocusPolicyConverter (Element elem) throws Exception
	{
		return (null == elem) ? null : FocusTraversalPolicyReflectiveProxy.BY_NAME_POLICY;
	}

	public FocusTraversalPolicy setFocusTraversalPolicy (final C src, final Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends FocusTraversalPolicy>	proxy=
			getFocusPolicyConverter(elem);
		final FocusTraversalPolicy	p=
			(null == proxy) ? null : proxy.fromXml(elem);
		if (p != null)
			src.setFocusTraversalPolicy(p);
		return p;
	}
	/*
	 * @see net.community.chest.awt.dom.proxy.ComponentReflectiveProxy#fromXmlChild(java.awt.Component, org.w3c.dom.Element)
	 */
	@Override
	public C fromXmlChild (C src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isLayoutElement(elem, tagName))
		{
			setLayout(src, elem);
			return src;
		}
		else if (isFocusTraversalPolicy(elem, tagName))
		{
			setFocusTraversalPolicy(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}
	/**
	 * @param c The {@link Container} instance
	 * @param sMap The sections/components {@link Map} - key={@link Component#getName()} 
	 * (preferably case <U>insensitive</U>), value=the XML
	 * {@link Element} to be used to initialize the component
	 * @return A {@link Collection} of "pairs" of all the {@link Component} on
	 * which XML element has been applied - key=the {@link Component}, value=
	 * the XML {@link Element} that was used to initialize the component
	 * @throws RuntimeException If failed to apply the XML element
	 * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#getDefaultObjectProxy(Object, Class)
	 */
	public static final Collection<Map.Entry<Component,Element>> applyContainedComponentsSections (
			final Container c, final Map<String, ? extends Element> sMap) throws RuntimeException
	{
		if ((null == sMap) || (sMap.size() <= 0))
			return null;

		return applyComponentsSections(sMap, c.getComponents());
	}

	public static final <V extends Component> V addConstrainedComponent (Container c, V comp, Node constValue)
	{
		if (null == comp)
			return comp;

		final LayoutManager														l=
			(null == c) ? null : c.getLayout();
		final LayoutConstraintXmlValueInstantiator<? extends LayoutManager,?>	cl=
			AbstractLayoutConstraintXmlValueInstantiator.getLayoutConstraintInstantiator(l);
		if (null == cl)
			throw new IllegalStateException("addConstrainedComponent(" + constValue + ") no constraint initializaer");

		final Node		effNode=cl.isConstraintNode(constValue) ? constValue : cl.getContainerConstraintNode(constValue);
		final Object	cValue;
		try
		{
			if (null == (cValue=cl.fromConstraintNode(effNode)))
				throw new IllegalStateException("No constraint extracted");
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		c.add(comp, cValue);
		return comp;
	}

	public static final ContainerReflectiveProxy<Container>	CONTAINER=
			new ContainerReflectiveProxy<Container>(Container.class, true);
}
