/*
 *
 */
package net.community.chest.ui.helpers;

import java.awt.Container;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.xml.parsers.ParserConfigurationException;

import net.community.chest.awt.LocalizedComponent;
import net.community.chest.awt.border.BorderReflectiveProxy;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.impl.EmptyDocumentImpl;
import net.community.chest.dom.proxy.AbstractXmlProxyConverter;
import net.community.chest.dom.proxy.XmlProxyConvertible;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.resources.AnchoredResourceAccessor;
import net.community.chest.resources.XmlDocumentRetriever;
import net.community.chest.util.locale.LocaleUtils;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Oct 23, 2008 2:13:46 PM
 */
public final class HelperUtils {
    private HelperUtils ()
    {
        // no instance
    }
    /**
     * Attempts to find an XML {@link Document} associated with the provided
     * {@link Class} by searching for an XML resource that has the same name
     * as the <U>simple</U> class name and which is located in the <U>same</U>
     * package as the class. Failing that it tries to locate such a resource
     * for the super-class, and so on.
     * @param c The {@link Class} instance
     * @param baseClass The &quot;maximum&quot; hierarchy to climb in search
     * for the resource (inclusive) - if <code>null</code> then stop only at
     * {@link Object}.</BR>
     * <P><B>Note:</B> if the base class is not part of the hierarchy then the
     * lookup will stop at the original class and go no further.</P>
     * @param anchor The {@link Class#getResource(String)} to use
     * @param l The {@link Locale} for the document - <code>null</code> is same
     * as {@link Locale#getDefault()}
     * @return The located {@link Document}
     * @throws IOException If failed to access XML data
     * @throws ParserConfigurationException If failed to initialize XML parser
     * @throws SAXException If bad XML format
     */
    public static final Document loadClassComponentDocument (
            final Class<?> c, final Class<?> baseClass, final AnchoredResourceAccessor anchor, final Locale l)
        throws IOException, ParserConfigurationException, SAXException
    {
        final Map.Entry<? extends URL,? extends Document>    resPair=
            XmlDocumentRetriever.loadDerivedClassDocument(c, baseClass, anchor, l, XmlDocumentRetriever.XML_SUFFIX);
        final Document                                        doc=
            (null == resPair) ? null : resPair.getValue();
        if (doc != null)
            return doc;

        return null;
    }

    public static final Document loadClassComponentDocument (
            final Class<?> c, final Class<?> baseClass, final Class<?> anchor, final Locale l)
        throws IOException, ParserConfigurationException, SAXException
    {
        return loadClassComponentDocument(c, baseClass, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l);
    }
    /**
     * Serves as a cache for the XML {@link Document}-s that have been
     * resolved for the UI components. Key=fully qualified class path,
     * value=the assigned {@link Document}
     */
    private static Map<String,Document>    _compDocMap    /* =null */;
    public static final synchronized Map<String,Document> getClassComponentDocumentsMap ()
    {
        if (null == _compDocMap)
            _compDocMap = new TreeMap<String,Document>();
        return _compDocMap;
    }
    // returns previous instance
    public static final synchronized Map<String,Document> setClassComponentDocumentsMap (Map<String,Document> m)
    {
        final Map<String,Document>    prev=_compDocMap;
        _compDocMap = m;
        return prev;
    }

    private static final String getClassComponentKey (final Class<?> c, final Locale l)
    {
        final String    cName=(null == c) ? null : c.getName(),
                        lName=(null == l) ? null : LocaleUtils.getLocalePattern(l);
        return cName + "[" + lName + "]";
    }

