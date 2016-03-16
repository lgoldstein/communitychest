/*
 *
 */
package net.community.chest.apache.log4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import net.community.chest.dom.DOMUtils;
import net.community.chest.resources.XmlAnchoredResourceAccessor;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 24, 2008 9:04:17 AM
 */
public final class Log4jUtils {
    private Log4jUtils ()
    {
        // no instance
    }
    /**
     * Default XML configuration file name used
     */
    public static final String    DEFAULT_CONFIG_FILE_NAME="log4j.xml";
    /**
     * Loads the log4j initialization {@link Document} using the {@link #DEFAULT_CONFIG_FILE_NAME}
     * @param acc The {@link XmlAnchoredResourceAccessor} to be used to access
     * the configuration file and its DTD
     * @param configFileName The XML file name to be used
     * @return The loaded {@link Document} - null if configuration file resource not found.
     * @throws Exception If cannot access the configuration file or error
     * during loading
     */
    public static final Document loadConfigDocument (final XmlAnchoredResourceAccessor acc, final String configFileName) throws Exception
    {
        final URL    resURL=acc.getResource(configFileName);
        if (null == resURL)
            return null;

        final DocumentBuilderFactory    docFactory=DOMUtils.getDefaultDocumentsFactory();
        final DocumentBuilder            docBuilder;
        synchronized(docFactory)
        {
            docBuilder = docFactory.newDocumentBuilder();
        }
        docBuilder.setEntityResolver(new Log4jAnchoredResourceEntityResolver(acc,configFileName));

        InputStream    in=null;
        try
        {
            in = resURL.openStream();
            return docBuilder.parse(in);
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch(IOException ioe)
                {
                    // ignored
                }

                in = null;
            }
        }
    }
    /**
     * Loads the log4j initialization {@link Document} using the {@link #DEFAULT_CONFIG_FILE_NAME}
     * @param acc The {@link XmlAnchoredResourceAccessor} to be used to access
     * the configuration file and its DTD
     * @return The loaded {@link Document} - null if configuration file resource not found.
     * @throws Exception If cannot access the configuration file or error
     * during loading
     */
    public static final Document loadConfigDocument (final XmlAnchoredResourceAccessor acc) throws Exception
    {
        return loadConfigDocument(acc, DEFAULT_CONFIG_FILE_NAME);
    }
    /**
     * Initializes the log4j mechanism using the specified (XML) configuration
     * file
     * @param acc The {@link XmlAnchoredResourceAccessor} to be used to access
     * the configuration file
     * @param configFileName The configuration file name
     * @return The {@link Document} used to initialize the log4j mechanism - null
     * if configuration file resource not found.
     * @throws Exception If cannot access the configuration file or error
     * during initialization
     */
    public static final Document log4jInit (final XmlAnchoredResourceAccessor acc, final String configFileName) throws Exception
    {
        final Document    doc=loadConfigDocument(acc, configFileName);
        final Element    docElem=(null == doc) ? null : doc.getDocumentElement();
        if (docElem != null)
            DOMConfigurator.configure(docElem);

        return doc;
    }
    /**
     * @param acc Initializes the log4j mechanism using the {@link #DEFAULT_CONFIG_FILE_NAME}
     * configuration file
     * @return The {@link Document} used to initialize the log4j mechanism - null
     * if configuration file resource not found.
     * @throws Exception If cannot access the configuration file or error
     * during initialization
     * @see #log4jInit(XmlAnchoredResourceAccessor, String)
     */
    public static final Document log4jInit (final XmlAnchoredResourceAccessor acc) throws Exception
    {
        return log4jInit(acc, DEFAULT_CONFIG_FILE_NAME);
    }
}
