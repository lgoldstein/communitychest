/*
 *
 */
package net.community.chest.io.file;

import java.io.File;

import net.community.chest.lang.StringUtil;

/**
 * <P>Copyright 2009 as per GPLv2</P>
 *
 * <P>Compares the {@link File#getName()} values (case <U>insensitive</U>)</P>
 * @author Lyor G.
 * @since Aug 6, 2009 10:28:07 AM
 */
public class FileNameComparator extends AbstractFileComparator {
    /**
     *
     */
    private static final long serialVersionUID = 7370998496469172690L;

    public FileNameComparator (boolean ascending)
    {
        super(!ascending);
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (File v1, File v2)
    {
        final String    p1=(null == v1) ? null : v1.getName(),
                        p2=(null == v2) ? null : v2.getName();
        return StringUtil.compareDataStrings(p1, p2, true);
    }

    public static final FileNameComparator    ASCENDING=new FileNameComparator(true),
                                            DESCENDING=new FileNameComparator(false);
}
