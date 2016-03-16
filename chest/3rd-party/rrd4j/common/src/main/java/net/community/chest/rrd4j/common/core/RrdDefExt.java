package net.community.chest.rrd4j.common.core;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import net.community.chest.dom.DOMUtils;
import net.community.chest.dom.transform.XmlConvertible;
import net.community.chest.resources.SystemPropertiesResolver;
import net.community.chest.rrd4j.common.RrdUtils;
import net.community.chest.rrd4j.common.core.util.RrdTimestampValueStringInstantiator;
import net.community.chest.util.datetime.Duration;

import org.rrd4j.core.ArcDef;
import org.rrd4j.core.DsDef;
import org.rrd4j.core.RrdDef;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Jan 8, 2008 1:27:40 PM
 */
public class RrdDefExt extends RrdDef implements XmlConvertible<RrdDefExt> {
    public RrdDefExt (String path)
    {
        super(path);
    }

    public RrdDefExt (String path, long step)
    {
        super(path, step);
    }

    public RrdDefExt (String path, long startTime, long step)
    {
        super(path, startTime, step);
    }

    public RrdDefExt ()
    {
        this("'*!@$#%^()&'");    // because a null/empty path is not allowed
    }

    private DsDef[]    _dsDefs    /* =null */;
    /*
     * @see org.rrd4j.core.RrdDef#getDsDefs()
     */
    @Override
    public DsDef[] getDsDefs ()
    {
        if (null == _dsDefs)
            _dsDefs = super.getDsDefs();
        return _dsDefs;
    }
    /*
     * @see org.rrd4j.core.RrdDef#addDatasource(org.rrd4j.core.DsDef)
     */
    @Override
    public void addDatasource (DsDef dsDef)
    {
        super.addDatasource(dsDef);
        if (_dsDefs != null)
            _dsDefs = null;
    }
    /*
     * @see org.rrd4j.core.RrdDef#removeDatasources()
     */
    @Override
    public void removeDatasources ()
    {
        super.removeDatasources();
        if (_dsDefs != null)
            _dsDefs = null;
    }

    public DsDef addDatasource (Element elem) throws Exception
    {
        final DsDef    dsDef=(null == elem) ? null : new DsDefExt(elem);
        if (dsDef != null)
            addDatasource(dsDef);
        return dsDef;
    }

    private ArcDef[]    _arcDefs    /* =null */;
    /*
     * @see org.rrd4j.core.RrdDef#getArcDefs()
     */
    @Override
    public ArcDef[] getArcDefs ()
    {
        if (null == _arcDefs)
            _arcDefs = super.getArcDefs();
        return _arcDefs;
    }
    /*
     * @see org.rrd4j.core.RrdDef#addArchive(org.rrd4j.core.ArcDef)
     */
    @Override
    public void addArchive (ArcDef arcDef)
    {
        super.addArchive(arcDef);
        if (_arcDefs != null)
            _arcDefs = null;
    }
    /*
     * @see org.rrd4j.core.RrdDef#removeArchives()
     */
    @Override
    public void removeArchives ()
    {
        super.removeArchives();
        if (_arcDefs != null)
            _arcDefs = null;
    }

    public ArcDef addArchive (Element elem) throws Exception
    {
        final ArcDef    arcDef=(null == elem) ? null : new ArcDefExt(elem);
        if (arcDef != null)
            addArchive(arcDef);
        return arcDef;
    }
    // any returned non-null object is added to the addDataSourcesAndArchives returned collection
    protected Object handleUnknownElement (Element elem, String tagName) throws Exception
    {
        // just so compiler does not complain about unreferenced parameters
        if ((null == elem) || (null == tagName) || (tagName.length() <= 0))
            throw new DOMException(DOMException.INVALID_STATE_ERR, "handleUnknownElement(" + tagName + ") incomplete arguments");

        throw new UnsupportedOperationException("handleUnknownElement(" + tagName + ")");
    }

