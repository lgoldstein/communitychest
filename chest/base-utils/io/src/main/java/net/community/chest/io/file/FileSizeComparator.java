/*
 *
 */
package net.community.chest.io.file;

import java.io.File;

import net.community.chest.lang.math.LongsComparator;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * @author Lyor G.
 * @since Aug 6, 2009 10:29:55 AM
 */
public class FileSizeComparator extends AbstractFileComparator {
    /**
     *
     */
    private static final long serialVersionUID = 1663272109234332690L;

    public FileSizeComparator (boolean ascending)
    {
        super(!ascending);
    }
    /**
     * @param f The {@link File} whose size value is requested
     * @return The file size (in bytes) - <B>Note(s):</B></BR>
     * <UL>
     *         <LI>
     *         A <code>null</code> instance size is reported as
     *         <U>negative</U>
     *         </LI>
     *
     *         <LI>
     *         If a file does not {@link File#exists()} then its size is
     *         reported as <U>{@link Long#MAX_VALUE}</U>
     *         </LI>
     *
     *         <LI>
     *      If a file is not {@link File#isFile()} then its size is
     *      reported as <U>zero</U>
     *      </LI>
     * </UL>
     */
    public static final long getFileSizeValue (final File f)
    {
        if (null == f)
            return (-1L);
        if (!f.exists())
            return Long.MAX_VALUE;
        if (!f.isFile())
            return 0L;

        return f.length();
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (File v1, File v2)
    {
        return LongsComparator.compare(getFileSizeValue(v1), getFileSizeValue(v2));
    }

    public static final FileSizeComparator    ASCENDING=new FileSizeComparator(true),
                                            DESCENDING=new FileSizeComparator(false);
}
