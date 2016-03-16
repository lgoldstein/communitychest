/*
 *
 */
package net.community.chest.apache.log4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import net.community.chest.resources.XmlAnchoredResourceAccessor;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 25, 2008 3:16:11 PM
 */
public class Log4jAnchoredResourceEntityResolver implements EntityResolver {
    private XmlAnchoredResourceAccessor    _acc    /* =null */;
    public XmlAnchoredResourceAccessor getAnchoredAccessor ()
    {
        return _acc;
    }

    public void setAnchoredAccessor (XmlAnchoredResourceAccessor acc)
    {
        _acc = acc;
    }

    private String    _configFileName    /* =null */;
    public String getConfigFileName ()
    {
        return _configFileName;
    }

    public void setConfigFileName (String configFileName)
    {
        _configFileName = configFileName;
    }

    public Log4jAnchoredResourceEntityResolver (final XmlAnchoredResourceAccessor acc, final String configFileName)
    {
        _acc = acc;
        _configFileName = configFileName;
    }

    public Log4jAnchoredResourceEntityResolver ()
    {
        this(null, null);
    }
    /*
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity (final String publicId, final String systemId) throws SAXException, IOException
    {
        final int    idLen=(null == systemId) ? 0 : systemId.length();
        if ((idLen <= 0) || (!systemId.endsWith(".dtd")))
            return null;

        final int                            pPos=systemId.lastIndexOf('/');
        final String                        dtdFile=(pPos < 0) ? systemId : systemId.substring(pPos + 1);
        final XmlAnchoredResourceAccessor    acc=getAnchoredAccessor();
        final URL                            dtdURL=(null == acc) ? null : acc.getResource(dtdFile);
        final InputStream                    in;
        if (null == (in=(null == dtdURL) ? null : dtdURL.openStream()))
            return null;

        return new InputSource(in);
    }
}
