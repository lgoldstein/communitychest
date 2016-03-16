package net.community.chest.apache.maven.helpers;

import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.CoVariantReturn;
import net.community.chest.dom.DOMUtils;
import net.community.chest.reflect.ClassUtil;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 8, 2007 4:22:04 PM
 */
public class BuildProject extends BuildTargetDetails {
    /**
     *
     */
    private static final long serialVersionUID = 7543730569823267741L;
    public BuildProject ()
    {
        super();
    }

    private ParentTargetDetails    _parentTarget;
    /**
     * @return the parent target - null if none specified/set
     */
    public ParentTargetDetails getParentTarget ()
    {
        return _parentTarget;
    }

    public void setParentTarget (ParentTargetDetails parentTarget)
    {
        _parentTarget = parentTarget;
    }

    public static final String    PROJECT_ELEMENT_NAME="project";
    /**
     * <P>Copyright 2008 as per GPLv2</P>
     *
     * <P>Types of {@link DependenciesList} available</P>
     *
     * @author Lyor G.
     * @since Aug 14, 2008 9:17:38 AM
     */
    public static enum DepenencyListType {
        PROJECT,
        MANAGEMENT;

        private static DepenencyListType[]    _values    /* =null */;
        public static final synchronized DepenencyListType[] getValues ()
        {
            if (null == _values)
                _values = values();
            return _values;
        }
    }

    public static final String    DEFAULT_POM_FILE_NAME="pom.xml";

    private Map<DepenencyListType,DependenciesList>    _depsMap    /* =null */;
    public Map<DepenencyListType,DependenciesList> getDependenciesMap ()
    {
        return _depsMap;
    }

    public void setDependenciesMap (Map<DepenencyListType,DependenciesList> m)
    {
        _depsMap = m;
    }

    protected Map<DepenencyListType,DependenciesList> createDependenciesMap (final String callerId)
    {
        Map<DepenencyListType,DependenciesList>    dm=getDependenciesMap();
        if (null == dm)
        {
            setDependenciesMap(new EnumMap<DepenencyListType,DependenciesList>(DepenencyListType.class));
            if (null == (dm=getDependenciesMap()))
                throw new IllegalStateException("createDependenciesMap(" + callerId + ") no instance created though set");
        }

        return dm;
    }

    public DependenciesList getDependenciesList (final DepenencyListType t)
    {
        final Map<DepenencyListType,? extends DependenciesList>    dm=
            (null == t) ? null : getDependenciesMap();
        if ((null == t) || (null == dm) || (dm.size() <= 0))
            return null;

        return dm.get(t);
    }
    // returns previous instance (if any)
    public DependenciesList setDependenciesList (final DepenencyListType t, final DependenciesList l)
    {
        if ((null == t) || (null == l))
            return null;

        final Map<DepenencyListType,DependenciesList>    dm=createDependenciesMap("setDependenciesList");
        return dm.put(t, l);
    }

    public DependenciesList getProjectDependencies ()
    {
        return getDependenciesList(DepenencyListType.PROJECT);
    }

    public void setProjectDependencies (DependenciesList deps)
    {
        setDependenciesList(DepenencyListType.PROJECT, deps);
    }

    public DependenciesList getManagerDependencies ()
    {
        return getDependenciesList(DepenencyListType.MANAGEMENT);
    }

    public void setManagerDependencies (DependenciesList deps)
    {
        setDependenciesList(DepenencyListType.MANAGEMENT, deps);
    }

    protected DependenciesList createDependenciesList (final DepenencyListType t, final int initialSize, final String callerId)
    {
        if (null == t)
            return null;

        final Map<DepenencyListType,DependenciesList>    dm=createDependenciesMap(callerId);
        DependenciesList                                deps=dm.get(t);
        if (null == deps)
        {
            dm.put(t, new DependenciesList(initialSize));
            if (null == (deps=dm.get(t)))
                throw new IllegalStateException("createDependenciesList(" + callerId + ")[" + t + "] no instance created though set");
        }

        return deps;
    }

    protected DependenciesList createDependenciesList (final DepenencyListType t, final Element elem, final String callerId) throws Exception
    {
        DependenciesList    deps=getDependenciesList(t);
        if (null == deps)
        {
            deps = new DependenciesList(elem);
            setDependenciesList(t, deps);
        }
        else if (deps.fromXml(elem) != deps)
            throw new IllegalStateException("createDependenciesList(" + t + ")[" + callerId + "] mismatched re-constructed list instance");

        return deps;
    }

