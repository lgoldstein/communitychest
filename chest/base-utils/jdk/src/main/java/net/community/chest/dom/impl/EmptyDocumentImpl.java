/*
 *
 */
package net.community.chest.dom.impl;

import net.community.chest.dom.DOMUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 25, 2009 10:43:17 AM
 */
public class EmptyDocumentImpl extends StandaloneDocumentImpl {
    public EmptyDocumentImpl ()
    {
        super();
    }
    /*
     * @see net.community.chest.dom.StandaloneDocumentImpl#createElement(java.lang.String)
     */
    @Override
    public Element createElement (String tagName) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "createElement(" + tagName + ") N/A");
    }
    /*
     * @see net.community.chest.dom.StandaloneDocumentImpl#setDocumentElement(org.w3c.dom.Element)
     */
    @Override
    public void setDocumentElement (Element docElem)
    {
        if (docElem != null)
            throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "setDocumentElement(" + DOMUtils.toString(docElem) + ") N/A");
    }

    public static final EmptyDocumentImpl    EMPTY_DOC=new EmptyDocumentImpl();
}
