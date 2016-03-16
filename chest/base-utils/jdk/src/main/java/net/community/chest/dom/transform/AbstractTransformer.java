/*
 *
 */
package net.community.chest.dom.transform;

import java.io.InputStream;
import java.io.Reader;
import java.net.URL;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.community.chest.dom.DOMUtils;
import net.community.chest.xml.transform.BaseTransformer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Provides some default implementations for some of the {@link javax.xml.transform.Transformer}
 * abstract methods with DOM orientation</P>
 * @author Lyor G.
 * @since May 7, 2009 2:24:18 PM
 */
public abstract class AbstractTransformer extends BaseTransformer {
    protected AbstractTransformer ()
    {
        super();
    }

    protected Document getDocument (final InputStream src) throws TransformerException
    {
        if (null == src)
            return null;

        try
        {
            return DOMUtils.loadDocument(src);
        }
        catch(Exception e)
        {
            if (e instanceof TransformerException)
                throw (TransformerException) e;
            throw new TransformerException(e);
        }
    }

    protected Document getDocument (final Reader src) throws TransformerException
    {
        if (null == src)
            return null;

        throw new TransformerException("getDocument(" + Reader.class.getSimpleName() + ") N/A");
    }

    protected Document getDocument (final String src) throws TransformerException
    {
        if (null == src)
            return null;

        try
        {
            return DOMUtils.loadDocument(new URL(src));
        }
        catch(Exception e)
        {
            if (e instanceof TransformerException)
                throw (TransformerException) e;
            throw new TransformerException(e);
        }
    }

    protected Document getDocument (final StreamSource src) throws TransformerException
    {
        if (null == src)
            return null;

        final InputStream    in=src.getInputStream();
        if (in != null)
            return getDocument(in);

        final Reader    r=src.getReader();
        if (r != null)
            return getDocument(r);

        final String    pid=src.getPublicId();
        if ((pid != null) && (pid.length() > 0))
            return getDocument(pid);

        final String    sid=src.getSystemId();
        if ((sid != null) && (sid.length() > 0))
            return getDocument(sid);

        throw new TransformerException("getDocument(" + StreamSource.class.getSimpleName() + ") no input");
    }

    public Document getDocument (final Source src) throws TransformerException
    {
        if (null == src)
            return null;
        else if (src instanceof DOMSource)
        {
            final Node    n=((DOMSource) src).getNode();
            if ((null == n) || (n.getNodeType() != Node.DOCUMENT_NODE))
                throw new TransformerException("Non-" + Document.class.getSimpleName() + " source node");
            return (Document) n;
        }
        else if (src instanceof StreamSource)
            return getDocument((StreamSource) src);
        else
            throw new TransformerException("Unknown source type: " + src.getClass().getName());
    }

    public Document getDocument (final StreamResult res) throws TransformerException
    {
        if (null == res)
            return null;

        throw new TransformerException("getDocument(" + StreamResult.class.getSimpleName() + ") N/A");
    }

    public Document getDocument (final Result res) throws TransformerException
    {
        if (null == res)
            return null;
        else if (res instanceof DOMResult)
        {
            final Node    n=((DOMResult) res).getNode();
            if ((null == n) || (n.getNodeType() != Node.DOCUMENT_NODE))
                throw new TransformerException("Non-" + Document.class.getSimpleName() + " source node");
            return (Document) n;
        }
        else if (res instanceof StreamResult)
            return getDocument((StreamResult) res);
        else
            throw new TransformerException("Unknown result type: " + res.getClass().getName());
    }
    /**
     * Output property name to be used for {@link #setOutputProperty(String, String)}
     * call in order to determine whether the written {@link Element}-s attributes
     * are written in lexicographical order (default=yes/true)
     */
    public static final String    SORT_ELEMENT_ATTRIBUTES="x-sort-element-attrs";
    public boolean isSortedElementAttributes ()
    {
        return isTrueProperty(getOutputProperty(SORT_ELEMENT_ATTRIBUTES));
    }

    public void setSortedElementAttributes (boolean val)
    {
        setOutputProperty(SORT_ELEMENT_ATTRIBUTES, String.valueOf(val));
    }
    /**
     * Output property name to be used for {@link #setOutputProperty(String, String)}
     * call in order to determine whether the comments are also output
     */
    public static final String    SHOW_COMMENTS="x-show-comments";
    public boolean isShowComments ()
    {
        return isTrueProperty(getOutputProperty(SHOW_COMMENTS));
    }

    public void setShowComments (boolean val)
    {
        setOutputProperty(SHOW_COMMENTS, String.valueOf(val));
    }
}
