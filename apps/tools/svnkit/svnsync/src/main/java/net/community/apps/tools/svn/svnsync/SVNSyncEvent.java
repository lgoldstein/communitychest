/*
 *
 */
package net.community.apps.tools.svn.svnsync;

import java.util.Map;

import net.community.chest.Triplet;
import net.community.chest.svnkit.SVNLocation;
import net.community.chest.svnkit.core.wc.SVNEventActionEnum;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Aug 19, 2010 12:52:45 PM
 *
 */
public class SVNSyncEvent extends Triplet<SVNLocation,SVNLocation,SVNEventActionEnum> {
    private Throwable    _actionError;
    public Throwable getActionError ()
    {
        return _actionError;
    }

    public void setActionError (Throwable actionError)
    {
        _actionError = actionError;
    }

    public boolean isActionError ()
    {
        return getActionError() != null;
    }

    private Map<String,String>    _addProps, _delProps, _updProps;
    public Map<String,String> getAddedProperties ()
    {
        return _addProps;
    }

    public void setAddedProperties (Map<String,String> addProps)
    {
        _addProps = addProps;
    }

    public Map<String,String> getDeletedProperties ()
    {
        return _delProps;
    }

    public void setDeletedProperties (Map<String,String> delProps)
    {
        _delProps = delProps;
    }

    public Map<String,String> getUpdatedProperties ()
    {
        return _updProps;
    }

    public void setUpdatedProperties (Map<String,String> updProps)
    {
        _updProps = updProps;
    }

    public SVNSyncEvent (SVNLocation v1, SVNLocation v2, SVNEventActionEnum v3, Throwable err, Map<String,String> addProps, Map<String,String> delProps, Map<String,String> updProps)
    {
        super(v1, v2, v3);
        _actionError = err;
        _addProps = addProps;
        _delProps = delProps;
        _updProps = updProps;
    }

    public SVNSyncEvent (SVNLocation v1, SVNLocation v2, SVNEventActionEnum v3, Map<String,String> addProps, Map<String,String> delProps, Map<String,String> updProps)
    {
        this(v1, v2, v3, null, addProps, delProps, updProps);
    }

    public SVNSyncEvent (SVNLocation v1, SVNLocation v2, SVNEventActionEnum v3, Throwable err)
    {
        this(v1, v2, v3, err, null, null, null);
    }

    public SVNSyncEvent (SVNLocation v1, SVNLocation v2, SVNEventActionEnum v3)
    {
        this(v1, v2, v3, null);
    }

    public SVNSyncEvent ()
    {
        this(null, null, (SVNEventActionEnum) null);
    }

    public SVNLocation getSourceFile ()
    {
        return getV1();
    }

    public void setSourceFile (SVNLocation f)
    {
        setV1(f);
    }

    public SVNLocation getTargetFile ()
    {
        return getV2();
    }

    public void setTargetFile (SVNLocation f)
    {
        setV2(f);
    }

    public SVNEventActionEnum getSyncAction ()
    {
        return getV3();
    }

    public void setSyncAction (SVNEventActionEnum a)
    {
        setV3(a);
    }
    /*
     * @see net.community.chest.Triplet#isEmpty()
     */
    @Override
    public boolean isEmpty ()
    {
        if (!super.isEmpty())
            return false;

        return (null == getActionError());
    }
    /*
     * @see net.community.chest.Triplet#clear()
     */
    @Override
    public void clear ()
    {
        super.clear();
        setActionError(null);
    }
    /*
     * @see net.community.chest.Triplet#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        final Class<?>    oc=(obj == null) ? null : obj.getClass();
        if (oc != getClass())
            return false;
        if (this == obj)
            return true;

        if (this == obj)
            return true;

        final SVNSyncEvent    oe=(SVNSyncEvent) obj;
        if (!equals(oe.getV1(), oe.getV2(), oe.getV3()))
            return false;
        if (isActionError() != oe.isActionError())
            return false;

        return true;
    }
    /*
     * @see net.community.chest.Triplet#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return super.hashCode()
            + (isActionError() ? 1 : 0)
            ;
    }
    /*
     * @see net.community.chest.Triplet#toString()
     */
    @Override
    public String toString ()
    {
        final Throwable    t=getActionError();
        if (null == t)
            return super.toString();

        return super.toString()
            + " => " + t.getClass().getName()
            + ": " + t.getMessage()
            ;
    }
}
