/*
 *
 */
package net.community.apps.tools.filesync;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Represents the possible actions of files synchronization</P>
 * @author Lyor G.
 * @since Apr 5, 2009 12:02:50 PM
 */
public enum SyncAction {
    ADD,
    REMOVE,
    UPDATE;

    public static final List<SyncAction>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final SyncAction fromString (String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }
}
