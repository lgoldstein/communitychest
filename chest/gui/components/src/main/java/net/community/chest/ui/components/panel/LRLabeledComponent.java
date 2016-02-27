/*
 * 
 */
package net.community.chest.ui.components.panel;

import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.Icon;
import javax.swing.JLabel;

import net.community.chest.awt.TypedComponentAssignment;
import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.awt.layout.FlowLayoutAlignment;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.lang.TypedValuesContainer;
import net.community.chest.swing.HAlignmentValue;
import net.community.chest.swing.component.label.BaseLabel;
import net.community.chest.ui.helpers.panel.PresetFlowLayoutPanel;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Provides a panel that allows adding a {@link JLabel} to describe
 * some other displayed {@link Component}. The panel allows arranging the
 * label/component either horizontally or vertically and also define whether
 * the label is displayed first (or the component).</P>
 * 
 * @param <C> The {@link Component} type being labeled
 * @author Lyor G.
 * @since Mar 12, 2009 8:38:46 AM
 */
public class LRLabeledComponent<C extends Component> extends PresetFlowLayoutPanel
		implements TypedValuesContainer<C>, TypedComponentAssignment<C>,
				  Iconable, Titled {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5457988173257644169L;
	private final Class<C>	_valsClass; 
	/*
	 * @see net.community.chest.lang.TypedValuesContainer#getValuesClass()
	 */
	@Override
	public final Class<C> getValuesClass ()
	{
		return _valsClass;
	}

	private C	_value;
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#getAssignedValue()
	 */
	@Override
	public C getAssignedValue ()
	{
		return _value;
	}
	/*
	 * @see net.community.chest.ui.helpers.TypedComponentAssignment#setAssignedValue(java.lang.Object)
	 */
	@Override
	public void setAssignedValue (C v)
	{
		_value = v;
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.HelperPanel#getPanelConverter(org.w3c.dom.Element)
	 */
	@Override
	protected XmlProxyConvertible<?> getPanelConverter (Element elem)
	{
		return (null == elem) ? null : LRLabeledComponentReflectiveProxy.LRLBLCOMP;
	}

	public C getLabeledComponent ()
	{
		return getAssignedValue();
	}
	// NOTE might have no effect if called after {@link #layoutComponent}
	public void setLabeledComponent (C c)
	{
		setAssignedValue(c);
	}

	private JLabel	_lbl;
	protected JLabel getLabel (boolean createIfNoExist)
	{
		if ((null == _lbl) && createIfNoExist)
			_lbl = new BaseLabel();
		return _lbl;
	}

	public JLabel getLabel ()
	{
		return getLabel(false);
	}

	public void setLabel (JLabel lbl)
	{
		_lbl = lbl;
	}
	/*
	 * @see net.community.chest.awt.attributes.Titled#getTitle()
	 */
	@Override
	public String getTitle ()
	{
		final JLabel	l=getLabel();
		return (null == l) ? null : l.getText();
	}
	/*
	 * @see net.community.chest.awt.attributes.Titled#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle (final String t)
	{
		final JLabel	l=getLabel(true);
		if (l != null)
			l.setText((null == t) ? "" : t);
	}
	/*
	 * @see net.community.chest.awt.attributes.Iconable#getIcon()
	 */
	@Override
	public Icon getIcon ()
	{
		final JLabel	l=getLabel();
		return (null == l) ? null : l.getIcon();
	}
	/*
	 * @see net.community.chest.awt.attributes.Iconable#setIcon(javax.swing.Icon)
	 */
	@Override
	public void setIcon (Icon i)
	{
		final JLabel	l=getLabel(true);
		if (l != null)
			l.setIcon(i);
	}

	private boolean	_lblLastPos	/* =false */;
	public boolean isLabelLastPos ()
	{
		return _lblLastPos;
	}

 	public void setLabelLastPos (final boolean lblLastPos)
	{
		if (lblLastPos != isLabelLastPos())
			_lblLastPos = lblLastPos;
	}

	protected void layoutComponent (JLabel lbl, C comp)
	{
		final boolean		lblLastPos=isLabelLastPos();
		final Component[]	ca={ lblLastPos ? comp : lbl, lblLastPos ? lbl : comp };
		for (final Component c : ca)
		{
			if (null == c)
				continue;
			add(c);
		}
	}
	/*
	 * @see net.community.chest.ui.helpers.panel.HelperPanel#layoutComponent()
	 */
	@Override
	public void layoutComponent () throws RuntimeException
	{
		super.layoutComponent();

		final C			comp=getLabeledComponent();
		final JLabel	lbl=getLabel(true);
		layoutComponent(lbl, comp);
	}

	public LRLabeledComponent (Class<C> vc, C v, JLabel l, boolean lblLastPos, FlowLayoutAlignment fla, int gap, Document doc, boolean autoLayout)
	{
		super(new FlowLayout(((null == fla) ? DEFAULT_ALIGNMENT : fla).getAlignment(), gap, 0), doc, false /* delay auto-layout till initialized the value and its class */);

		if (null == (_valsClass=vc))
			throw new IllegalArgumentException("No values class specified");

		_lblLastPos = lblLastPos;
		_value = v;
		_lbl = l;

		if (autoLayout)
			layoutComponent();
	}

	public LRLabeledComponent (Class<C> vc, C v, String text, Icon icon, HAlignmentValue align, boolean lblLastPos, FlowLayoutAlignment fla, int gap, boolean autoLayout)
	{
		this(vc, v, new BaseLabel(text, icon, (null == align) ? Integer.MIN_VALUE : align.getAlignmentValue()), lblLastPos, fla, gap, null, autoLayout);
	}

	public LRLabeledComponent (Class<C> vc, C v, String text, Icon icon, HAlignmentValue align, boolean lblLastPos, FlowLayoutAlignment fla, int gap)
	{
		this(vc, v, text, icon, align, lblLastPos, fla, gap, true);
	}

	public static final int	DEFAULT_GAP=5;
	public LRLabeledComponent (Class<C> vc, C v, JLabel lbl, boolean lblLastPos, boolean autoLayout)
	{
		this(vc, v, lbl, lblLastPos, DEFAULT_ALIGNMENT, DEFAULT_GAP, null, autoLayout);
	}

	public LRLabeledComponent (Class<C> vc, C v, JLabel lbl)
	{
		this(vc, v, lbl, false, true);
	}

	public LRLabeledComponent (Class<C> vc, C v)
	{
		this(vc, v, (JLabel) null);
	}

	public LRLabeledComponent (Class<C> vc)
	{
		this(vc, null);
	}

	@SuppressWarnings("unchecked")
	public LRLabeledComponent (C v, String text, Icon icon, HAlignmentValue align, boolean lblLastPos, FlowLayoutAlignment fla, int gap, boolean autoLayout)
	{
		this((null == v) ? null : (Class<C>) v.getClass(), v, text, icon, align, lblLastPos, fla, gap, autoLayout);
	}

	public LRLabeledComponent (C v, String text, Icon icon, HAlignmentValue align, boolean lblLastPos, boolean autoLayout)
	{
		this(v, text, icon, align, lblLastPos, DEFAULT_ALIGNMENT, DEFAULT_GAP, autoLayout);
	}

	public LRLabeledComponent (C v, String text, Icon icon, HAlignmentValue align)
	{
		this(v, text, icon, align, false, true);
	}

	public static final HAlignmentValue	DEFAULT_LABEL_ALIGNMENT=HAlignmentValue.LEFT;
	public LRLabeledComponent (Class<C> vc, C v, String text, Icon icon, HAlignmentValue align, boolean lblLastPos, boolean autoLayout)
	{
		this(vc, v, text, icon, align, lblLastPos, DEFAULT_ALIGNMENT, DEFAULT_GAP, autoLayout);
	}

	public LRLabeledComponent (Class<C> vc, C v, String text, Icon icon, boolean lblLastPos, boolean autoLayout)
	{
		this(vc, v, text, icon, DEFAULT_LABEL_ALIGNMENT, lblLastPos, autoLayout);
	}

	public LRLabeledComponent (Class<C> vc, C v, String text, Icon icon)
	{
		this(vc, v, text, icon, false, true);
	}

	public LRLabeledComponent (Class<C> vc, C v, String text)
	{
		this(vc, v, text, null);
	}

	public LRLabeledComponent (C v, String text, Icon icon)
	{
		this(v, text, icon, DEFAULT_LABEL_ALIGNMENT, false, true);
	}

	public LRLabeledComponent (C v, String text)
	{
		this(v, text, null);
	}

	public LRLabeledComponent (C v, Icon icon)
	{
		this(v, null, icon);
	}

	@SuppressWarnings("unchecked")
	public LRLabeledComponent (C v, JLabel lbl, boolean lblLastPos, boolean autoLayout)
	{
		this((null == v) ? null : (Class<C>) v.getClass(), v, lbl, lblLastPos, DEFAULT_ALIGNMENT, DEFAULT_GAP, null, autoLayout);
	}

	public LRLabeledComponent (C v, JLabel lbl)
	{
		this(v, lbl, false, true);
	}

	public LRLabeledComponent (C v)
	{
		this(v, (String) null);
	}
}
