/*
 *
 */
package net.community.chest.eclipse.launch;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import net.community.chest.convert.ValueStringInstantiator;
import net.community.chest.dom.DOMUtils;
import net.community.chest.io.FileUtil;
import net.community.chest.lang.ExceptionUtil;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.map.MapEntryImpl;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Contains various launch file definitions</P>
 * @author Lyor G.
 * @since Oct 8, 2009 1:40:03 PM
 */
public final class LaunchUtils {
    private LaunchUtils ()
    {
        // no instance
    }
    /**
     * Name of XML element holding the launch configuration
     */
    public static final String LAUNCH_CONFIG_ELEM_NAME="launchConfiguration",
                                    TYPE_ATTR="type";
    /**
     * Default suffix of a launch configuration file
     */
    public static final String    LAUNCH_FILE_SUFFIX="launch";
    public static final boolean isLaunchFile (final String filePath)
    {
        return FileUtil.isMatchingFileSuffix(filePath, LAUNCH_FILE_SUFFIX);
    }

    public static final boolean isLaunchFile (final URL url)
    {
        return (null == url) ? false : isLaunchFile(url.getPath());
    }

    private static final Class<?> getTagNameClass (
            final String                             tagName,
            final String                            tagSuffix,
            final Collection<? extends Class<?>>    allowedTypes)
    {
        if ((null == tagSuffix) || (tagSuffix.length() <= 0)
         || (null == allowedTypes) || (allowedTypes.size() <= 0)
         || (!StringUtil.endsWith(tagName, tagSuffix, true, false)))
            return null;

        final String    attrType=
            tagName.substring(0, tagName.length() - tagSuffix.length());
        for (final Class<?> t : allowedTypes)
        {
            final String    tn=(null == t) ? null : t.getSimpleName();
            if (StringUtil.compareDataStrings(tn, attrType, false) == 0)
                return t;
        }

        return null;
    }
    /**
     * A {@link List} of all supported attribute types
     */
    public static final List<Class<?>>    ATTR_TYPES=
            Collections.unmodifiableList(Arrays.asList(Boolean.class, String.class, List.class));
    /**
     * XML element suffix of attribute entries
     */
    public static final String    ATTRIBUTE_ELEMENT_SUFFIX="Attribute",
                                    KEY_ATTR="key",
                                    VALUE_ATTR="value";
    /**
     * Extracts the attribute type from its XML {@link Element} tag name
     * @param tagName The XML {@link Element} tag name
     * @return The {@link Class} representing the attribute type
     * - <code>null</code> if the tag name does not end in the {@link #ATTRIBUTE_ELEMENT_SUFFIX}
     * suffix or the type is not listed in the {@link #ATTR_TYPES} list
     */
    public static final Class<?> getAttributeClass (final String tagName)
    {
        return getTagNameClass(tagName, ATTRIBUTE_ELEMENT_SUFFIX, ATTR_TYPES);
    }

    public static final Class<?> getAttributeClass (final Element elem)
    {
        return (null == elem) ? null : getAttributeClass(elem.getTagName());
    }
    /**
     * A {@link List} of all supported entries types
     */
    @SuppressWarnings("unchecked")
    public static final List<Class<?>>    ENTRY_TYPES=
        (List<Class<?>>) ((List<?>) Arrays.asList(List.class));
    /**
     * Suffix used for list entries
     */
    public static final String    ENTRY_ELEMENT_SUFFIX="Entry";
    /**
     * Extracts the attribute type from its XML {@link Element} tag name
     * @param tagName The XML {@link Element} tag name
     * @return The {@link Class} representing the entry type
     * - <code>null</code> if the tag name does not end in the {@link #ENTRY_ELEMENT_SUFFIX}
     * suffix or the type is not listed in the {@link #ENTRY_TYPES} list
     */
    public static final Class<?> getEntryClass (final String tagName)
    {
        return getTagNameClass(tagName, ENTRY_ELEMENT_SUFFIX, ENTRY_TYPES);
    }

