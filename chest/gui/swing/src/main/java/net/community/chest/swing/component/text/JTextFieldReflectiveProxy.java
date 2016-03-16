package net.community.chest.swing.component.text;

import javax.swing.JTextField;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <F> The reflected {@link JTextField} type
 * @author Lyor G.
 * @since May 21, 2008 3:40:46 PM
 */
public class JTextFieldReflectiveProxy<F extends JTextField> extends JTextComponentReflectiveProxy<F> {
    public JTextFieldReflectiveProxy (Class<F> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JTextFieldReflectiveProxy (Class<F> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }
    /**
     * Default element name used for text fields
     */
    public static final String TEXTFIELD_ELEMNAME="textField";

    public static final JTextFieldReflectiveProxy<JTextField>    TXTFIELD=
                new JTextFieldReflectiveProxy<JTextField>(JTextField.class, true);
}
