/*
 * 
 */
package net.community.chest.jfree.jfreechart.data;

import java.awt.Component;
import java.util.Map;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.ui.helpers.HelperUtils;
import net.community.chest.ui.helpers.SectionsMap;
import net.community.chest.ui.helpers.SectionsMapImpl;
import net.community.chest.ui.helpers.XmlDocumentComponentInitializer;

import org.jfree.chart.JFreeChart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 2, 2009 3:01:45 PM
 */
public class HelperChartPanel extends BaseChartPanel implements XmlDocumentComponentInitializer {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3001950821967168950L;
	public HelperChartPanel (JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean save, boolean print, boolean zoom, boolean tooltips,
			boolean autoLayout)
	{
		super(chart, width, height, minimumDrawWidth, minimumDrawHeight,
				maximumDrawWidth, maximumDrawHeight, useBuffer, properties, save,
				print, zoom, tooltips);
		if (autoLayout)
			layoutComponent();
	}

	public HelperChartPanel (JFreeChart chart, int width, int height,
			int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
			int maximumDrawHeight, boolean useBuffer, boolean properties,
			boolean save, boolean print, boolean zoom, boolean tooltips)
	{
		this(chart, width, height, minimumDrawWidth, minimumDrawHeight,
			 maximumDrawWidth, maximumDrawHeight, useBuffer, properties, save,
			 print, zoom, tooltips,
			 true);
	}

	public HelperChartPanel (JFreeChart chart, boolean properties,
			boolean save, boolean print, boolean zoom, boolean tooltips)
	{
        this(chart,
             DEFAULT_WIDTH,
             DEFAULT_HEIGHT,
             DEFAULT_MINIMUM_DRAW_WIDTH,
             DEFAULT_MINIMUM_DRAW_HEIGHT,
             DEFAULT_MAXIMUM_DRAW_WIDTH,
             DEFAULT_MAXIMUM_DRAW_HEIGHT,
             DEFAULT_BUFFER_USED,
             properties,
             save,
             print,
             zoom,
             tooltips
        );
	}

	public HelperChartPanel (JFreeChart chart, boolean useBuffer, boolean autoLayout)
	{
        this(chart,
                DEFAULT_WIDTH,
                DEFAULT_HEIGHT,
                DEFAULT_MINIMUM_DRAW_WIDTH,
                DEFAULT_MINIMUM_DRAW_HEIGHT,
                DEFAULT_MAXIMUM_DRAW_WIDTH,
                DEFAULT_MAXIMUM_DRAW_HEIGHT,
                useBuffer,
                true,  // properties
                true,  // save
                true,  // print
                true,  // zoom
                true,   // tooltips
                autoLayout
           );
	}

	public HelperChartPanel (JFreeChart chart, boolean useBuffer)
	{
		this(chart, useBuffer, true);
	}

	public HelperChartPanel (JFreeChart chart)
	{
        this(chart, DEFAULT_BUFFER_USED);
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

	public HelperChartPanel (JFreeChart chart, Document doc, boolean autoLayout)
	{
		this(chart, DEFAULT_BUFFER_USED, false /* no auto-layout yet */);

		setComponentDocument(doc);
		if (autoLayout)
			layoutComponent();
	}

	public HelperChartPanel (JFreeChart chart, Document doc)
	{
		this(chart, doc, true);
	}

	public HelperChartPanel (JFreeChart chart, Element elem, boolean autoLayout)
	{
		this(chart, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public HelperChartPanel (Element elem, boolean autoLayout)
	{
		this(null, elem, autoLayout);
	}

	public HelperChartPanel (JFreeChart chart, Element elem)
	{
		this(chart, elem, true);
	}

	public HelperChartPanel (Element elem)
	{
		this(elem, true);
	}
}
