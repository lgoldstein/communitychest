/*
 *
 */
package net.community.chest.svnkit.core.wc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.lang.StringUtil;
import net.community.chest.util.collection.CollectionsUtils;

import org.tmatesoft.svn.core.wc.SVNEventAction;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Encapsulates pre-defined {@link SVNEventAction} as {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Aug 24, 2010 9:37:57 AM
 */
public enum SVNEventActionEnum {
    PROGRESS(SVNEventAction.PROGRESS),
    ADD(SVNEventAction.ADD),
    COPY(SVNEventAction.COPY),
    DELETE(SVNEventAction.DELETE),
    RESTORE(SVNEventAction.RESTORE),
    REVERT(SVNEventAction.REVERT),
    FAILED_REVERT(SVNEventAction.FAILED_REVERT),
    RESOLVED(SVNEventAction.RESOLVED),
    SKIP(SVNEventAction.SKIP),
    UPDATE_DELETE(SVNEventAction.UPDATE_DELETE),
    UPDATE_ADD(SVNEventAction.UPDATE_ADD),
    UPDATE_UPDATE(SVNEventAction.UPDATE_UPDATE),
    UPDATE_NONE(SVNEventAction.UPDATE_NONE),
    UPDATE_COMPLETED(SVNEventAction.UPDATE_COMPLETED),
    UPDATE_EXTERNAL(SVNEventAction.UPDATE_EXTERNAL),
    STATUS_COMPLETED(SVNEventAction.STATUS_COMPLETED),
    STATUS_EXTERNAL(SVNEventAction.STATUS_EXTERNAL),
    COMMIT_MODIFIED(SVNEventAction.COMMIT_MODIFIED),
    COMMIT_ADDED(SVNEventAction.COMMIT_ADDED),
    COMMIT_DELETED(SVNEventAction.COMMIT_DELETED),
    COMMIT_REPLACED(SVNEventAction.COMMIT_REPLACED),
    COMMIT_DELTA_SENT(SVNEventAction.COMMIT_DELTA_SENT),
    COMMIT_COMPLETED(SVNEventAction.COMMIT_COMPLETED),
    ANNOTATE(SVNEventAction.ANNOTATE),
    LOCKED(SVNEventAction.LOCKED),
    UNLOCKED(SVNEventAction.UNLOCKED),
    LOCK_FAILED(SVNEventAction.LOCK_FAILED),
    UNLOCK_FAILED(SVNEventAction.UNLOCK_FAILED),
    UPGRADE(SVNEventAction.UPGRADE),
    UPDATE_EXISTS(SVNEventAction.UPDATE_EXISTS),
    CHANGELIST_SET(SVNEventAction.CHANGELIST_SET),
    CHANGELIST_CLEAR(SVNEventAction.CHANGELIST_CLEAR),
    CHANGELIST_MOVED(SVNEventAction.CHANGELIST_MOVED),
    MERGE_BEGIN(SVNEventAction.MERGE_BEGIN),
    FOREIGN_MERGE_BEGIN(SVNEventAction.FOREIGN_MERGE_BEGIN),
    UPDATE_REPLACE(SVNEventAction.UPDATE_REPLACE),
    PROPERTY_ADD(SVNEventAction.PROPERTY_ADD),
    PROPERTY_MODIFY(SVNEventAction.PROPERTY_MODIFY),
    PROPERTY_DELETE(SVNEventAction.PROPERTY_DELETE),
    PROPERTY_DELETE_NONEXISTENT(SVNEventAction.PROPERTY_DELETE_NONEXISTENT),
    REVPROPER_SET(SVNEventAction.REVPROPER_SET),
    REVPROP_DELETE(SVNEventAction.REVPROP_DELETE),
    MERGE_COMPLETE(SVNEventAction.MERGE_COMPLETE),
    TREE_CONFLICT(SVNEventAction.TREE_CONFLICT),
    FAILED_EXTERNAL(SVNEventAction.FAILED_EXTERNAL);

    private final SVNEventAction    _action;
    public final SVNEventAction getAction ()
    {
        return _action;
    }

    SVNEventActionEnum (SVNEventAction a)
    {
        _action = a;
    }

    public static final List<SVNEventActionEnum>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final SVNEventActionEnum fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final SVNEventActionEnum fromAction (final SVNEventAction a)
    {
        if (null == a)
            return null;

        for (final SVNEventActionEnum v : VALUES)
        {
            final SVNEventAction    va=(null == v) ? null : v.getAction();
            if ((va != null) && a.equals(va))
                return v;
        }

        return null;
    }

    public static final SVNEventActionEnum fromActionId (final int id)
    {
        for (final SVNEventActionEnum v : VALUES)
        {
            final SVNEventAction    va=(null == v) ? null : v.getAction();
            final int                vv=(null == va) ? Integer.MIN_VALUE : va.getID();
            if ((va != null) && (vv == id))
                return v;
        }

        return null;
    }

    public static final SVNEventActionEnum fromActionString (final String s, final boolean caseSensitive)
    {
        if ((null == s) || (s.length() <= 0))
            return null;

        for (final SVNEventActionEnum v : VALUES)
        {
            final SVNEventAction    va=(null == v) ? null : v.getAction();
            final String            vs=(null == va) ? null : va.toString();
            if (0 == StringUtil.compareDataStrings(s, vs, caseSensitive))
                return v;
        }

        return null;
    }
 }
