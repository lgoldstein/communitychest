/*
 *
 */
package net.community.chest.ui.components.input.panel.img;

import java.lang.reflect.Method;

import net.community.chest.ui.helpers.panel.HelperPanelReflectiveProxy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <P> Type of reflected {@link BgImagePanel}
 * @author Lyor G.
 * @since Mar 8, 2009 3:19:21 PM
 *
 */
public class BgImagePanelReflectiveProxy<P extends BgImagePanel> extends HelperPanelReflectiveProxy<P> {
    protected BgImagePanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public BgImagePanelReflectiveProxy (Class<P> objClass)
        throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public static final String    BGIMG_ATTR="bgImage";
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected P updateObjectAttribute (P src, String name, String value, Method setter) throws Exception
    {
        if (BGIMG_ATTR.equalsIgnoreCase(name))
            return updateObjectResourceAttribute(src, name, value, setter);

        return super.updateObjectAttribute(src, name, value, setter);
    }

    public static final BgImagePanelReflectiveProxy<BgImagePanel>    BGIMGPNL=
            new BgImagePanelReflectiveProxy<BgImagePanel>(BgImagePanel.class, true);
}
