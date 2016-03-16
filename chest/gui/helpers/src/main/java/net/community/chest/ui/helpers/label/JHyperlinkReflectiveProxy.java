/*
 *
 */
package net.community.chest.ui.helpers.label;

import net.community.chest.awt.CursorTypeValueStringInstantiator;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.swing.component.label.JLabelReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <H> The reflected {@link JHyperlink} instance
 * @author Lyor G.
 * @since Nov 12, 2008 1:17:01 PM
 */
public class JHyperlinkReflectiveProxy<H extends JHyperlink> extends JLabelReflectiveProxy<H> {
    public JHyperlinkReflectiveProxy (Class<H> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JHyperlinkReflectiveProxy (Class<H> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /*
     * @see net.community.chest.swing.component.label.JLabelReflectiveProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if ("HoverCursorType".equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) CursorTypeValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    public static final JHyperlinkReflectiveProxy<JHyperlink>    HYPERLINK=
                new JHyperlinkReflectiveProxy<JHyperlink>(JHyperlink.class, true);
}