    public static final Class<?> getEntryClass (final Element elem)
    {
        return (null == elem) ? null : getEntryClass(elem.getTagName());
    }
    /**
     * Extracts the {@link String} values of a list attribute
     * @param el A {@link Collection} of XML {@link Element}-s that represent
     * the list entries
     * @return A {@link List} with all the entries values
     * @throws DOMException If unknown entry type found
     * @see #getEntryClass(Element)
     * @see #getEntryClass(String)
     */
    public static final List<String> getListEntriesValues (final Collection<? extends Element> el)
        throws DOMException
    {
        final int    numEntries=(null == el) ? 0 : el.size();
        if (numEntries <= 0)
            return null;

        final List<String>    res=new ArrayList<String>(numEntries);
        for (final Element elem : el)
        {
            if (null == elem)
                continue;

            final Class<?>    ec=getEntryClass(elem);
            if (null == ec)
                throw new DOMException(DOMException.NAMESPACE_ERR, "Unknown entry type: " + DOMUtils.toString(elem));

            final String    val=elem.getAttribute(VALUE_ATTR);
            res.add(val);
        }

        return res;
    }
    /**
     * Extracts the {@link String} values of a list attribute
     * @param elem The root XML {@link Element}
     * @return A {@link List} with all the entries values
     * @throws DOMException If unknown entry type found
     * @see #getEntryClass(Element)
     * @see #getEntryClass(String)
     */
    public static final List<String> getListEntriesValues (final Element elem)
        throws DOMException
    {
        return (null == elem) ? null : getListEntriesValues(DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE));
    }

    public static final Object parseElementValue (final AttributeDescriptor ad, final Element elem)
        throws Exception
    {
        final Class<?>    vc=(null == ad) ? null : ad.getAttributeClass();
        if ((null == vc) || (null == elem))
            return null;

        if (List.class.isAssignableFrom(vc))
            return getListEntriesValues(elem);

        final String                        vv=elem.getAttribute(VALUE_ATTR);
        final ValueStringInstantiator<?>    vsi=
            ClassUtil.getJDKStringInstantiator(vc);
        if (null == vsi)
            throw new NoSuchElementException("parseElementValue(" + DOMUtils.toString(elem) + ") no instantiator for type=" + vc.getName());

        return vsi.newInstance(vv);
    }

    public static final Map.Entry<AttributeDescriptor,Object> parseAttributeElement (final Element elem)
        throws RuntimeException
    {
        final AttributeType                            aType=AttributeType.fromElement(elem);
        final Class<? extends AttributeDescriptor>    dc=
            (null == aType) ? null : aType.getAttributeDescriptorClass();
        if (null == dc)
            return null;

        // TODO find some generic way to do this
        final AttributeDescriptor    ad;
        final String                kv=elem.getAttribute(KEY_ATTR);
        if (dc == DebugAttribute.class)
            ad = DebugAttribute.fromString(kv);
        else if (dc == LaunchAttribute.class)
            ad = LaunchAttribute.fromString(kv);
        else
            throw new IllegalArgumentException("parseAttributeElement(" + DOMUtils.toString(elem) + ") unknown attribute type class: " + dc.getName());

        final Object    val;
        try
        {
            val = ad.newInstance(elem);
        }
        catch(Exception e)
        {
            throw ExceptionUtil.toRuntimeException(e);
        }

        return new MapEntryImpl<AttributeDescriptor,Object>(ad, val);
    }

    public static final List<Map.Entry<AttributeDescriptor,Object>> parseLaunchAttributes (final Element root)
        throws RuntimeException
    {
        final Collection<? extends Element>                    el=
            DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
        final int                                            numAttrs=
            (null == el) ? 0 : el.size();
        final List<Map.Entry<AttributeDescriptor,Object>>    ret=
            (numAttrs <= 0) ? null : new ArrayList<Map.Entry<AttributeDescriptor,Object>>(Math.max(numAttrs, 5));
        if (null == ret)
            return null;

        for (final Element elem : el)
        {
            final Map.Entry<AttributeDescriptor,Object>    pv=parseAttributeElement(elem);
            if (null == pv)
                continue;

            ret.add(pv);
        }

        return ret;
    }

    public static final List<Map.Entry<AttributeDescriptor,Object>> parseLaunchAttributes (final Document doc)
        throws RuntimeException
    {
        return parseLaunchAttributes((null == doc) ? null : doc.getDocumentElement());
    }
}
