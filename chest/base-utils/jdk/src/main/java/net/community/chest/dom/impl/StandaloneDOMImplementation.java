/*
 *
 */
package net.community.chest.dom.impl;

import java.util.Comparator;
import java.util.TreeMap;

import org.w3c.dom.DOMException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Feb 12, 2009 10:52:05 AM
 */
public class StandaloneDOMImplementation extends TreeMap<String,Object> implements DOMImplementation {
    /**
     *
     */
    private static final long serialVersionUID = -834060221307514669L;
    public StandaloneDOMImplementation (Comparator<? super String> c)
    {
        super(c);
    }

    public StandaloneDOMImplementation ()
    {
        this(String.CASE_INSENSITIVE_ORDER);
    }

    public StandaloneDOMImplementation (StandaloneDOMImplementation impl)
    {
        this();

        if ((impl != null) && (impl.size() > 0))
            putAll(impl);
    }
    /*
     * @see org.w3c.dom.DOMImplementation#createDocument(java.lang.String, java.lang.String, org.w3c.dom.DocumentType)
     */
    @Override
    public Document createDocument (String namespaceURI, String qualifiedName, DocumentType doctype) throws DOMException
    {
        final StandaloneDocumentImpl    doc=new StandaloneDocumentImpl();
        doc.setBaseURI(namespaceURI);
        doc.setNodeName(qualifiedName);
        doc.setDoctype(doctype);
        return doc;
    }
    /*
     * @see org.w3c.dom.DOMImplementation#createDocumentType(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public DocumentType createDocumentType (String qualifiedName, String publicId, String systemId) throws DOMException
    {
        return null;
    }

    protected String getFeatureKey (String feature, String version)
    {
        return feature + "[" + version + "]";
    }

    public Object putFeature (String feature, String version, Object val)
    {
        final String    k=getFeatureKey(feature, version);
        if (null == val)
            return remove(k);
        else
            return put(k, val);
    }
    /*
     * @see org.w3c.dom.DOMImplementation#getFeature(java.lang.String, java.lang.String)
     */
    @Override
    public Object getFeature (String feature, String version)
    {
        final String    k=getFeatureKey(feature, version);
        return get(k);
    }
    /*
     * @see org.w3c.dom.DOMImplementation#hasFeature(java.lang.String, java.lang.String)
     */
    @Override
    public boolean hasFeature (String feature, String version)
    {
        return (getFeature(feature, version) != null);
    }
}
