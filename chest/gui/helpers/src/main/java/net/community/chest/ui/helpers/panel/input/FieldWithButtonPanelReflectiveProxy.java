/*
 *
 */
package net.community.chest.ui.helpers.panel.input;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;

import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.button.AbstractButtonReflectiveProxy;
import net.community.chest.swing.component.button.JButtonReflectiveProxy;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <P> Type of reflected {@link AbstractFieldWithButtonPanel}
 * @author Lyor G.
 * @since Aug 20, 2008 2:51:40 PM
 */
public class FieldWithButtonPanelReflectiveProxy<P extends AbstractFieldWithButtonPanel>
        extends InputTextPanelReflectiveProxy<P> {
    public FieldWithButtonPanelReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected FieldWithButtonPanelReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public boolean isButtonElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, AbstractButtonReflectiveProxy.BUTTON_ELEMNAME);
    }

    public XmlProxyConvertible<? extends JButton> getButtonConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : JButtonReflectiveProxy.BUTTON;
    }

    public JButton setButton (final P src, final Element elem) throws Exception
    {
        final XmlProxyConvertible<? extends JButton>    proxy=getButtonConverter(elem);
        final JButton                                    orgBtn=src.getButton(), b;
        if (null == orgBtn)
        {
            if ((b=proxy.fromXml(elem)) != null)
                src.setButton(b);
        }
        else
        {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Object    o=((XmlProxyConvertible) proxy).fromXml(orgBtn, elem);
            if (o != orgBtn)
                throw new IllegalStateException("setButton(" + DOMUtils.toString(elem) + ") mismatched reconstructed instances");

            b = orgBtn;
        }

        return b;
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
     * @see net.community.chest.swing.component.panel.input.AbstractInputTextPanelReflectiveProxy#fromXmlChild(net.community.chest.swing.component.panel.input.AbstractInputTextPanel, org.w3c.dom.Element)
     */
    @Override
    public P fromXmlChild (P src, Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isButtonElement(elem, tagName))
        {
            setButton(src, elem);
            return src;
        }

        return super.fromXmlChild(src, elem);
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChildren(java.lang.Object, java.util.Collection, java.util.Map)
     */
    @Override
    public P fromXmlChildren (P src, Collection<? extends Element> cl, Map<String,AttributeHandlingResult<Method>> resMap) throws Exception
    {
        // handle any deferred attributes
        final P    sVal=super.fromXmlChildren(src, cl, resMap);
        return fromXmlAttributes(sVal, resMap, true, DEFERRED_ATTRS);
    }

    public static final FieldWithButtonPanelReflectiveProxy<AbstractFieldWithButtonPanel>    FLDWITHBTNPNL=
        new FieldWithButtonPanelReflectiveProxy<AbstractFieldWithButtonPanel>(AbstractFieldWithButtonPanel.class, true);
}
