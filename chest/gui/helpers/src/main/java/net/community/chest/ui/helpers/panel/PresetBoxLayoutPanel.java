/*
 *
 */
package net.community.chest.ui.helpers.panel;

import javax.swing.BoxLayout;

import net.community.chest.awt.layout.BoxLayoutAxis;
import net.community.chest.dom.impl.StandaloneDocumentImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Mar 12, 2009 8:41:05 AM
 *
 */
public class PresetBoxLayoutPanel extends PresetLayoutPanel<BoxLayout> {
    /**
     *
     */
    private static final long serialVersionUID = 1082105246191868187L;
    /**
     * Default {@link BoxLayout} axis if none specified. <B>Note:</B>
     * {@link #setLayout(java.awt.LayoutManager)} can always be called
     * at a later stage with a different {@link BoxLayout} (as long as
     * {@link #layoutComponent()} has not been called)
     */
    public static final BoxLayoutAxis    DEFAULT_AXIS=BoxLayoutAxis.X;
    public PresetBoxLayoutPanel (BoxLayoutAxis axis, Document doc, boolean autoLayout)
    {
        super(BoxLayout.class, null, doc, false /* delay auto-layout till BoxLayout set */);

        setLayout(new BoxLayout(this, ((null == axis) ? DEFAULT_AXIS : axis).getAxis()));

        if (autoLayout)
            layoutComponent();
    }

    public PresetBoxLayoutPanel (BoxLayoutAxis axis, boolean autoLayout)
    {
        this(axis, (Document) null, autoLayout);
    }

    public PresetBoxLayoutPanel (BoxLayoutAxis axis, Document doc)
    {
        this(axis, doc, true);
    }

    public PresetBoxLayoutPanel (BoxLayoutAxis axis, Element elem, boolean autoLayout)
    {
        this(axis, (null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    public PresetBoxLayoutPanel (BoxLayoutAxis axis, Element elem)
    {
        this(axis, elem, true);
    }

    public PresetBoxLayoutPanel (BoxLayoutAxis axis)
    {
        this(axis, true);
    }

    public PresetBoxLayoutPanel (boolean autoLayout)
    {
        this(DEFAULT_AXIS, autoLayout);
    }

    public PresetBoxLayoutPanel ()
    {
        this(true);
    }

    public PresetBoxLayoutPanel (Document doc, boolean autoLayout)
    {
        this(DEFAULT_AXIS, doc, autoLayout);
    }

    public PresetBoxLayoutPanel (Document doc)
    {
        this(doc, true);
    }

    public PresetBoxLayoutPanel (Element elem, boolean autoLayout)
    {
        this((null == elem) ? null : new StandaloneDocumentImpl(elem), autoLayout);
    }

    public PresetBoxLayoutPanel (Element elem)
    {
        this(elem, true);
    }
}
