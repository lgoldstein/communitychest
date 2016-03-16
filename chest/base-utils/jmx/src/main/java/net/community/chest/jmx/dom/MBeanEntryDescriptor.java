package net.community.chest.jmx.dom;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.ElementIndicatorExceptionContainer;
import net.community.chest.dom.impl.StandaloneElementImpl;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.dom.transform.XmlValueInstantiator;
import net.community.chest.jmx.JMXErrorHandler;
import net.community.chest.lang.PubliclyCloneable;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;
import net.community.chest.util.map.MapEntryImpl;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to hold MBean deployment data</P>
 *
 * @author Lyor G.
 * @since Aug 14, 2007 8:54:42 AM
 */
public class MBeanEntryDescriptor implements
        Serializable, PubliclyCloneable<MBeanEntryDescriptor>, XmlConvertible<MBeanEntryDescriptor> {
    /**
     *
     */
    private static final long serialVersionUID = -37160946990621151L;
    /**
     * Default (empty) constructor
     */
    public MBeanEntryDescriptor ()
    {
        super();
    }

    private String    _className    /* =null */;
    /**
     * @return the (fully qualified) class name that implements the MBean
     */
    public String getClassName ()
    {
        return _className;
    }

    public void setClassName (String className)
    {
        _className = className;
    }

    private String    _objName    /* =null */;
    /**
     * @return the object name under which this MBean is/should be registered
     */
    public String getObjectName ()
    {
        return _objName;
    }

    public void setObjectName (String objName)
    {
        _objName = objName;
    }

    private String    _depName    /* =null */;
    /**
     * @return the object name of another MBean this one depends on - may
     * be null/empty if no dependency
     */
    public String getDependsName ()
    {
        return _depName;
    }

    public void setDependsName (String depName)
    {
        _depName = depName;
    }

    private String _description;
    public String getDescription ()
    {
        return _description;
    }

    public void setDescription (String description)
    {
        _description = description;
    }

    public MBeanEntryDescriptor (String objName, String clsName, String depName, String description)
    {
        _objName = objName;
        _className = clsName;
        _depName = depName;
        _description = description;
    }

    public MBeanEntryDescriptor (String objName, String clsName, String depName)
    {
        this(objName, clsName, depName, null);
    }

    public MBeanEntryDescriptor (String objName, String clsName)
    {
        this(objName, clsName, null);
    }

    public MBeanEntryDescriptor (String objName)
    {
        this(objName, null);
    }

    public MBeanEntryDescriptor (ObjectName objName, Class<?> clsType, ObjectName depName)
    {
        this((null == objName) ? null : objName.toString(), (null == clsType) ? null : clsType.getName(), (null == depName) ? null : depName.toString());
    }

    public MBeanEntryDescriptor (ObjectName objName, Class<?> clsType)
    {
        this(objName, clsType, null);
    }

    public MBeanEntryDescriptor (ObjectName objName)
    {
        this(objName, null);
    }

    private Collection<MBeanAttributeDescriptor>    _attrs    /* =null */;
    /**
     * @return {@link Collection} of attributes to be set when the MBean is
     * instantiated (may be null/empty if none to be set). Each attribute is
     * represented by a {@link java.util.Map.Entry} where the key=attribute name and
     * value=attribute value (as a {@link String} - should be converted to the
     * corrected set-ter type when actual attribute set)
     */
    public Collection<MBeanAttributeDescriptor> getAttributes ()
    {
        return _attrs;
    }

    public Map<String,MBeanAttributeDescriptor> getAttributesMap ()
    {
        final Collection<? extends MBeanAttributeDescriptor>    al=getAttributes();
        if ((null == al) || (al.size() <= 0))
            return null;

        final Map<String,MBeanAttributeDescriptor>    aMap=
            new TreeMap<String,MBeanAttributeDescriptor>(String.CASE_INSENSITIVE_ORDER);
        for (final MBeanAttributeDescriptor a : al)
        {
            final String    an=(null == a) ? null : a.getName();
            if ((null == an) || (an.length() <= 0))
                continue;

            aMap.put(an, a);
        }

        return aMap;
    }
    /**
     * Adds an attribute to the currently specified ones as the <U>last</U>
     * one (first one if no previous attributes). <B>Note:</B> does not check
     * if <U>duplicate</U> attribute added.
     * @param a The {@link MBeanAttributeDescriptor} to be added - ignored if null
     * @return updated attributes {@link Collection} - may be null/empty if
     * null/empty original attributes and no attribute added
     */
    public Collection<MBeanAttributeDescriptor> addAttribute (final MBeanAttributeDescriptor a)
    {
        if (a != null)
        {
            Collection<MBeanAttributeDescriptor>    attrs=getAttributes();
            if (null == attrs)
            {
                setAttributes(new LinkedList<MBeanAttributeDescriptor>());
                if (null == (attrs=getAttributes()))    // should not happen
                    throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "addAttribute") + " no attribute " + Collection.class.getName() + " created though requested");
            }

            attrs.add(a);
        }

        return getAttributes();
    }

    public MBeanAttributeDescriptor addAttributeInfo (final MBeanAttributeInfo aInfo)
    {
        if (null == aInfo)
            return null;

        final MBeanAttributeDescriptor    a=new MBeanAttributeDescriptor(aInfo);
        addAttribute(a);
        return a;
    }
    // returns only the ADDED entries
    public Collection<MBeanAttributeDescriptor> addAttributesInfo (final Collection<? extends MBeanAttributeInfo> ai)
    {
        final int    numAttrs=(null == ai) ? 0 : ai.size();
        if (numAttrs <= 0)
            return null;

        Collection<MBeanAttributeDescriptor>    ret=null;
        for (final MBeanAttributeInfo aInfo : ai)
        {
            final MBeanAttributeDescriptor    a=addAttributeInfo(aInfo);
            if (null == a)
                continue;

            if (null == ret)
                ret = new ArrayList<MBeanAttributeDescriptor>(numAttrs);
            ret.add(a);
        }

        return ret;
    }

    public Collection<MBeanAttributeDescriptor> setAttributesInfo (final Collection<? extends MBeanAttributeInfo> ai)
    {
        final Collection<? extends MBeanAttributeDescriptor>    al=getAttributes();
        if ((al != null) && (al.size() > 0))
            al.clear();

        return addAttributesInfo(ai);
    }

    public Collection<MBeanAttributeDescriptor> setAttributesInfo (MBeanAttributeInfo ... ai)
    {
        return setAttributesInfo((null == ai) || (ai.length <= 0) ? null : Arrays.asList(ai));
    }

    private Collection<MBeanOperationDescriptor>    _opers;
    public Collection<MBeanOperationDescriptor> getOperations ()
    {
        return _opers;
    }

    public void setOperations (Collection<MBeanOperationDescriptor> opers)
    {
        _opers = opers;
    }

    public Collection<MBeanOperationDescriptor> addOperation (MBeanOperationDescriptor d)
    {
        if (d != null)
        {
            Collection<MBeanOperationDescriptor>    ol=getOperations();
            if (null == ol)
            {
                setOperations(new LinkedList<MBeanOperationDescriptor>());
                if (null == (ol=getOperations()))
                    throw new IllegalStateException("No operations collection created");
            }
            ol.add(d);
        }

        return getOperations();
    }

    public MBeanOperationDescriptor addOperation (MBeanOperationInfo o)
    {
        if (null == o)
            return null;

        final MBeanOperationDescriptor    d=new MBeanOperationDescriptor(o);
        addOperation(d);
        return d;
    }

    public Map<String,Collection<MBeanOperationDescriptor>> getOperationsMap ()
    {
        final Collection<? extends MBeanOperationDescriptor>    ol=getOperations();
        if ((null == ol) || (ol.size() <= 0))
            return null;

        final Map<String,Collection<MBeanOperationDescriptor>>    om=
            new TreeMap<String,Collection<MBeanOperationDescriptor>>(String.CASE_INSENSITIVE_ORDER);
        for (final MBeanOperationDescriptor d : ol)
        {
            final String    dn=(null == d) ? null : d.getName();
            if ((null == dn) || (dn.length() <= 0))
                continue;

            Collection<MBeanOperationDescriptor>    dl=om.get(dn);
            if (null == dl)
            {
                dl = new LinkedList<MBeanOperationDescriptor>();
                om.put(dn, dl);
            }
            dl.add(d);
        }

        return om;
    }

    public Collection<MBeanOperationDescriptor> setOperations (final MBeanOperationInfo ... opers)
    {
        final Collection<? extends MBeanOperationDescriptor>    ol=getOperations();
        if ((ol != null) && (ol.size() > 0))
            ol.clear();

        final int    numOpers=(null == opers) ? 0 : opers.length;
        if (numOpers <= 0)
            return null;

        Collection<MBeanOperationDescriptor>    ret=null;
        for (final MBeanOperationInfo o : opers)
        {
            final MBeanOperationDescriptor    d=addOperation(o);
            if (null == d)
                continue;
            if (null == ret)
                ret = new ArrayList<MBeanOperationDescriptor>(numOpers);
            ret.add(d);
        }

        return ret;
    }

    public MBeanEntryDescriptor (MBeanInfo mbi)
    {
        fromMBeanInfo(mbi);
    }

    public void fromMBeanInfo (MBeanInfo mbi)
    {
        _className = (null == mbi) ? null : mbi.getClassName();
        _description = (null == mbi) ? null : mbi.getDescription();
        setAttributesInfo((null == mbi) ? null : mbi.getAttributes());
        setOperations((null == mbi) ? null : mbi.getOperations());
    }
    /**
     * Adds an attribute to the currently specified ones as the <U>last</U>
     * one (first one if no previous attributes). <B>Note:</B> does not check
     * if <U>duplicate</U> attribute added.
     * @param name attribute name - may NOT be null/empty
     * @param value attribute value - may NOT be null/empty
     * @return updated attributes {@link Collection}
     * @throws IllegalArgumentException if null/empty attribute name/value
     */
    public Collection<MBeanAttributeDescriptor> addAttribute (final String name, final String value) throws IllegalArgumentException
    {
        if ((null == name) || (name.length() <= 0)
         || (null == value) || (value.length() <= 0))
            throw new IllegalArgumentException(ClassUtil.getArgumentsExceptionLocation(getClass(), "addAttribute", name, value) + " null/empty attribute name/value");

        return addAttribute(new MBeanAttributeDescriptor(name, String.class.getName(), value));
    }

    public void setAttributes (Collection<MBeanAttributeDescriptor> attrs)
    {
        _attrs = attrs;
    }
    /**
     * Resets contents to (almost - does not null-ify the attributes only
     * clears them) same as default empty constructor
     */
    public void clear ()
    {
        setClassName((String) null);
        setObjectName((String) null);
        setDependsName((String) null);

        final Collection<MBeanAttributeDescriptor>    attrs=getAttributes();
        if (attrs != null)
            attrs.clear();

        final Collection<? extends MBeanOperationDescriptor>    ol=getOperations();
        if ((ol != null) && (ol.size() > 0))
            ol.clear();
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public MBeanEntryDescriptor clone () throws CloneNotSupportedException
    {
        return getClass().cast(super.clone());
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (final Object obj)
    {
        if ((null == obj) || (!(obj instanceof MBeanEntryDescriptor)))
            return false;
        if (this == obj)
            return true;

        final MBeanEntryDescriptor    mbd=(MBeanEntryDescriptor) obj;
        return (0 == StringUtil.compareDataStrings(getClassName(), mbd.getClassName(), true))
            && (0 == StringUtil.compareDataStrings(getObjectName(), mbd.getObjectName(), true))
            && (0 == StringUtil.compareDataStrings(getDependsName(), mbd.getDependsName(), true))
            && CollectionsUtils.isSameMembers(getAttributes(), mbd.getAttributes())
            ;    // NOTE: we do not compare description(s)
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getClassName(), true)
             + StringUtil.getDataStringHashCode(getObjectName(), true)
             + StringUtil.getDataStringHashCode(getDependsName(), true)
             + CollectionsUtils.getMembersHashCode(getAttributes())
             ;
    }
    /**
     * Default name used for MBean descriptor(s) XML elements
     */
    public static final String    MBEAN_ELEM_NAME="mbean",
                                    CODE_ATTR="code",
                                    NAME_ATTR="name",
                                    DESC_ATTR="description",
                                    DEPENDS_ATTR="depends";
    public String setClassName (final Element elem)
    {
        final String    cn=(null == elem) ? null : elem.getAttribute(CODE_ATTR);
        if ((cn != null) && (cn.length() > 0))
            setClassName(cn);

        return cn;
    }

    public String setObjectName (final Element elem) throws DOMException
    {
        final String    on=(null == elem) ? null : elem.getAttribute(NAME_ATTR);
        if ((on != null) && (on.length() > 0))
            setObjectName(on);

        return on;
    }
    // returns depends name - may be null/empty if none set
    public String setDependsName (final Element elem) throws DOMException
    {
        final String    dn=(null == elem) ? null : elem.getAttribute(DEPENDS_ATTR);
        if ((dn != null) && (dn.length() > 0))
            setDependsName(dn);

        return dn;
    }

    public String setDescription (Element elem)
    {
        final String    val=elem.getAttribute(DESC_ATTR);
        if ((val != null) && (val.length() > 0))
            setDescription(val);

        return val;
    }

    public MBeanAttributeDescriptor addAttribute (final Element elem) throws Exception
    {
        final MBeanAttributeDescriptor    a=new MBeanAttributeDescriptor(elem);
        // allow specification of value as attribute or as text element value
        String    value=elem.getAttribute(MBeanFeatureDescriptor.VALUE_ATTR);
        if ((null == value) || (value.length() <= 0))
        {
            if (((value=DOMUtils.getElementStringValue(elem)) != null) && (value.length() > 0))
            {
                final Object    v=a.resolveValueFromString(value);
                a.setValue(v);
            }
        }

        addAttribute(a);
        return a;
    }

    public MBeanOperationDescriptor addOperation (final Element elem) throws Exception
    {
        final MBeanOperationDescriptor    d=new MBeanOperationDescriptor(elem);
        // allow specification of value as attribute or as text element value
        String    value=elem.getAttribute(MBeanFeatureDescriptor.VALUE_ATTR);
        if ((null == value) || (value.length() <= 0))
        {
            if (((value=DOMUtils.getElementStringValue(elem)) != null) && (value.length() > 0))
            {
                final Object    v=d.resolveValueFromString(value);
                d.setValue(v);
            }
        }

        addOperation(d);
        return d;
    }
    /**
     * @return default XML element name to be used to create an XML
     * representation of current contents
     */
    public String getMBeanElementName ()
    {
        return MBEAN_ELEM_NAME;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return new StringBuilder(128)
                        .append("className=").append(getClassName())
                        .append(";objectName=").append(getObjectName())
                        .append(";description=").append(getDescription())
                        .append(";depends=").append(getDependsName())
                    .toString()
                    ;
    }
    /**
     * Called by default {@link #fromXml(Element)} implementation whenever a
     * non-{@link MBeanAttributeDescriptor#ATTR_ELEM_NAME} marked child element found to enabled
     * extensions to the XML. Does nothing unless overridden.
     * @param elem unknown {@link Element}
     * @param tagName element's tag name
     * @throws Exception never - by default
     */
    protected void handleUnknownAttributeElement (final Element elem, final String tagName) throws Exception
    {
        if ((null == elem) || (null == tagName) || (tagName.length() <= 0))    // just so compiler does not complain about un-referenced parameters
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getArgumentsExceptionLocation(getClass(), "handleUnknownAttributeElement", tagName) + " incomplete parameters");
    }

    public Collection<MBeanAttributeDescriptor> addAttributes (final Collection<? extends MBeanAttributeDescriptor> al)
    {
        Collection<MBeanAttributeDescriptor>    attrs=getAttributes();
        if ((al != null) && (al.size() > 0))
        {
            if (null == attrs)
            {
                setAttributes(new LinkedList<MBeanAttributeDescriptor>(al));
                if (null == (attrs=getAttributes()))    // should not happen
                    throw new IllegalStateException(ClassUtil.getExceptionLocation(getClass(), "addAttributes") + " no attribute " + Collection.class.getName() + " created though requested");
            }
            else
                attrs.addAll(al);
        }

        return attrs;
    }

    public boolean isAttributeElement (final Element elem, final String tagName)
    {
        return (elem != null) && MBeanAttributeDescriptor.ATTR_ELEM_NAME.equalsIgnoreCase(tagName);
    }

    public List<MBeanAttributeDescriptor> extractAttributes (final Collection<? extends Element> elems) throws Exception
    {
        final int    numAttrs=(elems == null) ? 0 : elems.size();
        if (numAttrs <= 0)
            return null;

        List<MBeanAttributeDescriptor>    attrs=null;
        for (final Element elem : elems)
        {
            final String    tagName=(elem == null) ? null : elem.getTagName();
            if (isAttributeElement(elem, tagName))
            {
                final MBeanAttributeDescriptor    a=addAttribute(elem);
                if (a == null)
                    continue;
                if (attrs == null)
                    attrs = new ArrayList<MBeanAttributeDescriptor>(numAttrs);
                if (!attrs.add(a))
                    continue;    // debug breakpoint
            }
            else
                handleUnknownAttributeElement(elem, tagName);
        }

        return attrs;
    }

    public List<MBeanAttributeDescriptor> extractAttributes (Element elem) throws Exception
    {
        return (elem == null) ? null : extractAttributes(DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE));
    }

    public boolean isOperationElement (final Element elem, final String tagName)
    {
        return (elem != null) && MBeanOperationDescriptor.OPER_ELEM_NAME.equalsIgnoreCase(tagName);
    }

    public List<MBeanOperationDescriptor> extractOperations (final Collection<? extends Element> elems) throws Exception
    {
        final int    numOpers=(elems == null) ? 0 : elems.size();
        if (numOpers <= 0)
            return null;

        List<MBeanOperationDescriptor>    opers=null;
        for (final Element elem : elems)
        {
            final String    tagName=(elem == null) ? null : elem.getTagName();
            if (isOperationElement(elem, tagName))
            {
                final MBeanOperationDescriptor    o=addOperation(elem);
                if (o == null)
                    continue;
                if (opers == null)
                    opers = new ArrayList<MBeanOperationDescriptor>(numOpers);
                if (!opers.add(o))
                    continue;    // debug breakpoint
            }
            else
                handleUnknownAttributeElement(elem, tagName);
        }

        return opers;
    }

    public List<MBeanOperationDescriptor> extractOperations (Element elem) throws Exception
    {
        return (elem == null) ? null : extractOperations(DOMUtils.extractAllNodes(Element.class, elem, Node.ELEMENT_NODE));
    }
    // returns ONLY the added (!) attributes/operations (if any)
    public Map.Entry<Collection<MBeanAttributeDescriptor>,Collection<MBeanOperationDescriptor>> addChildren (final NodeList attrs) throws Exception
    {
        final int                                numAttrs=(null == attrs) ? 0 : attrs.getLength();
        Collection<MBeanAttributeDescriptor>    al=null;
        Collection<MBeanOperationDescriptor>    ol=null;
        for (int    aIndex=0; aIndex < numAttrs; aIndex++)
        {
            final Node    n=attrs.item(aIndex);
            if ((null == n) || (n.getNodeType() != Node.ELEMENT_NODE))
                continue;

            final Element    elem=(Element) n;
            final String    tagName=elem.getTagName();
            if (isAttributeElement(elem, tagName))
            {
                final MBeanAttributeDescriptor    a=addAttribute(elem);
                if (a != null)
                {
                    if (null == al)
                        al = new LinkedList<MBeanAttributeDescriptor>();
                    al.add(a);
                }
            }
            else if (isOperationElement(elem, tagName))
            {
                final MBeanOperationDescriptor    d=addOperation(elem);
                if (d != null)
                {
                    if (null == ol)
                        ol = new LinkedList<MBeanOperationDescriptor>();
                    ol.add(d);
                }
            }
            else
                handleUnknownAttributeElement(elem, tagName);
        }

        if (((al != null) && (al.size() > 0))
         || ((ol != null) && (ol.size() > 0)))
            return new MapEntryImpl<Collection<MBeanAttributeDescriptor>,Collection<MBeanOperationDescriptor>>(al,ol);

        return null;
    }
    /* NOTE: does not reset contents prior to parsing nor does it check
     * the element name
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public MBeanEntryDescriptor fromXml (final Element root) throws Exception
    {
        if (null == root)
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(getClass(), "fromXml") + " no " + Element.class.getName() + " instance");

        setClassName(root);
        setObjectName(root);
        setDependsName(root);
        setDescription(root);

        final Collection<? extends Element>        children=DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
        if ((children != null) && (children.size() > 0))
        {
            final String    attrsTag=getAttributesElementName(), opersTag=getOperationsElementName();
            for (final Element elem : children)
            {
                final String    tagName=(elem == null) ? null : elem.getTagName();
                if (attrsTag.equalsIgnoreCase(tagName))
                    extractAttributes(elem);
                else if (opersTag.equalsIgnoreCase(tagName))
                    extractOperations(elem);
                else
                    handleUnknownAttributeElement(elem, tagName);
            }
        }

        return this;
    }

    public MBeanEntryDescriptor (final Element elem) throws Exception
    {
        final MBeanEntryDescriptor    inst=fromXml(elem);
        if (inst != this)
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + "[" + DOMUtils.toString(elem) + "] mismatched XML instances");
    }

    public static final String    ATTRS_ELEM_NAME="attributes";
    public String getAttributesElementName ()
    {
        return ATTRS_ELEM_NAME;
    }

    public List<Element> createAttributes (final Document doc, final boolean fetchValues, final boolean includeNulls)
        throws Exception
    {
        final Collection<? extends MBeanAttributeDescriptor>    attrs=getAttributes();
        final int                                                numAttrs=(attrs == null) ? 0 : attrs.size();
        if (numAttrs <= 0)
            return null;

        List<Element>    elems=null;
        for (final MBeanAttributeDescriptor a : attrs)
        {
            final Element    elem=(null == a) ? null : a.toXml(doc, fetchValues, includeNulls);
            if (elem == null)
                continue;

            if (elems == null)
                elems = new ArrayList<Element>(numAttrs);
            if (!elems.add(elem))
                continue;    // debug breakpoint
        }

        return elems;
    }

    public static final String    OPERS_ELEM_NAME="operations";
    public String getOperationsElementName ()
    {
        return OPERS_ELEM_NAME;
    }

    public List<Element> createOperations (final Document doc, final boolean fetchParams) throws Exception
    {
        final Collection<? extends MBeanOperationDescriptor>    opers=getOperations();
        final int                                                numOpers=(opers == null) ? 0 : opers.size();
        if (numOpers <= 0)
            return null;

        List<Element>    elems=null;
        for (final MBeanOperationDescriptor o : opers)
        {
            final Element    elem=(o == null) ? null : o.toXml(doc, fetchParams);
            if (elem == null)
                continue;

            if (elems == null)
                elems = new ArrayList<Element>(numOpers);
            if (!elems.add(elem))
                continue;    // debug breakpoint
        }

        return elems;
    }

    public Element toXml (final Document    doc,
                          final boolean        fetchAttributes,
                          final boolean        fetchValues,
                          final boolean        includeNulls,
                          final boolean        fetchOperations,
                          final boolean        fetchParams)
        throws Exception
    {
        final Element    root=doc.createElement(getMBeanElementName());

        DOMUtils.addNonEmptyAttribute(root, CODE_ATTR, getClassName());
        DOMUtils.addNonEmptyAttribute(root, NAME_ATTR, getObjectName());
        DOMUtils.addNonEmptyAttribute(root, DEPENDS_ATTR, getDependsName());
        DOMUtils.addNonEmptyAttribute(root, DESC_ATTR, getDescription());

        final Collection<? extends Element>    attrs=
            fetchAttributes ? createAttributes(doc, fetchValues, includeNulls) : null;
        final Element                        rootAttrs=
            DOMUtils.createOptionalRoot(doc, getAttributesElementName(), attrs);
        if (rootAttrs != null)
            root.appendChild(rootAttrs);

        final Collection<? extends Element>    opers=
            fetchOperations ? createOperations(doc, fetchParams) : null;
        final Element                        rootOpers=
            DOMUtils.createOptionalRoot(doc, getOperationsElementName(), opers);
        if (rootOpers != null)
            root.appendChild(rootOpers);

        return root;
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        return toXml(doc, true, true, true, true, true);
    }
    /**
     * Default {@link XmlValueInstantiator} for  {@link MBeanEntryDescriptor}-s
     */
    public static final XmlValueInstantiator<MBeanEntryDescriptor>    XMLINST=new XmlValueInstantiator<MBeanEntryDescriptor>() {
            /*
             * @see net.community.chest.dom.transform.XmlValueInstantiator#fromXml(org.w3c.dom.Element)
             */
            @Override
            public MBeanEntryDescriptor fromXml (Element elem) throws Exception
            {
                return (null == elem) ? null : new MBeanEntryDescriptor(elem);
            }
        };

    public static final <D extends MBeanEntryDescriptor> Collection<ElementIndicatorExceptionContainer> updateMBeans (
            final Collection<D>    org,
            final Class<D>        descClass,
            final NodeList        children,
            final String        tagName)
    {
        final int    numChildren=(null == children) /* OK if no children */ ? 0 : children.getLength();
        if (numChildren <= 0)    // OK if no children
            return null;

        Collection<ElementIndicatorExceptionContainer>    errs=null;
        if ((null == org) || (null == descClass) || (null == tagName) || (tagName.length() <= 0))
        {
            final ElementIndicatorExceptionContainer    ind=new ElementIndicatorExceptionContainer(new StandaloneElementImpl(), new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(MBeanEntryDescriptor.class, "updateMBeans") + " no collection/class/tag name specified"));
            errs = new LinkedList<ElementIndicatorExceptionContainer>();
            errs.add(ind);
            return errs;
        }

        for (int    cIndex=0; cIndex < numChildren; cIndex++)
        {
            final Node    n=children.item(cIndex);
            if ((null == n) || (n.getNodeType() != Node.ELEMENT_NODE))
                continue;

            final Element    elem=(Element) n;
            final String    eName=elem.getTagName();
            if (!tagName.equalsIgnoreCase(eName))
                continue;    // ignore elements not having specified tag

            try
            {
                final D    descInstance=descClass.newInstance();
                descInstance.fromXml(elem);
                org.add(descInstance);
            }
            catch(Exception e)
            {
                final ElementIndicatorExceptionContainer    ind=new ElementIndicatorExceptionContainer(elem, e);
                if (null == errs)
                    errs = new LinkedList<ElementIndicatorExceptionContainer>();
                errs.add(ind);
            }
        }

        return errs;
    }

    public static final <D extends MBeanEntryDescriptor> Collection<ElementIndicatorExceptionContainer> updateMBeans (
            final Collection<D>    org,
            final Class<D>        descClass,
            final Element        root,
            final String        tagName)
    {
        return (null == root) /* OK if no root */ ? null : updateMBeans(org, descClass, root.getChildNodes(), tagName);

    }
    public static final <D extends MBeanEntryDescriptor> Collection<ElementIndicatorExceptionContainer> updateMBeans (
            final Collection<D>    org,
            final Class<D>        descClass,
            final Document        doc,
            final String        tagName)
    {
        return updateMBeans(org, descClass, (null == doc) ? null : doc.getDocumentElement(), tagName);
    }

    public static final Collection<ElementIndicatorExceptionContainer> updateDefaultMBeans (final Collection<MBeanEntryDescriptor> org, final NodeList children)
    {
        return updateMBeans(org, MBeanEntryDescriptor.class, children, MBEAN_ELEM_NAME);
    }

    public static final Collection<ElementIndicatorExceptionContainer> updateDefaultMBeans (final Collection<MBeanEntryDescriptor> org, final Element root)
    {
        return updateMBeans(org, MBeanEntryDescriptor.class, root, MBEAN_ELEM_NAME);
    }

    public static final Collection<ElementIndicatorExceptionContainer> updateDefaultMBeans (final Collection<MBeanEntryDescriptor> org, final Document doc)
    {
        return updateMBeans(org, MBeanEntryDescriptor.class, doc, MBEAN_ELEM_NAME);
    }

    public static final Collection<ElementIndicatorExceptionContainer> updateDefaultMBeans (final Collection<MBeanEntryDescriptor> org, final InputStream in) throws Exception
    {
        if (null == in)
            throw new IOException(ClassUtil.getExceptionLocation(MBeanEntryDescriptor.class, "updateDefaultMBeans") + " no input stream to extract XML from");

        return updateDefaultMBeans(org, DOMUtils.loadDocument(in));
    }

    public static final Collection<ElementIndicatorExceptionContainer> updateDefaultMBeans (final Collection<MBeanEntryDescriptor> org, final String filePath) throws Exception
    {
        if ((null == filePath) || (filePath.length() <= 0))
            throw new IOException(ClassUtil.getArgumentsExceptionLocation(MBeanEntryDescriptor.class, "updateDefaultMBeans", filePath) + " null/empty file path to read from");

        return updateDefaultMBeans(org, DOMUtils.loadDocument(filePath));
    }

    public static final Collection<MBeanEntryDescriptor> readMBeans (final NodeList children, final JMXErrorHandler eh) throws Exception
    {
        final int        numChildren=(null == children) /* OK if no children */ ? 0 : children.getLength();
        if (numChildren <= 0)    // OK if no children
            return null;

        final Collection<MBeanEntryDescriptor>        desc=new LinkedList<MBeanEntryDescriptor>();
        final Collection<ElementIndicatorExceptionContainer>    errs=updateDefaultMBeans(desc, children);
        if ((errs != null) && (errs.size() > 0))
        {
            for (final ElementIndicatorExceptionContainer ei : errs)
            {
                final Element    elem=(null == ei) ? null : ei.getObjectValue();
                final Throwable    t=(null == ei) ? null : ei.getCause();
                if ((null == elem) || (null == t))
                    continue;

                if (eh != null)
                    eh.mbeanError(null, "readMBeans() failed to read from element=" + DOMUtils.toString(elem), t);
                else
                {
                    if (t instanceof Exception)
                        throw (Exception) t;
                    else
                        throw new Exception(t.getClass().getName() + " while read from element=" + DOMUtils.toString(elem) + ": " + t.getMessage());
                }
            }
        }

        return desc;
    }

    public static final Collection<MBeanEntryDescriptor> readMBeans (final Element    root, final JMXErrorHandler eh) throws Exception
    {
        return (null == root) /* OK if no root */ ? null : readMBeans(root.getChildNodes(), eh);
    }

    public static final Collection<MBeanEntryDescriptor> readMBeans (final Document    doc, final JMXErrorHandler eh) throws Exception
    {
        return readMBeans((null == doc) ? null : doc.getDocumentElement(), eh);
    }

    public static final Collection<MBeanEntryDescriptor> readMBeans (final InputStream in, final JMXErrorHandler eh) throws Exception
    {
        if (null == in)
            throw new IOException("updateDefaultMBeans() no " + InputStream.class.getName() + " instance to extract XML from");

        return readMBeans(DOMUtils.loadDocument(in), eh);
    }

    public static final Collection<MBeanEntryDescriptor> readMBeans (final String filePath, final JMXErrorHandler eh) throws Exception
    {
        if ((null == filePath) || (filePath.length() <= 0))
            throw new IOException("readMBeans(" +  filePath + ") null/empty file path to read from");

        return readMBeans(DOMUtils.loadDocument(filePath), eh);
    }

    public static final Collection<MBeanEntryDescriptor> buildMBeansDescriptors (final Map<String,? extends Collection<? extends MBeanAttributeDescriptor>>    attrsMap) throws Exception
    {
        final Collection<? extends Map.Entry<String,? extends Collection<? extends MBeanAttributeDescriptor>>>    attrsSet=
            ((null == attrsMap) || (attrsMap.size() <= 0)) ? null : attrsMap.entrySet();
        final int    numEntries=(null == attrsSet) ? 0 : attrsSet.size();
        if (numEntries <= 0)
            return null;

        final Collection<MBeanEntryDescriptor>    descs=new ArrayList<MBeanEntryDescriptor>(numEntries);
        for (final Map.Entry<String,? extends Collection<? extends MBeanAttributeDescriptor>> ae : attrsSet)
        {
            final String                                            mbName=(null == ae) ? null : ae.getKey();
            final Collection<? extends MBeanAttributeDescriptor>    al=(null == ae) ? null : ae.getValue();
            if ((null == mbName) || (mbName.length() <= 0)
             || (null == al) || (al.size() <= 0))
                continue;

            final MBeanEntryDescriptor    d=new MBeanEntryDescriptor(mbName);
            d.addAttributes(al);
            descs.add(d);
        }

        return descs;
    }
    /**
     * @param descsMap Original {@link Map} whose key=MBean name, value=the
     * currently associated {@link MBeanEntryDescriptor}
     * @param mbNames The names of the MBeans whose attributes are to be
     * processed from the supplied attributes map
     * @param madMap A {@link Map} whose key=the MBean name and the value=a
     * {@link Collection} of {@link MBeanAttributeDescriptor} each describing
     * an attribute of the associated MBean
     * @return The same as the original map with each {@link MBeanEntryDescriptor}
     * entry's attributes set according to the result in the attributes map.
     * If any MBean in the supplied names {@link Collection} does not appear
     * in the attributes map or has no attributes, then it is <U>removed</U>
     * from the {@link MBeanEntryDescriptor}-s map.
     */
    public static final Map<String,MBeanEntryDescriptor> updateDescriptorsMap (
                    final Map<String,MBeanEntryDescriptor>                    descsMap,
                    final Collection<String>                                mbNames,
                    final Map<String,Collection<MBeanAttributeDescriptor>>    madMap)
    {
        if ((null == descsMap) || (descsMap.size() <= 0)
         || (null == mbNames) || (mbNames.size() <= 0))
            return descsMap;

        for (final String n : mbNames)
        {
            if ((null == n) || (n.length() <= 0))
                continue;

            final Collection<MBeanAttributeDescriptor>    attrs=
                ((null == madMap) || (madMap.size() <= 0)) ? null : madMap.get(n);

            final MBeanEntryDescriptor    d;
            if ((attrs != null) && (attrs.size() > 0))
            {
                if ((d=descsMap.get(n)) != null)    // should not be otherwise
                    d.setAttributes(attrs);
            }
            else    // remove descriptors that have no attributes attached
                d = descsMap.remove(n);
        }

        return descsMap;
    }

    public static final Map<String,Collection<String>> buildAttributesMap (final Collection<MBeanEntryDescriptor> descs)
    {
        final int    numDescs=(null == descs) ? 0 : descs.size();
        if (numDescs <= 0)
            return null;

        final Map<String,Collection<String>>    mbMap=new TreeMap<String,Collection<String>>();
           for (final MBeanEntryDescriptor d : descs)
           {
               final String                                            n=
                    (null == d) ? null : d.getObjectName();
               final Collection<? extends MBeanAttributeDescriptor>    al=
                    ((null == n) || (n.length() <= 0)) ? null : d.getAttributes();
               final int                                                numAttrs=
                    (null == al) ? 0 : al.size();
               if (numAttrs <= 0)
                   continue;

               Collection<String>    aNames=mbMap.get(n);
               for (final MBeanAttributeDescriptor ad : al)
               {
                   final String    an=(null == ad) ? null : ad.getName();
                   if ((null == an) || (an.length() <= 0))
                       continue;

                   if (null == aNames)
                   {
                       aNames = new LinkedList<String>();
                       mbMap.put(n, aNames);
                   }
                   aNames.add(an);
               }
           }

           return mbMap;
    }

    public static Map<String,MBeanEntryDescriptor> updateMBeansAttributesValues (
            final Map<String,MBeanEntryDescriptor> descsMap, final Map<String,Collection<Map.Entry<MBeanAttributeInfo,Object>>>    mbValues)
    {
        final Collection<? extends Map.Entry<String,? extends Collection<? extends Map.Entry<? extends MBeanAttributeInfo,?>>>> inSet=
            ((null == mbValues) || (mbValues.size() <= 0)) ? null : mbValues.entrySet();
        if ((null == inSet) || (inSet.size() <= 0))
            return null;

        for (final Map.Entry<String,? extends Collection<? extends Map.Entry<? extends MBeanAttributeInfo,?>>> ev : inSet)
        {
            final String    mbName=(null == ev) ? null : ev.getKey();
            if ((null == mbName) || (mbName.length() <= 0))
                continue;

            final MBeanEntryDescriptor    d=descsMap.get(mbName);
            if (null == d)
                continue;

            final Collection<? extends Map.Entry<? extends MBeanAttributeInfo,?>>    vl=(null == ev) ? null : ev.getValue();
            if ((null == vl) || (vl.size() <= 0))    // rempove descriptors that have no values
            {
                descsMap.remove(mbName);
                continue;
            }

            final Map<String,MBeanAttributeDescriptor>    aMap=d.getAttributesMap();
            if ((null == aMap) || (aMap.size() <= 0))    // rempove descriptors that have no attributes
            {
                descsMap.remove(mbName);
                continue;
            }

            for (final Map.Entry<? extends MBeanAttributeInfo,?> ve : vl)
            {
                final MBeanAttributeInfo    aInfo=(null == ve) ? null : ve.getKey();
                final String                aName=(null == aInfo) ? null : aInfo.getName(),
                                            aType=(null == aInfo) ? null : aInfo.getType();
                if ((null == aName) || (aName.length() <= 0)
                 || (null == aType) || (aType.length() <= 0))
                    continue;

                final MBeanAttributeDescriptor    ad=aMap.get(aName);
                if (null == ad)
                    continue;

                ad.setType(aType);    // refresh the type
                ad.setValue(ve.getValue());
            }
        }

        return descsMap;
    }
}
