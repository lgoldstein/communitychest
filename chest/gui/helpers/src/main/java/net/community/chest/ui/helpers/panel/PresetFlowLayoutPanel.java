/*
 * 
 */
package net.community.chest.ui.helpers.panel;

import java.awt.FlowLayout;

import net.community.chest.awt.layout.FlowLayoutAlignment;
import net.community.chest.dom.impl.StandaloneDocumentImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 15, 2009 12:39:59 PM
 */
public class PresetFlowLayoutPanel extends PresetLayoutPanel<FlowLayout> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6179365242605286844L;

	public PresetFlowLayoutPanel (FlowLayout l, Document doc, boolean autoLayout)
	{
		super(FlowLayout.class, l, doc, autoLayout);
	}

	public PresetFlowLayoutPanel (FlowLayout l, Element elem, boolean autoLayout)
	{
		this(l, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public PresetFlowLayoutPanel (FlowLayout l, Element elem)
	{
		this(l, elem, true);
	}

	public PresetFlowLayoutPanel (FlowLayout l, Document doc)
	{
		this(l, doc, true);
	}

	public PresetFlowLayoutPanel (FlowLayout l, boolean autoLayout)
	{
		this(l, (Document) null, autoLayout);
	}
	
	public PresetFlowLayoutPanel (FlowLayout l)
	{
		this(l, true);
	}

	public static final FlowLayoutAlignment	DEFAULT_ALIGNMENT=FlowLayoutAlignment.LEFT;
	public static final int					DEFAULT_HGAP=0, DEFAULT_VGAP=0;

	public PresetFlowLayoutPanel (FlowLayoutAlignment align, int hGap, int vGap, boolean autoLayout)
	{
		this(new FlowLayout(((null == align) ? DEFAULT_ALIGNMENT : align).getAlignment(), hGap, vGap), (Document) null, autoLayout);
	}

	public PresetFlowLayoutPanel (FlowLayoutAlignment align, int hGap, int vGap)
	{
		this(align, hGap, vGap, true);
	}

	public PresetFlowLayoutPanel (int hGap, int vGap, boolean autoLayout)
	{
		this(DEFAULT_ALIGNMENT, hGap, vGap, autoLayout);
	}

	public PresetFlowLayoutPanel (int hGap, int vGap)
	{
		this(hGap, vGap, true);
	}

	public PresetFlowLayoutPanel (FlowLayoutAlignment align, boolean autoLayout)
	{
		this(align, DEFAULT_HGAP, DEFAULT_VGAP, autoLayout);
	}
	
	public PresetFlowLayoutPanel (FlowLayoutAlignment align)
	{
		this(align, true);
	}

	public PresetFlowLayoutPanel (boolean autoLayout)
	{
		this(DEFAULT_ALIGNMENT, autoLayout);
	}

	public PresetFlowLayoutPanel ()
	{
		this(true);
	}
}
