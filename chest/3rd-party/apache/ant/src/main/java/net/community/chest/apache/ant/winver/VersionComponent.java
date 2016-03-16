/*
 *
 */
package net.community.chest.apache.ant.winver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright as per GPLv2</P>
 *
 * <P>Represents the 4 standard Windows version numbers</P>
 *
 * @author Lyor G.
 * @since Jul 7, 2009 10:30:58 AM
 */
public enum VersionComponent {
    // NOTE !!! order is important
    MAJOR,
    MINOR,
    RELEASE,
    BUILD;

    public static final List<VersionComponent>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final VersionComponent fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final boolean isValidComponentNumber (final int n)
    {
        return (n >= 0) && (n < 0x0FFFF);
    }

    public static final boolean isValidComponentNumber (final Number n)
    {
        return (n != null) && isValidComponentNumber(n.intValue());
    }
}
