package net.community.chest.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.io.FileUtil;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Copyright 2007 as per GPLv2
 *
 * Accesses an XML resource, parses it and converts it to a {@link Document}
 *
 * @author Lyor G.
 * @since Jul 19, 2007 10:42:33 AM
 */
public class XmlDocumentRetriever extends ResourceDataRetriever<Document> {
    private DocumentBuilderFactory    _docFactory;
    // NOTE: creates one if necessary
    public DocumentBuilderFactory getDocumentsFatory ()
    {
        if (null == _docFactory)
            _docFactory = DOMUtils.getDefaultDocumentsFactory();
        return _docFactory;
    }

    public void setDocumentsFatory (DocumentBuilderFactory docFactory)
    {
        _docFactory = docFactory;
    }

    protected DocumentBuilder getDocumentBuilder () throws ParserConfigurationException
    {
        final DocumentBuilderFactory    docFactory=getDocumentsFatory();
        synchronized(docFactory)
        {
            return docFactory.newDocumentBuilder();
        }
    }
    /**
     * The ".xml" file suffix
     */
    public static final String    XML_SUFFIX=".xml";
    /**
     * The ".xsl" file suffix
     */
    public static final String    XSLT_SUFFIX=".xsl";
    /**
     * Checks if name already ends in ".xml" (case insensitive). If not,
     * then adds the ".xml" suffix
     * @param name original name
     * @return ".xml" suffixed name - May be original name - if null/empty or
     * already contains the necessary suffix
     */
    public static final String adjustResourceFileName (final String name)
    {
        return FileUtil.adjustFileName(name, XML_SUFFIX);
    }
    /*
     * @see net.community.chest.resources.ResourceDataRetriever#loadResourceData(java.io.InputStream)
     */
    @Override
    public Document loadResourceData (final InputStream in) throws IOException
    {
        try
        {
            final DocumentBuilder    docBuilder=getDocumentBuilder();
            final Document            doc=docBuilder.parse(in);
            if (null == doc)    // should not happen
                throw new NullPointerException("No " + Document.class.getSimpleName() + " instance parsed for URL=" + getResourceURL());

            return doc;
        }
        catch(Exception e)
        {
            if (e instanceof IOException)
                throw (IOException) e;

            throw new StreamCorruptedException(getArgumentsExceptionLocation("loadResourceData", getResourceURL()) + " " + e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public XmlDocumentRetriever (final Class<?> anchor, final Locale lcl, final String name) throws IOException
    {
        super(Document.class, anchor, lcl, adjustResourceFileName(name));
    }

    public XmlDocumentRetriever (final Class<?> anchor, final String name) throws IOException
    {
        super(Document.class, anchor, adjustResourceFileName(name));
    }

    public static final Map.Entry<URL,Document> loadAnchoredLocalizedDocument (
            final Class<?> c, final AnchoredResourceAccessor anchor, final Locale l, final String docName)
                throws IOException, ParserConfigurationException, SAXException
    {
         final URL    resURL=lookupAnchoredLocalizedResource(c, anchor, l, docName);
         if (null == resURL)
             return null;

         final Document    doc=DOMUtils.loadDocument(resURL);
         return new MapEntryImpl<URL,Document>(resURL,doc);
    }

    public static final Map.Entry<URL,Document> loadAnchoredLocalizedDocument (
            final Class<?> c, final Class<?> anchor, final Locale l, final String docName)
                throws IOException, ParserConfigurationException, SAXException
    {
        return loadAnchoredLocalizedDocument(c, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l, docName);
    }
    /**
     * Attempts to find an XML {@link Document} associated with the provided
     * {@link Class} by searching for an XML resource that has the same name
     * as the <U>simple</U> class name and which is located in the <U>same</U>
     * package as the class. Failing that it tries to locate such a resource
     * for the super-class, and so on.
     * @param c The {@link Class} instance
     * @param baseClass The &quot;maximum&quot; hierarchy to climb in search
     * for the resource (inclusive) - if <code>null</code> then stop only at
     * {@link Object}.</BR>
     * <P><B>Note:</B> if the base class is not part of the hierarchy then the
     * lookup will stop at the original class and go no further.</P>
     * @param anchor The {@link Class#getResource(String)} to use
     * @param lcl The {@link Locale} to look for - <code>null</code> is same
     * as {@link Locale#getDefault()}
     * @param resSuffix The suffix of the file containing the document (with
     * or without the dot) - usually it should be {@link #XML_SUFFIX}
     * @return The a {@link java.util.Map.Entry} representing the located resource (null
     * if none found) where key=the {@link URL} used to load the resource,
     * value=the loaded {@link Document}
     * @throws IOException If cannot access the document data
     * @throws ParserConfigurationException If bad XML parser initialization
     * @throws SAXException If bad XML format
     */
    public static final Map.Entry<URL,Document> loadDerivedClassDocument (
            final Class<?> c, final Class<?> baseClass, final AnchoredResourceAccessor anchor, final Locale lcl, final String resSuffix)
                throws IOException, ParserConfigurationException, SAXException
    {
        final URL    resURL=lookupDerivedClassResource(c, baseClass, anchor, lcl, resSuffix);
         if (null == resURL)
             return null;

         final Document    doc=DOMUtils.loadDocument(resURL);
         return new MapEntryImpl<URL,Document>(resURL,doc);
    }

    public static final Map.Entry<URL,Document> loadDerivedClassDocument (
            final Class<?> c, final Class<?> baseClass, final Class<?> anchor, final Locale lcl, final String resSuffix)
                throws IOException, ParserConfigurationException, SAXException
    {
        return loadDerivedClassDocument(c, baseClass, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), lcl, resSuffix);
    }
    /**
     * @param doc The XML {@link Document} - may be null
     * @param ra The {@link ResourcesAnchor} annotation value - may NOT be null
     * @return The {@link Map} of &quot;sections&quot; according to the
     * annotation values
     * @throws IllegalArgumentException - see {@link DOMUtils#getSubsections(Document, String, String)}
     * @throws IllegalStateException - see {@link DOMUtils#getSubsections(Document, String, String)}
     * @throws NoSuchElementException - see {@link DOMUtils#getSubsections(Document, String, String)}
     */
    public static final Map<String,Element> loadResourcesAnchorSections (final Document doc, final ResourcesAnchor ra)
        throws IllegalArgumentException, IllegalStateException, NoSuchElementException
    {
        if (null == doc)
            return null;

        final String    tagName=(null == ra) ? null : ra.elementName(),
                        attrName=(null == ra) ? null : ra.attributeName();
        return DOMUtils.getSubsections(doc, tagName, attrName);
    }

    public static final Map.Entry<URL,Document> loadResourcesAnchorDocument (
            final Class<?> c, final Class<?> baseClass, final AnchoredResourceAccessor anchor, final Locale l, final ResourcesAnchor ra)
                throws IOException, ParserConfigurationException, SAXException
    {
        final String    docVal=(null == ra) ? null : ra.documentName(),
                        docName=XmlDocumentRetriever.adjustResourceFileName(docVal);
        if ((null == docName) || (docName.length() <= 0))
            return null;

        final URL    docURL=ResourceDataRetriever.lookupAnchoredClassResource(c, baseClass, anchor, l, docName);
        if (null == docURL)
            return null;

        final Document    doc=DOMUtils.loadDocument(docURL);
        if (null == doc)
            return null;

        return new MapEntryImpl<URL,Document>(docURL, doc);
    }

    public static final Map.Entry<URL,Document> loadResourcesAnchorDocument (
            final Class<?> c, final Class<?> baseClass, final Class<?> anchor, final Locale l, final ResourcesAnchor ra)
                throws IOException, ParserConfigurationException, SAXException
    {
        return loadResourcesAnchorDocument(c, baseClass, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l, ra);
    }

    public static final Map<String,Element> loadResourcesAnchorSections (
            final Class<?> c, final Class<?> baseClass, final AnchoredResourceAccessor anchor, final Locale l, final ResourcesAnchor ra)
                throws IOException, ParserConfigurationException, SAXException
    {
        final Map.Entry<? extends URL,? extends Document>    resPair=
                loadResourcesAnchorDocument(c, baseClass, anchor, l, ra);
        final Document                                        doc=
                (null == resPair) ? null : resPair.getValue();
        return loadResourcesAnchorSections(doc, ra);
    }

    public static final Map<String,Element> loadResourcesAnchorSections (
            final Class<?> c, final Class<?> baseClass, final Class<?> anchor, final Locale l, final ResourcesAnchor ra)
                throws IOException, ParserConfigurationException, SAXException
    {
        return loadResourcesAnchorSections(c, baseClass, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l, ra);
    }
    public static final Element loadResourcesAnchorSection (
            final Class<?> c, final Class<?> baseClass, final Class<?> anchor, final Locale l, final ResourcesAnchor ra)
                throws IOException, ParserConfigurationException, SAXException
    {
        if (null == c)
            return null;
        final String    secName=(null == ra) ? null : ra.sectionName(),
                        effName=     // may be null/empty for anonymous classes
            ((null == secName) || (secName.length() <= 0)) ? c.getSimpleName() : secName;
        if ((null == effName) || (effName.length() <= 0))
            return null;

        final Map<String,? extends Element>    sMap=loadResourcesAnchorSections(c, baseClass, anchor, l, ra);
        if ((null == sMap) || (sMap.size() <= 0))
            return null;

        return sMap.get(effName);
    }
}
