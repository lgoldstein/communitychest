package net.community.chest.swing.component;

import java.util.Map;
import java.util.NoSuchElementException;

import javax.accessibility.AccessibleContext;
import javax.swing.JComponent;
import javax.swing.border.Border;

import net.community.chest.awt.border.BorderReflectiveProxy;
import net.community.chest.awt.dom.proxy.AccessibleContextReflectiveProxy;
import net.community.chest.awt.dom.proxy.ContainerReflectiveProxy;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Element;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @param <JC> The reflected {@link JComponent} type
 * @author Lyor G.
 * @since Mar 20, 2008 8:37:10 AM
 */
public class JComponentReflectiveProxy<JC extends JComponent> extends ContainerReflectiveProxy<JC> {
    public JComponentReflectiveProxy (Class<JC> objClass) throws IllegalArgumentException
    {
        this(objClass, false);
    }

    protected JComponentReflectiveProxy (Class<JC> objClass, boolean registerAsDefault)
        throws IllegalArgumentException, IllegalStateException
    {
        super(objClass, registerAsDefault);
    }

    public XmlProxyConvertible<?> getAccessibleContextConverter (final Element elem) throws Exception
    {
        return (null == elem) ? null : AccessibleContextReflectiveProxy.ACCCTX;
    }

    public static final String    ACCESSIBLE_CONTEXT_ELEMNAME="accessibleContext";
    public boolean isAccessibleContextElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, ACCESSIBLE_CONTEXT_ELEMNAME);
    }
    /**
     * Called by {@link #fromXmlChild(JComponent, Element)} when an {@link AccessibleContext}
     * XML {@link Element} is encountered in order to re-construct it
     * @param src The {@link javax.swing.JMenu} instance to be updated
     * @param elem XML element to use for reconstructing the data
     * @return Updated {@link AccessibleContext} instance - default calls
     * {@link #getAccessibleContextConverter(Element)} and then invokes its
     * {@link XmlProxyConvertible#fromXml(Object, Element)} method
     * @throws Exception if cannot update the context
     */
    public AccessibleContext setAccessibleContext (final JC src, final Element elem) throws Exception
    {
        final AccessibleContext            ctxOrg=src.getAccessibleContext();
        final XmlProxyConvertible<?>    proxy=getAccessibleContextConverter(elem);
        @SuppressWarnings("unchecked")
        final Object                    ctxUpd=
            ((XmlProxyConvertible<Object>) proxy).fromXml(ctxOrg, elem);
        if (ctxOrg != ctxUpd)
            throw new IllegalStateException(ClassUtil.getArgumentsExceptionLocation(getClass(), "updateAccessibleContext", DOMUtils.toString(elem)) + " mismatched re-constructed instances");

        return ctxOrg;
    }

    public boolean isBorderElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, BorderReflectiveProxy.BORDER_ELEM_NAME);
    }

    public XmlValueInstantiator<? extends Border> getBorderProxy (final Element elem) throws Exception
    {
        if (null == elem)
            return null;
        // uses the class attribute to determine type of instantiator to return
        final XmlValueInstantiator<? extends Border>    proxy=BorderReflectiveProxy.getBorderInstantiator(elem);
        if (null == proxy)
            throw new NoSuchElementException("getBorderProxy(" + DOMUtils.toString(elem) + ") no proxy available");

        return proxy;
    }

    public Border setBorder (final JC src, final Element elem) throws Exception
    {
        final XmlValueInstantiator<? extends Border>    proxy=getBorderProxy(elem);
        final Border                                    b=(null == proxy) ? null : proxy.fromXml(elem);
        if (b != null)
            src.setBorder(b);

        return b;
    }

    public static final String    PROPERTY_ELEM_NAME="clientProperty";
    public boolean isPropertyElement (final Element elem, final String tagName)
    {
        return isMatchingElement(elem, tagName, PROPERTY_ELEM_NAME);
    }

    public Map.Entry<Object,Object> setClientProperty (final JC src, final Element elem) throws Exception
    {
        final String    name=elem.getAttribute(NAME_ATTR),
                        nameType=elem.getAttribute("nameType"),
                        value=elem.getAttribute("value"),
                        valueType=elem.getAttribute("valueType");
        final Object    nameKey, valueKey;
        if ((nameType != null) && (nameType.length() > 0))
        {
            final ValueStringInstantiator<?>    vsi=resolveAttributeInstantiator(name, nameType);
            if (vsi == null)
                throw new IllegalStateException("setClientProperty(" + name + ") no converter for name type=" + nameType);
            nameKey = vsi.newInstance(name);
        }
        else
            nameKey = name;

        if ((valueType != null) && (valueType.length() > 0))
        {
            final ValueStringInstantiator<?>    vsi=resolveAttributeInstantiator(name, valueType);
            if (vsi == null)
                throw new IllegalStateException("setClientProperty(" + value + ") no converter for value type=" + valueType);
            valueKey = vsi.newInstance(value);
        }
        else
            valueKey = value;

        src.putClientProperty(nameKey, valueKey);
        return new MapEntryImpl<Object,Object>(nameKey, valueKey);
    }
    /*
     * @see net.community.chest.dom.transform.AbstractReflectiveProxy#fromXmlChild(java.lang.Object, org.w3c.dom.Element)
     */
    @Override
    public JC fromXmlChild (final JC src, final Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (isAccessibleContextElement(elem, tagName))
        {
            setAccessibleContext(src, elem);
            return src;
        }
        else if (isBorderElement(elem, tagName))
        {
            setBorder(src, elem);
            return src;
        }
        else if (isPropertyElement(elem, tagName))
        {
            setClientProperty(src, elem);
            return src;
        }


        return super.fromXmlChild(src, elem);
    }

    public static final JComponentReflectiveProxy<JComponent>    JCOMP=
                new JComponentReflectiveProxy<JComponent>(JComponent.class, true);
}