    public DependenciesList addDependency (final DepenencyListType t, final BuildDependencyDetails tgt)
    {
        DependenciesList    deps=getDependenciesList(t);
        if (tgt != null)
        {
            if (null == deps)
                deps = createDependenciesList(t, 10, "addDependency");
            deps.add(tgt);
        }

        return deps;
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BuildTargetDetails#clear()
     */
    @Override
    public void clear ()
    {
        final Map<DepenencyListType,? extends DependenciesList>                                dm=
            getDependenciesMap();
        final Collection<? extends Map.Entry<DepenencyListType,? extends DependenciesList>>    dml=
            ((null == dm) || (dm.size() <= 0)) ? null : dm.entrySet();
        if ((dml != null) && (dml.size() > 0))
        {
            for (final Map.Entry<DepenencyListType,? extends DependenciesList> lp : dml)
            {
                final DependenciesList    l=(null == lp) ? null : lp.getValue();
                if ((null == l) || (l.size() <= 0))
                    continue;
                l.clear();
            }
        }

        setParentTarget((ParentTargetDetails) null);

        super.clear();
    }

    public static final String    PARENT_ELEM_NAME="parent";
    public ParentTargetDetails setParentTarget (final Element elem) throws Exception
    {
        final ParentTargetDetails    tgt=new ParentTargetDetails(elem);
        setParentTarget(tgt);
        return tgt;
    }

    public static final String    DEPENDENCIES_ELEM_NAME="dependencies";
    public DependenciesList setProjectDependencies (final Element elem) throws Exception
    {
        return createDependenciesList(DepenencyListType.PROJECT, elem, DEPENDENCIES_ELEM_NAME);
    }

    public void handleUnknownDependencyManagementElement (final Element elem, final String tagName) throws Exception
    {
        if ((null == elem) || (null == tagName) || (tagName.length() <= 0))    // just so compiler does not complain about unreferenced parameter
            throw new DOMException(DOMException.HIERARCHY_REQUEST_ERR, ClassUtil.getArgumentsExceptionLocation(getClass(), "handleUnknownDependencyManagementElement", tagName) + " incomplete parameters");
    }

    public static final String    DEPENDENCY_MGR_ELEMENT_NAME="dependencyManagement";
    public void setDependencyManagement (final Element root) throws Exception
    {
        final Collection<? extends Element>    el=DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
        if ((null == el) || (el.size() <= 0))
            return;

        for (final Element elem : el)
        {
            final String    tagName=(null == elem) ? null : elem.getTagName();
            if (DEPENDENCIES_ELEM_NAME.equalsIgnoreCase(tagName))
                createDependenciesList(DepenencyListType.MANAGEMENT, elem, DEPENDENCY_MGR_ELEMENT_NAME);
            else
                handleUnknownDependencyManagementElement(elem, tagName);
        }
    }

    public static final String    DEVELOPERS_ELEMENT_NAME="developers";
    public void setDevelopers (final Element root) throws Exception
    {
        if (null == root)
            throw new NullPointerException("setDevelopers() no root element");
        // TODO implement "setDevelopers"
    }

    public static final String    BUILD_ELEMENT_NAME="build";
    public void setBuildConfiguration (final Element root) throws Exception
    {
        if (null == root)
            throw new NullPointerException("setBuildConfiguration() no root element");
        // TODO implement "setBuildConfiguration"
    }

    public static final String    PROFILES_ELEMENT_NAME="profiles";
    public void setProfiles (final Element root) throws Exception
    {
        if (null == root)
            throw new NullPointerException("setProfiles() no root element");
        // TODO implement "setProfiles"
    }

    public static final String    MODULES_ELEMENT_NAME="modules";
    public void setModules (final Element root) throws Exception
    {
        if (null == root)
            throw new NullPointerException("setModules() no root element");
        // TODO implement "setModules"
    }

    public static final String    REPORTING_ELEMENT_NAME="reporting";
    public void setReporting (final Element root) throws Exception
    {
        if (null == root)
            throw new NullPointerException("setReporting() no root element");
        // TODO implement "setReporting"
    }

    public static final Map<String,String> updateProperties (
            final Map<String,String> org, final Element root, final boolean allowDuplicates)
        throws IllegalStateException
    {
        final Collection<? extends Element>    el=
            DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE);
        if ((null == el) || (el.size() <= 0))
            return org;

        Map<String,String>    ret=org;
        for (final Element elem : el)
        {
            final String    propName=(null == elem) ? null : elem.getTagName();
            if ((null == propName) || (propName.length() <= 0))
                continue;    // should not happen

            if (null == ret)
                ret = new TreeMap<String,String>(String.CASE_INSENSITIVE_ORDER);

            final String    propVal=DOMUtils.getElementStringValue(elem),
                            prevVal=ret.put(propName, (null == propVal) ? "" : propVal);
            if ((prevVal != null) && (!allowDuplicates))
                throw new IllegalStateException("updateProperties(" + propName + ") multiple values: old=" + prevVal + "/new=" + propVal);
        }

        return ret;
    }

