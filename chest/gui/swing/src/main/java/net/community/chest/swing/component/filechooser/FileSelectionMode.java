package net.community.chest.swing.component.filechooser;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;

import net.community.chest.util.collection.CollectionsUtils;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to convert between <code>int</code> values and {@link Enum}-s</P>
 *
 * @author Lyor G.
 * @since Jul 26, 2007 1:29:38 PM
 */
public enum FileSelectionMode {
    FILES(JFileChooser.FILES_ONLY),
    FOLDERS(JFileChooser.DIRECTORIES_ONLY),
    ALL(JFileChooser.FILES_AND_DIRECTORIES);

    private final int    _mode;
    public int getModeValue ()
    {
        return _mode;
    }

    FileSelectionMode (final int m)
    {
        _mode = m;
    }

    public static final List<FileSelectionMode>    VALUES=Collections.unmodifiableList(Arrays.asList(values()));
    public static FileSelectionMode fromString (final String s)
    {
        return CollectionsUtils.fromString(VALUES, s, false);
    }

    public static FileSelectionMode fromModeValue (final int m)
    {
        for (final FileSelectionMode v : VALUES)
        {
            if ((v != null) && (v.getModeValue() == m))
                return v;
        }

        return null;
    }
}
