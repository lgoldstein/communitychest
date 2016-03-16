/*
 *
 */
package net.community.apps.tools.jardiff;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Mar 2, 2011 12:07:21 PM
 */
public enum JarEntriesTableColumns {
    ENTRY_PATH,
    ENTRY_NAME,
    ENTRY_SIZE,
    ENTRY_TIME;

    public static final List<JarEntriesTableColumns>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static JarEntriesTableColumns fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }
}