    // same as "load" only uses a cache
    public static final Document getClassComponentDocument (
            final Class<?> c, final Class<?> baseClass, final AnchoredResourceAccessor anchor, final Locale l)
                throws IOException, ParserConfigurationException, SAXException
    {
        if (null == c)
            return null;

        final Map<String,Document>    dm=getClassComponentDocumentsMap();
        final String                ck=getClassComponentKey(c, l);
        Document                    doc=null;
        // check if have it cached
        synchronized(dm)
        {
            if ((doc=dm.get(ck)) != null)
                return doc;
        }

        /* NOTE !!! we release the lock on the cache while we look for the
         *         document in order not to lock others. We run the risk of the
         *         same resource being requested in a multi-threaded manner, but
         *         the assumption is that since the same result will be reached it
         *         does not matter if the resource is re-mapped (unless the SAME
         *         class is re-used but with a different base class).
         */
        if (null == (doc=loadClassComponentDocument(c, baseClass, anchor, l)))
            doc = EmptyDocumentImpl.EMPTY_DOC;    // use a placeholder to avoid lookup on next call

        final Document    prev;
        synchronized(dm)
        {
            prev = dm.put(ck, doc);
        }

        if (prev != null)    // mainly a debug breakpoint...
            return prev;

        return doc;
    }

    public static final Document getClassComponentDocument (
            final Class<?> c, final Class<?> baseClass, final Class<?> anchor, final Locale l)
                throws IOException, ParserConfigurationException, SAXException
    {
        return getClassComponentDocument(c, baseClass, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l);
    }

    public static final Document getObjectComponentDocument (
            final Object o, final Class<?> baseClass, final AnchoredResourceAccessor anchor, final Locale l)
                throws IOException, ParserConfigurationException, SAXException
    {
        return (null == o) ? null : getClassComponentDocument(o.getClass(), baseClass, anchor, l);
    }

