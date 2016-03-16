package net.community.chest.swing.component.text;

import javax.swing.text.JTextComponent;

import net.community.chest.swing.component.JComponentReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <TC> The reflected {@link JTextComponent} type
 * @author Lyor G.
 * @since May 21, 2008 3:38:33 PM
 */
public class JTextComponentReflectiveProxy<TC extends JTextComponent> extends JComponentReflectiveProxy<TC> {
    public JTextComponentReflectiveProxy (Class<TC> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JTextComponentReflectiveProxy (Class<TC> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final JTextComponentReflectiveProxy<JTextComponent>    TXTCOMP=
        new JTextComponentReflectiveProxy<JTextComponent>(JTextComponent.class, true);
}
