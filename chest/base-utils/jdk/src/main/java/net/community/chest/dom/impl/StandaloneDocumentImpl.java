/*
 *
 */
package net.community.chest.dom.impl;

import net.community.chest.dom.DOMUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.DOMConfiguration;
import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;
import org.w3c.dom.Text;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 26, 2008 1:05:15 PM
 */
public class StandaloneDocumentImpl extends BaseNodeImpl<Document> implements Document {
    public StandaloneDocumentImpl ()
    {
        super(Document.class);
    }
    /*
     * @see org.w3c.dom.Document#adoptNode(org.w3c.dom.Node)
     */
    @Override
    public Node adoptNode (Node source) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "adoptNode(" + source + ") N/A");
    }
    /*
     * @see org.w3c.dom.Document#createAttributeNS(java.lang.String, java.lang.String)
     */
    @Override
    public Attr createAttributeNS (String namespaceURI, String qualifiedName) throws DOMException
    {
        return new StandaloneAttrImpl((Element) null, namespaceURI, qualifiedName, null);
    }
    /*
     * @see org.w3c.dom.Document#createAttribute(java.lang.String)
     */
    @Override
    public Attr createAttribute (String name) throws DOMException
    {
        return createAttributeNS(null, name);
    }
    /*
     * @see org.w3c.dom.Document#createCDATASection(java.lang.String)
     */
    @Override
    public CDATASection createCDATASection (String data) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "createCDATASection(" + data + ") N/A");
    }
    /*
     * @see org.w3c.dom.Document#createComment(java.lang.String)
     */
    @Override
    public Comment createComment (String data)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "createComment(" + data + ") N/A");
    }
    /*
     * @see org.w3c.dom.Document#createDocumentFragment()
     */
    @Override
    public DocumentFragment createDocumentFragment ()
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "createDocumentFragment() N/A");
    }
    /*
     * @see org.w3c.dom.Document#createElement(java.lang.String)
     */
    @Override
    public Element createElement (String tagName) throws DOMException
    {
        return new StandaloneElementImpl(this, tagName);
    }
    /*
     * @see org.w3c.dom.Document#createElementNS(java.lang.String, java.lang.String)
     */
    @Override
    public Element createElementNS (String namespaceURI, String qualifiedName) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "createElementNS(" + namespaceURI + "[" + qualifiedName + "]) N/A");
    }
    /*
     * @see org.w3c.dom.Document#createEntityReference(java.lang.String)
     */
    @Override
    public EntityReference createEntityReference (String name) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "createEntityReference(" + name + ") N/A");
    }
    /*
     * @see org.w3c.dom.Document#createProcessingInstruction(java.lang.String, java.lang.String)
     */
    @Override
    public ProcessingInstruction createProcessingInstruction (String target, String data) throws DOMException
    {
        return new StandaloneProcessingInstructionImpl(target, data);
    }
    /*
     * @see org.w3c.dom.Document#createTextNode(java.lang.String)
     */
    @Override
    public Text createTextNode (String data)
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "createTextNode(" + data + ") N/A");
    }

    private DocumentType    _docType;
    /*
     * @see org.w3c.dom.Document#getDoctype()
     */
    @Override
    public DocumentType getDoctype ()
    {
        return _docType;
    }

    public void setDoctype (DocumentType t)
    {
        _docType = t;
    }

    private Element    _docElem    /* =null */;
    /*
     * @see org.w3c.dom.Document#getDocumentElement()
     */
    @Override
    public Element getDocumentElement ()
    {
        return _docElem;
    }

    public void setDocumentElement (Element docElem)
    {
        _docElem = docElem;
    }
    /*
     * @see net.community.chest.dom.impl.BaseNodeImpl#appendChild(org.w3c.dom.Node)
     */
    @Override
    public synchronized Node appendChild (final Node newChild) throws DOMException
    {
        // define 1st added element as document element
        final int    nodeType=(null == newChild) ? Integer.MIN_VALUE : newChild.getNodeType();
        if (ELEMENT_NODE == nodeType)
        {
            final Element    elem=(Element) newChild, docElem=getDocumentElement();
            if (docElem != null)
                throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "appendChild(" + DOMUtils.toString(elem) + ") already have a document element: " + DOMUtils.toString(docElem));
            setDocumentElement(elem);
        }

        return super.appendChild(newChild);
    }
    // wrap an element as the document top element
    public StandaloneDocumentImpl (Element docElem)
    {
        this();

        _docElem = docElem;
    }
    /*
     * @see org.w3c.dom.Document#getDomConfig()
     */
    @Override
    public DOMConfiguration getDomConfig ()
    {
        return null;
    }
    /*
     * @see org.w3c.dom.Document#getElementById(java.lang.String)
     */
    @Override
    public Element getElementById (String elementId)
    {
        return DOMUtils.getElementById(getDocumentElement(), elementId, true);
    }
    /*
     * @see org.w3c.dom.Document#getElementsByTagName(java.lang.String)
     */
    @Override
    public NodeList getElementsByTagName (String tagname)
    {
        return DOMUtils.getElementsByTagName(getDocumentElement(), tagname);
    }
    /*
     * @see org.w3c.dom.Document#getElementsByTagNameNS(java.lang.String, java.lang.String)
     */
    @Override
    public NodeList getElementsByTagNameNS (String namespaceURI, String localName)
    {
        return DOMUtils.getElementsByTagNameNS(getDocumentElement(), namespaceURI, localName);
    }

    private DOMImplementation    _domImpl;
    /*
     * @see org.w3c.dom.Document#getImplementation()
     */
    @Override
    public DOMImplementation getImplementation ()
    {
        return _domImpl;
    }

    public void setImplementation (DOMImplementation impl)
    {
        _domImpl = impl;
    }
    /*
     * @see org.w3c.dom.Document#getInputEncoding()
     */
    @Override
    public String getInputEncoding ()
    {
        return "UTF-8";
    }

    private boolean    _strictErrCheck    /* =false */;
    /*
     * @see org.w3c.dom.Document#getStrictErrorChecking()
     */
    @Override
    public boolean getStrictErrorChecking ()
    {
        return _strictErrCheck;
    }

    public boolean isStrictErrorChecking ()
    {
        return getStrictErrorChecking();
    }
    /*
     * @see org.w3c.dom.Document#setStrictErrorChecking(boolean)
     */
    @Override
    public void setStrictErrorChecking (boolean strictErrorChecking)
    {
        _strictErrCheck = strictErrorChecking;
    }
    /*
     * @see org.w3c.dom.Document#getXmlEncoding()
     */
    @Override
    public String getXmlEncoding ()
    {
        return "UTF-8";
    }

    private boolean    _xmlStandalone    /* =false */;
    /*
     * @see org.w3c.dom.Document#getXmlStandalone()
     */
    @Override
    public boolean getXmlStandalone ()
    {
        return _xmlStandalone;
    }

    public boolean isXmlStandalone ()
    {
        return getXmlStandalone();
    }
    /*
     * @see org.w3c.dom.Document#setXmlStandalone(boolean)
     */
    @Override
    public void setXmlStandalone (boolean xmlStandalone) throws DOMException
    {
        _xmlStandalone = xmlStandalone;
    }

    private String    _xmlVersion="1.0";
    /*
     * @see org.w3c.dom.Document#getXmlVersion()
     */
    @Override
    public String getXmlVersion ()
    {
        return _xmlVersion;
    }
    /*
     * @see org.w3c.dom.Document#setXmlVersion(java.lang.String)
     */
    @Override
    public void setXmlVersion (String xmlVersion) throws DOMException
    {
        _xmlVersion = xmlVersion;
    }
    /*
     * @see org.w3c.dom.Document#importNode(org.w3c.dom.Node, boolean)
     */
    @Override
    public Node importNode (Node importedNode, boolean deep) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "importNode(" + importedNode + "[deep=" + deep + "]) N/A");
    }
    /*
     * @see org.w3c.dom.Document#normalizeDocument()
     */
    @Override
    public void normalizeDocument ()
    {
        // do nothing
    }
    /*
     * @see org.w3c.dom.Document#renameNode(org.w3c.dom.Node, java.lang.String, java.lang.String)
     */
    @Override
    public Node renameNode (Node n, String namespaceURI, String qualifiedName) throws DOMException
    {
        throw new DOMException(DOMException.NOT_SUPPORTED_ERR, "renameNode(" + n + "/" + namespaceURI + "[" + qualifiedName + "]) N/A");
    }

    private String    _docURI    /* =null */;
    /*
     * @see org.w3c.dom.Document#getDocumentURI()
     */
    @Override
    public String getDocumentURI ()
    {
        return _docURI;
    }
    /*
     * @see org.w3c.dom.Document#setDocumentURI(java.lang.String)
     */
    @Override
    public void setDocumentURI (String documentURI)
    {
        _docURI = documentURI;
    }
    /*
     * @see org.w3c.dom.Node#getNodeType()
     */
    @Override
    public final /* no cheating */  short getNodeType ()
    {
        return DOCUMENT_NODE;
    }
    /*
     * @see org.w3c.dom.Node#getOwnerDocument()
     */
    @Override
    public Document getOwnerDocument ()
    {
        return null;
    }
    /*
     * @see org.w3c.dom.Node#getParentNode()
     */
    @Override
    public Node getParentNode ()
    {
        return null;
    }
}
