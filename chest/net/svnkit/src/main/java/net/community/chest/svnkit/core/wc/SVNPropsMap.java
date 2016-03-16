/*
 *
 */
package net.community.chest.svnkit.core.wc;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.wc.ISVNPropertyHandler;
import org.tmatesoft.svn.core.wc.SVNPropertyData;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.core.wc.SVNWCClient;

/**
 * <P>Copyright as per GPLv2</P>
 * <P>Provides a {@link Map} whose key=property name, value=the associated {@link SVNPropertyValue}. It
 * also implements {@link ISVNPropertyHandler} so it can be used in various SVNKit calls</P>
 *
 * @author Lyor G.
 * @since Oct 27, 2010 10:06:40 AM
 *
 */
public class SVNPropsMap extends TreeMap<String,SVNPropertyValue> implements ISVNPropertyHandler {
    /**
     *
     */
    private static final long serialVersionUID = -8721186658329845242L;

    public SVNPropsMap ()
    {
        super();
    }

    public SVNPropsMap (Comparator<? super String> comparator)
    {
        super(comparator);
    }

    public SVNPropsMap (Map<? extends String,? extends SVNPropertyValue> m)
    {
        super(m);
    }

    public SVNPropertyValue putProperty (SVNPropertyData property)
    {
        final String            name=(property == null) ? null : property.getName();
        final SVNPropertyValue    value=(property == null) ? null : property.getValue();
        return put(name, value);
    }

    public SVNPropertyValue removeProperty (SVNPropertyData property)
    {
        return remove((property == null) ? null : property.getName());
    }

    public SVNPropertyValue getProperty (SVNPropertyData property)
    {
        return get((property == null) ? null : property.getName());
    }
    /*
     * @see org.tmatesoft.svn.core.wc.ISVNPropertyHandler#handleProperty(java.io.File, org.tmatesoft.svn.core.wc.SVNPropertyData)
     */
    @Override
    public void handleProperty (File path, SVNPropertyData property)
            throws SVNException
    {
        putProperty(property);
    }
    /*
     * @see org.tmatesoft.svn.core.wc.ISVNPropertyHandler#handleProperty(org.tmatesoft.svn.core.SVNURL, org.tmatesoft.svn.core.wc.SVNPropertyData)
     */
    @Override
    public void handleProperty (SVNURL url, SVNPropertyData property)
            throws SVNException
    {
        putProperty(property);
    }
    /*
     * @see org.tmatesoft.svn.core.wc.ISVNPropertyHandler#handleProperty(long, org.tmatesoft.svn.core.wc.SVNPropertyData)
     */
    @Override
    public void handleProperty (long revision, SVNPropertyData property)
            throws SVNException
    {
        putProperty(property);
    }
    // NOTE: uses same comparator as this map
    public Map<String,String> toStringsMap ()
    {
        if (size() <= 0)
            return null;

        final Map<String,String>    ret=new TreeMap<String,String>(comparator());
        for (final Map.Entry<String,? extends SVNPropertyValue> ve : entrySet())
        {
            final SVNPropertyValue    vv=ve.getValue();
            final String            kv=ve.getKey(), vs=SVNPropertyValue.getPropertyAsString(vv);
            if (ret.put(kv, vs) != null)
                continue;    // TODO consider throwing an IllegalStateException
        }

        return ret;
    }

    public static final SVNPropsMap getSVNProperties (final SVNWCClient wcc, final File f)
        throws SVNException
    {
        return getSVNProperties(wcc, f, new SVNPropsMap());
    }

    public static final <M extends SVNPropsMap> M getSVNProperties (
            final SVNWCClient wcc, final File f, final M propsMap)
        throws SVNException
    {
        wcc.doGetProperty(f, null, SVNRevision.WORKING, SVNRevision.WORKING, SVNDepth.EMPTY, propsMap, Collections.emptyList());
        return propsMap;
    }

    public static final SVNPropsMap getSVNProperties (final SVNWCClient wcc, final SVNURL url)
        throws SVNException
    {
        return getSVNProperties(wcc, url, new SVNPropsMap());
    }

    public static final <M extends SVNPropsMap> M getSVNProperties (
            final SVNWCClient wcc, final SVNURL url, final M propsMap)
        throws SVNException
    {
        wcc.doGetProperty(url, null, SVNRevision.WORKING, SVNRevision.WORKING, SVNDepth.EMPTY, propsMap);
        return propsMap;
    }
}
