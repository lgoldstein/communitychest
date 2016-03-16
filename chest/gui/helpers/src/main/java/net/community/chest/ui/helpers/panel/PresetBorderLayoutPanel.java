/*
 *
 */
package net.community.chest.ui.helpers.panel;

import java.awt.BorderLayout;
import java.awt.Component;

import net.community.chest.awt.layout.border.BorderLayoutPosition;
import net.community.chest.dom.impl.StandaloneDocumentImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>A panel with a {@link BorderLayout}</P>
 *
 * @author Lyor G.
 * @since Aug 25, 2008 12:37:11 PM
 */
public class PresetBorderLayoutPanel extends PresetLayoutPanel<BorderLayout> {
    /**
     *
     */
    private static final long serialVersionUID = 3349116602810805587L;

    public PresetBorderLayoutPanel (final BorderLayout l, final Document doc, final boolean autoLayout)
    {
        super(BorderLayout.class, l, doc, autoLayout);
    }

    public PresetBorderLayoutPanel (final BorderLayout l, final Element elem, final boolean autoLayout)
    {
        this(l, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    public PresetBorderLayoutPanel (final BorderLayout l, final boolean autoLayout)
    {
        this(l, (Document) null, autoLayout);
    }

    public PresetBorderLayoutPanel (final BorderLayout l)
    {
        this(l, true);
    }

    public PresetBorderLayoutPanel (final int hgap, final int vgap, final Document doc, final boolean autoLayout)
    {
        this(new BorderLayout(hgap, vgap), doc, autoLayout);
    }

    public PresetBorderLayoutPanel (final int hgap, final int vgap, final Element elem, final boolean autoLayout)
    {
        this(hgap, vgap, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    public PresetBorderLayoutPanel (final int hgap, final int vgap, final boolean autoLayout)
    {
        this(hgap, vgap, (Document) null, autoLayout);
    }

    public PresetBorderLayoutPanel (final int hgap, final int vgap)
    {
        this(hgap, vgap, true);
    }

    public PresetBorderLayoutPanel (final boolean autoLayout)
    {
        this(0, 0, autoLayout);
    }

    public PresetBorderLayoutPanel (Element elem, boolean autoLayout)
    {
        this(new BorderLayout(0,0), elem, autoLayout);
    }

    public PresetBorderLayoutPanel (Element elem)
    {
        this(elem, true);
    }

    public PresetBorderLayoutPanel (Document doc, boolean autoLayout)
    {
        this(new BorderLayout(0,0), doc, autoLayout);
    }

    public PresetBorderLayoutPanel (Document doc)
    {
        this(doc, true);
    }

    public PresetBorderLayoutPanel ()
    {
        this(true);
    }

    public void add (Component c, BorderLayoutPosition pos)
    {
        super.add(c, pos.getPosition());
    }
}
