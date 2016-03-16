/*
 *
 */
package net.community.chest.swing.component.text;

import java.io.IOException;
import java.io.StreamCorruptedException;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

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
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jul 29, 2009 1:12:33 PM
 */
public class BaseTextPane extends JTextPane
        implements XmlConvertible<BaseTextPane>, Appendable,
                      Textable, Enabled, FontControl, Editable,
                      Tooltiped, Backgrounded, Foregrounded {
    /**
     *
     */
    private static final long serialVersionUID = -5959330344540704976L;
    public BaseTextPane ()
    {
        super();
    }

    public BaseTextPane (StyledDocument doc)
    {
        super(doc);
    }

    protected XmlProxyConvertible<?> getPaneConverter (Element elem)
    {
        return (null == elem) ? null : JTextPaneReflectiveProxy.TXTPANE;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public BaseTextPane fromXml (Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getPaneConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
        if (o != this)
            throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");

        return this;
    }


    public BaseTextPane (Element elem) throws Exception
    {
        final Object    o=fromXml(elem);
        if (o != this)
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (org.w3c.dom.Document doc) throws Exception
    {
        throw new UnsupportedOperationException("toXml() N/A");
    }
    /*
     * @see java.lang.Appendable#append(java.lang.CharSequence)
     */
    @Override
    @CoVariantReturn
    public BaseTextPane append (final CharSequence csq) throws IOException
    {
        final String    str=(null == csq) ? null : csq.toString();
        final Document    doc=((null == str) || (str.length() <= 0)) ? null : getDocument();
        if (doc != null)
        {
            final int    docPos=doc.getLength();
            try
            {
                doc.insertString(docPos, str, null);
            }
            catch (BadLocationException e)
            {
                throw new StreamCorruptedException("append(" + str + ")[pos=" + docPos + "]" + e.getClass().getName() + ": " + e.getMessage());
            }
        }

        return this;
    }
    /*
     * @see java.lang.Appendable#append(char)
     */
    @Override
    @CoVariantReturn
    public BaseTextPane append (char c) throws IOException
    {
        return append(String.valueOf(c));
    }
    /*
     * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
     */
    @Override
    @CoVariantReturn
    public BaseTextPane append (CharSequence csq, int start, int end) throws IOException
    {
        return append((start < end) ? csq.subSequence(start, end) : null);
    }
}
