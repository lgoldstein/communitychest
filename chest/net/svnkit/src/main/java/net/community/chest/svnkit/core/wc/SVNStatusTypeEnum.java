/*
 *
 */
package net.community.chest.svnkit.core.wc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.EnumUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.tmatesoft.svn.core.wc.SVNStatusType;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Encapsulates the {@link SVNStatusType} values as an {@link Enum}</P>
 *
 * @author Lyor G.
 * @since Aug 5, 2009 9:04:09 AM
 */
public enum SVNStatusTypeEnum {
    INAPPLICABLE(SVNStatusType.INAPPLICABLE),
    UNKNOWN(SVNStatusType.UNKNOWN),
    UNCHANGED(SVNStatusType.UNCHANGED),
    MISSING(SVNStatusType.MISSING),
    OBSTRUCTED(SVNStatusType.OBSTRUCTED),
    CHANGED(SVNStatusType.CHANGED),
    MERGED(SVNStatusType.MERGED),
    CONFLICTED(SVNStatusType.CONFLICTED),
    CONFLICTED_UNRESOLVED(SVNStatusType.CONFLICTED_UNRESOLVED),
    LOCK_INAPPLICABLE(SVNStatusType.LOCK_INAPPLICABLE),
    LOCK_UNKNOWN(SVNStatusType.LOCK_UNKNOWN),
    LOCK_UNCHANGED(SVNStatusType.LOCK_UNCHANGED),
    LOCK_LOCKED(SVNStatusType.LOCK_LOCKED),
    LOCK_UNLOCKED(SVNStatusType.LOCK_UNLOCKED),
    STATUS_NONE(SVNStatusType.STATUS_NONE),
    STATUS_NORMAL(SVNStatusType.STATUS_NORMAL),
    STATUS_MODIFIED(SVNStatusType.STATUS_MODIFIED),
    STATUS_ADDED(SVNStatusType.STATUS_ADDED),
    STATUS_DELETED(SVNStatusType.STATUS_DELETED),
    STATUS_UNVERSIONED(SVNStatusType.STATUS_UNVERSIONED),
    STATUS_MISSING(SVNStatusType.STATUS_MISSING),
    STATUS_REPLACED(SVNStatusType.STATUS_REPLACED),
    STATUS_CONFLICTED(SVNStatusType.STATUS_CONFLICTED),
    STATUS_OBSTRUCTED(SVNStatusType.STATUS_OBSTRUCTED),
    STATUS_IGNORED(SVNStatusType.STATUS_IGNORED),
    STATUS_INCOMPLETE(SVNStatusType.STATUS_INCOMPLETE),
    STATUS_EXTERNAL(SVNStatusType.STATUS_EXTERNAL);

    private final SVNStatusType    _t;
    public final SVNStatusType getSVNStatusType ()
    {
        return _t;
    }

    public final boolean isVersioned ()
    {
        return isVersioned(this);
    }

    SVNStatusTypeEnum (SVNStatusType t)
    {
        _t = t;
    }
    /*
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString ()
    {
        return getSVNStatusType().toString();
    }

    public static final List<SVNStatusTypeEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final SVNStatusTypeEnum fromName (final String name)
    {
        return EnumUtil.fromName(VALUES, name, false);
    }

    public static final SVNStatusTypeEnum fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final SVNStatusTypeEnum fromStatus (final SVNStatusType t)
    {
        if (null == t)
            return null;

        for (final SVNStatusTypeEnum v : VALUES)
        {
            final SVNStatusType    vt=(null == v) ? null : v.getSVNStatusType();
            if (t.equals(vt))
                return v;
        }

        return null;
    }

    public static final boolean isVersioned (final SVNStatusTypeEnum stLocal)
    {
        if ((null == stLocal)
          || SVNStatusTypeEnum.INAPPLICABLE.equals(stLocal)
          || SVNStatusTypeEnum.UNKNOWN.equals(stLocal)
          || SVNStatusTypeEnum.STATUS_NONE.equals(stLocal)
          || SVNStatusTypeEnum.STATUS_UNVERSIONED.equals(stLocal))
            return false;

        return true;

    }
}
