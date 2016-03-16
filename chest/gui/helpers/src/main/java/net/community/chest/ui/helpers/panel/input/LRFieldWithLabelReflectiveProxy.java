/*
 *
 */
package net.community.chest.ui.helpers.panel.input;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.label.JLabelReflectiveProxy;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> The reflected {@link LRFieldWithLabelPanel}
 * @author Lyor G.
 * @since Aug 25, 2008 1:31:56 PM
 */
public class LRFieldWithLabelReflectiveProxy<P extends LRFieldWithLabelPanel> extends InputTextPanelReflectiveProxy<P> {
    public LRFieldWithLabelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected LRFieldWithLabelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public boolean isLabelElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, JLabelReflectiveProxy.LABEL_ELEMNAME);
    }

    public XmlProxyConvertible<?> getLabelConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JLabelReflectiveProxy.LABEL;
    }

    public JLabel setLabel (final P src, final Element elem) throws Exception
    {
        final XmlProxyConvertible<?>    proxy=getLabelConverter(elem);
        final JLabel                    l=src.getLabel(true);
        @SuppressWarnings("unchecked")
        final Object                    o=
            ((XmlProxyConvertible<Object>) proxy).fromXml(l, elem);
        if (o != l)
            throw new IllegalStateException("setLabel(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");

        return l;
    }

    private static final List<String>    DEFERRED_ATTRS=Arrays.asList(
            Titled.ATTR_NAME,
            Iconable.ATTR_NAME
        );
    /*
     * @see net.community.chest.dom.proxy.AbstractReflectiveProxy#handleDeferredAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.AccessibleObject)
     */
    @Override
    protected P handleDeferredAttribute (P src, String aName, String aValue, Method setter) throws Exception
    {
        if (CollectionsUtils.containsElement(DEFERRED_ATTRS, aName, String.CASE_INSENSITIVE_ORDER))
            return super.updateObjectAttribute(src, aName, aValue, setter);
        else
            return super.handleDeferredAttribute(src, aName, aValue, setter);
    }
    /*
     * @see net.community.chest.dom.proxy.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected P updateObjectAttribute (final P src, final String aName, final String aValue, final Method setter) throws Exception
    {
        if (CollectionsUtils.containsElement(DEFERRED_ATTRS, aName, String.CASE_INSENSITIVE_ORDER))
            return src;    // defer these attributes till after the children set

        return super.updateObjectAttribute(src, aName, aValue, setter);
    }
    /*
     * @see net.community.chest.ui.helpers.panel.input.InputTextPanelReflectiveProxy#fromXmlChild(net.community.chest.ui.helpers.panel.input.AbstractInputTextPanel, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isLabelElement(elem, tagName))
        {
            setLabel(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }
    /*
     * @see net.community.chest.ui.helpers.panel.input.InputTextPanelReflectiveProxy#fromXmlChildren(net.community.chest.ui.helpers.panel.input.AbstractInputTextPanel, java.util.Collection, java.util.Map)
     */
    @Override
    public P fromXmlChildren (P src, Collection<? extends Element> cl, Map<String,AttributeHandlingResult<Method>> resMap) throws Exception
    {
        // handle any deferred attributes
        final P    sVal=super.fromXmlChildren(src, cl, resMap);
        return fromXmlAttributes(sVal, resMap, true, DEFERRED_ATTRS);
    }

    public static final LRFieldWithLabelReflectiveProxy<LRFieldWithLabelPanel>    LRLBLFLDPNL=
        new LRFieldWithLabelReflectiveProxy<LRFieldWithLabelPanel>(LRFieldWithLabelPanel.class, true);
}
