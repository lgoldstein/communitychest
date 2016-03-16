/*
 *
 */
package net.community.chest.awt.layout.dom;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.LayoutManager;
import java.util.Map;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.awt.layout.border.BorderLayoutXmlConstraintValueInstantiator;
import net.community.chest.awt.layout.gridbag.GridBagLayoutXmlConstraintValueInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.AbstractReflectiveProxy;
import net.community.chest.lang.TypedValuesContainer;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ProcessingInstruction;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <L> The type of {@link LayoutManager} for which this instantiator
 * generates constraints
 * @param <V> The type of constraint being generated
 * @author Lyor G.
 * @since Jan 8, 2009 9:03:14 AM
 */
public class AbstractLayoutConstraintXmlValueInstantiator<L extends LayoutManager,V>
        extends BaseTypedValuesContainer<V>
        implements LayoutConstraintXmlValueInstantiator<L,V> {

    private final Class<L>    _lmClass;
    /*
     * @see net.community.chest.awt.layout.dom.LayoutConstraintXmlValueInstantiator#getLayoutClass()
     */
    @Override
    public final Class<L> getLayoutClass ()
    {
        return _lmClass;
    }

    protected AbstractLayoutConstraintXmlValueInstantiator (Class<L> lmc, Class<V> vc) throws IllegalArgumentException
    {
        super(vc);

        if (null == (_lmClass=lmc))
            throw new IllegalArgumentException("No " + LayoutManager.class.getSimpleName() + " class specified");
    }

    public AbstractReflectiveProxy<V,?> getConstraintProxy (V src) throws Exception
    {
        throw new UnsupportedOperationException("getConstraintProxy(" + getLayoutClass().getSimpleName() + "[" + getValuesClass().getSimpleName() + "]){" + src + "} N/A");
    }

    public V fromXmlAttributes (final V src, final NamedNodeMap attrs) throws Exception
    {
        final AbstractReflectiveProxy<V,?>    proxy=getConstraintProxy(src);
        if (proxy != null)
        {
            final Map.Entry<V,?>    rp=proxy.fromXmlAttributes(src, attrs);
            return (null == rp) ? null : rp.getKey();
        }

        return src;
    }

    public V fromXmlAttributes (final NamedNodeMap attrs) throws Exception
    {
        return fromXmlAttributes(getValuesClass().newInstance(), attrs);
    }

    public NamedNodeMap getConstraintNodeAttributes (final Node org)
    {
        Node    n=org;
        if (null == n)
            return null;

        final short    nt=n.getNodeType();
        switch(nt)
        {
            case Node.PROCESSING_INSTRUCTION_NODE    :
                n = DOMUtils.toElement((ProcessingInstruction) n);
                break;
            case Node.ELEMENT_NODE                    :
                break;

            default                                    :
                throw new UnsupportedOperationException("getConstraintNodeAttributes(" + n + ") unknown node type: " + nt);
        }

        return n.getAttributes();
    }
    /*
     * @see net.community.chest.awt.layout.dom.LayoutConstraintXmlValueInstantiator#fromConstraintNode(org.w3c.dom.Node)
     */
    @Override
    public V fromConstraintNode (final Node n) throws Exception
    {
        return fromXmlAttributes(getConstraintNodeAttributes(n));
    }
    /*
     * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
     */
    @Override
    public V fromXml (Element elem) throws Exception
    {
        return fromConstraintNode(elem);
    }

    public static final String    CONSTRAINT_NODE_NAME="constraint";
    /*
     * @see net.community.chest.awt.layout.dom.LayoutConstraintXmlValueInstantiator#isConstraintNode(org.w3c.dom.Node)
     */
    @Override
    public boolean isConstraintNode (Node n)
    {
        if (null == n)
            return false;

        final String    pn;
        final short        nt=n.getNodeType();
        switch(nt)
        {
            case Node.PROCESSING_INSTRUCTION_NODE    :
                {
                    final ProcessingInstruction    pi=(ProcessingInstruction) n;
                    pn = pi.getTarget();
                }
                break;

            case Node.ELEMENT_NODE                    :
                {
                    final Element    elem=(Element) n;
                    pn = elem.getTagName();
                }
                break;

            default    :
                return false;
        }

        if (CONSTRAINT_NODE_NAME.equalsIgnoreCase(pn))
            return true;

        return false;
    }
    /*
     * @see net.community.chest.awt.layout.dom.LayoutConstraintXmlValueInstantiator#getContainerConstraintNode(org.w3c.dom.Node)
     */
    @Override
    public Node getContainerConstraintNode (Node parent)
    {
        if (isConstraintNode(parent))
            return parent;

        final NodeList    nl=(null == parent) ? null : parent.getChildNodes();
        final int        numNodes=(null == nl) ? 0 : nl.getLength();
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
        {
            final Node    n=nl.item(nIndex);
            if (isConstraintNode(n))
                return n;
        }

        return null;
    }
    /*
     * @see net.community.chest.awt.layout.dom.LayoutConstraintXmlValueInstantiator#fromXmlContainer(org.w3c.dom.Element)
     */
    @Override
    public V fromXmlContainer (Element elem) throws Exception
    {
        final Node    n=getContainerConstraintNode(elem);
        return fromConstraintNode(n);
    }

    public static final LayoutConstraintXmlValueInstantiator<? extends LayoutManager,?> getLayoutConstraintInstantiator (final Class<?> lmc)
    {
        if (null == lmc)
            return null;
        else if (BorderLayout.class.isAssignableFrom(lmc))
            return BorderLayoutXmlConstraintValueInstantiator.BLCONST;
        else if (GridBagLayout.class.isAssignableFrom(lmc))
            return GridBagLayoutXmlConstraintValueInstantiator.GBCONST;
        else
            return null;
    }

    public static final LayoutConstraintXmlValueInstantiator<? extends LayoutManager,?> getLayoutConstraintInstantiator (final LayoutManager lm)
    {
        return (null == lm) ? null : getLayoutConstraintInstantiator(lm.getClass());
    }

    public static final LayoutConstraintXmlValueInstantiator<? extends LayoutManager,?> getLayoutConstraintInstantiator (final Container c)
    {
        return (null == c) ? null : getLayoutConstraintInstantiator(c.getLayout());
    }

    public static final Class<?> getLayoutConstraintClass (final Element elem)
    {
        final Object    vsi=AbstractLayoutManagerReflectiveProxy.getLayoutConverter(elem);
        if (vsi instanceof TypedValuesContainer<?>)
            return ((TypedValuesContainer<?>) vsi).getValuesClass();
        return null;
    }

    public static final LayoutConstraintXmlValueInstantiator<? extends LayoutManager,?> getLayoutConstraintInstantiator (final Element elem)
    {
        return (null == elem) ? null : getLayoutConstraintInstantiator(getLayoutConstraintClass(elem));
    }
}
