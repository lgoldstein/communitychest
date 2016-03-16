/*
 *
 */
package net.community.chest.swing.component.panel;

import javax.swing.JPanel;

import net.community.chest.swing.component.JComponentReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> Type of reflected {@link JPanel}
 * @author Lyor G.
 * @since Aug 14, 2008 2:34:55 PM
 */
public class JPanelReflectiveProxy<P extends JPanel> extends JComponentReflectiveProxy<P> {
    public JPanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JPanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final JPanelReflectiveProxy<JPanel>    PANEL=
                new JPanelReflectiveProxy<JPanel>(JPanel.class, true);
}
