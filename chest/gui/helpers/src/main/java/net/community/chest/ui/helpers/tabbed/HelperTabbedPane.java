/*
 * 
 */
package net.community.chest.ui.helpers.tabbed;

import java.awt.Component;
import java.util.Collection;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.tabbed.BaseTabbedPane;
import net.community.chest.swing.component.tabbed.TabLayoutPolicy;
import net.community.chest.swing.component.tabbed.TabPlacement;
import net.community.chest.ui.helpers.HelperUtils;
import net.community.chest.ui.helpers.SectionsMap;
import net.community.chest.ui.helpers.SectionsMapImpl;
import net.community.chest.ui.helpers.XmlDocumentComponentInitializer;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 23, 2008 9:00:56 AM
 */
public class HelperTabbedPane extends BaseTabbedPane implements XmlDocumentComponentInitializer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5429659135069061352L;
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
	 * @see net.community.chest.swing.component.tabbed.BaseTabbedPane#getPaneConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPaneConverter (Element elem)
	{
		return (null == elem) ? null : HelperTabbedPaneReflectiveProxy.TABBEDHLPR;
	}

	public boolean isTabEntry (
			@SuppressWarnings("unused") final String tabName, final Element elem)
	{
		return HelperTabbedPaneReflectiveProxy.isDefaultTabElement(elem);
	}

	public boolean isTabEntry (final Map.Entry<String,? extends Element> te)
	{
		return isTabEntry((null == te) ? null : te.getKey(), (null == te) ? null : te.getValue());
	}

	public void addTabEntry (final String tabName, final Element elem)
	{
		if (isTabEntry(tabName, elem))
			throw new UnsupportedOperationException("addTabEntry(" + tabName + ") N/A for " + DOMUtils.toString(elem));
	}

	public void addTabEntry (final Map.Entry<String,? extends Element> te)
	{
		addTabEntry((null == te) ? null : te.getKey(), (null == te) ? null : te.getValue());
	}
	/*
	 * @see net.community.chest.ui.helpers.XmlContainerComponentInitializer#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem) throws RuntimeException
	{
		addTabEntry(name, elem);
	}

	public void addTabs (final Collection<? extends Map.Entry<String,? extends Element>>	tl)
	{
		if ((null == tl) || (tl.size() <= 0))
			return;

		for (final Map.Entry<String,? extends Element> te : tl)
		{
			if (!isTabEntry(te))
				continue;

			addTabEntry(te);
		}
	}

	public void addTabs (final Map<String,? extends Element> tabsMap)
	{
		addTabs(((null == tabsMap) || (tabsMap.size() <= 0)) ? null : tabsMap.entrySet());
	}
	/* NOTE: assumes that "sections" are potentially tabs to be added at end of this method
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

	public HelperTabbedPane (Document doc, boolean autoInit)
	{
		setComponentDocument(doc);
		if (autoInit)
			layoutComponent();
	}

	public HelperTabbedPane (Element elem, boolean autoInit)
	{
		this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
	}

	public HelperTabbedPane (boolean autoInit)
	{
		this((Document) null, autoInit);
	}

	public HelperTabbedPane (int placement, boolean autoInit)
	{
		super(placement);

		if (autoInit)
			layoutComponent();
	}

	public HelperTabbedPane (int placement, int layoutPolicy, boolean autoInit)
	{
		super(placement, layoutPolicy);

		if (autoInit)
			layoutComponent();
	}

	public HelperTabbedPane (Element elem)
	{
		this(elem, true);
	}

	public HelperTabbedPane (Document doc)
	{
		this(doc, true);
	}

	public HelperTabbedPane ()
	{
		this(true);
	}

	public HelperTabbedPane (int placement)
	{
		this(placement, true);
	}

	public HelperTabbedPane (int placement, int layoutPolicy)
	{
		this(placement, layoutPolicy, true);
	}

	public HelperTabbedPane (TabPlacement p, TabLayoutPolicy l, boolean autoInit)
	{
		this(p.getPlacement(), l.getPolicy(), autoInit);
	}

	public HelperTabbedPane (TabPlacement p, TabLayoutPolicy l)
	{
		this(p, l, true);
	}

	public HelperTabbedPane (TabPlacement p, boolean autoInit)
	{
		this(p.getPlacement(), autoInit);
	}

	public HelperTabbedPane (TabPlacement p)
	{
		this(p, true);
	}
}
