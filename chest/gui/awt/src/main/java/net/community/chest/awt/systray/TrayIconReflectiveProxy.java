/*
 *
 */
package net.community.chest.awt.systray;

import java.awt.PopupMenu;
import java.awt.TrayIcon;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import net.community.chest.awt.dom.UIReflectiveAttributesProxy;
import net.community.chest.awt.menu.MenuReflectiveProxy;
import net.community.chest.awt.menu.PopupMenuReflectiveProxy;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <I> The reflected {@link TrayIcon} instance
 * @author Lyor G.
 * @since Dec 2, 2008 1:28:08 PM
 */
public class TrayIconReflectiveProxy<I extends TrayIcon> extends UIReflectiveAttributesProxy<I> {
    public TrayIconReflectiveProxy (Class<I> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected TrayIconReflectiveProxy (Class<I> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public static final String    IMAGE_ATTR="image";
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected I updateObjectAttribute (I src, String name, String value, Method setter) throws Exception
    {
        if (IMAGE_ATTR.equalsIgnoreCase(name))
            return updateObjectResourceAttribute(src, name, value, setter);

        return super.updateObjectAttribute(src, name, value, setter);
    }

    public boolean isPopupMenuElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, MenuReflectiveProxy.MENU_ELEMNAME);
    }

    public XmlProxyConvertible<? extends PopupMenu> getPopupMenuConverter (Element elem) throws Exception
    {
        return (null == elem) ? null : PopupMenuReflectiveProxy.POPUP;
    }

    public PopupMenu setPopupMenu (I src, Element elem) throws Exception
    {
        final XmlProxyConvertible<? extends PopupMenu>    proxy=getPopupMenuConverter(elem);
        @SuppressWarnings("unchecked")
        final PopupMenu                                    org=src.getPopupMenu(),
                                                        mnu=
            (null == org) ? proxy.fromXml(elem) : ((XmlProxyConvertible<PopupMenu>) proxy).fromXml(org, elem);
        if (mnu != null)
        {
            if (null == org)
                src.setPopupMenu(mnu);
            else if (org != mnu)
                throw new IllegalStateException("setPopupMenu(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
        }

        return mnu;
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public I fromXmlChild (I src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isPopupMenuElement(elem, tagName))
        {
            setPopupMenu(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final TrayIconReflectiveProxy<TrayIcon>    TRAYICON=
            new TrayIconReflectiveProxy<TrayIcon>(TrayIcon.class, true) {
                private Constructor<?>    _ctor    /* =null */;
                public synchronized Constructor<?> getDefaultConstructor () throws Exception
                {
                    if (null == _ctor)
                    {
                        final Class<TrayIcon>    vc=getValuesClass();
                        final Constructor<?>[]    ca=vc.getDeclaredConstructors();
                        if ((null == ca) || (ca.length <= 0))
                            throw new NoSuchMethodException("No constructors");

                        for (final Constructor<?> c : ca)
                        {
                            final Class<?>[]    params=c.getParameterTypes();
                            if ((params != null) && (params.length > 0))
                                continue;

                            if (null == _ctor)
                                _ctor = c;
                            else
                                throw new IllegalStateException("Multiple default constructors");
                        }

                        if (null == _ctor)
                            throw new NoSuchMethodException("No default constructor");

                        if (!_ctor.isAccessible())
                            _ctor.setAccessible(true);
                    }

                    return _ctor;
                }
                /*
                 * @see net.community.chest.dom.transform.AbstractReflectiveProxy#createInstance(org.w3c.dom.Element)
                 */
                @Override
                public TrayIcon createInstance (Element elem) throws Exception
                {
                    final Constructor<?>    c=getDefaultConstructor();
                    final Object            inst=(null == c) ? null : c.newInstance();
                    return (null == inst) ? null : getValuesClass().cast(inst);
                }
        };
}
