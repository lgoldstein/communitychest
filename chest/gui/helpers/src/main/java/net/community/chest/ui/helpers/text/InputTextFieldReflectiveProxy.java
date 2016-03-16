/*
 *
 */
package net.community.chest.ui.helpers.text;

import net.community.chest.swing.component.text.JTextFieldReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link InputTextField} type
 * @author Lyor G.
 * @since Jan 12, 2009 12:22:26 PM
 */
public class InputTextFieldReflectiveProxy<F extends InputTextField> extends JTextFieldReflectiveProxy<F> {
    // need it because of get/setOk/ErrFieldColor
    protected InputTextFieldReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public InputTextFieldReflectiveProxy (Class<F> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final InputTextFieldReflectiveProxy<InputTextField>    INPTXT=
        new InputTextFieldReflectiveProxy<InputTextField>(InputTextField.class, true);
}
