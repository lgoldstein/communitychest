/*
 *
 */
package net.community.chest.swing;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.DropMode;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2008 as per GPLv2</P>
 *
 * <P>Provides some useful methods for {@link DropMode}-s
 * @author Lyor G.
 * @since Aug 20, 2008 9:36:24 AM
 */
public final class DropModeValue {
    private DropModeValue ()
    {
        // no instance
    }

    public static final List<DropMode>    VALUES=Collections.unmodifiableList(Arrays.asList(DropMode.values()));
    public static final DropMode fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }
}
