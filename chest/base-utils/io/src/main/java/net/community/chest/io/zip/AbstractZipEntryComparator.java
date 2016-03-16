/*
 *
 */
package net.community.chest.io.zip;

import java.util.zip.ZipEntry;

import net.community.chest.lang.StringUtil;
import net.community.chest.lang.math.LongsComparator;
import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright as per GPLv2</P>
 * @param <T> Type of {@link ZipEntry} being compared
 * @author Lyor G.
 * @since Mar 7, 2011 1:27:41 PM
 */
public abstract class AbstractZipEntryComparator<T extends ZipEntry> extends AbstractComparator<T> {
    /**
     *
     */
    private static final long serialVersionUID = -2359809209524362466L;

    protected AbstractZipEntryComparator (Class<T> valsClass, boolean reverseMatch)
            throws IllegalArgumentException
    {
        super(valsClass, reverseMatch);
    }

    public static final int compareByName (ZipEntry v1, ZipEntry v2, boolean caseSensitive)
    {
        final String    n1=(v1 == null) ? null : v1.getName(),
                        n2=(v2 == null) ? null : v2.getName();
        return StringUtil.compareDataStrings(n1, n2, caseSensitive);
    }

    public static final int compareBySize (ZipEntry v1, ZipEntry v2)
    {
        final long    s1=(v1 == null) ? (-1L) : v1.getSize(),
                    s2=(v2 == null) ? (-1L) : v2.getSize();
        return LongsComparator.compare(s1, s2);
    }

    public static final int compareByComment (ZipEntry v1, ZipEntry v2, boolean caseSensitive)
    {
        final String    n1=(v1 == null) ? null : v1.getComment(),
                        n2=(v2 == null) ? null : v2.getComment();
        return StringUtil.compareDataStrings(n1, n2, caseSensitive);
    }
}
