/*
 *
 */
package net.community.chest.ui.components.panel;

import java.awt.Component;
import java.lang.reflect.Method;

import javax.swing.JLabel;

import org.w3c.dom.Element;

import net.community.chest.awt.attributes.Iconable;
import net.community.chest.awt.attributes.Titled;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.swing.component.label.JLabelReflectiveProxy;
import net.community.chest.ui.helpers.panel.HelperPanelReflectiveProxy;

/**
 * <P>Copyright GPLv2</P>
 *
 * @param <C> Type of {@link Component} being labeled
 * @param <P> Type of {@link LRLabeledComponent} being reflected
 * @author Lyor G.
 * @since Mar 12, 2009 12:40:05 PM
 */
public class LRLabeledComponentReflectiveProxy<C extends Component, P extends LRLabeledComponent<C>>
        extends HelperPanelReflectiveProxy<P> {
    protected LRLabeledComponentReflectiveProxy (Class<P> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public LRLabeledComponentReflectiveProxy (Class<P> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
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
    /*
     * @see net.community.chest.dom.transform.ReflectiveAttributesProxy#updateObjectAttribute(java.lang.Object, java.lang.String, java.lang.String, java.lang.reflect.Method)
     */
    @Override
    protected P updateObjectAttribute (P src, String name, String value, Method setter) throws Exception
    {
        if (Titled.ATTR_NAME.equals(name)
         || Iconable.ATTR_NAME.equalsIgnoreCase(name))
            return src;    // defer these attributes till after the children set

        return super.updateObjectAttribute(src, name, value, setter);
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

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static final LRLabeledComponentReflectiveProxy    LRLBLCOMP=
        new LRLabeledComponentReflectiveProxy(LRLabeledComponent.class, true);
}
