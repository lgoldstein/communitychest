/*
 *
 */
package net.community.chest.javaagent.dumper;

import java.util.Collection;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.javaagent.dumper.filter.ClassFilter;
import net.community.chest.javaagent.dumper.filter.IncludeExcludeFilter;
import net.community.chest.javaagent.dumper.filter.XmlConvertibleClassFilter;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 14, 2011 9:17:23 AM
 */
public class Configuration implements XmlConvertible<Configuration> {
    public Configuration ()
    {
        super();
    }

    public Configuration (Document doc) throws Exception
    {
        final Object    config=fromXml(doc.getDocumentElement());
        if (config != this)
            throw new IllegalStateException("Mismatched reconstructed instances");
    }

    private ClassFilter    _filter;
    public ClassFilter getFilter ()
    {
        return _filter;
    }

    public void setFilter (ClassFilter filter)
    {
        _filter = filter;
    }

    public static final String    CONFIG_ELEMENT="configuration";
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        final Element    root=doc.createElement(CONFIG_ELEMENT);
        {
            final ClassFilter    filter=getFilter();
            if (filter instanceof XmlConvertibleClassFilter)
                root.appendChild(((XmlConvertibleClassFilter) filter).toXml(doc));
        }

        return root;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public Configuration fromXml (Element root) throws Exception
    {
        final Collection<? extends Element>    elems=DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
        if ((elems == null) || elems.isEmpty())
            return this;

        for (final Element elem : elems)
        {
            final String    tagName=elem.getTagName();
            if (IncludeExcludeFilter.FILTERS_ELEMENT.equals(tagName))
                setFilter(elem);
            else
                throw new DOMException(DOMException.NAMESPACE_ERR, "Unknown element: " + DOMUtils.toString(elem));
        }

        return this;
    }

    protected XmlConvertibleClassFilter setFilter (Element elem) throws Exception
    {
        final XmlConvertibleClassFilter    filter=new IncludeExcludeFilter(elem);
        setFilter(filter);
        return filter;
    }
}
