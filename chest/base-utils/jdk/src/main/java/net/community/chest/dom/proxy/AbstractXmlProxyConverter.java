/*
 *
 */
package net.community.chest.dom.proxy;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TreeMap;

import net.community.chest.BaseTypedValuesContainer;
import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.resources.AnchoredResourceAccessor;
import net.community.chest.resources.AnchoredResourceAccessorsChain;
import net.community.chest.resources.ResourceDataRetriever;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @param <V> Type of value being converted
 * @author Lyor G.
 * @since Jul 30, 2009 10:01:12 AM
 */
public abstract class AbstractXmlProxyConverter<V> extends BaseTypedValuesContainer<V>
                implements XmlConvertible<V>, ResourcedXmlProxyConvertible<V>    {
    protected AbstractXmlProxyConverter (Class<V> objClass) throws IllegalArgumentException
    {
        super(objClass);
    }
    /**
     * Called by {@link #getAttributeInstantiator(Object, String, Class)} if a new
     * {@link Class} was encountered for which no previous
     * {@link ValueStringInstantiator} is cached.
     * @param <C> The instantiated {@link Object} class
     * @param aName attribute name
     * @param aType attribute type {@link Class}
     * @return The {@link ValueStringInstantiator} for the conversion
     * @throws Exception if cannot find an appropriate converter
     */
    protected <C> ValueStringInstantiator<C> resolveAttributeInstantiator (final String aName, final Class<C> aType) throws Exception
    {
        if ((null == aName) || (aName.length() <= 0) || (null == aType))
            return null;

        return ClassUtil.getJDKStringInstantiator(aType);
    }

    protected ValueStringInstantiator<?> resolveAttributeInstantiator (final String aName, final String aType) throws Exception
    {
        return resolveAttributeInstantiator(aName, ClassUtil.loadClassByName(aType));
    }

    private final Map<String,ValueStringInstantiator<?>>    _instsMap=
        new TreeMap<String,ValueStringInstantiator<?>>(String.CASE_INSENSITIVE_ORDER);
    /**
     * Called by default implementation of {@link #getObjectAttributeValue(Object, String, String, Class)}
     * in order to retrieve a {@link ValueStringInstantiator} to be used to
     * convert the string from the XML attribute into the correct {@link Object}
     * for invoking the setter.
     * @param src The object whose attribute is requested
     * @param aName attribute name
     * @param aType attribute type {@link Class}
     * @return The {@link ValueStringInstantiator} for the conversion
     * @throws Exception if cannot find an appropriate converter
     */
    protected ValueStringInstantiator<?> getAttributeInstantiator (final V src, final String aName, final Class<?> aType) throws Exception
    {
        if (null == aType)
            return null;

        synchronized(_instsMap)
        {
            ValueStringInstantiator<?>    vsi=_instsMap.get(aName);
            if (null == vsi)
            {
                if ((vsi=resolveAttributeInstantiator(aName, aType)) != null)
                {
                    final ValueStringInstantiator<?>    prev=_instsMap.put(aName, vsi);
                    if (prev != null)    // no way this should happen since we use synchronized access
                        throw new IllegalStateException("getAttributeInstantiator(" + src + "[" + aName + "])[" + aType.getName() + "] duplicate " + ValueStringInstantiator.class.getSimpleName() + " instances");
                }
                else
                    throw new IllegalStateException("getAttributeInstantiator(" + src + "[" + aName + "])[" + aType.getName() + "] no " + ValueStringInstantiator.class.getSimpleName() + " resolved");
            }

            return vsi;
        }
    }

    protected Object getObjectAttributeValue (final V src, final String aName, final String aValue, final Class<?> aType) throws Exception
    {
        final ValueStringInstantiator<?>    inst=
            getAttributeInstantiator(src, aName, aType);
        return inst.newInstance(aValue);
    }
    /**
     * Can be used to create/initialize an attributes aliases {@link Map}
     * suitable for the implementation of {@link #getAttributesAliases()}.
     * @param org The original {@link Map} to which to add the pairs - if
     * none provided, one will be created (provided some pairs specified)
     * @param strings &quot;Pairs&quot; where even index is the alias and odd
     * index the effective name
     * @return A {@link Map} of {@link String}-s where key=attribute alias,
     * value=the effective attribute name. May be null if no strings to begin
     * with and no original {@link Map}.
     * @throws IllegalStateException If duplicate entries or unpaired value
     * found or null/empty alias/name
     */
    public static final Map<String,String> addAttributeAliases (final Map<String,String> org, final String ...strings) throws IllegalStateException
    {
        Map<String,String>    ret=org;
        if ((null == strings) || (strings.length <= 0))
            return ret;
        // make sure all values are paired
        if ((strings.length & 0x01) != 0)
            throw new IllegalStateException("Unpaired values found");

        for (int    sIndex=0; sIndex < strings.length; sIndex += 2)
        {
            final String    a=strings[sIndex], n=strings[sIndex+1];
            if ((null == a) || (a.length() <= 0)
             || (null == n) || (n.length() <= 0))
                throw new IllegalStateException("Incomplete pair: " + a + "/" + n);

            if (null == ret)
                ret = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);

            final String    p=ret.put(a, n);
            if (p != null)
                throw new IllegalStateException("Duplicate mapping for alias=" + a + ": " + n + "/" + p);
        }

        return ret;
    }
    /**
     * Used to 'encode' attributes with a known prefix
     * @param name Original attribute name as found in the XML element
     * @param prefix The expected prefix
     * @param caseSensitive TRUE=check if prefix is in name case sensitive
     * @return The remainder of the string from the original name after
     * stripping the prefix - null if the original name does not contain
     * the specified prefix
     */
    public static final String extractSubAttribute (final String name, final String prefix, final boolean caseSensitive)
    {
        final int    nLen=(null == name) ? 0 : name.length(),
                    pLen=(null == prefix) ? 0 : prefix.length();
        if ((nLen <= 0) || (pLen <= 0) || (nLen <= pLen))
            return null;

        final String    np=name.substring(0, pLen);
        if (StringUtil.compareDataStrings(prefix, np, caseSensitive) != 0)
            return null;

        return name.substring(pLen);
    }

    protected Map<String,String> initializeAliasesMap (Map<String,String> org)
    {
        return org;
    }

    private Map<String,String>    _aliasMap;
    /**
     * Called by default implementation of {@link #getEffectiveAttributeName(String)}
     * @return A {@link Map} of {@link String}-s where key=attribute alias,
     * value=the effective attribute name. If null/empty (default) then no aliases
     * exist and {@link #getEffectiveAttributeName(String)} will simply return
     * its input parameter. <B>Note:</B> recommend using a <U>case insensitive</U>
     * {@link Map} unless good reason not to.
     */
    public synchronized Map<String,String> getAttributesAliases ()
    {
        if (null == _aliasMap)
            _aliasMap = initializeAliasesMap(_aliasMap);
        return _aliasMap;
    }

    protected synchronized Map<String,String> setAttributesAliases (Map<String,String> m)
    {
        final Map<String,String>    prev=getAttributesAliases();
        if (_aliasMap != m)
            _aliasMap = m;
        return prev;
    }

    protected Map<String,String> addAttributeAlias (final String aAlias, final String aName)
    {
        Map<String,String>    am=getAttributesAliases();
        if ((null == aAlias) || (aAlias.length() <= 0)
         || (null == aName) || (aName.length() <= 0))
            return am;

        if (null == am)
        {
            setAttributesAliases(new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER));
            if (null == (am=getAttributesAliases()))
                throw new IllegalStateException("addAttributeAlias(" + aAlias + "/" + aName + ") no map set");
        }

        final String    prev=am.put(aAlias, aName);
        if ((null == prev) || (prev.length() <= 0) && (!prev.equalsIgnoreCase(aName)))
            throw new IllegalStateException("addAttributeAlias(" + aAlias + "/" + aName + ") remapped (" + prev + ")");

        return am;
    }
    // even index=alias, odd index=attribute
    protected Map<String,String> addAttributeAliases (final String ... vals)
    {
        Map<String,String>    am=getAttributesAliases();
        if ((null == vals) || (vals.length <= 0))
            return am;
        if ((vals.length & 0x01) != 0)
            throw new IllegalStateException("addAttributeAliases() unpaired value(s)");

        for (int    aIndex=0; aIndex < vals.length; aIndex += 2)
            am = addAttributeAlias(vals[aIndex], vals[aIndex+1]);
        return am;
    }
    /**
     * Used to "alias" some attributes and map them to others
     * @param name original attribute name in XML element
     * @return effective attribute to use - same as input if no mapping
     */
    public String getEffectiveAttributeName (final String name)
    {
        if ((null == name) || (name.length() <= 0))
            return null;

        final Map<String,String>    aMap=getAttributesAliases();
        final String                n=
            ((null == aMap) || (aMap.size() <= 0)) ? null : aMap.get(name);
        if ((n != null) && (n.length() > 0))
            return n;

        return name;
    }
    /**
     * Helper method - default implementation checks that XML {@link Element}
     * is non-null and that the non-null/empty expected tag name matches the
     * supplied one
     * @param elem The XML {@link Element}
     * @param tagName The XML {@link Element}-s tag name (Note: not checked that
     * this is indeed the same as {@link Element#getTagName()})
     * @param expName The expected tag name
     * @return TRUE if XML {@link Element} is not null, the expected name is not
     * null/empty and it matches the supplied tag name <U>case insensitive</U>
     */
    public static final boolean isDefaultMatchingElement (final Element elem, final String tagName, final String expName)
    {
        return (elem != null)
            && (expName != null) && (expName.length() > 0)
            && expName.equalsIgnoreCase(tagName)
            ;
    }
    // @see isDefaultMatchingElement
    protected boolean isMatchingElement (final Element elem, final String tagName, final String expName)
    {
        return isDefaultMatchingElement(elem, tagName, expName);
    }
    /**
     * Helper method - default implementation checks that XML {@link Element}
     * is non-null and that the non-null/empty expected attribute value
     * matches the supplied one
     * @param elem The XML {@link Element}
     * @param attrVal The XML {@link Element}-s attribute (Note: not checked
     * that this is indeed the same as {@link Element#getAttribute(String)}
     * @param expVal The expected value
     * @return TRUE if XML {@link Element} is not null, the expected value is not
     * null/empty and it matches the supplied one <U>case insensitive</U>
     */
    protected boolean isMatchingAttribute (final Element elem, final String attrVal, final String expVal)
    {
        return (elem != null)
            && (expVal != null) && (expVal.length() > 0)
            && expVal.equalsIgnoreCase(attrVal)
            ;
    }
    /**
     * Called by {@link #fromXmlChild(Object, Element)} default implementation
     * @param src The last known object instance value to be recovered
     * @param elem The child element
     * @return Updated instance - should be same as input unless very good
     * reason not to (in which case caller code must be <U>thoroughly</U>
     * tested)
     * @throws Exception Unless overridden throws an {@link UnsupportedOperationException}
     */
    public V handleUnknownXmlChild (final V src, final Element elem) throws Exception
    {
        throw new UnsupportedOperationException("handleUnknownXmlChild(" + getValuesClass().getName() + ") no handler child=" + elem.getTagName() + " of " + src);
    }
    /**
     * Called by <code>fromXmlChildren(Object, Collection, Map)<code> default
     * implementation when it receives non-empty {@link Collection} of
     * children {@link Element}-s
     * @param src The last known object instance value to be recovered
     * @param elem The child element
     * @return Updated instance - should be same as input unless very good
     * reason not to (in which case caller code must be <U>thoroughly</U>
     * tested)
     * @throws Exception Unless overridden throws an {@link UnsupportedOperationException}
     */
    public V fromXmlChild (final V src, final Element elem) throws Exception
    {
        return handleUnknownXmlChild(src, elem);
    }
    /**
     * A {@link ThreadLocal} instance used to hold the current
     * {@link ReflectiveResourceLoaderContext} instance for the duration
     * of the {@link #fromXml(Object, Element, ReflectiveResourceLoaderContext)}
     * method invocation
     */
    private static final ThreadLocal<ReflectiveResourceLoaderContext>    _resContext=new ThreadLocal<ReflectiveResourceLoaderContext>();
    public ReflectiveResourceLoaderContext getResourceLoaderContext ()
    {
        return _resContext.get();
    }
    /**
     * Default {@link ReflectiveResourceLoaderContext} instance that is used
     * by {@link #resolveResourceLoaderContext()} default implementation
     */
    private static ReflectiveResourceLoaderContext    _defLoader    /* =null */;
    public static final synchronized ReflectiveResourceLoaderContext getDefaultLoader ()
    {
        return _defLoader;
    }

    public static final synchronized ReflectiveResourceLoaderContext setDefaultLoader (
            final ReflectiveResourceLoaderContext resLoader)
    {
        final ReflectiveResourceLoaderContext    prev=_defLoader;
        _defLoader = resLoader;
        return prev;
    }

    public static final AnchoredResourceAccessor getAnchoredResourceChain (final Class<?> anchor)
    {
        final AnchoredResourceAccessor    defLoader=getDefaultLoader(),
                                        ancLoader=ResourceDataRetriever.getAnchoredResourceAccessor(anchor);
        if (null == defLoader)
            return ancLoader;
        else if (null == ancLoader)
            return defLoader;

        return new AnchoredResourceAccessorsChain(ancLoader, defLoader);
    }

    protected ReflectiveResourceLoaderContext resolveResourceLoaderContext ()
    {
        final ReflectiveResourceLoaderContext    resLoader=getResourceLoaderContext();
        if (null == resLoader)
            return getDefaultLoader();

        return resLoader;
    }

    protected Object loadObjectResourceAttribute (final V src, final String aName, final String aValue, final Class<?> aType) throws Exception
    {
        final ReflectiveResourceLoaderContext    ctx=resolveResourceLoaderContext();
        final ReflectiveResourceLoader            rld=(null == ctx) ? null : ctx.getResourceLoader(aType, src, aName, aValue);
        if (null == rld)
            throw new MissingResourceException("loadObjectResourceAttribute(" + getValuesClass().getName() + "#" + aName + ")", (null == aType) ? null : aType.getName(), aValue);

        return rld.loadAttributeResource(aType, src, aName, aValue);
    }
    /*
     * @see net.community.chest.dom.transform.ResourcedXmlProxyConvertible#fromXml(java.lang.Object, org.w3c.dom.Element, net.community.chest.dom.transform.ReflectiveResourceLoaderContext)
     */
    @Override
    public V fromXml (V src, Element elem, ReflectiveResourceLoaderContext resContext) throws Exception
    {
        _resContext.set(resContext);
        try
        {
            return fromXml(src, elem);
        }
        finally
        {
            _resContext.set(null);    // restore to empty state
        }
    }
    /**
     * Called by default implementation of {@link #fromXml(Element)} in order
     * to obtain a "clean" instance to be initialized from the {@link Element}.
     * The default implementation simply calls {@link Class#newInstance()} of
     * the container values class
     * @param elem The {@link Element} for which a new instance is required
     * @return created "clean" instance - may NOT be null
     * @throws Exception if cannot create a new instance.
     */
    public V createInstance (Element elem) throws Exception
    {
        if (null == elem)
            throw new IllegalArgumentException("createInstance() no XML element instance");

        return getValuesClass().newInstance();
    }

    private Constructor<V>    _elemCtor;
    protected Constructor<V> getElementConstructor ()
    {
        if (null == _elemCtor)
        {
            try
            {
                final Class<V>    vc=getValuesClass();
                if (null == (_elemCtor=vc.getConstructor(Element.class)))
                    throw new NoSuchMethodException("No " + Element.class.getSimpleName() + " constructor");
            }
            catch(NoSuchMethodException e)
            {
                return null;
            }
        }

        return _elemCtor;
    }

    protected void setElementConstructor (Constructor<V> ctor)
    {
        _elemCtor = ctor;
    }

    private Boolean    _useElemCtor    /* =null */;
    /*
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public V fromXml (Element elem) throws Exception
    {
        synchronized(this)
        {
            if (null == _useElemCtor)
                _useElemCtor = Boolean.valueOf(getElementConstructor() != null);
        }

        if (_useElemCtor.booleanValue())
        {
            final Constructor<V>    ec=getElementConstructor();
            return ec.newInstance(elem);
        }

        return fromXml(createInstance(elem), elem);
    }
    /**
     * Attempts to find the &quot;best&quot; constructor to be used for XML
     * {@link Element} construction
     * @param <O> The explored type
     * @param oc The inspected {@link Class} instance
     * @return The &quot;best&quot; {@link Constructor} resolved as follows:</P></BR>
     * <UL>
     *         <LI>
     *         If a constructor accepting an XML {@link Element} is found, then
     *         use it
     *         </LI>
     *
     *         <LI>
     *         If an &quot;empty&quot; (no-args) constructor is found then use it
     *         </LI>
     *
     *         <LI>
     *         Otherwise return <code>null</code>
     *         </LI>
     * </UL>
     * @throws Exception if cannot resolve any constructor
     */
    public static final <O> Constructor<O> resolveConstructorInstance (final Class<O> oc) throws Exception
    {
        if (null == oc)
            return null;

        try
        {
            return oc.getConstructor(Element.class);
        }
        catch(NoSuchMethodException e)
        {
            // ignored
        }

        try
        {
            return oc.getConstructor();
        }
        catch(NoSuchMethodException e)
        {
            // ignored
        }

        return null;
    }

    public String getRootElementName ()
    {
        final Class<V>    vClass=getValuesClass();
        return (null == vClass) ? null : vClass.getSimpleName();
    }
    /*
     * @see net.community.chest.dom.transform.XmlTranslator#toXml(java.lang.Object, org.w3c.dom.Document)
     */
    @Override
    public Element toXml (V src, Document doc) throws Exception
    {
        if (null == doc)    // just so compiler does not complain about unused parameter
            throw new IllegalArgumentException("toXml(" + src + ") no " + Document.class.getSimpleName() + " instance");

        final String    elemName=getRootElementName();
        final Element    rootElem=
            ((null == elemName) || (elemName.length() <= 0)) ? null : doc.createElement(elemName);
        return toXml(src, doc, rootElem);
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        return toXml(null, doc);
    }
}
