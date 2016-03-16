/*
 *
 */
package net.community.chest.swing.applet;

import javax.swing.JApplet;
import javax.swing.JMenuBar;

import net.community.chest.awt.dom.proxy.AppletReflectiveProxy;
import net.community.chest.awt.menu.MenuReflectiveProxy;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.menu.JMenuBarReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <A> The reflected {@link JApplet} class
 * @author Lyor G.
 * @since Feb 16, 2009 12:54:56 PM
 */
public class JAppletReflectiveProxy<A extends JApplet> extends AppletReflectiveProxy<A> {
    protected JAppletReflectiveProxy (Class<A> objClass, boolean registerAsDefault)
            throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public JAppletReflectiveProxy (Class<A> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    public boolean isMenuBarElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, MenuReflectiveProxy.MENU_ELEMNAME);
    }

    public XmlProxyConvertible<? extends JMenuBar> getMenuBarConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JMenuBarReflectiveProxy.BAR;
    }

    public JMenuBar setMenuBar (final A src, final Element elem) throws Exception
    {
        final XmlProxyConvertible<? extends JMenuBar>    inst=getMenuBarConverter(elem);
        @SuppressWarnings("unchecked")
        final JMenuBar                                    org=src.getJMenuBar(),
                                                        bar=
            (null == org) ? inst.fromXml(elem) : ((XmlProxyConvertible<JMenuBar>) inst).fromXml(org, elem);
        if (bar != null)
        {
            if (null == org)
                src.setJMenuBar(bar);
            else if (org != bar)
                throw new IllegalStateException("setMenuBar(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");
        }

        return bar;
    }
    /*
     * @see net.community.chest.awt.dom.proxy.ContainerReflectiveProxy#fromXmlChild(java.awt.Container, org.w3c.dom.Element)
     */
    @Override
    public A fromXmlChild (A src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isMenuBarElement(elem, tagName))
        {
            setMenuBar(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }

    public static final JAppletReflectiveProxy<JApplet>    APPLET=
            new JAppletReflectiveProxy<JApplet>(JApplet.class, true);
}
