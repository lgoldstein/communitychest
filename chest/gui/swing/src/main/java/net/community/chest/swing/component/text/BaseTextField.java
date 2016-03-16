/*
 *
 */
package net.community.chest.swing.component.text;

import javax.swing.JTextField;
import javax.swing.text.Document;

import net.community.chest.CoVariantReturn;
import net.community.chest.awt.attributes.Backgrounded;
import net.community.chest.awt.attributes.Editable;
import net.community.chest.awt.attributes.Enabled;
import net.community.chest.awt.attributes.FontControl;
import net.community.chest.awt.attributes.Foregrounded;
import net.community.chest.awt.attributes.Textable;
import net.community.chest.awt.attributes.Tooltiped;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlConvertible;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 19, 2009 2:00:38 PM
 */
public class BaseTextField extends JTextField
        implements XmlConvertible<BaseTextField>,
                   Textable, Enabled, FontControl, Editable,
                   Tooltiped, Backgrounded, Foregrounded {
    /**
     *
     */
    private static final long serialVersionUID = 1193429791912300476L;

    public BaseTextField (Document doc, String text, int columns)
    {
        super(doc, text, columns);
    }

    public BaseTextField (String text, int columns)
    {
        this(null, text, columns);
    }

    public BaseTextField (int columns)
    {
        this(null, columns);
    }

    public BaseTextField (String text)
    {
        this(text, 0);
    }

    public BaseTextField ()
    {
        this(0);
    }

    protected XmlProxyConvertible<?> getFieldConverter (Element elem)
    {
        return (null == elem) ? null : JTextFieldReflectiveProxy.TXTFIELD;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public BaseTextField fromXml (Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getFieldConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");

        return this;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (org.w3c.dom.Document doc) throws Exception
    {
        throw new UnsupportedOperationException("toXml() N/A");
    }

    public BaseTextField (Element elem) throws Exception
    {
        final Object    o=fromXml(elem);
        if (o != this)
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");
    }
}
