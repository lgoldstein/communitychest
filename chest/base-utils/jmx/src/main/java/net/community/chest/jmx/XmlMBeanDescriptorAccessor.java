package net.community.chest.jmx;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Provides the {@link XmlDynamicMBeanEmbedder} with access to the XML
 * definitions.</P>
 *
 * @author Lyor G.
 * @since Aug 15, 2007 7:21:04 AM
 */
public abstract class XmlMBeanDescriptorAccessor {
    protected XmlMBeanDescriptorAccessor ()
    {
        super();
    }
    /**
     * Invoked (indirectly) by {@link #getRootDescriptorElement(String)} in
     * order to load an XML definitions file.
     * @param clsName fully-qualified class name whose XML definitions file
     * is required - <B>Note:</B> can be the name of an <U>imported</U>
     * definition.
     * @return full path of the XML definitions file. <B>Note:</B> if reading
     * from a resources JAR/WAR/EAR/etc., the override this method (and throw
     * an exception if called), and also the {@link #openDescriptorFile(String)}
     * by calling {@link Class#getResourceAsStream(String)}
     * @throws IOException if cannot resolve the XML file to be used
     */
    public abstract String getDescriptorFilePath (final String clsName) throws IOException;
    /**
     * @param clsName fully-qualified class name whose XML definitions file
     * is required - <B>Note:</B> can be the name of an <U>imported</U>
     * definition.
     * @return open {@link InputStream} to be used to import the file - the
     * caller must {@link InputStream#close()} it once done with it.
     * @throws IOException if cannot open the XML definition stream
     */
    public InputStream openDescriptorFile (final String clsName) throws IOException
    {
        final String    filePath=getDescriptorFilePath(clsName);
        if ((null == filePath) || (filePath.length() <= 0))
            throw new FileNotFoundException(ClassUtil.getArgumentsExceptionLocation(getClass(), "openDescriptorFile", clsName) + " no file path returned");

        return new FileInputStream(filePath);
    }
    /**
     * @param clsName fully-qualified class name whose XML definitions file
     * is required - <B>Note:</B> can be the name of an <U>imported</U>
     * definition.
     * @return Container {@link Document} of the imported XML definitions
     * @throws IOException if cannot access the definitions
     * @throws SAXException if cannot create XML document
     * @throws ParserConfigurationException if bad XML document format
     */
    public Document loadDescriptorFile (final String clsName) throws IOException, SAXException, ParserConfigurationException
    {
        InputStream    in=null;
        try
        {

            if (null == (in=openDescriptorFile(clsName)))
                throw new FileNotFoundException(ClassUtil.getArgumentsExceptionLocation(getClass(), "loadDescriptorFile", clsName) + " no " + InputStream.class.getName() + " to extract XML from");

            final DocumentBuilderFactory    docFactory=DOMUtils.getDefaultDocumentsFactory();
            final DocumentBuilder            docBuilder=docFactory.newDocumentBuilder();
            return docBuilder.parse(in);
        }
        finally
        {
            FileUtil.closeAll(in);
        }
    }
    /**
     * @param clsName fully-qualified class name whose XML definitions file
     * is required - <B>Note:</B> can be the name of an <U>imported</U>
     * definition.
     * @return root XML {@link Element} under which the MBean definitions are
     * located.
     * @throws IOException if cannot access the definitions
     * @throws SAXException if cannot create XML document
     * @throws ParserConfigurationException if bad XML document format
     */
    public Element getRootDescriptorElement (final String clsName) throws IOException, SAXException, ParserConfigurationException
    {
        final Document    doc=loadDescriptorFile(clsName);
        if (null == doc)
            throw new FileNotFoundException(ClassUtil.getArgumentsExceptionLocation(getClass(), "getRootDescriptorElement", clsName) + " no " + Document.class.getName() + " instance");

        return doc.getDocumentElement();
    }
}
