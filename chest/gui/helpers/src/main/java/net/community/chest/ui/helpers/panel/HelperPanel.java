/*
 * 
 */
package net.community.chest.ui.helpers.panel;

import java.awt.Component;
import java.awt.LayoutManager;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JTextField;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.panel.BasePanel;
import net.community.chest.ui.helpers.HelperUtils;
import net.community.chest.ui.helpers.SectionsMap;
import net.community.chest.ui.helpers.SectionsMapImpl;
import net.community.chest.ui.helpers.XmlDocumentComponentInitializer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 30, 2008 11:40:12 AM
 */
public class HelperPanel extends BasePanel implements XmlDocumentComponentInitializer {
	private static final long serialVersionUID = -9122273380194968112L;

	public HelperPanel (boolean isDoubleBuffered, boolean autoInit)
	{
		super(isDoubleBuffered);

		if (autoInit)
			layoutComponent();
	}

	public HelperPanel (Document doc, boolean autoInit)
	{
		setComponentDocument(doc);
		if (autoInit)
			layoutComponent();
	}

	public HelperPanel (LayoutManager layout, Element elem, boolean autoInit)
	{
		this(layout, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
	}

	public HelperPanel (LayoutManager layout, Element elem)
	{
		this(layout, elem, true);
	}

	public HelperPanel (Element elem, boolean autoInit)
	{
		this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
	}

	public HelperPanel (Element elem)
	{
		this(elem, true);
	}

	public HelperPanel (LayoutManager layout, Document doc, boolean autoInit)
	{
		super(layout);

		setComponentDocument(doc);
		if (autoInit)
			layoutComponent();
	}

	private Document	_doc	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.XmlDocumentComponentInitializer#getComponentDocument()
	 */
	@Override
	public synchronized Document getComponentDocument () throws RuntimeException
	{
		try
		{
			if (null == _doc)
				_doc = HelperUtils.getObjectComponentDocument(this, Component.class /* don't go below the UI hierarchy */, getClass());
			return _doc;
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlDocumentComponentInitializer#setComponentDocument(org.w3c.dom.Document)
	 */
	@Override
	public synchronized void setComponentDocument (final Document doc)
	{
		if (_doc != doc)
			_doc = doc;
	}

	private SectionsMap	_sectionsMap	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.XmlContainerComponentInitializer#getSectionsMap()
	 */
	@Override
	public SectionsMap getSectionsMap ()
	{
		return _sectionsMap;
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlContainerComponentInitializer#setSectionsMap(java.util.Map)
	 */
	@Override
	public void setSectionsMap (SectionsMap sectionsMap)
	{
		_sectionsMap = sectionsMap;
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlContainerComponentInitializer#addSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public Element addSection (final String name, final Element elem)
	{
		if ((null == name) || (name.length() <= 0) || (null == elem))
			return null;

		SectionsMap	sm=getSectionsMap();
		if (null == sm)
		{
			setSectionsMap(new SectionsMapImpl());
			if (null == (sm=getSectionsMap()))
				throw new IllegalStateException("No sections map instance though created");
		}

		return sm.put(name, elem);
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlContainerComponentInitializer#getSection(java.lang.String)
	 */
	@Override
	public Element getSection (final String name)
	{
		if ((null == name) || (name.length() <= 0))
			return null;

		final Map<String,? extends Element>	sMap=getSectionsMap();
		if ((null == sMap) || (sMap.size() <= 0))
			return null;

		return sMap.get(name);
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlContainerComponentInitializer#getSectionName(java.lang.Enum)
	 */
	@Override
	public <E extends Enum<E>> String getSectionName (E v)
	{
		return SectionsMapImpl.getSectionName(v);
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlContainerComponentInitializer#getSection(java.lang.Enum)
	 */
	@Override
	public <E extends Enum<E>> Element getSection (E v)
	{
		final String	n=getSectionName(v);
		return getSection(n);
	}

	public Element applyDefinitionElement (final String					name,
									   	   final Object					object,
									   	   final XmlProxyConvertible<?>	proxy) throws RuntimeException
	{
		return HelperUtils.applyDefinitionElement(this, name, object, proxy);
	}
	// calls "applyDefinitionElement" with "comp.getName()"
	public Element applyDefinitionElement (final Component comp, final XmlProxyConvertible<?> proxy) throws RuntimeException
	{
		return applyDefinitionElement((null == comp) ? null : comp.getName(), comp, proxy);
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlContainerComponentInitializer#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		if (null == elem)
			return;

		throw new UnsupportedOperationException("layoutSection(" + name + ") N/A for " + DOMUtils.toString(elem));
	}

	private Element	_elem	/* =null */;
	/*
	 * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#getComponentElement()
	 */
	@Override
	public synchronized Element getComponentElement () throws RuntimeException
	{
		if (null == _elem)
		{
			final Document	doc=getComponentDocument();
			if (null == doc)
			{
				final Map.Entry<String,Element>	ce=
					HelperUtils.getComponentObjectElement(getSectionsMap(), this);
				_elem = (null == ce) ? null : ce.getValue(); 
				
			}
			else
				_elem = doc.getDocumentElement();
		}

		return _elem;
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#setComponentElement(org.w3c.dom.Element)
	 */
	@Override
	public synchronized void setComponentElement (final Element elem)
	{
		if (_elem != elem)
			_elem = elem;
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlElementComponentInitializer#layoutComponent(org.w3c.dom.Element)
	 */
	@Override
	public void layoutComponent (final Element elem) throws RuntimeException
	{
		if (elem != null)
		{
			try
			{
				final Object	inst=fromXml(elem);
				if (inst != this)
					throw new IllegalStateException("laoyutComponent(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");
				setComponentElement(elem);	// remember last used
			}
			catch(Exception e)
			{
				throw ExceptionUtil.toRuntimeException(e);
			}
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlDocumentComponentInitializer#layoutComponent(org.w3c.dom.Document)
	 */
	@Override
	public void layoutComponent (final Document doc) throws RuntimeException
	{
		layoutComponent((null == doc) ? getComponentElement() : doc.getDocumentElement());
		setComponentDocument(doc);
	}
	/*
	 * @see net.community.chest.ui.helpers.ComponentInitializer#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		try
		{
			layoutComponent(getComponentDocument());
		}
		catch(Exception e)
		{
			throw ExceptionUtil.toRuntimeException(e);
		}

		HelperUtils.layoutSections(this);
	}
	/*
	 * @see net.community.chest.swing.component.panel.BasePanel#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (Element elem)
	{
		return (null == elem) ? null : HelperPanelReflectiveProxy.HLPRPNL;
	}
	// returns removed components
	protected Collection<Component> removeComponents ()
	{
		final int	numComps=getComponentCount();
		if (numComps <= 0)
			return null;

		final Component[]	ca=getComponents();
		// remove the components if added
		if ((null == ca) || (ca.length <= 0))
			return null;	// should not happen

		Collection<Component>	rl=null;
		for (final Component c : ca)
		{
			if ((c instanceof JTextField) || (c instanceof JButton))
			{
				remove(c);

				if (null == rl)
					rl = new LinkedList<Component>();
				rl.add(c);
			}
		}

		return rl;
	}
	/**
	 * Should be called if setter(s) called after {@link #layoutComponent()}
	 * has been called
	 * @return A {@link Collection} of the removed components - may be null/empty
	 */
	public Collection<Component> rebuildComponent ()
	{
		final Collection<Component>	rl=removeComponents();
		layoutComponent();
		return rl;
	}

	public HelperPanel (Document doc)
	{
		this(doc, true);
	}

	public HelperPanel (boolean autoLayout)
	{
		this((Document) null, autoLayout);
	}

	public HelperPanel (LayoutManager layout, boolean autoLayout)
	{
		this(layout, (Document) null, autoLayout);
	}

	public HelperPanel ()
	{
		this(true);
	}

	public HelperPanel (LayoutManager layout)
	{
		this(layout, true);
	}
}
