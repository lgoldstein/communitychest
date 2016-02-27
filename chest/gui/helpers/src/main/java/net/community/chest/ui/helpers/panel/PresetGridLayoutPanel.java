/*
 * 
 */
package net.community.chest.ui.helpers.panel;

import java.awt.GridLayout;

import net.community.chest.dom.impl.StandaloneDocumentImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Dec 16, 2008 1:36:02 PM
 */
public class PresetGridLayoutPanel extends PresetLayoutPanel<GridLayout> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1091543959344964466L;

	public PresetGridLayoutPanel (GridLayout lm, Document doc, boolean autoLayout)
	{
		super(GridLayout.class, lm, doc, autoLayout);
	}

	public PresetGridLayoutPanel (GridLayout lm, Element elem, boolean autoLayout)
	{
		this(lm, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public PresetGridLayoutPanel (GridLayout lm, boolean autoLayout)
	{
		this(lm, (Document) null, autoLayout);
	}

	public PresetGridLayoutPanel (GridLayout lm)
	{
		this(lm, true);
	}

	public PresetGridLayoutPanel (int rows, int cols, int hgap, int vgap, Document doc, boolean autoLayout)
	{
		this(new GridLayout(rows, cols, hgap, vgap), doc, autoLayout);
	}

	public PresetGridLayoutPanel (int rows, int cols, int hgap, int vgap, Element elem, boolean autoLayout)
	{
		this(rows, cols, hgap, vgap, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public PresetGridLayoutPanel (int rows, int cols, int hgap, int vgap, boolean autoLayout)
	{
		this(rows, cols, hgap, vgap, (Document) null, autoLayout);
	}

	public PresetGridLayoutPanel (int rows, int cols, int hgap, int vgap)
	{
		this(rows, cols, hgap, vgap, true);
	}

	public PresetGridLayoutPanel (int rows, int cols, Document doc, boolean autoLayout)
	{
		this(rows, cols, 0, 0, doc, autoLayout);
	}

	public PresetGridLayoutPanel (int rows, int cols, Element elem, boolean autoLayout)
	{
		this(rows, cols, 0, 0, elem, autoLayout);
	}

	public PresetGridLayoutPanel (int rows, int cols, boolean autoLayout)
	{
		this(rows, cols, (Document) null, autoLayout);
	}
	
	public PresetGridLayoutPanel (int rows, int cols)
	{
		this(rows, cols, true);
	}

	public PresetGridLayoutPanel (Document doc, boolean autoLayout)
	{
		this(1, 0, doc, autoLayout);
	}

	public PresetGridLayoutPanel (Document doc)
	{
		this(doc, true);
	}

	public PresetGridLayoutPanel (Element elem, boolean autoLayout)
	{
		this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
	}

	public PresetGridLayoutPanel (Element elem)
	{
		this(elem, true);
	}

	public PresetGridLayoutPanel (boolean autoLayout)
	{
		this((Document) null, autoLayout);
	}

	public PresetGridLayoutPanel ()
	{
		this(true);
	}
}