    public static final Document getObjectComponentDocument (
            final Object o, final Class<?> baseClass, final Class<?> anchor, final Locale l)
                throws IOException, ParserConfigurationException, SAXException
    {
        return (null == o) ? null : getObjectComponentDocument(o, baseClass, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor), l);
    }

    public static final Document getObjectComponentDocument (
            final Object o, final Class<?> baseClass, final AnchoredResourceAccessor anchor)
                throws IOException, ParserConfigurationException, SAXException
    {
        final Locale    l=(o instanceof LocalizedComponent) ? ((LocalizedComponent) o).getDisplayLocale() : null;
        return getObjectComponentDocument(o, baseClass, anchor, l);
    }

    public static final Document getObjectComponentDocument (
            final Object o, final Class<?> baseClass, final Class<?> anchor)
                throws IOException, ParserConfigurationException, SAXException
    {
        return getObjectComponentDocument(o, baseClass, AbstractXmlProxyConverter.getAnchoredResourceChain(anchor));
    }
    /**
     * Default prefix of images sub-folder
     */
    public static final String    IMAGES_SUB_FOLDER="images";
    /**
     * @param value The configured image location (may be null/empty)
     * @return The adjusted location with {@link #IMAGES_SUB_FOLDER} as
     * its parent folder (if not already such) - may be null/empty or same
     * as input value if input value is null/empty or already contains the
     * {@link #IMAGES_SUB_FOLDER} as its parent folder
     */
    public static final String getDefaultImageLocationPath (final String value)
    {
        final int    vLen=(null == value) ? 0 : value.length();
        if (vLen <= 0)
            return null;

        if (StringUtil.startsWith(value, IMAGES_SUB_FOLDER, true, false))
        {
            final char    ch=value.charAt(IMAGES_SUB_FOLDER.length());
            if (('/' == ch) || ('\\' == ch))
                return value;
        }

        return IMAGES_SUB_FOLDER + "/" + value;
    }
    /**
     * @param c Anchor {@link Class}
     * @param value Image resource location <U>relative</U> to the class
     * location (package). <B>Note</B>: assumes that the image resource is in
     * the {@link #IMAGES_SUB_FOLDER} sub-folder <U>relative</U> to the anchor
     * {@link Class} location.
     * @return The {@link URL} of the image location after adjustment via call
     * to {@link #getDefaultImageLocationPath(String)} - may be null if resource
     * does not exist.
     * @throws Exception If failed to load resource
     */
    public static final URL getDefaultClassImageLocation (final Class<?> c, final String value) throws Exception
    {
        final String    resLoc=(null == c) ? null : getDefaultImageLocationPath(value);
        if ((null == resLoc) || (resLoc.length() <= 0))
            return null;

        return c.getResource(resLoc);
    }
    /**
     * Uses the current {@link Thread}'s context {@link ClassLoader} to
     * resolve the image resource location
     * @param value The location of the image resource - if not relative to
     * {@link #IMAGES_SUB_FOLDER} then it is added as the parent folder.
     * @return The resource URL - may be null if no initial location provided
     * or resource does not exist
     */
    public static final URL getDefaultImageLocation (final String value)
    {
        final String    resLoc=getDefaultImageLocationPath(value);
        if ((null == resLoc) || (resLoc.length() <= 0))
            return null;

        final String        resPath=('/' == resLoc.charAt(0)) ? resLoc : "/" + resLoc;
        final Thread        t=Thread.currentThread();
        final ClassLoader    cl=(null == t) ? null : t.getContextClassLoader();
        return (null == cl) /* should not happen */ ? null : cl.getResource(resPath);
    }
    /**
     * Locates a component's XML {@link Element} in a sections {@link Map}
     * @param sMap The sections/components {@link Map} - key=section/component
     * "name", value=the XML {@link Element}
     * @param compClass The component {@link Class} whose {@link Class#getSimpleName()}
     * value is used to look for the component XML element. If not found, then
     * the {@link Class#getSuperclass()} is used.
     * @param baseClass The top-most superclass to reach in search - if null then
     * {@link Object} is assumed
     * @return The resulting associated XML element "pair" (null if not found),
     * represented as a {@link java.util.Map.Entry} whose key=the resource name that was
     * used to locate the XML element, value=the associated XML element.
     */
    public static Map.Entry<String,Element> getComponentElement (final Map<String,Element> sMap, final Class<?> compClass, final Class<?> baseClass)
    {
        if ((null == sMap) || (sMap.size() <= 0) || (null == compClass))
            return null;

        // may be null/empty for anonymous classes
        final String    cn=compClass.getSimpleName();
        final Element    resElem=
            ((null == cn) || (cn.length() <= 0)) ? null : sMap.get(cn);
        if (resElem != null)
            return new MapEntryImpl<String,Element>(cn,resElem);

        final Class<?>    sc=compClass.getSuperclass();
        if (null == sc)    // OK if reached top-level
            return null;
        if ((baseClass != null) && (!baseClass.isAssignableFrom(sc)))
            return null;    // stop if gone beyond the base class

        return getComponentElement(sMap, sc, baseClass);
    }

    public static Map.Entry<String,Element> getComponentElement (final Map<String,Element> sMap, final Class<?> compClass)
    {
        return getComponentElement(sMap, compClass, Object.class);
    }

    public static Map.Entry<String,Element> getComponentObjectElement (final Map<String,Element> sMap, final Object comp, final Class<?> baseClass)
    {
        return (null == comp) ? null : getComponentElement(sMap, comp.getClass(), baseClass);
    }

    public static Map.Entry<String,Element> getComponentObjectElement (final Map<String,Element> sMap, final Object comp)
    {
        return getComponentObjectElement(sMap, comp, Object.class);
    }

    public static final <I extends XmlContainerComponentInitializer> I layoutSections (
            final I cci, final Collection<? extends Map.Entry<String,? extends Element>> sl) throws RuntimeException
    {
        if ((null == sl) || (sl.size() <= 0))
            return cci;

        for (final Map.Entry<String,? extends Element> se : sl)
        {
            final String    sn=(null == se) ? null : se.getKey();
            final Element    elem=(null == se) ? null : se.getValue();
            if (null == elem)
                continue;
            cci.layoutSection(sn, elem);
        }

        return cci;
    }

    public static final <I extends XmlContainerComponentInitializer> I layoutSections (final I cci) throws RuntimeException
    {
        final SectionsMap    sm=
            (null == cci) ? null : cci.getSectionsMap();
        return layoutSections(cci, ((null == sm) || (sm.size() <= 0)) ? null : sm.sectionsSet());
    }
    /**
     * @param sm The {@link Map} to use in order to retrieve the XML
     * {@link Element} for the object - if <code>null</code>/empty
     * then nothing is applied
     * @param name Section name under which XML element is mapped - if
     * <code>null/empty</code> or no element mapped then nothing is applied
     * @param object Object on which to apply the XML element - if
     * <code>null</code> then nothing is applied
     * @param proxy The {@link XmlProxyConvertible} instance to use - if
     * <code>null</code> then nothing is applied
     * @return The applied element - null if none applied
     * @throws RuntimeException If exception(s) while applying the XML
     * element
     */
    public static final Element applyDefinitionElement (
                final Map<String,? extends Element>    sm,
                final String                        name,
                final Object                        object,
                final XmlProxyConvertible<?>        proxy) throws RuntimeException
    {
        final Element    elem=
            ((null == sm) || (null == name) || (name.length() <= 0) || (null == object) || (null == proxy)) ? null : sm.get(name);
        if (null == elem)
            return null;
        try
        {
            @SuppressWarnings("unchecked")
            final Object    o=((XmlProxyConvertible<Object>) proxy).fromXml(object, elem);
            if (o != object)
                throw new IllegalStateException("applyDefinitionElement(" + name + ")[" + DOMUtils.toString(elem) + "] mismatched reconstructed instances");
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        return elem;
    }
    /**
     * @param cci The {@link XmlContainerComponentInitializer} to use in order
     * to retrieve the XML {@link Element} for the object
     * @param name Section name under which XML element is mapped - if
     * <code>null/empty</code> or no element mapped then nothing is applied
     * @param object Object on which to apply the XML element - if
     * <code>null</code> then nothing is applied
     * @param proxy The {@link XmlProxyConvertible} instance to use - if
     * <code>null</code> then nothing is applied
     * @return The applied element - null if none applied
     * @throws RuntimeException If exception(s) while applying the XML
     * element
     */
    public static final Element applyDefinitionElement (
            final XmlContainerComponentInitializer     cci,
            final String                            name,
            final Object                            object,
            final XmlProxyConvertible<?>            proxy) throws RuntimeException
    {
        return applyDefinitionElement((null == cci) ? null : cci.getSectionsMap(), name, object, proxy);
    }
    /**
     * @param <V> The component value type
     * @param c The component {@link Object} - if <code>instanceof {@link SettableComponent}</code>
     * then assume to accept a value of type V.
     * @param value The value to use if need to call {@link SettableComponent#setContent(Object)}
     * or {@link SettableComponent#refreshContent(Object)}
     * @param itemNewState <P>The call to invoke on {@link SettableComponent} interface:</P></BR>
     * <UL>
     *         <LI><code>null</code> - invoke {@link SettableComponent#clearContent()}</LI>
     *         <LI><code>TRUE</code> - invoke {@link SettableComponent#setContent(Object)}</LI>
     *         <LI><code>FALSE</code> - invoke {@link SettableComponent#refreshContent(Object)}</LI>
     * </UL>
     * @return The invoked {@link SettableComponent} instance if indeed the
     * component object was such and the invocation was executed
     */
    public static final <V> SettableComponent<V> updateSettableObject (
                    final Object c, final V value, final Boolean itemNewState)
    {
        if (c instanceof SettableComponent<?>)
        {
            @SuppressWarnings("unchecked")
            final SettableComponent<V>    sc=(SettableComponent<V>) c;
            if (null == itemNewState)
                sc.clearContent();
            else if (itemNewState.booleanValue())
                sc.setContent(value);
            else
                sc.refreshContent(value);

            return sc;
        }

        return null;
    }
    /**
     * @param <V> The component value type
     * @param value The value to use if need to call {@link SettableComponent#setContent(Object)}
     * or {@link SettableComponent#refreshContent(Object)}
     * @param itemNewState <P>The call to invoke on {@link SettableComponent} interface:</P></BR>
     * <UL>
     *         <LI><code>null</code> - invoke {@link SettableComponent#clearContent()}</LI>
     *         <LI><code>TRUE</code> - invoke {@link SettableComponent#setContent(Object)}</LI>
     *         <LI><code>FALSE</code> - invoke {@link SettableComponent#refreshContent(Object)}</LI>
     * </UL>
     * @param comps A {@link Collection} of objects to be checked if indeed
     * they implement the {@link SettableComponent} interface (if so then
     * assumed to expect a value of type V) - may be null/empty
     * @return A {@link Collection} of all the {@link SettableComponent}
     * on which the interface method was invoked - may be null/empty if no
     * original components to start with or none was a {@link SettableComponent}
     * @see #updateSettableObject(Object, Object, Boolean)
     */
    public static final <V> Collection<SettableComponent<V>> updateSettableComponents (
            final V value, final Boolean itemNewState, final Collection<?> comps)
    {
        if ((null == comps) || (comps.size() <= 0))
            return null;

        Collection<SettableComponent<V>>    cl=null;
        for (final Object c : comps)
        {
            final SettableComponent<V>    sc=updateSettableObject(c, value, itemNewState);
            if (null == sc)
                continue;

            if (null == cl)
                cl = new LinkedList<SettableComponent<V>>();
            cl.add(sc);
        }

        return cl;
    }
    /**
     * @param <V> The component value type
     * @param value The value to use if need to call {@link SettableComponent#setContent(Object)}
     * or {@link SettableComponent#refreshContent(Object)}
     * @param itemNewState <P>The call to invoke on {@link SettableComponent} interface:</P></BR>
     * <UL>
     *         <LI><code>null</code> - invoke {@link SettableComponent#clearContent()}</LI>
     *         <LI><code>TRUE</code> - invoke {@link SettableComponent#setContent(Object)}</LI>
     *         <LI><code>FALSE</code> - invoke {@link SettableComponent#refreshContent(Object)}</LI>
     * </UL>
     * @param comps An array of objects to be checked if indeed
     * they implement the {@link SettableComponent} interface (if so then
     * assumed to expect a value of type V) - may be null/empty
     * @return A {@link Collection} of all the {@link SettableComponent}
     * on which the interface method was invoked - may be null/empty if no
     * original components to start with or none was a {@link SettableComponent}
     */
    public static final <V> Collection<SettableComponent<V>> updateSettableComponents (
                        final V value, final Boolean itemNewState, final Object ... comps)
    {
        return ((null == comps) || (comps.length <= 0)) ? null : updateSettableComponents(value, itemNewState, Arrays.asList(comps));
    }
    /**
     * @param <V> The component value type
     * @param c The {@link Container} whose sub-components are to be checked
     * @param value The value to use if need to call {@link SettableComponent#setContent(Object)}
     * or {@link SettableComponent#refreshContent(Object)}
     * @param itemNewState <P>The call to invoke on {@link SettableComponent} interface:</P></BR>
     * <UL>
     *         <LI><code>null</code> - invoke {@link SettableComponent#clearContent()}</LI>
     *         <LI><code>TRUE</code> - invoke {@link SettableComponent#setContent(Object)}</LI>
     *         <LI><code>FALSE</code> - invoke {@link SettableComponent#refreshContent(Object)}</LI>
     * </UL>
     * @return A {@link Collection} of all the {@link SettableComponent}
     * on which the interface method was invoked - may be null/empty if no
     * original components to start with or none was a {@link SettableComponent}
     * @see #updateSettableComponents(Object, Boolean, Object...)
     */
    public static final <V> Collection<SettableComponent<V>> updateSettableComponents (
                                final Container c, final V value, final Boolean itemNewState)
    {
        return (null == c) ? null : updateSettableComponents(value, itemNewState, (Object[]) c.getComponents());
    }
    /**
     * Helper method for setting a {@link JComponent}-s {@link Border} value
     * from an XML {@link Element}
     * @param jc The {@link JComponent} to set - ignored if <code>null</code>
     * @param elem The XML {@link Element} to use in order to instantiate the
     * border value - ignored if <code>null</code>
     * @return The border - or <code>null</code> if none instantiated
     * (including if no matching instantiator found)
     * @throws RuntimeException If failed to instantiate the border from the
     * XML element.
     */
    public static final Border setBorder (final JComponent jc, final Element elem) throws RuntimeException
    {
        final XmlValueInstantiator<? extends Border>    proxy=
            ((null == elem) || (null == jc)) ? null : BorderReflectiveProxy.getBorderInstantiator(elem);
        try
        {
            final Border    b=(null == proxy) ? null : proxy.fromXml(elem);
            if (b != null)
                jc.setBorder(b);
            return b;
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }
    }
    /**
     * Helper method for setting a {@link JComponent}-s {@link Border} value
     * from an XML {@link Element}
     * @param jc The {@link JComponent} to set - ignored if <code>null</code>
     * @param cci The {@link XmlContainerComponentInitializer#getSection(String)}
     * instance to call - ignored if <code>null</code>
     * @param secName The XML section name to query - ignored if
     * <code>null</code>/empty
     * @return A pair represented as a {@link java.util.Map.Entry} whose key=the XML
     * element used to instantiate the border, value=the {@link Border}. May
     * be <code>null</code> if no component/section element found. <B>Note:</B>
     * if no instantiator found then a non-<code>null</code> pair is returned
     * with a <code>null</code> border instance value
     * @throws RuntimeException If failed to instantiate the border from the
     * XML element.
     */
    public static final Map.Entry<Element,Border> setBorder (
            final JComponent                        jc,
            final XmlContainerComponentInitializer    cci,
            final String                            secName) throws RuntimeException
    {
        final Element    elem=
            ((null == jc) || (null == cci) || (null == secName) || (secName.length() <= 0)) ? null : cci.getSection(secName);
        if (null == elem)
            return null;

        final Border    b=setBorder(jc, elem);
           return new MapEntryImpl<Element,Border>(elem, b);
    }
    /**
     * Helper method for setting a {@link JComponent}-s {@link Border} value
     * from an XML {@link Element}
     * @param jc The {@link JComponent} to set - ignored if <code>null</code>
     * @param cci The {@link Map} of XML "sections" to query - key=section
     * name string, value=matching XML element - ignored if <code>null</code>
     * @param secName The XML section name to query - ignored if
     * <code>null</code>/empty
     * @return A pair represented as a {@link java.util.Map.Entry} whose key=the XML
     * element used to instantiate the border, value=the {@link Border}. May
     * be <code>null</code> if no component/section element found. <B>Note:</B>
     * if no instantiator found then a non-<code>null</code> pair is returned
     * with a <code>null</code> border instance value
     * @throws RuntimeException If failed to instantiate the border from the
     * XML element.
     */
    public static final Map.Entry<Element,Border> setBorder (
            final JComponent                        jc,
            final Map<String, ? extends Element>    cci,
            final String                            secName) throws RuntimeException
    {
        final Element    elem=
            ((null == jc) || (null == cci) || (null == secName) || (secName.length() <= 0)) ? null : cci.get(secName);
        if (null == elem)
            return null;

        final Border    b=setBorder(jc, elem);
           return new MapEntryImpl<Element,Border>(elem, b);
    }
}
