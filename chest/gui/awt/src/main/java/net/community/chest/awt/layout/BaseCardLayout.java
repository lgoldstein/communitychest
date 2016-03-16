/*
 *
 */
package net.community.chest.awt.layout;

import java.awt.CardLayout;

import net.community.chest.awt.layout.dom.CardLayoutReflectiveProxy;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 20, 2008 1:44:01 PM
 */
public class BaseCardLayout extends CardLayout implements XmlConvertible<BaseCardLayout> {
    /**
     *
     */
    private static final long serialVersionUID = 8528027752703640680L;
    public BaseCardLayout ()
    {
        super();
    }

    public BaseCardLayout (int hgap, int vgap)
    {
        super(hgap, vgap);
    }

    public XmlProxyConvertible<?> getLayoutConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : CardLayoutReflectiveProxy.CARD;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public BaseCardLayout fromXml (final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getLayoutConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "fromXml") + " mismatched initialization instances");

        return this;
    }

    public BaseCardLayout (final Element elem) throws Exception
    {
        if (fromXml(elem) != this)
            throw new IllegalStateException("Mismatched constructed instances");
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement toXml
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }
}
