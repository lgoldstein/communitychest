/*
 *
 */
package net.community.chest.swing.component.text;

import javax.swing.JTextArea;

import net.community.chest.swing.component.scroll.HorizontalPolicy;
import net.community.chest.swing.component.scroll.VerticalPolicy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 1, 2009 9:24:19 AM
 */
public class DefaultTextAreaScroll extends ScrolledTextArea<JTextArea> {
    /**
     *
     */
    private static final long serialVersionUID = 6684052185128314461L;

    public DefaultTextAreaScroll (JTextArea view, VerticalPolicy vp, HorizontalPolicy hp)
    {
        super(JTextArea.class, view, vp, hp);
    }

    public DefaultTextAreaScroll (VerticalPolicy vp, HorizontalPolicy hp)
    {
        this(null, vp, hp);
    }

    public DefaultTextAreaScroll (JTextArea view)
    {
        this(view, VerticalPolicy.BYNEED, HorizontalPolicy.BYNEED);
    }

    public DefaultTextAreaScroll ()
    {
        this(null);
    }
}
