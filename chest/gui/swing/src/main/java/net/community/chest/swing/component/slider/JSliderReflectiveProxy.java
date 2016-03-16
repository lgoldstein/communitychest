/*
 *
 */
package net.community.chest.swing.component.slider;

import javax.swing.JSlider;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.swing.SwingConstantsValueStringInstantiator;
import net.community.chest.swing.component.JComponentReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <S> The reflected {@link JSlider}
 * @author Lyor G.
 * @since Oct 15, 2008 4:33:52 PM
 */
public class JSliderReflectiveProxy<S extends JSlider> extends JComponentReflectiveProxy<S> {
    public JSliderReflectiveProxy (Class<S> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JSliderReflectiveProxy (Class<S> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    // special attribute
    public static final String    ORIENTATION_ATTR="orientation";
    /*
     * @see net.community.chest.awt.dom.UIReflectiveAttributesProxy#resolveAttributeInstantiator(java.lang.String, java.lang.Class)
     */
    @SuppressWarnings("unchecked")
    @Override
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (String name, Class<C> type) throws Exception
    {
        if (ORIENTATION_ATTR.equalsIgnoreCase(name))
            return (ValueStringInstantiator<C>) SwingConstantsValueStringInstantiator.DEFAULT;

        return super.resolveAttributeInstantiator(name, type);
    }

    public static final JSliderReflectiveProxy<JSlider>    SLIDER=
            new JSliderReflectiveProxy<JSlider>(JSlider.class, true);
}
