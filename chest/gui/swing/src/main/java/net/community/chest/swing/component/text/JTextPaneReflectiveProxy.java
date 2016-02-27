/*
 * 
 */
package net.community.chest.swing.component.text;

import java.util.NoSuchElementException;

import javax.swing.JTextPane;
import javax.swing.text.Document;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.swing.text.DocumentReflectiveProxy;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <P> Type of {@link JTextPane} being reflected
 * @author Lyor G.
 * @since Jul 29, 2009 2:01:30 PM
 */
public class JTextPaneReflectiveProxy<P extends JTextPane> extends JEditorPaneReflectiveProxy<P> {
	protected JTextPaneReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
		throws IllegalArgumentException, IllegalStateException
	{
		super(objClass, registerAsDefault);
	}

	public JTextPaneReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
	{
		this(objClass, false);
	}

	public static final String	DOCUMENT_ELEMNAME=Document.class.getSimpleName().toLowerCase();
	public boolean isDocumentElement (final Element elem, final String tagName)
	{
		return isMatchingElement(elem, tagName, DOCUMENT_ELEMNAME);
	}

	protected XmlValueInstantiator<? extends Document>	getDocumentInstantiator (final Element elem) throws Exception
	{
		final String									type=elem.getAttribute(CLASS_ATTR);
		final XmlValueInstantiator<? extends Document>	proxy=DocumentReflectiveProxy.getDocumentReflectiveProxy(type);
		if (proxy == null)
			throw new NoSuchElementException("No proxy found for document element=" + DOMUtils.toString(elem));
		return proxy;
	}

	protected Document setDocument (P src, Element elem) throws Exception
	{
		final XmlValueInstantiator<? extends Document>	proxy=getDocumentInstantiator(elem);
		final Document									doc=(proxy == null) ? null : proxy.fromXml(elem);
		if (doc != null)
			src.setDocument(doc);
		return doc;
	}
	/*
	 * @see net.community.chest.swing.component.JComponentReflectiveProxy#fromXmlChild(javax.swing.JComponent, org.w3c.dom.Element)
	 */
	@Override
	public P fromXmlChild (P src, Element elem) throws Exception
	{
		final String	tagName=elem.getTagName();
		if (isDocumentElement(elem, tagName))
		{
			setDocument(src, elem);
			return src;
		}

		return super.fromXmlChild(src, elem);
	}

	public static final JTextPaneReflectiveProxy<JTextPane>	TXTPANE=
		new JTextPaneReflectiveProxy<JTextPane>(JTextPane.class, true);
}
