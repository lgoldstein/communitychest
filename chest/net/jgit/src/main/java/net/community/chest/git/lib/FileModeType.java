/*
 *
 */
package net.community.chest.git.lib;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.community.chest.util.collection.CollectionsUtils;

import org.eclipse.jgit.lib.FileMode;

/**
 * <P>Copyright as per GPLv2</P>
 * Encapsulates the available {@link FileMode} values as an {@link Enum}
 * @author Lyor G.
 * @since Mar 15, 2011 10:39:56 AM
 */
public enum FileModeType {
    TREE(FileMode.TREE),
    SYMLINK(FileMode.SYMLINK),
    REGULAR(FileMode.REGULAR_FILE),
    EXECUTABLE(FileMode.EXECUTABLE_FILE),
    GITLINK(FileMode.GITLINK),
    MISSING(FileMode.MISSING);

    private final FileMode    _mode;
    public final FileMode getMode ()
    {
        return _mode;
    }

    FileModeType (FileMode mode)
    {
        _mode = mode;
    }

    public static final List<FileModeType>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static final FileModeType fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static final FileModeType fromMode (final FileMode mode)
    {
        if (mode == null)
            return null;

        for (final FileModeType val : VALUES)
        {
            final FileMode    vMode=(val == null) ? null : val.getMode();
            if ((vMode != null) && (mode.equals(vMode) || (vMode.getBits() == mode.getBits())))
                return val;
        }

        return null;
    }

    public static final FileModeType fromBits (final int bits)
    {
        return fromMode(FileMode.fromBits(bits));
    }
}