    public Object addDefinitionElement (final Element elem) throws Exception
    {
        final String    tagName=elem.getTagName();
        if (DsDefExt.DSDEF_ELEM_NAME.equalsIgnoreCase(tagName))
            return addDatasource(elem);
        else if (ArcDefExt.ARCDEF_ELEM_NAME.equalsIgnoreCase(tagName))
            return addArchive(elem);
        else
            return handleUnknownElement(elem, tagName);
    }
    // members can be either DsDef(s) or ArcDef(s)
    public Collection<?> addDataSourcesAndArchives (final Collection<? extends Element> nodes) throws Exception
    {
        if ((null == nodes) || (nodes.size() <= 0))
            return null;

        Collection<Object>    ret=null;
        for (final Element elem : nodes)
        {
            final Object    o=(null == elem) ? null : addDefinitionElement(elem);
            if (null == o)
                continue;

            if (null == ret)
                ret = new LinkedList<Object>();
            ret.add(o);
        }

        return ret;
    }
    // members can be either DsDef(s) or ArcDef(s)
    public Collection<?> addDataSourcesAndArchives (Element root) throws Exception
    {
        return (null == root) ? null : addDataSourcesAndArchives(DOMUtils.extractAllNodes(Element.class, root, Node.ELEMENT_NODE));
    }

    public static final String    PATH_ATTR="path";
    public String setPath (Element elem) throws Exception
    {
        final String    val=elem.getAttribute(PATH_ATTR),
                        path=((null == val) || (val.length() <= 0)) ? null : SystemPropertiesResolver.SYSTEM.format(val);
        if ((path != null) && (path.length() > 0))
            setPath(path);
        return path;
    }

    public static final String    STEP_ATTR="step";
    public Long setStep (Element elem)
    {
        final String    val=elem.getAttribute(STEP_ATTR);
        if ((null == val) || (val.length() <= 0))
            return null;

        final long        s=RrdUtils.toRrdTime(Duration.fromTimespec(val));    // step is in seconds
        if (s <= 0L)
            throw new IllegalStateException("setStep(" + val + ") illegal value");

        setStep(s);
        return Long.valueOf(s);
    }

    public static final String    START_TIME_ATTR="startTime";
    public Long setStartTime (Element elem) throws Exception
    {
        final String    val=elem.getAttribute(START_TIME_ATTR);
        final Long        v=((null == val) || (val.length() <= 0)) ? null : RrdTimestampValueStringInstantiator.DEFAULT.newInstance(val);
        if (v != null)
            setStartTime(v.longValue());

        return v;
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#fromXml(org.w3c.dom.Element)
     */
    public RrdDefExt fromXml (Element elem) throws Exception
    {
        setPath(elem);
        setStep(elem);
        setStartTime(elem);

        addDataSourcesAndArchives(elem);
        return this;
    }

    public RrdDefExt (Element elem) throws Exception
    {
        this();

        if (this != fromXml(elem))
            throw new IllegalStateException("<init>(" + DOMUtils.toString(elem) + ") mismatched re-constructed instances");
    }
    /*
     * @see net.community.chest.dom.transform.XmlConvertible#toXml(org.w3c.dom.Document)
     */
    public Element toXml (Document doc) throws Exception
    {
        // TODO convert step to some time factor
        throw new UnsupportedOperationException("toXml() - TODO");
    }
    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString ()
    {
        return dump();
    }
    /**
     * Checks if a given {@link Collection} of {@link RrdDef}-s contains duplicate paths
     * @param <D> The {@link RrdDef} generic type
     * @param defs Original definitions - <B>Note:</B> definitions with
     * null/empty {@link RrdDef#getPath()} value(s) are ignored.
     * @return Definitions that have duplicate paths as a {@link Map} whose
     * key=path and value=a {@link Collection} of all definitions that have
     * this path (case <U>insensitive</U> for more robust restriction).
     */
    public static final <D extends RrdDef> Map<String,? extends Collection<? extends D>> checkDuplicatePaths (final Collection<? extends D> defs)
    {
        if ((null == defs) || (defs.size() <= 0))
            return null;

        Map<String,Collection<D>>    res=null;
        final Map<String,D>            pathsMap=new TreeMap<String,D>(String.CASE_INSENSITIVE_ORDER);
        for (final D d : defs)
        {
            final String    dPath=(null == d) ? null : d.getPath();
            if ((null == dPath) || (dPath.length() <= 0))
                continue;

            final D    prev=pathsMap.get(dPath);
            if (null == prev)
            {
                pathsMap.put(dPath, d);
                continue;
            }

            if (null == res)
                res = new TreeMap<String,Collection<D>>(String.CASE_INSENSITIVE_ORDER);

            Collection<D>    gd=res.get(dPath);
            if (null == gd)
            {
                gd = new LinkedList<D>();
                gd.add(prev);    // add the original instance
                res.put(dPath, gd);
            }
            gd.add(d);
        }

        return res;
    }
}
