/*
 *
 */
package net.community.chest.resources;

import java.net.URL;
import java.util.Map;

import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.util.map.ClassNameMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 21, 2008 12:35:54 PM
 */
public class BaseXmlAnchoredResourceAccessor extends BaseAnchoredResourceAccessor implements XmlAnchoredResourceAccessor {
    public BaseXmlAnchoredResourceAccessor ()
    {
        super();
    }
    /*
     * @see net.community.chest.resources.XmlAnchoredResourceAccessor#getDocument(java.lang.String)
     */
    @Override
    public Document getDocument (final String name) throws Exception
    {
        final String    effName=XmlDocumentRetriever.adjustResourceFileName(name);
        final URL        docURL=getResource(effName);
        return (null == docURL) ? null : DOMUtils.loadDocument(docURL);
    }
    /*
     * @see net.community.chest.resources.XmlAnchoredResourceAccessor#getDefaultDocument()
     */
    @Override
    public Document getDefaultDocument () throws Exception
    {
        return null;
    }

    private Map<String,Element>    _sectionsMap    /* =null */;
    /*
     * @see net.community.chest.resources.XmlAnchoredResourceAccessor#getSectionsMap()
     */
    @Override
    public synchronized Map<String,Element> getSectionsMap () throws RuntimeException
    {
        if (null == _sectionsMap)
        {
            try
            {
                _sectionsMap = DOMUtils.getSubsections(getDefaultDocument(), SECTION_ELEM_NAME, SECTION_NAME_ATTR);
            }
            catch(Exception e)
            {
                throw ExceptionUtil.toRuntimeException(e);
            }
        }

        return _sectionsMap;
    }

    public synchronized void setSectionsMap (Map<String,Element> m)
    {
        if (_sectionsMap != m)
            _sectionsMap = m;
    }
    /*
     * @see net.community.chest.resources.XmlAnchoredResourceAccessor#getSection(java.lang.String)
     */
    @Override
    public Element getSection (final String name) throws RuntimeException
    {
        if ((null == name) || (name.length() <= 0))
            return null;

        final Map<String,? extends Element>    sm=getSectionsMap();
        if ((null == sm) || (sm.size() <= 0))
            return null;

        return sm.get(name);
    }
    /**
     * Expected XML {@link Element#getTagName()} value for specific class
     * value(s)
     */
    public static final String    VALUE_ELEM_NAME="value";

    private ClassNameMap<Map<String,Element>>    _valsMap;
    public Map<String,Element> getClassValuesMap (final Class<?> c)
    {
        if (null == c)
            return null;

        synchronized(this)
        {
            if (null == _valsMap)
                _valsMap = new ClassNameMap<Map<String,Element>>(String.CASE_INSENSITIVE_ORDER);
        }

        Map<String,Element>    ret=null;
        synchronized(_valsMap)
        {
            if ((ret=_valsMap.get(c)) != null)
                return ret;
        }

        final Element    root=getSection(c.getSimpleName());
        if (null == (ret=DOMUtils.getSubsections(root, VALUE_ELEM_NAME, SECTION_NAME_ATTR)))
            return null;

        synchronized(_valsMap)
        {
            final Map<String,Element>    prev=_valsMap.put(c, ret);
            if (prev != null)
                return ret;    // debug breakpoint
        }

        return ret;
    }
}
