/*
 *
 */
package net.community.chest.ui.helpers.button;

import org.w3c.dom.Element;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.swing.component.button.JButtonReflectiveProxy;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <V> Type of value carried by the {@link TypedButton}
 * @param <B> Type of {@link TypedButton} being reflected
 * @author Lyor G.
 * @since Jan 19, 2009 1:10:44 PM
 */
public class TypedButtonReflectiveProxy<V,B extends TypedButton<V>> extends JButtonReflectiveProxy<B> {
    protected TypedButtonReflectiveProxy (Class<B> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public TypedButtonReflectiveProxy (Class<B> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final TypedButtonReflectiveProxy    TYPBTN=
        new TypedButtonReflectiveProxy(TypedButton.class, true) {
            /*
             * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXml(org.w3c.dom.Element)
             */
            @Override
            @CoVariantReturn
            public TypedButton fromXml (Element elem) throws Exception
            {
                final Class<?>    vc=loadElementClass(elem);
                if (null == vc)
                    throw new ClassNotFoundException("fromXml(" + DOMUtils.toString(elem) + ") no class found");

                return new TypedButton(vc, elem, true /* auto-layout */);
            }
        };
}
