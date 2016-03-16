/*
 *
 */
package net.community.chest.swing.component.button;

import javax.swing.JToggleButton;


/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link JToggleButton} instance
 * @author Lyor G.
 * @since Aug 21, 2008 3:42:30 PM
 */
public class JToggleButtonReflectiveProxy<B extends JToggleButton> extends AbstractButtonReflectiveProxy<B> {
    public JToggleButtonReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JToggleButtonReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final JToggleButtonReflectiveProxy<JToggleButton>    TOGGLEBTN=
            new JToggleButtonReflectiveProxy<JToggleButton>(JToggleButton.class, true);
}
