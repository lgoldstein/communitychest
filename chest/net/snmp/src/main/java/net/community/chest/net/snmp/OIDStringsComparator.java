package net.community.chest.net.snmp;

import net.community.chest.util.compare.AbstractComparator;

/**
 * <P>Copyright 2007 as per GPLv2</P>
 *
 * <P>Used to sort OID values according to their correct order</P>
 *
 * @author Lyor G.
 * @since Oct 18, 2007 12:47:51 PM
 */
public class OIDStringsComparator extends AbstractComparator<String> {
    /**
     *
     */
    private static final long serialVersionUID = -3551993827685885485L;

    public OIDStringsComparator (boolean ascending)
    {
        super(String.class, !ascending);
    }
    /*
     * @see net.community.chest.util.AbstractComparator#compareValues(java.lang.Object, java.lang.Object)
     */
    @Override
    public int compareValues (String o1, String o2)
    {
        return SNMPProtocol.compareOIDs(o1, o2);
    }

    public static final OIDStringsComparator    ASCENDING=new OIDStringsComparator(true),
                                                DESCENDING=new OIDStringsComparator(false);
}
