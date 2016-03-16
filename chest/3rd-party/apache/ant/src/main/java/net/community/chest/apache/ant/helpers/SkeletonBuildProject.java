package net.community.chest.apache.ant.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.lang.StringUtil;
import net.community.chest.reflect.ClassUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Represents basic structure of an ANT build XML project file</P>
 *
 * @author Lyor G.
 * @since Jul 19, 2007 1:54:53 PM
 */
public class SkeletonBuildProject extends BaseExecutableElement {
    /**
     *
     */
    private static final long serialVersionUID = 1303538358242466934L;
    /**
     * Default empty constructor
     */
    public SkeletonBuildProject ()
    {
        super();
    }

    private String    _defaultTarget    /* =null */;
    /**
     * @return Default target name
     */
    public String getDefaultTarget ()
    {
        return _defaultTarget;
    }

    public void setDefaultTarget (String defaultTarget)
    {
        _defaultTarget = defaultTarget;
    }

    private Collection<SkeletonBuildTarget>    _targets;
    /**
     * @return {@link Collection} of {@link SkeletonBuildTarget}-s in the project
     */
    public Collection<SkeletonBuildTarget> getTargets ()
    {
        return _targets;
    }

    public void setTargets (Collection<SkeletonBuildTarget> targets)
    {
        _targets = targets;
    }
    /**
     * Adds (non-null) target to the current targets list
     * @param target {@link SkeletonBuildTarget} to be added - ignored if null
     * @return updated targets list - may be null/empty if null/empty to
     * begin with and no target added
     */
    public Collection<SkeletonBuildTarget> addTarget (final SkeletonBuildTarget target)
    {
        try
        {
            @SuppressWarnings("unchecked")
            final Collection<SkeletonBuildTarget>    tgts=CollectionsUtils.addMember(getTargets(), target, LinkedList.class);
            setTargets(tgts);
            return getTargets();
        }
        catch(Exception e)    // should not happen
        {
            if (e instanceof RuntimeException)
                throw (RuntimeException) e;

            throw new IllegalStateException("addTarget(" + target + ") " + e.getClass().getName() + ": " + e.getMessage());
        }
    }
    /**
     * Resets internal fields to null-s
     */
    public void clear ()
    {
        setName(null);
        setDefaultTarget(null);
        setTargets(null);
    }
    /*
     * @see java.lang.Object#clone()
     */
    @Override
    @CoVariantReturn
    public SkeletonBuildProject clone () throws CloneNotSupportedException
    {
        final SkeletonBuildProject                cpyProj=getClass().cast(super.clone());
        final Collection<SkeletonBuildTarget>    tgtList=cpyProj.getTargets();
        if (tgtList != null)
            cpyProj.setTargets(CollectionsUtils.duplicateCollection(tgtList, new LinkedList<SkeletonBuildTarget>()));

        return cpyProj;
    }

    public static final String    DESCRIPTION_ELEMNAME="description";
    public String setDescription (final Element elem) throws Exception
    {
        if (null == elem)
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getExceptionLocation(getClass(), "setDescription") + " no " + Element.class.getName() + " instance");

        final String    desc=DOMUtils.getElementStringValue(elem);
        if ((desc != null) && (desc.length() > 0))
            setDescription(desc);

