/*
 * 
 */
package net.community.chest.ui.helpers.dialog;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.util.Map;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.swing.component.dialog.BaseDialog;
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
 * @since Oct 30, 2008 11:11:53 AM
 */
public class HelperDialog extends BaseDialog implements XmlDocumentComponentInitializer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2724958554422360212L;
	public HelperDialog (boolean autoInit)
	{
		this((Document) null, autoInit);
	}

	public HelperDialog (Frame owner, boolean modal, boolean autoInit)
	{
		super(owner, modal);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Dialog owner, boolean modal, boolean autoInit)
	{
		super(owner, modal);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Frame owner, Element elem, boolean autoInit)
	{
		this(owner, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
	}

	public HelperDialog (Element elem, boolean autoInit)
	{
		this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
	}

	public HelperDialog (Dialog owner, Element elem, boolean autoInit)
	{
		this(owner, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
	}

	public HelperDialog (Document doc, boolean autoInit)
	{
		setComponentDocument(doc);
		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Frame owner, Document doc, boolean autoInit)
	{
		super(owner);

		setComponentDocument(doc);
		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Dialog owner, Document doc, boolean autoInit)
	{
		super(owner);

		setComponentDocument(doc);
		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Frame owner, String title, boolean modal, boolean autoInit)
	{
		super(owner, title, modal);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Dialog owner, String title, boolean modal, boolean autoInit)
	{
		super(owner, title, modal);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Frame owner, String title, boolean modal, GraphicsConfiguration gc, boolean autoInit)
	{
		super(owner, title, modal, gc);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Dialog owner, String title, boolean modal, GraphicsConfiguration gc, boolean autoInit)
	{
		super(owner, title, modal, gc);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Window owner, boolean autoInit)
	{
		this(owner, (Document) null, autoInit);
	}

	public HelperDialog (Window owner, Element elem, boolean autoInit)
	{
		this(owner, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoInit);
	}

	public HelperDialog (Window owner, Document doc, boolean autoInit)
	{
		super(owner);

		setComponentDocument(doc);
		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Window owner, ModalityType modalityType, boolean autoInit)
	{
		super(owner, modalityType);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc, boolean autoInit)
	{
		super(owner, title, modalityType, gc);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Window owner, String title, ModalityType modalityType, boolean autoInit)
	{
		super(owner, title, modalityType);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Window owner, String title, boolean autoInit)
	{
		super(owner, title);

		if (autoInit)
			layoutComponent();
	}

	public HelperDialog (Window owner, String title, ModalityType modalityType)
	{
		this(owner, title, modalityType, true);
	}

	public HelperDialog (Window owner, String title, ModalityType modalityType, GraphicsConfiguration gc)
	{
		this(owner, title, modalityType, gc, true);
	}

	public HelperDialog (Window owner, ModalityType modalityType)
	{
		this(owner, modalityType, true);
	}

	public HelperDialog (Window owner, String title)
	{
		this(owner, title, true);
	}

	public HelperDialog (Window owner)
	{
		this(owner, true);
	}

	public HelperDialog (Window owner, Element elem)
	{
		this(owner, elem, true);
	}

	public HelperDialog (Window owner, Document doc)
	{
		this(owner, doc, true);
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
										   final Object 				object,
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
				setComponentElement(elem);	// remember the last used
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
		setComponentDocument(doc);	// remember the last used
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
	 * @see net.community.chest.swing.component.dialog.BaseDialog#getDialogConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getDialogConverter (Element elem)
	{
		return (null == elem) ? null : HelperDialogReflectiveProxy.HLPRDLG;
	}

	public HelperDialog ()
	{
		this(true);
	}

	public HelperDialog (Frame owner)
	{
		this(owner, false);
	}

	public HelperDialog (Dialog owner)
	{
		this(owner, false);
	}

	public HelperDialog (Frame owner, boolean modal)
	{
		this(owner, modal, true);
	}

	public HelperDialog (Frame owner, String title)
	{
		this(owner, title, false);
	}

	public HelperDialog (Dialog owner, boolean modal)
	{
		this(owner, modal, true);
	}

	public HelperDialog (Dialog owner, String title)
	{
		this(owner, title, false);
	}

	public HelperDialog (Frame owner, Element elem)
	{
		this(owner, elem, true);
	}

	public HelperDialog (Element elem)
	{
		this(elem, true);
	}

	public HelperDialog (Dialog owner, Element elem)
	{
		this(owner, elem, true);
	}

	public HelperDialog (Document doc)
	{
		this(doc, true);
	}

	public HelperDialog (Frame owner, Document doc)
	{
		this(owner, doc, true);
	}

	public HelperDialog (Dialog owner, Document doc)
	{
		this(owner, doc, true);
	}

	public HelperDialog (Frame owner, String title, boolean modal)
	{
		this(owner, title, modal, true);
	}

	public HelperDialog (Dialog owner, String title, boolean modal)
	{
		this(owner, title, modal, true);
	}

	public HelperDialog (Frame owner, String title, boolean modal, GraphicsConfiguration gc)
	{
		this(owner, title, modal, gc, true);
	}

	public HelperDialog (Dialog owner, String title, boolean modal, GraphicsConfiguration gc)
	{
		this(owner, title, modal, gc, true);
	}
}
