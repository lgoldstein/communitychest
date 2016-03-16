/*
 *
 */
package net.community.chest.awt.dnd;

import java.awt.dnd.DnDConstants;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright GPLv2</P>
 *
 * <P>Encapsulate the {@link DnDConstants} as {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Mar 10, 2009 9:10:26 AM
 *
 */
public enum DnDActionType {
    NONE(DnDConstants.ACTION_NONE),
    COPY(DnDConstants.ACTION_COPY),
    MOVE(DnDConstants.ACTION_MOVE),
    CPYORMOV(DnDConstants.ACTION_COPY_OR_MOVE),
    LINK(DnDConstants.ACTION_LINK),
    // NOTE !!! ACTION_REFERENCE is same as ACTION_LINK
    REFERENCE(DnDConstants.ACTION_REFERENCE);

    private final int    _a;
    public final int getAction ()
    {
        return _a;
    }

    DnDActionType (int a)
    {
        _a = a;
    }

    public static final List<DnDActionType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final DnDActionType fromString (String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }
    // NOTE !!! ACTION_REFERENCE is same as ACTION_LINK either can be returned
    public static final DnDActionType fromAction (final int a)
    {
        for (final DnDActionType v : VALUES)
        {
            if ((v != null) && (v.getAction() == a))
                return v;
        }

        return null;
    }
}
