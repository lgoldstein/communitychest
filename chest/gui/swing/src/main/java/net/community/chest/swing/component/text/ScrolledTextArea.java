/*
 *
 */
package net.community.chest.swing.component.text;

import javax.swing.JTextArea;

import net.community.chest.awt.attributes.Editable;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.swing.component.scroll.HorizontalPolicy;
import net.community.chest.swing.component.scroll.ScrolledComponent;
import net.community.chest.swing.component.scroll.VerticalPolicy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <C> Type of scrolled JTextArea
 * @author Lyor G.
 * @since Apr 1, 2009 9:36:53 AM
 */
public class ScrolledTextArea<C extends JTextArea> extends ScrolledComponent<C>
        // NOTE !!! we do not declare FontControl, Tooltiped, Backgrounded, etc. since these are implemented by the scroll pane itself
        implements Textable, Editable    {
    /**
     *
     */
    private static final long serialVersionUID = -2192335648348034356L;
    public ScrolledTextArea (Class<C> vc, C view, VerticalPolicy vp, HorizontalPolicy hp)
    {
        super(vc, view, vp, hp);
    }

    public ScrolledTextArea (Class<C> vc, C view)
    {
        this(vc, view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
    }

    public ScrolledTextArea (Class<C> vc, VerticalPolicy vp, HorizontalPolicy hp)
    {
        this(vc, null, vp, hp);
    }

    public ScrolledTextArea (Class<C> vc)
    {
        this(vc, null);
    }

    @SuppressWarnings("unchecked")
    public ScrolledTextArea (C view, VerticalPolicy vp, HorizontalPolicy hp)
    {
        this((null == view) ? null : (Class<C>) view.getClass(), view, vp, hp);
    }

    public ScrolledTextArea (C view)
    {
        this(view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#getText()
     */
    @Override
    public String getText ()
    {
        final JTextArea    c=getAssignedValue();
        return (null == c) ? null : c.getText();
    }
    /*
     * @see net.community.chest.awt.attributes.Textable#setText(java.lang.String)
     */
    @Override
    public void setText (String t)
    {
        final JTextArea    c=getAssignedValue();
        if (c != null)
            c.setText(t);
    }
    /*
     * @see net.community.chest.awt.attributes.Editable#isEditable()
     */
    @Override
    public boolean isEditable ()
    {
        final JTextArea    c=getAssignedValue();
        return (c != null) && c.isEditable();
    }
    /*
     * @see net.community.chest.awt.attributes.Editable#setEditable(boolean)
     */
    @Override
    public void setEditable (boolean b)
    {
        final JTextArea    c=getAssignedValue();
        if (c != null)
            c.setEditable(b);
    }
}
