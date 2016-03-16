package net.community.chest.net.dns;

import net.community.chest.util.compare.AbstractComparator;

/**
 * Helper class for sorting and comparing MX records
 * @author Lyor G.
 * @since 04/08/2004
 */
public class MXRecordsComparator extends AbstractComparator<SMTPMxRecord> {
    /**
     *
     */
    private static final long serialVersionUID = 3848099859569595422L;
    public MXRecordsComparator (boolean ascending)
    {
        super(SMTPMxRecord.class, !ascending);
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (SMTPMxRecord o1, SMTPMxRecord o2)
    {
        return compareComparables(o1, o2);
    }
    /*
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals (Object obj)
    {
        return (obj instanceof MXRecordsComparator);
    }
    /*
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode ()
    {
        return 0;
    }
    /**
     * The {@link java.util.Comparator}-s used to sort returned MX records according to
     * preference order.
     */
    public static final MXRecordsComparator ASCENDING=new MXRecordsComparator(true),
                                            DESCENDING=new MXRecordsComparator(false);
}
