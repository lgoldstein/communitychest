/*
 *
 */
package net.community.chest.swing.component.progress;

import javax.swing.JProgressBar;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <B> The reflected {@link JProgressBar} instance
 * @author Lyor G.
 * @since Dec 3, 2008 4:06:52 PM
 */
public class JProgressBarReflectiveProxy<B extends JProgressBar> extends JComponentReflectiveProxy<B> {
    public JProgressBarReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JProgressBarReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    // attributes of interest
    public static final String    ORIENTATION_ATTR="Orientation";
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (ORIENTATION_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) BarOrientationValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    public static final JProgressBarReflectiveProxy<JProgressBar>    PROGBAR=
        new JProgressBarReflectiveProxy<JProgressBar>(JProgressBar.class, true);
}