    public static final Map<String,String> extractProperties (final Element root) throws IllegalStateException
    {
        return updateProperties(null, root, false);
    }

    private Map<String,String>    _props;
    public Map<String,String> getProperties ()
    {
        return _props;
    }

    public void setProperties (Map<String,String> pm)
    {
        _props = pm;
    }

    public static final String    PROPERTIES_ELEMENT_NAME="properties";
    public Map<String,String> setProperties (final Element root)
    {
        if (null == root)
            throw new NullPointerException("setProperties() no root element");

        final Map<String,String>    pm=extractProperties(root);
        if (null == pm)    // debug breakpoint
            return null;

        setProperties(pm);
        return pm;
    }
    /*
     * @see net.community.chest.apache.maven.helpers.BuildTargetDetails#handleUnknownElement(org.w3c.dom.Element, java.lang.String)
     */
    @Override
    public void handleUnknownElement (final Element elem, final String tagName) throws Exception
    {
        if (PARENT_ELEM_NAME.equalsIgnoreCase(tagName))
            setParentTarget(elem);
        else if (DEPENDENCIES_ELEM_NAME.equalsIgnoreCase(tagName))
            setProjectDependencies(elem);
        else if (DEPENDENCY_MGR_ELEMENT_NAME.equalsIgnoreCase(tagName))
            setDependencyManagement(elem);
        else if (DEVELOPERS_ELEMENT_NAME.equalsIgnoreCase(tagName))
            setDevelopers(elem);
        else if (BUILD_ELEMENT_NAME.equalsIgnoreCase(tagName))
            setBuildConfiguration(elem);
        else if (PROFILES_ELEMENT_NAME.equalsIgnoreCase(tagName))
            setProfiles(elem);
        else if (MODULES_ELEMENT_NAME.equalsIgnoreCase(tagName))
            setModules(elem);
        else if (REPORTING_ELEMENT_NAME.equalsIgnoreCase(tagName))
            setReporting(elem);
        else if (PROPERTIES_ELEMENT_NAME.equalsIgnoreCase(tagName))
            setProperties(elem);
        else    // TODO add repositories...
            super.handleUnknownElement(elem, tagName);
    }
    /* <B>Note:</B> {@link #clear()}-s the contents <U>before</U> anything else
     * @see net.community.chest.dom.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    @Override
    @CoVariantReturn
    public BuildProject fromXml (final Element root) throws Exception
    {
        return getClass().cast(super.fromXml(root));
    }

    public BuildProject (final Element elem) throws Exception
    {
        final BuildProject    inst=fromXml(elem);
        if (inst != this)
            throw new IllegalStateException(ClassUtil.getConstructorExceptionLocation(getClass()) + " mismatched instances");
    }

    public BuildProject fromDocument (final Document doc) throws Exception
    {
        return fromXml((null == doc) ? null : doc.getDocumentElement());
    }

    public BuildProject (final Document doc) throws Exception
    {
        final BuildProject    proj=fromDocument(doc);
        if (proj != this)
            throw new IllegalStateException(ClassUtil.getConstructorArgumentsExceptionLocation(getClass(), Document.class.getName()) + " mismatched recovered XML instance");
    }

    public BuildProject fromInputStream (final InputStream in) throws Exception
    {
        return fromDocument(DOMUtils.loadDocument(in));
    }

    public BuildProject (final InputStream in) throws Exception
    {
        final BuildProject    proj=fromInputStream(in);
        if (proj != this)
            throw new IllegalStateException(ClassUtil.getConstructorArgumentsExceptionLocation(getClass(), InputStream.class.getName()) + " mismatched recovered XML instance");
    }

    public BuildProject fromFilepath (final String filePath) throws Exception
    {
        return fromDocument(DOMUtils.loadDocument(filePath));
    }

    public BuildProject (final String path) throws Exception
    {
        final BuildProject    proj=fromFilepath(path);
        if (proj != this)
            throw new IllegalStateException(ClassUtil.getConstructorArgumentsExceptionLocation(getClass(), path) + " mismatched recovered XML instance");
    }

    public BuildProject fromURL (final URL url) throws Exception
    {
        return fromDocument(DOMUtils.loadDocument(url));
    }

    public BuildProject (final URL url) throws Exception
    {
        final BuildProject    proj=fromURL(url);
        if (proj != this)
            throw new IllegalStateException(ClassUtil.getConstructorArgumentsExceptionLocation(getClass(), url) + " mismatched recovered XML instance");
    }
    /*
     * @see net.community.chest.dom.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    @Override
    public Element toXml (Document doc) throws Exception
    {
        // TODO implement toXml
        throw new UnsupportedOperationException(ClassUtil.getExceptionLocation(getClass(), "toXml") + " N/A");
    }
}
