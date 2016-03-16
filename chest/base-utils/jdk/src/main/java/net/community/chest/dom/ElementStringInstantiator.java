package net.community.chest.dom;

import org.w3c.dom.Element;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.Triplet;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.transform.XmlValueInstantiator;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Apr 24, 2008 4:00:00 PM
 */
public class ElementStringInstantiator extends BaseTypedValuesContainer<Element>
            implements ValueStringInstantiator<Element>,
                       XmlValueInstantiator<Element> {
    public ElementStringInstantiator () throws IllegalArgumentException
    {
        super(Element.class);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#convertInstance(java.lang.Object)
     */
    @Override
    public String convertInstance (final Element inst) throws Exception
    {
        if (null == inst)
            return null;

        return DOMUtils.toString(inst);
    }
    /*
     * @see net.community.chest.convert.ValueStringInstantiator#newInstance(java.lang.String)
     */
    @Override
    public Element newInstance (final String s) throws Exception
    {
        if ((null == s) || (s.length() <= 0))
            return null;

        final Triplet<? extends Element,?,?>    pe=DOMUtils.parseElementString(s);
        return (null == pe) ? null : pe.getV1();
    }
    /*
     * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
     */
    @Override
    public Element fromXml (Element elem) throws Exception
    {
        return elem;
    }

    public static final ElementStringInstantiator    DEFAULT=new ElementStringInstantiator();
}
