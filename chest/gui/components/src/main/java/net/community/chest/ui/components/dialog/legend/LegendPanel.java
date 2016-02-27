/*
 * 
 */
package net.community.chest.ui.components.dialog.legend;

import java.awt.Component;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.StandaloneDocumentImpl;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.swing.component.label.JLabelReflectiveProxy;
import net.community.chest.ui.helpers.panel.PresetGridLayoutPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 11, 2009 12:46:56 PM
 */
public class LegendPanel extends PresetGridLayoutPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -238086921667578017L;

	public LegendPanel (int hgap, int vgap, Document doc, boolean autoLayout)
	{
		super(0, 1, hgap, vgap, doc, autoLayout);
	}

	public LegendPanel (int hgap, int vgap, Element elem, boolean autoLayout)
	{
		this(hgap, vgap, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public LegendPanel (int hgap, int vgap, boolean autoLayout)
	{
		this(hgap, vgap, (Document) null, autoLayout);
	}

	public LegendPanel (int hgap, int vgap)
	{
		this(hgap, vgap, true);
	}

	public LegendPanel (Document doc, boolean autoLayout)
	{
		this(0, 5, doc, autoLayout);
	}

	public LegendPanel (boolean autoLayout)
	{
		this((Document) null, autoLayout);
	}

	public LegendPanel ()
	{
		this(true);
	}

	public LegendPanel (Document doc)
	{
		this(doc, true);
	}

	public LegendPanel (Element elem, boolean autoLayout)
	{
		this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}
	
	public LegendPanel (Element elem)
	{
		this(elem, true);
	}

	public boolean isLegendSection (String name, Element elem)
	{
		if ((null == name) || (name.length() <= 0) || (null == elem))
			return false;

		return true;
	}

	public XmlValueInstantiator<? extends Component> getLegendEntryConverter (Element elem)
	{
		return (null == elem) ? null : JLabelReflectiveProxy.LABEL;
	}

	public Component createLegendEntry (String name, Element elem)
	{
		try
		{
			final XmlValueInstantiator<? extends Component>	lsi=getLegendEntryConverter(elem);
			return (null == lsi) ? null : lsi.fromXml(elem);
		}
		catch(Exception e)
		{
			throw new IllegalStateException("createLegendEntry(" + name + ") " + e.getClass().getName() + " while handling element=" + DOMUtils.toString(elem) + ": " + e.getMessage());
		}
	}
	/* Each section is assumed to be a legend entry
	 * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutSection(java.lang.String, org.w3c.dom.Element)
	 */
	@Override
	public void layoutSection (String name, Element elem)
	{
		if (isLegendSection(name, elem))
		{
			final Component	c=createLegendEntry(name, elem);
			if (c != null)
				add(c);
		}

		super.layoutSection(name, elem);
	}

	public Component addLegendEntry (final String t, final Icon i)
	{
		if (((null == t) || (t.length() <= 0)) && (null == i))
			return null;

		final JLabel	lbl=new JLabel(t, i, SwingConstants.LEFT);
		add(lbl);
		return lbl;
	}

	public Component addLegendEntry (final Map.Entry<String, ? extends Icon> le)
	{
		return (null == le) ? null : addLegendEntry(le.getKey(), le.getValue());
	}

	public Collection<Component> addLegendEntries (final Collection<? extends Map.Entry<String, ? extends Icon>> ll)
	{
		final int	numEntries=(null == ll) ? 0 : ll.size();
		if (numEntries <= 0)
			return null;

		Collection<Component>	rl=null;
		for (final Map.Entry<String, ? extends Icon> le : ll)
		{
			final Component	c=addLegendEntry(le);
			if (null == c)
				continue;

			if (null == rl)
				rl = new LinkedList<Component>();
			rl.add(c);
		}

		return rl;
	}

	public Collection<Component> addLegendEntries(@SuppressWarnings("unchecked") final Map.Entry<String, ? extends Icon> ... ll)
	{
		return ((null == ll) || (ll.length <= 0)) ? null : addLegendEntries(Arrays.asList(ll));
	}
}