        return desc;
    }
    /**
     * Adds the extract targets from the "target" XML elements in the nodes list
     * @param projNodes nodes list to look for "target" XML {@link Element}-s - may
     * be null/empty
     * @return current {@link Collection} of {@link SkeletonBuildTarget}-s in the
     * project - may be null/empty if no changes made and null argument
     * @throws Exception if bad format
     */
    public Collection<SkeletonBuildTarget> addTargets (final NodeList projNodes) throws Exception
    {
        final int    numNodes=(null == projNodes) ? 0 : projNodes.getLength();
        for (int    nIndex=0; nIndex < numNodes; nIndex++)
        {
            final Node    n=projNodes.item(nIndex);
            if ((null == n)
             || (n.getNodeType() != Node.ELEMENT_NODE))
                continue;

            // ignore non-target elements
            final Element    elem=(Element) n;
            final String    tagName=elem.getTagName();
            if (SkeletonBuildTarget.TARGET_ELEMNAME.equals(tagName))
                addTarget(new SkeletonBuildTarget(elem));
            else if (DESCRIPTION_ELEMNAME.equals(tagName))
                setDescription(elem);
        }

        return getTargets();
    }

    public static final String    PROJECT_ELEMNAME="project",
                                    NAME_ATTR="name",
                                    DEFAULT_ATTR="default";
    /*
     * <B>Note:</B> {@link #clear()}-s the contents <U>before</U> anything else
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    public SkeletonBuildProject fromXml (final Element root) throws Exception
    {
        if (null == root)
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "No root element to extract data from");

        clear();

        // make sure this is the "project" element
        {
            final String    eName=root.getNodeName();
            if (!PROJECT_ELEMNAME.equals(eName))
                throw new DOMException(DOMException.NAMESPACE_ERR, "Unexpected root element name: " + eName);
        }

        // extract project name and default target
        {
            final String    projName=root.getAttribute(NAME_ATTR);
            if ((null == projName) || (projName.length() <= 0))
                throw new DOMException(DOMException.NOT_FOUND_ERR, "Missing project '" + NAME_ATTR + "' attribute");
            setName(projName);

            final String    defTarget=root.getAttribute(DEFAULT_ATTR);
            if ((null == defTarget) || (defTarget.length() <= 0))
                throw new DOMException(DOMException.NOT_FOUND_ERR, "Missing project '" + DEFAULT_ATTR + "' target name attribute");
            setDefaultTarget(defTarget);
        }

        addTargets(root.getChildNodes());
        return this;
    }

    public SkeletonBuildProject (Element elem) throws Exception
    {
        this();

        final SkeletonBuildProject    proj=fromXml(elem);
        if (proj != this)
            throw new IllegalStateException("Mismatched recovered XML instance");
    }
    /**
     * <B>Note:</B> {@link #clear()}-s the contents <U>before</U> anything else
     * @param doc {@link Document} whose root element will be used
     * @return initialized instance - should be same as <code>this</code>
     * @throws Exception if null/empty document/root element or bad format
     * @see #fromXml(Element)
     */
    public SkeletonBuildProject fromDocument (final Document doc) throws Exception
    {
        return fromXml((null == doc) ? null : doc.getDocumentElement());
    }

    public SkeletonBuildProject (Document doc) throws Exception
    {
        this();

        final SkeletonBuildProject    proj=fromDocument(doc);
        if (proj != this)
            throw new IllegalStateException("Mismatched recovered XML instance");
    }
    /**
     * @param in input stream to import XML from
     * @return initialized instance - should be same as <code>this</code>
     * @throws Exception unable to read from stream and import the XML
     */
    public SkeletonBuildProject fromInputStream (final InputStream in) throws Exception
    {
        if (null == in)
            throw new IOException("No input stream to extract XML from");

        final DocumentBuilderFactory    docFactory=DOMUtils.getDefaultDocumentsFactory();
        final DocumentBuilder            docBuilder=docFactory.newDocumentBuilder();
        return fromDocument(docBuilder.parse(in));
    }

    public SkeletonBuildProject (final InputStream in) throws Exception
    {
        final SkeletonBuildProject    proj=fromInputStream(in);
        if (proj != this)
            throw new IllegalStateException("Mismatched recovered XML instance");
    }
    /**
     * @return initialized instance - should be same as <code>this</code>
     * @param filePath XML file path to read from
     * @throws Exception unable to read from stream and import the XML
     */
    public SkeletonBuildProject fromFilepath (final String filePath) throws Exception
    {
        if ((null == filePath) || (filePath.length() <= 0))
            throw new IOException("Null/empty file path to read from");

        try(InputStream   fin=new FileInputStream(filePath)) {
            return fromInputStream(fin);
        }
    }

    public SkeletonBuildProject (final String filePath) throws Exception
    {
        final SkeletonBuildProject    proj=fromFilepath(filePath);
        if (proj != this)
            throw new IllegalStateException("<init>(path=" + filePath + ") mismatched recovered XML instance");
    }
    /**
     * @return initialized instance - should be same as <code>this</code>
     * @param f XML {@link File} path to read from
     * @throws Exception unable to read from stream and import the XML
     */
    public SkeletonBuildProject fromFile (final File f) throws Exception
    {
        if (null == f)
            throw new IOException("Null/empty file to read from");

        try(InputStream   fin=new FileInputStream(f)) {
            return fromInputStream(fin);
        }
    }

    public SkeletonBuildProject (final File f) throws Exception
    {
        final SkeletonBuildProject    proj=fromFile(f);
        if (proj != this)
            throw new IllegalStateException("<init>(file=" + f + ") mismatched recovered XML instance");
    }

    public Collection<SkeletonBuildTarget> addTargets (final Document doc, final Element root) throws Exception
    {
        final Collection<SkeletonBuildTarget>    tgtList=getTargets();
        if ((tgtList != null) && (tgtList.size() > 0))
        {
            for (final SkeletonBuildTarget tgt : tgtList)
            {
                if (null == tgt)    // should not happen
                    continue;

                final Element    tgtElem=tgt.toXml(doc);
                if (null == tgtElem)
                    throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, "addTargets(" + tgt.getName() + ") no XML " + Element.class.getName() + " created");

                root.appendChild(tgtElem);
            }
        }

        return tgtList;
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (final Document doc) throws Exception
    {
        final Element    root=doc.createElement(PROJECT_ELEMNAME);

        final String    projName=getName();
        if ((null == projName) || (projName.length() <= 0))
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Missing project '" + NAME_ATTR + "' attribute");
        root.setAttribute(NAME_ATTR, projName);

        final String    defTarget=getDefaultTarget();
        if ((null == defTarget) || (defTarget.length() <= 0))
            throw new DOMException(DOMException.NOT_FOUND_ERR, "Missing project '" + DEFAULT_ATTR + "' target name attribute");
        root.setAttribute(DEFAULT_ATTR, defTarget);

        DOMUtils.appendOptionalElement(doc, root, DESCRIPTION_ELEMNAME, getDescription());
        return root;
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (final Object obj)
    {
        if (this == obj)
            return true;
        if ((null == obj) || (!(obj instanceof SkeletonBuildProject)))
            return false;

        final SkeletonBuildProject    bp=(SkeletonBuildProject) obj;
        return (0 == StringUtil.compareDataStrings(getName(), bp.getName(), true))
            && (0 == StringUtil.compareDataStrings(getDefaultTarget(), bp.getDefaultTarget(), true))
            && CollectionsUtils.isSameMembers(getTargets(), bp.getTargets())
            ;
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return StringUtil.getDataStringHashCode(getName(), true)
             + StringUtil.getDataStringHashCode(getDefaultTarget(), true)
             + CollectionsUtils.getMembersHashCode(getTargets())
             ;
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return getName() + "[" + getDefaultTarget() + "]: " + getDescription();
    }
}
