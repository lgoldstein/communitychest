package net.community.chest.apache.ant.helpers;

import java.util.Collection;
import java.util.LinkedList;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Copyright 2007 as per GPLv2
 *
 * <P>Represents a basic ANT &lt;target&gt; XMl element information</P>
 *
 * @author Lyor G.
 * @since Jul 19, 2007 1:22:16 PM
 */
public class SkeletonBuildTarget extends BaseExecutableElement {
    /**
     *
     */
    private static final long serialVersionUID = 3980235356560478490L;
    /**
     * Default empty constructor
     */
    public SkeletonBuildTarget ()
    {
        super();
    }

    private String    _ifProprety;
    /**
     * @return <code>if</code> property value - may be null/empty
     */
    public String getIfProprety ()
    {
        return _ifProprety;
    }

    public void setIfProprety (String ifProprety)
    {
        _ifProprety = ifProprety;
    }

    private String    _unlessProperty;
    /**
     * @return the <code>unless</code> property value - may be null/empty
     */
    public String getUnlessProperty ()
    {
        return _unlessProperty;
    }

    public void setUnlessProperty (String unlessProperty)
    {
        _unlessProperty = unlessProperty;
    }

    private Collection<String>    _dependsList;
    /**
     * @return the <code>depends</code> names list - may be null/empty
     */
    public Collection<String> getDependsList ()
    {
        return _dependsList;
    }
    /**
     * Adds specified target name to the dependencies list - <B>Note:</B> does
     * <U>not</U> check if target already exists
     * @param name target name to be added - ignored if null/empty
     * @return updated list - may be null/empty if null/empty to begin with
     * and null/empty name to add
     */
    public Collection<String> addDependency (final String name)
    {
        try
        {
            @SuppressWarnings("unchecked")
            final Collection<String>    dl=CollectionsUtils.addStringMember(getDependsList(), name, LinkedList.class);
            setDependsList(dl);
            return getDependsList();
        }
        catch(Exception e)    // should not happen
        {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;

            throw new IllegalStateException("addDependency(" + name + ") " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    /**
     * @param depList comma separated list of dependency targets - may be
     * null/empty and/or contain spaces/tabs/CR/LF
     * @return list - may be null/empty if null/empty to begin with
     * and null/empty list to add
     * @throws IllegalArgumentException if after stripping all the tab/CR/LF
     * and trimming the spaces we are left with no name or name contains
     * spaces
     */
    public Collection<String> addDependencies (final String depList) throws IllegalArgumentException
    {
        final Collection<String>    tokens=StringUtil.splitString(depList, ',');
        if ((tokens != null) && (tokens.size() > 0))
        {
            for (final String t : tokens)
            {
                final String    ct=(null == t) ? null : t.replaceAll("[\t\r\n]", " ").trim();
                if ((null == ct) || (ct.length() <= 0))
                    throw new IllegalArgumentException("Null/empty target in list: " + depList);
                if (ct.indexOf(' ') >= 0)
                    throw new IllegalArgumentException("Bad name (" + ct + ") in list: " + depList);

                addDependency(ct);
            }
        }

        return getDependsList();
    }

    public void setDependsList (Collection<String> dependsList)
    {
        _dependsList = dependsList;
    }
    /**
     * Resets internal fields to null-s
     */
    public void clear ()
    {
        setName(null);
        setDescription(null);
        setIfProprety(null);
        setUnlessProperty(null);
        setDependsList(null);
    }
    /**
     * Base name of target XML element
     */
    public static final String    TARGET_ELEMNAME="target",
                                    NAME_ATTR="name",
                                    DEPENDS_ATTR="depends",
                                    DESCRIPTION_ATTR="description",
                                    IF_ATTR="if",
                                    UNLESS_ATTR="unless";
    /* <B>Note:</B> {@link #clear()}-s the contents <U>before</U> anything else
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public SkeletonBuildTarget fromXml (final Element root) throws Exception
    {
        if (null == root)
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "No root element to extract data from");

        clear();

        // make sure this is the "target" element
        {
            final String    eName=root.getNodeName();
            if (!TARGET_ELEMNAME.equals(eName))
                throw new DOMException(DOMException.NAMESPACE_ERR, "Unexpected root element name: " + eName);
        }

        final String    tgtName=root.getAttribute(NAME_ATTR);
        if ((null == tgtName) || (tgtName.length() <= 0))
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Missing target '" + NAME_ATTR + "' attribute");
        setName(tgtName);

        // OK if null/empty
        setDescription(root.getAttribute(DESCRIPTION_ATTR));
        setIfProprety(root.getAttribute(IF_ATTR));
        setUnlessProperty(root.getAttribute(UNLESS_ATTR));

        // make sure only if or unless but not both have been defined
        final String    ifProp=getIfProprety(), ulsProp=getUnlessProperty();
        if ((ifProp != null) && (ifProp.length() > 0)
         && (ulsProp != null) && (ulsProp.length() > 0))
            throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, getClass().getName() + "#fromXml(" + getName() + ") Not allowed both '" + IF_ATTR + "' and '" + UNLESS_ATTR + "'");

        addDependencies(root.getAttribute(DEPENDS_ATTR));
        return this;
    }

    public SkeletonBuildTarget (final Element elem) throws Exception
    {
        this();

        if (fromXml(elem) != this)
            throw new IllegalStateException("Mismatched recovered XML instance");
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (final Document doc) throws Exception
    {
        final Element    tgtElem=doc.createElement(TARGET_ELEMNAME);
        final String    tgtName=getName();
        if ((null == tgtName) || (tgtName.length() <= 0))
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Missing target '" + NAME_ATTR + "' attribute");

        DOMUtils.addNonEmptyAttribute(tgtElem, DESCRIPTION_ATTR, getDescription());

        // make sure only if or unless but not both have been defined
        final String    ifProp=getIfProprety(), ulsProp=getUnlessProperty();
        if ((ifProp != null) && (ifProp.length() > 0))
        {
            if ((ulsProp != null) && (ulsProp.length() > 0))
                throw new DOMException(DOMException.INUSE_ATTRIBUTE_ERR, getClass().getName() + "#toXml(" + getName() + ") Not allowed both '" + IF_ATTR + "' and '" + UNLESS_ATTR + "'");

            tgtElem.setAttribute(IF_ATTR, ifProp);
        }
        else if ((ulsProp != null) && (ulsProp.length() > 0))
            tgtElem.setAttribute(UNLESS_ATTR, ulsProp);

        final String    depList=StringUtil.asStringList(getDependsList(), ',');
        DOMUtils.addNonEmptyAttribute(tgtElem, DEPENDS_ATTR, depList);

        return tgtElem;
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public SkeletonBuildTarget clone () throws CloneNotSupportedException
    {
        final SkeletonBuildTarget    cpyTarget=getClass().cast(super.clone());
        final Collection<String>    depList=getDependsList();
        if (depList != null)
            cpyTarget.setDependsList(CollectionsUtils.duplicateCollection(depList, new LinkedList<String>()));

        return cpyTarget;
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (final Object obj)
    {
        if (this == obj)
            return true;
        if ((null == obj) || (!(obj instanceof SkeletonBuildTarget)))
            return false;

        final SkeletonBuildTarget    bt=(SkeletonBuildTarget) obj;
        return (0 == StringUtil.compareDataStrings(getName(), bt.getName(), true))
            && (0 == StringUtil.compareDataStrings(getDescription(), bt.getDescription(), true))
            && (0 == StringUtil.compareDataStrings(getIfProprety(), bt.getIfProprety(), true))
            && (0 == StringUtil.compareDataStrings(getUnlessProperty(), bt.getUnlessProperty(), true))
            && CollectionsUtils.isSameMembers(getDependsList(), bt.getDependsList())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getName(), true)
            + StringUtil.getDataStringHashCode(getDescription(), true)
            + StringUtil.getDataStringHashCode(getIfProprety(), true)
            + StringUtil.getDataStringHashCode(getUnlessProperty(), true)
            + CollectionsUtils.getMembersHashCode(getDependsList())
        ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getName() + "[" + getDescription() + "]";
    }
}
