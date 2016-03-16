/*
 *
 */
package net.community.chest.io.file;

import java.io.File;

/**
 * <P>Copyright as per GPLv2</P>
 * @author Lyor G.
 * @since Nov 11, 2010 9:29:53 AM
 *
 */
public class FileAttributeValueComparator extends AbstractFileComparator {
    /**
     *
     */
    private static final long serialVersionUID = -5430154071838919239L;
    private final FileAttributeType    _aType;
    public final FileAttributeType getComparedAttribute ()
    {
        return _aType;
    }

    public FileAttributeValueComparator (final FileAttributeType aType, final boolean reverseMatch)
        throws IllegalStateException
    {
        super(reverseMatch);

        if ((_aType=aType) == null)
            throw new IllegalStateException("No file attribute provided");
    }
    /*
     * @see net.community.chest.util.compare.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })    // we know both values are of same type since extracted by the same attribute
    @Override
    public int compareValues (File f1, File f2)
    {
        final FileAttributeType    aType=getComparedAttribute();
        final Comparable<?>     v1=(f1 == null) ? null : aType.getValue(f1),
                                v2=(f2 == null) ? null : aType.getValue(f2);
        return compareComparables((Comparable) v1,  (Comparable) v2);
    }
}
