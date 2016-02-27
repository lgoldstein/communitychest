/*
 * 
 */
package net.community.chest.swing.component.text;

import java.io.IOException;
import java.io.StreamCorruptedException;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;
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
 * @since Feb 22, 2009 11:02:54 AM
 */
public class BaseTextArea extends JTextArea
			implements XmlConvertible<BaseTextArea>, Appendable,
					   Textable, Enabled, FontControl, Editable,
					   Tooltiped, Backgrounded, Foregrounded {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7056125227268279127L;
	public BaseTextArea (Document doc, String text, int rows, int columns)
	{
		super(doc, text, rows, columns);
	}

	public BaseTextArea (String text)
	{
        this(null, text, 0, 0);
	}

	public BaseTextArea ()
	{
		this((String) null);
	}

	public BaseTextArea (String text, int rows, int columns)
	{
        this(null, text, rows, columns);
	}

	public BaseTextArea (int rows, int columns)
	{
        this(null, rows, columns);
	}

	public BaseTextArea (Document doc)
	{
        this(doc, null, 0, 0);
	}

	protected XmlProxyConvertible<?> getAreaConverter (Element elem)
	{
		return (null == elem) ? null : JTextAreaReflectiveProxy.TXTAREA;
	}
	/*
	 * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
	 */
	@Override
	@CoVariantReturn
	public BaseTextArea fromXml (Element elem) throws Exception
	{
		final XmlProxyConvertible<?>	proxy=getAreaConverter(elem);
		@SuppressWarnings("unchecked")
		final Object					o=
			((XmlProxyConvertible<Object>) proxy).fromXml(this, elem);
		if (o != this)
			throw new IllegalStateException("fromXml(" + DOMUtils.toString(elem) + ") mismatched re-constructed instance");

		return this;
	}

	public BaseTextArea (Element elem) throws Exception
	{
		final Object	o=fromXml(elem);
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
	public BaseTextArea append (final CharSequence csq) throws IOException
	{
		final String	str=(null == csq) ? null : csq.toString();
		final Document	doc=((null == str) || (str.length() <= 0)) ? null : getDocument();
        if (doc != null)
        {
        	final int	docPos=doc.getLength();
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
	public BaseTextArea append (char c) throws IOException
	{
		return append((CharSequence) String.valueOf(c));
	}
	/*
	 * @see java.lang.Appendable#append(java.lang.CharSequence, int, int)
	 */
	@Override
	@CoVariantReturn
	public BaseTextArea append (CharSequence csq, int start, int end) throws IOException
	{
		return append((start < end) ? csq.subSequence(start, end) : null);
	}
}
